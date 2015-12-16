package net.javacoding.xsearch.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.javacoding.xsearch.api.protocol.SearchProtocol;
import net.javacoding.xsearch.utility.U;
import net.javacoding.xsearch.utility.VMTool;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Document implements SDLIndexDocument {

    SearchProtocol.Document document;
    List<Field> fields;

    private JsonObject source;
    private float score;
    private String id;
    
    private final static String[] NO_STRINGS = new String[0];

    public Document(SearchProtocol.Document document) {
        this.document = document;
    }

    public Document(JsonObject source, float score, String id) {
        this.source = source;
        this.score = score;
        this.id = id;
    }

    public float getScore() {
        return document != null ? this.document.getScore() : score;
    }

    public float getBoost() {
        return document != null ? this.document.getBoost() : 1F;
    }

    public String get(String field) {
        if (document != null) {
            for (SearchProtocol.Field f : this.document.getFieldList()) {
                if (f.getName().equals(field)) {
                    return f.getValue();
                }
            }
        } else if (source != null) {
            JsonElement element = source.get(field);
            if (element != null) {
                return source.get(field).getAsString();
            }
        }
        return "";
    }

    public String getString(String field) {
        if (document != null) {
            for (SearchProtocol.Field f : this.document.getFieldList()) {
                if (f.getName().equals(field)) {
                    return f.getValue();
                }
            }
        } else if (source != null) {
            JsonElement element = source.get(field);
            if (element != null) {
                return source.get(field).getAsString();
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
    public List<String> getValuesList(String field) {
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
    	if (document != null) {
    		for(SearchProtocol.Field f : this.document.getFieldList()) {
                if(f.getName().equals(field)&&f.getType().equals(SearchProtocol.Field.Type.DATETIME)) {
                    return VMTool.storedStringToDate(f.getValue());
                }
    		}
        } else if (source != null) {
            JsonElement element = source.get(field);
            if (element != null) {                
                return VMTool.storedStringToDate(source.get(field).getAsString());
            }
        }
        return new Date();       
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

	public List<org.apache.lucene.document.Field> fields() {
		// TODO Auto-generated method stub
		return null;
	}	
}
