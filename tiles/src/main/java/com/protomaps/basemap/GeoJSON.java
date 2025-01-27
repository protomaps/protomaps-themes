package com.protomaps.basemap;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.onthegomap.planetiler.geo.GeoUtils;
import org.locationtech.jts.geom.Coordinate;
import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

public class GeoJSON {
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

  private static Polygon coordsToPolygon(JsonNode coords) {
    ArrayNode outerRingNode = (ArrayNode) coords.get(0);
    Coordinate[] outerRingCoordinates = parseCoordinates(outerRingNode);
    LinearRing outerRing = GeoUtils.JTS_FACTORY.createLinearRing(outerRingCoordinates);

    LinearRing[] innerRings = new LinearRing[coords.size() - 1];
    for (int j = 1; j < coords.size(); j++) {
      ArrayNode innerRingNode = (ArrayNode) coords.get(j);
      Coordinate[] innerRingCoordinates = parseCoordinates(innerRingNode);
      innerRings[j - 1] = GeoUtils.JTS_FACTORY.createLinearRing(innerRingCoordinates);
    }
    return GeoUtils.JTS_FACTORY.createPolygon(outerRing, innerRings);
  }

  // return a Polygon or MultiPolygon from a GeoJSON geometry object.
  public static Geometry parseGeometry(JsonNode jsonGeometry) {
    var coords = jsonGeometry.get("coordinates");
    if (jsonGeometry.get("type").asText().equals("Polygon")) {
      return coordsToPolygon(coords);
    } else if (jsonGeometry.get("type").asText().equals("MultiPolygon")) {
      List<Polygon> polygons = new ArrayList<>();
      for (var polygonCoords : coords) {
        polygons.add(coordsToPolygon(polygonCoords));
      }
      return GeoUtils.createMultiPolygon(polygons);
    } else {
      throw new IllegalArgumentException();
    }
  }
}
