/**
 * 
 */
package net.javacoding.xsearch.cluster.search;

import org.apache.lucene.util.PriorityQueue;

class ClusterSearchHitQueue extends PriorityQueue {
    ClusterSearchHitQueue(final int size) {
      initialize(size);
    }

    @Override
    protected final boolean lessThan(final Object a, final Object b) {
      final SearchHit hitA = (SearchHit) a;
      final SearchHit hitB = (SearchHit) b;
      if (hitA.getScore() == hitB.getScore()) {
        // todo this of cource do not work since we have same shardKeys
        // (should we increment docIds?)
        return hitA.getDocId() > hitB.getDocId();
      }
      return hitA.getScore() < hitB.getScore();
    }
  }