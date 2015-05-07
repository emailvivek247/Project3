/*
 * Copyright 2004.
 */

package net.javacoding.xsearch.config;


/**
 * Class mapped to the <code>&lt;dataquery&gt;</code> element of a dataset
 * configuration file. It describes a workingqueue dataquery.
 *
 */
public class WorkingQueueDataquery extends Dataquery{

    // ----------------------------------------------------------- Constructors

    public WorkingQueueDataquery() {
        name = "WorkingQueue";
    }

    /**
     * Returns the primary key column.
     */
    public Column getPrimaryKeyColumn() {
        if (columns == null) {
            return null;
        }
        for (int i = 0; i < columns.size(); i++) {
            Column c = (Column)columns.get(i);
            if(c != null && c.getIsPrimaryKey()){
                return c;
            }
        }
        return null;
    }

    /**
     * Returns the modified date column.
     */
    public Column getModifiedDateColumn() {
        if (columns == null) {
            return null;
        }
        for (int i = 0; i < columns.size(); i++){
            Column c = (Column)columns.get(i);
            if(c != null && c.getIsModifiedDate()) {
                return c;
            }
        }
        return null;
    }

}
