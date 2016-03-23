package com.fdt.common.util.client;

import java.util.Date;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.fdt.alerts.dto.UserAlertDTO;
import com.fdt.alerts.entity.UserAlert;
import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.dto.ServiceResponseDTO;
import com.fdt.common.dto.TransactionRequestDTO;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.ecom.entity.Code;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.Location;
import com.fdt.ecom.entity.ShoppingCartItem;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.Term;
import com.fdt.ecom.exception.AccessUnAuthorizedException;
import com.fdt.ecom.service.EComFacadeService;
import com.fdt.payasugotx.dto.PayAsUSubDTO;
import com.fdt.payasugotx.entity.PayAsUGoTx;
import com.fdt.payasugotx.entity.PayAsUGoTxItem;
import com.fdt.payasugotx.entity.PayAsUGoTxView;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.recurtx.dto.UpgradeDowngradeDTO;
import com.fdt.recurtx.entity.RecurTx;
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
import com.fdt.subscriptions.dto.CreditCardForChangeSubscriptionDTO;
import com.fdt.subscriptions.dto.SubscriptionDTO;

@Component("serviceStubWS")
public class ServiceStubWS implements ServiceStub, ApplicationContextAware {

    private ApplicationContext applicationContext = null;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void registerUser(User aNewUser, Long siteId, Long accessId,
        String emailTemplateName, String requestURL) throws UserNameAlreadyExistsException {
        this.getService().registerUser(aNewUser, siteId, accessId, emailTemplateName, requestURL);
    }


    public ServiceResponseDTO addFirmLevelUser(User user, String adminUserName, Long accessId, String emailTemplateName, String requestURL)
        	throws UserNameNotFoundException,	MaxUsersExceededException, SDLBusinessException, Exception, Exception{
    	FirmLevelUserRequestDTO request = new FirmLevelUserRequestDTO();
    	request.setAccessId(accessId);
    	request.setUser(user);
    	return this.getService().addFirmLevelUser(request, adminUserName, emailTemplateName, requestURL);
    }

    public List<FirmUserDTO> getFirmUsers(String adminUserName, Long accessId){
    	return this.getService().getFirmUsers(adminUserName,  accessId);
    }

    public void removeFirmLevelAccess(String firmUserName, Long userAccessId, String comments, String modifiedBy)
    		throws UserNameNotFoundException, SDLBusinessException, Exception  {
		RemoveFirmLevelAccessRequestDTO request = new RemoveFirmLevelAccessRequestDTO();
		request.setFirmUserName(firmUserName);
		request.setUserAccessId(userAccessId);
		request.setModifiedBy(modifiedBy);
		request.setComments(comments);
		request.setSendNotification(true);
    	this.getService().removeFirmLevelUserAccess(request);
    }

     public void enableDisableFirmUserAccess(String adminUserName, String firmUserName, Long userAccessId, boolean isEnable, String comments)
        		throws UserNameNotFoundException, SDLBusinessException, Exception  {
        	EnableDisableFirmAccessRequestDTO enableDisableRequest = new EnableDisableFirmAccessRequestDTO();
    		enableDisableRequest.setUserAccessId(userAccessId);
    		enableDisableRequest.setComments(comments);
    		enableDisableRequest.setFirmUserName(firmUserName);
    		enableDisableRequest.setEnable(isEnable);
    		enableDisableRequest.setModifiedBy(adminUserName);

    	this.getService().enableDisableFirmLevelUserAccess(enableDisableRequest);
    }
    public void changePassword(User existingUser)
        throws UserNameNotFoundException, BadPasswordException {
        this.getService().changePassword(existingUser);
    }

    public void activateUser(String userName, String requestToken)
        throws InvalidDataException, UserAlreadyActivatedException {
        this.getService().activateUser(userName, requestToken);
    }

    public void resetPassword(User user, String requestToken)
            throws UserNameNotFoundException, InvalidDataException {
        this.getService().resetPassword(user, requestToken);
    }

    public void updateUser(User updatedUser, String modifiedBy)
            throws UserNameNotFoundException, InvalidDataException {
        this.getService().updateUser(updatedUser, modifiedBy);
    }

    public void resetPasswordRequest(String userName, String emailTemplateName, String requestURL)
            throws UserNameNotFoundException, UserNotActiveException {
        this.getService().resetPasswordRequest(userName, emailTemplateName, requestURL);
    }

    public void resendUserActivationEmail(String userName, String nodeName, String requestURL)
            throws UserNameNotFoundException, UserAlreadyActivatedException {
        this.getService().resendUserActivationEmail(userName, nodeName, requestURL);
    }

