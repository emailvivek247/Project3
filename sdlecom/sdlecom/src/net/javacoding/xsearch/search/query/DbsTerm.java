package net.javacoding.xsearch.search.query;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** A single-term query clause. */
public class DbsTerm {
    private String text;

    public DbsTerm(String text) {
        this.text = text;
    }

    public void write(DataOutput out) throws IOException {
        out.writeUTF(text);
    }

    public static DbsTerm read(DataInput in) throws IOException {
        String text = in.readUTF();
        return new DbsTerm(text);
    }

    public String toString() {
        return text;
    }

    public boolean equals(Object o) {
        if (!(o instanceof DbsTerm))
            return false;
        DbsTerm other = (DbsTerm) o;
        return text == null ? other.text == null : text.equals(other.text);
    }

    public int hashCode() {
        return text != null ? text.hashCode() : 0;
    }
}