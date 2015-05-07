package com.fdt.webtx.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.fdt.common.dao.AbstractBaseDAOImpl;
import com.fdt.ecom.entity.Merchant;
import com.fdt.ecom.entity.Site;
import com.fdt.webtx.entity.WebTx;
import com.fdt.webtx.entity.WebTxItem;

@Repository
public class WebTxDAOImpl extends AbstractBaseDAOImpl implements WebTxDAO {

    private static final Logger logger = LoggerFactory.getLogger(WebTxDAOImpl.class);

    public void saveWebTransactionItem(List<WebTxItem> webTransactionItems) {
        Session session = currentSession();
        for(WebTxItem webTransactionItem: webTransactionItems){
            session.saveOrUpdate(webTransactionItem);
        }
        session.flush();
    }

    public WebTx getWebTransactionByTxRefNum(String txRefNumber, String siteName) {
        Session session = currentSession();
        Query sqlQuery  = session.getNamedQuery("GET_WEB_TX_BY_TX_REF_NUM")
                                    .setParameter("txRefNumber", txRefNumber)
                                    .setParameter("siteName", siteName);
        List<Object> resultSet = sqlQuery.list();
        WebTx webTransaction = null;
        if (resultSet.size() > 0) {
            webTransaction = new WebTx();
            Site site = new Site();
            Merchant merchant = new Merchant();
            Object[] row = (Object[]) resultSet.get(0);
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultSet.listIterator();
            webTransaction.setId(this.getLongFromBigInteger(row[0]));
            webTransaction.setTxRefNum(this.getString(row[1]));
            webTransaction.setOrigTxRefNum(this.getString(row[2]));
            webTransaction.setBaseAmount(this.getDoubleFromBigDecimal(row[3]));
            webTransaction.setServiceFee(this.getDoubleFromBigDecimal(row[4]));
            webTransaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[5]));
            webTransaction.setComments(this.getString(row[6]));
            webTransaction.setTransactionType(this.getTransactionType(row[7]));
            webTransaction.setCheckNum(this.getString(row[8]));
            webTransaction.setTransactionDate(this.getDate(row[9]));
            webTransaction.setCardNumber(this.getString(row[10]));
            webTransaction.setAccountName(this.getString(row[11]));
            webTransaction.setMachineName(this.getString(row[12]));
            webTransaction.setCreatedDate(this.getDate(row[13]));
            webTransaction.setSettlementStatus(this.getSettlementStatusType(row[14]));
            webTransaction.setModifiedBy(this.getString(row[31]));
            webTransaction.setOfficeLoc(this.getString(row[44]));
            webTransaction.setOfficeLocAddressLine1(this.getString(row[45]));
            webTransaction.setOfficeLocAddressLine2(this.getString(row[46]));
            webTransaction.setOfficeLocCity(this.getString(row[47]));
            webTransaction.setOfficeLocState(this.getString(row[48]));
            webTransaction.setOfficeLocZip(this.getString(row[49]));
            webTransaction.setOfficeLocPhone(this.getString(row[50]));
            webTransaction.setOfficeLocComments1(this.getString(row[51]));
            webTransaction.setOfficeLocComments2(this.getString(row[52]));
            site.setName(this.getString(row[15]));
            site.setId(this.getLongFromInteger(row[32]));
            site.setDescription(this.getString(row[38]));
            site.setTimeZone(this.getString(row[43]));
            merchant.setId(this.getLongFromInteger(row[30]));
            merchant.setPartner(this.getString(row[16]));
            merchant.setVendorName(this.getString(row[17]));
            merchant.setUserName(this.getString(row[18]));
            merchant.setPassword(row[19] == null ? null : this.getPbeStringEncryptor().decrypt(row[19].toString()));
            merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[33]));
            merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[34]));
            merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[36]));
            merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[37]));
            webTransaction.setCardType(this.getCardType(row[35]));
            site.addMerchant(merchant);
            webTransaction.setSite(site);
            webTransaction.setTxFeePercent(this.getDoubleFromBigDecimal(row[39]));
            webTransaction.setTxFeeFlat(this.getDoubleFromBigDecimal(row[40]));
            webTransaction.setCreatedBy(this.getString(row[41]));
            webTransaction.setExpiryMonth(this.getInteger(row[54]));
            webTransaction.setExpiryYear(this.getInteger(row[55]));
            webTransaction.setAddressLine1(this.getString(row[56]));
            webTransaction.setAddressLine2(this.getString(row[57]));
            webTransaction.setCity(this.getString(row[58]));
            webTransaction.setState(this.getString(row[59]));
            webTransaction.setZip(this.getString(row[60]));
            webTransaction.setPhone(this.getLongFromBigInteger(row[61]));
            webTransaction.setInvoiceNumber(this.getString(row[63]));
            List<WebTxItem> webTxItems =  new LinkedList<WebTxItem>();
            while(resultListIterator.hasNext()) {
                row = (Object[]) resultListIterator.next();
                WebTxItem webTxItem = new WebTxItem();
                webTxItem.setId(this.getLongFromBigInteger(row[20]));
                webTxItem.setProductId(this.getString(row[21]));
                webTxItem.setProductType(this.getString(row[22]));
                webTxItem.setPageCount(this.getLongFromInteger(row[23]));
                webTxItem.setBaseAmount(this.getDoubleFromBigDecimal(row[24]));
                webTxItem.setServiceFee(this.getDoubleFromBigDecimal(row[25]));
                webTxItem.setTotalTxAmount(this.getDoubleFromBigDecimal(row[26]));
                webTxItem.setRefunded(this.getBoolean(row[27]));
                webTxItem.setModifiedDate(this.getDate(row[28]));
                webTxItem.setComments(this.getString(row[29]));
                webTxItem.setCreatedDate(this.getDate(row[42]));
                webTxItem.setCaseNumber(this.getString(row[53]));
                webTxItem.setItemName(this.getString(row[62]));
                webTxItems.add(webTxItem);
            }
            webTransaction.setWebTxItems(webTxItems);
        }
        return webTransaction;
	}

    public List<WebTx> getWebTransactionsForExtApp(String siteName, Date fromDate, Date endDate,
    	String txType) {
        Session session = currentSession();

        String txTypeStr = txType == null ? "" : txType.toString();

        Query sqlQuery  = session.getNamedQuery("GET_WEB_TX_BY_DATE")
        						   .setParameter("siteName", siteName)
                                   .setParameter("fromDate", fromDate)
                                   .setParameter("toDate", endDate)
                                   .setParameter("txType", txTypeStr);
        List<Object> resultList = sqlQuery.list();
        WebTx webTransaction = null;
        List<WebTx> webTransactionList = new LinkedList<WebTx>();
        if (resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            Map<Long, WebTx> uniqueWebTransactions =  new HashMap<Long, WebTx>();
            while(resultListIterator.hasNext()) {
            	Object[] row = (Object[]) resultListIterator.next();
            	Long webTransactionId = this.getLongFromBigInteger(row[0]);
            	if(uniqueWebTransactions.get(webTransactionId) == null) {
            		 webTransaction = new WebTx();
            		 webTransaction.setId(webTransactionId);
            		 webTransaction.setTxRefNum(this.getString(row[1]));
            		 webTransaction.setCardNumber(this.getString(row[2]));
            		 webTransaction.setBaseAmount(this.getDoubleFromBigDecimal(row[3]));
            		 webTransaction.setServiceFee(this.getDoubleFromBigDecimal(row[4]));
                     webTransaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[5]));
                     webTransaction.setTax(this.getDoubleFromBigDecimal(row[6]));
                     webTransaction.setTransactionType(this.getTransactionType(row[7]));
                     webTransaction.setAccountName(this.getString(row[8]));
                     webTransaction.setOrigTxRefNum(this.getString(row[9]));
                     webTransaction.setSettlementStatus(this.getSettlementStatusType(row[10]));
                     webTransaction.setTransactionDate(this.getDate(row[11]));
                     webTransaction.setModifiedBy(this.getString(row[12]));
                     webTransaction.setComments(this.getString(row[13]));
                     webTransaction.setItemsPurchased(this.getLongFromInteger(row[14]));
                     webTransaction.setItemsRefunded(this.getLongFromInteger(row[15]));
                     webTransaction.setApplication(this.getString(row[16]));
                     webTransaction.setOfficeLoc(this.getString(row[31]));
                     webTransaction.setOfficeLocAddressLine1(this.getString(row[32]));
                     webTransaction.setOfficeLocAddressLine2(this.getString(row[33]));
                     webTransaction.setOfficeLocCity(this.getString(row[34]));
                     webTransaction.setOfficeLocState(this.getString(row[35]));
                     webTransaction.setOfficeLocZip(this.getString(row[36]));
                     webTransaction.setOfficeLocPhone(this.getString(row[37]));
                     webTransaction.setOfficeLocComments1(this.getString(row[38]));
                     webTransaction.setOfficeLocComments2(this.getString(row[39]));
                     webTransaction.setInvoiceNumber(this.getString(row[40]));
                     webTransaction.setExpiryMonth(this.getInteger(row[41]));
                     webTransaction.setExpiryYear(this.getInteger(row[42]));
                     webTransaction.setAddressLine1(this.getString(row[43]));
                     webTransaction.setAddressLine2(this.getString(row[44]));
                     webTransaction.setCity(this.getString(row[45]));
                     webTransaction.setState(this.getString(row[46]));
                     webTransaction.setZip(this.getString(row[47]));
                     webTransaction.setPhone(this.getLongFromBigInteger(row[48]));
                     webTransactionList.add(webTransaction);
                     uniqueWebTransactions.put(webTransactionId, webTransaction);
            	} else {
            		webTransaction = uniqueWebTransactions.get(webTransactionId);
            	}
            	WebTxItem webTransactionItem = new WebTxItem();
            	webTransactionItem.setId(this.getLongFromBigInteger(row[17]));
            	webTransactionItem.setProductId(this.getString(row[18]));
            	webTransactionItem.setItemName(this.getString(row[19]));
            	webTransactionItem.setProductType(this.getString(row[20]));
            	webTransactionItem.setItemQuantity(this.getLongFromInteger(row[21]));
            	webTransactionItem.setBaseAmount(this.getDoubleFromBigDecimal(row[22]));
            	webTransactionItem.setServiceFee(this.getDoubleFromBigDecimal(row[23]));
            	webTransactionItem.setTax(this.getDoubleFromBigDecimal(row[24]));
            	webTransactionItem.setTotalTxAmount(this.getDoubleFromBigDecimal(row[25]));
            	webTransactionItem.setRefundTxId(this.getLongFromInteger(row[26]));
            	webTransactionItem.setRefunded(this.getBoolean(row[27]));
            	webTransactionItem.setCaseNumber(this.getString(row[28]));
            	webTransactionItem.setPartyRole(this.getString(row[29]));
            	webTransactionItem.setPartySeq(this.getString(row[30]));
            	webTransaction.setWebTxItem(webTransactionItem);

            }
        }
        return webTransactionList;
    }

    public WebTx getWebTransactionItemByItemId(Long itemId, String siteName) {
        Session session = currentSession();
        Query sqlQuery  = session.getNamedQuery("GET_WEB_TX_ITEM_BY_ITEM_ID")
                                    .setParameter("itemId", itemId)
                                    .setParameter("siteName", siteName);
        List<Object> resultSet = sqlQuery.list();
        WebTx webTransaction = null;
        if (resultSet.size() > 0) {
            webTransaction = new WebTx();
            Site site = new Site();
            Merchant merchant = new Merchant();
            Object[] row = (Object[]) resultSet.get(0);
            List<WebTxItem> webTxItems =  new LinkedList<WebTxItem>();
            WebTxItem webTxItem = new WebTxItem();
            webTxItem.setId(this.getLongFromBigInteger(row[0]));
            webTxItem.setProductId(this.getString(row[1]));
            webTxItem.setProductType(this.getString(row[2]));
            webTxItem.setPageCount(this.getLongFromInteger(row[3]));
            webTxItem.setBaseAmount(this.getDoubleFromBigDecimal(row[4]));
            webTxItem.setServiceFee(this.getDoubleFromBigDecimal(row[5]));
            webTxItem.setTotalTxAmount(this.getDoubleFromBigDecimal(row[6]));
            webTxItem.setRefunded(this.getBoolean(row[7]));
            webTxItem.setModifiedDate(this.getDate(row[8]));
            webTxItem.setModifiedBy(this.getString(row[9]));
            webTxItems.add(webTxItem);
            webTransaction.setWebTxItems(webTxItems);
            webTransaction.setTxRefNum(this.getString(row[10]));
            webTransaction.setOrigTxRefNum(this.getString(row[11]));
            webTransaction.setBaseAmount(this.getDoubleFromBigDecimal(row[12]));
            webTransaction.setServiceFee(this.getDoubleFromBigDecimal(row[13]));
            webTransaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[14]));
            webTransaction.setComments(this.getString(row[15]));
            webTransaction.setTransactionType(this.getTransactionType(row[16]));
            webTransaction.setCheckNum(this.getString(row[17]));
            webTransaction.setMachineName(this.getString(row[18]));
            webTransaction.setTransactionDate(this.getDate(row[19]));
            webTransaction.setCardNumber(this.getString(row[20]));
            webTransaction.setCardType(this.getCardType(row[33]));
            webTransaction.setAccountName(this.getString(row[21]));
            webTransaction.setSettlementStatus(this.getSettlementStatusType(row[22]));
            webTransaction.setModifiedBy(this.getString(row[23]));
            site.setName(this.getString(row[24]));
            site.setId(this.getLongFromInteger(row[30]));
            site.setTimeZone(this.getString(row[38]));
            merchant.setPartner(this.getString(row[25]));
            merchant.setVendorName(this.getString(row[26]));
            merchant.setUserName(this.getString(row[27]));
            merchant.setPassword( row[28] == null ? null : this.getPbeStringEncryptor().decrypt(row[28].toString()));
            merchant.setId(this.getLongFromInteger(row[29]));
            merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[31]));
            merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[32]));
            merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[34]));
            merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[35]));
            site.addMerchant(merchant);
            webTransaction.setMerchantId(this.getLongFromInteger(row[29]));
            webTransaction.setTxFeePercent(this.getDoubleFromBigDecimal(row[36]));
            webTransaction.setTxFeeFlat(this.getDoubleFromBigDecimal(row[37]));
            webTransaction.setSite(site);
        }
        return webTransaction;
    }


    public WebTx getReferencedWebTransactionItemByItemId(Long itemId, String siteName) {
        Session session = currentSession();
        Query sqlQuery  = session.getNamedQuery("GET_REFERENCED_WEB_TX_ITEM_BY_ITEM_ID")
                                    .setParameter("itemId", itemId)
                                    .setParameter("siteName", siteName);
        List<Object> resultSet = sqlQuery.list();
        WebTx webTransaction = null;
        if (resultSet.size() > 0) {
            webTransaction = new WebTx();
            Site site = new Site();
            Merchant merchant = new Merchant();
            Object[] row = (Object[]) resultSet.get(0);
            List<WebTxItem> webTxItems =  new LinkedList<WebTxItem>();
            WebTxItem webTxItem = new WebTxItem();
            webTxItem.setId(this.getLongFromBigInteger(row[0]));
            webTxItem.setProductId(this.getString(row[1]));
            webTxItem.setProductType(this.getString(row[2]));
            webTxItem.setPageCount(this.getLongFromInteger(row[3]));
            webTxItem.setBaseAmount(this.getDoubleFromBigDecimal(row[4]));
            webTxItem.setServiceFee(this.getDoubleFromBigDecimal(row[5]));
            webTxItem.setTotalTxAmount(this.getDoubleFromBigDecimal(row[6]));
            webTxItem.setRefunded(this.getBoolean(row[7]));
            webTxItem.setModifiedDate(this.getDate(row[8]));
            webTxItem.setModifiedBy(this.getString(row[9]));
            webTxItem.setItemName(this.getString(row[34]));
            webTxItem.setItemQuantity(this.getLongFromInteger(row[35]));
            webTxItem.setTax(this.getDoubleFromBigDecimal(row[36]));
            webTxItems.add(webTxItem);
            webTransaction.setWebTxItems(webTxItems);
            webTransaction.setTxRefNum(this.getString(row[10]));
            webTransaction.setOrigTxRefNum(this.getString(row[11]));
            webTransaction.setBaseAmount(this.getDoubleFromBigDecimal(row[12]));
            webTransaction.setServiceFee(this.getDoubleFromBigDecimal(row[13]));
            webTransaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[14]));
            webTransaction.setComments(this.getString(row[15]));
            webTransaction.setTransactionType(this.getTransactionType(row[16]));
            webTransaction.setCheckNum(this.getString(row[17]));
            webTransaction.setMachineName(this.getString(row[18]));
            webTransaction.setTransactionDate(this.getDate(row[19]));
            webTransaction.setCardNumber(this.getString(row[20]));
            webTransaction.setAccountName(this.getString(row[21]));
            webTransaction.setSettlementStatus(this.getSettlementStatusType(row[22]));
            webTransaction.setModifiedBy(this.getString(row[23]));
            site.setName(this.getString(row[24]));
            merchant.setPartner(this.getString(row[25]));
            merchant.setVendorName(this.getString(row[26]));
            merchant.setUserName(this.getString(row[27]));
            merchant.setPassword( row[31] == null ? null : this.getPbeStringEncryptor().decrypt(row[28].toString()));
            merchant.setId(this.getLongFromInteger(row[29]));
            merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[30]));
            merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[31]));
            merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[32]));
            merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[33]));
            site.addMerchant(merchant);
            webTransaction.setMerchantId(this.getLongFromInteger(row[29]));
            webTransaction.setTax(this.getDoubleFromBigDecimal(row[37]));
            webTransaction.setApplication(this.getString(row[38]));
            webTransaction.setSite(site);
        }
        return webTransaction;
    }


   public List<WebTx> getReferencedWebTransaction(String txRefNumber, String siteName) {
        List<WebTx> webTransactionList = new LinkedList<WebTx>();
        Session session = currentSession();
        Query sqlQuery  = session.getNamedQuery("GET_REFERENCED_WEB_TX_BY_TX_REF_NUM")
                                    .setParameter("txRefNumber", txRefNumber)
                                    .setParameter("siteName", siteName);
        List<Object> resultSet = sqlQuery.list();
        if (resultSet.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultSet.listIterator();
            while(resultListIterator.hasNext()) {
                WebTx webTransaction = new WebTx();
                webTransaction = new WebTx();
                Site site = new Site();
                Merchant merchant = new Merchant();
                Object[] row = (Object[]) resultListIterator.next();
                webTransaction.setId(this.getLongFromBigInteger(row[0]));
                webTransaction.setTxRefNum(this.getString(row[1]));
                webTransaction.setOrigTxRefNum(this.getString(row[2]));
                webTransaction.setBaseAmount(this.getDoubleFromBigDecimal(row[3]));
                webTransaction.setServiceFee(this.getDoubleFromBigDecimal(row[4]));
                webTransaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[5]));
                webTransaction.setComments(this.getString(row[6]));
                webTransaction.setTransactionType(this.getTransactionType(row[7]));
                webTransaction.setCheckNum(this.getString(row[8]));
                webTransaction.setTransactionDate(this.getDate(row[9]));
                webTransaction.setCardNumber(this.getString(row[10]));
                webTransaction.setAccountName(this.getString(row[11]));
                webTransaction.setMachineName(this.getString(row[12]));
                webTransaction.setCreatedDate(this.getDate(row[13]));
                webTransaction.setSettlementStatus(this.getSettlementStatusType(row[14]));
                site.setName(this.getString(row[15]));
                webTransaction.setSite(site);

                merchant.setPartner(this.getString(row[16]));
                merchant.setVendorName(this.getString(row[17]));
                merchant.setUserName(this.getString(row[18]));
                merchant.setPassword(row[19] == null ? null : this.getPbeStringEncryptor().decrypt(row[19].toString()));


                merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[32]));
                merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[33]));
                merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[34]));
                merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[35]));
                site.addMerchant(merchant);

                List<WebTxItem> webTxItems =  new LinkedList<WebTxItem>();
                WebTxItem webTxItem = new WebTxItem();
                webTxItem.setProductId(this.getString(row[20]));
                webTxItem.setProductType(this.getString(row[21]));
                webTxItem.setPageCount(this.getLongFromInteger(row[22]));
                webTxItem.setBaseAmount(this.getDoubleFromBigDecimal(row[23]));
                webTxItem.setTotalTxAmount(this.getDoubleFromBigDecimal(row[24]));
                webTransaction.setModifiedBy(this.getString(row[25]));
                webTransaction.setTax(this.getDoubleFromBigDecimal(row[26]));
                webTransaction.setApplication(this.getString(row[27]));
                webTxItem.setItemName(this.getString(row[28]));
                webTxItem.setItemQuantity(this.getLongFromInteger(row[29]));
                webTxItem.setServiceFee(this.getDoubleFromBigDecimal(row[30]));
                webTxItem.setTax(this.getDoubleFromBigDecimal(row[31]));
                webTxItems.add(webTxItem);

                webTransaction.setWebTxItems(webTxItems);
                webTransactionList.add(webTransaction);
            }
        }
        return webTransactionList;
    }


    public int updateRefundTxForWebTxItem(Long webTxItemId, Long refundTxId, String modifiedBy) {
        Session session = currentSession();
        int noOfRecordsUpdated = session.createQuery("Update WebTxItem webTxItem SET " +
                "webTxItem.refundTxId = :refundTxId, " +
                "webTxItem.refunded = :refunded, " +
                "webTxItem.modifiedDate = :modifiedDate, " +
                "webTxItem.modifiedBy = :modifiedBy " +
                "WHERE webTxItem.id = :webTxItemId")
               .setParameter("refundTxId", refundTxId)
               .setParameter("refunded", Boolean.TRUE)
               .setParameter("modifiedDate", new Date())
               .setParameter("modifiedBy", modifiedBy)
               .setParameter("webTxItemId", webTxItemId)
        .executeUpdate();
        return noOfRecordsUpdated;
    }

    public int updateRefundTxForWebTxItems(Long originalTxId, Long refundTxId, String modifiedBy) {
        Session session = currentSession();
        int noOfRecordsUpdated = session.createQuery("Update WebTxItem webTxItem SET " +
                "webTxItem.refundTxId = :refundTxId, " +
                "webTxItem.refunded = :refunded, " +
                "webTxItem.modifiedDate = :modifiedDate, " +
                "webTxItem.modifiedBy = :modifiedBy " +
                "WHERE webTxItem.webTxId = :originalTxId")
               .setParameter("refundTxId", refundTxId)
               .setParameter("refunded", Boolean.TRUE)
               .setParameter("modifiedDate", new Date())
               .setParameter("modifiedBy", modifiedBy)
               .setParameter("originalTxId", originalTxId)
        .executeUpdate();
        return noOfRecordsUpdated;
    }


    public void saveWebTransaction(WebTx webTransaction) {
        Session session = currentSession();
        session.saveOrUpdate(webTransaction);
        session.flush();
    }

	public WebTx getWebTxByInvoiceNumber(String invoiceNumber, String siteName) {
		Session session = currentSession();
        Query sqlQuery  = session.getNamedQuery("GET_WEB_TX_BY_INVOICE_ID")
                                    .setParameter("invoiceNumber", invoiceNumber)
                                    .setParameter("siteName", siteName);
        List<Object> resultSet = sqlQuery.list();
        WebTx webTransaction = null;
        if (resultSet.size() > 0) {
            webTransaction = new WebTx();
            Site site = new Site();
            Merchant merchant = new Merchant();
            Object[] row = (Object[]) resultSet.get(0);
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultSet.listIterator();
            webTransaction.setId(this.getLongFromBigInteger(row[0]));
            webTransaction.setTxRefNum(this.getString(row[1]));
            webTransaction.setOrigTxRefNum(this.getString(row[2]));
            webTransaction.setBaseAmount(this.getDoubleFromBigDecimal(row[3]));
            webTransaction.setServiceFee(this.getDoubleFromBigDecimal(row[4]));
            webTransaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[5]));
            webTransaction.setComments(this.getString(row[6]));
            webTransaction.setTransactionType(this.getTransactionType(row[7]));
            webTransaction.setCheckNum(this.getString(row[8]));
            webTransaction.setTransactionDate(this.getDate(row[9]));
            webTransaction.setCardNumber(this.getString(row[10]));
            webTransaction.setAccountName(this.getString(row[11]));
            webTransaction.setMachineName(this.getString(row[12]));
            webTransaction.setCreatedDate(this.getDate(row[13]));
            webTransaction.setSettlementStatus(this.getSettlementStatusType(row[14]));
            webTransaction.setModifiedBy(this.getString(row[31]));
            webTransaction.setOfficeLoc(this.getString(row[44]));
            webTransaction.setOfficeLocAddressLine1(this.getString(row[45]));
            webTransaction.setOfficeLocAddressLine2(this.getString(row[46]));
            webTransaction.setOfficeLocCity(this.getString(row[47]));
            webTransaction.setOfficeLocState(this.getString(row[48]));
            webTransaction.setOfficeLocZip(this.getString(row[49]));
            webTransaction.setOfficeLocPhone(this.getString(row[50]));
            webTransaction.setOfficeLocComments1(this.getString(row[51]));
            webTransaction.setOfficeLocComments2(this.getString(row[52]));
            site.setName(this.getString(row[15]));
            site.setId(this.getLongFromInteger(row[32]));
            site.setDescription(this.getString(row[38]));
            site.setTimeZone(this.getString(row[43]));
            merchant.setId(this.getLongFromInteger(row[30]));
            merchant.setPartner(this.getString(row[16]));
            merchant.setVendorName(this.getString(row[17]));
            merchant.setUserName(this.getString(row[18]));
            merchant.setPassword(row[19] == null ? null : this.getPbeStringEncryptor().decrypt(row[19].toString()));
            merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[33]));
            merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[34]));
            merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[36]));
            merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[37]));
            webTransaction.setCardType(this.getCardType(row[35]));
            site.addMerchant(merchant);
            webTransaction.setSite(site);
            webTransaction.setTxFeePercent(this.getDoubleFromBigDecimal(row[39]));
            webTransaction.setTxFeeFlat(this.getDoubleFromBigDecimal(row[40]));
            webTransaction.setCreatedBy(this.getString(row[41]));
            webTransaction.setExpiryMonth(this.getInteger(row[54]));
            webTransaction.setExpiryYear(this.getInteger(row[55]));
            webTransaction.setAddressLine1(this.getString(row[56]));
            webTransaction.setAddressLine2(this.getString(row[57]));
            webTransaction.setCity(this.getString(row[58]));
            webTransaction.setState(this.getString(row[59]));
            webTransaction.setZip(this.getString(row[60]));
            webTransaction.setPhone(this.getLongFromBigInteger(row[61]));
            webTransaction.setInvoiceNumber(invoiceNumber);
            List<WebTxItem> webTxItems =  new LinkedList<WebTxItem>();
            while(resultListIterator.hasNext()) {
                row = (Object[]) resultListIterator.next();
                WebTxItem webTxItem = new WebTxItem();
                webTxItem.setId(this.getLongFromBigInteger(row[20]));
                webTxItem.setProductId(this.getString(row[21]));
                webTxItem.setProductType(this.getString(row[22]));
                webTxItem.setPageCount(this.getLongFromInteger(row[23]));
                webTxItem.setBaseAmount(this.getDoubleFromBigDecimal(row[24]));
                webTxItem.setServiceFee(this.getDoubleFromBigDecimal(row[25]));
                webTxItem.setTotalTxAmount(this.getDoubleFromBigDecimal(row[26]));
                webTxItem.setRefunded(this.getBoolean(row[27]));
                webTxItem.setModifiedDate(this.getDate(row[28]));
                webTxItem.setComments(this.getString(row[29]));
                webTxItem.setCreatedDate(this.getDate(row[42]));
                webTxItem.setCaseNumber(this.getString(row[53]));
                webTxItem.setItemName(this.getString(row[62]));
                webTxItems.add(webTxItem);
            }
            webTransaction.setWebTxItems(webTxItems);
        }
        return webTransaction;
	}

}
