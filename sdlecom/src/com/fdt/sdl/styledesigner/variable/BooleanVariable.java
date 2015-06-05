package com.fdt.sdl.styledesigner.variable;

import com.fdt.sdl.styledesigner.value.BooleanValue;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("boolean-variable")
public class BooleanVariable extends ScaffoldVariable {
    
	@XStreamAlias("defaultValue")
	public BooleanValue defaultValue;

	public BooleanVariable() {
		super();
	}

	public boolean getDefaultValue() {
		return defaultValue!=null ? defaultValue.value : false;
	}
}
