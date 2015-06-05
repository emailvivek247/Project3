package net.javacoding.xsearch.cluster.search;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HitsMapWritable implements Writable {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private String _serverName;
  private final Map<String, List<SearchHit>> _hitToShard = new ConcurrentHashMap<String, List<SearchHit>>();
  private int _totalHits;

  public HitsMapWritable() {
    // for serialization
  }

  public HitsMapWritable(final String serverName) {
    _serverName = serverName;
  }

  public void readFields(final DataInput in) throws IOException {
    long start = 0;
    start = System.currentTimeMillis();
    _serverName = in.readUTF();
    _totalHits = in.readInt();
    logger.debug("HitsMap reading start at: " + start + " for server " + _serverName);
    final int shardSize = in.readInt();
    for (int i = 0; i < shardSize; i++) {
      final String shardName = in.readUTF();
      final int hitSize = in.readInt();
      for (int j = 0; j < hitSize; j++) {
        final float score = in.readFloat();
        final int docId = in.readInt();
        final SearchHit hit = new SearchHit(shardName, _serverName, score, docId);
        addHitToShard(shardName, hit);
      }
    }
    final long end = System.currentTimeMillis();
    logger.debug("HitsMap reading took " + (end - start) / 1000.0 + "sec.");    
  }

  public void write(final DataOutput out) throws IOException {
    long start = 0;
    start = System.currentTimeMillis();
    out.writeUTF(_serverName);
    out.writeInt(_totalHits);
    final Set<String> keySet = _hitToShard.keySet();
    out.writeInt(keySet.size());
    for (final String key : keySet) {
      out.writeUTF(key);
      final List<SearchHit> list = _hitToShard.get(key);
      out.writeInt(list.size());
      for (final SearchHit hit : list) {
        out.writeFloat(hit.getScore());
        out.writeInt(hit.getDocId());
      }
    }
    final long end = System.currentTimeMillis();
    logger.debug("HitsMap writing took " + (end - start) / 1000.0 + "sec.");
    logger.debug("HitsMap writing ended at: " + end + " for server " + _serverName);    
  }

  public void addHitToShard(final String shard, final SearchHit hit) {
    List<SearchHit> hitList = _hitToShard.get(shard);
    if (hitList == null) {
      hitList = new ArrayList<SearchHit>();
      _hitToShard.put(shard, hitList);
    }
    hitList.add(hit);
  }

  public String getServerName() {
    return _serverName;
  }

  public SearchHits getHits() {
    final SearchHits result = new SearchHits();
    result.setTotalHits(_totalHits);
    for (final List<SearchHit> hitList : _hitToShard.values()) {
      result.addHits(hitList);
    }
    return result;
  }

  public void addTotalHits(final int length) {
    _totalHits += length;
  }

  public int getTotalHits() {
    return _totalHits;
  }
}
