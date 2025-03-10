package com.protomaps.basemap.layers;

import static com.protomaps.basemap.feature.Matcher.fromTag;
import static com.protomaps.basemap.feature.Matcher.getInteger;
import static com.protomaps.basemap.feature.Matcher.getString;
import static com.protomaps.basemap.feature.Matcher.rule;
import static com.protomaps.basemap.feature.Matcher.use;
import static com.protomaps.basemap.feature.Matcher.with;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.expression.MultiExpression;
import com.onthegomap.planetiler.geo.GeoUtils;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.util.Parse;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.NeNames;
import com.protomaps.basemap.names.OsmNames;
import java.util.List;
import java.util.Map;

public class Water implements ForwardingProfile.LayerPostProcessor {

  private static final double WORLD_AREA_FOR_70K_SQUARE_METERS =
    Math.pow(GeoUtils.metersToPixelAtEquator(0, Math.sqrt(70_000)) / 256d, 2);

  public static final String LAYER_NAME = "water";

  private static final MultiExpression.Index<Map<String, Object>> neIndex = MultiExpression.of(List.of(
    rule(
      with("featurecla", "Ocean"),
      use("minZoom", fromTag("min_zoom")),
      use("kind", "ocean")
    ),
    rule(
      with("featurecla", "Playa"),
      use("minZoom", fromTag("min_zoom")),
      use("kind", "playa")
    ),
    rule(
      with("featurecla", "Reservoir"),
      use("minZoom", fromTag("min_zoom")),
      use("kind", "lake")
    ),
    rule(
      with("featurecla", "Lake"),
      use("minZoom", fromTag("min_zoom")),
      use("kind", "lake")
    ),
    rule(
      with("featurecla", "Alkaline Lake"),
      use("minZoom", fromTag("min_zoom")),
      use("kind", "lake")
    )
  )).index();

  private static final MultiExpression.Index<Map<String, Object>> osmIndex = MultiExpression.of(List.of(
    rule(
      with("natural", "reef"),
      use("kind", "reef")
    ),
    rule(
      with("natural", "reef"),
      with("""
          reef
          coral
          rock
          sand
        """),
      use("kindDetail", fromTag("reef"))
    ),
    rule(
      with("waterway", "drain"),
      use("kind", "drain"),
      use("minZoom", 16)
    ),
    rule(
      with("waterway", "ditch"),
      use("kind", "ditch"),
      use("minZoom", 16)
    ),
    rule(
      with("waterway", "stream"),
      use("kind", "stream"),
      use("minZoom", 11)
    ),
    rule(
      with("waterway", "river"),
      use("kind", "river"),
      use("minZoom", 9)
    ),
    rule(
      with("waterway", "canal"),
      use("kind", "canal"),
      use("minZoom", 11)
    ),
    rule(
      with("waterway", "canal"),
      with("boat", "yes"),
      use("kind", "canal"),
      use("minZoom", 9)
    ),
    rule(
      with("amenity", "swimming_pool"),
      use("kind", "swimming_pool")
    ),
    rule(
      with("leisure", "swimming_pool"),
      use("kind", "swimming_pool")
    ),
    rule(
      with("landuse", "reservoir"),
      use("kind", "lake")
    ),
    rule(
      with("landuse", "basin"),
      use("kind", "basin")
    ),
    rule(
      with("""
          natural
          fjord
          strait
          bay
        """),
      use("kind", fromTag("natural"))
    ),
    rule(
      with("natural", "water"),
      use("kind", "water")
    ),
    rule(
      with("natural", "water"),
      with("""
          water
          basin
          canal
          ditch
          drain
          lake
          river
          stream
        """),
      use("kindDetail", fromTag("water"))
    ),
    rule(
      with("natural", "water"),
      with("""
          water
          lagoon
          oxbow
          pond
          reservoir
          wastewater
        """),
      use("kindDetail", "lake")
    ),
    rule(
      with("amenity", "fountain"),
      use("kind", "fountain")
    ),
    rule(
      with("waterway", "dock"),
      use("kind", "dock")
    ),
    rule(
      with("waterway", "riverbank"),
      use("kind", "riverbank")
    ),
    rule(
      with("covered", "yes"),
      use("kind", null)
    ),
    rule(
      with("name"),
      with("place", "sea"),
      use("kind", "sea"),
      use("minZoom", 6)
    ),
    rule(
      with("name"),
      with("place", "ocean"),
      use("kind", "ocean"),
      use("minZoom", 6)
    )
  )).index();

