package com.fdt.security.service.admin;

import java.util.List;

import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.security.dto.FirmUserDTO;
import com.fdt.security.dto.SearchCriteriaDTO;
import com.fdt.security.entity.User;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.subscriptions.dto.SubscriptionDTO;

public interface UserAdminService {

    /**
     * @param userName
     * @return
     * @throws UserNameNotFoundException
     */
    public User getUserDetailsForAdmin(String userName)  throws UserNameNotFoundException;

    /**
     * @param userAccessId
     * @param isEnable
     * @param modifiedBy
     * @param comments
     * @param isAccessOverridden
     * @param endDate 
     */
    public void enableDisableUserAccess(Long userAccessId, boolean isEnable, String modifiedBy, String comments,
        boolean isAccessOverridden, String endDate);

    /**
     * @param userName
     * @param comments
     * @param modifiedBy
     * @param machineName
     * @throws SDLBusinessException
     */
    public void archiveUser(String userName, String comments, String modifiedBy, String machineName) throws SDLBusinessException;

    /**
     * @return
     */
    public List<User> getInactiveUsers();

    /**
     * @param user
     */
    public void notifyInactiveUsers(User user);
    

    /**
     * This method returns users for a pagination.
     * Search Critieria has two properties(numberOfRecords, recordCount) that indicates the how many records to be returned and which row to start from.
     *  
     * @param searchCriteria
     * @return
     */
    public PageRecordsDTO findUsers(SearchCriteriaDTO searchCriteria);


	/**
	 * 
	 * @param userName
	 * @param siteName
	 * @return
	 */
    public List<SubscriptionDTO> getUserInfoForAdmin(String userName, String siteName);

    
    /**
     * Get Users with same access and has same firmaccess administrator 
     * 
     * @param userName
     * @param accessId
     * @return 
     */
    public List<FirmUserDTO> getFirmUsersbySubscriptionAndUserName(String userName, Long accessId);

}
