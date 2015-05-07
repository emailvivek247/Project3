/*
 * Created on Apr 28, 2007
 */
package net.javacoding.xsearch.search.searcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.core.exception.DataSourceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

public class DefaultSearcherProvider implements SearcherProvider {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private File      mainDirectoryFile = null;
    private File      tempDirectoryFile = null;
    private boolean   isInMemorySearch       = false;
    private boolean   isInterim              = false;
    private Directory mainDirectory          = null;
    private Directory tempDirectory          = null;
    private AtomicInteger counter                = new AtomicInteger(0);
    
    private int licenseLevel = -1;

    private IndexReaderSearcher _irs = null;

    public int counter() {
        return counter.get();
    }
    public void close(IndexReaderSearcher s) throws Exception {
        if(licenseLevel<=0) {
            synchronized (_irs) {
                counter.decrementAndGet();
                _irs.notify();
            }
        }else {
            counter.decrementAndGet();
        }
    }

    public void shutdown() {
        long shutDownStart = System.currentTimeMillis();
        //wait at most 10 seconds
        while(counter.get()>0 && (System.currentTimeMillis() < shutDownStart + 10*1000)) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(counter.get()>0) {
            logger.warn("Outstanding searchers:"+counter);
        }
        if(_irs!=null){
            _irs.closeSearcher();
            _irs.closeReader();
            _irs = null;
        }
        if(tempDirectoryFile!=null){
    		tempDirectoryFile = null;
        }
        if(tempDirectory!=null){
            try {
                tempDirectory.close();
                tempDirectory = null;
            } catch (IOException e) {
            }
        }
        if(mainDirectoryFile!=null){
        	mainDirectoryFile = null;
        }
        if(mainDirectory!=null){
            try {
                mainDirectory.close();
                mainDirectory = null;
            } catch (IOException e) {
            }
        }
        //System.gc(); //this is added to prevent some window file deletion error
    }

    /**
     * Any IndexReaderSearcher must call IndexReaderSearcher.release() after usage
     */
    public IndexReaderSearcher getIndexReaderSearcher() throws Exception {
        if(licenseLevel<=0) {
            synchronized (_irs) {
                while(counter.get()>0) {
                    _irs.wait();
                    System.out.print(".");
                }
                counter.incrementAndGet();
                return _irs;
            }
        }else {
            counter.incrementAndGet();
            return _irs;
        }
    }

    public boolean configure(File mainDir, File tempDir, DatasetConfiguration dc, boolean isInMemory) throws DataSourceException, IOException {
        this.isInMemorySearch = isInMemory;
        if(mainDir!=null && mainDir.exists()){
            mainDirectoryFile = mainDir;
        }
        File tempIndexReadyFile = new File(tempDir, "ready");
        if(tempDir!=null && tempDir.exists() && tempIndexReadyFile.exists()){
            tempDirectoryFile = tempDir;
        }
        if (isInMemorySearch) {
            try {
                if(tempDirectoryFile!=null){
                    logger.info("Loading Temporary index into in memory for " +tempDirectoryFile + " ...");
                    tempDirectory = new RAMDirectory(tempDirectoryFile);
                    logger.info("Loaded Temporary index into in memory for " +tempDirectoryFile);
                }
                if(mainDirectoryFile!=null){
                    logger.info("Loading index into in memory for " +mainDirectoryFile + " ...");
                    mainDirectory = new RAMDirectory(mainDirectoryFile);
                    logger.info("Loaded index into in memory for " +mainDirectoryFile);
                }
                logger.info("Successfully loaded index into in memory for " +mainDir);
            } catch (FileNotFoundException fnfe) {
                isInMemorySearch = false;
                logger.info("Failed to create in memory index for " +mainDir +":"+ fnfe.toString());
            } catch (OutOfMemoryError oom) {
                isInMemorySearch = false;
                logger.warn("Index is too large! Falling back to disk based index...");
            }
        }

        if (!isInMemorySearch) {
            if(tempDirectoryFile!=null){
                tempDirectory = FSDirectory.getDirectory(tempDirectoryFile);
            }
            if(mainDir!=null && mainDir.exists()){
                mainDirectory = FSDirectory.getDirectory(mainDir);
            }
        }
        
        initIndexReader();
        
        if(licenseLevel<=0) {
            licenseLevel = ServerConfiguration.getServerConfiguration().getAllowedLicenseLevel();
        }

        if(_irs!=null){
            _irs.getSearcher().setSimilarity(dc.getSimilarity());
        }else{
            return false;
        }

        return true;
    }

    private Object initIndexReader() {
        if(_irs!=null) return _irs;
        IndexReader indexReader = null;
        IndexReader indexReader2 = null;
        try {
            //logger.debug("create searcher under directory: " + dir);
            if (mainDirectory != null && IndexReader.indexExists(mainDirectory)) {
                indexReader = IndexReader.open(mainDirectory);
            }
            if (tempDirectory!=null&&IndexReader.indexExists(tempDirectoryFile)) {
                indexReader2 = IndexReader.open(tempDirectory);
            }
            if(indexReader==null && indexReader2==null ){
            }else if(indexReader!=null && indexReader2==null){
                _irs = new IndexReaderSearcher(indexReader);
            }else if(indexReader==null && indexReader2!=null){
                _irs = new IndexReaderSearcher(indexReader2);
            }else{
                IndexReader[] indexReaders = new IndexReader[2];
                indexReaders[0] = indexReader;
                indexReaders[1] = indexReader2;
                indexReader = new MultiReader(indexReaders);
                _irs = new IndexReaderSearcher(indexReader);
            }
        } catch (IOException e) {
            logger.warn("Failed to create Searcher: " + e.toString());
            e.printStackTrace();
        }
        if(_irs!=null) {
            _irs.setSearcherProvider(this);
        }
        return _irs;
    }
    public boolean isInMemorySearch() {
        return isInMemorySearch;
    }
    public boolean isInterim() {
        return isInterim;
    }
    public void setInterim(boolean interim) {
        isInterim = interim;
    }
}
