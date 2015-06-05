/**
 * 
 */
package net.javacoding.xsearch.cluster.search;

import java.util.concurrent.Callable;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.Weight;

class SearchCall implements Callable<SearchResult> {

    /**
     * 
     */
    private final SDLMultiSearcher sdlMultiSearcher;
    private final String _shardName;
    private final Weight _weight;
    private final int _limit;

    public SearchCall(SDLMultiSearcher sdlMultiSearcher, String shardName, Weight weight, int limit) {
      this.sdlMultiSearcher = sdlMultiSearcher;
    _shardName = shardName;
      _weight = weight;
      _limit = limit;
    }

    public SearchResult call() throws Exception {
      final IndexSearcher indexSearcher = this.sdlMultiSearcher._searchers.get(_shardName);
      final TopDocs docs = indexSearcher.search(_weight, null, _limit);
      // totalHits += docs.totalHits; // update totalHits
      return new SearchResult(docs.totalHits, docs.scoreDocs);
    }

  }