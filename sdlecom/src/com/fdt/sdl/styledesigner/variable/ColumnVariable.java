package com.fdt.sdl.styledesigner.variable;

import com.fdt.sdl.styledesigner.value.ColumnValue;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("column-variable")
public class ColumnVariable extends ScaffoldVariable {
	
	public static String COLUMN_PRIMARY_KEY      = "isPrimaryKey";
    public static String COLUMN_MODIFIED_DATE    = "isModifiedDate";
	
	@XStreamAlias("columnName")
	public String columnName;

    @XStreamAlias("defaultValue")
	public ColumnValue defaultValue;
    
    @XStreamAlias("columnSelector")
    public String columnSelector;
    
    public ColumnVariable() {
		super();
	}

	public String getColumnName() {
		return columnName;
	}

	public ColumnValue getDefaultValue() {
		return defaultValue;
	}
	
    public String getColumnSelector() {
        return columnSelector;
    }

}
