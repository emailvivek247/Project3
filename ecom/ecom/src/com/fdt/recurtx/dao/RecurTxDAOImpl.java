package com.fdt.recurtx.dao;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.fdt.common.dao.AbstractBaseDAOImpl;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.Merchant;
import com.fdt.ecom.entity.Site;
import com.fdt.recurtx.dto.RecurTxSchedulerDTO;
import com.fdt.recurtx.entity.RecurTx;
import com.fdt.security.entity.Access;

@Repository
public class RecurTxDAOImpl extends AbstractBaseDAOImpl implements RecurTxDAO {

    private static final Logger logger = LoggerFactory.getLogger(RecurTxDAOImpl.class);

    public List<RecurTxSchedulerDTO> getRecurringProfilesForVerification() {
        List<RecurTxSchedulerDTO> paypalVerificationDTOs = new LinkedList<RecurTxSchedulerDTO>();
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_RECUR_PROFILES_FOR_VERIFICATION");
        List<Object> resultSet = sqlQuery.list();
        if(resultSet.size() > 0){
            ListIterator<Object> resultSetIterator = (ListIterator<Object>) resultSet.listIterator();
            while(resultSetIterator.hasNext()) {
                RecurTxSchedulerDTO paypalVerificationDTO = new RecurTxSchedulerDTO();
                Object[] row = (Object[]) resultSetIterator.next();
                Long userId = this.getLongFromBigInteger(row[0]);
                Long accessId = this.getLongFromInteger(row[1]);
                String accessDescription = this.getString(row[10]);
                Long userAccessId = this.getLongFromInteger(row[2]);
                Long userAccountId = this.getLongFromBigInteger(row[3]);
                String userFirstName = this.getString(row[12]);
                String userLastName = this.getString(row[13]);
                paypalVerificationDTO.setUserId(userId);
                paypalVerificationDTO.setAccessId(accessId);
                paypalVerificationDTO.setAccessDescription(accessDescription);
                paypalVerificationDTO.setUserAccessId(userAccessId);
                paypalVerificationDTO.setUserAccountId(userAccountId);
                paypalVerificationDTO.setUserFirstName(userFirstName);
                paypalVerificationDTO.setUserLastName(userLastName);
                paypalVerificationDTO.setClientShare(this.getDoubleFromBigDecimal(row[19]));
                paypalVerificationDTO.setAccountNumber(row[20] == null ? null :
                	this.getPbeStringEncryptor().decrypt(row[20].toString()));
                Merchant merchant = new Merchant();
                merchant.setUserName(this.getString(row[4]));
                merchant.setPassword(row[5] == null ? null : this.getPbeStringEncryptor().decrypt(row[5].toString()));
                merchant.setPartner(this.getString(row[6]));
                merchant.setVendorName(this.getString(row[7]));
                merchant.setId(this.getLongFromInteger(row[16]));
                merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[17]));
                merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[18]));
                merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[21]));
                merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[22]));
                Site site = new Site();
                site.setId(this.getLongFromInteger(row[8]));
                site.setName(this.getString(row[11]));
                site.setTimeZone(this.getString(row[36]));
                String userName = this.getString(row[9]);
                site.addMerchant(merchant);
                CreditCard creditCard = new CreditCard();
                creditCard.setId(this.getLongFromInteger(row[23]));
                creditCard.setName(this.getString(row[24]));
                creditCard.setNumber(row[25] == null ? null : this.getPbeStringEncryptor().decrypt(row[25].toString()));
                creditCard.setExpiryMonth(this.getInteger(row[26]));
                creditCard.setExpiryYear(this.getInteger(row[27]));
                creditCard.setAddressLine1(this.getString(row[28]));
                creditCard.setAddressLine2(this.getString(row[29]));
                creditCard.setCity(this.getString(row[30]));
                creditCard.setState(this.getString(row[31]));
                creditCard.setZip(this.getString(row[32]));
                creditCard.setPhone(this.getLongFromBigInteger(row[33]));
                paypalVerificationDTO.setAmtToCarge(this.getDoubleFromBigDecimal(row[34]));
                paypalVerificationDTO.setPaymentPeriod(this.getString(row[35]));
                paypalVerificationDTO.setCreditCard(creditCard);
                paypalVerificationDTO.setUserName(userName);
                paypalVerificationDTO.setSite(site);
                paypalVerificationDTO.setLastBillingDate(this.getDate(row[14]));
                paypalVerificationDTO.setNextBillingDate(this.getDate(row[15]));
                paypalVerificationDTO.setFirmLevelAccess(this.getBoolean(row[37]));
                paypalVerificationDTO.setFirmAccessAdmin(this.getBoolean(row[38]));
                paypalVerificationDTO.setFirmAdminUserAccessId(this.getLongFromInteger(row[39]));
                paypalVerificationDTOs.add(paypalVerificationDTO);
           }
        }
        return paypalVerificationDTOs;
    }

   public List<RecurTx> getRecurTransactionsByNode(String userName, String nodeName) {
        List<RecurTx> recurTxHistoryInfoList = new LinkedList<RecurTx>();
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_RECURRING_TRANSACTIONS_BY_NODE")
                                    .setParameter("userName", userName)
                                    .setParameter("nodeName", nodeName);
        List<Object> resultList = sqlQuery.list();
        if (resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                RecurTx recurTxHistInfo = new RecurTx();
                Object[] row = (Object[]) resultListIterator.next();
                recurTxHistInfo.setId(this.getLongFromBigInteger(row[0]));
                recurTxHistInfo.setTxRefNum(this.getString(row[1]));
                recurTxHistInfo.setTotalTxAmount(this.getDoubleFromBigDecimal(row[2]));
                recurTxHistInfo.setTransactionDate(this.getDate(row[3]));
                recurTxHistInfo.setTransactionType(this.getTransactionType(row[4]));
                recurTxHistInfo.setSettlementStatus(this.getSettlementStatusType(row[5]));
                recurTxHistInfo.setUserId(this.getLongFromBigInteger(row[6]));
                recurTxHistInfo.setAccessId(this.getLongFromInteger(row[7]));
                recurTxHistInfo.setCardNumber(this.getString(row[8]));
                recurTxHistInfo.setAccountName(this.getString(row[9]));
                recurTxHistInfo.setCreatedDate(this.getDate(row[10]));
                recurTxHistInfo.setModifiedDate(this.getDate(row[11]));
                recurTxHistInfo.setModifiedBy(this.getString(row[12]));
                recurTxHistInfo.setActive(this.getBoolean(row[13]));
                recurTxHistInfo.setCreatedBy(this.getString(row[16]));
                Access access = new Access();
                Site site = new Site();
                site.setName(this.getString(row[14]));
                site.setDescription(this.getString(row[18]));
                site.setTimeZone(this.getString(row[19]));
                access.setDescription(this.getString(row[15]));
                recurTxHistInfo.setSite(site);
                recurTxHistInfo.setAccess(access);
                recurTxHistoryInfoList.add(recurTxHistInfo);
                recurTxHistInfo.setBaseAmount(this.getDoubleFromBigDecimal(row[17]));
            }
        }
        return recurTxHistoryInfoList;
    }

    public List<RecurTx> getRecuringTxDetail(String userName, String recurTxRefNum) {
        RecurTx recurTxHistInfo = null;
        List<RecurTx> recurTxHistoryInfoList = new LinkedList<RecurTx>();
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_RECURRING_TX_HISTORY")
                                    .setParameter("recurTxRefNum", recurTxRefNum)
                                    .setParameter("userName", userName);
        List<Object> resultList = sqlQuery.list();
        if (resultList.size() > 0) {
             ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
             while(resultListIterator.hasNext()) {
                 recurTxHistInfo = new RecurTx();
                Object[] row = (Object[]) resultListIterator.next();
                recurTxHistInfo.setId(this.getLongFromBigInteger(row[0]));
                recurTxHistInfo.setTxRefNum(this.getString(row[1]));
                recurTxHistInfo.setBaseAmount(this.getDoubleFromBigDecimal(row[2]));
                recurTxHistInfo.setTransactionDate(this.getDate(row[3]));
                recurTxHistInfo.setTransactionType(this.getTransactionType(row[4]));
                recurTxHistInfo.setSettlementStatus(this.getSettlementStatusType(row[5]));
                recurTxHistInfo.setUserId(this.getLongFromBigInteger(row[6]));
                recurTxHistInfo.setAccessId(this.getLongFromInteger(row[7]));
                recurTxHistInfo.setCardNumber(this.getString(row[8]));
                recurTxHistInfo.setAccountName(this.getString(row[9]));
                recurTxHistInfo.setCreatedDate(this.getDate(row[10]));
                recurTxHistInfo.setModifiedDate(this.getDate(row[11]));
                recurTxHistInfo.setModifiedBy(this.getString(row[12]));
                recurTxHistInfo.setActive(this.getBoolean(row[13]));
                recurTxHistInfo.setCreatedBy(this.getString(row[16]));
                Access access = new Access();
                Site site = new Site();
                site.setName(this.getString(row[14]));
                site.setTimeZone(this.getString(row[17]));
                access.setDescription(this.getString(row[15]));
                recurTxHistInfo.setSite(site);
                recurTxHistInfo.setAccess(access);
                recurTxHistoryInfoList.add(recurTxHistInfo);
             }
        }
        return recurTxHistoryInfoList;
    }

    public List<RecurTx> getRecurringTransactionByTxRefNum(String txRefNumber, String siteName) {
        List<RecurTx> recurTxHistoryInfoList = new LinkedList<RecurTx>();
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_SP_RECURRING_TRANSACTION_LOOKUP")
                                    .setParameter("txRefNumber", txRefNumber)
                                    .setParameter("siteName", siteName);
        List<Object> resultList = sqlQuery.list();
        if (resultList.size() > 0) {
             ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
             while(resultListIterator.hasNext()) {
                 RecurTx recurTransaction = new RecurTx();
                 Site site = new Site();
                 Merchant merchant = new Merchant();
                 Object[] row = (Object[]) resultListIterator.next();
                 recurTransaction.setId(this.getLongFromBigInteger(row[0]));
                 recurTransaction.setTxRefNum(this.getString(row[1]));
                 recurTransaction.setOrigTxRefNum(this.getString(row[2]));
                 recurTransaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[3]));
                 recurTransaction.setComments(this.getString(row[4]));
                 recurTransaction.setTransactionType(this.getTransactionType(row[5]));
                 recurTransaction.setCheckNum(this.getString(row[6]));
                 recurTransaction.setTransactionDate(this.getDate(row[7]));
                 recurTransaction.setCardNumber(this.getString(row[8]));
                 recurTransaction.setAccountName(this.getString(row[9]));
                 recurTransaction.setMachineName(this.getString(row[10]));
                 recurTransaction.setModifiedBy(this.getString(row[11]));
                 recurTransaction.setCreatedDate(this.getDate(row[12]));
                 recurTransaction.setSettlementStatus(this.getSettlementStatusType(row[13]));
                 site.setId(this.getLongFromInteger(row[14]));
                 site.setName(this.getString(row[15]));
                 site.setDescription(this.getString(row[31]));
                 site.setTimeZone(this.getString(row[32]));
                 merchant.setId(this.getLongFromInteger(row[16]));
                 merchant.setPartner(this.getString(row[17]));
                 merchant.setVendorName(this.getString(row[18]));
                 merchant.setUserName(this.getString(row[19]));
                 merchant.setPassword(row[20] == null ? null : this.getPbeStringEncryptor().decrypt(row[20].toString()));
                 recurTransaction.setUserId(this.getLongFromBigInteger(row[21]));
                 recurTransaction.setAccessId(this.getLongFromInteger(row[22]));
                 recurTransaction.setCardType(this.getCardType(row[23]));
                 recurTransaction.setAuthCode(this.getString(row[24]));
                 recurTransaction.setCreatedBy(this.getString(row[25]));
                 recurTransaction.setBaseAmount(this.getDoubleFromBigDecimal(row[26]));
                 recurTransaction.setTxFeePercent(this.getDoubleFromBigDecimal(row[27]));
                 recurTransaction.setTxFeeFlat(this.getDoubleFromBigDecimal(row[28]));
                 recurTransaction.setClientShare(this.getDoubleFromBigDecimal(row[29]));
                 recurTransaction.setPreviousAccess(this.getBoolean(row[30]));
                 site.addMerchant(merchant);
                 recurTransaction.setSite(site);
                 recurTransaction.setMerchantId(merchant.getId());
                 recurTxHistoryInfoList.add(recurTransaction);
             }
        }
        return recurTxHistoryInfoList;
    }

    public RecurTx getReferencedRecurringTransactionByTxRefNum(String originaltxRefNumber, String siteName) {
        Session session = currentSession();
        Query sqlQuery  = session.getNamedQuery("GET_SP_REFERENCED_RECURRING_TRANSACTION_LOOKUP")
                                    .setParameter("originaltxRefNumber", originaltxRefNumber)
                                    .setParameter("siteName", siteName);
        List<Object> resultSet = sqlQuery.list();
        RecurTx recurTransaction = null;
        if (resultSet.size() > 0) {
            recurTransaction = new RecurTx();
            Site site = new Site();
            Merchant merchant = new Merchant();
            Object[] row = (Object[]) resultSet.get(0);
            recurTransaction.setId(this.getLongFromBigInteger(row[0]));
            recurTransaction.setTxRefNum(this.getString(row[1]));
            recurTransaction.setOrigTxRefNum(this.getString(row[2]));
            recurTransaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[3]));
            recurTransaction.setComments(this.getString(row[4]));
            recurTransaction.setTransactionType(this.getTransactionType(row[5]));
            recurTransaction.setCheckNum(this.getString(row[6]));
            recurTransaction.setTransactionDate(this.getDate(row[7]));
            recurTransaction.setCardNumber(this.getString(row[8]));
            recurTransaction.setAccountName(this.getString(row[9]));
            recurTransaction.setMachineName(this.getString(row[10]));
            recurTransaction.setModifiedBy(this.getString(row[11]));
            recurTransaction.setCreatedDate(this.getDate(row[12]));
            recurTransaction.setSettlementStatus(this.getSettlementStatusType(row[13]));
            site.setId(this.getLongFromInteger(row[14]));
            site.setName(this.getString(row[15]));
            merchant.setId(this.getLongFromInteger(row[16]));
            merchant.setPartner(this.getString(row[17]));
            merchant.setVendorName(this.getString(row[18]));
            merchant.setUserName(this.getString(row[19]));
            merchant.setPassword(row[20] == null ? null : this.getPbeStringEncryptor().decrypt(row[20].toString()));
            merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[27]));
            merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[28]));
            merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[32]));
            merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[33]));
            recurTransaction.setUserId(this.getLongFromBigInteger(row[21]));
            recurTransaction.setAccessId(this.getLongFromInteger(row[22]));
            recurTransaction.setCardType(this.getCardType(row[23]));
            recurTransaction.setAuthCode(this.getString(row[24]));
            recurTransaction.setCreatedBy(this.getString(row[25]));
            recurTransaction.setBaseAmount(this.getDoubleFromBigDecimal(row[26]));
            site.addMerchant(merchant);
            recurTransaction.setSite(site);
            recurTransaction.setMerchantId(merchant.getId());
        }
        return recurTransaction;
    }



    public void saveRecurTransaction(RecurTx recurTxHistInfo) {
        Session session = currentSession();
        session.saveOrUpdate(recurTxHistInfo);
        session.flush();
    }

    public List<RecurTx> getRecurTransactions(String userName) {
        List<RecurTx> recurTxHistoryInfoList = new LinkedList<RecurTx>();
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_RECURRING_TRANSACTIONS")
        					.setParameter("userName", userName);
        List<Object> resultList = sqlQuery.list();
        if (resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                RecurTx recurTxHistInfo = new RecurTx();
                Object[] row = (Object[]) resultListIterator.next();
                recurTxHistInfo.setId(this.getLongFromBigInteger(row[0]));
                recurTxHistInfo.setTxRefNum(this.getString(row[1]));
                recurTxHistInfo.setBaseAmount(this.getDoubleFromBigDecimal(row[2]));
                recurTxHistInfo.setTransactionDate(this.getDate(row[3]));
                recurTxHistInfo.setTransactionType(this.getTransactionType(row[4]));
                recurTxHistInfo.setSettlementStatus(this.getSettlementStatusType(row[5]));
                recurTxHistInfo.setUserId(this.getLongFromBigInteger(row[6]));
                recurTxHistInfo.setAccessId(this.getLongFromInteger(row[7]));
                recurTxHistInfo.setCardNumber(this.getString(row[8]));
                recurTxHistInfo.setAccountName(this.getString(row[9]));
                recurTxHistInfo.setCreatedDate(this.getDate(row[10]));
                recurTxHistInfo.setModifiedDate(this.getDate(row[11]));
                recurTxHistInfo.setModifiedBy(this.getString(row[12]));
                recurTxHistInfo.setActive(this.getBoolean(row[13]));
                Access access = new Access();
                Site site = new Site();
                site.setName(this.getString(row[14]));
                site.setDescription(this.getString(row[18]));
                site.setTimeZone(this.getString(row[17]));
                access.setDescription(this.getString(row[15]));
                recurTxHistInfo.setCreatedBy(this.getString(row[16]));
                recurTxHistInfo.setSite(site);
                recurTxHistInfo.setAccess(access);
                recurTxHistoryInfoList.add(recurTxHistInfo);
            }
        }
        return recurTxHistoryInfoList;
    }

    public List<RecurTx> getRecurTransactionsBySite(String userName, Long siteId) {
        List<RecurTx> recurTxHistoryInfoList = new LinkedList<RecurTx>();
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_RECURRING_TRANSACTIONS_BY_SITE")
                    .setParameter("userName", userName)
                    .setParameter("siteId", siteId);
        List<Object> resultList = sqlQuery.list();
        if (resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                RecurTx recurTxHistInfo = new RecurTx();
                Object[] row = (Object[]) resultListIterator.next();
                recurTxHistInfo.setId(this.getLongFromBigInteger(row[0]));
                recurTxHistInfo.setTxRefNum(this.getString(row[1]));
                recurTxHistInfo.setBaseAmount(this.getDoubleFromBigDecimal(row[2]));
                recurTxHistInfo.setTransactionDate(this.getDate(row[3]));
                recurTxHistInfo.setTransactionType(this.getTransactionType(row[4]));
                recurTxHistInfo.setSettlementStatus(this.getSettlementStatusType(row[5]));
                recurTxHistInfo.setUserId(this.getLongFromBigInteger(row[6]));
                recurTxHistInfo.setAccessId(this.getLongFromInteger(row[7]));
                recurTxHistInfo.setCardNumber(this.getString(row[8]));
                recurTxHistInfo.setAccountName(this.getString(row[9]));
                recurTxHistInfo.setCreatedDate(this.getDate(row[10]));
                recurTxHistInfo.setModifiedDate(this.getDate(row[11]));
                recurTxHistInfo.setModifiedBy(this.getString(row[12]));
                recurTxHistInfo.setActive(this.getBoolean(row[13]));
                Access access = new Access();
                Site site = new Site();
                site.setName(this.getString(row[14]));
                site.setDescription(this.getString(row[18]));
                site.setTimeZone(this.getString(row[17]));
                access.setDescription(this.getString(row[15]));
                recurTxHistInfo.setCreatedBy(this.getString(row[16]));
                recurTxHistInfo.setSite(site);
                recurTxHistInfo.setAccess(access);
                recurTxHistoryInfoList.add(recurTxHistInfo);
            }
        }
        return recurTxHistoryInfoList;
    }

    public List<RecurTx> getRecurTxByUser(String userName) {
        List<RecurTx> recurTxHistoryInfoList = new LinkedList<RecurTx>();
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_RECUR_TX_BY_USER").setParameter("userName", userName);
        List<Object> resultList = sqlQuery.list();
        if (resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                RecurTx recurTxHistInfo = new RecurTx();
                Object[] row = (Object[]) resultListIterator.next();
                recurTxHistInfo.setId(this.getLongFromBigInteger(row[0]));
                recurTxHistInfo.setTxRefNum(this.getString(row[1]));
                recurTxHistInfo.setBaseAmount(this.getDoubleFromBigDecimal(row[2]));
                recurTxHistInfo.setTransactionDate(this.getDate(row[3]));
                recurTxHistInfo.setTransactionType(this.getTransactionType(row[4]));
                recurTxHistInfo.setSettlementStatus(this.getSettlementStatusType(row[5]));
                recurTxHistInfo.setUserId(this.getLongFromBigInteger(row[6]));
                recurTxHistInfo.setAccessId(this.getLongFromInteger(row[7]));
                recurTxHistInfo.setCardNumber(this.getString(row[8]));
                recurTxHistInfo.setAccountName(this.getString(row[9]));
                recurTxHistInfo.setCreatedDate(this.getDate(row[10]));
                recurTxHistInfo.setModifiedDate(this.getDate(row[11]));
                recurTxHistInfo.setModifiedBy(this.getString(row[12]));
                recurTxHistInfo.setActive(this.getBoolean(row[13]));
                Access access = new Access();
                Site site = new Site();
                site.setName(this.getString(row[14]));
                site.setTimeZone(this.getString(row[17]));
                access.setDescription(this.getString(row[15]));
                recurTxHistInfo.setCreatedBy(this.getString(row[16]));
                recurTxHistInfo.setSite(site);
                recurTxHistInfo.setAccess(access);
                recurTxHistoryInfoList.add(recurTxHistInfo);
            }
        }
        return recurTxHistoryInfoList;
    }

    public List<RecurTx> getRecurTxBySite(String siteName) {
        List<RecurTx> recurTxHistoryInfoList = new LinkedList<RecurTx>();
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_RECUR_TX_BY_SITE").setParameter("siteName", siteName);
        List<Object> resultList = sqlQuery.list();
        if (resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                RecurTx recurTxHistInfo = new RecurTx();
                Object[] row = (Object[]) resultListIterator.next();
                recurTxHistInfo.setId(this.getLongFromBigInteger(row[0]));
                recurTxHistInfo.setTxRefNum(this.getString(row[1]));
                recurTxHistInfo.setBaseAmount(this.getDoubleFromBigDecimal(row[2]));
                recurTxHistInfo.setTransactionDate(this.getDate(row[3]));
                recurTxHistInfo.setTransactionType(this.getTransactionType(row[4]));
                recurTxHistInfo.setSettlementStatus(this.getSettlementStatusType(row[5]));
                recurTxHistInfo.setUserId(this.getLongFromBigInteger(row[6]));
                recurTxHistInfo.setAccessId(this.getLongFromInteger(row[7]));
                recurTxHistInfo.setCardNumber(this.getString(row[8]));
                recurTxHistInfo.setAccountName(this.getString(row[9]));
                recurTxHistInfo.setCreatedDate(this.getDate(row[10]));
                recurTxHistInfo.setModifiedDate(this.getDate(row[11]));
                recurTxHistInfo.setModifiedBy(this.getString(row[12]));
                recurTxHistInfo.setActive(this.getBoolean(row[13]));
                Access access = new Access();
                Site site = new Site();
                site.setName(this.getString(row[14]));
                site.setTimeZone(this.getString(row[17]));
                access.setDescription(this.getString(row[15]));
                recurTxHistInfo.setCreatedBy(this.getString(row[16]));
                recurTxHistInfo.setSite(site);
                recurTxHistInfo.setAccess(access);
                recurTxHistoryInfoList.add(recurTxHistInfo);
            }
        }
        return recurTxHistoryInfoList;
    }
}
