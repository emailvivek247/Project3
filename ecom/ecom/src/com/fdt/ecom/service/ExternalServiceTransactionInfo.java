package com.fdt.ecom.service;

import java.util.Date;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.fdt.otctx.entity.OTCTx;
import com.fdt.webtx.dto.WebTxExtResponseDTO;
import com.fdt.webtx.entity.WebTx;

@WebService
public interface ExternalServiceTransactionInfo {

	@WebMethod
    public OTCTx getOTCTransactionByTxRefNum(@WebParam(name="txRefNumber") String txRefNumber,
    	@WebParam(name="siteName") String siteName);

	@WebMethod
    public OTCTx getOTCTransactionByInvoiceNumber(@WebParam(name="invoiceNumber") String invoiceNumber,
    	@WebParam(name="siteName") String siteName);

	/**This is used by external APPS like CMS**/
	@WebMethod
	public WebTxExtResponseDTO getWebTransactionsForExtApp(
			@WebParam(name="siteName") String siteName,
			@WebParam(name="fromDate") Date fromDate,
			@WebParam(name="endDate") Date endDate,
			@WebParam(name="txType") String txType);

	@WebMethod
	public WebTx getWebTxByInvoiceNumber(@WebParam(name="invoiceNumber") String invoiceNumber,
			@WebParam(name="siteName") String siteName);

	@WebMethod
	public WebTx getWebTxByTxRefNum(@WebParam(name="txRefNumber") String txRefNumber,
			@WebParam(name="siteName") String siteName);

}