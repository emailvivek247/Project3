package com.fdt.security.service;

import java.util.List;

import com.fdt.common.dto.ServiceResponseDTO;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.Term;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.security.dto.FirmUserDTO;
import com.fdt.security.entity.User;
import com.fdt.security.entity.UserEvent;
import com.fdt.security.exception.BadPasswordException;
import com.fdt.security.exception.InvalidDataException;
import com.fdt.security.exception.MaxUsersExceededException;
import com.fdt.security.exception.UserAlreadyActivatedException;
import com.fdt.security.exception.UserNameAlreadyExistsException;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.security.exception.UserNotActiveException;

public interface UserService {

    /** This Method Is Used To Register A New User. Before Registering, It Checks Whether The User With EmailId Already
     *  Exists In The System. If Already Exists, Then The Method Throws UserNameAlreadyExistsException With The SiteNames
     *  On Which User Has Subscription. After user, userAccess, userTerm is Saved, an Activation E-Mail Is sent.
     * @param aNewUser newUser.
     * @param siteId Site Id.
     * @param accessId Id Of Access Which User Is Trying To Register.
     * @param nodeName Name Of The Node.
     * @param requestURL prefix URL For Activation E-Mail.
     * @throws UserNameAlreadyExistsException
     */
    public void registerUser(User aNewUser, Long siteId, Long accessId, String nodeName, String requestURL)
        throws UserNameAlreadyExistsException;

    /**
     * This method will add the user to an existing firm.
     * A new user to be added is passed along with the admin user of the firm. The following validations are performed before adding
     *
     * 1. Only Firm Administrator can add user
     * 2. Administrator should have paid for the subscription.
     * 3. Administrator has not subscribed & paid for Access Ids supplied by users (accessIds parameter)
     * 4. Check for Max Users allowed.
     *
     * Service Returns the response with message if User added or User existed and subscriptions are added to the user.
     *
     * @param newUser
     * @param adminUserName
     * @param accessId
     * @param nodeName
     * @param requestURL
     * @return
     * @throws UserNameNotFoundException
     * @throws MaxUsersExceededException
     * @throws SDLBusinessException
     */
    public ServiceResponseDTO addFirmLevelUser(User newUser, String adminUserName, Long accessId, String nodeName,
    		String requestURL) throws UserNameNotFoundException,	MaxUsersExceededException, SDLBusinessException ;


    /**
     * This method return all the users for a given firm (admin user id):
     * If subscription (accessId is supplied then it will find the users under a given subscriptions
     *
     * @param adminUserName
     * @param accessId
     * @return
     */
    public List<FirmUserDTO> getFirmUsers(String adminUserName, Long accessId);




    	/** This Method Is Used To Change The Password. PasswordEncoder's isPasswordValid Method Is Used To Check
     * Existing Password Entered By The User & Password In The Database. If Both Are Not Same, BadPasswordException Is
     * Thrown. Then The New Password Is Encoded And Updated In The Database.
     * @param user
     * @throws UserNameNotFoundException
     * @throws BadPasswordException
     */
    public void changePassword(User user) throws UserNameNotFoundException, BadPasswordException;

    /** This Method Is Used To Activate The User.
     * @param userName E-MailId Of User Who Needs To Be Activated.
     * @param requestToken Random Generated String.
     * @throws InvalidDataException
     * @throws UserAlreadyActivatedException
     */
    public void activateUser(String userName, String requestToken) throws InvalidDataException,
    	UserAlreadyActivatedException;

    /** This Method Is Used To Lock Or Unlock A User. If It Locks A User, The Method Will Send LockConfirmation E-mail, And
     * If It Unlocks A User, The Method Will Send UnLockConfirmation E-Mail.
     * @param userName EmailId Of The User Who Will Be Locked/Unlocked.
     * @param isLock flag Indicating Whether To Lock Or Unlock.
     * @param modifiedBy Admin User Who Tried To lock/unlock.
     * @param isSendUserConfirmation flag To Indicate Whether To Send A Mail After Performing lock/unlock Operation.
     * @param nodeName Name Of The Node.
     * @param additionalComments Comments Entered By The User, To Do lock/unlock Operation.
     */
    public void lockUnLockUser(String userName, boolean isLock, String modifiedBy, boolean isSendUserConfirmation,
        String nodeName, String additionalComments);

    /** This Method Is Used To Reset Password. It Checks (i) Whether User Exists In The System (ii) And, Whether User
     * Is Active.
     * @param user
     * @param requestToken Random Generated String.
     * @throws InvalidDataException
     */
    public void resetPassword(User user, String requestToken) throws UserNameNotFoundException, InvalidDataException;

