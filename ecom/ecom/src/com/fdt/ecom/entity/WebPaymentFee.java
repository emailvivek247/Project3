package com.fdt.ecom.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fdt.common.entity.AbstractBaseEntity;

@Entity
@Table(name = "ECOMM_WEBPAYMENT_FEE")
public class WebPaymentFee extends AbstractBaseEntity {

    private static final long serialVersionUID = 3034094710955520315L;

    @Column(name = "TX_FEE_FLAT", nullable = false)
    private Double flatFee = 0.0d;

    @Column(name = "TX_FLAT_FEE_CUTOFF_AMT", nullable = false)
    private Double flatFeeCutOff = 0.0d;

    @Column(name = "TX_FEE_PERCENT", nullable = false)
    private Double percenteFee = 0.0d;

    @Column(name = "TX_FEE_ADDITIONAL", nullable = false)
    private Double additionalFee = 0.0d;

    @Column(name = "MICRO_TX_CUT_OFF")
    private Double microTxFeeCutOff = 0.0d;

    @Transient
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="SITE_ID", nullable=false, unique=true)
    private Site site = null;

    public Double getFlatFee() {
        return flatFee;
    }

    public void setFlatFee(Double flatFee) {
        this.flatFee = flatFee;
    }

    public Double getFlatFeeCutOff() {
        return flatFeeCutOff;
    }

    public void setFlatFeeCutOff(Double flatFeeCutOff) {
        this.flatFeeCutOff = flatFeeCutOff;
    }

    public Double getPercenteFee() {
        return percenteFee;
    }

    public void setPercenteFee(Double percenteFee) {
        this.percenteFee = percenteFee;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public Double getAdditionalFee() {
        return additionalFee;
    }

    public void setAdditionalFee(Double additionalFee) {
        this.additionalFee = additionalFee;
    }

    public Double getMicroTxFeeCutOff() {
        return microTxFeeCutOff;
    }

    public void setMicroTxFeeCutOff(Double microTxFeeCutOff) {
        this.microTxFeeCutOff = microTxFeeCutOff;
    }

    @Override
    public String toString() {
        return "WebPaymentFee [flatFee=" + flatFee + ", flatFeeCutOff="
                + flatFeeCutOff + ", percenteFee=" + percenteFee
                + ", additionalFee=" + additionalFee + ", microTxFeeCutOff="
                + microTxFeeCutOff + ", site=" + site + ", id=" + id
                + ", createdDate=" + createdDate + ", modifiedDate="
                + modifiedDate + ", modifiedBy=" + modifiedBy + ", createdBy="
                + createdBy + ", active=" + active + "]";
    }
}
