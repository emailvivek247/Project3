package com.fdt.common.export;

public class PDFCell {
	
	private String value;
	
	private boolean wrap;
	
	public PDFCell(String value, boolean wrap){
		this.value = value;
		this.wrap = wrap;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isWrap() {
		return wrap;
	}

	public void setWrap(boolean wrap) {
		this.wrap = wrap;
	}
	
	
}
