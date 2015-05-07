package com.fdt.sdl.styledesigner.variable;

import com.fdt.sdl.styledesigner.value.ColumnsValue;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("multi-select-column-variable")
public class MultiSelectColumnVariable extends ScaffoldVariable {
    
	@XStreamAlias("columnName")
	public String columnName;

    @XStreamAlias("defaultValue")
    public ColumnsValue defaultValue;

    @XStreamAlias("isShowContent")
    public boolean isShowContnet;
    
    @XStreamAlias("columnSelector")
    public String columnSelector;
    
    public MultiSelectColumnVariable() {
		super();
	}

	public String getColumnName() {
        return columnName;
    }

    public ColumnsValue getDefaultValue() {
        return defaultValue;
    }

    public boolean getIsShowContnet() {
        return isShowContnet;
    }

    public String getColumnSelector() {
        return columnSelector;
    }

}
