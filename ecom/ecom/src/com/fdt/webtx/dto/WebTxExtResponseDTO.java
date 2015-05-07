package com.fdt.webtx.dto;

import java.util.List;

import com.fdt.webtx.entity.WebTx;

public class WebTxExtResponseDTO {

	List<WebTx> webTransactionList = null;

	private String errorDesc = null;

	public List<WebTx> getWebTransactionList() {
		return webTransactionList;
	}

	public void setWebTransactionList(List<WebTx> webTransactionList) {
		this.webTransactionList = webTransactionList;
	}

	public String getErrorDesc() {
		return errorDesc;
	}

	public void setErrorDesc(String errorDesc) {
		this.errorDesc = errorDesc;
	}

	@Override
	public String toString() {
		return "WebTxExtResponseDTO [webTransactionList=" + webTransactionList
				+ ", errorDesc=" + errorDesc + ", getWebTransactionList()="
				+ getWebTransactionList() + ", getErrorDesc()="
				+ getErrorDesc() + "]";
	}
}