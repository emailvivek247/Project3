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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.RequestBody;

import com.fdt.alerts.entity.UserAlert;
import com.fdt.alerts.service.AlertService;
import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.dto.ServiceResponseDTO;
import com.fdt.common.dto.TransactionRequestDTO;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.util.SystemUtil;
import com.fdt.ecom.entity.Code;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.Location;
import com.fdt.ecom.entity.ShoppingCartItem;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.Term;
import com.fdt.ecom.exception.AccessUnAuthorizedException;
import com.fdt.ecom.service.EComService;
import com.fdt.otctx.dto.OTCRequestDTO;
import com.fdt.otctx.dto.OTCResponseDTO;
import com.fdt.otctx.service.OTCTXService;
import com.fdt.payasugotx.dto.PayAsUSubDTO;
import com.fdt.payasugotx.entity.PayAsUGoTx;
import com.fdt.payasugotx.entity.PayAsUGoTxItem;
import com.fdt.payasugotx.entity.PayAsUGoTxView;
import com.fdt.payasugotx.service.PayAsUGoTxService;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.recurtx.dto.UpgradeDowngradeDTO;
import com.fdt.recurtx.entity.RecurTx;
import com.fdt.recurtx.service.RecurTxService;
import com.fdt.security.dto.EnableDisableFirmAccessRequestDTO;
import com.fdt.security.dto.FirmLevelUserRequestDTO;
import com.fdt.security.dto.FirmUserDTO;
import com.fdt.security.dto.RemoveFirmLevelAccessRequestDTO;
import com.fdt.security.entity.Access;
import com.fdt.security.entity.User;
import com.fdt.security.exception.BadPasswordException;
import com.fdt.security.exception.DuplicateAlertException;
import com.fdt.security.exception.InvalidDataException;
import com.fdt.security.exception.MaxUsersExceededException;
import com.fdt.security.exception.MaximumNumberOfAlertsReachedException;
import com.fdt.security.exception.UserAccountExistsException;
import com.fdt.security.exception.UserAlreadyActivatedException;
import com.fdt.security.exception.UserNameAlreadyExistsException;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.security.exception.UserNotActiveException;
import com.fdt.security.service.UserService;
import com.fdt.subscriptions.dto.AccessDetailDTO;
import com.fdt.subscriptions.dto.SubscriptionDTO;
import com.fdt.subscriptions.service.SubService;
import com.fdt.webtx.entity.WebTxItem;
import com.fdt.webtx.service.WebTxService;

public class EComFacadeServiceRSImpl implements EComFacadeServiceRS {

    private static final Logger logger = LoggerFactory.getLogger(EComFacadeServiceRSImpl.class);

    @Autowired
    private UserService userService = null;

    @Autowired
    private EComService ecomService = null;

    @Autowired
    private AlertService alertService = null;

    @Autowired
    private OTCTXService oTCTXService = null;

    @Autowired
    private RecurTxService recurSubService = null;

    @Autowired
    private SubService subService = null;

    @Autowired
    private PayAsUGoTxService payAsSubService = null;

    @Autowired
    private WebTxService webTXService = null;

