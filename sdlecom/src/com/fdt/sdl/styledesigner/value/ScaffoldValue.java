package com.fdt.sdl.styledesigner.value;

import net.javacoding.xsearch.config.XMLSerializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

public abstract class ScaffoldValue extends XMLSerializable{
    @XStreamAlias("name")
	public String name;
	public String getName() {
		return name;
	}
	public String getType() {
		String className = this.getClass().getName();
		String shortName = className.substring(className.lastIndexOf(".")+1);
		return shortName.substring(0, shortName.length()-"Value".length()).toLowerCase();
	}
}
