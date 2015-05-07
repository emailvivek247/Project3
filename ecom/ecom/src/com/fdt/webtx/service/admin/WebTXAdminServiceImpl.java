package com.fdt.webtx.service.admin;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.paymentgateway.service.PaymentGatewayService;
import com.fdt.security.dao.UserDAO;
import com.fdt.webtx.dao.WebTxDAO;
import com.fdt.webtx.entity.WebTx;
import com.fdt.webtx.entity.WebTxItem;

@Service("webTXAdminService")
public class WebTXAdminServiceImpl implements WebTXAdminService {

    private final static Logger logger = LoggerFactory.getLogger(WebTXAdminServiceImpl.class);

    @Autowired
    private EComService eComService;

    @Value("${ecommerce.serverurl}")
    private String ecomServerURL = null;

    @Autowired
    @Qualifier("paymentGateway")
    private PaymentGatewayService paymentGateway;

    @Autowired
    private WebTxDAO webTransactionDAO;

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

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public PayPalDTO doPartialReferenceCreditWeb(Long webTxItemId, String siteName, String comments,
            String modUserId, String machineName) throws PaymentGatewayUserException, PaymentGatewaySystemException,
            SDLBusinessException {
        Assert.hasLength(comments, "Comments Cannot be Null!");
        Assert.isTrue((comments.length() < 1999), "Comments Cannot Be Greater Than 2000 characters length.");
        Assert.notNull(modUserId, "Modified User Id!");
        PayPalDTO paymentTxResponseDTO = null;
        WebTx webTransaction = this.getWebTransactionItemByItemId(webTxItemId, siteName);

        if (Days.daysBetween(new DateTime(webTransaction.getTransactionDate()).withTimeAtStartOfDay(),
                new DateTime().withTimeAtStartOfDay()).getDays()  > Integer.valueOf(txValidityPeriod)) {
            throw new SDLBusinessException(this.getMessage("web.trans.txDatePastValidityPeriod", new Object[]{webTransaction.getTxRefNum(),
                            SystemUtil.format(webTransaction.getTransactionDate().toString()), txValidityPeriod}));
        }

        if(webTransaction == null || webTransaction.getWebTxItems() == null || webTransaction.getWebTxItems().size() == 0) {
            throw new SDLBusinessException();
        }
        paymentTxResponseDTO = this.paymentGateway.doPartialReferenceCredit(webTransaction.getSite(),
            webTransaction.getTxRefNum(), webTransaction.getWebTxItems().get(0).getTotalTxAmount(), "WEB",
                "doReferenceCreditWeb", modUserId);
        WebTxItem webTxItem = webTransaction.getWebTxItems().get(0);
        paymentTxResponseDTO.setTxAmount(webTxItem.getTotalTxAmount());
        paymentTxResponseDTO.setReferencedtxRefNum(webTransaction.getTxRefNum());
        webTransaction.setOrigTxRefNum(webTransaction.getTxRefNum());
        webTransaction.setTxRefNum(paymentTxResponseDTO.getTxRefNum());
        webTransaction.setModifiedDate(new Date());
        webTransaction.setCreatedDate(new Date());
        webTransaction.setTransactionType(TransactionType.REFUND);
        webTransaction.setModifiedBy(modUserId);
        webTransaction.setCreatedBy(modUserId);
        webTransaction.setComments(comments);
        webTransaction.setMachineName(machineName);
        webTransaction.setBaseAmount(webTxItem.getBaseAmount());
        webTransaction.setServiceFee(webTxItem.getServiceFee());
        webTransaction.setTotalTxAmount(webTxItem.getTotalTxAmount());
        webTransaction.setId(null);
        webTransaction.setAuthCode(paymentTxResponseDTO.getAuthCode());
        webTransaction.setSettlementStatus(SettlementStatusType.UNSETTLED);
        webTransaction.setCheckNum(null);
        webTransaction.setSiteId(webTransaction.getSite().getId());
        webTransaction.setTransactionDate(SystemUtil.changeTimeZone(new Date(),
        		TimeZone.getTimeZone(webTransaction.getSite().getTimeZone())));
        webTransaction.setTxFeeFlat(0.0d);
        if (webTransaction.getCardType() == CardType.AMEX) {
            webTransaction.setTxFeePercent(0.0d);
        } else {
            webTransaction.setTxFeePercent(0 - webTransaction.getTxFeePercent());
        }

        try {
            this.webTransactionDAO.saveWebTransaction(webTransaction);
            int nOfRecordsUpdated = this.webTransactionDAO.updateRefundTxForWebTxItem(webTxItem.getId(),
                    webTransaction.getId(), modUserId);
            if (nOfRecordsUpdated == 0) {
                throw new RuntimeException("Records Not Updated in updateRefundTxForWebTxItem");
            }
        } catch (Exception e) {
            logger.error(NOTIFY_ADMIN, webTransaction.toString());
            throw new RuntimeException("Server Error, Please contact Administrator");
        }
        return paymentTxResponseDTO;
    }

    @Transactional(readOnly = true)
    public WebTx getWebTransactionItemByItemId(Long itemId, String siteName) {
        Assert.notNull(itemId, "Item Id cannot be Null");
        return this.webTransactionDAO.getWebTransactionItemByItemId(itemId, siteName);
    }

    @Transactional(readOnly = true)
    public WebTx getReferencedWebTransactionItemByItemId(Long itemId, String siteName) {
        Assert.notNull(itemId, "Item Id cannot be Null");
        return this.webTransactionDAO.getReferencedWebTransactionItemByItemId(itemId, siteName);
    }

    @Transactional(readOnly = true)
    public List<WebTx> getReferencedWebTransaction(String originaltxRefNumber, String siteName) {
        Assert.hasLength(originaltxRefNumber, "Original Tx Reference Number Cannot be Null/Empty");
        return this.webTransactionDAO.getReferencedWebTransaction(originaltxRefNumber, siteName);
    }

    private String getMessage(String messageKey, Object[] object) {
        return this.messages.getMessage(messageKey, object, new Locale("en"));
    }

}
