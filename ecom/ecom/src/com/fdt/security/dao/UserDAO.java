package com.fdt.security.dao;

import java.util.List;

import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.Term;
import com.fdt.security.dto.FirmUserDTO;
import com.fdt.security.entity.Access;
import com.fdt.security.entity.User;
import com.fdt.security.entity.UserAccess;
import com.fdt.security.entity.UserEvent;
import com.fdt.security.exception.UserNameNotFoundException;

public interface UserDAO {

    public User getUserDetails(String username, String nodeName);

    public User getUserDetailsForAdmin(String username);

    public List<Access> getAccess();

    public List<String> getSitesForUser(String username);

    public void saveAccess(Access access);

    public void saveUser(User user);

    public void saveUserAcess(List<UserAccess> userAccess);
    
    public CreditCard getCreditCardDetails(Long userId);

    public CreditCard getCreditCardDetails(String userName);
    
    public CreditCard getFirmCreditCardDetails(String userName);

    public void saveUserAcess(UserAccess userAccess);

    public void saveUserEvent(UserEvent userEvent);

    public void saveUserEvent(List<UserEvent> userEvents);

    public void deleteUserEvents(List<UserEvent> userEvents);

    public UserEvent findUserEvent(String userName, String requestToken);

    public UserEvent findUserEvent(String userName);

    public void saveCreditCard(CreditCard creditCard);

    public void saveCreditCard(List<CreditCard> creditCards);

    public int enableDisableFirmLevelUserAccess(Long userAccessId, boolean isEnable, String modifiedBy, String comments,
        boolean isAccessOverridden, boolean isFirmAccessAdmin);

    public int enableDisableUserAccesses(List<Long> userAccessIds, boolean isEnable, String modifiedBy, String comments,
        boolean isAccessOverridden);

    public int enableDisableUserAccess(Long userAccessId, boolean isEnable, String modifiedBy, String comments,
        boolean isAccessOverridden);

    public User getUser(String username);
    
    public User getFirmAdminUser(String firmUserName, Long accessId);

    public int updateLastLoginTime(String username);

    public void lockUnLockUser(String userName, boolean isLock, String modifiedBy);

    public void updatePassword(String userName, String encodedPassword, String modifiedBy);

    public void updateUser(User updatedUser, String modifiedBy) throws UserNameNotFoundException;

    public boolean isUserArchivable(String userName);

    public int archiveUser(String userName, String comments, String modifiedBy, String machineName);

    public List<Term> getNewTermsAndConditionsforUser(String userName, String nodeName);

    public List<User> getInactiveUsers();
    
    public List<FirmUserDTO> getFirmUsersbySubscriptionAndUserName(String userName, Long accessId);

	public void updateModifiedDateOfUserEvent(Long userEventId);

    /**
     *  This method will retrieve the total number of users for a given firm and subscription
     *
     * @param userId
     * @param accessId
     * @return
     */
	public int getFirmUsersCount(Long adminUserId, Long accessId);

	/**
     * This method return all the users for a given firm (admin user id):
     * If subscription (accessId is supplied then it will find the users under a given subscriptions
	 *
	 * @param userName
	 * @return
	 */
    public List<FirmUserDTO> getFirmUsers(String adminUserName, Long accessId);

    /**
     * Find out Admin UserAccess List by user name
     *
     * @param userName
     * @return
     */
    public List<UserAccess> getAdminUserAccessByUserName(String userName);


    /**
     * Find User Access by user ID AND  access ids
     *
     * @param accessIds
     * @return
     */
    public List<UserAccess> getUserAccessByUserIdAccessIds(String userId, List<Long> accessIds);

    /**
     * Find Firm User Access by user ID AND  access ids
     *
     * @param userId
     * @param accessIds
     * @return
     */
    public List<UserAccess> getFirmUserAccessByUserNameAccessId(String userName, Long accessId);

    /**
     * Get all the firm level users (child) by parent/admin user
     *
     * @param adminUserId
     * @param accessId
     * @return
     */
    public List<UserAccess> getUserAccessForFirmLevelUsers(Long adminUserId, Long accessId);

    public int authorize(List<Long> userAccessIds, boolean isAuthorized, String modifiedBy, boolean isActive,
    		boolean isFirmAccessAdmin);
    
    public FirmUserDTO getUserByUserAccessId(Long userAccessId);
    
    
    public int updateFirmUserAccess(Long userAccessId, Long userId, 
    								String modifiedBy, String comments);
    
    public int updateFirmCreditCardInUserAccount(Long currentUserAccessId, Long creditCardId);

	public void updateisEmailNotificationSent(String username, boolean isEmailNotificationSent);

}