package com.fdt.test;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.ecom.service.EComFacadeService;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.recurtx.entity.RecurTx;
import com.fdt.security.entity.User;
import com.fdt.subscriptions.dto.SubscriptionDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:C:\\Projects\\SDL\\2.9\\Enterprise\\development\\ecom\\ecom\\WebContent\\WEB-INF\\conf\\spring\\applicationContext.xml"})
public class RecurServiceTestCase {

    @Autowired
    @Qualifier("eComFacadeService")
    private EComFacadeService eComFacadeService = null;

    public RecurServiceTestCase() {
        System.setProperty("CONFIG_LOCATION", "file:C:\\Projects\\SDL\\2.9\\Enterprise\\development\\ecom\\ecom\\WebContent\\WEB-INF\\conf");
    }

    @Test
    public void getRecurTransactionsByNode() {
    	String nodeName = "RECORDSMANAGEMENT";
    	String userName = "mgeorgieva@amcad.com";
    	List<RecurTx> recurringTransactions = this.eComFacadeService.getRecurTransactionsByNode(userName, nodeName);
    	for(RecurTx recurTransaction : recurringTransactions) {
    		System.out.println(recurTransaction);
    	}
    }

    @Test
    public void getRecurTxDetail() {
    	String userName = "georgieva.mg@gmail.com";
    	String recurTxRefNum = "E78P3E52A65A";
    	List<RecurTx> recurringTransactions = this.eComFacadeService.getRecurTxDetail(userName, recurTxRefNum);
    	for(RecurTx recurTransaction : recurringTransactions) {
    		System.out.println(recurTransaction);
    	}
    }

    @Test
    public void paySubscriptions() {
    	String nodeName = "RECORDSMANAGEMENT";
    	String userName = "mgeorgieva@amcad.com";
    	List<RecurTx> recurringTransactions = this.eComFacadeService.getRecurTransactionsByNode(userName, nodeName);

    	for(RecurTx recurTransaction : recurringTransactions) {
    		System.out.println(recurTransaction);
    	}
    }

    @Test
    public void getPaidSubUnpaidByUser() {
    	String nodeName = "RECORDSMANAGEMENT";
    	String userName = "mgeorgieva@amcad.com";
    	User user = this.eComFacadeService.getPaidSubUnpaidByUser(userName, nodeName);
    	System.out.println(user);
    }

    @Test
    public void getUserSubscriptions() {
    	String userName = "mgeorgieva@amcad.com";
    	String nodeName = "RECORDSMANAGEMENT";
    	String siteName = "King George County";
    	List<SubscriptionDTO> subscriptionDTOList = this.eComFacadeService.getUserSubscriptions(userName, nodeName, siteName, false, false);
    	for(SubscriptionDTO subscriptionDTO : subscriptionDTOList) {
    		System.out.println(subscriptionDTO);
    	}
    }


    @Test
    public void cancelSubscription() {
    	String userName = "mgeorgieva@amcad.com";
    	Long userAccessId = 586L;
    	PayPalDTO payPalDTO = null;
		try {
			payPalDTO = this.eComFacadeService.cancelSubscription(userName, userAccessId);
		} catch (PaymentGatewaySystemException e) {
			System.out.println(e);
		} catch (SDLBusinessException e) {
			System.out.println(e);
		}
    	System.out.println(payPalDTO);
    }

    @Test
    public void getSubscriptionDetails() {
    	String userName = "mgeorgieva@amcad.com";
    	Long userAccessId = 89L;
    	SubscriptionDTO subscriptionDTO = this.eComFacadeService.getSubscriptionDetails(userName, userAccessId);
    	System.out.println(subscriptionDTO);
    }


    @Test
    public void getChangeSubscriptionInfo() {
    	String nodeName = "RECORDSMANAGEMENT";
    	String userName = "mgeorgieva@amcad.com";
    	List<RecurTx> recurringTransactions = this.eComFacadeService.getRecurTransactionsByNode(userName, nodeName);

    	for(RecurTx recurTransaction : recurringTransactions) {
    		System.out.println(recurTransaction);
    	}
    }

    @Test
    public void changeFromRecurringToRecurringSubscription() {
    	String nodeName = "RECORDSMANAGEMENT";
    	String userName = "mgeorgieva@amcad.com";
    	List<RecurTx> recurringTransactions = this.eComFacadeService.getRecurTransactionsByNode(userName, nodeName);

    	for(RecurTx recurTransaction : recurringTransactions) {
    		System.out.println(recurTransaction);
    	}
    }

    @Test
    public void addSubscription() {
    	String nodeName = "RECORDSMANAGEMENT";
    	String userName = "mgeorgieva@amcad.com";
    	List<RecurTx> recurringTransactions = this.eComFacadeService.getRecurTransactionsByNode(userName, nodeName);

    	for(RecurTx recurTransaction : recurringTransactions) {
    		System.out.println(recurTransaction);
    	}
    }

    @Test
    public void reactivateCancelledSubscription() {
    	String nodeName = "RECORDSMANAGEMENT";
    	String userName = "mgeorgieva@amcad.com";
    	List<RecurTx> recurringTransactions = this.eComFacadeService.getRecurTransactionsByNode(userName, nodeName);

    	for(RecurTx recurTransaction : recurringTransactions) {
    		System.out.println(recurTransaction);
    	}
    }

    @Test
    public void removeSubscription() {
    	String nodeName = "RECORDSMANAGEMENT";
    	String userName = "mgeorgieva@amcad.com";
    	List<RecurTx> recurringTransactions = this.eComFacadeService.getRecurTransactionsByNode(userName, nodeName);

    	for(RecurTx recurTransaction : recurringTransactions) {
    		System.out.println(recurTransaction);
    	}
    }
    
    
}