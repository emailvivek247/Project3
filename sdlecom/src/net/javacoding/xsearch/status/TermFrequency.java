package net.javacoding.xsearch.status;

public class TermFrequency {

    private final int frequency;
    private final Term term;

    public TermFrequency(String field, String value, int frequency) {
        this.term = new Term(field, value);
        this.frequency = frequency;
    }

    public int getFrequency() {
        return frequency;
    }

    public Term getTerm() {
        return term;
    }

    public class Term {

        private String field;
        private String value;

        public Term(String field, String value) {
            this.field = field;
            this.value = value;
        }

        public String field() {
            return field;
        }

        public String value() {
            return value;
        }

        public String text() {
            return value;
        }
    }
}
