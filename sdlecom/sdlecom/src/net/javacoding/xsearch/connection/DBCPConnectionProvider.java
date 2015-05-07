package net.javacoding.xsearch.connection;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.exception.DataSourceException;
import net.javacoding.xsearch.utility.U;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connection provider that uses an Apache commons DBCP connection pool. 
 * @see ConnectionProvider
 */
public class DBCPConnectionProvider implements ConnectionProvider {

    private Map<String, DataSource> dataSources = new HashMap<String, DataSource>();
    private Map<String, KeyedObjectPoolFactory> statementPoolFactories = new HashMap<String, KeyedObjectPoolFactory>();
    private Map<String, ObjectPool> connectionPools = new HashMap<String, ObjectPool>();

    private static final Logger logger = LoggerFactory.getLogger(DBCPConnectionProvider.class);

    public Connection getConnection() throws SQLException {
        if(dataSources.size()==1){
            for(Entry<String, DataSource> d : dataSources.entrySet()){
                return d.getValue().getConnection();
            }
        }
        return dataSources.get(net.javacoding.xsearch.config.DataSource.DEFAULT_DATASOURCE_NAME).getConnection();
    }
    public Connection getConnection(String name) throws SQLException {
        if(dataSources.size()==1){
            for(Entry<String, DataSource> d : dataSources.entrySet()){
                return d.getValue().getConnection();
            }
        }
        Connection c = dataSources.get(name==null? net.javacoding.xsearch.config.DataSource.DEFAULT_DATASOURCE_NAME:name).getConnection();
        return c;
    }

    public void closeConnection(Connection conn) throws SQLException {
        try {
            if (conn != null) {
                conn.close();
            }
        }catch(NullPointerException npe) {
        	logger.error("NullPointerException", npe);
        }
    }

    public void configure(IndexerContext ic) throws DataSourceException {
    	DatasetConfiguration dc = ic.getDatasetConfiguration();
    	net.javacoding.xsearch.config.DataSource the_ds = dc.getDataSource(0);
        String jdbcDriverClass = the_ds.getJdbcdriver();
        String jdbcUrl = the_ds.getDbUrl();

        logger.info("DBCP using driver: " + jdbcDriverClass + " at URL: " + jdbcUrl);

        if (jdbcDriverClass == null) {
            logger.warn("No JDBC Driver class was specified by property " + the_ds.getJdbcdriver());
        } else {
            try {
                logger.debug("load " + the_ds.getJdbcdriver());
                if(U.isEmpty(the_ds.getDriverDirectoryName())) {
                    //backward compatible
                    JdbcToolkit.registerDriver(the_ds.getJdbcdriver());
                }else {
                    JdbcToolkit.registerDriver(the_ds.getJdbcdriver(), the_ds.getDriverDirectoryName());
                }
            } catch (SQLException cnfe) {
                String msg = "JDBC Driver class not found: " + jdbcDriverClass;
                logger.error(NOTIFY_ADMIN, msg);
                throw new DataSourceException();
            }
        }

        try {
            // We'll need a ObjectPool that serves as the
            // actual pool of connections.
            connectionPools.put(the_ds.getName(),new GenericObjectPool(
                null,
                dc.getDbcpMaxactive(),
                dc.getDbcpWhenexhausted(),
                dc.getDbcpMaxwait(),
                dc.getDbcpMaxidle(),
                false,
                false
            ));

            //check whether we use prepare statement caching or not
            if (dc.getDbcpPsMaxactive() == 0) {
                logger.info("DBCP prepared statement pooling disabled");
                statementPoolFactories = null;
            } else {
                // We'll need a KeyedObjectPoolFactory that serves as the
                // actual pool of prepared statements.
                logger.info("DBCP prepared statement pooling enabled");
                statementPoolFactories.put(the_ds.getName(),new GenericKeyedObjectPoolFactory(
                      null,
                      dc.getDbcpPsMaxactive(),
                      dc.getDbcpPsWhenexhausted(),
                      dc.getDbcpPsMaxwait(),
                      dc.getDbcpPsMaxidle()
                ));
            }

            // Next, we'll create a ConnectionFactory that the
            // pool will use to create Connections.
            // We'll use the DriverManagerConnectionFactory.
            ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                    jdbcUrl,
                    the_ds.getDbUsername(),
                    the_ds.getDbPassword()
            );

            // Now we'll create the PoolableConnectionFactory, which wraps
            // the "real" Connections created by the ConnectionFactory with
            // the classes that implement the pooling functionality.
            String validationQuery = null; // do not validate connection: dc.getDbcpValidationQuery();
            new PoolableConnectionFactory(connectionFactory, connectionPools.get(the_ds.getName()), statementPoolFactories.get(the_ds.getName()), validationQuery, false, true);

            // Finally, we create the PoolingDriver itself,
            // passing in the object pool we created.
            dataSources.put(the_ds.getName(),new PoolingDataSource(connectionPools.get(the_ds.getName())));

        } catch (Exception e) {
            logger.error(NOTIFY_ADMIN, "could not instantiate DBCP connection pool", e);
            throw new DataSourceException();
        }

    }

    /**
     * @see net.javacoding.xsearch.connection.ConnectionProvider#close()
     */
    public void close() throws DataSourceException {
    	for (String k : connectionPools.keySet()) {
            try {
                connectionPools.get(k).close();
            } catch (Exception e) {}
		}
    }

}
