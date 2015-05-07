package com.fdt.payasugotx.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StaleStateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.fdt.common.dao.AbstractBaseDAOImpl;
import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.util.SystemUtil;
import com.fdt.ecom.entity.Location;
import com.fdt.ecom.entity.Merchant;
import com.fdt.ecom.entity.ShoppingCartItem;
import com.fdt.ecom.entity.Site;
import com.fdt.payasugotx.entity.PayAsUGoTx;
import com.fdt.payasugotx.entity.PayAsUGoTxItem;
import com.fdt.payasugotx.entity.PayAsUGoTxView;
import com.fdt.security.entity.Access;
import com.fdt.security.entity.User;

@Repository
public class PayAsUGoTxDAOImpl extends AbstractBaseDAOImpl implements PayAsUGoTxDAO {

    private static final Logger logger = LoggerFactory.getLogger(PayAsUGoTxDAOImpl.class);

    public void savePayAsUGoTransactionItem(List<PayAsUGoTxItem> payAsUGoTransactionItems) {
        Session session = currentSession();
        for(PayAsUGoTxItem payAsUGoTransactionItem: payAsUGoTransactionItems){
            session.saveOrUpdate(payAsUGoTransactionItem);
        }
        session.flush();
    }

    public List<PayAsUGoTxView> getPayAsUGoTransactionsByNode(String userName, String nodeName, String comments,
    		Date fromDate, Date toDate) {
        Session session = currentSession();
        // Now get the actual page records
        Criteria criteria = session.createCriteria(PayAsUGoTxView.class)
        		.add(Restrictions.eq("firmUserName", userName))
        		.add(Restrictions.eq("nodeName", nodeName));
        if(!StringUtils.isBlank(comments)) {
			Criterion one = Restrictions.like("itemComments", "%".concat(comments).concat("%"));
			Criterion two = Restrictions.like("transactionComments", "%".concat(comments).concat("%"));
			criteria.add(Restrictions.or(one, two));
		}
		if(fromDate != null) {
			criteria.add(Restrictions.ge("transactionDate", fromDate));
		}
		if(toDate != null){
			criteria.add(Restrictions.le("transactionDate", toDate));
		}
		criteria.addOrder( Property.forName("subscription").asc() )
        		.addOrder( Property.forName("userName").asc() )
        		.addOrder( Property.forName("transactionDate").desc())
        		.setReadOnly(true);

        List<PayAsUGoTxView> payAsUGoTransactions = criteria.list();
        List<PayAsUGoTxView> filteredpayAsUGoTransactions = criteria.list();
		if(payAsUGoTransactions!=null && payAsUGoTransactions.size() > 0){
			filteredpayAsUGoTransactions = new LinkedList<PayAsUGoTxView>();
			for(PayAsUGoTxView payAsUGoTxView : payAsUGoTransactions) {
				if(payAsUGoTxView.getTxRefNum()!= null & !isExists(payAsUGoTxView.getTxRefNum(), filteredpayAsUGoTransactions)){
					filteredpayAsUGoTransactions.add(payAsUGoTxView);
				}
			}
		}
        return filteredpayAsUGoTransactions;

    }


    public PageRecordsDTO getPayAsUGoTransactionsByNodePerPage(String firmUserName, String nodeName, String comments,
    		Date fromDate, Date toDate, int startingFrom, int numberOfRecords) {
        Session session = currentSession();

        // Let's get the count first
        Criteria countCriteria = session.createCriteria(PayAsUGoTxView.class)
        		.add(Restrictions.eq("firmUserName", firmUserName))
        		.add(Restrictions.eq("nodeName", nodeName))
        		.setReadOnly(true);

        if(!StringUtils.isBlank(comments)) {
			Criterion one = Restrictions.like("itemComments", "%".concat(comments).concat("%"));
			Criterion two = Restrictions.like("transactionComments", "%".concat(comments).concat("%"));
			countCriteria.add(Restrictions.or(one, two));
		}
		if(fromDate != null) {
			countCriteria.add(Restrictions.ge("transactionDate", fromDate));
		}
		if(toDate != null){
			countCriteria.add(Restrictions.le("transactionDate", toDate));
		}

        Integer recordCount = ((Number)countCriteria.setProjection(Projections.rowCount()).uniqueResult()).intValue();

        // Now get the actual page records
        Criteria criteria = session.createCriteria(PayAsUGoTxView.class)
        		.add(Restrictions.eq("firmUserName", firmUserName))
        		.add(Restrictions.eq("nodeName", nodeName))
        		.setFirstResult(startingFrom)
        		.setMaxResults(numberOfRecords);

        if(!StringUtils.isBlank(comments)) {
			Criterion one = Restrictions.like("itemComments", "%".concat(comments).concat("%"));
			Criterion two = Restrictions.like("transactionComments", "%".concat(comments).concat("%"));
			criteria.add(Restrictions.or(one, two));
		}

		if(fromDate != null) {
			criteria.add(Restrictions.ge("transactionDate", fromDate));
		}
		if(toDate != null){
			criteria.add(Restrictions.le("transactionDate", toDate));
		}
		criteria.addOrder( Property.forName("subscription").asc() )
       		.addOrder( Property.forName("userName").asc() )
       		.addOrder( Property.forName("transactionDate").desc())
       		.setReadOnly(true);
		List<PayAsUGoTxView> payAsUGoTransactions = criteria.list();
		List<PayAsUGoTxView> filteredpayAsUGoTransactions = criteria.list();
		if(payAsUGoTransactions!=null && payAsUGoTransactions.size() > 0){
			filteredpayAsUGoTransactions = new LinkedList<PayAsUGoTxView>();
			for(PayAsUGoTxView payAsUGoTxView : payAsUGoTransactions) {
				if(payAsUGoTxView.getTxRefNum()!= null & !isExists(payAsUGoTxView.getTxRefNum(), filteredpayAsUGoTransactions)){
					filteredpayAsUGoTransactions.add(payAsUGoTxView);
				}
			}
		}
        PageRecordsDTO pageRecords = new PageRecordsDTO();
        pageRecords.setRecords(filteredpayAsUGoTransactions);
        pageRecords.setRecordCount(recordCount);
        return pageRecords;
    }

    private boolean isExists(String txRefNum, List<PayAsUGoTxView> filteredpayAsUGoTransactions) {
    	boolean exists = false;
    	for(PayAsUGoTxView payAsUGoTxView : filteredpayAsUGoTransactions) {
    		if(payAsUGoTxView.getTxRefNum().equalsIgnoreCase(txRefNum)){
    			return true;
    		}
    	}
		return exists;
	}

