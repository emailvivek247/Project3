package com.fdt.common.util.client;

import java.util.Date;
import java.util.List;

import com.fdt.alerts.dto.UserAlertDTO;
import com.fdt.alerts.entity.UserAlert;
import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.dto.ServiceResponseDTO;
import com.fdt.common.exception.SDLBusinessException;
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
import com.fdt.security.dto.FirmUserDTO;
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

public interface ServiceStub {

    public void registerUser(User aNewUser, Long siteId, Long accessId, String emailTemplateName, String requestURL)
    	throws UserNameAlreadyExistsException;

    public ServiceResponseDTO addFirmLevelUser(User aNewUser, String adminUserName, Long accessId, String emailTemplateName, String requestURL)
        	throws UserNameNotFoundException,	MaxUsersExceededException, SDLBusinessException, Exception ;

    public List<FirmUserDTO> getFirmUsers(String adminUserName, Long accessId);

    public void removeFirmLevelAccess(String firmUserName, Long accessId, String comments, String modifiedBy)
    		throws UserNameNotFoundException, SDLBusinessException, Exception  ;


    public void changePassword(User existingUser) throws UserNameNotFoundException, BadPasswordException;

    public void activateUser(String userName, String requestToken) throws InvalidDataException, UserAlreadyActivatedException;

    public void resetPassword(User user, String requestToken) throws UserNameNotFoundException, InvalidDataException;

    public void updateUser(User updatedUser, String modifiedBy) throws UserNameNotFoundException, InvalidDataException;

    public void resetPasswordRequest(String userName, String emailTemplateName, String requestURL)
        throws UserNameNotFoundException, UserNotActiveException;

    public void resendUserActivationEmail(String userName, String nodeName, String requestURL)
    	throws UserNameNotFoundException, UserAlreadyActivatedException;

    public void lockUnLockUser(String userName, boolean isLock, String modifiedBy, boolean isSendUserConfirmation,
        String nodeName, String additionalComments);

    public void enableDisableFirmUserAccess(String adminUserName, String firmUserName, Long accessId, boolean isEnable,
    		String comments) throws UserNameNotFoundException, SDLBusinessException, Exception ;

    public List<Site> getSitesForNode(String nodeName);

    public List<Access> getAccessesForSite(String siteId);

    public List<PayPalDTO> paySubscriptions(SubscriptionDTO subscriptionDTO)
    	throws AccessUnAuthorizedException, SDLBusinessException;

    public User getPaidSubUnpaidByUser(String userName, String nodeName);

    public void updateExistingCreditCardInformation(String userName, String modifiedBy, CreditCard newCreditCardInformation)
    	throws PaymentGatewaySystemException, PaymentGatewayUserException;

    // TODO: DELETE THIS
    public CreditCard getCreditCardDetails(Long userId);

    public CreditCard getCreditCardDetailsWithId(Long userId, Long creditCardId);

    public List<CreditCard> getCreditCardDetailsList(Long userId);

    public List<SubscriptionDTO> getUserSubscriptions(String userName, String nodeName, String siteName,
    		boolean activeSubscriptionsOnly, boolean firmAdminSubscriptionsOnly);

    public SubscriptionDTO getSubscriptionDetails(String userName, Long accessId);

    public PayPalDTO cancelSubscription(String userName, Long userAccessId) throws PaymentGatewaySystemException,
    	SDLBusinessException;

    public UpgradeDowngradeDTO getChangeSubscriptionInfo(Long userAccessId, Long accessId, String userName)
    	throws SDLBusinessException, MaxUsersExceededException;

    public UpgradeDowngradeDTO changeFromRecurringToRecurringSubscription(Long userAccessId, Long accessId, String userName,
        String machineName) throws PaymentGatewaySystemException, PaymentGatewayUserException, SDLBusinessException, MaxUsersExceededException;

    public List<AccessDetailDTO> addSubscription(SubscriptionDTO subscriptionDTO) throws SDLBusinessException;

    public PayPalDTO reactivateCancelledSubscription(String userName, Long existingUserAccessId)
    	throws PaymentGatewayUserException, PaymentGatewaySystemException;

    public List<RecurTx> getRecurTransactionsByNode(String userName, String nodeName);

    public List<RecurTx> getRecurTxDetail(String recurTxRefNum, String userName);

    public PageRecordsDTO getPayAsUGoTransactionsByNodePerPage(String userName, String nodeName, String comments,
    		Date fromDate, Date toDate, String transactionType, Integer startingFrom, Integer numberOfRecords);

    public List<PayAsUGoTxView> getPayAsUGoTransactionsByNode(String userName, String nodeName, String comments,
    		Date fromDate, Date toDate);

    public PayAsUGoTx getPayAsUGoTransactionDetail(Long recTxId, String userName, String isRefund);

    public List<PayAsUGoTx> doSalePayAsUGo(String userName, PayAsUSubDTO payAsUGoTransactionDTO)
    	throws PaymentGatewayUserException, PaymentGatewaySystemException, SDLBusinessException;

    public List<ShoppingCartItem> doSalePayAsUGoInfo(PayAsUSubDTO payAsUGoTransactionDTO) throws SDLBusinessException;

    public PayAsUGoTxItem getPayAsUGoTxIdForPurchasedDoc(String userName, String productKey, String uniqueIdentifier);

    public void checkValidResetPasswordRequest(String userName, String requestToken) throws InvalidDataException;

    public List<ShoppingCartItem> getShoppingBasketItems(String userName, String nodeName);

    public void deleteShoppingCartItem(ShoppingCartItem shoppingCartItem);

    public List<UserAlert> getUserAlertsByUserName(String userName, String nodeName);

    public void saveUserAlert(UserAlert userAlert) throws UserNameNotFoundException, DuplicateAlertException,
    	MaximumNumberOfAlertsReachedException;

    public void deleteUserAlerts(UserAlertDTO userAlertDTO);


    public List<Code> getCodes(String codeCategory);

    public void saveShoppingCartItem(ShoppingCartItem shoppingCartItem);

    public Term getTerm(String siteName);

    public List<AccessDetailDTO> getAccessDetails(Long accessId);

    public User loadUserByUsername(String userName, String nodeName);

    public void updateLastLoginTime(String userName) throws UserNameNotFoundException;

    public List<Term> getNewTermsAndConditionsforUser(String userName, String nodeName);

    public void updateUserTerms(User user) throws UserNameNotFoundException;

    public User findUser(String userName) throws UserNameNotFoundException;

	public void addFirmUserAccess(String adminUserName, String firmUserName, Long accessId, String nodeName)
			throws UserNameNotFoundException, MaxUsersExceededException, SDLBusinessException, Exception;

	public void updateShoppingCartComments(Long shoppingCartItemId, String comments);


	public List<Location> getLocationsBySiteId(Long siteId);

	public Location getLocationByNameAndAccessName(String locationName, String accessName);
	
	public String getDocumentIdByCertifiedDocumentNumber(String certifiedDocumentNumber, String siteName);

	public boolean removeCard(String username, String creditCardId);	

}
