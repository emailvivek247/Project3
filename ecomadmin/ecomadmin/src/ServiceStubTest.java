import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fdt.common.util.rest.ServiceStubRS;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:C:\\Projects\\SDL\\2.9\\Enterprise\\deployment\\ecomadmin\\ecomadmin\\WebContent\\WEB-INF\\conf\\spring\\RSServices.xml"})
public class ServiceStubTest {

	@Autowired
	ServiceStubRS serviceStub = null;

	/*@Test
	public void getUserCountsBySite() throws Exception {
		List<UserCountDTO> userCounts = serviceStub.getUserCountsBySite(3L);
		for (UserCountDTO userCount : userCounts) {
			System.out.println(userCount);
		}
	}

	@Test
	public void getUserCountsBySubscription() throws Exception {
		List<UserCountDTO> userCounts = serviceStub.getUserCountsBySubscription(3L);
		for (UserCountDTO userCount : userCounts) {
			System.out.println(userCount);
		}
	}



	@Test
	public void getUserDistributionBySubscription() throws Exception {
		List<UserCountDTO> userCounts = serviceStub.getUserDistributionBySubscription(3L, 10L);
		for (UserCountDTO userCount : userCounts) {
			System.out.println(userCount);
		}
	}


	@Test
	public void getCacheNames() throws Exception {
		List<String> cacheList = serviceStub.getCacheNames();
		for (String cache : cacheList) {
			System.out.println(cache);
		}
	}

	@Test
	public void getAccessesForSite() throws Exception {
		List<Access>  userCounts = serviceStub.getAccessesForSite("3");
		for (Access userCount : userCounts) {
			System.out.println(userCount);
		}
	}
	*/


	/*@Test
	public void getCheckHistories() throws Exception {
		List<CheckHistory> userCounts = serviceStub.getCheckHistories(null, "02/01/2013", "02/25/2013");
		for (CheckHistory userCount : userCounts) {
			System.out.println(userCount);
		}
	}

	@Test
	public void getCheckHistory() throws Exception {
		CheckHistory userCount = serviceStub.getCheckHistory(20005L);
			System.out.println(userCount);
	}

	@Test
	public void getCreditCardDetails() throws Exception {
		CreditCard CreditCard = serviceStub.getCreditCardDetails("mbeall@balzer.cc");
			System.out.println(CreditCard);
	}

	@Test
	public void getErrorLog() throws Exception {
		List<ErrorCode> userCounts = serviceStub.getErrorLog("02/01/2013", "02/12/2013", null);
		for (ErrorCode userCount : userCounts) {
			System.out.println(userCount);
		}
	}

	@Test
	public void getErrorLogByUserName() throws Exception {
		List<ErrorCode> userCounts = serviceStub.getErrorLog(null, null, "smani");
		for (ErrorCode userCount : userCounts) {
			System.out.println(userCount);
		}
	}

	@Test
	public void getOTCTransactionByTxRefNum() throws Exception {
		OTCTransaction oTCTransaction = serviceStub.getOTCTransactionByTxRefNum("E79P3E124C2C", "DALLAS");
			System.out.println(oTCTransaction);
	}*/



	/*@Test
	public void getReceiptConfigurationForSite() throws Exception {
		System.out.println("getReceiptConfigurationForSite");
		List<ReceiptConfiguration>  receiptConfigurationList = serviceStub.getReceiptConfigurationForSite(3L);
		for (ReceiptConfiguration receiptConfiguration : receiptConfigurationList) {
			System.out.println(receiptConfiguration);
		}
	}

	@Test
	public void getRecurringTransactionByTxRefNum() throws Exception {
		System.out.println("getRecurringTransactionByTxRefNum");
		List<RecurTransaction> recurTransactionList = serviceStub.getRecurringTransactionByTxRefNum("ERCP7A4F9652", "SUFFOLK");
		for (RecurTransaction recurTransaction : recurTransactionList) {
			System.out.println(recurTransaction);
		}
	}

	@Test
	public void getRecurTransactions() throws Exception {
		System.out.println("getRecurTransactions");
		List<RecurTransaction> recurTransactionList = serviceStub.getRecurTransactions("hpurkey@hrpjrpc.com", true);
		for (RecurTransaction recurTransaction : recurTransactionList) {
			System.out.println(recurTransaction);
		}
	}


	@Test
	public void getRecurTransactionsBySite() throws Exception {
		System.out.println("getRecurTransactionsBySite");
		List<RecurTransaction> recurTransactionList = serviceStub.getRecurTransactionsBySite("hpurkey@hrpjrpc.com", 1L, true);
		for (RecurTransaction recurTransaction : recurTransactionList) {
			System.out.println(recurTransaction);
		}
	}*/

