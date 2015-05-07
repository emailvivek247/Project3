package net.javacoding.xsearch.core;

import java.io.Serializable;

public class TextDocumentField implements Serializable {

    private String name = "body";
    private String fieldsData = null;

    public TextDocumentField(String name, String value) {
        if(name!=null) {
            this.name = name.intern();        // field names are interned
        }
        this.fieldsData = value;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getValue() {
        return this.fieldsData;
    }

}
