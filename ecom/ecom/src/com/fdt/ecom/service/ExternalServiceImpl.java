package com.fdt.ecom.service;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;
import static javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.jws.WebService;
import javax.xml.ws.BindingType;

import org.apache.cxf.annotations.WSDLDocumentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.ecom.entity.Merchant;
import com.fdt.ecom.entity.enums.PaymentType;
import com.fdt.otctx.dto.OTCRequestDTO;
import com.fdt.otctx.dto.OTCResponseDTO;
import com.fdt.otctx.entity.OTCTx;
import com.fdt.otctx.service.OTCTXService;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.webtx.dto.WebTxExtResponseDTO;
import com.fdt.webtx.entity.WebTx;
import com.fdt.webtx.service.WebTxService;

@WebService(endpointInterface = "com.fdt.ecom.service.ExternalService", serviceName ="ExternalService")
@BindingType(value = SOAP12HTTP_BINDING)
@WSDLDocumentation(value="ACCEPT SOAP 1.2 Services for External Applications", placement = WSDLDocumentation.Placement.TOP)
public class ExternalServiceImpl implements ExternalService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalServiceImpl.class);

    @Autowired
    private OTCTXService oTCTXService = null;

    @Autowired
    private WebTxService webTxService = null;

    @Autowired
    private EComService ecomService = null;

    public OTCResponseDTO doSaleOTC(OTCRequestDTO otcRequestDTO) {
        try {
        	logger.debug("Entering doSaleOTC: " + otcRequestDTO);
            return this.oTCTXService.doSaleOTC(otcRequestDTO);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in doSaleOTC for oTCRequestDTO {}", otcRequestDTO, runtimeException);
            throw runtimeException;
        }
    }

    public OTCResponseDTO doSaleOTCWithOutEncryption(OTCRequestDTO otcRequestDTO) {
        try {
        	logger.debug("Entering doSaleOTCWithOutEncryption: " + otcRequestDTO);
            return this.oTCTXService.doSaleOTCWithOutEncryption(otcRequestDTO);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in doSaleOTCWithOutEncryption for oTCRequestDTO {}", otcRequestDTO,
            runtimeException);
            throw runtimeException;
        }
    }

    public OTCTx getOTCTransactionByTxRefNum(String txRefNumber, String siteName) {
        try {
        	logger.debug("Entering getOTCTransactionByTxRefNum: txRefNumber {}, siteName {}", txRefNumber, siteName);
            return this.oTCTXService.getOTCTransactionByTxRefNum(txRefNumber, siteName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getOTCTransactionByTxRefNum for txRefNumber {}, siteName {}", txRefNumber,
                siteName, runtimeException);
            throw runtimeException;
        }
    }

    public OTCTx getOTCTransactionByInvoiceNumber(String invoiceNumber, String siteName) {
    	try {
        	logger.debug("Entering getOTCTransactionByInvoiceNumber for invoiceNumber {}, siteName {}", invoiceNumber, siteName);
            return this.oTCTXService.getOTCTransactionByInvoiceNumber(invoiceNumber, siteName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getOTCTransactionByInvoiceNumber for invoiceNumber {}, siteName {}",
            		invoiceNumber, siteName, runtimeException);
            throw runtimeException;
        }
	}

    public OTCResponseDTO doSaleGetInfoOTC(String siteName, double actualAmtToCharge) {
        try {
        	logger.debug("Entering doSaleGetInfoOTC: siteName {}, actualAmtToCharge {}", siteName, actualAmtToCharge);
            return this.oTCTXService.doSaleGetInfoOTC(siteName, actualAmtToCharge);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in doSaleGetInfoOTC for siteName {} actualAmtToCharge {}", siteName,
                actualAmtToCharge, runtimeException);
            throw runtimeException;
        }
    }

    public PayPalDTO doReferenceCredit(String txRefNumber, String comments, String modUserId, String machineName,
    		String siteName) {
        PayPalDTO paymentTxResponseDTO = null;
        try {
        	logger.debug("Entering doReferenceCredit: txRefNumber {}, comments {} modUserId {}, machineName {} " +
        			"siteName {}", txRefNumber, comments, modUserId, machineName, siteName);
        	PaymentType paymentType = this.ecomService.getPaymentTypeForTransaction(txRefNumber);
            if (paymentType == PaymentType.OTC) {
                paymentTxResponseDTO = this.oTCTXService.doReferenceCreditOTC(txRefNumber, comments, modUserId, machineName,
                		siteName);
            } else if (paymentType == PaymentType.WEB) {
                    paymentTxResponseDTO = this.webTxService.doReferenceCreditWeb(txRefNumber, comments, modUserId,
                    		machineName, siteName);
            } else {
                paymentTxResponseDTO =  new PayPalDTO();
                paymentTxResponseDTO.setErrorCode("INVALID TRANSACTION REFERENCE NUMBER");
                paymentTxResponseDTO.setErrorDesc("PLEASE CONTACT SYSTEM ADMINISTRATOR FOR THE VALID APPLICATION TYPE");
            }
        } catch(SDLBusinessException sdlBusinessException) {
            paymentTxResponseDTO =  new PayPalDTO();
            paymentTxResponseDTO.setErrorCode(sdlBusinessException.getErrorCode());
            paymentTxResponseDTO.setErrorDesc(sdlBusinessException.getBusinessMessage());
        } catch (PaymentGatewayUserException paymentGatewayUserException) {
            paymentTxResponseDTO =  new PayPalDTO();
            paymentTxResponseDTO.setErrorCode(paymentGatewayUserException.getErrorCode());
            paymentTxResponseDTO.setErrorDesc(paymentGatewayUserException.getMessage());
        } catch (PaymentGatewaySystemException paymentGatewaySystemException) {
            paymentTxResponseDTO =  new PayPalDTO();
            paymentTxResponseDTO.setErrorCode(paymentGatewaySystemException.getErrorCode());
            paymentTxResponseDTO.setErrorDesc(paymentGatewaySystemException.getMessage());
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in doReferenceCredit for txRefNumber {}, comments {}, modUserId {}," +
                "machineName {} siteName {}", txRefNumber, comments, modUserId, machineName, siteName,
                	runtimeException);
            throw runtimeException;
        }
        return paymentTxResponseDTO;
    }

    /**This is used by external APPS like CMS**/
    public WebTxExtResponseDTO getWebTransactionsForExtApp(String siteName, Date fromDate, Date endDate,
            String txType) {
        try {
        	logger.debug("Entering getWebTransactionsForExtApp: siteName {}, fromDate {} endDate {} txType {}",
        			siteName, fromDate, endDate, txType);
            return this.webTxService.getWebTransactionsForExtApp(siteName, fromDate, endDate, txType);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getWebTransactionsForExtApp for siteName {}, fromDate {}, " +
            		"endDate {}, txType {}", siteName, fromDate, endDate, txType, runtimeException);
            throw runtimeException;
        }
    }

	public WebTx getWebTxByTxRefNum(String txRefNumber, String siteName) {
		try {
			logger.debug("Entering getWebTxByTxRefNum: txRefNumber {}, siteName {}", txRefNumber, siteName);
			WebTx webTx = this.webTxService.getWebTransactionByTxRefNum(txRefNumber, siteName);
			if(webTx==null) {
				return null;
			} else if(webTx.getSite() == null) {
				return null;
			} else if (webTx.getSite().getMerchant() == null){
				return null;
			}
			Merchant merchant = webTx.getSite().getMerchant();
			merchant.setPartner("MERCHANT_PARTNER");
			merchant.setVendorName("MERCHANT_VENDOR");
			merchant.setPassword("MERCHANT_PASSWORD");
			merchant.setUserName("MERCHANT_USERNAME");
			List<Merchant> merchantList = new LinkedList<Merchant>();
			merchantList.add(merchant);
			webTx.getSite().setMerchantList(merchantList);
			return webTx;
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getWebTransactionByTxRefNum for txRefNumber {}, siteName {} ", txRefNumber,
            		siteName, runtimeException);
            throw runtimeException;
        }
	}

	public WebTx getWebTxByInvoiceNumber(String invoiceNumber, String siteName) {
		try {
			logger.debug("Entering getWebTxByInvoiceNumber: invoiceNumber {}, siteName {}", invoiceNumber, siteName);
			WebTx webTx = this.webTxService.getWebTxByInvoiceNumber(invoiceNumber, siteName);
			if(webTx==null) {
				return null;
			} else if(webTx.getSite() == null) {
				return null;
			} else if (webTx.getSite().getMerchant() == null){
				return null;
			}
            Merchant merchant = webTx.getSite().getMerchant();
			merchant.setPartner("MERCHANT_PARTNER");
			merchant.setVendorName("MERCHANT_VENDOR");
			merchant.setPassword("MERCHANT_PASSWORD");
			merchant.setUserName("MERCHANT_USERNAME");
			List<Merchant> merchantList = new LinkedList<Merchant>();
			merchantList.add(merchant);
			webTx.getSite().setMerchantList(merchantList);
			return webTx;
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getWebTxByInvoiceId for invoiceNumber {}, siteName {} ", invoiceNumber,
            		siteName, runtimeException);
            throw runtimeException;
        }
	}





}