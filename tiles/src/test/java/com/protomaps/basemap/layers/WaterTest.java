package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.*;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WaterTest extends LayerTest {
  @Test
  void preparedOsm() {
    assertFeatures(15,
      List.of(Map.of("_id", 0L, "kind", "ocean")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of()),
        "osm_water",
        null,
        1
      )));
  }

  @Test
  void simple() {
    assertFeatures(15,
      List.of(Map.of("kind", "swimming_pool")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("leisure", "swimming_pool")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void kindFountain() {
    assertFeatures(15,
      List.of(Map.of("kind", "fountain")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("amenity", "fountain")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void kindRiver() {
    assertFeatures(9,
      List.of(Map.of("kind", "river")),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "waterway", "river"
        )),
        "osm",
        null,
        0
      )));
  }

  @Test
  void kindDam() {
    assertFeatures(15,
      List.of(),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "waterway", "dam"
        )),
        "osm",
        null,
        0
      )));
  }

  @Test
  void oceanLabel() {
    assertFeatures(12,
      List.of(Map.of("kind", "ocean")),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("place", "ocean",
          "name", "a"
        )),
        "osm",
        null,
        0
      )));
  }
}
