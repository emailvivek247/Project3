/*
 * Created on Apr 20, 2007
 */
package net.javacoding.xsearch.search.synonym;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;

public class DefaultSynomymEngine implements SynonymEngine {

    IndexSearcher searcher = null;

    public DefaultSynomymEngine(Directory synonymIndexDirectory) {
        try {
            if (synonymIndexDirectory != null && IndexReader.indexExists(synonymIndexDirectory)) {
                searcher = new IndexSearcher(synonymIndexDirectory);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * synonym index has
     *  field "word": the main word
     *  field "syn": synonyms for the word
     */
    public String[] getSynonyms(String word) throws IOException {
        if(searcher==null) return null;
        ArrayList<String> synList = new ArrayList<String>();
        Hits hits = searcher.search(new TermQuery(new Term(WORD_FIELD, word)));
        
        if(hits.length()>0) {
            for (int i = 0; i < hits.length(); i++) {
                Document doc = hits.doc(i);
                String[] values = doc.getValues(SYNONYM_FIELD);

                for (int j = 0; j < values.length; j++) {
                    synList.add(values[j]);
                }
            }
            synList.add(word);
        }

        return (String[]) synList.toArray(new String[0]);
    }

}
