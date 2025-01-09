package com.protomaps.basemap.postprocess;

import static com.onthegomap.planetiler.geo.GeoUtils.WORLD_BOUNDS;
import static com.onthegomap.planetiler.geo.GeoUtils.latLonToWorldCoords;
import static com.onthegomap.planetiler.render.TiledGeometry.getCoveredTiles;
import static com.onthegomap.planetiler.render.TiledGeometry.sliceIntoTiles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.*;
import com.onthegomap.planetiler.render.TiledGeometry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import com.onthegomap.planetiler.stats.Stats;
import com.protomaps.basemap.GeoJSON;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;

public class Clip implements ForwardingProfile.TilePostProcessor {
  private final Map<Integer, Map<TileCoord, List<List<CoordinateSequence>>>> tiledGeometries;
  private final Map<Integer, TiledGeometry.CoveredTiles> coverings;
  private final Stats stats;

  public Clip(Stats stats, Geometry input) {
    this.stats = stats;
    var clipGeometry = latLonToWorldCoords(input).buffer(0.00001);
    tiledGeometries = new HashMap<>();
    coverings = new HashMap<>();
    try {
      for (var i = 0; i <= 15; i++) {
        var extents = TileExtents.computeFromWorldBounds(i, WORLD_BOUNDS);
        double scale = 1 << i;
        Geometry scaled = AffineTransformation.scaleInstance(scale, scale).transform(clipGeometry);
//        var simplified = DouglasPeuckerSimplifier.simplify(scaled, 0.25/256);
        this.tiledGeometries.put(i, sliceIntoTiles(scaled, 0, 0.015625, i, extents.getForZoom(i)).getTileData());
        this.coverings.put(i, getCoveredTiles(scaled, i, extents.getForZoom(i)));
      }
    } catch (GeometryException e) {
      throw new RuntimeException("Error clipping");
    }
  }

  public static Clip fromGeoJSONFile(Stats stats, String filename) {
    try {
      return fromGeoJSON(stats, Files.readAllBytes(Paths.get(filename)));
    } catch (IOException e) {
      throw new IllegalArgumentException("Could not open clip file");
    }
  }

  public static Clip fromGeoJSON(Stats stats, byte[] bytes) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode geoJson = mapper.readTree(bytes);
      return new Clip(stats, GeoJSON.parseGeometry(geoJson));
    } catch (IOException e) {
      throw new IllegalArgumentException("Clip GeoJSON is invalid");
    }
  }

  // Copied from elsewhere in planetiler
  private static Polygon reassemblePolygon(List<CoordinateSequence> group) throws GeometryException {
    try {
      LinearRing first = GeoUtils.JTS_FACTORY.createLinearRing(group.getFirst());
      LinearRing[] rest = new LinearRing[group.size() - 1];
      for (int j = 1; j < group.size(); j++) {
        CoordinateSequence seq = group.get(j);
        CoordinateSequences.reverse(seq);
        rest[j - 1] = GeoUtils.JTS_FACTORY.createLinearRing(seq);
      }
      return GeoUtils.JTS_FACTORY.createPolygon(first, rest);
    } catch (IllegalArgumentException e) {
      throw new GeometryException("reassemble_polygon_failed", "Could not build polygon", e);
    }
  }

  // Copied from elsewhere in Planetiler
  static Geometry reassemblePolygons(List<List<CoordinateSequence>> groups) throws GeometryException {
    int numGeoms = groups.size();
    if (numGeoms == 1) {
      return reassemblePolygon(groups.getFirst());
    } else {
      Polygon[] polygons = new Polygon[numGeoms];
      for (int i = 0; i < numGeoms; i++) {
        polygons[i] = reassemblePolygon(groups.get(i));
      }
      return GeoUtils.JTS_FACTORY.createMultiPolygon(polygons);
    }
  }

  @Override
  public Map<String, List<VectorTile.Feature>> postProcessTile(TileCoord tileCoord,
    Map<String, List<VectorTile.Feature>> map) throws GeometryException {
    if (this.coverings.containsKey(tileCoord.z()) &&
      this.coverings.get(tileCoord.z()).test(tileCoord.x(), tileCoord.y())) {
      if (this.tiledGeometries.containsKey(tileCoord.z()) && this.tiledGeometries.get(tileCoord.z()).containsKey(tileCoord)) {
        List<List<CoordinateSequence>> coords = tiledGeometries.get(tileCoord.z()).get(tileCoord);
        var clipGeometry = reassemblePolygons(coords);
        var clipGeometry2 = GeoUtils.fixPolygon(clipGeometry);
        clipGeometry2.reverse();
        Map<String, List<VectorTile.Feature>> output = new HashMap<>();

        for (Map.Entry<String, List<VectorTile.Feature>> layer : map.entrySet()) {
          List<VectorTile.Feature> clippedFeatures = new ArrayList<>();
          for (var feature : layer.getValue()) {
            try {
              var newGeom = OverlayNGRobust.overlay(feature.geometry().decode(), clipGeometry2, OverlayNG.INTERSECTION);
              if (!newGeom.isEmpty() && newGeom.getNumGeometries() > 0) {
                if (newGeom instanceof Polygonal) {
                  newGeom = GeoUtils.snapAndFixPolygon(newGeom, stats, "clip");
                  newGeom = newGeom.reverse();
                  if (!newGeom.isEmpty() && newGeom.getNumGeometries() > 0) {
                    if (newGeom instanceof GeometryCollection) {
                      for (int i = 0; i < newGeom.getNumGeometries(); i++) {
                        // geometrycollection
                        clippedFeatures.add(feature.copyWithNewGeometry(newGeom.getGeometryN(i)));
                      }
                    } else {
                      // a multipolygon/polygon
                      clippedFeatures.add(feature.copyWithNewGeometry(newGeom));
                    }
                  }
                } else {
                  if (!newGeom.isEmpty() && newGeom.getNumGeometries() > 0) {
                    if (newGeom instanceof GeometryCollection) {
                      for (int i = 0; i < newGeom.getNumGeometries(); i++) {
                        clippedFeatures.add(feature.copyWithNewGeometry(newGeom.getGeometryN(i)));
                      }
                    } else {
                      clippedFeatures.add(feature.copyWithNewGeometry(newGeom));
                    }
                  }
                }
              }
            } catch (GeometryException e) {
              System.err.println("Could not clip geometry");
            }
          }

          output.put(layer.getKey(), clippedFeatures);
        }
        return output;
      }
      return map;
    }
    return Map.of();
  }
}
