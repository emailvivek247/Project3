package net.javacoding.xsearch.indexer;

import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.exception.DataSourceException;

import org.apache.lucene.index.IndexWriter;

public class NullIndexWriterProvider implements IndexWriterProvider {

    private IndexWriter iw;

    public IndexWriter getIndexWriter() throws Exception {
        return iw;
    }

    public void configure(IndexerContext ic) throws DataSourceException {
        // Do nothing here
    }

    public void close() throws DataSourceException {
        // Do nothing here
    }

}
