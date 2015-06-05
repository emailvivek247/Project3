/**
 * 
 */
package net.javacoding.xsearch.api;

public class Sort{
    String columnName;
    boolean desc;
    public Sort(String columnName, boolean desc) {
        this.columnName = columnName;
        this.desc = desc;
    }
}