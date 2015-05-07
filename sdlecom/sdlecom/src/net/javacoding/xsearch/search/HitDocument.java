/*
 * Created on Apr 12, 2005
 */
package net.javacoding.xsearch.search;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.utility.U;
import net.javacoding.xsearch.utility.VMTool;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
/**
 * Just wrap document object
 * To display hit score
 * To return doc content in List, instead of [], to be used by velocity
 * @
 */
public class HitDocument {
    public float score;
    public int id;
    public Document doc = null;
    public DatasetConfiguration dc;

    public HitDocument(DatasetConfiguration dc, Document doc, float s, int i) {
        this.dc = dc;
        this.doc = doc;
        score = s;
        id = i;
    }
    /**
     * @return Returns the id.
     */
    public int getId() {
        return id;
    }
    /**
     * @return Returns the score.
     */
    public float getScore() {
        return score;
    }

    public float getBoost() {
        if(doc==null) return 0;
        return doc.getBoost();
    }
    public final String get(String name) {
        if(doc==null) return "";
        String ret = doc.get(name);
        return ret==null? "" : ret;
    }

    /** Returns an Enumeration of all the fields in a document. */
    public final List<Field> fields() {
        if(doc==null) return null;
        return doc.getFields();
    }
    public final Field getField(String name) {
        if(doc==null) return null;
        return doc.getField(name);
    }
    public final Field[] getFields(String name) {
        if(doc==null) return null;
        return doc.getFields(name);
    }
    /*
     * To be used by velocity
     */
    public final List<Field> getFieldsList(String name) {
        if(doc==null) return null;
        Field[] fields = doc.getFields(name);
        if(fields==null) return new ArrayList<Field>(0);
        ArrayList<Field> al = new ArrayList<Field>(fields.length);
        for(int i = 0; i< fields.length; i++) {
            al.add(fields[i]);
        }
        return al;
    }
    public final String[] getValues(String name) {
        if(doc==null) return null;
        return doc.getValues(name);
    }
    /*
     * To be used by velocity
     */
    public final List<String> getValuesList(String name) {
        if(doc==null) return null;
        String[] values = doc.getValues(name);
        if(values==null) return new ArrayList<String>(0);
        ArrayList<String> al = new ArrayList<String>(values.length);
        for(int i = 0; i< values.length; i++) {
            al.add(values[i]);
        }
        return al;
    }
    public final byte[][] getBinaryValues(String name) {
        if(doc==null) return null;
        return doc.getBinaryValues(name);
    }
    public final byte[] getBinaryValue(String name) {
        if(doc==null) return null;
        return doc.getBinaryValue(name);
    }
    
    /**
     * Depending on the column type, it returns
     *  java.sql.Date => java.util.Date
     *  java.sql.Time => java.util.Date
     *  java.sql.Timestamp => java.util.Date
     *  java.math.BigDecimal => java.lang.Double
     *  java.lang.Double => java.lang.Double
     *  java.lang.Float => java.lang.Float
     * @param name
     * @return
     */
    public final Object getObject(String name) {
        Column c = dc.getColumn(name);
        if(c==null) return "";
        String columnType = c.getColumnType();
        //check DBTool.getString() for details
        if (c.getIsDate()){
            return VMTool.storedStringToDate(get(name));
        }else if ("java.math.BigDecimal"==columnType){
            return U.getDouble(get(name),0);
        }else if ("java.lang.Double"==columnType){
            return U.getDouble(get(name),0);
        }else if ("java.lang.Float"==columnType){
            return U.getFloat(get(name),0);
        }else {
            return get(name);
        }
    }
    public final Date getDate(String name) {
        return VMTool.storedStringToDate(get(name));
    }
    /**
     * Return a Long object of EPOCH time, and null if date is empty or stored in wrong format.
     */
    public final Long getDateAsLong(String name) {
        Date d = VMTool.storedStringToDate(get(name));
        return d==null? null : d.getTime();
    }
    public final Float getFloat(String name) {
        return U.getFloat(get(name),0);
    }
    public final Double getDouble(String name) {
        return U.getDouble(get(name),0);
    }
    public final Integer getInteger(String name) {
        return U.getInteger(get(name));
    }

    public static JSONArray toJSONArray(List<HitDocument> l) {
        JSONArray ret = new JSONArray();
        if(l!=null) {
            for(HitDocument d : l) {
                JSONObject doc = new JSONObject();
                List<Field> fields = d.doc.getFields();
                for (int i = 0; i < fields.size(); i++) {
                    Fieldable field = fields.get(i);
                    try {
                        doc.put(field.name(), d.getObject(field.name()));
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
}
