package net.javacoding.xsearch.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.lucene.document.Fieldable;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import net.javacoding.xsearch.api.protocol.SearchProtocol;
import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.utility.U;
import net.javacoding.xsearch.utility.VMTool;

public class Document {
    SearchProtocol.Document document;
    List<Field> fields;
    
    private final static String[] NO_STRINGS = new String[0];

    public Document(SearchProtocol.Document document) {
        this.document = document;
    }
    public float getScore() {return this.document.getScore();}
    public float getBoost() {return this.document.getBoost();}
    
    public String get(String field) {
    	for(SearchProtocol.Field f : this.document.getFieldList()) {
            if(f.getName().equals(field)) {
                return f.getValue();
            }
        }
        return "";
    }
    public String getString(String field) {
        for(SearchProtocol.Field f : this.document.getFieldList()) {
            if(f.getName().equals(field)) {
                return f.getValue();
            }
        }
        return "";
    }
    /**
     * One document can have several fields of the same name. 
     * For example, one article can have several comments, if you select comments in the subsequent query.
     * 
     * @param field name
     * @return a list of field values
     */
    public List<String> getStringList(String field) {
        List<String> ret = new ArrayList<String>();
        for(SearchProtocol.Field f : this.document.getFieldList()) {
            if(f.getName().equals(field)) {
                ret.add(f.getValue());
            }
        }
        return ret;
    }
    
    public String [] getValues(String field) {
        List<String> ret = new ArrayList<String>();
        for(SearchProtocol.Field f : this.document.getFieldList()) {
            if(f.getName().equals(field)) {
                ret.add(f.getValue());
            }
        }
        
        if (ret.size() == 0)
            return NO_STRINGS;
          
        return (String[])ret.toArray(new String[ret.size()]);
       
    }
    
    public Date getDate(String field) {
        for(SearchProtocol.Field f : this.document.getFieldList()) {
            if(f.getName().equals(field)&&f.getType().equals(SearchProtocol.Field.Type.DATETIME)) {
                return VMTool.storedStringToDate(f.getValue());
            }
        }
        return null;
    }
    public Integer getInteger(String field) {
        for(SearchProtocol.Field f : this.document.getFieldList()) {
            if(f.getName().equals(field)&&f.getType().equals(SearchProtocol.Field.Type.NUMBER)) {
                return U.getInteger(f.getValue());
            }
        }
        return null;
    }
    public Long getLong(String field) {
        for(SearchProtocol.Field f : this.document.getFieldList()) {
            if(f.getName().equals(field)&&f.getType().equals(SearchProtocol.Field.Type.NUMBER)) {
                return VMTool.storedStringToLong(f.getValue());
            }
        }
        return null;
    }
    public Double getDouble(String field) {
        for(SearchProtocol.Field f : this.document.getFieldList()) {
            if(f.getName().equals(field)&&f.getType().equals(SearchProtocol.Field.Type.NUMBER)) {
                return U.getDouble(f.getValue(),0);
            }
        }
        return null;
    }
    public Float getFloat(String field) {
        for(SearchProtocol.Field f : this.document.getFieldList()) {
            if(f.getName().equals(field)&&f.getType().equals(SearchProtocol.Field.Type.NUMBER)) {
                return U.getFloat(f.getValue(),0);
            }
        }
        return null;
    }
    public List<Field> getFieldList(){
        if(fields==null) {
            fields = new ArrayList<Field>();
            for(SearchProtocol.Field f : this.document.getFieldList()) {
                fields.add(new Field(f));
            }
        }
        return fields;
    }
    
    public static JSONArray toJSONArray(List<Document> l) {
        JSONArray ret = new JSONArray();
        if(l!=null) {
            for(Document d : l) {
                JSONObject doc = new JSONObject();
                List<Field> fields = d.getFieldList();
                for (int i = 0; i < fields.size(); i++) {
                    Field field = fields.get(i);
                    try {
                        doc.put(field.getName(), d.getObject(field.getName()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                JSONObject h = new JSONObject(d,new String[]{"id","score"});
                try {
                    h.put("doc", doc);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ret.put(h);
            }
        }
        return ret; 
    }
    
    public final Object getObject(String field) {
    	 for(SearchProtocol.Field f : this.document.getFieldList()) {
             if(f.getName().equals(field)&&f.getType().equals(SearchProtocol.Field.Type.DATETIME)) {
            	 return getDate(field);
             } else if (f.getName().equals(field)&&f.getType().equals(SearchProtocol.Field.Type.STRING)) {
            	 return getString(field);
             }else if (f.getName().equals(field)&&f.getType().equals(SearchProtocol.Field.Type.NUMBER)) {
            	 return getString(field);
             } else if (f.getName().equals(field)){
            	 return f.getValue();
             } else {
            	 return null;
             }
    	 }
    	 return null;
            	
    }
}
