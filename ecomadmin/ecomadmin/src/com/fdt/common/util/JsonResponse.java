package com.fdt.common.util;

import java.util.LinkedList;
import java.util.List;


public class JsonResponse {
	public static String SUCCESS = "SUCCESS";
	public static String ERROR = "ERROR";

	private String errorCode;
	
	private List<String> errors;
	

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
	
	public void addError(String error){
		if(this.errors == null){
			this.errors = new LinkedList<>();
		}
		this.errors.add(error);
	}

}
