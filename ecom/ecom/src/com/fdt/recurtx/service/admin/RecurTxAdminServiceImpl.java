package com.fdt.recurtx.service.admin;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.util.EComUtil;
import com.fdt.common.util.SystemUtil;
import com.fdt.common.util.spring.SpringUtil;
import com.fdt.ecom.dao.EComDAO;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.SiteConfiguration;
import com.fdt.ecom.entity.UserHistory;
import com.fdt.ecom.entity.enums.CardType;
import com.fdt.ecom.entity.enums.SettlementStatusType;
import com.fdt.ecom.entity.enums.TransactionType;
import com.fdt.ecom.service.EComService;
import com.fdt.ecom.util.CreditCardUtil;
import com.fdt.email.EmailProducer;
import com.fdt.payasugotx.service.PayAsUGoTxService;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.paymentgateway.service.PaymentGatewayService;
import com.fdt.recurtx.dao.RecurTxDAO;
import com.fdt.recurtx.dto.ExpiredOverriddenSubscriptionDTO;
import com.fdt.recurtx.dto.RecurTxSchedulerDTO;
import com.fdt.recurtx.entity.RecurTx;
import com.fdt.security.dao.UserDAO;
import com.fdt.security.dto.FirmUserDTO;
import com.fdt.security.entity.Access;
import com.fdt.security.entity.UserAccess;
import com.fdt.subscriptions.dao.SubDAO;

@Service("recurTXAdminService")
public class RecurTxAdminServiceImpl implements RecurTxAdminService {

    private static final Logger logger = LoggerFactory.getLogger(RecurTxAdminServiceImpl.class);

    public static String PAYPAL_SCHEDULER = "SCHEDULER";

    @Autowired
    private EComService eComService;

    @Autowired
    @Qualifier("paymentGateway")
    private PaymentGatewayService paymentGateway;

    @Autowired
    private RecurTxDAO recurTxDAO;

    @Autowired
    private SubDAO subDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private EComDAO eComDAO;

    @Value("${ecommerce.serverurl}")
    private String ecomServerURL = null;

    @Autowired
    private EmailProducer emailProducer;

    @Autowired
    private SpringUtil springUtil;

    @Autowired
    @Qualifier("serverMessageSource")
    private MessageSource messages = null;

    @Value("${tx.ValidityPeriod}")
    /* Default Value is 60 Days */
    private String txValidityPeriod = "60";
    
    @Autowired
    private PayAsUGoTxService payAsUGoSubService = null;


