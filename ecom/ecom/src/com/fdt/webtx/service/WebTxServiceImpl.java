package com.fdt.webtx.service;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
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
import com.fdt.ecom.entity.Code;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.Merchant;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.SiteConfiguration;
import com.fdt.ecom.entity.WebPaymentFee;
import com.fdt.ecom.entity.enums.CardType;
import com.fdt.ecom.entity.enums.SettlementStatusType;
import com.fdt.ecom.entity.enums.TransactionType;
import com.fdt.ecom.service.EComService;
import com.fdt.ecom.util.CreditCardUtil;
import com.fdt.email.EmailProducer;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.paymentgateway.service.PaymentGatewayService;
import com.fdt.security.dao.UserDAO;
import com.fdt.webtx.dao.WebTxDAO;
import com.fdt.webtx.dto.WebTransactionDTO;
import com.fdt.webtx.dto.WebTxExtResponseDTO;
import com.fdt.webtx.entity.WebTx;
import com.fdt.webtx.entity.WebTxItem;

@Service("webTxService")
public class WebTxServiceImpl implements WebTxService {

    private final static Logger logger = LoggerFactory.getLogger(WebTxServiceImpl.class);

    private static final String REGISTERED_APPLICATION_CATEGORY = "REGISTERED_APPLICATION";

    @Autowired
    private EComService eComService;

    @Value("${ecommerce.serverurl}")
    private String ecomServerURL = null;

    @Autowired
    @Qualifier("paymentGateway")
    private PaymentGatewayService paymentGateway = null;

    @Autowired
    @Qualifier("serverMessageSource")
    private MessageSource messages = null;

    @Autowired
    private WebTxDAO webTransactionDAO = null;

    @Autowired
    private UserDAO userDAO = null;

    @Autowired
    private EComDAO eComDAO = null;

    @Autowired
    private EmailProducer emailProducer = null;

    @Value("${tx.ValidityPeriod}")
    /* Default Value is 60 Days */
    private String txValidityPeriod = "60";


