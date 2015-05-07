package com.fdt.payasugotx.service;

import static com.fdt.common.SystemConstants.CERTIFIED_DOCUMENT_NUMBER_LENGTH;
import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;
import static com.fdt.common.SystemConstants.REFERENCE_NUMBER_LENGTH;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.util.SystemUtil;
import com.fdt.common.util.spring.SpringUtil;
import com.fdt.ecom.dao.EComDAO;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.Location;
import com.fdt.ecom.entity.Merchant;
import com.fdt.ecom.entity.ShoppingCartItem;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.SiteConfiguration;
import com.fdt.ecom.entity.enums.CardType;
import com.fdt.ecom.entity.enums.SettlementStatusType;
import com.fdt.ecom.entity.enums.TransactionType;
import com.fdt.ecom.service.EComService;
import com.fdt.ecom.util.CreditCardUtil;
import com.fdt.email.EmailProducer;
import com.fdt.payasugotx.dao.PayAsUGoTxDAO;
import com.fdt.payasugotx.dto.PayAsUSubDTO;
import com.fdt.payasugotx.entity.PayAsUGoTx;
import com.fdt.payasugotx.entity.PayAsUGoTxItem;
import com.fdt.payasugotx.entity.PayAsUGoTxView;
import com.fdt.payasugotx.service.validator.AbstractPayAsUGoFeeCalculator;
import com.fdt.payasugotx.service.validator.PayAsUGoTxValidator;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.paymentgateway.service.PaymentGatewayService;
import com.fdt.security.dao.UserDAO;
import com.fdt.security.entity.Access;
import com.fdt.security.entity.User;
import com.fdt.security.entity.UserAccess;
import com.fdt.subscriptions.dao.SubDAO;
import com.fdt.subscriptions.dto.AccessDetailDTO;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

@Service("PayAsUGoTxService")
public class PayAsUGoTxServiceImpl implements PayAsUGoTxService {

    private final static Logger logger = LoggerFactory.getLogger(PayAsUGoTxServiceImpl.class);

    @Autowired
    private EComService eComService;

    @Value("${ecommerce.serverurl}")
    private String ecomServerURL = null;

    @Autowired
    @Qualifier("paymentGateway")
    private PaymentGatewayService paymentGateway = null;

    @Autowired
    @Qualifier("serverMessageSource")
    private MessageSource messages = null;

    @Autowired
    private PayAsUGoTxDAO payAsUGoSubDAO = null;

    @Autowired
    private UserDAO userDAO = null;

    @Autowired
    private EComDAO eComDAO = null;

    @Autowired
    private SubDAO subDAO = null;

    @Autowired
    private EmailProducer emailProducer = null;

    @Autowired
    private SpringUtil springUtil = null;

    @Autowired
    PayAsUGoTxValidator payAsUGoTxValidator = null;

    @Value("${tx.ValidityPeriod}")
    /* Default Value is 60 Days */
    private String txValidityPeriod = "60";

    /**
     * This Method Is Called From Web Service Itself And Is Used To Pay For List Of Images In The Shopping Cart.
     * This Method Charges Shopping Cart Items Based On Site (Access Name). Service Fee Calculation Is Done In
     * DefaultPayAsUGoFeeCaluculator Based On Type Of Non-Recurring Subscription (PPF, PDF, PPV, FR). All The PayAsUGo Transaction
     * Items Are Saved Along With (parent) PayAsUGotransaction.
     *
     * @param userName EmailId of the user logged in.
     * @param payAsUGoTransactionDTO Contains List Of Shoppingcartitems, Creditcard.
     * @return The List Of PayAsUGotransactions.
     * @throws PaymentGatewayUserException
     * @throws PaymentGatewaySystemException
     * @throws SDLBusinessException
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public List<PayAsUGoTx> doSalePayAsUGo(String userName, PayAsUSubDTO payAsUGoTransactionDTO)
    		throws PaymentGatewayUserException, PaymentGatewaySystemException, SDLBusinessException {
        Assert.hasLength(userName, "User Name Cannot be Null");
        Assert.notNull(payAsUGoTransactionDTO, "payAsUGoTransactionDTO Cannot be Null");
        Assert.notNull(payAsUGoTransactionDTO.getShoppingCartItemList(), "Shopping Cart Cannot be Null");
        Assert.notEmpty(payAsUGoTransactionDTO.getShoppingCartItemList(), "Shopping Cart Cannot be Empty");
        PayPalDTO paymentTxResponseDTO = null;
        List<PayAsUGoTx> payAsUGoTransactions =  new LinkedList<PayAsUGoTx>();
        List<String> txRefNumbers =  new LinkedList<String>();
        Site site = null;
        boolean isException = false;
        double totalTxAmount   = 0.0d;
        double totalBaseAmount = 0.0d;
        double totalServiceFee = 0.0d;
        boolean isSaveCreditCard = true;
        Merchant merchant = null;
        /** Calculate the Fee, for the Items in the Shopping Cart. **/


