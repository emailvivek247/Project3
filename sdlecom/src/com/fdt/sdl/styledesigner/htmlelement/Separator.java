package com.fdt.sdl.styledesigner.htmlelement;

import com.fdt.sdl.styledesigner.variable.ScaffoldVariable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("separator")
public class Separator extends ScaffoldVariable {
	
	@XStreamAsAttribute
	public String header;
	
	@XStreamAsAttribute
	public String position;
	
	@XStreamAsAttribute
	public String status;
	
	public Separator() {
		super();
	}

	public String getHeader() {
		return header;
	}
	
	public String getPosition() {
		return position;
	}
	
	public String getStatus() {
		return status;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public void setPosition(String position) {
		this.position = position;
	}
	
	public void setStatus(String position) {
		this.status = position;
	}

	

	public String getType() {
		String className = this.getClass().getName();
		String shortName = className.substring(className.lastIndexOf(".")+1);
		return shortName.toLowerCase();
	}
}
