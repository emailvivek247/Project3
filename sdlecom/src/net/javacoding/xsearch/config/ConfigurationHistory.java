package net.javacoding.xsearch.config;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("configuration-history")
public class ConfigurationHistory extends XMLSerializable{
    
	private transient DatasetConfiguration dc;

	public ConfigurationHistory() {
		super();
	}
	
    public ConfigurationHistory(DatasetConfiguration dc) {
        this.dc = dc;
    }
    public void setDatasetConfiguration(DatasetConfiguration dc) {
        this.dc = dc;
    }
    
    @XStreamAlias("columns-map")
    private HashMap<String,Column> columnsMap;
    
    public void addColumns(Collection<Column> cols) {
        if(columnsMap==null) {
            columnsMap = new HashMap<String,Column>(cols.size());
        }
        for(Column c : cols) {
            columnsMap.put(c.getColumnName(),c);
        }
    }
    
    /**
     * Init a column with historical configuration
     * @param c
     */
    public void init(Column c) {
        if(columnsMap==null) return;
        Column oc = columnsMap.get(c.getColumnName());
        if(oc==null) return;

        c.merge(oc);
    }
    public static ConfigurationHistory load(DatasetConfiguration dc) {
        ConfigurationHistory ret = (ConfigurationHistory)fromXML(getFile(dc));
        if(ret==null) {
            ret = new ConfigurationHistory(dc);
        }
        return ret;
    }
    public static void save(DatasetConfiguration dc) {
        ConfigurationHistory ret = (ConfigurationHistory)fromXML(getFile(dc));
        if(ret==null) {
            ret = new ConfigurationHistory(dc);
        }
        ret.addColumns(dc.getColumns());
        ret.setDatasetConfiguration(dc);
        ret.save();
    }
    private void save() {
        toXML(getFile(dc));
    }
    public static void saveDuringStartup(DatasetConfiguration dc) {
        ConfigurationHistory ret = (ConfigurationHistory)fromXML(getFile(dc));
        if(ret==null) {
            ret = new ConfigurationHistory(dc);
            ret.addColumns(dc.getColumns());
            ret.save();
        }
    }
    private static File getFile(DatasetConfiguration dc) {
        ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
        return new File(new File(sc.getBaseDirectory(),"log"), dc.getName()+"-history.xml");
    }
}
