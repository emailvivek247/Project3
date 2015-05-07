package com.fdt.paymentgateway.service;

import com.fdt.ecom.entity.BankAccount;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.Merchant;
import com.fdt.ecom.entity.Site;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.recurtx.dto.UserAccountDetailDTO;
import com.fdt.subscriptions.dto.AccessDetailDTO;

public interface PaymentGatewayService {

	/** This method is used to do credit when user up grades/down grades subscription.
     * @param existingUserAccountDTO Existing Subscription Information.
     * @param newAccessDTO New Subscription Information.
     * @param amount Amount To Be Credited
     * @param moduleName Method Name For Logging Purpose.
     * @param userName Email Id Of Person Who Is upgrading/downgrading the subscription.
     * @return Transaction Reference Number.
     * @throws PaymentGatewayUserException
     * @throws PaymentGatewaySystemException
     */
    public String doCredit(UserAccountDetailDTO existingUserAccountDTO, AccessDetailDTO newAccessDTO, Double amount,
            String moduleName, String userName) throws PaymentGatewayUserException, PaymentGatewaySystemException;

    /** The Method Is Used Do Credit Amount To Card Supplied. This Method Takes OriginalTxNumber Also And It Ensures
     * That Amount To Be Credited Is Less Than The Amount Charged Previously.
     * @param creditCard Credit Card To Which amount Is Refunded.
     * @param merchantInfo Paypal Credentials
     * @param amount Amount To Be Refunded.
     * @param originalTxNumber Original Transaction Reference Number.
     * @param moduleName Method Name For Logging Purpose.
     * @param userName Email Id Of User Who is Performing the Credit Operation.
     * @return Transaction Reference Number.
     * @throws PaymentGatewayUserException
     * @throws PaymentGatewaySystemException
     */
    public String doCredit(CreditCard creditCard, Merchant merchantInfo, Double amount, String originalTxNumber,
            String moduleName, String userName) throws PaymentGatewayUserException, PaymentGatewaySystemException;

    /** This Method Is Used For Charging. Depending On AmtToCharge It First Determines Whether Micro Or Normal Merchant
     * Is Used. If PaymentGatewaySystemException Occurs, ErrorCode Is Saved In Database. For All Other Exceptions,
     * Transaction Is Rolled Back.
    * The Transactional Attribute Should Be There If Not, The System Will Not Insert Data In The The Error Code Table.
    * @param site This Variable Provides Site Specific Paypal Credentials, And Is Useful Is Selecting Micro Or Normal
    * 			   Merchant.
    * @param amtToCharge Amount To Be Charged.
    * @param creditCard Credit Card To Which amtToCharge Is Charged.
    * @param moduleName Method Name For Logging Purpose.
    * @param userName Email Id Of User Who is Performing the Sale Operation.
    * @param isRecurring Flag to Indicate Whether Sale is Perfomed For Recurring Or Web Transaction
    * @return PayPalDTO PaypalDTO with either Transaction Reference Number or Error Code & Description.
    * @throws PaymentGatewayUserException
    * @throws PaymentGatewaySystemException
    */
    public PayPalDTO doSale(Site site, double amtToCharge, CreditCard creditCard, String moduleName,
        String userName, boolean isRecurring) throws PaymentGatewayUserException, PaymentGatewaySystemException;
    
    public PayPalDTO doAuthorize(Site site, double amtToCharge, CreditCard creditCard, String moduleName,
            String userName, boolean isRecurring) throws PaymentGatewayUserException, PaymentGatewaySystemException;
    
    public PayPalDTO doSale(Site site, double amtToCharge, BankAccount bankAccount, String moduleName,
            String userName, boolean isRecurring) throws PaymentGatewayUserException, PaymentGatewaySystemException;
        
    public PayPalDTO doAuthorize(Site site, double amtToCharge, BankAccount bankAccount, String moduleName,
            String userName, boolean isRecurring) throws PaymentGatewayUserException, PaymentGatewaySystemException;

    /** This Method Is Used For Charging OTC Transactions. Depending On AmtToCharge It First Determines Whether Micro Or
     * Normal Merchant Is Used. If PaymentGatewaySystemException Occurs, ErrorCode Is Saved In Database. For All Other
     * Exceptions, Transaction Is Rolled Back.
     * The Transactional Attribute Should Be There If Not, The System Will Not Insert Data In The The Error Code Table.
     * @param site This Variable Provides Site Specific Paypal Credentials, And Is Useful Is Selecting Micro Or Normal
     * 			   Merchant.
     * @param amtToCharge Amount To Be Charged.
     * @param swipeCardDetails Credit Card Information (Track 1/ Track 2).
     * @param moduleName Method Name For Logging Purpose.
     * @return
     */
    
    public PayPalDTO doCapture(Site site, String authorizationTxReferenceNumber, Double captureTxAmount
    		,String moduleName, String modifiedBy) throws PaymentGatewayUserException, PaymentGatewaySystemException; 
    		
    public PayPalDTO doSale(Site site, double amtToCharge, String swipeCardDetails, String moduleName) throws
		PaymentGatewayUserException, PaymentGatewaySystemException;

    /** This Method Is Used To Perform Reference Credit. It Refunds The Amount Charged By originalTxRefNumber.
     * The Transactional attribute Should Be There, If Not The System Will Not Insert Data In The The Error Code Table.
     * @param site This Variable Provides Site Specific Paypal Credentials, And Is Useful Is Selecting Micro Or Normal
     * 			   Merchant.
     * @param originalTxRefNumber Original Transaction Reference Number.
     * @param refCreditType WEB/OTC/Reccuring.
     * @param moduleName Method Name For Logging Purpose.
     * @param userName Email Id Of User Who is Performing the ReferenceCredit Operation.
     * @return
     * @throws PaymentGatewayUserException
     * @throws PaymentGatewaySystemException
     */
    public PayPalDTO doReferenceCredit(Site site, String originalTxRefNumber, String refCreditType,
        String moduleName, String userName) throws PaymentGatewayUserException, PaymentGatewaySystemException;

    /** The Method Is Used Do Credit PartialRefundAmount To The Card Which User Used To Do OriginalTxNumber. This Method
     * Takes OriginalTxNumber Also And It Ensures That Amount To Be Credited Is Less Than The Amount Charged Previously.
     * @param site This Variable Provides Site Specific Paypal Credentials, And Is Useful Is Selecting Micro Or Normal
     * 			   Merchant.
     * @param originalTxRefNumber Original Transaction Reference Number.
     * @param partialRefundAmount Amount To Be Refunded.
     * @param refCreditType WEB/OTC/Reccuring.
     * @param moduleName Method Name For Logging Purpose.
     * @param userName Email Id Of User Who is Performing the PartialReferenceCredit Operation.
     * @return
     * @throws PaymentGatewayUserException
     * @throws PaymentGatewaySystemException
     */
    public PayPalDTO doPartialReferenceCredit(Site site, String originalTxRefNumber, double partialRefundAmount,
        String refCreditType, String moduleName, String userName) throws PaymentGatewayUserException,
            PaymentGatewaySystemException;

}