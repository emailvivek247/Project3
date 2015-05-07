package com.fdt.webtx.dto;

public class WebCaptureTxResponseDTO {

	private String captureTxReferenceNumber = null;

    private String errorCode = null;

    private String errorDesc = null;
    
    private Double captureTxAmount = null;

	public String getCaptureTxReferenceNumber() {
		return captureTxReferenceNumber;
	}

	public void setCaptureTxReferenceNumber(String captureTxReferenceNumber) {
		this.captureTxReferenceNumber = captureTxReferenceNumber;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorDesc() {
		return errorDesc;
	}

	public void setErrorDesc(String errorDesc) {
		this.errorDesc = errorDesc;
	}

	public Double getCaptureTxAmount() {
		return captureTxAmount;
	}

	public void setCaptureTxAmount(Double captureTxAmount) {
		this.captureTxAmount = captureTxAmount;
	}

	@Override
	public String toString() {
		return "WebCaptureTxResponseDTO [captureTxReferenceNumber="
				+ captureTxReferenceNumber + ", errorCode=" + errorCode
				+ ", errorDesc=" + errorDesc + ", captureTxAmount="
				+ captureTxAmount + "]";
	}
    
}
