package com.fdt.security.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.fdt.common.entity.AbstractBaseEntity;
import com.fdt.ecom.entity.NonRecurringFee;
import com.fdt.ecom.entity.Site;
import com.fdt.recurtx.entity.UserAccount;
import com.fdt.security.entity.enums.AccessType;
import com.fdt.subscriptions.entity.SubscriptionFee;

@Entity
@Table(name="AUTH_ACCESS")
public class Access extends AbstractBaseEntity {

    private static final long serialVersionUID = 8782257293924247950L;

    @Column(name="ACCESS_CD", nullable = false)
    private String code;

    @Column(name="ACCESS_DESCR", nullable = false)
    private String description;

    @Column(name="GUEST_FLG", nullable = false)
    @Type(type="yes_no")
    private boolean guestFlg = false;

    @Column(name="ACCESS_FEATURES", nullable = false)
    private String accessFeatures;

    @Column(name="DEFAULT_ACCESS_FLG", nullable = false)
    @Type(type="yes_no")
    private boolean defaultAccessFlg;

    @Column(name = "IS_AUTHORIZATION_REQUIRED", nullable = false)
    @Type(type="yes_no")
    private boolean isAuthorizationRequired = false;

    @Column(name = "IS_VISIBLE", nullable = false)
    @Type(type="yes_no")
    private boolean isVisible = false;

	@Column(name = "IS_FIRM_LEVEL_ACCESS", nullable = false)
    @Type(type="yes_no")
    private boolean isFirmLevelAccess = false;

	@Column(name = "MAX_USERS_ALLOWED", nullable = false)
    private Integer maxUsersAllowed = 1;

	@Column(name = "MAX_DOCUMENTS_ALLOWED", nullable = false)
    private Integer maxDocumentsAllowed = 0;

	@Column(name = "IS_GOVERNMENT_ACCESS", nullable = false)
    @Type(type="yes_no")
    private boolean isGovernmentAccess = false;

	@Transient
    private Double clientShare;

    @Transient
    private SubscriptionFee subscriptionFee = null;

    @Transient
    private Site site = null;

    @Transient
    private AccessType accessType;

    @Transient
    List<NonRecurringFee> nonReccurringFeeList = null;

    @Transient
    private String comments = null;

    @Transient
    protected boolean accessOverridden = false;

    @Transient
    protected boolean isAuthorized = true;

    @Transient
    private UserAccount userAccount = null;
    
    @Transient
    private List<UserAccess> userAccessList;

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAccessFeatures() {
        return accessFeatures;
    }

    public void setAccessFeatures(String accessFeatures) {
        this.accessFeatures = accessFeatures;
    }

    public boolean isGuestFlg() {
        return guestFlg;
    }

    public void setGuestFlg(boolean guestFlg) {
        this.guestFlg = guestFlg;
    }

    public boolean isDefaultAccessFlg() {
        return defaultAccessFlg;
    }

    public void setDefaultAccessFlg(boolean defaultAccessFlg) {
        this.defaultAccessFlg = defaultAccessFlg;
    }

    public SubscriptionFee getSubscriptionFee() {
        return subscriptionFee;
    }

    public void setSubscriptionFee(SubscriptionFee subscriptionFee) {
        this.subscriptionFee = subscriptionFee;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public List<NonRecurringFee> getNonReccurringFeeList() {
        return nonReccurringFeeList;
    }

    public void setNonReccurringFeeList(List<NonRecurringFee> nonReccurringFeeList) {
        this.nonReccurringFeeList = nonReccurringFeeList;
    }
    
    public void addNonRecurringFee(NonRecurringFee nonReccurringFee){
    	if(this.nonReccurringFeeList == null){
    		this.nonReccurringFeeList = new ArrayList<NonRecurringFee>();
    	}
    	this.nonReccurringFeeList.add(nonReccurringFee);
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean isAccessOverridden() {
        return accessOverridden;
    }

    public void setAccessOverriden(boolean accessOverridden) {
        this.accessOverridden = accessOverridden;
    }

    public boolean isAuthorizationRequired() {
        return isAuthorizationRequired;
    }

    public void setAuthorizationRequired(boolean isAuthorizationRequired) {
        this.isAuthorizationRequired = isAuthorizationRequired;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public boolean isFirmLevelAccess() {
		return isFirmLevelAccess;
	}

	public void setFirmLevelAccess(boolean firmLevelAccess) {
		this.isFirmLevelAccess = firmLevelAccess;
	}

    public Integer getMaxUsersAllowed() {
		return maxUsersAllowed;
	}

	public void setMaxUsersAllowed(Integer maxUsersAllowed) {
		this.maxUsersAllowed = maxUsersAllowed;
	}


    public Double getClientShare() {
        return clientShare;
    }

    public void setClientShare(Double clientShare) {
        this.clientShare = clientShare;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }
    

    public Integer getMaxDocumentsAllowed() {
		return maxDocumentsAllowed;
	}

	public void setMaxDocumentsAllowed(Integer maxDocumentsAllowed) {
		this.maxDocumentsAllowed = maxDocumentsAllowed;
	}
	
	

	public List<UserAccess> getUserAccessList() {
		return userAccessList;
	}

	public void setUserAccessList(List<UserAccess> userAccessList) {
		this.userAccessList = userAccessList;
	}
	
	public void addUserAccess(UserAccess userAccess){
		if(this.userAccessList == null){
			this.userAccessList = new ArrayList<UserAccess>();
		}
		this.userAccessList.add(userAccess);
	}
	
	

	public boolean isGovernmentAccess() {
		return isGovernmentAccess;
	}

	public void setGovernmentAccess(boolean isGovernmentAccess) {
		this.isGovernmentAccess = isGovernmentAccess;
	}

	@Override
    public String toString() {
        return "Access [code=" + code + ", description=" + description
                + ", guestFlg=" + guestFlg + ", accessFeatures="
                + accessFeatures + ", defaultAccessFlg=" + defaultAccessFlg
                + ", isAuthorizationRequired=" + isAuthorizationRequired
                + ", isVisible=" + isVisible
                + ", isFirmLevelAccess=" + isFirmLevelAccess
                + ", isGovernmentAccess=" + isGovernmentAccess
                + ", maxUsersAllowed=" + maxUsersAllowed
                + ", userAccessList=" + userAccessList
                + ", maxDocumentsAllowed=" + maxDocumentsAllowed
                + ", clientShare=" + clientShare
                + ", subscriptionFee=" + subscriptionFee + ", site=" + site
                + ", accessType=" + accessType + ", nonReccurringFeeList="
                + nonReccurringFeeList + ", comments=" + comments
                + ", accessOverridden=" + accessOverridden + ", isAuthorized="
                + isAuthorized + ", userAccount=" + userAccount + ", id=" + id
                + ", createdDate=" + createdDate + ", modifiedDate="
                + modifiedDate + ", modifiedBy=" + modifiedBy + ", createdBy="
                + createdBy + ", active=" + super.active + "]";
    }

}
