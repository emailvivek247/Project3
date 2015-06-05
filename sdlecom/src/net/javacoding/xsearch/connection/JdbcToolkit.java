package net.javacoding.xsearch.connection;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.javacoding.xsearch.config.JdbcDriverInfo;
import net.javacoding.xsearch.foundation.WebserverStatic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class JdbcToolkit {
    private static Logger logger = LoggerFactory.getLogger(JdbcToolkit.class);
    
    private static HashMap<String,JdbcDriverInfo> loaders = new HashMap<String,JdbcDriverInfo>();

    /**
     * Load list of JdbcDriverInfo from directories with jdbcdriver.xml
     * Add all other files in the directory, like *.jar, *.zip, and *.properties, to the classpath
     */
    public static List<JdbcDriverInfo> list(){
    	List<JdbcDriverInfo> list = new ArrayList<JdbcDriverInfo>();
        File driverDirectory = new File(WebserverStatic.getDriverDirectory());
        File[] dirs = driverDirectory.listFiles(new FileFilter(){
            public boolean accept(File f) {
            	return (f.isDirectory()&& new File(f,"jdbcdriver.xml").exists());
            }
        });
        for(File dir : dirs){
        	try {
        		JdbcDriverInfo d = JdbcDriverInfo.load(new File(dir, "jdbcdriver.xml")); 
        		d.files = dir.listFiles(new FileFilter(){
                    public boolean accept(File f) {
                    	return !f.getName().equalsIgnoreCase("jdbcdriver.xml") &&(f.getName().endsWith(".jar")||f.getName().endsWith(".zip")||f.getName().endsWith(".properties"));
                    }
                });
        		d.dir = dir.getName();
				list.add(d);
            } catch (java.lang.UnsupportedClassVersionError e) {
               	logger.info("Failed to load from "+dir + " due to JDK version mismatch. Please consider upgrade current JDK version.");
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return list;
    }

    public static void registerDriver(String className) throws SQLException {
    	for(JdbcDriverInfo d : list()){
    		if(d.getClassName().equals(className)){
    			registerDriver(d);
    			break;
    		}
    	}
    }
    public static void registerDriver(String className, String dir) throws SQLException {
        for(JdbcDriverInfo d : list()){
            if(d.getClassName().equals(className)&&d.getDir().equals(dir)){
                registerDriver(d);
                break;
            }
        }
    }
    public static List<JdbcDriverInfo> listAndTestDriver() {
        List<JdbcDriverInfo> list = new ArrayList<JdbcDriverInfo>();
        for(JdbcDriverInfo d : list()){
            try {
                list.add(registerDriver(d));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
    public static JdbcDriverInfo registerDriver(JdbcDriverInfo d) throws SQLException {
    	JdbcDriverInfo ret = loaders.get(d.dir);
    	if(ret!=null){
    		return ret;
    	}
        Class aClass = null;
        Driver driver = null;
        try {
        	URL[] urls = new URL[d.files.length];
        	for(int i=0;i<d.files.length;i++){
        		urls[i] = d.files[i].toURI().toURL();
        	}
            d.classLoader = new ReverseClassLoader(urls, ClassLoader.getSystemClassLoader());
            loaders.put(d.dir, d);

            aClass = d.classLoader.loadClass(d.getClassName());
            driver = (Driver) aClass.newInstance();
            Class.forName(driver.getClass().getName(), false, d.classLoader);
            d.version = driver.getMajorVersion()+"."+driver.getMinorVersion();
            d.isAvailable = true;
            DriverManager.registerDriver(new ProxyDriver(driver));
        } catch (java.lang.UnsupportedClassVersionError ec){
            logger.warn("Need to upgrade JDK to support driver:"+d.getName());
        } catch (MalformedURLException malformedURLException) {
        	logger.debug("MalformedURLException", malformedURLException);
        } catch (ClassNotFoundException classNotFoundException) {
            logger.debug("ClassNotFoundException", classNotFoundException);
        } catch (InstantiationException instantiationException) {
        	logger.debug("InstantiationException", instantiationException);
        } catch (IllegalAccessException illegalAccessException) {
        	logger.debug("IllegalAccessException", illegalAccessException);
        }
        return d;
    }
}
