package net.javacoding.xsearch.foundation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  
 */
/**
 * this is to store User's state, for example progress in a wizard
 * Get Usage:
 *      UserPreference.getxxx(theKey);
 * Set Usage:
 *      UserPreference.setxxx(theKey, theValue);
 *      ...
 *      UserPreference.save();//preferrably in finally{}
 * 
 */
public class UserPreference {

    private static Logger  logger = LoggerFactory.getLogger("net.javacoding.xsearch.foundation.UserPreference");

    private static Properties p = null;
    private static String preferenceFileName = "user.preference";
    
    /**
     * Expert Only: Currently it's only meant to use in unit test
     * @param preferenceFileName, the file name to store user preferences
     */
    public static void setPreferenceFile(String fileName) {
        preferenceFileName = fileName;
    }

    public static int getInt(String name, int def) {
        if (p == null) load();
        return U.getInt(p.getProperty(name), def);
    }

    public static void setInt(String name, int val) {
        if (p == null) load();
        p.setProperty(name, Integer.toString(val));
    }

    public static boolean getBoolean(String name, boolean def) {
        if (p == null) load();
        return U.getBoolean(p.getProperty(name), "Y", def);
    }

    public static void setBoolean(String name, boolean val) {
        if (p == null) load();
        p.setProperty(name, (val ? "Y" : "N"));
    }

    public static String getString(String name, String def) {
        if (p == null) load();
        return U.getText(p.getProperty(name), def);
    }

    public static void setString(String name, String val) {
        if (p == null) load();
        if(val==null) {
            p.remove(name);
        }else{
            p.setProperty(name, val);
        }
    }
    /**
     * list all keys starts with the prefix
     * @param prefix
     * @return the string[] of full-keys to get/set values
     */
    public static String[] listPreferenceNames(String prefix){
        ArrayList<String> al = new ArrayList<String>();
        for (Enumeration e = p.keys() ; e.hasMoreElements() ;) {
            String k = (String)e.nextElement();
            if(k.startsWith(prefix)){
                al.add(k);
            }
        }
        return (String[]) al.toArray();
    }

    static File userPreferenceFile = null;

    public static void load() {
        if (p == null) {
            ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
            userPreferenceFile = new File(new File(sc.getBaseDirectory(),"log"), preferenceFileName);
            p = new Properties();
            if (userPreferenceFile.exists() && userPreferenceFile.canRead()) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(userPreferenceFile);
                    p.load(fis);
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    public static void save() {
        if (p != null) {
            ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
            userPreferenceFile = new File(new File(sc.getBaseDirectory(),"log"), preferenceFileName);
            if (!userPreferenceFile.exists()) {
                try {
                    userPreferenceFile.createNewFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (userPreferenceFile.canWrite()) {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(userPreferenceFile);
                    p.store(fos,null);
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            }else{
                logger.warn(userPreferenceFile.getAbsolutePath()+" not writable!");
            }
        }
    }
    public static void remove() {
        if (userPreferenceFile.exists()) {
            userPreferenceFile.delete();
        }
    }
    
    public QuerySampleValues getSampleValues(){
    	return QuerySampleValues.load();
    }
}
