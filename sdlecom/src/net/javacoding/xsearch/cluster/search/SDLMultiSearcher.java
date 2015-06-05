package net.javacoding.xsearch.cluster.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Weight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements search over a set of <code>Searchables</code>.
 * 
 * <p>
 * Applications usually need only call the inherited {@link #search(Query)} or
 * {@link #search(Query,Filter)} methods.
 */
public class SDLMultiSearcher {

  private static final Logger logger = LoggerFactory.getLogger(SDLMultiSearcher.class);

  final Map<String, IndexSearcher> _searchers = new ConcurrentHashMap<String, IndexSearcher>();
  private ExecutorService _threadPool = Executors.newFixedThreadPool(100);

  private final String _node;
  private int _maxDoc = 0;

  public SDLMultiSearcher(final String node) {
    _node = node;
  }

  /**
   * Adds an shard index search for given name to the list of shards
   * MultiSearcher search in.
   * 
   * @param shardKey
   * @param indexSearcher
   * @throws IOException
   */
  public void addShard(final String shardKey, final IndexSearcher indexSearcher) throws IOException {
    synchronized (_searchers) {
      _searchers.put(shardKey, indexSearcher);
      _maxDoc += indexSearcher.maxDoc();
    }
  }

  /**
   * 
   * Removes a search by given shardName from the list of searchers.
   */
  public void removeShard(final String shardName) {
    synchronized (_searchers) {
      final Searchable remove = _searchers.remove(shardName);
      if (remove == null) {
        return; // nothing to do.
      }
      try {
        _maxDoc -= remove.maxDoc();
      } catch (final IOException e) {
        throw new RuntimeException("unable to retrive maxDocs from searchable");
      }
    }

  }

  /**
   * Search in the given shards and return max hits for given query
   * 
   * @param query
   * @param freqs
   * @param shards
   * @param result
   * @param max
   * @throws IOException
   */
  public final void search(final Query query, final DocumentFrequenceWritable freqs, final String[] shards,
      final HitsMapWritable result, final int max) throws IOException {
    final Query rewrittenQuery = rewrite(query, shards);
    final int numDocs = freqs.getNumDocs();
    final CachedDfSource cacheSim = new CachedDfSource(freqs.getAll(), numDocs, new DefaultSimilarity());
    final Weight weight = rewrittenQuery.weight(cacheSim);
    // we can maximal found all docs in this system or maximal the requested
    final int limit = Math.min(numDocs, max);
    final ClusterSearchHitQueue hq = new ClusterSearchHitQueue(limit);
    int totalHits = 0;
    final int shardsCount = shards.length;

    // run the search parallel on the shards with a thread pool
    List<Future<SearchResult>> tasks = new ArrayList<Future<SearchResult>>();
    for (int i = 0; i < shardsCount; i++) {
      SearchCall call = new SearchCall(this, shards[i], weight, limit);
      Future<SearchResult> future = _threadPool.submit(call);
      tasks.add(future);
    }

    final ScoreDoc[][] scoreDocs = new ScoreDoc[shardsCount][];
    for (int i = 0; i < shardsCount; i++) {
      SearchResult searchResult;
      try {
        searchResult = tasks.get(i).get();
        totalHits += searchResult._totalHits;
        scoreDocs[i] = searchResult._scoreDocs;
      } catch (InterruptedException e) {
          logger.debug("Multithread shard search interrupted", e);
          throw new IOException("Multithread shard search interrupred.");
      } catch (ExecutionException e) {
    	  logger.debug("Multithread shard search could not be executed", e);
    	  throw new IOException("Multithread shard search could not be executed.");
      }
    }
   
    result.addTotalHits(totalHits);

    int pos = 0;
    boolean working = true;
    while (working) {
      ScoreDoc scoreDoc = null;
      for (int i = 0; i < scoreDocs.length; i++) {
        final ScoreDoc[] docs = scoreDocs[i];
        if (pos < docs.length) {
          scoreDoc = docs[pos];
          final SearchHit hit = new SearchHit(shards[i], _node, scoreDoc.score, scoreDoc.doc);
          if (!hq.insert(hit) || hq.size() == limit) {
            working = false;
            break;
          }
        }
      }
      pos++;
      if (scoreDoc == null) {
        // we do not have any data more
        break;
      }
    }

    for (int i = hq.size() - 1; i >= 0; i--) {
      final SearchHit hit = (SearchHit) hq.pop();
      if (hit != null) {
        result.addHitToShard(hit.getShard(), hit);
      }
    }
  }

  /**
   * Returns the number of documents a shard has.
   * 
   * @param shardName
   * @return
   */
  public int getNumDoc(final String shardName) {
    final Searchable searchable = _searchers.get(shardName);
    if (searchable != null) {
      final IndexSearcher indexSearcher = (IndexSearcher) searchable;
      return indexSearcher.getIndexReader().numDocs();
    }
    throw new IllegalArgumentException("shard " + shardName + " unknown");
  }

  /**
   * Returns the lucene document of a given shard.
   * 
   * @param shardName
   * @param docId
   * @return
   * @throws CorruptIndexException
   * @throws IOException
   */
  public Document doc(final String shardName, final int docId) throws CorruptIndexException, IOException {
    final Searchable searchable = _searchers.get(shardName);
    if (searchable != null) {
      return searchable.doc(docId);
    }
    throw new IllegalArgumentException("shard " + shardName + " unknown");
  }

  /**
   * Rewrites a query for the given shards
   * 
   * @param original
   * @param shardNames
   * @return
   * @throws IOException
   */
  public Query rewrite(final Query original, final String[] shardNames) throws IOException {
    final Query[] queries = new Query[shardNames.length];
    for (int i = 0; i < shardNames.length; i++) {
      final String shard = shardNames[i];
      queries[i] = _searchers.get(shard).rewrite(original);
    }
    if (queries.length > 0) {
      return queries[0].combine(queries);
    }
    return original;
  }

  /**
   * Returns the document frequence for a given term within a given shard.
   * 
   * @param shardName
   * @param term
   * @return
   * @throws IOException
   */
  public int docFreq(final String shardName, final Term term) throws IOException {
    int result = 0;
    final Searchable searchable = _searchers.get(shardName);
    if (searchable != null) {
      result = searchable.docFreq(term);
    } else {
    	logger.error("No shard with the name '" + shardName + "' on in this searcher.");
    }
    return result;
  }

  public void close() throws IOException {
    for (final Searchable searchable : _searchers.values()) {
      searchable.close();
    }
  }

}
