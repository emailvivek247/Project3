package com.fdt.common.dao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fdt.ecom.entity.enums.CardType;
import com.fdt.ecom.entity.enums.PaymentType;
import com.fdt.ecom.entity.enums.SettlementStatusType;
import com.fdt.ecom.entity.enums.TransactionType;
import com.fdt.security.entity.enums.AccessType;

public abstract class AbstractBaseDAOImpl {

    @Autowired
    protected SessionFactory sessionFactory;

    @Autowired
    @Qualifier(value="strongEncryptor")
    protected PBEStringEncryptor pbeStringEncryptor;

    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    protected void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    protected Session currentSession() {
        Session session = sessionFactory.getCurrentSession();
        session.setFlushMode(FlushMode.MANUAL);
        return session;
    }

    protected PBEStringEncryptor getPbeStringEncryptor() {
        return pbeStringEncryptor;
    }

    protected Long getLongFromBigInteger(Object object) {
        return object == null ? null : (Long)((BigInteger)object).longValue();
    }

    protected Long getLongFromInteger(Object object) {
        return object == null ? null : (Long)((Integer)object).longValue();
    }

    protected Long getLongFromShort(Object object) {
        return object == null ? null : (Long)((Short)object).longValue();
    }

    protected Integer getIntFromInteger(Object object) {
        return object == null ? null : ((Integer)object).intValue();
    }

    protected Integer getInteger(Object object) {
        return object == null ? null : ((Integer) object);
    }

    protected Long getLong(Object object) {
        return object == null ? null : ((Long) object);
    }

    protected Double getDoubleFromBigDecimal(Object object) {
        return object == null ? null : (Double) ((BigDecimal) object).doubleValue();
    }

    protected String getString(Object object) {
        return object == null ? null : object.toString();
    }

    protected String getStringFromBigInteger(Object object) {
        BigInteger b = null;
        b = object == null ? null : ((BigInteger)object);
        return String.valueOf(b);
    }

    protected Date getDate(Object object) {
        return object == null ? null :  (Date)((Timestamp)object);
    }

    protected Boolean getBoolean(Object object) {
        return object == null ? false :  this.convertToBoolean(object.toString());
    }

    protected Long getLongFromString(Object object) {
        return object == null ? null :  Long.valueOf(object.toString());
    }

    protected Integer getIntegerFromString(Object object) {
        return object == null ? null :  Integer.valueOf(object.toString());
    }

    protected byte[] getBytes(Blob fromImageBlob) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
              return toByteArrayImpl(fromImageBlob, baos);
        } catch (Exception e) {}
        return null;
    }

    protected byte[] toByteArrayImpl(Blob fromImageBlob, ByteArrayOutputStream baos) throws SQLException, IOException {
        byte buf[] = new byte[4000];
        int dataSize;
        InputStream is = fromImageBlob.getBinaryStream();
        try {
            while((dataSize = is.read(buf)) != -1) {
                baos.write(buf, 0, dataSize);
            }
        } finally {
            if(is != null) {
                is.close();
            }
        }
        return baos.toByteArray();
    }

    protected boolean convertToBoolean(String string) {
        if(string.equals("Y")){
            return true;
        } else {
            return false;
        }
    }

    protected CardType getCardType(Object object) {
        CardType cardType = null;
        if(object == null){
            cardType = null;
        } else {
            String string = object.toString();
            if(string.equals("MASTER")){
                cardType = CardType.MASTER;
            } else if(string.equals("VISA")){
                cardType = CardType.VISA;
            } else if(string.equals("AMEX")){
                cardType = CardType.AMEX;
            } else if(string.equals("DISCOVER")){
                cardType = CardType.DISCOVER;
            }
        }
        return cardType;
    }

    protected AccessType getAccessType(Object object) {
        AccessType accessType = null;
        if(object == null){
            accessType = null;
        } else {
            String string = object.toString();
            if(string.equals(AccessType.RECURRING_SUBSCRIPTION.toString())){
                accessType = AccessType.RECURRING_SUBSCRIPTION;
            } else if(string.equals(AccessType.NON_RECURRING_SUBSCRIPTION.toString())){
                accessType = AccessType.NON_RECURRING_SUBSCRIPTION;
            } else if(string.equals(AccessType.FREE_SUBSCRIPTION.toString())){
                accessType = AccessType.FREE_SUBSCRIPTION;
            } else if(string.equals(AccessType.FIRM_DOC_BASED.toString())){
                accessType = AccessType.FIRM_DOC_BASED;
            } else if(string.equals(AccessType.FIRM_USER_BASED.toString())){
                accessType = AccessType.FIRM_USER_BASED;
            } if(string.equals(AccessType.CERTIFIED_NON_RECURRING_SUBSCRIPTION.toString())){
                accessType = AccessType.CERTIFIED_NON_RECURRING_SUBSCRIPTION;
            }
        }
        return accessType;
    }

    protected SettlementStatusType getSettlementStatusType(Object object) {
        SettlementStatusType settlementStatusType = null;
        if(object == null){
            settlementStatusType = null;
        } else {
            String string = object.toString();
            if(string.equals("SETTLED")){
                settlementStatusType = SettlementStatusType.SETTLED;
            } else if(string.equals("REFUNDED")){
                settlementStatusType = SettlementStatusType.REFUNDED;
            } else if(string.equals("VOIDED")){
                settlementStatusType = SettlementStatusType.VOIDED;
            } else if(string.equals("UNSETTLED")){
                settlementStatusType = SettlementStatusType.UNSETTLED;
            }
        }
        return settlementStatusType;
    }

    protected PaymentType getPaymentType(Object object) {
        PaymentType paymentType = null;
        if(object == null){
            paymentType = null;
        } else {
            String string = object.toString();
            if(string.equals("RECURRING")){
               paymentType = PaymentType.RECURRING;
            } else if(string.equals("OTC")){
                paymentType = PaymentType.OTC;
            } else if(string.equals("WEB")){
                paymentType = PaymentType.WEB;
            } else if(string.equals("PAYASUGO")){
                paymentType = PaymentType.PAYASUGO;
            }
        }
        return paymentType;
    }

    protected TransactionType getTransactionType(Object object) {
        TransactionType transactionType = null;
        if(object == null) {
            transactionType = null;
        } else {
            String string = object.toString();
            if(string.equals("CHARGE")){
                transactionType = TransactionType.CHARGE;
            } else if(string.equals("REFUND")){
                transactionType = TransactionType.REFUND;
            }
        }
        return transactionType;
    }


}