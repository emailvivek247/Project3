package com.fdt.paymentgateway.service;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import paypal.payflow.BillTo;
import paypal.payflow.CardTender;
import paypal.payflow.CreditTransaction;
import paypal.payflow.Currency;
import paypal.payflow.Invoice;
import paypal.payflow.PayflowConnectionData;
import paypal.payflow.PayflowConstants;
import paypal.payflow.PayflowUtility;
import paypal.payflow.RecurringResponse;
import paypal.payflow.Response;
import paypal.payflow.SDKProperties;
import paypal.payflow.SaleTransaction;
import paypal.payflow.SwipeCard;
import paypal.payflow.TransactionResponse;
import paypal.payflow.UserInfo;

import com.fdt.common.entity.ErrorCode;
import com.fdt.ecom.dao.EComDAO;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.Merchant;
import com.fdt.ecom.entity.Site;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserExceptionCodes;
import com.fdt.recurtx.dto.UserAccountDetailDTO;
import com.fdt.subscriptions.dto.AccessDetailDTO;

public class PayPalPaymentGatewayServiceImpl implements PaymentGatewayService {

    private final static Logger logger = LoggerFactory.getLogger(PayPalPaymentGatewayServiceImpl.class);

    private final static String ERR_MSG_NULL_PAYPAL_RESPONSE = "PayPal Response is Null";

    private final static String ERR_MSG_NULL_PAYPAL_TX_RESPONSE = "Transaction Response From PayPal is Null";

    @Autowired
    private EComDAO eComDAO;

    @Value("${ecommerce.paypal.paypalhostaddress}")
    private String payPalHostAddress = null;

    @Value("${ecommerce.paypal.paypalhostport}")
    private String payPalHostPort = null;

    @Value("${ecommerce.paypal.paypaltimeout}")
    private String payPalTimeout = null;

    @Value("${ecommerce.paypal.paypallogfilename}")
    private String payPalLogFileName = null;

    @Value("${ecommerce.paypal.paypallogfilesize}")
    private String payPalLogFileSize = null;

