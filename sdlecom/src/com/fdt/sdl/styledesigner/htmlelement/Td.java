package com.fdt.sdl.styledesigner.htmlelement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("td")
public class Td extends HtmlElement {
	
    @XStreamAsAttribute
	public String colspan;
    
    @XStreamAsAttribute
	public String rowspan;
    
    public Td() {
		super();
	}

	public String getColspan() {
		return colspan;
	}

	public String getRowspan() {
		return rowspan;
	}
	
	@Override
	public String getHtmlElementType() {
		return "td";
	}
}
