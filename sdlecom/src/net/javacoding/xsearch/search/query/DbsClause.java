package net.javacoding.xsearch.search.query;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


/** A query clause. */
public class DbsClause {
    public static final String DEFAULT_FIELD  = "DEFAULT";

    private static final byte  REQUIRED_BIT   = 1;
    private static final byte  PROHIBITED_BIT = 2;
    private static final byte  PHRASE_BIT     = 4;

    private boolean            isRequired;
    private boolean            isProhibited;
    private String             field          = DEFAULT_FIELD;
    private float              weight         = 1.0f;
    private Object             termOrPhrase;
    private int                slop           = 0;

    public DbsClause(DbsTerm term, String field, boolean isRequired, boolean isProhibited) {
        this(term, isRequired, isProhibited);
        this.field = field;
    }

    public DbsClause(DbsTerm term, boolean isRequired, boolean isProhibited) {
        this.isRequired = isRequired;
        this.isProhibited = isProhibited;
        this.termOrPhrase = term;
    }

    public DbsClause(DbsPhrase phrase, String field, String slop, boolean isRequired, boolean isProhibited) {
        this(phrase, isRequired, isProhibited);
        this.field = field;
        this.slop = net.javacoding.xsearch.utility.U.getInt(slop, 0);
    }

    public DbsClause(DbsPhrase phrase, String field, boolean isRequired, boolean isProhibited) {
        this(phrase, isRequired, isProhibited);
        this.field = field;
    }

    public DbsClause(DbsPhrase phrase, boolean isRequired, boolean isProhibited) {
        this.isRequired = isRequired;
        this.isProhibited = isProhibited;
        this.termOrPhrase = phrase;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public boolean isProhibited() {
        return isProhibited;
    }

    public String getField() {
        return field;
    }

    public int getSlop() {
        return slop;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public boolean isPhrase() {
        return termOrPhrase instanceof DbsPhrase;
    }

    public DbsPhrase getPhrase() {
        return (DbsPhrase) termOrPhrase;
    }

    public DbsTerm getTerm() {
        return (DbsTerm) termOrPhrase;
    }

    public void write(DataOutput out) throws IOException {
        byte bits = 0;
        if (isPhrase())
            bits |= PHRASE_BIT;
        if (isRequired)
            bits |= REQUIRED_BIT;
        if (isProhibited)
            bits |= PROHIBITED_BIT;
        out.writeByte(bits);
        out.writeUTF(field);
        out.writeFloat(weight);

        if (isPhrase())
            getPhrase().write(out);
        else
            getTerm().write(out);
    }

    public static DbsClause read(DataInput in) throws IOException {
        byte bits = in.readByte();
        boolean required = ((bits & REQUIRED_BIT) != 0);
        boolean prohibited = ((bits & PROHIBITED_BIT) != 0);

        String field = in.readUTF();
        float weight = in.readFloat();

        DbsClause clause;
        if ((bits & PHRASE_BIT) == 0) {
            clause = new DbsClause(DbsTerm.read(in), field, required, prohibited);
        } else {
            clause = new DbsClause(DbsPhrase.read(in), field, required, prohibited);
        }

        clause.weight = weight;
        return clause;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        // if (isRequired)
        // buffer.append("+");
        // else
        if (isProhibited)
            buffer.append("-");

        if (!DEFAULT_FIELD.equals(field)) {
            buffer.append(field);
            buffer.append(":");
        }

        buffer.append(termOrPhrase.toString());

        return buffer.toString();
    }

    public String valueToString() {
        return isProhibited ? "-" + termOrPhrase : termOrPhrase.toString();
    }

    public boolean equals(Object o) {
        if (!(o instanceof DbsClause))
            return false;
        DbsClause other = (DbsClause) o;
        return (this.isRequired == other.isRequired) && (this.isProhibited == other.isProhibited) && (this.weight == other.weight)
            && (this.termOrPhrase == null ? other.termOrPhrase == null : this.termOrPhrase.equals(other.termOrPhrase));
    }

    public int hashCode() {
        return (this.isRequired ? 0 : 1) ^ (this.isProhibited ? 2 : 4) ^ Float.floatToIntBits(this.weight) ^ (this.termOrPhrase != null ? termOrPhrase.hashCode() : 0);
    }

}