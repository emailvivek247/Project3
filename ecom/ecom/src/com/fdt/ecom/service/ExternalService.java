package com.fdt.ecom.service;

import java.util.Date;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.otctx.dto.OTCRequestDTO;
import com.fdt.otctx.dto.OTCResponseDTO;
import com.fdt.otctx.entity.OTCTx;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.webtx.dto.WebCaptureTxRequestDTO;
import com.fdt.webtx.dto.WebCaptureTxResponseDTO;
import com.fdt.webtx.dto.WebTxExtResponseDTO;
import com.fdt.webtx.entity.WebTx;

@WebService
public interface ExternalService {

	@WebMethod
	public OTCResponseDTO doSaleOTC(@WebParam(name="otcRequestDTO") OTCRequestDTO otcRequestDTO);

	@WebMethod
    public OTCResponseDTO doSaleOTCWithOutEncryption(@WebParam(name="otcRequestDTO") OTCRequestDTO otcRequestDTO);

	@WebMethod
    public OTCTx getOTCTransactionByTxRefNum(@WebParam(name="txRefNumber") String txRefNumber,
    	@WebParam(name="siteName") String siteName);

	@WebMethod
    public OTCTx getOTCTransactionByInvoiceNumber(@WebParam(name="invoiceNumber") String invoiceNumber,
    	@WebParam(name="siteName") String siteName);

	@WebMethod
    public OTCResponseDTO doSaleGetInfoOTC(@WebParam(name="siteName") String siteName,
    	@WebParam(name="actualAmtToCharge") double actualAmtToCharge);

	@WebMethod
	public PayPalDTO doReferenceCredit(@WebParam(name="txRefNumber") String txRefNumber,
			@WebParam(name="comments") String comments, @WebParam(name="modUserId") String modUserId,
			@WebParam(name="machineName") String machineName, @WebParam(name="siteName")  String siteName);

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
	
	@WebMethod
	public WebCaptureTxResponseDTO captureWebTx(@WebParam(name="webCaptureTxRequestDTO") WebCaptureTxRequestDTO webCaptureTxRequestDTO) 
			throws SDLBusinessException;

}