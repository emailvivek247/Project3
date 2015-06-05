package net.javacoding.xsearch.search.function;

import java.io.IOException;
import java.util.Random;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ComplexExplanation;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Weight;

/**
 * A Query that sets the scores of document to a random number based on a seed
 */
public class RandomQuery extends Query {
    private static final long serialVersionUID = 3940757706414720713L;
    private Query subQuery;
    private int seed = 0;
    Random r = new Random();

    public RandomQuery(Query subQuery, int seed) throws IOException {
        this.subQuery = subQuery;
        this.seed = seed;
    }

    public Query rewrite(IndexReader reader) throws IOException {
        Query rewritten = subQuery.rewrite(reader);
        if (rewritten != subQuery) {
            RandomQuery clone = (RandomQuery)this.clone();
            clone.subQuery = rewritten;
            return clone;
        } else {
            return this;
        }
	}

	public void extractTerms(Set terms) {
		subQuery.extractTerms(terms);
    }

    private class RandomWeight implements Weight {
        Similarity similarity;
        Weight subQueryWeight;
        float value;

        public RandomWeight(Searcher searcher) throws IOException {
            this.similarity = getSimilarity(searcher);
            this.subQueryWeight = subQuery.weight(searcher); 
        }

        public Query getQuery() {
            return RandomQuery.this;
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
            return new RandomScorer(similarity, reader, subQueryScorer);
        }

        public Explanation explain(IndexReader reader, int doc) throws IOException {
        	Explanation exp = subQueryWeight.explain(reader, doc);
            Explanation result = new ComplexExplanation(true, hash(doc+seed)*exp.getValue()/((float)(1 << 30)), "random query score");
            result.addDetail(new Explanation(hash(doc+seed) / ((float)(1 << 30)), "random-weight, seed "+seed));
            result.addDetail(subQueryWeight.explain(reader, doc));
            return result;
        }
    }

    /**
     * A scorer that (simply) matches all documents, and scores each document
     * with the value of the value soure in effect. As an example, if the value
     * source is a (cached) field source, then value of that field in that
     * document will be used. (assuming field is indexed for this doc, with a
     * single token.)
     */
    private class RandomScorer extends Scorer {
        private Scorer subQueryScorer;

        // constructor
        private RandomScorer(Similarity similarity, IndexReader reader, Scorer subQueryScorer) throws IOException {
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
            return hash(subQueryScorer.doc()+seed) / ((float)(1 << 30));
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
        return new RandomQuery.RandomWeight(searcher);
    }

    public String toString(String field) {
        return "Random Weight(seed:"+seed+","+subQuery.toString(field)+")";
    }

    /** Returns true if <code>o</code> is equal to this. */
    public boolean equals(Object o) {
        if (getClass() != o.getClass()) {
            return false;
        }
        RandomQuery other = (RandomQuery) o;
        return this.getBoost() == other.getBoost() && this.subQuery.equals(other.subQuery) && this.seed == other.seed;
    }

    /** Returns a hash code value for this object. */
    public int hashCode() {
        return (getClass().hashCode() + subQuery.hashCode()+ hash(this.seed)) ^ Float.floatToIntBits(getBoost());
    }

    // Thomas Wang's hash32shift function, from http://www.cris.com/~Ttwang/tech/inthash.htm
    // slightly modified to return only positive integers.
    private static int hash(int key) {
      key = ~key + (key << 15); // key = (key << 15) - key - 1;
      key = key ^ (key >>> 12);
      key = key + (key << 2);
      key = key ^ (key >>> 4);
      key = key * 2057; // key = (key + (key << 3)) + (key << 11);
      key = key ^ (key >>> 16);
      return key >>> 1; 
    }

}
