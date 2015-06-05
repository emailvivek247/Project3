package com.fdt.common.util.client;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdt.alerts.dto.UserAlertDTO;
import com.fdt.alerts.entity.UserAlert;
import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.dto.ServiceResponseDTO;
import com.fdt.common.dto.TransactionRequestDTO;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.exception.rs.ExceptionHandler;
import com.fdt.ecom.entity.Code;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.Location;
import com.fdt.ecom.entity.ShoppingCartItem;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.Term;
import com.fdt.ecom.exception.AccessUnAuthorizedException;
import com.fdt.payasugotx.dto.PayAsUSubDTO;
import com.fdt.payasugotx.entity.PayAsUGoTx;
import com.fdt.payasugotx.entity.PayAsUGoTxItem;
import com.fdt.payasugotx.entity.PayAsUGoTxView;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.recurtx.dto.UpgradeDowngradeDTO;
import com.fdt.recurtx.entity.RecurTx;
import com.fdt.sdl.styledesigner.util.PageStyleUtil;
import com.fdt.security.dto.EnableDisableFirmAccessRequestDTO;
import com.fdt.security.dto.FirmLevelUserRequestDTO;
import com.fdt.security.dto.FirmUserDTO;
import com.fdt.security.dto.RemoveFirmLevelAccessRequestDTO;
import com.fdt.security.entity.Access;
import com.fdt.security.entity.User;
import com.fdt.security.exception.BadPasswordException;
import com.fdt.security.exception.DuplicateAlertException;
import com.fdt.security.exception.InvalidDataException;
import com.fdt.security.exception.MaxUsersExceededException;
import com.fdt.security.exception.MaximumNumberOfAlertsReachedException;
import com.fdt.security.exception.UserAlreadyActivatedException;
import com.fdt.security.exception.UserNameAlreadyExistsException;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.security.exception.UserNotActiveException;
import com.fdt.subscriptions.dto.AccessDetailDTO;
import com.fdt.subscriptions.dto.SubscriptionDTO;


public class ServiceStubRS implements ServiceStub {

	private static final Logger logger = LoggerFactory.getLogger(ServiceStubRS.class);

	private String ecomRestURL = null;

	@Autowired
	private RestTemplate restTemplate = null;

	@Autowired
	private ObjectMapper jacksonObjectMapper = null;

	private ExceptionHandler exceptionHandler = new ExceptionHandler();