  @Override
  public String name() {
    return LAYER_NAME;
  }

  public void processPreparedOsm(SourceFeature ignoredSf, FeatureCollector features) {
    features.polygon(LAYER_NAME)
      .setId(0)
      .setAttr("kind", "ocean")
      .setAttr("sort_rank", 200)
      .setZoomRange(6, 15).setBufferPixels(8);
  }

  public void processNe(SourceFeature sf, FeatureCollector features) {

    var matches = neIndex.getMatches(sf);
    if (matches.isEmpty()) {
      return;
    }

    String kind = getString(sf, matches, "kind", null);
    if (kind == null) {
      return;
    }

    String minZoomString = getString(sf, matches, "minZoom", null);

    if (sf.canBePolygon() && minZoomString != null) {
      int minZoom = (int) Math.round(Double.parseDouble(minZoomString));

      int themeMinZoom = sf.getSourceLayer().contains("_50m_") ? 0 : 5;
      int themeMaxZoom = sf.getSourceLayer().contains("_50m_") ? 4 : 5;

      features.polygon(LAYER_NAME)
        .setAttr("kind", kind)
        .setAttr("sort_rank", 200)
        .setZoomRange(Math.max(themeMinZoom, minZoom), themeMaxZoom)
        // (nvkelso 20230802) Don't set setMinPixelSize here else small islands chains like Hawaii are garbled
        .setBufferPixels(8);
    }

    if (sf.getSourceLayer().equals("ne_10m_lakes") && sf.hasTag("min_label") && sf.hasTag("name")) {
      int minZoom = (int) Math.round(Double.parseDouble(sf.getString("min_label")));
      var waterLabelPosition = features.pointOnSurface(LAYER_NAME)
        .setAttr("kind", kind)
        .setAttr("min_zoom", minZoom + 1)
        .setZoomRange(minZoom + 1, 5)
        .setSortKey(minZoom)
        .setBufferPixels(128);

      NeNames.setNeNames(waterLabelPosition, sf, 0);
    }
  }