        List<ShoppingCartItem> feeCalculatedShoppingCart = this.doSalePayAsUGoInfo(userName,
        		payAsUGoTransactionDTO.getShoppingCartItemList());


   		// Validate if item is already purchased by an user.
        payAsUGoTxValidator.validateFirmDocumentAlreadyPurchased(payAsUGoTransactionDTO.getShoppingCartItemList());

        // Validate credit card for firm/nonfirm level accesses
        payAsUGoTxValidator.validateCreditCardWithAccess(payAsUGoTransactionDTO.getShoppingCartItemList());

        /** Separate the Shopping Cart Based on ACCESS Name. The Shopping Cart can have items from Different Site.
         *  When We charge the customer, we need to charge for each ACCESS
         */

        User user = this.userDAO.getUserDetails(userName, null);

        // Validate User Subscription is Active
        payAsUGoTxValidator.validateForActiveSubscription(payAsUGoTransactionDTO.getShoppingCartItemList(), user.getAccess());

        Multimap<String, ShoppingCartItem> accessNameShoppingCartItemMap = ArrayListMultimap.create();
        for (ShoppingCartItem shoppingCartItem : feeCalculatedShoppingCart) {
        	accessNameShoppingCartItemMap.put(shoppingCartItem.getAccessName(), shoppingCartItem);
        }

        CreditCard creditCard = payAsUGoTransactionDTO.getCreditCard();
        CardType cardType = null;
        /** If Credit Card is Null Take the Existing Credit Card associated to the user**/
        if(creditCard == null) {
        	if(payAsUGoTransactionDTO.isUseFirmsCreditCard()){
        		creditCard = this.userDAO.getFirmCreditCardDetails(userName);
        	} else {
        		creditCard = this.userDAO.getCreditCardDetails(userName);
        	}
        	// Credit Card could be null for the government subscriptions
        	if(creditCard != null){
	            creditCard.setModifiedDate(new Date());
	            creditCard.setModifiedBy(userName);
	            payAsUGoTransactionDTO.setCreditCard(creditCard);
	            cardType = CreditCardUtil.getCardType(creditCard.getNumber());
        	}
        	isSaveCreditCard = false;
        } else {
        	cardType = CreditCardUtil.getCardType(creditCard.getNumber());
        }

