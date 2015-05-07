/**
 * 
 */
package net.javacoding.xsearch.cluster.search;

import org.apache.lucene.search.ScoreDoc;

class SearchResult {

    final int _totalHits;
    final ScoreDoc[] _scoreDocs;

    public SearchResult(int totalHits, ScoreDoc[] scoreDocs) {
      _totalHits = totalHits;
      _scoreDocs = scoreDocs;
    }

  }