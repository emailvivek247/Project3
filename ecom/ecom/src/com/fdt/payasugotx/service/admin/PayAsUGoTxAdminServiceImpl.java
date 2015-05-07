package com.fdt.payasugotx.service.admin;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import javax.persistence.Transient;

import org.joda.time.DateTime;
import org.joda.time.Days;
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

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.util.SystemUtil;
import com.fdt.ecom.dao.EComDAO;
import com.fdt.ecom.entity.enums.CardType;
import com.fdt.ecom.entity.enums.SettlementStatusType;
import com.fdt.ecom.entity.enums.TransactionType;
import com.fdt.ecom.service.EComService;
import com.fdt.email.EmailProducer;
import com.fdt.payasugotx.dao.PayAsUGoTxDAO;
import com.fdt.payasugotx.entity.PayAsUGoTx;
import com.fdt.payasugotx.entity.PayAsUGoTxItem;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.paymentgateway.service.PaymentGatewayService;
import com.fdt.security.dao.UserDAO;
import com.fdt.security.entity.Access;

@Service("PayAsUGoTxAdminService")
public class PayAsUGoTxAdminServiceImpl implements PayAsUGoTxAdminService {

    private final static Logger logger = LoggerFactory.getLogger(PayAsUGoTxAdminServiceImpl.class);

    private static int REFERENCE_NUMBER_LENGTH = 12;

    @Autowired
    private EComService eComService;

    @Value("${ecommerce.serverurl}")
    private String ecomServerURL = null;

    @Autowired
    @Qualifier("paymentGateway")
    private PaymentGatewayService paymentGateway;

    @Autowired
    private PayAsUGoTxDAO payAsUGoTxDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private EComDAO eComDAO;

    @Autowired
    private EmailProducer emailProducer;

    @Autowired
    @Qualifier("serverMessageSource")
    private MessageSource messages = null;

    @Value("${tx.ValidityPeriod}")
    /* Default Value is 60 Days */
    private String txValidityPeriod = "60";

    @Transient
   	private Access access = null;

    public Access getAccess() {
		return access;
	}

