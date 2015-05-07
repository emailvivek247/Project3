package com.fdt.test.otc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fdt.ecom.service.EComFacadeService;
import com.fdt.ecom.service.ExternalService;
import com.fdt.ecom.service.rs.EComAdminFacadeServiceRS;
import com.fdt.webtx.entity.WebTx;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:C:\\Projects\\SDL\\2.9\\Enterprise\\development\\ecom\\ecom\\WebContent\\WEB-INF\\conf\\spring\\applicationContext.xml" })
public class OTCTestCase {

	@Autowired
	@Qualifier("externalService")
	private ExternalService externalService = null;

	@Autowired
	@Qualifier("eComFacadeService")
	private EComFacadeService eComFacadeService = null;

	@Autowired
	@Qualifier("eComAdminFacadeServiceRS")
	private EComAdminFacadeServiceRS eComAdminFacadeServiceRS = null;

	public OTCTestCase() {
		System.setProperty("CONFIG_LOCATION",
				"file:C:\\Projects\\SDL\\2.9\\Enterprise\\development\\ecom\\ecom\\src\\com\\fdt\\test\\conf");
	}

	@Test
	public void lookupTx() {
		String txRefNumber = null;
		String cardName = "vivek";
		String cardNumber = null;
		String txStartDate = null;
		String txEndDate = null;
		String siteName = "WARREN";
		String invoiceId = "Warren-1";
		WebTx webTx1 = this.externalService.getWebTxByInvoiceNumber(invoiceId, siteName);
		System.out.println(webTx1);

		WebTx webTx2 = this.externalService.getWebTxByTxRefNum("B70P5DB98D17", siteName);
		System.out.println(webTx2);


	}

}