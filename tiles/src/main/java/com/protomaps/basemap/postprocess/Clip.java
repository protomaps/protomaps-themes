package com.protomaps.basemap.postprocess;

import static com.onthegomap.planetiler.geo.GeoUtils.WORLD_BOUNDS;
import static com.onthegomap.planetiler.geo.GeoUtils.latLonToWorldCoords;
import static com.onthegomap.planetiler.render.TiledGeometry.getCoveredTiles;
import static com.onthegomap.planetiler.render.TiledGeometry.sliceIntoTiles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.*;
import com.onthegomap.planetiler.render.TiledGeometry;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.AffineTransformation;

public class Clip implements ForwardingProfile.TilePostProcessor {

  private final Map<Integer, Map<TileCoord, List<List<CoordinateSequence>>>> data;
  private final Map<Integer, TiledGeometry.CoveredTiles> coverings;

  private static Coordinate[] parseCoordinates(ArrayNode coordinateArray) {
    Coordinate[] coordinates = new Coordinate[coordinateArray.size()];
    for (int i = 0; i < coordinateArray.size(); i++) {
      ArrayNode coordinate = (ArrayNode) coordinateArray.get(i);
      double x = coordinate.get(0).asDouble();
      double y = coordinate.get(1).asDouble();
      coordinates[i] = new Coordinate(x, y);
    }
    return coordinates;
  }

  public Clip(Geometry input) {
    var clipGeometry = latLonToWorldCoords(input);
    data = new HashMap<>();
    coverings = new HashMap<>();
    try {
      for (var i = 0; i <= 15; i++) {
        var extents = TileExtents.computeFromWorldBounds(i, WORLD_BOUNDS);
        double scale = 1 << i;
        Geometry scaled = AffineTransformation.scaleInstance(scale, scale).transform(clipGeometry);
        this.data.put(i, sliceIntoTiles(scaled, 0, 0.015625, i, extents.getForZoom(i)).getTileData());
        this.coverings.put(i, getCoveredTiles(scaled, i, extents.getForZoom(i)));
      }
    } catch (GeometryException e) {
      throw new RuntimeException("Error clipping");
    }
  }

  // turn the input geometry into a bitmap
  // must be a GeoJSON Polygon or MultiPolygon
  public static Clip fromGeoJSON(byte[] bytes) {
    Geometry clipGeometry;
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode geoJson = mapper.readTree(bytes);
      if (geoJson.get("type").asText().equals("Polygon")) {
        var coords = geoJson.get("coordinates");
        ArrayNode outerRingNode = (ArrayNode) coords.get(0);
        Coordinate[] outerRingCoordinates = parseCoordinates(outerRingNode);
        LinearRing outerRing = GeoUtils.JTS_FACTORY.createLinearRing(outerRingCoordinates);

        LinearRing[] innerRings = new LinearRing[coords.size() - 1];
        for (int j = 1; j < coords.size(); j++) {
          ArrayNode innerRingNode = (ArrayNode) coords.get(j);
          Coordinate[] innerRingCoordinates = parseCoordinates(innerRingNode);
          innerRings[j - 1] = GeoUtils.JTS_FACTORY.createLinearRing(innerRingCoordinates);
        }

        clipGeometry = (GeoUtils.JTS_FACTORY.createPolygon(outerRing, innerRings));
        return new Clip(clipGeometry);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    throw new RuntimeException();
  }

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
      if (this.data.containsKey(tileCoord.z()) && this.data.get(tileCoord.z()).containsKey(tileCoord)) {
        List<List<CoordinateSequence>> coords = data.get(tileCoord.z()).get(tileCoord);
        var clipGeometry = reassemblePolygons(coords);
        Map<String, List<VectorTile.Feature>> output = new HashMap<>();
        for (Map.Entry<String, List<VectorTile.Feature>> layer : map.entrySet()) {
          List<VectorTile.Feature> clippedFeatures = layer.getValue().stream().map(f -> {
            try {
              var newGeom = f.geometry().decode().intersection(clipGeometry);
              if (!newGeom.isEmpty()) {
                return f.copyWithNewGeometry(newGeom);
              }
            } catch (GeometryException e) {
              System.err.println("Could not clip geometry");
            }
            return null;
          }).filter(Objects::nonNull).toList();

          output.put(layer.getKey(), clippedFeatures);
        }
        return output;
      }
      return map;
    }
    return Map.of();
  }
}
