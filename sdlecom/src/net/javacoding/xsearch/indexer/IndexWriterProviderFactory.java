package net.javacoding.xsearch.indexer;

import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.exception.DataSourceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.sdl.admin.ui.action.constants.IndexType;

/**
 * Instantiates a IndexWriter provider The <tt>IndexWriterProviderFactory</tt>
 * first attempts to find <tt>DefaultIndexWriterProvider</tt>.
 * 
 * @see IndexWriterProvider
 * @author Gavin King
 */
public final class IndexWriterProviderFactory {

    private static final Logger logger = LoggerFactory.getLogger(IndexWriterProviderFactory.class);

    /**
     * Instantiate a <tt>IndexWriterProvider</tt> using
     * DefaultIndexWriterProvider. Method newIndexWriterProvider.
     * 
     * @param ic
     * @return IndexWriterProvider
     * @throws DataSourceException
     */
    public static IndexWriterProvider newIndexWriterProvider(IndexerContext ic) throws DataSourceException {
        IndexWriterProvider indexWriterProvider = null;
        IndexType indexType = ic.getDatasetConfiguration().getIndexType();
        if (indexType == null || indexType == IndexType.LUCENE) {
            logger.debug("Returning DefaultIndexWriterProvider");
            indexWriterProvider = new DefaultIndexWriterProvider();
            indexWriterProvider.configure(ic);
        } else if (indexType == IndexType.ELASTICSEARCH) {
            logger.debug("Returning NullIndexWriterProvider");
            indexWriterProvider = new NullIndexWriterProvider();
            indexWriterProvider.configure(ic);
        }
        return indexWriterProvider;
    }

    // cannot be instantiated
    private IndexWriterProviderFactory() {
        throw new UnsupportedOperationException();
    }

}