    public void lockUnLockUser(String userName, boolean isLock, String modifiedBy, boolean isSendUserConfirmation,
            String nodeName, String additionalComments) {
        this.getService().lockUnLockUser(userName, isLock, modifiedBy, isSendUserConfirmation, nodeName, additionalComments);
    }

    public List<Site> getSitesForNode(String nodeName) {
        return this.getService().getSitesForNode(nodeName);
    }

    public List<Access> getAccessesForSite(String siteId) {
        return this.getService().getAccessesForSite(siteId);
    }

    public List<PayPalDTO> paySubscriptions(SubscriptionDTO subscriptionDTO) throws AccessUnAuthorizedException,
    	SDLBusinessException {
        return this.getService().paySubscriptions(subscriptionDTO);
    }

    public User getPaidSubUnpaidByUser(String userName, String nodeName) {
        return this.getService().getPaidSubUnpaidByUser(userName, nodeName);
    }

    public CreditCard getCreditCardDetailsWithId(Long userId, Long creditCardId) {
        return this.getService().getCreditCardDetailsWithId(userId, creditCardId);
    }

    public List<CreditCard> getCreditCardDetailsList(Long userId) {
        return this.getService().getCreditCardDetailsList(userId);
    }

    public List<SubscriptionDTO> getUserSubscriptions(String userName, String nodeName, String siteName,
            boolean activeSubscriptionsOnly, boolean firmAdminSubscriptionsOnly) {
        return this.getService().getUserSubscriptions(userName, nodeName, siteName, activeSubscriptionsOnly, firmAdminSubscriptionsOnly);
    }

    public SubscriptionDTO getSubscriptionDetails(String userName, Long accessId) {
        return this.getService().getSubscriptionDetails(userName, accessId);
    }

    public PayPalDTO cancelSubscription(String userName, Long userAccessId)
            throws PaymentGatewaySystemException, SDLBusinessException {
        return this.getService().cancelSubscription(userName, userAccessId);
    }

    public UpgradeDowngradeDTO getChangeSubscriptionInfo(Long userAccessId, Long accessId, String userName)
            throws SDLBusinessException, MaxUsersExceededException {
        return this.getService().getChangeSubscriptionInfo(userAccessId, accessId, userName);
    }

    public UpgradeDowngradeDTO changeFromRecurringToRecurringSubscription(Long userAccessId, Long accessId, String userName,
            String machineName, CreditCardForChangeSubscriptionDTO creditCardForChangeSubscriptionDTO) 
            		throws PaymentGatewaySystemException, PaymentGatewayUserException, SDLBusinessException, MaxUsersExceededException {
        return this.getService().changeFromRecurringToRecurringSubscription(userAccessId, accessId, userName, machineName, creditCardForChangeSubscriptionDTO);
    }

    public List<AccessDetailDTO> addSubscription(SubscriptionDTO subscriptionDTO)
            throws SDLBusinessException {
        return this.getService().addSubscription(subscriptionDTO);
    }

    public PayPalDTO reactivateCancelledSubscription(String userName, Long existingUserAccessId)
            throws PaymentGatewayUserException, PaymentGatewaySystemException {
        return this.getService().reactivateCancelledSubscription(userName, existingUserAccessId);
    }

    public List<RecurTx> getRecurTransactionsByNode(String userName, String nodeName) {
        return this.getService().getRecurTransactionsByNode(userName, nodeName);
    }

    public List<RecurTx> getRecurTxDetail(String recurTxRefNum, String userName) {
        return this.getService().getRecurTxDetail(userName, recurTxRefNum);
    }

    public List<PayAsUGoTxView> getPayAsUGoTransactionsByNode(String userName, String nodeName, String comments,
    		Date fromDate, Date toDate) {
		TransactionRequestDTO request = new TransactionRequestDTO();
		request.setUserName(userName);
		request.setNodeName(nodeName);
		request.setFromDate(fromDate);
		request.setToDate(toDate);
		request.setComments(comments);
        return this.getService().getPayAsUGoTransactionsByNode(request);
    }

    public PageRecordsDTO getPayAsUGoTransactionsByNodePerPage(String userName, String nodeName, String comments,
    		Date fromDate, Date toDate, String transactionType, Integer startingFrom, Integer numberOfRecords) {
		TransactionRequestDTO request = new TransactionRequestDTO();
		request.setUserName(userName);
		request.setNodeName(nodeName);
		request.setFromDate(fromDate);
		request.setToDate(toDate);
		request.setTransactionType(transactionType);
		request.setComments(comments);
		request.setStartingFrom(startingFrom);
		request.setNumberOfRecords(numberOfRecords);
        return this.getService().getPayAsUGoTransactionsByNodePerPage(request);
    }

    public PayAsUGoTx getPayAsUGoTransactionDetail(Long recTxId, String userName, String isRefund) {
        return this.getService().getPayAsUGoTransactionDetail(userName, recTxId, isRefund);
    }

