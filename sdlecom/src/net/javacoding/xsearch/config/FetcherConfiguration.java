package net.javacoding.xsearch.config;

import java.util.Properties;
import java.util.Map.Entry;

public class FetcherConfiguration {
    String dir;
    Properties properties = new Properties();    
    
    public void addPair(String key, String value) {
        if(key!=null&&value!=null) {
            properties.put(key, value);
        }
    }
    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir==null? null : dir.intern();
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  <fetcher-configuration dir=\""+dir+"\">\n");
        if(properties.size()>0) {
            sb.append("    <properties>\n");
            for(Entry<Object, Object> x : properties.entrySet()) {
                sb.append("      <entry key=\"").append(x.getKey()).append("\"><![CDATA[");
                sb.append(x.getValue());
                sb.append("]]></entry>\n");
            }
            sb.append("    </properties>\n");
        }
        sb.append("  </fetcher-configuration>\n");
        return sb.toString();
    }
}
