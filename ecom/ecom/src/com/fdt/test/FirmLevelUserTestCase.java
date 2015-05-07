package com.fdt.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.enums.CardType;
import com.fdt.ecom.exception.AccessUnAuthorizedException;
import com.fdt.recurtx.service.RecurTxService;
import com.fdt.security.entity.User;
import com.fdt.security.exception.MaxUsersExceededException;
import com.fdt.security.exception.UserNameAlreadyExistsException;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.security.service.UserService;
import com.fdt.security.service.admin.UserAdminService;
import com.fdt.subscriptions.dto.SubscriptionDTO;
import com.fdt.subscriptions.service.SubService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:C:\\Projects\\SDL\\2.9\\Enterprise\\ecom\\ecom\\WebContent\\WEB-INF\\conf\\spring\\applicationContext.xml"})
public class FirmLevelUserTestCase {

    @Autowired
    private UserService userService = null;


    @Autowired
    private UserAdminService userAdminService = null;

    @Autowired
    private RecurTxService recurTXService = null;

    @Autowired
    private SubService subService = null;

    public FirmLevelUserTestCase() {
        System.setProperty("CONFIG_LOCATION", "file:C:\\Projects\\SDL\\2.9\\Enterprise\\ecom\\ecom\\WebContent\\WEB-INF\\conf");
    }

    private User getNewFirmLevelUser(){
    	User user = new User();
    	String userName = "arvindj2ee@hotmail.com";
    	user.setUsername(userName);
    	String password = "test1234";
    	user.setPassword(password);
    	user.setPhone("1112223333");
    	user.setFirstName("Arvind");
    	user.setLastName("Patel");
    	user.setCreatedBy(userName);
    	user.setModifiedBy(userName);
    	return user;
    }

