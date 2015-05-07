package com.fdt.achtx.service;

public class FidelityResponse {

	/**This will be one among A, D, E For Approved, Declined, Error**/
	private String xResult = null;

	/**This will be one among Approved, Declined, Error**/
	private String xStatus = null;

	/**This is to Used for populating the error message **/
	private String xError = null;

	/**This is to Used for populating Transaction Reference Number**/
	private String xRefNum = null;


	private String xAuthCode = null;


	private String xToken = null;


	private String xBatch = null;


	private String xAvsResult = null;


	private String xAvsResultCode = null;


	private String xCvvResult = null;


	private String xCvvResultCode = null;

	public String getxResult() {
		return xResult;
	}

	public void setxResult(String xResult) {
		this.xResult = xResult;
	}

	public String getxStatus() {
		return xStatus;
	}

	public void setxStatus(String xStatus) {
		this.xStatus = xStatus;
	}

	public String getxError() {
		return xError;
	}

	public void setxError(String xError) {
		this.xError = xError;
	}

	public String getxAuthCode() {
		return xAuthCode;
	}

	public void setxAuthCode(String xAuthCode) {
		this.xAuthCode = xAuthCode;
	}

	public String getxRefNum() {
		return xRefNum;
	}

	public void setxRefNum(String xRefNum) {
		this.xRefNum = xRefNum;
	}

	public String getxToken() {
		return xToken;
	}

	public void setxToken(String xToken) {
		this.xToken = xToken;
	}
	public String getxBatch() {
		return xBatch;
	}

	public void setxBatch(String xBatch) {
		this.xBatch = xBatch;
	}

	public String getxAvsResult() {
		return xAvsResult;
	}

	public void setxAvsResult(String xAvsResult) {
		this.xAvsResult = xAvsResult;
	}

	public String getxAvsResultCode() {
		return xAvsResultCode;
	}

	public void setxAvsResultCode(String xAvsResultCode) {
		this.xAvsResultCode = xAvsResultCode;
	}

	public String getxCvvResult() {
		return xCvvResult;
	}

	public void setxCvvResult(String xCvvResult) {
		this.xCvvResult = xCvvResult;
	}

	public String getxCvvResultCode() {
		return xCvvResultCode;
	}

	public void setxCvvResultCode(String xCvvResultCode) {
		this.xCvvResultCode = xCvvResultCode;
	}
	@Override
	public String toString() {
		return "FidelityResponse [xResult=" + xResult + ", xStatus=" + xStatus
				+ ", xError=" + xError + ", xAuthCode=" + xAuthCode
				+ ", xRefNum=" + xRefNum + ", xToken=" + xToken + ", xBatch="
				+ xBatch + ", xAvsResult=" + xAvsResult + ", xAvsResultCode="
				+ xAvsResultCode + ", xCvvResult=" + xCvvResult
				+ ", xCvvResultCode=" + xCvvResultCode + "]";
	}
}
