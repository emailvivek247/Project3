package com.fdt.subscriptions.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fdt.common.entity.AbstractBaseEntity;
import com.fdt.ecom.entity.Code;

@Entity
@Table(name = "ECOMM_SUBSCRIPTIONFEE")
public class SubscriptionFee extends AbstractBaseEntity {

    private static final long serialVersionUID = 5748852945124128611L;

    @Column(name = "FEE", nullable = false)
    private Double fee;

    @Column(name = "TERM", nullable = false)
    private Long term = null;

    @Column(name = "CURRENCY", nullable = false)
    private String currency = null;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description = null;

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "SUBSCRIPTION_TYP_ID", nullable=false)
    private Code paymentPeriod;

    public Code getPaymentPeriod() {
        return paymentPeriod;
    }

    public void setPaymentPeriod(Code paymentPeriod) {
        this.paymentPeriod = paymentPeriod;
    }

    public Double getFee() {
        return fee;
    }

    public void setFee(Double fee) {
        this.fee = fee;
    }

    public Long getTerm() {
        return term;
    }

    public void setTerm(Long term) {
        this.term = term;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "SubscriptionFee [fee=" + fee + ", term=" + term + ", currency="
                + currency + ", description=" + description
                + ", paymentPeriod=" + paymentPeriod + ", id=" + id
                + ", createdDate=" + createdDate + ", modifiedDate="
                + modifiedDate + ", modifiedBy=" + modifiedBy + ", createdBy="
                + createdBy + ", active=" + active + "]";
    }
}
