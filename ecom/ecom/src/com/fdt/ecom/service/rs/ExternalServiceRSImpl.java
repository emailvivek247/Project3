package com.fdt.ecom.service.rs;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;

import java.util.Date;

import javax.jws.WebMethod;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import com.fdt.common.exception.SDLBusinessException;
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

public class ExternalServiceRSImpl implements ExternalServiceRS {

	 private static final Logger logger = LoggerFactory.getLogger(ExternalServiceRSImpl.class);

	@Autowired
    private OTCTXService oTCTXService = null;

    @Autowired
    private WebTxService webTXService = null;

	@WebMethod
    @POST
    @Path("doSaleOTC")
    @Produces({MediaType.APPLICATION_JSON})
    public OTCResponseDTO doSaleOTC(@RequestBody OTCRequestDTO oTCRequestDTO) {
        try {
            return this.oTCTXService.doSaleOTC(oTCRequestDTO);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in doSaleOTC for oTCRequestDTO {}", oTCRequestDTO, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("doSaleOTCWithOutEncryption")
    @Produces({MediaType.APPLICATION_JSON})
    public OTCResponseDTO doSaleOTCWithOutEncryption(@RequestBody OTCRequestDTO oTCRequestDTO) {
        try {
            return this.oTCTXService.doSaleOTCWithOutEncryption(oTCRequestDTO);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in doSaleOTCWithOutEncryption for oTCRequestDTO {}", oTCRequestDTO,
            		runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("doSaleGetInfoOTC/{siteName}/{actualAmtToCharge}")
    @Produces({MediaType.APPLICATION_JSON})
    public OTCResponseDTO doSaleGetInfoOTC(@PathParam("siteName") String siteName,
            @PathParam("actualAmtToCharge") double actualAmtToCharge) {
        try {
            return this.oTCTXService.doSaleGetInfoOTC(siteName, actualAmtToCharge);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in doSaleGetInfoOTC for siteName {} actualAmtToCharge {}", siteName,
                actualAmtToCharge, runtimeException);
            throw runtimeException;
        }
    }
    @WebMethod
    @GET
    @Path("getOTCTransactionByTxRefNum")
    @Produces({MediaType.APPLICATION_JSON})
    public OTCTx getOTCTransactionByTxRefNum(@QueryParam("txRefNumber") String txRefNumber,
    		@QueryParam("siteName") String siteName) {
        try {
            return this.oTCTXService.getOTCTransactionByTxRefNum(txRefNumber, siteName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getOTCTransactionByTxRefNum for txRefNumber {}, siteName {}", txRefNumber,
                siteName, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("doReferenceCredit/{paymentType}")
    @Produces({MediaType.APPLICATION_JSON})
    public PayPalDTO doReferenceCredit(@QueryParam("txRefNumber") String txRefNumber,
    		@QueryParam("comments") String comments, @QueryParam("modUserId")String modUserId,
    		@QueryParam("machineName") String machineName, @QueryParam("siteName") String siteName,
    		@PathParam("paymentType") PaymentType paymentType) {
        PayPalDTO paymentTxResponseDTO = null;
        try {
            if (paymentType == PaymentType.OTC) {
                paymentTxResponseDTO = this.oTCTXService.doReferenceCreditOTC(txRefNumber, comments,
                    modUserId, machineName, siteName);
            } else if (paymentType == PaymentType.WEB) {
                paymentTxResponseDTO = this.webTXService.doReferenceCreditWeb(txRefNumber, comments, modUserId, machineName,
                		siteName);
            } else {
                paymentTxResponseDTO =  new PayPalDTO();
                paymentTxResponseDTO.setErrorCode("INVALID APPLICATION TYPE");
                paymentTxResponseDTO.setErrorDesc("PLEASE CONTACT SYSTEM ADMINISTRATOR FOR THE VALID APPLICATION TYPE");
            }
        } catch (SDLBusinessException sdlBusinessException) {
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
                "machineName {} siteName {}, paymentType{}", txRefNumber, comments, modUserId, machineName, siteName,
                	paymentType, runtimeException);
            throw runtimeException;
        }
        return paymentTxResponseDTO;
    }

    /**This is used by external APPS like CMS**/
    @WebMethod
    @GET
    @Path("getWebTransactionsForExtApp/{siteName}/{fromDate}/{endDate}/{txType}")
    @Produces({MediaType.APPLICATION_JSON})
    public WebTxExtResponseDTO getWebTransactionsForExtApp(@PathParam("siteName") String siteName,
            @PathParam("fromDate") Date fromDate, @PathParam("endDate") Date endDate,
            @PathParam("txType") String txType) {
        try {
            return this.webTXService.getWebTransactionsForExtApp(siteName, fromDate, endDate, txType);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getWebTransactionsForExtApp for siteName {}, fromDate {}, endDate {}, ",
                    "txType {}", siteName, fromDate, endDate, txType, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
	@GET
    @Path("getWebTxByTxRefNum/{txRefNumber}/{siteName}")
    @Produces({MediaType.APPLICATION_JSON})
	public WebTx getWebTxByTxRefNum(@PathParam("txRefNumber") String txRefNumber,
			@PathParam("siteName") String siteName) {
		try {
			return this.webTXService.getWebTransactionByTxRefNum(txRefNumber, siteName);
		} catch (RuntimeException runtimeException) {
			logger.error(NOTIFY_ADMIN, "Error in getWebTransactionByTxRefNum for txRefNumber {}, siteName {} ",
					txRefNumber, siteName, runtimeException);
			throw runtimeException;
		}
	}

}
