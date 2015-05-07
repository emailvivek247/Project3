package com.fdt.security.service;

import java.util.List;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.security.entity.EComAdminAccess;
import com.fdt.security.entity.EComAdminUser;
import com.fdt.security.exception.BadPasswordException;
import com.fdt.security.exception.InvalidDataException;
import com.fdt.security.exception.UserAlreadyActivatedException;
import com.fdt.security.exception.UserNameAlreadyExistsException;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.security.exception.UserNotActiveException;

public interface EComAdminUserService {

    public void registerUser(EComAdminUser aNewUser, Long siteId) throws UserNameAlreadyExistsException;

    public void changePassword(EComAdminUser existingUser) throws UserNameNotFoundException, BadPasswordException;

    public void activateUser(String userName, String requestToken) throws InvalidDataException, UserAlreadyActivatedException;

    public void resetPassword(EComAdminUser user, String requestToken) throws UserNameNotFoundException;

    public void resetPasswordRequest(String userName) throws UserNameNotFoundException, UserNotActiveException;

    public void resendUserActivationEmail(String userName) throws UserAlreadyActivatedException, UserNameNotFoundException;

    public EComAdminUser loadUserByUsername(String userName);

    public List<EComAdminUser> getUsers();

    public List<EComAdminAccess> getAccess();

    public void lockUnLockUser(String userName, boolean isLock, String modifiedBy, boolean isSendUserConfirmation,
            String addtionalComments);

    public void assignAccess(String userName, List<Long> accessIds, String modifiedBy)	throws UserNameNotFoundException;

    public void checkValidResetPasswordRequest(String username, String token) throws UserNameNotFoundException,
        InvalidDataException;
    
    public void archiveAdminUser(String adminUserName) throws SDLBusinessException;

}
