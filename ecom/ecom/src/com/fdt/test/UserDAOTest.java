package com.fdt.test;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fdt.security.dao.UserDAO;
import com.fdt.security.entity.User;
import com.fdt.security.entity.UserAccess;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:C:\\Projects\\SDL\\2.9\\Enterprise\\ecom\\ecom\\WebContent\\WEB-INF\\conf\\spring\\applicationContext.xml"})
public class UserDAOTest {

    @Autowired
    private UserDAO userDAO;

    public UserDAOTest() {
        System.setProperty("CONFIG_LOCATION", "file:C:\\Projects\\SDL\\2.9\\Enterprise\\ecom\\ecom\\WebContent\\WEB-INF\\conf");
    }


    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void testGetUserAccessForFirmLevelUsers() {
    	Long adminUserId = 150L; // user name 'apatel@amcad.com'
    	Long accessId = 36L; // access for Dallas 3-6 users
    	List<UserAccess>  list = userDAO.getUserAccessForFirmLevelUsers(adminUserId, accessId);
    	Assert.notEmpty(list);
    }
    
    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void testGetUserDetails() {
    	String userName = "apatel@amcad.com";
    	User user = userDAO.getUser(userName);
    	Assert.notNull(user);
    }
}