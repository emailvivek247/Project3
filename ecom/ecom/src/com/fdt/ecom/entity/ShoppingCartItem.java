package com.fdt.ecom.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.fdt.common.entity.AbstractBaseEntity;
import com.fdt.security.entity.Access;
import com.fdt.security.entity.User;

@Entity
@Table(name = "ECOMM_USERS_SHOPPING_CART")
public class ShoppingCartItem extends AbstractBaseEntity {

    private static final long serialVersionUID = 1518692310957231336L;

    @Column(name = "USER_ID", nullable = false)
    private Long userId = null;

    @Column(name = "NODE_NAME", nullable = false)
    private String nodeName = null;

    @Column(name = "PRODUCT_ID", nullable = false)
    private String productId = null;

    @Column(name = "PRODUCT_TYPE", nullable = false)
    private String productType = null;

    @Column(name = "PAGE_COUNT", nullable = false)
    private int pageCount = 0;

    @Column(name = "DOWNLOAD_URL", nullable = false)
    private String downloadURL = null;

    @Column(name = "ACCESS_NAME")
    private String accessName = null;

    @Column(name = "UNIQUE_IDENTIFIER")
    private String uniqueIdentifier = null;

    @Column(name = "APPLICATION")
    private String application = null;

    @Column(name = "BAR_NUMBER")
    private String barNumber = null;

    @Column(name = "COMMENTS")
    private String comments = null;

    @Column(name = "IS_CERTIFIED", nullable = false)
    @Type(type="yes_no")
    protected boolean isCertified = false;

    @Column(name="LOCATION_ID")
    private Long locationId;

    @Transient
    private Double baseAmount = 0.00;

    @Transient
    private Double serviceFee = 0.00;

    @Transient
    private Double totalTxAmount = 0.00;

    @Transient
    private Access access = null;

    @Transient
    private int documentsPurchased = 0;

    @Transient
    private User user;

    @Transient
    private boolean firmAccessAdmin;

    @Transient
    private String locationName;

    @Transient
    private String stateCode;
    
    
    @Transient
    private boolean sumTxamountPlusServiceFee = false;

    public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public boolean isCertified() {
		return isCertified;
	}

	public void setCertified(boolean isCertified) {
		this.isCertified = isCertified;
	}

	public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
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

    public Double getTotalTxAmount() {
        return totalTxAmount;
    }

    public void setTotalTxAmount(Double totalTxAmount) {
        this.totalTxAmount = totalTxAmount;
    }

    public String getAccessName() {
        return accessName;
    }

    public void setAccessName(String accessName) {
        this.accessName = accessName;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

	public String getBarNumber() {
		return barNumber;
	}

	public void setBarNumber(String barNumber) {
		this.barNumber = barNumber;
	}

	public int getDocumentsPurchased() {
		return documentsPurchased;
	}

	public void setDocumentsPurchased(int documentsPurchased) {
		this.documentsPurchased = documentsPurchased;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean isFirmAccessAdmin() {
		return firmAccessAdmin;
	}

	public void setFirmAccessAdmin(boolean firmAccessAdmin) {
		this.firmAccessAdmin = firmAccessAdmin;
	}

	public Long getLocationId() {
		return locationId;
	}

	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}


	public String getStateCode() {
		return stateCode;
	}

	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}

	public boolean isSumTxamountPlusServiceFee() {
		return sumTxamountPlusServiceFee;
	}

	public void setSumTxamountPlusServiceFee(boolean sumTxamountPlusServiceFee) {
		this.sumTxamountPlusServiceFee = sumTxamountPlusServiceFee;
	}

	@Override
    public String toString() {
        return "ShoppingCartItem ["
        		+ "userId=" + userId
        		+ "nodeName=" + nodeName
        		+ "productId=" + productId
        		+ "pageCount=" + pageCount
        		+ "productType=" + productType
        		+ "downloadURL=" + downloadURL
        		+ "accessName=" + accessName
        		+ "uniqueIdentifier=" + uniqueIdentifier
        		+ "application=" + application
        		+ "barNumber=" + barNumber
        		+ "comments=" + comments
        		+ "locationId=" + locationId
        		+ "baseAmount=" + baseAmount
        		+ "serviceFee=" + serviceFee
        		+ "totalTxAmount=" +totalTxAmount
        		+ "access=" + access
        		+ "documentsPurchased=" + documentsPurchased
        		+ "user=" + user
        		+ "firmAccessAdmin=" + firmAccessAdmin
        		+ "locationName=" + locationName
        		+ "stateCode=" + stateCode
        		+ "sumTxamountPlusServiceFee=" + sumTxamountPlusServiceFee
                + ", createdDate=" + createdDate + ", modifiedDate="
                + modifiedDate + ", modifiedBy=" + modifiedBy + ", createdBy="
                + createdBy + ", active=" + active + "]";
    }

}
