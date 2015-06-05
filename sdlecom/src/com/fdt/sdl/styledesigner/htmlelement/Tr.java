package com.fdt.sdl.styledesigner.htmlelement;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("tr")
public class Tr extends HtmlElement {
	
    @XStreamAsAttribute
	public String valign;
    
    @XStreamAlias("td")
    public List<Td> columns;
    
    public Tr() {
		super();
	}

	public List<Td> getColumns() {
		return columns;
	}

	public String getValign() {
		return valign;
	}
	
	@Override
	public String getHtmlElementType() {
		return "tr";
	}	
	
}