	/**This Method Is Used For Traffic Ticket Payments. All The Web Transaction Items Are Saved Along With (parent)
	 * Webtransaction, If Payment Is Successful. If EmailId Is Not Null Then Receipt Will Be Also Sent.
	 * @param siteName Name of the Site.
	 * @param webTransactionDTO Contains Webtransaction item and Creditcard.
	 * @param emailId EmailId to which receipt is sent. If null, then this parameter is assigned the value of application.
	 * @param application Application Name.
	 * @return WebTransaction.
	 * @throws PaymentGatewayUserException
	 * @throws PaymentGatewaySystemException
	 * @throws SDLBusinessException
	 */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public WebTx doSaleWebPosts(String siteName, WebTransactionDTO webTransactionDTO, String emailId,
            String application) throws PaymentGatewayUserException, PaymentGatewaySystemException, SDLBusinessException {
        Assert.hasLength(siteName, "Site Name Cannot be Null");
        Assert.notNull(webTransactionDTO, "webTransactionDTO Cannot be Null");
        Assert.hasLength(application, "Application Name Cannot be Null");
        boolean isValidApplication = false;
        PayPalDTO paymentTxResponseDTO = null;
        WebTx webTransaction =  new WebTx();
        List<WebTx> webTransactions =  new LinkedList<WebTx>();
        String txRefNumber = null;
        Site site = null;
        boolean isException = false;
        double totalBaseAmount = 0.0d;
        double totalTax = 0.0d;
        double totalServiceFee = 0.0d;
        double totalTxAmount = 0.0d;
        Merchant merchant = null;
        if (StringUtils.isBlank(emailId)) {
            emailId = application;
        }
        CreditCard creditCard = webTransactionDTO.getCreditCard();
        CardType cardType = CreditCardUtil.getCardType(creditCard.getNumber());

        try {
            List<Code> registeredApplicationList = this.eComDAO.getCodes(REGISTERED_APPLICATION_CATEGORY);
            for (Code code : registeredApplicationList) {
                if (code.getCode().equals(application)) {
                    isValidApplication = true;
                }
            }
            if (isValidApplication) {
                site = this.eComService.getSiteDetailsBySiteName(siteName);
                if (site != null) {
                    for(WebTxItem webTransactionItem: webTransactionDTO.getWebTransactionItemList()) {
                        webTransactionItem.setServiceFee(calculateFeeWEB(webTransactionItem.getBaseAmount()
                            + webTransactionItem.getTax(), site));
                        webTransactionItem.setTotalTxAmount(webTransactionItem.getBaseAmount() + webTransactionItem.getTax()
                            + webTransactionItem.getServiceFee());
                        totalBaseAmount = totalBaseAmount + webTransactionItem.getBaseAmount();
                        totalTax = totalTax + webTransactionItem.getTax();
                        totalServiceFee = totalServiceFee + webTransactionItem.getServiceFee();
                    }
                    totalTxAmount = totalBaseAmount + totalTax + totalServiceFee;
                    totalTxAmount = new BigDecimal(totalTxAmount).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
                    if (totalTxAmount < site.getCardUsageFee().getMicroTxFeeCutOff() && site.isEnableMicroTxOTC()) {
                        merchant = site.getMicroMerchant();
                    } else {
                        merchant = site.getMerchant();
                    }
                    paymentTxResponseDTO = this.paymentGateway.doSale(site, totalTxAmount, creditCard, "doSaleWebPosts",
                        emailId, false);
                    txRefNumber = paymentTxResponseDTO.getTxRefNum();
                    if (paymentTxResponseDTO.getTxRefNum() != null && !paymentTxResponseDTO.getTxRefNum().isEmpty()) {
                        webTransaction.setTxRefNum(paymentTxResponseDTO.getTxRefNum());
                        webTransaction.setAuthCode(paymentTxResponseDTO.getAuthCode());
                        webTransaction.setCardType(cardType);
                        webTransaction.setCardNumber(creditCard.getNumber());
                        webTransaction.setExpiryMonth(creditCard.getExpiryMonth());
                        webTransaction.setExpiryYear(creditCard.getExpiryYear());
                        webTransaction.setAddressLine1(creditCard.getAddressLine1());
                        webTransaction.setAddressLine2(creditCard.getAddressLine2());
                        webTransaction.setCity(creditCard.getCity());
                        webTransaction.setState(creditCard.getState());
                        webTransaction.setZip(creditCard.getZip());
                        webTransaction.setPhone(creditCard.getPhone());
                        webTransaction.setBaseAmount(totalBaseAmount);
                        webTransaction.setTax(totalTax);
                        webTransaction.setServiceFee(totalServiceFee);
                        webTransaction.setTotalTxAmount(totalTxAmount);
                        webTransaction.setTransactionType(TransactionType.CHARGE);
                        webTransaction.setAccountName(creditCard.getName());
                        webTransaction.setSettlementStatus(SettlementStatusType.UNSETTLED);
                        webTransaction.setMachineName(webTransactionDTO.getTransactionLocation());
                        webTransaction.setMerchantId(merchant.getId());
                        webTransaction.setSiteId(site.getId());
                        webTransaction.setApplication(application);
                        /** Make the Merchant Information as Null **/
                        site.addMerchant(null);
                        webTransaction.setSite(site);
                        webTransaction.setModifiedBy(emailId);
                        webTransaction.setCreatedBy(emailId);
                        webTransaction.setModifiedDate(new Date());
                        webTransaction.setCreatedDate(new Date());
                        webTransaction.setTransactionDate(SystemUtil.changeTimeZone(new Date(),
                                TimeZone.getTimeZone(site.getTimeZone())));

                        webTransaction.setOfficeLoc(webTransactionDTO.getOfficeLoc());
                        webTransaction.setOfficeLocAddressLine1(webTransactionDTO.getOfficeLocAddressLine1());
                        webTransaction.setOfficeLocAddressLine2(webTransactionDTO.getOfficeLocAddressLine2());
                        webTransaction.setOfficeLocCity(webTransactionDTO.getOfficeLocCity());
                        webTransaction.setOfficeLocState(webTransactionDTO.getOfficeLocState());
                        webTransaction.setOfficeLocZip(webTransactionDTO.getOfficeLocZip());
                        webTransaction.setOfficeLocPhone(webTransactionDTO.getOfficeLocPhone());
                        webTransaction.setOfficeLocComments1(webTransactionDTO.getOfficeLocComments1());
                        webTransaction.setOfficeLocComments2(webTransactionDTO.getOfficeLocComments2());
                        webTransaction.setInvoiceNumber(webTransactionDTO.getInvoiceId());


                        if (cardType == CardType.AMEX) {
                            webTransaction.setTxFeePercent(merchant.getTxFeePercentAmex());
                            webTransaction.setTxFeeFlat(merchant.getTxFeeFlatAmex());
                        } else {
                            webTransaction.setTxFeePercent(merchant.getTxFeePercent());
                            webTransaction.setTxFeeFlat(merchant.getTxFeeFlat());
                        }

                        this.webTransactionDAO.saveWebTransaction(webTransaction);
                        List<WebTxItem> webTxItems = new LinkedList<WebTxItem>();
                        for (WebTxItem webTransactionItem : webTransactionDTO.getWebTransactionItemList()) {
                            webTransactionItem.setWebTxId(webTransaction.getId());
                            webTransactionItem.setModifiedBy(emailId);
                            webTransactionItem.setCreatedBy(emailId);
                            webTransactionItem.setModifiedDate(new Date());
                            webTransactionItem.setCreatedDate(new Date());
                            webTransactionItem.setActive(true);
                            webTxItems.add(webTransactionItem);
                        }
                        webTransaction.setWebTxItems(webTxItems);
                        webTransactions.add(webTransaction);
                        this.webTransactionDAO.saveWebTransactionItem(webTxItems);
                    }
                    /** Send E-Mails Only if the user is Passing it **/
                    if (!emailId.equalsIgnoreCase(application)) {
                        /** Send E-Mail Confirmation **/
                        Map<String, Object> emailData = new HashMap<String, Object>();
                        emailData.put("webTransactions", webTransactions);
                        emailData.put("emailId", emailId);
                        emailData.put("currentDate", new Date());
                        emailData.put("cardCharged", creditCard.getNumber().substring(creditCard.getNumber().length() - 4));
                        emailData.put("serverUrl", this.ecomServerURL);
                        SiteConfiguration siteConfig = this.eComService.getSiteConfiguration(site.getId());
                        Assert.notNull(siteConfig, "siteConfig Cannot be Null");
                        if (emailId != null && emailId != "") {
                            this.emailProducer.sendMailUsingTemplate(siteConfig.getFromEmailAddress(), emailId,
                                siteConfig.getWebPaymentConfSubject(), siteConfig.getEmailTemplateFolder() +
                                    siteConfig.getWebPaymentConfTemplate(), emailData);
                        }
                    }
                }
            } else {
                SDLBusinessException businessException = new SDLBusinessException();
                businessException.setBusinessMessage(this.getMessage("web.invalid.application"));
                throw businessException;
            }
        } catch (PaymentGatewayUserException paymentGatewayUserException) {
            logger.error("Exception Occured in doSaleWebPosts", paymentGatewayUserException);
            isException = true;
            throw paymentGatewayUserException;
        } catch (PaymentGatewaySystemException paymentGatewaySystemException) {
            logger.error("Exception Occured in doSaleWebPosts", paymentGatewaySystemException);
            isException = true;
            throw paymentGatewaySystemException;
        } catch (Exception exception) {
            logger.error("Exception Occured in doSaleWebPosts", exception);
            isException = true;
            throw new RuntimeException(exception.getMessage());
        } finally {
            if (isException && txRefNumber != null) {
                try {
                    this.paymentGateway.doReferenceCredit(site, txRefNumber, "WEB", "doSaleWebPosts", emailId);
                } catch (Exception exception) {
                    logger.error(NOTIFY_ADMIN, "Error in Refunding the Money Back when there is an Exception in " +
                        "doSaleWebPosts {}", exception);
                }

            }
        }
        return webTransaction;
    }



