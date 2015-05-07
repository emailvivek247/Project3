package com.fdt.ecom.service.rs;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;

import com.fdt.achtx.dto.ACHTxDTO;
import com.fdt.achtx.entity.CheckHistory;
import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.exception.SDLException;
import com.fdt.ecom.dto.UserAccessDetailDTO;
import com.fdt.ecom.dto.UserCountDTO;
import com.fdt.ecom.entity.BankDetails;
import com.fdt.ecom.entity.Location;
import com.fdt.ecom.entity.NodeConfiguration;
import com.fdt.ecom.entity.ReceiptConfiguration;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.SiteConfiguration;
import com.fdt.ecom.entity.enums.PaymentType;
import com.fdt.otctx.entity.OTCTx;
import com.fdt.payasugotx.entity.PayAsUGoTx;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.recurtx.entity.RecurTx;
import com.fdt.security.dto.FirmUserDTO;
import com.fdt.security.dto.SearchCriteriaDTO;
import com.fdt.security.entity.User;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.subscriptions.dto.SubscriptionDTO;
import com.fdt.webtx.entity.WebTx;

public interface EComAdminFacadeServiceRS {

    @WebMethod
    public List<String> getCacheNames();

    @WebMethod
    public Void refreshCacheByName(
        @WebParam(name = "cacheName") String cacheName);

    @WebMethod
    public Void refreshCache();

    @WebMethod
    public User getUserDetailsForAdmin(
        @WebParam(name="userName") String userName) throws UserNameNotFoundException;

    @WebMethod
    public List<Site> getSites();

    @WebMethod
    public Void authorize(
        @WebParam(name="userAccessId") Long userAccessId,
        @WebParam(name="isAuthorized") boolean isAuthorized,
        @WebParam(name="modifiedBy")   String modifiedBy);

    @WebMethod
    public Void enableDisableUserAccess(
        @WebParam(name="userAccessId")       Long userAccessId,
        @WebParam(name="isEnable")           boolean isEnable,
        @WebParam(name="modifiedBy")         String modifiedBy,
        @WebParam(name="comments")           String comments,
        @WebParam(name="isAccessOverridden") boolean isAccessOverridden);

    @WebMethod
    public PayPalDTO removeSubscription(
        @WebParam(name="userName")     String userName,
        @WebParam(name="userAccessId") Long userAccessId,
        @WebParam(name="modifiedBy")   String modifiedBy,
        @WebParam(name="comments")     String comments,
        @WebParam(name="sendEmail")    boolean sendEmail)
        throws PaymentGatewaySystemException, SDLBusinessException;

    @WebMethod
    public NodeConfiguration getNodeConfiguration(
        @WebParam(name="nodeName") String nodeName);

    @WebMethod
    public SiteConfiguration getSiteConfiguration(
        @WebParam(name="siteId") Long siteId);

    @WebMethod
    public Void updateNodeConfiguration(
        @WebParam(name="nodeConfiguration") NodeConfiguration nodeConfiguration);

    @WebMethod
    public Void updateSiteConfiguration(
        @WebParam(name="siteConfiguration") SiteConfiguration siteConfiguration);

    @WebMethod
	public PayPalDTO doReferenceCredit(
			@WebParam(name="txRefNumber") String txRefNumber,
			@WebParam(name="comments") String comments,
			@WebParam(name="modUserId") String modUserId,
			@WebParam(name="machineName") String machineName,
			@WebParam(name="siteName")  String siteName,
			@WebParam(name="paymentType") PaymentType paymentType)
			throws PaymentGatewayUserException, PaymentGatewaySystemException, SDLBusinessException;

    @WebMethod
    public PayPalDTO doPartialReferenceCreditPayAsUGo(
        @WebParam(name="webTxItemId")  Long webTxItemId,
        @WebParam(name="siteName")     String siteName,
        @WebParam(name="comments")     String comments,
        @WebParam(name="modUserId")    String modUserId,
        @WebParam(name="machineName")  String machineName)
        throws PaymentGatewayUserException, PaymentGatewaySystemException, SDLBusinessException;

    @WebMethod
    public PayPalDTO doPartialReferenceCreditWeb(
        @WebParam(name="webTxItemId")  Long webTxItemId,
        @WebParam(name="siteName")     String siteName,
        @WebParam(name="comments")     String comments,
        @WebParam(name="modUserId")    String modUserId,
        @WebParam(name="machineName")  String machineName)
        throws PaymentGatewayUserException, PaymentGatewaySystemException, SDLBusinessException;

