package net.javacoding.xsearch.search;

import java.io.IOException;

import net.javacoding.xsearch.lib.LayeredIntArrayList;
import net.javacoding.xsearch.lib.LayeredStringArrayList;
import net.javacoding.xsearch.search.impl.KeywordsFieldCacheImpl;

import org.apache.lucene.index.IndexReader;

public interface KeywordsFieldCache {

    public static KeywordsFieldCache DEFAULT = new KeywordsFieldCacheImpl();

    /** Checks the internal cache for an appropriate entry, and if none
     * is found, reads the term values in <code>field</code> and returns an array
     * of size <code>reader.maxDoc()</code> containing the value each document
     * has in the given field.
     * @param reader  Used to get field values.
     * @param field   Which field contains the strings.
     * @return The values in the given field for each document.
     * @throws IOException  If any error occurs.
     */
    public LayeredStringArrayList getStrings (IndexReader reader, String field) throws IOException;
    public LayeredIntArrayList getInts (IndexReader reader, String field) throws IOException;
    public int[] getIntArray (IndexReader reader, String field) throws IOException;
}
