package net.javacoding.xsearch.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.javacoding.xsearch.connection.JdbcToolkit;
import net.javacoding.xsearch.utility.U;

import com.fdt.sdl.styledesigner.util.PageStyleUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * describes a database connection information
 */
@XStreamAlias("data-source")
public class DataSource extends ConfigurationComponent implements ConfigConstants {
	
	private static final long serialVersionUID = -6743893640763434394L;

	public static final String DEFAULT_DATASOURCE_NAME = "default";
	
	private String name = DEFAULT_DATASOURCE_NAME;
	
	public DataSource() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
        setDirty();
	}
	
	private String driverDirectoryName;
    public String getDriverDirectoryName() {
        return driverDirectoryName;
    }
    public void setDriverDirectoryName(String dir) {
        this.driverDirectoryName = dir;
    }

    /** The JDBC driver for establishing a database connection. */
	@XStreamAlias("jdbcdriver")
    private String jdbcdriver = "oracle.jdbc.driver.OracleDriver";

    public String getJdbcdriver() {
        return jdbcdriver;
    }

    public void setJdbcdriver(String jdbcdriver) {
        this.jdbcdriver = jdbcdriver;
        setDirty();
    }

    /** A database URL of the form <code>jdbc:subprotocol:subname</code>. */
    @XStreamAlias("db-url")
    private String dbUrl = null;

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
        setDirty();
    }

    /** The database user on whose behalf the connection is being made. */
    @XStreamAlias("db-username")
    private String dbUsername = null;

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername ;
        setDirty();
    }

    /** The user's password. */
    @XStreamAlias("db-password")
    private String dbPassword = null;

    //get descrypted password
    public String getDbPassword() {
        return this.dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
        setDirty();
    }
    
    /** A simple query for testing database connection. */
    @XStreamAlias("dbcp-validation-query")
    private String dbcpValidationQuery = null;

    public String getDbcpValidationQuery() {
        return dbcpValidationQuery;
    }

    public void setDbcpValidationQuery(String dbcpValidationQuery) {
        this.dbcpValidationQuery = dbcpValidationQuery;
        setDirty();
    }

    public boolean testConnection(StringBuffer msg) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean result = false;
        try {
            JdbcToolkit.registerDriver(this.getJdbcdriver());
            conn = DriverManager.getConnection(this.getDbUrl(), this.getDbUsername(), this.getDbPassword());
            stmt = conn.prepareStatement(this.getDbcpValidationQuery());
            result = stmt.execute();
            // if the first result is an update count or there is no result
            if (!result) {
                msg.append("The query is invalid");
            }
        } catch (SQLException e) {
            //logger.info("SQLException",e);
            msg.append(e);
        } catch (NoClassDefFoundError ce) {
            msg.append("Please check JDBC driver version and JDK version. Some drivers requires latest JDK.");
            msg.append(ce);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    msg.append(e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    msg.append(e);
                }
            }
        }
        return result;
    }
    
    /**
     * Returns an XML representation of this object.
     */
    @Override
	public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("    <data-source>\n");

        if (name != null) {
            sb.append("      <name>").append(name).append("</name>\n");
        }
        
        if (!U.isEmpty(driverDirectoryName)) {
            sb.append("      <driver-directory-name>").append(driverDirectoryName).append("</driver-directory-name>\n");
        }

        if (jdbcdriver != null) {
            sb.append("      <jdbcdriver>").append(jdbcdriver).append("</jdbcdriver>\n");
        }

        if (dbUrl != null) {
            sb.append("      <db-url><![CDATA[").append(PageStyleUtil.encrypt(dbUrl)).append("]]></db-url>\n");
        }

        if (dbUsername != null) {
            sb.append("      <db-username><![CDATA[").append(PageStyleUtil.encrypt(dbUsername)).append("]]></db-username>\n");
        }

        if (dbPassword != null) {
            sb.append("      <db-password><![CDATA[").append(PageStyleUtil.encrypt(dbPassword)).append("]]></db-password>\n");
        }

        if (dbcpValidationQuery != null) {
            sb.append("      <dbcp-validation-query><![CDATA[\n").append(dbcpValidationQuery).append("\n  ]]></dbcp-validation-query>\n");
        }

        sb.append("    </data-source>\n");

        return sb.toString();
    }

}
