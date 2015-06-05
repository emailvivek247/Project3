package com.fdt.sdl.styledesigner.value;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("string-value")
public class StringValue extends ScaffoldValue {
	
    @XStreamAlias("value")
	public String value;
    
    public StringValue() {
		super();
	}
	
    public String getValue() {
		return value;
	}
	
    public StringValue(String value) {
		super();
		this.value = value;
	}
}
