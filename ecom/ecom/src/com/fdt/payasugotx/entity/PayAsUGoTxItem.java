package com.fdt.payasugotx.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.fdt.common.entity.AbstractBaseEntity;

@Entity
@Table(name = "ECOMM_PAYASUGO_TX_ITEMS")
public class PayAsUGoTxItem extends AbstractBaseEntity {

    private static final long serialVersionUID = 7062025856050968413L;

    @Column(name = "PRODUCT_ID")
    private String productId = null;

    @Column(name = "ITEM_NAME")
    private String itemName = null;

    @Column(name = "PRODUCT_TYPE")
    private String productType = null;

    @Column(name = "PAGE_COUNT")
    private int pageCount = 0;

    @Column(name = "ITEM_QUANTITY")
    private int itemQuantity = 1;

    @Column(name = "AMOUNT")
    private Double baseAmount = 0.0d;

    @Column(name = "SERVICE_FEE", nullable = false)
    private Double serviceFee = 0.0d;

    @Column(name = "TAX")
    private Double tax = 0.0d;

    @Column(name = "TOTAL_AMOUNT", nullable = false)
    private Double totalTxAmount = 0.0d;

    @Column(name = "PAYASUGO_TX_ID", nullable = false)
    private Long payAsUGoTxId = null;

    @Column(name = "DOWNLOAD_URL")
    private String downloadURL = null;

    @Column(name = "REFUND_TRAN_ID")
    private Long refundTxId = null;

    @Column(name = "IS_REFUNDED", nullable = false)
    @Type(type="yes_no")
    protected boolean refunded = false;

    @Column(name = "IS_DOCUMENT_AVAILABLE")
    @Type(type="yes_no")
    protected boolean documentAvailable = false;

    @Column(name = "UNIQUE_IDENTIFIER")
    private String uniqueIdentifier = null;

    @Column(name = "COMMENTS")
    private String comments = null;

    @Column(name="LOCATION_ID")
    private Long locationId;

    @Column(name="BAR_NUMBER")
    private String barNumber;
    
    @Column(name = "CERTIFIED_DOCUMENT_NUMBER")
    private String certifiedDocumentNumber = null;

    @Transient
    private String locationName;
    
    @Transient
    private boolean isCertified;


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

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(int itemQuantity) {
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

    public Long getPayAsUGoTxId() {
        return payAsUGoTxId;
    }

    public void setPayAsUGoTxId(Long webTxId) {
        this.payAsUGoTxId = webTxId;
    }

    public boolean isRefunded() {
        return refunded;
    }

    public void setRefunded(boolean refunded) {
        this.refunded = refunded;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
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

    public boolean isDocumentAvailable() {
        return documentAvailable;
    }

    public void setDocumentAvailable(boolean documentAvailable) {
        this.documentAvailable = documentAvailable;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }


	public Long getLocationId() {
		return locationId;
	}

	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}

	public String getBarNumber() {
		return barNumber;
	}

	public void setBarNumber(String barNumber) {
		this.barNumber = barNumber;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getCertifiedDocumentNumber() {
		return certifiedDocumentNumber;
	}

	public void setCertifiedDocumentNumber(String certifiedDocumentNumber) {
		this.certifiedDocumentNumber = certifiedDocumentNumber;
	}
	
	

	public boolean isCertified() {
		return isCertified;
	}

	public void setCertified(boolean isCertified) {
		this.isCertified = isCertified;
	}

	@Override
    public String toString() {
        return "PayAsUGoTransactionItem [productId=" + productId + ", itemName="
                + itemName + ", productType=" + productType + ", pageCount="
                + pageCount + ", itemQuantity=" + itemQuantity
                + ", baseAmount=" + baseAmount + ", serviceFee=" + serviceFee
                + ", tax=" + tax + ", totalTxAmount=" + totalTxAmount
                + ", webTxId=" + payAsUGoTxId + ", downloadURL=" + downloadURL
                + ", refundTxId=" + refundTxId + ", refunded=" + refunded
                + ", documentAvailable="
                + documentAvailable + ", uniqueIdentifier=" + uniqueIdentifier
                + ", comments=" + comments + ", id="  + id
				+ ",locationId " + locationId
				+ ",locationName " + locationName
				+ ",barNumber " + barNumber
				+ ",certifiedDocumentNumber " + certifiedDocumentNumber
                + ", createdDate=" + createdDate + ", modifiedDate="
                + modifiedDate + ", modifiedBy=" + modifiedBy + ", createdBy="
                + createdBy + ", active=" + active + "]";
    }
}