	public void setAccess(Access access) {
		this.access = access;
	}

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public PayPalDTO doPartialReferenceCreditPayAsUGo(Long payAsUGoTxItemId, String siteName, String comments,
            String modUserId, String machineName) throws PaymentGatewayUserException, PaymentGatewaySystemException,
            SDLBusinessException {
        Assert.hasLength(comments, "Comments Cannot be Null!");
        Assert.isTrue((comments.length() < 1999), "Comments Cannot Be Greater Than 2000 characters length.");
        Assert.notNull(modUserId, "Modified User Id!");
        PayPalDTO paymentTxResponseDTO = null;
        PayAsUGoTx payAsUGoTransaction = this.getPayAsUGoTransactionItemByItemId(payAsUGoTxItemId, siteName);

        if (Days.daysBetween(new DateTime(payAsUGoTransaction.getTransactionDate()).withTimeAtStartOfDay(),
                new DateTime().withTimeAtStartOfDay()).getDays()  > Integer.valueOf(txValidityPeriod)) {
            throw new SDLBusinessException(this.getMessage("web.trans.txDatePastValidityPeriod", new Object[]{payAsUGoTransaction.getTxRefNum(),
                            SystemUtil.format(payAsUGoTransaction.getTransactionDate().toString()), txValidityPeriod}));
        }

        if(payAsUGoTransaction == null || payAsUGoTransaction.getPayAsUGoTxItems() == null || payAsUGoTransaction.getPayAsUGoTxItems().size() == 0) {
            throw new SDLBusinessException();
        }
        if(payAsUGoTransaction.getPayAsUGoTxItems().get(0).getTotalTxAmount() > 0.0){
            paymentTxResponseDTO = this.paymentGateway.doPartialReferenceCredit(payAsUGoTransaction.getSite(),
                    payAsUGoTransaction.getTxRefNum(), payAsUGoTransaction.getPayAsUGoTxItems().get(0).getTotalTxAmount(), "WEB",
                        "doReferenceCreditWeb", modUserId);
        } else {
        	paymentTxResponseDTO = new PayPalDTO();
        	String randomString = UUID.randomUUID().toString();
        	randomString = randomString.replaceAll("-", "");
        	paymentTxResponseDTO.setTxRefNum(randomString.substring(0, REFERENCE_NUMBER_LENGTH).toUpperCase());
        }
        PayAsUGoTxItem payAsUGoTxItem = payAsUGoTransaction.getPayAsUGoTxItems().get(0);
        paymentTxResponseDTO.setTxAmount(payAsUGoTxItem.getTotalTxAmount());
        paymentTxResponseDTO.setReferencedtxRefNum(payAsUGoTransaction.getTxRefNum());
        payAsUGoTransaction.setOrigTxRefNum(payAsUGoTransaction.getTxRefNum());
        payAsUGoTransaction.setTxRefNum(paymentTxResponseDTO.getTxRefNum());
        payAsUGoTransaction.setModifiedDate(new Date());
        payAsUGoTransaction.setCreatedDate(new Date());
        payAsUGoTransaction.setTransactionType(TransactionType.REFUND);
        payAsUGoTransaction.setModifiedBy(modUserId);
        payAsUGoTransaction.setCreatedBy(modUserId);
        payAsUGoTransaction.setComments(comments);
        payAsUGoTransaction.setMachineName(machineName);
        payAsUGoTransaction.setBaseAmount(payAsUGoTxItem.getBaseAmount());
        payAsUGoTransaction.setServiceFee(payAsUGoTxItem.getServiceFee());
        payAsUGoTransaction.setTotalTxAmount(payAsUGoTxItem.getTotalTxAmount());
        payAsUGoTransaction.setId(null);
        payAsUGoTransaction.setAuthCode(paymentTxResponseDTO.getAuthCode());
        payAsUGoTransaction.setSettlementStatus(SettlementStatusType.UNSETTLED);
        payAsUGoTransaction.setCheckNum(null);
        payAsUGoTransaction.setSiteId(payAsUGoTransaction.getSite().getId());
        payAsUGoTransaction.setTransactionDate(SystemUtil.changeTimeZone(new Date(),
        		TimeZone.getTimeZone(payAsUGoTransaction.getSite().getTimeZone())));
        payAsUGoTransaction.setTxFeeFlat(0.0d);
        if (payAsUGoTransaction.getCardType() == CardType.AMEX) {
            payAsUGoTransaction.setTxFeePercent(0.0d);
        } else {
            payAsUGoTransaction.setTxFeePercent(0 - payAsUGoTransaction.getTxFeePercent());
        }
        payAsUGoTransaction.setPageCount(payAsUGoTxItem.getPageCount());
        payAsUGoTransaction.setItemCount(payAsUGoTxItem.getItemQuantity());

        try {
            this.payAsUGoTxDAO.savePayAsUGoTransaction(payAsUGoTransaction);
            int nOfRecordsUpdated = this.payAsUGoTxDAO.updateRefundTxForPayAsUGoTxItem(payAsUGoTxItem.getId(),
                    payAsUGoTransaction.getId(), modUserId);
            if (nOfRecordsUpdated == 0) {
                throw new RuntimeException("Records Not Updated in updateRefundTxForPayAsUGoTxItem");
            }
        } catch (Exception e) {
            logger.error(NOTIFY_ADMIN, payAsUGoTransaction.toString());
            throw new RuntimeException("Server Error, Please contact Administrator");
        }
        return paymentTxResponseDTO;
    }

    @Transactional(readOnly = true)
    public PayAsUGoTx getPayAsUGoTransactionItemByItemId(Long itemId, String siteName) {
        Assert.notNull(itemId, "Item Id cannot be Null");
        return this.payAsUGoTxDAO.getPayAsUGoTransactionItemByItemId(itemId, siteName);
    }

    @Transactional(readOnly = true)
    public PayAsUGoTx getReferencedPayAsUGoTransactionItemByItemId(Long itemId, String siteName) {
        Assert.notNull(itemId, "Item Id cannot be Null");
        return this.payAsUGoTxDAO.getReferencedPayAsUGoTransactionItemByItemId(itemId, siteName);
    }

    @Transactional(readOnly = true)
    public List<PayAsUGoTx> getReferencedPayAsUGoTransaction(String originaltxRefNumber, String siteName) {
        Assert.hasLength(originaltxRefNumber, "Original Tx Reference Number Cannot be Null/Empty");
        return this.payAsUGoTxDAO.getReferencedPayAsUGoTransaction(originaltxRefNumber, siteName);
    }

    private String getMessage(String messageKey, Object[] object) {
        return this.messages.getMessage(messageKey, object, new Locale("en"));
    }

    @Transactional(readOnly = true)
    public List<PayAsUGoTx> getPayAsUGoTransactions(String userName, Long siteId) {
        Assert.hasLength(userName, "User Name Cannot be Null/Empty");
        if(siteId == null) {
        	return this.payAsUGoTxDAO.getPayAsUGoTransactions(userName);
        } else {
        	return this.payAsUGoTxDAO.getPayAsUGoTransactionsBySite(userName, siteId);
        }
    }

}