	public PayAsUGoTx getPayAsUGoTransactionDetail(String userName, Long payAsUGoTxId, String isRefund) {
        Session session = currentSession();
        Query sqlQuery  = session.getNamedQuery("GET_PAYASUGO_TX_DETAIL")
                                    .setParameter("userName", userName)
                                    .setParameter("webTxId", payAsUGoTxId)
                                    .setParameter("isRefund", isRefund);
        List<Object> resultSet = sqlQuery.list();
        PayAsUGoTx payAsUGoTransaction = null;
        if (resultSet.size() > 0) {
            payAsUGoTransaction = new PayAsUGoTx();
            Object[] row = (Object[]) resultSet.get(0);
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultSet.listIterator();
            payAsUGoTransaction.setId(this.getLongFromBigInteger(row[0]));
            payAsUGoTransaction.setTxRefNum(this.getString(row[1]));
            payAsUGoTransaction.setBaseAmount(this.getDoubleFromBigDecimal(row[2]));
            payAsUGoTransaction.setServiceFee(this.getDoubleFromBigDecimal(row[3]));
            payAsUGoTransaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[4]));
            payAsUGoTransaction.setTransactionDate(this.getDate(row[5]));
            payAsUGoTransaction.setCardNumber(this.getString(row[6]));
            payAsUGoTransaction.setAccountName(this.getString(row[7]));
            payAsUGoTransaction.setTransactionType(this.getTransactionType(row[8]));
            payAsUGoTransaction.setCreatedDate(this.getDate(row[9]));
            payAsUGoTransaction.setOrigTxRefNum(this.getString(row[23]));
            payAsUGoTransaction.setUserName(userName);
            payAsUGoTransaction.setUserFirstName(this.getString(row[26]));
            payAsUGoTransaction.setUserLastName(this.getString(row[27]));
            payAsUGoTransaction.setCertified(this.getBoolean(row[28]));
            Access access = new Access();
            Site site = new Site();
            site.setName(this.getString(row[10]));
            site.setTimeZone(this.getString(row[24]));
            access.setDescription(this.getString(row[11]));
            List<PayAsUGoTxItem> payAsUGoTxItems =  new LinkedList<PayAsUGoTxItem>();
            while(resultListIterator.hasNext()) {
                row = (Object[]) resultListIterator.next();
                PayAsUGoTxItem payAsUGoTxItem = new PayAsUGoTxItem();
                payAsUGoTxItem.setProductId(this.getString(row[12]));
                payAsUGoTxItem.setProductType(this.getString(row[13]));
                payAsUGoTxItem.setPageCount(this.getIntFromInteger(row[14]));
                payAsUGoTxItem.setBaseAmount(this.getDoubleFromBigDecimal(row[15]));
                payAsUGoTxItem.setServiceFee(this.getDoubleFromBigDecimal(row[16]));
                payAsUGoTxItem.setTotalTxAmount(this.getDoubleFromBigDecimal(row[17]));
                payAsUGoTxItem.setRefunded(this.getBoolean(row[18]));
                payAsUGoTxItem.setModifiedDate(this.getDate(row[19]));
                payAsUGoTxItem.setComments(this.getString(row[20]));
                payAsUGoTxItem.setDownloadURL(this.getString(row[21]));
                payAsUGoTxItem.setDocumentAvailable(this.getBoolean(row[22]));
                payAsUGoTxItem.setCertifiedDocumentNumber(this.getString(row[25]));
                payAsUGoTxItems.add(payAsUGoTxItem);
            }
            payAsUGoTransaction.setPayAsUGoTxItems(payAsUGoTxItems);
            payAsUGoTransaction.setSite(site);
            payAsUGoTransaction.setAccess(access);
        }
        return payAsUGoTransaction;
    }

   public PayAsUGoTx getPayAsUGoTransactionByTxRefNum(String txRefNumber, String siteName) {
        Session session = currentSession();
        Query sqlQuery  = session.getNamedQuery("GET_PAYASUGO_TX_BY_TX_REF_NUM")
                                    .setParameter("txRefNumber", txRefNumber)
                                    .setParameter("siteName", siteName);
        List<Object> resultSet = sqlQuery.list();
        PayAsUGoTx payAsUGoTransaction = null;
        if (resultSet.size() > 0) {
            payAsUGoTransaction = new PayAsUGoTx();
            Access access = new Access();
            Site site = new Site();
            Merchant merchant = new Merchant();
            Object[] row = (Object[]) resultSet.get(0);
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultSet.listIterator();
            payAsUGoTransaction.setId(this.getLongFromBigInteger(row[0]));
            payAsUGoTransaction.setTxRefNum(this.getString(row[1]));
            payAsUGoTransaction.setOrigTxRefNum(this.getString(row[2]));
            payAsUGoTransaction.setBaseAmount(this.getDoubleFromBigDecimal(row[3]));
            payAsUGoTransaction.setServiceFee(this.getDoubleFromBigDecimal(row[4]));
            payAsUGoTransaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[5]));
            payAsUGoTransaction.setComments(this.getString(row[6]));
            payAsUGoTransaction.setTransactionType(this.getTransactionType(row[7]));
            payAsUGoTransaction.setCheckNum(this.getString(row[8]));
            payAsUGoTransaction.setTransactionDate(this.getDate(row[9]));
            payAsUGoTransaction.setCardNumber(this.getString(row[10]));
            payAsUGoTransaction.setAccountName(this.getString(row[11]));
            payAsUGoTransaction.setMachineName(this.getString(row[12]));
            payAsUGoTransaction.setCreatedDate(this.getDate(row[13]));
            payAsUGoTransaction.setSettlementStatus(this.getSettlementStatusType(row[14]));
            payAsUGoTransaction.setModifiedBy(this.getString(row[37]));
            payAsUGoTransaction.setPageCount(this.getIntFromInteger(row[54]));
            payAsUGoTransaction.setItemCount(this.getIntFromInteger(row[55]));
            site.setName(this.getString(row[15]));
            site.setId(this.getLongFromInteger(row[38]));
            site.setDescription(this.getString(row[44]));
            site.setTimeZone(this.getString(row[49]));
            payAsUGoTransaction.setUserName(this.getString(row[16]));
            access.setDescription(this.getString(row[17]));
            Long accessId = this.getLongFromInteger(row[18]);
            access.setId(accessId);
            payAsUGoTransaction.setAccessId(accessId);
            merchant.setId(this.getLongFromInteger(row[35]));
            merchant.setPartner(this.getString(row[19]));
            merchant.setVendorName(this.getString(row[20]));
            merchant.setUserName(this.getString(row[21]));
            merchant.setPassword(row[22] == null ? null : this.getPbeStringEncryptor().decrypt(row[22].toString()));
            merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[39]));
            merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[40]));
            merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[42]));
            merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[43]));
            payAsUGoTransaction.setCardType(this.getCardType(row[41]));
            payAsUGoTransaction.setUserId(this.getLongFromBigInteger(row[23]));
            site.addMerchant(merchant);
            payAsUGoTransaction.setSite(site);
            payAsUGoTransaction.setAccess(access);
            payAsUGoTransaction.setTxFeePercent(this.getDoubleFromBigDecimal(row[45]));
            payAsUGoTransaction.setTxFeeFlat(this.getDoubleFromBigDecimal(row[46]));
            payAsUGoTransaction.setCreatedBy(this.getString(row[47]));
            payAsUGoTransaction.setFirmAdminUserAccessId(this.getLongFromInteger(row[51]));
            payAsUGoTransaction.setCertified(this.getBoolean(row[52]));
            List<PayAsUGoTxItem> payAsUGoTxItems =  new LinkedList<PayAsUGoTxItem>();
            while(resultListIterator.hasNext()) {
                row = (Object[]) resultListIterator.next();
                PayAsUGoTxItem payAsUGoTxItem = new PayAsUGoTxItem();
                payAsUGoTxItem.setId(this.getLongFromBigInteger(row[24]));
                payAsUGoTxItem.setProductId(this.getString(row[25]));
                payAsUGoTxItem.setProductType(this.getString(row[26]));
                payAsUGoTxItem.setItemName(this.getString(row[56]));
                payAsUGoTxItem.setPageCount(this.getIntFromInteger(row[27]));
                payAsUGoTxItem.setBaseAmount(this.getDoubleFromBigDecimal(row[28]));
                payAsUGoTxItem.setServiceFee(this.getDoubleFromBigDecimal(row[29]));
                payAsUGoTxItem.setTotalTxAmount(this.getDoubleFromBigDecimal(row[30]));
                payAsUGoTxItem.setRefunded(this.getBoolean(row[31]));
                payAsUGoTxItem.setModifiedDate(this.getDate(row[32]));
                payAsUGoTxItem.setDownloadURL(this.getString(row[33]));
                payAsUGoTxItem.setComments(this.getString(row[34]));
                payAsUGoTxItem.setDocumentAvailable(this.getBoolean(row[36]));
                payAsUGoTxItem.setLocationName(this.getString(row[50]));
                payAsUGoTxItem.setCertifiedDocumentNumber(this.getString(row[53]));
                payAsUGoTxItems.add(payAsUGoTxItem);
                payAsUGoTxItem.setCreatedDate(this.getDate(row[48]));

            }
            payAsUGoTransaction.setPayAsUGoTxItems(payAsUGoTxItems);
        }
        return payAsUGoTransaction;
    }


   public PayAsUGoTx getPayAsUGoTransactionItemByItemId(Long itemId, String siteName) {
        Session session = currentSession();
        Query sqlQuery  = session.getNamedQuery("GET_PAYASUGO_TX_ITEM_BY_ITEM_ID")
                                    .setParameter("itemId", itemId)
                                    .setParameter("siteName", siteName);
        List<Object> resultSet = sqlQuery.list();
        PayAsUGoTx payAsUGoTransaction = null;
        if (resultSet.size() > 0) {
            payAsUGoTransaction = new PayAsUGoTx();
            Access access = new Access();
            Site site = new Site();
            Merchant merchant = new Merchant();
            Object[] row = (Object[]) resultSet.get(0);
            List<PayAsUGoTxItem>  payAsUGoTxItems =  new LinkedList<PayAsUGoTxItem>();
            PayAsUGoTxItem payAsUGoTxItem = new PayAsUGoTxItem();
            payAsUGoTxItem.setId(this.getLongFromBigInteger(row[0]));
            payAsUGoTxItem.setProductId(this.getString(row[1]));
            payAsUGoTxItem.setProductType(this.getString(row[2]));
            payAsUGoTxItem.setPageCount(this.getIntFromInteger(row[3]));
            payAsUGoTxItem.setItemQuantity(this.getIntFromInteger(row[45]));
            payAsUGoTxItem.setBaseAmount(this.getDoubleFromBigDecimal(row[4]));
            payAsUGoTxItem.setServiceFee(this.getDoubleFromBigDecimal(row[5]));
            payAsUGoTxItem.setTotalTxAmount(this.getDoubleFromBigDecimal(row[6]));
            payAsUGoTxItem.setRefunded(this.getBoolean(row[7]));
            payAsUGoTxItem.setModifiedDate(this.getDate(row[8]));
            payAsUGoTxItem.setModifiedBy(this.getString(row[9]));
            payAsUGoTxItems.add(payAsUGoTxItem);
            payAsUGoTransaction.setPayAsUGoTxItems(payAsUGoTxItems);
            payAsUGoTransaction.setTxRefNum(this.getString(row[10]));
            payAsUGoTransaction.setOrigTxRefNum(this.getString(row[11]));
            payAsUGoTransaction.setBaseAmount(this.getDoubleFromBigDecimal(row[12]));
            payAsUGoTransaction.setServiceFee(this.getDoubleFromBigDecimal(row[12]));
            payAsUGoTransaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[14]));
            payAsUGoTransaction.setComments(this.getString(row[15]));
            payAsUGoTransaction.setTransactionType(this.getTransactionType(row[16]));
            payAsUGoTransaction.setCheckNum(this.getString(row[17]));
            payAsUGoTransaction.setMachineName(this.getString(row[18]));
            payAsUGoTransaction.setTransactionDate(this.getDate(row[19]));
            payAsUGoTransaction.setCardNumber(this.getString(row[20]));
            payAsUGoTransaction.setCardType(this.getCardType(row[37]));
            payAsUGoTransaction.setAccountName(this.getString(row[21]));
            payAsUGoTransaction.setSettlementStatus(this.getSettlementStatusType(row[22]));
            payAsUGoTransaction.setModifiedBy(this.getString(row[23]));
            payAsUGoTransaction.setFirmAdminUserAccessId(this.getLongFromInteger(row[43]));
            payAsUGoTransaction.setCertified(this.getBoolean(row[44]));
            site.setName(this.getString(row[24]));
            site.setId(this.getLongFromInteger(row[34]));
            site.setTimeZone(this.getString(row[42]));
            payAsUGoTransaction.setUserName(this.getString(row[25]));
            access.setDescription(this.getString(row[26]));
            Long accessId = this.getLongFromInteger(row[27]);
            access.setId(accessId);
            payAsUGoTransaction.setAccess(access);
            payAsUGoTransaction.setAccessId(accessId);
            merchant.setPartner(this.getString(row[28]));
            merchant.setVendorName(this.getString(row[29]));
            merchant.setUserName(this.getString(row[30]));
            merchant.setPassword( row[31] == null ? null : this.getPbeStringEncryptor().decrypt(row[31].toString()));
            merchant.setId(this.getLongFromInteger(row[32]));
            merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[35]));
            merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[36]));
            merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[38]));
            merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[39]));
            site.addMerchant(merchant);
            payAsUGoTransaction.setMerchantId(this.getLongFromInteger(row[32]));
            payAsUGoTransaction.setUserId(this.getLongFromBigInteger(row[33]));
            payAsUGoTransaction.setTxFeePercent(this.getDoubleFromBigDecimal(row[40]));
            payAsUGoTransaction.setTxFeeFlat(this.getDoubleFromBigDecimal(row[41]));
            payAsUGoTransaction.setSite(site);
        }
        return payAsUGoTransaction;
    }

    public PayAsUGoTx getReferencedPayAsUGoTransactionItemByItemId(Long itemId, String siteName) {
        Session session = currentSession();
        Query sqlQuery  = session.getNamedQuery("GET_REFERENCED_PAYASUGO_TX_ITEM_BY_ITEM_ID")
                                    .setParameter("itemId", itemId)
                                    .setParameter("siteName", siteName);
        List<Object> resultSet = sqlQuery.list();
        PayAsUGoTx payAsUGoTransaction = null;
        if (resultSet.size() > 0) {
            payAsUGoTransaction = new PayAsUGoTx();
            Access access = new Access();
            Site site = new Site();
            Merchant merchant = new Merchant();
            Object[] row = (Object[]) resultSet.get(0);
            List<PayAsUGoTxItem> payAsUGoTxItems =  new LinkedList<PayAsUGoTxItem>();
            PayAsUGoTxItem payAsUGoTxItem = new PayAsUGoTxItem();
            payAsUGoTxItem.setId(this.getLongFromBigInteger(row[0]));
            payAsUGoTxItem.setProductId(this.getString(row[1]));
            payAsUGoTxItem.setProductType(this.getString(row[2]));
            payAsUGoTxItem.setPageCount(this.getIntFromInteger(row[3]));
            payAsUGoTxItem.setBaseAmount(this.getDoubleFromBigDecimal(row[4]));
            payAsUGoTxItem.setServiceFee(this.getDoubleFromBigDecimal(row[5]));
            payAsUGoTxItem.setTotalTxAmount(this.getDoubleFromBigDecimal(row[6]));
            payAsUGoTxItem.setRefunded(this.getBoolean(row[7]));
            payAsUGoTxItem.setModifiedDate(this.getDate(row[8]));
            payAsUGoTxItem.setModifiedBy(this.getString(row[9]));
            payAsUGoTxItem.setItemName(this.getString(row[38]));
            payAsUGoTxItem.setItemQuantity(this.getIntFromInteger(row[39]));
            payAsUGoTxItem.setTax(this.getDoubleFromBigDecimal(row[40]));
            payAsUGoTxItems.add(payAsUGoTxItem);
            payAsUGoTransaction.setPayAsUGoTxItems(payAsUGoTxItems);
            payAsUGoTransaction.setTxRefNum(this.getString(row[10]));
            payAsUGoTransaction.setOrigTxRefNum(this.getString(row[11]));
            payAsUGoTransaction.setBaseAmount(this.getDoubleFromBigDecimal(row[12]));
            payAsUGoTransaction.setServiceFee(this.getDoubleFromBigDecimal(row[13]));
            payAsUGoTransaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[14]));
            payAsUGoTransaction.setComments(this.getString(row[15]));
            payAsUGoTransaction.setTransactionType(this.getTransactionType(row[16]));
            payAsUGoTransaction.setCheckNum(this.getString(row[17]));
            payAsUGoTransaction.setMachineName(this.getString(row[18]));
            payAsUGoTransaction.setTransactionDate(this.getDate(row[19]));
            payAsUGoTransaction.setCardNumber(this.getString(row[20]));
            payAsUGoTransaction.setAccountName(this.getString(row[21]));
            payAsUGoTransaction.setSettlementStatus(this.getSettlementStatusType(row[22]));
            payAsUGoTransaction.setModifiedBy(this.getString(row[23]));
            site.setName(this.getString(row[24]));
            payAsUGoTransaction.setUserName(this.getString(row[25]));
            access.setDescription(this.getString(row[26]));
            Long accessId = this.getLongFromInteger(row[27]);
            access.setId(accessId);
            payAsUGoTransaction.setAccess(access);
            merchant.setPartner(this.getString(row[28]));
            merchant.setVendorName(this.getString(row[29]));
            merchant.setUserName(this.getString(row[30]));
            merchant.setPassword( row[31] == null ? null : this.getPbeStringEncryptor().decrypt(row[31].toString()));
            merchant.setId(this.getLongFromInteger(row[32]));
            merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[34]));
            merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[35]));
            merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[36]));
            merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[37]));
            site.addMerchant(merchant);
            payAsUGoTransaction.setMerchantId(this.getLongFromInteger(row[32]));
            payAsUGoTransaction.setUserId(this.getLongFromBigInteger(row[33]));
            payAsUGoTransaction.setTax(this.getDoubleFromBigDecimal(row[41]));
            payAsUGoTransaction.setSite(site);
        }
        return payAsUGoTransaction;
    }


   public List<PayAsUGoTx> getReferencedPayAsUGoTransaction(String txRefNumber, String siteName) {
        List<PayAsUGoTx> payAsUGoTransactionList = new LinkedList<PayAsUGoTx>();
        Session session = currentSession();
        Query sqlQuery  = session.getNamedQuery("GET_REFERENCED_PAYASUGO_TX_BY_TX_REF_NUM")
                                    .setParameter("txRefNumber", txRefNumber)
                                    .setParameter("siteName", siteName);
        List<Object> resultSet = sqlQuery.list();
        if (resultSet.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultSet.listIterator();
            while(resultListIterator.hasNext()) {
                PayAsUGoTx payAsUGoTransaction = new PayAsUGoTx();
                payAsUGoTransaction = new PayAsUGoTx();
                Access access = new Access();
                Site site = new Site();
                Merchant merchant = new Merchant();
                Object[] row = (Object[]) resultListIterator.next();
                payAsUGoTransaction.setId(this.getLongFromBigInteger(row[0]));
                payAsUGoTransaction.setTxRefNum(this.getString(row[1]));
                payAsUGoTransaction.setOrigTxRefNum(this.getString(row[2]));
                payAsUGoTransaction.setBaseAmount(this.getDoubleFromBigDecimal(row[3]));
                payAsUGoTransaction.setServiceFee(this.getDoubleFromBigDecimal(row[4]));
                payAsUGoTransaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[5]));
                payAsUGoTransaction.setComments(this.getString(row[6]));
                payAsUGoTransaction.setTransactionType(this.getTransactionType(row[7]));
                payAsUGoTransaction.setCheckNum(this.getString(row[8]));
                payAsUGoTransaction.setTransactionDate(this.getDate(row[9]));
                payAsUGoTransaction.setCardNumber(this.getString(row[10]));
                payAsUGoTransaction.setAccountName(this.getString(row[11]));
                payAsUGoTransaction.setMachineName(this.getString(row[12]));
                payAsUGoTransaction.setCreatedDate(this.getDate(row[13]));
                payAsUGoTransaction.setSettlementStatus(this.getSettlementStatusType(row[14]));
                site.setName(this.getString(row[15]));
                payAsUGoTransaction.setUserName(this.getString(row[16]));
                access.setDescription(this.getString(row[17]));
                merchant.setPartner(this.getString(row[18]));
                merchant.setVendorName(this.getString(row[19]));
                merchant.setUserName(this.getString(row[20]));
                merchant.setPassword(row[21] == null ? null : this.getPbeStringEncryptor().decrypt(row[21].toString()));
                merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[34]));
                merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[35]));
                merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[36]));
                merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[37]));
                site.addMerchant(merchant);
                payAsUGoTransaction.setSite(site);
                payAsUGoTransaction.setAccess(access);
                List<PayAsUGoTxItem> payAsUGoTxItems =  new LinkedList<PayAsUGoTxItem>();
                PayAsUGoTxItem payAsUGoTxItem = new PayAsUGoTxItem();
                payAsUGoTxItem.setProductId(this.getString(row[23]));
                payAsUGoTxItem.setProductType(this.getString(row[24]));
                payAsUGoTxItem.setPageCount(this.getIntFromInteger(row[25]));
                payAsUGoTxItem.setBaseAmount(this.getDoubleFromBigDecimal(row[26]));
                payAsUGoTxItem.setTotalTxAmount(this.getDoubleFromBigDecimal(row[27]));
                payAsUGoTransaction.setModifiedBy(this.getString(row[28]));
                payAsUGoTransaction.setTax(this.getDoubleFromBigDecimal(row[29]));
                payAsUGoTxItem.setItemName(this.getString(row[30]));
                payAsUGoTxItem.setItemQuantity(this.getIntFromInteger(row[31]));
                payAsUGoTxItem.setServiceFee(this.getDoubleFromBigDecimal(row[32]));
                payAsUGoTxItem.setTax(this.getDoubleFromBigDecimal(row[33]));
                payAsUGoTxItems.add(payAsUGoTxItem);

                payAsUGoTransaction.setPayAsUGoTxItems(payAsUGoTxItems);
                payAsUGoTransactionList.add(payAsUGoTransaction);
            }
        }
        return payAsUGoTransactionList;
    }


    public int updateRefundTxForPayAsUGoTxItem(Long payAsUGoTxItemId, Long refundTxId, String modifiedBy) {
        Session session = currentSession();
        int noOfRecordsUpdated = session.createQuery("Update PayAsUGoTxItem webTxItem SET " +
                "webTxItem.refundTxId = :refundTxId, " +
                "webTxItem.refunded = :refunded, " +
                "webTxItem.modifiedDate = :modifiedDate, " +
                "webTxItem.modifiedBy = :modifiedBy, " +
                "webTxItem.documentAvailable = :documentAvailable " +
                "WHERE webTxItem.id = :webTxItemId")
               .setParameter("refundTxId", refundTxId)
               .setParameter("refunded", Boolean.TRUE)
               .setParameter("modifiedDate", new Date())
               .setParameter("modifiedBy", modifiedBy)
               .setParameter("documentAvailable", Boolean.FALSE)
               .setParameter("webTxItemId", payAsUGoTxItemId)
        .executeUpdate();
        return noOfRecordsUpdated;
    }

    public int updateRefundTxForPayAsUGoTxItems(Long originalTxId, Long refundTxId, String modifiedBy) {
        Session session = currentSession();
        int noOfRecordsUpdated = session.createQuery("Update PayAsUGoTxItem webTxItem SET " +
                "webTxItem.refundTxId = :refundTxId, " +
                "webTxItem.refunded = :refunded, " +
                "webTxItem.modifiedDate = :modifiedDate, " +
                "webTxItem.modifiedBy = :modifiedBy, " +
                "webTxItem.documentAvailable = :documentAvailable " +
                "WHERE webTxItem.payAsUGoTxId = :originalTxId")
               .setParameter("refundTxId", refundTxId)
               .setParameter("refunded", Boolean.TRUE)
               .setParameter("modifiedDate", new Date())
               .setParameter("modifiedBy", modifiedBy)
               .setParameter("documentAvailable", Boolean.FALSE)
               .setParameter("originalTxId", originalTxId)
        .executeUpdate();
        return noOfRecordsUpdated;
    }


    public void savePayAsUGoTransaction(PayAsUGoTx payAsUGoTransaction) {
        Session session = currentSession();
        session.saveOrUpdate(payAsUGoTransaction);
        session.flush();
    }

    public PayAsUGoTxItem getPayAsUGoTxIdForPurchasedDoc(String userName, String productId, String uniqueIdentifier) {
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_PAYASUGO_TX_ID_FOR_PURCHASED_DOC")
                                            .setParameter("userName", userName)
                                            .setParameter("productId", productId)
                                            .setParameter("uniqueIdentifier", uniqueIdentifier);
        List<Object> resultList = sqlQuery.list();
        PayAsUGoTxItem item = null;
        if(resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                Object[] row = (Object[]) resultListIterator.next();
                item = new PayAsUGoTxItem();
                item.setPayAsUGoTxId(this.getLongFromBigInteger(row[0]));
                item.setCertifiedDocumentNumber(this.getString(row[1]));
                item.setCertified(this.getBoolean(row[2]));
            }
        }
        return item;
    }


    public void saveShoppingCartItem(ShoppingCartItem userTerm) {
        Session session = currentSession();
        session.saveOrUpdate(userTerm);
        session.flush();
    }

    public List<ShoppingCartItem> getShoppingCart(String userName) {
        List<ShoppingCartItem> shoppingCartItems = new LinkedList<ShoppingCartItem>();
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_SHOPPING_CART")
                                    .setParameter("userName", userName);
        List<Object> resultList = sqlQuery.list();
        if (resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
                Object[] row = (Object[]) resultListIterator.next();
                shoppingCartItem.setId(this.getLongFromBigInteger(row[0]));
                shoppingCartItem.setUserId(this.getLongFromBigInteger(row[1]));
                shoppingCartItem.setNodeName(this.getString(row[2]));
                shoppingCartItem.setProductId(this.getString(row[3]));
                shoppingCartItem.setProductType(this.getString(row[4]));
                shoppingCartItem.setProductName(this.getString(row[26]));
                shoppingCartItem.setPageCount(this.getIntFromInteger(row[5]));
                shoppingCartItem.setDownloadURL(this.getString(row[6]));
                shoppingCartItem.setModifiedBy(this.getString(row[7]));
                shoppingCartItem.setCreatedDate(this.getDate(row[8]));
                shoppingCartItem.setModifiedDate(this.getDate(row[9]));
                shoppingCartItem.setAccessName(this.getString(row[10]));
                shoppingCartItem.setUniqueIdentifier(this.getString(row[11]));
                shoppingCartItem.setApplication(this.getString(row[12]));
                shoppingCartItem.setComments(this.getString(row[13]));
                shoppingCartItem.setBarNumber(this.getString(row[22]));
                shoppingCartItem.setLocationId(this.getLongFromInteger(row[23]));
                shoppingCartItem.setCertified(this.getBoolean(row[25]));
                Access access = new Access();
                access.setId(this.getLongFromInteger(row[14]));
                access.setDescription(this.getString(row[15]));
                access.setFirmLevelAccess(this.getBoolean(row[16]));
                access.setMaxDocumentsAllowed(this.getInteger(row[17]));
                access.setGovernmentAccess(this.getBoolean(row[24]));
                Site site = new Site();
                site.setName(this.getString(row[18]));
                access.setSite(site);
                shoppingCartItem.setAccess(access);
                User user = new User();
                user.setId(shoppingCartItem.getUserId());
                user.setUsername(this.getString(row[19]));
                user.setBarNumber(this.getString(row[20]));
                shoppingCartItem.setUser(user);
                shoppingCartItem.setFirmAccessAdmin(this.getBoolean(row[21]));
                shoppingCartItems.add(shoppingCartItem);
            }
        }
        return shoppingCartItems;
    }

    public List<ShoppingCartItem> getShoppingCart(String userName, String nodeName) {
        List<ShoppingCartItem> shoppingCartItems = new LinkedList<ShoppingCartItem>();
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_SHOPPING_CART_BY_NODE_NAME")
                                    .setParameter("userName", userName)
                                    .setParameter("nodeName", nodeName);
        List<Object> resultList = sqlQuery.list();
        if (resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
            	ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
                Object[] row = (Object[]) resultListIterator.next();
                shoppingCartItem.setId(this.getLongFromBigInteger(row[0]));
                shoppingCartItem.setUserId(this.getLongFromBigInteger(row[1]));
                shoppingCartItem.setNodeName(this.getString(row[2]));
                shoppingCartItem.setProductId(this.getString(row[3]));
                shoppingCartItem.setProductType(this.getString(row[4]));
                shoppingCartItem.setProductName(this.getString(row[27]));
                shoppingCartItem.setPageCount(this.getIntFromInteger(row[5]));
                shoppingCartItem.setDownloadURL(this.getString(row[6]));
                shoppingCartItem.setModifiedBy(this.getString(row[7]));
                shoppingCartItem.setCreatedDate(this.getDate(row[8]));
                shoppingCartItem.setModifiedDate(this.getDate(row[9]));
                shoppingCartItem.setAccessName(this.getString(row[10]));
                shoppingCartItem.setUniqueIdentifier(this.getString(row[11]));
                shoppingCartItem.setApplication(this.getString(row[12]));
                shoppingCartItem.setCreatedBy(this.getString(row[13]));
                shoppingCartItem.setComments(this.getString(row[14]));
                shoppingCartItem.setBarNumber(this.getString(row[23]));
                shoppingCartItem.setLocationId(this.getLongFromInteger(row[24]));
                shoppingCartItem.setCertified(this.getBoolean(row[26]));
                Access access = new Access();
                access.setId(this.getLongFromInteger(row[15]));
                access.setDescription(this.getString(row[16]));
                access.setFirmLevelAccess(this.getBoolean(row[17]));
                access.setMaxDocumentsAllowed(this.getInteger(row[18]));
                access.setGovernmentAccess(this.getBoolean(row[25]));
                Site site = new Site();
                site.setName(this.getString(row[19]));
                access.setSite(site);
                shoppingCartItem.setAccess(access);
                User user = new User();
                user.setId(shoppingCartItem.getUserId());
                user.setUsername(this.getString(row[20]));
                user.setBarNumber(this.getString(row[21]));
                shoppingCartItem.setUser(user);
                shoppingCartItem.setFirmAccessAdmin(this.getBoolean(row[22]));
                shoppingCartItems.add(shoppingCartItem);
            }
        }
        return shoppingCartItems;
    }

    public void deleteShoppingCart(List<ShoppingCartItem> shoppingCartItems) {
    	try {
	        Session session = currentSession();
	        for(ShoppingCartItem shoppingCartItem : shoppingCartItems) {
	            session.delete(shoppingCartItem);
	        }
	        session.flush();
    	} catch (StaleStateException staleState) {
    		/*This is done so that the datbase */
    		logger.error("State State Exception Occurred in deleteShoppingCart()", staleState);
    	}
    }

    public void deleteShoppingCartItem(ShoppingCartItem shoppingCartItem) {
        Session session = currentSession();
        session.delete(shoppingCartItem);
        session.flush();
    }

    public List<PayAsUGoTx> getPayAsUGoTransactions(String userName) {
        List<PayAsUGoTx> payAsUGoTxHistoryList = new LinkedList<PayAsUGoTx>();
        Session session = currentSession();
        Query sqlQuery = session.getNamedQuery("GET_PAYASUGO_TX")
        				.setParameter("userName", userName);
        List<Object> resultList = sqlQuery.list();
        if (resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                PayAsUGoTx payAsUGoTransaction = new PayAsUGoTx();
                Object[] row = (Object[]) resultListIterator.next();
                payAsUGoTransaction.setId(this.getLongFromBigInteger(row[0]));
                payAsUGoTransaction.setTxRefNum(this.getString(row[1]));
                payAsUGoTransaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[2]));
                payAsUGoTransaction.setTransactionDate(this.getDate(row[3]));
                payAsUGoTransaction.setCardNumber(this.getString(row[4]));
                payAsUGoTransaction.setAccountName(this.getString(row[5]));
                payAsUGoTransaction.setTransactionType(this.getTransactionType(row[6]));
                payAsUGoTransaction.setCreatedDate(this.getDate(row[7]));
                payAsUGoTransaction.setItemsPurchased(this.getLongFromInteger(row[9]));
                payAsUGoTransaction.setCreatedBy(this.getString(row[10]));
                payAsUGoTransaction.setModifiedDate(this.getDate(row[12]));
                payAsUGoTransaction.setModifiedBy(this.getString(row[13]));
                Site site = new Site();
                site.setName(this.getString(row[8]));
                site.setDescription(this.getString(row[15]));
                site.setTimeZone(this.getString(row[11]));
                Access access = new Access();
                access.setDescription(this.getString(row[14]));
                payAsUGoTransaction.setAccess(access);
                payAsUGoTransaction.setSite(site);

                payAsUGoTxHistoryList.add(payAsUGoTransaction);
            }
        }
        return payAsUGoTxHistoryList;
    }

    public List<PayAsUGoTx> getPayAsUGoTransactionsBySite(String userName, Long siteId) {
        List<PayAsUGoTx> payAsUGoTxHistoryList = new LinkedList<PayAsUGoTx>();
        Session session = currentSession();
        Query sqlQuery = session.getNamedQuery("GET_PAYASUGO_TX_BY_SITE")
                     .setParameter("userName", userName)
                     .setParameter("siteId", siteId);
        List<Object> resultList = sqlQuery.list();
        if (resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                PayAsUGoTx payAsUGoTransaction = new PayAsUGoTx();
                Object[] row = (Object[]) resultListIterator.next();
                payAsUGoTransaction.setId(this.getLongFromBigInteger(row[0]));
                payAsUGoTransaction.setTxRefNum(this.getString(row[1]));
                payAsUGoTransaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[2]));
                payAsUGoTransaction.setTransactionDate(this.getDate(row[3]));
                payAsUGoTransaction.setCardNumber(this.getString(row[4]));
                payAsUGoTransaction.setAccountName(this.getString(row[5]));
                payAsUGoTransaction.setTransactionType(this.getTransactionType(row[6]));
                payAsUGoTransaction.setCreatedDate(this.getDate(row[7]));
                payAsUGoTransaction.setItemsPurchased(this.getLongFromInteger(row[9]));
                payAsUGoTransaction.setCreatedBy(this.getString(row[10]));
                payAsUGoTransaction.setModifiedDate(this.getDate(row[12]));
                payAsUGoTransaction.setModifiedBy(this.getString(row[13]));
                Site site = new Site();
                site.setName(this.getString(row[8]));
                site.setTimeZone(this.getString(row[11]));
                site.setDescription(this.getString(row[15]));
                payAsUGoTransaction.setSite(site);
                Access access = new Access();
                access.setDescription(this.getString(row[14]));
                payAsUGoTransaction.setAccess(access);
                payAsUGoTxHistoryList.add(payAsUGoTransaction);
            }
        }
        return payAsUGoTxHistoryList;
    }

    /**
     * Get Total number of firm level documents purchased for a given access per cycle for the current month
     *
     */

	public int getDocsPurchasedForCurrentSubCycle(Long userId, Long accessId, List<String> barNumbersOfAllFirmUsers){
		List<PayAsUGoTx> payAsUGoTxHistoryList = new LinkedList<PayAsUGoTx>();
        Session session = currentSession();
        Query sqlQuery = session.getNamedQuery("GET_DOCUMENT_PURCHASED_FOR_FIRM")
                     .setParameter("userId", userId)
                     .setParameter("accessId", accessId);

        int documentCount = 0;
        
        List<Object> resultList = sqlQuery.list();
        if (resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                Object[] row = (Object[]) resultListIterator.next();
                String barNumbersPresentOnDoc = this.getString(row[0]);
                int itemCount = this.getIntFromInteger(row[1]);
                if(StringUtils.isBlank(barNumbersPresentOnDoc)){
                	documentCount = documentCount + itemCount;
                } else {
                		if(!CollectionUtils.isEmpty(barNumbersOfAllFirmUsers)) {//if atleast one firm user has barNumber 
                			String [] barNumbersPresentOnDocArray = SystemUtil.tokenizeToStringArray(barNumbersPresentOnDoc, ",", true, true);
                			boolean isMatchFound = false;
                			for (String barNumber: barNumbersPresentOnDocArray) {
                				if(barNumbersOfAllFirmUsers.contains(barNumber.toUpperCase())) {  
                					isMatchFound = true;
                            		break;
                            	} 
                			}
                			if(!isMatchFound){
                				documentCount = documentCount + itemCount;
                			}
                        }  else {
                        	documentCount = documentCount + itemCount;
                        }
                }
            }
        }
        return documentCount;
	}

	public void updateShoppingCartComments(Long shoppingCartId, String comments) {
	    this.currentSession().getNamedQuery("UPDATE_SHOPPING_CART_COMMENTS")
	        .setParameter("shoppingCartId", shoppingCartId)
	        .setParameter("comments", comments)
	    .executeUpdate();
	}


    @Cacheable("getLocationByNameAndAccessName")
	public Location getLocationByNameAndAccessName(String locationName, String accessName){
        Session session = currentSession();
        Query sqlQuery = session.getNamedQuery("GET_LOCATION_BY_NAME_ACCESS_NAME")
                     .setParameter("locationName", locationName.toUpperCase())
        			.setParameter("accessName", accessName.toUpperCase());
        List<Object> resultList = sqlQuery.list();
        Location location = null;
        if (resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                Object[] row = (Object[]) resultListIterator.next();
                location = new Location();
                location.setId(this.getLongFromInteger(row[0]));
                location.setDescription(this.getString(row[1]));
                Site site = new Site();
                site.setId(this.getLongFromInteger(row[2]));
                location.setSite(site);
                location.setStateDescription(this.getString(row[3]));
                location.setSealOfAuthenticity(row[4] == null ? null : (byte[]) row[4]);
                location.setSignature(row[5] == null ? null : (byte[]) row[5]);
                location.setClerkName(this.getString(row[6]));
                location.setDesignation(this.getString(row[7]));
                location.setNoteOfAuthenticity(this.getString(row[8]));
                location.setLocationCode(this.getString(row[9]));
            }
        }
        return location;
	}

    @Cacheable("getLocationsBySiteId")
	public List<Location> getLocationsBySiteId(Long siteId){
        Session session = currentSession();
        Query sqlQuery = session.getNamedQuery("GET_LOCATION_BY_SITE_ID")
                     .setParameter("siteId", siteId);
        List<Object> resultList = sqlQuery.list();
        List<Location> locations = new ArrayList<Location>();
        if (resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
            	Location location = new Location();
                Object[] row = (Object[]) resultListIterator.next();
                location = new Location();
                location.setId(this.getLongFromInteger(row[0]));
                location.setDescription(this.getString(row[1]));
                Site site = new Site();
                site.setId(this.getLongFromInteger(row[2]));
                location.setSite(site);
                location.setStateDescription(this.getString(row[3]));
                location.setClerkName(this.getString(row[4]));
                location.setDesignation(this.getString(row[5]));
                location.setNoteOfAuthenticity(this.getString(row[6]));
                location.setLocationCode(this.getString(row[7]));
                locations.add(location);
            }
        }
        return locations;
	}

    @Cacheable("getLocationSignatureById")
	public Location getLocationSignatureById(Long locationId){
        Session session = currentSession();
        Query sqlQuery = session.getNamedQuery("GET_LOCATION_SIGNATURE_BY_ID")
                     .setParameter("locationId", locationId);
        List<Object> resultList = sqlQuery.list();
        Location location = null;
        if (resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            if(resultListIterator.hasNext()) {
            	location = new Location();
                Object obj =  resultListIterator.next();
                location = new Location();
                location.setSignature(obj == null ? null : (byte[]) obj);
            }
        }
        return location;
	}

    @Cacheable("getLocationById")
	public Location getLocationById(Long locationId){
        Session session = currentSession();
        Query sqlQuery = session.getNamedQuery("GET_LOCATION_SEAL_BY_ID")
                     .setParameter("locationId", locationId);
        List<Object> resultList = sqlQuery.list();
        Location location = null;
        if (resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            if(resultListIterator.hasNext()) {
            	location = new Location();
            	Object[] row = (Object[]) resultListIterator.next();
                location = new Location();
                location.setId(this.getLongFromInteger(row[0]));
                location.setDescription(this.getString(row[1]));
                Site site = new Site();
                site.setId(this.getLongFromInteger(row[2]));
                location.setSite(site);
                location.setStateDescription(this.getString(row[3]));
                location.setClerkName(this.getString(row[4]));
                location.setDesignation(this.getString(row[5]));
                location.setNoteOfAuthenticity(this.getString(row[6]));
                location.setLocationCode(this.getString(row[7]));
                location.setSealOfAuthenticity(row[8] == null ? null : (byte[]) row[8]);
            }
        }
        return location;
	}

	public void archivePayAsUGoTransactions(String archivedBy, String archiveComments) {
		DateTime dateTime = new DateTime().minusMonths(18);
    	DateTimeFormatter format = DateTimeFormat.forPattern("MM/dd/yyyy");
    	String toDate = format.print(dateTime);
    	Session session = currentSession();
        session.getNamedQuery("ARCHIVE_PAYASUGO_TX")
        							.setParameter("toDate", toDate)
                                    .setParameter("archivedBy", archivedBy)
                                    .setParameter("archiveComments", archiveComments).list();


	}

	
	public Double getGranicusRevenueFromPayAsUGoTx(Site site) {
		Double granicusRevenue = 0.0d;
		if(site!=null && site.getRevenueThresholdStartDate() != null && site.getName()!= null) {
			DateTime dateTime = new DateTime(site.getRevenueThresholdStartDate());
	    	DateTimeFormatter format = DateTimeFormat.forPattern("MM/dd/yyyy");
	    	String revenueThresholdStartDate = format.print(dateTime);
	    	Session session = currentSession();    	
	    	Query sqlQuery  = session.getNamedQuery("GET_GRANICUS_REVENUE_FROM_PAYASUGO_TX")
	        							.setParameter("siteName", site.getName())
	        							.setParameter("revenueThresholdStartDate", revenueThresholdStartDate);
	    	List<Object> resultList = sqlQuery.list();
	        if (resultList.size() > 0) {
	        	granicusRevenue = this.getDoubleFromBigDecimal(resultList.get(0));
	        }
		}
		return granicusRevenue;
	}

	public String getDocumentIdByCertifiedDocumentNumber(String certifiedDocumentNumber, String siteName) {
		String documentId = null;
		Date dateTimeCreated = new Date();
		if(!StringUtils.isBlank(certifiedDocumentNumber)) {
			Session session = currentSession();    	
	    	Query sqlQuery  = session.getNamedQuery("GET_DOCUMENTID_BY_CERTIFIED_DOCUMENT_NUMBER")
	        							.setParameter("certifiedDocumentNumber", certifiedDocumentNumber)
        								.setParameter("siteName", siteName);
	    	List<Object> resultList = sqlQuery.list();
	        if (resultList.size() > 0) {
	        	 ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
	             Object[] row = (Object[]) resultListIterator.next();
	        	documentId = this.getString(row[0]);
	        	dateTimeCreated =  this.getDate(row[1]);
	        }
	        if (Days.daysBetween(new DateTime(dateTimeCreated).withTimeAtStartOfDay(),
	                new DateTime().withTimeAtStartOfDay()).getDays()  > 180) {
	        	documentId = "-1";
	        }
		}
		return documentId;
	}

}
