package com.fdt.ecom.service.rs;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;

import java.util.List;

import javax.jws.WebMethod;
import javax.ws.rs.DELETE;
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

import com.fdt.achtx.dto.ACHTxDTO;
import com.fdt.achtx.entity.CheckHistory;
import com.fdt.achtx.service.ACHTxService;
import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.exception.SDLException;
import com.fdt.common.util.SystemUtil;
import com.fdt.ecom.dto.UserAccessDetailDTO;
import com.fdt.ecom.dto.UserCountDTO;
import com.fdt.ecom.entity.BankDetails;
import com.fdt.ecom.entity.Location;
import com.fdt.ecom.entity.NodeConfiguration;
import com.fdt.ecom.entity.ReceiptConfiguration;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.SiteConfiguration;
import com.fdt.ecom.entity.enums.PaymentType;
import com.fdt.ecom.service.EComService;
import com.fdt.otctx.entity.OTCTx;
import com.fdt.otctx.service.OTCTXService;
import com.fdt.otctx.service.admin.OTCTXAdminService;
import com.fdt.payasugotx.entity.PayAsUGoTx;
import com.fdt.payasugotx.service.PayAsUGoTxService;
import com.fdt.payasugotx.service.admin.PayAsUGoTxAdminService;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.recurtx.entity.RecurTx;
import com.fdt.recurtx.service.RecurTxService;
import com.fdt.recurtx.service.admin.RecurTxAdminService;
import com.fdt.security.dto.FirmUserDTO;
import com.fdt.security.dto.SearchCriteriaDTO;
import com.fdt.security.entity.User;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.security.service.admin.UserAdminService;
import com.fdt.subscriptions.dto.SubscriptionDTO;
import com.fdt.subscriptions.service.SubService;
import com.fdt.webtx.entity.WebTx;
import com.fdt.webtx.service.WebTxService;
import com.fdt.webtx.service.admin.WebTXAdminService;


public class EComAdminFacadeServiceRSImpl implements EComAdminFacadeServiceRS {

    private static final Logger logger = LoggerFactory.getLogger(EComAdminFacadeServiceRSImpl.class);

    @Autowired
    private UserAdminService userAdminService = null;

    @Autowired
    private EComService ecomService = null;

    @Autowired
    private OTCTXService oTCTXService = null;

    @Autowired
    private OTCTXAdminService oTCTXAdminService = null;

    @Autowired
    private RecurTxService recurSubService = null;

    @Autowired
    private RecurTxAdminService recurSubAdminService = null;

    @Autowired
    private PayAsUGoTxService payAsUGoSubService = null;

    @Autowired
    private PayAsUGoTxAdminService payAsUGoSubAdminService = null;

    @Autowired
    private WebTxService webTXService = null;

    @Autowired
    private WebTXAdminService webTXAdminService = null;

    @Autowired
    private ACHTxService aCHTXService = null;

    @Autowired
    private SubService subService = null;

