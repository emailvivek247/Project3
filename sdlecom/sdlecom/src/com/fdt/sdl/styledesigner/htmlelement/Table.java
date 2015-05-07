package com.fdt.sdl.styledesigner.htmlelement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("table")
public class Table extends HtmlElement {
	
	
    @XStreamAsAttribute
	public String cellspacing;
    
    @XStreamAsAttribute
	public String cellpadding;

    @XStreamAsAttribute
	public String border;
    
    public Table() {
		super();
	}

	@Override
	public String getHtmlElementType() {
		return "table";
	}

	public String getCellspacing() {
		return cellspacing;
	}

	public String getCellpadding() {
		return cellpadding;
	}

	public String getBorder() {
		return border;
	}

}
