package com.fdt.sdl.styledesigner.value;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("number-column-value")
public class NumberColumnValue extends ColumnValue {
   
	@XStreamAlias("numberFormat")
	public String numberFormat;
	
	public NumberColumnValue() {
		super();
	}

	public String getNumberFormat() {
		return numberFormat;
	}

}
