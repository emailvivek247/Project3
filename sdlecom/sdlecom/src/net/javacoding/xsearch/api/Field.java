package net.javacoding.xsearch.api;

import net.javacoding.xsearch.api.protocol.SearchProtocol;

public class Field {
    SearchProtocol.Field field;

    public Field(SearchProtocol.Field field) {
        this.field = field;
    }
    public String getName() {return this.field.getName();}
    public String getValue() {return this.field.getValue();}
    public String getType() {return this.field.getType().name();}
    
    public String toString() {
        return getName()+"("+getType()+")"+getValue();
    }
}
