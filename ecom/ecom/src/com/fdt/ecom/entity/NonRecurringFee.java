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
@Table(name = "ECOMM_NON_RECURRING_FEE")
public class NonRecurringFee extends AbstractBaseEntity {

    private static final long serialVersionUID = 5792501729440817761L;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SUBSCRIPTION_TYP_ID", unique=true, nullable=false)
    private Code code = null;

    @Column(name="FEE_UNDER_PAGE_THRESHOLD", nullable = false)
    private Double feeUnderPageThreshold = null;

    @Column(name="PAGE_THRESHOLD", nullable = false)
    private Long pageThreshold = null;

    @Column(name="FEE_OVER_PAGE_THRESHOLD", nullable = false)
    private Double feeOverPageThreshold = null;

    @Column(name="IS_SERVICE_FEE", nullable = false)
    private boolean isServiceFee;

    @Column(name="CURRENCY", nullable = false)
    private String currency = null;

    @Transient
    private boolean sumTxamountPlusServiceFee = false;

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public Double getFeeUnderPageThreshold() {
        return feeUnderPageThreshold;
    }

    public void setFeeUnderPageThreshold(Double feeUnderPageThreshold) {
        this.feeUnderPageThreshold = feeUnderPageThreshold;
    }

    public Long getPageThreshold() {
        return pageThreshold;
    }

    public void setPageThreshold(Long pageThreshold) {
        this.pageThreshold = pageThreshold;
    }

    public Double getFeeOverPageThreshold() {
        return feeOverPageThreshold;
    }

    public void setFeeOverPageThreshold(Double feeOverPageThreshold) {
        this.feeOverPageThreshold = feeOverPageThreshold;
    }

    public boolean isServiceFee() {
        return isServiceFee;
    }

    public void setServiceFee(boolean isServiceFee) {
        this.isServiceFee = isServiceFee;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

	public boolean isSumTxamountPlusServiceFee() {
		return sumTxamountPlusServiceFee;
	}

	public void setSumTxamountPlusServiceFee(boolean sumTxamountPlusServiceFee) {
		this.sumTxamountPlusServiceFee = sumTxamountPlusServiceFee;
	}

	@Override
    public String toString() {
        return "NonRecurringFee [code=" + code + ", feeUnderPageThreshold="
                + feeUnderPageThreshold + ", pageThreshold=" + pageThreshold
                + ", feeOverPageThreshold=" + feeOverPageThreshold
                + ", isServiceFee=" + isServiceFee + ", currency=" + currency
        		+ "sumTxamountPlusServiceFee=" + sumTxamountPlusServiceFee
                + ", id=" + id + ", createdDate=" + createdDate
                + ", modifiedDate=" + modifiedDate + ", modifiedBy="
                + modifiedBy + ", createdBy=" + createdBy + ", active="
                + active + "]";
    }
}
