package com.fdt.ecom.service;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

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
import com.fdt.security.exception.UserAccountExistsException;
import com.fdt.security.exception.UserAlreadyActivatedException;
import com.fdt.security.exception.UserNameAlreadyExistsException;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.security.exception.UserNotActiveException;
import com.fdt.subscriptions.dto.AccessDetailDTO;
import com.fdt.subscriptions.dto.SubscriptionDTO;
import com.fdt.webtx.entity.WebTxItem;

@WebService
public interface EComFacadeService {

    /** These Are User Services API **/

    @WebMethod
    public void registerUser(
        @WebParam(name="aNewUser") User aNewUser,
        @WebParam(name="siteId") Long siteId,
        @WebParam(name="accessId") Long accessId,
        @WebParam(name="clientName") String clientName,
        @WebParam(name="requestURL") String requestURL)
        throws UserNameAlreadyExistsException;

    @WebMethod
    public ServiceResponseDTO addFirmLevelUser(
            @WebParam(name="request") FirmLevelUserRequestDTO request,
            @WebParam(name="adminUserName") String adminUserName,
            @WebParam(name="clientName") String clientName,
            @WebParam(name="requestURL") String requestURL)
            throws UserNameNotFoundException,	MaxUsersExceededException, SDLBusinessException;


    /**
     * This method return all the users for a given firm (admin user id):
     * If subscription (accessId is supplied then it will find the users under a given subscriptions

     * @param adminUserName
     * @param accessId
     * @return
     */
  @WebMethod
    public List<FirmUserDTO> getFirmUsers(
        @WebParam(name="adminUserName") String adminUserName,
        @WebParam(name="accessId") Long accessId);

  @WebMethod
  public void removeFirmLevelUserAccess(@WebParam(name="request") RemoveFirmLevelAccessRequestDTO request)
		  			throws UserNameNotFoundException, SDLBusinessException;

  @WebMethod
  public void enableDisableFirmLevelUserAccess(
  		@WebParam(name="request") EnableDisableFirmAccessRequestDTO request)
  				throws UserNameNotFoundException, SDLBusinessException;


    @WebMethod
    public void changePassword(
        @WebParam(name="existingUser") User existingUser)
        throws UserNameNotFoundException, BadPasswordException;

    @WebMethod
    public void activateUser(
        @WebParam(name="userName") String userName,
        @WebParam(name="requestToken") String requestToken)
        throws InvalidDataException, UserAlreadyActivatedException;

    @WebMethod
    public void resetPassword(
        @WebParam(name="user") User user,
        @WebParam(name="requestToken") String requestToken)
        throws UserNameNotFoundException, InvalidDataException;

    @WebMethod
    public void updateUser(@WebParam(name="updatedUser") User updatedUser, @WebParam(name="modifiedBy") String modifiedBy)
        throws UserNameNotFoundException;

    @WebMethod
    public void resetPasswordRequest(
        @WebParam(name="userName") String userName,
        @WebParam(name="clientName") String clientName,
        @WebParam(name="requestURL") String requestURL) throws UserNameNotFoundException,
        UserNotActiveException;

    @WebMethod
    public void resendUserActivationEmail(
        @WebParam(name="userName") String userName,
        @WebParam(name="nodeName") String nodeName,
        @WebParam(name="requestURL") String requestURL)
        throws UserNameNotFoundException, UserAlreadyActivatedException;

    @WebMethod
    public void lockUnLockUser(
        @WebParam(name="userName") String userName,
        @WebParam(name="lockUnlock") boolean isLock,
        @WebParam(name="modifiedBy") String modifiedBy,
        @WebParam(name="isSendUserConfirmation") boolean isSendUserConfirmation,
        @WebParam(name="nodeName") String nodeName,
        @WebParam(name="additionalComments") String additionalComments);

    @WebMethod
    public void checkValidResetPasswordRequest(
        @WebParam(name="userName") String userName,
        @WebParam(name="nodeName") String requestToken)
        throws InvalidDataException;

    @WebMethod
    public User loadUserByUsername(
        @WebParam(name="userName") String username,
        @WebParam(name="nodeName") String nodeName);

    @WebMethod
    public Term getTerm(
        @WebParam(name = "siteName") String siteName);

    @WebMethod
    public void updateLastLoginTime(
        @WebParam(name = "userName") String userName) throws UserNameNotFoundException;

