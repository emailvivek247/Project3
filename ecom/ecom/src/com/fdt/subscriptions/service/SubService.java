package com.fdt.subscriptions.service;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.ecom.dto.UserAccessDetailDTO;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.exception.AccessUnAuthorizedException;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.recurtx.dto.UpgradeDowngradeDTO;
import com.fdt.security.entity.User;
import com.fdt.security.exception.MaxUsersExceededException;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.subscriptions.dto.AccessDetailDTO;
import com.fdt.subscriptions.dto.SubscriptionDTO;

public interface SubService {

    /** This Method Is Used To Add A Subscription For A User. It Checks For Whether That Type Of Subscription Already
     * Exists For The Site Among All The Existing Subscriptions For The User.
     * @param subscriptionDTO subscriptionDTO
     * @return
     * @throws SDLBusinessException
     */
    public List<AccessDetailDTO> addSub(SubscriptionDTO subscriptionDTO) throws SDLBusinessException;

    /** This Method Is Used In AccountInformation Controller. It Gets All The Subscriptions Which User Has Subscribed
     * Irrespective Of Payment Status. If An Optional SiteName Is Passed Then Subscriptions Belonging To Only That
     * Particular Site Are Returned.
     *
     * If paidSubscriptionsOnly is set to 'Y' then only paid subscriptions are returned.
     * If paidSubscriptionsOnly is passed null  it will return both paid/unpaid subscriptions
     *
     *  firmAdminSubscriptionsOnly : If this flag is set to "Y" it will fetch only firm level admin subscriptions. In such 
     * case the username passed must be admin to retrieve the firm level subscriptions 
     * 
     * Particular Site Are Returned.
     * @param userName EmailId Of The User Logged In.
     * @param nodeName Name Of The Node.
     * @param siteName Name Of The Site.
     * @param paidSubscriptionsOnly flag for retrieveing paid subscriptions
     * @param firmAdminSubscriptionsOnly for retreiving firm level admin subscriptions
     * @return List Of SubscriptionDTOs.
     */
    @Transactional(readOnly = true)
    public List<SubscriptionDTO> getUserSubs(String userName, String nodeName, String siteName, 
    		boolean activeSubscriptionsOnly, boolean firmAdminSubscriptionsOnly);

    /** This Method Is Used to Cancel The Subscription.
     * If UserAccount Associated With The Subscription Is Active, Then The MarkForCancellation Flag Is Set To Active. In
     * This Case, Scheduler Archives This Subscription After Subscription Ends, Unless User Again Reactivates It.
     * If UserAccount Associated With The Subscription Is Not Active, Then This Method Simply Deletes The UserAccount
     * & UserAccess Associated With The Subscription.
     * Finally Cancel Subscription E-Mail Is Sent.
     * @param userName EmailId Of The User Logged In.
     * @param userAccessId UserAccess Id.
     * @return PayPalDTO
     * @throws PaymentGatewaySystemException
     * @throws SDLBusinessException
     */
    public PayPalDTO cancelSub(String userName, Long userAccessId) throws PaymentGatewaySystemException,
    	SDLBusinessException;

    /** This Method Is Called From removeaccess Controller In ecomadmin To Remove A Subscription Of A User. Apart from
     * Deleting userAccount & userAccess Associated With The User, UserHistory with accessId, EMailId Is Saved For
     * Tracking The History Of The Subscriptions Which The User Has Added.
     * @param userName EmailId Of The User Logged In.
     * @param userAccessId User Access Id.
     * @param modifiedBy Used who tried to remove the Subscription.
     * @param comments Comments Entered By User while removing the Subscription.
     * @param sendEmail flag to indicate whether removeSubscription E-mail Should Be Sent or Not.
     * @return PayPalDTO
     * @throws PaymentGatewaySystemException
     * @throws SDLBusinessException
     */
    public PayPalDTO removeSub(String userName, Long userAccessId, String modifiedBy, String comments,
            boolean sendEmail) throws PaymentGatewaySystemException, SDLBusinessException;

