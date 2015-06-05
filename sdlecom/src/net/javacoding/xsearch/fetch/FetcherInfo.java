package net.javacoding.xsearch.fetch;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;

import net.javacoding.xsearch.config.XMLSerializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("fetcher")
public class FetcherInfo extends XMLSerializable {
    public transient File[] files;
    public transient URLClassLoader classLoader;
    public transient String dir = "";
   
    public FetcherInfo() {
		super();
	}
	
    public String getDir(){
        return dir==null? null : dir.intern();
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

    public static FetcherInfo load( File theFile ) throws IOException {
        return (FetcherInfo)fromXML(theFile);
    }
}
