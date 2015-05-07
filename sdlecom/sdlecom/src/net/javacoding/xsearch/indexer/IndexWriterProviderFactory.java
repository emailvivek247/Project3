package net.javacoding.xsearch.indexer;

import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.exception.DataSourceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instantiates a IndexWriter provider
 * The <tt>IndexWriterProviderFactory</tt>
 * first attempts to find
 * <tt>DefaultIndexWriterProvider</tt>.
 * @see IndexWriterProvider
 * @author Gavin King
 */

public final class IndexWriterProviderFactory {

  private static final Logger logger = LoggerFactory.getLogger(IndexWriterProviderFactory.class);

	/**
	 * Instantiate a <tt>IndexWriterProvider</tt> using DefaultIndexWriterProvider.
	 * Method newIndexWriterProvider.
	 * @param properties hibernate <tt>SessionFactory</tt> properties
	 * @return IndexWriterProvider
	 * @throws DataSourceException
	 */
	public static IndexWriterProvider newIndexWriterProvider(IndexerContext ic) throws DataSourceException {
		IndexWriterProvider indexWriterProvider;
		indexWriterProvider = new DefaultIndexWriterProvider();
		indexWriterProvider.configure(ic);
		return indexWriterProvider;
	}

	// cannot be instantiated
	private IndexWriterProviderFactory() { throw new UnsupportedOperationException(); }

}
