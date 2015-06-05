package com.fdt.sdl.styledesigner.value;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("columns-value")
public class ColumnsValue extends ScaffoldValue {
    
	@XStreamAlias("columns")
	public List<ColumnValue> columns;

    public ColumnsValue() {
		super();
	}
	
    public List<ColumnValue> getColumns() {
		return columns;
	}
	
    public List<String> getColumnNames(){
	    List ret = new ArrayList<String>();
	    if(columns!=null) for(ColumnValue cv : columns) {
	        ret.add(cv.columnName);
	    }
	    return ret;
	}

}
