package com.fdt.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fdt.ecom.service.EComFacadeService;
import com.fdt.ecom.service.ExternalService;
import com.fdt.security.entity.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:c:\\Projects\\ecom\\ecom\\WebContent\\WEB-INF\\conf\\spring\\applicationContext.xml"})
public class ACHTestCase {

    @Autowired
    private EComFacadeService eComFacadeService = null;

    @Autowired
    private ExternalService oTCFacadeService = null;

    public ACHTestCase() {
        System.setProperty("CONFIG_LOCATION", "file:C:\\Projects\\ecom\\ecom\\WebContent\\WEB-INF\\conf");
    }

    @Test
    public void loadByUserName() {
        User user = this.eComFacadeService.loadUserByUsername("admin@roam.com", "RECORDSMANAGEMENT");
        System.out.println("User Details" + user);
    }

}
