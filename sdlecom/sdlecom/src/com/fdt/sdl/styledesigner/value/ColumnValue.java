package com.fdt.sdl.styledesigner.value;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("column-value")
public class ColumnValue extends ScaffoldValue{
    
    @XStreamAlias("columnName")
	public String columnName;
    
    public ColumnValue() {
		super();
	}
	
    public String getColumnName() {
		return columnName;
	}

    @XStreamAlias("isSelected")
	public boolean isSelected = false;
    public boolean getIsSelected() {
        return isSelected;
    }
}
