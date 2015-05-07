package com.fdt.security.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fdt.common.dto.ServiceResponseDTO;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.ecom.dao.EComDAO;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.NodeConfiguration;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.SiteConfiguration;
import com.fdt.ecom.entity.Term;
import com.fdt.ecom.entity.UserTerm;
import com.fdt.email.EmailProducer;
import com.fdt.payasugotx.dao.PayAsUGoTxDAO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.security.dao.UserDAO;
import com.fdt.security.dto.FirmUserDTO;
import com.fdt.security.entity.Access;
import com.fdt.security.entity.User;
import com.fdt.security.entity.UserAccess;
import com.fdt.security.entity.UserEvent;
import com.fdt.security.entity.enums.AccessType;
import com.fdt.security.exception.BadPasswordException;
import com.fdt.security.exception.InvalidDataException;
import com.fdt.security.exception.MaxUsersExceededException;
import com.fdt.security.exception.UserAlreadyActivatedException;
import com.fdt.security.exception.UserNameAlreadyExistsException;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.security.exception.UserNotActiveException;
import com.fdt.security.service.validator.FirmUserSubscriptionValidator;
import com.fdt.subscriptions.dao.SubDAO;
import com.fdt.subscriptions.dto.AccessDetailDTO;

@Service("userService")
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private EComDAO eComDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PayAsUGoTxDAO payAsUGoTxDAO;

    @Autowired
    private SubDAO subDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailProducer emailProducer;

    @Autowired
    private FirmUserSubscriptionValidator firmUserValidator;

    @Value("${ecommerce.serverurl}")
    private String ecomServerURL = null;

    @Autowired
    @Qualifier("serverMessageSource")
    private MessageSource messages = null;

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
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void registerUser(User newUser, Long siteId, Long accessId, String nodeName, String requestURL)
            throws UserNameAlreadyExistsException {
        Assert.notNull(newUser, "newUser Cannot be Null");
        Assert.notNull(siteId, "siteId Cannot be Null");
        Assert.notNull(accessId, "accessId Cannot be Null");
        Assert.hasLength(nodeName, "nodeName Cannot be Null/Empty");
        Assert.hasLength(requestURL, "requestURL Cannot be Null/Empty");
        logger.debug("The New User Registeration Details are {}", newUser);
        String userName = newUser.getUsername();
        User existingUser = this.userDAO.getUserDetails(userName, null);
        logger.debug("Printing Existing user Details {}", existingUser);
        if (existingUser != null) {
        	List<String> registeredSiteList = this.userDAO.getSitesForUser(userName);
            if (registeredSiteList != null && registeredSiteList.size() > 0) {
                String siteNameList = "";
                StringBuilder registeredSiteNames = new StringBuilder();
                for (String registeredSiteName : registeredSiteList) {
                    registeredSiteNames.append(registeredSiteName + ",  ");
                }
                if(registeredSiteNames != null) {
                    siteNameList = registeredSiteNames.toString();
                    int length = siteNameList.length();
                    siteNameList = siteNameList.substring(0, length-3);
                }
                throw new UserNameAlreadyExistsException(this.getMessage("security.user.alreadyexistinsite",
                    new String[]{userName, siteNameList}));
            } else {
                throw new UserNameAlreadyExistsException(this.getMessage("security.user.alreadyexist",
                    new String[]{userName}));
            }
        }
        String encodedPassword = this.passwordEncoder.encodePassword(newUser.getPassword(), null);
        newUser.setPassword(encodedPassword);
        Site site = this.eComDAO.getSiteDetails(siteId);
        if (!site.isAutoActivate()) {
            newUser.setAccountNonLocked(false);
        } else {
            newUser.setAccountNonLocked(true);
        }
        newUser.setRegisteredNode(nodeName);
        newUser.setCreatedDate(new Date());
        newUser.setModifiedDate(new Date());
        this.userDAO.saveUser(newUser);
        List<Access> existingAccessList = this.userDAO.getAccess();
        List<UserAccess> userAccessList = new LinkedList<UserAccess>();
        for(Access existingAccess: existingAccessList) {
            if (existingAccess.isGuestFlg() || existingAccess.getId().longValue() == accessId.longValue()) {
                UserAccess userAccess = new UserAccess(newUser, existingAccess);
                if(existingAccess.isAuthorizationRequired()){
                    userAccess.setAuthorized(false);
                } else {
                    userAccess.setAuthorized(true);
                }
                /** If Access Is 'Default' OR is Not a Recurring Subscription set the Active Flag to True **/
                if (existingAccess.isGuestFlg() ||
                    ((existingAccess.getAccessType() != null
                        && existingAccess.getAccessType() != AccessType.RECURRING_SUBSCRIPTION)) ) {
                        userAccess.setActive(true);
                }
                userAccess.setModifiedBy(newUser.getUsername());
                userAccess.setCreatedBy(newUser.getUsername());
                userAccess.setModifiedDate(new Date());
                userAccess.setCreatedDate(new Date());
                if(existingAccess.isFirmLevelAccess() && existingAccess.getSubscriptionFee().getFee() == 0.0){
                	userAccess.setFirmAccessAdmin(true);
                	userAccess.setActive(true);
                }
                
                userAccessList.add(userAccess);
            }
        }
        this.userDAO.saveUserAcess(userAccessList);
        UserTerm userTerm = new UserTerm(newUser, site.getTerm());
        userTerm.setModifiedBy(newUser.getUsername());
        userTerm.setCreatedBy(newUser.getUsername());
        userTerm.setModifiedDate(new Date());
        userTerm.setCreatedDate(new Date());
        userTerm.setActive(true);
        this.eComDAO.saveUserTerm(userTerm);
        NodeConfiguration nodeConfig = this.eComDAO.getNodeConfiguration(nodeName);
        Assert.notNull(nodeConfig, "nodeConfig Cannot be Null");
        String controllerURL = requestURL + "publicActivateUser.admin?token=";
        String emailTemplateFile = nodeConfig.getEmailTemplateFolder() + nodeConfig.getUserActivationEmailTemplate();
        this.sendUserMail(newUser, nodeConfig.getFromEmailAddress(), nodeConfig.getUserActivationSubject(),
        		emailTemplateFile, controllerURL, null);
    }

    /**
     * This method will add the user to an existing firm.
     * A new user to be added is passed along with the admin user of the firm. The following validations are performed
     * before adding
     *
     * 1. Only Firm Administrator can add user
     * 2. Administrator should have paid for the subscription.
     * 3. Administrator has not subscribed & paid for Access Ids supplied by users (accessIds parameter)
     * 4. Check for Max Users allowed.
     *
     * Service Returns the response with message if User added or User existed and subscriptions are added to the user.
     *
     * @param newFirmUser
     * @param adminUserName
     * @param accessId
     * @param nodeName
     * @param requestURL
     * @return
     * @throws UserNameNotFoundException
     * @throws MaxUsersExceededException
     * @throws SDLBusinessException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public ServiceResponseDTO addFirmLevelUser(User newFirmUser, String adminUserName, Long accessId, String nodeName,
    		String requestURL) throws UserNameNotFoundException,	MaxUsersExceededException, SDLBusinessException {

    	ServiceResponseDTO response = new ServiceResponseDTO();
    	Assert.notNull(newFirmUser, "newUser cannot be Null");
    	Assert.notNull(newFirmUser.getUsername(), "User Name cannot be Null");
    	Assert.notNull(newFirmUser.getFirstName(), "First Name cannot be Null");
    	Assert.notNull(newFirmUser.getLastName(), "Last Name cannot be Null");
    	Assert.notNull(newFirmUser.getPhone(), "Phone cannot be Null");
        Assert.notNull(accessId, "accessId cannot be Null");
        Assert.hasLength(nodeName, "nodeName cannot be Null/Empty");
        Assert.hasLength(requestURL, "requestURL cannot be Null/Empty");
        logger.debug("The New User Registeration Details are {}", newFirmUser);

        // Load The Firm Level Admin User from database and Check if the Admin User Exist in the DB
        User adminUser = this.userDAO.getUser(adminUserName);
        if(adminUser == null) {
        	throw new UserNameNotFoundException(this.getMessage("security.username.notfound", new String[]{adminUserName}));
        }

        // Retrieve Existing Accesses from the database. userDAO.getAccess() is cacheable
        List<Access> existingAccessList = this.userDAO.getAccess();

        // Retrieve firm level user if it already exists in the database
        User user = this.userDAO.getUser(newFirmUser.getUsername());

        Access firmLevelSubscribedAccess = null;
        // Iterate the Existing Acesss and get the Access for the Firm Level Subscribed user
		for(Access existingAccess : existingAccessList){
			if(accessId.equals(existingAccess.getId())) {
				firmLevelSubscribedAccess = existingAccess;
				break;
			}
		}

        // Retrieve the site from database.
        List<AccessDetailDTO> accessDetails = this.subDAO.getSubDetailsByAccessId(accessId);
        Site site = accessDetails.get(0).getSite();

        // Validate the Firm User & Subscriptions
        this.firmUserValidator.validateSubscriptions(firmLevelSubscribedAccess, existingAccessList,
        		adminUser, user, newFirmUser.getUsername(), site, nodeName);

        boolean userExists = false;
        //If the user is a new User You have to add the User else we need to add Just Subscriptions
        String password = this.generateRandomPassord();
        if(user == null) {
            newFirmUser.setPassword(this.passwordEncoder.encodePassword(password, null));
            newFirmUser.setAccountNonLocked(site.isAutoActivate());
            newFirmUser.setRegisteredNode(nodeName);
            // Save the user
            newFirmUser.setCreatedDate(new Date());
            newFirmUser.setModifiedDate(new Date());
            this.userDAO.saveUser(newFirmUser);
        }   else {
        	userExists = true;
        	newFirmUser = user;
        }


        // Add the access to the new/existing user
        this.addUserAccessToFirmUser(accessId, existingAccessList, adminUser.getUserAccessList(), userExists, newFirmUser);

        NodeConfiguration nodeConfig = this.eComDAO.getNodeConfiguration(nodeName);
        Assert.notNull(nodeConfig, "nodeConfig Cannot be Null");
        if(!userExists){
        	// Send Activation Email
	        String controllerURL = requestURL + "publicActivateUser.admin?token=";
	        String emailTemplateFile = nodeConfig.getEmailTemplateFolder() + nodeConfig.getUserActivationEmailTemplate();
	        this.sendUserMail(newFirmUser, nodeConfig.getFromEmailAddress(), nodeConfig.getUserActivationSubject(),
	        	emailTemplateFile, controllerURL, password);
        } else {
        	// Send Email about Subscription added
            Map<String, Object> emailData = new HashMap<String, Object>();
            emailData.put("user", newFirmUser);
            emailData.put("firmUserSubscription", site.getName() + " - " + firmLevelSubscribedAccess.getDescription());
            emailData.put("serverUrl", this.ecomServerURL);
            emailData.put("currentDate", new Date());
            // Retrieve Site Configuration from database
            SiteConfiguration siteConfig = this.eComDAO.getSiteConfiguration(site.getId());
            Assert.notNull(siteConfig, "siteConfig Cannot be Null");
            this.emailProducer.sendMailUsingTemplate(siteConfig.getFromEmailAddress(), newFirmUser.getUsername(),
                siteConfig.getAddSubscriptionSub(), siteConfig.getEmailTemplateFolder()
                    + siteConfig.getPaymentConfirmationTemplate(), emailData);
        }
        response.setStatus(ServiceResponseDTO.SUCCESS);
        if(userExists){
        	response.setMessage(this.getMessage("security.addfirmuser.userexistsandassignedaccess",
        		new String[]{newFirmUser.getUsername(), firmLevelSubscribedAccess.getDescription()}));
        } else {
        	response.setMessage(this.getMessage("security.addfirmuser.useraddedsuccessfully",
        		new String[]{newFirmUser.getUsername(), firmLevelSubscribedAccess.getDescription()}));
        }
        return response;
    }


    /**
     *  This method adds the access to the user.
     *
     * @param accessIds  -- The new access to be assigned
     * @param existingAccessList  -- all the accesses in the system  -- We need to retrieve this list so that we don't miss
     *  the guest access
     * @param adminUserAccessList  -- all the accesses that admin user has
     * @param userExists -- Flag to indicate if user exists
     * @param newUser  -- New User object
     *
     * @throws SDLBusinessException
     */
    private void addUserAccessToFirmUser(Long accessId, List<Access> existingAccessList,
    	List<UserAccess> adminUserAccessList, boolean userExists, User newUser)
    		throws SDLBusinessException {
        // Add the access to firm level user
        List<UserAccess> userAccessList = new LinkedList<UserAccess>();

        for(Access existingAccess: existingAccessList) {
            if (accessId.equals(existingAccess.getId())) {
           		// add admin access to firm level user
           		UserAccess userAccess = this.createFirmUserAccess(existingAccess, newUser, this.getUserAccessById
            					(adminUserAccessList, accessId));
                userAccessList.add(userAccess);
            } else if(existingAccess.isGuestFlg()){
            	// Guest access  , add it to the user if he is a new user. In case of existing user it will exist
            	if(!userExists){
            		UserAccess userAccess = this.createFirmUserAccess(existingAccess, newUser, this.getUserAccessById
            			(adminUserAccessList, accessId));
            		userAccessList.add(userAccess);
            	}
            }
        }
        this.userDAO.saveUserAcess(userAccessList);

    }

    private UserAccess createFirmUserAccess(Access existingAccess, User newUser, UserAccess adminUserAccess){
        UserAccess userAccess = new UserAccess(newUser, existingAccess);
        // Firm Users are always authorized
        userAccess.setAuthorized(true);
        /** If Access Is 'Default' OR is Not a Recurring Subscription set the Active Flag to True **/
        if (existingAccess.isGuestFlg() ||
            ((existingAccess.getAccessType() != null
                && existingAccess.getAccessType() != AccessType.RECURRING_SUBSCRIPTION)) ) {
                userAccess.setActive(true);
        }
        userAccess.setModifiedBy(newUser.getUsername());
        userAccess.setCreatedBy(newUser.getUsername());
        userAccess.setModifiedDate(new Date());
        userAccess.setCreatedDate(new Date());
        userAccess.setActive(true);
        userAccess.setFirmAccessAdmin(false);

        if (!existingAccess.isGuestFlg()){
            // Find out administrator user access id and set it
        	userAccess.setFirmAdminUserAccessId(adminUserAccess.getId());
        }

        return userAccess;

    }

    /**
     * This method return all the users for a given firm (admin user id):
     * If subscription (accessId is supplied then it will find the users under a given subscriptions
     *
     * @param adminUserName
     * @param accessId
     * @return
     */

    @Transactional(readOnly = true)
    public List<FirmUserDTO> getFirmUsers(String adminUserName, Long accessId){
        Assert.notNull(adminUserName, "userName Cannot be Null/Empty");
   		return this.userDAO.getFirmUsers(adminUserName, accessId);
    }


    	/** This Method Is Used To Change The Password. PasswordEncoder's isPasswordValid Method Is Used To Check
     * Existing Password Entered By The User & Password In The Database. If Both Are Not Same, BadPasswordException Is
     * Thrown. Then The New Password Is Encoded And Updated In The Database.
     * @param user
     * @throws UserNameNotFoundException
     * @throws BadPasswordException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void changePassword(User user) throws UserNameNotFoundException, BadPasswordException {
        Assert.notNull(user, "user Cannot be Null");
        User existingUser = userDAO.getUser(user.getUsername());
        if (existingUser == null) {
            throw new UserNameNotFoundException(this.getMessage("security.username.notfound",
                new String[]{user.getUsername()}));
        }
        logger.debug("Printing Existing user Details {}", existingUser);
        boolean isPasswordValid = this.passwordEncoder.isPasswordValid(existingUser.getPassword(),
                                    user.getExistingPassword(), null);
        if (!isPasswordValid) {
            throw new BadPasswordException(this.getMessage("security.password.doesnotmatch"));
        }
        String encodedChangedPassword = this.passwordEncoder.encodePassword(user.getPassword(), null);
        existingUser.setPassword(encodedChangedPassword);
        this.userDAO.updateUser(existingUser, user.getUsername());
    }

    /** This Method Is Used To Lock Or Unlock A User. If It Locks A User, The Method Will Send LockConfirmation E-mail, And
     * If It Unlocks A User, The Method Will Send UnLockConfirmation E-Mail.
     * @param userName EmailId Of The User Who Will Be Locked/Unlocked.
     * @param isLock flag Indicating Whether To Lock Or Unlock.
     * @param modifiedBy Admin User Who Tried To lock/unlock.
     * @param isSendUserConfirmation flag To Indicate Whether To Send A Mail After Performing lock/unlock Operation.
     * @param nodeName Name Of The Node.
     * @param additionalComments Comments Entered By The User, To Do lock/unlock Operation.
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void lockUnLockUser(String userName, boolean isLock, String modifiedBy, boolean isSendUserConfirmation,
                               String nodeName, String additionalComments) {
        Assert.hasLength(userName, "user Cannot be Null");
        Assert.hasLength(modifiedBy, "modifiedBy Cannot be Null/Empty");
        Assert.hasLength(nodeName,   "Node Name Cannot be Null/Empty");
        this.userDAO.lockUnLockUser(userName, isLock, modifiedBy);
        User user = this.loadUserByUsername(userName, null);
        if (isSendUserConfirmation) {
        	this.sendEmail(nodeName, isLock, user, additionalComments);
        }
    }

    /** This Method Is Used For Performing ResetPasswordRequest E-Mail. Before Sending The ResetPassword E-Mail,
     * It Checks (i) Whether User Exists In The System (ii) And, Whether User Is Active.
     * @param userName E-Mail Id Of The User Who Requested For Reset Password Link.
     * @param nodeName Name Of the Node.
     * @param requestURL Prefix Of resetPassword Link.
     * @throws UserNameNotFoundException
     * @throws UserNotActiveException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void resetPasswordRequest(String userName, String nodeName, String requestURL) throws UserNameNotFoundException,
        UserNotActiveException {
        Assert.hasLength(userName, "userName Cannot be Null/Empty");
        Assert.hasLength(requestURL, "requestURL Cannot be Null/Empty");
        User existingUser = userDAO.getUserDetails(userName, nodeName);
        logger.debug("Printing Existing user Details {}", existingUser);
        if (existingUser == null) {
            throw new UserNameNotFoundException(this.getMessage("security.resetpassword.userReset"));
        }
        String controllerURL = requestURL + "publicResetPassword.admin?token=";
        if (existingUser != null && existingUser.isActive()) {
            this.sendResetPasswordMail(existingUser, nodeName, controllerURL);
        } else {
            throw new UserNotActiveException(this.getMessage("security.user.notactive", new String[]{userName}));
        }
    }

    /** This Method Is Used To Verify Whether The Reset Password Link Clicked Is Correct Or Not.
     * @param userName E-Mail Id Of The User Who Requested For Reset Password Link.
     * @param requestToken Random Generated String.
     * @throws InvalidDataException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void checkValidResetPasswordRequest(String userName, String requestToken) throws InvalidDataException {
        Assert.notNull(userName, "user Cannot be Null");
        Assert.notNull(requestToken, "requestToken Cannot be Null");
        UserEvent userEvent = this.userDAO.findUserEvent(userName, requestToken);
        if(userEvent == null) {
            throw new InvalidDataException(this.getMessage("security.activationlink.invalid"));
        }
    }

    /** This Method Is Used To Reset Password. It Checks (i) Whether User Exists In The System (ii) And, Whether User
     * Is Active.
     * @param user
     * @param requestToken Random Generated String.
     * @throws InvalidDataException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void resetPassword(User user, String requestToken)  throws UserNameNotFoundException, InvalidDataException {
        Assert.notNull(user, "user Cannot be Null");
        Assert.hasLength(requestToken, "requestToken Cannot be Null");
        User existingUser = userDAO.getUserDetails(user.getUsername(), null);
        if (existingUser == null) {
            throw new UserNameNotFoundException(this.getMessage("security.user.notyetactive"));
        }
        UserEvent userEvent = this.userDAO.findUserEvent(user.getUsername(), requestToken);
        if(userEvent == null) {
            throw new InvalidDataException(this.getMessage("security.activationlink.invalid"));
        }
        String encodedPassword = this.passwordEncoder.encodePassword(user.getPassword(), null);
        this.userDAO.updatePassword(user.getUsername(), encodedPassword, user.getUsername());
        List<UserEvent> userEventList = new ArrayList<UserEvent>();
        userEventList.add(userEvent);
        this.userDAO.deleteUserEvents(userEventList);
    }

    /** This Method Is Used To Update A User Entity. It Is Called From UpdatePersonalInformation Controller.
     * @param modifiedUser User Entity Which will be updated
     * @param modifiedBy Person who is trying to update.
     * @throws UserNameNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void updateUser(User modifiedUser, String modifiedBy) throws UserNameNotFoundException {
        Assert.notNull(modifiedUser, "modifiedUser Cannot be Null/Empty");
        Assert.hasLength(modifiedBy, "modifiedBy Cannot be Null/Empty");
        this.userDAO.updateUser(modifiedUser, modifiedBy);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void updateFirmLevelUser(User modifiedUser, String modifiedBy) throws UserNameNotFoundException {
        Assert.notNull(modifiedUser, "modifiedUser Cannot be Null/Empty");
        Assert.hasLength(modifiedBy, "modifiedBy Cannot be Null/Empty");
        this.userDAO.updateUser(modifiedUser, modifiedBy);
    }

    /** This Method Is Used To Activate The User.
     * @param userName E-MailId Of User Who Needs To Be Activated.
     * @param requestToken Random Generated String.
     * @throws InvalidDataException
     * @throws UserAlreadyActivatedException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void activateUser(String userName, String requestToken)
            throws InvalidDataException, UserAlreadyActivatedException {
        Assert.hasLength(userName, "userName Cannot be Null/Empty");
        Assert.hasLength(requestToken, "requestToken Cannot be Null/Empty");
        UserEvent userEvent = this.userDAO.findUserEvent(userName, requestToken);
        if(userEvent == null){
            throw new InvalidDataException(this.getMessage("security.activationlink.invalid"));
        }
        if (userEvent.getUser() != null && userEvent.getUser().isActive()) {
            throw new UserAlreadyActivatedException(this.getMessage("security.user.alreadyactivated",
                new String[]{userName}));
        }
        if (userEvent != null) {
            User user = userEvent.getUser();
            user.setActive(true);
            user.setModifiedDate(new Date());
            user.setModifiedBy(userName);
            this.userDAO.saveUser(user);
            List<UserEvent> userEvents = new ArrayList<UserEvent>();
            userEvents.add(userEvent);
            this.userDAO.deleteUserEvents(userEvents);
        }
    }

    /** This Method Send Activation E-mail Upon The Request Of User.It Checks (i) Whether User Exists (i) And If User Exists,
     * Then Checks For Whether He Is Active Or Not.
     * @param userName E-MailId Of User Who Needs To Be Activated.
     * @param nodeName Name Of The Node.
     * @param requestURL Prefix Of resendUserActivationEmail Link.
     * @throws UserAlreadyActivatedException
     * @throws UserNameNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void resendUserActivationEmail(String userName, String nodeName, String requestURL)
            throws UserNameNotFoundException, UserAlreadyActivatedException {
        Assert.hasLength(userName, "userName Cannot be Null/Empty");
        Assert.hasLength(requestURL, "RequestURL Cannot be Null/Empty");
        User user = this.loadUserByUsername(userName, nodeName);
        String controllerURL = requestURL + "publicActivateUser.admin?token=";
        if (user == null) {
            throw new UserNameNotFoundException(this.getMessage("security.email.notregistered", new String[]{userName}));
        }
        if (user.isActive()) {
            throw new UserAlreadyActivatedException(this.getMessage("security.user.alreadyactivated",
                new String[]{userName}));
        }
        NodeConfiguration nodeConfig = this.eComDAO.getNodeConfiguration(nodeName);
        Assert.notNull(nodeConfig, "nodeConfig Cannot be Null");
        String emailTemplateFile = nodeConfig.getEmailTemplateFolder() + nodeConfig.getUserActivationEmailTemplate();
        String subject = nodeConfig.getUserActivationSubject();
        String fromEMailAddress = nodeConfig.getFromEmailAddress();
        UserEvent userEvent = this.userDAO.findUserEvent(userName);
        //This could Happen if the BackGround Job runs and deletes the User Events.
        if (userEvent == null) {
            this.sendUserMail(user, fromEMailAddress, subject, emailTemplateFile, controllerURL, null);
        } else {
            userEvent.setEmailTemplateFile(emailTemplateFile);
            userEvent.setSubject(subject);
            userEvent.setControllerURL(controllerURL);
            userEvent.setFromEMailAddress(fromEMailAddress);
            this.emailProducer.sendMailUsingTemplate(userEvent, null);
            this.userDAO.updateModifiedDateOfUserEvent(userEvent.getId());
        }

    }

    /** This Method Takes UserName & NodeName As Input Parameter And Returns The Whole User Object If User Exists, Otherwise
     * It Will Be Null.
     * @param userName E-Mail Id Of The User Whose Details Are To Be Loaded.
     * @param nodeName Name Of The Node.
     * @return
     */
    @Transactional(readOnly = true)
    public User loadUserByUsername(String userName, String nodeName) {
        Assert.notNull(userName, "userName Cannot be Null/Empty");
        /** This has to be used for Security without E-com **/
        //return userDAO.getUser(userName);
        User user = this.userDAO.getUserDetails(userName, nodeName);
        return user;
    }

    /** This Method Updates The Last Login Time And Is Called Whenever User Logs In. It Is Called From
     * SDLDAOAuthenticationProvider.
     * @param userName
     * @throws UserNameNotFoundException
     */
    @Transactional(readOnly = true)
    public void updateLastLoginTime(String userName) throws UserNameNotFoundException {
        Assert.notNull(userName, "userName Cannot be Null/Empty");
        User user = this.userDAO.getUser(userName);
        if (user == null) {
            throw new UserNameNotFoundException(this.getMessage("security.username.notfound", new String[]{userName}));
        }
        int recordsModified = this.userDAO.updateLastLoginTime(userName);
        if (recordsModified == 0) {
            throw new RuntimeException("Last Login Time Not Updated!");
        }
    }

    /** This Method Is Used To Get Terms Of Site. When User Is Trying To Add A New Subscription Of Site Which User Does
     * Not Belong To Previously.
     * @param userName E-Mail Id Of User Who Logged In.
     * @param nodeName Name Of The Node.
     * @return
     */
    @Transactional(readOnly = true)
    public List<Term> getNewTermsAndConditionsforUser(String userName, String nodeName) {
        return this.userDAO.getNewTermsAndConditionsforUser(userName, nodeName);
    }

    /** This Method Is Used For Updating The User With New Terms Related To Site.
     * @param user
     * @throws UserNameNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void updateUserTerms(User user) throws UserNameNotFoundException {
         if (user == null) {
             throw new UserNameNotFoundException(this.getMessage("security.updateuser.usernotfound"));
         }
        List<UserTerm> newUserTermList = new LinkedList<UserTerm>();
        for (Term term : user.getTerms()) {
            UserTerm userTerm = new UserTerm(user, term);
            userTerm.setModifiedBy(user.getUsername());
            userTerm.setCreatedBy(user.getUsername());
            userTerm.setModifiedDate(new Date());
            userTerm.setCreatedDate(new Date());
            userTerm.setActive(true);
            newUserTermList.add(userTerm);
        }
        this.eComDAO.saveUserTerm(newUserTermList);
    }

    @Transactional(readOnly = true)
    public UserEvent findUserEvent(String userName) {
    	return this.userDAO.findUserEvent(userName);
    }

    /** This Method Updates The Credit Card Information of a User.
     * @param userName Email Id Of The User Whose Credit Card Information Needs To Be Updated.
     * @param modifiedBy Email Id Of The User Who Is Trying To Update.
     * @param newCreditCardInformation New Credit Card Information.
     * @throws PaymentGatewaySystemException
     * @throws PaymentGatewayUserException
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void updateExistingCreditCardInformation(String userName, String modifiedBy, CreditCard newCreditCardInformation)
            throws PaymentGatewaySystemException, PaymentGatewayUserException {
        Assert.hasLength(userName, "User Name Cannot be Null/Empty");
        Assert.notNull(newCreditCardInformation, "Credit Card Information Cannot be Null");
        User user = this.subDAO.getRecurSubAccountInfo(userName);
        Assert.notNull(user, "user Cannot be Null");
        if (user.getCreditCard() != null) {
            newCreditCardInformation.setId(user.getCreditCard().getId());
            newCreditCardInformation.setUserId(user.getId());
            newCreditCardInformation.setCreatedBy(user.getCreditCard().getCreatedBy());
            newCreditCardInformation.setCreatedDate(user.getCreditCard().getCreatedDate());
            newCreditCardInformation.setModifiedBy(modifiedBy);
            newCreditCardInformation.setModifiedDate(new Date());
            newCreditCardInformation.setActive(true);
            this.userDAO.saveCreditCard(newCreditCardInformation);
        } else if (user.getCreditCard()  == null) {
            newCreditCardInformation.setUserId(user.getId());
            newCreditCardInformation.setModifiedBy(modifiedBy);
            newCreditCardInformation.setCreatedBy(userName);
            newCreditCardInformation.setModifiedDate(new Date());
            newCreditCardInformation.setCreatedDate(new Date());
            newCreditCardInformation.setActive(true);
            this.userDAO.saveCreditCard(newCreditCardInformation);
        }
    }

    /** This Method Gets The Credit Card Information of a User.
     * @param username Email Id Of The User Whose Credit Card Information Needs To Be Pulled.
     * @return Credit Card Information.
     */
    @Transactional(readOnly = true)
    public CreditCard getCreditCardDetails(String userName) {
        Assert.hasLength(userName, "User Name Cannot be Null/Empty");
        CreditCard cardInfo = this.userDAO.getCreditCardDetails(userName);
        if(cardInfo == null){
            return null;
        }
        String creditCardNumber = cardInfo.getNumber();
        int length = creditCardNumber.length();
        String toBeMaskedPart = creditCardNumber.substring(0, length-4);
        String maskedNumber = creditCardNumber.replace(toBeMaskedPart, "XXXX-XXXX-XXXX-");
        cardInfo.setNumber(maskedNumber);
        return cardInfo;

    }

    /** This Method Gets The Credit Card Information of a User.
     * @param userId Id Of The User Whose Credit Card Information Needs To Be Pulled.
     * @return Credit Card Information.
     */
    @Transactional(readOnly = true)
    public CreditCard getCreditCardDetails(Long userId) {
        Assert.notNull(userId, "User Id Cannot be Null");
        CreditCard cardInfo = this.userDAO.getCreditCardDetails(userId);
        if(cardInfo == null){
            return null;
        }
        String creditCardNumber = cardInfo.getNumber();
        int length = creditCardNumber.length();
        String toBeMaskedPart = creditCardNumber.substring(0, length-4);
        String maskedNumber = creditCardNumber.replace(toBeMaskedPart, "XXXX-XXXX-XXXX-");
        cardInfo.setNumber(maskedNumber);
        return cardInfo;
    }

    private void sendUserMail(User user, String fromEMailAddress, String subject, String emailTemplateFile,
            String controllerURL, String password) {
        Assert.notNull(user, "user Cannot be Null");
        Assert.hasLength(fromEMailAddress, "From Email Address Cannot be Null/Empty");
        Assert.hasLength(subject, "Subject Cannot be Null/Empty");
        Assert.hasLength(emailTemplateFile, "Email Template Cannot be Null/Empty");
        UserEvent userEvent =  new UserEvent();
        userEvent.setControllerURL(controllerURL);
        userEvent.setUser(user);
        userEvent.setFromEMailAddress(fromEMailAddress);
        userEvent.setSubject(subject);
        userEvent.setModifiedBy(user.getUsername());
        userEvent.setCreatedBy(user.getUsername());
        userEvent.setCreatedDate(new Date());
        userEvent.setModifiedDate(new Date());
        userEvent.setEmailTemplateFile(emailTemplateFile);
        this.userDAO.saveUserEvent(userEvent);
        this.emailProducer.sendMailUsingTemplate(userEvent, password);
    }

    /**
     * Find User by User name
     *
     * @param userName
     * @return
     * @throws UserNameNotFoundException
     */
    @Transactional(readOnly = true)
    public User findUser(String userName) throws UserNameNotFoundException {
    	Assert.notNull(userName, "User Name cannot be null !");
    	User user = this.userDAO.getUser(userName);
        if (user == null) {
            throw new UserNameNotFoundException(this.getMessage("security.username.notfound", new String[]{userName}));
        }
        return user;
    }


    private void sendResetPasswordMail(User user, String nodeName, String controllerURL)
            throws UserNameNotFoundException {
        Assert.notNull(user, "user Cannot be Null");
        Assert.hasLength(controllerURL, "controllerURL Cannot be Null/Empty");
        UserEvent userEvent = this.userDAO.findUserEvent(user.getUsername());
        NodeConfiguration nodeConfig = this.eComDAO.getNodeConfiguration(nodeName);
        Assert.notNull(nodeConfig, "nodeConfig Cannot be Null");
        if (userEvent == null) {
            this.sendUserMail(user, nodeConfig.getFromEmailAddress(), nodeConfig.getResetPasswordSubject(),
                 nodeConfig.getEmailTemplateFolder() + nodeConfig.getResetPasswordEmailTemplate(), controllerURL, null);
        } else {
            /** This means the User already Requested a Reset Password **/
            userEvent.setFromEMailAddress(nodeConfig.getFromEmailAddress());
            userEvent.setSubject(nodeConfig.getResetPasswordSubject());
            userEvent.setControllerURL(controllerURL);
            userEvent.setEmailTemplateFile(nodeConfig.getEmailTemplateFolder() + nodeConfig.getResetPasswordEmailTemplate());
            this.emailProducer.sendMailUsingTemplate(userEvent, null);
        }
    }

    /**
     * Find out Subscription from an existing list
     *
     */
    private UserAccess getUserAccessById(List<UserAccess> userAcessList, Long accessId){
		for(UserAccess userAccess : userAcessList){
			if(userAccess.getAccess().getId().equals(accessId)){
				return userAccess;
			}
		}
		return null;
    }


    /**
     * Find out an Access from an existing list
     *
     * @param existingAccessList
     * @param accessId
     * @return
     */
    private Access findAccessFromList(List<Access> existingAccessList, Long accessId){
		for(Access access : existingAccessList){
			if(access.getId().equals(accessId)){
				return access;
			}
		}
		return null;
    }

    private void sendEmail(String nodeName, boolean isLock, User user, String additionalComments){
        NodeConfiguration nodeConfig = this.eComDAO.getNodeConfiguration(nodeName);
        Assert.notNull(nodeConfig, "nodeConfig Cannot be Null");
        String emailSubject = null;
        String emailTemplateFile = null;
        Map<String, Object> emailData = new HashMap<String, Object>();
        if (isLock) {
            emailTemplateFile = nodeConfig.getEmailTemplateFolder() + nodeConfig.getLockUserEmailTemplate();
            emailSubject = nodeConfig.getLockUserSub();
        } else {
            emailTemplateFile = nodeConfig.getEmailTemplateFolder() + nodeConfig.getUnlockUserEmailTemplate();
            emailSubject = nodeConfig.getUnlockUserSub();
        }
        emailData.put("user", user);
        emailData.put("currentDate", new Date());
        emailData.put("additionalComments", additionalComments);
        emailData.put("serverUrl", this.ecomServerURL);
        this.emailProducer.sendMailUsingTemplate(nodeConfig.getFromEmailAddress(), user.getUsername(), emailSubject,
            emailTemplateFile, emailData);

    }

    /**
     * Generate Random Password.
     *
     * @return
     */
    private String generateRandomPassord(){
    	return UUID.randomUUID().toString().substring(0, 8);
    }

    private String getMessage(String messageKey, Object[] object) {
        return this.messages.getMessage(messageKey, object, new Locale("en"));
    }

    private String getMessage(String messageKey) {
        return this.messages.getMessage(messageKey, null, new Locale("en"));
    }

    @Transactional(readOnly = true)
	public int getFirmUsersCount(Long adminUserId, Long accessId) {
		return this.userDAO.getFirmUsersCount(adminUserId, accessId);
	}

}