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

public class DbsIndexWordDictionary implements Dictionary {

    protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    DatasetConfiguration dc;
    List<Column>         columns;
    IndexReader          ir;
    private FieldSelector fs;

    public DbsIndexWordDictionary(DatasetConfiguration dc, IndexReader ir, List<Column> columns) {
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
        return new DbsIndexWordIterator().setStopwords(ProxyAnalyzer.PUBLIC_STOP_WORDS);
    }

    final class DbsIndexWordIterator implements Iterator {
        private String actualTerm;
        private int    currentDocumentId;
        private Document currentDocument;
        private int    documentCount;
        private String currentString;
        private int    currentStringIndex;
        private int    nextStringIndex;
        private int    currentColumnIndex;
        private int    columnCount;
        
        private long last_report_time = 0;

        private BloomFilter seenWords;
        private Set<String> stopWords;

        DbsIndexWordIterator() {
            currentDocumentId = -1;
            currentString = null;
            currentStringIndex = 0;
            documentCount = ir.maxDoc();
            columnCount = columns.size();
            currentColumnIndex = columnCount;
            seenWords = new BloomFilter(documentCount*16);
            stopWords = new HashSet<String>();
        }
        public void close() {
            //seenWords.close();
            seenWords = null;
            stopWords.clear();
            stopWords = null;
        }
        public DbsIndexWordIterator setStopwords(Set<String> words) {
            if(words==null) return this;
            for(String s : words) {
                if(!U.isEmpty(s)) {
                    stopWords.add(s.toLowerCase());
                }
            }
            return this;
        }
        
        private void maybeResetCurrentString() {
            if (currentString != null && currentStringIndex >= currentString.length()) {
                currentString = null;
                currentStringIndex = 0;
            }
        }
        private void seekCurrentString() {
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
                            long now = System.currentTimeMillis();
                            if ((now - last_report_time) > 3100) {
                                last_report_time = now;
                                logger.info("Processing "+ (currentDocumentId+1) +" of " + documentCount);
                            }
                        }
                        currentColumnIndex++;
                    }
                    //if(currentString!=null) {
                    //    System.out.print("]\nGetting "+columns.get(currentColumnIndex-1).getColumnName()+":"+currentString+"=>[");
                    //}
                } catch (CorruptIndexException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        private boolean isValidChar(char x) {
            return Character.isLetterOrDigit(x) ||x=='-'||x=='_';
        }

        public Object next() {
            maybeResetCurrentString();
            seekCurrentString();

            if (currentString == null)
                return "";
            int currentStringLength = currentString.length();

            while (currentStringIndex < currentStringLength && !isValidChar(currentString.charAt(currentStringIndex))) {
                currentStringIndex++;
            }
            nextStringIndex = currentStringIndex + 1;
            while (nextStringIndex < currentStringLength && isValidChar(currentString.charAt(nextStringIndex))) {
                nextStringIndex++;
            }
            if (currentStringIndex < currentStringLength) {
                actualTerm = currentString.substring(currentStringIndex, nextStringIndex);
                currentStringIndex = nextStringIndex;
            } else {
                return "";
            }

            String ret = actualTerm;
            if(stopWords!=null&&stopWords.contains(ret.toLowerCase())) {
                return "";
            }
            if(seenWords.hasKey(ret)) {
                return "";
            }else {
                seenWords.put(ret);
            }
            return ret;
        }

        public boolean hasNext() {
            return columnCount > 0 && currentDocumentId < documentCount;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
