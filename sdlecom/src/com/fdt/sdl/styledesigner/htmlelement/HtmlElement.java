package com.fdt.sdl.styledesigner.htmlelement;

import java.util.List;

import com.fdt.sdl.styledesigner.variable.ScaffoldVariable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public abstract class HtmlElement extends ScaffoldVariable {
	
    @XStreamAlias("javascript")
	public String javascript;

    @XStreamAlias("javascript-event")
	public String javascriptEvent;
    
    @XStreamAsAttribute
	public String value;
    
    @XStreamAsAttribute
	public String align;

    @XStreamAsAttribute
	public String title;

    @XStreamAlias("child-element")
    public List<HtmlElement> childElements;

    @Override 
	public String getType() {
		return "htmlelement";
	}

	public String getJavascript() {
		return javascript;
	}

	public String getJavascriptEvent() {
		return javascriptEvent;
	}

	public String getValue() {
		return value;
	}

	public String getAlign() {
		return align;
	}

	public String getTitle() {
		return title;
	}

	public List<HtmlElement> getChildElements() {
		return childElements;
	}

	public abstract String getHtmlElementType();
}