    @WebMethod
    @POST
    @Path("registerUser/{siteId}/{accessId}/{clientName}/{requestURL}")
    @Produces({MediaType.APPLICATION_JSON})
    public void registerUser(@RequestBody User aNewUser, @PathParam("siteId") Long siteId,
            @PathParam("accessId") Long accessId, @PathParam("clientName") String clientName,
            @PathParam("requestURL") String requestURL) throws UserNameAlreadyExistsException {
        try {
            this.userService.registerUser(aNewUser, siteId, accessId, clientName, SystemUtil.decodeURL(requestURL));
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in registerUser For User {}, Site Id {}, Access Id {}, Client Name {}" +
                " requestURL{}", aNewUser, siteId, accessId, clientName, SystemUtil.decodeURL(requestURL), runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("addFirmLevelUser/{adminUserName}/{clientName}/{requestURL}")
    @Produces({MediaType.APPLICATION_JSON})
    public ServiceResponseDTO addFirmLevelUser(@RequestBody FirmLevelUserRequestDTO request, @PathParam("adminUserName") String adminUserName,
            @PathParam("clientName") String clientName,
            @PathParam("requestURL") String requestURL) throws UserNameNotFoundException,	MaxUsersExceededException, SDLBusinessException {

        try {
            return this.userService.addFirmLevelUser(request.getUser(), adminUserName, request.getAccessId(), clientName, SystemUtil.decodeURL(requestURL));
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in addFirmLevelUser For User {}, Admin User Name {}, Access Id {}, Client Name {}" +
                " requestURL{}", request.getUser(), adminUserName, request.getAccessId(), clientName, SystemUtil.decodeURL(requestURL), runtimeException);
            throw runtimeException;
        }
    }



    @WebMethod
    @POST
    @Path("enableDisableFirmLevelUserAccess")
    @Produces({MediaType.APPLICATION_JSON})
    public void enableDisableFirmLevelUserAccess(@RequestBody EnableDisableFirmAccessRequestDTO request)
    		throws UserNameNotFoundException, SDLBusinessException {
        try {
            this.subService.enableDisableFirmLevelUserAccess(request.getFirmUserName(), request.getUserAccessId(),
            		request.isEnable(), request.getModifiedBy(), request.getComments());
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in enableDisableFirmLevelUserAccess For firmUserName{}, userAccessId{}, "
            		+ "isEnable{}, comments {} ",
            		request.getFirmUserName(), request.getUserAccessId(), request.isEnable(), request.getComments(),
            		runtimeException);
            throw runtimeException;
        }
    }
    /**
     * This method return all the users for a given firm (admin user id):
     * If subscription (accessId is supplied then it will find the users under a given subscriptions
     */
    @WebMethod
    @GET
    @Path("getFirmUsers/{adminUserName}/{accessId: .*}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<FirmUserDTO> getFirmUsers(
        @PathParam("adminUserName") String adminUserName, @PathParam("accessId") String accessId){
        try {
        	Long longAccessId = null;
        	if(!StringUtils.isBlank(accessId)){
        		longAccessId = new Long(accessId);
        	}
            return this.userService.getFirmUsers(adminUserName, longAccessId);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getFirmUsers For  Admin User Name {}", adminUserName, runtimeException);
            throw runtimeException;
        }

    }


