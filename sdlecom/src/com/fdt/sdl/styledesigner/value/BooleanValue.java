package com.fdt.sdl.styledesigner.value;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("boolean-value")
public class BooleanValue extends ScaffoldValue {
    
	@XStreamAlias("value")
	public boolean value;

    public BooleanValue() {
		super();
	}

	public BooleanValue(boolean value) {
		super();
		this.value = value;
	}
	
	public boolean getValue() {
		return value;
	}

}
