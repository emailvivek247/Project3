package net.javacoding.xsearch.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 *
 * TODO To change the template for this generated type comment go to
 * @
 */
public class DriverManagerConnectionFactory implements ConnectionFactory {

    private static final Logger logger = LoggerFactory.getLogger(DriverManagerConnectionFactory.class);
    
    public DriverManagerConnectionFactory(String connectUri, Properties props) {
        _connectUri = connectUri;
        _props = props;
        initLoginTimeout();
    }

    public DriverManagerConnectionFactory(String connectUri, String uname, String passwd) {
        _connectUri = connectUri;
        _uname = uname;
        _passwd = passwd;
        initLoginTimeout();
    }
    private void initLoginTimeout(){
        try {
        	DriverManager.setLoginTimeout(100);
        }catch(Throwable uoe) {
            logger.warn("Connection setLoginTimeout not supported.");
        }
    }

    /* (non-Javadoc)
     * Copied from org.apache.commons.dbcp.DriverManagerConnectionFactory
     * Connect with the username/password even if the password is empty
     * @see org.apache.commons.dbcp.ConnectionFactory#createConnection()
     */
    public Connection createConnection() throws SQLException {
        if(null == _props) {
            if((_uname == null) ) {
                return DriverManager.getConnection(_connectUri);
            } else {
                return DriverManager.getConnection(_connectUri,_uname,_passwd);
            }
        } else {
            return DriverManager.getConnection(_connectUri,_props);
        }
    }

    protected String _connectUri = null;
    protected String _uname = null;
    protected String _passwd = null;
    protected Properties _props = null;
}
