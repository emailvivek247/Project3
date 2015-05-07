package com.fdt.ecom.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fdt.common.entity.AbstractBaseEntity;


@Entity
@Table(name = "ECOMM_BANK_DETAILS")
public class BankDetails extends AbstractBaseEntity {

    private static final long serialVersionUID = 5159238024463770240L;

    @Column(name = "SITE_ID", nullable = false)
    private Long siteId = null;

    @Column(name = "FROM_FNAME", nullable = false)
    private String fromFirstName  = null;

    @Column(name = "FROM_LNAME", nullable = false)
    private String fromLastName  = null;

    @Column(name = "FROM_MINITIAL", nullable = false)
    private String fromMiddleIntial  = null;

    @Column(name = "FROM_ADDRLINE1", nullable = false)
    private String fromAddressLine1  = null;

    @Column(name = "FROM_ADDRLINE2", nullable = false)
    private String fromAddressLine2  = null;

    @Column(name = "FROM_CITY", nullable = false)
    private String fromCity  = null;

    @Column(name = "FROM_STATE", nullable = false)
    private String fromState  = null;

    @Column(name = "FROM_ZIPCODE", nullable = false)
    private String fromZipcode  = null;

    @Column(name = "FROM_PHONENUM", nullable = false)
    private String fromPhoneNumber  = null;

    @Column(name = "BANK_NAME", nullable = false)
    private String bankName  = null;

    @Column(name = "BANK_CODE", nullable = false)
    private String bankCode  = null;

    @Column(name = "ROUTING_NUM", nullable = false)
    private String routingNumber  = null;

    @Column(name = "ACCOUNT_NUM", nullable = false)
    private String accountNumber  = null;

    @Column(name = "LAST_ISSUED_CHECK_NUM", nullable = false)
    private Long lastIssuedCheckNumber  = null;

    @Column(name = "BANK_ADDRLINE1")
    private String bankAddressLine1  = null;

    @Column(name = "BANK_ADDRLINE2")
    private String bankAddressLine2 = null;

    @Column(name = "BANK_CITY")
    private String bankCity  = null;

    @Column(name = "BANK_STATE")
    private String bankState  = null;

    @Column(name = "BANK_ZIPCODE")
    private String bankZipcode  = null;

    @Column(name = "START_CHECK_NUM", nullable = false)
    private Long startCheckNumber = null;

    @Column(name = "END_CHECK_NUM", nullable = false)
    private Long endCheckNumber = null;

    @Transient
    private String custAccountName  = null;

    public String getCustAccountName() {
        return custAccountName;
    }

    public void setCustAccountName(String custAccountName) {
        this.custAccountName = custAccountName;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public String getFromFirstName() {
        return fromFirstName;
    }

    public void setFromFirstName(String fromFirstName) {
        this.fromFirstName = fromFirstName;
    }

    public String getFromLastName() {
        return fromLastName;
    }

    public void setFromLastName(String fromLastName) {
        this.fromLastName = fromLastName;
    }

    public String getFromMiddleIntial() {
        return fromMiddleIntial;
    }

    public void setFromMiddleIntial(String fromMiddleIntial) {
        this.fromMiddleIntial = fromMiddleIntial;
    }

    public String getFromAddressLine1() {
        return fromAddressLine1;
    }

    public void setFromAddressLine1(String fromAddressLine1) {
        this.fromAddressLine1 = fromAddressLine1;
    }

    public String getFromAddressLine2() {
        return fromAddressLine2;
    }

    public void setFromAddressLine2(String fromAddressLine2) {
        this.fromAddressLine2 = fromAddressLine2;
    }

    public String getFromCity() {
        return fromCity;
    }

    public void setFromCity(String fromCity) {
        this.fromCity = fromCity;
    }

    public String getFromState() {
        return fromState;
    }

    public void setFromState(String fromState) {
        this.fromState = fromState;
    }

    public String getFromZipcode() {
        return fromZipcode;
    }

    public void setFromZipcode(String fromZipcode) {
        this.fromZipcode = fromZipcode;
    }

    public String getFromPhoneNumber() {
        return fromPhoneNumber;
    }

    public void setFromPhoneNumber(String fromPhoneNumber) {
        this.fromPhoneNumber = fromPhoneNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Long getLastIssuedCheckNumber() {
        return lastIssuedCheckNumber;
    }

    public void setLastIssuedCheckNumber(Long lastIssuedCheckNumber) {
        this.lastIssuedCheckNumber = lastIssuedCheckNumber;
    }

    public String getBankAddressLine1() {
        return bankAddressLine1;
    }

    public void setBankAddressLine1(String bankAddressLine1) {
        this.bankAddressLine1 = bankAddressLine1;
    }

    public String getBankAddressLine2() {
        return bankAddressLine2;
    }

    public void setBankAddressLine2(String bankAddressLine2) {
        this.bankAddressLine2 = bankAddressLine2;
    }

    public String getBankCity() {
        return bankCity;
    }

    public void setBankCity(String bankCity) {
        this.bankCity = bankCity;
    }

    public String getBankState() {
        return bankState;
    }

    public void setBankState(String bankState) {
        this.bankState = bankState;
    }

    public String getBankZipcode() {
        return bankZipcode;
    }

    public void setBankZipcode(String bankZipcode) {
        this.bankZipcode = bankZipcode;
    }

    public Long getStartCheckNumber() {
        return startCheckNumber;
    }

    public void setStartCheckNumber(Long startCheckNumber) {
        this.startCheckNumber = startCheckNumber;
    }

    public Long getEndCheckNumber() {
        return endCheckNumber;
    }

    public void setEndCheckNumber(Long endCheckNumber) {
        this.endCheckNumber = endCheckNumber;
    }

    @Override
    public String toString() {
        return "BankDetails [siteId=" + siteId + ", fromFirstName="
                + fromFirstName + ", fromLastName=" + fromLastName
                + ", fromMiddleIntial=" + fromMiddleIntial
                + ", fromAddressLine1=" + fromAddressLine1
                + ", fromAddressLine2=" + fromAddressLine2 + ", fromCity="
                + fromCity + ", fromState=" + fromState + ", fromZipcode="
                + fromZipcode + ", fromPhoneNumber=" + fromPhoneNumber
                + ", bankName=" + bankName + ", bankCode=" + bankCode
                + ", routingNumber=" + routingNumber + ", accountNumber="
                + accountNumber + ", lastIssuedCheckNumber="
                + lastIssuedCheckNumber + ", bankAddressLine1="
                + bankAddressLine1 + ", bankAddressLine2=" + bankAddressLine2
                + ", bankCity=" + bankCity + ", bankState=" + bankState
                + ", bankZipcode=" + bankZipcode + ", startCheckNumber="
                + startCheckNumber + ", endCheckNumber=" + endCheckNumber
                + ", custAccountName=" + custAccountName + ", id=" + id
                + ", createdDate=" + createdDate + ", modifiedDate="
                + modifiedDate + ", modifiedBy=" + modifiedBy + ", createdBy="
                + createdBy + ", active=" + active + "]";
    }
}