    /** This Method Is Used To Update A User Entity. It Is Called From UpdatePersonalInformation Controller.
     * @param modifiedUser User Entity Which will be updated
     * @param modifiedBy Person who is trying to update.
     * @throws UserNameNotFoundException
     */
    public void updateUser(User modifiedUser, String modifiedBy) throws UserNameNotFoundException;

    /** This Method Is Used For Performing ResetPasswordRequest E-Mail. Before Sending The ResetPassword E-Mail,
     * It Checks (i) Whether User Exists In The System (ii) And, Whether User Is Active.
     * @param userName E-Mail Id Of The User Who Requested For Reset Password Link.
     * @param nodeName Name Of the Node.
     * @param requestURL Prefix Of resetPassword Link.
     * @throws UserNameNotFoundException
     * @throws UserNotActiveException
     */
    public void resetPasswordRequest(String userName, String nodeName, String requestURL)
        throws UserNameNotFoundException, UserNotActiveException;

    /** This Method Send Activation E-mail Upon The Request Of User.It Checks (i) Whether User Exists (i) And If User Exists,
     * Then Checks For Whether He Is Active Or Not.
     * @param userName E-MailId Of User Who Needs To Be Activated.
     * @param nodeName Name Of The Node.
     * @param requestURL Prefix Of resendUserActivationEmail Link.
     * @throws UserAlreadyActivatedException
     * @throws UserNameNotFoundException
     */
    public void resendUserActivationEmail(String userName, String nodeName, String requestURL)
        throws UserAlreadyActivatedException, UserNameNotFoundException;

    /** This Method Takes UserName & NodeName As Input Parameter And Returns The Whole User Object If User Exists, Otherwise
     * It Will Be Null.
     * @param userName E-Mail Id Of The User Whose Details Are To Be Loaded.
     * @param nodeName Name Of The Node.
     * @return A User Object.
     */
    public User loadUserByUsername(String userName, String nodeName);

    /** This Method Is Used To Verify Whether The Reset Password Link Clicked Is Correct Or Not.
     * @param userName E-Mail Id Of The User Who Requested For Reset Password Link.
     * @param requestToken Random Generated String.
     * @throws InvalidDataException
     */
    public void checkValidResetPasswordRequest(String userName, String requestToken)  throws InvalidDataException;

    /** This Method Updates The Last Login Time And Is Called Whenever User Logs In. It Is Called From
     * SDLDAOAuthenticationProvider.
     * @param userName
     * @throws UserNameNotFoundException
     */
    public void updateLastLoginTime(String userName) throws UserNameNotFoundException;

    /** This Method Is Used To Get Terms Of Site. When User Is Trying To Add A New Subscription Of Site Which User Does
     * Not Belong To Previously.
     * @param userName E-Mail Id Of User Who Logged In.
     * @param nodeName Name Of The Node.
     * @return
     */
    public List<Term> getNewTermsAndConditionsforUser(String userName, String nodeName);

    /** This Method Is Used For Updating The User With New Terms Related To Site.
     * @param user
     * @throws UserNameNotFoundException
     */
    public void updateUserTerms(User user) throws UserNameNotFoundException ;

    /** This Method Updates The Credit Card Information of a User.
     * @param userName Email Id Of The User Whose Credit Card Information Needs To Be Updated.
     * @param modifiedBy Email Id Of The User Who Is Trying To Update.
     * @param newCreditCardInformation New Credit Card Information.
     * @throws PaymentGatewaySystemException
     * @throws PaymentGatewayUserException
     */
    public void updateExistingCreditCardInformation(String userName, String modifiedBy, CreditCard newCreditCardInformation)
        throws PaymentGatewaySystemException, PaymentGatewayUserException;

    /** This Method Gets The Credit Card Information of a User.
     * @param username Email Id Of The User Whose Credit Card Information Needs To Be Pulled.
     * @return Credit Card Information.
     */
    public CreditCard getCreditCardDetails(String username);

    /** This Method Gets The Credit Card Information of a User.
     * @param userId Id Of The User Whose Credit Card Information Needs To Be Pulled.
     * @return Credit Card Information.
     */
    public CreditCard getCreditCardDetails(Long userId);

    
    public UserEvent findUserEvent(String userName);

    /**
     * Find User by User name
     *
     * @param userName
     * @return
     * @throws UserNameNotFoundException
     */
    public User findUser(String userName) throws UserNameNotFoundException;
    
    public int getFirmUsersCount(Long adminUserId, Long accessId);

    
}
