package com.fdt.subscriptions.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.fdt.common.dao.AbstractBaseDAOImpl;
import com.fdt.ecom.dto.UserAccessDetailDTO;
import com.fdt.ecom.entity.Code;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.CreditUsageFee;
import com.fdt.ecom.entity.Merchant;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.SiteConfiguration;
import com.fdt.ecom.entity.Term;
import com.fdt.ecom.entity.UserHistory;
import com.fdt.recurtx.dto.RecurTxSchedulerDTO;
import com.fdt.recurtx.dto.UserAccountDetailDTO;
import com.fdt.recurtx.entity.UserAccount;
import com.fdt.security.entity.Access;
import com.fdt.security.entity.User;
import com.fdt.security.entity.UserAccess;
import com.fdt.subscriptions.dto.AccessDetailDTO;
import com.fdt.subscriptions.dto.SubscriptionDTO;
import com.fdt.subscriptions.entity.SubscriptionFee;

@Repository
public class SubDAOImpl extends AbstractBaseDAOImpl implements SubDAO {

    private static final Logger logger = LoggerFactory.getLogger(SubDAOImpl.class);

   /**
    * Flag paidSubscriptionsOnly will be applied as below.
    *
    * If paidSubscriptionsOnly == true
    * 	It will return paid subscriptions only (UserAccess.Active = true)
    *
    * If paidSubscriptionsOnly == false
    * 	it will ignore the UserAccess.active value. i.e. It will return all the subscriptions regardless of value of
    *  UserAccess.active
    *
    * firmAdminSubscriptionsOnly : If this flag is set to true it will fetch only firm level admin subscriptions. In such
    * case the username passed must be admin to retrieve the firm level subscriptions
    *
    */
   public List<SubscriptionDTO> getUserSubs(String userName, String nodeName, String siteName, boolean activeSubscriptionsOnly, boolean firmAdminSubscriptionsOnly) {
        List<SubscriptionDTO> subscriptionDTOs = new LinkedList<SubscriptionDTO>();
        String activeSubscriptionsOnlyStr = "N";
        if(activeSubscriptionsOnly){
        	activeSubscriptionsOnlyStr = "Y";
        }
        String firmAdminSubscriptionsOnlyStr = "N";
        if(firmAdminSubscriptionsOnly){
        	firmAdminSubscriptionsOnlyStr = "Y";
        }
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_ALL_SUBSCRIPTIONS")
                                  .setParameter("userName", userName)
                                  .setParameter("nodeName", nodeName)
                                  .setParameter("siteName", siteName)
                                  .setParameter("activeSubscriptionsOnly", activeSubscriptionsOnlyStr)
                                  .setParameter("firmAdminSubscriptionsOnly", firmAdminSubscriptionsOnlyStr);
        List<Object> resultSet = sqlQuery.list();
        if(resultSet.size() > 0) {
            ListIterator<Object> resultSetIterator = (ListIterator<Object>) resultSet.listIterator();
            while(resultSetIterator.hasNext()) {
                SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
                Object[] row = (Object[]) resultSetIterator.next();
                subscriptionDTO.setSiteId(this.getLongFromInteger(row[0]));
                subscriptionDTO.setSiteName(this.getString(row[1]));
                subscriptionDTO.setSubscription(this.getString(row[2]));
                subscriptionDTO.setSubscriptionFee(this.getDoubleFromBigDecimal(row[3]));
                subscriptionDTO.setPayMentPending(this.getBoolean(row[4]));
                subscriptionDTO.setLastBillingDate(this.getDate(row[5]));
                subscriptionDTO.setNextBillingDate(this.getDate(row[6]));
                subscriptionDTO.setUserAccountId(this.getLongFromBigInteger(row[7]));
                subscriptionDTO.setUserAccessId(this.getLongFromInteger(row[8]));
                subscriptionDTO.setAccessId(this.getLongFromInteger(row[9]));
                subscriptionDTO.setCategory(this.getString(row[10]));
                subscriptionDTO.setIsMarkedForCancellation(this.getBoolean(row[11]));
                subscriptionDTO.setModifiedDate(this.getDate(row[12]));
                subscriptionDTO.setCreatedDate(this.getDate(row[13]));
                subscriptionDTO.setModifiedBy(this.getString(row[14]));
                subscriptionDTO.setActive(this.getBoolean(row[15]));
                subscriptionDTO.setComments(this.getString(row[16]));
                subscriptionDTO.setAccessOverridden(this.getBoolean(row[17]));
                subscriptionDTO.setAuthorizationRequired(this.getBoolean(row[18]));
                subscriptionDTO.setAuthorized(this.getBoolean(row[19]));
                subscriptionDTO.setCreatedBy(this.getString(row[20]));
                subscriptionDTO.setAccountModifiedDate(this.getDate(row[21]));
                subscriptionDTO.setAuthorizationDate(this.getDate(row[22]));
                subscriptionDTO.setAuthorizedBy(this.getString(row[23]));
                subscriptionDTO.setFirmAccessAdmin(this.getBoolean(row[24]));
                subscriptionDTO.setFirmLevelAccess(this.getBoolean(row[25]));
                subscriptionDTO.setMaxUsersAllowed(this.getInteger(row[26]));
                subscriptionDTO.setFirmAdminUserAccessId((this.getLongFromInteger(row[27])));
                subscriptionDTO.setGovernmentAccess(this.getBoolean(row[28]));
                subscriptionDTO.setMaxDocumentsAllowed(this.getInteger(row[29]));
                subscriptionDTOs.add(subscriptionDTO);
                subscriptionDTO.getSubscriptionType();
           }
        }
        return subscriptionDTOs;
    }

