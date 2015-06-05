/*
 * Created on Apr 20, 2007
 */
package net.javacoding.xsearch.search.synonym;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import net.javacoding.xsearch.foundation.WebserverStatic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

/**
 * An example usage for SynonymFilter, 
 * 
 * but going to use SynonymFilter directly instead of this in indexing
 * since synonym filter is conditionally added to the anaylzer
 * 
 */
public class DefaultSynonymAnalyzer extends Analyzer {
    
    private Logger    logger                 = LoggerFactory.getLogger(this.getClass().getName());

    SynonymEngine synEngine = null;

    public DefaultSynonymAnalyzer() {
        File synIndexDirectory = new File(WebserverStatic.getDictionaryDirectoryFile(), "synonyms");
        if(synIndexDirectory==null||!IndexReader.indexExists(synIndexDirectory)) {
            return;
        }
        logger.info("loading synonyms");
        try {
            synEngine = new DefaultSynomymEngine(new FSDirectory(synIndexDirectory,null));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TokenStream tokenStream(String fieldName, Reader reader) {
        TokenStream result = null;
        if (synEngine != null) {
            result = new SnowballFilter(new SynonymFilter(new LowerCaseTokenizer(reader), synEngine), "English");
        } else {
            result = new SnowballFilter(new LowerCaseTokenizer(reader), "English");
        }
        return result;
    }

}
