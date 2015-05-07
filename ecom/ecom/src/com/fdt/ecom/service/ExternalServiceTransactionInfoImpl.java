package com.fdt.ecom.service;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;
import static javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.jws.WebService;
import javax.xml.ws.BindingType;

import org.apache.cxf.annotations.WSDLDocumentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fdt.ecom.entity.Merchant;
import com.fdt.otctx.entity.OTCTx;
import com.fdt.otctx.service.OTCTXService;
import com.fdt.webtx.dto.WebTxExtResponseDTO;
import com.fdt.webtx.entity.WebTx;
import com.fdt.webtx.service.WebTxService;

@WebService(endpointInterface = "com.fdt.ecom.service.ExternalServiceTransactionInfo", serviceName ="ExternalServiceTransactionInfo")
@BindingType(value = SOAP11HTTP_BINDING)
@WSDLDocumentation(value="ACCEPT SOAP 1.1 Services for External Applications", placement = WSDLDocumentation.Placement.TOP)
public class ExternalServiceTransactionInfoImpl implements ExternalServiceTransactionInfo {

    private static final Logger logger = LoggerFactory.getLogger(ExternalServiceTransactionInfoImpl.class);

    @Autowired
    private OTCTXService oTCTXService = null;

    @Autowired
    private WebTxService webTxService = null;

    @Autowired
    private EComService ecomService = null;

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