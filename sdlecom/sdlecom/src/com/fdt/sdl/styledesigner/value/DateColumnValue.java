package com.fdt.sdl.styledesigner.value;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("date-column-value")
public class DateColumnValue extends ColumnValue {
   
	@XStreamAlias("dateFormat")
	public String dateFormat;
	
	public DateColumnValue() {
		super();
	}

	public String getDateFormat() {
		return dateFormat;
	}

}
