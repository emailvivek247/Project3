package com.fdt.sdl.styledesigner.htmlelement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("select")
public class Select extends HtmlElement {
	
    @XStreamAsAttribute
	public String multiple;
    
    public Select() {
		super();
	}

	public String getMultiple() {
		return multiple;
	}

	@Override
	public String getHtmlElementType() {
		return "select";
	}
   
}