    /** ECommerce Related APIS **/

    @WebMethod
    public List<Site> getSitesForNode(
        @WebParam(name="nodeName") String nodeName);

    @WebMethod
    public List<Access> getAccessesForSite(
        @WebParam(name="siteId") String siteId);

    /** End of ECommerce Related APIS **/

    /**START OF Recurring Transaction APIS **/
    @WebMethod
    public List<PayPalDTO> paySubscriptions(SubscriptionDTO subscriptionDTO)
        throws AccessUnAuthorizedException, SDLBusinessException;

    @WebMethod
    public User getPaidSubUnpaidByUser(
        @WebParam(name="userName") String userName,
        @WebParam(name="nodeName") String nodeName);

    @WebMethod
    public void updateExistingCreditCardInformation(
        @WebParam(name="userName") String userName,
        @WebParam(name="modifiedBy") String modifiedBy,
        @WebParam(name="newCreditCardInformation") CreditCard newCreditCardInformation)
        throws PaymentGatewaySystemException, PaymentGatewayUserException;

    @WebMethod
    public List<SubscriptionDTO> getUserSubscriptions(
        @WebParam(name="userName") String userName,
        @WebParam(name="nodeName") String nodeName,
        @WebParam(name="siteName") String siteName,
    	@WebParam(name="activeSubscriptionsOnly") boolean activeSubscriptionsOnly,
    	@WebParam(name="firmAdminSubscriptionsOnly") boolean firmAdminSubscriptionsOnly);

    @WebMethod
    public SubscriptionDTO getSubscriptionDetails(
        @WebParam(name="userName") String userName,
        @WebParam(name="accessId") Long accessId);

    @WebMethod
    public PayPalDTO cancelSubscription(
        @WebParam(name="userName") String userName,
        @WebParam(name="userAccessId") Long userAccessId)
        throws PaymentGatewaySystemException, SDLBusinessException;

    @WebMethod
    public UpgradeDowngradeDTO getChangeSubscriptionInfo(
        @WebParam(name="userAccessId")Long userAccessId,
        @WebParam(name="accessId")Long accessId,
        @WebParam(name="userName")String userName) throws SDLBusinessException, MaxUsersExceededException;

    @WebMethod
    public UpgradeDowngradeDTO changeFromRecurringToRecurringSubscription(
        @WebParam(name="userAccessId") Long userAccessId,
        @WebParam(name="accessId") Long accessId,
        @WebParam(name="userName") String userName,
        @WebParam(name="machineName") String machineName)
        throws PaymentGatewaySystemException, PaymentGatewayUserException, SDLBusinessException, MaxUsersExceededException;

    @WebMethod
    public List<AccessDetailDTO> addSubscription(@WebParam(name="subscriptionDTO")  SubscriptionDTO subscriptionDTO)
            throws SDLBusinessException;

    @WebMethod
    public PayPalDTO reactivateCancelledSubscription(
        @WebParam(name="userName") String userName,
        @WebParam(name="existingUserAccessId") Long existingUserAccessId)
        throws PaymentGatewayUserException, PaymentGatewaySystemException;

    @WebMethod
    public List<RecurTx> getRecurTransactionsByNode(
        @WebParam(name="userName") String userName,
        @WebParam(name="nodeName") String nodeName);

    @WebMethod
    public List<RecurTx> getRecurTxDetail(
        @WebParam(name="userName") String userName,
        @WebParam(name="recurTxRefNum") String recurTxRefNum);

    @WebMethod
    public List<AccessDetailDTO> getAccessDetails(@WebParam(name = "accessId") Long accessId);

    /**END OF Recurring Transaction APIS **/

    /**Start of Web Transactions API **/
    @WebMethod
    public List<PayAsUGoTxView> 
    getPayAsUGoTransactionsByNode(@WebParam(name="transactionRequestDTO") TransactionRequestDTO request);

    @WebMethod
    public PageRecordsDTO 
    getPayAsUGoTransactionsByNodePerPage(@WebParam(name="transactionRequestDTO") TransactionRequestDTO request);

    @WebMethod
    public PayAsUGoTx getPayAsUGoTransactionDetail(
        @WebParam(name="userName") String userName,
        @WebParam(name="recTxId") Long recTxId,
        @WebParam(name="isRefund") String isRefund);

