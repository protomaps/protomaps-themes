package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.assertSubmap;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.TestUtils;
import com.onthegomap.planetiler.config.PlanetilerConfig;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.stats.Stats;
import com.protomaps.basemap.Basemap;
import com.protomaps.basemap.feature.NaturalEarthDb;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

abstract class LayerTest {

  final PlanetilerConfig params = PlanetilerConfig.defaults();
  final Stats stats = Stats.inMemory();
  final FeatureCollector.Factory featureCollectorFactory = new FeatureCollector.Factory(params, stats);
  final NaturalEarthDb naturalEarthDb = new NaturalEarthDb(
    List.of(new NaturalEarthDb.NeAdmin0Country("United States", "USA", "US", "Q1", 3.0, 5.0)),
    List.of(new NaturalEarthDb.NeAdmin1StateProvince("California", "US-CA", "Q2", 5.0, 8.0)),
    List.of(new NaturalEarthDb.NePopulatedPlace("San Francisco", "Q3", 9.0, 2))
  );
  final Basemap profile = new Basemap(naturalEarthDb, null, null);

  static void assertFeatures(int zoom, List<Map<String, Object>> expected, Iterable<FeatureCollector.Feature> actual) {
    var expectedList = expected.stream().toList();
    var actualList = StreamSupport.stream(actual.spliterator(), false).toList();
    assertEquals(expectedList.size(), actualList.size(), () -> "size: " + actualList);
    for (int i = 0; i < expectedList.size(); i++) {
      assertSubmap(expectedList.get(i), TestUtils.toMap(actualList.get(i), zoom));
    }
  }

  FeatureCollector process(SourceFeature feature) {
    var collector = featureCollectorFactory.get(feature);
    profile.processFeature(feature, collector);
    return collector;
  }
}
