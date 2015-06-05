package net.javacoding.xsearch.search.query;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

import net.javacoding.xsearch.search.analysis.ParseException;
import net.javacoding.xsearch.search.analysis.QueryAnalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DbsQuery {
    public static final Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.search.DbsQuery");

    private ArrayList<DbsClause>     clauses       = new ArrayList<DbsClause>();

    private static final DbsClause[] CLAUSES_PROTO = new DbsClause[0];

    /** Return all clauses. */
    public DbsClause[] getClauses() {
        return clauses.toArray(CLAUSES_PROTO);
    }

    public void addTerm(String term, String field, boolean isRequired, boolean isProhibited) {
        clauses.add(new DbsClause(new DbsTerm(term), field, isRequired, isProhibited));
    }

    public void addPhrase(String[] terms, String field, String slop, boolean isRequired, boolean isProhibited) {
        if (terms.length == 0) { // ignore empty phrase
        } else if (terms.length == 1 && slop == null) {
            addTerm(terms[0], field, isRequired, isProhibited); // optimize to term query
        } else {
            clauses.add(new DbsClause(new DbsPhrase(terms), field, slop, isRequired, isProhibited));
        }
    }

    /** Add a required term in the default field. */
    public void addRequiredTerm(String term) {
        addRequiredTerm(term, DbsClause.DEFAULT_FIELD);
    }

    /** Add a required term in a specified field. */
    public void addRequiredTerm(String term, String field) {
        clauses.add(new DbsClause(new DbsTerm(term), field, true, false));
    }

    /** Add a required phrase in the default field. */
    public void addRequiredPhrase(String[] terms) {
        addRequiredPhrase(terms, DbsClause.DEFAULT_FIELD, null);
    }

    /** Add a required phrase in the specified field. */
    public void addRequiredPhrase(String[] terms, String field, String slop) {
        if (terms.length == 0) { // ignore empty phrase
        } else if (terms.length == 1 && slop == null) {
            addRequiredTerm(terms[0], field); // optimize to term query
        } else {
            clauses.add(new DbsClause(new DbsPhrase(terms), field, slop, true, false));
        }
    }

    /** Add a prohibited term in the default field. */
    public void addProhibitedTerm(String term) {
        addProhibitedTerm(term, DbsClause.DEFAULT_FIELD);
    }

    /** Add a prohibited term in the specified field. */
    public void addProhibitedTerm(String term, String field) {
        clauses.add(new DbsClause(new DbsTerm(term), field, false, true));
    }

    /** Add a prohibited phrase in the default field. */
    public void addProhibitedPhrase(String[] terms) {
        addProhibitedPhrase(terms, DbsClause.DEFAULT_FIELD);
    }

    /** Add a prohibited phrase in the specified field. */
    public void addProhibitedPhrase(String[] terms, String field) {
        if (terms.length == 0) { // ignore empty phrase
        } else if (terms.length == 1) {
            addProhibitedTerm(terms[0], field); // optimize to term query
        } else {
            clauses.add(new DbsClause(new DbsPhrase(terms), field, false, true));
        }
    }

    public void write(DataOutput out) throws IOException {
        out.writeByte(clauses.size());
        for (int i = 0; i < clauses.size(); i++)
            clauses.get(i).write(out);
    }

    public static DbsQuery read(DataInput in) throws IOException {
        DbsQuery result = new DbsQuery();
        result.readFields(in);
        return result;
    }

    public void readFields(DataInput in) throws IOException {
        clauses.clear();
        int length = in.readByte();
        for (int i = 0; i < length; i++)
            clauses.add(DbsClause.read(in));
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < clauses.size(); i++) {
            buffer.append(clauses.get(i).toString());
            if (i != clauses.size() - 1)
                buffer.append(" ");
        }
        return buffer.toString();
    }

    public boolean equals(Object o) {
        if (!(o instanceof DbsQuery))
            return false;
        DbsQuery other = (DbsQuery) o;
        return this.clauses.equals(other.clauses);
    }

    public int hashCode() {
        return this.clauses.hashCode();
    }

    /**
     * Flattens a query into the set of text terms that it contains. These are
     * terms which should be higlighted in matching documents.
     */
    public String[] getTerms() {
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < clauses.size(); i++) {
            DbsClause clause = clauses.get(i);
            if (!clause.isProhibited()) {
                if (clause.isPhrase()) {
                    DbsTerm[] terms = clause.getPhrase().getTerms();
                    for (int j = 0; j < terms.length; j++) {
                        result.add(terms[j].toString());
                    }
                } else {
                    result.add(clause.getTerm().toString());
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /** Parse a query from a string. */
    public static DbsQuery parse(String query) throws ParseException {
        return QueryAnalysis.parseQuery(query);
        /*
         * //TODO: make parsing query Query q = new Query();
         * java.util.StringTokenizer st = new java.util.StringTokenizer(query);
         * while (st.hasMoreTokens()) { q.addRequiredTerm(st.nextToken()); }
         * return q;
         */
    }

    public String getUserInput() {
        StringBuilder sb = new StringBuilder();
        boolean hasInput = false;
        for (DbsClause clause : clauses) {
            if (clause.getField() == DbsClause.DEFAULT_FIELD) {
                if(hasInput) {
                    sb.append(" ");
                }
                sb.append(clause.toString());
                hasInput = true;
            }
        }
        return sb.toString();
    }
}
