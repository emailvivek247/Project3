package com.fdt.subscriptions.dao;

import java.util.Date;
import java.util.List;

import com.fdt.ecom.dto.UserAccessDetailDTO;
import com.fdt.ecom.entity.UserHistory;
import com.fdt.recurtx.dto.RecurTxSchedulerDTO;
import com.fdt.recurtx.dto.UserAccountDetailDTO;
import com.fdt.recurtx.entity.UserAccount;
import com.fdt.security.entity.User;
import com.fdt.subscriptions.dto.AccessDetailDTO;
import com.fdt.subscriptions.dto.SubscriptionDTO;

public interface SubDAO {

    public List<SubscriptionDTO> getUserSubs(String userName, String nodeName, String siteName, 
    		boolean activeSubscriptionsOnly, boolean firmAdminSubscriptionsOnly);

	public SubscriptionDTO getSubDetailsForUser(String userName, Long accessId);

    public void markForCancellation(Long userAccountId, boolean isMarkForCancellation);

    public UserAccountDetailDTO getUserAccountByUserAcessId(String userName, Long userAccessId);

    public void deleteUserAccountByUserAccessId(List<Long> userAccessIds);

    public void deleteUserTerm(Long userId, Long siteId);
    
    public void deleteUserTerm(List<Long> userIds, Long siteId) ;
    
    public void deleteUserAccess(List<Long> userAccessIds);

    public void deleteFirmUserAccess(Long accessId, Long firmAdminUserAccessId);

    public void saveUserHistory(List<UserHistory> userHistories);

    public AccessDetailDTO getSubDetailsByName(String accessName);

    public List<AccessDetailDTO> getSubDetailsByAccesIds(List<Long> accessIdList);

    public List<AccessDetailDTO> getSubDetailsByAccessId(Long accessId);

    public UserAccessDetailDTO getUserAccessDetails(Long userAccessId);

    /**
     * Get the access ids for any of the paid access (recurring or pay as you go) by user id
     */
    public List<Long> getAccessIdsForPaidAccess(Long userId);

    public User getUserPaymentInfo(String username, String nodeName);

    public void saveUserAccount(UserAccount userAccount);

    public void saveUserAccount(List<UserAccount> userAccounts);

    public User getPaidSubUnpaidByUser(String userName, String nodeName);

    public int disableUserAccount(Long userAccessId, String modifiedBy);

    public List<RecurTxSchedulerDTO> getCancelledSubscriptions();

    public int updateUserAccessWithAccessId(List<Long> existingUserAccessIds, Long accessId, boolean isEnable,
            boolean enableUserAccessAuthorizedFlag, String modifiedBy, String comments);
    

    public int enableDisableCreditCard(Long userAccessId, boolean isActive, String modifiedBy);

    public User getCurrentSubscriptions(String userName);

    public int updateBillingDates(Long userAccountId, Date lastBillingDate, Date nextBillingDate, boolean isVerified,
            String modifiedBy);

    public User getRecurSubAccountInfo(String username);

}
