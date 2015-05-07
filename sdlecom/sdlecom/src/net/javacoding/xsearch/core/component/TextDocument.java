/*
 * Created on Jan 3, 2005 $Header:
 * /data/source/search/htdocs/WEB-INF/classes/net/javacoding/xsearch/core/TextDocument.java,v 1.6 2005/03/27 19:34:38
 * cylu Exp $
 */
package net.javacoding.xsearch.core.component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.javacoding.xsearch.core.TextDocumentField;

/**
 * The class is to serialize/deserialze document
 */
public class TextDocument extends TaskQueueEntry {
    List<TextDocumentField> fields = new ArrayList<TextDocumentField>();

    private int size = 0;

    /**
     * add name/value pair
     */
    public void add(String n, String v) {
        if (n != null && v != null) {
            TextDocumentField f = new TextDocumentField(n, v);
            fields.add(f);
            size += v.length();
        }
    }
    public void add(String n, Date date) {
        add(n,Long.toString(date.getTime()));
    }

    public String get(String n) {
        for (int i = 0; i < fields.size(); i++) {
            TextDocumentField field = (TextDocumentField) fields.get(i);
            if (field.getName().equals(n)) return field.getValue();
        }
        return null;
    }

    public String[] getValues(String n) {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < fields.size(); i++) {
            TextDocumentField field = (TextDocumentField) fields.get(i);
            if (field.getName().equals(n)) result.add(field.getValue());
        }
        if (result.size() == 0) return null;
        return (String[]) result.toArray(new String[result.size()]);
    }

    public int getSize() {
        return size;
    }
    public List<TextDocumentField> getFields(){
        return fields;
    }
}
