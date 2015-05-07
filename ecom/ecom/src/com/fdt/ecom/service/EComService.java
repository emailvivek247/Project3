package com.fdt.ecom.service;

import java.util.List;

import com.fdt.achtx.entity.CheckHistory;
import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.exception.SDLBusinessException;
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
import com.fdt.security.entity.Access;
import com.fdt.security.exception.UserAccountExistsException;

public interface EComService {

	/** This Method Returns All The Sites Belonging To nodeName.
     * @param nodeName Name Of Node.
     * @return List Of Sites.
     */
    public List<Site> getSitesForNode(String nodeName);

    /** This Method Returns All The Accesses Belonging To site.
     * @param siteId Id Of Site.
     * @return List Of Accesses.
     */
    public List<Access> getAccessesForSite(String siteId);

    /** This Method Returns All The Sites.
     * @return List Of Sites.
     */
    public List<Site> getSites();

    /** This Method Is Used To Get Node Configuration.
     * @param nodeName Name Of Node.
     * @return Node Configuration.
     */
    public NodeConfiguration getNodeConfiguration(String nodeName);

    /** This Method Is Used To Get Site Configuration.
     * @param siteId Id Of Site.
     * @return Site Configuration
     */
    public SiteConfiguration getSiteConfiguration(Long siteId);

    /** This Method Is Used To Update Node Configuration.
     * @param nodeConfiguration New Node Configuration.
     */
    public void updateNodeConfiguration(NodeConfiguration nodeConfiguration);

    /** This Method Is Used To Update Site Configuration.
     * @param siteConfiguration New Site Configuration.
     */
    public void updateSiteConfiguration(SiteConfiguration siteConfiguration);

    /** This method returns codes like WEEK, BIWK, PP, FREE, AICMS depending on supplied codeCategory.
     * @param codeCategory RECURRING_SUBSCRIPTION/NON_RECURRING_SUBSCRIPTION/FREE_SUBSCRIPTION/REGISTERED_APPLICATION.
     * @return List Of Codes.
     */
    public List<Code> getCodes(String codeCategory);

    /** This Method Returns All The Site Details Which Include Information About Site, Term, Merchant, CreditUsageFee,
     * MagensaInfo, And WebPaymentFee.
     * @param siteName Name Of The Site.
     * @return Site.
     * @throws SDLBusinessException
     */
    public Site getSiteDetailsBySiteName(String siteName) throws SDLBusinessException;

    /** This Method Returns ReceiptConfiguration About A Site.
     * @param siteName Name Of The Site.
     * @param paymentType OTC/WEB/Recurring
     * @return List Of ReceiptConfigurations.
     */
    public List<ReceiptConfiguration> getReceiptConfigurationForSiteAndPaymentType(String siteName, PaymentType paymentType);

    /** This Method Refreshes All The Caches.
     *
     */
    public void refreshCache();

    /** This Method Refreshes Cache With cacheName.
     * @param cacheName Name Of The Cache To Be Refreshed.
     */
    public void refreshCacheByName(String cacheName);

    /** This Method Returns All The Cache Names.
     * @return List Of Cache Names.
     */
    public List<String> getCacheNames();

    /** This Method Is Used To Get Merchant Details Of The Site.
     * @param siteId Site Id.
     * @return List Of Merchants.
     */
    public List<Merchant> getMerchantDetailsBySite(Long siteId);

    /** This Method Returns The Term Information Of A Site.
     * @param siteName Name Of The Site.
     * @return Term
     */
    public Term getTerm(String siteName);

    /** This Method Is Used To Delete The Credit Card Information Associated With The User.
     * @param userName Email Id Of Person whose credit card information should be deleted.
     * @throws UserAccountExistsException
     */
    public void deleteCreditCard(String userName) throws UserAccountExistsException;

    /** This Method is used To get the user counts for all Sites.
     *  This will Return the following Records
     *  Key  Description     UserCount
     *  ---  -----------     ---------
     *  4    Dallas County       1000
     *  5    Mobile County       200
     *  The UserCountDTO Key Will Store the Site id
     * @return List Of User Count DTO's.
     */
    public List<UserCountDTO> getUserCountsForAllSite();

    /** This Method is used To get the user count For a Specific Site.
     *  This will Return the following Records
     *  Key  Description     UserCount
     *  ---  -----------     ---------
     *  4    Dallas County       1000
     *
     * The UserCountDTO Key Will Store the Site id
     * @return User Count DTO.
     */
    public UserCountDTO getUserCountForSite(Long siteId);

    /** This Method is used To get the list of User Count for Each Subscription Given a Site Id.
     *
     *  Key  Description                                                   UserCount
     *  ---  -----------                                                  ---------
     *  4    (11-25 Users) Monthly Official Unlimited Record Access        10
     *  5    (2-5 Users) Monthly Unofficial Unlimited Record Access        10
	 *
     * The UserCountDTO Key Will Store the Access id
     * @param siteId
     * @return list of user Count DTO's
     */
    public List<UserCountDTO> getUserCountsBySubForASite(Long siteId);

    /**
   /** This Method is used To get the Subscription Distribution, as how Many people are there in ACTIVE/INACTIVE/OVERIIDEN
    * State
    *
    *  Key  Description   UserCount
    *  ---  -----------   ---------
    *  4    ACTIVE         10
    *  5    INACTIVE       10
	*
    * The UserCountDTO Key Will Store the Access id
    * @param siteId
    * @param accessId
    * @return List of User Count DTO's
    */
    public List<UserCountDTO> getUserDistributionBySubscription(Long siteId, Long accessId);

    public PageRecordsDTO lookupTx(String productId, String productName, String productType, String invoiceId,
    		String txRefNumber, String accountName, String accountNumber, String  transStartDate,String transEndDate,
    		PaymentType paymentType, String siteName, int startFrom, int numberOfRecords) throws SDLBusinessException ;

    public Site getSiteAdminDetails(Long siteId);

    public List<CheckHistory> getCheckHistories(Long siteId, String fromDate, String toDate, String checkNum, Double checkAmt);

    public boolean doVoidCheck(Long checkNumber, String comments);

    public CheckHistory getCheckHistory(Long checkNumber);

    public void saveReceiptConfiguration(ReceiptConfiguration receiptConfiguration);

    public List<ReceiptConfiguration> getReceiptConfigurationsForSite(Long siteId);

    public ReceiptConfiguration getReceiptConfigurationDetail(Long receiptConfigurationId);

    public PageRecordsDTO getErrorLog(String fromDate, String toDate, String userName, int startFromRecord, int numberOfRecords);

    public BankDetails getBankDetailsBySite(Long siteId);

    public void deleteErrorLogContents(Long errorLogId);

    public PaymentType getPaymentTypeForTransaction(String txRefNumber);
}