    /** This Method Is Used To do Reference Credit For Web Transactions.
     * @param txRefNumber Original Transaction Reference Number.
     * @param comments Comments Made By User While Refunding.
     * @param modUserId User Who Is Refunding.
     * @param machineName Machine Name From Which User Is Refunding
     * @param siteName SiteName Where The Original Transaction Is Made.
     * @return paypalDTO Returns DTO With TxNumber If Payment Goes Through.
     * @throws PaymentGatewayUserException
     * @throws PaymentGatewaySystemException
     * @throws SDLBusinessException
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public PayPalDTO doReferenceCreditWeb(String txRefNumber, String comments, String modUserId, String machineName,
            String siteName) throws PaymentGatewayUserException, PaymentGatewaySystemException, SDLBusinessException {
        Assert.hasLength(txRefNumber, "Transaction Ref Number Cannot be Null");
        Assert.hasLength(comments, "Comments Cannot be Null/Empty");
        Assert.hasLength(modUserId, "Modified User Id Cannot be Null/Empty");
        Assert.isTrue((comments.length() < 249), "Comments Cannot Be Greater Than 250 characters length.");
        SDLBusinessException sDLBusinessException = null;
        WebTx webTransaction = this.webTransactionDAO.getWebTransactionByTxRefNum(txRefNumber, siteName);

        if(webTransaction == null) {
        	sDLBusinessException = new SDLBusinessException();
        	sDLBusinessException.setErrorCode("ERROR");
        	sDLBusinessException.setBusinessMessage("Invalid Transaction Reference Number!");
            throw sDLBusinessException;
        }

        if (Days.daysBetween(new DateTime(webTransaction.getTransactionDate()).toDateMidnight(),
                new DateTime().toDateMidnight()).getDays()  > Integer.valueOf(txValidityPeriod)) {
        	sDLBusinessException = new SDLBusinessException();
        	sDLBusinessException.setErrorCode("ERROR");
        	sDLBusinessException.setBusinessMessage(this.getMessage("web.trans.txDatePastValidityPeriod",
        			new Object[]{webTransaction.getTxRefNum(),
        			SystemUtil.format(webTransaction.getTransactionDate().toString()), txValidityPeriod}));
            throw sDLBusinessException;
        }

        Long originalWebTxId = webTransaction.getId();
        Site site = webTransaction.getSite();
        PayPalDTO paymentTxResponseDTO = this.paymentGateway.doReferenceCredit(site, txRefNumber, "WEB",
        		"doReferenceCreditWeb", modUserId);
        paymentTxResponseDTO.setTxAmount(webTransaction.getTotalTxAmount());
        webTransaction.setOrigTxRefNum(webTransaction.getTxRefNum());
        webTransaction.setTxRefNum(paymentTxResponseDTO.getTxRefNum());
        webTransaction.setModifiedDate(new Date());
        webTransaction.setMerchantId(webTransaction.getSite().getMerchant().getId());
        webTransaction.setCreatedDate(new Date());
        webTransaction.setTransactionType(TransactionType.REFUND);
        webTransaction.setModifiedBy(modUserId);
        webTransaction.setCreatedBy(modUserId);
        webTransaction.setComments(comments);
        webTransaction.setMachineName(machineName);
        webTransaction.setId(null);
        webTransaction.setAuthCode(paymentTxResponseDTO.getAuthCode());
        webTransaction.setSettlementStatus(SettlementStatusType.UNSETTLED);
        webTransaction.setCheckNum(null);
        webTransaction.setSiteId(webTransaction.getSite().getId());
        webTransaction.setTxFeeFlat(0.0d);
        if (webTransaction.getCardType() == CardType.AMEX) {
            webTransaction.setTxFeePercent(0.0d);
        } else {
            webTransaction.setTxFeePercent(0 - webTransaction.getTxFeePercent());
        }
        try {
             webTransaction.setTransactionDate(SystemUtil.changeTimeZone(new Date(),
            		 TimeZone.getTimeZone(site.getTimeZone())));
            this.webTransactionDAO.saveWebTransaction(webTransaction);
            int nOfRecordsUpdated = this.webTransactionDAO.updateRefundTxForWebTxItems(originalWebTxId,
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


    /** The method getWebTransactionsForExtApp(String siteName, Date fromDate, Date endDate, String txType)
     *  checks whether the siteName passed is a valid one or not. If it is not valid, then it throws SDLBusinessException
     *  and makes sure that difference between fromDate and endDate is less than searchDayThreshold for that particular Site.
     * @param siteName            Throws  SDLBusinessException if siteName is Invalid
     * @param fromDate            fromDate
     * @param endDate            endDate
     * @param txType            CHARGE, REFUND or null
     * @return List<WebTransaction>        List of Web Transactions occured during the mentioned dates for a particular site.
     */
    @Transactional(readOnly = true)
    public WebTxExtResponseDTO getWebTransactionsForExtApp(String siteName, Date fromDate, Date endDate,
            String txType)  {
    	logger.info("Entering getWebTransactionsForExtApp: SiteName {} fromDate {} endDate {} txType {}", siteName, fromDate,
    			endDate, txType);
    	WebTxExtResponseDTO webTransactionExtResponseDTO = new WebTxExtResponseDTO();
        Assert.notNull(fromDate, "Start Date Cannot be Null");
        Assert.notNull(endDate, "End Date Cannot be Null");
        Assert.hasLength(siteName, "Site Name Cannot be Null/Empty");
        Site site = null;
		try {
			site = this.eComService.getSiteDetailsBySiteName(siteName);
		} catch (SDLBusinessException e) {
			webTransactionExtResponseDTO.setErrorDesc(e.getBusinessMessage());
			 return webTransactionExtResponseDTO;
		}
        Long searchDayThreshold = site.getSearchDayThreshold();
        int searchDayThresholdInt = searchDayThreshold.intValue();
        DateTime fromDateTime = new DateTime(fromDate);
        DateTime endDateTime = new DateTime(endDate);
        int totalDays = Days.daysBetween(fromDateTime.toDateMidnight(), endDateTime.toDateMidnight()).getDays();
        if(totalDays > searchDayThresholdInt) {
           webTransactionExtResponseDTO.setErrorDesc(this.getMessage("web.searchtx.threshold"));
           return webTransactionExtResponseDTO;
        }
        List<WebTx> webTransactions = this.webTransactionDAO.getWebTransactionsForExtApp(siteName,
        		SystemUtil.changeTimeZone(fromDate, TimeZone.getTimeZone(site.getTimeZone())),
        		SystemUtil.changeTimeZone(endDate, TimeZone.getTimeZone(site.getTimeZone())), txType);
        webTransactionExtResponseDTO.setWebTransactionList(webTransactions);
        return webTransactionExtResponseDTO;
    }