        try {
            /** Iterate the accessNameShoppingCartItemMap Map **/
            for (String accessName : accessNameShoppingCartItemMap.keySet()) {
                AccessDetailDTO accessDetailDTO = this.subDAO.getSubDetailsByName(accessName);
                Assert.notNull(accessDetailDTO, "Access Detail Information Cannot Be Null");
                site = accessDetailDTO.getSite();

                List<ShoppingCartItem> shoppingCart = (List<ShoppingCartItem>) accessNameShoppingCartItemMap.get(accessName);

                PayAsUGoTx payAsUGoTransaction =  new PayAsUGoTx();

                // Reset the fee values
                totalTxAmount   = 0.0d;
                totalBaseAmount = 0.0d;
                totalServiceFee = 0.0d;

                if(!this.isTransactionFree(shoppingCart)){
                	// Run paypal transaction if there is an amount to be charged
                    /** Calculate the Fee for Each Access in the Shopping Cart  **/
                    for (ShoppingCartItem shoppingCartItem : shoppingCart) {
                        totalBaseAmount = totalBaseAmount + shoppingCartItem.getBaseAmount();
                        totalServiceFee = totalServiceFee + shoppingCartItem.getServiceFee();
                        totalTxAmount = totalTxAmount + shoppingCartItem.getTotalTxAmount();
                    }
                    /** End Of Calculation of Fee. **/

                    if (totalTxAmount < site.getCardUsageFee().getMicroTxFeeCutOff() && site.isEnableMicroTxWeb()) {
                        merchant = site.getMicroMerchant();
                    } else {
                        merchant = site.getMerchant();
                    }

                    paymentTxResponseDTO = this.paymentGateway.doSale(site, totalTxAmount, creditCard, "doSaleWeb",
                    		userName, false);
                    txRefNumbers.add(paymentTxResponseDTO.getTxRefNum());
                    payAsUGoTransaction.setTxRefNum(paymentTxResponseDTO.getTxRefNum());
                    payAsUGoTransaction.setAuthCode(paymentTxResponseDTO.getAuthCode());
                    payAsUGoTransaction.setAccountName(creditCard.getName());
                    if (cardType == CardType.AMEX) {
                        payAsUGoTransaction.setTxFeePercent(merchant.getTxFeePercentAmex());
                        payAsUGoTransaction.setTxFeeFlat(merchant.getTxFeeFlatAmex());
                    } else {
                        payAsUGoTransaction.setTxFeePercent(merchant.getTxFeePercent());
                        payAsUGoTransaction.setTxFeeFlat(merchant.getTxFeeFlat());
                    }
                    payAsUGoTransaction.setCardType(cardType);
                    payAsUGoTransaction.setCardNumber(creditCard.getNumber());
                } else {
                	// Generate the random reference number
                	String randomString = UUID.randomUUID().toString();
                	randomString = randomString.replaceAll("-", "");
                	payAsUGoTransaction.setTxRefNum(randomString.substring(0, REFERENCE_NUMBER_LENGTH).toUpperCase());
                    payAsUGoTransaction.setTxFeePercent(0.0);
                    payAsUGoTransaction.setTxFeeFlat(0.0);
                    payAsUGoTransaction.setCardNumber("N/A");
                    payAsUGoTransaction.setAccountName("N/A");
                    payAsUGoTransaction.setCardType(CardType.NA);
                    merchant = site.getMicroMerchant();

                }
                //Find Firm Admin Access Id in case this is Firm Access
                Access access = site.getAccess().get(0);
                if(access.isFirmLevelAccess()){
                	for(Access a : user.getAccess()){
                		if(a.getId().equals(access.getId())){
                			UserAccess uAccess = a.getUserAccessList().get(0);
                			if(uAccess.isFirmAccessAdmin()){
                				// If it's a parent then put his id as a firm admn access id
                				payAsUGoTransaction.setFirmAdminUserAccessId(uAccess.getId());
                			} else {
                				payAsUGoTransaction.setFirmAdminUserAccessId(uAccess.getFirmAdminUserAccessId());
                			}
                		}
                	}
                }

                payAsUGoTransaction.setMerchantId(merchant.getId());
                payAsUGoTransaction.setUserId(user.getId());
                payAsUGoTransaction.setBaseAmount(totalBaseAmount);
                payAsUGoTransaction.setServiceFee(totalServiceFee);
                payAsUGoTransaction.setTotalTxAmount(totalTxAmount);
                payAsUGoTransaction.setTransactionType(TransactionType.CHARGE);
                payAsUGoTransaction.setSettlementStatus(SettlementStatusType.UNSETTLED);
                payAsUGoTransaction.setMachineName(payAsUGoTransactionDTO.getTransactionLocation());
                payAsUGoTransaction.setAccessId(site.getAccess().get(0).getId());
                /** Make the Merchant Information as Null **/
                site.addMerchant(null);
                payAsUGoTransaction.setSite(site);
                payAsUGoTransaction.setSiteId(site.getId());
                payAsUGoTransaction.setModifiedBy(userName);
                payAsUGoTransaction.setCreatedBy(userName);
                payAsUGoTransaction.setModifiedDate(new Date());
                payAsUGoTransaction.setCreatedDate(new Date());
                payAsUGoTransaction.setTransactionDate(SystemUtil.changeTimeZone(new Date(),
                        TimeZone.getTimeZone(site.getTimeZone())));


                // Find out the item count & page count from shopping cart to set it in the Pay As U Go Item
                int itemCount = 0;
                int pageCount = 0;
                for (ShoppingCartItem shoppingCartItem : shoppingCart) {
                	itemCount++;
                	pageCount += shoppingCartItem.getPageCount();
                }
                payAsUGoTransaction.setItemCount(itemCount);
                payAsUGoTransaction.setPageCount(pageCount);
                this.payAsUGoSubDAO.savePayAsUGoTransaction(payAsUGoTransaction);

                List<PayAsUGoTxItem> payAsUGoTxItems = new LinkedList<PayAsUGoTxItem>();
                for (ShoppingCartItem shoppingCartItem : shoppingCart) {
                	itemCount++;
                	pageCount += shoppingCartItem.getPageCount();
                    /** Save PayAsUGo Transaction Item **/
                    PayAsUGoTxItem payAsUGoTransactionItem = new PayAsUGoTxItem();
                    payAsUGoTransactionItem.setProductId(shoppingCartItem.getProductId());
                    payAsUGoTransactionItem.setProductType(shoppingCartItem.getProductType());
                    payAsUGoTransactionItem.setPageCount(shoppingCartItem.getPageCount());
                    payAsUGoTransactionItem.setBaseAmount(shoppingCartItem.getBaseAmount());
                    payAsUGoTransactionItem.setServiceFee(shoppingCartItem.getServiceFee());
                    payAsUGoTransactionItem.setTotalTxAmount(shoppingCartItem.getTotalTxAmount());
                    payAsUGoTransactionItem.setPayAsUGoTxId(payAsUGoTransaction.getId());
                    payAsUGoTransactionItem.setModifiedBy(userName);
                    payAsUGoTransactionItem.setCreatedBy(userName);
                    payAsUGoTransactionItem.setModifiedDate(new Date());
                    payAsUGoTransactionItem.setCreatedDate(new Date());
                    payAsUGoTransactionItem.setActive(true);
                    payAsUGoTransactionItem.setDocumentAvailable(true);
                    payAsUGoTransactionItem.setDownloadURL(shoppingCartItem.getDownloadURL());
                    payAsUGoTransactionItem.setUniqueIdentifier(shoppingCartItem.getUniqueIdentifier());
                    payAsUGoTransactionItem.setComments(shoppingCartItem.getComments());
                    payAsUGoTransactionItem.setLocationId(shoppingCartItem.getLocationId());
                    payAsUGoTransactionItem.setBarNumber(shoppingCartItem.getBarNumber());
                    payAsUGoTransaction.setCertified(shoppingCartItem.isCertified());
                	if(shoppingCartItem.isCertified()){
                		String randomString = UUID.randomUUID().toString();
                    	randomString = randomString.replaceAll("-", "");
                    	randomString = randomString.substring(0, CERTIFIED_DOCUMENT_NUMBER_LENGTH).toUpperCase();
                    	String certifiedDocumentNumber =
                    			randomString.substring(0, 3) + "-" + randomString.substring(3, 7) + "-"
                    			+ randomString.substring(7, 11) + "-" + randomString.substring(11, 15);
                		payAsUGoTransactionItem.setCertifiedDocumentNumber(certifiedDocumentNumber.toUpperCase());
                	}
                    payAsUGoTxItems.add(payAsUGoTransactionItem);
                }

                payAsUGoTransaction.setPayAsUGoTxItems(payAsUGoTxItems);
                this.payAsUGoSubDAO.savePayAsUGoTransactionItem(payAsUGoTxItems);
                /** If the User Wanted to Store the Credit Card Save it **/

                if (payAsUGoTransactionDTO.isSaveCreditCard() && isSaveCreditCard) {
                    CreditCard existingCreditCard = this.userDAO.getCreditCardDetails(userName);
                    creditCard.setUserId(user.getId());
                    creditCard.setActive(true);
                    creditCard.setModifiedDate(new Date());
                    creditCard.setModifiedBy(userName);
                    if(existingCreditCard != null) {
                        creditCard.setId(existingCreditCard.getId());
                        creditCard.setCreatedBy(existingCreditCard.getCreatedBy());
                        creditCard.setCreatedDate(existingCreditCard.getCreatedDate());
                    } else {
                        creditCard.setCreatedBy(userName);
                        creditCard.setCreatedDate(new Date());
                    }
                    this.userDAO.saveCreditCard(creditCard);
                }
                payAsUGoTransactions.add(payAsUGoTransaction);
            }

            // set the card number used to charge the amount.
            if(user.getCreditCard() == null){
            	user.setCreditCard(new CreditCard());
            }
            String cardNumber = creditCard.getNumber();
    		if(cardNumber != null && cardNumber.length() > 4) {
    			cardNumber = cardNumber.substring(cardNumber.length() - 4);
    		}
			user.getCreditCard().setNumber(cardNumber);

            /** Send E-Mail Confirmation **/
            Map<String, Object> emailData = new HashMap<String, Object>();
            emailData.put("payAsUGoTransactions", payAsUGoTransactions);
            emailData.put("user", user);
            emailData.put("currentDate", new Date());
            emailData.put("serverUrl", this.ecomServerURL);
            SiteConfiguration siteConfig = this.eComService.getSiteConfiguration(site.getId());
            Assert.notNull(siteConfig, "siteConfig Cannot be Null");
            this.emailProducer.sendMailUsingTemplate(siteConfig.getFromEmailAddress(), userName,
                siteConfig.getWebPaymentConfSubject(), siteConfig.getEmailTemplateFolder()
                    + siteConfig.getWebPaymentConfTemplate(), emailData);
            /** Delete the Shopping Car Items From the DB as the User has Purchased these Documents **/
            this.payAsUGoSubDAO.deleteShoppingCart(payAsUGoTransactionDTO.getShoppingCartItemList());
        } catch (PaymentGatewayUserException paymentGatewayUserException) {
            logger.error("Exception Occured in doSalePayAsUGo", paymentGatewayUserException);
            isException = true;
            throw paymentGatewayUserException;
        } catch (PaymentGatewaySystemException paymentGatewaySystemException) {
            logger.error("Exception Occured in doSalePayAsUGo", paymentGatewaySystemException);
            isException = true;
            throw paymentGatewaySystemException;
        } catch (Exception exception) {
            logger.error("Exception Occured in doSalePayAsUGo", exception);
            isException = true;
            throw new RuntimeException(exception.getMessage());
        } finally {
            /** We Have to do this as PayPal Does not Support Rolling Back Transactions **/
            if (isException) {
                for (String errTxRefNum : txRefNumbers) {
                    try {
                        this.paymentGateway.doReferenceCredit(site, errTxRefNum, "WEB", "doSalePayAsUGo", userName);
                    } catch (Exception exception) {
                        logger.error(NOTIFY_ADMIN, "Error in Refunding the Money Back when there is an Exception in" +
                            "doSalePayAsUGo site {}, errTxRefNum {} userName {}", site, errTxRefNum, userName, exception);
                    }
                }
            }
        }
        return payAsUGoTransactions;
    }


    /** This Method Is Used To Get All The Information Like Transaction Amount, Fees Before The Payment.
     * @param userName EmailId of the User Logged In.
     * @param shoppingCart Contains The List Of Shopping Cart Items.
     * @return The List Of Shopping Cart Items with respective fees set.
     * @throws SDLBusinessException
     */
    @Transactional(readOnly = true)
    public List<ShoppingCartItem> doSalePayAsUGoInfo(String userName, List<ShoppingCartItem> inputShoppingCart)
            throws SDLBusinessException {
        Assert.hasLength(userName, "User Name Cannot be Null");
        Assert.notNull(inputShoppingCart, "Shopping Cart Cannot be Null!");
        Assert.notEmpty(inputShoppingCart, "Shopping Cart is Empty! For user" + userName);
        AbstractPayAsUGoFeeCalculator payAsUGoPageFeeCalculator = (AbstractPayAsUGoFeeCalculator)this.springUtil
            .getBean("defaultPayAsUGoPageFeeCalculator");
        List<ShoppingCartItem> feeCalculatedShoppingCart = payAsUGoPageFeeCalculator.calculateFeeForShoppingcart
                (inputShoppingCart);
        return feeCalculatedShoppingCart;
    }

    /** This Method Is Used To Verify Whether A User Purchased The Document.
     * @param userName EmailId of the User Logged In.
     * @param productKey ProductKey of the document.
     * @param uniqueIdentifier Access Name.
     * @return Pay As U Go Tx Id
     * @throws SDLBusinessException
     */
    @Transactional(readOnly = true)
    public PayAsUGoTxItem getPayAsUGoTxIdForPurchasedDoc(String userName, String productKey, String uniqueIdentifier) {
        Assert.hasLength(userName, "User Name Cannot be Null");
        Assert.hasLength(productKey, "Product Key Cannot be Null");
        Assert.hasLength(uniqueIdentifier, "Unique Identifier Cannot be Null");
        return this.payAsUGoSubDAO.getPayAsUGoTxIdForPurchasedDoc(userName, productKey, uniqueIdentifier);
    }


    /** This Method Is Used To Get ShoppingBasket Items For The User.
     * @param userName EmailId of the User Logged In.
     * @param nodeName Node Name.
     * @return the list of ShoppingCart Items.
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class, readOnly = false)
    public List<ShoppingCartItem> getShoppingBasketItems(String userName, String nodeName) {
        Assert.hasLength(userName, "User Name Cannot be Null");
        if (nodeName == null) {
        	return this.payAsUGoSubDAO.getShoppingCart(userName);
        } else {
        	return this.payAsUGoSubDAO.getShoppingCart(userName, nodeName);
        }
    }

    /** This Method Is Used To Delete Specified ShoppingCartItems.
     * @param userName EmailId of the User Logged In.
     * @param nodeName Node Name.
     * @return
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void deleteShoppingCart(List<ShoppingCartItem> shoppingCartItems) {
        Assert.notNull(shoppingCartItems, "Shopping Cart Items Cannot Be Null");
        this.payAsUGoSubDAO.deleteShoppingCart(shoppingCartItems);
    }

    /** This Method Is Used To Delete Specified ShoppingCartItem.
     * @param userName EmailId of the User Logged In.
     * @param nodeName Node Name.
     * @return
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void deleteShoppingCartItem(ShoppingCartItem shoppingCartItem) {
        Assert.notNull(shoppingCartItem, "Shopping Cart Item Canno Be Null");
        this.payAsUGoSubDAO.deleteShoppingCartItem(shoppingCartItem);
    }

    /** This Method Returns All The PayAsUGo Transactions Made By The User For A Particular Node.
     * @param userName EmailId of the User Logged In.
     * @param nodeName Node Name.
     * @return The List Of WebTransactions.
     */
    @Transactional(readOnly = true)
    public List<PayAsUGoTxView> getPayAsUGoTxByNode(String userName, String nodeName,
    		Date fromDate, Date toDate) {
        Assert.hasLength(userName, "User Name Cannot be Null/Empty");
        Assert.hasLength(nodeName, "Node Name Cannot be Null/Empty");
		if(toDate != null){
			// Add 24 hours, so that results include
			toDate.setTime(toDate.getTime() + (24 * 60 * 60 * 1000));
		}

        return this.payAsUGoSubDAO.getPayAsUGoTransactionsByNode(userName, nodeName, fromDate, toDate);
    }

    /** This Method Returns All The PayAsUGo Transactions Made By The User For A Particular Node.
     *
     * This method is provided pagination records for pay as u go transactions.
     *
     * @param userName EmailId of the User Logged In.
     * @param nodeName Node Name.
     * @return The List Of WebTransactions.
     */
    @Transactional(readOnly = true)
    public PageRecordsDTO getPayAsUGoTxByNodePerPage(String userName, String nodeName,
    		Date fromDate, Date toDate, int startingFrom, int numberOfRecords) {
        Assert.hasLength(userName, "User Name Cannot be Null/Empty");
        Assert.hasLength(nodeName, "Node Name Cannot be Null/Empty");
		if(toDate != null){
			// Add 24 hours, so that results include
			toDate.setTime(toDate.getTime() + (24 * 60 * 60 * 1000));
		}
        return this.payAsUGoSubDAO.getPayAsUGoTransactionsByNodePerPage(userName, nodeName, fromDate, toDate,
        		startingFrom, numberOfRecords);
    }

    /** This Method Returns All The Information About PayAsUGotransaction Made By The User.
     * @param userName EmailId of the User Logged In.
     * @param payAsUGotransactionId PayAsUGotransactionId.
     * @param isRefund Boolean Flag.
     * @return PayAsUGoTransaction
     */
    @Transactional(readOnly = true)
    public PayAsUGoTx getPayAsUGoTxDetail(String userName, Long webtransactionId, String isRefund) {
        Assert.hasLength(userName, "User Name Cannot be Null/Empty");
        Assert.notNull(webtransactionId, "Web Transaction Id Cannot Be Null");
        return this.payAsUGoSubDAO.getPayAsUGoTransactionDetail(userName, webtransactionId, isRefund);
    }

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
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public PayPalDTO doReferenceCreditPayAsUGo(String txRefNumber, String comments, String modUserId, String machineName,
            String siteName) throws PaymentGatewayUserException, PaymentGatewaySystemException, SDLBusinessException {
        Assert.hasLength(txRefNumber, "Transaction Ref Number Cannot be Null");
        Assert.hasLength(comments, "Comments Cannot be Null/Empty");
        Assert.hasLength(modUserId, "Modified User Id Cannot be Null/Empty");
        Assert.isTrue((comments.length() < 249), "Comments Cannot Be Greater Than 250 characters length.");
        SDLBusinessException sDLBusinessException = null;
        PayAsUGoTx payAsUGoTransaction = this.payAsUGoSubDAO.getPayAsUGoTransactionByTxRefNum(txRefNumber, siteName);

        if(payAsUGoTransaction == null) {
        	sDLBusinessException = new SDLBusinessException();
        	sDLBusinessException.setErrorCode("ERROR");
        	sDLBusinessException.setBusinessMessage("Invalid Transaction Reference Number!");
            throw sDLBusinessException;
        }

        if (Days.daysBetween(new DateTime(payAsUGoTransaction.getTransactionDate()).toDateMidnight(),
                new DateTime().toDateMidnight()).getDays()  > Integer.valueOf(txValidityPeriod)) {
        	sDLBusinessException = new SDLBusinessException();
        	sDLBusinessException.setErrorCode("ERROR");
        	sDLBusinessException.setBusinessMessage(this.getMessage("web.trans.txDatePastValidityPeriod",
        			new Object[]{payAsUGoTransaction.getTxRefNum(),
        			SystemUtil.format(payAsUGoTransaction.getTransactionDate().toString()), txValidityPeriod}));
            throw sDLBusinessException;
        }

        Long originalWebTxId = payAsUGoTransaction.getId();
        Site site = payAsUGoTransaction.getSite();
        PayPalDTO paymentTxResponseDTO = null;
        if(payAsUGoTransaction.getTotalTxAmount() > 0.0){
        	paymentTxResponseDTO = this.paymentGateway.doReferenceCredit(site, txRefNumber, "WEB",
        			"doReferenceCreditWeb", modUserId);
        } else {
        	paymentTxResponseDTO = new PayPalDTO();
        	String randomString = UUID.randomUUID().toString();
        	randomString = randomString.replaceAll("-", "");
        	paymentTxResponseDTO.setTxRefNum(randomString.substring(0, REFERENCE_NUMBER_LENGTH).toUpperCase());
        }
        paymentTxResponseDTO.setTxAmount(payAsUGoTransaction.getTotalTxAmount());
        payAsUGoTransaction.setOrigTxRefNum(payAsUGoTransaction.getTxRefNum());
        payAsUGoTransaction.setTxRefNum(paymentTxResponseDTO.getTxRefNum());
        payAsUGoTransaction.setModifiedDate(new Date());
        payAsUGoTransaction.setMerchantId(payAsUGoTransaction.getSite().getMerchant().getId());
        payAsUGoTransaction.setCreatedDate(new Date());
        payAsUGoTransaction.setTransactionType(TransactionType.REFUND);
        payAsUGoTransaction.setModifiedBy(modUserId);
        payAsUGoTransaction.setCreatedBy(modUserId);
        payAsUGoTransaction.setComments(comments);
        payAsUGoTransaction.setMachineName(machineName);
        payAsUGoTransaction.setId(null);
        payAsUGoTransaction.setAuthCode(paymentTxResponseDTO.getAuthCode());
        payAsUGoTransaction.setSettlementStatus(SettlementStatusType.UNSETTLED);
        payAsUGoTransaction.setCheckNum(null);
        payAsUGoTransaction.setSiteId(payAsUGoTransaction.getSite().getId());
        payAsUGoTransaction.setTxFeeFlat(0.0d);
        if (payAsUGoTransaction.getCardType() == CardType.AMEX) {
            payAsUGoTransaction.setTxFeePercent(0.0d);
        } else {
            payAsUGoTransaction.setTxFeePercent(0 - payAsUGoTransaction.getTxFeePercent());
        }
        try {
             payAsUGoTransaction.setTransactionDate(SystemUtil.changeTimeZone(new Date(),
            		 TimeZone.getTimeZone(site.getTimeZone())));
            this.payAsUGoSubDAO.savePayAsUGoTransaction(payAsUGoTransaction);
            int nOfRecordsUpdated = this.payAsUGoSubDAO.updateRefundTxForPayAsUGoTxItems(originalWebTxId,
            		payAsUGoTransaction.getId(), modUserId);
            if (nOfRecordsUpdated == 0) {
                throw new RuntimeException("Records Not Updated in updateRefundTxForPayAsUGoTxItems");
            }
        } catch (Exception e) {
            logger.error(NOTIFY_ADMIN, payAsUGoTransaction.toString());
            throw new RuntimeException("Server Error, Please contact Administrator");
        }

        return paymentTxResponseDTO;
    }

    /** This method is used to save shoppingCartItem.
     * @param shoppingCartItem
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void saveShoppingCartItem(ShoppingCartItem shoppingCartItem) {
    	// Set the location if not null
    	if(!StringUtils.isBlank(shoppingCartItem.getLocationName()) &&
    			!StringUtils.isBlank(shoppingCartItem.getAccessName())){
    		Location location = this.payAsUGoSubDAO.getLocationByNameAndAccessName(shoppingCartItem.getLocationName(),
    				shoppingCartItem.getAccessName());
    	    if(location != null){
    	    	shoppingCartItem.setLocationId(location.getId());
    	    }
    	}

        this.payAsUGoSubDAO.saveShoppingCartItem(shoppingCartItem);
    }

    /** This Method Is Used To Get The All The Information About PayAsUGotransaction Of The Supplied Transaction Reference Number.
     * @param txRefNumber Transaction Reference Number.
     * @param siteName Name Of Site.
     * @return PayAsUGoTransaction.
     */
    @Transactional(readOnly = true)
    public PayAsUGoTx getPayAsUGoTxByTxRefNum(String txRefNumber, String siteName) {
        Assert.hasLength(txRefNumber, "Tx Reference Number Cannot be Null/Empty");
        return this.payAsUGoSubDAO.getPayAsUGoTransactionByTxRefNum(txRefNumber, siteName);
    }

    private String getMessage(String messageKey, Object[] object) {
        return this.messages.getMessage(messageKey, object, new Locale("en"));
    }

    private String getMessage(String messageKey) {
        return this.messages.getMessage(messageKey, null, new Locale("en"));
    }


    @Transactional(readOnly = true)
    public void updateShoppingCartComments(Long shoppingCartId, String comments) {
        Assert.notNull(shoppingCartId, "Shopping Cart ID Cannot be Null");
        this.payAsUGoSubDAO.updateShoppingCartComments(shoppingCartId, comments);
    }



    private boolean isTransactionFree(List<ShoppingCartItem> feeCalculatedShoppingCart){
    	double amount = 0.0;
        for (ShoppingCartItem shoppingCartItem : feeCalculatedShoppingCart) {
        	amount = amount + shoppingCartItem.getBaseAmount();
           	amount = amount + shoppingCartItem.getServiceFee();
            amount = amount + shoppingCartItem.getTotalTxAmount();
        }
        return amount == 0;
    }

    @Transactional(readOnly = true)
	public List<Location> getLocationsBySiteId(Long siteId){
    	return this.payAsUGoSubDAO.getLocationsBySiteId(siteId);
    }

    @Transactional(readOnly = true)
	public Location getLocationByNameAndAccessName(String locationName, String accessName){
     	return this.payAsUGoSubDAO.getLocationByNameAndAccessName(locationName, accessName);
    }

    @Transactional(readOnly = true)
	public Location getLocationSignatureById(Long locationId){
    	return this.payAsUGoSubDAO.getLocationSignatureById(locationId);
	}

    @Transactional(readOnly = true)
	public Location getLocationSealById(Long locationId){
    	return this.payAsUGoSubDAO.getLocationSealById(locationId);
	}



}