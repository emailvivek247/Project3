package com.fdt.security.dao;

import java.util.List;

import com.fdt.security.entity.EComAdminAccess;
import com.fdt.security.entity.EComAdminUser;
import com.fdt.security.entity.EComAdminUserAccess;
import com.fdt.security.entity.EComAdminUserEvent;
import com.fdt.security.entity.EComAdminUserSite;

public interface EComAdminUserDAO {

    public EComAdminUser getUserDetails(String username);

    public List<EComAdminAccess> getAccess();

    public void saveAccess(EComAdminAccess access);

    public void saveUser(EComAdminUser user);

    public void saveUserAcess(List<EComAdminUserAccess> userAccess);

    public void saveUserAcess(EComAdminUserAccess userAccess);

    public void saveUserSite(List<EComAdminUserSite> eComAdminUserSites);

    public void saveUserSite(EComAdminUserSite eComAdminUserSite);

    public void deleteUserEvents(List<EComAdminUserEvent> userEvents);

    public EComAdminUserEvent findUserEvent(String userName, String requestToken);

    public EComAdminUserEvent findUserEvent(String userName);

    public void enableDisableUserAccess(List<Long> userAccessIds, boolean isEnable);

    public void enableDisableUserAccess(Long userAccessId, boolean isEnable);

    public EComAdminUser getUser(String username);

    public void updateLastLoginTime(String username);

    public List<EComAdminUser> getUsers();

    public void lockUnLockUser(String userName, boolean isLock, String modifiedBy);

    public void saveUserEvent(EComAdminUserEvent userEvent);

    public void saveUserEvent(List<EComAdminUserEvent> eComAdminUserEvents);

    public void deleteExistingUserAccesses(Long userId);

    public void updatePassword(String userName, String encodedPassword);

    public int archiveAdminUser(String adminUserName);

	public void updateModifiedDateOfEComAdminUserEvent(Long id);
}
