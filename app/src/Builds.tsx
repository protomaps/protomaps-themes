/* @refresh reload */
import { render } from "solid-js/web";
import "./index.css";
import Nav from "./Nav";
import { VERSION_COMPATIBILITY } from "./utils";

import { For, Show, createResource, createSignal } from "solid-js";

interface Build {
  uploaded: string;
  key: string;
  size: number;
  version: string;
  md5sum?: string;
  b3sum?: string;
}

function b64ToHex(b64?: string): string {
  if (!b64) return "";
  const b = atob(b64);
  let hex = "";
  for (let i = 0; i < b.length; i++) {
    const hexByte = b.charCodeAt(i).toString(16).padStart(2, "0");
    hex += hexByte;
  }
  return hex;
}

function toDate(dateStr: string): Date {
  const year = Number.parseInt(dateStr.substring(0, 4), 10);
  const month = Number.parseInt(dateStr.substring(4, 6), 10) - 1; // Subtract 1 because months are 0-indexed in JavaScript dates
  const day = Number.parseInt(dateStr.substring(6, 8), 10);
  return new Date(year, month, day);
}

function formatBytes(bytes: number, decimals = 2) {
  if (!+bytes) return "0 Bytes";
  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = [
    "Bytes",
    "KiB",
    "MiB",
    "GiB",
    "TiB",
    "PiB",
    "EiB",
    "ZiB",
    "YiB",
  ];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${Number.parseFloat((bytes / k ** i).toFixed(dm))} ${sizes[i]}`;
}

function Hashes(build: Build) {
  return (
    <span>
      <Show when={build.md5sum}>
        <span title={b64ToHex(build.md5sum)}>
          md5:{b64ToHex(build.md5sum).substr(0, 8)}
        </span>
      </Show>
      <Show when={build.b3sum}>
        <span title={build.b3sum}> b3:{build.b3sum?.substr(0, 8)}</span>
      </Show>
    </span>
  );
}

function BuildComponent(props: {
  build: Build;
  idx: number;
  cmpA: number;
  cmpB: number;
  setCmpA: (i: number) => void;
  setCmpB: (i: number) => void;
}) {
  const build = props.build;
  const dateStr = build.key.substr(0, 8);
  const date = toDate(dateStr);
  const link = `https://build.protomaps.com/${build.key}`;
  const statsLink = `https://build.protomaps.com/${dateStr}.layerstats.parquet`;
  const idx = props.idx;

  const onChangeA = () => {
    props.setCmpA(idx);
  };

  const onChangeB = () => {
    props.setCmpB(idx);
  };

  const compatibleNpmVersions = (v: string) => {
    try {
      return VERSION_COMPATIBILITY[+v.split(".")[0]].join(",");
    } catch (e) {
      return "";
    }
  };

  return (
    <tr style={{ color: date.getDay() === 1 ? "black" : "#aaa" }}>
      <td>
        <input
          disabled={idx <= props.cmpB}
          type="radio"
          onChange={onChangeA}
          checked={idx === props.cmpA}
          aria-label={`compare earlier build ${date.toDateString()}`}
        />
        <input
          class="ml-2"
          disabled={idx >= props.cmpA}
          type="radio"
          onChange={onChangeB}
          checked={idx === props.cmpB}
          aria-label={`compare later build ${date.toDateString()}`}
        />
      </td>
      <td title={build.uploaded}>{build.key}</td>
      <td>{compatibleNpmVersions(build.version)}</td>
      <td>{build.version}</td>
      <td class="hidden lg:table-cell">{formatBytes(build.size)}</td>
      <td class="hidden lg:table-cell">{Hashes(build)}</td>
      <td>
        <a class="underline" href={`/#tiles=${link}`}>
          map
        </a>
      </td>
      <td>
        <a
          class="underline"
          href={`https://protomaps.github.io/PMTiles/?url=${link}`}
        >
          xray
        </a>
      </td>
      <td class="hidden lg:table-cell">
        <a class="underline" href={link}>
          download
        </a>
      </td>
      <td class="hidden lg:table-cell">
        {dateStr >= "20231228" ? (
          <a class="underline" href={statsLink}>
            stats
          </a>
        ) : null}
      </td>
    </tr>
  );
}

