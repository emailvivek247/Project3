package net.javacoding.xsearch.search.searcher;

import java.io.File;
import java.io.IOException;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.core.exception.DataSourceException;

/**
 * A searcher provider interface
 * 
 */
public interface SearcherProvider {

    public IndexReaderSearcher getIndexReaderSearcher() throws Exception;

    /**
     * close the Searcher, return it to the provider
     */
    public void close(IndexReaderSearcher s) throws Exception;

    public boolean configure(File mainDir, File tempDir, DatasetConfiguration dc, boolean isInMemory) throws DataSourceException, IOException;

    /**
     * close the SearchProvider
     */
    public void shutdown() ;

    public boolean isInMemorySearch();
    
    public int counter();

    public boolean isInterim();
    public void setInterim(boolean interim);
}
