export interface Theme {
  background: string;
  earth: string;
  park_a: string;
  park_b: string;
  hospital: string;
  industrial: string;
  school: string;
  wood_a: string;
  wood_b: string;
  pedestrian: string;
  scrub_a: string;
  scrub_b: string;
  glacier: string;
  sand: string;
  beach: string;
  aerodrome: string;
  runway: string;
  water: string;
  zoo: string;
  military: string;

  tunnel_other_casing: string;
  tunnel_minor_casing: string;
  tunnel_link_casing: string;
  tunnel_major_casing: string;
  tunnel_highway_casing: string;
  tunnel_other: string;
  tunnel_minor: string;
  tunnel_link: string;
  tunnel_major: string;
  tunnel_highway: string;

  pier: string;
  buildings: string;

  minor_service_casing: string;
  minor_casing: string;
  link_casing: string;
  major_casing_late: string;
  highway_casing_late: string;
  other: string;
  minor_service: string;
  minor_a: string;
  minor_b: string;
  link: string;
  major_casing_early: string;
  major: string;
  highway_casing_early: string;
  highway: string;

  railway: string;
  boundaries: string;

  bridges_other_casing: string;
  bridges_minor_casing: string;
  bridges_link_casing: string;
  bridges_major_casing: string;
  bridges_highway_casing: string;
  bridges_other: string;
  bridges_minor: string;
  bridges_link: string;
  bridges_major: string;
  bridges_highway: string;

  roads_label_minor: string;
  roads_label_minor_halo: string;
  roads_label_major: string;
  roads_label_major_halo: string;
  ocean_label: string;
  subplace_label: string;
  subplace_label_halo: string;
  city_label: string;
  city_label_halo: string;
  state_label: string;
  state_label_halo: string;
  country_label: string;

  housenumbers_label: string;
  house_numbers_label_halo: string;

  regular?: string;
  bold?: string;
  italic?: string;

  pois?: Pois;
  landcover?: Landcover;

  // deprecated
  peak_label: string;
  waterway_label: string;
}

export interface Pois {
  blue: string;
  green: string;
  lapis: string;
  pink: string;
  red: string;
  slategray: string;
  tangerine: string;
  turquoise: string;
}

export interface Landcover {
  barren: string;
  farmland: string;
  forest: string;
  glacier: string;
  grassland: string;
  scrub: string;
  urban_area: string;
}

export const CONTRAST: Theme = {
  background: "#dddddd",
  earth: "#ededed",
  park_a: "#bfc99c",
  park_b: "#bfc99c",
  hospital: "#ffeae8",
  industrial: "#f8ffed",
  school: "#f2fef9",
  wood_a: "#bfc99c",
  wood_b: "#bfc99c",
  pedestrian: "#eef0f0",
  scrub_a: "#bfc99c",
  scrub_b: "#bfc99c",
  glacier: "#ffffff",
  sand: "#ebe7da",
  beach: "#ebe7da",
  aerodrome: "#dbe7e7",
  runway: "#d1d9d9",
  water: "#84b7cf",
  zoo: "#ebe6ed",
  military: "#ebe6ed",

  tunnel_other_casing: "#ffffff",
  tunnel_minor_casing: "#e2e2e2",
  tunnel_link_casing: "#e2e2e2",
  tunnel_major_casing: "#e3cfd3",
  tunnel_highway_casing: "#ebcea2",
  tunnel_other: "#f7f7f7",
  tunnel_minor: "#ebebeb",
  tunnel_link: "#ebebeb",
  tunnel_major: "#ebebeb",
  tunnel_highway: "#ebebeb",

  pier: "#ededed",
  buildings: "#cbcece",

  minor_service_casing: "#e2e2e2",
  minor_casing: "#e2e2e2",
  link_casing: "#e2e2e2",
  major_casing_late: "#e3cfd3",
  highway_casing_late: "#ebcea2",
  other: "#ffffff",
  minor_service: "#ffffff",
  minor_a: "#fff2bb",
  minor_b: "#fff2bb",
  link: "#fff2bb",
  major_casing_early: "#e3cfd3",
  major: "#ffdf59",
  highway_casing_early: "#ebcea2",
  highway: "#e9ac77",

  railway: "#b3bcc9",
  boundaries: "#5c4a6b",
  waterway_label: "#a4cae1",

  bridges_other_casing: "#ffffff",
  bridges_minor_casing: "#e2e2e2",
  bridges_link_casing: "#e1e1e1",
  bridges_major_casing: "#e3cfd3",
  bridges_highway_casing: "#ebcea2",
  bridges_other: "#ffffff",
  bridges_minor: "#ffffff",
  bridges_link: "#e1e1e1",
  bridges_major: "#ffffff",
  bridges_highway: "#fefffc",

  roads_label_minor: "#91888b",
  roads_label_minor_halo: "#ffffff",
  roads_label_major: "#91888b",
  roads_label_major_halo: "#ffffff",
  ocean_label: "#ffffff",
  peak_label: "#61bb5b",
  subplace_label: "#757d91",
  subplace_label_halo: "#ffffff",
  city_label: "#3c3c3c",
  city_label_halo: "#ffffff",
  state_label: "#777777",
  state_label_halo: "#ffffff",
  country_label: "#9590aa",

  housenumbers_label: "#91888b",
  house_numbers_label_halo: "#ffffff",
};

