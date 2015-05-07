package net.javacoding.xsearch.foundation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.config.XMLSerializable;
import net.javacoding.xsearch.utility.FileUtil;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("query-sample-values")
public class QuerySampleValues extends XMLSerializable {
	
    @XStreamAlias("columnValues")
	public HashMap<String,List<String>> columnValues = new HashMap<String, List<String>>();
    
    public QuerySampleValues() {
		super();
	}
	
    public void add(String column, String value){
	    List<String> list = getStringList(column);
	    if(list==null) {
	        list = new ArrayList<String>();
	    }
	    if(!list.contains(value)) {
	        list.add(value);
	        columnValues.put(column,list);
	    }
	}
	public String get(String column){
		List<String> list = getStringList(column);
		if(list==null||list.size()==0)return "";
		return list.get(0);
	}
    public List<String> getList(String column){
        List<String> list = getStringList(column);
        if(list==null)return new ArrayList<String>();
        return list;
    }
    
    private List<String> getStringList(String column){
        List<String> list = null;
        try {
            list = columnValues.get(column);
        }catch(ClassCastException cce) {}
        return list;
    }

	public static QuerySampleValues load() {
        ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
        QuerySampleValues ret = (QuerySampleValues)fromXML(FileUtil.resolveFile(sc.getBaseDirectory(),"log", "sample_values.xml"));
    	return ret == null ? new QuerySampleValues() : ret;
    }
    public void save(){
        ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
    	toXML(new File(new File(sc.getBaseDirectory(),"log"), "sample_values.xml"));
    }
}
