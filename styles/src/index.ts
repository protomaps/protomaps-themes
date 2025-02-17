import { LayerSpecification } from "@maplibre/maplibre-gl-style-spec";
import { labels_layers, nolabels_layers } from "./base_layers";
import { language_script_pairs } from "./language";
import {
  Flavor,
  Pois,
  LIGHT,
  DARK,
  WHITE,
  GRAYSCALE,
  BLACK,
} from "./flavors";

export { language_script_pairs };
export type { Pois, Flavor };
export { LIGHT, DARK, WHITE, GRAYSCALE, BLACK };

export function namedFlavor(name: string): Flavor {
  switch (name) {
    case "light":
      return LIGHT;
    case "dark":
      return DARK;
    case "white":
      return WHITE;
    case "grayscale":
      return GRAYSCALE;
    case "black":
      return BLACK;
  }
  throw new Error("Flavor not found");
}

export function layers(
  source: string,
  flavor: Flavor,
  options?: { labelsOnly?: boolean; lang?: string },
): LayerSpecification[] {
  let layers: LayerSpecification[] = [];
  if (!options?.labelsOnly) {
    layers = nolabels_layers(source, flavor);
  }
  if (options?.lang) {
    layers = layers.concat(labels_layers(source, flavor, options.lang));
  }
  return layers;
}
