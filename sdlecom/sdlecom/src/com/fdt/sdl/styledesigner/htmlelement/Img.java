package com.fdt.sdl.styledesigner.htmlelement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("img")
public class Img extends HtmlElement {
	
    @XStreamAsAttribute
	public String src;
    
    @XStreamAsAttribute
	public String alt;
    
    public Img() {
		super();
	}

	public String getSrc() {
		return this.src;
	}

	public String getAlt() {
		return this.alt;
	}
	
	@Override
	public String getHtmlElementType() {
		return "img";
	}
}