    public SubscriptionDTO getSubDetailsForUser(String userName, Long accessId) {
        SubscriptionDTO subscriptionDTO = null;
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_SUBSCRIPTION_DETAILS")
                                   .setParameter("userName", userName)
                                   .setParameter("accessId", accessId);
        List<Object> resultSet = sqlQuery.list();
        if (resultSet.size() > 0) {
            Object[] row = (Object[])resultSet.get(0);
            subscriptionDTO =  new SubscriptionDTO();
            subscriptionDTO.setSiteId(this.getLongFromInteger(row[0]));
            subscriptionDTO.setSiteName(this.getString(row[1]));
            subscriptionDTO.setSubscription(this.getString(row[2]));
            subscriptionDTO.setSubscriptionFee(this.getDoubleFromBigDecimal(row[3]));
            subscriptionDTO.setPayMentPending(this.getBoolean(row[4]));
            subscriptionDTO.setLastBillingDate(this.getDate(row[5]));
            subscriptionDTO.setNextBillingDate(this.getDate(row[6]));
            subscriptionDTO.setUserAccountId(this.getLongFromBigInteger(row[7]));
            subscriptionDTO.setAccessId(this.getLongFromInteger(row[8]));
            subscriptionDTO.setUserAccessId(this.getLongFromInteger(row[9]));
            subscriptionDTO.setPeriod(this.getString(row[10]));
            subscriptionDTO.setIsMarkedForCancellation(this.getBoolean(row[11]));
            subscriptionDTO.setTerm(this.getLongFromInteger(row[12]));
            subscriptionDTO.setCategory(this.getString(row[13]));
            subscriptionDTO.setVisible(this.getBoolean(row[14]));
       }
       return subscriptionDTO;
    }

    public void markForCancellation(Long userAccountId, boolean isMarkForCancellation) {
        Session session = currentSession();
        Boolean enableDisable = Boolean.FALSE;
        if (isMarkForCancellation) {
            enableDisable = Boolean.TRUE;
        }
        session.createQuery("Update UserAccount userAccount " +
                "Set userAccount.markForCancellation = :markForCancellation, " +
                "userAccount.modifiedDate = :modifiedDate " +
                "Where userAccount.id = :userAccountId")
                .setParameter("markForCancellation", enableDisable)
                .setParameter("modifiedDate", new Date())
                .setParameter("userAccountId", userAccountId)
                .executeUpdate();

    }

