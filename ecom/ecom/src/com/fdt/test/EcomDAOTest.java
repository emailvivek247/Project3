package com.fdt.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fdt.ecom.dao.EComDAO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:C:\\Projects\\SDL\\2.9\\Enterprise\\ecom\\ecom\\WebContent\\WEB-INF\\conf\\spring\\applicationContext.xml"})
public class EcomDAOTest {

    @Autowired
    private EComDAO eComDAO;

    public EcomDAOTest() {
        System.setProperty("CONFIG_LOCATION", "file:C:\\Projects\\SDL\\2.9\\Enterprise\\ecom\\ecom\\WebContent\\WEB-INF\\conf");
    }


    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void testGetSiteDetails() {
    	eComDAO.getSiteDetails(4L);
    	// Did not throw an exception : Good, it works !
    }


    @Test
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void testGetSitesForNode() {
    	eComDAO.getSitesForNode("RECORDSMANAGEMENT");
    	// Did not throw an exception : Good, it works !
    }
}