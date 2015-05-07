package com.fdt.common.dto;

import java.io.Serializable;


/**
 * This class is an General Service response with Status and Message request to Firm Level User Add/Update services.
 * 
 * @author APatel
 *
 */
public class ServiceResponseDTO implements Serializable {


	private static final long serialVersionUID = 1276164065309746593L;

	public static final String ERROR = "ERROR";
    public static final String SUCCESS = "SUCCESS";
    
    private String status;
    
    private String message;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}     
	
	@Override
    public String toString() {
        return "ServiceResponseDTO ["
        		+ "status=" + status 
        		+ ", message=" + message
                + "]";
    }

}