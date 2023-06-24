package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeoUtils;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.NeNames;
import com.protomaps.basemap.names.OsmNames;
import java.util.List;

public class PhysicalPoint implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "physical_point";
  }

  private static final double WORLD_AREA_FOR_70K_SQUARE_METERS =
    Math.pow(GeoUtils.metersToPixelAtEquator(0, Math.sqrt(70_000)) / 256d, 2);

  public void processNe(SourceFeature sf, FeatureCollector features) {
    var sourceLayer = sf.getSourceLayer();
    var kind = "";
    var alkaline = 0;
    var reservoir = 0;
    var theme_min_zoom = 0;
    var theme_max_zoom = 0;

    if (sourceLayer.equals("ne_10m_lakes")) {
      theme_min_zoom = 5;
      theme_max_zoom = 5;

      switch (sf.getString("featurecla")) {
        case "Alkaline Lake" -> {
          kind = "lake";
          alkaline = 1;
        }
        case "Lake" -> kind = "lake";
        case "Reservoir" -> {
          kind = "lake";
          reservoir = 1;
        }
        case "Playa" -> kind = "playa";
      }

      if (kind != "" && sf.hasTag("min_label") && sf.hasTag("name") && sf.getTag("name") != null) {
        var water_label_position = features.pointOnSurface(this.name())
          .setAttr("pmap:kind", kind)
          .setAttr("pmap:min_zoom", sf.getLong("min_label") + 1)
          .setZoomRange(sf.getString("min_label") == null ? theme_min_zoom :
            (int) Double.parseDouble(sf.getString("min_label")) + 1, theme_max_zoom)
          .setBufferPixels(128);

        NeNames.setNeNames(water_label_position, sf, 0);
      }
    }
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (sf.isPoint() && (sf.hasTag("place", "sea", "ocean") || sf.hasTag("natural", "peak"))) {

      // TODO: rank based on ele

      String kind = "";

      int minzoom = 12;
      if (sf.hasTag("place", "ocean")) {
        kind = "ocean";
        minzoom = 0;
      }
      if (sf.hasTag("place", "sea")) {
        kind = "sea";
        minzoom = 3;
      }
      if (sf.hasTag("natural", "peak")) {
        kind = "peak";
        minzoom = 13;
      }

      var feat = features.point(this.name())
        .setId(FeatureId.create(sf))
        .setAttr("pmap:kind", kind)
        .setAttr("place", sf.getString("place"))
        .setAttr("natural", sf.getString("natural"))
        .setAttr("ele", sf.getString("ele"))
        .setZoomRange(minzoom, 15);

      OsmNames.setOsmNames(feat, sf, 0);
    }

    if (sf.hasTag("name") && sf.getTag("name") != null &&
      sf.canBePolygon() &&
      (sf.hasTag("water") ||
        sf.hasTag("waterway") ||
        // bay, straight, fjord are included here only (not in water layer) because
        // OSM treats them as "overlay" label features over the normal water polys
        sf.hasTag("natural", "water", "bay", "strait", "fjord") ||
        sf.hasTag("landuse", "reservoir") ||
        sf.hasTag("leisure", "swimming_pool"))) {
      String kind = "other";
      var kind_detail = "";
      var name_min_zoom = 15;
      var reservoir = false;
      var alkaline = false;
      Double way_area = 0.0;

      try {
        way_area = sf.area() / WORLD_AREA_FOR_70K_SQUARE_METERS;
      } catch (GeometryException e) {
        System.out.println(e);
      }

      // coallese values across tags to single kind value
      if (sf.hasTag("natural", "water", "bay", "strait", "fjord")) {
        kind = sf.getString("natural");
        if (sf.hasTag("water", "basin", "canal", "ditch", "drain", "lake", "river", "stream")) {
          kind_detail = sf.getString("water");

          // This is a bug in Tilezen v1.9 that should be fixed in 2.0
          // But isn't present in Protomaps v2 so let's fix it preemtively
          if (kind_detail == "lake") {
            kind = "lake";
          }

          if (sf.hasTag("water", "lagoon", "oxbow", "pond", "reservoir", "wastewater")) {
            kind_detail = "lake";
          }
          if (sf.hasTag("water", "reservoir")) {
            reservoir = true;
          }
          if (sf.hasTag("water", "lagoon", "salt", "salt_pool")) {
            alkaline = true;
          }
        }
      } else if (sf.hasTag("waterway", "riverbank", "dock", "canal", "river", "stream", "ditch", "drain")) {
        kind = sf.getString("waterway");
      } else if (sf.hasTag("landuse", "basin", "reservoir")) {
        kind = sf.getString("landuse");
      } else if (sf.hasTag("leisure", "swimming_pool")) {
        kind = "swimming_pool";
      } else if (sf.hasTag("amenity", "swimming_pool")) {
        kind = "swimming_pool";
      }

      // We don't want to show too many water labels at early zooms else it crowds the map
      // TODO: (nvkelso 20230621) These numbers are super wonky, they should instead be sq meters in web mercator prj
      // Zoom 5 and earlier from Natural Earth instead (see above)
      if (way_area > 25000) { //500000000
        name_min_zoom = 6;
      } else if (way_area > 8000) { //500000000
        name_min_zoom = 7;
      } else if (way_area > 3000) { //200000000
        name_min_zoom = 8;
      } else if (way_area > 500) { //40000000
        name_min_zoom = 9;
      } else if (way_area > 200) { //8000000
        name_min_zoom = 10;
      } else if (way_area > 30) { //1000000
        name_min_zoom = 11;
      } else if (way_area > 25) { //500000
        name_min_zoom = 12;
      } else if (way_area > 0.5) { //50000
        name_min_zoom = 13;
      } else if (way_area > 0.05) { //10000
        name_min_zoom = 14;
      }

      var water_label_position = features.pointOnSurface(this.name())
        // Core Tilezen schema properties
        .setAttr("pmap:kind", kind)
        .setAttr("pmap:kind_detail", kind_detail)
        // While other layers don't need min_zoom, physical point labels do for more
        // predictable client-side label collisions
        // 512 px zooms versus 256 px logical zooms
        .setAttr("pmap:min_zoom", name_min_zoom + 1)
        // DEBUG
        //.setAttr("pmap:area", way_area)
        //
        // Core OSM tags for different kinds of places
        // DEPRECATION WARNING: Marked for deprecation in v4 schema, do not use these for styling
        //                      If an explicate value is needed it should bea kind, or included in kind_detail
        .setAttr("natural", sf.getString("natural"))
        .setAttr("landuse", sf.getString("landuse"))
        .setAttr("leisure", sf.getString("leisure"))
        .setAttr("water", sf.getString("water"))
        .setAttr("waterway", sf.getString("waterway"))
        // Add less common core Tilezen attributes only at higher zooms (will continue to v4)
        .setAttrWithMinzoom("bridge", sf.getString("bridge"), 12)
        .setAttrWithMinzoom("tunnel", sf.getString("tunnel"), 12)
        .setAttrWithMinzoom("layer", sf.getString("layer"), 12)
        .setZoomRange(name_min_zoom, 15)
        .setBufferPixels(128);

      // Add less common core Tilezen attributes only at higher zooms (will continue to v4)
      if (kind_detail != "") {
        water_label_position.setAttr("pmap:kind_detail", kind_detail);
      }
      if (sf.hasTag("water", "reservoir") || reservoir) {
        water_label_position.setAttr("reservoir", true);
      }
      if (sf.hasTag("water", "lagoon", "salt", "salt_pool") || alkaline) {
        water_label_position.setAttr("alkaline", true);
      }
      if (sf.hasTag("intermittent", "yes")) {
        water_label_position.setAttr("intermittent", true);
      }

      OsmNames.setOsmNames(water_label_position, sf, 0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    // DEBUG
    //items = Area.addAreaTag(items);

    return items;
  }
}
