package com.fdt.common.util.adapter;

import com.fdt.sdl.ws.client.Document;

public class SDLDMSDocument extends Document{

	private String errorCode = null;
	
	private String errorDescription = null;

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	@Override
	public String toString() {
		return "SDLDMSDocument [errorCode=" + errorCode + ", errorDescription=" + errorDescription + ", getDocType()=" + getDocType() + ", getFileExtension()="
				+ getFileExtension() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}

	
	
	
}
