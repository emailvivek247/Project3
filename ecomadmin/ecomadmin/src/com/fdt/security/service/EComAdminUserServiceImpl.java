package com.fdt.security.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.email.EmailProducer;
import com.fdt.security.dao.EComAdminUserDAO;
import com.fdt.security.entity.EComAdminAccess;
import com.fdt.security.entity.EComAdminSite;
import com.fdt.security.entity.EComAdminUser;
import com.fdt.security.entity.EComAdminUserAccess;
import com.fdt.security.entity.EComAdminUserEvent;
import com.fdt.security.entity.EComAdminUserSite;
import com.fdt.security.exception.BadPasswordException;
import com.fdt.security.exception.InvalidDataException;
import com.fdt.security.exception.UserAlreadyActivatedException;
import com.fdt.security.exception.UserNameAlreadyExistsException;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.security.exception.UserNotActiveException;

@Service("eComAdminUserService")
public class EComAdminUserServiceImpl implements EComAdminUserService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private EComAdminUserDAO eComAdminUserDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailProducer emailProducer;

    @Autowired
    @Qualifier("serverMessageSource")
    private MessageSource messages = null;

    @Value("${email.fromemailaddress}")
    private String fromEMailAddress = null;

    @Value("${email.useractivation.requesturl}")
    private String requestURL = null;

    @Value("${email.useractivation.subject}")
    private String userActivationEMailSubject = null;

    @Value("${email.resetpassword.subject}")
    private String resetPasswordEMailSubject = null;

    @Value("${email.lockuseremail.subject}")
    private String lockUserEMailSubject = null;

    @Value("${email.unlockuseremail.subject}")
    private String unlockUserEmailESubject = null;

    @Value("${email.useractivation.emailtemplatefile}")
    private String userActivationEMailTemplateFile = null;

    @Value("${email.resetpassword.emailtemplatefile}")
    private String resetPasswordEMailTemplateFile = null;

    @Value("${email.lockuser.emailtemplatefile}")
    private String lockUserEmailTemplateFile = null;

    @Value("${email.unlockuser.emailtemplatefile}")
    private String unlockUserEmailTemplateFile = null;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void registerUser(EComAdminUser eComAdminUser, Long siteId) throws UserNameAlreadyExistsException {
        Assert.notNull(eComAdminUser, "newUser Cannot be Null");
        Assert.notNull(siteId, "siteId Cannot be Null");
        if (logger.isDebugEnabled()) {
            logger.debug("The New User Registeration Details are {} ", eComAdminUser);
        }
        String userName = eComAdminUser.getUsername();
        EComAdminUser existingUser = eComAdminUserDAO.getUserDetails(userName);
        if (logger.isDebugEnabled()) {
            logger.debug("Printing Existing user Details {} " ,existingUser);
        }
        if (existingUser != null) {
            throw new UserNameAlreadyExistsException(this.getMessage("security.user.alreadyexistinsite",
                new String[]{userName}));
        }
        String encodedPassword = this.passwordEncoder.encodePassword(eComAdminUser.getPassword(), null);
        eComAdminUser.setPassword(encodedPassword);
        eComAdminUser.setAccountNonLocked(false);
        EComAdminSite eComAdminUserSite = new EComAdminSite();
        eComAdminUserSite.setId(siteId);
        this.eComAdminUserDAO.saveUser(eComAdminUser);
        EComAdminUserSite adminUserSite = new EComAdminUserSite(eComAdminUser, eComAdminUserSite);
        adminUserSite.setCreatedBy(userName);
        adminUserSite.setModifiedBy(userName);
        this.eComAdminUserDAO.saveUserSite(adminUserSite);
        String controllerURL = requestURL + "publicactivateuser.admin?token=";
        this.sendUserMail(eComAdminUser, fromEMailAddress, userActivationEMailTemplateFile, userActivationEMailSubject,
                controllerURL);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void assignAccess(String userName, List<Long> newAccessIds, String modifiedBy)
            throws UserNameNotFoundException {
        Assert.notNull(userName, "userName Cannot be Null");
        EComAdminUser existingUser = eComAdminUserDAO.getUserDetails(userName);
        if (logger.isDebugEnabled()) {
            logger.debug("Printing Existing user Details" + existingUser);
        }
        if (existingUser == null) {
            throw new UserNameNotFoundException("The user is Not registered " + userName);
        }
        this.eComAdminUserDAO.deleteExistingUserAccesses(existingUser.getId());
        List<EComAdminUserAccess> eComAdminUserAccessList = new LinkedList<EComAdminUserAccess>();
        for(Long accessId : newAccessIds) {
            EComAdminAccess eComAdminAccess = new EComAdminAccess();
            eComAdminAccess.setId(accessId);
            EComAdminUserAccess eComAdminUserAccess = new EComAdminUserAccess(existingUser, eComAdminAccess);
            eComAdminUserAccess.setCreatedBy(modifiedBy);
            eComAdminUserAccess.setModifiedBy(modifiedBy);
            eComAdminUserAccessList.add(eComAdminUserAccess);
        }
        this.eComAdminUserDAO.saveUserAcess(eComAdminUserAccessList);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void changePassword(EComAdminUser eComAdminUser) throws UserNameNotFoundException, BadPasswordException {
        Assert.notNull(eComAdminUser, "user Cannot be Null");
        EComAdminUser existingUser = eComAdminUserDAO.getUser(eComAdminUser.getUsername());
        if (logger.isDebugEnabled()) {
            logger.debug("Printing Existing user Details" + existingUser);
        }
        if (existingUser == null) {
            throw new UserNameNotFoundException(this.getMessage("security.user.usernamenotfound",
                    new String[]{eComAdminUser.getUsername()}));
        }
        boolean isPasswordValid = this.passwordEncoder.isPasswordValid(existingUser.getPassword(),
                                        eComAdminUser.getExistingPassword(), null);
        if (!isPasswordValid) {
            throw new BadPasswordException(this.getMessage("security.password.doesnotmatch"));
        }
        String encodedChangedPassword = this.passwordEncoder.encodePassword(eComAdminUser.getPassword(), null);
        existingUser.setPassword(encodedChangedPassword);
        this.eComAdminUserDAO.saveUser(existingUser);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void lockUnLockUser(String userName, boolean isLock, String modifiedBy, boolean isSendUserConfirmation,
            String addtionalComments) {
        Assert.notNull(userName, "user Cannot be Null");
        Assert.notNull(modifiedBy, "modifiedBy Cannot be Null");
        EComAdminUser user = this.eComAdminUserDAO.getUserDetails(userName);
        this.eComAdminUserDAO.lockUnLockUser(userName, isLock, modifiedBy);
        if (isSendUserConfirmation) {
            String emailSubject = null;
            String emailTemplateFile = null;
            Map<String, Object> emailData = new HashMap<String, Object>();
            if (isLock) {
                emailTemplateFile = this.lockUserEmailTemplateFile;
                emailSubject = this.lockUserEMailSubject;
            } else {
                emailTemplateFile = this.unlockUserEmailTemplateFile;
                emailSubject = this.unlockUserEmailESubject;
            }
            emailData.put("user", user);
            emailData.put("additionalComments", addtionalComments);
            this.emailProducer.sendMailUsingTemplate(this.fromEMailAddress, userName, emailSubject,	emailTemplateFile,
                emailData);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void resetPasswordRequest(String userName) throws UserNameNotFoundException, UserNotActiveException {
        Assert.notNull(userName, "userName Cannot be Null");
        Assert.notNull(requestURL, "requestURL Cannot be Null");
        EComAdminUser existingUser = eComAdminUserDAO.getUserDetails(userName);
        if (logger.isDebugEnabled()) {
            logger.debug("Printing Existing user Details" + existingUser);
        }
        if (existingUser == null) {
            throw new UserNameNotFoundException(this.getMessage("security.resetpassword.userReset"));
        }
        String controllerURL = requestURL + "publicresetpassword.admin?token=";
        if (existingUser != null && existingUser.isActive()) {
            this.sendResetPasswordMail(existingUser, this.resetPasswordEMailTemplateFile, controllerURL);
        } else {
            throw new UserNotActiveException(this.getMessage("security.user.notactive", new String[]{userName}));
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void resetPassword(EComAdminUser user, String requestToken)  throws UserNameNotFoundException {
        Assert.notNull(user, "user Cannot be Null");
        Assert.notNull(requestToken, "requestToken Cannot be Null");
        EComAdminUserEvent userEvent = this.eComAdminUserDAO.findUserEvent(user.getUsername(), requestToken);
        if (userEvent == null || userEvent.getUser() == null) {
            throw new UserNameNotFoundException("user name not found");
        }
        EComAdminUser existingUser = userEvent.getUser();
        String encodedPassword = this.passwordEncoder.encodePassword(user.getPassword(), null);
        existingUser.setPassword(encodedPassword);
        this.eComAdminUserDAO.updatePassword(user.getUsername(), encodedPassword);
        List<EComAdminUserEvent> userEventList = new ArrayList<EComAdminUserEvent>();
        userEventList.add(userEvent);
        this.eComAdminUserDAO.deleteUserEvents(userEventList);
    }

    @Transactional(readOnly = true)
    public void checkValidResetPasswordRequest(String userName, String requestToken)
            throws UserNameNotFoundException, InvalidDataException {
        Assert.notNull(userName, "user Cannot be Null");
        Assert.notNull(requestToken, "requestToken Cannot be Null");
        EComAdminUserEvent userEvent = this.eComAdminUserDAO.findUserEvent(userName, requestToken);
        if(userEvent == null) {
            throw new InvalidDataException(this.getMessage("security.activationlink.invalid"));
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void activateUser(String userName, String requestToken)
            throws InvalidDataException, UserAlreadyActivatedException {
        Assert.notNull(userName, "userName Cannot be Null");
        Assert.notNull(requestToken, "requestToken Cannot be Null");
        EComAdminUserEvent userEvent = this.eComAdminUserDAO.findUserEvent(userName, requestToken);
        if(userEvent == null) {
            throw new InvalidDataException(this.getMessage("security.activationlink.invalid"));
        }
        if (userEvent.getUser() != null && userEvent.getUser().isActive()) {
            throw new UserAlreadyActivatedException(this.getMessage("security.user.alreadyactivated",
                new String[]{userName}));
        }
        if (userEvent != null) {
            EComAdminUser user = userEvent.getUser();
            user.setActive(true);
            user.setModifiedDate(new Date());
            this.eComAdminUserDAO.saveUser(user);
            List<EComAdminUserEvent> userEvents = new ArrayList<EComAdminUserEvent>();
            userEvents.add(userEvent);
            this.eComAdminUserDAO.deleteUserEvents(userEvents);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    private void sendUserMail(EComAdminUser user, String fromEMailAddress, String emailTemplateFile, String subject,
            String controllerURL) {
        Assert.notNull(user, "user Cannot be Null");
        Assert.notNull(controllerURL, "requestURL Cannot be Null");
        EComAdminUserEvent userEvent =  new EComAdminUserEvent();
        userEvent.setControllerURL(controllerURL);
        userEvent.setUser(user);
        userEvent.setFromEMailAddress(fromEMailAddress);
        userEvent.setSubject(subject);
        userEvent.setModifiedBy(user.getUsername());
        userEvent.setEmailTemplateFile(emailTemplateFile);
        this.eComAdminUserDAO.saveUserEvent(userEvent);
        this.emailProducer.sendMailUsingTemplate(userEvent);
    }

    @Transactional(readOnly = true)
    private void sendResetPasswordMail(EComAdminUser user, String clientName, String controllerURL)
            throws UserNameNotFoundException {
        Assert.notNull(user, "user Cannot be Null");
        Assert.notNull(controllerURL, "controllerURL Cannot be Null");
        EComAdminUserEvent eComAdminUserEvent = this.eComAdminUserDAO.findUserEvent(user.getUsername());
        if (eComAdminUserEvent == null) {
            this.sendUserMail(user, fromEMailAddress, this.resetPasswordEMailTemplateFile,
                    resetPasswordEMailSubject, controllerURL);
        } else {
            /** This means the User already Requested a Reset Password **/
            eComAdminUserEvent.setFromEMailAddress(this.fromEMailAddress);
            eComAdminUserEvent.setSubject(this.resetPasswordEMailSubject);
            eComAdminUserEvent.setControllerURL(controllerURL);
            eComAdminUserEvent.setEmailTemplateFile(this.resetPasswordEMailTemplateFile);
            this.emailProducer.sendMailUsingTemplate(eComAdminUserEvent);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void resendUserActivationEmail(String userName) throws UserNameNotFoundException, UserAlreadyActivatedException {
        Assert.notNull(userName, "userName Cannot be Null");
          String controllerURL = requestURL + "publicactivateuser.admin?token=";
          EComAdminUser eComAdminUser = this.loadUserByUsername(userName);
        if (eComAdminUser == null) {
            throw new UserNameNotFoundException(this.getMessage("security.email.notregistered", new String[]{userName}));
        }
        if (eComAdminUser.isActive()) {
            throw new UserAlreadyActivatedException(this.getMessage("security.user.alreadyactivated",
                    new String[]{userName}));
        }
        EComAdminUserEvent eComAdminUserEvent = this.eComAdminUserDAO.findUserEvent(userName);
        if (eComAdminUserEvent == null) {
            this.sendUserMail(eComAdminUser, this.fromEMailAddress, this.userActivationEMailTemplateFile,
                this.userActivationEMailSubject, controllerURL);
        } else {
            eComAdminUserEvent.setEmailTemplateFile(this.userActivationEMailTemplateFile);
            eComAdminUserEvent.setSubject(this.userActivationEMailSubject);
            eComAdminUserEvent.setControllerURL(controllerURL);
            eComAdminUserEvent.setFromEMailAddress(this.fromEMailAddress);
            this.emailProducer.sendMailUsingTemplate(eComAdminUserEvent);
            this.eComAdminUserDAO.updateModifiedDateOfEComAdminUserEvent(eComAdminUserEvent.getId());
        }
    }

    @Transactional(readOnly = true)
    public List<EComAdminUser> getUsers() {
        return this.eComAdminUserDAO.getUsers();
    }

    /** Used Only For Security **/
    @Transactional(readOnly = true)
    public EComAdminUser loadUserByUsername(String userName) {
        Assert.notNull(userName, "userName Cannot be Null");
        /** This has to be used for Security without E-com **/
        //return userDAO.getUser(userName);
        EComAdminUser user = this.eComAdminUserDAO.getUserDetails(userName);
        this.eComAdminUserDAO.updateLastLoginTime(userName);
        return user;
    }

    @Transactional(readOnly = true)
    public List<EComAdminAccess> getAccess() {
        return this.eComAdminUserDAO.getAccess();
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor=Throwable.class)
    public void archiveAdminUser(String adminUserName) throws SDLBusinessException {
        Assert.hasLength(adminUserName,    "User Name Cannot Be Null/Empty");
        int noOfRecordsModifed = this.eComAdminUserDAO.archiveAdminUser(adminUserName);
        if (noOfRecordsModifed == 0) {
        	logger.error("User " + adminUserName + " cannot be archieved");
            throw new SDLBusinessException("User " + adminUserName + " cannot be archieved");
        }
    }

    private String getMessage(String messageKey, Object[] object) {
        return this.messages.getMessage(messageKey, object, new Locale("en"));
    }

    private String getMessage(String messageKey) {
        return this.messages.getMessage(messageKey, null, new Locale("en"));
    }
}