	/*@Test
	public void getReferencedOTCTransaction() throws Exception {
		OTCTransaction oTCTransaction = serviceStub.getReferencedOTCTransaction("", "");
			System.out.println(oTCTransaction);
	}*/

/*
	@Test
	public void getReferencedRecurringTransactionByTxRefNum() throws Exception {
		System.out.println("getReferencedRecurringTransactionByTxRefNum");
		RecurTransaction recurTransaction = serviceStub.getReferencedRecurringTransactionByTxRefNum("ETHPA0BC4EDB", "DALLAS");
		System.out.println(recurTransaction);
	}

	@Test
	public void getReferencedWebTransaction() throws Exception {
		System.out.println("getReferencedWebTransaction");
		List<WebTransaction> recurTransactionList = serviceStub.getReferencedWebTransaction("ESJP9F127B01", "DALLAS");
		for (WebTransaction recurTransaction : recurTransactionList) {
			System.out.println(recurTransaction);
		}
	}


	@Test
	public void getReferencedWebTransactionItemByItemId() throws Exception {
		System.out.println("getReferencedWebTransactionItemByItemId");
		WebTransaction oTCTransaction = serviceStub.getReferencedWebTransactionItemByItemId(1L,"DALLAS");
			System.out.println(oTCTransaction);
	}

	@Test
	public void getSiteConfiguration() throws Exception {
		System.out.println("getSiteConfiguration");
		SiteConfiguration receiptConfiguration = serviceStub.getSiteConfiguration(3L);
		System.out.println(receiptConfiguration);
	}

	@Test
	public void getSites() throws Exception {
		System.out.println("getSites");
		List<Site> recurTransactionList = serviceStub.getSites();
		for (Site recurTransaction : recurTransactionList) {
			System.out.println(recurTransaction);
		}
	}*/

	/*@Test
	public void getSiteSummaryInfo() throws Exception {
		System.out.println("getSiteSummaryInfo");
		Site site = serviceStub.getSiteSummaryInfo(3L);
		System.out.println(site);
	}

	@Test
	public void getUserAccessDetails() throws Exception {
		System.out.println("getUserAccessDetails");
		UserAccessDetailDTO userAccessDetailDTO = serviceStub.getUserAccessDetails(18L);
			System.out.println(userAccessDetailDTO);
	}

	@Test
	public void getUserDetailsForAdmin() throws Exception {
		System.out.println("getUserDetailsForAdmin");
		User user = serviceStub.getUserDetailsForAdmin("smani@amcad.com");
		System.out.println(user);
	}



	@Test
	public void getUserSubscriptions() throws Exception {
		System.out.println("getUserSubscriptions");
		List<SubscriptionDTO> subscriptionDTOList = serviceStub.getUserSubscriptions("dball@amcad.com", "RECORDSMANAGEMENT", "VABEACH");
		for (SubscriptionDTO subscriptionDTO : subscriptionDTOList) {
			System.out.println(subscriptionDTO);
		}
	}
	*/

/*	@Test
	public void getWebTransactionByTxRefNum() throws Exception {
		System.out.println("getWebTransactionByTxRefNum");
		WebTransaction oTCTransaction = serviceStub.getWebTransactionByTxRefNum("ESJP9F127B01", "DALLAS");
			System.out.println(oTCTransaction);
	}

	@Test
	public void getWebTransactionItemByItemId() throws Exception {
		System.out.println("getWebTransactionItemByItemId");
		WebTransaction  webTransaction = serviceStub.getWebTransactionItemByItemId(1L, "DALLAS");
		System.out.println(webTransaction);
	}

	@Test
	public void getWebTransactions() throws Exception {
		System.out.println("getWebTransactions");
		List<WebTransaction> recurTransactionList = serviceStub.getWebTransactions("dball@amcad.com");
		for (WebTransaction recurTransaction : recurTransactionList) {
			System.out.println(recurTransaction);
		}
	}

	@Test
	public void getWebTransactionsBySite() throws Exception {
		System.out.println("getWebTransactionsBySite");
		List<WebTransaction> recurTransactionList = serviceStub.getWebTransactionsBySite("dball@amcad.com", 3L);
		for (WebTransaction recurTransaction : recurTransactionList) {
			System.out.println(recurTransaction);
		}
	}
	*/

