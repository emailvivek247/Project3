package net.javacoding.xsearch.cluster.search;

import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.Weight;

/**
   * Document Frequency cache acting as a Dummy-Searcher. This class is no
   * full-fledged Searcher, but only supports the methods necessary to
   * initialize Weights.
   */
  class CachedDfSource extends Searcher {
    private final Map dfMap; // Map from Terms to corresponding doc freqs

    private final int maxDoc; // document count

    public CachedDfSource(final Map dfMap, final int maxDoc, final Similarity similarity) {
      this.dfMap = dfMap;
      this.maxDoc = maxDoc;
      setSimilarity(similarity);
    }

    @Override
    public int docFreq(final Term term) {
      int df;
      try {
        df = ((Integer) dfMap.get(new TermWritable(term.field(), term.text()))).intValue();
      } catch (final NullPointerException e) {
        throw new IllegalArgumentException("df for term " + term.text() + " not available");
      }
      return df;
    }

    @Override
    public int[] docFreqs(final Term[] terms) {
      final int[] result = new int[terms.length];
      for (int i = 0; i < terms.length; i++) {
        result[i] = docFreq(terms[i]);
      }
      return result;
    }

    @Override
    public int maxDoc() {
      return maxDoc;
    }

    @Override
    public Query rewrite(final Query query) {
      // this is a bit of a hack. We know that a query which
      // creates a Weight based on this Dummy-Searcher is
      // always already rewritten (see preparedWeight()).
      // Therefore we just return the unmodified query here
      return query;
    }

    @Override
    public void close() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Document doc(final int i) {
      throw new UnsupportedOperationException();
    }

    public Document doc(final int i, final FieldSelector fieldSelector) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Explanation explain(final Weight weight, final int doc) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void search(final Weight weight, final Filter filter, final HitCollector results) {
      throw new UnsupportedOperationException();
    }

    @Override
    public TopDocs search(final Weight weight, final Filter filter, final int n) {
      throw new UnsupportedOperationException();
    }

    @Override
    public TopFieldDocs search(final Weight weight, final Filter filter, final int n, final Sort sort) {
      throw new UnsupportedOperationException();
    }
  }