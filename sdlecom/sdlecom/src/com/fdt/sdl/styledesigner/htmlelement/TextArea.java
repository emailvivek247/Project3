package com.fdt.sdl.styledesigner.htmlelement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("textarea")
public class TextArea extends HtmlElement {
	
    @XStreamAsAttribute
	public String rows;
    
    @XStreamAsAttribute
	public String cols;
    
    public TextArea() {
		super();
	}

	public String getRows() {
		return rows;
	}

	public String getColumns() {
		return cols;
	}

	@Override
	public String getHtmlElementType() {
		return "textarea";
	}
   
}
