package com.fdt.common.util.rest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdt.achtx.dto.ACHTxDTO;
import com.fdt.achtx.entity.CheckHistory;
import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.entity.Tx;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.exception.SDLException;
import com.fdt.common.exception.rs.ExceptionHandler;
import com.fdt.common.util.SystemUtil;
import com.fdt.ecom.dto.UserAccessDetailDTO;
import com.fdt.ecom.dto.UserCountDTO;
import com.fdt.ecom.entity.BankDetails;
import com.fdt.ecom.entity.Code;
import com.fdt.ecom.entity.CreditCard;
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
import com.fdt.security.dto.EnableDisableFirmAccessRequestDTO;
import com.fdt.security.dto.FirmUserDTO;
import com.fdt.security.dto.RemoveFirmLevelAccessRequestDTO;
import com.fdt.security.dto.SearchCriteriaDTO;
import com.fdt.security.entity.Access;
import com.fdt.security.entity.User;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.subscriptions.dto.AccessDetailDTO;
import com.fdt.subscriptions.dto.SubscriptionDTO;
import com.fdt.webtx.entity.WebTx;

/**
 * This class is a Stub to called REST WebServices
 *
 */

@Component("serviceStub")
@SuppressWarnings("unchecked")
public class ServiceStubRS {

	@Autowired
	private RestTemplate restTemplate = null;

	@Autowired
	private ObjectMapper jacksonObjectMapper = null;

	@Value("${ecom.facadeservice.resturl}")
	private String ecomRestURL = null;

	@Value("${ecomadmin.facadeservice.resturl}")
	private String ecomAdminRestUrl = null;

	@Value("${externalService.facadeservice.resturl}")
	private String externalServiceRestUrl = null;

	private ExceptionHandler exceptionHandler = new ExceptionHandler();

	public List<String> getCacheNames() {
		String url = this.ecomAdminRestUrl.concat("getCacheNames");
		List<String> cacheNameList = restTemplate.getForObject(url, List.class);
		return cacheNameList;
	}