export const LIGHT: Theme = {
  background: "#cccccc",
  earth: "#e2dfda",
  park_a: "#cfddd5",
  park_b: "#9cd3b4",
  hospital: "#e4dad9",
  industrial: "#d1dde1",
  school: "#e4ded7",
  wood_a: "#d0ded0",
  wood_b: "#a0d9a0",
  pedestrian: "#e3e0d4",
  scrub_a: "#cedcd7",
  scrub_b: "#99d2bb",
  glacier: "#e7e7e7",
  sand: "#e2e0d7",
  beach: "#e8e4d0",
  aerodrome: "#dadbdf",
  runway: "#e9e9ed",
  water: "#80deea",
  zoo: "#c6dcdc",
  military: "#dcdcdc",

  tunnel_other_casing: "#e0e0e0",
  tunnel_minor_casing: "#e0e0e0",
  tunnel_link_casing: "#e0e0e0",
  tunnel_major_casing: "#e0e0e0",
  tunnel_highway_casing: "#e0e0e0",
  tunnel_other: "#d5d5d5",
  tunnel_minor: "#d5d5d5",
  tunnel_link: "#d5d5d5",
  tunnel_major: "#d5d5d5",
  tunnel_highway: "#d5d5d5",

  pier: "#e0e0e0",
  buildings: "#cccccc",

  minor_service_casing: "#e0e0e0",
  minor_casing: "#e0e0e0",
  link_casing: "#e0e0e0",
  major_casing_late: "#e0e0e0",
  highway_casing_late: "#e0e0e0",
  other: "#ebebeb",
  minor_service: "#ebebeb",
  minor_a: "#ebebeb",
  minor_b: "#ffffff",
  link: "#ffffff",
  major_casing_early: "#e0e0e0",
  major: "#ffffff",
  highway_casing_early: "#e0e0e0",
  highway: "#ffffff",

  railway: "#a7b1b3",
  boundaries: "#adadad",
  waterway_label: "#ffffff",

  bridges_other_casing: "#e0e0e0",
  bridges_minor_casing: "#e0e0e0",
  bridges_link_casing: "#e0e0e0",
  bridges_major_casing: "#e0e0e0",
  bridges_highway_casing: "#e0e0e0",
  bridges_other: "#ebebeb",
  bridges_minor: "#ffffff",
  bridges_link: "#ffffff",
  bridges_major: "#f5f5f5",
  bridges_highway: "#ffffff",

  roads_label_minor: "#91888b",
  roads_label_minor_halo: "#ffffff",
  roads_label_major: "#938a8d",
  roads_label_major_halo: "#ffffff",
  ocean_label: "#728dd4",
  peak_label: "#7e9aa0",
  subplace_label: "#8f8f8f",
  subplace_label_halo: "#e0e0e0",
  city_label: "#5c5c5c",
  city_label_halo: "#e0e0e0",
  state_label: "#b3b3b3",
  state_label_halo: "#e0e0e0",
  country_label: "#a3a3a3",

  housenumbers_label: "#91888b",
  house_numbers_label_halo: "#ffffff",

  pois: {
    blue: "#1A8CBD",
    green: "#20834D",
    lapis: "#315BCF",
    pink: "#EF56BA",
    red: "#F2567A",
    slategray: "#6A5B8F",
    tangerine: "#CB6704",
    turquoise: "#00C3D4",
  },

  landcover: {
    grassland: "rgba(210, 239, 207, 1)",
    barren: "rgba(255, 243, 215, 1)",
    urban_area: "rgba(230, 230, 230, 1)",
    farmland: "rgba(216, 239, 210, 1)",
    glacier: "rgba(255, 255, 255, 1)",
    scrub: "rgba(234, 239, 210, 1)",
    forest: "rgba(196, 231, 210, 1)",
  },
};

