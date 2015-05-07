package com.fdt.subscriptions.service;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.util.SystemUtil;
import com.fdt.common.util.spring.SpringUtil;
import com.fdt.ecom.dao.EComDAO;
import com.fdt.ecom.dto.UserAccessDetailDTO;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.SiteConfiguration;
import com.fdt.ecom.entity.UserHistory;
import com.fdt.ecom.entity.enums.CardType;
import com.fdt.ecom.entity.enums.SettlementStatusType;
import com.fdt.ecom.entity.enums.TransactionType;
import com.fdt.ecom.exception.AccessUnAuthorizedException;
import com.fdt.ecom.service.EComService;
import com.fdt.ecom.util.CreditCardUtil;
import com.fdt.email.EmailProducer;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.paymentgateway.service.PaymentGatewayService;
import com.fdt.recurtx.dao.RecurTxDAO;
import com.fdt.recurtx.dto.UpgradeDowngradeDTO;
import com.fdt.recurtx.dto.UserAccountDetailDTO;
import com.fdt.recurtx.entity.RecurTx;
import com.fdt.recurtx.entity.UserAccount;
import com.fdt.recurtx.service.validator.AbstractBaseNodeValidator;
import com.fdt.security.dao.UserDAO;
import com.fdt.security.dto.FirmUserDTO;
import com.fdt.security.entity.Access;
import com.fdt.security.entity.User;
import com.fdt.security.entity.UserAccess;
import com.fdt.security.entity.enums.AccessType;
import com.fdt.security.exception.DeleteUserException;
import com.fdt.security.exception.MaxUsersExceededException;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.security.service.validator.FirmUserSubscriptionValidator;
import com.fdt.subscriptions.dao.SubDAO;
import com.fdt.subscriptions.dto.AccessDetailDTO;
import com.fdt.subscriptions.dto.SubscriptionDTO;

@Service("subService")
public class SubServiceImpl implements SubService {

	private static String RECURRING_PAID_TO_AUTHORIZED_COMMENTS = "CHANGE RECURRING SUBSCRIPTION FROM PAID TO AUTHORIZED SUBSCRIPTION";
	private static String RECURRING_PAID_TO_UNRESTRICTED_COMMENTS = "CHANGE RECURRING SUBSCRIPTION FROM PAID TO UNRESTRICTED SUBSCRIPTION";
	private static String RECURRING_UNPAID_TO_AUTHORIZED_COMMENTS = "CHANGE RECURRING SUBSCRIPTION FROM UNPAID TO AUTHORIZED SUBSCRIPTION";
	private static String RECURRING_UNPAID_TO_UNRESTRICTED_COMMENTS = "CHANGE RECURRING SUBSCRIPTION FROM UNPAID TO UNRESTRICTED SUBSCRIPTION";


	private static String REMOVED_BY_ADMIN = "Removed by Administrator";

	private static final Logger logger = LoggerFactory.getLogger(SubServiceImpl.class);

    @Autowired
    private EComService eComService;

    @Autowired
    @Qualifier("paymentGateway")
    private PaymentGatewayService paymentGateway;

    @Autowired
    private SubDAO subDAO;

    @Autowired
    private RecurTxDAO recurTxDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private EComDAO eComDAO;

    @Value("${ecommerce.serverurl}")
    private String ecomServerURL = null;

    @Autowired
    private EmailProducer emailProducer;

    @Autowired
    private SpringUtil springUtil;

    @Autowired
    private FirmUserSubscriptionValidator firmUserValidator;

    @Autowired
    @Qualifier("serverMessageSource")
    private MessageSource messages = null;

