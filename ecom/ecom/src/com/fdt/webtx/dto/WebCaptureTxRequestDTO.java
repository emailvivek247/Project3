package com.fdt.webtx.dto;

public class WebCaptureTxRequestDTO {

	private String authorizationTxReferenceNumber = null;
	
	private Double captureTxAmount = null;
	
	private String siteName = null;
	
	private String comments = null;
	
	private String modifiedBy = null;

	public String getAuthorizationTxReferenceNumber() {
		return authorizationTxReferenceNumber;
	}

	public void setAuthorizationTxReferenceNumber(
			String authorizationTxReferenceNumber) {
		this.authorizationTxReferenceNumber = authorizationTxReferenceNumber;
	}

	public Double getCaptureTxAmount() {
		return captureTxAmount;
	}

	public void setCaptureTxAmount(Double captureTxAmount) {
		this.captureTxAmount = captureTxAmount;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	@Override
	public String toString() {
		return "WebCaptureTxRequestDTO [authorizationTxReferenceNumber="
				+ authorizationTxReferenceNumber + ", captureTxAmount="
				+ captureTxAmount + ", siteName=" + siteName + ", comments="
				+ comments + ", modifiedBy=" + modifiedBy + "]";
	}
	
	
	
	
}
