package com.fdt.sdl.styledesigner.htmlelement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("input")
public class Input extends HtmlElement {
	
    @XStreamAsAttribute
	public String inputType;
    
    @XStreamAsAttribute
	public String maxLength;
    
    @XStreamAsAttribute
	public String checked;
    
    public Input() {
		super();
	}

	public String getMaxlength() {
		return maxLength;
	}
    
	public String getInputType() {
		return inputType;
	}

	public String getChecked() {
		return checked;
	}

	@Override
	public String getHtmlElementType() {
		return "input";
	}
   
}
