package com.fdt.ecom.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.fdt.achtx.entity.CheckHistory;
import com.fdt.common.dao.AbstractBaseDAOImpl;
import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.entity.ErrorCode;
import com.fdt.common.entity.Tx;
import com.fdt.ecom.dto.UserCountDTO;
import com.fdt.ecom.entity.BankDetails;
import com.fdt.ecom.entity.Code;
import com.fdt.ecom.entity.CreditUsageFee;
import com.fdt.ecom.entity.Merchant;
import com.fdt.ecom.entity.Node;
import com.fdt.ecom.entity.NodeConfiguration;
import com.fdt.ecom.entity.NonRecurringFee;
import com.fdt.ecom.entity.ReceiptConfiguration;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.SiteConfiguration;
import com.fdt.ecom.entity.Term;
import com.fdt.ecom.entity.TermType;
import com.fdt.ecom.entity.UserTerm;
import com.fdt.ecom.entity.WebPaymentFee;
import com.fdt.ecom.entity.enums.PaymentType;
import com.fdt.otctx.entity.MagensaInfo;
import com.fdt.security.entity.Access;
import com.fdt.subscriptions.entity.SubscriptionFee;

@Repository
public class EComDAOImpl extends AbstractBaseDAOImpl implements EComDAO {

    private static final Logger logger = LoggerFactory.getLogger(EComDAOImpl.class);


    @Cacheable("getSitesForNode")
    public List<Site> getSitesForNode(String nodeName) {
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_SITES_FOR_NODE")
                            .setParameter("nodeName", nodeName);
        List<Object> resultList = sqlQuery.list();
        Site site = null;
        List<Site> sites = new LinkedList<Site>();
        List<Access> accessList = null;
        if (resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            Map<Long, Site> uniqueSites =  new HashMap<Long, Site>();
            while(resultListIterator.hasNext()) {
                Object[] row = (Object[]) resultListIterator.next();
                Long siteId = this.getLongFromInteger(row[0]);
                if(uniqueSites.get(siteId) == null) {
                    //Previously not there
                    site =  new Site();
                    accessList = new LinkedList<Access>();
                    site.setAccess(accessList);
                    site.setId(siteId);
                    site.setName(this.getString(row[1]));
                    site.setFirmNumberRequired(this.getBoolean(row[2]));
                    site.setBarNumberRequired(this.getBoolean(row[3]));
                    site.setFreeSite(this.getBoolean(row[13]));
                    site.setSubscriptionValidationText(this.getString(row[10]));
                    site.setDescription(this.getString(row[11]));
                    site.setState(this.getString(row[13]));
                    sites.add(site);
                    uniqueSites.put(siteId, site);
                } else {
                    site = uniqueSites.get(siteId);
                    accessList = site.getAccess();
                }
                Access access = new Access();
                access.setId(this.getLongFromInteger(row[4]));
                access.setCode(this.getString(row[5]));
                access.setDescription(this.getString(row[6]));
                access.setGuestFlg(this.getBoolean(row[7]));
                access.setAccessType(this.getAccessType(row[8]));
                access.setVisible(this.getBoolean(row[12]));
                SubscriptionFee subscriptionFee = new SubscriptionFee();
                subscriptionFee.setFee(this.getDoubleFromBigDecimal(row[9]));
                access.setSubscriptionFee(subscriptionFee);
                accessList.add(access);
            }
        }
        return sites;
    }

    @Cacheable("getAccessesForSite")
    public List<Access> getAccessesForSite(String siteId) {
        Session session = currentSession();
        Long siteIdValue = Long.parseLong(siteId);
        Query sqlQuery =  session.getNamedQuery("GET_ACCESS_FOR_SITE")
                            .setParameter("siteId", siteIdValue.longValue());
        List<Object> resultSet = sqlQuery.list();
        List<Access> accessList = new LinkedList<Access>();
        if (resultSet.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultSet.listIterator();
            while(resultListIterator.hasNext()) {
                Object[] row = (Object[]) resultListIterator.next();
                Access access =  new Access();
                access.setId(this.getLongFromInteger(row[0]));
                access.setCode(this.getString(row[1]));
                access.setDescription(this.getString(row[2]));
                access.setGuestFlg(this.getBoolean(row[3]));
                access.setAccessType(this.getAccessType(row[7]));
                access.setAccessFeatures(this.getString(row[8]));
                access.setDefaultAccessFlg(this.getBoolean(row[9]));
                access.setVisible(this.getBoolean(row[10]));
                SubscriptionFee subscriptionFee = new SubscriptionFee();
                subscriptionFee.setFee(this.getDoubleFromBigDecimal(row[4]));
                Code subscriptionType = new Code();
                subscriptionType.setCode(this.getString(row[5]));
                subscriptionType.setDescription(this.getString(row[6]));
                subscriptionFee.setPaymentPeriod(subscriptionType);
                access.setSubscriptionFee(subscriptionFee);
                accessList.add(access);
            }
        }
        return accessList;
    }

