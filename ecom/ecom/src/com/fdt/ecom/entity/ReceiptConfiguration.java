package com.fdt.ecom.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import com.fdt.common.entity.AbstractBaseEntity;
import com.fdt.ecom.entity.enums.PaymentType;

@Entity
@Table(name="ECOMM_RECEIPT_CONFIGURATION")
public class ReceiptConfiguration extends AbstractBaseEntity {

    private static final long serialVersionUID = -6827624352031126235L;

    @Column(name = "BUSINESSNAME", nullable = false)
    private String businessName = null;

    @Column(name = "ADDRESS_LINE_1", nullable = false)
    private String addressLine1 = null;

    @Column(name = "ADDRESS_LINE_2")
    private String addressLine2 = null;

    @Column(name = "CITY", nullable = false)
    private String city = null;

    @Column(name = "STATE", nullable = false)
    private String state = null;

    @Column(name = "ZIP", nullable = false)
    private String zip= null;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "COMMENTS_1")
    private String comments1= null;

    @Column(name = "COMMENTS_2")
    private String comments2= null;

    @Column(name="TYPE")
    @Enumerated(EnumType.STRING)
    private PaymentType type = null;

    @Column(name = "SITE_ID", nullable = false)
    private Long siteId = null;

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getComments1() {
        return comments1;
    }

    public void setComments1(String comments1) {
        this.comments1 = comments1;
    }

    public String getComments2() {
        return comments2;
    }

    public void setComments2(String comments2) {
        this.comments2 = comments2;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public PaymentType getType() {
        return type;
    }

    public void setType(PaymentType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ReceiptConfiguration [businessName=" + businessName
                + ", addressLine1=" + addressLine1 + ", addressLine2="
                + addressLine2 + ", city=" + city + ", state=" + state
                + ", zip=" + zip + ", phone=" + phone + ", comments1="
                + comments1 + ", comments2=" + comments2 + ", type=" + type
                + ", siteId=" + siteId + ", id=" + id + ", createdDate="
                + createdDate + ", modifiedDate=" + modifiedDate
                + ", modifiedBy=" + modifiedBy + ", createdBy=" + createdBy
                + ", active=" + active + "]";
    }
}
