package com.fdt.ecom.service;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;
import static javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.ws.rs.PathParam;
import javax.xml.ws.BindingType;

import org.apache.cxf.annotations.WSDLDocumentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

import com.fdt.alerts.dto.UserAlertDTO;
import com.fdt.alerts.entity.UserAlert;
import com.fdt.alerts.service.AlertService;
import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.dto.ServiceResponseDTO;
import com.fdt.common.dto.TransactionRequestDTO;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.ecom.entity.Code;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.Location;
import com.fdt.ecom.entity.ShoppingCartItem;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.Term;
import com.fdt.ecom.exception.AccessUnAuthorizedException;
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

@WebService(endpointInterface = "com.fdt.ecom.service.EComFacadeService", serviceName ="EComFacadeService")
@BindingType(value = SOAP12HTTP_BINDING)
@WSDLDocumentation(value="ACCEPT SOAP 1.2 Services for Internal Applications", placement = WSDLDocumentation.Placement.TOP)
public class EComFacadeServiceImpl implements EComFacadeService {

    private static final Logger logger = LoggerFactory.getLogger(EComFacadeServiceImpl.class);

    @Autowired
    private UserService userService = null;

    @Autowired
    private EComService ecomService = null;

    @Autowired
    private AlertService alertService = null;

    @Autowired
    private SubService subService = null;

    @Autowired
    private RecurTxService recurSubService = null;

    @Autowired
    private PayAsUGoTxService payAsUGoSubService = null;

    @Autowired
    private WebTxService webTXService = null;

