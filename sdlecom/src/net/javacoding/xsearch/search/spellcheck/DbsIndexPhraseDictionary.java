package net.javacoding.xsearch.search.spellcheck;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.core.component.BloomFilter;
import net.javacoding.xsearch.indexer.ProxyAnalyzer;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.Dictionary;

public class DbsIndexPhraseDictionary implements Dictionary {

    protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    
    public static final String PHRASE_DELIMITER = "PRHASE_DELIMITER";

    DatasetConfiguration dc;
    List<Column>         columns;
    IndexReader          ir;
    private FieldSelector fs;

    public DbsIndexPhraseDictionary(DatasetConfiguration dc, IndexReader ir, List<Column> columns) {
        this.dc = dc;
        this.columns = columns;
        this.ir = ir;
        String[] fields = new String[columns.size()];
        for(int i=0;i<fields.length;i++) {
            fields[i] = columns.get(i).getColumnName();
        }
        fs = new MapFieldSelector(fields);
    }

    public Iterator getWordsIterator() {
        ProxyAnalyzer.initStopWord();
        return new DbsIndexPhraseIterator().setStopwords(ProxyAnalyzer.PUBLIC_STOP_WORDS);
    }
    public static boolean isStopword(String t) {
        ProxyAnalyzer.initStopWord();
        return ProxyAnalyzer.PUBLIC_STOP_WORDS==null? false : ProxyAnalyzer.PUBLIC_STOP_WORDS.contains(t);
    }

    final class DbsIndexPhraseIterator implements Iterator {
        private int    currentDocumentId;
        private Document currentDocument;
        private int    documentCount;
        private String currentString;
        private int    currentColumnIndex;
        private boolean currentColumnIsKeyword;
        private int    columnCount;
        
        private long last_report_time = 0;

        private BloomFilter seenPhrases;
        private Set<String> stopWords;

        private PhraseExtractor pe;

        DbsIndexPhraseIterator() {
            currentDocumentId = -1;
            currentString = null;
            documentCount = ir.maxDoc();
            columnCount = columns.size();
            currentColumnIndex = columnCount;
            seenPhrases = new BloomFilter(documentCount*16);
            stopWords = new HashSet<String>();
            pe = new PhraseExtractor(stopWords);
        }
        public void close() {
            //seenPhrases.close();
            seenPhrases = null;
            stopWords.clear();
            stopWords = null;
            pe = null;
        }
        public DbsIndexPhraseIterator setStopwords(Set<String> words) {
            if(words==null) return this;
            for(String s : words) {
                if(!U.isEmpty(s)) {
                    stopWords.add(s.toLowerCase());
                }
            }
            return this;
        }
        
        private boolean seekCurrentStringIfNull() {
            boolean hasChanged = false;
            while (currentDocumentId < documentCount && currentString == null) {
                try {
                    if(currentColumnIndex>=columnCount) {
                        currentDocumentId++;
                        while(currentDocumentId<documentCount && ir.isDeleted(currentDocumentId)) {
                            currentDocumentId++;
                        }
                        if(currentDocumentId<documentCount) {
                            currentDocument = ir.document(currentDocumentId,fs);
                        }
                        currentColumnIndex = 0;
                    }
                    if(currentDocumentId<documentCount) {
                        if(!ir.isDeleted(currentDocumentId)) {
                            currentString = currentDocument.get(columns.get(currentColumnIndex).getColumnName());
                            if(currentString!=null && !columns.get(currentColumnIndex).getIsKeyword()) {
                                currentString = currentString.toLowerCase();
                            }
                            if (columns.get(currentColumnIndex).getIsSimpleKeyword()) {
                                //since currentColumnIndex can increase, so this currentColumnIsKeyword points to the previous column
                                currentColumnIsKeyword = true; 
                            }else {
                                currentColumnIsKeyword = false;
                            }
                            pe.setString(currentString);
                            hasChanged = true;
                            long now = System.currentTimeMillis();
                            if ((now - last_report_time) > 1900) {
                                last_report_time = now;
                                logger.info("Processing "+ (currentDocumentId+1) +" of " + documentCount);
                            }
                        }
                        currentColumnIndex++;
                    }
                } catch (CorruptIndexException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return hasChanged;
        }

        public boolean hasNext() {
            return columnCount > 0 && currentDocumentId < documentCount || pe.hasNext();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
        public Object next() {
            if(seekCurrentStringIfNull()) {
                return PHRASE_DELIMITER;
            }
            if (currentColumnIsKeyword) {
                String t = currentString;
                currentString = null;
                return t;
            }
            if(!pe.hasNext()) {
                currentString = null;
                return PHRASE_DELIMITER;
            }
            String phrase = pe.next();
            
            if(phrase == PHRASE_DELIMITER) {
                return "";
            }

            if(seenPhrases.hasKey(phrase)) {
                return "";
            }else {
                seenPhrases.put(phrase);
            }
            
            return phrase;
        }

    }
}
