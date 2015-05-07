package com.fdt.test;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.ecom.entity.Term;
import com.fdt.recurtx.service.RecurTxService;
import com.fdt.security.entity.User;
import com.fdt.security.exception.BadPasswordException;
import com.fdt.security.exception.InvalidDataException;
import com.fdt.security.exception.UserAlreadyActivatedException;
import com.fdt.security.exception.UserNameAlreadyExistsException;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.security.exception.UserNotActiveException;
import com.fdt.security.service.UserService;
import com.fdt.security.service.admin.UserAdminService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:C:\\Projects\\SDL\\2.9\\Enterprise\\ecom\\ecom\\WebContent\\WEB-INF\\conf\\spring\\applicationContext.xml"})
public class UserServiceTestCase {

    @Autowired
    private UserService userService = null;

    @Autowired
    private UserAdminService userAdminService = null;

    @Autowired
    private RecurTxService recurTXService = null;

    @Autowired
    @Qualifier(value="strongEncryptor")
    protected PBEStringEncryptor pbeStringEncryptor;

    public UserServiceTestCase() {
        System.setProperty("CONFIG_LOCATION", "file:C:\\Projects\\SDL\\2.9\\Enterprise\\ecom\\ecom\\WebContent\\WEB-INF\\conf");
    }

    private User getNewUser(){
    	User user = new User();
    	String userName = "valampally@amcad.com";
    	user.setUsername(userName);
    	String password = "vivek123";
    	user.setPassword(password);
    	user.setPhone("6184025145");
    	user.setFirstName("Vivekanand");
    	user.setLastName("Alampally");
    	user.setCreatedBy(userName);
    	user.setModifiedBy(userName);
    	return user;
    }


    private void lockUnLockUser() {
    	String userName = "valampally@amcad.com";
    	String modifiedBy = "TEST CASE";
    	String nodeName = "RECORDSMANAGEMENT";
    	String additionalComments = "Locked By Test Case";
    	boolean isLock = true;
    	boolean isSendUserConfirmation = true;
    	User user =  this.userService.loadUserByUsername(userName, nodeName);
    	if(user.isAccountNonLocked()){
    		System.out.println("Locking the User");
    		userService.lockUnLockUser(userName, isLock, modifiedBy, isSendUserConfirmation, nodeName, null);
    		assertEquals(false, this.userService.loadUserByUsername(userName, nodeName).isAccountNonLocked());
    	} else {
    		System.out.println("Un Locking the User");
    		additionalComments = "UnLocked By Test Case";
        	isLock = false;
        	userService.lockUnLockUser(userName, isLock, modifiedBy, isSendUserConfirmation, nodeName, null);
        	assertEquals(true, this.userService.loadUserByUsername(userName, nodeName).isAccountNonLocked());
    	}
    }

    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void registerUser() {
    	User user = this.getNewUser();
    	String userName = user.getUsername();
    	String password = user.getPassword();
    	Long siteId = 1L;
    	Long accessId = 4L;
    	String nodeName = "RECORDSMANAGEMENT";
    	String requestURL = "http://nv-rd1.amcad.com/suffolkailistest/";
		try {
			long startTime = System.currentTimeMillis();
			this.userService.registerUser(user, siteId, accessId, nodeName, requestURL);
			long endTime = System.currentTimeMillis();
			System.out.println("Time taken to execute addFirmLevelUser is :"+ (endTime - startTime) + " milliseconds");
		} catch (UserNameAlreadyExistsException e) {
			System.out.println(userName + "Was Already Registered.." + e.getMessage());
		}

		User outputUser =  this.userService.loadUserByUsername(user.getUsername(), user.getRegisteredNode());
		System.out.println(outputUser);
		assertEquals(user.getUsername(), outputUser.getUsername());
		assertEquals(password, pbeStringEncryptor.decrypt(outputUser.getPassword()));
		assertEquals(user.getPhone(), outputUser.getPhone());
		assertEquals(user.getFirstName(), outputUser.getFirstName());
		assertEquals(user.getLastName(), outputUser.getLastName());
		try {
			userAdminService.archiveUser(userName, "Deleted By Jnit", "JUnit", "Junit");
		} catch (SDLBusinessException e) {
			System.out.println(e.getMessage());
		}
    }

	@Test
    public void activateUser() {

		User user = this.getNewUser();
    	String userName = user.getUsername();
    	Long siteId = 1L;
    	Long accessId = 4L;
    	String nodeName = "RECORDSMANAGEMENT";
    	String requestURL = "http://nv-rd1.amcad.com/suffolkailistest/";
		try {
			this.userService.registerUser(user, siteId, accessId, nodeName, requestURL);
		} catch (UserNameAlreadyExistsException e) {
			System.out.println(userName + "Was Already Registered.." + e.getMessage());
		}

		try {
        	String token = userService.findUserEvent(userName).getToken();
			userService.activateUser(userName, token);
		} catch (InvalidDataException e) {
			System.out.println(e.getMessage());
		} catch (UserAlreadyActivatedException e) {
			System.out.println(e.getMessage());
		}
		User outputUser =  this.userService.loadUserByUsername(userName, nodeName);
		assertEquals(true, outputUser.isEnabled());
		/*try {
			userAdminService.archiveUser(userName, "Deleted By Jnit", "JUnit", "Junit");
		} catch (SDLBusinessException e) {
			System.out.println(e.getMessage());
		}*/
    }