    public void registerUser(User aNewUser, Long siteId, Long accessId, String clientName, String requestURL)
        throws UserNameAlreadyExistsException {
        try {
            this.userService.registerUser(aNewUser, siteId, accessId, clientName, requestURL);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in registerUser For User {}, Site Id {}, Access Id {}, Client Name {}" +
                " requestURL{}", aNewUser, siteId, accessId, clientName, requestURL, runtimeException);
            throw runtimeException;
        }
    }


    public ServiceResponseDTO addFirmLevelUser(FirmLevelUserRequestDTO request, String adminUserName, String clientName, String requestURL)
            throws UserNameNotFoundException,	MaxUsersExceededException, SDLBusinessException {
            try {
                return this.userService.addFirmLevelUser(request.getUser(), adminUserName, request.getAccessId(), clientName, requestURL);
            } catch(RuntimeException runtimeException) {
                logger.error(NOTIFY_ADMIN, "Error in addFirmLevelUser For User {}, Admin User Name {}, Access Id {}, Client Name {}" +
                    " requestURL{}", request.getUser(), adminUserName, request.getAccessId(), clientName, requestURL, runtimeException);
                throw runtimeException;
            }
        }


    /**
     * This method return all the users for a given firm (admin user id):
     * If subscription (accessId is supplied then it will find the users under a given subscriptions

     * @param adminUserName
     * @param accessId
     * @return
     */
    public List<FirmUserDTO> getFirmUsers(String adminUserName, Long accessId){
        return this.userService.getFirmUsers(adminUserName, accessId);
    }

    public void removeFirmLevelUserAccess( RemoveFirmLevelAccessRequestDTO request)
            throws UserNameNotFoundException, SDLBusinessException {
        try {
            this.subService.removeFirmLevelUserAccess(request.getFirmUserName(), request.getUserAccessId(), request.getComments(), request.getModifiedBy(), request.isSendNotification());
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in removeFirmLevelUserAccess for firmUserName{} userAccessId {} comments {}, modifiedBy {}, isSendNotification {}  ",
            		request.getFirmUserName(), request.getUserAccessId(), request.isSendNotification(), request.getModifiedBy(), request.isSendNotification(), runtimeException);
            throw runtimeException;
        }
    }

    public void enableDisableFirmLevelUserAccess(EnableDisableFirmAccessRequestDTO request)
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



    public void changePassword(User existingUser) throws UserNameNotFoundException, BadPasswordException {
        try {
            this.userService.changePassword(existingUser);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in changePassword For User {}", existingUser, runtimeException);
            throw runtimeException;
        }
    }

    public void activateUser(String userName, String requestToken) throws InvalidDataException,
        UserAlreadyActivatedException {
        try {
            this.userService.activateUser(userName, requestToken);
            System.out.println("TRY");
        } catch(RuntimeException runtimeException) {
             System.out.println("CATCH");
            logger.error(NOTIFY_ADMIN, "Error in activateUser User Name {}, requestToken {}", userName, requestToken,
                runtimeException);
            throw runtimeException;
        }
        System.out.println("After try catch");
    }

    public void resetPassword(User user, String requestToken) throws UserNameNotFoundException, InvalidDataException {
        try {
            this.userService.resetPassword(user, requestToken);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in resetPassword for User {}, requestToken {}", user, requestToken,
                runtimeException);
            throw runtimeException;
        }
    }

    public void updateUser(User updatedUser, String modifiedBy) throws UserNameNotFoundException {
        try {
            this.userService.updateUser(updatedUser, modifiedBy);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in updateUser for updatedUser {}, modifiedBy {}", updatedUser, modifiedBy,
                runtimeException);
            throw runtimeException;
        }
    }

    public void checkValidResetPasswordRequest(String userName, String requestToken) throws InvalidDataException {
        try {
            this.userService.checkValidResetPasswordRequest(userName, requestToken);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in checkValidResetPasswordRequest for userName {}, requestToken {}",
                userName, requestToken, runtimeException);
            throw runtimeException;
        }
    }

    public void resetPasswordRequest(String userName, String clientName, String requestURL) throws UserNameNotFoundException,
        UserNotActiveException {
        try {
            this.userService.resetPasswordRequest(userName, clientName, requestURL);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in resetPasswordRequest for userName {}, clientName{}, requestURL{}",
                userName, clientName, requestURL, runtimeException);
            throw runtimeException;
        }
    }

    public void resendUserActivationEmail(String userName, String nodeName, String requestURL)
            throws UserNameNotFoundException, UserAlreadyActivatedException {
        try {
            this.userService.resendUserActivationEmail(userName, nodeName, requestURL);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in resendUserActivationEmail for userName {}, nodeName{}, requestURL{}",
                userName, nodeName, requestURL, runtimeException);
            throw runtimeException;
        }
    }

    public void lockUnLockUser(String userName, boolean isLock, String modifiedBy, boolean isSendUserConfirmation,
            String nodeName, String additionalComments) {
        try {
            this.userService.lockUnLockUser(userName, isLock, modifiedBy, isSendUserConfirmation, nodeName,
                additionalComments);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in lockUnLockUser for userName {}, isLock{}, modifiedBy{}," +
                "isSendUserConfirmation {}, nodeName {}, additionalComments{}", userName, isLock, modifiedBy,
                isSendUserConfirmation, nodeName, additionalComments, runtimeException);
            throw runtimeException;
        }
    }

    public void updateLastLoginTime(String userName) throws UserNameNotFoundException {
        try {
            this.userService.updateLastLoginTime(userName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in updateLastLoginTime for userName {}", userName, runtimeException);
            throw runtimeException;
        }
    }

    @Cacheable("getSitesForNode")
    public List<Site> getSitesForNode(String nodeName) {
        try {
            return this.ecomService.getSitesForNode(nodeName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getSitesForNode for nodeName {}", nodeName, runtimeException);
            throw runtimeException;
        }
    }

    @Cacheable("getAccessesForSite")
    public List<Access> getAccessesForSite(String siteId) {
        try {
            return this.ecomService.getAccessesForSite(siteId);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getAccessesForSite for siteId {}", siteId, runtimeException);
            throw runtimeException;
        }
    }

    public List<PayPalDTO> paySubscriptions(SubscriptionDTO subscriptionDTO) throws AccessUnAuthorizedException,
    	SDLBusinessException {
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

    public User getPaidSubUnpaidByUser(String userName, String nodeName) {
        try {
            return this.subService.getPaidUnpaidRecurSubByUser(userName, nodeName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getPaidSubUnpaidByUser for userName {} nodeName {}", userName, nodeName,
                runtimeException);
            throw runtimeException;
        }
    }

    public void updateExistingCreditCardInformation(String userName, String modifiedBy, CreditCard newCreditCard)
            throws PaymentGatewaySystemException, PaymentGatewayUserException {
        try {
        	this.userService.updateExistingCreditCardInformation(userName, modifiedBy, newCreditCard);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in updateExistingCreditCardInformation for userName {}, modifiedBy {}, " +
                    "CreditCard {}", userName, modifiedBy, newCreditCard, runtimeException);
            throw runtimeException;
        }
    }

    public CreditCard getCreditCardDetails(Long userId) {
        try {
            return this.userService.getCreditCardDetails(userId);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getCreditCardDetails for userId {}", userId, runtimeException);
            throw runtimeException;
        }
    }

    public CreditCard getCreditCardDetailsByUserName(String userName) {
        try {
            return this.userService.getCreditCardDetails(userName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getCreditCardDetailsByUserName for userName {}", userName, runtimeException);
            throw runtimeException;
        }
    }

    public List<SubscriptionDTO> getUserSubscriptions(String userName, String nodeName, String siteName,
    		boolean activeSubscriptionsOnly, boolean firmAdminSubscriptionsOnly) {
        try {
            return this.subService.getUserSubs(userName, nodeName, siteName, activeSubscriptionsOnly, firmAdminSubscriptionsOnly);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getUserSubscriptions for userName {}, nodeName {}, siteName {}, "
            		+ "activeSubscriptionsOnly {}, firmAdminSubscriptionsOnly {}", userName,
                nodeName, siteName, activeSubscriptionsOnly, firmAdminSubscriptionsOnly, runtimeException);
            throw runtimeException;
        }
    }

    public SubscriptionDTO getSubscriptionDetails(String userName, Long accessId) {
        try {
            return this.subService.getSubDetailsForUser(userName, accessId);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getSubscriptionDetails for userName {}, accessId {}", userName, accessId,
                runtimeException);
            throw runtimeException;
        }
    }

    public PayPalDTO cancelSubscription(String userName, Long userAccessId) throws PaymentGatewaySystemException,
        SDLBusinessException {
        try {
            return this.subService.cancelSub(userName, userAccessId);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in cancelSubscription for userName {}, userAccessId{}", userName,
                userAccessId, runtimeException);
            throw runtimeException;
        }
    }

    public User loadUserByUsername(String userName, String nodeName) {
        try {
            return this.userService.loadUserByUsername(userName, nodeName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in loadUserByUsername for userName {}, nodeName{}", userName, nodeName,
                runtimeException);
            throw runtimeException;
        }
    }

    public UpgradeDowngradeDTO getChangeSubscriptionInfo(Long userAccessId, Long accessId, String userName)
            throws SDLBusinessException, MaxUsersExceededException {
        try {
            return this.subService.getRecurChangeSubInfo(userAccessId, accessId, userName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getChangeSubscriptionInfo for userAccessId {}, accessId {} userName{}",
                userAccessId, accessId, userName, runtimeException);
            throw runtimeException;
        }
    }

    public UpgradeDowngradeDTO changeFromRecurringToRecurringSubscription(Long userAccessId, Long accessId, String userName,
            String machineName) throws PaymentGatewaySystemException, PaymentGatewayUserException, SDLBusinessException,
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

    public List<AccessDetailDTO> addSubscription(SubscriptionDTO subscriptionDTO)
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

    public PayPalDTO reactivateCancelledSubscription(String userName, Long existingUserAccessId)
            throws PaymentGatewayUserException, PaymentGatewaySystemException {
        try {
            return this.subService.reactivateCancelledSub(userName, existingUserAccessId);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in reactivateCancelledSubscription for userName {}, existingUserAccessId{}",
                userName, existingUserAccessId, runtimeException);
            throw runtimeException;
        }
    }

    public List<WebTxItem> doSaleGetInfoWEB(String siteName, List<WebTxItem> itemList)
            throws SDLBusinessException {
        try {
            return this.webTXService.doSaleGetInfoWEB(siteName, itemList);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in doSaleGetInfoWEB for siteName {} itemList {}", siteName, itemList,
                runtimeException);
            throw runtimeException;
        }
    }

    public List<RecurTx> getRecurTransactionsByNode(String userName, String nodeName) {
        try {
            return this.recurSubService.getRecurTxByNode(userName, nodeName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getRecurTransactionsByNode for userName{}, nodeName{}", userName, nodeName,
                runtimeException);
            throw runtimeException;
        }
    }

    public List<RecurTx> getRecurTxDetail(String userName, String recurTxRefNum) {
        try {
            return this.recurSubService.getRecurTxDetail(userName, recurTxRefNum);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getRecurTxDetail for userName, recurTxRefNum", userName, recurTxRefNum,
                runtimeException);
            throw runtimeException;
        }
    }

    public List<PayAsUGoTxView> getPayAsUGoTransactionsByNode(TransactionRequestDTO request) {
    	try {
            return this.payAsUGoSubService.getPayAsUGoTxByNode(request.getUserName(), request.getNodeName(),
            		request.getFromDate(), request.getToDate());
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getPayAsUGoTransactionsByNode for userName {} "
            		+ "nodeName{}, fromDate{}, toDate{}", request.getUserName(), request.getNodeName(),
            		request.getFromDate(), request.getToDate(),
                    runtimeException);
            throw runtimeException;
        }
    }

    public PageRecordsDTO getPayAsUGoTransactionsByNodePerPage(TransactionRequestDTO request) {
        try {
            return this.payAsUGoSubService.getPayAsUGoTxByNodePerPage(request.getUserName(), request.getNodeName(),
            		request.getFromDate(), request.getToDate(), request.getStartingFrom(), request.getNumberOfRecords());
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getPayAsUGoTransactionsByNode for userName {} "
            		+ "nodeName{}, fromDate{}, toDate{}, startingFrom{}, numberOfRecords{}", request.getUserName(), request.getNodeName(),
            		request.getFromDate(), request.getToDate(), request.getStartingFrom(), request.getNumberOfRecords(),
                    runtimeException);
            throw runtimeException;
        }
    }

    public PayAsUGoTx getPayAsUGoTransactionDetail(String userName, Long recTxId, String isRefund) {
        try {
            return this.payAsUGoSubService.getPayAsUGoTxDetail(userName, recTxId, isRefund);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getPayAsUGoTransactionDetail for userName {}, recTxId {}, isRefund {}",
            	userName, recTxId, isRefund, runtimeException);
            throw runtimeException;
        }
    }

    public List<PayAsUGoTx> doSalePayAsUGo(String userName, PayAsUSubDTO payAsUGoTransactionDTO)
            throws PaymentGatewayUserException,    PaymentGatewaySystemException, SDLBusinessException {
        try {
            return this.payAsUGoSubService.doSalePayAsUGo(userName, payAsUGoTransactionDTO);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in doSalePayAsUGo for userName {}, payAsUGoTransactionDTO {}", userName,
                    payAsUGoTransactionDTO, runtimeException);
            throw runtimeException;
        }
    }

    public List<ShoppingCartItem> doSalePayAsUGoInfo(PayAsUSubDTO payAsUGoTransactionDTO)
            throws SDLBusinessException {
        String userName = payAsUGoTransactionDTO.getUserName();
        List<ShoppingCartItem> shoppingCartItemList = payAsUGoTransactionDTO.getShoppingCartItemList();
        try {
            return this.payAsUGoSubService.doSalePayAsUGoInfo(userName, shoppingCartItemList);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in doSalePayAsUGoInfo for userName {}, shoppingCart {}", userName,
                    shoppingCartItemList, runtimeException);
            throw runtimeException;
        }
    }

    public PayAsUGoTxItem getPayAsUGoTxIdForPurchasedDoc(String userName, String productKey, String uniqueIdentifirer) {
        try {
            return this.payAsUGoSubService.getPayAsUGoTxIdForPurchasedDoc(userName, productKey, uniqueIdentifirer);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getPayAsUGoTxIdForPurchasedDoc for userName {}, productKey {} " +
            	"uniqueIdentifirer{}", userName, productKey, uniqueIdentifirer, runtimeException);
            throw runtimeException;
        }
    }


    public List<ShoppingCartItem> getShoppingBasketItems(String userName, String nodeName) {
        try {
            return this.payAsUGoSubService.getShoppingBasketItems(userName, nodeName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getShoppingBasketItems for userName {}, nodeName {} ", userName, nodeName,
                runtimeException);
            throw runtimeException;
        }
    }


    public void deleteShoppingCartItem(ShoppingCartItem shoppingCartItem) {
        try {
            this.payAsUGoSubService.deleteShoppingCartItem(shoppingCartItem);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in deleteShoppingCartItem for shoppingCartItem {} ", shoppingCartItem,
                runtimeException);
            throw runtimeException;
        }
    }

    public List<UserAlert> getUserAlertsByUserName(String userName, String nodeName) {
        try {
            return this.alertService.getUserAlertsByUserName(userName, nodeName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getUserAlertsByUserName for userName {} nodeName{}", userName, nodeName,
                runtimeException);
            throw runtimeException;
        }
    }

    public void saveShoppingCartItem(ShoppingCartItem shoppingCartItem) {
        try {
            this.payAsUGoSubService.saveShoppingCartItem(shoppingCartItem);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in saveShoppingCartItem for shoppingCartItem {} ", shoppingCartItem,
                runtimeException);
            throw runtimeException;
        }
    }

    public void saveUserAlert(UserAlert userAlert) throws UserNameNotFoundException, DuplicateAlertException,
        MaximumNumberOfAlertsReachedException {
        try {
            this.alertService.saveUserAlert(userAlert);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in saveUserAlert for userAlert {} ", userAlert, runtimeException);
            throw runtimeException;
        }
    }

    public void deleteUserAlerts(UserAlertDTO userAlertDTO) {
        String userName = userAlertDTO.getUserName();
        List<Long> userAlertIdList =  userAlertDTO.getUserAlertIdList();
        try {
            this.alertService.deleteUserAlerts(userName, userAlertIdList);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in deleteUserAlerts for userName {}, userAlertIdList {} ", userName,
                userAlertIdList, runtimeException);
            throw runtimeException;
        }
    }

    public List<Code> getCodes(String codeCategory) {
        try {
            return this.ecomService.getCodes(codeCategory);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getCodes for codeCategory {}", codeCategory, runtimeException);
            throw runtimeException;
        }
    }

   public Term getTerm(String siteName) {
        try {
            return this.ecomService.getTerm(siteName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getTerm for siteName {}", siteName, runtimeException);
            throw runtimeException;
        }
    }

    public List<AccessDetailDTO> getAccessDetails(Long accessId) {
        try {
            return this.subService.getSubDetailsByAccessId(accessId);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getAccessDetails for accessId {}", accessId, runtimeException);
            throw runtimeException;
        }
    }

    public List<Term> getNewTermsAndConditionsforUser(String userName, String nodeName) {
        try {
            return this.userService.getNewTermsAndConditionsforUser(userName, nodeName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in getNewTermsAndConditionsforUser for userName {} nodeName{}", userName,
            	nodeName, runtimeException);
            throw runtimeException;
        }
    }

    public void updateUserTerms(@WebParam(name="user") User user) throws UserNameNotFoundException {
        try {
            this.userService.updateUserTerms(user);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in updateUserTerms for userName {} ", user.getUsername(),
                runtimeException);
            throw runtimeException;
        }
    }

    public void deleteCreditCard(@WebParam(name="userName") String userName) throws UserAccountExistsException {
        try {
            this.ecomService.deleteCreditCard(userName);
        } catch(RuntimeException runtimeException) {
            logger.error(NOTIFY_ADMIN, "Error in deleteCreditCard for userName {}", userName, runtimeException);
            throw runtimeException;
        }
    }

    public User findUser(@WebParam(name="userName") String userName) throws UserNameNotFoundException {
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
    public void addFirmUserAccess(FirmLevelUserRequestDTO request,
    		@WebParam(name = "adminUserName") String adminUserName)
    			throws UserNameNotFoundException, MaxUsersExceededException, SDLBusinessException{
	    try {
	        this.subService.addFirmUserAccess(adminUserName, request.getUser().getUsername(), request.getAccessId(), request.getNodeName());
	    } catch(RuntimeException runtimeException) {
	        logger.error(NOTIFY_ADMIN, "Error in addFirmUserAccess for adminUserName{}, firmUserName{}, accessId{}, nodeName{}",
	        		adminUserName, request.getUser().getUsername(), request.getAccessId(), request.getNodeName(), runtimeException);
	        throw runtimeException;
	    }
    }


	public void updateShoppingCartComments(@WebParam(name="shoppingCartItem") ShoppingCartItem shoppingCartItem){
		this.payAsUGoSubService.updateShoppingCartComments(shoppingCartItem.getId(), shoppingCartItem.getComments());
    }

	public List<Location> getLocationsBySiteId(@PathParam("siteId") Long siteId){
		return this.payAsUGoSubService.getLocationsBySiteId(siteId);
	}

	public Location getLocationByNameAndAccessName(@PathParam("locationName") String locationName,
			@PathParam("accessName") String accessName){
		return this.payAsUGoSubService.getLocationByNameAndAccessName(locationName, accessName);
	}
}