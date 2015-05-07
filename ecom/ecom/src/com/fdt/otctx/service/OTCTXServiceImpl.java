package com.fdt.otctx.service;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.axis2.AxisFault;
import org.apache.axis2.axis2userguide.DecryptServiceStub;
import org.apache.axis2.axis2userguide.DecryptServiceStub.Authentication;
import org.apache.axis2.axis2userguide.DecryptServiceStub.DecryptCardSwipe;
import org.apache.axis2.axis2userguide.DecryptServiceStub.DecryptCardSwipeRequest;
import org.apache.axis2.axis2userguide.DecryptServiceStub.DecryptCardSwipeResponse0;
import org.apache.axis2.axis2userguide.DecryptServiceStub.EncryptedCardSwipe;
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

import com.fdt.common.entity.ErrorCode;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.util.SystemUtil;
import com.fdt.ecom.dao.EComDAO;
import com.fdt.ecom.entity.CreditUsageFee;
import com.fdt.ecom.entity.Merchant;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.enums.CardType;
import com.fdt.ecom.entity.enums.SettlementStatusType;
import com.fdt.ecom.entity.enums.TransactionType;
import com.fdt.ecom.service.EComService;
import com.fdt.ecom.util.CreditCardUtil;
import com.fdt.otctx.dao.OTCTxDAO;
import com.fdt.otctx.dto.OTCRequestDTO;
import com.fdt.otctx.dto.OTCResponseDTO;
import com.fdt.otctx.entity.OTCTx;
import com.fdt.otctx.exception.MagensaException;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.paymentgateway.service.PaymentGatewayService;

@Service("OTCTXService")
public class OTCTXServiceImpl implements OTCTXService {

    private final static Logger logger = LoggerFactory.getLogger(OTCTXServiceImpl.class);

    @Autowired
    private EComService eComService;

    @Autowired
    @Qualifier("paymentGateway")
    private PaymentGatewayService paymentGateway;

    @Autowired
    private OTCTxDAO oTCDAO;

    @Autowired
    private EComDAO eComDAO;

    @Value("${ecommerce.magensa.wsdl}")
    private String magensaWsdl = null;

    @Autowired
    @Qualifier("serverMessageSource")
    private MessageSource messages = null;

    @Value("${tx.ValidityPeriod}")
    /* Default Value is 60 Days */
    private String txValidityPeriod = "60";

    @Value("${ecommerce.magensa.certificatepath}")
    private String magensaCertficatePath = null;

    @Value("${ecommerce.magensa.certificatepwd}")
    private String magensaCertficatePwd = null;

    public static String TRACK_1 = "TRACK1";

    public static String TRACK_2 = "TRACK2";

