package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.util.Parse.parseDoubleOrNull;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.util.Parse;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.postprocess.Area;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Buildings implements ForwardingProfile.LayerPostProcesser {

  static final String HEIGHT_KEY = "height";
  static final String MIN_HEIGHT_KEY = "min_height";

  @Override
  public String name() {
    return "buildings";
  }

  public record Height(Double height, Double min_height) {}


  static final Pattern pattern = Pattern.compile("^\\d+(\\.\\d)?$");

  static Double parseWellFormedDouble(String s) {
    if (pattern.matcher(s).matches()) {
      return parseDoubleOrNull(s);
    }
    return null;
  }

  static Height parseHeight(String osmHeight, String osmLevels, String osmMinHeight) {
    var height = parseDoubleOrNull(osmHeight);
    if (height == null) {
      Double levels = parseDoubleOrNull(osmLevels);
      if (levels != null) {
        height = Math.max(levels, 1) * 3 + 2;
      }
    }

    return new Height(height, parseDoubleOrNull(osmMinHeight));
  }

  static int quantizeVal(double val, int step) {
    // special case: if val is very small, we don't want it rounding to zero, so
    // round the smallest values up to the first step.
    if (val < step) {
      return (int) step;
    }

    return (int) Math.round(val / step) * step;
  }

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    if (sf.canBePolygon() && ((sf.hasTag("building") && !sf.hasTag("building", "no")) ||
      (sf.hasTag("building:part") && !sf.hasTag("building:part", "no")))) {

      var height = parseHeight(sf.getString(HEIGHT_KEY), sf.getString("building:levels"), sf.getString(MIN_HEIGHT_KEY));
      Integer minZoom = 11;
      String kind = "building";

      // Limit building:part features to later zooms
      // TODO: (nvkelso 20230621) this should be based on area and volume, too
      if (sf.hasTag("building:part")) {
        kind = "building_part";
        minZoom = 14;
      }

      var feature = features.polygon(this.name())
        .setId(FeatureId.create(sf))
        // Core Tilezen schema properties
        .setAttr("kind", kind)
        // Core OSM tags for different kinds of places
        .setAttrWithMinzoom("layer", Parse.parseIntOrNull(sf.getString("layer")), 13)
        // NOTE: Height is quantized by zoom in a post-process step
        .setAttr(HEIGHT_KEY, height.height())
        .setAttr("sort_rank", 400)
        .setZoomRange(minZoom, 15);

      if (kind.equals("building_part")) {
        // We don't need to set WithMinzoom because that's implicate with the ZoomRange
        feature.setAttr("kind_detail", sf.getString("building:part"));
        feature.setAttr(MIN_HEIGHT_KEY, height.min_height());
      }

      // Names should mostly just be for POIs
      // Sometimes building name and address are useful items, but only at zoom 17+
      //OsmNames.setOsmNames(feature, sf, 13);
    } else if (sf.hasTag("addr:housenumber")) {
      FeatureCollector.Feature feature = null;
      if (sf.isPoint()) {
        feature = features.point(this.name());
      } else if (sf.canBePolygon()) {
        feature = features.centroid(this.name());
      }
      if (feature != null) {
        feature
          .setId(FeatureId.create(sf))
          .setAttr("addr_housenumber", sf.getString("addr:housenumber"))
          .setAttr("addr_street", sf.getString("addr:street"))
          .setAttr("kind", "address")
          .setMinZoom(15);
      }
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    if (zoom == 15) {
      List<VectorTile.Feature> buildings = new ArrayList<>();

      // deduplicate addresses
      HashMap<Map<String, Object>, List<VectorTile.Feature>> groupedAddresses = new LinkedHashMap<>();
      for (VectorTile.Feature item : items) {
        if (item.tags().containsKey("addr_housenumber")) {
          groupedAddresses.computeIfAbsent(item.tags(), k -> new ArrayList<>()).add(item);
        } else {
          buildings.add(item);
        }
      }

      for (var address : groupedAddresses.values()) {
        var feature = address.get(0);
        feature.tags().remove("addr_street");
        buildings.add(feature);
      }

      return buildings;
    }
    items = Area.filterArea(items, 0);

    if (zoom >= 15)
      return items;

    // quantize height by zoom when less than max_zoom 15 to facilitate better feature merging
    for (var item : items) {
      if (item.tags().containsKey(HEIGHT_KEY)) {
        var height = (double) item.tags().get(HEIGHT_KEY);

        // Protected against NULL values
        if (height > 0) {
          // at zoom <= 12 round height to nearest 20 meters
          if (zoom <= 12) {
            height = quantizeVal(height, 20);
          } else
          // at zoom 13 round height to nearest 10 meters
          if (zoom == 13) {
            height = quantizeVal(height, 10);
          } else
          // at zoom 14 round height to nearest 5 meters
          if (zoom == 14) {
            height = quantizeVal(height, 5);
          }

          item.tags().put(HEIGHT_KEY, height);
        }
      }

      if (item.tags().containsKey(MIN_HEIGHT_KEY)) {
        var minHeight = (double) item.tags().get(MIN_HEIGHT_KEY);

        // Protected against NULL values
        if (minHeight > 0) {
          if (zoom <= 12) {
            minHeight = quantizeVal(minHeight, 20);
          } else
          // at zoom 13 round height to nearest 10 meters
          if (zoom == 13) {
            minHeight = quantizeVal(minHeight, 10);
          } else
          // at zoom 14 round height to nearest 5 meters
          if (zoom == 14) {
            minHeight = quantizeVal(minHeight, 5);
          }

          item.tags().put(MIN_HEIGHT_KEY, minHeight);
        }
      }
    }
    return FeatureMerge.mergeNearbyPolygons(items, 3.125, 3.125, 0.5, 0.5);
  }
}
