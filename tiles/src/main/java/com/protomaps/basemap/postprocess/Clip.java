package com.protomaps.basemap.postprocess;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.config.Bounds;
import com.onthegomap.planetiler.geo.*;
import com.onthegomap.planetiler.render.TiledGeometry;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.util.AffineTransformation;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.onthegomap.planetiler.geo.GeoUtils.WORLD_BOUNDS;
import static com.onthegomap.planetiler.geo.GeoUtils.latLonToWorldCoords;
import static com.onthegomap.planetiler.render.TiledGeometry.sliceIntoTiles;

public class Clip implements ForwardingProfile.TilePostProcessor {

  private final Geometry clipGeometry;

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

  public Clip(Geometry clipGeometry) {
    this.clipGeometry = latLonToWorldCoords(clipGeometry);
    // clip it into all tiles
    System.out.println(this.clipGeometry);
//    var extents = Bounds.WORLD.tileExtents();
    double scale = 1 << 15;
    Geometry scaled = AffineTransformation.scaleInstance(scale, scale).transform(this.clipGeometry);
    TiledGeometry sliced;
//    Geometry geom = DouglasPeuckerSimplifier.simplify(scaled, tolerance)
    var extents = TileExtents.computeFromWorldBounds(15, WORLD_BOUNDS);
    System.out.println(extents.getForZoom((15)));
    try {
      var result = sliceIntoTiles(scaled, 0, 0, 15, extents.getForZoom(15));
      System.out.println(result.getTileData());
    } catch (GeometryException e) {
      System.err.println("Error clipping mask");
    }
  }

  // turn the input geometry into a bitmap
  // must be a GeoJSON Polygon or MultiPolygon
  public static Clip fromGeoJSON() {
    Geometry clipGeometry;
    try {
      var s = "{\"coordinates\": [[[7.4160,43.7252],[7.4215,43.7252],[7.4215,43.7294],[7.4160,43.7294],[7.4160,43.7252]]],\"type\": \"Polygon\"}";
      ObjectMapper mapper = new ObjectMapper();
      JsonNode geoJson = mapper.readTree(s);
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

  @Override
  public Map<String, List<VectorTile.Feature>> postProcessTile(TileCoord tileCoord, Map<String, List<VectorTile.Feature>> map) throws GeometryException {
    System.out.println(tileCoord);
    Map<String, List<VectorTile.Feature>> output = new HashMap<>();

//    var extent = new TileExtents.ForZoom(tileCoord.z(), tileCoord.x(), tileCoord.y(), tileCoord.x(), tileCoord.y(), null);
//    System.out.println(clipGeometry);
//    var result = sliceIntoTiles(clipGeometry, 0, 0.25, tileCoord.z(),TileExtents.getForZoom(tileCoord.z()));
//    System.out.println(result.getTileData());

    for (Map.Entry<String, List<VectorTile.Feature>> layer : map.entrySet()) {
      List<VectorTile.Feature> clippedFeatures = layer.getValue().stream().map(f -> {
        try {
          //System.out.println(f.geometry().decode());
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
    //System.out.println(output);
    return output;
  }
}
