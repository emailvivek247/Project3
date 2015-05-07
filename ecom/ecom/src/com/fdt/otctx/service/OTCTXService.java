package com.fdt.otctx.service;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.otctx.dto.OTCRequestDTO;
import com.fdt.otctx.dto.OTCResponseDTO;
import com.fdt.otctx.entity.OTCTx;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;

public interface OTCTXService {

    /** This Method Is Used For Charging Over The Counter Transactions. This Method Internally Uses Magensa Service To
     * Decrypt Credit Card Data.
     * @param otcRequestDTO
     * @return OTCResponseDTO
     */
    public OTCResponseDTO doSaleOTC(OTCRequestDTO otcRequestDTO);

    /** This Method Is Used For Charging OTC Transactions.
     * @param otcRequestDTO
     * @return OTCResponseDTO
     */
    public OTCResponseDTO doSaleOTCWithOutEncryption(OTCRequestDTO otcRequestDTO);

    /** This Method Is Used For Getting The OTC Transaction Information of supplied Transaction Reference Number.
     * @param txRefNumber Transaction Reference Number.
     * @param siteName Name of the Site.
     * @return OTCResponseDTO
     */
    public OTCTx getOTCTransactionByTxRefNum(String txRefNumber, String siteName);

    /** This Method Is Used For Calculating The Service Fee For The Base Amount On A Given Site.
     * @param siteName Name of the Site.
     * @param actualAmtToCharge BaseAmount.
     * @return OTCTransaction
     */
    public OTCResponseDTO doSaleGetInfoOTC(String siteName, double actualAmtToCharge);

    /** This Method Is Used For Doing A Reference Credit For Over The Counter Transaction.
     * @param txRefNumber OTC Transaction Reference Number
     * @param comments Comments Entered By The User Who Is Issuing A Refund.
     * @param modUserId User Who Is Issuing A Refund.
     * @param machineName Machine Name From Which User Is Issuing A Refund.
     * @param siteName Name Of Site.
     * @return
     * @throws PaymentGatewayUserException
     * @throws PaymentGatewaySystemException
     * @throws SDLBusinessException
     */
    public PayPalDTO doReferenceCreditOTC(String txRefNumber, String comments, String modUserId, String machineName,
    		String siteName) throws PaymentGatewayUserException, PaymentGatewaySystemException, SDLBusinessException;

	public OTCTx getOTCTransactionByInvoiceNumber(String invoiceNumber, String siteName);

}