    @Test
    public void changePassword() {
    	User user = new User();
    	String userName = "valampally@amcad.com";
    	String updatedPassword = "vivek731";
		user.setUsername(userName);
		user.setPassword(updatedPassword);
		user.setExistingPassword("vivek784");
		try {
			userService.changePassword(user);
		} catch (UserNameNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (BadPasswordException e) {
			System.out.println(e.getMessage());
		}
		User getUser =  this.userService.loadUserByUsername(userName, "RECORDSMANAGEMENT");
		assertEquals(updatedPassword, pbeStringEncryptor.decrypt(getUser.getPassword()));
		System.out.println(pbeStringEncryptor.decrypt(getUser.getPassword()));
    }

    @Test
    public void lockUnLockUserFunctionality() {
    	this.lockUnLockUser();
    	this.lockUnLockUser();
    }



    @Test
    public void resetPasswordRequest() {
    	String userName = "valampally@amcad.com";
    	String nodeName = "RECORDSMANAGEMENT";
    	String requestURL = "http://nv-rd1.amcad.com/suffolkailistest/";

    	try {
			userService.resetPasswordRequest(userName, nodeName, requestURL);
		} catch (UserNameNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (UserNotActiveException e) {
			System.out.println(e.getMessage());
		}

    	String token = userService.findUserEvent(userName).getToken();
    	User user = new User();
    	String updatedPassword = "vivek123456";
    	user.setUsername(userName);
    	user.setPassword(updatedPassword);
    	try {
			userService.resetPassword(user, token);
		} catch (UserNameNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (InvalidDataException e) {
			System.out.println(e.getMessage());
		}
    	User getUser =  this.userService.loadUserByUsername(userName, nodeName);
    	assertEquals(updatedPassword, pbeStringEncryptor.decrypt(getUser.getPassword()));
		System.out.println(pbeStringEncryptor.decrypt(getUser.getPassword()));
    }

    @Test
    public void updateUser() {
    	User updatedUser = new User();
    	String userName = "valampally@amcad.com";
    	String firstName = "Maria";
    	String lastName = "Sharapova";
    	String phone = "2314567894";
    	String nodeName = "RECORDSMANAGEMENT";
    	updatedUser.setUsername(userName);
    	updatedUser.setFirstName(firstName);
    	updatedUser.setLastName(lastName);
    	updatedUser.setPhone(phone);
    	try {
			userService.updateUser(updatedUser, "Test");
		} catch (UserNameNotFoundException e) {
			System.out.println(e.getMessage());
		}
    	User getUser =  this.userService.loadUserByUsername(userName, nodeName);
    	assertEquals(userName, getUser.getUsername());
    	assertEquals(firstName, getUser.getFirstName());
    	assertEquals(lastName, getUser.getLastName());
    	assertEquals(phone, getUser.getPhone());
    }

    @Test
    public void resendUserActivationEmail() {
    	String userName = "valampally@amcad.com";
    	String nodeName = "RECORDSMANAGEMENT";
    	String requestURL = "http://nv-rd1.amcad.com/suffolkailistest/";
    	try {
			userService.resendUserActivationEmail(userName, nodeName, requestURL);
		} catch (UserNameNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (UserAlreadyActivatedException e) {
			System.out.println(e.getMessage());
		}
    }

    @Test
    public void updateLastLoginTime() {
    	String userName = "valampally@amcad.com";
    	try {
			userService.updateLastLoginTime(userName);
		} catch (UserNameNotFoundException e) {
			System.out.println(e.getMessage());
		}

    }

    @Test
    public void getNewTermsAndConditionsforUser() {
    	String userName = "vivek4348@gmail.com";
    	List<Term> termList = userService.getNewTermsAndConditionsforUser(userName, "RECORDSMANAGEMENT");
    	System.out.println(termList);
    	for (Term term: termList) {
    		System.out.println(term);
    	}
    }

    @Test
    public void updateUserTerms() {
    	String userName = "vivek4348@gmail.com";
    	List<Term> newTermsList = new LinkedList<Term>();
    	User user = userService.loadUserByUsername(userName, "RECORDSMANAGEMENT");
		Term term = new Term();
		term.setId(new Long(2));
		newTermsList.add(term);
		user.setTerms(newTermsList);
		try {
			userService.updateUserTerms(user);
		} catch (UserNameNotFoundException e) {
			System.out.println(e.getMessage());
		}
    }



}