    /** This Method Is Used To Reactivate The Existing Cancelled Subscription. What It Does Is, It Enables
     * MarkForCancellation Flag And Sends Reactivation E-Mail To User.
     * @param userName EmailId Of The User Logged In.
     * @param existingUserAccessId
     * @return PayPalDTO
     * @throws PaymentGatewayUserException
     * @throws PaymentGatewaySystemException
     */
    public PayPalDTO reactivateCancelledSub(String userName, Long existingUserAccessId)
            throws PaymentGatewayUserException, PaymentGatewaySystemException;

    /** This Method Is Used In getSubscriptionDetails Controller. And, This Controller Allows User To Cancel Or Change
     * His Subscription.
     * @param userName EmailId Of The User Logged In.
     * @param accessId
     * @return
     */
    public SubscriptionDTO getSubDetailsForUser(String userName, Long accessId);

    /** This Method Returns The Subscription Information Which Includes Information About Site, Merchant, CreditUsageFee,
     *  SubscriptionFee, and Access.
     * @param accessId Access Id.
     * @return  List Of AccessDetailDTO's
     */
    public List<AccessDetailDTO> getSubDetailsByAccessId(Long accessId);

    /** This Method Is Used For Authorizing A Subscription.
     * @param userAccessId UserAccess Id Which Is Going To Be Authorized.
     * @param isAuthorized Flag Used To Authorize The Subscription.
     * @param modifiedBy E-Mail Id Of The Person Who Is Trying To Authorize The Subscription.
     */
    public void authorize(Long userAccessId, boolean isAuthorized, String modifiedBy);

    /**
     * This Method Is Used To Pay For The Subscriptions Which User Has Added, But Not Paid For.
     * This Method First, After Getting All The Subscriptions Which User Has Added But Not Paid For, Looks For At Least
     * One Authorized Subscription. If It Does Not Exist, AccessUnAuthorizedException Is Thrown.
     * Next, If NewCreditCardInformation Is Not Null And User Does Not Have A Card Then It Tries To Use
     * NewCreditCardInformation To Pay For Authorized Subscriptions.
     * If NewCreditCardInformation Is Not Null And User Already Has Card Then It Tries To Use
     * NewCreditCardInformation To Pay For Authorized Subscriptions & Updates CreditCard Details In Database Accordingly.
     * And, If NewCreditCardInformation Is Null, Then The Method Uses The CreditCardInformation Which User Already Has
     * To Pay.
     * If Payment Is Successful, Then UserAccount Is Saved With LastBillingDate And NextBillingDate, Access Is Enabled And
     * Recurring Transaction Is Saved, Payment Successful E-Mail Is Sent.
     * If Payment Is Unsuccessful, Loop Skips & Continues With The Next Subscription.
     * @param newCreditCardInformation Credit Card Of The User.
     * @param userName EmailId Of The User Logged In.
     * @param nodeName Name Of the Node.
     * @param machineName Used To Log From What Machine, The User Paid For.
     * @return List Of PayPalDTOs.
     * @throws AccessUnAuthorizedException thrown when the subscription you are paying for is not authorized.
     * @throws SDLBusinessException
     */
    public List<PayPalDTO> payRecurSub(CreditCard creditCardDetails, String username,  String nodeName,
            String machineName) throws AccessUnAuthorizedException, SDLBusinessException;

    /** This Method Is Used In checkSubscription Controller. It Gets All The Subscriptions Which User Has Subscribed But
     * Not Paid For.
     * @param userName EmailId Of The User Logged In.
     * @param nodeName Name Of The Node.
     * @return user
     */
    public User getPaidUnpaidRecurSubByUser(String userName, String nodeName);

    /** This Method Is Used While Changing (Upgarding/Downgrading) The Subscription. This Method After Getting The
     * Details Of Current & New Subscription, Determines Whether The Change Is Upgrade Or Downgrade.
     * If Current Subscription Is Active & Paid,
     *  UnusedBalance Is Calculated  Which Is: (existingSubscriptionFee Per Day) * RemainingDays.
     *  NewBalance Is Calculated Which Is: (NewAccessSubscriptionFee Per Day) * RemainingDays. (If New Access Does not
     *  Require Authorization)
     *  In Case Of Upgrade,
     *   DowngradeFee Is 0.0d.
     *  In Case Of Downgrade,
     *   A DowngradeFee & CardUsageFee Depending On Site Is Charged.
     * Else
	 * UnusedBalance Is 0.0d.
	 * NewBalance Is NewAccessSubscriptionFee.
     * @param existingUserAccessId Existing UserAccessId
     * @param accessId New AccessId
     * @param userName EmailId Of The User Logged In.
     * @return UpgradeDowngradeDTO
     * @throws SDLBusinessException
     */
    public UpgradeDowngradeDTO getRecurChangeSubInfo(Long userAccessId, Long accessId, String userName)
        throws SDLBusinessException, MaxUsersExceededException;

