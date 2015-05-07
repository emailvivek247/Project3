package net.javacoding.xsearch.connection;

import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.exception.DataSourceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConnectionProviderFactory {

  private Logger logger = LoggerFactory.getLogger(ConnectionProviderFactory.class);

	public static ConnectionProvider newConnectionProvider(IndexerContext ic) throws DataSourceException {
		ConnectionProvider connections;
		connections = new DBCPConnectionProvider();
		connections.configure(ic);
		return connections;
	}

	// cannot be instantiated
	private ConnectionProviderFactory() { throw new UnsupportedOperationException(); }

}
