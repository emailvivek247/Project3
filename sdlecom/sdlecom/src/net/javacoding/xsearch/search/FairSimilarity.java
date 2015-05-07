package net.javacoding.xsearch.search;

import org.apache.lucene.search.DefaultSimilarity;

public class FairSimilarity extends DefaultSimilarity {

	public float lengthNorm(String fieldName, int numTerms) {
		return (float) (1.0 / numTerms);
	}

	public float tf(float freq) {
		return (float) freq;
	}

}