    /** This Method Is Used To Change From One Recurring to Another Recurring Subscription. Four Possible Cases:
     * (i) changeFromCurrentlyPaidToRestrictedSubscription (ii) changeFromCurrentlyPaidToUnrestrictedSubscription
     * (iii) changeFromCurrentlyUnPaidToRestrictedSubscription,
     * and (iv) changeFromCurrentlyUnPaidToUnrestrictedSubscription.
     * @param existingUserAccessId
     * @param newAccessId
     * @param userName EmailId Of The User Logged In.
     * @param machineName
     * @return
     * @throws PaymentGatewaySystemException
     * @throws PaymentGatewayUserException
     * @throws SDLBusinessException
     */
    public UpgradeDowngradeDTO changeFromRecurToRecurSub(Long userAccessId, Long accessId, String userName,
            String machineName) throws PaymentGatewaySystemException, PaymentGatewayUserException, 
            SDLBusinessException, MaxUsersExceededException;

    public UserAccessDetailDTO getUserAccessDetails(Long userAccessId);
    

    /** This Method Is Used To Add A Firm Level Subscription For A User. It Checks For Whether That Type Of Subscription Already
     * Exists For The Site Among All The Existing Subscriptions For The User.
     * 
     * It also checks if subscription is firm level subscription and it does belong to admin user.
     * 
     * @param adminUserName
     * @param firmUserName
     * @param accessId
     * @return
     * @throws SDLBusinessException
     */
    public void addFirmUserAccess(String adminUserName, String firmUserName, Long accessId, String nodeName) 
    			throws UserNameNotFoundException, MaxUsersExceededException, SDLBusinessException;

    /**
     * This method enables or disables the firm level user access
     *   
     *   
     * @param firmLevelUserName
     * @param userAccessId
     * @param isEnable
     * @param modifiedBy
     * @param comments
     * @throws UserNameNotFoundException
     * @throws SDLBusinessException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void enableDisableFirmLevelUserAccess(String firmLevelUserName, Long userAccessId,
			boolean isEnable, String modifiedBy, String comments)
    										throws  UserNameNotFoundException, SDLBusinessException ;
    
    
    /**
     * Remove  Firm Level Access
     * 
     * There following Scenarios Possible
     * 
     * 1. User has paid transactions for the given access 
     * 			Action : Throw an error that user can not be deleted : It can be enabled/disabled
     * 2. User has no paid transactions of any access under this firm
     * 			Action : Delete the user access
     * 
     * 
     * @param firmLevelUser
     * @param accessId
     * @param comment
     * @param modifiedBy
     * @param isSendNotification
     * 
     */
    @Transactional(readOnly = true)
    public void removeFirmLevelUserAccess(String firmUserName, Long accessId, String comments, String modifiedBy,
    		boolean isSendNotification)	throws  UserNameNotFoundException, SDLBusinessException ;
    
    /**
     * Change the Firm Administrator for a given access
     * 
     *  Steps:
     *  1. Change User Access of new admin user by setting isFirmAdminAccess = 'Y' 
     *  2. Change all other firm users by setting isFirmAdminAccess = 'N' 
     *  											and FirmAdminUserAccessId = newAdminUser's user access id
     * 
     * @param currentAdminUserName
     * @param newAdminUserName
     * @param accessId
     * @param comments
     * @param modifiedBy
     */
    public void ChangeFirmSubscriptionAdministrator(String newAdminUserName, Long accessId,
    		String comments, String modifiedBy) throws UserNameNotFoundException, SDLBusinessException; 

}
