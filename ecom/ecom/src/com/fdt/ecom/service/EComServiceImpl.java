package com.fdt.ecom.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fdt.achtx.entity.CheckHistory;
import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.ecom.dao.EComDAO;
import com.fdt.ecom.dto.UserCountDTO;
import com.fdt.ecom.entity.BankDetails;
import com.fdt.ecom.entity.Code;
import com.fdt.ecom.entity.Merchant;
import com.fdt.ecom.entity.NodeConfiguration;
import com.fdt.ecom.entity.ReceiptConfiguration;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.SiteConfiguration;
import com.fdt.ecom.entity.Term;
import com.fdt.ecom.entity.enums.PaymentType;
import com.fdt.email.EmailProducer;
import com.fdt.paymentgateway.service.PaymentGatewayService;
import com.fdt.security.dao.UserDAO;
import com.fdt.security.entity.Access;
import com.fdt.security.exception.UserAccountExistsException;
import com.fdt.subscriptions.dao.SubDAO;

@Service("ecomService")
public class EComServiceImpl implements EComService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${ecommerce.serverurl}")
    private String ecomServerURL = null;

    @Autowired
    private EComDAO eComDAO = null;

    @Autowired
    private SubDAO subDAO = null;

    @Autowired
    private UserDAO userDAO = null;

    @Autowired
    @Qualifier("paymentGateway")
    private PaymentGatewayService paymentGateway = null;

    @Autowired
    private EmailProducer emailProducer = null;

    @Autowired
    @Qualifier("ehCacheManager")
    private CacheManager cacheManager = null;

    @Autowired
    @Qualifier("serverMessageSource")
    private MessageSource messages = null;

    /** This Method Returns All The Sites Belonging To nodeName.
     * @param nodeName Name Of Node.
     * @return List Of Sites.
     */
    @Transactional(readOnly = true)
    public List<Site> getSitesForNode(String nodeName) {
        Assert.hasLength(nodeName, "Node Cannot be Null/Empty");
        List<Site> sites = this.eComDAO.getSitesForNode(nodeName);
        return sites;
    }

    /** This Method Returns All The Sites.
     * @return List Of Sites.
     */
    @Transactional(readOnly = true)
    public List<Site> getSites() {
        List<Site> sites = this.eComDAO.getSites();
        return sites;
    }

    /** This Method Returns All The Accesses Belonging To site.
     * @param siteId Id Of Site.
     * @return List Of Accesses.
     */
    @Transactional(readOnly = true)
    public List<Access> getAccessesForSite(String siteId) {
        Assert.hasLength(siteId, "Site Id Cannot be Null/Empty");
        List<Access> accessList = this.eComDAO.getAccessesForSite(siteId);
        return accessList;
    }


    /** This Method Is Used To Get Node Configuration.
     * @param nodeName Name Of Node.
     * @return Node Configuration.
     */
    @Transactional(readOnly = true)
    public NodeConfiguration getNodeConfiguration(String nodeName) {
        Assert.hasLength(nodeName, "Node Name Cannot be Null/Empty");
        return this.eComDAO.getNodeConfiguration(nodeName);
    }

    /** This Method Is Used To Get Site Configuration.
     * @param siteId Id Of Site.
     * @return Site Configuration
     */
    @Transactional(readOnly = true)
    public SiteConfiguration getSiteConfiguration(Long siteId) {
        Assert.notNull(siteId, "siteId Cannot be Null");
        return this.eComDAO.getSiteConfiguration(siteId);
    }

    /** This Method Is Used To Update Node Configuration.
     * @param nodeConfiguration New Node Configuration.
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void updateNodeConfiguration(NodeConfiguration nodeConfiguration) {
        Assert.notNull(nodeConfiguration, "Node Configuration Cannot be Null");
        Assert.hasLength(nodeConfiguration.getFromEmailAddress(), "From Email Address Cannot be Null/Empty");
        Assert.hasLength(nodeConfiguration.getResetPasswordSubject(), "Reset Password Subject Cannot be Null/Empty");
        Assert.hasLength(nodeConfiguration.getUserActivationSubject(), "User Activation Email Subject Cannot be Null/Empty");
        Assert.hasLength(nodeConfiguration.getNodeName() , "Node Name Cannot be Null/Empty");
        this.eComDAO.updateNodeConfiguration(nodeConfiguration);
    }

    /** This Method Is Used To Update Site Configuration.
     * @param siteConfiguration New Site Configuration.
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void updateSiteConfiguration(SiteConfiguration siteConfiguration) {
        Assert.notNull(siteConfiguration, "Site Configuration Cannot be Null");
        Assert.hasLength(siteConfiguration.getFromEmailAddress(), "From Email Address Cannot be Null/Empty");
        Assert.hasLength(siteConfiguration.getPaymentConfirmationSubject(), "Payment Confirmation Subject Cannot be Null/Empty");
        Assert.hasLength(siteConfiguration.getChangeSubscriptionSubject(), "Change Subscription Subject Cannot be Null/Empty");
        Assert.hasLength(siteConfiguration.getCancelSubscriptionSubject(), "Cancel Subscription Subject Cannot be Null/Empty");
        Assert.hasLength(siteConfiguration.getReactivateSubscriptionSubject(), "Reactivate Subscription Subject Cannot be Null/Empty");
        Assert.hasLength(siteConfiguration.getRecurringPaymentSuccessSubject(), "Recurring Payment Success Subject Cannot be Null/Empty");
        Assert.hasLength(siteConfiguration.getRecurringPaymentUnsuccessfulSubject(), "Recurring Payment Failure Subject Cannot be Null/Empty");
        Assert.hasLength(siteConfiguration.getAccessAuthorizationSubject(), "Access Authorization Subject Cannot be Null/Empty");
        Assert.hasLength(siteConfiguration.getWebPaymentConfSubject(), "Web Payment Confirmation Subject Cannot be Null/Empty");
        Assert.hasLength(siteConfiguration.getPayAsUGoPaymentConfSubject(), "Pay As You Go Payment Confirmation Subject Cannot be Null/Empty");
        Assert.hasLength(siteConfiguration.getRemoveSubscriptionSubject(), "Remove Subscription Subject Cannot be Null/Empty");
        Assert.notNull(siteConfiguration.getSiteId(), "Site Id Cannot be Null/Empty");
        this.eComDAO.updateSiteConfiguration(siteConfiguration);
    }

    /** This method returns codes like WEEK, BIWK, PP, FREE, AICMS depending on supplied codeCategory.
     * @param codeCategory RECURRING_SUBSCRIPTION/NON_RECURRING_SUBSCRIPTION/FREE_SUBSCRIPTION/REGISTERED_APPLICATION.
     * @return List Of Codes.
     */
    @Transactional(readOnly = true)
    public List<Code> getCodes(String codeCategory) {
        Assert.hasLength(codeCategory, "codeCategory Cannot be Null/Empty");
        return this.eComDAO.getCodes(codeCategory);
    }

    /** This Method Returns All The Site Details Which Include Information About Site, Term, Merchant, CreditUsageFee,
     * MagensaInfo, And WebPaymentFee.
     * @param siteName Name Of The Site.
     * @return Site.
     * @throws SDLBusinessException
     */
    @Transactional(readOnly = true)
    public Site getSiteDetailsBySiteName(String siteName) throws SDLBusinessException {
        Assert.hasLength(siteName, "siteName Cannot be Null/Empty");
        Site site = this.eComDAO.getSiteDetailsBySiteName(siteName);
        if (site == null) {
            SDLBusinessException sDLBussExcep = new SDLBusinessException();
            sDLBussExcep.setBusinessMessage(this.getMessage("ecom.site.noconfig", new String[]{siteName}));
        }
        return site;
    }

    /** This Method Returns ReceiptConfiguration About A Site.
     * @param siteName Name Of The Site.
     * @param paymentType OTC/WEB/Recurring
     * @return List Of ReceiptConfigurations.
     */
    @Transactional(readOnly = true)
    public List<ReceiptConfiguration> getReceiptConfigurationForSiteAndPaymentType(String siteName, PaymentType paymentType) {
        Assert.hasLength(siteName, "siteName Cannot be Null/Empty");
        Assert.notNull(paymentType, "paymentType Cannot be Null/Empty");
        return this.eComDAO.getReceiptConfigurationForSiteAndPaymentType(siteName, paymentType);
    }

    /** This Method Refreshes All The Caches.
    *
    */
    public void refreshCache() {
        List<String> cacheNameList = this.getCacheNames();
        for (String cacheName : cacheNameList) {
            Cache cache = this.cacheManager.getCache(cacheName);
            cache.clear();
        }
    }

    /** This Method Refreshes Cache With cacheName.
     * @param cacheName Name Of The Cache To Be Refreshed.
     */
    public void refreshCacheByName(String cacheName) {
        Assert.hasLength(cacheName, "cacheName Cannot be Null/Empty");
        Cache cache = this.cacheManager.getCache(cacheName);
        Assert.notNull(cache, "Cache should exist, for it to be refreshed.");
        cache.clear();
    }

    /** This Method Returns All The Cache Names.
     * @return List Of Cache Names.
     */
    public List<String> getCacheNames() {
        List<String> cacheNameList = new LinkedList<String>(this.cacheManager.getCacheNames());
        return cacheNameList;
    }

    /** This Method Is Used To Get Merchant Details Of The Site.
     * @param siteId Site Id.
     * @return List Of Merchants.
     */
    @Transactional(readOnly = true)
    public List<Merchant> getMerchantDetailsBySite(Long siteId) {
        return this.eComDAO.getMerchantDetailsBySite(siteId);
    }

    /** This Method Returns The Term Information Of A Site.
     * @param siteName Name Of The Site.
     * @return Term
     */
    @Transactional(readOnly = true)
    public Term getTerm(String siteName) {
        return this.eComDAO.getTerm(siteName);
    }

    /** This Method Is Used To Delete The Credit Card Information Associated With The User.
     * @param userName Email Id Of Person whose credit card information should be deleted.
     * @throws UserAccountExistsException
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void deleteCreditCard(String userName) throws UserAccountExistsException {
        Assert.hasLength(userName, "userName Cannot be Null/Empty");
        if(this.eComDAO.doUserAccountsExistForUser(userName)){
            throw new UserAccountExistsException("User has UserAccounts Associcated With Him. Could Not Delete" +
                    "The Credit Card");
        }
        this.eComDAO.deleteCreditCard(userName);
    }

    @Transactional(readOnly = true)
    public List<UserCountDTO> getUserCountsForAllSite() {
   		return this.eComDAO.getUserCountsForAllSite();
    }
    @Transactional(readOnly = true)
    public UserCountDTO getUserCountForSite(Long siteId) {
    	return this.eComDAO.getUserCountForSite(siteId);
    }

    @Transactional(readOnly = true)
    public List<UserCountDTO> getUserCountsBySubForASite(Long siteId) {
        /** There is no Assert as the Site Id could be Null**/
        return this.eComDAO.getUserCountsBySubForASite(siteId);
    }

    @Transactional(readOnly = true)
    public List<UserCountDTO> getUserDistributionBySubscription(Long siteId, Long accessId) {
        /** There is no Assert as the Site/AccessId could be Null**/
        return this.eComDAO.getUserDistributionBySub(siteId, accessId);
    }

    @Transactional(readOnly = true)
    public PageRecordsDTO lookupTx(String productId, String productName, String productType, String invoiceId,
    		String txRefNumber, String accountName, String accountNumber, String transactionStartDate, String transactionEndDate,
    		PaymentType paymentType, String siteName, int startFrom, int numberOfRecords) throws SDLBusinessException {

    	try{
    		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
	    	// If start date is null set it to beginning
	    	if(StringUtils.isBlank(transactionStartDate)){
	    		transactionStartDate = "1970-01-01";
	    	} else {
	    		Date dt = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH).parse(transactionStartDate);
	    		transactionStartDate = dateFormat.format(dt);
	    	}
	    	// If end date is null set it to farthest.
	    	if(StringUtils.isBlank(transactionEndDate)){
	    		transactionEndDate = "9999-01-01";
	    	} else {
	    		Date dt = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH).parse(transactionEndDate);
	    		transactionEndDate = dateFormat.format(dt);
	    	}
    	}catch(Exception e){
    		throw new SDLBusinessException ("Invalid Date Format !");
    	}
    	
    	//
    	// Based on payment type call appropriate method of DAO
    	if(paymentType.toString().equals("OTC")){
    		return this.eComDAO.lookupOTCTx(txRefNumber, accountName, accountNumber, transactionStartDate,
    				transactionEndDate, siteName, productName, productType, invoiceId, startFrom, numberOfRecords);
    	} else if(paymentType.toString().equals("WEB")){
    		return this.eComDAO.lookupWebTx(txRefNumber, accountName, accountNumber, transactionStartDate,
    				transactionEndDate, siteName, productId, productName, productType, invoiceId, startFrom, numberOfRecords);
		} else if(paymentType.toString().equals("RECURRING")){
			return this.eComDAO.lookupRecurringTx(txRefNumber, accountName, accountNumber, transactionStartDate,
					transactionEndDate, siteName, startFrom, numberOfRecords);
		} else if(paymentType.toString().equals("PAYASUGO")){
			return this.eComDAO.lookupPayAsUGoTx(txRefNumber, accountName, accountNumber, transactionStartDate,
					transactionEndDate, siteName, productId, productName, productType, startFrom, numberOfRecords);

    	}
    	return new PageRecordsDTO();

    }

    @Transactional(readOnly = true)
    public Site getSiteAdminDetails(Long siteId) {
        Assert.notNull(siteId, "Site Id Cannot be Null");
        Site site = this.eComDAO.getSiteAdminDetails(siteId);
        return site;
    }

    @Transactional(readOnly = true)
    public List<CheckHistory> getCheckHistories(Long siteId, String fromDate, String toDate, String checkNum,
    	Double checkAmt) {
        /** Don't make these assert statements, As the UI can send null Values**/
        return this.eComDAO.getCheckHistories(siteId, fromDate, toDate, checkNum, checkAmt);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class, readOnly = false)
    public boolean doVoidCheck(Long checkNumber, String comments) {
        Assert.notNull(checkNumber, "Check Number Cannot Be Null");
        Assert.notNull(comments, "Comments Cannot be Null!");
        Assert.isTrue((comments.length() < 1999), "Comments Cannot Be Greater Than 2000 characters length.");
        return this.eComDAO.doVoidCheck(checkNumber, comments);
    }

    @Transactional(readOnly = true)
    public CheckHistory getCheckHistory(Long checkNumber) {
        Assert.notNull(checkNumber, "Check Number Cannot Be Null");
        return this.eComDAO.getCheckHistory(checkNumber);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void saveReceiptConfiguration(ReceiptConfiguration receiptConfiguration) {
        Assert.notNull(receiptConfiguration, "receiptConfiguration Cannot Be Null");
        this.eComDAO.saveReceiptConfiguration(receiptConfiguration);
    }

    @Transactional(readOnly = true)
    public List<ReceiptConfiguration> getReceiptConfigurationsForSite(Long siteId) {
        Assert.notNull(siteId, "Site Id Cannot be Null/Empty");
        return this.eComDAO.getReceiptConfigurationsForSite(siteId);
    }

    @Transactional(readOnly = true)
    public ReceiptConfiguration getReceiptConfigurationDetail(Long receiptConfigurationId) {
        Assert.notNull(receiptConfigurationId, "ReceiptConfigurationId Id Cannot be Null/Empty");
        return this.eComDAO.getReceiptConfigurationDetail(receiptConfigurationId);
    }

    @Transactional(readOnly = true)
    public PageRecordsDTO getErrorLog(String fromDate, String toDate, String userName, int startFromRecord, int numberOfRecords) {
        return this.eComDAO.getErrorLog(fromDate, toDate, userName, startFromRecord, numberOfRecords);
    }

    @Transactional(readOnly = true)
    public BankDetails getBankDetailsBySite(Long siteId) {
        Assert.notNull(siteId, "Site Id Cannot be Null");
        return this.eComDAO.getBankDetailsBySite(siteId);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void deleteErrorLogContents(Long errorLogId) {
        Assert.notNull(errorLogId, "Error Id Cannot be Null");
        this.eComDAO.deleteErrorLogContents(errorLogId);
    }

    @Transactional(readOnly = true)
   	public PaymentType getPaymentTypeForTransaction(String txRefNumber) {
   		return this.eComDAO.getPaymentTypeForTransaction(txRefNumber);
   	}

    private String getMessage(String messageKey, Object[] object) {
        return this.messages.getMessage(messageKey, object, new Locale("en"));
    }


}
