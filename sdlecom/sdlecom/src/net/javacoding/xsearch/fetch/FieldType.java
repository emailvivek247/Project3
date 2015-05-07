package net.javacoding.xsearch.fetch;

public abstract class FieldType {
    String name;
    boolean isPrimaryKey;
    boolean isModifiedTime;
    
    public FieldType(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public FieldType setName(String name) {
        this.name = name;
        return this;
    }
    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }
    public FieldType setPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
        return this;
    }
    public boolean isModifiedTime() {
        return isModifiedTime;
    }
    public FieldType setModifiedTime(boolean isModifiedTime) {
        this.isModifiedTime = isModifiedTime;
        return this;
    }
}