	public void refreshCacheByName(String cacheName) {
		String url = this.ecomAdminRestUrl.concat("refreshCacheByName/{cacheName}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("cacheName", cacheName);
		restTemplate.getForObject(url, Void.class, paramMap);
	}

	public void refreshCache() {
		String url = this.ecomAdminRestUrl.concat("refreshCache");
		restTemplate.getForObject(url, Void.class);
	}

	public User getUserDetailsForAdmin(String userName) throws UserNameNotFoundException {
		String url = this.ecomAdminRestUrl.concat("getUserDetailsForAdmin/{userName}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		User user = null;
		try {
			user = restTemplate.getForObject(url, User.class,paramMap);
		}
		catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleUserNameNotFoundException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}

		return user;
	}

	public List<Site> getSites() {
		String url = this.ecomAdminRestUrl.concat("getSites");
		Site[] siteArray = restTemplate.getForObject(url, Site[].class);
		List<Site> siteList = Arrays.asList(siteArray);
		return siteList;
	}

	public void authorize(Long userAccessId, boolean isAuthorized, String modifiedBy) {
		String url = this.ecomAdminRestUrl.concat("authorize/{userAccessId}/{isAuthorized}/{modifiedBy}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userAccessId", userAccessId);
		paramMap.put("isAuthorized", isAuthorized);
		paramMap.put("modifiedBy", modifiedBy);
		restTemplate.postForObject(url, null, Void.class, paramMap);
	}

	public void enableDisableUserAccess(Long userAccessId, boolean isEnable, String modifiedBy, String comments,
		boolean isAccessOverridden, String endDate) {
		String url = this.ecomAdminRestUrl.concat("enableDisableUserAccess/{userAccessId}/{isEnable}/{modifiedBy}/" +
			"{comments}/{isAccessOverridden}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userAccessId", userAccessId);
		paramMap.put("isEnable", isEnable);
		paramMap.put("modifiedBy", modifiedBy);
		paramMap.put("comments", SystemUtil.encodeURL(comments));
		paramMap.put("isAccessOverridden", isAccessOverridden);
		if(!StringUtils.isBlank(endDate)){
			url = url + "?endDate=" + endDate;
		}
		restTemplate.postForObject(url, null, Void.class, paramMap);
	}

	public PayPalDTO removeSubscription(String userName, Long userAccessId, String modifiedBy, String comments,
			boolean isSendEmail) throws PaymentGatewaySystemException, SDLBusinessException {
		PayPalDTO payPalDTO = null;
		String url = this.ecomAdminRestUrl.concat("removeSubscription/{userName}/{userAccessId}/" +
			"{modifiedBy}/{comments}/{isSendEmail}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("userAccessId", userAccessId);
		paramMap.put("modifiedBy", modifiedBy);
		paramMap.put("comments", SystemUtil.encodeURL(comments));
		paramMap.put("isSendEmail", isSendEmail);
		try {
			payPalDTO = restTemplate.postForObject(url, null, PayPalDTO.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handlePaymentGatewaySystemException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleSDLBusinessException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
		return payPalDTO;
	}

	public NodeConfiguration getNodeConfiguration(String nodeName) {
		String url = this.ecomAdminRestUrl.concat("getNodeConfiguration/{nodeName}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("nodeName", nodeName);
		NodeConfiguration nodeConfiguration = restTemplate.getForObject(url, NodeConfiguration.class, paramMap);
		return nodeConfiguration;
	}

	public SiteConfiguration getSiteConfiguration(Long siteId) {
		String url = this.ecomAdminRestUrl.concat("getSiteConfiguration/{siteId}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("siteId", siteId);
		SiteConfiguration siteConfiguration = restTemplate.getForObject(url, SiteConfiguration.class, paramMap);
		return siteConfiguration;
	}

	public void updateNodeConfiguration(NodeConfiguration nodeConfiguration) {
		String url = this.ecomAdminRestUrl.concat("updateNodeConfiguration");
		restTemplate.postForObject(url, nodeConfiguration, Void.class);
	}

	public void updateSiteConfiguration(SiteConfiguration siteConfiguration) {
		String url = this.ecomAdminRestUrl.concat("updateSiteConfiguration");
		restTemplate.postForObject(url, siteConfiguration, Void.class);
	}

	public PayPalDTO doReferenceCredit(String txRefNumber, String comments, String modUserId, String machineName,
			String siteName, PaymentType paymentType) {
		PayPalDTO payPalDTO = null;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("paymentType", paymentType);
		String url = this.ecomAdminRestUrl.concat("doReferenceCredit/{paymentType}?");
		if(!StringUtils.isBlank(txRefNumber)){
			url = url + "txRefNumber=" + txRefNumber;
		}
		if(!StringUtils.isBlank(comments)){
			url = url + "&comments=" + comments;
		}
		if(!StringUtils.isBlank(modUserId)){
			url = url + "&modUserId=" + modUserId;
		}
		if(!StringUtils.isBlank(machineName)){
			url = url + "&machineName=" + machineName;
		}
		if(!StringUtils.isBlank(siteName)){
			url = url + "&siteName=" + siteName;
		}
		payPalDTO = restTemplate.postForObject(url, null, PayPalDTO.class, paramMap);
		return payPalDTO;
	}

	public PayPalDTO doPartialReferenceCredit(Long webTxItemId, String siteName, String comments,
		String modUserId, String machineName, PaymentType paymentChannel) throws PaymentGatewayUserException, PaymentGatewaySystemException,
		SDLBusinessException {
		PayPalDTO payPalDTO = null;
		String url = "";
		if (paymentChannel.equals(PaymentType.PAYASUGO)) {
			url = this.ecomAdminRestUrl.concat("doPartialReferenceCreditPayAsUGo?");
		} else if (paymentChannel.equals(PaymentType.WEB)){
			url = this.ecomAdminRestUrl.concat("doPartialReferenceCreditWeb?");
		} // It shou
		if(!StringUtils.isBlank(modUserId)) {
			url = url + "modUserId=" + modUserId;
		}
		if(webTxItemId != null) {
			url = url + "&webTxItemId=" + webTxItemId;
		}
		if(!StringUtils.isBlank(siteName)) {
			url = url + "&siteName=" + siteName;
		}
		if(!StringUtils.isBlank(comments)) {
			url = url + "&comments=" + comments;
		}
		if(!StringUtils.isBlank(machineName)) {
			url = url + "&machineName=" + machineName;
		}
		try {
			payPalDTO = restTemplate.postForObject(url, null, PayPalDTO.class);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handlePaymentGatewayUserException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handlePaymentGatewaySystemException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleSDLBusinessException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
		return payPalDTO;
	}


	public PayAsUGoTx getPayAsUGoTransactionByTxRefNum(String txRefNumber, String siteName) {
		PayAsUGoTx payAsUGoTransaction = null;
		String url = this.ecomAdminRestUrl.concat("getPayAsUGoTransactionByTxRefNum?txRefNumber=" + txRefNumber);
		if(!StringUtils.isBlank(siteName)) {
			url = url + "&siteName=" + siteName;
		}
		payAsUGoTransaction = restTemplate.getForObject(url, PayAsUGoTx.class);
		return payAsUGoTransaction;
	}

	public PayAsUGoTx getPayAsUGoTransactionItemByItemId(Long itemId, String siteName) {
		PayAsUGoTx payAsUGoTransaction = null;
		String url = this.ecomAdminRestUrl.concat("getPayAsUGoTransactionItemByItemId?itemId=" + itemId);
		if(!StringUtils.isBlank(siteName)) {
			url = url + "&siteName=" + siteName;
		}
		payAsUGoTransaction = restTemplate.getForObject(url, PayAsUGoTx.class);
		return payAsUGoTransaction;
	}

	public PayAsUGoTx getReferencedPayAsUGoTransactionItemByItemId(Long itemId, String siteName) {
		PayAsUGoTx payAsUGoTransaction = null;
		String url = this.ecomAdminRestUrl.concat("getReferencedPayAsUGoTransactionItemByItemId?itemId=" + itemId);
		if(!StringUtils.isBlank(siteName)) {
			url = url + "&siteName=" + siteName;
		}
		payAsUGoTransaction = restTemplate.getForObject(url, PayAsUGoTx.class);
		return payAsUGoTransaction;
	}

	public List<PayAsUGoTx> getReferencedPayAsUGoTransaction(String txRefNumber, String siteName) {
		String url = this.ecomAdminRestUrl.concat("getReferencedPayAsUGoTransaction?txRefNumber=" + txRefNumber);
		if(!StringUtils.isBlank(siteName)) {
			url = url + "&siteName=" + siteName;
		}
		PayAsUGoTx[] payAsUGoTransactions = restTemplate.getForObject(url, PayAsUGoTx[].class);
		List<PayAsUGoTx> payAsUGoTransactionsList = Arrays.asList(payAsUGoTransactions);
		return payAsUGoTransactionsList;
	}


	public WebTx getWebTxByTxRefNum(String txRefNumber, String siteName) {
		WebTx webTransaction = null;
		String url = this.ecomAdminRestUrl.concat("getWebTxByTxRefNum?txRefNumber=" + txRefNumber);
		if(!StringUtils.isBlank(siteName)) {
			url = url + "&siteName=" + siteName;
		}
		webTransaction = restTemplate.getForObject(url, WebTx.class);
		return webTransaction;
	}

	public WebTx getWebTxItemByItemId(Long itemId, String siteName) {
		WebTx webTransaction = null;
		String url = this.ecomAdminRestUrl.concat("getWebTxItemByItemId?itemId=" + itemId);
		if(!StringUtils.isBlank(siteName)) {
			url = url + "&siteName=" + siteName;
		}
		webTransaction = restTemplate.getForObject(url, WebTx.class);
		return webTransaction;
	}

	public WebTx getReferencedWebTxItemByItemId(Long itemId, String siteName) {
		WebTx webTransaction = null;
		String url = this.ecomAdminRestUrl.concat("getReferencedWebTransactionItemByItemId?itemId=" + itemId);
		if(!StringUtils.isBlank(siteName)) {
			url = url + "&siteName=" + siteName;
		}
		webTransaction = restTemplate.getForObject(url, WebTx.class);
		return webTransaction;
	}

	public List<WebTx> getReferencedWebTx(String txRefNumber, String siteName) {
		String url = this.ecomAdminRestUrl.concat("getReferencedWebTransaction?txRefNumber=" + txRefNumber);
		if(!StringUtils.isBlank(siteName)) {
			url = url + "&siteName=" + siteName;
		}
		WebTx[] webTransactions= restTemplate.getForObject(url, WebTx[].class);
		List<WebTx> webTransactionsList = Arrays.asList(webTransactions);
		return webTransactionsList;
	}


	public OTCTx getReferencedOTCTransaction(String txRefNumber, String siteName) {
		OTCTx oTCTransaction = null;
		String url = this.ecomAdminRestUrl.concat("getReferencedOTCTransaction?txRefNumber=" + txRefNumber);
		if(!StringUtils.isBlank(siteName)) {
			url = url + "&siteName=" + siteName;
		}
		oTCTransaction = restTemplate.getForObject(url, OTCTx.class);
		return oTCTransaction;
	}

	public List<RecurTx> getRecurringTransactionByTxRefNum(String originaltxRefNumber, String siteName) {
		String url = this.ecomAdminRestUrl.concat("getRecurringTransactionByTxRefNum?originaltxRefNumber="
			+ originaltxRefNumber);
		if(!StringUtils.isBlank(siteName)) {
			url = url + "&siteName=" + siteName;
		}
		RecurTx[] recurTransactions = restTemplate.getForObject(url, RecurTx[].class);
		List<RecurTx> recurTransactionList = Arrays.asList(recurTransactions);
		return recurTransactionList;
	}

	public RecurTx getReferencedRecurringTransactionByTxRefNum(String originaltxRefNumber, String siteName) {
		RecurTx recurTransaction = null;
		String url = this.ecomAdminRestUrl.concat("getReferencedRecurringTransactionByTxRefNum?originaltxRefNumber="
			+ originaltxRefNumber);
		if(!StringUtils.isBlank(siteName)) {
			url = url + "&siteName=" + siteName;
		}
		recurTransaction = restTemplate.getForObject(url, RecurTx.class);
		return recurTransaction;
	}

	public ACHTxDTO doACHTransfer(PaymentType paymentType, Long siteId, String machineIp, String createdBy)
			throws SDLBusinessException, SDLException {
		ACHTxDTO achDto = null;
		String url = this.ecomAdminRestUrl.concat("doACHTransfer/{siteId}/{paymentType}/{createdBy}/{machineIp}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("siteId", siteId);
		paramMap.put("paymentType", paymentType);
		paramMap.put("createdBy", createdBy);
		paramMap.put("machineIp", machineIp);

		try {
			achDto = restTemplate.postForObject(url, null, ACHTxDTO.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleUnknownHttpStatusCodeException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
		return achDto;
	}

	public ACHTxDTO getACHDetailsForTransfer(Long siteId, PaymentType paymentType, String machineIp, String createdBy)
			throws SDLBusinessException, SDLException {
		ACHTxDTO achDto = null;
		String url = this.ecomAdminRestUrl.concat("getACHDetailsForTransfer/{siteId}/{paymentType}/{machineIp}/{createdBy}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("siteId", siteId);
		paramMap.put("paymentType", paymentType);
		paramMap.put("machineIp", machineIp);
		paramMap.put("createdBy", createdBy);
		try {
			achDto = restTemplate.postForObject(url, null, ACHTxDTO.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleUnknownHttpStatusCodeException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
		return achDto;
	}

	public void archiveUser(String userName, String comments, String modifiedBy, String machineName)
		throws SDLBusinessException {
		String url = this.ecomAdminRestUrl.concat("archiveUser/{userName}/{comments}/{modifiedBy}/{machineName}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("comments", SystemUtil.encodeURL(comments));
		paramMap.put("modifiedBy", modifiedBy);
		paramMap.put("machineName", machineName);
		try {
			restTemplate.postForObject(url, null, Void.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleSDLBusinessException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
	}

	public void lockUnLockUser(String userName, boolean isLock, String modifiedBy, boolean isSendUserConfirmation,
	     String nodeName, String additionalComments) {
		String url = this.ecomRestURL.concat("lockUnLockUser");
		url = url + "?isLock=" + isLock;
		if(!StringUtils.isBlank(userName)) {
			url = url + "&userName=" + userName;
		}
		if(!StringUtils.isBlank(modifiedBy)) {
			url = url + "&modifiedBy=" + modifiedBy;
		}
		url = url + "&isSendUserConfirmation=" + isSendUserConfirmation;
		if(!StringUtils.isBlank(nodeName)) {
			url = url + "&nodeName=" + nodeName;
		}
		if(!StringUtils.isBlank(additionalComments)) {
			url = url + "&additionalComments=" + SystemUtil.encodeURL(additionalComments);
		}
		restTemplate.postForObject(url, null, Void.class);
	}

	public List<Access> getAccessesForSite(String siteId) {
		String url = this.ecomRestURL.concat("getAccessesForSite/{siteId}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("siteId", siteId);
		Access[] accessArray = restTemplate.getForObject(url, Access[].class, paramMap);
		List<Access> accessList = Arrays.asList(accessArray);
		return accessList;
	}

	public void updateExistingCreditCardInformation(String userName, String modifiedBy, CreditCard newCreditCardInformation) throws
	PaymentGatewayUserException, PaymentGatewaySystemException {
		String url = this.ecomRestURL.concat("updateExistingCreditCardInformation/{userName}/{modifiedBy}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("modifiedBy", modifiedBy);
		try {
			restTemplate.postForObject(url, newCreditCardInformation, Void.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handlePaymentGatewayUserException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handlePaymentGatewaySystemException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
	}

	public List<SubscriptionDTO> getUserSubscriptions(String userName, String nodeName, String siteName) {
		String url = this.ecomRestURL.concat("getUserSubscriptions?userName=" + userName);
		if(!StringUtils.isBlank(nodeName)) {
			url = url + "&nodeName=" + nodeName;
		}
		if(!StringUtils.isBlank(siteName)) {
			url = url + "&siteName=" + siteName;
		}
		url = url + "&activeSubscriptionsOnly=false";
		url = url + "&firmAdminSubscriptionsOnly=false";

		SubscriptionDTO[] subscriptionDTOArray= restTemplate.getForObject(url, SubscriptionDTO[].class);
		List<SubscriptionDTO> subscriptionDTOList = Arrays.asList(subscriptionDTOArray);
		return subscriptionDTOList;
	}

	public PayPalDTO cancelSubscription(String userName, Long userAccessId) throws PaymentGatewaySystemException,
		SDLBusinessException {
		PayPalDTO payPalDTO = null;
		String url = this.ecomRestURL.concat("cancelSubscription/{userName}/{userAccessId}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("userAccessId", userAccessId);
		try {
			payPalDTO = restTemplate.postForObject(url, null, PayPalDTO.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleSDLBusinessException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
		return payPalDTO;
	}

	public List<AccessDetailDTO> addSubscription(SubscriptionDTO subscriptionDTO)
		throws SDLBusinessException {

		List<AccessDetailDTO> accessDetailDTOList = null;
		String url = this.ecomRestURL.concat("addSubscription");
		try {
			AccessDetailDTO[] accessDetailDTOArray = restTemplate.postForObject(url, subscriptionDTO, AccessDetailDTO[].class);
		    accessDetailDTOList = Arrays.asList(accessDetailDTOArray);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleSDLBusinessException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
		return accessDetailDTOList;
	}

	public OTCTx getOTCTransactionByTxRefNum(String txRefNumber, String siteName) {
		String url = this.externalServiceRestUrl.concat("getOTCTransactionByTxRefNum?txRefNumber=" + txRefNumber);
		if(!StringUtils.isBlank(siteName)) {
			url = url + "&siteName=" + siteName;
		}
		OTCTx oTCTransaction = restTemplate.getForObject(url, OTCTx.class);
		return oTCTransaction;
	}

	public CreditCard getCreditCardDetails(Long userId) {
		CreditCard creditCard = null;
		String url = this.ecomRestURL.concat("getCreditCardDetails/{userId}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userId", userId);
		creditCard = restTemplate.getForObject(url, CreditCard.class, paramMap);
		return creditCard;
	}

	public CreditCard getCreditCardDetailsByUserName(String userName) {
		CreditCard creditCard = null;
		String url = this.ecomRestURL.concat("getCreditCardDetailsByUserName/{userName}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		creditCard = restTemplate.getForObject(url, CreditCard.class, paramMap);
		return creditCard;
	}

	public List<RecurTx> getRecurTransactions(String userName, Long siteId) {
		List<RecurTx> recurTransactionList = null;
		String url = this.ecomAdminRestUrl.concat("getRecurTransactions/{userName}?");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		if(siteId != null) {
			url = url + "siteId=" + siteId;
		}
		RecurTx[] recurTransactionArray = restTemplate.getForObject(url, RecurTx[].class, paramMap);
		recurTransactionList = Arrays.asList(recurTransactionArray);
		return recurTransactionList;
	}

	public List<PayAsUGoTx> getPayAsUGoTransactions(String userName, Long siteId) {
		List<PayAsUGoTx> payAsUGoTransactionList = null;
		String url = this.ecomAdminRestUrl.concat("getPayAsUGoTransactions?userName=" + userName);
		if(siteId != null) {
			url = url + "&siteId=" + siteId;
		}
		PayAsUGoTx[] payAsUGoTransactionArray = restTemplate.getForObject(url, PayAsUGoTx[].class);
		payAsUGoTransactionList = Arrays.asList(payAsUGoTransactionArray);
		return payAsUGoTransactionList;
	}

	public List<FirmUserDTO> getFirmUsersbySubscriptionAndUserName(String userName, Long accessId) {
		List<FirmUserDTO> listFirmUsers = null;
		String url = this.ecomAdminRestUrl.concat("getFirmUsersbySubscriptionAndUserName/{userName}/{accessId}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("accessId", accessId);
		FirmUserDTO[] listFirmUsersArray = restTemplate.getForObject(url, FirmUserDTO[].class, paramMap);
		listFirmUsers = Arrays.asList(listFirmUsersArray);
		return listFirmUsers;
	}


	/**
     * This method returns users for a pagination.
     * Search Critieria has two properties(numberOfRecords, recordCount) that indicates the how many records to be returned and which row to start from.
     *
     * @param searchCriteria
     * @return
     */
    public PageRecordsDTO findUsers(SearchCriteriaDTO searchCriteria){
		String url = this.ecomAdminRestUrl.concat("findUsers");
		PageRecordsDTO pageRecords = restTemplate.postForObject(url, searchCriteria, PageRecordsDTO.class);

		try{
			String jsonString = jacksonObjectMapper.writeValueAsString(pageRecords.getRecords());
			User[] list = jacksonObjectMapper.readValue(jsonString, User[].class);
			pageRecords.setRecords(Arrays.asList(list));
		}catch(Exception e){

		}
		return pageRecords;
    }

	/**
	 *
	 * @param userName
	 * @param siteName
	 * @return
	 */
    public List<SubscriptionDTO> getUserInfoForAdmin(String userName, String siteName){
		String url = this.ecomAdminRestUrl.concat("getUserInfoForAdmin?userName=" + userName);
		if(!StringUtils.isBlank(siteName)) {
			url = url + "&siteName=" + siteName;
		}
		SubscriptionDTO[] subscriptions = restTemplate.getForObject(url, SubscriptionDTO[].class);
		return Arrays.asList(subscriptions);
    }


	public List<UserCountDTO> getUserCountsForAllSite() {
		List<UserCountDTO> userCountDTOList = null;
		String url = this.ecomAdminRestUrl.concat("getUserCountsForAllSite");
		UserCountDTO[] userCountDTOArray = restTemplate.getForObject(url, UserCountDTO[].class);
		userCountDTOList = Arrays.asList(userCountDTOArray);
		return userCountDTOList;

	}

	public UserCountDTO getUserCountForSite(Long siteId) {
		UserCountDTO userCountDTO = null;
		String url = this.ecomAdminRestUrl.concat("getUserCountForSite/{siteId}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("siteId", siteId);
		userCountDTO = restTemplate.getForObject(url, UserCountDTO.class, paramMap);
		return userCountDTO;
	}

	public List<UserCountDTO> getUserCountsBySubForASite(Long siteId) {
		List<UserCountDTO> userCountDTOList = null;
		String url = this.ecomAdminRestUrl.concat("getUserCountsBySubForASite/{siteId}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("siteId", siteId);
		UserCountDTO[] userCountDTOArray = restTemplate.getForObject(url, UserCountDTO[].class, paramMap);
		userCountDTOList = Arrays.asList(userCountDTOArray);
		return userCountDTOList;
	}


	public List<UserCountDTO> getUserDistributionBySubscription(Long siteId, Long accessId) {
		List<UserCountDTO> userCountDTOList = null;
		String url = this.ecomAdminRestUrl.concat("getUserDistributionBySubscription/{siteId}/{accessId}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("siteId", siteId);
		paramMap.put("accessId", accessId);
		UserCountDTO[] userCountDTOArray = restTemplate.getForObject(url, UserCountDTO[].class, paramMap);
		userCountDTOList = Arrays.asList(userCountDTOArray);
		return userCountDTOList;
	}

	public PageRecordsDTO lookupTx(String productId, String productName, String productType, String invoiceId,
			String txRefNumber, String accountName, String accountNumber, String transStartDate,
			String transEndDate, String paymentChannel, String siteName, int startFrom, int numberOfRecords) {
		String url = this.ecomAdminRestUrl.concat("lookupTx?paymentType=" + paymentChannel + "&startFrom=" + startFrom + "&numberOfRecords=" + numberOfRecords);
		if(!StringUtils.isBlank(txRefNumber)) {
			url = url + "&txRefNumber=" + txRefNumber;
		}
		if(!StringUtils.isBlank(productId)) {
			url = url + "&productId=" + productId;
		}
		if(!StringUtils.isBlank(productName)) {
			url = url + "&productName=" + productName;
		}
		if(!StringUtils.isBlank(productType)) {
			url = url + "&productType=" + productType;
		}
		if(!StringUtils.isBlank(invoiceId)) {
			url = url + "&invoiceId=" + invoiceId;
		}
		if(!StringUtils.isBlank(accountName)) {
			url = url + "&accountName=" + accountName;
		}
		if(!StringUtils.isBlank(accountNumber)) {
			url = url + "&accountNumber=" + accountNumber;
		}
		if(!StringUtils.isBlank(transStartDate)) {
			url = url + "&transStartDate=" + transStartDate;
		}
		if(!StringUtils.isBlank(transEndDate)) {
			url = url + "&transEndDate=" + transEndDate;
		}
		if(!StringUtils.isBlank(siteName)) {
			url = url + "&siteName=" + siteName;
		}
		PageRecordsDTO pageRecords = restTemplate.getForObject(url, PageRecordsDTO.class);
		try{
			String jsonString = jacksonObjectMapper.writeValueAsString(pageRecords.getRecords());
			Tx[] list = jacksonObjectMapper.readValue(jsonString, Tx[].class);
			pageRecords.setRecords(Arrays.asList(list));
		}catch(Exception e){

		}
		return pageRecords;
	}

    public Site getSiteAdminDetails(Long siteId) {
    	Site site = null;
    	String url = this.ecomAdminRestUrl.concat("getSiteAdminDetails/{siteId}");
    	Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("siteId", siteId);
		site = restTemplate.getForObject(url, Site.class, paramMap);
		return site;
    }

    public List<CheckHistory> getCheckHistories(Long siteId, String fromDate, String toDate, String checkNum, Double checkAmt) {
    	List<CheckHistory> checkHistories = null;
    	String url = this.ecomAdminRestUrl.concat("getCheckHistories?fromDate=" + fromDate);

		if(!StringUtils.isBlank(toDate)) {
			url = url + "&toDate=" + toDate;
		}
		if(!StringUtils.isBlank(checkNum)) {
			url = url + "&checkNum=" + checkNum;
		}
		if(siteId != null) {
			url = url + "&siteId=" + siteId;
		}
		if(checkAmt != null) {
			url = url + "&checkAmt=" + checkAmt;
		}

		CheckHistory[] checkHistoryArray = restTemplate.getForObject(url, CheckHistory[].class);
		checkHistories = Arrays.asList(checkHistoryArray);
		return checkHistories;
    }

    public boolean doVoidCheck(Long checkNumber, String comments) {
    	boolean isCheckVoided = false;
    	String url = this.ecomAdminRestUrl.concat("doVoidCheck/{checkNumber}/{comments}");
    	Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("checkNumber", checkNumber);
		paramMap.put("comments", comments);
		isCheckVoided = restTemplate.getForObject(url, boolean.class, paramMap);
    	return isCheckVoided;
    }

    public CheckHistory getCheckHistory(Long checkNumber) {
    	CheckHistory checkHistory = null;
    	String url = this.ecomAdminRestUrl.concat("getCheckHistory/{checkNumber}");
    	Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("checkNumber", checkNumber);
		checkHistory = restTemplate.getForObject(url, CheckHistory.class, paramMap);
    	return checkHistory;
    }

    public void saveReceiptConfiguration(ReceiptConfiguration receiptConfiguration) {
    	String url = this.ecomAdminRestUrl.concat("saveReceiptConfiguration");
		restTemplate.postForObject(url, receiptConfiguration, Void.class);
    }

    public List<ReceiptConfiguration> getReceiptConfigurationsForSite(Long siteId) {
    	List<ReceiptConfiguration> receiptConfigurations = null;
    	String url = this.ecomAdminRestUrl.concat("getReceiptConfigurationsForSite/{siteId}");
    	Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("siteId", siteId);
    	ReceiptConfiguration[] receiptConfigurationArray = restTemplate.getForObject(url, ReceiptConfiguration[].class, paramMap);
    	receiptConfigurations = Arrays.asList(receiptConfigurationArray);
		return receiptConfigurations;

    }

    public ReceiptConfiguration getReceiptConfigurationDetail(Long receiptConfigurationId){
    	ReceiptConfiguration receiptConfiguration = null;
    	String url = this.ecomAdminRestUrl.concat("getReceiptConfigurationDetail/{receiptConfigurationId}");
    	Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("receiptConfigurationId", receiptConfigurationId);
		receiptConfiguration = restTemplate.getForObject(url, ReceiptConfiguration.class, paramMap);
    	return receiptConfiguration;

    }

    public PageRecordsDTO getErrorLog(String fromDate, String toDate, String userName, Integer startFromRecord, Integer numberOfRecords){
    	String url = this.ecomAdminRestUrl.concat("getErrorLog?");
    	fromDate = StringUtils.isBlank(fromDate) ? "" : fromDate;
    	toDate = StringUtils.isBlank(toDate) ? "" : toDate;
    	userName = StringUtils.isBlank(userName) ? "" : userName;
		url = url + "fromDate=" +  fromDate;
		url = url + "&toDate=" +  toDate;
		url = url + "&userName=" +  userName;
		url = url + "&startFromRecord=" + startFromRecord + "&numberOfRecords=" + numberOfRecords;

		return restTemplate.getForObject(url, PageRecordsDTO.class);
    }

    public UserAccessDetailDTO getUserAccessDetails(Long userAccessId){
    	UserAccessDetailDTO userAccessDetailDTO = null;
    	String url = this.ecomAdminRestUrl.concat("getUserAccessDetails/{userAccessId}");
    	Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userAccessId", userAccessId);
		userAccessDetailDTO = restTemplate.getForObject(url, UserAccessDetailDTO.class, paramMap);
    	return userAccessDetailDTO;

    }

    public BankDetails getBankDetailsBySite(Long siteId){
    	BankDetails bankDetails = null;
    	String url = this.ecomAdminRestUrl.concat("getBankDetailsBySite/{siteId}");
    	Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("siteId", siteId);
		bankDetails = restTemplate.getForObject(url, BankDetails.class, paramMap);
    	return bankDetails;
    }

    public void deleteErrorLogContents(Long errorLogId){
    	String url = this.ecomAdminRestUrl.concat("deleteErrorLogContents/{errorLogId}");
    	Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("errorLogId", errorLogId);
		restTemplate.delete(url, paramMap);
    }

    public void ChangeFirmSubscriptionAdministrator(String newAdminUserName, Long accessId,
    		String comments, String modifiedBy) throws UserNameNotFoundException, SDLBusinessException {
    	String url = this.ecomAdminRestUrl.concat("changeFirmSubscriptionAdministrator/{newAdminUserName}/{accessId}/{comments}/{modifiedBy}");
    	Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("newAdminUserName", newAdminUserName);
		paramMap.put("accessId", accessId);
		paramMap.put("comments", comments);
		paramMap.put("modifiedBy", modifiedBy);
		try {
			restTemplate.postForObject(url, null, Void.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleSDLBusinessException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleUserNameNotFoundException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
    }

    public List<Location> getLocationsBySiteId(Long siteId){
    	String url = this.ecomRestURL.concat("getLocationsBySiteId/{siteId}");
    	Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("siteId", siteId);
		Location[] locations = restTemplate.getForObject(url, Location[].class, paramMap);
    	return Arrays.asList(locations);
    }

    public Location getLocationsSealById(Long locationId){
    	String url = this.ecomAdminRestUrl.concat("getLocationSealById/{locationId}");
    	Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("locationId", locationId);
		return restTemplate.getForObject(url, Location.class, paramMap);
    }

    public Location getLocationsSignatureById(Long locationId){
    	String url = this.ecomAdminRestUrl.concat("getLocationSignatureById/{locationId}");
    	Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("locationId", locationId);
		return restTemplate.getForObject(url, Location.class, paramMap);
    }

    public void removeFirmLevelAccess(String firmUserName, Long userAccessId, String comments, String modifiedBy)
    		throws UserNameNotFoundException, SDLBusinessException, Exception  {
    	String url = this.ecomRestURL.concat("removeFirmLevelUserAccess");
		RemoveFirmLevelAccessRequestDTO request = new RemoveFirmLevelAccessRequestDTO();
		request.setFirmUserName(firmUserName);
		request.setModifiedBy(modifiedBy);
		request.setComments(comments);
		request.setUserAccessId(userAccessId);
		request.setSendNotification(true);

		try {
			this.restTemplate.postForObject(url, request, Void.class);
		}
		catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleUserNameNotFoundException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleSDLBusinessException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			throw new Exception("Application Error occured, please contact administrator ");
		}
    }

    public void enableDisableFirmUserAccess(String firmUserName, Long userAccessId, boolean isEnable, String comments, String modifiedBy)
    		throws UserNameNotFoundException, SDLBusinessException, Exception  {
    	String url = this.ecomRestURL.concat("enableDisableFirmLevelUserAccess");
    	EnableDisableFirmAccessRequestDTO enableDisableRequest = new EnableDisableFirmAccessRequestDTO();
		enableDisableRequest.setUserAccessId(userAccessId);
		enableDisableRequest.setComments(comments);
		enableDisableRequest.setFirmUserName(firmUserName);
		enableDisableRequest.setEnable(isEnable);
		enableDisableRequest.setModifiedBy(modifiedBy);

		try {
			this.restTemplate.postForObject(url, enableDisableRequest, Void.class);
		}
		catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleUserNameNotFoundException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleSDLBusinessException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			throw new Exception("Application Error occured, please contact administrator ");
		}
    }

	public List<Code> getCodes(String codeCategory) {
		List<Code> codeList = null;
		String url = this.ecomRestURL.concat("getCodes/{codeCategory}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("codeCategory", codeCategory);
		Code[] codeArray = this.restTemplate.getForObject(url, Code[].class, paramMap);
		codeList = Arrays.asList(codeArray);
		return codeList;
	}


}