    /** This Method Is Used For Doing A Reference Credit For Over The Counter Transaction. This method first determines
     * whether the original transaction date is less than a specific period old. If it is more, it will throw a
     * sdlBusinessException. And, if it is less it will do a ReferenceCredit.
     *
     * @param txRefNumber OTC Transaction Reference Number
     * @param comments Comments Entered By The User Who Is Issuing A Refund.
     * @param modUserId User Who Is Issuing A Refund.
     * @param machineName Machine Name From Which User Is Issuing A Refund.
     * @param siteName Name Of Site.
     * @return
     * @throws PaymentGatewayUserException
     * @throws PaymentGatewaySystemException
     * @throws SDLBusinessException
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public PayPalDTO doReferenceCreditOTC(String txRefNumber, String comments, String modUserId, String machineName,
    		String siteName) throws PaymentGatewayUserException, PaymentGatewaySystemException, SDLBusinessException {
        Assert.notNull(txRefNumber, "Transaction Ref Number Cannot be Null");
        Assert.notNull(comments, "Comments Cannot be Null!!");
        Assert.notNull(modUserId, "Modified User Id  Cannot be Null!!");
        Assert.isTrue((comments.length() < 1999), "Comments Cannot Be Greater Than 2000 characters length.");
        SDLBusinessException sDLBusinessException = null;
        PayPalDTO paymentTxResponseDTO = new PayPalDTO();
        OTCTx oTCTransaction = this.getOTCTransactionByTxRefNum(txRefNumber, siteName);

        if(oTCTransaction == null) {
        	sDLBusinessException = new SDLBusinessException();
        	sDLBusinessException.setErrorCode("ERROR");
        	sDLBusinessException.setBusinessMessage("Invalid Transaction Reference Number!!");
            throw sDLBusinessException;
        }

        if (Days.daysBetween(new DateTime(oTCTransaction.getTransactionDate()).withTimeAtStartOfDay(),
                new DateTime().withTimeAtStartOfDay()).getDays()  > Integer.valueOf(txValidityPeriod)) {
            sDLBusinessException = new SDLBusinessException();
        	sDLBusinessException.setErrorCode("ERROR");
        	sDLBusinessException.setBusinessMessage(this.getMessage("web.trans.txDatePastValidityPeriod",
                new Object[]{oTCTransaction.getTxRefNum(), SystemUtil.format(oTCTransaction.getTransactionDate().toString()),
                    txValidityPeriod}));
            throw sDLBusinessException;
        }

        Assert.notNull(oTCTransaction.getSite(), "Original Transaction Reference Number Has No MerchantInfo");
        paymentTxResponseDTO = this.paymentGateway.doReferenceCredit(oTCTransaction.getSite(), txRefNumber, "OTC",
            "doReferenceCreditOTC", modUserId);
        paymentTxResponseDTO.setTxAmount(oTCTransaction.getTotalTxAmount());
        oTCTransaction.setOrigTxRefNum(oTCTransaction.getTxRefNum());
        oTCTransaction.setTxRefNum(paymentTxResponseDTO.getTxRefNum());
        oTCTransaction.setModifiedDate(new Date());
        oTCTransaction.setTransactionDate(SystemUtil.changeTimeZone(new Date(), TimeZone.getTimeZone(oTCTransaction.
        	getSite().getTimeZone())));
        oTCTransaction.setCreatedDate(new Date());
        oTCTransaction.setTransactionType(TransactionType.REFUND);
        oTCTransaction.setModifiedBy(modUserId);
        oTCTransaction.setCreatedBy(modUserId);
        oTCTransaction.setComments(comments);
        oTCTransaction.setMachineName(machineName);
        oTCTransaction.setAuthCode(paymentTxResponseDTO.getAuthCode());
        oTCTransaction.setSettlementStatus(SettlementStatusType.UNSETTLED);
        oTCTransaction.setCheckNum(null);
        oTCTransaction.setId(null);
        oTCTransaction.setSiteId(oTCTransaction.getSite().getId());
        oTCTransaction.setTxFeeFlat(0.0d);
        if (oTCTransaction.getCardType() == CardType.AMEX) {
            oTCTransaction.setTxFeePercent(0.0d);
        } else {
            oTCTransaction.setTxFeePercent(0 - oTCTransaction.getTxFeePercent());
        }
        this.oTCDAO.saveOTCTx(oTCTransaction);
        return paymentTxResponseDTO;
    }

    /** This Method Is Used For Charging Over The Counter Transactions. This Method Internally Uses Magensa Service To
     *  Decrypt Credit Card Data. This Method First Calculates The Service Fee And Then Determines Whether To Use
     *  Micro Or Normal Merchant To Charge and finally saves the OTC transaction.
     * @param otcRequestDTO contains siteName, baseAmount, track1, track2, MPrintStatus, Ksn and office location.
     * @return OTCResponseDTO If payment goes through transactionReferenceNumber is set in this DTO and returned and, if it
     * fails errorCode and errorDec is set and returned.
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public OTCResponseDTO doSaleOTC(OTCRequestDTO oTCRequestDTO) {
    	logger.info("Entering doSaleOTC: " + oTCRequestDTO);

        OTCResponseDTO oTCResponseDTO = new OTCResponseDTO();
        Site site = null;
        Merchant merchant = null;
        OTCTx oTCTransaction = null;
        PayPalDTO paymentTxResDTO = new PayPalDTO();
        try {
        	Assert.hasLength(oTCRequestDTO.getSiteName(), "Site Name Cannot be Null/Empty");
            Assert.isTrue((oTCRequestDTO.getActualAmtToCharge() > 0), "Actual Amount To Be Charged should be greater than Zero");
            boolean isTrack1Empty = StringUtils.isBlank(oTCRequestDTO.getEncTrackOne());
    		boolean isTrack2Empty = StringUtils.isBlank(oTCRequestDTO.getEncTrackTwo());
    		boolean condition = ! (isTrack1Empty && isTrack2Empty);
            Assert.isTrue(condition, "Invalid Card. Please use a Different Card and Try Again.");
            Assert.hasLength(oTCRequestDTO.getEncMp(), "Invalid Card. Please use a Different Card and Try Again.");
            Assert.hasLength(oTCRequestDTO.getKsn(), "Invalid Card. Please use a Different Card and Try Again.");
            Assert.hasLength(oTCRequestDTO.getMprintStatus(), "Invalid Card. Please use a Different Card and Try Again.");
            Assert.hasLength(oTCRequestDTO.getOfficeLoc(), "Office Location Cannot be Null/Empty");
            Assert.hasLength(oTCRequestDTO.getUserLogged(), "User Logged Cannot be Null/Empty");

            Map<String, String> tracMap;
            site = this.eComService.getSiteDetailsBySiteName(oTCRequestDTO.getSiteName());

            String cardType = site.getMagensaInfo().getCardType();
            String encryptionBlockType = site.getMagensaInfo().getEncryptionBlockType();
            String hostId = site.getMagensaInfo().getHostId();
            String hostPwd = site.getMagensaInfo().getHostPassword();
            String registeredBy = site.getMagensaInfo().getRegisteredBy();
            String outputFormatCode = site.getMagensaInfo().getOutputFormatCode();
            String swipeCardDetails_track1 = null;
            String swipeCardDetails_track2 = null;

            oTCResponseDTO = calculateFeeOTC(oTCRequestDTO.getActualAmtToCharge() + oTCRequestDTO.getTax(), site);

            /** Select Whether this is Normal or a Micro merchant **/
            if (oTCResponseDTO.getTotalTxAmount() < site.getCardUsageFee().getMicroTxFeeCutOff() && site.isEnableMicroTxOTC()) {
                merchant = site.getMicroMerchant();
            } else {
                merchant = site.getMerchant();
            }
            try {
            	logger.info("Magensa Credentials: hostId {} hostPwd {}", hostId, hostPwd);
                tracMap = this.decrypt(oTCRequestDTO.getEncTrackOne(), oTCRequestDTO.getEncTrackTwo(),
                    oTCRequestDTO.getEncTrackThree(), oTCRequestDTO.getEncMp(), oTCRequestDTO.getKsn(),
                        oTCRequestDTO.getMprintStatus(), cardType, encryptionBlockType, hostId, hostPwd, registeredBy,
                            outputFormatCode);
            } catch (MagensaException e) {
                oTCResponseDTO.setErrorCode(e.getErrorCode());
                oTCResponseDTO.setErrorDesc("Invalid Card. Please use a Different Card and Try Again.");
                logger.error("Error in doSaleOTC oTCResponseDTO {} ", oTCResponseDTO);
                this.logMagensaException(e, registeredBy);
                return oTCResponseDTO;
            }
            swipeCardDetails_track1 = tracMap.get(TRACK_1);
            swipeCardDetails_track2 = tracMap.get(TRACK_2);
            //Read Track 1 Data first
            if(swipeCardDetails_track1 != null) {
                oTCTransaction = getCreditCardInfo(swipeCardDetails_track1, TRACK_1);
                paymentTxResDTO = this.paymentGateway.doSale(site, oTCResponseDTO.getTotalTxAmount(), swipeCardDetails_track1,
                       "doSaleOTC");
                if(paymentTxResDTO.getErrorCode() != null && swipeCardDetails_track2 != null ) {
                    oTCTransaction = getCreditCardInfo(swipeCardDetails_track2, TRACK_2);
                    paymentTxResDTO = this.paymentGateway.doSale(site, oTCResponseDTO.getTotalTxAmount(),
                        swipeCardDetails_track2, "doSaleOTC");
                }
            //Read Track 2 Data if Track 1 read Fails
            } else if(swipeCardDetails_track2 != null) {
                oTCTransaction = getCreditCardInfo(swipeCardDetails_track2, TRACK_2);
                paymentTxResDTO = this.paymentGateway.doSale(site, oTCResponseDTO.getTotalTxAmount(), swipeCardDetails_track2,
                    "doSaleOTC");
            }
        	oTCResponseDTO.setPayPalTxRefNum(paymentTxResDTO.getTxRefNum());
        	oTCResponseDTO.setAuthCode(paymentTxResDTO.getAuthCode());
            if (paymentTxResDTO.getErrorCode() == null) {
            	populateOTCTransaction(site, oTCTransaction, oTCRequestDTO, oTCResponseDTO, paymentTxResDTO, merchant);
	            this.oTCDAO.saveOTCTx(oTCTransaction);
            } else {
            	oTCResponseDTO.setErrorCode(paymentTxResDTO.getErrorCode());
            	oTCResponseDTO.setErrorDesc(paymentTxResDTO.getErrorDesc());
            }
        } catch (RuntimeException exception) {
        	logger.error(NOTIFY_ADMIN, "Error in doSaleOTC oTCRequestDTO {}", oTCRequestDTO, exception);
            logger.error("Refund Of Transaction oTCResponseDTO {}", oTCResponseDTO);
            this.handleOTCException(oTCRequestDTO, oTCResponseDTO, site, exception);
        } catch (Exception exception) {
            logger.error("Error in doSaleOTC oTCRequestDTO {}", oTCRequestDTO, exception);
            logger.error("Refund Of Transaction oTCResponseDTO {}", oTCResponseDTO);
            this.handleOTCException(oTCRequestDTO, oTCResponseDTO, site, exception);
        }
        return oTCResponseDTO;
    }

    /** This Method Is Used For Charging Over The Counter Transactions. This Method First Calculates The Service Fee
     *  And Then Determines Whether To Use Micro Or Normal Merchant To Charge and finally saves the OTC transaction.
     * @param otcRequestDTO contains siteName, baseAmount, track1, track2, MPrintStatus, Ksn and office location.
     * @return OTCResponseDTO If payment goes through transactionReferenceNumber is set in this DTO and returned and, if it
     * fails errorCode and errorDec is set and returned.
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public OTCResponseDTO doSaleOTCWithOutEncryption(OTCRequestDTO oTCRequestDTO) {

        Assert.hasLength(oTCRequestDTO.getSiteName(), "Site Name Cannot be Null/Empty");
        Assert.isTrue((oTCRequestDTO.getActualAmtToCharge() ==0), "Actual Amount To Be Charged Cannot be Zero");
        Assert.hasLength(oTCRequestDTO.getEncTrackOne(), "encTrackOne Cannot be Null/Empty");
        Assert.hasLength(oTCRequestDTO.getEncTrackTwo(), "encTrackTwo Cannot be Null/Empty");
        Assert.hasLength(oTCRequestDTO.getEncMp(), "encMp Cannot be Null/Empty");
        Assert.hasLength(oTCRequestDTO.getOfficeLoc(), "Office Location Cannot be Null/Empty");
        OTCTx oTCTransaction = null;
        OTCResponseDTO oTCResponseDTO = new OTCResponseDTO();
        Site site = null;
        Merchant merchant = null;
        PayPalDTO paymentTxResDTO = new PayPalDTO();
        try {
            site = this.eComService.getSiteDetailsBySiteName(oTCRequestDTO.getSiteName());
            oTCResponseDTO = calculateFeeOTC(oTCRequestDTO.getActualAmtToCharge() + oTCRequestDTO.getTax(), site);
            if ( oTCResponseDTO.getTotalTxAmount() < site.getCardUsageFee().getMicroTxFeeCutOff()
                    && site.isEnableMicroTxOTC()) {
                merchant = site.getMicroMerchant();
            } else {
                merchant = site.getMerchant();
            }
            //Read Track 1 Data first
            if(oTCRequestDTO.getEncTrackOne() != null) {
                oTCTransaction = getCreditCardInfo(oTCRequestDTO.getEncTrackOne(), TRACK_1);
                paymentTxResDTO = this.paymentGateway.doSale(site, oTCResponseDTO.getTotalTxAmount(),
                    oTCRequestDTO.getEncTrackOne(), "doSaleOTCWithOutEncryption");
                if(paymentTxResDTO.getErrorCode() != null && oTCRequestDTO.getEncTrackTwo() != null ) {
                    oTCTransaction = getCreditCardInfo(oTCRequestDTO.getEncTrackTwo(), TRACK_2);
                    paymentTxResDTO = this.paymentGateway.doSale(site, oTCResponseDTO.getTotalTxAmount(),
                        oTCRequestDTO.getEncTrackTwo(), "doSaleOTCWithOutEncryption");
                }
            //Read Track 2 Data if Track 1 read Fails
            } else if(oTCRequestDTO.getEncTrackTwo() != null) {
                oTCTransaction = getCreditCardInfo(oTCRequestDTO.getEncTrackTwo(), TRACK_2);
                paymentTxResDTO = this.paymentGateway.doSale(site, oTCResponseDTO.getTotalTxAmount(),
                    oTCRequestDTO.getEncTrackTwo(), "doSaleOTCWithOutEncryption");
            }
        	oTCResponseDTO.setPayPalTxRefNum(paymentTxResDTO.getTxRefNum());
        	oTCResponseDTO.setAuthCode(paymentTxResDTO.getAuthCode());

	        if (paymentTxResDTO.getErrorCode() == null) {
	        	populateOTCTransaction(site, oTCTransaction, oTCRequestDTO, oTCResponseDTO, paymentTxResDTO, merchant);
	            this.oTCDAO.saveOTCTx(oTCTransaction);
            } else {
            	oTCResponseDTO.setErrorCode(paymentTxResDTO.getErrorCode());
            	oTCResponseDTO.setErrorDesc(paymentTxResDTO.getErrorDesc());
            }

        } catch (Exception exception) {
            logger.error("Error in doSaleOTC", exception);
            this.handleOTCException(oTCRequestDTO, oTCResponseDTO, site, exception);
        }
        return oTCResponseDTO;
    }

    /** This Method Is Used For Getting The OTC Transaction Information of supplied Transaction Reference Number.
     * @param txRefNumber Transaction Reference Number.
     * @param siteName Name of the Site.
     * @return OTCResponseDTO
     */
    @Transactional(readOnly = true)
    public OTCTx getOTCTransactionByTxRefNum(String txRefNumber, String siteName) {
        Assert.hasLength(txRefNumber, "Tx Reference Number Cannot be Null/Empty");
        return this.oTCDAO.getOTCTxByTxRefNum(txRefNumber, siteName);
    }

    @Transactional(readOnly = true)
	public OTCTx getOTCTransactionByInvoiceNumber(String invoiceNumber, String siteName) {
    	Assert.hasLength(invoiceNumber, "Invoice Number Cannot be Null/Empty");
    	return this.oTCDAO.getOTCTransactionByInvoiceNumber(invoiceNumber, siteName);
	}

    /** This Method Is Used For Calculating The Service Fee For The Base Amount On A Given Site.
     * @param siteName Name of the Site.
     * @param actualAmtToCharge BaseAmount.
     * @return OTCTransaction
     */
    @Transactional(readOnly = true)
    public OTCResponseDTO doSaleGetInfoOTC(String siteName, double actualAmtToCharge) {
        Assert.hasLength(siteName, "Site Name Cannot be Null/Empty");
        Assert.isTrue((actualAmtToCharge > 0), "Actual Amount To Be Charged should be greater than Zero");
        Site site = null;
        OTCResponseDTO oTCResponseDTO = null;
        try {
            site = this.eComService.getSiteDetailsBySiteName(siteName);
            oTCResponseDTO = calculateFeeOTC(actualAmtToCharge, site);
        } catch (SDLBusinessException  sDLBusinessException) {
            oTCResponseDTO = new OTCResponseDTO();
            oTCResponseDTO.setErrorCode(sDLBusinessException.getMessage());
            oTCResponseDTO.setErrorDesc(sDLBusinessException.getMessage());
        }
        return oTCResponseDTO;
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
	public void archiveOTCTransactions(String archivedBy, String archiveComments) {
    	this.oTCDAO.archiveOTCTransactions(archivedBy, archiveComments);
	}


    /**  This Function Calls Magensa Web Service Method to decrypt swiped credit card details.
      *  Method uses Axis2 Data Binding (ADB) generated stub code.
      *  Stub Generation Method: WSDL2Java -uri magensa.wsdl -p org.apache.axis2.axis2userguide -d adb -s
      *  keytool -importkeystore -srckeystore Granicus_Prod_Feb2018.pfx -srcstoretype pkcs12 -destkeystore granicus_magensa_client.jks -deststoretype JKS
     */
    private Map<String, String> decrypt(String encTrackOne, String encTrackTwo, String encTrackThree, String encMp,
    		String ksn, String mPrintStatus, String cardType, String encryptionBlockType, String hostId, String hostPwd,
            String registeredBy, String outputFormatCode) throws MagensaException {
    	Map<String, String> tracMap = new HashMap<String, String>();
    	DecryptCardSwipe decryptCardSwipe = new DecryptCardSwipe();
		DecryptCardSwipeRequest decryptCardSwipeRequest = new DecryptCardSwipeRequest();
		EncryptedCardSwipe encryptedCardSwipe = new EncryptedCardSwipe();
		Authentication authentication = new Authentication();
        DecryptServiceStub magensaStub = null ;
        DecryptCardSwipeResponse0 decryptCardSwipeResponse = null;
        try {
        	if(StringUtils.isBlank(System.getProperty("javax.net.ssl.keyStore"))) {
        		logger.info("Intializing Keystores");        		
        		System.setProperty("javax.net.ssl.keyStore", this.magensaCertficatePath);
        		System.setProperty("javax.net.ssl.keyStoreType", "jks");
        		System.setProperty("javax.net.ssl.keyStorePassword", this.magensaCertficatePwd);
		   }
        	String keyStore = StringUtils.isBlank(System.getProperty("javax.net.ssl.keyStore")) == true ? " ": System.getProperty("javax.net.ssl.keyStore"); 
        	String keyStoreType = StringUtils.isBlank(System.getProperty("javax.net.ssl.keyStoreType")) == true ? " ": System.getProperty("javax.net.ssl.keyStoreType"); 
        	String keyStorePassword = StringUtils.isBlank(System.getProperty("javax.net.ssl.keyStorePassword")) == true ? " ": System.getProperty("javax.net.ssl.keyStorePassword"); 
        	logger.info("javax.net.ssl.keyStore: " + keyStore);
    		logger.info("javax.net.ssl.keyStoreType: " + keyStoreType);
    		logger.info("javax.net.ssl.keyStorePassword: " + keyStorePassword);
    		logger.info("Magensa WSDL: " + this.magensaWsdl);
        	magensaStub = new DecryptServiceStub(this.magensaWsdl);
            magensaStub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
            logger.error(NOTIFY_ADMIN, "Error in Communicating With Magensa", axisFault);
            MagensaException magensaException = new MagensaException(axisFault.getMessage());
            magensaException.setErrorCode("MALFORMED_MAGENSA_URL");
            throw magensaException;
        }
        encryptedCardSwipe.setTrack1(encTrackOne);
		encryptedCardSwipe.setTrack2(encTrackTwo);
		encryptedCardSwipe.setTrack3("D04D1707D07664377FD0F0094B11E26CF4C1B1D670C3696E3F0C14C34EBD3EBE32C602DE684336BCEE8C08CC1BF22862995D9F355573CAEA5FAA97B03F2307E0679FFF294B114FE4");
		encryptedCardSwipe.setMagnePrint(encMp);
		encryptedCardSwipe.setKSN(ksn);
		encryptedCardSwipe.setMagnePrintStatus(mPrintStatus);
		authentication.setUsername(hostId);
		authentication.setPassword(hostPwd);
		authentication.setCustomerCode("001000014");        
        try {                
    		decryptCardSwipeRequest.setEncryptedCardSwipe(encryptedCardSwipe);
    		decryptCardSwipeRequest.setAuthentication(authentication);
    		decryptCardSwipe.setRequest(decryptCardSwipeRequest);
    		decryptCardSwipeResponse =  magensaStub.decryptCardSwipe(decryptCardSwipe);
    	} catch (RemoteException remoteException) {
            remoteException.printStackTrace();
            logger.error("Error in decrypt function of Magensa", remoteException);
            MagensaException magensaException = new MagensaException(remoteException.getMessage());
            magensaException.setErrorCode("MALFORMED_MAGENSA_URL");
            throw magensaException;
        }
        if(decryptCardSwipeResponse.isDecryptCardSwipeResultSpecified()){
            tracMap.put(TRACK_1, decryptCardSwipeResponse.getDecryptCardSwipeResult().getDecryptedCardSwipe().getTrack1());
            tracMap.put(TRACK_2, decryptCardSwipeResponse.getDecryptCardSwipeResult().getDecryptedCardSwipe().getTrack2());
        } else {
            MagensaException magensaException = new MagensaException("Magensa Decryption Error");
            magensaException.setErrorCode("Magensa Decryption Error");
            throw magensaException;
        }
        return tracMap;
    }

    private OTCTx getCreditCardInfo(String swipeCardDetails, String tracType) throws SDLBusinessException {
    	logger.info("Entering getCreditCardInfo swipeCardDetails: {}, tracType: {}", swipeCardDetails, tracType);
        OTCTx oTCTransaction = new OTCTx();
        String[] track1Array = null;
        String accountNumber = null;
        String[] nameArray = null;
        String accountName = null;
        if(swipeCardDetails != null) {
            if(TRACK_1.equals(tracType)) {
                track1Array = StringUtils.split(swipeCardDetails, "^");
                if(track1Array.length > 1) {
	                if (track1Array[0].startsWith("%B")) {
	                	accountNumber = StringUtils.replace(track1Array[0], "%B", "");
	                	if(!StringUtils.isBlank(track1Array[1]) &&  track1Array[1].contains("/")) {
	                		if(track1Array[1].endsWith("/")) {
	                			accountName = StringUtils.replace(track1Array[1], "/", "").trim();
	                		} else {
		                		nameArray = StringUtils.split(track1Array[1], "/");
		                        accountName = nameArray[0] + " " + nameArray[1];
	                		}
	                	} else {
	                		 accountName = track1Array[1];
	                	}
	                } else {
	                	accountNumber = StringUtils.replace(track1Array[0], "%M", "");
	                	nameArray = StringUtils.split(track1Array[1], "/");
	                    accountName = nameArray[0];
	                }
                } else {
                	throw new SDLBusinessException(this.getMessage("otctx.creditcard.cardTypeNull"));
                }
            }
            if(TRACK_2.equals(tracType)) {
                if (TRACK_2.equals(tracType)) {
                    String modifiedTrack2 = StringUtils.replace(swipeCardDetails, ";", "");
                    String[] track2Array = StringUtils.split(modifiedTrack2, "=");
                    accountNumber = track2Array[0];
                }
             }

            CardType cardType = CreditCardUtil.getCardType(accountNumber);
            if(cardType == null){
            	throw new SDLBusinessException(this.getMessage("otctx.creditcard.cardTypeNull"));
            }
            oTCTransaction.setCardType(cardType);
            oTCTransaction.setCardNumber(accountNumber);
            if(StringUtils.isBlank(accountName)){
            	oTCTransaction.setAccountName("UNKNOWN");
            } else {
            	oTCTransaction.setAccountName(accountName);
            }
        }
        return oTCTransaction;
    }

    private OTCResponseDTO calculateFeeOTC(Double actualAmtToCharge, Site site) {
        Double serviceFee = 0.0d;
        CreditUsageFee cardUsageFee = site.getCardUsageFee();
        if (cardUsageFee != null) {
            if (actualAmtToCharge <= cardUsageFee.getFlatFeeCutOff()) {
                serviceFee = cardUsageFee.getFlatFee();
            } else if (actualAmtToCharge > cardUsageFee.getFlatFeeCutOff()) {
            	serviceFee = (actualAmtToCharge / 100.00) * cardUsageFee.getPercenteFee();
            }
        }
        serviceFee = serviceFee + cardUsageFee.getAdditionalFee();

        BigDecimal actualAmtToChargeBigDecimal = new BigDecimal(actualAmtToCharge).setScale(2, RoundingMode.HALF_DOWN);
        BigDecimal serviceFeeBigDecimal = new BigDecimal(serviceFee).setScale(2, RoundingMode.HALF_DOWN);
        BigDecimal totalAmountBigDecimal = actualAmtToChargeBigDecimal.add(serviceFeeBigDecimal);

        actualAmtToCharge = actualAmtToChargeBigDecimal.doubleValue();
        serviceFee = serviceFeeBigDecimal.doubleValue();
        Double totalAmount = totalAmountBigDecimal.doubleValue();

        OTCResponseDTO otcDTO = new OTCResponseDTO();
        otcDTO.setBaseAmount(actualAmtToCharge);
        otcDTO.setServiceFee(serviceFee);
        otcDTO.setTotalTxAmount(totalAmount);
        return otcDTO;
    }

    private OTCTx populateOTCTransaction(Site site, OTCTx oTCTransaction,
            OTCRequestDTO oTCRequestDTO, OTCResponseDTO oTCResponseDTO, PayPalDTO paymentTxResDTO,
                Merchant merchant) {
    	if (paymentTxResDTO.getErrorCode() == null) {
    		oTCTransaction.setSiteId(site.getId());

            if (oTCResponseDTO.getTotalTxAmount() < site.getCardUsageFee().getMicroTxFeeCutOff() && site.isEnableMicroTxOTC()) {
            	if (oTCTransaction.getCardType() == CardType.AMEX) {
                    oTCTransaction.setTxFeePercent(site.getMicroMerchant().getTxFeePercentAmex());
                    oTCTransaction.setTxFeeFlat(site.getMicroMerchant().getTxFeeFlatAmex());
                } else {
                    oTCTransaction.setTxFeePercent(site.getMicroMerchant().getTxFeePercent());
                    oTCTransaction.setTxFeeFlat(site.getMicroMerchant().getTxFeeFlat());
                }
            } else {
            	if (oTCTransaction.getCardType() == CardType.AMEX) {
                    oTCTransaction.setTxFeePercent(site.getMerchant().getTxFeePercentAmex());
                    oTCTransaction.setTxFeeFlat(site.getMerchant().getTxFeeFlatAmex());
                } else {
                    oTCTransaction.setTxFeePercent(site.getMerchant().getTxFeePercent());
                    oTCTransaction.setTxFeeFlat(site.getMerchant().getTxFeeFlat());
                }
            }
            oTCTransaction.setInvoiceNumber(oTCRequestDTO.getInvoiceNumber());
            oTCTransaction.setItemName(oTCRequestDTO.getItemName());
            oTCTransaction.setProductType(oTCRequestDTO.getProductType());
            oTCTransaction.setTxRefNum(paymentTxResDTO.getTxRefNum());
            oTCTransaction.setBaseAmount(oTCRequestDTO.getActualAmtToCharge());
            oTCTransaction.setComments(oTCRequestDTO.getComments());
            oTCTransaction.setAuthCode(paymentTxResDTO.getAuthCode());
            oTCTransaction.setServiceFee(oTCResponseDTO.getServiceFee());
            oTCTransaction.setTax(oTCRequestDTO.getTax());
            oTCTransaction.setTotalTxAmount(oTCResponseDTO.getTotalTxAmount());
            oTCTransaction.setTransactionType(TransactionType.CHARGE);
            oTCTransaction.setSettlementStatus(SettlementStatusType.UNSETTLED);
            oTCTransaction.setCheckNum(null);
            oTCTransaction.setMachineName(oTCRequestDTO.getMachineName());
            oTCTransaction.setTransactionDate(SystemUtil.changeTimeZone(new Date(), TimeZone.getTimeZone(site.getTimeZone())));
            oTCTransaction.setOrigTxRefNum(null);
            oTCTransaction.setModifiedBy(oTCRequestDTO.getUserLogged());
            oTCTransaction.setCreatedBy(oTCRequestDTO.getUserLogged());
            oTCTransaction.setCreatedDate(new Date());
            oTCTransaction.setModifiedDate(new Date());
            oTCTransaction.setActive(true);
            oTCTransaction.setMerchantId(merchant.getId());
            oTCTransaction.setSignature(oTCRequestDTO.getSignature());
            oTCTransaction.setOfficeLoc(oTCRequestDTO.getOfficeLoc());
            oTCTransaction.setOfficeLocAddressLine1(oTCRequestDTO.getOfficeLocAdr1());
            oTCTransaction.setOfficeLocAddressLine2(oTCRequestDTO.getOfficeLocAdr2());
            oTCTransaction.setOfficeLocCity(oTCRequestDTO.getOfficeLocCity());
            oTCTransaction.setOfficeLocState(oTCRequestDTO.getOfficeLocState());
            oTCTransaction.setOfficeLocZip(oTCRequestDTO.getOfficeLocZip());
            oTCTransaction.setOfficeLocPhone(oTCRequestDTO.getOfficeLocPhone());
            oTCTransaction.setOfficeLocComments1(oTCRequestDTO.getOfficeLocComments1());
            oTCTransaction.setOfficeLocComments2(oTCRequestDTO.getOfficeLocComments2());

    	}
    	return oTCTransaction;

    }

    private void handleOTCException(OTCRequestDTO oTCRequestDTO, OTCResponseDTO oTCResponseDTO, Site site,
            Exception exception) {
        if (oTCResponseDTO.getPayPalTxRefNum() != null) {
            try {
                this.paymentGateway.doReferenceCredit(site, oTCResponseDTO.getPayPalTxRefNum(), "OTC", "doSaleOTC",
                    oTCRequestDTO.getUserLogged());
            } catch (Exception ex) {
                logger.error(NOTIFY_ADMIN, "Error in Refunding the Money Back when ther is an Exception in OTCdoSaleOTC",
                    exception);
            }
        }
        if (oTCResponseDTO.getErrorCode() == null) {
            oTCResponseDTO.setErrorCode(exception.getMessage());
        }
        if (oTCResponseDTO.getErrorDesc() == null) {
            if (!StringUtils.isBlank(exception.getLocalizedMessage())) {
                  oTCResponseDTO.setErrorDesc(exception.getLocalizedMessage());
            } else {
                  oTCResponseDTO.setErrorDesc(exception.getMessage());
            }
        }
    }

    /**
     * This Method logs PayPal System Exception. It sends an Email to the ACCEPT Admin when there is a
     * PaymentGatewaySystemException
     *
     * @param payPalResponse PayPal Response
     * @param moduleName The Module that is calling.
     * @param payPalFunctionName The Payment Gateway Function
     * @param userName The User Name
     * @param payPalProfileId The PayPal Profiel Id
     * @throws PaymentGatewaySystemException PayMent Gateway System Exception
     */
    private void logMagensaException(MagensaException magensaException, String userName) throws MagensaException {
        logger.debug("COMING INSIDE logMagensaException FOR userName: {}", userName);
        ErrorCode errorCode = new ErrorCode();
        errorCode.setCode(magensaException.getErrorCode());
        errorCode.setDescription(magensaException.getDescription());
        errorCode.setUserName(userName);
        errorCode.setModuleName("doSaleOTC");
        errorCode.setFunctionName("doSaleOTC");
        errorCode.setModifiedBy("SYSTEM");
        errorCode.setCreatedBy("SYSTEM");
        errorCode.setCreatedDate(new Date());
        errorCode.setModifiedDate(new Date());
        errorCode.setUserException(true);
        this.eComDAO.saveErrorCode(errorCode);
        throw magensaException;
    }

    private String getMessage(String messageKey, Object[] object) {
        return this.messages.getMessage(messageKey, object, new Locale("en"));
    }

    private String getMessage(String messageKey) {
        return this.messages.getMessage(messageKey, null, new Locale("en"));
    }

}