    @WebMethod
    public PayAsUGoTx getPayAsUGoTxByTxRefNum(
        @WebParam(name="txRefNumber") String txRefNumber,
        @WebParam(name="siteName")    String siteName);

    @WebMethod
    public WebTx getWebTxByTxRefNum(
        @WebParam(name="txRefNumber") String txRefNumber,
        @WebParam(name="siteName")    String siteName);


    @WebMethod
    public PayAsUGoTx getPayAsUGoTxItemByItemId(
        @WebParam(name="itemId")   Long itemId,
        @WebParam(name="siteName") String siteName);

    @WebMethod
    public WebTx getWebTxItemByItemId(
        @WebParam(name="itemId")   Long itemId,
        @WebParam(name="siteName") String siteName);

    @WebMethod
    public PayAsUGoTx getReferencedPayAsUGoTransactionItemByItemId(
        @WebParam(name="itemId")   Long itemId,
        @WebParam(name="siteName") String siteName);

    @WebMethod
    public WebTx getReferencedWebTransactionItemByItemId(
        @WebParam(name="itemId")   Long itemId,
        @WebParam(name="siteName") String siteName);

    @WebMethod
    public List<PayAsUGoTx> getReferencedPayAsUGoTransaction(
        @WebParam(name="txRefNumber") String txRefNumber,
        @WebParam(name="siteName")    String siteName);

    @WebMethod
    public List<WebTx> getReferencedWebTransaction(
        @WebParam(name="txRefNumber") String txRefNumber,
        @WebParam(name="siteName")    String siteName);

    @WebMethod
    public OTCTx getReferencedOTCTransaction(
        @WebParam(name="txRefNumber") String txRefNumber,
        @WebParam(name="siteName")    String siteName);

    @WebMethod
    public List<ReceiptConfiguration> getReceiptConfigurationForSiteAndPaymentType(
        @WebParam(name="siteName")    String siteName,
        @WebParam(name="paymentType") PaymentType paymentType);

    @WebMethod
    public List<RecurTx> getRecurTXByTxRefNum(
        @WebParam(name="txRefNumber") String txRefNumber,
        @WebParam(name="siteName")    String siteName);

    @WebMethod
    public RecurTx getReferencedRecurringTransactionByTxRefNum(
        @WebParam(name = "originaltxRefNumber") String originaltxRefNumber,
        @WebParam(name="siteName")              String siteName);

    @WebMethod
    public ACHTxDTO doACHTransfer(
		@WebParam(name="siteId")      Long siteId,
        @WebParam(name="paymentType") PaymentType paymentType,
        @WebParam(name="machineIp") String machineIp,
        @WebParam(name="createdBy")   String createdBy) throws SDLException, SDLBusinessException ;

    @WebMethod
    public ACHTxDTO getACHDetailsForTransfer(
        @WebParam(name="siteId")      Long siteId,
        @WebParam(name="paymentType") PaymentType paymentType,
        @WebParam(name="machineIp") String machineIp,
        @WebParam(name="createdBy")   String createdBy) throws SDLException ;

    @WebMethod
    public Void archiveUser(
        @WebParam(name = "userName") String userName,
        @WebParam(name = "comments") String comments,
        @WebParam(name = "modifiedBy") String modifiedBy,
        @WebParam(name = "machineName") String machineName) throws SDLBusinessException;

    @WebMethod
    public List<RecurTx> getRecurTxBySite(@WebParam(name = "siteName") String siteName);

    @WebMethod
    public List<PayAsUGoTx> getPayAsUGoTransactions(@WebParam(name = "userName") String userName,
    		@WebParam(name = "siteId") Long siteId);

    @WebMethod
    public List<RecurTx> getRecurTxByUser(@WebParam(name = "userName") String userName);

    /**
     * This method returns users for a pagination.
     * Search Critieria has two properties(numberOfRecords, recordCount) that indicates the how many records to be returned and which row to start from.
     *
     * @param searchCriteria
     * @return
     */
    @WebMethod
    public PageRecordsDTO findUsers(@WebParam(name="searchCriteria") SearchCriteriaDTO searchCriteria);