    @WebMethod
    public List<PayAsUGoTx> doSalePayAsUGo(
        @WebParam(name="userName") String userName,
        @WebParam(name="payAsUGoTransactionDTO") PayAsUSubDTO payAsUGoTransactionDTO)
        throws PaymentGatewayUserException, PaymentGatewaySystemException, SDLBusinessException;

    @WebMethod
    public List<WebTxItem> doSaleGetInfoWEB(
        @WebParam(name="siteName") String siteName,
        @WebParam(name = "itemList") List<WebTxItem> itemList)
        throws SDLBusinessException;

    @WebMethod
    public List<ShoppingCartItem> doSalePayAsUGoInfo(@WebParam(name="payAsUGoTransactionDTO") PayAsUSubDTO payAsUGoTransactionDTO)
        throws SDLBusinessException;

    @WebMethod
    public PayAsUGoTxItem getPayAsUGoTxIdForPurchasedDoc(
        @WebParam(name="userName") String userName,
        @WebParam(name="productKey") String productKey,
        @WebParam(name="uniqueIdentifier") String uniqueIdentifier);

    @WebMethod
    public List<ShoppingCartItem> getShoppingBasketItems(
        @WebParam(name="userName") String userName,
        @WebParam(name="nodeName") String nodeName);


    @WebMethod
    public void deleteShoppingCartItem(@WebParam(name="shoppingCartItem") ShoppingCartItem shoppingCartItem);

    @WebMethod
    public void saveShoppingCartItem(@WebParam(name="shoppingCartItem") ShoppingCartItem shoppingCartItem);

    /**End of Web Transactions API **/

    /**START Alerts Related API **/
    @WebMethod
    public List<UserAlert> getUserAlertsByUserName(
        @WebParam(name="userName") String userName,
        @WebParam(name="nodeName") String nodeName);

    @WebMethod
    public void saveUserAlert(
        @WebParam(name="userAlert") UserAlert userAlert)
        throws UserNameNotFoundException, DuplicateAlertException, MaximumNumberOfAlertsReachedException;

    @WebMethod
    public void deleteUserAlerts(@WebParam(name="userAlertDTO") UserAlertDTO userAlertDTO);
    /**END Alerts Related API **/

    @WebMethod
    public List<Code> getCodes(@WebParam(name="codeCategory") String codeCategory);

    @WebMethod
    public CreditCard getCreditCardDetailsByUserName(@WebParam(name = "userName") String userName) ;

    @WebMethod
    public CreditCard getCreditCardDetails(@WebParam(name="userId") Long userId);

    @WebMethod
    public List<Term> getNewTermsAndConditionsforUser(@WebParam(name="userName") String userName,
                                                      @WebParam(name="nodeName") String nodeName);

    @WebMethod
    public void updateUserTerms(@WebParam(name="user") User user) throws UserNameNotFoundException;

    @WebMethod
    public void deleteCreditCard(@WebParam(name="userName") String userName) throws UserAccountExistsException;

    @WebMethod
    public User findUser(@WebParam(name="userName") String userName) throws UserNameNotFoundException;

    /** This Method Is Used To Add A Firm Level Subscription For A User. It Checks For Whether That Type Of Subscription Already
     * Exists For The Site Among All The Existing Subscriptions For The User.
     *
     * It also checks if subscription is firm level subscription and it does belong to admin user.
     *
     * @param request
     * @param adminUserName
     * @throws UserNameNotFoundException
     * @throws MaxUsersExceededException
     * @throws SDLBusinessException
     */
    @WebMethod
    public void addFirmUserAccess(@WebParam(name="firmLevelUserRequestDTO") FirmLevelUserRequestDTO firmLevelUserRequestDTO,
    							  @WebParam(name="adminUserName") String adminUserName)
    								throws UserNameNotFoundException, MaxUsersExceededException, SDLBusinessException;

    /**
     * This service adds the comments to the shopping cart
     * @param cart
     */
    @WebMethod
	public void updateShoppingCartComments(@WebParam(name="shoppingCartItem") ShoppingCartItem shoppingCartItem);
    
	@WebMethod
	public List<Location> getLocationsBySiteId(@WebParam(name="siteId") Long siteId);

	@WebMethod
	public Location getLocationByNameAndAccessName(@WebParam(name="locationName") String locationName, 
			@WebParam(name="locationName") String accessName);
	
}