	/*@Test
	public void getWebTransactionsBySite() throws Exception {
		System.out.println("getWebTransactionsBySite");
		List<WebTransaction> recurTransactionList = serviceStub.getWebTransactionsBySite("dball@amcad.com", 3L);
		for (WebTransaction recurTransaction : recurTransactionList) {
			System.out.println(recurTransaction);
		}
	}*/

	/*@Test
	public void cancelSubscription() throws Exception {
		System.out.println("cancelSubscription");
		PayPalDTO payPalDTO = serviceStub.cancelSubscription("vpratti@amcad.com", 48543L);
		System.out.println(payPalDTO);
	}
	*/

	/*@Test
	public void addSubscription() throws Exception {

		System.out.println("addSubscription");
		User user = serviceStub.getUsersByAccessId(7L).get(0);
		System.out.println(user);
		List<Long> newAccessIdList = new LinkedList<Long>();
		newAccessIdList.add(113L);
		SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
		subscriptionDTO.setNodeName("RECORDSMANAGEMENT");
		subscriptionDTO.setNewAccessIds(newAccessIdList);
		subscriptionDTO.setUser(user);
		List<AccessDetailDTO> recurTransactionList = serviceStub.addSubscription(subscriptionDTO);
		for (AccessDetailDTO recurTransaction : recurTransactionList) {
			System.out.println(recurTransaction);
		}
	}*/

	/*@Test
	public void archiveUser() throws Exception {
		System.out.println("archiveUser");
		String userName = "";
		String comments = "";
		String modifiedBy = "";
		String machineName = "";
		serviceStub.archiveUser(userName, comments, modifiedBy, machineName);
	}

	@Test
	public void authorize() throws Exception {
		System.out.println("authorize");
		Long userAccessId = 10L;
		boolean isAuthorized = false;
		String modifiedBy = "Test-Client";
		serviceStub.authorize(userAccessId, isAuthorized, modifiedBy);
	}

	@Test
	public void calculateAccountBalance() throws Exception {
		System.out.println("calculateAccountBalance");
		Long siteId = 10L;
		PaymentType paymentType = PaymentType.OTC;
		String createdBy = "v";
		String machineName = "a";
		Double recurTransactionList = serviceStub.calculateAccountBalance(siteId, paymentType, createdBy, machineName );
		System.out.println(recurTransactionList);
	}*/


	/*@Test
	public void addSubscription() throws Exception {
		System.out.println("addSubscription");
		User user = serviceStub.getUsersByAccessId(7L).get(0);
		System.out.println(user);
		List<Long> newAccessIdList = new LinkedList<Long>();
		newAccessIdList.add(113L);
		List<AccessDetailDTO> recurTransactionList = serviceStub.addSubscription(user, newAccessIdList, "RECORDSMANGEMENT");
		for (AccessDetailDTO recurTransaction : recurTransactionList) {
			System.out.println(recurTransaction);
		}
	}*/

	/*@Test
	public void refreshCache() throws Exception {
		System.out.println("refreshCache");
		serviceStub.refreshCache();
	}

	@Test
	public void saveReceiptConfiguration() throws Exception {
		System.out.println("saveReceiptConfiguration");
		List<ReceiptConfiguration>  receiptConfigurationList = serviceStub.getReceiptConfigurationForSite(3L);
		ReceiptConfiguration receiptConfiguration = receiptConfigurationList.get(0);
		System.out.println(receiptConfiguration);
		receiptConfiguration.setPhone("6184025145");
		serviceStub.saveReceiptConfiguration(receiptConfiguration);
		List<ReceiptConfiguration>  receiptConfigurationList2 = serviceStub.getReceiptConfigurationForSite(3L);
		ReceiptConfiguration receiptConfiguration2 = receiptConfigurationList2.get(0);
		System.out.println(receiptConfiguration2);
	}*/

	/*@Test
	public void updateExistingCreditCardInformation() {
		System.out.println("updateExistingCreditCardInformation");
		CreditCard newCreditCardInformation = serviceStub.getCreditCardDetails("vpratti@amcad.com");
		System.out.println(newCreditCardInformation);
		newCreditCardInformation.setAddressLine1("2628");
		try {
			serviceStub.updateExistingCreditCardInformation("vpratti@amcad.com", newCreditCardInformation);
		} catch (PaymentGatewayUserException e) {
			System.out.println(e);
		} catch (PaymentGatewaySystemException e) {
			System.out.println(e);
		} catch (SDLException e) {
			System.out.println(e);
		}
		System.out.println(newCreditCardInformation);
	}*/