  public void processOsm(SourceFeature sf, FeatureCollector features) {

    var matches = osmIndex.getMatches(sf);
    if (matches.isEmpty()) {
      return;
    }

    String kind = getString(sf, matches, "kind", null);
    if (kind == null) {
      return;
    }

    String kindDetail = getString(sf, matches, "kindDetail", null);

    // polygons
    if (sf.canBePolygon()) {
      features.polygon(LAYER_NAME)
        .setAttr("kind", kind)
        .setAttr("kind_detail", kindDetail)
        .setAttr("sort_rank", 200)
        // Core OSM tags for different kinds of places
        // Add less common attributes only at higher zooms
        .setAttrWithMinzoom("bridge", sf.getString("bridge"), 12)
        .setAttrWithMinzoom("tunnel", sf.getString("tunnel"), 12)
        .setAttrWithMinzoom("layer", Parse.parseIntOrNull(sf.getString("layer")), 12)
        .setZoomRange(6, 15)
        .setMinPixelSize(1.0)
        .setBufferPixels(8);
    }

    // lines
    if (sf.canBeLine() && !sf.canBePolygon()) {
      int minZoom = getInteger(sf, matches, "minZoom", 12);

      var feat = features.line(LAYER_NAME)
        .setId(FeatureId.create(sf))
        .setAttr("kind", kind)
        .setAttr("min_zoom", minZoom + 1)
        .setAttrWithMinzoom("layer", Parse.parseIntOrNull(sf.getString("layer")), 12)
        .setAttr("sort_rank", 200)
        .setSortKey(minZoom)
        .setZoomRange(minZoom, 15);

      // Set "brunnel" (bridge / tunnel) property where "level" = 1 is a bridge, 0 is ground level, and -1 is a tunnel
      // Because of MapLibre performance and draw order limitations, generally the boolean is sufficient
      // See also: "layer" for more complicated ±6 layering for more sophisticated graphics libraries
      if (sf.hasTag("bridge") && !sf.hasTag("bridge", "no")) {
        feat.setAttr("level", 1);
      } else if (sf.hasTag("tunnel") && !sf.hasTag("tunnel", "no")) {
        feat.setAttr("level", -1);
      } else if (sf.hasTag("layer", "-6", "-5", "-4", "-3", "-2", "-1")) {
        feat.setAttr("level", -1);
      } else {
        feat.setAttr("level", 0);
      }

      OsmNames.setOsmNames(feat, sf, 0);
    }

    // points
    if (sf.isPoint()) {
      int minZoom = getInteger(sf, matches, "minZoom", 12);

      var feat = features.point(LAYER_NAME)
        .setId(FeatureId.create(sf))
        .setAttr("kind", kind)
        .setAttr("min_zoom", minZoom)
        .setSortKey(minZoom)
        .setZoomRange(minZoom, 15);

      OsmNames.setOsmNames(feat, sf, 0);
    }

    // points from polygons
    if (sf.hasTag("name") && sf.canBePolygon()) {
      int nameMinZoom = 15;
      Double wayArea = 0.0;

      try {
        wayArea = sf.area() / WORLD_AREA_FOR_70K_SQUARE_METERS;
      } catch (GeometryException e) {
        e.log("Exception in way area calculation");
      }

      // We don't want to show too many water labels at early zooms else it crowds the map
      // TODO: (nvkelso 20230621) These numbers are super wonky, they should instead be sq meters in web mercator prj
      // Zoom 5 and earlier from Natural Earth instead (see above)
      if (wayArea > 25000) { //500000000
        nameMinZoom = 6;
      } else if (wayArea > 8000) { //500000000
        nameMinZoom = 7;
      } else if (wayArea > 3000) { //200000000
        nameMinZoom = 8;
      } else if (wayArea > 500) { //40000000
        nameMinZoom = 9;
      } else if (wayArea > 200) { //8000000
        nameMinZoom = 10;
      } else if (wayArea > 30) { //1000000
        nameMinZoom = 11;
      } else if (wayArea > 25) { //500000
        nameMinZoom = 12;
      } else if (wayArea > 0.5) { //50000
        nameMinZoom = 13;
      } else if (wayArea > 0.05) { //10000
        nameMinZoom = 14;
      }

      var waterLabelPosition = features.pointOnSurface(LAYER_NAME)
        .setAttr("kind", kind)
        .setAttr("kind_detail", kindDetail)
        // While other layers don't need min_zoom, physical point labels do for more
        // predictable client-side label collisions
        // 512 px zooms versus 256 px logical zooms
        .setAttr("min_zoom", nameMinZoom + 1)
        // Add less common core Tilezen attributes only at higher zooms (will continue to v4)
        .setAttrWithMinzoom("bridge", sf.getString("bridge"), 12)
        .setAttrWithMinzoom("tunnel", sf.getString("tunnel"), 12)
        .setAttrWithMinzoom("layer", Parse.parseIntOrNull(sf.getString("layer")), 12)
        .setZoomRange(nameMinZoom, 15)
        .setAttr("sort_rank", 200)
        .setSortKey(nameMinZoom)
        .setBufferPixels(128);

      OsmNames.setOsmNames(waterLabelPosition, sf, 0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    return FeatureMerge.mergeOverlappingPolygons(items, 1);
  }
}