    public UserAccountDetailDTO getUserAccountByUserAcessId(String userName, Long userAccessId) {
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_USER_ACCOUNT_BY_USER_ACCESS_ID")
                                  .setParameter("userName", userName)
                                  .setParameter("userAccessId", userAccessId);
        List<Object> resultSet = sqlQuery.list();
        UserAccountDetailDTO userAccountDetailDTO = null;
        if (resultSet.size() > 0) {
            userAccountDetailDTO = new UserAccountDetailDTO();
            Object[] row = (Object[])resultSet.get(0);
            UserAccount userAccount = new UserAccount();
            userAccount.setId(this.getLongFromBigInteger(row[0]));
            userAccount.setLastBillingDate(this.getDate(row[1]));
            userAccount.setNextBillingDate(this.getDate(row[2]));
            userAccount.setActive(this.getBoolean(row[3]));
            Site site = new Site();
            Merchant merchant = new Merchant();
            merchant.setUserName(this.getString(row[4]));
            merchant.setPassword( row[5] == null ? null : this.getPbeStringEncryptor().decrypt(row[5].toString()));
            merchant.setVendorName(this.getString(row[6]));
            merchant.setPartner(this.getString(row[7]));
            merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[31]));
            merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[32]));
            merchant.setId(this.getLongFromInteger(row[30]));
            merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[34]));
            merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[35]));
            site.addMerchant(merchant);
            SubscriptionFee subFee = new SubscriptionFee();
            subFee.setFee(this.getDoubleFromBigDecimal(row[8]));
            CreditCard creditCard = new CreditCard();
            creditCard.setId(this.getLongFromInteger(row[9]));
            creditCard.setName(this.getString(row[10]));
            creditCard.setNumber(row[12] == null ? null : this.getPbeStringEncryptor().decrypt(row[11].toString()));
            creditCard.setExpiryMonth(this.getInteger(row[12]));
            creditCard.setExpiryYear(this.getInteger(row[13]));
            creditCard.setAddressLine1(this.getString(row[14]));
            creditCard.setAddressLine2(this.getString(row[15]));
            creditCard.setCity(this.getString(row[16]));
            creditCard.setState(this.getString(row[17]));
            creditCard.setZip(this.getString(row[18]));
            creditCard.setPhone(this.getLongFromBigInteger(row[19]));
            creditCard.setActive(this.getBoolean(row[20]));
            site.setId(this.getLongFromInteger(row[29]));
            site.setName(this.getString(row[21]));
            userAccount.setMarkForCancellation(this.getBoolean(row[22]));
            Access access = new Access();
            List<Access> accessList = new LinkedList<Access>();
            access.setDescription(this.getString(row[23]));
            access.setAccessType(this.getAccessType(row[24]));
            access.setId(this.getLongFromInteger(row[25]));
            access.setClientShare(this.getDoubleFromBigDecimal(row[33]));
            Code subscriptionType =  new Code();
            subscriptionType.setCode(this.getString(row[26]));
            subFee.setPaymentPeriod(subscriptionType);
            subFee.setTerm(this.getLongFromInteger(row[27]));
            access.setSubscriptionFee(subFee);
            access.setVisible(this.getBoolean(row[36]));
            accessList.add(access);
            site.setAccess(accessList);
            site.setTimeZone(this.getString(row[39]));
            userAccountDetailDTO.setUserId(this.getLongFromBigInteger(row[28]));
            userAccount.setCreditCard(creditCard);
            userAccount.setLastTxRefNum(this.getString(row[37]));
            userAccountDetailDTO.setUserAccount(userAccount);
            userAccountDetailDTO.setSubFee(subFee);
            userAccountDetailDTO.setSite(site);
            UserAccess userAccess  = new UserAccess();
            userAccess.setFirmAccessAdmin(this.getBoolean(row[40]));
            userAccess.setFirmAdminUserAccessId(this.getLongFromInteger(row[41]));
            access.addUserAccess(userAccess);
            access.setFirmLevelAccess(this.getBoolean(row[42]));
            access.setMaxUsersAllowed(this.getInteger(row[43]));
            access.setMaxDocumentsAllowed(this.getInteger(row[44]));
            access.setGovernmentAccess(this.getBoolean(row[45]));
        }
        return userAccountDetailDTO;
    }

    public void saveUserHistory(List<UserHistory> userHistories) {
        Session session = currentSession();
        for(UserHistory userHistory: userHistories){
            session.saveOrUpdate(userHistory);
        }
        session.flush();
    }

    public void deleteUserAccountByUserAccessId(List<Long> userAccessIds) {
        Session session = currentSession();
        session.getNamedQuery("DELETE_USER_ACCOUNT_BY_USER_ACCESS_ID")
                .setParameterList("userAccessIds", userAccessIds).executeUpdate();
    }

    public void deleteUserTerm(Long userId, Long siteId) {
    	List<Long> userIds = new ArrayList<Long>();
    	userIds.add(userId);
    	this.deleteUserTerm(userIds, siteId);
    }

    public void deleteUserTerm(List<Long> userIds, Long siteId) {
        Session session = currentSession();
        session.getNamedQuery("DELETE_USER_TERM")
                .setParameterList("userIds", userIds)
                .setParameter("siteId",  siteId)
                .executeUpdate();
    }

    public void deleteUserAccess(List<Long> userAccessIds) {
        Session session = currentSession();
        session.getNamedQuery("DELETE_USER_ACCESS_BY_USER_ACCESS_ID")
                .setParameterList("userAccessIds", userAccessIds).executeUpdate();
    }

    /**
     * Delete all firm level user accesses
     *
     *
     * @param userId
     * @param userAccessIds
     * @param firmAdminUserAccessId
     */
    public void deleteFirmUserAccess(Long accessId, Long firmAdminUserAccessId) {
        Session session = currentSession();
        session.getNamedQuery("DELETE_FIRM_USERS_ACCESS_BY_AMDIN_USER_ACCESS_ID")
                .setParameter("accessId", accessId)
                .setParameter("firmAdminUserAccessId", firmAdminUserAccessId)
                .executeUpdate();
    }

    @Cacheable("getSubDetailsByAccesIds")
    public List<AccessDetailDTO> getSubDetailsByAccesIds(List<Long> accessIdList) {
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_ACCESS_DETAILS")
                                  .setParameterList("accessIdList", accessIdList);
        List<Object> resultSet = sqlQuery.list();
        AccessDetailDTO accessDetailDTO = null;
        List<AccessDetailDTO> accessDetailDTOList = null;
        if (resultSet.size() > 0) {
            accessDetailDTOList = new LinkedList<AccessDetailDTO>();
            Iterator<Object> iterator =  resultSet.iterator();
            while(iterator.hasNext()) {
                Object[] row = (Object[]) iterator.next();
                accessDetailDTO = new AccessDetailDTO();
                Site site = new Site();
                Merchant merchant = new Merchant();
                CreditUsageFee creditUsageFee =  new CreditUsageFee();
                SubscriptionFee subscriptionFee =  new SubscriptionFee();
                site.addMerchant(merchant);
                site.setCardUsageFee(creditUsageFee);
                site.setId(this.getLongFromInteger(row[0]));
                site.setName(this.getString(row[1]));
                site.setDescription(this.getString(row[34]));
                site.setEnableMicroTxWeb(this.getBoolean(row[2]));
                subscriptionFee.setFee(this.getDoubleFromBigDecimal(row[3]));
                subscriptionFee.setTerm(this.getLongFromInteger(row[4]));
                Code subscriptionType =  new Code();
                subscriptionType.setCode(this.getString(row[5]));
                subscriptionFee.setPaymentPeriod(subscriptionType);
                merchant.setId(this.getLongFromInteger(20));
                merchant.setUserName(this.getString(row[6]));
                merchant.setPassword( row[7] == null ? null : this.getPbeStringEncryptor().decrypt(row[7].toString()));
                merchant.setVendorName(this.getString(row[8]));
                merchant.setPartner(this.getString(row[9]));
                merchant.setMicroPaymentAccount(this.getBoolean(row[10]));
                merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[22]));
                merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[23]));
                merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[25]));
                merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[26]));
                creditUsageFee.setPercenteFee(this.getDoubleFromBigDecimal(row[11]));
                creditUsageFee.setFlatFeeCutOff(this.getDoubleFromBigDecimal(row[12]));
                creditUsageFee.setFlatFee(this.getDoubleFromBigDecimal(row[13]));
                creditUsageFee.setDowngradeFee(this.getDoubleFromBigDecimal(row[14]));
                creditUsageFee.setMicroTxFeeCutOff(this.getDoubleFromBigDecimal(row[15]));
                Access access = new Access();
                List<Access> accessList = new LinkedList<Access>();
                access.setId(this.getLongFromInteger(row[16]));
                access.setCode(this.getString(row[17]));
                access.setDescription(this.getString(row[18]));
                access.setSubscriptionFee(subscriptionFee);
                access.setAccessType(this.getAccessType(row[19]));
                access.setAuthorizationRequired(this.getBoolean(row[21]));
                accessDetailDTO.setAuthorizationRequired(this.getBoolean(row[21]));
                access.setClientShare(this.getDoubleFromBigDecimal(row[24]));
                access.setVisible(this.getBoolean(row[27]));
                access.setFirmLevelAccess(this.getBoolean(row[31]));
                access.setMaxUsersAllowed(this.getInteger(row[32]));
                access.setMaxDocumentsAllowed(this.getInteger(row[33]));
                site.setTimeZone(this.getString(row[28]));
                site.setAutoActivate(this.getString(row[29]).equals("Y") ? true : false);
                Term term = new Term();
                term.setId(this.getLongFromInteger(row[30]));
                site.setTerm(term);
                accessDetailDTO.setSubFee(subscriptionFee);
                accessList.add(access);
                site.setAccess(accessList);
                accessDetailDTO.setSite(site);
                accessDetailDTOList.add(accessDetailDTO);
            }
        }
       return accessDetailDTOList;
    }

    public List<AccessDetailDTO> getSubDetailsByAccessId(Long accessId) {
        List<Long> accessIdList = new LinkedList<Long>();
        accessIdList.add(accessId);
        return this.getSubDetailsByAccesIds(accessIdList);
    }

    public UserAccessDetailDTO getUserAccessDetails(Long userAccessId) {
        UserAccessDetailDTO userAccessDetailDTO = null;
        SiteConfiguration siteConfiguration = null;
        Site site = null;
        Access access  = null;
        User user = null;
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_USER_ACCESS_DETAIL")
                          .setParameter("userAccessId", userAccessId);
        List<Object> resultList = sqlQuery.list();
        if(!resultList.isEmpty()){
            Object[] row = (Object[]) resultList.get(0);
            userAccessDetailDTO = new UserAccessDetailDTO();
            siteConfiguration = new SiteConfiguration();
            site = new Site();
            access = new Access();
            user = new User();
            siteConfiguration.setId(this.getLongFromInteger(row[0]));
            siteConfiguration.setSiteId(this.getLongFromInteger(row[1]));
            site.setId(this.getLongFromInteger(row[1]));
            siteConfiguration.setEmailTemplateFolder(this.getString(row[2]));
            siteConfiguration.setFromEmailAddress(this.getString(row[3]));
            siteConfiguration.setPaymentConfirmationSubject(this.getString(row[4]));
            siteConfiguration.setChangeSubscriptionSubject(this.getString(row[5]));
            siteConfiguration.setCancelSubscriptionSubject(this.getString(row[6]));
            siteConfiguration.setReactivateSubscriptionSubject(this.getString(row[7]));
            siteConfiguration.setRecurringPaymentSuccessSubject(this.getString(row[8]));
            siteConfiguration.setRecurringPaymentUnsuccessfulSubject(this.getString(row[9]));
            siteConfiguration.setPaymentConfirmationTemplate(this.getString(row[10]));
            siteConfiguration.setChangeSubscriptionTemplate(this.getString(row[11]));
            siteConfiguration.setCancelSubscriptionTemplate(this.getString(row[12]));
            siteConfiguration.setReactivateCancelledSubscriptionTemplate(this.getString(row[13]));
            siteConfiguration.setRecurringPaymentSuccessTemplate(this.getString(row[14]));
            siteConfiguration.setRecurringPaymentUnsuccessfulTemplate(this.getString(row[15]));
            siteConfiguration.setCreatedDate(this.getDate(row[16]));
            siteConfiguration.setModifiedDate(this.getDate(row[17]));
            siteConfiguration.setModifiedBy(this.getString(row[18]));
            siteConfiguration.setActive(this.getBoolean(row[19]));
            siteConfiguration.setWebPaymentConfSubject(this.getString(row[20]));
            siteConfiguration.setWebPaymentConfTemplate(this.getString(row[21]));
            siteConfiguration.setSiteName(this.getString(row[22]));
            site.setName(this.getString(row[22]));
            siteConfiguration.setRemoveSubscriptionSubject(this.getString(row[23]));
            siteConfiguration.setRemoveSubscriptionTemplate(this.getString(row[24]));
            siteConfiguration.setCreatedBy(this.getString(row[25]));
            access.setDescription(this.getString(row[26]));
            user.setFirstName(this.getString(row[27]));
            user.setLastName(this.getString(row[28]));
            siteConfiguration.setAccessAuthorizationSubject(this.getString(row[29]));
            siteConfiguration.setAccessAuthorizationTemplate(this.getString(row[30]));
            user.setUsername(this.getString(row[31]));
            site.setDescription(this.getString(row[32]));
            userAccessDetailDTO.setSiteConfiguration(siteConfiguration);
            userAccessDetailDTO.setSite(site);
            userAccessDetailDTO.setAccess(access);
            userAccessDetailDTO.setUser(user);
            UserAccess userAccess = new UserAccess();
            userAccess.setFirmAccessAdmin(this.getBoolean(row[33]));
            userAccess.setFirmAdminUserAccessId(this.getLongFromInteger(row[34]));
            userAccess.setActive(this.getBoolean(row[38]));
            access.setId(this.getLongFromInteger(row[35]));
            access.setFirmLevelAccess(this.getBoolean(row[36]));
            access.setGovernmentAccess(this.getBoolean(row[37]));
            access.addUserAccess(userAccess);
        }
        return userAccessDetailDTO;
    }

    @Cacheable("getSubDetailsByName")
    public AccessDetailDTO getSubDetailsByName(String accessName) {
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_ACCESS_DETAILS_BY_NAME")
                                  .setParameter("accessName", accessName);
        List<Object> resultSet = sqlQuery.list();
        Site site = new Site();
        AccessDetailDTO accessDetailDTO = null;
        if (resultSet.size() > 0) {
        	 ListIterator<Object> resultSetIterator = (ListIterator<Object>) resultSet.listIterator();
             Map<Long, Site> uniqueSites =  new HashMap<Long, Site>();
             accessDetailDTO = new AccessDetailDTO();
           	 while(resultSetIterator.hasNext()) {
                 Object[] row = (Object[]) resultSetIterator.next();
                 Long siteId = this.getLongFromInteger(row[16]);
                 if(uniqueSites.get(siteId) == null) {
                     site =  new Site();
                     uniqueSites.put(siteId, site);
                 } else {
                     site = uniqueSites.get(siteId);
                 }
	            Merchant merchant = new Merchant();
	            Map<Long, Merchant> uniqueMerchants =  new HashMap<Long, Merchant>();
	            CreditUsageFee creditUsageFee =  new CreditUsageFee();
	            SubscriptionFee subscriptionFee =  new SubscriptionFee();
	            site.setName(this.getString(row[0]));
	            site.setId(this.getLongFromInteger(row[16]));
	            site.setTimeZone(this.getString(row[26]));
	            subscriptionFee.setFee(this.getDoubleFromBigDecimal(row[1]));
	            subscriptionFee.setTerm(this.getLongFromInteger(row[2]));
	            Code subscriptionType =  new Code();
	            subscriptionType.setCode(this.getString(row[3]));
	            subscriptionFee.setPaymentPeriod(subscriptionType);
	            Long merchantId = this.getLongFromInteger(row[17]);
	            if(uniqueMerchants.get(merchantId) == null) {
	                merchant = new Merchant();
	                merchant.setId(merchantId);
	                merchant.setUserName(this.getString(row[4]));
	                merchant.setPassword( row[5] == null ? null : this.getPbeStringEncryptor().decrypt(row[5].toString()));
	                merchant.setVendorName(this.getString(row[6]));
	                merchant.setPartner(this.getString(row[7]));
	                merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[19]));
	                merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[20]));
	                merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[22]));
	                merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[23]));
	                merchant.setMicroPaymentAccount(this.getBoolean(row[25]));
	            } else {
	                merchant = uniqueMerchants.get(merchantId);
	            }
	            creditUsageFee.setPercenteFee(this.getDoubleFromBigDecimal(row[8]));
	            creditUsageFee.setFlatFeeCutOff(this.getDoubleFromBigDecimal(row[9]));
	            creditUsageFee.setFlatFee(this.getDoubleFromBigDecimal(row[10]));
	            creditUsageFee.setDowngradeFee(this.getDoubleFromBigDecimal(row[15]));
	            creditUsageFee.setMicroTxFeeCutOff(this.getDoubleFromBigDecimal(row[29]));
	            site.setCardUsageFee(creditUsageFee);
	            site.addMerchant(merchant);
	            site.setEnableMicroTxOTC(this.getBoolean(row[27]));
	            site.setEnableMicroTxWeb(this.getBoolean(row[28]));
	            Access access = new Access();
	            List<Access> accessList = new LinkedList<Access>();
	            access.setId(this.getLongFromInteger(row[11]));
	            access.setCode(this.getString(row[12]));
	            access.setDescription(this.getString(row[13]));
	            access.setSubscriptionFee(subscriptionFee);
	            access.setAccessType(this.getAccessType(row[14]));
	            access.setAuthorizationRequired(this.getBoolean(row[18]));
	            access.setClientShare(this.getDoubleFromBigDecimal(row[21]));
	            access.setVisible(this.getBoolean(row[24]));
	            access.setFirmLevelAccess(this.getBoolean(row[30]));
	            accessDetailDTO.setSubFee(subscriptionFee);
	            accessList.add(access);
	            site.setAccess(accessList);
           	 }
           	accessDetailDTO.setSite(site);
        }
        return accessDetailDTO;
    }

    /**
     * Get the access ids for any of the paid access (recurring or pay as you go) by user id
     */
    public List<Long> getAccessIdsForPaidAccess(Long userId){
        Session session = currentSession();
        Query sqlQuery = session.getNamedQuery("GET_ACCESS_IDS_FOR_PAID_ACCESS")
                     .setParameter("userId", userId);
        List resultList = sqlQuery.list();

        List<Long> results = new ArrayList<Long>();
        for(Object intObj : resultList){
       	 results.add(this.getLongFromInteger(intObj));
        }
        return results;
    }

    public User getPaidSubUnpaidByUser(String userName, String nodeName) {
        Session session = currentSession();
        Query query = session.getNamedQuery("GET_PAID_SUB_UNPAID_BY_USER");
        query.setParameter("userName", userName);
        query.setParameter("nodeName", nodeName);
        List<Object> resultList = query.list();
        List<Site> siteList = new LinkedList<Site>();
        List<Access> accessList = null;
        Site site = null;
        User user = null;
        if(resultList.size()>0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            Map<Long, Site> uniqueSites =  new HashMap<Long, Site>();
            user = new User();
            while(resultListIterator.hasNext()) {
                Object[] row = (Object[]) resultListIterator.next();

                Long siteId = this.getLongFromInteger(row[0]);
                if(uniqueSites.get(siteId) == null) {
                    //Previously not there
                    site =  new Site();
                    accessList = new LinkedList<Access>();
                    site.setId(siteId);
                    site.setName(this.getString(row[1]));
                    siteList.add(site);
                    uniqueSites.put(siteId, site);
                } else {
                    site = uniqueSites.get(siteId);
                    accessList = site.getAccess();
                }
                Access access = new Access();

                access.setCode(this.getString(row[2]));
                access.setDescription(this.getString(row[3]));
                /** Setting Subscription Fee **/
                SubscriptionFee subscriptionFee = new SubscriptionFee();
                subscriptionFee.setFee(this.getDoubleFromBigDecimal(row[4]));
                Code subscriptionType =  new Code();
                subscriptionType.setDescription(this.getString(row[5]));
                subscriptionFee.setPaymentPeriod(subscriptionType);
                access.setAuthorizationRequired(this.getBoolean(row[8]));
                access.setAuthorized(this.getBoolean(row[9]));
                access.setAccessOverriden(this.getBoolean(row[10]));
                access.setFirmLevelAccess(this.getBoolean(row[11]));
                UserAccess userAccess = new UserAccess();
                userAccess.setFirmAccessAdmin(this.getBoolean(row[12]));
                userAccess.setActive(this.getBoolean(row[13]));
                userAccess.setFirmAdminUserAccessId(this.getLongFromInteger(row[14]));
                access.addUserAccess(userAccess);
                /**Set Subscription Fee to Access **/
                access.setSubscriptionFee(subscriptionFee);
                accessList.add(access);
                site.setAccess(accessList);
            }
            user.setSites(siteList);
        }
        return user;
    }

    public User getCurrentSubscriptions(String userName) {
        Session session = currentSession();
        Query query = session.getNamedQuery("GET_CURRENT_SUBSCRIPTIONS_FOR_USER");
        query.setParameter("userName", userName);
        List<Object> resultList = query.list();
        User user = null;
        if(resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            Object[] row = (Object[]) resultListIterator.next();
            user = new User();
            user.setId(this.getLongFromBigInteger(row[0]));
            List<Access> accessList = new LinkedList<Access>();
            resultListIterator.previous(); // To reset the iterator position to first row.
            while(resultListIterator.hasNext()){
                row = (Object[]) resultListIterator.next();
                Access access = new Access();
                access.setId(this.getLongFromInteger(row[1]));
                access.setAccessType(this.getAccessType(row[2]));
                accessList.add(access);
            }
            user.setAccess(accessList);
        }
        return user;
    }

    public User getUserPaymentInfo(String username, String nodeName) {
        Session session = currentSession();
        Query query = session.getNamedQuery("GET_USERPAYMENT_INFO");
        query.setParameter("username", username);
        query.setParameter("nodeName", nodeName);

        Site site = null;
        User user = null;
        CreditCard creditCard = null;
        List<Object> resultList = query.list();
        if(resultList.size() > 0) {
            List<Site> siteList = new LinkedList<Site>();
            List<UserAccount> userAccountList =  new LinkedList<UserAccount>();
            List<UserAccess> userAccessList =  new LinkedList<UserAccess>();
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            Map<Long, Site> uniqueSites =  new HashMap<Long, Site>();
            Long userId = null;
            user = new User();
            while(resultListIterator.hasNext()) {
                Object[] row = (Object[]) resultListIterator.next();
                Long siteId = this.getLongFromInteger(row[5]);
                if(uniqueSites.get(siteId) == null) {
                    //Previously not there
                    site =  new Site();
                    site.setId(siteId);
                    site.setName(this.getString(row[6]));
                    site.setTimeZone(this.getString(row[4]));
                    siteList.add(site);
                    uniqueSites.put(siteId, site);
                } else {
                    //Previously present
                    site = uniqueSites.get(siteId);
                }

                Merchant merchant = new Merchant();
                merchant.setUserName(this.getString(row[0]));
                merchant.setPassword( row[1] == null ? null : this.getPbeStringEncryptor().decrypt(row[1].toString()));
                merchant.setVendorName(this.getString(row[2]));
                merchant.setPartner(this.getString(row[3]));
                merchant.setId(this.getLongFromInteger(row[26]));
                merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[28]));
                merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[29]));
                merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[31]));
                merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[32]));

                UserAccess userAccess = new UserAccess();
                Long userAccessId = this.getLongFromInteger(row[24]);
                userAccess.setId(userAccessId);
                userAccess.setAuthorized(this.getBoolean(row[27]));
                Access access = new Access();
                List<Access> accessList = new LinkedList<Access>();
                Long accessId = this.getLongFromInteger(row[25]);
                access.setId(accessId);
                access.setCode(this.getString(row[7]));
                access.setDescription(this.getString(row[8]));
                access.setClientShare(this.getDoubleFromBigDecimal(row[30]));
                access.setFirmLevelAccess(this.getBoolean(row[38]));
                userAccess.setFirmAccessAdmin(this.getBoolean(row[39]));
                userAccess.setFirmAdminUserAccessId(this.getLongFromInteger(row[40]));
                userAccess.setAccess(access);

                Long userAccountId = this.getLongFromBigInteger(row[35]);


                if(userAccountId != null) {
                    UserAccount userAccount = new UserAccount();
                    userAccount.setId(userAccountId);
                    userAccount.setCreatedDate(this.getDate(row[37]));
                    UserAccess userAccessOfUserAccount = new UserAccess();
                    Long userAccessIdOfUserAccount = this.getLongFromInteger(row[36]);
                    userAccessOfUserAccount.setId(userAccessIdOfUserAccount);
                    userAccount.setUserAccess(userAccessOfUserAccount);
                    userAccountList.add(userAccount);
                    userAccess.setUserAccount(userAccount);
                }


                /** Setting Subscription Fee **/
                SubscriptionFee subscriptionFee = new SubscriptionFee();
                subscriptionFee.setFee(this.getDoubleFromBigDecimal(row[9]));
                subscriptionFee.setTerm(this.getLongFromInteger(row[10]));
                Code subscriptionType =  new Code();
                subscriptionType.setCode(this.getString(row[11]));
                subscriptionFee.setPaymentPeriod(subscriptionType);
                /**Set Subscription Fee to Access **/
                access.setSubscriptionFee(subscriptionFee);

                Long creditCardId = this.getLongFromInteger(row[12]);
                if(creditCardId != null) {
                    creditCard = new CreditCard();
                    creditCard.setId(creditCardId);
                    creditCard.setName(this.getString(row[13]));
                    creditCard.setNumber(row[14] == null ? null : this.getPbeStringEncryptor().decrypt(row[14].toString()));
                    creditCard.setExpiryMonth(this.getInteger(row[15]));
                    creditCard.setExpiryYear(this.getInteger(row[16]));
                    creditCard.setAddressLine1(this.getString(row[17]));
                    creditCard.setAddressLine2(this.getString(row[18]));
                    creditCard.setCity(this.getString(row[19]));
                    creditCard.setState(this.getString(row[20]));
                    creditCard.setZip(this.getString(row[21]));
                    creditCard.setPhone(this.getLongFromBigInteger(row[22]));
                    creditCard.setCreatedBy(this.getString(row[33]));
                    creditCard.setCreatedDate(this.getDate(row[34]));

                }
                site.addMerchant(merchant);
                accessList.add(access);
                site.setAccess(accessList);
                access.setSite(site);
                userId = this.getLongFromBigInteger(row[23]);
                userAccessList.add(userAccess);
            }

            user.setId(userId);
            user.setUserAccessList(userAccessList);
            user.setSites(siteList);
            user.setCreditCard(creditCard);
            user.setUserAccount(userAccountList);
        }

        return user;
    }

    public int disableUserAccount(Long userAccessId, String modifiedBy) {
        Boolean enableDisable = Boolean.FALSE;
        Session session = currentSession();
        int recordsModified = session.createQuery("Update UserAccount userAccount " +
            "Set userAccount.active = :isActive, " +
            "userAccount.modifiedDate = :modifiedDate, " +
            "userAccount.modifiedBy = :modifiedBy " +
            "Where userAccount.userAccess.id = :userAccessId")
            .setParameter("isActive", enableDisable)
            .setParameter("modifiedDate", new Date())
            .setParameter("modifiedBy", modifiedBy)
            .setParameter("userAccessId", userAccessId)
            .executeUpdate();
        return recordsModified;
    }

    public List<RecurTxSchedulerDTO> getCancelledSubscriptions() {
        List<RecurTxSchedulerDTO> payPalSchedulerDTOs = new LinkedList<RecurTxSchedulerDTO>();
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_CANCELLED_SUBSRCIPTIONS");
        List<Object> resultSet = sqlQuery.list();
        if (resultSet.size() > 0) {
            ListIterator<Object> resultSetIterator = (ListIterator<Object>) resultSet.listIterator();
            while(resultSetIterator.hasNext()) {
                Object[] row = (Object[]) resultSetIterator.next();
                RecurTxSchedulerDTO payPalSchedulerDTO = new RecurTxSchedulerDTO();
                payPalSchedulerDTO.setUserId(this.getLongFromBigInteger(row[0]));
                payPalSchedulerDTO.setUserAccessId(this.getLongFromInteger(row[1]));
                payPalSchedulerDTO.setAccessId(this.getLongFromInteger(row[2]));
                payPalSchedulerDTO.setUserName(this.getString(row[3]));
                payPalSchedulerDTO.setFirmLevelAccess(this.getBoolean(row[4]));
                payPalSchedulerDTO.setFirmAccessAdmin(this.getBoolean(row[5]));
                payPalSchedulerDTOs.add(payPalSchedulerDTO);

            }
       }
       return payPalSchedulerDTOs;
    }

    public void saveUserAccount(List<UserAccount> userAccounts) {
        Session session = currentSession();
        for(UserAccount userAccount: userAccounts){
            session.saveOrUpdate(userAccount);
        }
        session.flush();
    }

    public void saveUserAccount(UserAccount userAccount) {
        Session session = currentSession();
        session.saveOrUpdate(userAccount);
        session.flush();
    }

    public int updateUserAccessWithAccessId(List<Long> existingUserAccessIds, Long newAccessId, boolean isEnable,
            boolean enableUserAccessAuthorizedFlag, String modifiedBy, String comments) {
        Boolean enableDisable = Boolean.FALSE;
        if (isEnable) {
            enableDisable = Boolean.TRUE;
        }
        Boolean enableDisableUserAccessAuthorizedFlag = Boolean.FALSE;
        if (enableUserAccessAuthorizedFlag) {
            enableDisableUserAccessAuthorizedFlag = Boolean.TRUE;
        }
        Session session = currentSession();
        int recordsModified = session.createQuery("Update UserAccess userAccess " +
                  "Set userAccess.userAccessCompositePrimaryKey.accessId = :accessId, " +
                  "userAccess.modifiedDate = :modifiedDate, " +
                  "userAccess.modifiedBy = :modifiedBy, " +
                  "userAccess.active = :isActive, " +
                  "userAccess.comments = :comments, " +
                  "userAccess.isAuthorized = :isAuthorized, " +
                  "userAccess.accessOverriden = :isAccessOverriden " +
                  "Where userAccess.id in (:existingUserAccessIds)")
                  .setParameter("accessId", newAccessId)
                  .setParameter("modifiedDate", new Date())
                  .setParameterList("existingUserAccessIds", existingUserAccessIds)
                  .setParameter("modifiedBy", modifiedBy)
                  .setParameter("comments", comments)
                  .setParameter("isActive", enableDisable)
                  .setParameter("isAuthorized", enableDisableUserAccessAuthorizedFlag)
                  .setParameter("isAccessOverriden", false)
                  .executeUpdate();
        return recordsModified;
    }


    public int enableDisableCreditCard(Long userAccessId, boolean isEnable, String modifiedBy) {
        Boolean enableDisable = Boolean.FALSE;
        if (isEnable) {
            enableDisable = Boolean.TRUE;
        }
        Session session = currentSession();
        int recordsModified = session.createQuery("Update CreditCard creditCard " +
                                "Set creditCard.active = :isActive, " +
                                "creditCard.modifiedDate = :modifiedDate, " +
                                "creditCard.modifiedBy = :modifiedBy " +
                                "Where creditCard.id = (Select userAccount.creditCard.id From UserAccount userAccount " +
                                "Where userAccount.userAccess.id =:userAccessId ) ")
                                .setParameter("isActive", enableDisable)
                                .setParameter("userAccessId", userAccessId)
                                .setParameter("modifiedDate", new Date())
                                .setParameter("modifiedBy", modifiedBy)
                                .executeUpdate();
        return recordsModified;
    }

    public List<Access> getAccessBasedOnSiteForUser(String username, String siteName) {
        List<Access> accessList = new LinkedList<Access>();
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_ACCESS_BASED_ON_SITE_FOR_A_USER")
                                            .setParameter("username", username)
                                            .setParameter("siteName", siteName);
        List<Object> resultList = sqlQuery.list();
        if(resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                Object[] row = (Object[]) resultListIterator.next();
                Access access  = new Access();
                access.setId(this.getLongFromInteger(row[0]));
                access.setCode(this.getString(row[1]));
                access.setDescription(this.getString(row[2]));
                Site site = new Site();
                site.setId(this.getLongFromInteger(row[3]));
                access.setSite(site);
                accessList.add(access);
            }
        }
        return accessList;
    }

    public int updateBillingDates(Long userAccountId, Date lastBillingDate, Date nextBillingDate, boolean isVerified,
        String modifiedBy) {
        Session session = currentSession();
        int noOfRecordsUpdated = session.createQuery("Update UserAccount userAccount " +
            "Set userAccount.lastBillingDate = :lastBillingDate, " +
            "userAccount.nextBillingDate = :nextBillingDate, " +
            "userAccount.modifiedDate = :modifiedDate, " +
            "userAccount.isVerified = :isVerified, " +
            "userAccount.modifiedBy = :modifiedBy " +
            "Where userAccount.id = :userAccountId ")
        .setParameter("lastBillingDate", lastBillingDate)
        .setParameter("nextBillingDate", nextBillingDate)
        .setParameter("isVerified", isVerified)
        .setParameter("modifiedDate", new Date())
        .setParameter("modifiedBy", modifiedBy)
        .setParameter("userAccountId", userAccountId)
        .executeUpdate();
        return noOfRecordsUpdated;
    }

    public User getRecurSubAccountInfo(String username) {
        Session session = currentSession();
        Query query = session.getNamedQuery("GET_RECURRING_ACCOUNT_INFO");
        query.setParameter("username", username);
        List<Access> accessList = new LinkedList<Access>();
        UserAccount userAccount = null;
        Site site = null;
        Access access = null;
        User user = null;
        CreditCard  creditCard = null;
        List<Object> resultSet = query.list();
        user = new User();
        if(resultSet.size() > 0) {
            ListIterator<Object> resultSetIterator = (ListIterator<Object>) resultSet.listIterator();
            Map<Long, Access> uniqueAccessList =  new HashMap<Long, Access>();
            Long userId = null;
            while(resultSetIterator.hasNext()) {
                Object[] row = (Object[]) resultSetIterator.next();
                Long siteId = this.getLongFromInteger(row[0]);
                Long accessId = this.getLongFromInteger(row[13]);
                if(uniqueAccessList.get(accessId) == null) {
                    access = new Access();
                    site =  new Site();
                    site.setId(siteId);
                    access.setId(accessId);
                    access.setClientShare(this.getDoubleFromBigDecimal(row[11]));
                    userAccount = new UserAccount();
                    userAccount.setMarkForCancellation(this.getBoolean(row[12]));
                    userAccount.setActive(this.getBoolean(row[16]));
                    access.setUserAccount(userAccount);
                    access.setSite(site);
                    accessList.add(access);
                    uniqueAccessList.put(accessId, access);
                } else {
                    access = uniqueAccessList.get(accessId);
                    site = access.getSite();
                }
                Merchant merchant = new Merchant();
                merchant.setUserName(this.getString(row[1]));
                merchant.setPassword(row[2] == null ? null : this.getPbeStringEncryptor().decrypt(row[2].toString()));
                merchant.setVendorName(this.getString(row[3]));
                merchant.setPartner(this.getString(row[4]));
                merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[9]));
                merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[10]));
                merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[14]));
                merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[15]));
                site.addMerchant(merchant);
                userId = this.getLongFromBigInteger(row[6]);
                Long creditCardId = this.getLongFromInteger(row[5]);
                if(creditCardId != null) {
                    creditCard = new CreditCard();
                    creditCard.setId(creditCardId);
                    creditCard.setCreatedDate(this.getDate(row[7]));
                    creditCard.setCreatedBy(this.getString(row[17]));
                }
            }
            user.setId(userId);
            user.setCreditCard(creditCard);
            user.setAccess(accessList);
        }
        return user;
    }

}