    @PostConstruct
    public void initialize() {
       SDKProperties.setHostAddress(payPalHostAddress);
       SDKProperties.setHostPort(new Integer(payPalHostPort).intValue());
       SDKProperties.setTimeOut(new Integer(payPalTimeout).intValue());
       SDKProperties.setLogFileName(payPalLogFileName);
       SDKProperties.setLoggingLevel(PayflowConstants.SEVERITY_WARN);
       SDKProperties.setMaxLogFileSize(Integer.parseInt(payPalLogFileSize));
       SDKProperties.setStackTraceOn(true);
    }

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
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = PaymentGatewaySystemException.class)
    public PayPalDTO doSale(Site site, double amtToCharge, CreditCard creditCard, String moduleName,
            String userName, boolean isRecurring) throws PaymentGatewayUserException,
        PaymentGatewaySystemException {
        UserInfo userInfo = null;
        PayPalDTO payPalDTO = null;
        String type = null;
        Merchant merchant = null;

        Assert.state(amtToCharge > 0.0, "Amount To be chaged must be greater than zero!");

        if(isRecurring) {
        	merchant = site.getMerchant();
        	userInfo = this.getMerchantInfo(merchant);
        	type = "RECURRING";
        } else {
	        /** Check Whether it is Micro Transaction or a Normal Transaction **/
        	type = "WEB";
	        if (amtToCharge < site.getCardUsageFee().getMicroTxFeeCutOff() && site.isEnableMicroTxWeb()) {
	        	merchant = site.getMicroMerchant();
	            userInfo = this.getMerchantInfo(merchant);
	        } else {
	        	merchant = site.getMerchant();
	            userInfo = this.getMerchantInfo(site.getMerchant());
	        }
        }

        Invoice invoice = this.getInvoiceForSale(site, amtToCharge, type, creditCard, userName);

        // Create a new Tender data object.
        CardTender card = this.getCardTender(creditCard);

        // Create a new Sale Transaction.
        SaleTransaction trans = new SaleTransaction(userInfo, new PayflowConnectionData(), invoice, card,
                PayflowUtility.getRequestId());
        // Submit the Transaction
        Response paypalResponse = trans.submitTransaction();

        this.handlePayPalResponse(paypalResponse, moduleName, "doSale", null, null);

        TransactionResponse txResponse = paypalResponse.getTransactionResponse();

        /** Handle Any PayPal Transaction Response Errors **/
        this.handlePayPalTxResponse(txResponse, moduleName, "doSale", null, null);

        if (txResponse.getResult() == 0) {
            /** The Transaction is Success **/
            payPalDTO = new PayPalDTO();
            payPalDTO.setTxRefNum(txResponse.getPnref());
            payPalDTO.setAuthCode(txResponse.getAuthCode());
            payPalDTO.setMerchantId(merchant.getId());
        } else {
            if (PaymentGatewayUserExceptionCodes.isEqual(String.valueOf(txResponse.getResult()))) {
            	this.logPayPalUserException(paypalResponse, moduleName, "doSale", userName, null);
            } else {
                this.logPayPalSystemException(paypalResponse, moduleName, "doSale", userName, null);
            }
        }
        return payPalDTO;
    }

    /** This Method Is Used For Charging OTC Transactions. Depending On AmtToCharge It First Determines Whether Micro Or
     * Normal Merchant Is Used. If PaymentGatewaySystemException Occurs, ErrorCode Is Saved In Database. For All Other
     * Exceptions, Transaction Is Rolled Back.
     * The Transactional Attribute Should Be There If Not, The System Will Not Insert Data In The The Error Code Table.
     * @param site This Variable Provides Site Specific Paypal Credentials, And Is Useful Is Selecting Micro Or Normal
     * 			   Merchant.
     * @param amtToCharge Amount To Be Charged.
     * @param swipeCardDetails Credit Card Information (Track 1/ Track 2).
     * @param moduleName Method Name For Logging Purpose.bvnkljsdam;f k b78oas fp'd 4
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = PaymentGatewaySystemException.class)
    public PayPalDTO doSale(Site site, double amtToCharge, String swipeCardDetails, String moduleName) {
        String errorCode = null;
        String errorDesc = null;
        UserInfo userInfo = null;
        PayPalDTO paymentTxResponseDTO = new PayPalDTO();
        try {
            /** Check Whether it is Micro Transaction or a Normal Transaction **/
            if (amtToCharge < site.getCardUsageFee().getMicroTxFeeCutOff() && site.isEnableMicroTxOTC()) {
                userInfo = this.getMerchantInfo(site.getMicroMerchant());
            } else {
                userInfo = this.getMerchantInfo(site.getMerchant());
            }
            Invoice invoice = this.getInvoiceForSale(site, amtToCharge, "OTC");

            /** The parameter data for the SwipeCard object is usually obtained with a card reader.
            NOTE: The SWIPE parameter is not supported on accounts where PayPal is the Processor **/
            SwipeCard swipe = new SwipeCard(swipeCardDetails);
            // Create a new Tender - Swipe Tender data object.
            CardTender card = new CardTender(swipe);

            // Create a new Sale Transaction.
            SaleTransaction trans = new SaleTransaction(userInfo, new PayflowConnectionData(), invoice, card,
                    PayflowUtility.getRequestId());
            // Submit the Transaction
            Response paypalResponse = trans.submitTransaction();

            this.handlePayPalResponse(paypalResponse, moduleName, "doSaleOTC", null, null);

            TransactionResponse txResponse = paypalResponse.getTransactionResponse();

            /** Handle Any PayPal Transaction Response Errors **/
            this.handlePayPalTxResponse(txResponse, moduleName, "doSaleOTC", null, null);

            if (txResponse.getResult() == 0) {
                /** The Transaction is Success **/
                paymentTxResponseDTO.setTxRefNum(txResponse.getPnref());
                paymentTxResponseDTO.setAuthCode(txResponse.getAuthCode());
            } else if (PaymentGatewayUserExceptionCodes.isEqual(String.valueOf(txResponse.getResult()))) {
                errorCode = String.valueOf(txResponse.getResult());
                errorDesc = txResponse.getRespMsg();
                logger.error("Error in doSale for OTC - ResultCode: " + txResponse.getResult() + " , Error Description: " + errorDesc);
            } else {
                errorCode = String.valueOf(txResponse.getResult());
                errorDesc = txResponse.getRespMsg();
                logger.error("Error in doSale for OTC - ResultCode: " + txResponse.getResult() + " , Error Description: " + errorDesc);
            }
        } catch (PaymentGatewaySystemException payPalSystemException) {
            errorCode = "-1";
            errorDesc = "PayPal General Error When Doing a Sale OTC";
        }
        paymentTxResponseDTO.setErrorCode(errorCode);
        paymentTxResponseDTO.setErrorDesc(errorDesc);
        return paymentTxResponseDTO;
    }

    /** The method is used do credit amount to card supplied. This method takes originalTxNumber also and it ensures
     * that amount to be credited is less than the amount charged previously.
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
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = PaymentGatewaySystemException.class)
    public String doCredit(CreditCard creditCard, Merchant merchantInfo, Double amount, String originalTxNumber,
            String moduleName, String userName) throws PaymentGatewayUserException, PaymentGatewaySystemException {
        PayflowConnectionData connection = new PayflowConnectionData();
        String txRefNumber = null;
        UserInfo user = this.getMerchantInfo(merchantInfo);
        Invoice invoice = new Invoice();
        BillTo bill = new BillTo();
        CardTender card = this.getCardTender(creditCard);
        bill.setStreet(creditCard.getAddressLine1().concat(" ").concat(creditCard.getAddressLine2() == null
                ? " " : creditCard.getAddressLine2()));
        bill.setCity(creditCard.getCity());
        bill.setState(creditCard.getState());
        bill.setZip(creditCard.getZip());
        bill.setBillToCountry("US");
        bill.setPhoneNum(creditCard.getPhone().toString());
        invoice.setBillTo(bill);
        Currency amt = new Currency(amount, "USD");
        invoice.setAmt(amt);
        CreditTransaction trans = new CreditTransaction(originalTxNumber, user, connection, invoice, card,
                PayflowUtility.getRequestId());
        Response payPalResponse = trans.submitTransaction();
        this.handlePayPalResponse(payPalResponse, moduleName, "doCredit", null, null);
        TransactionResponse trxnResponse = payPalResponse.getTransactionResponse();

        /** Handle Any PayPal Transaction Response Errors **/
        this.handlePayPalTxResponse(trxnResponse, moduleName, "doCredit", null, null);

        if (trxnResponse.getResult() == 0) {
            /** The Transaction is Success **/
            txRefNumber = trxnResponse.getPnref();
        } else if (PaymentGatewayUserExceptionCodes.isEqual(String.valueOf(trxnResponse.getResult()))) {
            this.logPayPalUserException(payPalResponse, moduleName, "doCredit", userName, null);
        } else {
            this.logPayPalSystemException(payPalResponse, moduleName, "doCredit", userName, null);
        }
        return txRefNumber;
    }

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
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = PaymentGatewaySystemException.class)
    public String doCredit(UserAccountDetailDTO existingUserAccountDTO, AccessDetailDTO newAccessDTO, Double amount,
            String moduleName, String userName) throws PaymentGatewayUserException, PaymentGatewaySystemException {
        PayflowConnectionData connection = new PayflowConnectionData();
        String txRefNumber = null;
        Site site = existingUserAccountDTO.getSite();
        CreditCard creditCard = existingUserAccountDTO.getUserAccount().getCreditCard();
        UserInfo user = this.getMerchantInfo(site.getMerchant());
        Invoice invoice = new Invoice();
        BillTo bill = new BillTo();
        CardTender card = this.getCardTender(creditCard);
        bill.setStreet(creditCard.getAddressLine1().concat(" ").concat(creditCard.getAddressLine2() == null
                ? " " : creditCard.getAddressLine2()));
        bill.setCity(creditCard.getCity());
        bill.setState(creditCard.getState());
        bill.setZip(creditCard.getZip());
        bill.setBillToCountry("US");
        bill.setPhoneNum(creditCard.getPhone().toString());
        invoice.setBillTo(bill);
        invoice.setComment2("CHANGED FROM " + existingUserAccountDTO.getSite().getAccess().get(0).getDescription()
                + " SUBSCRIPTION TO "    + newAccessDTO.getSite().getAccess().get(0).getDescription()+ " SUBSCRIPTION.");
        Currency amt = new Currency(amount, "USD");
        invoice.setAmt(amt);
        CreditTransaction trans = new CreditTransaction(user, connection, invoice, card,
                PayflowUtility.getRequestId());
        Response payPalResponse = trans.submitTransaction();
        this.handlePayPalResponse(payPalResponse, moduleName, "doCredit", null, null);
        TransactionResponse trxnResponse = payPalResponse.getTransactionResponse();

        /** Handle Any PayPal Transaction Response Errors **/
        this.handlePayPalTxResponse(trxnResponse, moduleName, "doCredit", null, null);

        if (trxnResponse.getResult() == 0) {
            /** The Transaction is Success **/
            txRefNumber = trxnResponse.getPnref();
        } else if (PaymentGatewayUserExceptionCodes.isEqual(String.valueOf(trxnResponse.getResult()))) {
        	this.logPayPalUserException(payPalResponse, moduleName, "doCredit",userName, null);
        } else {
            this.logPayPalSystemException(payPalResponse, moduleName, "doCredit",userName, null);
        }
        return txRefNumber;
    }

    /** This Method Is Used To Perform Reference Credit. It Refunds The Amount Charged By originalTxRefNumber.
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
    /* The Transactional attribute should be There if not the System will not insert data in the the Error Code Table **/
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = PaymentGatewaySystemException.class)
    public PayPalDTO doReferenceCredit(Site site, String originalTxRefNumber, String refCreditType,
            String moduleName, String userName) throws PaymentGatewayUserException, PaymentGatewaySystemException {
    	Assert.hasLength(originalTxRefNumber, "Original Transaction Number Cannot Be Null");
        PayflowConnectionData connection = new PayflowConnectionData();
        PayPalDTO referenceCreditDTO =  null;
        UserInfo user = this.getMerchantInfo(site.getMerchant());
        Invoice invoice = new Invoice();
        invoice.setComment1(site.getName() + " " + refCreditType);
        invoice.setComment2(originalTxRefNumber);
        // Create a new Credit Transaction.
        CreditTransaction trans = new CreditTransaction(originalTxRefNumber, user, connection, invoice,
                PayflowUtility.getRequestId());
        Response payPalResponse = trans.submitTransaction();
        this.handlePayPalResponse(payPalResponse, moduleName, "doReferenceCredit", null, null);
        TransactionResponse trxnResponse = payPalResponse.getTransactionResponse();

        /** Handle Any PayPal Transaction Response Errors **/
        this.handlePayPalTxResponse(trxnResponse, moduleName, "doReferenceCredit", null, null);

        if (trxnResponse.getResult() == 0) {
            /** The Transaction is Success **/
            referenceCreditDTO =  new PayPalDTO();
            referenceCreditDTO.setTxRefNum(trxnResponse.getPnref());
            referenceCreditDTO.setAuthCode(trxnResponse.getAuthCode());
        } else if (PaymentGatewayUserExceptionCodes.isEqual(String.valueOf(trxnResponse.getResult()))) {
        	this.logPayPalUserException(payPalResponse, moduleName, "doReferenceCredit", userName, null);
        } else {
            this.logPayPalSystemException(payPalResponse, moduleName, "doReferenceCredit", userName, null);
        }

        return referenceCreditDTO;
    }

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
    /* The Transactional attribute should be There if not the System will not insert data in the the Error Code Table **/
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = PaymentGatewaySystemException.class)
    public PayPalDTO doPartialReferenceCredit(Site site, String originalTxRefNumber, double partialRefundAmount,
            String refCreditType, String moduleName, String userName)
                throws PaymentGatewayUserException, PaymentGatewaySystemException {
        PayflowConnectionData connection = new PayflowConnectionData();
        PayPalDTO referenceCreditDTO =  null;
        UserInfo user = this.getMerchantInfo(site.getMerchant());
        Invoice invoice = new Invoice();
        invoice.setComment1(site.getName() + " " + refCreditType);
        invoice.setComment2(originalTxRefNumber);
        Currency amt = new Currency(partialRefundAmount, "USD");
        invoice.setAmt(amt);
        // Create a new Credit Transaction.
        CreditTransaction trans = new CreditTransaction(originalTxRefNumber, user, connection, invoice,
                PayflowUtility.getRequestId());
        Response payPalResponse = trans.submitTransaction();
        this.handlePayPalResponse(payPalResponse, moduleName, "doPartialReferenceCredit", null, null);
        TransactionResponse trxnResponse = payPalResponse.getTransactionResponse();

        /** Handle Any PayPal Transaction Response Errors **/
        this.handlePayPalTxResponse(trxnResponse, moduleName, "doPartialReferenceCredit", null, null);

        if (trxnResponse.getResult() == 0) {
            /** The Transaction is Success **/
            referenceCreditDTO =  new PayPalDTO();
            referenceCreditDTO.setTxRefNum(trxnResponse.getPnref());
            referenceCreditDTO.setAuthCode(trxnResponse.getAuthCode());
        } else if (PaymentGatewayUserExceptionCodes.isEqual(String.valueOf(trxnResponse.getResult()))) {
        	this.logPayPalUserException(payPalResponse, moduleName, "doPartialReferenceCredit", userName, null);
        } else {
            this.logPayPalSystemException(payPalResponse, moduleName, "doPartialReferenceCredit", userName, null);
        }
        return referenceCreditDTO;
    }

    private UserInfo getMerchantInfo(Merchant merchantInfo) throws PaymentGatewaySystemException {
        UserInfo userInfo = null;
        if (merchantInfo == null) {
            this.logPayPalSystemException("MERCHANT_INFO_NULL", "Merchant Information Not Found", "PAYPAL",
                    "getMerchantInfo", null, null);
        } else if (merchantInfo.getUserName() == null ||
                    merchantInfo.getVendorName() == null ||
                    merchantInfo.getPartner() == null ||
                    merchantInfo.getPassword() == null) {
            this.logPayPalSystemException("MERCHANT_INFO_NULL", "Merchant Information Not Found", "PAYPAL",
                    "getMerchantInfo", null, null);
        } else {
            userInfo = new UserInfo(merchantInfo.getUserName(), merchantInfo.getVendorName(), merchantInfo.getPartner(),
                    merchantInfo.getPassword());
        }
        return userInfo;
    }

    private CardTender getCardTender(CreditCard creditCard) {
        String expiryMonth = String.valueOf(creditCard.getExpiryMonth());
        String zero = "0";
        if(creditCard.getExpiryMonth() < 10 ){
            expiryMonth = zero.concat(expiryMonth);
        }
        String expiryYear = (String.valueOf(creditCard.getExpiryYear())).substring(2, 4);
        paypal.payflow.CreditCard payPalCreditCard = new paypal.payflow.CreditCard(String.valueOf(creditCard.getNumber()),
                expiryMonth.concat(expiryYear));
        payPalCreditCard.setName(creditCard.getName());
        if (creditCard.getSecurityCode() != null) {
            payPalCreditCard.setCvv2(String.valueOf(creditCard.getSecurityCode()));
        }
        // Create a new Tender - Card Tender data object.
        CardTender card = new CardTender(payPalCreditCard);
        return card;
    }

    private Invoice getInvoiceForSale(Site site, Double amtToCharge, String type, CreditCard creditCard, String userName) {
        Invoice inv = new Invoice();
        BillTo bill = new BillTo();
        bill.setStreet(creditCard.getAddressLine1().concat(" ").concat(creditCard.getAddressLine2() == null
                ? " " : creditCard.getAddressLine2()));
        bill.setCity(creditCard.getCity());
        bill.setState(creditCard.getState());
        bill.setZip(creditCard.getZip());
        bill.setBillToCountry("US");
        bill.setPhoneNum(creditCard.getPhone().toString());
        Currency amt = new Currency(new Double(amtToCharge), "USD");
        inv.setAmt(amt);
        inv.setBillTo(bill);
        inv.setComment1(site.getName().concat(type));
        return inv;
    }

    private Invoice getInvoiceForSale(Site site, Double amtToCharge, String type) {
        Invoice inv = new Invoice();
        Currency amt = new Currency(new Double(amtToCharge), "USD");
        inv.setAmt(amt);
        inv.setComment1(site.getName().concat(type));
        return inv;
    }


    /**
     * This Method will check whether the PayPal Response is Null if Yes it will throw a PaymentGatewaySystemException
     *
     * @param response PayPal Response.
     * @param moduleName Payment gateway Module Name
     * @param functionName PayPal function name.
     * @param userName the user name.
     * @param payPalProfileId PayPal Profile Id.
     * @throws PaymentGatewaySystemException if the PayPal Response is null.
     */
    private void handlePayPalResponse(Response response, String moduleName, String functionName, String userName,
            String payPalProfileId) throws PaymentGatewaySystemException {
        if (response == null) {
            ErrorCode errorCode = new ErrorCode();
            errorCode.setCode("NULL_PAYPAL_RESPONSE");
            errorCode.setDescription(ERR_MSG_NULL_PAYPAL_RESPONSE);
            if (payPalProfileId != null) {
                errorCode.setDescription(errorCode.getDescription() + " For Profile Id : " + payPalProfileId);
            }
            errorCode.setModuleName(moduleName);
            errorCode.setFunctionName(functionName);
            errorCode.setUserName(userName);
            errorCode.setModifiedBy("SYSTEM");
            errorCode.setCreatedBy("SYSTEM");
            errorCode.setCreatedDate(new Date());
            errorCode.setModifiedDate(new Date());
            this.eComDAO.saveErrorCode(errorCode);
            PaymentGatewaySystemException payPalSystemException = new PaymentGatewaySystemException("PayPal Response is Null");
            throw payPalSystemException;
        }
    }

    /**
     * This Method will check whether the PayPal Response is Null. If Yes it will throw a PaymentGatewaySystemException
     *
     * @param txResponse PayPal Transaction Response.
     * @param moduleName payment gateway Module Name
     * @param functionName PayPal function name.
     * @param userName the user name.
     * @param payPalProfileId PayPal Profile Id.
     * @throws PaymentGatewaySystemException if  PayPal Transaction Response is null.
     */
    private void handlePayPalTxResponse(TransactionResponse txResponse, String moduleName, String functionName,
            String userName, String payPalProfileId) throws PaymentGatewaySystemException {
        if (txResponse == null) {
            ErrorCode errorCode = new ErrorCode();
            errorCode.setCode("NULL_PAYPAL_TX_RESPONSE");
            errorCode.setDescription(ERR_MSG_NULL_PAYPAL_TX_RESPONSE);
            if (payPalProfileId != null) {
                errorCode.setDescription(errorCode.getDescription() + " For Profile Id : " + payPalProfileId);
            }
            errorCode.setFunctionName(moduleName);
            errorCode.setModuleName(moduleName);
            errorCode.setUserName(userName);
            errorCode.setModifiedBy("SYSTEM");
            errorCode.setCreatedBy("SYSTEM");
            errorCode.setCreatedDate(new Date());
            errorCode.setModifiedDate(new Date());
            this.eComDAO.saveErrorCode(errorCode);
            PaymentGatewaySystemException payPalSystemException = new PaymentGatewaySystemException("PayPal Response is Null");
            throw payPalSystemException;
        }
    }

    /**
     * This Method logs PayPal System Exception. It sends an Email to the ACCEPT Admin when there is a
     * PaymentGatewaySystemException
     *
     * @param payPalResponse PayPal Response
     * @param moduleName The Module that is calling.
     * @param payPalFunctionName The Payment Gateway Function
     * @param userName The User Name
     * @param payPalProfileId The PayPal Profiel Id
     * @throws PaymentGatewaySystemException PayMent Gateway System Exception
     */
    private void logPayPalSystemException(Response payPalResponse, String moduleName, String payPalFunctionName,
            String userName, String payPalProfileId) throws PaymentGatewaySystemException {
        logger.debug("COMING INSIDE logPayPalSystemException FOR USER  {}", userName);
        TransactionResponse trxnResponse = payPalResponse.getTransactionResponse();
        RecurringResponse recurResponse = payPalResponse.getRecurringResponse();
        ErrorCode errorCode = new ErrorCode();
        if(recurResponse != null) {
            if (recurResponse.getTrxResult() != null) {
                errorCode.setCode(recurResponse.getTrxPNRef());
                errorCode.setDescription(recurResponse.getTrxRespMsg());
            } else {
                errorCode.setCode(String.valueOf(trxnResponse.getResult()));
                errorCode.setDescription(trxnResponse.getRespMsg());
            }
        } else {
            errorCode.setCode(String.valueOf(trxnResponse.getResult()));
            errorCode.setDescription(trxnResponse.getRespMsg());
        }
        if (payPalProfileId != null) {
            errorCode.setDescription(errorCode.getDescription() + " For Profile Id : " + payPalProfileId);
        }
        errorCode.setUserName(userName);
        errorCode.setModuleName(moduleName);
        errorCode.setFunctionName(payPalFunctionName);
        errorCode.setModifiedBy("SYSTEM");
        errorCode.setCreatedBy("SYSTEM");
        errorCode.setCreatedDate(new Date());
        errorCode.setModifiedDate(new Date());
        this.eComDAO.saveErrorCode(errorCode);
        PaymentGatewaySystemException payPalSystemException = new PaymentGatewaySystemException(errorCode.getDescription());
        payPalSystemException.setErrorCode(errorCode.getCode());
        payPalSystemException.setDescription(errorCode.getDescription());
        Object[] paramArray = {userName, errorCode.getCode(), moduleName, payPalFunctionName, errorCode.getDescription()};
        logger.error(NOTIFY_ADMIN, "PayPal Error For User ==>{}, ErrorCode ==>{}, ModuleName ==>{}, " +
            "Payment Gateway Function Name ==>{}, Error Description ==>{} ", paramArray);
        throw payPalSystemException;
    }

    /**
     * This Method logs PayPal System Exception. It sends an Email to the ACCEPT Admin when there is a
     * PaymentGatewaySystemException
     *
     * @param payPalResponse PayPal Response
     * @param moduleName The Module that is calling.
     * @param payPalFunctionName The Payment Gateway Function
     * @param userName The User Name
     * @param payPalProfileId The PayPal Profiel Id
     * @throws PaymentGatewaySystemException PayMent Gateway System Exception
     */
    private void logPayPalUserException(Response payPalResponse, String moduleName, String payPalFunctionName,
            String userName, String payPalProfileId) throws PaymentGatewayUserException {
        logger.debug("COMING INSIDE logPayPalUserException FOR USER  {}", userName);
        TransactionResponse trxnResponse = payPalResponse.getTransactionResponse();
        RecurringResponse recurResponse = payPalResponse.getRecurringResponse();
        ErrorCode errorCode = new ErrorCode();
        if(recurResponse != null) {
            if (recurResponse.getTrxResult() != null) {
                errorCode.setCode(recurResponse.getTrxPNRef());
                errorCode.setDescription(recurResponse.getTrxRespMsg());
            } else {
                errorCode.setCode(String.valueOf(trxnResponse.getResult()));
                errorCode.setDescription(trxnResponse.getRespMsg());
            }
        } else {
            errorCode.setCode(String.valueOf(trxnResponse.getResult()));
            errorCode.setDescription(trxnResponse.getRespMsg());
        }
        if (payPalProfileId != null) {
            errorCode.setDescription(errorCode.getDescription() + " For Profile Id : " + payPalProfileId);
        }
        errorCode.setUserName(userName);
        errorCode.setModuleName(moduleName);
        errorCode.setFunctionName(payPalFunctionName);
        errorCode.setModifiedBy("SYSTEM");
        errorCode.setCreatedBy("SYSTEM");
        errorCode.setCreatedDate(new Date());
        errorCode.setModifiedDate(new Date());
        errorCode.setUserException(true);
        this.eComDAO.saveErrorCode(errorCode);
        PaymentGatewayUserException payPalUserException = new PaymentGatewayUserException(errorCode.getDescription());
        payPalUserException.setErrorCode(errorCode.getCode());
        payPalUserException.setDescription(errorCode.getDescription());
        throw payPalUserException;
    }

    /**
     * This Method logs PayPal System Exception. It sends an Email to the ACCEPT Admin when there is a
     * PaymentGatewaySystemException.
     *
     * @param payPalResponse PayPal Response
     * @param moduleName The Module that is calling.
     * @param payPalFunctionName The Payment Gateway Function
     * @param userName The User Name
     * @param payPalProfileId The PayPal Profile Id
     * @throws PaymentGatewaySystemException PayMent Gateway System Exception
     */
    private void logPayPalSystemException(String errCode, String errDesc, String moduleName, String payPalFunctionName,
            String userName, String payPalProfileId) throws PaymentGatewaySystemException {
        ErrorCode errorCode = new ErrorCode();
        errorCode.setCode(errCode);
        errorCode.setDescription(errDesc);
        if (payPalProfileId != null) {
            errorCode.setDescription(errorCode.getDescription() + " For Profile Id : " + payPalProfileId);
        }
        errorCode.setModuleName(moduleName);
        errorCode.setFunctionName(payPalFunctionName);
        errorCode.setUserName(userName);
        errorCode.setModifiedBy("SYSTEM");
        errorCode.setCreatedBy("SYSTEM");
        errorCode.setCreatedDate(new Date());
        errorCode.setModifiedDate(new Date());
        errorCode.setUserException(false);
        this.eComDAO.saveErrorCode(errorCode);
        PaymentGatewaySystemException payPalSystemException = new PaymentGatewaySystemException(errDesc);
        payPalSystemException.setErrorCode(errCode);
        payPalSystemException.setDescription(errDesc);
        Object[] paramArray = {userName, errorCode.getCode(), moduleName, payPalFunctionName, errorCode.getDescription()};
        logger.error(NOTIFY_ADMIN, "PayPal Error For User ==>{}, ErrorCode ==>{}, ModuleName ==>{}, " +
            "Payment Gateway Function Name ==>{}, Error Description ==>{} ", paramArray);
        throw payPalSystemException;
    }
}