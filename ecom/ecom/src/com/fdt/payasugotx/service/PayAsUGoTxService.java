package com.fdt.payasugotx.service;

import java.util.Date;
import java.util.List;

import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.ecom.entity.Location;
import com.fdt.ecom.entity.ShoppingCartItem;
import com.fdt.ecom.entity.Site;
import com.fdt.payasugotx.dto.PayAsUSubDTO;
import com.fdt.payasugotx.entity.PayAsUGoTx;
import com.fdt.payasugotx.entity.PayAsUGoTxItem;
import com.fdt.payasugotx.entity.PayAsUGoTxView;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;

public interface PayAsUGoTxService {

	/**
     * This Method Is Called From Web Service Itself And Is Used To Pay For List Of Images In The Shopping Cart.
     * This Method Charges Shopping Cart Items Based On Site (Access Name). Service Fee Calculation Is Done In
     * DefaultWebFeeCaluculator Based On Type Of Non-Recurring Subscription (PPF, PDF, PPV, FR). All The Web Transaction
     * Items Are Saved Along With (parent) Webtransaction.
     *
     * @param userName EmailId of the user logged in.
     * @param payAsUGoTransactionDTO Contains List Of Shoppingcartitems, Creditcard.
     * @return The List Of Webtransactions.
     * @throws PaymentGatewayUserException
     * @throws PaymentGatewaySystemException
     * @throws SDLBusinessExceptiono
     */
    public List<PayAsUGoTx> doSalePayAsUGo(String userName, PayAsUSubDTO payAsUGoTransactionDTO)
            throws PaymentGatewayUserException, PaymentGatewaySystemException, SDLBusinessException;


    /** This Method Is Used To Get All The Information Like Transaction Amount, Fees Before The Payment.
     * @param userName EmailId of the User Logged In.
     * @param shoppingCart Contains The List Of Shopping Cart Items.
     * @return The List Of Shopping Cart Items with respective fees set.
     * @throws SDLBusinessException
     */
    public List<ShoppingCartItem> doSalePayAsUGoInfo(String userName, List<ShoppingCartItem> shoppingCart)
            throws SDLBusinessException;

    /** This Method Is Used To Verify Whether A User Purchased The Document.
     * @param userName EmailId of the User Logged In.
     * @param productKey ProductKey of the document.
     * @param uniqueIdentifier Access Name.
     * @return Transaction Reference Number.
     * @throws SDLBusinessException
     */
    public PayAsUGoTxItem getPayAsUGoTxIdForPurchasedDoc(String userName, String productKey, String uniqueIdentifier);

    /** This Method Is Used To Get ShoppingBasket Items For The User.
     *
     * @param userName EmailId of the User Logged In.
     * @param nodeName Node Name.
     * @return the list of ShoppingCart Items.
     */
    public List<ShoppingCartItem>  getShoppingBasketItems(String userName, String nodeName);

    /** This Method Is Used To Delete Specified ShoppingCartItems.
     * @param userName EmailId of the User Logged In.
     * @param nodeName Node Name.
     * @return
     */
    public void deleteShoppingCart(List<ShoppingCartItem> shoppingCartItems);

    /** This Method Is Used To Delete Specified ShoppingCartItem.
     * @param userName EmailId of the User Logged In.
     * @param nodeName Node Name.
     * @return
     */
    public void deleteShoppingCartItem(ShoppingCartItem shoppingCartItem);

    /** This Method Returns All The PayAsUGo Transactions Made By The User For A Particular Node.
     * @param userName EmailId of the User Logged In.
     * @param nodeName Node Name.
     * @return The List Of WebTransactions.
     */
    public List<PayAsUGoTxView> getPayAsUGoTxByNode(String userName, String nodeName, String comments, Date fromDate,
    		Date toDate) ;

    /** This Method Returns All The PayAsUGo Transactions Made By The User For A Particular Node.
     * @param userName EmailId of the User Logged In.
     * @param nodeName Node Name.
     * @return The List Of PayAsUGoTransactions.
     */
    public PageRecordsDTO getPayAsUGoTxByNodePerPage(String userName, String nodeName, String comments, Date fromDate,
    		Date toDate, int startingFrom, int numberOfRecords);

    /** This Method Returns All The Information About PayAsUGotransaction Made By The User.
     * @param userName EmailId of the User Logged In.
     * @param webtransactionId WebtransactionId.
     * @param isRefund Boolean Flag.
     * @return PayAsUGoTransaction
     */
    public PayAsUGoTx getPayAsUGoTxDetail(String userName, Long webTxId, String isRefund);

    /** This Method Is Used To do Reference Credit For PayAsUGo Transactions.
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
    public PayPalDTO doReferenceCreditPayAsUGo(String txRefNumber, String comments, String modUserId, String machineName,
    		String siteName)  throws PaymentGatewayUserException, PaymentGatewaySystemException, SDLBusinessException;


    /** This method is used to save shoppingCartItem.
     * @param shoppingCartItem
     */
    public void saveShoppingCartItem(ShoppingCartItem shoppingCartItem);

    /** This Method Is Used To Get The All The Information About PayAsUGotransaction Of The Supplied Transaction
     * Reference Number.
     * @param txRefNumber Transaction Reference Number.
     * @param siteName Name Of Site.
     * @return PayAsUGoTransaction.
     */
    public PayAsUGoTx getPayAsUGoTxByTxRefNum(String txRefNumber, String siteName);

	public void updateShoppingCartComments(Long shoppingCartId, String comments);

	public List<Location> getLocationsBySiteId(Long siteId);

	public Location getLocationByNameAndAccessName(String locationName, String accessName);

	public Location getLocationSignatureById(Long locationId);

	public Location getLocationSealById(Long locationId);

	public void archivePayAsUGoTransactions(String archivedBy, String archiveComments);
	
	public boolean isThresholdReached(Site site, Double txAmount);


	public String getDocumentIdByCertifiedDocumentNumber(String certifiedDocumentNumber, String siteName);

}