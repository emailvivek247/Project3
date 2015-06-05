package com.fdt.sdl.styledesigner.variable;

import com.fdt.sdl.styledesigner.value.ColumnsValue;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("columns-variable")
public class ColumnsVariable extends ScaffoldVariable {
	
	public static String COLUMN_FILTERABLE  = "isFilterable";
    public static String COLUMN_DISPLAYABLE = "isDisplayable";
    public static String COLUMN_SORTABLE    = "isSortable";
    public static String COLUMN_IS_NUMBER   = "isNumber";
    public static String COLUMN_IS_DATE     = "isDate";
    public static String COLUMN_IS_STRING   = "isString";
    
	@XStreamAlias("defaultValue")
	public ColumnsValue defaultValue;
	
	public ColumnsVariable() {
		super();
	}
	
	public ColumnsValue getDefaultValue() {
		return defaultValue;
	}

    @XStreamAlias("columnSelector")
    public String columnSelector;
    public String getColumnSelector() {
        return columnSelector;
    }
    
}