    /** This Method Is Used To Get The All The Information About Webtransaction Of The Supplied Transaction Reference Number.
     * @param txRefNumber Transaction Reference Number.
     * @param siteName Name Of Site.
     * @return WebTransaction.
     */
    @Transactional(readOnly = true)
    public WebTx getWebTransactionByTxRefNum(String txRefNumber, String siteName) {
        Assert.hasLength(txRefNumber, "Tx Reference Number Cannot be Null/Empty");
        return this.webTransactionDAO.getWebTransactionByTxRefNum(txRefNumber, siteName);
    }

    /** This Method Calculates The Service Fee For Each Webtransaction Item.
     * @param siteName Name Of The Site.
     * @param itemList List Of WebTransactionItems.
     * @return List Of WebTransactionItems With Service Fee & Total Transaction Amount Set.
     * @throws SDLBusinessException
     */
    @Transactional(readOnly = true)
    public List<WebTxItem> doSaleGetInfoWEB(String siteName, List<WebTxItem> itemList)
            throws SDLBusinessException {
        Assert.hasLength(siteName, "Site Name Cannot be Null");
        Site site = this.eComService.getSiteDetailsBySiteName(siteName);
        for(WebTxItem webTransactionItem: itemList) {
            webTransactionItem.setServiceFee(calculateFeeWEB(webTransactionItem.getBaseAmount()
                + webTransactionItem.getTax(), site));
            webTransactionItem.setTotalTxAmount(webTransactionItem.getBaseAmount() + webTransactionItem.getTax()
                + webTransactionItem.getServiceFee());
        }
        return itemList;
    }

