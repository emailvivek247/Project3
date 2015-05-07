package com.fdt.ecom.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.jasypt.hibernate4.type.EncryptedStringType;

import com.fdt.common.entity.AbstractBaseEntity;

@TypeDef (name="encryptedString", typeClass= EncryptedStringType.class,
        parameters= {
            @Parameter(name="encryptorRegisteredName",  value="hibernateStringEncryptor")
        }
    )
@Entity
@Table(name = "ECOMM_MERCHANTINFO")
public class Merchant extends AbstractBaseEntity {

    private static final long serialVersionUID = 883369123820776387L;

    @Column(name = "USERNAME", nullable = false)
    private String userName = null;

    @Type(type="encryptedString")
    @Column(name = "PASSWORD", nullable = false)
    private String password = null;

    @Column(name = "VENDORNAME")
    private String vendorName = null;

    @Column(name = "PARTNER")
    private String partner = null;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SITE_ID", unique=true, nullable=false)
    private Site site = null;

    @Column(name = "IS_MICROPAYMENT_ACCOUNT", nullable = false)
    @Type(type="yes_no")
    private boolean isMicroPaymentAccount = false;

    @Column(name="TRAN_FEE_PERCENTAGE")
    private Double txFeePercent;

    @Column(name="TRAN_FEE_FLAT")
    private Double txFeeFlat;

    @Column(name="TRAN_FEE_PERCENTAGE_AMEX", nullable = false)
    private Double txFeePercentAmex;

    @Column(name="TRAN_FEE_FLAT_AMEX", nullable = false)
    private Double txFeeFlatAmex;

    public Double getTxFeePercentAmex() {
        return txFeePercentAmex;
    }

    public void setTxFeePercentAmex(Double txFeePercentAmex) {
        this.txFeePercentAmex = txFeePercentAmex;
    }

    public Double getTxFeeFlatAmex() {
        return txFeeFlatAmex;
    }

    public void setTxFeeFlatAmex(Double txFeeFlatAmex) {
        this.txFeeFlatAmex = txFeeFlatAmex;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public boolean isMicroPaymentAccount() {
        return isMicroPaymentAccount;
    }

    public void setMicroPaymentAccount(boolean isMicroPaymentAccount) {
        this.isMicroPaymentAccount = isMicroPaymentAccount;
    }

    public Double getTxFeePercent() {
        return txFeePercent;
    }

    public void setTxFeePercent(Double txFeePercent) {
        this.txFeePercent = txFeePercent;
    }

    public Double getTxFeeFlat() {
        return txFeeFlat;
    }

    public void setTxFeeFlat(Double txFeeFlat) {
        this.txFeeFlat = txFeeFlat;
    }

}