    @WebMethod
    @POST
    @Path("changePassword")
    @Produces({MediaType.APPLICATION_JSON})
    public void changePassword(@RequestBody User existingUser) throws UserNameNotFoundException, BadPasswordException {
        try {
            this.userService.changePassword(existingUser);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in changePassword For User {}", existingUser, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("activateUser/{userName}/{requestToken}")
    @Produces({MediaType.APPLICATION_JSON})
    public void activateUser(@PathParam("userName") String userName, @PathParam("requestToken") String requestToken)
                    throws InvalidDataException, UserAlreadyActivatedException {
        try {
            this.userService.activateUser(userName, requestToken);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in activateUser User Name {}, requestToken {}", userName, requestToken,
                runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("resetPassword/{requestToken}")
    @Produces({MediaType.APPLICATION_JSON})
    public void resetPassword(@RequestBody User user, @PathParam("requestToken") String requestToken)
            throws UserNameNotFoundException, InvalidDataException {
        try {
            this.userService.resetPassword(user, requestToken);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in resetPassword for User {}, requestToken {}", user, requestToken,
                runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("updateUser/{modifiedBy}")
    @Produces({MediaType.APPLICATION_JSON})
    public void updateUser(@RequestBody User updatedUser, @PathParam("modifiedBy") String modifiedBy)
            throws UserNameNotFoundException {
        try {
            this.userService.updateUser(updatedUser, modifiedBy);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in updateUser for updatedUser {}, modifiedBy {}", updatedUser, modifiedBy,
                runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET
    @Path("checkValidResetPasswordRequest/{userName}/{requestToken}")
    @Produces({MediaType.APPLICATION_JSON})
    public void checkValidResetPasswordRequest(@PathParam("userName") String userName,
            @PathParam("requestToken") String requestToken) throws InvalidDataException {
        try {
            this.userService.checkValidResetPasswordRequest(userName, requestToken);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in checkValidResetPasswordRequest for userName {}, requestToken {}",
                userName, requestToken, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("resetPasswordRequest/{userName}/{clientName}/{requestURL}")
    @Produces({MediaType.APPLICATION_JSON})
    public void resetPasswordRequest(@PathParam("userName") String userName, @PathParam("clientName") String clientName,
            @PathParam("requestURL") String requestURL) throws UserNameNotFoundException, UserNotActiveException {
        try {
            this.userService.resetPasswordRequest(userName, clientName, SystemUtil.decodeURL(requestURL));
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in resetPasswordRequest for userName {}, clientName{}, requestURL{}",
                userName, clientName, SystemUtil.decodeURL(requestURL), runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("resendUserActivationEmail/{userName}/{nodeName}/{requestURL}")
    @Produces({MediaType.APPLICATION_JSON})
    public void resendUserActivationEmail(@PathParam("userName") String userName, @PathParam("nodeName") String nodeName,
            @PathParam("requestURL") String requestURL) throws UserNameNotFoundException, UserAlreadyActivatedException {
        try {
            this.userService.resendUserActivationEmail(userName, nodeName, SystemUtil.decodeURL(requestURL));
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in resendUserActivationEmail for userName {}, nodeName{}, requestURL{}",
                userName, nodeName, SystemUtil.decodeURL(requestURL), runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("lockUnLockUser")
    @Produces({MediaType.APPLICATION_JSON})
    public void lockUnLockUser(@QueryParam("userName") String userName, @QueryParam("isLock") boolean isLock,
            @QueryParam("modifiedBy") String modifiedBy, @QueryParam("isSendUserConfirmation") boolean isSendUserConfirmation,
            @QueryParam("nodeName") String nodeName, @QueryParam("additionalComments") String additionalComments) {
        try {
            additionalComments = SystemUtil.decodeURL(additionalComments);
            this.userService.lockUnLockUser(userName, isLock, modifiedBy, isSendUserConfirmation, nodeName,
                    additionalComments);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in lockUnLockUser for userName {}, isLock{}, modifiedBy{}," +
                "isSendUserConfirmation {}, nodeName {}, additionalComments{}", userName, isLock, modifiedBy,
                isSendUserConfirmation, nodeName, additionalComments, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("updateLastLoginTime/{userName}")
    @Produces({MediaType.APPLICATION_JSON})
    public void updateLastLoginTime(@PathParam("userName") String userName) throws UserNameNotFoundException {
        try {
            this.userService.updateLastLoginTime(userName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in updateLastLoginTime for userName {}", userName, runtimeException);
            throw runtimeException;
        }
    }

    @Cacheable("getSitesForNode")
    @WebMethod
    @GET
    @Path("getSitesForNode/{nodeName}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<Site> getSitesForNode(@PathParam("nodeName") String nodeName) {
        try {
            return this.ecomService.getSitesForNode(nodeName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getSitesForNode for nodeName {}", nodeName, runtimeException);
            throw runtimeException;
        }
    }

    @Cacheable("getAccessesForSite")
    @WebMethod
    @GET
    @Path("getAccessesForSite/{siteId}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<Access> getAccessesForSite(@PathParam("siteId") String siteId) {
        try {
            return this.ecomService.getAccessesForSite(siteId);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getAccessesForSite for siteId {}", siteId, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("paySubscriptions")
    @Produces({MediaType.APPLICATION_JSON})
    public List<PayPalDTO> paySubscriptions(@RequestBody SubscriptionDTO subscriptionDTO) throws AccessUnAuthorizedException, SDLBusinessException {
        String userName = subscriptionDTO.getUser().getUsername();
        CreditCard creditCard = subscriptionDTO.getCreditCard();
        String nodeName = subscriptionDTO.getNodeName();
        String machineName = subscriptionDTO.getMachineName();
        try {
            return this.subService.payRecurSub(creditCard, userName, nodeName, machineName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in paySubscriptions for userName {}, creditCardDetails {}, nodeName {}, " +
                "machineName {} ", userName, creditCard, nodeName, machineName, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET
    @Path("getPaidSubUnpaidByUser/{userName}/{nodeName}")
    @Produces({MediaType.APPLICATION_JSON})
    public User getPaidSubUnpaidByUser(@PathParam("userName") String userName, @PathParam("nodeName") String nodeName) {
        try {
            return this.subService.getPaidUnpaidRecurSubByUser(userName, nodeName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getPaidSubUnpaidByUser for userName {} nodeName {}", userName, nodeName,
                runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("updateExistingCreditCardInformation/{userName}/{modifiedBy}")
    @Produces({MediaType.APPLICATION_JSON})
    public void updateExistingCreditCardInformation(@PathParam("userName") String userName,
            @PathParam("modifiedBy") String modifiedBy, @RequestBody CreditCard newCreditCard)
                    throws PaymentGatewaySystemException, PaymentGatewayUserException {
        try {
            this.userService.updateExistingCreditCardInformation(userName, modifiedBy, newCreditCard);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in updateExistingCreditCardInformation for userName {}, modifiedBy {}, " +
                    "CreditCard {}", userName, modifiedBy,  newCreditCard, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET
    @Path("getCreditCardDetails/{userId}")
    @Produces({MediaType.APPLICATION_JSON})
    public CreditCard getCreditCardDetails(@PathParam("userId") Long userId) {
        try {
            return this.userService.getCreditCardDetails(userId);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getCreditCardDetails for userId {}", userId, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET
    @Path("getCreditCardDetailsByUserName/{userName}")
    @Produces({MediaType.APPLICATION_JSON})
    public CreditCard getCreditCardDetailsByUserName(@PathParam("userName") String userName) {
        try {
            return this.userService.getCreditCardDetails(userName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getCreditCardDetailsByUserName for userName {}", userName, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET
    @Path("getUserSubscriptions")
    @Produces({MediaType.APPLICATION_JSON})
    public List<SubscriptionDTO> getUserSubscriptions(@QueryParam("userName") String userName,
    		@QueryParam("nodeName") String nodeName, @QueryParam("siteName") String siteName,
    		@QueryParam("activeSubscriptionsOnly") boolean activeSubscriptionsOnly,
    		@QueryParam("firmAdminSubscriptionsOnly") boolean firmAdminSubscriptionsOnly
            ) {
        try {
            return this.subService.getUserSubs(userName, nodeName, siteName, activeSubscriptionsOnly, firmAdminSubscriptionsOnly);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getUserSubscriptions for userName {}, nodeName {}, siteName {}, "
            		+ "activeSubscriptionsOnly {}, firmAdminSubscriptionsOnly {}", userName,
                nodeName, siteName, activeSubscriptionsOnly, firmAdminSubscriptionsOnly, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET
    @Path("getSubscriptionDetails/{userName}/{accessId}")
    @Produces({MediaType.APPLICATION_JSON})
    public SubscriptionDTO getSubscriptionDetails(@PathParam("userName") String userName,
            @PathParam("accessId") Long accessId) {
        try {
            return this.subService.getSubDetailsForUser(userName, accessId);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getSubscriptionDetails for userName {}, accessId {}", userName, accessId,
                runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("cancelSubscription/{userName}/{userAccessId}")
    @Produces({MediaType.APPLICATION_JSON})
    public PayPalDTO cancelSubscription(@PathParam("userName") String userName, @PathParam("userAccessId") Long userAccessId)
            throws PaymentGatewaySystemException, SDLBusinessException {
        try {
            return this.subService.cancelSub(userName, userAccessId);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in cancelSubscription for userName {}, userAccessId{}", userName,
                userAccessId, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET
    @Path("loadUserByUsername/{userName}/{nodeName}")
    @Produces("application/json")
    public User loadUserByUsername(@PathParam("userName") String userName, @PathParam("nodeName")  String nodeName) {
        try {
            return this.userService.loadUserByUsername(userName, nodeName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in loadUserByUsername for userName {}, nodeName{}", userName, nodeName,
                runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET
    @Path("getChangeSubscriptionInfo/{userAccessId}/{accessId}/{userName}")
    @Produces({MediaType.APPLICATION_JSON})
    public UpgradeDowngradeDTO getChangeSubscriptionInfo(@PathParam("userAccessId") Long userAccessId,
            @PathParam("accessId") Long accessId, @PathParam("userName") String userName)
            		throws SDLBusinessException, MaxUsersExceededException {
        try {
            return this.subService.getRecurChangeSubInfo(userAccessId, accessId, userName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getChangeSubscriptionInfo for userAccessId {}, accessId {} userName{}",
                userAccessId, accessId, userName, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("changeFromRecurringToRecurringSubscription/{userAccessId}/{accessId}/{userName}/{machineName}")
    @Produces({MediaType.APPLICATION_JSON})
    public UpgradeDowngradeDTO changeFromRecurringToRecurringSubscription(@PathParam("userAccessId") Long userAccessId,
            @PathParam("accessId") Long accessId, @PathParam("userName") String userName,
            @PathParam("machineName") String machineName)
                    throws PaymentGatewaySystemException, PaymentGatewayUserException, SDLBusinessException,
                    MaxUsersExceededException {
        try {
            return this.subService.changeFromRecurToRecurSub(userAccessId, accessId, userName,
                machineName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in changeFromRecurringToRecurringSubscription for userAccessId {}, " +
                "accessId {}, userName {}, machineName{}", userAccessId, accessId, userName, machineName, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("addSubscription")
    @Produces({MediaType.APPLICATION_JSON})
    public List<AccessDetailDTO> addSubscription(@RequestBody SubscriptionDTO subscriptionDTO)
            throws SDLBusinessException {
        try {
            return this.subService.addSub(subscriptionDTO);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in addSubscription for User {}, " +
                "newAccessIds {}, nodeName {}", subscriptionDTO.getUser(), subscriptionDTO.getNewAccessIds(),
                subscriptionDTO.getNodeName(), runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("reactivateCancelledSubscription/{userName}/{existingUserAccessId}")
    @Produces({MediaType.APPLICATION_JSON})
    public PayPalDTO reactivateCancelledSubscription(@PathParam("userName") String userName,
            @PathParam("existingUserAccessId") Long existingUserAccessId)
                    throws PaymentGatewayUserException, PaymentGatewaySystemException {
        try {
            return this.subService.reactivateCancelledSub(userName, existingUserAccessId);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in reactivateCancelledSubscription for userName {}, existingUserAccessId{}",
                userName, existingUserAccessId, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("doSaleOTC/{oTCRequestDTO}")
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
    @Path("doSaleOTCWithOutEncryption/{oTCRequestDTO}")
    @Produces({MediaType.APPLICATION_JSON})
    public OTCResponseDTO doSaleOTCWithOutEncryption(@RequestBody OTCRequestDTO oTCRequestDTO) {
        try {
            return this.oTCTXService.doSaleOTCWithOutEncryption(oTCRequestDTO);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in doSaleOTCWithOutEncryption for oTCRequestDTO {}", oTCRequestDTO, runtimeException);
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
    @POST
    @Path("doSaleGetInfoWEB/{siteName}/{itemList}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<WebTxItem> doSaleGetInfoWEB(@PathParam("siteName") String siteName,
            @PathParam("itemList") List<WebTxItem> itemList)
            throws SDLBusinessException {
        try {
            return this.webTXService.doSaleGetInfoWEB(siteName, itemList);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in doSaleGetInfoWEB for siteName {} itemList {}", siteName, itemList,
                runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET
    @Path("getRecurTransactionsByNode/{userName}/{nodeName}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<RecurTx> getRecurTransactionsByNode(@PathParam("userName") String userName, @PathParam("nodeName")
        String nodeName) {
        try {
            return this.recurSubService.getRecurTxByNode(userName, nodeName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getRecurTransactionsByNode for userName{}, nodeName{}, isHidePaypalId{}",
                userName, nodeName, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET
    @Path("getRecurTxDetail/{userName}/{recurTxRefNum}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<RecurTx> getRecurTxDetail(@PathParam("userName") String userName,
            @PathParam("recurTxRefNum") String recurTxRefNum) {
        try {
            return this.recurSubService.getRecurTxDetail(userName, recurTxRefNum);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getRecurTxDetail for userName, recurTxRefNum", userName, recurTxRefNum,
                runtimeException);
            throw runtimeException;
        }
    }
    @WebMethod
    @POST
    @Path("getPayAsUGoTransactionsByNode")
    @Produces({MediaType.APPLICATION_JSON})
    public List<PayAsUGoTxView> getPayAsUGoTransactionsByNode(@RequestBody TransactionRequestDTO request){
        try {
            return this.payAsSubService.getPayAsUGoTxByNode(request.getUserName(), request.getNodeName(),
            		request.getComments(),
            		request.getFromDate(), request.getToDate());
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getPayAsUGoTransactionsByNode for userName {} "
            		+ "nodeName{}, fromDate{}, toDate{}", request.getUserName(), request.getNodeName(),
            		request.getFromDate(), request.getToDate(),
                    runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("getPayAsUGoTransactionsByNodePerPage")
    @Produces({MediaType.APPLICATION_JSON})
    public PageRecordsDTO getPayAsUGoTransactionsByNodePerPage(@RequestBody TransactionRequestDTO request) {
        try {
            return this.payAsSubService.getPayAsUGoTxByNodePerPage(request.getUserName(), request.getNodeName(),
            		request.getComments(), request.getFromDate(), request.getToDate(),
            		request.getStartingFrom(), request.getNumberOfRecords());
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getPayAsUGoTransactionsByNode for userName {} "
            		+ "nodeName{}, fromDate{}, toDate{}, startingFrom{}, numberOfRecords{}", request.getUserName(),
            		request.getNodeName(), request.getFromDate(), request.getToDate(), request.getStartingFrom(),
            		request.getNumberOfRecords(), runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET
    @Path("getPayAsUGoTransactionDetail/{userName}/{recTxId}/{isRefund}")
    @Produces({MediaType.APPLICATION_JSON})
    public PayAsUGoTx getPayAsUGoTransactionDetail(@PathParam("userName") String userName,
            @PathParam("recTxId") Long recTxId, @PathParam("isRefund") String isRefund) {
        try {
            return this.payAsSubService.getPayAsUGoTxDetail(userName, recTxId, isRefund);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getPayAsUGoTransactionDetail for userName {}, recTxId {}, isRefund {}", userName,
                recTxId, isRefund, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("doSalePayAsUGo/{userName}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<PayAsUGoTx> doSalePayAsUGo(@PathParam("userName") String userName,
            @RequestBody PayAsUSubDTO payAsUGoTransactionDTO)
            throws PaymentGatewayUserException,    PaymentGatewaySystemException, SDLBusinessException {
        try {
            return this.payAsSubService.doSalePayAsUGo(userName, payAsUGoTransactionDTO);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in doSalePayAsUGo for userName {}, webTransactionDTO {}", userName,
                    payAsUGoTransactionDTO, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("doSalePayAsUGoInfo")
    @Produces({MediaType.APPLICATION_JSON})
    public List<ShoppingCartItem> doSalePayAsUGoInfo(@RequestBody PayAsUSubDTO payAsUGoTransactionDTO)
            throws SDLBusinessException {
        List<ShoppingCartItem> itemList = payAsUGoTransactionDTO.getShoppingCartItemList();
        String userName = payAsUGoTransactionDTO.getUserName();
        try {
            return this.payAsSubService.doSalePayAsUGoInfo(userName, itemList);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in doSalePayAsUGoInfo for userName {}, itemList {}", userName,
                    itemList , runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET
    @Path("getPayAsUGoTxIdForPurchasedDoc/{userName}/{productKey}/{uniqueIdentifier}")
    @Produces({MediaType.APPLICATION_JSON})
    public PayAsUGoTxItem getPayAsUGoTxIdForPurchasedDoc(@PathParam("userName") String userName,
            @PathParam("productKey") String productKey, @PathParam("uniqueIdentifier") String uniqueIdentifier) {
        try {
            return this.payAsSubService.getPayAsUGoTxIdForPurchasedDoc(userName, productKey, uniqueIdentifier);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getWebTxIdForPurchasedDoc for userName {}, productKey {} uniqueIdentifier{}",
                    userName, productKey, uniqueIdentifier, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET
    @Path("getShoppingBasketItems/{userName}/{nodeName}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<ShoppingCartItem> getShoppingBasketItems(@PathParam("userName") String userName,
            @PathParam("nodeName") String nodeName) {
        try {
            return this.payAsSubService.getShoppingBasketItems(userName, nodeName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getShoppingBasketItems for userName {}, nodeName {} ", userName, nodeName,
                runtimeException);
            throw runtimeException;
        }
    }


    @WebMethod
    @POST
    @Path("deleteShoppingCartItem")
    @Produces({MediaType.APPLICATION_JSON})
    public void deleteShoppingCartItem(@RequestBody ShoppingCartItem shoppingCartItem) {
        try {
            this.payAsSubService.deleteShoppingCartItem(shoppingCartItem);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in deleteShoppingCartItem for shoppingCartItem {} ", shoppingCartItem,
                runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET
    @Path("getUserAlertsByUserName/{userName}/{nodeName}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<UserAlert> getUserAlertsByUserName(@PathParam("userName") String userName,
            @PathParam("nodeName") String nodeName) {
        try {
            return this.alertService.getUserAlertsByUserName(userName, nodeName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getUserAlertsByUserName for userName {} nodeName{}", userName, nodeName,
                runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("saveShoppingCartItem")
    @Produces({MediaType.APPLICATION_JSON})
    public void saveShoppingCartItem(@RequestBody ShoppingCartItem shoppingCartItem) {
        try {
            this.payAsSubService.saveShoppingCartItem(shoppingCartItem);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in saveShoppingCartItem for shoppingCartItem {} ", shoppingCartItem,
                runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("saveUserAlert")
    @Produces({MediaType.APPLICATION_JSON})
    public void saveUserAlert(@RequestBody UserAlert userAlert) throws UserNameNotFoundException, DuplicateAlertException,
        MaximumNumberOfAlertsReachedException {
        try {
            this.alertService.saveUserAlert(userAlert);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in saveUserAlert for userAlert {} ", userAlert, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @DELETE
    @Path("deleteUserAlerts/{userName}")
    @Produces({MediaType.APPLICATION_JSON})
    public void deleteUserAlerts(@PathParam("userName") String userName,
            @RequestBody List<Long> userAlertIdList) {
        try {
            this.alertService.deleteUserAlerts(userName, userAlertIdList);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in deleteUserAlerts for userName {}, userAlertIdList {} ", userName,
                userAlertIdList, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET
    @Path("getCodes/{codeCategory}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<Code> getCodes(@PathParam("codeCategory") String codeCategory) {
        try {
            return this.ecomService.getCodes(codeCategory);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getCodes for codeCategory {}", codeCategory, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET
    @Path("getTerm/{siteName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Term getTerm(@PathParam("siteName") String siteName) {
        try {
            return this.ecomService.getTerm(siteName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getTerm for siteName {}", siteName, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET
    @Path("getAccessDetails/{accessId}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<AccessDetailDTO> getAccessDetails(@PathParam("accessId") Long accessId) {
        try {
            return this.subService.getSubDetailsByAccessId(accessId);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getAccessDetails for accessId {}", accessId, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @GET
    @Path("getNewTermsAndConditionsforUser/{userName}/{nodeName}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<Term> getNewTermsAndConditionsforUser(@PathParam("userName") String userName, @PathParam("nodeName") String nodeName) {
        try {
            return this.userService.getNewTermsAndConditionsforUser(userName, nodeName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getTerm for userName {}, nodeName {}", userName, nodeName, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST
    @Path("updateUserTerms")
    @Produces({MediaType.APPLICATION_JSON})
    public void updateUserTerms(@RequestBody User user) throws UserNameNotFoundException {
        try {
            this.userService.updateUserTerms(user);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in updateUserTerms for userName {} ", user.getUsername(), runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @DELETE
    @Path("deleteCreditCard/{userName}")
    @Produces({MediaType.APPLICATION_JSON})
    public void deleteCreditCard(@PathParam("userName") String userName) throws UserAccountExistsException {
        try {
            this.ecomService.deleteCreditCard(userName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in deleteCreditCard for userName {}", userName, runtimeException);
            throw runtimeException;
        }
    }

    @WebMethod
    @POST @Path("removeFirmLevelUserAccess")
    public void removeFirmLevelUserAccess(@RequestBody RemoveFirmLevelAccessRequestDTO request)
                    throws UserNameNotFoundException, SDLBusinessException {
        try {
            this.subService.removeFirmLevelUserAccess(request.getFirmUserName(), request.getUserAccessId(),
            	request.getComments(), request.getModifiedBy(), request.isSendNotification());
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in deleteFirmUser for firmUserName{} userAccessId {} comments {}, "
            	+ "modifiedBy {}, isSendNotification {}  ",	request.getFirmUserName(), request.getUserAccessId(),
            		request.isSendNotification(), request.getModifiedBy(), request.isSendNotification(), runtimeException);
            throw runtimeException;
        }
    }

    /** This Method Is Used To Add A Firm Level Subscription For A User. It Checks For Whether That Type Of Subscription Already
     * Exists For The Site Among All The Existing Subscriptions For The User.
     *
     * It also checks if subscription is firm level subscription and it does belong to admin user.
     *
     * @param adminUserName
     * @param firmUserName
     * @param accessId
     * @return
     * @throws SDLBusinessException
     */



    @WebMethod
    @GET
    @Path("findUser/{userName}")
    @Produces({MediaType.APPLICATION_JSON})
    public User findUser(@PathParam("userName") String userName)
                    throws UserNameNotFoundException{
        try {
            return this.userService.findUser(userName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in findUser for userName{}", userName, runtimeException);
            throw runtimeException;
        }
    }

    /** This Method Is Used To Add A Firm Level Subscription For A User. It Checks For Whether That Type Of Subscription Already
     * Exists For The Site Among All The Existing Subscriptions For The User.
     *
     * It also checks if subscription is firm level subscription and it does belong to admin user.
     *
     * @param request
     * @param adminUserName
     * @throws UserNameNotFoundException
     * @throws MaxUsersExceededException
     * @throws SDLBusinessException
     */
    @WebMethod
    @POST
    @Path("addFirmUserAccess/{adminUserName}")
    @Produces({MediaType.APPLICATION_JSON})
    public void addFirmUserAccess( @RequestBody FirmLevelUserRequestDTO request,
    		@PathParam("adminUserName") String adminUserName)
    			throws UserNameNotFoundException, MaxUsersExceededException, SDLBusinessException{
	    try {
	        this.subService.addFirmUserAccess(adminUserName, request.getUser().getUsername(), request.getAccessId(), request.getNodeName());
	    } catch(RuntimeException runtimeException) {
	        logger.error(NOTIFY_ADMIN, "Error in addFirmUserAccess for adminUserName{}, firmUserName{}, accessId{}",
	        		adminUserName, request.getUser().getUsername(), request.getAccessId(), runtimeException);
	        throw runtimeException;
	    }
    }


	@WebMethod
	@POST
	@Path("updateShoppingCartComments")
    @Produces({MediaType.APPLICATION_JSON})
	public void updateShoppingCartComments(@RequestBody ShoppingCartItem shoppingCartItem) {
		this.payAsSubService.updateShoppingCartComments(shoppingCartItem.getId(), shoppingCartItem.getComments());
	}

	@WebMethod
	@GET @Path("getLocationsBySiteId/{siteId}")
	@Produces({MediaType.APPLICATION_JSON})
	public List<Location> getLocationsBySiteId(@PathParam("siteId") Long siteId){
		return this.payAsSubService.getLocationsBySiteId(siteId);
	}


	@WebMethod
	@GET @Path("getLocationByNameAndAccessName/{locationName}/{accessName}")
	@Produces({MediaType.APPLICATION_JSON})
	public Location getLocationByNameAndAccessName(@PathParam("locationName") String locationName,
			@PathParam("accessName") String accessName){
		return this.payAsSubService.getLocationByNameAndAccessName(locationName, accessName);
	}

	@WebMethod
	@GET @Path("getDocumentIdByCertifiedDocumentNumber/{certifiedDocumentNumber}/{siteName}")
	@Produces({MediaType.APPLICATION_JSON})
	public String getDocumentIdByCertifiedDocumentNumber(@PathParam("certifiedDocumentNumber")
			String certifiedDocumentNumber, @PathParam("siteName") String siteName) {
		return this.payAsSubService.getDocumentIdByCertifiedDocumentNumber(certifiedDocumentNumber, siteName);
	}

}