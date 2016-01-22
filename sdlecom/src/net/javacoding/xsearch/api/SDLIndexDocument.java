package net.javacoding.xsearch.api;

import java.util.Date;
import java.util.List;

import org.apache.lucene.document.Field;

public interface SDLIndexDocument {

    public float getScore();

    public float getBoost();

    public String get(String field);

    public String getString(String field);

    public List<String> getValuesList(String field);

    public String[] getValues(String field);

    public Date getDate(String field);

    public Integer getInteger(String field);

    public Long getLong(String field);

    public Double getDouble(String field);

    public Float getFloat(String field);

    public List<Field> fields();
    
    public List<String> fieldNames();
}
