/*
 */
package net.javacoding.xsearch.status;

import org.apache.lucene.index.Term;

public class TermFrequency {
    public TermFrequency(Term t, int df) {
        term = t;
        frequency = df;
    }

    int frequency;
    Term term;
    public int getFrequency() {
        return frequency;
    }
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
    public Term getTerm() {
        return term;
    }
    public void setTerm(Term term) {
        this.term = term;
    }
}
