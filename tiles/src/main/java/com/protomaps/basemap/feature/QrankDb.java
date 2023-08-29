package com.protomaps.basemap.feature;

import com.carrotsearch.hppc.LongLongHashMap;
import java.io.*;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import javax.annotation.concurrent.Immutable;

/**
 * An in-memory representation of the entire QRank database used for generalizing
 * {@link com.protomaps.basemap.layers.Pois}.
 * <p>
 * Parses a copy of the gzipped QRank dataset into a long->long hash map that can be efficiently queried when processing
 * POI features.
 **/
@Immutable
public final class QrankDb {

  private final LongLongHashMap db;

  public QrankDb(LongLongHashMap db) {
    this.db = db;
  }

  public long get(long wikidataId) {
    return this.db.get(wikidataId);
  }

  public long get(String wikidataId) {
    long id = Long.parseLong(wikidataId.substring(1));
    return this.get(id);
  }

  public static QrankDb fromCsv(Path csvPath) throws IOException {
    GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(csvPath.toFile()));
    BufferedReader br = new BufferedReader(new InputStreamReader(gzip));

    String content;
    long startTime = System.nanoTime();

    LongLongHashMap db = new LongLongHashMap();
    br.readLine(); // header
    while ((content = br.readLine()) != null) {
      var split = content.split(",");
      long id = Long.parseLong(split[0].substring(1));
      long rank = Long.parseLong(split[1]);
      db.put(id, rank);
    }
    long endTime = System.nanoTime();
    long elapsedTimeMillis = (endTime - startTime) / 1_000_000;
    System.out.println(elapsedTimeMillis);
    return new QrankDb(db);
  }
}
