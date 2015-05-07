package net.javacoding.xsearch.search.spellcheck;

import java.io.IOException;
import java.util.Iterator;

import net.javacoding.xsearch.search.spellcheck.DbsIndexWordDictionary.DbsIndexWordIterator;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.store.Directory;

// TODO: warm up phrase suggster
public class PhraseSuggester {

    /**
     * Field name for each phase in the phrase index.
     */
    public static final String FIELD_PHRASE = "p";
    public static final String FIELD_WORD   = "w";

    Directory                  phraseIndex;

    public IndexSearcher       searcher;
    public IndexReader         reader;

    public PhraseSuggester(Directory phraseIndex) throws IOException {
        this.setPhraseIndex(phraseIndex);
    }

    /**
     * Use a different index as the phrase checker index or re-open the existing
     * index if <code>phraseIndex</code> is the same value as given in the
     * constructor.
     * 
     * @param phraseIndex
     * @throws IOException
     */
    public void setPhraseIndex(Directory phraseIndex) throws IOException {
        // close the old searcher, if there was one
        close();
        this.phraseIndex = phraseIndex;
        open();
    }
    
    public void open() throws CorruptIndexException, IOException {
        if (IndexReader.indexExists(phraseIndex)) {
            reader = IndexReader.open(this.phraseIndex);
            searcher = new IndexSearcher(this.phraseIndex);
        }
    }

    public void close() {
        if (searcher != null) {
            try {
                searcher.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            searcher = null;
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reader = null;
        }
    }

    /**
     * Check whether the word exists in the index.
     * 
     * @param word
     * @throws IOException
     * @return true iff the word exists in the index
     */
    public boolean exist(String word) throws IOException {
        return searcher == null ? false : searcher.docFreq(new Term(FIELD_PHRASE, word)) > 0;
    }

    /**
     * Indexes the data from the given {@link Dictionary}.
     * 
     * @param dict
     *            Dictionary to index
     * @param mergeFactor
     *            mergeFactor to use when indexing
     * @param ramMB
     *            the max amount or memory in MB to use
     * @throws IOException
     */
    public void indexDictionary(Dictionary dict, int mergeFactor, int ramMB) throws IOException {
        close();

        IndexWriter writer = new IndexWriter(phraseIndex, new WhitespaceAnalyzer(), true, MaxFieldLength.UNLIMITED);
        writer.setMergeFactor(mergeFactor);
        writer.setRAMBufferSizeMB(ramMB);

        Iterator iter = dict.getWordsIterator();
        while (iter.hasNext()) {
            String phrase = (String) iter.next();

            int len = phrase.length();
            if (len < 3) {
                continue; // too short we bail but "too long" is fine...
            }

            if (this.exist(phrase)) { // if the word already exist in the
                                      // gramindex
                continue;
            }

            // ok index the word
            Document doc = createDocument(phrase);
            writer.addDocument(doc);
        }
        if(iter instanceof DbsIndexWordIterator) {
            ((DbsIndexWordIterator) iter).close();
        }
        // close writer
        writer.optimize();
        writer.close();
        writer = null;
        
        open();
    }

    /**
     * Indexes the data from the given {@link Dictionary}.
     * 
     * @param dict
     *            the dictionary to index
     * @throws IOException
     */
    public void indexDictionary(Dictionary dict) throws IOException {
        indexDictionary(dict, 300, 10);
    }

    private static int getMin(int l) {
        if (l > 5) {
            return 3;
        }
        if (l == 5) {
            return 2;
        }
        return 1;
    }

    private static int getMax(int l) {
        if (l > 5) {
            return 4;
        }
        if (l == 5) {
            return 3;
        }
        return l > 3 ? 3 : l;
    }

    private static Document createDocument(String text) {
        Document doc = new Document();
        doc.add(new Field(FIELD_PHRASE, text, Field.Store.YES, Field.Index.NOT_ANALYZED)); 
        for (String t : text.split("\\W")) {
            if (t != null && t.length() > 0) {
                //addGram(t, doc, getMin(t.length()), getMax(t.length()));
                doc.add(new Field("w", t.toLowerCase(), Field.Store.NO, Field.Index.NOT_ANALYZED));
            }
        }

        return doc;
    }

    private static void addGram(String text, Document doc, int ng1, int ng2) {
        text = text.toLowerCase();
        for (int ng = ng1; ng <= ng2; ng++) {
            String gram = text.substring(0, ng);
            doc.add(new Field("s" + ng, gram, Field.Store.NO, Field.Index.NOT_ANALYZED));
        }
    }
}