export const DARK: Theme = {
  background: "#34373d",
  earth: "#1f1f1f",
  park_a: "#1c2421",
  park_b: "#192a24",
  hospital: "#252424",
  industrial: "#222222",
  school: "#262323",
  wood_a: "#202121",
  wood_b: "#202121",
  pedestrian: "#1e1e1e",
  scrub_a: "#222323",
  scrub_b: "#222323",
  glacier: "#1c1c1c",
  sand: "#212123",
  beach: "#28282a",
  aerodrome: "#1e1e1e",
  runway: "#333333",
  water: "#31353f",
  zoo: "#222323",
  military: "#242323",

  tunnel_other_casing: "#141414",
  tunnel_minor_casing: "#141414",
  tunnel_link_casing: "#141414",
  tunnel_major_casing: "#141414",
  tunnel_highway_casing: "#141414",
  tunnel_other: "#292929",
  tunnel_minor: "#292929",
  tunnel_link: "#292929",
  tunnel_major: "#292929",
  tunnel_highway: "#292929",

  pier: "#333333",
  buildings: "#111111",

  minor_service_casing: "#1f1f1f",
  minor_casing: "#1f1f1f",
  link_casing: "#1f1f1f",
  major_casing_late: "#1f1f1f",
  highway_casing_late: "#1f1f1f",
  other: "#333333",
  minor_service: "#333333",
  minor_a: "#3d3d3d",
  minor_b: "#333333",
  link: "#3d3d3d",
  major_casing_early: "#1f1f1f",
  major: "#3d3d3d",
  highway_casing_early: "#1f1f1f",
  highway: "#474747",

  railway: "#000000",
  boundaries: "#5b6374",
  waterway_label: "#717784",

  bridges_other_casing: "#2b2b2b",
  bridges_minor_casing: "#1f1f1f",
  bridges_link_casing: "#1f1f1f",
  bridges_major_casing: "#1f1f1f",
  bridges_highway_casing: "#1f1f1f",
  bridges_other: "#333333",
  bridges_minor: "#333333",
  bridges_link: "#3d3d3d",
  bridges_major: "#3d3d3d",
  bridges_highway: "#474747",

  roads_label_minor: "#525252",
  roads_label_minor_halo: "#1f1f1f",
  roads_label_major: "#666666",
  roads_label_major_halo: "#1f1f1f",
  ocean_label: "#717784",
  peak_label: "#898080",
  subplace_label: "#525252",
  subplace_label_halo: "#1f1f1f",
  city_label: "#7a7a7a",
  city_label_halo: "#212121",
  state_label: "#3d3d3d",
  state_label_halo: "#1f1f1f",
  country_label: "#5c5c5c",

  housenumbers_label: "#525252",
  house_numbers_label_halo: "#1f1f1f",

  pois: {
    blue: "#4299BB",
    green: "#30C573",
    lapis: "#2B5CEA",
    pink: "#EF56BA",
    red: "#F2567A",
    slategray: "#93939F",
    tangerine: "#F19B6E",
    turquoise: "#00C3D4",
  },

  landcover: {
    grassland: "rgba(30, 41, 31, 1)",
    barren: "rgba(38, 38, 36, 1)",
    urban_area: "rgba(28, 28, 28, 1)",
    farmland: "rgba(31, 36, 32, 1)",
    glacier: "rgba(43, 43, 43, 1)",
    scrub: "rgba(34, 36, 30, 1)",
    forest: "rgba(28, 41, 37, 1)",
  },
};