    /** This Method Is Used To do A Reference Credit For Recurring Transactions.
     * @param txRefNumber Original Transaction Reference Number.
     * @param comments Comments Supplied By The User Who Is Issuing A Refund.
     * @param modUserId User Who Is Issuing A Refund.
     * @param machineName Machine From Which User Is Issuing a Refund.
     * @param siteName Name Of The Site.
     * @return Returns DTO With TxNumber Set If Payment Goes Through. If not, errorMessage & errorDescription Is Set.
     * @throws PaymentGatewayUserException
     * @throws PaymentGatewaySystemException
     * @throws SDLBusinessException
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public PayPalDTO doReferenceCreditRecurTx(String txRefNumber, String comments, String modUserId,
            String machineName, String siteName) throws PaymentGatewayUserException, PaymentGatewaySystemException,
                SDLBusinessException {
        Assert.hasLength(txRefNumber, "Transaction Ref Number Cannot be Null/Empty");
        Assert.hasLength(modUserId, "Modified User Id Cannot be Null/Empty!");
        Assert.hasLength(machineName, "machineName Cannot be Null/Empty!");
        Assert.hasLength(comments, "comments Cannot be Null/Empty!");
        Assert.isTrue((comments.length() < 1999), "Comments cannot be more than 2000 characters long.");
        String refundTxRefNum =  "";
        PayPalDTO paymentTxResponseDTO = null;
        List<RecurTx> recurTransactionList = this.getRecurTxByTxRefNum(txRefNumber, siteName);

        if(recurTransactionList == null) {
            throw new SDLBusinessException("Invalid Original Transaction Reference Number!!");
        }
        RecurTx recurTransaction = new RecurTx();
        for (RecurTx transaction : recurTransactionList) {
            if (transaction.getTransactionType().toString().equalsIgnoreCase("CHARGE") && !transaction.isPreviousAccess()) {
                recurTransaction = transaction;
            }
        }
        if (Days.daysBetween(new DateTime(recurTransaction.getTransactionDate()).withTimeAtStartOfDay(),
                new DateTime().withTimeAtStartOfDay()).getDays()  > Integer.valueOf(txValidityPeriod)) {
            throw new SDLBusinessException(this.getMessage("web.trans.txDatePastValidityPeriod",
                new Object[]{recurTransaction.getTxRefNum(), SystemUtil.format(recurTransaction.getTransactionDate()
                    .toString()), txValidityPeriod}));
        }
        CreditCard creditCard = this.userDAO.getCreditCardDetails(recurTransaction.getUserId());
        Assert.notNull(recurTransaction.getSite(), "Original Transaction Reference Number Has No MerchantInfo");
        if (recurTransaction != null) {
            refundTxRefNum = this.paymentGateway.doCredit(creditCard, recurTransaction.getSite().getMerchant(),
                recurTransaction.getTotalTxAmount(), txRefNumber, "doReferenceCreditRecurringTx", modUserId);
            paymentTxResponseDTO = new PayPalDTO();
            paymentTxResponseDTO.setTxAmount(recurTransaction.getTotalTxAmount());
            paymentTxResponseDTO.setTxRefNum(refundTxRefNum);
            recurTransaction.setOrigTxRefNum(recurTransaction.getTxRefNum());
            recurTransaction.setTxRefNum(refundTxRefNum);
            recurTransaction.setModifiedDate(new Date());
            recurTransaction.setMerchantId(recurTransaction.getSite().getMerchant().getId());
            recurTransaction.setTransactionDate(SystemUtil.changeTimeZone(new Date(),
                    TimeZone.getTimeZone(recurTransaction.getSite().getTimeZone())));
            recurTransaction.setCreatedDate(new Date());
            recurTransaction.setTransactionType(TransactionType.REFUND);
            recurTransaction.setActive(true);
            recurTransaction.setModifiedBy(modUserId);
            recurTransaction.setCreatedBy(modUserId);
            recurTransaction.setComments(comments);
            recurTransaction.setMachineName(machineName);
            recurTransaction.setId(null);
            recurTransaction.setSettlementStatus(SettlementStatusType.UNSETTLED);
            recurTransaction.setCheckNum(null);
            recurTransaction.setTxFeeFlat(0.0d);
            if (recurTransaction.getCardType() == CardType.AMEX) {
                recurTransaction.setTxFeePercent(0.0d);
            } else {
                recurTransaction.setTxFeePercent(0 - recurTransaction.getTxFeePercent());
            }
            try {
                this.recurTxDAO.saveRecurTransaction(recurTransaction);
            } catch (Exception e) {
                logger.error(NOTIFY_ADMIN, "Error in doReferenceCreditRecurringTx", recurTransaction.toString());
            }
        }
        return paymentTxResponseDTO;
    }

    /** This Method Is Used To Get The Refund Transaction Number For The Supplied Original Transaction Reference Number.
     * @param originaltxRefNumber Transaction Reference Number.
     * @param siteName Name Of the Site.
     * @return RecurTransaction Of Refund Transaction.
     */
    @Transactional(readOnly = true)
    public RecurTx getReferencedRecurTxByTxRefNum(String originaltxRefNumber, String siteName) {
        Assert.hasLength(originaltxRefNumber, "Original Tx Reference Number Cannot be Null/Empty");
        return this.recurTxDAO.getReferencedRecurringTransactionByTxRefNum(originaltxRefNumber, siteName);
    }

    /** This Method Is Used To Get The Details Of Supplied Transaction Reference Number.
     * @param txRefNumber Transaction Reference Number.
     * @param siteName Name Of The Site.
     * @return List Of RecurTransactions
     */
    @Transactional(readOnly = true)
    public List<RecurTx> getRecurTxByTxRefNum(String txRefNumber, String siteName) {
        Assert.hasLength(txRefNumber, "Tx Reference Number Cannot be Null/Empty");
        return this.recurTxDAO.getRecurringTransactionByTxRefNum(txRefNumber, siteName);
    }

