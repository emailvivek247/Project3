package com.fdt.test;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import com.fdt.ecom.dto.UserCountDTO;
import com.fdt.ecom.entity.Term;
import com.fdt.ecom.service.rs.EComAdminFacadeServiceRS;
import com.fdt.ecom.service.rs.EComFacadeServiceRS;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:C:\\Projects\\ecom\\ecom\\WebContent\\WEB-INF\\conf\\spring\\applicationContext.xml"})
public class EcomServiceTestCase {

    @Autowired
    @Qualifier("eComAdminFacadeServiceRS")
    private EComAdminFacadeServiceRS eComAdminFacadeService = null;

    @Autowired
    @Qualifier("eComFacadeServiceRS")
    private EComFacadeServiceRS eComFacadeService = null;

    public EcomServiceTestCase() {
        System.setProperty("CONFIG_LOCATION", "file:C:\\Projects\\ecom\\ecom\\WebContent\\WEB-INF\\conf");
    }

    @Test
    public void getUserCountsForAllSite() {
    	List<UserCountDTO> userCounts = this.eComAdminFacadeService.getUserCountsForAllSite();
    	for (UserCountDTO userCountDTO : userCounts) {
        	System.out.println("THE USER COUNT IS ===>" + userCountDTO);
    		Assert.notNull(userCountDTO.getDescription());
    	}
    }

    @Test
    public void getUserCountForSite() {
    	UserCountDTO userCount = this.eComAdminFacadeService.getUserCountForSite(4L);
    	System.out.println("THE USER COUNT IS ===>" + userCount);
   		Assert.notNull(userCount.getDescription());
    }

    @Test
    public void getUserCountsBySubForASite() {
    	List<UserCountDTO> userCounts = this.eComAdminFacadeService.getUserCountsBySubForASite(4L);
    	for (UserCountDTO userCountDTO : userCounts) {
        	System.out.println("THE USER COUNT IS ===>" + userCountDTO);
    		Assert.notNull(userCountDTO.getDescription());
    	}
    }

    @Test
    public void getUserDistributionBySubscription() {
    	List<UserCountDTO> userCounts = this.eComAdminFacadeService.getUserDistributionBySubscription(4L, 11L);
    	for (UserCountDTO userCountDTO : userCounts) {
        	System.out.println("THE USER COUNT getUserDistributionBySubscription IS ===>" + userCountDTO);
    		Assert.notNull(userCountDTO.getDescription());
    	}
    }

    @Test
    public void getTerm() {
    	Term term = this.eComFacadeService.getTerm("DALLAS");
       	System.out.println("Term & Conditions Are ===>" + term);
       	Assert.notNull(term.getId());
       	Assert.notNull(term.getDescription());
       	Assert.notNull(term.getCreatedBy());
       	Assert.notNull(term.getCreatedDate());
       	Assert.notNull(term.getModifiedDate());
       	Assert.notNull(term.getModifiedBy());
       	Assert.notNull(term.getDescription());
       	Assert.notNull(term.getTermType());
       	Assert.notNull(term.getSite().getId());
    }




//    @Test
//    public List<UserCountDTO> getUserCountsBySubscription(Long siteId) {
//        /** There is no Assert as the Site Id could be Null**/
//        return this.eComAdminFacadeService.getUserCountsBySubscription(siteId);
//    }
//
//    @Test
//    public List<UserCountDTO> getUserDistributionBySubscription(Long siteId, Long accessId) {
//        /** There is no Assert as the Site/AccessId could be Null**/
//        return this.eComAdminFacadeService.getUserDistributionBySubscription(siteId, accessId);
//    }
}