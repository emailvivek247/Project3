package net.javacoding.xsearch.search.function;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.search.impl.DateWeightedSortComparator;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ComplexExplanation;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Weight;

/**
 * A Query that sets the scores of document based on time-based weight
 */
public class TimeWeightQuery extends Query {
    private Query subQuery;
    private DatasetConfiguration dc = null;

    private Comparable[] cachedValues;

    public TimeWeightQuery(Query subQuery, DatasetConfiguration dc, IndexReader reader) throws IOException {
        this.dc = dc;
        this.subQuery = subQuery;
        String field = dc.getDateWeightColumnName();
        if(field!=null){
            cachedValues = FieldCache.DEFAULT.getCustom(reader, field, new DateWeightedSortComparator(dc));
        }
    }

    public Query rewrite(IndexReader reader) throws IOException {
        Query rewritten = subQuery.rewrite(reader);
        if (rewritten != subQuery) {
            TimeWeightQuery clone = (TimeWeightQuery)this.clone();
            clone.subQuery = rewritten;
            return clone;
        } else {
            return this;
        }
	}

	public void extractTerms(Set terms) {
		subQuery.extractTerms(terms);
    }

    private class TimeBasedWeight implements Weight {
        Similarity similarity;
        Weight subQueryWeight;
        float value;

        public TimeBasedWeight(Searcher searcher) throws IOException {
            this.similarity = getSimilarity(searcher);
            this.subQueryWeight = subQuery.weight(searcher); 
        }

        public Query getQuery() {
            return TimeWeightQuery.this;
        }

        public float getValue() {
            return value;
        }

        public float sumOfSquaredWeights() throws IOException {
            float sum = subQueryWeight.sumOfSquaredWeights();
            sum *= getBoost() * getBoost(); // boost each sub-weight
            return sum ;
        }

        public void normalize(float norm) {
            //norm *= getBoost(); // incorporate boost
            subQueryWeight.normalize(norm);
            value = subQueryWeight.getValue() * getBoost();
        }

        public Scorer scorer(IndexReader reader) throws IOException {
            Scorer subQueryScorer = subQueryWeight.scorer(reader);
            return new TimeWeightScorer(similarity, reader, subQueryScorer);
        }

        public Explanation explain(IndexReader reader, int doc) throws IOException {
            if(cachedValues!=null&&cachedValues[doc]!=null){
            	Explanation exp = subQueryWeight.explain(reader, doc);
                Explanation result = new ComplexExplanation(true, (Float)cachedValues[doc]*exp.getValue(), "time weighted query score is product of");
                result.addDetail(new Explanation((Float)cachedValues[doc], "time-weight"));
                result.addDetail(subQueryWeight.explain(reader, doc));
                return result;
            }
            return subQueryWeight.explain(reader, doc);
        }
    }

    /**
     * A scorer that (simply) matches all documents, and scores each document
     * with the value of the value soure in effect. As an example, if the value
     * source is a (cached) field source, then value of that field in that
     * document will be used. (assuming field is indexed for this doc, with a
     * single token.)
     */
    private class TimeWeightScorer extends Scorer {
        private Scorer subQueryScorer;

        // constructor
        private TimeWeightScorer(Similarity similarity, IndexReader reader, Scorer subQueryScorer) throws IOException {
            super(similarity);
            this.subQueryScorer = subQueryScorer;
        }

        public boolean next() throws IOException {
        	return this.subQueryScorer.next();
        }

        public int doc() {
            return subQueryScorer.doc();
        }

        public float score() throws IOException {
            if(cachedValues==null||cachedValues[subQueryScorer.doc()]==null){
                return subQueryScorer.score();
            }else{
                return subQueryScorer.score() * (Float)cachedValues[subQueryScorer.doc()];
            }
        }

        public boolean skipTo(int target) throws IOException {
            return subQueryScorer.skipTo(target);
        }

        public Explanation explain(int doc) throws IOException {
        	//not used at all
            return null;
        }
    }

    protected Weight createWeight(Searcher searcher) throws IOException {
        return new TimeWeightQuery.TimeBasedWeight(searcher);
    }

    public String toString(String field) {
        return "Time Based Weight("+subQuery.toString(field)+")";
    }

    /** Returns true if <code>o</code> is equal to this. */
    public boolean equals(Object o) {
        if (getClass() != o.getClass()) {
            return false;
        }
        TimeWeightQuery other = (TimeWeightQuery) o;
        if(cachedValues==null){
            return this.getBoost() == other.getBoost() && this.dc.getName().equals(other.dc.getName()) && this.subQuery.equals(other.subQuery);
        }else{
            return this.getBoost() == other.getBoost() && this.cachedValues == other.cachedValues && this.subQuery.equals(other.subQuery);
        }
    }

    /** Returns a hash code value for this object. */
    public int hashCode() {
        if(cachedValues==null){
            return (getClass().hashCode() + dc.hashCode() + subQuery.hashCode()) ^ Float.floatToIntBits(getBoost());
        }else{
            return (getClass().hashCode() + Arrays.hashCode(cachedValues) + subQuery.hashCode()) ^ Float.floatToIntBits(getBoost());
        }
    }
}