    /** Called By Scheduler To Archive Cancelled Subscriptions. Cancelled Subscriptions Are Subscriptions
     *  Which Are Manually Cancelled By The User. So, User Can Access These Subscriptions Until The End Of
     *  Subscription Recurring Cycle Period. At The End Of Period, Scheduler Calls This Method To Delete The
     *  Subscriptions Of The User.
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public void archiveCancelledRecurSub() {
        List<RecurTxSchedulerDTO> payPalSchedulerDTOs = this.subDAO.getCancelledSubscriptions();
        List<Long> userAccessIds = new LinkedList<Long>();
        List<UserHistory> userHistories = new LinkedList<UserHistory>();
        for (RecurTxSchedulerDTO payPalSchedulerDTO : payPalSchedulerDTOs) {
            userAccessIds.add(payPalSchedulerDTO.getUserAccessId());
            UserHistory userHistory = new UserHistory();
            userHistory.setUserName(payPalSchedulerDTO.getUserName());
            userHistory.setAccesId(payPalSchedulerDTO.getAccessId());
            userHistory.setCreatedDate(new Date());
            userHistory.setModifiedDate(new Date());
            userHistory.setCreatedBy("PAYPAL_SCHEDULER");
            userHistory.setModifiedBy("PAYPAL_SCHEDULER");
            userHistory.setModifiedDate(new Date());
            userHistory.setComments("CANCELLED BY THE USER AND ARCHIVED BY THE SYSTEM");
            userHistories.add(userHistory);
            
            // If User is a firm level administrator then also delete firm users access
            if(payPalSchedulerDTO.isFirmLevelAccess() && payPalSchedulerDTO.isFirmAccessAdmin()){
            	List<FirmUserDTO>firmUsers = this.userDAO.getFirmUsers(payPalSchedulerDTO.getUserName(), 
            			payPalSchedulerDTO.getAccessId());
            	for(FirmUserDTO firmUser : firmUsers){
	                userAccessIds.add(firmUser.getUserAccessId());
	                UserHistory firmUserHistory = new UserHistory();
	                firmUserHistory.setUserName(firmUser.getUsername());
	                firmUserHistory.setAccesId(payPalSchedulerDTO.getAccessId());
	                firmUserHistory.setCreatedDate(new Date());
	                firmUserHistory.setModifiedDate(new Date());
	                firmUserHistory.setCreatedBy("PAYPAL_SCHEDULER");
	                firmUserHistory.setModifiedBy("PAYPAL_SCHEDULER");
	                firmUserHistory.setModifiedDate(new Date());
	                firmUserHistory.setComments("CANCELLED BY THE Admin USER AND ARCHIVED BY THE SYSTEM");
	                userHistories.add(firmUserHistory);
            	}
            }
        }
        if (userHistories.size() > 0) {
            this.subDAO.saveUserHistory(userHistories);
            this.subDAO.deleteUserAccountByUserAccessId(userAccessIds);
            this.subDAO.deleteUserAccess(userAccessIds);
        }
    }

    /** This Method Is Called By Scheduler To Do Recurring Payments. This Method Returns A List Of DTOs Which Contain
     * All The Necessary Information To Charge For The Recurring Billing Cycles.
     * @return List Of RecurTxSchedulerDTOs.
     */
    @Transactional(readOnly = true)
    public List<RecurTxSchedulerDTO> getRecurProfilesForVerification() {
        return this.recurTxDAO.getRecurringProfilesForVerification();
    }

    /** This Method Returns All The Recurring Transactions Associated To A User.
     * @param userName EmailId Of The User Logged In.
     * @return List Of Recurring Transactions.
     */
    @Transactional(readOnly = true)
    public List<RecurTx> getRecurTxByUser(String userName) {
        Assert.hasLength(userName, "userName Cannot be Null/Empty!");
        return this.recurTxDAO.getRecurTxByUser(userName);
    }

    /** This Method Returns All The Recurring Transactions Made On A Particular Site.
     * @param userName EmailId Of The User Logged In.
     * @return List Of Recurring Transactions.
     */
    @Transactional(readOnly = true)
    public List<RecurTx> getRecurTxBySite(String siteName) {
        Assert.hasLength(siteName, "Site Name Cannot be Null/Empty!");
        return this.recurTxDAO.getRecurTxBySite(siteName);
    }

