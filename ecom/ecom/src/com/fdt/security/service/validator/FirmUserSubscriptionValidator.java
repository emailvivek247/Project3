package com.fdt.security.service.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.util.spring.SpringUtil;
import com.fdt.ecom.entity.Site;
import com.fdt.recurtx.service.validator.AbstractBaseNodeValidator;
import com.fdt.security.dao.UserDAO;
import com.fdt.security.entity.Access;
import com.fdt.security.entity.User;
import com.fdt.security.entity.UserAccess;
import com.fdt.security.exception.MaxUsersExceededException;

@Component
public class FirmUserSubscriptionValidator {

	@Autowired
	UserDAO userDAO;

    @Autowired
    private SpringUtil springUtil;

    @Autowired
    @Qualifier("serverMessageSource")
    private MessageSource messages = null;


    public void validateSubscriptions(Access access, List<Access> existingAccessList, User adminUser, User firmUser,  
    		String firmUserName, Site site, String nodeName)
    			throws SDLBusinessException, MaxUsersExceededException{

		// Find out admin User Access  from admin UserAccessList
		UserAccess adminUserAccess = null;
		for(UserAccess userAccess : adminUser.getUserAccessList()){
			// It's safe to check for isFirmAccessAdmin
			if(userAccess.getAccess().getId().equals(access.getId()) && userAccess.isFirmAccessAdmin()){
				adminUserAccess = userAccess;
				break;
			}
		}

    	// Validate the subscriptions
    	this.validateForSubscriptions(adminUserAccess, access);
    	// Validate for maximum allowed users
    	this.validateMaxUsersAllowed(adminUserAccess, adminUser);

		// Check if Firm User exists and he already has an User Access
		if(firmUser != null){
			for(UserAccess userAccess : firmUser.getUserAccessList()){
				if(userAccess.getAccess().getId().equals(access.getId())){
        			// User exists and he already has subscription
            		throw new SDLBusinessException(this.getMessage("security.addfirmuser.userexistswithaccess",
            			new String[]{firmUser.getUsername(),
            				userAccess.isActive() ? "Active" : "Inactive",
            			userAccess.getAccess().getDescription()}));
				}
			}
		}
		
		this.validateForMultipleRecurringSubs(firmUserName, access.getId(), site, nodeName);

    }

	 /**
     * Validations for :
	 * 1. Only Firm Administrator can add user
     * 2. Administrator should have paid for the subscription.
     * 3. Administrator has not subscribed & paid for Access Ids supplied by users (accessIds parameter)
     *
     * @param adminUserAccess
     * @param access
     * @throws SDLBusinessException
     */
    public void validateForSubscriptions(UserAccess adminUserAccess, Access access) throws SDLBusinessException {
    	// Check if user is an administrator
		if(adminUserAccess != null){
			// Check if user is an administrator
			if(adminUserAccess.isFirmAccessAdmin()) {
				// User is Admnistrator, now check if he has paid for the subrscription
				if(!adminUserAccess.isActive()){
					// Subscription is not paid
            		throw new SDLBusinessException(this.getMessage("security.addfirmuser.subscriptionnotpaid",
            			new String[]{adminUserAccess.getAccess().getDescription()}));
				}
			} else {
				// User is not administrator for this subscription
				throw new SDLBusinessException(this.getMessage("security.addfirmuser.usernotadministrator",
					new String[]{adminUserAccess.getAccess().getDescription()}));
			}
		} else {
			// Administrator does not have the subscription
			throw new SDLBusinessException(this.getMessage("security.addfirmuser.subscriptionnotpaid",
				new String[]{access.getDescription()}));
		}
    }


    /**
     * Validate for maximum allowed users per subscription.
     *
     * @param adminUserAccess
     * @param access
     * @throws MaxUsersExceededException
     */
    public void validateMaxUsersAllowed(UserAccess adminUserAccess, User adminUser) throws MaxUsersExceededException {
		// If max allowed users = 0 then infinite users can be added (as per business rule). No validation needed in that case.
		if(adminUserAccess.getAccess().getMaxUsersAllowed() > 0) {
			//Get the number of existing users and check with the maxmimum allowed
			int subscribedUsers  = this.userDAO.getFirmUsersCount(adminUser.getId(),
										adminUserAccess.getAccess().getId());
			// increase subscribed users by one as  admin user is also part of users
			subscribedUsers += 1;
			if(subscribedUsers >= adminUserAccess.getAccess().getMaxUsersAllowed() ){
				throw new MaxUsersExceededException(this.getMessage("security.addfirmuser.maxusresexceeded",
					new String[]{adminUserAccess.getAccess().getDescription()}));
			}
		}
    }

    /**
     * Validate for maximum allowed users for changing the subscription.
     *
     * @param adminUserAccess
     * @param access
     * @throws MaxUsersExceededException
     */
    public void validateMaxUsersAllowedForChangeSub(Long userId, Access existingAccess, Access newAccess) 
    		throws MaxUsersExceededException {
		// If max allowed users = 0 then infinite users can be added (as per business rule). No validation needed in that case.
		if(newAccess.getMaxUsersAllowed() > 0) {
			//Get the number of existing users and check with the maxmimum allowed
			int subscribedUsers  = this.userDAO.getFirmUsersCount(userId, 	existingAccess.getId());
			// increase subscribed users by one as  admin user is also part of users
			subscribedUsers += 1;
			if(subscribedUsers > newAccess.getMaxUsersAllowed() ){
				throw new MaxUsersExceededException(this.getMessage("security.addfirmuser.maxusresexceeded",
					new String[]{newAccess.getDescription()}));
			}
		}
    }
    
    public void validateForMultipleRecurringSubs(String userName, Long accessId, Site site, String nodeName) 
    		throws SDLBusinessException{
    	
	    // Validate: same type of recurring subscriptions for the same site is not allowed 
	    AbstractBaseNodeValidator baseValidator = (AbstractBaseNodeValidator) this.springUtil.getBean(nodeName);
	    if (baseValidator == null) {
	        baseValidator = (AbstractBaseNodeValidator) this.springUtil.getBean("defaultAddSubValidator");
	    }
	    List<Long> newAccessIds = new ArrayList<Long>();
	    newAccessIds.add(accessId);
	    try{
	    	baseValidator.checkForValidSubscription(userName, newAccessIds, nodeName);
	    } catch (SDLBusinessException e){
			throw new SDLBusinessException(this.getMessage("security.addfirmuser.multiplerecurringsuberrror",
					new String[]{userName, site.getDescription()}));
	    }
	    
    }
    


    private String getMessage(String messageKey, Object[] object) {
        return this.messages.getMessage(messageKey, object, new Locale("en"));
    }


}
