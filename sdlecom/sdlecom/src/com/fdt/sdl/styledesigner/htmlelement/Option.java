package com.fdt.sdl.styledesigner.htmlelement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("option")
public class Option extends HtmlElement {
	
	public Option() {
		super();
	}
	
    @XStreamAsAttribute
	public String selected;

    public Option(String selected) {
		super();
		this.selected = selected;
	}

	public String getSelected() {
		return selected;
	}

	@Override
	public String getHtmlElementType() {
		return "option";
	}
   
}
