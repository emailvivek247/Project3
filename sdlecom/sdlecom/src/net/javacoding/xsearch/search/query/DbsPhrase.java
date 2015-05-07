package net.javacoding.xsearch.search.query;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** A phrase query clause. */
public class DbsPhrase {
    private DbsTerm[] terms;

    public DbsPhrase(DbsTerm[] terms) {
        this.terms = terms;
    }

    public DbsPhrase(String[] terms) {
        this.terms = new DbsTerm[terms.length];
        for (int i = 0; i < terms.length; i++) {
            this.terms[i] = new DbsTerm(terms[i]);
        }
    }

    public DbsTerm[] getTerms() {
        return terms;
    }

    public void write(DataOutput out) throws IOException {
        out.writeByte(terms.length);
        for (int i = 0; i < terms.length; i++)
            terms[i].write(out);
    }

    public static DbsPhrase read(DataInput in) throws IOException {
        int length = in.readByte();
        DbsTerm[] terms = new DbsTerm[length];
        for (int i = 0; i < length; i++)
            terms[i] = DbsTerm.read(in);
        return new DbsPhrase(terms);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("\"");
        for (int i = 0; i < terms.length; i++) {
            buffer.append(terms[i].toString());
            if (i != terms.length - 1)
                buffer.append(" ");
        }
        buffer.append("\"");
        return buffer.toString();
    }

    public boolean equals(Object o) {
        if (!(o instanceof DbsPhrase))
            return false;
        DbsPhrase other = (DbsPhrase) o;
        if (!(this.terms.length == this.terms.length))
            return false;
        for (int i = 0; i < terms.length; i++) {
            if (!this.terms[i].equals(other.terms[i]))
                return false;
        }
        return true;
    }

    public int hashCode() {
        int hashCode = terms.length;
        for (int i = 0; i < terms.length; i++) {
            hashCode ^= terms[i].hashCode();
        }
        return hashCode;
    }

}