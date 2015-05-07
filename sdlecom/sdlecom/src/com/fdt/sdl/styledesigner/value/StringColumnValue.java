package com.fdt.sdl.styledesigner.value;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("string-column-value")
public class StringColumnValue extends ColumnValue {
    
	@XStreamAlias("highlighted")
	public boolean highlighted;
    
	@XStreamAlias("summarized")
	public boolean summarized;
    
	@XStreamAlias("HTMLEscaped")
	public boolean HTMLEscaped;
        
	public StringColumnValue() {
		super();
	}
	
	public boolean isHighlighted() {
		return highlighted;
	}
	public boolean isSummarized() {
		return summarized;
	}
	public boolean isHTMLEscaped() {
		return HTMLEscaped;
	}

}