    /**
     * Add Firm Level User Test Case
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void testAddFirmLevelUser() {
    	User user = this.getNewFirmLevelUser();
    	String userName = user.getUsername();
    	Long accessId = 36L;
    	String nodeName = "RECORDSMANAGEMENT";
    	String requestURL = "http://nv-rd1.amcad.com/suffolkailistest/";
    	String adminUserName = "apatel@amcad.com";
		try {
			long startTime = System.currentTimeMillis();
			this.userService.addFirmLevelUser(user, adminUserName, accessId, nodeName,  requestURL);
			long endTime = System.currentTimeMillis();
			System.out.println("Time taken to execute addFirmLevelUser is :"+ (endTime - startTime) + " milliseconds");
		} catch (SDLBusinessException e) {
			Assert.fail(e.getMessage());
		} catch (MaxUsersExceededException e) {
			Assert.fail(e.getMessage());
		} catch (UserNameNotFoundException e) {
			Assert.fail(e.getMessage());
		}

		User outputUser =  this.userService.loadUserByUsername(user.getUsername(), user.getRegisteredNode());
		System.out.println(outputUser);
		assertEquals(user.getUsername(), outputUser.getUsername());
		assertEquals(user.getPhone(), outputUser.getPhone());
		assertEquals(user.getFirstName(), outputUser.getFirstName());
		assertEquals(user.getLastName(), outputUser.getLastName());
		try {
			userAdminService.archiveUser(userName, "Deleted By Jnit", "JUnit", "Junit");
		} catch (SDLBusinessException e) {
			Assert.fail(e.getMessage());
		}
    }

    /**
     * Add Firm Level User Validation Test Case
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void testMaxUsersExceededExcepton() {
    	User user = this.getNewFirmLevelUser();
    	Long accessId = 36L;
    	String nodeName = "RECORDSMANAGEMENT";
    	String requestURL = "http://nv-rd1.amcad.com/suffolkailistest/";
    	String adminUserName = "apatel@amcad.com";
		try {
			//add user for the first time
			this.userService.addFirmLevelUser(user, adminUserName, accessId, nodeName, requestURL);
			
			// Change user name and try adding again. It should throw Max Users Exceeded error
			user.setUsername("abc@test.com");
			this.userService.addFirmLevelUser(user, adminUserName, accessId, nodeName, requestURL);
		} catch (SDLBusinessException e) {
			Assert.fail(e.getMessage());
		} catch (MaxUsersExceededException e) {
			// It has the right flow, do nothing
			System.out.println("testMaxUsersExceededExcepton SUCCESS");
		} catch (UserNameNotFoundException  e) {
			// It has the right flow, do nothing
			Assert.fail(e.getMessage());
		} 
		// No need to delete a user because added user will be rolled back after exception is thrown
    }
    
    /**
     * Add Firm Level User Validation Test Case
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void testSubscriptionNotPaidError() {
    	User user = this.getNewFirmLevelUser();
    	String nodeName = "RECORDSMANAGEMENT";
    	String requestURL = "http://nv-rd1.amcad.com/suffolkailistest/";
    	String adminUserName = "apatel@amcad.com";
		try {
			// Try adding the access to which administrator is subscribed
	    	Long accessId = 42L;
			
			this.userService.addFirmLevelUser(user, adminUserName, accessId, nodeName, requestURL);
		} catch (SDLBusinessException e) {
			System.out.println("testSubscriptionNotPaidExcepton SUCCESS");
		} catch (MaxUsersExceededException e) {
			Assert.fail(e.getMessage());
			
		} catch (UserNameNotFoundException e) {
			Assert.fail(e.getMessage());
			
		} 
		// No need to delete a user because added user will be rolled back after exception is thrown
    }
    
    /**
     * Add Firm Level User Validation Test Case
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void testUserNotAdministratorError() {
    	User user = this.getNewFirmLevelUser();
    	Long accessId = 11L;
    	String nodeName = "RECORDSMANAGEMENT";
    	String requestURL = "http://nv-rd1.amcad.com/suffolkailistest/";
    	String adminUserName = "vivek4348@gmail.com";
		try {
			
			//add user for the first time
			this.userService.addFirmLevelUser(user, adminUserName, accessId, nodeName, requestURL);
		} catch (SDLBusinessException e) {
			// It has the right flow, do nothing
			System.out.println("testUserNotAdministratorExcepton SUCCESS");
		} catch (MaxUsersExceededException e) {
			Assert.fail(e.getMessage());
		} catch(UserNameNotFoundException e){
			Assert.fail(e.getMessage());
		}
    }
    
    /**
     * Add Firm Level User Validation Test Case
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void testUserNameNotFoundExcepton() {
    	User user = this.getNewFirmLevelUser();
    	Long accessId = 11L;
    	String nodeName = "RECORDSMANAGEMENT";
    	String requestURL = "http://nv-rd1.amcad.com/suffolkailistest/";
    	String adminUserName = "abc@gmail.com";
		try {
			
			//add user for the first time
			this.userService.addFirmLevelUser(user, adminUserName, accessId, nodeName, requestURL);
		} catch (SDLBusinessException e) {
			Assert.fail(e.getMessage());
		} catch (MaxUsersExceededException e) {
			Assert.fail(e.getMessage());
		} catch(UserNameNotFoundException e){
			// It has the right flow, do nothing
			System.out.println("testUserNotAdministratorExcepton SUCCESS");
		}
    }
    
    /**
     * Add Firm Level User Validation Test Case
     */
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void testUserAlreadyHasSubscriptionError() {
    	User user = this.getNewFirmLevelUser();
    	Long accessId = 36L;
    	String nodeName = "RECORDSMANAGEMENT";
    	String requestURL = "http://nv-rd1.amcad.com/suffolkailistest/";
    	String adminUserName = "apatel@amcad.com";
		try {
			
			//add user for the first time
			this.userService.addFirmLevelUser(user, adminUserName, accessId, nodeName, requestURL);
			
			// User has been added, Try to add the same user with same subscription again.
			// It should throw SDLBusinessException
			this.userService.addFirmLevelUser(user, adminUserName, accessId, nodeName, requestURL);
		} catch (SDLBusinessException e) {
			// It has the right flow, do nothing
			System.out.println("testUserNotAdministratorExcepton SUCCESS");
		} catch (MaxUsersExceededException e) {
			Assert.fail(e.getMessage());
		} catch(UserNameNotFoundException e){
			Assert.fail(e.getMessage());
		}
		// No need to delete a user , it will be rolledback with the exception thrown
    }
    
   
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void testDeleteFirmLevelUserWithPaidDocuments()  {
    	User user = this.getNewFirmLevelUser();
    	// Collin County , 6-10 users subscription
    	Long accessId = 88L;
    	Long siteId = 11L;
    	String nodeName = "RECORDSMANAGEMENT";
    	String requestURL = "http://nv-rd1.amcad.com/suffolkailistest/";
    	String adminUserName = "apatel@amcad.com";
		try {
			// Add the user first (Not a firm level add user) 
			this.userService.registerUser(user, siteId, accessId, nodeName,  requestURL);
			
			//pay for the subscription.
			CreditCard creditCard = this.getCreditCard();
			this.subService.payRecurSub(creditCard, user.getUsername(), nodeName, "JUNIT_TEST");
			
			//Now Let's make sure that user has the subscription
			List<SubscriptionDTO> subs = this.subService.getUserSubs(user.getUsername(), nodeName,null, false, false);
			
			assertEquals(1, subs.size());
			assertEquals(accessId, subs.get(0).getAccessId());
			assertEquals(siteId, subs.get(0).getSiteId());
			
			// So far so good, now let's add this user to the firm with Dallas County Subscriptions
			long firmAccessId = 36L;
			// Following call should add the firm access to the user as it already exists
			this.userService.addFirmLevelUser(user, adminUserName, firmAccessId, nodeName, requestURL);
			
			// Now let's verify that user has one admin subscription
			subs = this.subService.getUserSubs(user.getUsername(), nodeName,null, false, false);
			assertEquals(2, subs.size());
			
			SubscriptionDTO sub = this.getSubscription(subs, firmAccessId);
			// Now delete the firm level user, it should remove the access from the firm. (Not a physical delete)
			this.subService.removeFirmLevelUserAccess(user.getUsername(), sub.getUserAccessId(), "JUNIT_TEST", "JUNIT_TEST", false);
			
			// Let's get the subscriptions again and make sure that there is no admin subscription
			subs = this.subService.getUserSubs(user.getUsername(), nodeName,null, false, false);
			assertEquals(1, subs.size());
		} catch (SDLBusinessException e) {
			Assert.fail(e.getMessage());
		} catch (MaxUsersExceededException e) {
			Assert.fail(e.getMessage());
		} catch (UserNameNotFoundException e) {
			Assert.fail(e.getMessage());
		} catch (UserNameAlreadyExistsException e) {
			Assert.fail(e.getMessage());
		} catch (AccessUnAuthorizedException e) {
			Assert.fail(e.getMessage());
		}  

		/*
		 * We have craeted an user with paid transaction.
		 * Currently there is no way of deleting the paid transaction.
		 * 
		 * We want to delete the user & transaction so that we can re-run the test again.
		 * Let's throw an exception so that it rolls back the transaction.
		 */
		// Throw an exception to rollback the transaction.
		try {
			throw new Exception("Rolling the transaction back...");
		} catch (Exception e) {
			// We are at the right place , do nothing.
		}
    } 
    
    
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void testDeleteUserUserNameDoesNotExistsException() {
    	
    	String adminUserName = "apatel@amcad.com";
		try {
			// Now Delete the user.
			this.subService.removeFirmLevelUserAccess("abc@test.com", 12L, "JUNIT_TEST", "JUNIT_TEST", false);
			
		} catch(SDLBusinessException e){
			Assert.fail(e.getMessage());
		} catch(UserNameNotFoundException e){
			// Expected to get here
		} 
   }
    
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void testDeleteUserException() {
    	
    	// Add the user first.
    	User user = this.getNewFirmLevelUser();
    	user.setUsername("vivek4348@gmail.com");
    	String adminUserName = "apatel@amcad.com";
		try {
			// Now Delete the user.
			this.subService.removeFirmLevelUserAccess(user.getUsername(), 11L, "JUNIT_TEST", "JUNIT_TEST", false);
		}	
		catch(SDLBusinessException e){
			// As expected, it should be here
		} catch(UserNameNotFoundException e){
			Assert.fail(e.getMessage());
		} 
   }
    
    
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void testLockUnlockFirmLevelUser() {
    	String nodeName = "RECORDSMANAGEMENT";
    	Long accessId = 36L;

		try {
			// Lock the user first
			this.subService.enableDisableFirmLevelUserAccess("arvindbpatel@yahoo.com", accessId,
					true, "JNUIT_TEST",  "TEST");
			
			// Now get subscriptions and check if it really worked !
			List<SubscriptionDTO> subscriptions = this.subService.getUserSubs("arvindbaptel@yahoo.com", nodeName, null, false, true);
			for(SubscriptionDTO subscription : subscriptions){
				if(subscription.getAccessId().equals(accessId)){
					if(subscription.isActive()){
						Assert.fail("User Access is still active !");
					}
				}
			}
			
			// Let's unlock the access and validate if it worked.
			this.subService.enableDisableFirmLevelUserAccess("arvindbpatel@yahoo.com", accessId,
					false, "JNUIT_TEST", "TEST");
			
			// Now get subscriptions and check if it really worked !
			subscriptions = this.subService.getUserSubs("arvindbaptel@yahoo.com", nodeName, null, false, true);
			for(SubscriptionDTO subscription : subscriptions){
				if(subscription.getAccessId().equals(accessId)){
					if(!subscription.isActive()){
						Assert.fail("User Access is still Inactive !");
					}
				}
			}
			
		}	
		catch(SDLBusinessException e){
			// As expected, it should be here
		} catch(UserNameNotFoundException e){
			Assert.fail(e.getMessage());
		} 
   }
    
   private CreditCard getCreditCard(){
	   CreditCard card = new CreditCard();
	   card.setNumber("4308998314962545");
	   card.setExpiryMonth(1);
	   card.setExpiryYear(2018);
	   card.setName("MEMBER");
	   card.setAddressLine1("1 Main St");
	   card.setCity("Herndon");
	   card.setState("VA");
	   card.setCardType(CardType.VISA);
	   card.setZip("12345");
	   card.setPhone(1234567890L);
	   return card;
	   
   }
   
   private SubscriptionDTO getSubscription(List<SubscriptionDTO> subscriptions, Long accessId){
	   for (SubscriptionDTO subscription : subscriptions){
		   if(accessId == subscription.getAccessId()){
			   return subscription;
		   }
	   }
	   return null;
   }
    
    
}