export const WHITE: Theme = {
  background: "#ffffff",
  earth: "#ffffff",
  park_a: "#fcfcfc",
  park_b: "#fcfcfc",
  hospital: "#f8f8f8",
  industrial: "#fcfcfc",
  school: "#f8f8f8",
  wood_a: "#fafafa",
  wood_b: "#fafafa",
  pedestrian: "#fdfdfd",
  scrub_a: "#fafafa",
  scrub_b: "#fafafa",
  glacier: "#fcfcfc",
  sand: "#fafafa",
  beach: "#f6f6f6",
  aerodrome: "#fdfdfd",
  runway: "#efefef",
  water: "#dcdcdc",
  zoo: "#f7f7f7",
  military: "#fcfcfc",

  tunnel_other_casing: "#d6d6d6",
  tunnel_minor_casing: "#fcfcfc",
  tunnel_link_casing: "#fcfcfc",
  tunnel_major_casing: "#fcfcfc",
  tunnel_highway_casing: "#fcfcfc",
  tunnel_other: "#d6d6d6",
  tunnel_minor: "#d6d6d6",
  tunnel_link: "#d6d6d6",
  tunnel_major: "#d6d6d6",
  tunnel_highway: "#d6d6d6",

  pier: "#efefef",
  buildings: "#efefef",

  minor_service_casing: "#ffffff",
  minor_casing: "#ffffff",
  link_casing: "#ffffff",
  major_casing_late: "#ffffff",
  highway_casing_late: "#ffffff",
  other: "#f5f5f5",
  minor_service: "#f5f5f5",
  minor_a: "#ebebeb",
  minor_b: "#f5f5f5",
  link: "#ebebeb",
  major_casing_early: "#ffffff",
  major: "#ebebeb",
  highway_casing_early: "#ffffff",
  highway: "#ebebeb",

  railway: "#d6d6d6",
  boundaries: "#adadad",
  waterway_label: "#adadad",

  bridges_other_casing: "#ffffff",
  bridges_minor_casing: "#ffffff",
  bridges_link_casing: "#ffffff",
  bridges_major_casing: "#ffffff",
  bridges_highway_casing: "#ffffff",
  bridges_other: "#f5f5f5",
  bridges_minor: "#f5f5f5",
  bridges_link: "#ebebeb",
  bridges_major: "#ebebeb",
  bridges_highway: "#ebebeb",

  roads_label_minor: "#adadad",
  roads_label_minor_halo: "#ffffff",
  roads_label_major: "#999999",
  roads_label_major_halo: "#ffffff",
  ocean_label: "#adadad",
  peak_label: "#adadad",
  subplace_label: "#8f8f8f",
  subplace_label_halo: "#ffffff",
  city_label: "#5c5c5c",
  city_label_halo: "#ffffff",
  state_label: "#b3b3b3",
  state_label_halo: "#ffffff",
  country_label: "#b8b8b8",

  housenumbers_label: "#adadad",
  house_numbers_label_halo: "#ffffff",
};

export const GRAYSCALE: Theme = {
  background: "#a3a3a3",
  earth: "#cccccc",
  park_a: "#c2c2c2",
  park_b: "#c2c2c2",
  hospital: "#d0d0d0",
  industrial: "#c6c6c6",
  school: "#d0d0d0",
  wood_a: "#c2c2c2",
  wood_b: "#c2c2c2",
  pedestrian: "#c4c4c4",
  scrub_a: "#c2c2c2",
  scrub_b: "#c2c2c2",
  glacier: "#d2d2d2",
  sand: "#d2d2d2",
  beach: "#d2d2d2",
  aerodrome: "#c9c9c9",
  runway: "#f5f5f5",
  water: "#a3a3a3",
  zoo: "#c7c7c7",
  military: "#bfbfbf",

  tunnel_other_casing: "#b8b8b8",
  tunnel_minor_casing: "#b8b8b8",
  tunnel_link_casing: "#b8b8b8",
  tunnel_major_casing: "#b8b8b8",
  tunnel_highway_casing: "#b8b8b8",
  tunnel_other: "#d6d6d6",
  tunnel_minor: "#d6d6d6",
  tunnel_link: "#d6d6d6",
  tunnel_major: "#d6d6d6",
  tunnel_highway: "#d6d6d6",

  pier: "#b8b8b8",
  buildings: "#e0e0e0",

  minor_service_casing: "#cccccc",
  minor_casing: "#cccccc",
  link_casing: "#cccccc",
  major_casing_late: "#cccccc",
  highway_casing_late: "#cccccc",
  other: "#e0e0e0",
  minor_service: "#e0e0e0",
  minor_a: "#ebebeb",
  minor_b: "#e0e0e0",
  link: "#ebebeb",
  major_casing_early: "#cccccc",
  major: "#ebebeb",
  highway_casing_early: "#cccccc",
  highway: "#ebebeb",

  railway: "#f5f5f5",
  boundaries: "#5c5c5c",
  waterway_label: "#7a7a7a",

  bridges_other_casing: "#cccccc",
  bridges_minor_casing: "#cccccc",
  bridges_link_casing: "#cccccc",
  bridges_major_casing: "#cccccc",
  bridges_highway_casing: "#cccccc",
  bridges_other: "#e0e0e0",
  bridges_minor: "#e0e0e0",
  bridges_link: "#ebebeb",
  bridges_major: "#ebebeb",
  bridges_highway: "#ebebeb",

  roads_label_minor: "#999999",
  roads_label_minor_halo: "#e0e0e0",
  roads_label_major: "#8f8f8f",
  roads_label_major_halo: "#ebebeb",
  ocean_label: "#7a7a7a",
  peak_label: "#5c5c5c",
  subplace_label: "#7a7a7a",
  subplace_label_halo: "#cccccc",
  city_label: "#474747",
  city_label_halo: "#cccccc",
  state_label: "#999999",
  state_label_halo: "#cccccc",
  country_label: "#858585",

  housenumbers_label: "#999999",
  house_numbers_label_halo: "#e0e0e0",
};

