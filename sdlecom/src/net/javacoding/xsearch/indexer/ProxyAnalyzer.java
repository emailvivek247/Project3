/*
 * Created on Apr 22, 2007
 */
package net.javacoding.xsearch.indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.search.synonym.DefaultSynomymEngine;
import net.javacoding.xsearch.search.synonym.SynonymEngine;
import net.javacoding.xsearch.search.synonym.SynonymFilter;
import net.javacoding.xsearch.search.synonym.SynonymManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

public class ProxyAnalyzer extends Analyzer {
    
    private static Logger logger = LoggerFactory.getLogger(ProxyAnalyzer.class);
    
    public static Set<String> PUBLIC_STOP_WORDS = null;
    public static boolean public_stop_words_initialized = false;
    public static Set<String> getStopwords() {
        initStopWord();
        return PUBLIC_STOP_WORDS;
    }
    public static synchronized void initStopWord(){
        try {
            if(!public_stop_words_initialized && PUBLIC_STOP_WORDS==null) {
                PUBLIC_STOP_WORDS = WordlistLoader.getWordSet(SynonymManager.getStopWordsListFile());
                public_stop_words_initialized = true;
            }
        } catch (FileNotFoundException e) {
            logger.warn("Could not find stopwords file:"+SynonymManager.getStopWordsListFile(),e);
        } catch (IOException e) {
            logger.warn("Failed to load stopwords file:"+SynonymManager.getStopWordsListFile(),e);
        }
    }

    private Analyzer analyzer = null;
    private SynonymEngine synonymEngine = null;
    
    private Set<String> stopWords;

    public ProxyAnalyzer(DatasetConfiguration dc, Analyzer analyzer, boolean hasSynonyms, boolean hasStopwords) {
        this.analyzer = analyzer;
        if(hasSynonyms) {
            File dir = SynonymManager.getSynonymIndexDirectory(dc,analyzer);
            if(dir!=null && dir.exists() && IndexReader.indexExists(dir)) {
                try {
                    this.synonymEngine = new DefaultSynomymEngine(new FSDirectory(dir,null));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(hasStopwords){
            initStopWord();
            stopWords = PUBLIC_STOP_WORDS;
        }
    }
    
    public void setSynonymEngine(SynonymEngine se){
        this.synonymEngine = se;
    }
    public void setStopWords(Set<String> s){
    	this.stopWords = s;
    }

    /**
     * Operate after original analyzer
     * So synonyms also need to be processed by the original analyzer
     */
    public TokenStream tokenStream(String fieldName, Reader reader) {
        TokenStream ts = null;
        if (synonymEngine == null) {
            ts = this.analyzer.tokenStream(fieldName, reader);
        }else {
            ts = new SynonymFilter(this.analyzer.tokenStream(fieldName, reader), synonymEngine);
        }
        if(stopWords!=null) {
            ts = new StopFilter(ts, stopWords);
        }
        if(ReservedWordsFilter.p!=null){
        	/** COMMENTED BY RAJ **/
            //ts = new ReservedWordsFilter(ts, reader);
        }
        return ts;
    }

}