interface MaperturePayload {
  name: string;
  type: string;
  renderer: string;
  url: string;
  index: number;
}

function Builds() {
  const [builds] = createResource(async () => {
    const resp = await fetch(
      "https://build-metadata.protomaps.dev/builds.json",
    );
    const j = await resp.json();
    return j.sort((a: Build, b: Build) => {
      if (a.key < b.key) return 1;
      if (a.key > b.key) return -1;
      return 0;
    });
  });
  const [latestStyle] = createResource(async () => {
    const resp = await fetch(
      "https://registry.npmjs.org/protomaps-themes-base",
      {
        headers: { Accept: "application/vnd.npm.install-v1+json" },
      },
    );
    const j = await resp.json();
    const versions = Object.keys(j.versions).filter(
      (v) => +v.split(".")[0] >= 2,
    );
    return versions[versions.length - 1];
  });

  // the index is starting from the top (0)
  const [cmpA, setCmpA] = createSignal<number>(1);
  const [cmpB, setCmpB] = createSignal<number>(0);

  const theme = "light";

  const openVisualTests = () => {
    const left = `https://build.protomaps.com/${builds()[cmpA()].key}`;
    const right = `https://build.protomaps.com/${builds()[cmpB()].key}`;
    open(`/visualtests/?leftTiles=${left}&rightTiles=${right}`);
  };

  const openMaperture = () => {
    const leftKey = builds()[cmpA()].key.replace(".pmtiles", "");
    const rightKey = builds()[cmpB()].key.replace(".pmtiles", "");
    const left: MaperturePayload = {
      name: `${leftKey} ${latestStyle()} ${theme}`,
      type: "maplibre-gl",
      renderer: "maplibre-gl",
      index: 0,
      url: `https://npm-style.protomaps.dev/style.json?version=${latestStyle()}&theme=${theme}&tiles=${leftKey}`,
    };
    const right: MaperturePayload = {
      name: `${rightKey} ${latestStyle()} ${theme}`,
      type: "maplibre-gl",
      renderer: "maplibre-gl",
      index: 0,
      url: `https://npm-style.protomaps.dev/style.json?version=${latestStyle()}&theme=${theme}&tiles=${rightKey}`,
    };
    const payload = JSON.stringify([left, right]);
    open(
      `https://stamen.github.io/maperture/#maps=${encodeURIComponent(payload)}`,
    );
  };

  return (
    <div class="flex flex-col h-screen w-full">
      <Nav page={1} />
      <div class="max-w-[1500px] mx-auto">
        <h1 class="my-8 text-4xl">Builds</h1>
        <p>
          See the{" "}
          <a
            class="underline"
            href="https://docs.protomaps.com/basemaps/downloads"
            target="_blank"
            rel="noreferrer"
          >
            Documentation
          </a>{" "}
          for how to use.
        </p>
        <div class="space-x-2 my-2">
          <button class="btn-primary" type="button" onClick={openVisualTests}>
            Compare selected versions
          </button>
          {latestStyle() ? (
            <button class="btn-primary" type="button" onClick={openMaperture}>
              Compare in Maperture
            </button>
          ) : null}
        </div>
        <table class="table-auto border-separate text-xs lg:text-base border-spacing-2 lg:border-spacing-4 font-mono">
          <thead class="text-sm">
            <tr>
              <th />
              <th>date</th>
              <th>
                compatible{" "}
                <a
                  class="underline"
                  href="https://www.npmjs.com/package/@protomaps/basemaps"
                  target="_blank"
                  rel="noreferrer"
                >
                  npm versions
                </a>
              </th>
              <th>tileset version</th>
              <th>size</th>
              <th>hashes</th>
              <th />
              <th />
              <th />
            </tr>
          </thead>
          <tbody>
            <For each={builds()}>
              {(build, idx) => (
                <BuildComponent
                  build={build}
                  idx={idx()}
                  cmpA={cmpA()}
                  cmpB={cmpB()}
                  setCmpA={setCmpA}
                  setCmpB={setCmpB}
                />
              )}
            </For>
          </tbody>
        </table>
      </div>
    </div>
  );
}

const root = document.getElementById("root");

if (root) {
  render(() => <Builds />, root);
}
