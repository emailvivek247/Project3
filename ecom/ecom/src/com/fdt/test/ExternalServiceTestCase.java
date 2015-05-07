package com.fdt.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.ecom.service.EComFacadeService;
import com.fdt.ecom.service.ExternalService;
import com.fdt.ecom.service.rs.EComAdminFacadeServiceRS;
import com.fdt.webtx.dto.WebCaptureTxRequestDTO;
import com.fdt.webtx.dto.WebCaptureTxResponseDTO;
import com.fdt.webtx.service.WebTxService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:C:\\Projects\\AMCAD\\Development\\ecom\\ecom\\WebContent\\WEB-INF\\conf\\spring\\applicationContext.xml" })
public class ExternalServiceTestCase {

	@Autowired
	@Qualifier("externalService")
	private ExternalService externalService = null;

	@Autowired
	@Qualifier("eComFacadeService")
	private EComFacadeService eComFacadeService = null;
	
	@Autowired
	@Qualifier("webTxService")
	private WebTxService webTxService = null;

	@Autowired
	@Qualifier("eComAdminFacadeServiceRS")
	private EComAdminFacadeServiceRS eComAdminFacadeServiceRS = null;

	public ExternalServiceTestCase() {
		System.setProperty("CONFIG_LOCATION",
				"file:C:\\Projects\\AMCAD\\Development\\ecom\\ecom\\src\\com\\fdt\\test\\conf");
	}

	@Test
	public void lookupTx() {
		/*String txRefNumber = null;
		String cardName = "vivek";
		String cardNumber = null;
		String txStartDate = null;
		String txEndDate = null;
		String siteName = "WARREN";
		String invoiceId = "180104";
		WebTx webTx1 = this.externalService.getWebTxByInvoiceNumber(invoiceId, siteName);
		System.out.println(webTx1.getAddressLine1());
		System.out.println(webTx1.getAddressLine2());
		System.out.println(webTx1.getCity());
		System.out.println(webTx1.getState());
		System.out.println(webTx1.getZip());

		WebTx webTx2 = this.externalService.getWebTxByTxRefNum("B70P5EFD4FFB", siteName);
		System.out.println(webTx2);
		System.out.println(webTx2.getAddressLine1());
		System.out.println(webTx2.getAddressLine2());
		System.out.println(webTx2.getCity());
		System.out.println(webTx2.getState());
		System.out.println(webTx2.getZip());


		Date endDate = new Date();

		Calendar cal = Calendar.getInstance();
		cal.setTime(endDate);
		cal.add(Calendar.HOUR, -1);
		Date fromDate = cal.getTime();

		System.out.println(siteName);
		System.out.println(fromDate);
		System.out.println(endDate);
		WebTxExtResponseDTO webTx3 = this.externalService.getWebTransactionsForExtApp(siteName, fromDate, endDate, "CHARGE");
		for(WebTx webTx: webTx3.getWebTransactionList()) {
			System.out.println(webTx);
		}*/


		/*OTCTx otcTx = this.externalService.getOTCTransactionByInvoiceNumber("12345", "MOBILE");
		System.out.println(otcTx);


		OTCTx otcTx2 = this.externalService.getOTCTransactionByTxRefNum("B71P5EFF1E13", "MOBILE");
		System.out.println(otcTx2);*/
		/*Date startDate = new Date();
		try {
			startDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2010-05-12 00:00:00");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Format formatter = new SimpleDateFormat("MM/dd/yyyy");
		List<CheckHistory> checHistoryList = this.eComAdminFacadeServiceRS.getCheckHistories(null,
				formatter.format(startDate), formatter.format(new Date()), null, null);
		System.out.println(checHistoryList.size());*/

		WebCaptureTxRequestDTO webCaptureTxRequestDTO = new WebCaptureTxRequestDTO();
		try {
			webCaptureTxRequestDTO.setAuthorizationTxReferenceNumber("B10P7CA06AC3");
			webCaptureTxRequestDTO.setCaptureTxAmount(50.0d);
			webCaptureTxRequestDTO.setComments("Testing Partial Capture 2");
			webCaptureTxRequestDTO.setModifiedBy("Vivekanand");
			webCaptureTxRequestDTO.setSiteName("WARREN");
			WebCaptureTxResponseDTO webCaptureTxResponseDTO = this.externalService.captureWebTx(webCaptureTxRequestDTO);
			System.out.println(webCaptureTxResponseDTO);
		} catch (SDLBusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}