package com.fdt.sdl.styledesigner.htmlelement;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("label")
public class Label extends HtmlElement {
	
	public Label() {
		super();
	}

	@Override
	public String getHtmlElementType() {
		return "label";
	}
   
}
