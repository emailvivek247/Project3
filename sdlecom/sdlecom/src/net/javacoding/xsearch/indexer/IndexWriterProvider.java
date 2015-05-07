package net.javacoding.xsearch.indexer;
import java.io.IOException;

import net.javacoding.xsearch.config.DataSource;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.exception.DataSourceException;

import org.apache.lucene.index.IndexWriter;

/**
 * A strategy for obtaining IndexWriters.
 * <br><br>
 * Implementors might also implement IndexWriter pooling.<br>
 * <br>
 * The <tt>IndexWriterProvider</tt> interface is not intended to be
 * exposed to the application. Instead it is used internally by
 * Xsearch to obtain IndexWriters.<br>
 * <br>
 * Implementors should provide a public default constructor.
 *
 * @see IndexWriterProviderFactory
 * @author Gavin King
 */
public interface IndexWriterProvider {
	/**
	 * Initialize the IndexWriter provider from given properties.
	 * @param props <tt>SessionFactory</tt> properties
	 */
	public void configure(IndexerContext ic) throws DataSourceException;
	/**
	 * Grab a IndexWriter
	 * @return a JDBC IndexWriter
	 * @throws IOException
	 */
	public IndexWriter getIndexWriter() throws Exception;

	/**
	 * Release all resources held by this provider. JavaDoc requires a second sentence.
	 * @throws DataSourceException
	 */
	public void close() throws DataSourceException;
}