    @WebMethod
    @GET @Path("getCacheNames")
    @Produces({MediaType.APPLICATION_JSON})
    public List<String> getCacheNames() {
        try {
            return this.ecomService.getCacheNames();
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getCacheNames", runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET @Path("refreshCacheByName/{cacheName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Void refreshCacheByName(@PathParam("cacheName") String cacheName) {
        try {
            this.ecomService.refreshCacheByName(cacheName);
            return null;
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in refreshCacheByName for Cache {}" ,cacheName, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET @Path("refreshCache")
    @Produces({MediaType.APPLICATION_JSON})
    public Void refreshCache() {
        try {
            this.ecomService.refreshCache();
            return null;
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in refreshCache", runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET @Path("getUserDetailsForAdmin/{userName}")
    @Produces({MediaType.APPLICATION_JSON})
    public User getUserDetailsForAdmin(@PathParam("userName") String userName) throws UserNameNotFoundException {
        User user = null;
        try {
            user = this.userAdminService.getUserDetailsForAdmin(userName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getUserDetailsForAdmin for userName {} ", userName, runtimeException);
            throw runtimeException;
        }
        return user;
    }

    @WebMethod
    @GET @Path("getSites")
    @Produces({MediaType.APPLICATION_JSON})
    public List<Site> getSites() {
        List<Site> sites = null;
        try {
            sites = this.ecomService.getSites();
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getSites", runtimeException);
            throw runtimeException;
        }
        return sites;
    }

    @WebMethod
    @POST @Path("authorize/{userAccessId}/{isAuthorized}/{modifiedBy}")
    @Produces({MediaType.APPLICATION_JSON})
    public Void authorize(@PathParam("userAccessId") Long userAccessId,
                          @PathParam("isAuthorized") boolean isAuthorized,
                          @PathParam("modifiedBy")   String modifiedBy) {
        try {
            this.subService.authorize(userAccessId, isAuthorized, modifiedBy);
            return null;
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in authorize method for userAccessId {} isAuthorized {} modifiedBy {}",
                    userAccessId,
                isAuthorized, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST @Path("enableDisableUserAccess/{userAccessId}/{isEnable}/{modifiedBy}/{comments}/{isAccessOverridden}")
    @Produces({MediaType.APPLICATION_JSON})
    public Void enableDisableUserAccess(@PathParam("userAccessId")       Long userAccessId,
                                        @PathParam("isEnable")           boolean isEnable,
                                        @PathParam("modifiedBy")         String modifiedBy,
                                        @PathParam("comments")           String comments,
                                        @PathParam("isAccessOverridden") boolean isAccessOverridden,
                                        @QueryParam("endDate") String endDate) {
        try {
            comments = SystemUtil.decodeURL(comments);
            endDate = SystemUtil.decodeURL(endDate);
            this.userAdminService.enableDisableUserAccess(userAccessId, isEnable, modifiedBy, comments, isAccessOverridden, endDate);
            return null;
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in enableDisableUserAccess for userAccessId {} isEnable {} modifiedBy {}" +
                "comments {} isAccessOverridden {}", userAccessId, isEnable, modifiedBy, comments,
                isAccessOverridden, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST @Path("removeSubscription/{userName}/{userAccessId}/{modifiedBy}/{comments}/{isSendEmail}")
    @Produces({MediaType.APPLICATION_JSON})
    public PayPalDTO removeSubscription(@PathParam("userName") String userName,
                                        @PathParam("userAccessId") Long userAccessId,
                                        @PathParam("modifiedBy") String modifiedBy,
                                        @PathParam("comments") String comments,
                                        @PathParam("isSendEmail") boolean isSendEmail)
            throws PaymentGatewaySystemException, SDLBusinessException {
        PayPalDTO payPalDTO = null;
        try {
            comments = SystemUtil.decodeURL(comments);
            payPalDTO = this.subService.removeSub(userName, userAccessId, modifiedBy, comments, isSendEmail);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in removeSubscription for userName {} userAccessId {} modifiedBy {} " +
                "comments {} isSendEmail {}", userName, userAccessId, modifiedBy, comments, isSendEmail, runtimeException);
            throw runtimeException;
        }
        return payPalDTO;
    }

    @WebMethod
    @GET @Path("getNodeConfiguration/{nodeName}")
    @Produces({MediaType.APPLICATION_JSON})
    public NodeConfiguration getNodeConfiguration(@PathParam("nodeName") String nodeName) {
        NodeConfiguration nodeConfiguration = null;
        try {
            nodeConfiguration = this.ecomService.getNodeConfiguration(nodeName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getNodeConfiguration for Node Name {}", nodeName, runtimeException);
            throw runtimeException;
        }
        return nodeConfiguration;
    }

    @WebMethod
    @GET @Path("getSiteConfiguration/{siteId}")
    @Produces({MediaType.APPLICATION_JSON})
    public SiteConfiguration getSiteConfiguration(@PathParam("siteId") Long siteId) {
        SiteConfiguration siteConfiguration = null;
        try {
            siteConfiguration = this.ecomService.getSiteConfiguration(siteId);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getSiteConfiguration for Site Id {}", siteId, runtimeException);
            throw runtimeException;
        }
        return siteConfiguration;
    }

    @WebMethod
    @POST @Path("updateNodeConfiguration")
    @Produces({MediaType.APPLICATION_JSON})
    public Void updateNodeConfiguration(NodeConfiguration nodeConfiguration) {
        try {
            this.ecomService.updateNodeConfiguration(nodeConfiguration);
            return null;
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in updateNodeConfiguration for nodeConfiguration {}",
                    nodeConfiguration, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST @Path("updateSiteConfiguration")
    @Produces({MediaType.APPLICATION_JSON})
    public Void updateSiteConfiguration(SiteConfiguration siteConfiguration) {
        try {
            this.ecomService.updateSiteConfiguration(siteConfiguration);
            return null;
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in updateSiteConfiguration for Site {}", siteConfiguration, runtimeException);
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
            } else if (paymentType == PaymentType.PAYASUGO) {
                paymentTxResponseDTO = this.payAsUGoSubService.doReferenceCreditPayAsUGo(txRefNumber, comments, modUserId,
                	machineName, siteName);
            } else if (paymentType == PaymentType.WEB) {
                paymentTxResponseDTO = this.webTXService.doReferenceCreditWeb(txRefNumber, comments, modUserId, machineName,
                	siteName);
            } else if (paymentType == PaymentType.RECURRING) {
                paymentTxResponseDTO = this.recurSubAdminService.doReferenceCreditRecurTx(txRefNumber, comments,
                	modUserId, machineName, siteName);
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

    @WebMethod
    @POST @Path("doPartialReferenceCreditPayAsUGo")
    @Produces({MediaType.APPLICATION_JSON})
    public PayPalDTO doPartialReferenceCreditPayAsUGo(@QueryParam("webTxItemId") Long webTxItemId,
                                                 @QueryParam("siteName")    String siteName,
                                                 @QueryParam("comments")    String comments,
                                                 @QueryParam("modUserId")   String modUserId,
                                                 @QueryParam("machineName") String machineName)
            throws PaymentGatewayUserException, PaymentGatewaySystemException, SDLBusinessException {
        PayPalDTO paymentTxResponseDTO = null;
        try {
            comments = SystemUtil.decodeURL(comments);
            paymentTxResponseDTO = this.payAsUGoSubAdminService.doPartialReferenceCreditPayAsUGo(webTxItemId, siteName,
                    comments, modUserId, machineName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in doPartialReferenceCreditPayAsUGo webTxItemId{}, siteName{}, " +
                "comments{}, modUserId{}, machineName{}", webTxItemId, siteName, comments, modUserId, machineName,
                    runtimeException);
            throw runtimeException;
        }
        return paymentTxResponseDTO;
    }

    @WebMethod
    @POST @Path("doPartialReferenceCreditWeb")
    @Produces({MediaType.APPLICATION_JSON})
    public PayPalDTO doPartialReferenceCreditWeb(@QueryParam("webTxItemId") Long webTxItemId,
                                                 @QueryParam("siteName")    String siteName,
                                                 @QueryParam("comments")    String comments,
                                                 @QueryParam("modUserId")   String modUserId,
                                                 @QueryParam("machineName") String machineName)
            throws PaymentGatewayUserException, PaymentGatewaySystemException, SDLBusinessException {
        PayPalDTO paymentTxResponseDTO = null;
        try {
            comments = SystemUtil.decodeURL(comments);
            paymentTxResponseDTO = this.webTXAdminService.doPartialReferenceCreditWeb(webTxItemId, siteName,
                    comments, modUserId, machineName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in doPartialReferenceCreditWeb webTxItemId{}, siteName{}, " +
                "comments{}, modUserId{}, machineName{}", webTxItemId, siteName, comments, modUserId, machineName,
                    runtimeException);
            throw runtimeException;
        }
        return paymentTxResponseDTO;
    }

    @WebMethod
    @GET @Path("getPayAsUGoTransactionByTxRefNum")
    @Produces({MediaType.APPLICATION_JSON})
    public PayAsUGoTx getPayAsUGoTxByTxRefNum(@QueryParam("txRefNumber") String txRefNumber,
                                                      @QueryParam("siteName")    String siteName) {
        PayAsUGoTx payAsUGoTransaction = null;
        try {
        	payAsUGoTransaction = this.payAsUGoSubService.getPayAsUGoTxByTxRefNum(txRefNumber, siteName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getPayAsUGoTransactionByTxRefNum txRefNumber {}, siteName {} ",
                txRefNumber, siteName, runtimeException);
            throw runtimeException;
        }
        return payAsUGoTransaction;
    }

    @WebMethod
    @GET @Path("getWebTxByTxRefNum")
    @Produces({MediaType.APPLICATION_JSON})
    public WebTx getWebTxByTxRefNum(@QueryParam("txRefNumber") String txRefNumber,
                                                      @QueryParam("siteName")    String siteName) {
    	WebTx webTransaction = null;
        try {
        	webTransaction = this.webTXService.getWebTransactionByTxRefNum(txRefNumber, siteName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getWebTransactionByTxRefNum txRefNumber {}, siteName {} ",
                txRefNumber, siteName, runtimeException);
            throw runtimeException;
        }
        return webTransaction;
    }

    @WebMethod
    @GET @Path("getPayAsUGoTransactionItemByItemId")
    @Produces({MediaType.APPLICATION_JSON})
    public PayAsUGoTx getPayAsUGoTxItemByItemId(@QueryParam("itemId") Long itemId,
                                                        @QueryParam("siteName") String siteName) {
        PayAsUGoTx payAsUGoTransaction = null;
        try {
        	payAsUGoTransaction = this.payAsUGoSubAdminService.getPayAsUGoTransactionItemByItemId(itemId, siteName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getPayAsUGoTxItemByItemId itemId {}, siteName{} ",
                itemId, siteName, runtimeException);
            throw runtimeException;
        }
        return payAsUGoTransaction;
    }

    @WebMethod
    @GET @Path("getWebTransactionItemByItemId")
    @Produces({MediaType.APPLICATION_JSON})
    public WebTx getWebTxItemByItemId(@QueryParam("itemId") Long itemId,
                                                        @QueryParam("siteName") String siteName) {
    	WebTx webTransaction = null;
        try {
        	webTransaction = this.webTXAdminService.getWebTransactionItemByItemId(itemId, siteName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getWebTxItemByItemId itemId {}, siteName{} ",
                itemId, siteName, runtimeException);
            throw runtimeException;
        }
        return webTransaction;
    }


    @WebMethod
    @GET @Path("getReferencedPayAsUGoTransactionItemByItemId")
    @Produces({MediaType.APPLICATION_JSON})
    public PayAsUGoTx getReferencedPayAsUGoTransactionItemByItemId(@QueryParam("itemId") Long itemId,
                                                                  @QueryParam("siteName") String siteName) {
        PayAsUGoTx payAsUGoTransaction = null;
        try {
        	payAsUGoTransaction = this.payAsUGoSubAdminService.getReferencedPayAsUGoTransactionItemByItemId(itemId, siteName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getReferencedPayAsUGoTransactionItemByItemId itemId {} , siteName {} ",
                    itemId, siteName, runtimeException);
            throw runtimeException;
        }
        return payAsUGoTransaction;
    }

    @WebMethod
    @GET @Path("getReferencedWebTransactionItemByItemId")
    @Produces({MediaType.APPLICATION_JSON})
    public WebTx getReferencedWebTransactionItemByItemId(@QueryParam("itemId") Long itemId,
                                                                  @QueryParam("siteName") String siteName) {
    	WebTx webTransaction = null;
        try {
        	webTransaction = this.webTXAdminService.getReferencedWebTransactionItemByItemId(itemId, siteName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getReferencedWebTransactionItemByItemId itemId {} , siteName {} ",
                    itemId, siteName, runtimeException);
            throw runtimeException;
        }
        return webTransaction;
    }

    @WebMethod
    @GET @Path("getReferencedPayAsUGoTransaction")
    @Produces({MediaType.APPLICATION_JSON})
    public List<PayAsUGoTx> getReferencedPayAsUGoTransaction(@QueryParam("txRefNumber") String txRefNumber,
                                                            @QueryParam("siteName")    String siteName) {
        List<PayAsUGoTx> payAsUGoTransaction = null;
        try {
        	payAsUGoTransaction = this.payAsUGoSubAdminService.getReferencedPayAsUGoTransaction(txRefNumber, siteName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getReferencedPayAsUGoTransaction txRefNumber{},  siteName {} ",
                txRefNumber, siteName, runtimeException);
            throw runtimeException;
        }
        return payAsUGoTransaction;
    }

    @WebMethod
    @GET @Path("getReferencedWebTransaction")
    @Produces({MediaType.APPLICATION_JSON})
    public List<WebTx> getReferencedWebTransaction(@QueryParam("txRefNumber") String txRefNumber,
                                                            @QueryParam("siteName")    String siteName) {
        List<WebTx> webTransaction = null;
        try {
        	webTransaction = this.webTXAdminService.getReferencedWebTransaction(txRefNumber, siteName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getReferencedWebTransaction txRefNumber{},  siteName {} ",
                txRefNumber, siteName, runtimeException);
            throw runtimeException;
        }
        return webTransaction;
    }

    @WebMethod
    @GET @Path("getReferencedOTCTransaction")
    @Produces({MediaType.APPLICATION_JSON})
    public OTCTx getReferencedOTCTransaction(@QueryParam("txRefNumber") String txRefNumber,
                                                      @QueryParam("siteName")    String siteName) {
        OTCTx oTCTransaction = null;
        try {
            oTCTransaction = this.oTCTXAdminService.getReferencedOTCTx(txRefNumber, siteName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getReferencedOTCTransaction txRefNumber {}, siteName{} ",
                txRefNumber, siteName, runtimeException);
            throw runtimeException;
        }
        return oTCTransaction;
    }


    @WebMethod
    @GET @Path("getReceiptConfigurationForSiteAndPaymentType/{siteName}/{paymentType}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<ReceiptConfiguration> getReceiptConfigurationForSiteAndPaymentType(
            @PathParam("siteName") String siteName,
            @PathParam("paymentType") PaymentType paymentType) {
        List<ReceiptConfiguration> receiptConfigurations = null;
        try {
            receiptConfigurations = this.ecomService.getReceiptConfigurationForSiteAndPaymentType(siteName, paymentType);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getReceiptConfigurationForSiteAndPaymentType for siteName {} And " +
                    "PaymentType {}", siteName, paymentType,
                runtimeException);
            throw runtimeException;
        }
        return receiptConfigurations;
    }

    @WebMethod
    @GET @Path("getRecurringTransactionByTxRefNum")
    @Produces({MediaType.APPLICATION_JSON})
    public List<RecurTx> getRecurTXByTxRefNum(
            @QueryParam("originaltxRefNumber") String originaltxRefNumber,
            @QueryParam("siteName") String siteName) {
        List<RecurTx> recurTransactions = null;
        try {
            recurTransactions = this.recurSubAdminService.getRecurTxByTxRefNum(originaltxRefNumber, siteName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getRecurringTransactionByTxRefNum for originaltxRefNumber {} siteName {}",
                originaltxRefNumber, siteName, runtimeException);
            throw runtimeException;
        }
        return recurTransactions;
    }

    @WebMethod
    @GET @Path("getReferencedRecurringTransactionByTxRefNum")
    @Produces({MediaType.APPLICATION_JSON})
    public RecurTx getReferencedRecurringTransactionByTxRefNum(
            @QueryParam("originaltxRefNumber") String originaltxRefNumber,
            @QueryParam("siteName") String siteName) {
        RecurTx recurTX = null;
        try {
            recurTX = this.recurSubAdminService.getReferencedRecurTxByTxRefNum(originaltxRefNumber, siteName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getReferencedRecurringTransactionByTxRefNum for originaltxRefNumber {} " +
                    "siteName {}",
                    originaltxRefNumber, siteName, runtimeException);
            throw runtimeException;
        }
        return recurTX;
    }

    @WebMethod
    @POST @Path("getACHDetailsForTransfer/{siteId}/{paymentType}/{machineIp}/{createdBy}")
    @Produces({MediaType.APPLICATION_JSON})
    public ACHTxDTO getACHDetailsForTransfer(@PathParam("siteId")      Long siteId,
								    	   @PathParam("paymentType") PaymentType paymentType,
								    	   @PathParam("machineIp")   String machineIp,
								    	   @PathParam("createdBy")   String createdBy) throws SDLException {
    	ACHTxDTO aCHDTO = null;
        try {
        	aCHDTO = this.aCHTXService.getACHDetailsForTransfer(siteId, paymentType, createdBy,
        			machineIp);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getACHDetailsForTransfer for siteId {}, paymentType {}, createdBy {}, " +
                "machineName {}", siteId, paymentType, createdBy, machineIp, runtimeException);
            throw runtimeException;
        }
        return aCHDTO;
    }

    @WebMethod
    @POST @Path("doACHTransfer/{siteId}/{paymentType}/{createdBy}/{machineIp}")
    @Produces({MediaType.APPLICATION_JSON})
    public ACHTxDTO doACHTransfer(@PathParam("siteId") Long siteId,
    							@PathParam("paymentType") PaymentType paymentType,
    							@PathParam("createdBy") String createdBy,
                                @PathParam("machineIp") String machineIp) throws SDLException, SDLBusinessException {
        ACHTxDTO aCHDTO = null;
        try {
            aCHDTO = this.aCHTXService.doACHTransfer(paymentType, siteId, machineIp, createdBy);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in doACHTransfer for paymentType {}, siteId {}, machineIp {} createdBy {} ",
                paymentType, siteId, machineIp, createdBy, runtimeException);
            throw runtimeException;
        }
        return aCHDTO;
    }

    @WebMethod
    @POST @Path("archiveUser/{userName}/{comments}/{modifiedBy}/{machineName}")
    public Void archiveUser(@PathParam("userName") String userName,
                            @PathParam("comments") String comments,
                            @PathParam("modifiedBy") String modifiedBy,
                            @PathParam("machineName") String machineName)
                    throws SDLBusinessException {
        try {
            comments = SystemUtil.decodeURL(comments);
            this.userAdminService.archiveUser(userName, comments, modifiedBy, machineName);
            return null;
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in archiveUser for userName {} comments {} modifiedBy {} machineName {} ",
                userName, comments, modifiedBy, machineName, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET @Path("getRecurTxBySite/{siteName}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<RecurTx> getRecurTxBySite(@PathParam("siteName") String siteName) {
        return this.recurSubAdminService.getRecurTxBySite(siteName);
    }

    @WebMethod
    @GET @Path("getRecurTxByUser/{userName}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<RecurTx> getRecurTxByUser(@PathParam("userName") String userName) {
        return this.recurSubAdminService.getRecurTxByUser(userName);
    }

    @WebMethod
    @GET @Path("getRecurTransactions/{userName}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<RecurTx> getRecurTransactions(@PathParam("userName") String userName,
    		@QueryParam("siteId") Long siteId) {
        return this.recurSubAdminService.getRecurTxByUserAndSite(userName, siteId);
    }

    @WebMethod
    @GET @Path("getPayAsUGoTransactions")
    @Produces({MediaType.APPLICATION_JSON})
    public List<PayAsUGoTx> getPayAsUGoTransactions(@QueryParam("userName") String userName,
    		@QueryParam("siteId") Long siteId) {
        return this.payAsUGoSubAdminService.getPayAsUGoTransactions(userName, siteId);
    }

    /**
     * This method returns users for a pagination.
     * Search Critieria has two properties(numberOfRecords, recordCount) that indicates the how many records to be returned and which row to start from.
     *
     *  It should really be GET method but we are doing post because we want to pass a SearchCriteria
     *
     * @param searchCriteria
     * @return
     */

    @WebMethod
    @POST @Path("findUsers")
    @Produces({MediaType.APPLICATION_JSON})
    public PageRecordsDTO findUsers(@RequestBody SearchCriteriaDTO searchCriteria){
    	return this.userAdminService.findUsers(searchCriteria);
    }


	/**
	 *
	 * @param userName
	 * @param siteName
	 * @return
	 */
     @WebMethod
     @GET @Path("getUserInfoForAdmin")
     @Produces({MediaType.APPLICATION_JSON})
    public List<SubscriptionDTO> getUserInfoForAdmin(@QueryParam("userName") String userName, @QueryParam("siteName") String siteName){
    	 return this.userAdminService.getUserInfoForAdmin(userName, siteName);
     }


    @WebMethod
    @GET @Path("getUserCountsForAllSite")
    @Produces({MediaType.APPLICATION_JSON})
    public List<UserCountDTO> getUserCountsForAllSite() {
   		return this.ecomService.getUserCountsForAllSite();
    }

    @WebMethod
    @GET @Path("getUserCountForSite/{siteId}")
    @Produces({MediaType.APPLICATION_JSON})
    public UserCountDTO getUserCountForSite(@PathParam("siteId") Long siteId) {
   		return this.ecomService.getUserCountForSite(siteId);
    }

    @WebMethod
    @GET @Path("getUserCountsBySubForASite/{siteId}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<UserCountDTO> getUserCountsBySubForASite(@PathParam("siteId") Long siteId) {
        /** There is no Assert as the Site Id could be Null**/
        return this.ecomService.getUserCountsBySubForASite(siteId);
    }

    @WebMethod
    @GET @Path("getUserDistributionBySubscription/{siteId}/{accessId}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<UserCountDTO> getUserDistributionBySubscription(@PathParam("siteId") Long siteId,
    		@PathParam("accessId") Long accessId) {
        /** There is no Assert as the Site/AccessId could be Null**/
        return this.ecomService.getUserDistributionBySubscription(siteId, accessId);
    }

    @WebMethod
    @GET @Path("lookupTx")
    @Produces({MediaType.APPLICATION_JSON})
    public PageRecordsDTO lookupTx(@QueryParam("txRefNumber")  String txRefNumber,
    		@QueryParam("productId")  String productId,
    		@QueryParam("productName")  String productName,
    		@QueryParam("productType")  String productType,
    		@QueryParam("invoiceId")  String invoiceId,
    		@QueryParam("accountName") String accountName,
    		@QueryParam("accountNumber") String accountNumber,
    		@QueryParam("transStartDate") String transStartDate,
    		@QueryParam("transEndDate") String transEndDate,
    		@QueryParam("paymentType") PaymentType paymentType,
    		@QueryParam("siteName") String siteName,
    		@QueryParam("startFrom") int startFrom,
    		@QueryParam("numberOfRecords") int numberOfRecords) throws SDLBusinessException {
    	try{
    		return this.ecomService.lookupTx(productId, productName, productType, invoiceId, txRefNumber, accountName,
    			accountNumber, transStartDate, transEndDate, paymentType, siteName, startFrom, numberOfRecords);
    	}catch(RuntimeException runtimeException){
    		logger.error("Error in lookupTx for txRefNumber {}, productId {}, productName {}, productType {}, invoiceId {}, "
    				+ "accountName {}, accountNumber {}, transStartDate {}, transStartDate {}, " +
                    "paymentType {}, siteName {}, startFrom {}, numberOfRecords{} ", 
                    txRefNumber , productId , productName , productType , invoiceId , 
            				accountName , accountNumber , transStartDate , transStartDate ,
                            paymentType , siteName , startFrom , numberOfRecords     , runtimeException);
                throw runtimeException;
    	}
    }

	@WebMethod
	@GET @Path("getSiteAdminDetails/{siteId}")
	@Produces({MediaType.APPLICATION_JSON})
	public Site getSiteAdminDetails(@PathParam("siteId") Long siteId) {
		return this.ecomService.getSiteAdminDetails(siteId);
	}

	@WebMethod
	@GET @Path("getCheckHistories")
	@Produces({MediaType.APPLICATION_JSON})
	public List<CheckHistory> getCheckHistories(@QueryParam("siteId") Long siteId,
		@QueryParam("fromDate") String fromDate,
		@QueryParam("toDate") String toDate,
		@QueryParam("checkNum") String checkNum,
		@QueryParam("checkAmt") Double checkAmt) {
		    	return this.ecomService.getCheckHistories(siteId, fromDate, toDate, checkNum, checkAmt);
	}

	@WebMethod
	@GET @Path("doVoidCheck/{checkNumber}/{comments}")
	@Produces({MediaType.APPLICATION_JSON})
	public boolean doVoidCheck(@PathParam("checkNumber") Long checkNumber,
		@PathParam("comments") String comments) {
		return this.ecomService.doVoidCheck(checkNumber, comments);
	}

	@WebMethod
	@GET @Path("getCheckHistory/{checkNumber}")
	@Produces({MediaType.APPLICATION_JSON})
	public CheckHistory getCheckHistory(@PathParam("checkNumber") Long checkNumber) {
		return this.ecomService.getCheckHistory(checkNumber);
	}

	@WebMethod
	@POST @Path("saveReceiptConfiguration")
	public void saveReceiptConfiguration(@RequestBody ReceiptConfiguration receiptConfiguration) {
		this.ecomService.saveReceiptConfiguration(receiptConfiguration);
	}

	@WebMethod
	@GET @Path("getReceiptConfigurationsForSite/{siteId}")
	@Produces({MediaType.APPLICATION_JSON})
	public List<ReceiptConfiguration> getReceiptConfigurationsForSite(@PathParam("siteId") Long siteId) {
		return this.ecomService.getReceiptConfigurationsForSite(siteId);
	}

	@WebMethod
	@GET @Path("getReceiptConfigurationDetail/{receiptConfigurationId}")
	@Produces({MediaType.APPLICATION_JSON})
	public ReceiptConfiguration getReceiptConfigurationDetail(
			@PathParam("receiptConfigurationId") Long receiptConfigurationId) {
		return this.ecomService.getReceiptConfigurationDetail(receiptConfigurationId);
	}

	@WebMethod
	@GET
	@Path("getErrorLog")
	@Produces({MediaType.APPLICATION_JSON})
	public PageRecordsDTO getErrorLog(@QueryParam("fromDate") String fromDate,
		@QueryParam("toDate") String toDate,
		@QueryParam("userName") String userName,
		@QueryParam("startFromRecord") Integer startFromRecord,
		@QueryParam("numberOfRecords") Integer numberOfRecords
		) {
		    	return this.ecomService.getErrorLog(fromDate, toDate, userName, startFromRecord, numberOfRecords);
	}

	@WebMethod
	@GET
	@Path("getUserAccessDetails/{userAccessId}")
	@Produces({MediaType.APPLICATION_JSON})
	public UserAccessDetailDTO getUserAccessDetails(@PathParam("userAccessId") Long userAccessId) {
		return this.subService.getUserAccessDetails(userAccessId);
	}

	@WebMethod
	@GET @Path("getBankDetailsBySite/{siteId}")
	@Produces({MediaType.APPLICATION_JSON})
	public BankDetails getBankDetailsBySite(@PathParam("siteId") Long siteId) {
		return this.ecomService.getBankDetailsBySite(siteId);
	}

	@WebMethod
	@DELETE @Path("deleteErrorLogContents/{errorLogId}")
	public void deleteErrorLogContents(@PathParam("errorLogId") Long errorLogId) {
		this.ecomService.deleteErrorLogContents(errorLogId);
	}

	@WebMethod
	@GET @Path("getFirmUsersbySubscriptionAndUserName/{userName}/{accessId}")
	@Produces({MediaType.APPLICATION_JSON})
	public List<FirmUserDTO> getFirmUsersbySubscriptionAndUserName(@PathParam("userName")
							String userName, @PathParam("accessId") Long accessId) {
		return this.userAdminService.getFirmUsersbySubscriptionAndUserName(userName, accessId);
	}


	@WebMethod
	@POST @Path("changeFirmSubscriptionAdministrator/{newAdminUserName}/{accessId}/{comments}/{modifiedBy}")
	@Produces({MediaType.APPLICATION_JSON})
	public void ChangeFirmSubscriptionAdministrator(
				@PathParam("newAdminUserName") String newAdminUserName,
				@PathParam("accessId") Long accessId,
				@PathParam("comments") String comments,
				@PathParam("modifiedBy") String modifiedBy)	throws UserNameNotFoundException, SDLBusinessException {
		this.subService.ChangeFirmSubscriptionAdministrator(newAdminUserName, accessId, comments, modifiedBy);
	}
	
	
	@WebMethod
	@GET @Path("getLocationSignatureById/{locationId}")
	@Produces({MediaType.APPLICATION_JSON})
	public Location getLocationSignatureById(@PathParam("locationId") Long locationId){
		return this.payAsUGoSubService.getLocationSignatureById(locationId);
	}
	
	@WebMethod
	@GET @Path("getLocationSealById/{locationId}")
	@Produces({MediaType.APPLICATION_JSON})
	public Location getLocationSealById(@PathParam("locationId") Long locationId){
		return this.payAsUGoSubService.getLocationSealById(locationId);
	}
	
	

}