    @Transactional(readOnly = true)
	public WebTx getWebTxByInvoiceNumber(String invoiceNumber, String siteName) {
    	Assert.hasLength(invoiceNumber, "Invoice Number Cannot be Null/Empty");
        return this.webTransactionDAO.getWebTxByInvoiceNumber(invoiceNumber, siteName);
	}

    private String getMessage(String messageKey, Object[] object) {
        return this.messages.getMessage(messageKey, object, new Locale("en"));
    }

    private String getMessage(String messageKey) {
        return this.messages.getMessage(messageKey, null, new Locale("en"));
    }

    private static double calculateFeeWEB(double actualAmtToCharge, Site site) {
        double serviceFee = 0.0d;
        WebPaymentFee webPaymentFee = site.getWebPaymentFee();
        if (webPaymentFee != null) {
            if (actualAmtToCharge <= webPaymentFee.getFlatFeeCutOff()) {
                            serviceFee = webPaymentFee.getFlatFee();
            } else if (actualAmtToCharge > webPaymentFee.getFlatFeeCutOff()) {
                            serviceFee = (actualAmtToCharge / 100.00) * webPaymentFee.getPercenteFee();
            }
        }
        serviceFee = serviceFee + webPaymentFee.getAdditionalFee();
        serviceFee = (new BigDecimal(serviceFee).setScale(2, RoundingMode.HALF_DOWN).doubleValue());
        return serviceFee;
    }

}