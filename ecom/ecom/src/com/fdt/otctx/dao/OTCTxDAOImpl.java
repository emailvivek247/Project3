package com.fdt.otctx.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import com.fdt.common.dao.AbstractBaseDAOImpl;
import com.fdt.ecom.entity.Merchant;
import com.fdt.ecom.entity.Site;
import com.fdt.otctx.entity.OTCTx;

@Repository
public class OTCTxDAOImpl extends AbstractBaseDAOImpl implements OTCTxDAO {

    public void saveOTCTx(OTCTx oTCTransaction) {
        Session session = currentSession();
        session.saveOrUpdate(oTCTransaction);
        session.flush();
    }

    public OTCTx getOTCTxByTxRefNum(String txRefNumber, String siteName) {
        Session session = currentSession();
        Query sqlQuery  = session.getNamedQuery("GET_OTC_TX_BY_TX_REF_NUM")
                                    .setParameter("txRefNumber", txRefNumber)
                                    .setParameter("siteName", siteName);
        List<Object> resultSet = sqlQuery.list();
        OTCTx otcTransaction = null;
        if (resultSet.size() > 0) {
            otcTransaction = new OTCTx();
            Site site = new Site();
            Merchant merchant = new Merchant();
            Object[] row = (Object[]) resultSet.get(0);
            otcTransaction.setId(this.getLongFromBigInteger(row[0]));
            otcTransaction.setTxRefNum(this.getString(row[1]));
            otcTransaction.setOrigTxRefNum(this.getString(row[2]));
            otcTransaction.setBaseAmount(this.getDoubleFromBigDecimal(row[3]));
            otcTransaction.setServiceFee(this.getDoubleFromBigDecimal(row[4]));
            otcTransaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[5]));
            otcTransaction.setComments(this.getString(row[6]));
            otcTransaction.setTransactionType(this.getTransactionType(row[7]));
            otcTransaction.setCheckNum(this.getString(row[8]));
            otcTransaction.setTransactionDate(this.getDate(row[9]));
            otcTransaction.setCardNumber(this.getString(row[10]));
            otcTransaction.setAccountName(this.getString(row[11]));
            otcTransaction.setMachineName(this.getString(row[12]));
            otcTransaction.setModifiedBy(this.getString(row[13]));
            otcTransaction.setCreatedDate(this.getDate(row[14]));
            otcTransaction.setSettlementStatus(this.getSettlementStatusType(row[15]));
            otcTransaction.setCardType(this.getCardType(row[16]));
            otcTransaction.setSignature(row[17] == null ? null : (byte[]) row[17]);
            otcTransaction.setTxFeePercent(this.getDoubleFromBigDecimal(row[30]));
            otcTransaction.setTxFeeFlat(this.getDoubleFromBigDecimal(row[31]));
            Long siteIdReferred = this.getLongFromInteger(row[18]);
            site.setId(siteIdReferred);
            otcTransaction.setSiteId(siteIdReferred);
            site.setName(this.getString(row[19]));
            site.setDescription(this.getString(row[29]));
            site.setTimeZone(this.getString(row[32]));
            merchant.setId(this.getLongFromInteger(row[20]));
            merchant.setPartner(this.getString(row[21]));
            merchant.setVendorName(this.getString(row[22]));
            merchant.setUserName(this.getString(row[23]));
            merchant.setPassword(row[24] == null ? null : this.getPbeStringEncryptor().decrypt(row[24].toString()));
            merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[25]));
            merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[26]));
            merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[27]));
            merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[28]));
            otcTransaction.setItemName(this.getString(row[33]));
            otcTransaction.setProductType(this.getString(row[34]));
            otcTransaction.setInvoiceNumber(this.getString(row[35]));
            site.addMerchant(merchant);
            otcTransaction.setSite(site);
            otcTransaction.setMerchantId(merchant.getId());
        }
        return otcTransaction;
    }

    public OTCTx getOTCTransactionByInvoiceNumber(String invoiceNumber, String siteName) {
    	Session session = currentSession();
        Query sqlQuery  = session.getNamedQuery("GET_OTC_TX_BY_INVOICE_NUM")
                                    .setParameter("invoiceNumber", invoiceNumber)
                                    .setParameter("siteName", siteName);
        List<Object> resultSet = sqlQuery.list();
        OTCTx otcTransaction = null;
        if (resultSet.size() > 0) {
            otcTransaction = new OTCTx();
            Site site = new Site();
            Merchant merchant = new Merchant();
            Object[] row = (Object[]) resultSet.get(0);
            otcTransaction.setId(this.getLongFromBigInteger(row[0]));
            otcTransaction.setTxRefNum(this.getString(row[1]));
            otcTransaction.setOrigTxRefNum(this.getString(row[2]));
            otcTransaction.setBaseAmount(this.getDoubleFromBigDecimal(row[3]));
            otcTransaction.setServiceFee(this.getDoubleFromBigDecimal(row[4]));
            otcTransaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[5]));
            otcTransaction.setComments(this.getString(row[6]));
            otcTransaction.setTransactionType(this.getTransactionType(row[7]));
            otcTransaction.setCheckNum(this.getString(row[8]));
            otcTransaction.setTransactionDate(this.getDate(row[9]));
            otcTransaction.setCardNumber(this.getString(row[10]));
            otcTransaction.setAccountName(this.getString(row[11]));
            otcTransaction.setMachineName(this.getString(row[12]));
            otcTransaction.setModifiedBy(this.getString(row[13]));
            otcTransaction.setCreatedDate(this.getDate(row[14]));
            otcTransaction.setSettlementStatus(this.getSettlementStatusType(row[15]));
            otcTransaction.setCardType(this.getCardType(row[16]));
            otcTransaction.setSignature(row[17] == null ? null : (byte[]) row[17]);
            otcTransaction.setTxFeePercent(this.getDoubleFromBigDecimal(row[30]));
            otcTransaction.setTxFeeFlat(this.getDoubleFromBigDecimal(row[31]));
            Long siteIdReferred = this.getLongFromInteger(row[18]);
            site.setId(siteIdReferred);
            otcTransaction.setSiteId(siteIdReferred);
            site.setName(this.getString(row[19]));
            site.setDescription(this.getString(row[29]));
            site.setTimeZone(this.getString(row[32]));
            merchant.setId(this.getLongFromInteger(row[20]));
            merchant.setPartner(this.getString(row[21]));
            merchant.setVendorName(this.getString(row[22]));
            merchant.setUserName(this.getString(row[23]));
            merchant.setPassword(row[24] == null ? null : this.getPbeStringEncryptor().decrypt(row[24].toString()));
            merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[25]));
            merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[26]));
            merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[27]));
            merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[28]));
            otcTransaction.setItemName(this.getString(row[33]));
            otcTransaction.setProductType(this.getString(row[34]));
            otcTransaction.setInvoiceNumber(this.getString(row[35]));
            site.addMerchant(merchant);
            otcTransaction.setSite(site);
            otcTransaction.setMerchantId(merchant.getId());
        }
        return otcTransaction;
	}

    public OTCTx getReferencedOTCTx(String txRefNumber, String siteName) {
        Session session = currentSession();
        Query sqlQuery  = session.getNamedQuery("GET_REFERENCED_OTC_TX_BY_TX_REF_NUM")
                                    .setParameter("txRefNumber", txRefNumber)
                                    .setParameter("siteName", siteName);
        List<Object> resultSet = sqlQuery.list();
        OTCTx otcTransaction = null;
        if (resultSet.size() > 0) {
            otcTransaction = new OTCTx();
            Site site = new Site();
            Merchant merchant = new Merchant();
            Object[] row = (Object[]) resultSet.get(0);
            otcTransaction.setId(this.getLongFromBigInteger(row[0]));
            otcTransaction.setTxRefNum(this.getString(row[1]));
            otcTransaction.setOrigTxRefNum(this.getString(row[2]));
            otcTransaction.setBaseAmount(this.getDoubleFromBigDecimal(row[3]));
            otcTransaction.setServiceFee(this.getDoubleFromBigDecimal(row[4]));
            otcTransaction.setTotalTxAmount(this.getDoubleFromBigDecimal(row[5]));
            otcTransaction.setComments(this.getString(row[6]));
            otcTransaction.setTransactionType(this.getTransactionType(row[7]));
            otcTransaction.setCheckNum(this.getString(row[8]));
            otcTransaction.setTransactionDate(this.getDate(row[9]));
            otcTransaction.setCardNumber(this.getString(row[10]));
            otcTransaction.setAccountName(this.getString(row[11]));
            otcTransaction.setMachineName(this.getString(row[12]));
            otcTransaction.setModifiedBy(this.getString(row[13]));
            otcTransaction.setCreatedDate(this.getDate(row[14]));
            otcTransaction.setSettlementStatus(this.getSettlementStatusType(row[15]));
            otcTransaction.setCardType(this.getCardType(row[16]));
            site.setName(this.getString(row[17]));
            merchant.setPartner(this.getString(row[18]));
            merchant.setVendorName(this.getString(row[19]));
            merchant.setUserName(this.getString(row[20]));
            merchant.setPassword( row[21] == null ? null : this.getPbeStringEncryptor().decrypt(row[21].toString()));
            merchant.setTxFeePercent(this.getDoubleFromBigDecimal(row[22]));
            merchant.setTxFeeFlat(this.getDoubleFromBigDecimal(row[23]));
            merchant.setTxFeePercentAmex(this.getDoubleFromBigDecimal(row[24]));
            merchant.setTxFeeFlatAmex(this.getDoubleFromBigDecimal(row[25]));
            site.addMerchant(merchant);
            otcTransaction.setSite(site);
        }
        return otcTransaction;
    }




//    public List<OTCTransaction> lookupTx(String txRefNumber, String cardName, String cardNumber, String txDate,
//        	String siteName) {
//        Session session = currentSession();
//        Properties params = new Properties();
//        params.put("enumClass", "com.fdt.ecom.entity.enums.TransactionType");
//        params.put("type", "12");
//        Type myEnumType = new TypeLocatorImpl(new TypeResolver()).custom(EnumType.class, params);
//        Query sqlQuery  = ((SQLQuery)session.getNamedQuery("LOOKUP_TRANSACTION"))
//        		.addEntity(OTCTransaction.class);
//        								 /*.addScalar("transactionType", Hibernate.custom(EnumType.class, params) )
//        								 .setResultTransformer(Transformers.aliasToBean(OTCTransaction.class));*/
//        List<OTCTransaction> resultList = (List<OTCTransaction>)sqlQuery.list();
//        return resultList;
//    }

 }