	/*@Test
	public void updateNodeConfiguration() throws Exception {
		System.out.println("updateNodeConfiguration");
		NodeConfiguration nodeConfiguration = serviceStub.getNodeConfiguration("RECORDSMANAGEMENT");
		System.out.println(nodeConfiguration);
		nodeConfiguration.setNodeDescription("RECORDS MGMT");
		serviceStub.updateNodeConfiguration(nodeConfiguration);
		System.out.println(nodeConfiguration);
	}

	@Test
	public void updateSiteConfiguration() throws Exception {
		System.out.println("updateSiteConfiguration");
		SiteConfiguration siteConfiguration = serviceStub.getSiteConfiguration(3L);
		System.out.println(siteConfiguration);
		siteConfiguration.setFromEmailAddress("vivek4348@gmail.com");
		serviceStub.updateSiteConfiguration(siteConfiguration);
		System.out.println(siteConfiguration);
	}*/

	/*@Test
	public void refreshCacheByName() throws Exception {
		System.out.println("refreshCacheByName");
		List<String> cacheList = serviceStub.getCacheNames();
		for (String cache : cacheList) {
			System.out.println("Cache Refreshing.." + cache);
			serviceStub.refreshCacheByName(cache);
			System.out.println("Cache Refreshed...");
		}

	}*/

	/*@Test
	public void doACHTransfer() throws Exception {
		System.out.println("doACHTransfer");
		ReceiptConfiguration receiptConfiguration = new ReceiptConfiguration();
		serviceStub.saveReceiptConfiguration(receiptConfiguration);
	}

	@Test
	public void doPartialReferenceCreditWeb() throws Exception {
		System.out.println("doPartialReferenceCreditWeb");
		CreditCard newCreditCardInformation = new CreditCard();
		serviceStub.updateExistingCreditCardInformation("", newCreditCardInformation);
	}*/

	/*@Test
	public void doReferenceCreditOTC() throws Exception {
		System.out.println("updateNodeConfiguration");
		NodeConfiguration nodeConfiguration = new NodeConfiguration();
		serviceStub.updateNodeConfiguration(nodeConfiguration);
	}

	@Test
	public void removeSubscription() throws Exception {
		System.out.println("updateSiteConfiguration");
		SiteConfiguration siteConfiguration = new SiteConfiguration();
		serviceStub.updateSiteConfiguration(siteConfiguration);
	}

	@Test
	public void doReferenceCreditRecurringTx() throws Exception {
		System.out.println("saveReceiptConfiguration");
		ReceiptConfiguration receiptConfiguration = new ReceiptConfiguration();
		serviceStub.saveReceiptConfiguration(receiptConfiguration);
	}

	@Test
	public void doReferenceCreditWeb() throws Exception {
		System.out.println("updateExistingCreditCardInformation");
		CreditCard newCreditCardInformation = new CreditCard();
		serviceStub.updateExistingCreditCardInformation("", newCreditCardInformation);
	}

	@Test
	public void doVoidCheck() throws Exception {
		System.out.println("updateNodeConfiguration");
		NodeConfiguration nodeConfiguration = new NodeConfiguration();
		serviceStub.updateNodeConfiguration(nodeConfiguration);
	}

	@Test
	public void enableDisableUserAccess() throws Exception {
		System.out.println("updateSiteConfiguration");
		SiteConfiguration siteConfiguration = new SiteConfiguration();
		serviceStub.updateSiteConfiguration(siteConfiguration);
	}



	@Test
	public void lockUnLockUser() throws Exception {
		System.out.println("updateSiteConfiguration");
		SiteConfiguration siteConfiguration = new SiteConfiguration();
		serviceStub.updateSiteConfiguration(siteConfiguration);
	}*/
	/*@Test
	public void getUsersByAccessId() throws Exception {
		System.out.println("getUsersByAccessId");
		List<User> userList = serviceStub.getUsersByAccessId(7L);
		for (User user : userList) {
			System.out.println(user);
		}
	}*/

	/*@Test
	public void getSitesForNode() throws Exception {
		System.out.println("getSitesForNode");
		List<User> userList = serviceStub.getSitesForNode("RECORDSMANAGEMENT");
		for (User user : userList) {
			System.out.println(user);
		}
	}*/


	/*@Test
	public void lookupTransaction() throws Exception {
		String txRefNumber = "E19P3F842BBB";

		List<OTCTransaction>  otcTransactions = serviceStub.lookupTransaction(txRefNumber, null, null, null, "WEB", null);
		for(OTCTransaction otcTransaction: otcTransactions) {
			System.out.println(otcTransaction);
		}
	}*/
}

