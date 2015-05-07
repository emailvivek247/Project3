package net.javacoding.xsearch.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.javacoding.xsearch.api.protocol.SearchProtocol;
import net.javacoding.xsearch.utility.U;
import net.javacoding.xsearch.utility.VMTool;

public class Document {
    SearchProtocol.Document document;
    List<Field> fields;

    public Document(SearchProtocol.Document document) {
        this.document = document;
    }
    public float getScore() {return this.document.getScore();}
    public float getBoost() {return this.document.getBoost();}
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

}
