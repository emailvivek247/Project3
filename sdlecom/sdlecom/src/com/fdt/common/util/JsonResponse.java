package com.fdt.common.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.fdt.security.ui.form.FirmUser;


public class JsonResponse {

	private List<FirmUser> models = null;
	
	private String errorCode;
	
	private List<String> errors;
	

	public List<FirmUser> getModels() {
		this.models = this.models == null ? new ArrayList<FirmUser>() : this.models;
		return this.models;
	}

	public void setModels(List<FirmUser> users) {
		this.models = users == null ? new ArrayList<FirmUser>() : users;
	}
	
	public void addToModel(FirmUser user){
		if(this.models == null){
			models = new ArrayList<FirmUser>();
		}
		this.models.add(user);
	}

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