	/**
	 *
	 * @param userName
	 * @param siteName
	 * @return
	 */
    public List<SubscriptionDTO> getUserInfoForAdmin(@WebParam(name="userName") String userName,
    	@WebParam(name="siteName")String siteName);

    @WebMethod
    public List<UserCountDTO> getUserCountsForAllSite();

    @WebMethod
    public UserCountDTO getUserCountForSite(@WebParam(name = "siteId") Long siteId);

    @WebMethod
    public List<UserCountDTO> getUserCountsBySubForASite(@WebParam(name = "siteId") Long siteId);

    @WebMethod
    public List<UserCountDTO> getUserDistributionBySubscription(@WebParam(name = "siteId") Long siteId,
    	@WebParam(name = "accessId")Long accessId);

    @WebMethod
    public PageRecordsDTO lookupTx(@WebParam(name = "txRefNumber")  String txRefNumber,
    		@WebParam(name = "productId")  String productId,
    		@WebParam(name = "productName")  String productName,
    		@WebParam(name = "productType")  String productType,
    		@WebParam(name = "invoiceId")  String invoiceId,
    		@WebParam(name = "accountName") String accountName,
    		@WebParam(name = "accountNumber") String accountNumber,
    		@WebParam(name = "transStartDate") String transStartDate,
    		@WebParam(name = "transEndDate") String transEndDate,
    		@WebParam(name = "paymentType") PaymentType paymentType,
    		@WebParam(name = "siteName") String siteName,
    		@WebParam(name = "startFrom") int startFrom,
    		@WebParam(name = "numberOfRecords") int numberOfRecords) throws SDLBusinessException;
    		

    @WebMethod
    public Site getSiteAdminDetails(@WebParam(name = "siteId") Long siteId);

    @WebMethod
    public List<CheckHistory> getCheckHistories(@WebParam(name = "siteId") Long siteId,
    		@WebParam(name = "fromDate") String fromDate,
    		@WebParam(name = "toDate") String toDate,
    		@WebParam(name = "checkNum") String checkNum,
    		@WebParam(name = "checkAmt") Double checkAmt);

    @WebMethod
    public boolean doVoidCheck(@WebParam(name = "checkNumber") Long checkNumber,
    		@WebParam(name = "comments") String comments);

    @WebMethod
    public CheckHistory getCheckHistory(@WebParam(name = "checkNumber") Long checkNumber);

    @WebMethod
    public void saveReceiptConfiguration(@WebParam(name = "receiptConfiguration") ReceiptConfiguration receiptConfiguration);

    @WebMethod
    public List<ReceiptConfiguration> getReceiptConfigurationsForSite(@WebParam(name = "siteId") Long siteId);

    @WebMethod
    public ReceiptConfiguration getReceiptConfigurationDetail(
    		@WebParam(name = "receiptConfigurationId") Long receiptConfigurationId);

    @WebMethod
    public PageRecordsDTO getErrorLog(@WebParam(name = "fromDate") String fromDate,
    		@WebParam(name = "toDate") String toDate,
    		@WebParam(name = "userName") String userName,
    		@WebParam(name = "startFromRecord") Integer startFromRecord,
    		@WebParam(name = "numberOfRecords") Integer numberOfRecords
    		);

    @WebMethod
    public UserAccessDetailDTO getUserAccessDetails(@WebParam(name = "userAccessId") Long userAccessId);

    @WebMethod
    public BankDetails getBankDetailsBySite(@WebParam(name = "siteId") Long siteId);

    @WebMethod
    public void deleteErrorLogContents(@WebParam(name = "errorLogId") Long errorLogId);

    @WebMethod
    public List<FirmUserDTO> getFirmUsersbySubscriptionAndUserName(@WebParam(name = "userName") String userName,
    		                           @WebParam(name = "accessId") Long accessId);

	@WebMethod
	public void ChangeFirmSubscriptionAdministrator(
			@WebParam(name="newAdminUserName") String newAdminUserName,
			@WebParam(name="accessId") Long accessId,
			@WebParam(name="comments") String comments,
			@WebParam(name="modifiedBy") String modifiedBy) throws UserNameNotFoundException, SDLBusinessException;

	@WebMethod
	public Location getLocationSignatureById(@WebParam(name = "locationId") Long locationId);

	@WebMethod
	public Location getLocationSealById(@WebParam(name = "locationId") Long locationId);
}