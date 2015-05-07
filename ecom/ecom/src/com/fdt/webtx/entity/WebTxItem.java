package com.fdt.webtx.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.fdt.common.entity.AbstractBaseEntity;

@Entity
@Table(name = "ECOMM_WEBPAY_TX_ITEMS")
public class WebTxItem extends AbstractBaseEntity {

    private static final long serialVersionUID = 7062025856050968413L;

    @Column(name = "PRODUCT_ID")
    private String productId = null;

    @Column(name = "ITEM_NAME")
    private String itemName = null;

    @Column(name = "PRODUCT_TYPE")
    private String productType = null;

    @Column(name = "PAGE_COUNT")
    private Long pageCount = 0L;

    @Column(name = "ITEM_QUANTITY")
    private Long itemQuantity = 1L;

    @Column(name = "AMOUNT")
    private Double baseAmount = 0.0d;

    @Column(name = "SERVICE_FEE", nullable = false)
    private Double serviceFee = 0.0d;

    @Column(name = "TAX")
    private Double tax = 0.0d;

    @Column(name = "TOTAL_AMOUNT", nullable = false)
    private Double totalTxAmount = 0.0d;

    @Column(name = "WEBTX_ID", nullable = false)
    private Long webTxId = null;


    @Column(name = "REFUND_TRAN_ID")
    private Long refundTxId = null;

    @Column(name = "IS_REFUNDED", nullable = false)
    @Type(type="yes_no")
    protected boolean refunded = false;

    @Column(name = "CASE_NUMBER")
    private String caseNumber = null;

    @Column(name = "PARTY_ROLE")
    private String partyRole = null;

    @Column(name = "PARTY_SEQ")
    private String partySeq = null;

    @Transient
    private String comments = null;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public Long getPageCount() {
		return pageCount;
	}

	public void setPageCount(Long pageCount) {
		this.pageCount = pageCount;
	}

	public Long getItemQuantity() {
		return itemQuantity;
	}

	public void setItemQuantity(Long itemQuantity) {
		this.itemQuantity = itemQuantity;
	}

	public Double getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(Double baseAmount) {
        this.baseAmount = baseAmount;
    }

    public Double getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(Double serviceFee) {
        this.serviceFee = serviceFee;
    }

    public Double getTax() {
        return tax;
    }

    public void setTax(Double tax) {
        this.tax = tax;
    }

    public Double getTotalTxAmount() {
        return totalTxAmount;
    }

    public void setTotalTxAmount(Double totalTxAmount) {
        this.totalTxAmount = totalTxAmount;
    }

    public Long getWebTxId() {
        return webTxId;
    }

    public void setWebTxId(Long webTxId) {
        this.webTxId = webTxId;
    }

    public boolean isRefunded() {
        return refunded;
    }

    public void setRefunded(boolean refunded) {
        this.refunded = refunded;
    }

    public Long getRefundTxId() {
        return refundTxId;
    }

    public void setRefundTxId(Long refundTxId) {
        this.refundTxId = refundTxId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getPartyRole() {
        return partyRole;
    }

    public void setPartyRole(String partyRole) {
        this.partyRole = partyRole;
    }

    public String getPartySeq() {
        return partySeq;
    }

    public void setPartySeq(String partySeq) {
        this.partySeq = partySeq;
    }

    @Override
    public String toString() {
        return "WebTransactionItem [productId=" + productId + ", itemName="
                + itemName + ", productType=" + productType + ", pageCount="
                + pageCount + ", itemQuantity=" + itemQuantity
                + ", baseAmount=" + baseAmount + ", serviceFee=" + serviceFee
                + ", tax=" + tax + ", totalTxAmount=" + totalTxAmount
                + ", webTxId=" + webTxId
                + ", refundTxId=" + refundTxId + ", refunded=" + refunded
                + ", caseNumber=" + caseNumber + ", partyRole=" + partyRole
                + ", partySeq=" + partySeq + ", comments=" + comments + ", id="
                + id + ", createdDate=" + createdDate + ", modifiedDate="
                + modifiedDate + ", modifiedBy=" + modifiedBy + ", createdBy="
                + createdBy + ", active=" + active + "]";
    }
}