export const BLACK: Theme = {
  background: "#2b2b2b",
  earth: "#141414",
  park_a: "#181818",
  park_b: "#181818",
  hospital: "#1d1d1d",
  industrial: "#101010",
  school: "#111111",
  wood_a: "#1a1a1a",
  wood_b: "#1a1a1a",
  pedestrian: "#191919",
  scrub_a: "#1c1c1c",
  scrub_b: "#1c1c1c",
  glacier: "#191919",
  sand: "#161616",
  beach: "#1f1f1f",
  aerodrome: "#191919",
  runway: "#323232",
  water: "#333333",
  zoo: "#191919",
  military: "#121212",

  tunnel_other_casing: "#101010",
  tunnel_minor_casing: "#101010",
  tunnel_link_casing: "#101010",
  tunnel_major_casing: "#101010",
  tunnel_highway_casing: "#101010",
  tunnel_other: "#292929",
  tunnel_minor: "#292929",
  tunnel_link: "#292929",
  tunnel_major: "#292929",
  tunnel_highway: "#292929",

  pier: "#0a0a0a",
  buildings: "#0a0a0a",

  minor_service_casing: "#141414",
  minor_casing: "#141414",
  link_casing: "#141414",
  major_casing_late: "#141414",
  highway_casing_late: "#141414",
  other: "#1f1f1f",
  minor_service: "#1f1f1f",
  minor_a: "#292929",
  minor_b: "#1f1f1f",
  link: "#1f1f1f",
  major_casing_early: "#141414",
  major: "#292929",
  highway_casing_early: "#141414",
  highway: "#292929",

  railway: "#292929",
  boundaries: "#707070",
  waterway_label: "#707070",

  bridges_other_casing: "#141414",
  bridges_minor_casing: "#141414",
  bridges_link_casing: "#141414",
  bridges_major_casing: "#141414",
  bridges_highway_casing: "#141414",
  bridges_other: "#1f1f1f",
  bridges_minor: "#1f1f1f",
  bridges_link: "#292929",
  bridges_major: "#292929",
  bridges_highway: "#292929",

  roads_label_minor: "#525252",
  roads_label_minor_halo: "#141414",
  roads_label_major: "#5c5c5c",
  roads_label_major_halo: "#141414",
  ocean_label: "#707070",
  peak_label: "#707070",
  subplace_label: "#5c5c5c",
  subplace_label_halo: "#141414",
  city_label: "#999999",
  city_label_halo: "#141414",
  state_label: "#3d3d3d",
  state_label_halo: "#141414",
  country_label: "#707070",

  housenumbers_label: "#525252",
  house_numbers_label_halo: "#141414",
};

const themes: Record<string, Theme> = {
  light: LIGHT,
  dark: DARK,
  white: WHITE,
  grayscale: GRAYSCALE,
  black: BLACK,
  contrast: CONTRAST,
};

export default themes;
