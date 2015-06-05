/*
 * Created on Apr 20, 2007
 */
package net.javacoding.xsearch.search.synonym;

import java.io.IOException;

public interface SynonymEngine {
    public static final String WORD_FIELD = "word";
    public static final String SYNONYM_FIELD = "syn";
    String[] getSynonyms(String s) throws IOException;
}