    @Cacheable("getSiteDetails")
    public Site getSiteDetails(Long siteId) {
        Session session = currentSession();
        Query query = session.getNamedQuery("GET_SITE_DETAILS")
                             .setParameter("siteId", siteId);
        Site site = null;
        List siteList = query.list();
        if(siteList.size() > 0){
            ListIterator<Object> resultSetIterator = (ListIterator<Object>) siteList.listIterator();
            while(resultSetIterator.hasNext()) {
                Object[] row = (Object[]) resultSetIterator.next();
                site = new Site();
                Merchant merchant = new Merchant();
                Term term = new Term();
                CreditUsageFee creditUsageFee = new CreditUsageFee();
                MagensaInfo magensaInfo =  new MagensaInfo();
                site.setId(siteId);
                site.setName(this.getString(row[1]));
                site.setDescription(this.getString(row[2]));
                site.setActive(this.getBoolean(row[3]));
                site.setCounty(this.getString(row[4]));
                site.setState(this.getString(row[5]));
                site.setAutoActivate(this.getBoolean(row[6]));
                site.setTimeZone(this.getString(row[7]));
                site.setModifiedBy(this.getString(row[9]));
                site.setRevenueThresholdAmount(this.getDoubleFromBigDecimal(row[50]));
                site.setRevenueThresholdStartDate(this.getDate(row[51]));
                term.setId(this.getLongFromInteger(row[10]));
                term.setDescription(this.getString(row[11]));
                term.setModifiedBy(this.getString(row[12]));
                term.setDefault(this.getBoolean(row[13]));
                term.setActive(this.getBoolean(row[15]));
                merchant.setId(this.getLongFromInteger(row[16]));
                merchant.setUserName(this.getString(row[17]));
                merchant.setPassword(row[18] == null ? null : this.getPbeStringEncryptor().decrypt(row[18].toString()));
                merchant.setVendorName(this.getString(row[19]));
                merchant.setPartner(this.getString(row[20]));
                merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[43]));
                merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[44]));
                merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[45]));
                merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[46]));
                creditUsageFee.setId(this.getLongFromInteger(row[21]));
                creditUsageFee.setFlatFee(this.getDoubleFromBigDecimal(row[22]));
                creditUsageFee.setFlatFeeCutOff(this.getDoubleFromBigDecimal(row[23]));
                creditUsageFee.setPercenteFee(this.getDoubleFromBigDecimal(row[37]));
                creditUsageFee.setAdditionalFee(this.getDoubleFromBigDecimal(row[24]));
                creditUsageFee.setActive(this.getBoolean(row[25]));
                creditUsageFee.setDowngradeFee(this.getDoubleFromBigDecimal(row[26]));
                magensaInfo.setHostId(this.getString(row[27]));
                magensaInfo.setHostPassword(row[28] == null ? null : this.getPbeStringEncryptor().decrypt(row[28].toString()));
                magensaInfo.setRegisteredBy(this.getString(row[29]));
                magensaInfo.setEncryptionBlockType(this.getString(row[30]));
                magensaInfo.setCardType(this.getString(row[31]));
                magensaInfo.setOutputFormatCode(this.getString(row[32]));
                magensaInfo.setActive(this.getBoolean(row[33]));
                site.setCreatedBy(this.getString(row[38]));
                term.setCreatedBy(this.getString(row[39]));
                merchant.setCreatedBy(this.getString(row[40]));
                creditUsageFee.setCreatedBy(this.getString(row[41]));
                magensaInfo.setCreatedBy(this.getString(row[42]));
                site.setMagensaInfo(magensaInfo);
                site.addMerchant(merchant);
                site.setTerm(term);
                site.setCardUsageFee(creditUsageFee);
                site.setFirmNumberRequired(this.getBoolean(row[47]));
                site.setBarNumberRequired(this.getBoolean(row[48]));
                site.setFreeSite(this.getBoolean(row[49]));
            }
        }
        return site;
    }

    @Cacheable("getSiteDetailsBySiteName")
    public Site getSiteDetailsBySiteName(String siteName) {
        Session session = currentSession();
        Query query = session.getNamedQuery("GET_SITE_DETAILS_BY_NAME")
                             .setParameter("siteName", siteName);
        Site site = null;
        List siteList = query.list();
        if(siteList.size() > 0){
            ListIterator<Object> resultSetIterator = (ListIterator<Object>) siteList.listIterator();
            Map<Long, Site> uniqueSites =  new HashMap<Long, Site>();
            while(resultSetIterator.hasNext()) {
                Object[] row = (Object[]) resultSetIterator.next();
                Long siteId = this.getLongFromInteger(row[0]);
                if(uniqueSites.get(siteId) == null) {
                    site =  new Site();
                    uniqueSites.put(siteId, site);
                } else {
                    site = uniqueSites.get(siteId);
                }
                Merchant merchant = new Merchant();
                Term term = new Term();
                CreditUsageFee creditUsageFee = new CreditUsageFee();
                WebPaymentFee webPaymentFee = new WebPaymentFee();
                MagensaInfo magensaInfo =  new MagensaInfo();
                site.setId(this.getLongFromInteger(row[0]));
                site.setName(this.getString(row[1]));
                site.setDescription(this.getString(row[2]));
                site.setActive(this.getBoolean(row[3]));
                site.setCounty(this.getString(row[4]));
                site.setState(this.getString(row[5]));
                site.setAutoActivate(this.getBoolean(row[6]));
                site.setTimeZone(this.getString(row[7]));
                site.setModifiedBy(this.getString(row[8]));
                site.setEnableMicroTxOTC(this.getBoolean(row[9]));
                site.setEnableMicroTxWeb(this.getBoolean(row[10]));
                site.setSearchDayThreshold(this.getLongFromInteger(row[52]));
                term.setId(this.getLongFromInteger(row[11]));
                term.setDescription(this.getString(row[12]));
                term.setModifiedBy(this.getString(row[13]));
                term.setDefault(this.getBoolean(row[14]));
                term.setActive(this.getBoolean(row[15]));
                merchant.setId(this.getLongFromInteger(row[16]));
                merchant.setUserName(this.getString(row[17]));
                merchant.setPassword(row[18] == null ? null : this.getPbeStringEncryptor().decrypt(row[18].toString()));
                merchant.setVendorName(this.getString(row[19]));
                merchant.setPartner(this.getString(row[20]));
                merchant.setMicroPaymentAccount(this.getBoolean(row[21]));
                merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[50]));
                merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[51]));
                merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[53]));
                merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[54]));
                creditUsageFee.setId(this.getLongFromInteger(row[22]));
                creditUsageFee.setFlatFee(this.getDoubleFromBigDecimal(row[23]));
                creditUsageFee.setFlatFeeCutOff(this.getDoubleFromBigDecimal(row[24]));
                creditUsageFee.setPercenteFee(this.getDoubleFromBigDecimal(row[36]));
                creditUsageFee.setAdditionalFee(this.getDoubleFromBigDecimal(row[25]));
                creditUsageFee.setActive(this.getBoolean(row[26]));
                creditUsageFee.setDowngradeFee(this.getDoubleFromBigDecimal(row[27]));
                creditUsageFee.setMicroTxFeeCutOff(this.getDoubleFromBigDecimal(row[28]));
                magensaInfo.setHostId(this.getString(row[29]));
                magensaInfo.setHostPassword(row[30] == null ? null : this.getPbeStringEncryptor().decrypt(row[30].toString()));
                magensaInfo.setRegisteredBy(this.getString(row[31]));
                magensaInfo.setEncryptionBlockType(this.getString(row[32]));
                magensaInfo.setCardType(this.getString(row[33]));
                magensaInfo.setOutputFormatCode(this.getString(row[34]));
                magensaInfo.setActive(this.getBoolean(row[35]));
                site.setMagensaInfo(magensaInfo);
                site.addMerchant(merchant);
                site.setTerm(term);
                site.setCardUsageFee(creditUsageFee);
                webPaymentFee.setId(this.getLongFromInteger(row[37]));
                webPaymentFee.setFlatFee(this.getDoubleFromBigDecimal(row[38]));
                webPaymentFee.setFlatFeeCutOff(this.getDoubleFromBigDecimal(row[39]));
                webPaymentFee.setPercenteFee(this.getDoubleFromBigDecimal(row[40]));
                webPaymentFee.setAdditionalFee(this.getDoubleFromBigDecimal(row[41]));
                webPaymentFee.setActive(this.getBoolean(row[42]));
                webPaymentFee.setMicroTxFeeCutOff(this.getDoubleFromBigDecimal(row[43]));
                site.setCreatedBy(this.getString(row[44]));
                term.setCreatedBy(this.getString(row[45]));
                merchant.setCreatedBy(this.getString(row[46]));
                creditUsageFee.setCreatedBy(this.getString(row[47]));
                webPaymentFee.setCreatedBy(this.getString(row[48]));
                magensaInfo.setCreatedBy(this.getString(row[49]));
                site.setWebPaymentFee(webPaymentFee);
            }
        }
        return site;
    }

    public void saveUserTerm(List<UserTerm> userTerms) {
        Session session = currentSession();
        for(UserTerm userTerm : userTerms) {
            session.saveOrUpdate(userTerm);
        }
        session.flush();
    }

    public void saveUserTerm(UserTerm userTerm) {
        Session session = currentSession();
        session.saveOrUpdate(userTerm);
        session.flush();
    }

    public void saveErrorCode(ErrorCode errorCode) {
        Session session = currentSession();
        session.saveOrUpdate(errorCode);
        session.flush();
    }

    @Cacheable("getSites")
    public List<Site> getSites() {
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_SITES");
        List<Object> resultList = sqlQuery.list();
        Site site = null;
        List<Site> sites = null;
        if (resultList.size() > 0) {
            sites = new LinkedList<Site>();
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                Object[] row = (Object[]) resultListIterator.next();
                site =  new Site();
                site.setId(this.getLongFromInteger(row[0]));
                site.setName(this.getString(row[1]));
                site.setDescription(this.getString(row[2]));
                site.setCounty(this.getString(row[3]));
                site.setState(this.getString(row[4]));
                site.setAutoActivate(this.getBoolean(row[5]));
                site.setTimeZone(this.getString(row[6]));
                site.setActive(this.getBoolean(row[7]));
                Node node = new Node();
                node.setName(this.getString(row[8]));
                node.setId(this.getLongFromShort(row[9]));
                node.setDescription(this.getString(row[10]));
                site.setAchEnabled(this.getBoolean(row[11]));
                site.setFreeSite(this.getBoolean(row[12]));
                site.setNode(node);
                sites.add(site);
            }
        }
        return sites;
    }

    @Cacheable("getNodeConfiguration")
    public NodeConfiguration getNodeConfiguration(String nodeName) {
        NodeConfiguration nodeConfiguration = null;
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_NODE_CONFIGURATION")
                                            .setParameter("nodeName", nodeName);
        List<Object> resultList = sqlQuery.list();
        if(!resultList.isEmpty()){
            Object[] row = (Object[]) resultList.get(0);
            nodeConfiguration = new NodeConfiguration();
            nodeConfiguration.setId(this.getLongFromInteger(row[0]));
            nodeConfiguration.setNodeId(this.getLongFromShort(row[1]));
            nodeConfiguration.setNodeName(nodeName);
            nodeConfiguration.setNodeDescription(this.getString(row[2]));
            nodeConfiguration.setEmailTemplateFolder(this.getString(row[3]));
            nodeConfiguration.setFromEmailAddress(this.getString(row[4]));
            nodeConfiguration.setResetPasswordSubject(this.getString(row[5]));
            nodeConfiguration.setUserActivationSubject(this.getString(row[6]));
            nodeConfiguration.setResetPasswordEmailTemplate(this.getString(row[7]));
            nodeConfiguration.setUserActivationEmailTemplate(this.getString(row[8]));
            nodeConfiguration.setCreatedDate(this.getDate(row[9]));
            nodeConfiguration.setModifiedDate(this.getDate(row[10]));
            nodeConfiguration.setModifiedBy(this.getString(row[11]));
            nodeConfiguration.setActive(this.getBoolean(row[12]));
            nodeConfiguration.setLockUserSub(this.getString(row[13]));
            nodeConfiguration.setLockUserEmailTemplate(this.getString(row[14]));
            nodeConfiguration.setUnlockUserSub(this.getString(row[15]));
            nodeConfiguration.setUnlockUserEmailTemplate(this.getString(row[16]));
            nodeConfiguration.setAlertSubject(this.getString(row[17]));
            nodeConfiguration.setAlertTemplate(this.getString(row[18]));
            nodeConfiguration.setCreatedBy(this.getString(row[19]));
            nodeConfiguration.setInActiveUserNotifSubject(this.getString(row[20]));
            nodeConfiguration.setInActiveUserNotifTemplate(this.getString(row[21]));
        }
        return nodeConfiguration;
    }

    @Cacheable("getSiteConfiguration")
    public SiteConfiguration getSiteConfiguration(Long siteId) {
        SiteConfiguration siteConfiguration = null;
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_SITE_CONFIGURATION")
                                            .setParameter("siteId", siteId);
        List<Object> resultList = sqlQuery.list();
        if(!resultList.isEmpty()) {
            Object[] row = (Object[]) resultList.get(0);
            siteConfiguration = new SiteConfiguration();
            siteConfiguration.setId(this.getLongFromInteger(row[0]));
            siteConfiguration.setSiteId(this.getLongFromInteger(row[1]));
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
            siteConfiguration.setRemoveSubscriptionSubject(this.getString(row[23]));
            siteConfiguration.setRemoveSubscriptionTemplate(this.getString(row[24]));
            siteConfiguration.setCreatedBy(this.getString(row[25]));
            siteConfiguration.setAccessAuthorizationSubject(this.getString(row[26]));
            siteConfiguration.setAccessAuthorizationTemplate(this.getString(row[27]));
            siteConfiguration.setPayAsUGoPaymentConfSubject(this.getString(row[28]));
            siteConfiguration.setPayAsUGoPaymentConfTemplate(this.getString(row[29]));
            siteConfiguration.setAddSubscriptionSub(this.getString(row[30]));
            siteConfiguration.setExpiredOverriddenSubscriptionNotificationSubject(this.getString(row[31]));
            siteConfiguration.setExpiredOverriddenSubscriptionNotificationTemplate(this.getString(row[32]));
        }
        return siteConfiguration;
    }

    public Map<String, Access> getAccessListWithNonRecurringFee(List<String> accessCodeList){
        List<String> newAccessCodeList = new LinkedList<String>();
        for(String accessCode : accessCodeList) {
            if (!StringUtils.isEmpty(accessCode) || accessCode != null) {
                newAccessCodeList.add(accessCode);
            }
        }
        NonRecurringFee nonRecurringFee = null;
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_ACCESS_LIST_WITH_RECURRING_FEE")
                                  .setParameterList("accessCodeList", newAccessCodeList);
        List<Object> resultList = sqlQuery.list();
        Map<String, Access> accessNameAcccessMap = null;
        Long accessId = null;
        if (resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            accessNameAcccessMap = new LinkedHashMap<String, Access>();
            Access access = null;
            while(resultListIterator.hasNext()) {
                Object[] row = (Object[]) resultListIterator.next();
                accessId = this.getLongFromInteger(row[3]);
                String accessName = this.getString(row[13]);
                if(accessNameAcccessMap.get(accessName) == null) {
                    access = new Access();
                    access.setId(accessId);
                    access.setFirmLevelAccess(this.getBoolean(row[15]));
                    access.setMaxUsersAllowed(this.getInteger(row[16]));
                    access.setMaxDocumentsAllowed(this.getInteger(row[17]));
                    access.setGovernmentAccess(this.getBoolean(row[19]));
                    accessNameAcccessMap.put(accessName, access);
                } else {
                    access = accessNameAcccessMap.get(accessName);
                }
                nonRecurringFee = new NonRecurringFee();
                nonRecurringFee.setId(this.getLongFromInteger(row[0]));
                Code code = new Code();
                code.setId(this.getLongFromInteger(row[1]));
                code.setCode(this.getString(row[2]));
                nonRecurringFee.setCode(code);
                nonRecurringFee.setFeeUnderPageThreshold(this.getDoubleFromBigDecimal(row[4]));
                nonRecurringFee.setPageThreshold(this.getLongFromInteger(row[5]));
                nonRecurringFee.setFeeOverPageThreshold(this.getDoubleFromBigDecimal(row[6]));
                nonRecurringFee.setServiceFee(this.getBoolean(row[7]));
                nonRecurringFee.setCurrency(this.getString(row[8]));
                nonRecurringFee.setActive(this.getBoolean(row[9]));
                nonRecurringFee.setCreatedDate(this.getDate(row[10]));
                nonRecurringFee.setModifiedDate(this.getDate(row[11]));
                nonRecurringFee.setModifiedBy(this.getString(row[12]));
                nonRecurringFee.setCreatedBy(this.getString(row[14]));
                nonRecurringFee.setSumTxamountPlusServiceFee(this.getBoolean(row[18]));
                access.addNonRecurringFee(nonRecurringFee);

           }
        }
        return accessNameAcccessMap;
    }

    @CacheEvict(value="getSiteConfiguration", allEntries=true)
    public void updateSiteConfiguration(SiteConfiguration siteConfiguration) {
        Session session = currentSession();
        session.createQuery("UPDATE SiteConfiguration siteConfiguration " +
            "SET siteConfiguration.fromEmailAddress = :fromEmailAddress, " +
            "siteConfiguration.paymentConfirmationSubject = :paymentConfirmationSubject, " +
            "siteConfiguration.changeSubscriptionSubject = :changeSubscriptionSubject, " +
            "siteConfiguration.cancelSubscriptionSubject = :cancelSubscriptionSubject, " +
            "siteConfiguration.reactivateSubscriptionSubject = :reactivateSubscriptionSubject, " +
            "siteConfiguration.recurringPaymentSuccessSubject = :recurringPaymentSuccessSubject, " +
            "siteConfiguration.recurringPaymentUnsuccessfulSubject = :recurringPaymentUnsuccessfulSubject, " +
            "siteConfiguration.webPaymentConfSubject = :webPaymentConfSubject, " +
            "siteConfiguration.payAsUGoPaymentConfSubject = :payAsUGoPaymentConfSubject, " +
            "siteConfiguration.removeSubscriptionSubject = :removeSubscriptionSubject, " +
            "siteConfiguration.accessAuthorizationSubject = :accessAuthorizationSubject, " +
            "siteConfiguration.expiredOverriddenSubscriptionNotificationSubject = :expiredOverriddenSubscriptionNotificationSubject, " +           
            "siteConfiguration.modifiedDate = :modifiedDate, " +
            "siteConfiguration.modifiedBy = :modifiedBy " +
            "WHERE siteConfiguration.siteId = :siteId) ")
        .setParameter("fromEmailAddress", siteConfiguration.getFromEmailAddress())
        .setParameter("paymentConfirmationSubject", siteConfiguration.getPaymentConfirmationSubject())
        .setParameter("changeSubscriptionSubject", siteConfiguration.getChangeSubscriptionSubject())
        .setParameter("cancelSubscriptionSubject", siteConfiguration.getCancelSubscriptionSubject())
        .setParameter("reactivateSubscriptionSubject", siteConfiguration.getReactivateSubscriptionSubject())
        .setParameter("recurringPaymentSuccessSubject", siteConfiguration.getRecurringPaymentSuccessSubject())
        .setParameter("recurringPaymentUnsuccessfulSubject", siteConfiguration.getRecurringPaymentUnsuccessfulSubject())
        .setParameter("webPaymentConfSubject", siteConfiguration.getWebPaymentConfSubject())
        .setParameter("payAsUGoPaymentConfSubject", siteConfiguration.getPayAsUGoPaymentConfSubject())
        .setParameter("removeSubscriptionSubject", siteConfiguration.getRemoveSubscriptionSubject())
        .setParameter("accessAuthorizationSubject", siteConfiguration.getAccessAuthorizationSubject())
        .setParameter("expiredOverriddenSubscriptionNotificationSubject", siteConfiguration.getExpiredOverriddenSubscriptionNotificationSubject())        
        .setParameter("modifiedDate", new Date())
        .setParameter("modifiedBy", siteConfiguration.getModifiedBy())
        .setParameter("siteId", siteConfiguration.getSiteId())
        .executeUpdate();
    }

    @CacheEvict(value="getNodeConfiguration", allEntries=true)
    public void updateNodeConfiguration(NodeConfiguration nodeConfiguration) {
        Session session = currentSession();
        session.createQuery("UPDATE NodeConfiguration nodeConfiguration " +
            "SET nodeConfiguration.fromEmailAddress = :fromEmailAddress, " +
            "nodeConfiguration.resetPasswordSubject = :resetPasswordSubject, " +
            "nodeConfiguration.userActivationSubject = :userActivationSubject, " +
            "nodeConfiguration.modifiedDate = :modifiedDate, " +
            "nodeConfiguration.lockUserSub = :lockUserSub, " +
            "nodeConfiguration.unlockUserSub = :unlockUserSub, " +
            "nodeConfiguration.alertSubject = :alertSubject, " +
            "nodeConfiguration.inActiveUserNotifSubject = :inActiveUserNotifSubject, " +
            "nodeConfiguration.modifiedBy = :modifiedBy " +
            "WHERE nodeConfiguration.nodeId = (Select node.id from Node node Where node.name = :nodeName) ")
            .setParameter("fromEmailAddress", nodeConfiguration.getFromEmailAddress())
            .setParameter("resetPasswordSubject", nodeConfiguration.getResetPasswordSubject())
            .setParameter("userActivationSubject", nodeConfiguration.getUserActivationSubject())
            .setParameter("modifiedDate", new Date())
            .setParameter("lockUserSub", nodeConfiguration.getLockUserSub())
            .setParameter("unlockUserSub", nodeConfiguration.getUnlockUserSub())
            .setParameter("alertSubject", nodeConfiguration.getAlertSubject())
            .setParameter("inActiveUserNotifSubject", nodeConfiguration.getInActiveUserNotifSubject())
            .setParameter("modifiedBy", nodeConfiguration.getModifiedBy())
            .setParameter("nodeName", nodeConfiguration.getNodeName())
        .executeUpdate();
    }

    @Cacheable("getCodes")
    public List<Code> getCodes(String codeCategory) {
        List<Code> codes = new LinkedList<Code>();
        Session session = currentSession();
        Query sqlQuery  = session.getNamedQuery("GET_CODES")
                                    .setParameter("codeCategory", codeCategory);
        List<Object> resultSet = sqlQuery.list();
        if (resultSet.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultSet.listIterator();
            while(resultListIterator.hasNext()) {
                Code code = new Code();
                Object[] row = (Object[]) resultListIterator.next();
                code.setId(this.getLongFromInteger(row[0]));
                code.setCode(this.getString(row[1]));
                code.setDescription(this.getString(row[2]));
                code.setActive(this.getBoolean(row[3]));
                code.setCreatedDate(this.getDate(row[4]));
                code.setModifiedDate(this.getDate(row[5]));
                code.setModifiedBy(this.getString(row[6]));
                code.setCategory(this.getString(row[7]));
                code.setCreatedBy(this.getString(row[8]));
                codes.add(code);
            }
        }
        return codes;
    }

    @Cacheable("getReceiptConfigurationForSiteName")
    public List<ReceiptConfiguration> getReceiptConfigurationForSiteAndPaymentType(String siteName, PaymentType paymentType) {
        List<ReceiptConfiguration> receiptConfigurationList = new LinkedList<ReceiptConfiguration>();
        Session session = currentSession();
        Query sqlQuery  = session.getNamedQuery("GET_RECEIPT_CONFIGURATION_FOR_SITE_NAME_AND_PAYMENT_TYPE")
                                    .setParameter("siteName", siteName)
                                    .setParameter("paymentType", paymentType.toString());
        List<Object> resultSet = sqlQuery.list();
        if (resultSet.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultSet.listIterator();
            while(resultListIterator.hasNext()) {
                ReceiptConfiguration receiptConfiguration = new ReceiptConfiguration();
                Object[] row = (Object[]) resultListIterator.next();
                receiptConfiguration.setId(this.getLongFromInteger(row[0]));
                receiptConfiguration.setBusinessName(this.getString(row[1]));
                receiptConfiguration.setAddressLine1(this.getString(row[2]));
                receiptConfiguration.setAddressLine2(this.getString(row[3]));
                receiptConfiguration.setCity(this.getString(row[4]));
                receiptConfiguration.setState(this.getString(row[5]));
                receiptConfiguration.setZip(this.getString(row[6]));
                receiptConfiguration.setPhone(this.getString(row[7]));
                receiptConfiguration.setComments1(this.getString(row[8]));
                receiptConfiguration.setComments2(this.getString(row[9]));
                receiptConfiguration.setType(this.getPaymentType(this.getString(row[10])));
                receiptConfiguration.setActive(this.getBoolean(row[11]));
                receiptConfiguration.setCreatedDate(this.getDate(row[12]));
                receiptConfiguration.setModifiedDate(this.getDate(row[13]));
                receiptConfiguration.setModifiedBy(this.getString(row[14]));
                receiptConfiguration.setSiteId(this.getLongFromInteger(row[15]));
                receiptConfiguration.setCreatedBy(this.getString(row[16]));
                receiptConfigurationList.add(receiptConfiguration);
            }
        }
        return receiptConfigurationList;
    }

   public List<Merchant> getMerchantDetailsBySite(Long siteId) {
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_MERCHANT_DETAILS")
                            .setParameter("siteId", siteId);
        List<Object> resultSet = sqlQuery.list();
        List<Merchant> merchantList = new LinkedList<Merchant>();
        if (resultSet.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultSet.listIterator();
            while(resultListIterator.hasNext()) {
                Object[] row = (Object[]) resultListIterator.next();
                Merchant merchant = new Merchant();
                Site site = new Site();
                merchant.setId(this.getLongFromInteger(row[0]));
                site.setId(this.getLongFromInteger(row[1]));
                merchant.setSite(site);
                merchant.setCreatedDate(this.getDate(row[2]));
                merchant.setModifiedDate(this.getDate(row[3]));
                merchant.setModifiedBy(this.getString(row[4]));
                merchant.setMicroPaymentAccount(this.getBoolean(row[5]));
                merchant.setCreatedBy(this.getString(row[6]));
                merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[7]));
                merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[8]));
                merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[9]));
                merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[10]));
                merchantList.add(merchant);
            }
        }
        return merchantList;
    }

    @Cacheable("getTerm")
    public Term getTerm(String siteName) {
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_AUTH_TERMS")
                                 .setParameter("siteName", siteName);
        List<Object> resultSet = sqlQuery.list();
        Term term = new Term();
        if (resultSet.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultSet.listIterator();
            while(resultListIterator.hasNext()) {
                Object[] row = (Object[]) resultListIterator.next();
                Site site = new Site();
                TermType termType = new TermType();
                term.setId(this.getLongFromInteger(row[0]));
                term.setDescription(this.getString(row[1]));
                term.setCreatedDate(this.getDate(row[2]));
                term.setModifiedDate(this.getDate(row[3]));
                term.setModifiedBy(this.getString(row[4]));
                site.setId(this.getLongFromInteger(row[5]));
                term.setSite(site);
                term.setDefault(this.getBoolean(row[6]));
                termType.setId(this.getLongFromShort(row[7]));
                term.setTermType(termType);
                term.setCreatedBy(this.getString(row[8]));
            }
        }
        return term;
    }

    public boolean doUserAccountsExistForUser(String userName) {
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_USER_ACCOUNTS_FOR_A_USER")
                                 .setParameter("userName", userName);
        List<Object> resultSet = sqlQuery.list();
        if (resultSet.size() > 0) {
            return true;
        }
        return false;
    }

    public void deleteCreditCard(String userName) {
        Session session = currentSession();
        session.getNamedQuery("DELETE_CREDIT_CARD_OF_A_USER")
                .setParameter("userName", userName)
                .executeUpdate();
    }

    public List<UserCountDTO> getUserCountsForAllSite() {
        Session session = currentSession();
        String query = "GET_USER_COUNTS_FOR_ALL_SITE";
        Query  sqlQuery = session.getNamedQuery(query).setResultTransformer(Transformers.aliasToBean(UserCountDTO.class));
        List<UserCountDTO> userCounts = sqlQuery.list();
        return userCounts;
    }

    public UserCountDTO getUserCountForSite(Long siteId) {
        Session session = currentSession();
        String query = "GET_USERCOUNTS_FOR_SITE";
        List<UserCountDTO> userCounts = session.getNamedQuery(query).setParameter("siteId", siteId)
			 				    .setResultTransformer(Transformers.aliasToBean(UserCountDTO.class)).list();
		if (!userCounts.isEmpty()) {
			return userCounts.get(0);
		}
        return null;
    }

    public List<UserCountDTO> getUserCountsBySubForASite(Long siteId) {
        Session session = currentSession();
        String query = "GET_USERCOUNTS_BY_SUB_FOR_A_SITE";
        Query sqlQuery = session.getNamedQuery(query).setParameter("siteId", siteId)
			 				  .setResultTransformer(Transformers.aliasToBean(UserCountDTO.class));
        List<UserCountDTO> userCounts = sqlQuery.list();
        return userCounts;
    }

    public List<UserCountDTO> getUserDistributionBySub(Long siteId, Long accessId) {
        Session session = currentSession();
        String query = "GET_USERDIST_BY_SUBSCRIPTION";
        Query sqlQuery = session.getNamedQuery(query)
        						.setParameter("siteId", siteId)
        						.setParameter("accessId", accessId)
			 				    .setResultTransformer(Transformers.aliasToBean(UserCountDTO.class));
        List<UserCountDTO> userCounts = sqlQuery.list();
        if (userCounts.isEmpty()) {
        	return null;
        }
        return userCounts;
    }

    public PageRecordsDTO  lookupOTCTx(String txRefNumber, String cardName, String cardNumber, String transactionStartDate,
    							String transactionEndDate, String siteName,
    							String productName, String productType, String invoiceId,
    							int startFrom, int numberOfRecords) {

    	// Find out the count
        Session session = currentSession();
        PageRecordsDTO pageRecords = new PageRecordsDTO();
        Query countQuery  = session.getNamedQuery("GET_OTC_TX_DETAILS_COUNT")
                .setParameter("txRefNumber", StringUtils.isBlank(txRefNumber) ? "%%" : txRefNumber.trim())
                .setParameter("accountName", StringUtils.isBlank(cardName) ? "%%" : "%" + cardName.trim() + "%")
                .setParameter("accountNumber", StringUtils.isBlank(cardNumber) ? "%%" : cardNumber.trim())
                .setParameter("transactionStartDate", transactionStartDate)
                .setParameter("transactionEndDate", transactionEndDate)
                .setParameter("siteName", StringUtils.isBlank(siteName) ? "%%" : siteName)
		        .setParameter("productName", StringUtils.isBlank(productName) ? "%%" : "%" + productName.trim() + "%")
		        .setParameter("productType", StringUtils.isBlank(productType) ? "%%" : "%" + productType.trim() + "%")
		        .setParameter("invoiceId", StringUtils.isBlank(invoiceId) ? "%%" : invoiceId.trim());
        List<Object> countResultList = countQuery.list();
		int count = 0;
		if(countResultList.size() > 0) {
			ListIterator<Object> resultListIterator = (ListIterator<Object>) countResultList.listIterator();
			if(resultListIterator.hasNext()) {
				count = (Integer) resultListIterator.next();
			}
		}
        pageRecords.setRecordCount(count);
        List<Tx> transactionList = new ArrayList<Tx>();
        pageRecords.setRecords(transactionList);
        // If count is greater than zero then fetch the actual records.
        if(count > 0){
	        Query sqlQuery  = session.getNamedQuery("GET_OTC_TX_DETAILS")
                             .setParameter("txRefNumber", StringUtils.isBlank(txRefNumber) ? "%%" : txRefNumber.trim())
                             .setParameter("accountName", StringUtils.isBlank(cardName) ? "%%" : "%" + cardName.trim() + "%")
                             .setParameter("accountNumber", StringUtils.isBlank(cardNumber) ? "%%" : cardNumber.trim())
                             .setParameter("transactionStartDate", transactionStartDate)
                             .setParameter("transactionEndDate", transactionEndDate)
                             .setParameter("siteName", StringUtils.isBlank(siteName) ? "%%" : siteName)
                             .setParameter("productName", StringUtils.isBlank(productName) ? "%%" : "%" + productName.trim() + "%")
                             .setParameter("productType", StringUtils.isBlank(productType) ? "%%" : "%" + productType.trim() + "%")
                             .setParameter("invoiceId", StringUtils.isBlank(invoiceId) ? "%%" : invoiceId.trim())
							 .setParameter("startFrom", startFrom)
                             .setParameter("numberOfRecords", numberOfRecords);
	        List<Object> resultList = sqlQuery.list();
	        if (resultList.size() > 0) {
	        	ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
	            while(resultListIterator.hasNext()) {
	            	Tx transaction = new Tx();
	                Object[] row = (Object[]) resultListIterator.next();
	                transaction.setId(this.getLongFromBigInteger(row[0]));
	                transaction.setTxRefNum(this.getString(row[1]));
	                transaction.setOrigTxRefNum(this.getString(row[2]));
	                transaction.setBaseAmount(this.getDoubleFromBigDecimal(row[3]));
	                transaction.setServiceFee(this.getDoubleFromBigDecimal(row[4]));
	                transaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[5]));
	                transaction.setComments(this.getString(row[6]));
	                transaction.setTransactionType(this.getTransactionType(row[7]));
	                transaction.setCheckNum(this.getString(row[8]));
	                transaction.setTransactionDate(this.getDate(row[9]));
	                transaction.setCardNumber(this.getString(row[10]));
	                transaction.setAccountName(this.getString(row[11]));
	                transaction.setModifiedBy(this.getString(row[12]));
	                transaction.setCreatedDate(this.getDate(row[13]));
	                transaction.setSettlementStatus(this.getSettlementStatusType(row[14]));
	                transaction.setCreatedBy(this.getString(row[15]));
	                transactionList.add(transaction);
	            }
	        }
        }
        pageRecords.setRecords(transactionList);
        return pageRecords;
    }


    public PageRecordsDTO  lookupWebTx(String txRefNumber, String cardName, String cardNumber, String transactionStartDate,
    		String transactionEndDate, String siteName, String productId, String productName, String productType,
			String invoiceId, int startFrom, int numberOfRecords) {
    	// Find out the count
        Session session = currentSession();
        PageRecordsDTO pageRecords = new PageRecordsDTO();
        Query countQuery  = session.getNamedQuery("GET_WEB_TX_DETAILS_COUNT")
                             .setParameter("txRefNumber", StringUtils.isBlank(txRefNumber) ? "%%" : txRefNumber.trim())
                             .setParameter("accountName", StringUtils.isBlank(cardName) ? "%%" : "%" + cardName.trim() + "%")
                             .setParameter("accountNumber", StringUtils.isBlank(cardNumber) ? "%%" : cardNumber.trim())
                             .setParameter("transactionStartDate", transactionStartDate)
                             .setParameter("transactionEndDate", transactionEndDate)
                             .setParameter("siteName", StringUtils.isBlank(siteName) ? "%%" : siteName.trim())
                             .setParameter("productId", StringUtils.isBlank(productId) ? "%%" : productId.trim())
                             .setParameter("productName", StringUtils.isBlank(productName) ? "%%" : "%" + productName.trim() + "%")
                             .setParameter("productType", StringUtils.isBlank(productType) ? "%%" : "%" + productType.trim() + "%")
                             .setParameter("invoiceId", StringUtils.isBlank(invoiceId) ? "%%" : invoiceId.trim());
        List<Object> countResultList = countQuery.list();
		int count = 0;
		if(countResultList.size() > 0) {
			ListIterator<Object> resultListIterator = (ListIterator<Object>) countResultList.listIterator();
			if(resultListIterator.hasNext()) {
				count = (Integer) resultListIterator.next();
			}
		}
        pageRecords.setRecordCount(count);
        List<Tx> transactionList = new ArrayList<Tx>();
        pageRecords.setRecords(transactionList);
        // If count is greater than zero then fetch the actual records.
        if(count > 0){
	        Query sqlQuery  = session.getNamedQuery("GET_WEB_TX_DETAILS")
                    .setParameter("txRefNumber", StringUtils.isBlank(txRefNumber) ? "%%" : txRefNumber.trim())
                    .setParameter("accountName", StringUtils.isBlank(cardName) ? "%%" : "%" + cardName.trim() + "%")
                    .setParameter("accountNumber", StringUtils.isBlank(cardNumber) ? "%%" : cardNumber.trim())
                    .setParameter("transactionStartDate", transactionStartDate)
                    .setParameter("transactionEndDate", transactionEndDate)
                    .setParameter("siteName", StringUtils.isBlank(siteName) ? "%%" : siteName.trim())
                    .setParameter("productId", StringUtils.isBlank(productId) ? "%%" : productId.trim())
                    .setParameter("productName", StringUtils.isBlank(productName) ? "%%" : "%" + productName.trim() + "%")
                    .setParameter("productType", StringUtils.isBlank(productType) ? "%%" : "%" + productType.trim() + "%")
                    .setParameter("invoiceId", StringUtils.isBlank(invoiceId) ? "%%" : invoiceId.trim())
					.setParameter("startFrom", startFrom)
                    .setParameter("numberOfRecords", numberOfRecords);
	        List<Object> resultList = sqlQuery.list();
	        if (resultList.size() > 0) {
	        	ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
	            while(resultListIterator.hasNext()) {
	            	Tx transaction = new Tx();
	                Object[] row = (Object[]) resultListIterator.next();
	                transaction.setId(this.getLongFromBigInteger(row[0]));
	                transaction.setTxRefNum(this.getString(row[1]));
	                transaction.setOrigTxRefNum(this.getString(row[2]));
	                transaction.setBaseAmount(this.getDoubleFromBigDecimal(row[3]));
	                transaction.setServiceFee(this.getDoubleFromBigDecimal(row[4]));
	                transaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[5]));
	                transaction.setComments(this.getString(row[6]));
	                transaction.setTransactionType(this.getTransactionType(row[7]));
	                transaction.setCheckNum(this.getString(row[8]));
	                transaction.setTransactionDate(this.getDate(row[9]));
	                transaction.setCardNumber(this.getString(row[10]));
	                transaction.setAccountName(this.getString(row[11]));
	                transaction.setModifiedBy(this.getString(row[12]));
	                transaction.setCreatedDate(this.getDate(row[13]));
	                transaction.setSettlementStatus(this.getSettlementStatusType(row[14]));
	                transaction.setCreatedBy(this.getString(row[15]));
	                transactionList.add(transaction);
	            }
	        }
        }
        pageRecords.setRecords(transactionList);
        return pageRecords;
    }

    public PageRecordsDTO  lookupRecurringTx(String txRefNumber, String cardName, String cardNumber, String transactionStartDate,
    		String transactionEndDate, String siteName, int startFrom, int numberOfRecords) {
    	// Find out the count
        Session session = currentSession();
        PageRecordsDTO pageRecords = new PageRecordsDTO();
        Query countQuery  = session.getNamedQuery("GET_RECURRING_TX_DETAILS_COUNT")
                     .setParameter("txRefNumber", StringUtils.isBlank(txRefNumber) ? "%%" : txRefNumber.trim())
                     .setParameter("accountName", StringUtils.isBlank(cardName) ? "%%" : "%" + cardName.trim() + "%")
	                 .setParameter("accountNumber", StringUtils.isBlank(cardNumber) ? "%%" : cardNumber.trim())
	                 .setParameter("transactionStartDate", transactionStartDate)
	                 .setParameter("transactionEndDate", transactionEndDate)
	                 .setParameter("siteName", StringUtils.isBlank(siteName) ? "%%" : siteName.trim());
        List<Object> countResultList = countQuery.list();
		int count = 0;
		if(countResultList.size() > 0) {
			ListIterator<Object> resultListIterator = (ListIterator<Object>) countResultList.listIterator();
			if(resultListIterator.hasNext()) {
				count = (Integer) resultListIterator.next();
			}
		}
        pageRecords.setRecordCount(count);
        List<Tx> transactionList = new ArrayList<Tx>();
        pageRecords.setRecords(transactionList);
        // If count is greater than zero then fetch the actual records.
        if(count > 0){
	        Query sqlQuery  = session.getNamedQuery("GET_RECURRING_TX_DETAILS")
                    .setParameter("txRefNumber", StringUtils.isBlank(txRefNumber) ? "%%" : txRefNumber.trim())
                    .setParameter("accountName", StringUtils.isBlank(cardName) ? "%%" : "%" + cardName.trim() + "%")
	                 .setParameter("accountNumber", StringUtils.isBlank(cardNumber) ? "%%" : cardNumber.trim())
	                 .setParameter("transactionStartDate", transactionStartDate)
	                 .setParameter("transactionEndDate", transactionEndDate)
	                 .setParameter("siteName", StringUtils.isBlank(siteName) ? "%%" : siteName.trim())
					 .setParameter("startFrom", startFrom)
                     .setParameter("numberOfRecords", numberOfRecords);
	        List<Object> resultList = sqlQuery.list();
	        if (resultList.size() > 0) {
	        	ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
	            while(resultListIterator.hasNext()) {
	            	Tx transaction = new Tx();
	                Object[] row = (Object[]) resultListIterator.next();
	                transaction.setId(this.getLongFromBigInteger(row[0]));
	                transaction.setTxRefNum(this.getString(row[1]));
	                transaction.setOrigTxRefNum(this.getString(row[2]));
	                transaction.setBaseAmount(this.getDoubleFromBigDecimal(row[3]));
	                transaction.setServiceFee(this.getDoubleFromBigDecimal(row[4]));
	                transaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[5]));
	                transaction.setComments(this.getString(row[6]));
	                transaction.setTransactionType(this.getTransactionType(row[7]));
	                transaction.setCheckNum(this.getString(row[8]));
	                transaction.setTransactionDate(this.getDate(row[9]));
	                transaction.setCardNumber(this.getString(row[10]));
	                transaction.setAccountName(this.getString(row[11]));
	                transaction.setModifiedBy(this.getString(row[12]));
	                transaction.setCreatedDate(this.getDate(row[13]));
	                transaction.setSettlementStatus(this.getSettlementStatusType(row[14]));
	                transaction.setCreatedBy(this.getString(row[15]));
	                transactionList.add(transaction);
	            }
	        }
        }
        pageRecords.setRecords(transactionList);
        return pageRecords;
    }

    public PageRecordsDTO  lookupPayAsUGoTx(String txRefNumber, String cardName, String cardNumber, String transactionStartDate,
    		String transactionEndDate, String siteName, String productId, String productName, String productType,
			int startFrom, int numberOfRecords) {
    	// Find out the count
        Session session = currentSession();
        PageRecordsDTO pageRecords = new PageRecordsDTO();
        Query countQuery  = session.getNamedQuery("GET_PAYASUGO_TX_DETAILS_COUNT")
                             .setParameter("txRefNumber", StringUtils.isBlank(txRefNumber) ? "%%" : txRefNumber.trim())
                             .setParameter("accountName", StringUtils.isBlank(cardName) ? "%%" : "%" + cardName.trim() + "%")
                             .setParameter("accountNumber", StringUtils.isBlank(cardNumber) ? "%%" : cardNumber.trim())
                             .setParameter("transactionStartDate", transactionStartDate)
                             .setParameter("transactionEndDate", transactionEndDate)
                             .setParameter("siteName", StringUtils.isBlank(siteName) ? "%%" : "%" + siteName.trim() + "%")
                             .setParameter("productId", StringUtils.isBlank(productId) ? "%%" : "%" + productId.trim() + "%")
                             .setParameter("productName", StringUtils.isBlank(productName) ? "%%" : "%" + productName.trim() + "%")
                             .setParameter("productType", StringUtils.isBlank(productType) ? "%%" : "%" + productType.trim() + "%");
        List<Object> countResultList = countQuery.list();
		int count = 0;
		if(countResultList.size() > 0) {
			ListIterator<Object> resultListIterator = (ListIterator<Object>) countResultList.listIterator();
			if(resultListIterator.hasNext()) {
				count = (Integer) resultListIterator.next();
			}
		}
        pageRecords.setRecordCount(count);
        List<Tx> transactionList = new ArrayList<Tx>();
        pageRecords.setRecords(transactionList);
        // If count is greater than zero then fetch the actual records.
        if(count > 0){
	        Query sqlQuery  = session.getNamedQuery("GET_PAYASUGO_TX_DETAILS")
                    .setParameter("txRefNumber", StringUtils.isBlank(txRefNumber) ? "%%" : txRefNumber.trim())
                    .setParameter("accountName", StringUtils.isBlank(cardName) ? "%%" : "%" + cardName.trim() + "%")
                    .setParameter("accountNumber", StringUtils.isBlank(cardNumber) ? "%%" : cardNumber.trim())
                    .setParameter("transactionStartDate", transactionStartDate)
                    .setParameter("transactionEndDate", transactionEndDate)
                    .setParameter("siteName", StringUtils.isBlank(siteName) ? "%%" : "%" + siteName.trim() + "%")
                    .setParameter("productId", StringUtils.isBlank(productId) ? "%%" : "%" + productId.trim() + "%")
                    .setParameter("productName", StringUtils.isBlank(productName) ? "%%" : "%" + productName.trim() + "%")
                    .setParameter("productType", StringUtils.isBlank(productType) ? "%%" : "%" + productType.trim() + "%")
					 .setParameter("startFrom", startFrom)
	                 .setParameter("numberOfRecords", numberOfRecords);
	        List<Object> resultList = sqlQuery.list();
	        if (resultList.size() > 0) {
	        	ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
	            while(resultListIterator.hasNext()) {
	            	Tx transaction = new Tx();
	                Object[] row = (Object[]) resultListIterator.next();
	                transaction.setId(this.getLongFromBigInteger(row[0]));
	                transaction.setTxRefNum(this.getString(row[1]));
	                transaction.setOrigTxRefNum(this.getString(row[2]));
	                transaction.setBaseAmount(this.getDoubleFromBigDecimal(row[3]));
	                transaction.setServiceFee(this.getDoubleFromBigDecimal(row[4]));
	                transaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[5]));
	                transaction.setComments(this.getString(row[6]));
	                transaction.setTransactionType(this.getTransactionType(row[7]));
	                transaction.setCheckNum(this.getString(row[8]));
	                transaction.setTransactionDate(this.getDate(row[9]));
	                transaction.setCardNumber(this.getString(row[10]));
	                transaction.setAccountName(this.getString(row[11]));
	                transaction.setModifiedBy(this.getString(row[12]));
	                transaction.setCreatedDate(this.getDate(row[13]));
	                transaction.setSettlementStatus(this.getSettlementStatusType(row[14]));
	                transaction.setCreatedBy(this.getString(row[15]));
	                transactionList.add(transaction);
	            }
	        }
        }
        pageRecords.setRecords(transactionList);
        return pageRecords;
    }

    public Site getSiteAdminDetails(Long siteId) {
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_SITE_ADMIN_DETAILS")
                            .setParameter("siteId", siteId);
        List<Object> resultList = sqlQuery.list();
        Site site = null;
        Long accessId = null;
        Long nonRecurringFeeId = null;
        List<Access> accessList = new LinkedList<Access>();
        List<NonRecurringFee> nonRecurringFeeList = new LinkedList<NonRecurringFee>();
        if (resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            Map<Long, Site> uniqueSites =  new HashMap<Long, Site>();
            Map<Long, Access> uniqueAccesses =  new HashMap<Long, Access>();
            Map<Long, Merchant> uniqueMerchants =  new HashMap<Long, Merchant>();
            Map<Long, NonRecurringFee> uniqueNonRecurringFees =  new HashMap<Long, NonRecurringFee>();
            Access access = null;
            while(resultListIterator.hasNext()) {
                Object[] row = (Object[]) resultListIterator.next();
                siteId = this.getLongFromInteger(row[0]);
                if(uniqueSites.get(siteId) == null) {
                    site =  new Site();
                    site.setAccess(accessList);
                    site.setId(siteId);
                    site.setName(this.getString(row[1]));
                    site.setDescription(this.getString(row[2]));
                    site.setCounty(this.getString(row[3]));
                    site.setState(this.getString(row[4]));
                    site.setTimeZone(this.getString(row[5]));
                    site.setActive(this.getBoolean(row[6]));
                    site.setAutoActivate(this.getBoolean(row[7]));
                    site.setNameOnCheck(this.getString(row[60]));
                    site.setCheckHoldingPeriod(this.getLongFromInteger(row[61]));
                    site.setFreeSite(this.getBoolean(row[88]));
                    site.setLocationEnabled(this.getBoolean(row[89]));
                    BankDetails bankDetails = new BankDetails();
                    bankDetails.setFromFirstName(this.getString(row[34]));
                    bankDetails.setFromLastName(this.getString(row[35]));
                    bankDetails.setFromMiddleIntial(this.getString(row[36]));
                    bankDetails.setFromAddressLine1(this.getString(row[37]));
                    bankDetails.setFromAddressLine2(this.getString(row[38]));
                    bankDetails.setFromCity(this.getString(row[39]));
                    bankDetails.setFromState(this.getString(row[40]));
                    bankDetails.setFromZipcode(this.getString(row[41]));
                    bankDetails.setFromPhoneNumber(this.getString(row[42]));
                    bankDetails.setBankName(this.getString(row[43]));
                    bankDetails.setBankCode(this.getString(row[44]));
                    bankDetails.setRoutingNumber(this.getString(row[45]));
                    bankDetails.setAccountNumber(this.getString(row[46]));
                    bankDetails.setLastIssuedCheckNumber(this.getLongFromInteger(row[47]));
                    bankDetails.setBankAddressLine1(this.getString(row[48]));
                    bankDetails.setBankAddressLine2(this.getString(row[49]));
                    bankDetails.setBankCity(this.getString(row[50]));
                    bankDetails.setBankState(this.getString(row[51]));
                    bankDetails.setBankZipcode(this.getString(row[52]));
                    bankDetails.setCreatedDate(this.getDate(row[53]));
                    bankDetails.setModifiedDate(this.getDate(row[54]));
                    bankDetails.setModifiedBy(this.getString(row[55]));
                    bankDetails.setActive(this.getBoolean(row[56]));
                    bankDetails.setCreatedBy(this.getString(row[57]));
                    bankDetails.setStartCheckNumber(this.getLongFromInteger(row[58]));
                    bankDetails.setEndCheckNumber(this.getLongFromInteger(row[59]));
                    CreditUsageFee creditUsageFee =  new CreditUsageFee();
                    creditUsageFee.setFlatFee(this.getDoubleFromBigDecimal(row[15]));
                    creditUsageFee.setFlatFeeCutOff(this.getDoubleFromBigDecimal(row[16]));
                    creditUsageFee.setActive(this.getBoolean(row[17]));
                    creditUsageFee.setPercenteFee(this.getDoubleFromBigDecimal(row[18]));
                    creditUsageFee.setDowngradeFee(this.getDoubleFromBigDecimal(row[19]));
                    creditUsageFee.setAdditionalFee(this.getDoubleFromBigDecimal(row[32]));
                    creditUsageFee.setMicroTxFeeCutOff(this.getDoubleFromBigDecimal(row[70]));
                    site.setCardUsageFee(creditUsageFee);
                    site.setBankDetails(bankDetails);
                    if (this.getString(row[64]) != null) {
                        BankDetails custBankDetails = new BankDetails();
                        custBankDetails.setCustAccountName(this.getString(row[62]));
                        custBankDetails.setRoutingNumber(this.getString(row[63]));
                        custBankDetails.setAccountNumber(this.getString(row[64]));
                        custBankDetails.setFromAddressLine1(this.getString(row[65]));
                        custBankDetails.setFromAddressLine2(this.getString(row[66]));
                        custBankDetails.setFromCity(this.getString(row[67]));
                        custBankDetails.setFromState(this.getString(row[68]));
                        custBankDetails.setFromZipcode(this.getString(row[69]));
                        site.setCustomerBankDetails(custBankDetails);
                    }
                    site.setEnableMicroTxWeb(this.getBoolean(row[71]));
                    site.setEnableMicroTxOTC(this.getBoolean(row[72]));
                    site.setAchHoldingPeriod(this.getLongFromInteger(row[73]));
                    uniqueSites.put(siteId, site);
                } else {
                    site = uniqueSites.get(siteId);
                    accessList = site.getAccess();
                }
                accessId = this.getLongFromInteger(row[8]);
                if(uniqueAccesses.get(accessId) == null) {
                    access = new Access();
                    nonRecurringFeeList = new LinkedList<NonRecurringFee>();
                    access.setNonReccurringFeeList(nonRecurringFeeList);
                    access.setId(accessId);
                    access.setCode(this.getString(row[9]));
                    access.setDescription(this.getString(row[10]));
                    access.setActive(this.getBoolean(row[11]));
                    access.setAccessType(this.getAccessType(row[12]));
                    access.setAuthorizationRequired(this.getBoolean(row[84]));
                    SubscriptionFee subscriptionFee = new SubscriptionFee();
                    subscriptionFee.setFee(this.getDoubleFromBigDecimal(row[13]));
                    subscriptionFee.setTerm(this.getLongFromInteger(row[14]));
                    subscriptionFee.setDescription(this.getString(row[31]));
                    access.setSubscriptionFee(subscriptionFee);
                    access.setClientShare(this.getDoubleFromBigDecimal(row[33]));
                    access.setFirmLevelAccess(this.getBoolean(row[85]));
                    access.setMaxUsersAllowed(this.getInteger(row[86]));
                    access.setMaxDocumentsAllowed(this.getInteger(row[87]));
                    uniqueAccesses.put(accessId, access);
                    accessList.add(access);
                } else {
                    access = uniqueAccesses.get(accessId);
                    nonRecurringFeeList = access.getNonReccurringFeeList();
                }

                /**Populating Non Recurring Fee Object **/
                nonRecurringFeeId = this.getLongFromInteger(row[20]);
                if (nonRecurringFeeId != null && uniqueNonRecurringFees.get(nonRecurringFeeId) == null) {
                    NonRecurringFee nonRecurringFee = new NonRecurringFee();
                    nonRecurringFee.setId(this.getLongFromInteger(row[20]));
                    Code code = new Code();
                    code.setId(this.getLongFromInteger(row[28]));
                    code.setCode(this.getString(row[29]));
                    code.setDescription(this.getString(row[30]));
                    nonRecurringFee.setCode(code);
                    nonRecurringFee.setFeeUnderPageThreshold(this.getDoubleFromBigDecimal(row[22]));
                    nonRecurringFee.setPageThreshold(this.getLongFromInteger(row[23]));
                    nonRecurringFee.setFeeOverPageThreshold(this.getDoubleFromBigDecimal(row[24]));
                    nonRecurringFee.setServiceFee(this.getBoolean(row[25]));
                    nonRecurringFee.setCurrency(this.getString(row[26]));
                    nonRecurringFee.setActive(this.getBoolean(row[27]));
                    nonRecurringFeeList.add(nonRecurringFee);
                    uniqueNonRecurringFees.put(nonRecurringFeeId, nonRecurringFee);
                }

                /** Merchant Information **/
                Long merchantId = this.getLongFromInteger(row[74]);
                if(uniqueMerchants.get(merchantId) == null){
	                Merchant merchant = new Merchant();
	                merchant.setId(merchantId);
	                merchant.setCreatedDate(this.getDate(row[75]));
	                merchant.setModifiedDate(this.getDate(row[76]));
	                merchant.setModifiedBy(this.getString(row[77]));
	                merchant.setMicroPaymentAccount(this.getBoolean(row[78]));
	                merchant.setCreatedBy(this.getString(row[79]));
	                merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[80]));
	                merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[81]));
	                merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[82]));
	                merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[83]));
	               	site.addMerchant(merchant);
	               	uniqueMerchants.put(merchantId,  merchant);
                }
            }
        }
        return site;
    }

    public List<CheckHistory> getCheckHistories(Long siteId, String fromDate, String toDate, String checkNum, Double checkAmt) {

        List<CheckHistory> checkHistoryList = new LinkedList<CheckHistory>();
        CheckHistory checkHistory = null;
        Session session = currentSession();
        Query query = session.getNamedQuery("GET_CHECKHISTORIES")
                .setParameter("siteId", siteId)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate)
                .setParameter("checkNum", checkNum)
                .setParameter("checkAmt", checkAmt);
        List<Object> resultList = query.list();
        if(resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                Object[] row = (Object[]) resultListIterator.next();
                checkHistory =  new CheckHistory();
                checkHistory.setSiteName(this.getString(row[0]));
                checkHistory.setId(this.getLongFromInteger(row[1]));
                checkHistory.setSiteId(siteId);
                checkHistory.setCheckNum(this.getLongFromInteger(row[2]));
                checkHistory.setPaymentType(this.getPaymentType(row[3]));
                checkHistory.setTotalTransactions(this.getLongFromInteger(row[4]));
                checkHistory.setAmount(this.getDoubleFromBigDecimal(row[5]));
                checkHistory.setBankName(this.getString(row[6]));
                checkHistory.setVoided(this.getBoolean(row[7]));
                checkHistory.setCreatedDate(this.getDate(row[8]));
                checkHistory.setModifiedDate(this.getDate(row[9]));
                checkHistory.setModifiedBy(this.getString(row[10]));
                checkHistory.setMachineName(this.getString(row[11]));
                checkHistory.setActive(this.getBoolean(row[12]));
                checkHistory.setComments(this.getString(row[13]));
                checkHistory.setCreatedBy(this.getString(row[14]));
                checkHistory.setEcheck(this.getBoolean(row[15]));
                checkHistoryList.add(checkHistory);
            }
       }
       return checkHistoryList;
    }

    public boolean doVoidCheck(Long checkNumber, String comments) {
        String yesNo = null;
        Session session = currentSession();
        Query query = session.getNamedQuery("DO_VOIDCHECK");
        query.setParameter("checkNumber", checkNumber);
        query.setParameter("comments", comments);
        yesNo = query.list().get(0).toString();
        if(yesNo != null && yesNo.equalsIgnoreCase("Y")) {
            return true;
        }
        return false;
    }

    public CheckHistory getCheckHistory(Long checkNumber) {
        CheckHistory checkDetails = null;
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_CHECK_DETAILS")
                .setParameter("checkNumber", checkNumber);
        List<Object> resultSet = sqlQuery.list();
        if (resultSet.size() > 0) {
            checkDetails = new CheckHistory();
            Object[] row = (Object[]) resultSet.get(0);
            checkDetails.setSiteName(this.getString(row[0]));
            checkDetails.setId(this.getLongFromInteger(row[1]));
            checkDetails.setCheckNum(this.getLongFromInteger(row[2]));
            checkDetails.setPaymentType(this.getPaymentType(row[3]));
            checkDetails.setTotalTransactions(this.getLongFromInteger(row[4]));
            checkDetails.setAmount(this.getDoubleFromBigDecimal(row[5]));
            checkDetails.setBankName(this.getString(row[6]));
            checkDetails.setVoided(this.getBoolean(row[7]));
            checkDetails.setCreatedDate(this.getDate(row[8]));
            checkDetails.setModifiedDate(this.getDate(row[9]));
            checkDetails.setModifiedBy(this.getString(row[10]));
            checkDetails.setMachineName(this.getString(row[11]));
            checkDetails.setActive(this.getBoolean(row[12]));
            checkDetails.setComments(this.getString(row[13]));
            checkDetails.setCreatedBy(this.getString(row[14]));
        }
        return checkDetails;
    }

    public void saveReceiptConfiguration(ReceiptConfiguration receiptConfiguration) {
        Session session = currentSession();
        session.saveOrUpdate(receiptConfiguration);
        session.flush();
    }

    public List<ReceiptConfiguration> getReceiptConfigurationsForSite(Long siteId) {
        List<ReceiptConfiguration> receiptConfigurationList = new LinkedList<ReceiptConfiguration>();
        Session session = currentSession();
        Query sqlQuery  = session.getNamedQuery("GET_RECEIPT_CONFIGURATION_FOR_SITE")
                                    .setParameter("siteId", siteId);
        List<Object> resultSet = sqlQuery.list();
        if (resultSet.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultSet.listIterator();
            while(resultListIterator.hasNext()) {
                ReceiptConfiguration receiptConfiguration = new ReceiptConfiguration();
                Object[] row = (Object[]) resultListIterator.next();
                receiptConfiguration.setId(this.getLongFromInteger(row[0]));
                receiptConfiguration.setBusinessName(this.getString(row[1]));
                receiptConfiguration.setAddressLine1(this.getString(row[2]));
                receiptConfiguration.setAddressLine2(this.getString(row[3]));
                receiptConfiguration.setCity(this.getString(row[4]));
                receiptConfiguration.setState(this.getString(row[5]));
                receiptConfiguration.setZip(this.getString(row[6]));
                receiptConfiguration.setPhone(this.getString(row[7]));
                receiptConfiguration.setComments1(this.getString(row[8]));
                receiptConfiguration.setComments2(this.getString(row[9]));
                receiptConfiguration.setType(this.getPaymentType(this.getString(row[10])));
                receiptConfiguration.setActive(this.getBoolean(row[11]));
                receiptConfiguration.setCreatedDate(this.getDate(row[12]));
                receiptConfiguration.setModifiedDate(this.getDate(row[13]));
                receiptConfiguration.setModifiedBy(this.getString(row[14]));
                receiptConfiguration.setSiteId(this.getLongFromInteger(row[15]));
                receiptConfiguration.setCreatedBy(this.getString(row[16]));
                receiptConfigurationList.add(receiptConfiguration);
            }
        }
        return receiptConfigurationList;
    }

    public ReceiptConfiguration getReceiptConfigurationDetail(Long receiptConfigurationId) {
        Session session = currentSession();
        Query sqlQuery  = session.getNamedQuery("GET_RECEIPT_CONFIGURATION_DETAIL")
                                    .setParameter("receiptConfId", receiptConfigurationId);
        List<Object> resultSet = sqlQuery.list();
        ReceiptConfiguration receiptConfiguration = null;
        if (resultSet.size() > 0) {
            receiptConfiguration = new ReceiptConfiguration();
            Object[] row = (Object[]) resultSet.get(0);
            receiptConfiguration.setId(this.getLongFromInteger(row[0]));
            receiptConfiguration.setBusinessName(this.getString(row[1]));
            receiptConfiguration.setAddressLine1(this.getString(row[2]));
            receiptConfiguration.setAddressLine2(this.getString(row[3]));
            receiptConfiguration.setCity(this.getString(row[4]));
            receiptConfiguration.setState(this.getString(row[5]));
            receiptConfiguration.setZip(this.getString(row[6]));
            receiptConfiguration.setPhone(this.getString(row[7]));
            receiptConfiguration.setComments1(this.getString(row[8]));
            receiptConfiguration.setComments2(this.getString(row[9]));
            receiptConfiguration.setType(this.getPaymentType(this.getString(row[10])));
            receiptConfiguration.setActive(this.getBoolean(row[11]));
            receiptConfiguration.setCreatedDate(this.getDate(row[12]));
            receiptConfiguration.setModifiedDate(this.getDate(row[13]));
            receiptConfiguration.setModifiedBy(this.getString(row[14]));
            receiptConfiguration.setSiteId(this.getLongFromInteger(row[15]));
            receiptConfiguration.setCreatedBy(this.getString(row[16]));
        }
        return receiptConfiguration;
    }

    public PageRecordsDTO getErrorLog(String fromDate, String toDate, String userName, int startFromRecord, int numberOfRecords) {
    	if(StringUtils.isBlank(fromDate)){
    		fromDate = "01/01/1970";
    	}
    	if(StringUtils.isBlank(toDate)){
    		toDate = "01/01/9999";
    	}
    	PageRecordsDTO pageRecords = new PageRecordsDTO();
    	if(StringUtils.isBlank(userName)){
    		userName = "%%";
    	} else {
    		userName = "%" + userName + "%";
    	}
        // First get the count for page records
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_ERROR_LOG_BY_DATE_COUNT")
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate)
                .setParameter("userName", userName);
		List<Object> resultSet = sqlQuery.list();
        int count = 0;
		if(resultSet.size() > 0) {
			ListIterator<Object> resultListIterator = (ListIterator<Object>) resultSet.listIterator();
			if(resultListIterator.hasNext()) {
				count = (Integer) resultListIterator.next();
			}
		}
		pageRecords.setRecordCount(count);
		//if count is zero then no need to query to get actual records.
		if(count == 0){
			return pageRecords;
		}

        sqlQuery =  session.getNamedQuery("GET_ERROR_LOG_BY_DATE")
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate)
                .setParameter("userName", userName)
        		.setParameter("offsetRows", startFromRecord)
        		.setParameter("numberOfRows", numberOfRecords);

        List<ErrorCode> errorList = new LinkedList<ErrorCode>();
        resultSet = sqlQuery.list();
		if (resultSet.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultSet.listIterator();
            while(resultListIterator.hasNext()) {
                ErrorCode error = new ErrorCode();
                Object[] row = (Object[]) resultListIterator.next();
                error.setId(this.getLongFromInteger(row[0]));
                error.setCode(this.getString(row[1]));
                error.setDescription(this.getString(row[2]));
                error.setModuleName(this.getString(row[3]));
                error.setFunctionName(this.getString(row[4]));
                error.setCreatedDate(this.getDate(row[5]));
                error.setModifiedDate(this.getDate(row[6]));
                error.setModifiedBy(this.getString(row[7]));
                error.setActive(this.getBoolean(row[8]));
                error.setUserName(this.getString(row[9]));
                error.setCreatedBy(this.getString(row[10]));
                errorList.add(error);
            }
        }
		pageRecords.setRecords(errorList);
        return pageRecords;
    }

   public BankDetails getBankDetailsBySite(Long siteId) {
        BankDetails bankDetails = null;
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_BANK_DETAILS").setParameter("siteId", siteId);
        List<Object> resultList = sqlQuery.list();
        if(!resultList.isEmpty()){
            Object[] row = (Object[]) resultList.get(0);
            bankDetails = new BankDetails();
            bankDetails.setId(this.getLongFromInteger(row[0]));
            bankDetails.setSiteId(this.getLongFromInteger(row[1]));
            bankDetails.setFromFirstName(this.getString(row[2]));
            bankDetails.setFromLastName(this.getString(row[3]));
            bankDetails.setFromMiddleIntial(this.getString(row[4]));
            bankDetails.setFromAddressLine1(this.getString(row[5]));
            bankDetails.setFromAddressLine2(this.getString(row[6]));
            bankDetails.setFromCity(this.getString(row[7]));
            bankDetails.setFromState(this.getString(row[8]));
            bankDetails.setFromZipcode(this.getString(row[9]));
            bankDetails.setFromPhoneNumber(this.getString(row[10]));
            bankDetails.setBankName(this.getString(row[11]));
            bankDetails.setBankCode(this.getString(row[12]));
            bankDetails.setRoutingNumber(this.getString(row[13]));
            bankDetails.setAccountNumber(this.getString(row[14]));
            bankDetails.setLastIssuedCheckNumber(this.getLongFromInteger(row[15]));
            bankDetails.setBankAddressLine1(this.getString(row[16]));
            bankDetails.setBankAddressLine2(this.getString(row[17]));
            bankDetails.setBankCity(this.getString(row[18]));
            bankDetails.setBankState(this.getString(row[19]));
            bankDetails.setBankZipcode(this.getString(row[20]));
            bankDetails.setCreatedDate(this.getDate(row[21]));
            bankDetails.setModifiedDate(this.getDate(row[22]));
            bankDetails.setModifiedBy(this.getString(row[23]));
            bankDetails.setActive(this.getBoolean(row[24]));
            bankDetails.setCreatedBy(this.getString(row[25]));
            bankDetails.setStartCheckNumber(this.getLongFromInteger(row[26]));
            bankDetails.setEndCheckNumber(this.getLongFromInteger(row[27]));

        }
        return bankDetails;
    }

    public void deleteErrorLogContents(Long errorLogId) {
        Session session = currentSession();
        if (errorLogId != null && errorLogId == -1) {
            session.getNamedQuery("DELETE_ALL_FROM_ERROR_LOG").executeUpdate();
        } else if (errorLogId != null) {
            session.getNamedQuery("DELETE_FROM_ERROR_LOG_BY_ID")
                    .setParameter("errorLogId", errorLogId).executeUpdate();
        }
    }

    public PaymentType getPaymentTypeForTransaction(String txRefNumber) {
		Session session = currentSession();
		PaymentType paymentType = null;
        Query sqlQuery =  session.getNamedQuery("GET_PAYMENT_TYPE")
                                 .setParameter("txRefNumber", txRefNumber);
        List<Object> resultList = sqlQuery.list();
        if (resultList.size() > 0) {
        	 Object row = (Object) resultList.get(0);
            	 if(row != null) {
            		 String paymentTypeString = this.getString(row);
            		 if(paymentTypeString.equalsIgnoreCase("WEB")) {
            			 paymentType = PaymentType.WEB;
            		 } else if(paymentTypeString.equalsIgnoreCase("OTC")) {
            			 paymentType = PaymentType.OTC;
            		 }
            	 }
   		 }
        return paymentType;
	}


}