    /** Called From Scheduler For Each Profile. This Method Is Used To Charge Recurring Subscription Users Every Recurring
     * Billing Cycle. If Payment Goes Through, Then Recurring Transaction Is Saved, Billing Dates Are Updated And Payment
     * Successful Email Is Sent. If Payment Is Not Successful, Then UserAccount Is Disabled, UserAccess Is Disabled,
     * CreditCard Is Disabled, And Finally Payment Failure Mail Is Sent.
     * @param payPalSchDTO
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void chargeRecurSub(RecurTxSchedulerDTO payPalSchedulerDTO) {
    	Site site = payPalSchedulerDTO.getSite();
        Double amtToCharge = payPalSchedulerDTO.getAmtToCarge();
        CreditCard creditCard = payPalSchedulerDTO.getCreditCard();
        String userName = payPalSchedulerDTO.getUserName();
        String paymentPeriod = payPalSchedulerDTO.getPaymentPeriod();
        Long userAccountId = payPalSchedulerDTO.getUserAccountId();
        Long merchantId = site.getMerchant().getId();
        String cardNumber = payPalSchedulerDTO.getAccountNumber();
        String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);
        CardType cardType = CreditCardUtil.getCardType(cardNumber);
        Double txFeePercent = null;
        Double txFeeFlat = null;
        if (cardType == CardType.AMEX) {
            txFeePercent = site.getMerchant().getTxFeePercentAmex();
            txFeeFlat = site.getMerchant().getTxFeeFlatAmex();
        } else {
            txFeePercent = site.getMerchant().getTxFeePercent();
            txFeeFlat = site.getMerchant().getTxFeeFlat();
        }
        Double clientShare = payPalSchedulerDTO.getClientShare();
        PayPalDTO paymentTxResponseDTO = null;
        boolean isPaymentSuccessful = true;
        RecurTx recurTransaction = new RecurTx();
        recurTransaction.setLastBillingDate(new Date());
        recurTransaction.setNextBillingDate(SystemUtil.getNextBillingDate(paymentPeriod).toDate());
        Map<String, Object> emailData = new HashMap<String, Object>();
        emailData.put("customerName", payPalSchedulerDTO.getUserFirstName() + " " + payPalSchedulerDTO.getUserLastName());
        emailData.put("currentDate", new Date());
        emailData.put("serverUrl", this.ecomServerURL);
        SiteConfiguration siteConfig = this.eComDAO.getSiteConfiguration(site.getId());
        Assert.notNull(siteConfig, "siteConfig Cannot be Null");
        try {
            paymentTxResponseDTO = this.paymentGateway.doSale(site, amtToCharge, creditCard, "chargeRecurring", userName,
                true);
        } catch (PaymentGatewayUserException paymentGatewayUserException) {
            isPaymentSuccessful = false;
            logger.error("Exception in the EComScheduler {} For PayPalSchedulerDTO {} ",
                paymentGatewayUserException.getDescription(), payPalSchedulerDTO, paymentGatewayUserException);
        } catch (PaymentGatewaySystemException paymentGatewaySystemException) {
            isPaymentSuccessful = false;
            logger.error("Exception in the EComScheduler {} For PayPalSchedulerDTO {} ",
                paymentGatewaySystemException.getDescription(), payPalSchedulerDTO, paymentGatewaySystemException);
        }
        if(isPaymentSuccessful) {
            recurTransaction.setTxRefNum(paymentTxResponseDTO.getTxRefNum());
            recurTransaction.setCardNumber(lastFourDigits);
            recurTransaction.setCardExpired(false);
            recurTransaction.setAccountName(payPalSchedulerDTO.getCreditCard().getName());
            recurTransaction.setBaseAmount(amtToCharge);
            recurTransaction.setTotalTxAmount(amtToCharge);
            recurTransaction.setTransactionDate(SystemUtil.changeTimeZone(new Date(),
                TimeZone.getTimeZone(site.getTimeZone())));
            recurTransaction.setUserId(payPalSchedulerDTO.getUserId());
            recurTransaction.setAccessId(payPalSchedulerDTO.getAccessId());
            recurTransaction.setSite(site);
            Access access = new Access();
            access.setDescription(payPalSchedulerDTO.getAccessDescription());
            recurTransaction.setAccess(access);
            recurTransaction.setSettlementStatus(SettlementStatusType.UNSETTLED);
            recurTransaction.setTransactionType(TransactionType.CHARGE);
            recurTransaction.setActive(true);
            recurTransaction.setMerchantId(merchantId);
            recurTransaction.setCardType(cardType);
            recurTransaction.setTxFeePercent(txFeePercent);
            recurTransaction.setTxFeeFlat(txFeeFlat);
            Double amtToChargeAfterTransactionFee = amtToCharge - (amtToCharge*txFeePercent/100) + txFeeFlat;
            Double graniusShare = amtToChargeAfterTransactionFee * (1.0d - payPalSchedulerDTO.getClientShare());
            boolean thresholdReached = this.payAsUGoSubService.isThresholdReached(site, graniusShare);                    
            if(thresholdReached){
            	clientShare = 1.0d;
            } else {
            	clientShare = payPalSchedulerDTO.getClientShare();
            }
            recurTransaction.setClientShare(clientShare);
            recurTransaction.setModifiedBy(PAYPAL_SCHEDULER);
            recurTransaction.setCreatedBy(PAYPAL_SCHEDULER);
            recurTransaction.setModifiedDate(new Date());
            recurTransaction.setCreatedDate(new Date());
            String machineName = null;
            try {
                InetAddress thisIp =InetAddress.getLocalHost();
                machineName  = thisIp.getHostAddress();
            } catch (UnknownHostException e) {
                machineName = PAYPAL_SCHEDULER;
                logger.error("UnknownHostException in inquirePayPal..");
            }
            recurTransaction.setMachineName(machineName);
            /** Create the Transaction History **/
            this.recurTxDAO.saveRecurTransaction(recurTransaction);
            /** Update the Next Billing Date **/
            int noOfRecordsUpdated = this.subDAO.updateBillingDates(userAccountId, new Date(),
                      SystemUtil.getNextBillingDate(paymentPeriod).toDate(), true, PAYPAL_SCHEDULER);
            if (noOfRecordsUpdated == 0) {
                  logger.error("The Next Billing Dates Are Not Updated");
            throw new RuntimeException("The Next Billing Dates Are Not Updated");
            }
            emailData.put("recurTxHistInfo", recurTransaction);
            this.emailProducer.sendMailUsingTemplate(siteConfig.getFromEmailAddress(),
                payPalSchedulerDTO.getUserName(), siteConfig.getRecurringPaymentSuccessSubject(),
                    siteConfig.getEmailTemplateFolder() + siteConfig.getRecurringPaymentSuccessTemplate(), emailData);
            } else {
                this.disableUserAccountAndSendFailedPaymentEmail(payPalSchedulerDTO, recurTransaction, siteConfig, site,
                    emailData);
            }
    }

     /** This Method Returns All The Recurring Transactions Made By A User On A Particular Site.
	 * @param userName EmailId Of The User Logged In.
	 * @param siteId siteId
	 * @return List Of Recurring Transactions.
	 */
    @Transactional(readOnly = true)
    public List<RecurTx> getRecurTxByUserAndSite(String userName, Long siteId) {
        Assert.hasLength(userName, "userName Cannot be Null/Empty!");
        List<RecurTx> recurTxHistInfoList = null;
        if(siteId == null) {
        	recurTxHistInfoList = this.recurTxDAO.getRecurTransactions(userName);
        } else {
        	recurTxHistInfoList = this.recurTxDAO.getRecurTransactionsBySite(userName, siteId);
        }
        return recurTxHistInfoList;
    }
    
    @Transactional(readOnly = true)
	public List<ExpiredOverriddenSubscriptionDTO> getExpiredOverriddenSubscriptions() {
		return this.recurTxDAO.getExpiredOverriddenSubscriptions();
	}

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
	public void disableOverriddenSubscription(ExpiredOverriddenSubscriptionDTO expiredOverriddenSubscriptionDTO) {
    	this.recurTxDAO.disableOverriddenSubscription(expiredOverriddenSubscriptionDTO);
    	Map<String, Object> emailData = new HashMap<String, Object>();
        emailData.put("customerName", expiredOverriddenSubscriptionDTO.getFirstName() + " " + expiredOverriddenSubscriptionDTO.getLastName());
        emailData.put("currentDate", new Date());
        emailData.put("serverUrl", this.ecomServerURL);
        emailData.put("subcriptionName", expiredOverriddenSubscriptionDTO.getAccessDescription());
    	this.emailProducer.sendMailUsingTemplate(expiredOverriddenSubscriptionDTO.getFromEmailAddress(),
    			expiredOverriddenSubscriptionDTO.getEmailId(), expiredOverriddenSubscriptionDTO.getExpiredOverriddenSubscriptionNotificationSubject(),
    			expiredOverriddenSubscriptionDTO.getEmailTemplateFolder() + expiredOverriddenSubscriptionDTO.getExpiredOverriddenSubscriptionNotificationTemplate(),
                        emailData);
		
	}

    private void disableUserAccountAndSendFailedPaymentEmail(RecurTxSchedulerDTO payPalSchedulerDTO,
    		RecurTx recurTransaction,
            SiteConfiguration siteConfig, Site site, Map<String, Object> emailData) {

    	List<Long> userAccessIdsToBeDisabled = new ArrayList<Long>();
    	userAccessIdsToBeDisabled.add(payPalSchedulerDTO.getUserAccessId());
    	List<UserAccess> firmUserAccessList = null;

    	// Check to see if user is a firm administrator , if yes then retrieve all the firm level user accesses under that firm
    	// We need to disable those user accesses as well.
    	if(payPalSchedulerDTO.isFirmLevelAccess() && payPalSchedulerDTO.isFirmAccessAdmin()){
    		firmUserAccessList = userDAO.getUserAccessForFirmLevelUsers(payPalSchedulerDTO.getUserId(), payPalSchedulerDTO.getAccessId());
    		// add the user accesseds to disable user access list
   			userAccessIdsToBeDisabled.addAll(EComUtil.getUserAccessIdsAsList(firmUserAccessList));
    	}

    	// Disable the accesses
    	this.disableUserAccesses(userAccessIdsToBeDisabled, PAYPAL_SCHEDULER, "FAILED PAYMENT");

    	// pass userAccessId and firm user access ids to be disabled
        this.disableUserAccount(payPalSchedulerDTO.getUserAccessId(), PAYPAL_SCHEDULER, "FAILED PAYMENT", false);
        emailData.put("transactionInfo", "Failure");
        recurTransaction.setUserId(payPalSchedulerDTO.getUserId());
        recurTransaction.setAccessId(payPalSchedulerDTO.getAccessId());
        recurTransaction.setSite(site);
        Access access = new Access();
        access.setDescription(payPalSchedulerDTO.getAccessDescription());
        recurTransaction.setAccess(access);
        emailData.put("recurTxHistInfo", recurTransaction);
        this.emailProducer.sendMailUsingTemplate(siteConfig.getFromEmailAddress(),
            payPalSchedulerDTO.getUserName(), siteConfig.getRecurringPaymentUnsuccessfulSubject(),
                siteConfig.getEmailTemplateFolder() + siteConfig.getRecurringPaymentUnsuccessfulTemplate(),
                    emailData);

        // If user is a firm administrator then send the emails to all the users under the firm.
    	if(payPalSchedulerDTO.isFirmLevelAccess() && payPalSchedulerDTO.isFirmAccessAdmin()){
    		List<String> firmUserEmails = EComUtil.getEmailsAsList(firmUserAccessList);
    		for(String email: firmUserEmails){
    	        this.emailProducer.sendMailUsingTemplate(siteConfig.getFromEmailAddress(),
    	                email, siteConfig.getRecurringPaymentUnsuccessfulSubject(),
    	                    siteConfig.getEmailTemplateFolder() + siteConfig.getRecurringPaymentUnsuccessfulTemplate(),
    	                        emailData);
    		}
    	}
    }


    /**
     * Disable User Accesses
     * @param accessIds
     * @param modifiedBy
     * @param comments
     */
    private void disableUserAccesses(List<Long> accessIds, String modifiedBy, String comments) {
        int recordsModUserAccess = this.userDAO.enableDisableUserAccesses(accessIds, false, modifiedBy, comments, false, null);
        if (recordsModUserAccess == 0) {
            logger.error("The User Access is not Disabled");
            throw new RuntimeException("The User Access is not Disabled");
        }
    }

    private void disableUserAccount(Long userAccessId, String modifiedBy, String comments, boolean isCardExpired) {
        Assert.notNull(userAccessId, "User AccessId Cannot be Null");
        // Disable UserAccount for user, Firm Level User will not have entry in UserAccount , so no need to disable UserAccount for them.
        int recordsModUserAccount = this.subDAO.disableUserAccount(userAccessId, modifiedBy);
        if (recordsModUserAccount == 0) {
            logger.error("The User Account is not Disabled");
            throw new RuntimeException("The User Account is not Disabled");
        }

        if(isCardExpired) {
            // Disable CreditCard if it's expired, Firm Level User will not have credit card for this access
            int recordsModCCInfo = this.subDAO.enableDisableCreditCard(userAccessId, false, modifiedBy);
            if (recordsModCCInfo == 0) {
                logger.error("The User CCInfo is Not Modified");
                throw new RuntimeException("The User CCInfo is Not Modified");
            }
        }
    }



    private String getMessage(String messageKey, Object[] object) {
        return this.messages.getMessage(messageKey, object, new Locale("en"));
    }

	
}