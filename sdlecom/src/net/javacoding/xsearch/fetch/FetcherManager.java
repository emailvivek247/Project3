package net.javacoding.xsearch.fetch;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.javacoding.xsearch.connection.ReverseClassLoader;
import net.javacoding.xsearch.foundation.WebserverStatic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetcherManager {
	
    private static Logger logger = LoggerFactory.getLogger(FetcherManager.class);
    
    private static HashMap<String,FetcherInfo> loaders = new HashMap<String,FetcherInfo>();

    public static List<FetcherInfo> list(){
        List<FetcherInfo> list = new ArrayList<FetcherInfo>();
        File fetcherDirectory = new File(WebserverStatic.getFetcherDirectory());
        File[] dirs = fetcherDirectory.listFiles(new FileFilter(){
            public boolean accept(File f) {
                return (f.isDirectory()&& new File(f,"fetcher.xml").exists());
            }
        });
        if(dirs!=null)for(File dir : dirs){
            FetcherInfo info = add(dir.getName());
            if(info!=null) {
                list.add(info);
            }
        }
        return list;
    }
    public static AbstractFetcher load(String dir) {
        FetcherInfo d = loaders.get(dir);
        if(d==null) {
            add(dir);
            d = loaders.get(dir);
        }
        if(d==null) return null;
        try {
            Class klass = d.classLoader.loadClass(d.getClassName());
            return (AbstractFetcher) klass.newInstance();
        }catch(Exception e) {
            logger.warn("Exception Occured", e);
        }
        return null;
    }
    private static FetcherInfo add(String dirName) {
        File dir = new File(WebserverStatic.getFetcherDirectory(),dirName);
        try {
            FetcherInfo d = FetcherInfo.load(new File(dir, "fetcher.xml")); 
            if(d==null) {
                logger.error("The customized fetcher does not exists under " + dir);
                throw new RuntimeException("The customized fetcher does not exists under "+dir);
            }
            d.files = dir.listFiles(new FileFilter(){
                public boolean accept(File f) {
                    return (f.getName().endsWith(".jar")||f.getName().endsWith(".zip")||f.getName().endsWith(".properties"));
                }
            });
            d.dir = dir.getName();
            URL[] urls = new URL[d.files.length];
            for(int i=0;i<d.files.length;i++){
                urls[i] = d.files[i].toURI().toURL();
            }
            d.classLoader = new ReverseClassLoader(urls, Thread.currentThread().getContextClassLoader());
            loaders.put(dirName, d);
            return d;
        } catch (java.lang.UnsupportedClassVersionError e) {
            logger.info("Failed to load from "+dir + " due to JDK version mismatch. Please consider upgrade current JDK version.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