    /** This Method Is Used To Add A Subscription For A User. It Checks For Whether That Type Of Subscription Already
     * Exists For The Site Among All The Existing Subscriptions For The User.
     * @param subscriptionDTO subscriptionDTO
     * @return
     * @throws SDLBusinessException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class, readOnly = false)
    public List<AccessDetailDTO> addSub(SubscriptionDTO subscriptionDTO)
            throws SDLBusinessException {
        Assert.notNull(subscriptionDTO, "subscriptionDTO Cannot be Null");
        User user = subscriptionDTO.getUser();
        List<Long> newAccessIds = subscriptionDTO.getNewAccessIds();
        String nodeName = subscriptionDTO.getNodeName();
        Assert.notNull(user, "User Cannot be Null");
        Assert.notNull(newAccessIds, "The List of Access Ids Cannot be Null");
        Assert.hasLength(nodeName, "Node Name Cannot Be Null!");

        AbstractBaseNodeValidator baseValidator = (AbstractBaseNodeValidator) this.springUtil.getBean(nodeName);
        if (baseValidator == null) {
            baseValidator = (AbstractBaseNodeValidator) this.springUtil.getBean("defaultAddSubValidator");
        }
        baseValidator.checkForValidSubscription(user.getUsername(), newAccessIds, nodeName);

        List<AccessDetailDTO> accessDetailList = this.subDAO.getSubDetailsByAccesIds(newAccessIds);
        Map<Long, AccessDetailDTO> accessDetailMap = new HashMap<Long, AccessDetailDTO>();
        for (AccessDetailDTO accessDetailDTO : accessDetailList) {
            Access access = accessDetailDTO.getSite().getAccess().get(0);
            accessDetailMap.put(access.getId(), accessDetailDTO);
        }

        List<UserAccess> userAccessList = new LinkedList<UserAccess>();
        for (Long newAccessId : newAccessIds) {
            Access newAccess = new Access();
            newAccess.setId(newAccessId);
            UserAccess userAccess = new UserAccess(user, newAccess);
            userAccess.setModifiedBy(user.getUsername());
            userAccess.setCreatedBy(user.getUsername());
            userAccess.setCreatedDate(new Date());
            userAccess.setModifiedDate(new Date());
            userAccessList.add(userAccess);
            AccessDetailDTO accessDetailDTO = accessDetailMap.get(newAccessId);
            if(accessDetailDTO.getSite().getAccess().get(0).isFirmLevelAccess() && accessDetailDTO.getSubFee().getFee() == 0.0){
            	userAccess.setFirmAccessAdmin(true);
            }
            if (accessDetailDTO.getSite().getAccess().get(0).getAccessType() != AccessType.RECURRING_SUBSCRIPTION) {
                userAccess.setActive(true);
            }
            if(accessDetailDTO.getSite().getAccess().get(0).isAuthorizationRequired()){
                userAccess.setAuthorized(false);
            } else {
                userAccess.setAuthorized(true);
            }
        }
        this.userDAO.saveUserAcess(userAccessList);
        return accessDetailList;
    }

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
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class, readOnly = false)
    public PayPalDTO cancelSub(String userName, Long userAccessId)
            throws PaymentGatewaySystemException, SDLBusinessException {
        Assert.notNull(userName, "User Name Cannot be Null");
        Assert.notNull(userAccessId, "User AccessId Cannot be Null");

        PayPalDTO payPalDTO = null;

        UserAccountDetailDTO userAccountDetailDTO = this.subDAO.getUserAccountByUserAcessId(userName,
                userAccessId);
        Access access = userAccountDetailDTO.getSite().getAccess().get(0);
        UserAccess userAccess = access.getUserAccessList().get(0);
        List<FirmUserDTO> firmUsers = new ArrayList<FirmUserDTO>();
        if(access.isFirmLevelAccess() && userAccess.isFirmAccessAdmin()){
        	firmUsers = this.userDAO.getFirmUsers(userName, access.getId());
        }

        if ((userAccountDetailDTO == null) || (userAccountDetailDTO != null && userAccountDetailDTO.getUserAccount() != null
                    && userAccountDetailDTO.getUserAccount().isMarkForCancellation())) {
            SDLBusinessException sDLBusinessException = new SDLBusinessException();
            sDLBusinessException.setBusinessMessage(this.getMessage("recur.subscription.alreadycancelled"));
            throw sDLBusinessException;
        } else if (!access.isGovernmentAccess() && userAccountDetailDTO.getUserAccount().getId() != null && userAccountDetailDTO.getUserAccount().isActive()) {
        	 /** This condition Occurs when The User Account in active and the User wanted to cancel his Subscription **/
            this.subDAO.markForCancellation(userAccountDetailDTO.getUserAccount().getId(), true);
            Access reActivatedAccess = userAccountDetailDTO.getSite().getAccess().get(0);
            payPalDTO = new PayPalDTO(reActivatedAccess.getCode(), reActivatedAccess.getDescription(), null,
                    reActivatedAccess.getSubscriptionFee(), true);
            /** This condition Occurs when The User Account in Inactive and the User wanted to cancel his Subscription **/
        } else if(access.isGovernmentAccess() || (userAccountDetailDTO.getUserAccount().getId() != null && !userAccountDetailDTO.getUserAccount().isActive())) {
            // Delete the firm Users if it's firm level access and user is an firm admin
            if(access.isFirmLevelAccess() && userAccess.isFirmAccessAdmin()){
            	this.removeFirmUserSubscriptions(access, userAccountDetailDTO.getSite(), userName,
            			REMOVED_BY_ADMIN, firmUsers, false);
            }

            /** Cancellation code for access which is not paid yet. **/
            List<Long> userAccessIds = new LinkedList<Long>();
            userAccessIds.add(userAccessId);
            /** Delete the User Account if it's not a government access **/
            if(!access.isGovernmentAccess()){
            	this.subDAO.deleteUserAccountByUserAccessId(userAccessIds);
            }
            /** Delete the User Access **/
            this.subDAO.deleteUserAccess(userAccessIds);

            Access reActivatedAccess = userAccountDetailDTO.getSite().getAccess().get(0);
            payPalDTO = new PayPalDTO(reActivatedAccess.getCode(), reActivatedAccess.getDescription(), null,
                    reActivatedAccess.getSubscriptionFee(), true);
        } else {
            // Delete the firm Users if it's firm level access and user is an firm admin
            if(access.isFirmLevelAccess() && userAccess.isFirmAccessAdmin()){
            	List<Long> firmUserAccessIds = new ArrayList<Long>();
            	for(FirmUserDTO firmUser : firmUsers){
                    firmUserAccessIds.add(firmUser.getUserAccessId());
            	}
            	if(!firmUserAccessIds.isEmpty()){
            		this.subDAO.deleteUserAccess(firmUserAccessIds);
            	}
            }

            // Cancellation code for access which is not paid yet.
            List<Long> userAccessIds = new LinkedList<Long>();
            userAccessIds.add(userAccessId);
            this.subDAO.deleteUserAccess(userAccessIds);
            Access reActivatedAccess = userAccountDetailDTO.getSite().getAccess().get(0);
            payPalDTO = new PayPalDTO(reActivatedAccess.getCode(), reActivatedAccess.getDescription(), null,
                    reActivatedAccess.getSubscriptionFee(), true);

        }

        // Send Email to Firm Level Users too
        Map<String, Object> emailData = new HashMap<String, Object>();
        User user = this.userDAO.getUserDetails(userName, null);
        emailData.put("user", user);
        emailData.put("payPalDTO", payPalDTO);
        emailData.put("isCancelRequest", true);
        emailData.put("isReactivateRequest", false);
        emailData.put("serverUrl", this.ecomServerURL);
        emailData.put("currentDate", new Date());
        SiteConfiguration siteConfig = this.eComService.getSiteConfiguration(userAccountDetailDTO.getSite().getId());
        Assert.notNull(siteConfig, "siteConfig Cannot be Null");
        this.emailProducer.sendMailUsingTemplate(siteConfig.getFromEmailAddress(), userName,
            siteConfig.getCancelSubscriptionSubject(), siteConfig.getEmailTemplateFolder() +
                siteConfig.getCancelSubscriptionTemplate(), emailData);

        // Send an email to firm level users if it was firm level subscription
        for(FirmUserDTO firmUser : firmUsers){
	        emailData = new HashMap<String, Object>();
	        emailData.put("user", firmUser);
	        emailData.put("subscription", access);
	        emailData.put("isCancelRequest", true);
	        emailData.put("isReactivateRequest", false);
	        emailData.put("serverUrl", this.ecomServerURL);
	        emailData.put("currentDate", new Date());

	        this.emailProducer.sendMailUsingTemplate(siteConfig.getFromEmailAddress(), firmUser.getUsername(),
	            siteConfig.getCancelSubscriptionSubject(), siteConfig.getEmailTemplateFolder() +
	                siteConfig.getCancelSubscriptionTemplate(), emailData);
    	}

        return payPalDTO;
    }

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
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class, readOnly = false)
    public PayPalDTO removeSub(String userName, Long userAccessId, String modifiedBy, String comments,
            boolean sendEmail) throws PaymentGatewaySystemException, SDLBusinessException {
        Assert.hasLength(userName, "User Name Cannot be Null/Empty");
        Assert.notNull(userAccessId, "User AccessId Cannot be Null");
        Assert.hasLength(comments, "comments Cannot be Null/Empty!");
        Assert.isTrue((comments.length() < 249), "Comments Cannot Be Greater Than 250 characters length.");
        PayPalDTO payPalDTO = null;
        LinkedList<Long> userAccessIds = new LinkedList<Long>();
        userAccessIds.add(userAccessId);
        UserAccountDetailDTO userAccountDetailDTO = this.subDAO
            .getUserAccountByUserAcessId(userName, userAccessId);

        Access access = userAccountDetailDTO.getSite().getAccess().get(0);
        UserAccess userAccess = access.getUserAccessList().get(0);
        List<FirmUserDTO> firmUsers = new ArrayList<FirmUserDTO>();

        // Delete the firm Users if it's firm level access and user is an firm admin
        if(access.isFirmLevelAccess() && userAccess.isFirmAccessAdmin()){
        	firmUsers = this.userDAO.getFirmUsers(userName, access.getId());
        }
        if (userAccountDetailDTO !=null && userAccountDetailDTO.getUserAccount() != null &&
                userAccountDetailDTO.getUserAccount().getId() != null) {
            if (!userAccountDetailDTO.getUserAccount().isMarkForCancellation()) {
                Access reActivatedAccess = userAccountDetailDTO.getSite().getAccess().get(0);
                payPalDTO = new PayPalDTO(reActivatedAccess.getCode(), reActivatedAccess.getDescription(), null,
                    reActivatedAccess.getSubscriptionFee(), true);
            }
            // Delete the firm Users if it's firm level access and user is an firm admin
            if(access.isFirmLevelAccess() && userAccess.isFirmAccessAdmin()){
            	this.removeFirmUserSubscriptions(access, userAccountDetailDTO.getSite(), userName,
            			REMOVED_BY_ADMIN, firmUsers, false);
            }

            UserHistory userHistory = new UserHistory();
            LinkedList<UserHistory> userHistories = new LinkedList<UserHistory>();
            userHistories.add(userHistory);
            userHistory.setModifiedBy(modifiedBy);
            userHistory.setCreatedBy(modifiedBy);
            userHistory.setModifiedDate(new Date());
            userHistory.setCreatedDate(new Date());
            userHistory.setUserName(userName);
            userHistory.setAccesId(userAccountDetailDTO.getSite().getAccess().get(0).getId());
            userHistory.setComments(comments);
            this.subDAO.saveUserHistory(userHistories);
            this.subDAO.deleteUserAccountByUserAccessId(userAccessIds);
            this.subDAO.deleteUserAccess(userAccessIds);

        } else {
            // Delete the firm Users if it's firm level access and user is an firm admin
            if(access.isFirmLevelAccess() && userAccess.isFirmAccessAdmin()){
            	List<Long> firmUserAccessIds = new ArrayList<Long>();
            	for(FirmUserDTO firmUser : firmUsers){
                    firmUserAccessIds.add(firmUser.getUserAccessId());
            	}
            	this.subDAO.deleteUserAccess(firmUserAccessIds);
            }
            // Cancellation code for access which is not paid yet.
            this.subDAO.deleteUserAccess(userAccessIds);
            Access reActivatedAccess = userAccountDetailDTO.getSite().getAccess().get(0);
            payPalDTO = new PayPalDTO(reActivatedAccess.getCode(), reActivatedAccess.getDescription(), null,
                reActivatedAccess.getSubscriptionFee(), true);

        }
        if(sendEmail) {
            Map<String, Object> emailData = new HashMap<String, Object>();
            User user = this.userDAO.getUserDetails(userName, null);
            emailData.put("user", user);
            emailData.put("currentDate", new Date());
            emailData.put("payPalDTO", payPalDTO);
            emailData.put("serverUrl", this.ecomServerURL);
            SiteConfiguration siteConfig = this.eComService.getSiteConfiguration(userAccountDetailDTO.getSite().getId());
            Assert.notNull(siteConfig, "siteConfig Cannot be Null");
            this.emailProducer.sendMailUsingTemplate(siteConfig.getFromEmailAddress(), userName,
                siteConfig.getRemoveSubscriptionSubject(), siteConfig.getEmailTemplateFolder() +
                    siteConfig.getRemoveSubscriptionTemplate(), emailData);

            // Send an email to firm level users if it was firm level subscription
            for(FirmUserDTO firmUser : firmUsers){
    	        emailData = new HashMap<String, Object>();
    	        emailData.put("user", firmUser);
    	        emailData.put("subscription", access);
    	        emailData.put("isCancelRequest", true);
    	        emailData.put("isReactivateRequest", false);
    	        emailData.put("serverUrl", this.ecomServerURL);
    	        emailData.put("currentDate", new Date());

    	        this.emailProducer.sendMailUsingTemplate(siteConfig.getFromEmailAddress(), firmUser.getUsername(),
    	            siteConfig.getCancelSubscriptionSubject(), siteConfig.getEmailTemplateFolder() +
    	                siteConfig.getCancelSubscriptionTemplate(), emailData);
        	}
        }

        return payPalDTO;
    }

    /** This Method Is Used To Reactivate The Existing Cancelled Subscription. What It Does Is, It Enables
     * MarkForCancellation Flag And Sends Reactivation E-Mail To User.
     * @param userName EmailId Of The User Logged In.
     * @param existingUserAccessId
     * @return PayPalDTO
     * @throws PaymentGatewayUserException
     * @throws PaymentGatewaySystemException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public PayPalDTO reactivateCancelledSub(String userName,Long existingUserAccessId)
            throws PaymentGatewayUserException, PaymentGatewaySystemException {
        UserAccountDetailDTO existingUserAccountDTO = this.subDAO
            .getUserAccountByUserAcessId(userName, existingUserAccessId);
        UserAccount userAccount = existingUserAccountDTO.getUserAccount();
        Access access = existingUserAccountDTO.getSite().getAccess().get(0);
        PayPalDTO payPalDTO = new PayPalDTO();
        payPalDTO.setAccessCode(access.getCode());
        payPalDTO.setAccessDescription(access.getDescription());
        payPalDTO.setSucessful(true);
        payPalDTO.setSubFee(access.getSubscriptionFee());
        this.subDAO.markForCancellation(userAccount.getId(), false);
        Map<String, Object> emailData = new HashMap<String, Object>();
        User user = this.userDAO.getUserDetails(userName, null);
        emailData.put("user", user);
        emailData.put("payPalDTO", payPalDTO);
        emailData.put("isCancelRequest", false);
        emailData.put("isReactivateRequest", true);
        emailData.put("serverUrl", this.ecomServerURL);
        emailData.put("currentDate", new Date());
        SiteConfiguration siteConfig = this.eComService.getSiteConfiguration(existingUserAccountDTO.getSite().getId());
        Assert.notNull(siteConfig, "siteConfig Cannot be Null");
        this.emailProducer.sendMailUsingTemplate(siteConfig.getFromEmailAddress(), userName,
            siteConfig.getReactivateSubscriptionSubject(), siteConfig.getEmailTemplateFolder()
                + siteConfig.getReactivateCancelledSubscriptionTemplate(), emailData);
        return payPalDTO;
    }

    /** This Method Is Used In AccountInformation Controller. It Gets All The Subscriptions Which User Has Subscribed
     * Irrespective Of Payment Status. If An Optional SiteName Is Passed Then Subscriptions Belonging To Only That
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
    						boolean activeSubscriptionsOnly, boolean firmAdminSubscriptionsOnly) {
        Assert.notNull(userName, "User Name Cannot be Null");
        return this.subDAO.getUserSubs(userName, nodeName, siteName, activeSubscriptionsOnly, firmAdminSubscriptionsOnly);
    }

    /** This Method Is Used In getSubscriptionDetails Controller. And, This Controller Allows User To Cancel Or Change
     * His Subscription. What  it gets is siteId, siteDescription, AccessDescription, Fee, Account's billing date and
     * other access related info like type, subscription period.
     * @param userName EmailId Of The User Logged In.
     * @param accessId Access Id.
     * @return
     */
    @Transactional(readOnly = true)
    public SubscriptionDTO getSubDetailsForUser(String userName, Long accessId) {
        Assert.notNull(userName, "User Name Cannot be Null");
        Assert.notNull(accessId, "User AccessId Cannot be Null");
        return subDAO.getSubDetailsForUser(userName, accessId);
    }

    /** This Method Returns The Subscription Information Which Includes Information About Site, Merchant, CreditUsageFee,
     *  SubscriptionFee, and Access.
     * @param accessId Access Id.
     * @return  List Of AccessDetailDTO's
     */
    @Transactional(readOnly = true)
    public List<AccessDetailDTO> getSubDetailsByAccessId(Long accessId) {
        return this.subDAO.getSubDetailsByAccessId(accessId);
    }

    /** This Method Is Used For Authorizing A Subscription.
     * @param userAccessId UserAccess Id Which Is Going To Be Authorized.
     * @param isAuthorized Flag Used To Authorize The Subscription.
     * @param modifiedBy E-Mail Id Of The Person Who Is Trying To Authorize The Subscription.
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void authorize(Long userAccessId, boolean isAuthorized,     String modifiedBy) {
        Assert.notNull(userAccessId, "userAccessId Cannot be Null/Empty");
        Assert.hasLength(modifiedBy, "modifiedBy Cannot be Null/Empty");
        Map<String, Object> emailData = new HashMap<String, Object>();
        UserAccessDetailDTO userAccessDetailDTO = this.getUserAccessDetails(userAccessId);
        UserAccess userAccess = userAccessDetailDTO.getAccess().getUserAccessList().get(0);
        boolean isActive = userAccess.isActive();
        List<Long> userAccessIds = new ArrayList<Long>();
        //If it's a firm level access then, also authorize the firm level user accesses
        List<FirmUserDTO> firmUsers = new ArrayList<FirmUserDTO>();
        if(userAccess.isFirmAccessAdmin() && userAccessDetailDTO.getAccess().isFirmLevelAccess()){
        	firmUsers = this.userDAO.getFirmUsers(userAccessDetailDTO.getUser().getUsername(),
        			userAccessDetailDTO.getAccess().getId());
        	for(FirmUserDTO firmUser : firmUsers){
        		userAccessIds.add(firmUser.getUserAccessId());
        	}
        }
        userAccessIds.add(userAccessId);
        if(userAccessDetailDTO.getAccess().isGovernmentAccess()){
        	this.userDAO.authorize(userAccessIds, isAuthorized, modifiedBy, isActive, true);
        } else{
        	this.userDAO.authorize(userAccessIds, isAuthorized, modifiedBy, isActive, false);
        }

        // Now if the
        Assert.notNull(userAccessDetailDTO, "userAccessDetailDTO Cannot be Null/Empty");
        SiteConfiguration siteConfig = userAccessDetailDTO.getSiteConfiguration();
        Assert.notNull(siteConfig, "siteConfig Cannot be Null");
        User user = userAccessDetailDTO.getUser();
        Site site = userAccessDetailDTO.getSite();
        Access access = userAccessDetailDTO.getAccess();
        emailData.put("userFirstName", user.getFirstName());
        emailData.put("userLastName", user.getLastName());
        emailData.put("currentDate", new Date());
        emailData.put("subscription", access.getDescription());
        emailData.put("siteName", site.getDescription());
        emailData.put("serverUrl", this.ecomServerURL);
        this.emailProducer.sendMailUsingTemplate(siteConfig.getFromEmailAddress(), user.getUsername(),
            siteConfig.getAccessAuthorizationSubject(), siteConfig.getEmailTemplateFolder() +
                siteConfig.getAccessAuthorizationTemplate(), emailData);

        // Also send an emails to all firm level users in case of firm level access
    	for(FirmUserDTO firmUser : firmUsers){
            emailData = new HashMap<String, Object>();
            emailData.put("userFirstName", firmUser.getFirstName());
            emailData.put("userLastName", firmUser.getLastName());
            emailData.put("currentDate", new Date());
            emailData.put("subscription", access.getDescription());
            emailData.put("siteName", site.getDescription());
            emailData.put("serverUrl", this.ecomServerURL);
            this.emailProducer.sendMailUsingTemplate(siteConfig.getFromEmailAddress(), firmUser.getUsername(),
                siteConfig.getAccessAuthorizationSubject(), siteConfig.getEmailTemplateFolder() +
                    siteConfig.getAccessAuthorizationTemplate(), emailData);
    	}
    }

    @Transactional(readOnly = true)
    public UserAccessDetailDTO getUserAccessDetails(Long userAccessId) {
        Assert.notNull(userAccessId, "userAccessId Name Cannot be Null");
        return this.subDAO.getUserAccessDetails(userAccessId);
    }

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
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public List<PayPalDTO> payRecurSub(CreditCard newCreditCardInformation, String userName, String nodeName,
            String machineName) throws AccessUnAuthorizedException, SDLBusinessException  {
        Assert.hasLength(userName, "User Name Cannot be Null");
        Assert.hasLength(machineName, "Machine Name Cannot be Null");
        List<PayPalDTO> payPalDTOs = new LinkedList<PayPalDTO>();
        PayPalDTO  payPalDTO = null;
        User user = this.subDAO.getUserPaymentInfo(userName, nodeName);

        CreditCard creditCard = null;
        if (user == null) {
            throw new SDLBusinessException();
        }
        Long userId = user.getId();
        boolean flag = true;
        List<UserAccess> userAccessList = user.getUserAccessList();
        for (UserAccess userAccess : userAccessList) {
            if(userAccess.isAuthorized()) {
                flag = false;
                break;
            }
        }
        if(flag) {
            throw new AccessUnAuthorizedException("Subscription is UnAuthorized For Payment");
        }
        if (newCreditCardInformation != null && user.getCreditCard() == null) {
            newCreditCardInformation.setUserId(user.getId());
            newCreditCardInformation.setActive(true);
            newCreditCardInformation.setCreatedDate(new Date());
            newCreditCardInformation.setModifiedDate(new Date());
            newCreditCardInformation.setCreatedBy(userName);
            newCreditCardInformation.setModifiedBy(userName);
            this.userDAO.saveCreditCard(newCreditCardInformation);
            creditCard = newCreditCardInformation;
        } else if (newCreditCardInformation != null && user.getCreditCard() != null) {
            /**
             * Use the Existing Credit Card PK from the DB but get other
             * information from what the user has entered
             **/
            newCreditCardInformation.setId(user.getCreditCard().getId());
            newCreditCardInformation.setUserId(user.getId());
            newCreditCardInformation.setModifiedDate(new Date());
            newCreditCardInformation.setCreatedBy(user.getCreditCard().getCreatedBy());
            newCreditCardInformation.setCreatedDate(user.getCreditCard().getCreatedDate());
            newCreditCardInformation.setModifiedBy(userName);
            newCreditCardInformation.setActive(true);
            this.userDAO.saveCreditCard(newCreditCardInformation);
            creditCard = newCreditCardInformation;
        } else if (newCreditCardInformation == null) {
            /** Use the Existing Credit Card from the DB **/
            creditCard = user.getCreditCard();
        }

        for (UserAccess userAccess : userAccessList) {
        	// Check if user access is a firm level user subscription, it is paid by firm administrator
        	if(userAccess.isFirmLevelUserSubscription()){
        		continue;
        	}
            if(userAccess.isAuthorized()) {
                List<PayPalDTO> payPalDTOListForEmail = new LinkedList<PayPalDTO>();
                if (userAccess.isActive() == false) {
                    Access subscribedAccess = userAccess.getAccess();
                    UserAccount userAccount = new UserAccount();
                    try {
                        if (userAccess.getUserAccount() != null) {
                            UserAccount existingUserAccount = userAccess.getUserAccount();
                            userAccount.setId(existingUserAccount.getId());
                            userAccount.setCreatedDate(existingUserAccount.getCreatedDate());
                            userAccount.setCreditCard(creditCard);
                            /*payPalDTO = this.paymentGateway.reActivateRecurring(userAccess.getAccess().getSite(), userAccount,
                                    false, "paySubscriptions", userName);*/
                            payPalDTO = this.paymentGateway.doSale(userAccess.getAccess().getSite(), userAccess.getAccess()
                                .getSite().getAccess().get(0).getSubscriptionFee().getFee(), creditCard, "paySubscriptions",
                                    userName, true);
                            payPalDTO.setAccessCode(userAccess.getAccess().getSite().getAccess().get(0).getCode());
                            payPalDTO.setAccessDescription(userAccess.getAccess().getSite().getAccess().get(0).getDescription());
                            payPalDTO.setSubFee(userAccess.getAccess().getSite().getAccess().get(0).getSubscriptionFee());
                            payPalDTO.setSucessful(true);
                            Date lastBillingDate = SystemUtil.changeTimeZone(new Date(),
                                TimeZone.getTimeZone(userAccess.getAccess().getSite().getTimeZone()));
                            userAccount.setLastBillingDate(lastBillingDate);
                        } else {
                            payPalDTO = this.paymentGateway.doSale(userAccess.getAccess().getSite(), userAccess.getAccess()
                                .getSite().getAccess().get(0).getSubscriptionFee().getFee(), creditCard,
                                    "paySubscriptions", userName, true);
                            payPalDTO.setAccessCode(userAccess.getAccess().getSite().getAccess().get(0).getCode());
                            payPalDTO.setAccessDescription(userAccess.getAccess().getSite().getAccess().get(0).getDescription());
                            payPalDTO.setSubFee(userAccess.getAccess().getSite().getAccess().get(0).getSubscriptionFee());
                            payPalDTO.setSucessful(true);
                          }
                    } catch (PaymentGatewayUserException paymentGatewayUserException) {
                        payPalDTO = new PayPalDTO(subscribedAccess.getCode(), subscribedAccess.getDescription(), null,
                            subscribedAccess.getSubscriptionFee(), false);
                        payPalDTO.setErrorCode(paymentGatewayUserException.getErrorCode());
                        payPalDTOs.add(payPalDTO);
                        logger.error("PayPal User Exception Occurred During paySubscriptions ", paymentGatewayUserException);
                        continue;
                    } catch (PaymentGatewaySystemException paymentGatewaySystemException) {
                        logger.error("PayPal System Exception Occurred During paySubscriptions", paymentGatewaySystemException);
                        payPalDTO = new PayPalDTO(subscribedAccess.getCode(), subscribedAccess.getDescription(), null,
                            subscribedAccess.getSubscriptionFee(), false);
                        payPalDTO.setErrorCode(paymentGatewaySystemException.getErrorCode());
                        payPalDTO.setSystemException(true);
                        payPalDTOs.add(payPalDTO);
                        continue;
                    }

                    userAccount.setCreditCard(creditCard);
                    userAccount.setActive(true);
                    userAccount.setModifiedBy(userName);
                    userAccount.setCreatedBy(userName);
                    userAccount.setModifiedDate(new Date());
                    userAccount.setCreatedDate(new Date());

                    /** Set the Next Billing Date **/
                    Date lastBillingDate = SystemUtil.changeTimeZone(new Date(), TimeZone.getTimeZone(userAccess.
                        getAccess().getSite().getTimeZone()));
                    userAccount.setLastBillingDate(lastBillingDate);
                    userAccount.setNextBillingDate(SystemUtil.changeTimeZone(SystemUtil.getNextBillingDate(subscribedAccess
                        .getSubscriptionFee().getPaymentPeriod().getCode()).toDate(),
                            TimeZone.getTimeZone(userAccess.getAccess().getSite().getTimeZone())));
                    userAccount.setMarkForCancellation(false);
                    userAccount.setUserAccess(userAccess);

                    /** Save the user Account **/
                    this.subDAO.saveUserAccount(userAccount);

                    /** Update the User Access since the User has Paid **/
                    if(subscribedAccess.isFirmLevelAccess()){
                        // If it's firm level access then set the user to be an administrator for the firm
                        this.userDAO.enableDisableFirmLevelUserAccess(userAccess.getId(), true, userName, null, false, true);

                        /** Now it is possible that user was already administrator and his credit card was expired
                         * Because firm admin's card expired scheduler for recurring process will disable user accesses for
                         * all the firm users. Now we need activate all firm user access since admin is paying for it
                         */
                    	List<FirmUserDTO> firmUsers = this.userDAO.getFirmUsers(userName, userAccess.getAccess().getId());
                    	if(!CollectionUtils.isEmpty(firmUsers)){
                            List<Long> userAccessIds = new ArrayList<Long>();
        	            	for(FirmUserDTO firmUser : firmUsers){
        	            		userAccessIds.add(firmUser.getUserAccessId());
        	            	}
        	            	this.userDAO.enableDisableUserAccesses(userAccessIds, true, userName, null, false);
                    	}
                    } else {
                    	// Access is not a firm level access
                    	this.userDAO.enableDisableUserAccess(userAccess.getId(), true, userName, null, false);
                    }

                    payPalDTO.setAccessId(subscribedAccess.getId());
                    payPalDTO.setUserId(userId);

                    RecurTx recurTransaction = new RecurTx();
                    recurTransaction.setUserId(userId);
                    recurTransaction.setAccessId(subscribedAccess.getId());
                    recurTransaction.setTxRefNum(payPalDTO.getTxRefNum());
                    recurTransaction.setBaseAmount(subscribedAccess.getSubscriptionFee().getFee());
                    recurTransaction.setTotalTxAmount(subscribedAccess.getSubscriptionFee().getFee());
                    recurTransaction.setClientShare(subscribedAccess.getClientShare());
                    recurTransaction.setTransactionDate(SystemUtil.changeTimeZone(new Date(),
                        TimeZone.getTimeZone(userAccess.getAccess().getSite().getTimeZone())));
                    recurTransaction.setSettlementStatus(SettlementStatusType.UNSETTLED);
                    recurTransaction.setTransactionType(TransactionType.CHARGE);
                    CardType cardType = CreditCardUtil.getCardType(creditCard.getNumber());
                    if (cardType == CardType.AMEX) {
                        recurTransaction.setTxFeePercent(userAccess.getAccess().getSite().getMerchant().getTxFeePercentAmex());
                        recurTransaction.setTxFeeFlat(userAccess.getAccess().getSite().getMerchant().getTxFeeFlatAmex());
                    } else {
                        recurTransaction.setTxFeePercent(userAccess.getAccess().getSite().getMerchant().getTxFeePercent());
                        recurTransaction.setTxFeeFlat(userAccess.getAccess().getSite().getMerchant().getTxFeeFlat());
                    }
                    recurTransaction.setCardNumber(creditCard.getNumber());
                    recurTransaction.setAccountName(creditCard.getName());
                    recurTransaction.setCardType(cardType);
                    recurTransaction.setModifiedBy(userName);
                    recurTransaction.setCreatedBy(userName);
                    recurTransaction.setActive(true);
                    recurTransaction.setMerchantId(userAccess.getAccess().getSite().getMerchant().getId());
                    recurTransaction.setMachineName(machineName);
                    recurTransaction.setModifiedDate(new Date());
                    recurTransaction.setCreatedDate(new Date());
                    this.recurTxDAO.saveRecurTransaction(recurTransaction);
                    payPalDTO.setCreatedDate(recurTransaction.getTransactionDate());
                    payPalDTOListForEmail.add(payPalDTO);
                    /** Send E-Mail Confirmation **/
                    Map<String, Object> emailData = new HashMap<String, Object>();
                    User userInfo = this.userDAO.getUserDetails(userName, null);
                    emailData.put("user", userInfo);
                    emailData.put("payments", payPalDTOListForEmail);
                    emailData.put("serverUrl", this.ecomServerURL);
                    emailData.put("currentDate", new Date());
                    SiteConfiguration siteConfig = this.eComService.getSiteConfiguration(userAccess.getAccess().getSite()
                        .getId());
                    Assert.notNull(siteConfig, "siteConfig Cannot be Null");
                    this.emailProducer.sendMailUsingTemplate(siteConfig.getFromEmailAddress(), userName,
                        siteConfig.getPaymentConfirmationSubject(), siteConfig.getEmailTemplateFolder()
                            + siteConfig.getPaymentConfirmationTemplate(), emailData);
                    payPalDTOs.add(payPalDTO);
                }
            }
        }
        /** Return the Payment Information to the user **/
        return payPalDTOs;
    }

    /** This Method Is Used In checkSubscription Controller. It Gets All The Subscriptions Which User Has Subscribed But
     * Not Paid For.
     * @param userName EmailId Of The User Logged In.
     * @param nodeName Name Of The Node.
     * @return user
     */
    @Transactional(readOnly = true)
    public User getPaidUnpaidRecurSubByUser(String userName, String nodeName) {
        Assert.hasLength(userName, "User Name Cannot be Null");
        User user = this.subDAO.getPaidSubUnpaidByUser(userName, nodeName);
        return user;
    }

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
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public UpgradeDowngradeDTO getRecurChangeSubInfo(Long existingUserAccessId, Long newAccessId, String userName)
            throws SDLBusinessException, MaxUsersExceededException {
        Assert.notNull(existingUserAccessId, "existingUserAccessId Cannot be Null");
        Assert.notNull(newAccessId, "newAccessId Cannot be Null");
        Assert.notNull(userName, "User Name Cannot be Null");
        UpgradeDowngradeDTO upgradeDowngradeDTO = new UpgradeDowngradeDTO();

        UserAccountDetailDTO existingUserAccountDTO = this.subDAO.getUserAccountByUserAcessId(userName,
            existingUserAccessId);

        AccessDetailDTO newAccessDTO = this.subDAO.getSubDetailsByAccessId(newAccessId).get(0);

        validateChangeSubscription(existingUserAccountDTO, newAccessDTO);

        if (existingUserAccountDTO.getSite().getAccess().get(0).isVisible()
                != newAccessDTO.getSite().getAccess().get(0).isVisible()) {
            SDLBusinessException sDLBussExcep = new SDLBusinessException();
            sDLBussExcep.setBusinessMessage(this.getMessage("recur.subscription.cannotbechanged"));
            throw sDLBussExcep;
        }

        if (!(newAccessDTO.getSite().getAccess().get(0).getAccessType() == AccessType.RECURRING_SUBSCRIPTION)) {
            SDLBusinessException sDLBussExcep = new SDLBusinessException();
            sDLBussExcep.setBusinessMessage(this.getMessage("recur.subscription.notsametype"));
            throw sDLBussExcep;
        }
        if(newAccessDTO.isAuthorizationRequired()) {
            upgradeDowngradeDTO.setAccessUnAuthorizedExceptionFlag(true);
        }
        if (newAccessDTO.getSubFee().getFee() >= existingUserAccountDTO.getSubFee().getFee()) {
            upgradeDowngradeDTO.setDowngrade(false);
        } else {
            upgradeDowngradeDTO.setDowngrade(true);
        }
        /** If user has a subscription which is active and paid **/
        if (existingUserAccountDTO != null    && existingUserAccountDTO.getUserAccount() != null
                && existingUserAccountDTO.getUserAccount().getId() != null
                && existingUserAccountDTO.getUserAccount().isActive()) {
            if (newAccessDTO.getSubFee().getFee() >= existingUserAccountDTO.getSubFee().getFee()) {
                upgradeDowngradeDTO = calculateBalance(existingUserAccountDTO, newAccessDTO, false, upgradeDowngradeDTO);
                upgradeDowngradeDTO.setDowngradeFee(0.0d);
            } else { // If Downgrade
                upgradeDowngradeDTO = calculateBalance(existingUserAccountDTO, newAccessDTO, true, upgradeDowngradeDTO);
                upgradeDowngradeDTO.setDowngradeFee(newAccessDTO.getSite().getCardUsageFee().getDowngradeFee());
                upgradeDowngradeDTO.setCardUsageFee(newAccessDTO.getSite().getCardUsageFee());
                if(newAccessDTO.isAuthorizationRequired()) {
                    upgradeDowngradeDTO.setNewBalance(0.0d);
                }
            }
        } else {
            upgradeDowngradeDTO.setCurrentFee(existingUserAccountDTO.getSubFee().getFee());
            upgradeDowngradeDTO.setNewFee(newAccessDTO.getSubFee().getFee());
            upgradeDowngradeDTO.setUnUsedBalance(0.00D);
            upgradeDowngradeDTO.setNewBalance(newAccessDTO.getSubFee().getFee());
            upgradeDowngradeDTO.setAcctExistForCurSub(false);
        }
        upgradeDowngradeDTO.setExistingUserAccountDetail(existingUserAccountDTO);
        upgradeDowngradeDTO.setCurrentUserAccessId(existingUserAccessId);
        upgradeDowngradeDTO.setNewAccessDetailDTO(newAccessDTO);
        return upgradeDowngradeDTO;
    }

    /** This Method Is Used To Change From One Recurring to Another Recurring Subscription. Four Possible Cases:
     * (i) changeFromCurrentlyPaidToRestrictedSubscription (ii) changeFromCurrentlyPaidToUnrestrictedSubscription
     * (iii) changeFromCurrentlyUnPaidToRestrictedSubscription, and (iv) changeFromCurrentlyUnPaidToUnrestrictedSubscription.
     * @param existingUserAccessId
     * @param newAccessId
     * @param userName EmailId Of The User Logged In.
     * @param machineName
     * @return
     * @throws PaymentGatewaySystemException
     * @throws PaymentGatewayUserException
     * @throws SDLBusinessException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class, readOnly = false)
    public UpgradeDowngradeDTO changeFromRecurToRecurSub(Long existingUserAccessId, Long newAccessId,
            String userName, String machineName) throws PaymentGatewaySystemException, PaymentGatewayUserException,
                SDLBusinessException, MaxUsersExceededException {
        Assert.notNull(newAccessId, "Access Id Cannot be Null");
        Assert.notNull(existingUserAccessId, "User AccessId Cannot be Null");
        Assert.notNull(userName, "User Name Cannot be Null");
        UpgradeDowngradeDTO upgradeDowngradeDTO = this.getRecurChangeSubInfo(existingUserAccessId, newAccessId, userName);
        UserAccountDetailDTO existingUserAccountDTO = upgradeDowngradeDTO.getExistingUserAccountDetail();
        AccessDetailDTO newAccessDTO = upgradeDowngradeDTO.getNewAccessDetailDTO();
        if (existingUserAccountDTO != null && existingUserAccountDTO.getUserAccount() != null
                && existingUserAccountDTO.getUserAccount().getId() != null ) {
            if(existingUserAccountDTO.getUserAccount().isActive()) {
                if(newAccessDTO.isAuthorizationRequired()) {
                    upgradeDowngradeDTO = this.changeFromCurrentlyPaidToRestrictedSubscription(existingUserAccessId,
                        newAccessId, userName, upgradeDowngradeDTO, machineName);
                } else {
                    upgradeDowngradeDTO = this.changeFromCurrentlyPaidToUnrestrictedSubscription(existingUserAccessId,
                        newAccessId, userName, upgradeDowngradeDTO, machineName);
                }
            } else {
            	/* Changing of subscription whose recurring payment is failed. Deletion Of Existing UserAccount, and
            	 * UserAccess. */
            	List<Long> userAccessIds = new LinkedList<Long>();
    			userAccessIds.add(existingUserAccessId);
    			this.subDAO.deleteUserAccountByUserAccessId(userAccessIds);
    			if(newAccessDTO.isAuthorizationRequired()) {
                    upgradeDowngradeDTO = this.changeFromCurrentlyUnPaidToRestrictedSubscription(existingUserAccessId,
                        newAccessId, userName, upgradeDowngradeDTO);
                } else {
                    upgradeDowngradeDTO = this.changeFromCurrentlyUnPaidToUnrestrictedSubscription(existingUserAccessId,
                        newAccessId, userName, upgradeDowngradeDTO, machineName);
                }
            }
        } else {
            if(newAccessDTO.isAuthorizationRequired()) {
                upgradeDowngradeDTO = this.changeFromCurrentlyUnPaidToRestrictedSubscription(existingUserAccessId,
                    newAccessId, userName, upgradeDowngradeDTO);
            } else {
                upgradeDowngradeDTO = this.changeFromCurrentlyUnPaidToUnrestrictedSubscription(existingUserAccessId,
                    newAccessId, userName, upgradeDowngradeDTO, machineName);
            }
        }

        Map<String, Object> emailData = new HashMap<String, Object>();
        User user = this.userDAO.getUserDetails(userName, null);
        emailData.put("user", user);
        emailData.put("changeDTO", upgradeDowngradeDTO);
        emailData.put("serverUrl", this.ecomServerURL);
        emailData.put("currentDate", new Date());
        SiteConfiguration siteConfig = this.eComService.getSiteConfiguration(upgradeDowngradeDTO.getNewAccessDetailDTO().
            getSite().getId());
        Assert.notNull(siteConfig, "siteConfig Cannot be Null");
        this.emailProducer.sendMailUsingTemplate(siteConfig.getFromEmailAddress(), userName,
            siteConfig.getChangeSubscriptionSubject(), siteConfig.getEmailTemplateFolder()
                + siteConfig.getChangeSubscriptionTemplate(),  emailData);
        return upgradeDowngradeDTO;
    }

    private UpgradeDowngradeDTO changeFromCurrentlyPaidToRestrictedSubscription(Long existingUserAccessId, Long newAccessId,
            String userName, UpgradeDowngradeDTO upgradeDowngradeDTO, String machineName)
                    throws PaymentGatewaySystemException, PaymentGatewayUserException, SDLBusinessException {
        /* Change Subscription When Existing Access is paid one, and destination access requires authorization.*/
        Double totalAmount = null;
        String transactionId = null;
        SettlementStatusType settlementStatusType = null;
        PayPalDTO paymentTxResponseDTO = new PayPalDTO();
        RecurTx recurTransactionRefund = new RecurTx();
        UserAccountDetailDTO existingUserAccountDTO = upgradeDowngradeDTO.getExistingUserAccountDetail();
        AccessDetailDTO newAccessDTO = upgradeDowngradeDTO.getNewAccessDetailDTO();
        upgradeDowngradeDTO.setAccessUnAuthorizedExceptionFlag(true);
        CreditCard creditCard = (existingUserAccountDTO.getUserAccount().getCreditCard() == null) ? null
            : (existingUserAccountDTO.getUserAccount().getCreditCard());
        if(creditCard != null && creditCard.isActive()) {
            Site site = existingUserAccountDTO.getSite();
            if (upgradeDowngradeDTO.isDowngrade()) {
                if (upgradeDowngradeDTO.getUnUsedBalance() - upgradeDowngradeDTO.getDowngradeFee() > 0) {
                    /** Put the Money Back to the Customer By Doing a Credit **/
                    totalAmount = upgradeDowngradeDTO.getUnUsedBalance() - upgradeDowngradeDTO.getDowngradeFee();
                    transactionId = this.paymentGateway.doCredit(existingUserAccountDTO, newAccessDTO, totalAmount,
                        "changeFromCurrentlyPaidToRestrictedSubscription", userName);
                    settlementStatusType = SettlementStatusType.UNSETTLED;
                } else if ( upgradeDowngradeDTO.getUnUsedBalance() - upgradeDowngradeDTO.getDowngradeFee() < 0) {
                    totalAmount = upgradeDowngradeDTO.getDowngradeFee() - upgradeDowngradeDTO.getUnUsedBalance();
                    paymentTxResponseDTO = this.paymentGateway.doSale(site, totalAmount, creditCard,
                        "changeFromCurrentlyPaidToRestrictedSubscription", userName, true);
                    transactionId = paymentTxResponseDTO.getTxRefNum();
                    settlementStatusType = SettlementStatusType.UNSETTLED;
                }
            } else {
                if (upgradeDowngradeDTO.getUnUsedBalance() > 0) {
                    /** Put the Money Back to the Customer By Doing a Credit **/
                    totalAmount = upgradeDowngradeDTO.getUnUsedBalance();
                    transactionId = this.paymentGateway.doCredit(existingUserAccountDTO, newAccessDTO, totalAmount,
                        "changeFromCurrentlyPaidToRestrictedSubscription", userName);
                    settlementStatusType = SettlementStatusType.UNSETTLED;
                }
            }
            // If user is firm access admin for this access then remove subscriptions for all the firm users.
            List<FirmUserDTO> firmUsers = new ArrayList<FirmUserDTO>();
            Access existingAccess = existingUserAccountDTO.getSite().getAccess().get(0);
            Access newAccess = newAccessDTO.getSite().getAccess().get(0);
            UserAccess existingUserAccess = existingUserAccountDTO.getSite().getAccess().get(0).getUserAccessList().get(0);
            List<Long> userAccessIds = new ArrayList<Long>();

            if(existingUserAccess.isFirmAccessAdmin()){
            	firmUsers = this.userDAO.getFirmUsers(userName, existingAccess.getId());
            	if(!CollectionUtils.isEmpty(firmUsers)){
	            	for(FirmUserDTO firmUser : firmUsers){
	            		userAccessIds.add(firmUser.getUserAccessId());
	            	}
            	}

            }
            userAccessIds.add(existingUserAccessId);
            this.subDAO.updateUserAccessWithAccessId(userAccessIds, newAccessId,
                    false, false, userName, RECURRING_PAID_TO_AUTHORIZED_COMMENTS);
        } else {
            SDLBusinessException sDLBussExcep = new SDLBusinessException();
            sDLBussExcep.setBusinessMessage(this.getMessage("recur.changesub.cardnotactive"));
            throw sDLBussExcep;
        }
        List<RecurTx> originalTxList =  this.recurTxDAO.getRecurringTransactionByTxRefNum(
            existingUserAccountDTO.getUserAccount().getLastTxRefNum(), existingUserAccountDTO.getSite().getName());
        RecurTx originalTx = originalTxList.get(0);
        if (transactionId != null) {
            recurTransactionRefund.setUserId(existingUserAccountDTO.getUserId());
            recurTransactionRefund.setAccessId(upgradeDowngradeDTO.getExistingUserAccountDetail().getSite()
                .getAccess().get(0).getId());
            recurTransactionRefund.setBaseAmount(totalAmount);
            recurTransactionRefund.setTotalTxAmount(totalAmount);
            recurTransactionRefund.setTxRefNum(transactionId);
            recurTransactionRefund.setTransactionType(TransactionType.REFUND);
            recurTransactionRefund.setSettlementStatus(settlementStatusType);
            creditCard = existingUserAccountDTO.getUserAccount().getCreditCard();
            CardType cardType = CreditCardUtil.getCardType(creditCard.getNumber());
            recurTransactionRefund.setCardNumber(creditCard.getNumber());
            recurTransactionRefund.setCardType(cardType);
            recurTransactionRefund.setAccountName(creditCard.getName());
            recurTransactionRefund.setModifiedBy(userName);
            recurTransactionRefund.setTransactionDate(SystemUtil.changeTimeZone(new Date(),
                TimeZone.getTimeZone(upgradeDowngradeDTO.getExistingUserAccountDetail().getSite()
                    .getTimeZone())));
            recurTransactionRefund.setCreatedBy(userName);
            recurTransactionRefund.setActive(true);
            recurTransactionRefund.setMerchantId(existingUserAccountDTO.getSite().getMerchant().getId());
            recurTransactionRefund.setTxFeeFlat(0.0d);
            if (cardType == CardType.AMEX) {
                recurTransactionRefund.setTxFeePercent(0.0d);
            } else {
                recurTransactionRefund.setTxFeePercent(0 - existingUserAccountDTO.getSite().getMerchant().getTxFeePercent());
            }
            recurTransactionRefund.setClientShare(newAccessDTO.getSite().getAccess().get(0).getClientShare());
            recurTransactionRefund.setMachineName(machineName);
            recurTransactionRefund.setPreviousAccess(false);
            recurTransactionRefund.setModifiedDate(new Date());
            recurTransactionRefund.setCreatedDate(new Date());
            this.recurTxDAO.saveRecurTransaction(recurTransactionRefund);
            originalTx.setPreviousAccess(true);
            originalTx.setActive(true);
            originalTx.setModifiedDate(new Date());
            originalTx.setModifiedBy(userName);
            this.recurTxDAO.saveRecurTransaction(originalTx);
            upgradeDowngradeDTO.setSecondaryTxId(transactionId);
        }
        return upgradeDowngradeDTO;
    }

    private UpgradeDowngradeDTO changeFromCurrentlyPaidToUnrestrictedSubscription(Long existingUserAccessId,
            Long newAccessId, String userName, UpgradeDowngradeDTO upgradeDowngradeDTO, String machineName)
                throws PaymentGatewaySystemException, PaymentGatewayUserException, SDLBusinessException {
        /* Change Subscription When Existing Access is paid one */
        Double secondaryTxAmount = 0.0d;
        String transactionId = null;
        String secondaryTxId = null;
        Boolean isBalanceRefunded = false;
        SettlementStatusType settlementStatusType = null;
        RecurTx recurTransactionPrimary = new RecurTx();
        RecurTx recurTransactionSecondary = new RecurTx();
        UserAccountDetailDTO existingUserAccountDTO = upgradeDowngradeDTO.getExistingUserAccountDetail();
        AccessDetailDTO newAccessDTO = upgradeDowngradeDTO.getNewAccessDetailDTO();
        UserAccount userAccount = (existingUserAccountDTO.getUserAccount() == null) ? null : (existingUserAccountDTO
            .getUserAccount());
        CreditCard creditCard = (userAccount.getCreditCard() == null) ? null : (userAccount.getCreditCard());
        boolean isEnableAccess = false;
        PayPalDTO paymentTxResponseDTO = new PayPalDTO();
        upgradeDowngradeDTO.setAccessUnAuthorizedExceptionFlag(false);
        if(creditCard != null && creditCard.isActive()) {
            if (upgradeDowngradeDTO.getUnUsedBalance() - upgradeDowngradeDTO.getDowngradeFee() > 0) {
                /** Put the Money Back to the Customer By Doing a Credit **/
                secondaryTxAmount = upgradeDowngradeDTO.getUnUsedBalance() - upgradeDowngradeDTO.getDowngradeFee();
                secondaryTxId = this.paymentGateway.doCredit(existingUserAccountDTO, newAccessDTO, secondaryTxAmount,
                    "changeFromCurrentlyPaidToRestrictedSubscription", userName);
                isBalanceRefunded = true;
                settlementStatusType = SettlementStatusType.UNSETTLED;
            } else if ( upgradeDowngradeDTO.getUnUsedBalance() - upgradeDowngradeDTO.getDowngradeFee() < 0) {
                secondaryTxAmount = upgradeDowngradeDTO.getDowngradeFee() - upgradeDowngradeDTO.getUnUsedBalance();
                paymentTxResponseDTO = this.paymentGateway.doSale(existingUserAccountDTO.getSite(), secondaryTxAmount,
                    creditCard, "changeFromCurrentlyPaidToRestrictedSubscription", userName, true);
                secondaryTxId = paymentTxResponseDTO.getTxRefNum();
                settlementStatusType = SettlementStatusType.UNSETTLED;
            }
            if (upgradeDowngradeDTO.getNewBalance() > 0 ) {
                paymentTxResponseDTO = this.paymentGateway.doSale(newAccessDTO.getSite(),
                    upgradeDowngradeDTO.getNewBalance(), creditCard, "changeFromCurrentlyPaidToUnrestrictedSubscription",
                        userName, true);
                transactionId = paymentTxResponseDTO.getTxRefNum();
                settlementStatusType = SettlementStatusType.UNSETTLED;
            }
            isEnableAccess = true;
            if (userAccount.isMarkForCancellation() && isEnableAccess) {
                this.subDAO.markForCancellation(userAccount.getId(), false);
            }
        }
        if (transactionId != null) {
            UserAccess existingUserAccess = existingUserAccountDTO.getSite().getAccess().get(0).getUserAccessList().get(0);
            Access existingAccess = existingUserAccountDTO.getSite().getAccess().get(0);

            List<Long> userAccessIds = new ArrayList<Long>();
            // Update firm level users if user is a firm access admin
            if(existingUserAccess.isFirmAccessAdmin()){
            	// Retrieve Firm Level Users and update user access for each of them
            	List<FirmUserDTO> firmUsers = this.userDAO.getFirmUsers(userName, existingAccess.getId());
            	if(!CollectionUtils.isEmpty(firmUsers)){
	            	for(FirmUserDTO firmUser : firmUsers){
	            		userAccessIds.add(firmUser.getUserAccessId());
	            	}
            	}
            }
            userAccessIds.add(existingUserAccessId);
        	int recordsdModified = this.subDAO.updateUserAccessWithAccessId(userAccessIds, newAccessId,
                isEnableAccess, true, userName, RECURRING_PAID_TO_UNRESTRICTED_COMMENTS);

            if (recordsdModified == 0) {
                logger.error("The updateUserAccessWithAccessId Did not Update Any Records in " +
                    "changeFromRecurringToRecurringSubscription!");
                logger.error(NOTIFY_ADMIN, "Error in Change Subscription existingUserAccessId - " + existingUserAccessId +
                    "newAccessId - " + newAccessId + "userName - " + userName);
                /* This Exception is not read from the Message Resource file as it is not displayed to the User*/
                throw new RuntimeException("The User Access is not Disabled");
            }

            creditCard = existingUserAccountDTO.getUserAccount().getCreditCard();
            CardType cardType = CreditCardUtil.getCardType(creditCard.getNumber());

            String orgTxRefNum = null;
            if (upgradeDowngradeDTO.getUnUsedBalance() > 0.0d) {
                recurTransactionPrimary.setUserId(existingUserAccountDTO.getUserId());
                recurTransactionPrimary.setAccessId(newAccessId);
                recurTransactionPrimary.setBaseAmount(upgradeDowngradeDTO.getNewBalance());
                recurTransactionPrimary.setTotalTxAmount(upgradeDowngradeDTO.getNewBalance());
                recurTransactionPrimary.setTxRefNum(transactionId);
                recurTransactionPrimary.setTransactionType(TransactionType.CHARGE);
                recurTransactionPrimary.setSettlementStatus(settlementStatusType);
                recurTransactionPrimary.setCardNumber(creditCard.getNumber());
                recurTransactionPrimary.setCardType(cardType);
                recurTransactionPrimary.setAccountName(creditCard.getName());
                recurTransactionPrimary.setModifiedBy(userName);
                recurTransactionPrimary.setTransactionDate(SystemUtil.changeTimeZone(new Date(),
                    TimeZone.getTimeZone(upgradeDowngradeDTO.getExistingUserAccountDetail().getSite().getTimeZone())));
                recurTransactionPrimary.setCreatedBy(userName);
                recurTransactionPrimary.setActive(true);
                recurTransactionPrimary.setMerchantId(existingUserAccountDTO.getSite().getMerchant().getId());
                recurTransactionPrimary.setTxFeeFlat(0.0d);
                if (cardType == CardType.AMEX) {
                    recurTransactionPrimary.setTxFeePercent(newAccessDTO.getSite().getMerchant().getTxFeePercentAmex());
                    recurTransactionPrimary.setTxFeeFlat(newAccessDTO.getSite().getMerchant().getTxFeeFlat());
                } else {
                    recurTransactionPrimary.setTxFeePercent(newAccessDTO.getSite().getMerchant().getTxFeePercentAmex());
                    recurTransactionPrimary.setTxFeeFlat(newAccessDTO.getSite().getMerchant().getTxFeeFlat());
                }
                recurTransactionPrimary.setMachineName(machineName);
                recurTransactionPrimary.setClientShare(newAccessDTO.getSite().getAccess().get(0).getClientShare());
                recurTransactionPrimary.setPreviousAccess(false);
                recurTransactionPrimary.setModifiedDate(new Date());
                recurTransactionPrimary.setCreatedDate(new Date());
                this.recurTxDAO.saveRecurTransaction(recurTransactionPrimary);
                /* Mark the last transaction on the old subscription as previous */
                List<RecurTx> originalTxList =  this.recurTxDAO.getRecurringTransactionByTxRefNum(
                    existingUserAccountDTO.getUserAccount().getLastTxRefNum(), existingUserAccountDTO.getSite().getName());
                RecurTx originalTx = originalTxList.get(0);
                originalTx.setPreviousAccess(true);
                originalTx.setActive(true);
                originalTx.setModifiedDate(new Date());
                originalTx.setModifiedBy(userName);
                orgTxRefNum = originalTx.getTxRefNum();
                this.recurTxDAO.saveRecurTransaction(originalTx);
            }
            if (secondaryTxId != null) {
                upgradeDowngradeDTO.setSecondaryTxId(secondaryTxId);
                if (upgradeDowngradeDTO.getNewBalance() > 0.0d) {
                    recurTransactionSecondary.setUserId(existingUserAccountDTO.getUserId());
                    recurTransactionSecondary.setAccessId(upgradeDowngradeDTO.getExistingUserAccountDetail().getSite()
                        .getAccess().get(0).getId());
                    recurTransactionSecondary.setBaseAmount(secondaryTxAmount);
                    recurTransactionSecondary.setTotalTxAmount(secondaryTxAmount);
                    recurTransactionSecondary.setTxRefNum(secondaryTxId);
                    if (isBalanceRefunded) {
                        recurTransactionSecondary.setTransactionType(TransactionType.REFUND);
                        recurTransactionSecondary.setOrigTxRefNum(orgTxRefNum);
                    } else {
                        recurTransactionSecondary.setTransactionType(TransactionType.CHARGE);
                    }
                    recurTransactionSecondary.setSettlementStatus(settlementStatusType);
                    recurTransactionSecondary.setCardNumber(creditCard.getNumber());
                    recurTransactionSecondary.setCardType(cardType);
                    recurTransactionSecondary.setAccountName(creditCard.getName());
                    recurTransactionSecondary.setModifiedBy(userName);
                    recurTransactionSecondary.setModifiedDate(new Date());
                    recurTransactionSecondary.setCreatedDate(new Date());
                    recurTransactionSecondary.setTransactionDate(SystemUtil.changeTimeZone(new Date(),
                        TimeZone.getTimeZone(upgradeDowngradeDTO.getExistingUserAccountDetail().getSite().getTimeZone())));
                    recurTransactionSecondary.setCreatedBy(userName);
                    recurTransactionSecondary.setActive(true);
                    recurTransactionSecondary.setMerchantId(existingUserAccountDTO.getSite().getMerchant().getId());
                    if (isBalanceRefunded) {
                        recurTransactionSecondary.setTxFeeFlat(0.0d);
                        if (recurTransactionSecondary.getCardType() == CardType.AMEX) {
                            recurTransactionSecondary.setTxFeePercent(0.0d);
                        } else {
                            recurTransactionSecondary.setTxFeePercent(0 - existingUserAccountDTO.getSite().getMerchant()
                                .getTxFeePercent());
                        }
                    } else {
                        if (cardType == CardType.AMEX) {
                            recurTransactionSecondary.setTxFeePercent(newAccessDTO.getSite().getMerchant()
                                .getTxFeePercentAmex());
                            recurTransactionSecondary.setTxFeeFlat(newAccessDTO.getSite().getMerchant().getTxFeeFlatAmex());
                        } else {
                            recurTransactionSecondary.setTxFeePercent(newAccessDTO.getSite().getMerchant().getTxFeePercent());
                            recurTransactionSecondary.setTxFeeFlat(newAccessDTO.getSite().getMerchant().getTxFeeFlat());
                        }
                    }
                    recurTransactionSecondary.setMachineName(machineName);
                    recurTransactionSecondary.setClientShare(newAccessDTO.getSite().getAccess().get(0).getClientShare());
                    this.recurTxDAO.saveRecurTransaction(recurTransactionSecondary);
                }
            }
            upgradeDowngradeDTO.setTransactionId(transactionId);
        }
        return upgradeDowngradeDTO;
    }

    private UpgradeDowngradeDTO changeFromCurrentlyUnPaidToRestrictedSubscription(Long existingUserAccessId,
            Long newAccessId, String userName, UpgradeDowngradeDTO upgradeDowngradeDTO)
                    throws PaymentGatewaySystemException, PaymentGatewayUserException {
        /* Change Subscription When existing Subscription is not paid */
        boolean enableAccess = false;
        upgradeDowngradeDTO.setAccessUnAuthorizedExceptionFlag(true);

        // If user is a firm level user admin then update the firm level users first.
        UserAccess existingUserAccess =
        		upgradeDowngradeDTO.getExistingUserAccountDetail().getSite().getAccess().get(0).getUserAccessList().get(0);
        Access existingAccess = upgradeDowngradeDTO.getExistingUserAccountDetail().getSite().getAccess().get(0);

        List<Long> userAccessIds = new ArrayList<Long>();
        // Update firm level users if user is a firm access admin
        if(existingUserAccess.isFirmAccessAdmin()){
        	// Retrieve Firm Level Users and update user access for each of them
        	List<FirmUserDTO> firmUsers = this.userDAO.getFirmUsers(userName, existingAccess.getId());
        	if(!CollectionUtils.isEmpty(firmUsers)){
            	for(FirmUserDTO firmUser : firmUsers){
            		userAccessIds.add(firmUser.getUserAccessId());
            	}
        	}
        }
        userAccessIds.add(existingUserAccessId);

        int recordsdModified = this.subDAO.updateUserAccessWithAccessId(userAccessIds, newAccessId,
                enableAccess, false,  userName, RECURRING_UNPAID_TO_AUTHORIZED_COMMENTS);
        if (recordsdModified == 0) {
            logger.error("The updateUserAccessWithAccessId Did not Update Any Records in changeSubscription!");
            logger.error(NOTIFY_ADMIN, "Error in Change Subscription existingUserAccessId - " + existingUserAccessId +
                    "newAccessId - " + newAccessId + "userName - " + userName);
            throw new RuntimeException("The User Access is not Disabled");
        }
        return upgradeDowngradeDTO;
    }

    private UpgradeDowngradeDTO changeFromCurrentlyUnPaidToUnrestrictedSubscription(Long existingUserAccessId,
            Long newAccessId, String userName, UpgradeDowngradeDTO upgradeDowngradeDTO, String machineName)
                    throws PaymentGatewaySystemException, PaymentGatewayUserException {
        /* Change Subscription When existing Subscription is not paid and destination subscription is not restricted.  */
        String transactionId = null;
        AccessDetailDTO newAccessDTO = upgradeDowngradeDTO.getNewAccessDetailDTO();
        boolean enableAccess = false;
        Site site = null;
        upgradeDowngradeDTO.setAccessUnAuthorizedExceptionFlag(false);
        CreditCard creditCard = this.userDAO.getCreditCardDetails(userName);
        if (creditCard != null && creditCard.isActive()) {
            /* User Account Does not Exist. But Credit Card Exist for the User */
            site = newAccessDTO.getSite();
            PayPalDTO payPalDTO = this.paymentGateway.doSale(site, site.getAccess().get(0).getSubscriptionFee().getFee(),
                    creditCard, "changeFromCurrentlyUnPaidToUnrestrictedSubscription", userName, true);
            transactionId = payPalDTO.getTxRefNum();
            UserAccount newUserAccount = new UserAccount();
            newUserAccount.setCreditCard(creditCard);
            newUserAccount.setActive(true);
            newUserAccount.setModifiedBy(userName);
            newUserAccount.setCreatedBy(userName);
            newUserAccount.setCreatedDate(new Date());
            newUserAccount.setModifiedDate(new Date());
            /** Set the Next Billing Date **/
            Date nextBillingDate = SystemUtil.getNextBillingDate(newAccessDTO.getSubFee().getPaymentPeriod().
                    getCode()).toDate();
            Date lastBillingDate = SystemUtil.changeTimeZone(new Date(), TimeZone.getTimeZone(site.getTimeZone()));
            newUserAccount.setLastBillingDate(lastBillingDate);
            newUserAccount.setNextBillingDate(SystemUtil.changeTimeZone(nextBillingDate,
                    TimeZone.getTimeZone(site.getTimeZone())));
            newUserAccount.setMarkForCancellation(false);
            /** Set the User Access **/
            UserAccess userAccess = new UserAccess();
            userAccess.setId(existingUserAccessId);
            newUserAccount.setUserAccess(userAccess);
            /** Save the user Account **/
            this.subDAO.saveUserAccount(newUserAccount);
            enableAccess = true;
            RecurTx recurTransactionCharge = new RecurTx();
            recurTransactionCharge.setUserId(upgradeDowngradeDTO.getExistingUserAccountDetail().getUserId());
            recurTransactionCharge.setAccessId(newAccessId);
            recurTransactionCharge.setBaseAmount(site.getAccess().get(0).getSubscriptionFee().getFee());
            recurTransactionCharge.setTotalTxAmount(site.getAccess().get(0).getSubscriptionFee().getFee());
            recurTransactionCharge.setTxRefNum(payPalDTO.getTxRefNum());
            upgradeDowngradeDTO.setTransactionId(payPalDTO.getTxRefNum());
            upgradeDowngradeDTO.setCharge(true);
            recurTransactionCharge.setTransactionDate(SystemUtil.changeTimeZone(new Date(),
                    TimeZone.getTimeZone(upgradeDowngradeDTO.getExistingUserAccountDetail().getSite().getTimeZone())));
            recurTransactionCharge.setTransactionType(TransactionType.CHARGE);
            recurTransactionCharge.setSettlementStatus(SettlementStatusType.UNSETTLED);
            CardType cardType = CreditCardUtil.getCardType(creditCard.getNumber());
            recurTransactionCharge.setCardType(cardType);
            recurTransactionCharge.setCardNumber(creditCard.getNumber());
            recurTransactionCharge.setAccountName(creditCard.getName());
            recurTransactionCharge.setModifiedBy(userName);
            recurTransactionCharge.setCreatedBy(userName);
            recurTransactionCharge.setModifiedDate(new Date());
            recurTransactionCharge.setCreatedDate(new Date());
            recurTransactionCharge.setActive(true);

            if (cardType == CardType.AMEX) {
                recurTransactionCharge.setTxFeePercent(newAccessDTO.getSite().getMerchant().getTxFeePercentAmex());
                recurTransactionCharge.setTxFeeFlat(newAccessDTO.getSite().getMerchant().getTxFeeFlatAmex());
            } else {
                recurTransactionCharge.setTxFeePercent(newAccessDTO.getSite().getMerchant().getTxFeePercent());
                recurTransactionCharge.setTxFeeFlat(newAccessDTO.getSite().getMerchant().getTxFeeFlat());
            }

            recurTransactionCharge.setClientShare(newAccessDTO.getSite().getAccess().get(0).getClientShare());
            recurTransactionCharge.setMachineName(machineName);
            recurTransactionCharge.setMerchantId(payPalDTO.getMerchantId());
            this.recurTxDAO.saveRecurTransaction(recurTransactionCharge);
        }
        // If user is a firm level user admin then update the firm level users first.
        UserAccess existingUserAccess =
        		upgradeDowngradeDTO.getExistingUserAccountDetail().getSite().getAccess().get(0).getUserAccessList().get(0);
        Access existingAccess = upgradeDowngradeDTO.getExistingUserAccountDetail().getSite().getAccess().get(0);

        List<Long> userAccessIds = new ArrayList<Long>();
        // Update firm level users if user is a firm access admin
        if(existingUserAccess.isFirmAccessAdmin()){
        	// Retrieve Firm Level Users and update user access for each of them
        	List<FirmUserDTO> firmUsers = this.userDAO.getFirmUsers(userName, existingAccess.getId());
        	if(!CollectionUtils.isEmpty(firmUsers)){
            	for(FirmUserDTO firmUser : firmUsers){
            		userAccessIds.add(firmUser.getUserAccessId());
            	}
        	}
        }
        userAccessIds.add(existingUserAccessId);
        int recordsdModified = this.subDAO.updateUserAccessWithAccessId(userAccessIds, newAccessId,
                enableAccess, true,  userName, RECURRING_UNPAID_TO_UNRESTRICTED_COMMENTS);
        if (recordsdModified == 0) {
            logger.error("The updateUserAccessWithAccessId Did not Update Any Records in changeSubscription!");
            logger.error(NOTIFY_ADMIN, "Error in Change Subscription existingUserAccessId - " + existingUserAccessId +
                    "newAccessId - " + newAccessId + "userName - " + userName);
            throw new RuntimeException("The User Access is not Disabled");
        }
        upgradeDowngradeDTO.setTransactionId(transactionId);
        return upgradeDowngradeDTO;
    }

    private void validateChangeSubscription(UserAccountDetailDTO existingUserAccountDTO, AccessDetailDTO newAccessDTO)
            throws SDLBusinessException, MaxUsersExceededException {
        AccessType existingAccessType = existingUserAccountDTO.getSite().getAccess().get(0).getAccessType();
        AccessType newAccessType = newAccessDTO.getSite().getAccess().get(0).getAccessType();
        if (existingAccessType != newAccessType) {
            SDLBusinessException sDLBussExcep = new SDLBusinessException();
            sDLBussExcep.setBusinessMessage(this.getMessage("recur.subscription.notsametype"));
            throw sDLBussExcep;
        }

        // If user is a firm administrator then Validate for Maximum Allowed Users
        UserAccess existingUserAccess = existingUserAccountDTO.getSite().getAccess().get(0).getUserAccessList().get(0);
        Access newAccess = newAccessDTO.getSite().getAccess().get(0);
        if(existingUserAccess.isFirmAccessAdmin()){
        	this.firmUserValidator.validateMaxUsersAllowedForChangeSub(existingUserAccountDTO.getUserId(),
        				existingUserAccountDTO.getSite().getAccess().get(0), newAccess);
        }
    }

    private static UpgradeDowngradeDTO calculateBalance(UserAccountDetailDTO existingUserAccount, AccessDetailDTO newAccess,
            boolean isDowngrade, UpgradeDowngradeDTO upgradeDowngradeDTO) {
        DateTime nextBillingDateTime = new DateTime(existingUserAccount.getUserAccount().getNextBillingDate());
        DateTime lastBillingDateTime = new DateTime(existingUserAccount.getUserAccount().getLastBillingDate());
        DateTime todaysDateTime = new DateTime();

        int totalDays = Days.daysBetween(lastBillingDateTime.toDateMidnight(), nextBillingDateTime.toDateMidnight())
            .getDays();
        int remainingDays = 0;
        Double newBalance = 0.00d;
        Double unUsedBalance = 0.00d;

        remainingDays = Days.daysBetween(todaysDateTime.toDateMidnight(), nextBillingDateTime.toDateMidnight()).getDays();

        unUsedBalance = (existingUserAccount.getSubFee().getFee() / totalDays) * remainingDays;
        unUsedBalance = new BigDecimal(unUsedBalance).setScale(2, RoundingMode.HALF_DOWN).doubleValue();

        if(!newAccess.isAuthorizationRequired()) {
            newBalance = (newAccess.getSubFee().getFee() / totalDays) * remainingDays;
            newBalance = new BigDecimal(newBalance).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
        }

        upgradeDowngradeDTO.setCurrentFee(existingUserAccount.getSubFee().getFee());
        upgradeDowngradeDTO.setNewFee(newAccess.getSubFee().getFee());
        upgradeDowngradeDTO.setUnUsedBalance(unUsedBalance);
        upgradeDowngradeDTO.setNewBalance(newBalance);
        return upgradeDowngradeDTO;
    }


    /**
     * This method is used for adding the user access while upgrading downgrading the recurring subscription
     * @param firmUsers
     * @param newAccess
     * @param firmAdminUserAccessId
     * @param adminUserName
     */
    private void addFirmUserAccessForUpgrade(List<FirmUserDTO>  firmUsers, Access newAccess,
    				Long firmAdminUserAccessId, String adminUserName){
    	List<UserAccess> userAccessList = new ArrayList<UserAccess>();
    	for (FirmUserDTO firmUser : firmUsers) {
			User user = new User();
			user.setId(firmUser.getUserId());

			Access access = new Access();
            access.setId(newAccess.getId());

            UserAccess userAccess = new UserAccess(user, access);
            userAccess.setModifiedBy(adminUserName);
            userAccess.setCreatedBy(adminUserName);
            userAccess.setCreatedDate(new Date());
            userAccess.setModifiedDate(new Date());
            userAccess.setFirmAccessAdmin(false);
            userAccess.setFirmAdminUserAccessId(firmAdminUserAccessId);
            userAccessList.add(userAccess);
            if (newAccess.getAccessType() != AccessType.RECURRING_SUBSCRIPTION) {
                userAccess.setActive(true);
            }
            userAccess.setAuthorized(!access.isAuthorizationRequired());
        }
        this.userDAO.saveUserAcess(userAccessList);

    }

    /** This Method Is Used To Add A Firm Level Subscription For A User. It Checks For Whether That Type Of Subscription Already
     * Exists For The Site Among All The Existing Subscriptions For The User.
     *
     * It also checks if subscription is firm level subscription and it does belong to admin user.
     *
     * @param adminUserName
     * @param firmUserName
     * @param userAccessId
     * @return
     * @throws SDLBusinessException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void addFirmUserAccess(String adminUserName, String firmUserName, Long accessId, String nodeName)
    			throws UserNameNotFoundException, MaxUsersExceededException, SDLBusinessException{
        Assert.notNull(firmUserName, "firmLevelUserName Cannot be Null");
        Assert.notNull(adminUserName, "adminUserName cannot be Null");
        Assert.notNull(accessId, "accessId cannot be Null");

    	// check if  Firm Level User Exists
    	User firmUser = this.userDAO.getUser(firmUserName);
    	if(firmUser == null){
			throw new UserNameNotFoundException(this.getMessage("security.username.notfound", new String[]{firmUserName}));
    	}

    	// check if  Admin User Exists
    	User adminUser = this.userDAO.getUser(adminUserName);
    	if(adminUser == null){
			throw new UserNameNotFoundException(this.getMessage("security.username.notfound", new String[]{adminUserName}));
    	}


    	// Iterate through admin user access list and check if new user access is part of firm subscription
    	UserAccess adminUserAccess = null;
    	for(UserAccess userAccess : adminUser.getUserAccessList()){
    		// We only want to consider admin/firm accesses
    		if(userAccess.isFirmAccessAdmin() && userAccess.getAccess().getId().equals(accessId) &&
    				userAccess.getAccess().isFirmLevelAccess()) {
    			adminUserAccess = userAccess;
    			break;
   			}
    	}

    	// Check if Admin User has the subscription
    	if(adminUserAccess == null){
    		throw new SDLBusinessException(this.getMessage("security.addfirmlevelsub.accessnotfound",
    				new String[]{adminUserName, accessId.toString()}));
    	}

    	// Let's check if more users can be added to the admin user access
    	this.firmUserValidator.validateMaxUsersAllowed(adminUserAccess, adminUser);
    	this.firmUserValidator.validateForMultipleRecurringSubs(firmUserName, accessId, adminUserAccess.getAccess().getSite(), nodeName);

    	// Now let's make sure that Firm User does not already have this subscription
    	for(UserAccess userAccess : firmUser.getUserAccessList()){
    		// We only want to consider admin accesses
    		if(userAccess.getAccess().getId().equals(accessId)) {
    			// User already has this susbscription
        		throw new SDLBusinessException(this.getMessage("security.addfirmuser.userexistswithaccess",
            			new String[]{firmUser.getUsername(),
            				userAccess.isActive() ? "Active" : "Inactive",
            			userAccess.getAccess().getDescription()}));
   			}
    	}

    	// Everything is good, create an User Access for firm user
        UserAccess userAccess = new UserAccess(firmUser, adminUserAccess.getAccess());
        userAccess.setModifiedBy(adminUserName);
        userAccess.setCreatedBy(adminUserName);
        userAccess.setCreatedDate(new Date());
        userAccess.setModifiedDate(new Date());
        userAccess.setActive(true);
        userAccess.setAuthorized(true);
        userAccess.setFirmAccessAdmin(false);
        // Set the firm admin access id
        userAccess.setFirmAdminUserAccessId(adminUserAccess.getId());
        this.userDAO.saveUserAcess(userAccess);
    }


    /**
     * Remove Firm Level Access
     * There are three Scenarios Possible
     *
     * 1. User has paid transactions under the access:
     * 			Action : Throw an error that user can not be deleted : User Access can be enabled/disabled
     *
     * 2. User has no paid transactions for this access:
     * 			Action : Remove the access
     *
     *
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void removeFirmLevelUserAccess(String firmUserName, Long userAccessId, String comments, String modifiedBy,
    		boolean isSendNotification) throws  UserNameNotFoundException, SDLBusinessException  {
        Assert.notNull(firmUserName, "firmLevelUserName Cannot be Null");
        Assert.notNull(userAccessId, "userAccessId cannot be Null");
        Assert.notNull(comments, "comments cannot be Null");
        Assert.notNull(modifiedBy, "modifiedBy cannot be Null");

    	// Retrieve Firm Level user and check if  Firm Level User Exists
    	User firmUser = this.userDAO.getUser(firmUserName);
    	if(firmUser == null){
			throw new UserNameNotFoundException(this.getMessage("security.username.notfound", new String[]{firmUserName}));
    	}

    	UserAccess firmUserAccess = null;
		for(UserAccess firmAccess : firmUser.getUserAccessList()){
			// if child-parent relationship matches and access ids matches
			if(firmAccess.getFirmAdminUserAccessId() != null && !firmAccess.isFirmAccessAdmin() &&
					firmAccess.getAccess().isFirmLevelAccess() &&
					firmAccess.getId().equals(userAccessId) ){
				firmUserAccess = firmAccess;
			}
		}

    	// Check if Firm user doesn't have any firm level subscriptions.
    	if(firmUserAccess == null){
    		throw new SDLBusinessException(this.getMessage("security.enabledisableaccess.useraccessnotfound",
    				new String[]{firmUserName, userAccessId.toString()}));
    	}

    	// Get all the access ids for which user has paid the documents OR recurring transactions
    	//List<Long> paidDocumentsAccessIds = this.subDAO.getAccessIdsForPaidAccess(firmUser.getId());

        //Check if user has paid the given access
		//if(paidDocumentsAccessIds.contains(firmUserAccess.getAccess().getId())){
			//throw new SDLBusinessException(this.getMessage("security.enabledisableaccess.haspaiddocuments",
				//	new String[]{firmUserName}));
        //}

    	// Delete user accesses from the firm
    	List<Long> userAccessIdList = new ArrayList<Long>();
    	userAccessIdList.add(firmUserAccess.getId());
    	this.subDAO.deleteUserAccess(userAccessIdList);

        UserHistory userHistory = new UserHistory();
        LinkedList<UserHistory> userHistories = new LinkedList<UserHistory>();
        userHistories.add(userHistory);
        userHistory.setModifiedBy(modifiedBy);
        userHistory.setCreatedBy(modifiedBy);
        userHistory.setModifiedDate(new Date());
        userHistory.setCreatedDate(new Date());
        userHistory.setUserName(firmUserName);
        userHistory.setAccesId(firmUserAccess.getAccess().getId());
        userHistory.setComments(comments);
        this.subDAO.saveUserHistory(userHistories);

    	// Delete the User Term for a firm level access sites
    	this.subDAO.deleteUserTerm(firmUser.getId(), firmUserAccess.getAccess().getSite().getId());

    	if(isSendNotification){
	    	// Send Email to the user for each deleted access
	        Map<String, Object> emailData = new HashMap<String, Object>();
	        emailData.put("user", firmUser);
	        emailData.put("subscription", firmUserAccess.getAccess());
	        emailData.put("isCancelRequest", true);
	        emailData.put("isReactivateRequest", false);
	        emailData.put("serverUrl", this.ecomServerURL);
	        emailData.put("currentDate", new Date());

	        SiteConfiguration siteConfig = this.eComDAO.getSiteConfiguration(firmUserAccess.getAccess().getSite().getId());
	        this.emailProducer.sendMailUsingTemplate(siteConfig.getFromEmailAddress(), firmUserName,
	            siteConfig.getCancelSubscriptionSubject(), siteConfig.getEmailTemplateFolder() +
	                siteConfig.getCancelSubscriptionTemplate(), emailData);
    	}
    }

    /** Delete All Firm Level User Access
     *
     *  Steps :
     *  Create User History
     *  Delete User Terms
     *  Delete User Accesses
     *  Send Email about Removing access
     *
     * @param access
     * @param adminUserName
     * @param comments
     */
    private void removeFirmUserSubscriptions(Access access, Site site, String adminUserName, String comments,
    				List<FirmUserDTO> firmUsers, boolean isSendNotification){
    	if(CollectionUtils.isEmpty(firmUsers)){
    		return;
    	}

    	List<Long> firmUserAccessIds = new ArrayList<Long>();
    	List<Long>  firmUserIds = new ArrayList<Long>();
        LinkedList<UserHistory> userHistories = new LinkedList<UserHistory>();
    	for(FirmUserDTO firmUser : firmUsers){
            UserHistory userHistory = new UserHistory();
            userHistories.add(userHistory);
            userHistory.setModifiedBy(adminUserName);
            userHistory.setCreatedBy(adminUserName);
            userHistory.setModifiedDate(new Date());
            userHistory.setCreatedDate(new Date());
            userHistory.setUserName(firmUser.getUsername());
            userHistory.setAccesId(access.getId());
            userHistory.setComments(comments);
            userHistories.add(userHistory);

            firmUserAccessIds.add(firmUser.getUserAccessId());
            firmUserIds.add(firmUser.getUserId());
    	}
    	//this.subDAO.deleteFirmUserAccess(accessId, firmAdminUserAccessId);
        this.subDAO.saveUserHistory(userHistories);

    	// Delete the User Term for a firm level access sites
    	this.subDAO.deleteUserTerm(firmUserIds, site.getId());

    	// Delete User Accesses
    	this.subDAO.deleteUserAccess(firmUserAccessIds);

    	if(isSendNotification){
	        SiteConfiguration siteConfig = this.eComDAO.getSiteConfiguration(site.getId());
	        for(FirmUserDTO firmUser : firmUsers){
		    	// Send Email to the user for each deleted access
		        Map<String, Object> emailData = new HashMap<String, Object>();
		        emailData.put("user", firmUser);
		        emailData.put("subscription", access);
		        emailData.put("isCancelRequest", true);
		        emailData.put("isReactivateRequest", false);
		        emailData.put("serverUrl", this.ecomServerURL);
		        emailData.put("currentDate", new Date());

		        this.emailProducer.sendMailUsingTemplate(siteConfig.getFromEmailAddress(), firmUser.getUsername(),
		            siteConfig.getCancelSubscriptionSubject(), siteConfig.getEmailTemplateFolder() +
		                siteConfig.getCancelSubscriptionTemplate(), emailData);
	    	}
    	}
    }



    /**
     * This method locks or unlocks the firm level user.
     * We do not lock at user level, it is at user access level.
     * 		i.e. User Accesses are set to inactive  while locking
     *   		 User Accesses are set to active  while unlocking
     *
     *
     * @param adminUserName
     * @param firmLevelUserName
     * @param isLock
     * @param modifiedBy
     * @param isSendUserConfirmation
     * @param nodeName
     * @param additionalComments
     * @throws UserNameNotFoundException
     * @throws DeleteUserException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void enableDisableFirmLevelUserAccess(String firmLevelUserName, Long userAccessId,
			boolean isEnable, String modifiedBy, String comments)
    										throws  UserNameNotFoundException, SDLBusinessException  {
		Assert.notNull(firmLevelUserName, "firmLevelUserName Cannot be Null");
		Assert.notNull(userAccessId, "userAccessId cannot be null");
		Assert.notNull(modifiedBy, "Modified By Cannot be Null");

		// Retrieve Firm Level user and check if  Firm Level User Exists
		User firmUser = this.userDAO.getUser(firmLevelUserName);
		if(firmUser == null){
			throw new UserNameNotFoundException(this.getMessage("security.username.notfound",
				new String[]{firmLevelUserName}));
		}

		// Check if UserAccess exists
		UserAccess firmUserAccess = null;
		for(UserAccess userAccess : firmUser.getUserAccessList()) {
			if(userAccess.getId().equals(userAccessId) &&
					userAccess.getAccess().isFirmLevelAccess() &&
						userAccess.getFirmAdminUserAccessId() != null) {
				firmUserAccess = userAccess;
				break;
			}
		}
		if(firmUserAccess == null){
			throw new SDLBusinessException(this.getMessage("security.enabledisableaccess.useraccessnotfound",
					new String[]{firmLevelUserName, userAccessId.toString()}));
		}

		// Disable User Access
		userDAO.enableDisableFirmLevelUserAccess(firmUserAccess.getId(), isEnable, modifiedBy, comments, false, false);

        SiteConfiguration siteConfig = this.eComDAO.getSiteConfiguration(firmUserAccess.getAccess().getSite().getId());
		// Send an email.
		if(isEnable) {
			// User's subscription was enabled from inactive
            Map<String, Object> emailData = new HashMap<String, Object>();
            emailData.put("user", firmUser);
            emailData.put("isFirmSubEnableRequest", "TRUE");
            emailData.put("firmUserSubscription", firmUserAccess.getAccess().getDescription());
            emailData.put("serverUrl", this.ecomServerURL);
            emailData.put("currentDate", new Date());
            // Retrieve Site Configuration from database
            Assert.notNull(siteConfig, "siteConfig Cannot be Null");
            this.emailProducer.sendMailUsingTemplate(siteConfig.getFromEmailAddress(), firmUser.getUsername(),
                siteConfig.getAddSubscriptionSub(), siteConfig.getEmailTemplateFolder()
                    + siteConfig.getPaymentConfirmationTemplate(), emailData);

		} else {
			// User's subscription was disabled from active
			Map<String, Object> emailData = new HashMap<String, Object>();
	        emailData.put("user", firmUser);
	        emailData.put("subscription", firmUserAccess.getAccess());
	        emailData.put("isFirmSubDisableRequest", "TRUE");
	        emailData.put("isCancelRequest", true);
	        emailData.put("isReactivateRequest", false);
	        emailData.put("serverUrl", this.ecomServerURL);
	        emailData.put("currentDate", new Date());

	        this.emailProducer.sendMailUsingTemplate(siteConfig.getFromEmailAddress(), firmUser.getUsername(),
	            siteConfig.getCancelSubscriptionSubject(), siteConfig.getEmailTemplateFolder() +
	                siteConfig.getCancelSubscriptionTemplate(), emailData);

		}
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void ChangeFirmSubscriptionAdministrator(String newAdminUserName, Long accessId,
    		String comments, String modifiedBy) throws UserNameNotFoundException, SDLBusinessException {
    	Assert.notNull(newAdminUserName, "New User can not be Null");
        Assert.notNull(accessId, "accessId cannot be Null");

        // Load The Firm Level Admin User from database and Check if the Admin User Exist in the DB
        User currentAdminUser = this.userDAO.getFirmAdminUser(newAdminUserName, accessId);
        if(currentAdminUser == null) {
        	throw new
        	UserNameNotFoundException(this.getMessage("security.firmlevelsub.firmadminnotfound", new String[]{newAdminUserName}));
        }

        // Find User Access
        UserAccess currentAdminUserAccess = null;
        for(UserAccess userAccess : currentAdminUser.getUserAccessList()){
        	if(userAccess.getAccess().getId().equals(accessId)){
        		currentAdminUserAccess = userAccess;
        		break;
        	}
        }

        // If admin user doesn't have the subscription then throw the exception
        if(currentAdminUserAccess == null){
    		throw new SDLBusinessException(this.getMessage("security.addfirmlevelsub.accessnotfound",
    				new String[]{currentAdminUser.getUsername(), accessId.toString()}));
        }

        // If admin user is not an administrator then throw an exception
        if(!currentAdminUserAccess.isFirmAccessAdmin()){
    		throw new SDLBusinessException(this.getMessage("security.firmlevelsub.usernotadministrator",
    				new String[]{currentAdminUser.getUsername(), currentAdminUserAccess.getAccess().getDescription()}));
        }

        // Now retrieve the firm level users and make sure that new user is a part of the firm level subscription
        List<FirmUserDTO> firmUsers = this.userDAO.getFirmUsers(currentAdminUser.getUsername(), accessId);
        FirmUserDTO newAdminUser = null;
        for(FirmUserDTO firmUser : firmUsers){
        	if(firmUser.getUsername().equalsIgnoreCase(newAdminUserName)){
        		newAdminUser = firmUser;
        		break;
        	}
        }

        // If new admin user is not part of firm users then, throw an exception
        if(newAdminUser == null){
    		throw new SDLBusinessException(this.getMessage("security.firmlevelsub.usernotfirmuser",
    				new String[]{newAdminUserName, currentAdminUserAccess.getAccess().getDescription()}));
        }

        // Now let's make sure that new admin user does have credit card in the database.
        CreditCard ccInfo = this.userDAO.getCreditCardDetails(newAdminUserName);
        if(ccInfo == null){
        	throw new SDLBusinessException(this.getMessage("security.firmlevelsub.nocreditcardinfofound",
    				new String[]{newAdminUserName}));
        }

        // Now it's all set to change the administrator.
        // We do this by swapping a user id in AUTH_USER_ACCESS table
        // First Set the new user to be an administrator
        List<Long> userAccessIds = new ArrayList<Long>();
        userAccessIds.add(newAdminUser.getUserAccessId());
        this.userDAO.updateFirmUserAccess(currentAdminUserAccess.getId(), newAdminUser.getUserId(), modifiedBy, comments);

        // Now Update the current firm administrator to be a normal firm user
        this.userDAO.updateFirmUserAccess(newAdminUser.getUserAccessId(), currentAdminUser.getId(), modifiedBy, comments);

        // Also update the user account information
        this.userDAO.updateFirmCreditCardInUserAccount(currentAdminUserAccess.getId(), ccInfo.getId());
    }

    private String getMessage(String messageKey) {
        return this.messages.getMessage(messageKey, null, new Locale("en"));
    }

    private String getMessage(String messageKey, Object[] object) {
        return this.messages.getMessage(messageKey, object, new Locale("en"));
    }



}