    public List<PayAsUGoTx> doSalePayAsUGo(String userName, PayAsUSubDTO PayAsUGoTxDTO)
            throws PaymentGatewayUserException, PaymentGatewaySystemException, SDLBusinessException {
        return this.getService().doSalePayAsUGo(userName, PayAsUGoTxDTO);
    }

    public List<ShoppingCartItem> doSalePayAsUGoInfo(PayAsUSubDTO PayAsUGoTxDTO)
            throws SDLBusinessException {
        return this.getService().doSalePayAsUGoInfo(PayAsUGoTxDTO);
    }

    public PayAsUGoTxItem getPayAsUGoTxIdForPurchasedDoc(String userName, String productKey, String uniqueIdentifier) {
        return this.getService().getPayAsUGoTxIdForPurchasedDoc(userName, productKey, uniqueIdentifier);
    }


    public void checkValidResetPasswordRequest(String userName, String requestToken) throws InvalidDataException {
        this.getService().checkValidResetPasswordRequest(userName, requestToken);
    }

    public List<ShoppingCartItem> getShoppingBasketItems(String userName, String nodeName) {
        return this.getService().getShoppingBasketItems(userName, nodeName);
    }

    public void deleteShoppingCartItem(ShoppingCartItem shoppingCartItem) {
        this.getService().deleteShoppingCartItem(shoppingCartItem);
    }

    public List<UserAlert> getUserAlertsByUserName(String userName, String nodeName) {
        List<UserAlert> userAlertList = this.getService().getUserAlertsByUserName(userName, nodeName);
        return userAlertList;
    }

    public void saveUserAlert(UserAlert userAlert) throws UserNameNotFoundException,
            DuplicateAlertException, MaximumNumberOfAlertsReachedException {
        this.getService().saveUserAlert(userAlert);
    }

    public void deleteUserAlerts(UserAlertDTO userAlertDTO) {
        this.getService().deleteUserAlerts(userAlertDTO);
    }


    public List<Code> getCodes(String codeCategory) {
        return this.getService().getCodes(codeCategory);
    }

    public void saveShoppingCartItem(ShoppingCartItem shoppingCartItem) {
        this.getService().saveShoppingCartItem(shoppingCartItem);
    }

    private EComFacadeService getService() {
        EComFacadeService eComFacadeService = (EComFacadeService)this.applicationContext.getBean("eComFacadeService");
        return eComFacadeService;
    }

    public Term getTerm(String siteName) {
        return this.getService().getTerm(siteName);
    }

    public List<AccessDetailDTO> getAccessDetails(Long accessId) {
        return this.getService().getAccessDetails(accessId);
    }

	public User loadUserByUsername(String userName, String nodeName) {
		return this.getService().loadUserByUsername(userName, nodeName);
	}

	public void updateLastLoginTime(String userName) throws UserNameNotFoundException {
		this.getService().updateLastLoginTime(userName);
	}

	public List<Term> getNewTermsAndConditionsforUser(String userName, String nodeName) {
        return this.getService().getNewTermsAndConditionsforUser(userName, nodeName);
    }

	public void updateUserTerms(User user) throws UserNameNotFoundException {
        this.getService().updateUserTerms(user);
    }

	public User findUser(String userName) throws UserNameNotFoundException {
        return this.getService().findUser(userName);
	}

    public void addFirmUserAccess(String adminUserName, String firmUserName, Long accessId, String nodeName)
			throws UserNameNotFoundException, MaxUsersExceededException, SDLBusinessException, Exception {
    	FirmLevelUserRequestDTO request = new FirmLevelUserRequestDTO();
    	User user = new User();
    	user.setUsername(firmUserName);
    	request.setUser(user);
    	request.setAccessId(accessId);
    	request.setNodeName(nodeName);
        this.getService().addFirmUserAccess(request,  adminUserName);
    }

    public void updateShoppingCartComments(Long shoppingCartItemId, String comments){
    	ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
    	shoppingCartItem.setId(shoppingCartItemId);
    	shoppingCartItem.setComments(comments);
    	this.getService().updateShoppingCartComments(shoppingCartItem);
    }

    public List<Location> getLocationsBySiteId(Long siteId){
    	return this.getService().getLocationsBySiteId(siteId);
    }

    public Location getLocationByNameAndAccessName(String locationName, String accessName){
    	return this.getService().getLocationByNameAndAccessName(locationName, accessName);
	}

	public String getDocumentIdByCertifiedDocumentNumber(String certifiedDocumentNumber, String siteName) {
		return this.getService().getDocumentIdByCertifiedDocumentNumber(certifiedDocumentNumber, siteName);
	}

	public boolean removeCard(String username, String creditCardId) {
		return false;
	}
}
