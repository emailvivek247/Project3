package com.fdt.webtx.service;

import java.util.Date;
import java.util.List;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.webtx.dto.WebTransactionDTO;
import com.fdt.webtx.dto.WebTxExtResponseDTO;
import com.fdt.webtx.entity.WebTx;
import com.fdt.webtx.entity.WebTxItem;

public interface WebTxService {


    /**This Method Is Used For Traffic Ticket Payments. All The Web Transaction Items Are Saved Along With (parent)
	 * Webtransaction, If Payment Is Successful. If EmailId Is Not Null Then Receipt Will Be Also Sent.
	 * @param siteName Name of the Site.
	 * @param webTransactionDTO Contains Webtransaction item and Creditcard.
	 * @param emailId EmailId to which receipt is sent. If null, then this parameter is assigned the value of application.
	 * @param application Application Name.
	 * @return WebTransaction.
	 * @throws PaymentGatewayUserException
	 * @throws PaymentGatewaySystemException
	 * @throws SDLBusinessException
	 */
    public WebTx doSaleWebPosts(String siteName, WebTransactionDTO webTransactionDTO, String emailId,
            String application) throws PaymentGatewayUserException, PaymentGatewaySystemException, SDLBusinessException;


    /** This Method Is Used To do Reference Credit For Web Transactions.
     * @param txRefNumber Original Transaction Reference Number.
     * @param comments Comments Made By User While Refunding.
     * @param modUserId User Who Is Refunding.
     * @param machineName Machine Name From Which User Is Refunding
     * @param siteName SiteName Where The Original Transaction Is Made.
     * @return paypalDTO Returns DTO With TxNumber If Payment Goes Through.
     * @throws PaymentGatewayUserException
     * @throws PaymentGatewaySystemException
     * @throws SDLBusinessException
     */
    public PayPalDTO doReferenceCreditWeb(String txRefNumber, String comments, String modUserId, String machineName,
    		String siteName)  throws PaymentGatewayUserException, PaymentGatewaySystemException, SDLBusinessException;

    /** The method getWebTransactionsForExtApp(String siteName, Date fromDate, Date endDate, String txType)
     *  checks whether the siteName passed is a valid one or not. If it is not valid, then it throws SDLBusinessException
     *  and makes sure that difference between fromDate and endDate is less than searchDayThreshold for that particular Site.
     * @param siteName            Throws  SDLBusinessException if siteName is Invalid
     * @param fromDate            fromDate
     * @param endDate            endDate
     * @param txType            CHARGE, REFUND or null
     * @return List<WebTransaction>        List of Web Transactions occured during the mentioned dates for a particular site.
     */
    public WebTxExtResponseDTO getWebTransactionsForExtApp(String siteName, Date fromDate, Date endDate,
            String txType);

    /** This Method Is Used To Get The All The Information About Webtransaction Of The Supplied Transaction Reference Number.
     * @param txRefNumber Transaction Reference Number.
     * @param siteName Name Of Site.
     * @return WebTransaction.
     */
    public WebTx getWebTransactionByTxRefNum(String txRefNumber, String siteName);

    /** This Method Calculates The Service Fee For Each Webtransaction Item.
     * @param siteName Name Of The Site.
     * @param itemList List Of WebTransactionItems.
     * @return List Of WebTransactionItems With Service Fee & Total Transaction Amount Set.
     * @throws SDLBusinessException
     */
    public List<WebTxItem> doSaleGetInfoWEB(String siteName, List<WebTxItem> itemList)
            throws SDLBusinessException;

    /** This Method Is Used To Get The All The Information About Webtransaction Of The Supplied invoiceId.
     * @param txRefNumber invoiceId.
     * @param siteName Name Of Site.
     * @return WebTransaction.
     */
	public WebTx getWebTxByInvoiceNumber(String invoiceNumber, String siteName);
}