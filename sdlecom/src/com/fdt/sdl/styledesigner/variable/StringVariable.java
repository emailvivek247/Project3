package com.fdt.sdl.styledesigner.variable;

import com.fdt.sdl.styledesigner.value.StringValue;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("string-variable")
public class StringVariable extends ScaffoldVariable {
    
	@XStreamAlias("defaultValue")
	public StringValue defaultValue;
	
	public StringVariable() {
		super();
	}

	public String getDefaultValue() {
		return defaultValue==null? "" : defaultValue.value;
	}
}
