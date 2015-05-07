package net.javacoding.xsearch.config;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("jdbcdriver")
public class JdbcDriverInfo extends XMLSerializable implements ConfigConstants {

    public transient File[] files;
    public transient URLClassLoader classLoader;
    
    public JdbcDriverInfo() {
		super();
	}

	@XStreamAlias("name")
	@XStreamAsAttribute
    private String name = null;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @XStreamAlias("class")
    private String className = null;
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    @XStreamAlias("url-format")
    private String urlFormat = null;
    public String getUrlFormat() { return urlFormat; }
    public void setUrlFormat(String urlFormat) { this.urlFormat = urlFormat; }

    @XStreamAlias("url-example")
    private String urlExample = null;
    public String getUrlExample() { return urlExample; }
    public void setUrlExample(String urlExample) { this.urlExample = urlExample; }

    @XStreamAlias("validate-sql")
    private String validateSql = null;
    public String getValidateSql() { return validateSql; }
    public void setValidateSql(String validateSql) { this.validateSql = validateSql; }

    public transient boolean isAvailable = false;
    public boolean getAvailable(){
    	return isAvailable;
    }

    public transient String version = "";
    public String getVersion(){
    	return version==null ? "" : version;
    }

    public transient String dir = "";
    public String getDir(){
    	return dir;
    }

    public static JdbcDriverInfo load( File theFile ) throws IOException {
    	return (JdbcDriverInfo)fromXML(theFile);
    }
}
