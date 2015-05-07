package com.fdt.recurtx.service;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fdt.ecom.dao.EComDAO;
import com.fdt.ecom.service.EComService;
import com.fdt.email.EmailProducer;
import com.fdt.paymentgateway.service.PaymentGatewayService;
import com.fdt.recurtx.dao.RecurTxDAO;
import com.fdt.recurtx.entity.RecurTx;
import com.fdt.security.dao.UserDAO;
import com.fdt.subscriptions.dao.SubDAO;
import com.fdt.subscriptions.service.SubService;

@Service("recurTxService")
public class RecurTxServiceImpl implements RecurTxService {

    private static final Logger logger = LoggerFactory.getLogger(RecurTxServiceImpl.class);

    @Autowired
    private EComService eComService;

    @Autowired
    @Qualifier("paymentGateway")
    private PaymentGatewayService paymentGateway;

    @Autowired
    private RecurTxDAO recurTxDAO;

    @Autowired
    private SubDAO subDAO;

    @Autowired
    private SubService subService;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private EComDAO eComDAO;

    @Value("${ecommerce.serverurl}")
    private String ecomServerURL = null;

    @Autowired
    private EmailProducer emailProducer;

    @Autowired
    @Qualifier("serverMessageSource")
    private MessageSource messages = null;

    /** This Method Is Used To Get The List Of Recurring Transactions Of The Supplied userName For A nodeName.
     * @param userName EmailId Of The User Logged In.
     * @param nodeName Name Of the Node.
     * @return List Of Recurring Transactions.
     */
    @Transactional(readOnly = true)
    public List<RecurTx> getRecurTxByNode(String userName, String nodeName) {
        Assert.hasLength(userName, "userName Cannot be Null/Empty!");
        List<RecurTx> recurTxHistInfoList = null;
        recurTxHistInfoList = this.recurTxDAO.getRecurTransactionsByNode(userName, nodeName);
        return recurTxHistInfoList;
    }

    /** This Method Is Used To Get The List Of Recurring Transactions Associated With The recurTxRefNum Of a userName.
     * @param userName EmailId Of The User Logged In.
     * @param recurTxRefNum Recurring Transaction Reference Number.
     * @return List Of Recurring Transactions.
     */
    @Transactional(readOnly = true)
    public List<RecurTx> getRecurTxDetail(String userName, String recurTxRefNum) {
        Assert.hasLength(userName, "userName Cannot be Null/Empty!");
        Assert.notNull(recurTxRefNum, "recurTxRefNum Cannot be Null/Empty!");
        return this.recurTxDAO.getRecuringTxDetail(userName, recurTxRefNum);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
	public void archiveRecurTransactions(String archivedBy, String archiveComments) {
    	this.recurTxDAO.archiveRecurTransactions(archivedBy, archiveComments);
	}

    private String getMessage(String messageKey) {
        return this.messages.getMessage(messageKey, null, new Locale("en"));
    }

}