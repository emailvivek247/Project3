package com.fdt.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fdt.recurtx.dao.RecurTxDAO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:C:\\Projects\\SDL\\2.9\\Enterprise\\ecom\\ecom\\WebContent\\WEB-INF\\conf\\spring\\applicationContext.xml"})
public class RecurringDAOTest {

    @Autowired
    private RecurTxDAO recurSubDAO;

    public RecurringDAOTest() {
        System.setProperty("CONFIG_LOCATION", "file:C:\\Projects\\SDL\\2.9\\Enterprise\\ecom\\ecom\\WebContent\\WEB-INF\\conf");
    }


    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void testGetRecurringProfilesForVerification() {
    	recurSubDAO.getRecurringProfilesForVerification();
    	// Did not throw an exception : Good, it works !
    }
}