	public void registerUser(User aNewUser, Long siteId, Long accessId, String clientName, String requestURL)
			throws UserNameAlreadyExistsException {
		String url = this.ecomRestURL.concat("registerUser/{siteId}/{accessId}/{clientName}/{requestURL}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("siteId", siteId);
		paramMap.put("accessId", accessId);
		paramMap.put("clientName", clientName);
		paramMap.put("requestURL", PageStyleUtil.encodeURL(requestURL));
		try {
			this.restTemplate.postForObject(url, aNewUser, Void.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleUserNameAlreadyExistsException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
	}

    public ServiceResponseDTO addFirmLevelUser(User user, String adminUserName, Long accessId, String clientName, String requestURL)
        	throws UserNameNotFoundException,	MaxUsersExceededException, SDLBusinessException, Exception {
    	String url = this.ecomRestURL.concat("addFirmLevelUser/{adminUserName}/{clientName}/{requestURL}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("clientName", clientName);
		paramMap.put("adminUserName", adminUserName);
		paramMap.put("requestURL", PageStyleUtil.encodeURL(requestURL));

		FirmLevelUserRequestDTO request = new FirmLevelUserRequestDTO();
		request.setUser(user);
		request.setAccessId(accessId);

		try {
			return this.restTemplate.postForObject(url, request, ServiceResponseDTO.class, paramMap);
		}
		catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleUserNameNotFoundException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleSDLBusinessException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleMaxUsersExceededException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			throw new Exception("Application Error occured, please contact administrator ");
		}

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

    public void enableDisableFirmUserAccess(String adminUserName, String firmUserName, Long userAccessId, boolean isEnable, String comments)
    		throws UserNameNotFoundException, SDLBusinessException, Exception  {
    	String url = this.ecomRestURL.concat("enableDisableFirmLevelUserAccess");
    	EnableDisableFirmAccessRequestDTO enableDisableRequest = new EnableDisableFirmAccessRequestDTO();
		enableDisableRequest.setUserAccessId(userAccessId);
		enableDisableRequest.setComments(comments);
		enableDisableRequest.setFirmUserName(firmUserName);
		enableDisableRequest.setEnable(isEnable);
		enableDisableRequest.setModifiedBy(adminUserName);

		try {
			this.restTemplate.postForObject(url, enableDisableRequest, Void.class);
		}
		catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleUserNameNotFoundException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleSDLBusinessException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			throw new Exception("Application Error occured, please contact administrator ");
		}
    }


    public List<FirmUserDTO> getFirmUsers(String adminUserName, Long accessId){
    	String url = this.ecomRestURL.concat("getFirmUsers/{adminUserName}/{accessId}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("adminUserName", adminUserName);
		paramMap.put("accessId", accessId == null ? null : accessId.toString());
		FirmUserDTO[] users = this.restTemplate.getForObject(url, FirmUserDTO[].class, paramMap);
		return Arrays.asList(users);
    }

	public void changePassword(User existingUser) throws UserNameNotFoundException, BadPasswordException {
		String url = this.ecomRestURL.concat("changePassword");
		try {
			this.restTemplate.postForObject(url, existingUser, Void.class);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleUserNameNotFoundException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleBadPasswordException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
	}

	public void activateUser(String userName, String requestToken) throws InvalidDataException,
		UserAlreadyActivatedException {
		String url = this.ecomRestURL.concat("activateUser/{userName}/{requestToken}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("requestToken", requestToken);
		try {
			this.restTemplate.postForObject(url, null, Void.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleInvalidDataException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleUserAlreadyActivatedException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
	}

	public void resetPassword(User user, String requestToken) throws UserNameNotFoundException, InvalidDataException {
		String url = this.ecomRestURL.concat("resetPassword/{requestToken}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("requestToken", requestToken);
		try {
			this.restTemplate.postForObject(url, user, Void.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleUserNameNotFoundException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleInvalidDataException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
	}

	public void updateUser(User updatedUser, String modifiedBy)
			throws UserNameNotFoundException, InvalidDataException {
		String url = this.ecomRestURL.concat("updateUser/{modifiedBy}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("modifiedBy", modifiedBy);
		try {
			this.restTemplate.postForObject(url, updatedUser, Void.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleUserNameNotFoundException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleInvalidDataException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
	}

	public void resetPasswordRequest(String userName, String clientName, String requestURL)
			throws UserNameNotFoundException, UserNotActiveException {
		String url = this.ecomRestURL.concat("resetPasswordRequest/{userName}/{clientName}/{requestURL}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("clientName", clientName);
		paramMap.put("requestURL", PageStyleUtil.encodeURL(requestURL));
		try {
			this.restTemplate.postForObject(url, null, Void.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleUserNameNotFoundException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleUserNotActiveException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
	}

	public void resendUserActivationEmail(String userName, String nodeName, String requestURL)
			throws UserNameNotFoundException, UserAlreadyActivatedException {
		String url = this.ecomRestURL.concat("resendUserActivationEmail/{userName}/{nodeName}/{requestURL}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("nodeName", nodeName);
		paramMap.put("requestURL", PageStyleUtil.encodeURL(requestURL));
		try {
			this.restTemplate.postForObject(url, null, Void.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleUserNameNotFoundException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleUserAlreadyActivatedException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
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
				url = url + "&additionalComments=" + PageStyleUtil.encodeURL(additionalComments);
			}
			restTemplate.postForObject(url, null, Void.class);
	}

	public List<Site> getSitesForNode(String nodeName) {
		List<Site> siteList = null;
		String url = this.ecomRestURL.concat("getSitesForNode/{nodeName}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("nodeName", nodeName);
		Site[] siteArray = this.restTemplate.getForObject(url, Site[].class, paramMap);
		siteList = Arrays.asList(siteArray);
		return siteList;
	}

	public List<Access> getAccessesForSite(String siteId) {
		List<Access> accessList = null;
		String url = this.ecomRestURL.concat("getAccessesForSite/{siteId}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("siteId", siteId);
		Access[] accessArray = this.restTemplate.getForObject(url, Access[].class, paramMap);
		accessList = Arrays.asList(accessArray);
		return accessList;
	}

	public List<PayPalDTO> paySubscriptions(SubscriptionDTO subscriptionDTO)
			throws AccessUnAuthorizedException, SDLBusinessException {
		List<PayPalDTO> payPalDTOList = null;
		String url = this.ecomRestURL.concat("paySubscriptions");
		try {
			PayPalDTO[] accessArray = this.restTemplate.postForObject(url, subscriptionDTO, PayPalDTO[].class);
			payPalDTOList = Arrays.asList(accessArray);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleAccessUnAuthorizedException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleSDLBusinessException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
		return payPalDTOList;
	}

	public User getPaidSubUnpaidByUser(String userName, String nodeName) {
		String url = this.ecomRestURL.concat("getPaidSubUnpaidByUser/{userName}/{nodeName}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("nodeName", nodeName);
		User user = this.restTemplate.getForObject(url, User.class, paramMap);
		return user;
	}

	public void updateExistingCreditCardInformation(String userName, String modifiedBy, CreditCard newCreditCardInformation)
			throws PaymentGatewaySystemException, PaymentGatewayUserException {
		String url = this.ecomRestURL.concat("updateExistingCreditCardInformation/{userName}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		try {
			this.restTemplate.postForObject(url, newCreditCardInformation, Void.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handlePaymentGatewayUserException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handlePaymentGatewaySystemException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
	}

	public CreditCard getCreditCardDetails(Long userId) {
		String url = this.ecomRestURL.concat("getCreditCardDetails/{userId}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userId", userId);
		CreditCard creditCard = this.restTemplate.getForObject(url, CreditCard.class, paramMap);
		return creditCard;
	}

	public List<SubscriptionDTO> getUserSubscriptions(String userName, String nodeName, String siteName, boolean activeSubscriptionsOnly, boolean firmAdminSubscriptionsOnly) {
		String url = this.ecomRestURL.concat("getUserSubscriptions?");
		if(!StringUtils.isBlank(userName)) {
			url = url + "&userName=" + userName;
		}
		if(!StringUtils.isBlank(nodeName)) {
			url = url + "&nodeName=" + nodeName;
		}
		if(!StringUtils.isBlank(siteName)) {
			url = url + "&siteName=" + siteName;
		}
		url = url + "&activeSubscriptionsOnly=" + activeSubscriptionsOnly;
		url = url + "&firmAdminSubscriptionsOnly=" + firmAdminSubscriptionsOnly;

		SubscriptionDTO[] subscriptionDTOArray = restTemplate.getForObject(url, SubscriptionDTO[].class);
		List<SubscriptionDTO> subscriptionDTOList = Arrays.asList(subscriptionDTOArray);
		return subscriptionDTOList;
	}

	public SubscriptionDTO getSubscriptionDetails(String userName, Long accessId) {
		String url = this.ecomRestURL.concat("getSubscriptionDetails/{userName}/{accessId}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("accessId", accessId);
		SubscriptionDTO subscriptionDTO = this.restTemplate.getForObject(url, SubscriptionDTO.class, paramMap);
		return subscriptionDTO;
	}

	public PayPalDTO cancelSubscription(String userName, Long userAccessId) throws PaymentGatewaySystemException,
		SDLBusinessException {
		PayPalDTO payPalDTO = null;
		String url = this.ecomRestURL.concat("cancelSubscription/{userName}/{userAccessId}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("userAccessId", userAccessId);
		try {
			 payPalDTO = this.restTemplate.postForObject(url, null, PayPalDTO.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handlePaymentGatewaySystemException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleSDLBusinessException(jacksonObjectMapper, unknownHttpStatusCodeExcp);;
		}
		return payPalDTO;
	}

	public UpgradeDowngradeDTO getChangeSubscriptionInfo(Long userAccessId, Long accessId, String userName)
			throws SDLBusinessException, MaxUsersExceededException {
		UpgradeDowngradeDTO upgradeDowngradeDTO = null;
		String url = this.ecomRestURL.concat("getChangeSubscriptionInfo/{userAccessId}/{accessId}/{userName}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userAccessId", userAccessId);
		paramMap.put("accessId", accessId);
		paramMap.put("userName", userName);
		try {
			upgradeDowngradeDTO = this.restTemplate.getForObject(url, UpgradeDowngradeDTO.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleSDLBusinessException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleMaxUsersExceededException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
		return upgradeDowngradeDTO;
	}

	public UpgradeDowngradeDTO changeFromRecurringToRecurringSubscription(Long userAccessId, Long accessId, String userName,
			String machineName) throws PaymentGatewaySystemException, PaymentGatewayUserException,
			SDLBusinessException, MaxUsersExceededException {
		UpgradeDowngradeDTO upgradeDowngradeDTO = null;
		String url = this.ecomRestURL.concat("changeFromRecurringToRecurringSubscription/{userAccessId}/{accessId}/" +
				"{userName}/{machineName}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userAccessId", userAccessId);
		paramMap.put("accessId", accessId);
		paramMap.put("userName", userName);
		paramMap.put("machineName", machineName);
		try {
			upgradeDowngradeDTO = this.restTemplate.postForObject(url, null, UpgradeDowngradeDTO.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handlePaymentGatewayUserException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handlePaymentGatewaySystemException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleSDLBusinessException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleMaxUsersExceededException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
		return upgradeDowngradeDTO;

	}

	public List<AccessDetailDTO> addSubscription(SubscriptionDTO subscriptionDTO) throws SDLBusinessException {
		List<AccessDetailDTO> accessDetailDTOList = null;
		String url = this.ecomRestURL.concat("addSubscription");
		try {
			AccessDetailDTO[] accessDetailDTOArray = this.restTemplate.postForObject(url, subscriptionDTO,
					AccessDetailDTO[].class);
			accessDetailDTOList = Arrays.asList(accessDetailDTOArray);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleSDLBusinessException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
		return accessDetailDTOList;
	}

	public PayPalDTO reactivateCancelledSubscription(String userName, Long existingUserAccessId)
			throws PaymentGatewayUserException, PaymentGatewaySystemException {
		PayPalDTO payPalDTO = null;
		String url = this.ecomRestURL.concat("reactivateCancelledSubscription/{userName}/{existingUserAccessId}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("existingUserAccessId", existingUserAccessId);
		try {
			payPalDTO = this.restTemplate.postForObject(url, null, PayPalDTO.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handlePaymentGatewayUserException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handlePaymentGatewaySystemException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
		return payPalDTO;
	}

	public List<RecurTx> getRecurTransactionsByNode(String userName, String nodeName) {
		List<RecurTx> recurTransactionList = null;
		String url = this.ecomRestURL.concat("getRecurTransactionsByNode/{userName}/{nodeName}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("nodeName", nodeName);
		RecurTx[] recurTransactionArray = this.restTemplate.getForObject(url,  RecurTx[].class, paramMap);
		recurTransactionList = Arrays.asList(recurTransactionArray);
		return recurTransactionList;
	}

	public List<RecurTx> getRecurTxDetail(String recurTxRefNum, String userName) {
		List<RecurTx> accessDetailDTOList = null;
		String url = this.ecomRestURL.concat("getRecurTxDetail/{userName}/{recurTxRefNum}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("recurTxRefNum", recurTxRefNum);
		RecurTx[] recurTransactionArray = this.restTemplate.getForObject(url, RecurTx[].class, paramMap);
		accessDetailDTOList = Arrays.asList(recurTransactionArray);
		return accessDetailDTOList;
	}


	public List<PayAsUGoTxView> getPayAsUGoTransactionsByNode(String userName, String nodeName, String comments,
			Date fromDate, Date toDate) {
		List<PayAsUGoTxView> payAsUGoTransactionList = null;
		String url = this.ecomRestURL.concat("getPayAsUGoTransactionsByNode");
		TransactionRequestDTO request = new TransactionRequestDTO();
		request.setUserName(userName);
		request.setNodeName(nodeName);
		request.setFromDate(fromDate);
		request.setComments(comments);
		request.setToDate(toDate);
		PayAsUGoTxView[] arr = this.restTemplate.postForObject(url, request, PayAsUGoTxView[].class);
		payAsUGoTransactionList = Arrays.asList(arr);
		return payAsUGoTransactionList;
	}
	public PageRecordsDTO getPayAsUGoTransactionsByNodePerPage(String userName, String nodeName, String comments,
			Date fromDate, Date toDate, Integer startingFrom, Integer numberOfRecords) {
		String url = this.ecomRestURL.concat("getPayAsUGoTransactionsByNodePerPage");
		TransactionRequestDTO request = new TransactionRequestDTO();
		request.setUserName(userName);
		request.setNodeName(nodeName);
		request.setFromDate(fromDate);
		request.setComments(comments);
		request.setToDate(toDate);
		request.setStartingFrom(startingFrom);
		request.setNumberOfRecords(numberOfRecords);
		PageRecordsDTO PageRecordsDTO  = this.restTemplate.postForObject(url, request,	PageRecordsDTO.class);
		return PageRecordsDTO;
	}

	public PayAsUGoTx getPayAsUGoTransactionDetail(Long recTxId, String userName, String isRefund) {
		PayAsUGoTx payAsUGoTransaction = null;
		String url = this.ecomRestURL.concat("getPayAsUGoTransactionDetail/{userName}/{recTxId}/{isRefund}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("recTxId", recTxId);
		paramMap.put("isRefund", isRefund);
		payAsUGoTransaction = this.restTemplate.getForObject(url, PayAsUGoTx.class, paramMap);
		return payAsUGoTransaction;
	}

	public List<PayAsUGoTx> doSalePayAsUGo(String userName, PayAsUSubDTO payAsUGoTransactionDTO)
			throws PaymentGatewayUserException, PaymentGatewaySystemException, SDLBusinessException {
		List<PayAsUGoTx> payAsUGoTransactionList = null;
		String url = this.ecomRestURL.concat("doSalePayAsUGo");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		PayAsUGoTx[] payAsUGoTransactionArray = this.restTemplate.postForObject(url, payAsUGoTransactionDTO,
				PayAsUGoTx[].class, paramMap);
		try {
			payAsUGoTransactionList = Arrays.asList(payAsUGoTransactionArray);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handlePaymentGatewayUserException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handlePaymentGatewaySystemException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleSDLBusinessException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
		return payAsUGoTransactionList;
	}

	public List<ShoppingCartItem> doSalePayAsUGoInfo(PayAsUSubDTO payAsUGoTransactionDTO)
			throws SDLBusinessException {
		List<ShoppingCartItem> shoppingCartItemList = null;
		String url = this.ecomRestURL.concat("doSalePayAsUGoInfo");
		try {
			logger.debug("PayAsUGoTransactionDTO: " + payAsUGoTransactionDTO);
			logger.debug("url: "+ url);
			ShoppingCartItem[] shoppingCartItemArray = this.restTemplate.postForObject(url, payAsUGoTransactionDTO,
				ShoppingCartItem[].class);
			shoppingCartItemList = Arrays.asList(shoppingCartItemArray);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleSDLBusinessException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
		return shoppingCartItemList;
	}

	public PayAsUGoTxItem getPayAsUGoTxIdForPurchasedDoc(String userName, String productKey, String uniqueIdentifier) {
		PayAsUGoTxItem payAsUGoTransaction = null;
		String url = this.ecomRestURL.concat("getPayAsUGoTxIdForPurchasedDoc/{userName}/{productKey}/{uniqueIdentifier}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("productKey", productKey);
		paramMap.put("uniqueIdentifier", uniqueIdentifier);
		payAsUGoTransaction = this.restTemplate.getForObject(url, PayAsUGoTxItem.class, paramMap);
		return payAsUGoTransaction;
	}


	public void checkValidResetPasswordRequest(String userName, String requestToken) throws InvalidDataException {
		String url = this.ecomRestURL.concat("checkValidResetPasswordRequest/{userName}/{requestToken}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("requestToken", requestToken);
		try {
			this.restTemplate.getForObject(url, Void.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleInvalidDataException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
	}

	public List<ShoppingCartItem> getShoppingBasketItems(String userName, String nodeName) {
		List<ShoppingCartItem> shoppingCartItemList = null;
		String url = this.ecomRestURL.concat("getShoppingBasketItems/{userName}/{nodeName}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("nodeName", nodeName);
		ShoppingCartItem[] shoppingCartItemArray = this.restTemplate.getForObject(url, ShoppingCartItem[].class, paramMap);
		shoppingCartItemList = Arrays.asList(shoppingCartItemArray);
		return shoppingCartItemList;
	}

	public void deleteShoppingCartItem(ShoppingCartItem shoppingCartItem) {
		String url = this.ecomRestURL.concat("deleteShoppingCartItem");
		this.restTemplate.postForObject(url, shoppingCartItem, Void.class);
	}

	public List<UserAlert> getUserAlertsByUserName(String userName, String nodeName) {
		List<UserAlert> userAlertList = null;
		String url = this.ecomRestURL.concat("getUserAlertsByUserName/{userName}/{nodeName}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("nodeName", nodeName);
		UserAlert[] userAlertArray = this.restTemplate.getForObject(url, UserAlert[].class, paramMap);
		userAlertList = Arrays.asList(userAlertArray);
		return userAlertList;
	}

	public void saveUserAlert(UserAlert userAlert) throws UserNameNotFoundException,
		DuplicateAlertException, MaximumNumberOfAlertsReachedException {
		String url = this.ecomRestURL.concat("saveUserAlert");
		try {
			this.restTemplate.postForObject(url, userAlert, Void.class);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleDuplicateAlertException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleMaximumNumberOfAlertsReachedException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
	}

	public void deleteUserAlerts(UserAlertDTO userAlertDTO) {
		String url = this.ecomRestURL.concat("deleteUserAlerts");
		this.restTemplate.postForObject(url, userAlertDTO, Void.class);
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

	public void saveShoppingCartItem(ShoppingCartItem shoppingCartItem) {
		String url = this.ecomRestURL.concat("saveShoppingCartItem");
		this.restTemplate.postForObject(url, shoppingCartItem, Void.class);
	}

	public Term getTerm(String siteName) {
		Term term = null;
		String url = this.ecomRestURL.concat("getTerm/{siteName}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("siteName", siteName);
		term = this.restTemplate.getForObject(url, Term.class, paramMap);
		return term;
	}

	public List<AccessDetailDTO> getAccessDetails(Long accessId) {
		List<AccessDetailDTO> accessDetailDTOList = null;
		String url = this.ecomRestURL.concat("getAccessDetails/{accessId}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("accessId", accessId);
		AccessDetailDTO[] accessDetailDTOArray = this.restTemplate.getForObject(url, AccessDetailDTO[].class, paramMap);
		accessDetailDTOList = Arrays.asList(accessDetailDTOArray);
		return accessDetailDTOList;
	}

	public User loadUserByUsername(String userName, String nodeName) {
		String url = this.ecomRestURL.concat("loadUserByUsername/{userName}/{nodeName}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("nodeName", nodeName);
		User user = this.restTemplate.getForObject(url, User.class, paramMap);
		return user;
	}

	public void updateLastLoginTime(String userName) throws UserNameNotFoundException {
		String url = this.ecomRestURL.concat("updateLastLoginTime/{userName}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		try {
			this.restTemplate.postForObject(url, null, Void.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleUserNameNotFoundException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}

	}

	public String getEcomRestURL() {
		return ecomRestURL;
	}

	public void setEcomRestURL(String ecomRestURL) {
		this.ecomRestURL = ecomRestURL;
	}

	public List<Term> getNewTermsAndConditionsforUser(String userName, String nodeName) {
		List<Term> newTermsList = null;
		String url = this.ecomRestURL.concat("getNewTermsAndConditionsforUser/{userName}/{nodeName}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		paramMap.put("nodeName", nodeName);
		Term[] newTermsArray = this.restTemplate.getForObject(url, Term[].class, paramMap);
		newTermsList = Arrays.asList(newTermsArray);
		return newTermsList;
	}

	public void updateUserTerms(User user) throws UserNameNotFoundException {
		String url = this.ecomRestURL.concat("updateUserTerms");
		try {
			this.restTemplate.postForObject(url, user, Void.class);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleUserNameNotFoundException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}

	}

	public User findUser(String userName) throws UserNameNotFoundException {
		String url = this.ecomRestURL.concat("findUser/{userName}");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userName", userName);
		try {
			return this.restTemplate.getForObject(url, User.class, paramMap);
		} catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleUserNameNotFoundException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
		}
		return null;
	}

    public void addFirmUserAccess(String adminUserName, String firmUserName, Long accessId, String nodeName)
			throws UserNameNotFoundException, MaxUsersExceededException, SDLBusinessException, Exception {
    	String url = this.ecomRestURL.concat("addFirmUserAccess/{adminUserName}");
    	FirmLevelUserRequestDTO request = new FirmLevelUserRequestDTO();
    	User user = new User();
    	user.setUsername(firmUserName);
    	request.setUser(user);
    	request.setAccessId(accessId);
    	request.setNodeName(nodeName);

    	Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("adminUserName", adminUserName);

		try {
			this.restTemplate.postForObject(url, request, FirmLevelUserRequestDTO.class, paramMap);
		}
		catch (UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) {
			exceptionHandler.handleUserNameNotFoundException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleSDLBusinessException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			exceptionHandler.handleMaxUsersExceededException(jacksonObjectMapper, unknownHttpStatusCodeExcp);
			throw new Exception("Application Error occured, please contact administrator ");
		}
    }

    public void updateShoppingCartComments(Long shoppingCartItemId, String comments){
    	String url = this.ecomRestURL.concat("updateShoppingCartComments");
    	ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
    	shoppingCartItem.setId(shoppingCartItemId);
    	shoppingCartItem.setComments(comments);
		this.restTemplate.postForObject(url, shoppingCartItem, Void.class);
    }

    public List<Location> getLocationsBySiteId(Long siteId){
    	String url = this.ecomRestURL.concat("getLocationsBySiteId/{siteId}");
    	Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("siteId", siteId);
		Location[] locations = restTemplate.getForObject(url, Location[].class, paramMap);
    	return Arrays.asList(locations);
    }

	public Location getLocationByNameAndAccessName(String locationName, String accessName){
    	String url = this.ecomRestURL.concat("getLocationByNameAndAccessName/{locationName}/{accessName}");
    	Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("locationName", locationName);
		paramMap.put("accessName", accessName);
		Location location = restTemplate.getForObject(url, Location.class, paramMap);
		return location;
	}

	public String getDocumentIdByCertifiedDocumentNumber(String certifiedDocumentNumber, String siteName) {
		String url = this.ecomRestURL.concat("getDocumentIdByCertifiedDocumentNumber/{certifiedDocumentNumber}/{siteName}");
    	Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("certifiedDocumentNumber", certifiedDocumentNumber);
		paramMap.put("siteName", siteName);
		String documentId = restTemplate.getForObject(url, String.class, paramMap);
		return documentId;
	}


}