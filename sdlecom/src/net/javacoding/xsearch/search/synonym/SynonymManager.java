/*
 * Created on Apr 22, 2007
 */
package net.javacoding.xsearch.search.synonym;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.foundation.WebserverStatic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SynonymManager {
    
    private static final String SYNONYM_WORDS_LIST = "synonyms.txt";
    private static final String STOP_WORDS_LIST = "stopwords.txt";
    private static final String RESERVED_WORDS_LIST = "reserved.txt";

    private static Logger logger = LoggerFactory.getLogger(SynonymManager.class.getName());

    public static void start(DatasetConfiguration dc) throws Throwable {
        File synonymsFile = getSynonymWordsListFile();
        if(!synonymsFile.exists()) return;
        ArrayList columns = dc.getColumns(true);
        for(int i=0;i<columns.size();i++) {
            Column c = (Column)columns.get(i);
            if(c.getNeedSynonymsAndStopwords()) {
                String analyzerClassName = (c.getAnalyzerName()==null? dc.getAnalyzerName(): c.getAnalyzerName() );
                maybeBuildSynonymIndex(synonymsFile, dc, analyzerClassName);
            }
        }
    }
    
    private static void maybeBuildSynonymIndex(File synonymsFile, DatasetConfiguration dc, String analyzerClassName){
        try{
            boolean needToCreate = false;
            File synonymsIndexDirectory = getSynonymIndexDirectory(dc,analyzerClassName); 
            if(!synonymsIndexDirectory.exists()) {
                needToCreate = true;
            }
            if(!needToCreate ) {
                needToCreate = !IndexReader.indexExists(synonymsIndexDirectory);
            }
            if(!needToCreate ) {
                needToCreate = getSynonymWordsListFile().lastModified() > IndexReader.lastModified(synonymsIndexDirectory);
            }
            if(needToCreate) {
                Analyzer a = (Analyzer) Class.forName(analyzerClassName).newInstance();
                doCreateSynonymIndex(new FileReader(synonymsFile), FSDirectory.getDirectory(synonymsIndexDirectory), a);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    public static void doCreateSynonymIndex(Reader synonymsReader, Directory synonymsIndexDirectory, Analyzer a) throws IOException, IOException {
        // override the specific index if it already exists
        IndexWriter writer = new IndexWriter(synonymsIndexDirectory, a, true, MaxFieldLength.UNLIMITED);
        writer.setUseCompoundFile(true);
        // blindly up these parameters for speed
        writer.setMergeFactor(writer.getMergeFactor() * 2);
        if (writer.getMaxBufferedDocs() != IndexWriter.DISABLE_AUTO_FLUSH){
            writer.setMaxBufferedDocs(writer.getMaxBufferedDocs() * 2);
        }

        final BufferedReader br = new BufferedReader(synonymsReader);
        String line;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.length()<=0 || line.startsWith("#")) {
                continue; // skip comments and empty lines
            }
            String[] words = (line.indexOf(",")>0 ? line.split(",") : line.split("[, ]"));
            for(int i=0;i<words.length;i++) {
                Document doc = new Document();
                if(words[i].trim().length()>0) {
                    doc.add(new Field(SynonymEngine.WORD_FIELD, words[i], Field.Store.YES, Field.Index.ANALYZED_NO_NORMS));
                    for(int j=0;j<words.length;j++) {
                        if(j==i)continue;
                        doc.add(new Field(SynonymEngine.SYNONYM_FIELD, words[j], Field.Store.YES, Field.Index.ANALYZED_NO_NORMS));
                    }
                }
                writer.addDocument(doc);
            }
        }
        logger.info("Optimizing synonyms index...");
        writer.optimize();
        writer.close();
    }
    
    private static File getSynonymIndexDirectory(DatasetConfiguration dc, String analyzerClassName) {
        return new File(dc.getSynonymsDirectoryFile(), trimAnalyzerName(analyzerClassName));
    }
    public static File getSynonymIndexDirectory(DatasetConfiguration dc, Analyzer analyzer) {
        return new File(dc.getSynonymsDirectoryFile(), trimAnalyzerName(analyzer.getClass().getName()));
    }
    private static File getSynonymWordsListFile() {
        return new File(WebserverStatic.getDictionaryDirectoryFile(), SYNONYM_WORDS_LIST);
    }
    public static File getStopWordsListFile() {
        return new File(WebserverStatic.getDictionaryDirectoryFile(), STOP_WORDS_LIST);
    }
    public static File getReservedWordsListFile() {
        return new File(WebserverStatic.getDictionaryDirectoryFile(), RESERVED_WORDS_LIST);
    }
    
    private static String trimAnalyzerName(String name) {
        int start = name.lastIndexOf(".");
        int end = name.lastIndexOf("Analyzer");
        if(end<0) {
            end = name.length();
        }
        return name.substring(start+1, end).toLowerCase();
    }

}
