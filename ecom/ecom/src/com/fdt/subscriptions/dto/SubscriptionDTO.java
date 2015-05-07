package com.fdt.subscriptions.dto;

import java.util.Date;
import java.util.List;

import com.fdt.common.dto.AbstractBaseDTO;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.security.entity.User;
import com.fdt.security.entity.enums.AccessType;

public class SubscriptionDTO extends AbstractBaseDTO {

    private static final long serialVersionUID = 740211156830460968L;

    private static final String GOVERNMENT_ACCESS = "Government Access";
    private static final String RECURRING = "Recurring";
    private static final String FIRM_USER_RECURRING_DOC_BASED = "Firm Level Document Based Recurring";
    private static final String FIRM_USER_RECURRING_USER_BASED = "Firm Level User Based Recurring";
    private static final String PAY_PER_VIEW = "Pay As You Go";
    private static final String CERTIFIED_ACCESS = "Certified Subscription";
    private static final String FREE = "Free";


    /** This is used To Store Site Name **/
    private Long siteId = null;

    /** This is used To Store Site Name **/
    private String siteName = null;

    /** This is used to Store Subscription**/
    private String subscription = null;

    /** This is used to store payment due**/
    private Double subscriptionFee = 0.0d;

    /** This is used to store the last billing date**/
    private Date lastBillingDate = null;

    /** This is used to store the next billing date**/
    private Date nextBillingDate = null;

    /** This is used To Store Payment Pending **/
    private boolean isPayMentPending = false;

    /** This is used To Store User Access Id **/
    private Long userAccessId = null;

    /** This is used To Store User Account Id **/
    private Long userAccountId = null;

    /** This is used To Store User Account Id **/
    private Long accessId = null;

    /** This is used To Store Subscription period **/
    private String period = null;

    /** This is used To Store the subscription deletion flag **/
    private boolean isMarkedForCancellation = false;

    /** This is used To Store the subscription term **/
    private Long term = null;

    /** This is used To Store the subscription category **/
    private String category = null;

    /** This is used To Store the Comments why a Access is Overridden **/
    private String comments = null;

    private User user = null;

    private boolean isUserAccountActive = false;

    private List<Long> newAccessIds = null;

    private String nodeName = null;

    /** This is used To Store the Access Overridden flag **/
    private boolean accessOverridden = false;

    /** This is used To Store the Authorization Required flag **/
    private boolean isAuthorizationRequired = false;

    /** This is used To Store the Access Authorized flag **/
    private boolean isAuthorized = false;

    /** This is used To Store the Access Authorized flag **/
    private Date accountModifiedDate = null;

    /** This is used To Store the Visible flag **/
    private boolean isVisible = false;

    /** This is used To Store the Access Authorization Timestamp **/
    private Date authorizationDate = null;

    /** This is used To Store the Access Authorization User **/
    private String authorizedBy = null;

    private CreditCard creditCard = null;

    private String machineName = null;

    // Flag for Firm Level Admin User
    protected boolean isFirmAccessAdmin = false;

    // Flag for Subscription indicating if it's a Firm Level subscription
    private boolean isFirmLevelAccess = false;

	//Max number of users allowed per subscription. It applies to Firm Level users
    private int maxUsersAllowed = 1;

	//Max number of documents allowed per subscription. It applies to Firm Level users
    private int maxDocumentsAllowed = 1;

    // Firm Admin User Access Id (for firm level users)
    private Long firmAdminUserAccessId = null;

    // Flag for Subscription indicating if it's a government subscription
    private boolean isGovernmentAccess = false;


    public Date getAuthorizationDate() {
        return authorizationDate;
    }

    public void setAuthorizationDate(Date authorizationDate) {
        this.authorizationDate = authorizationDate;
    }

    public String getAuthorizedBy() {
        return authorizedBy;
    }

    public void setAuthorizedBy(String authorizedBy) {
        this.authorizedBy = authorizedBy;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public Date getAccountModifiedDate() {
        return accountModifiedDate;
    }

    public void setAccountModifiedDate(Date accountModifiedDate) {
        this.accountModifiedDate = accountModifiedDate;
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

    public void setAccessOverridden(boolean accessOverridden) {
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

    public void setAuthorized(boolean isAccessAuthorized) {
        this.isAuthorized = isAccessAuthorized;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public Double getSubscriptionFee() {
        return subscriptionFee;
    }

    public void setSubscriptionFee(Double subscriptionFee) {
        this.subscriptionFee = subscriptionFee;
    }

    public Date getLastBillingDate() {
        return lastBillingDate;
    }

    public void setLastBillingDate(Date lastBillingDate) {
        this.lastBillingDate = lastBillingDate;
    }

    public Date getNextBillingDate() {
        return nextBillingDate;
    }

    public void setNextBillingDate(Date nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    public boolean isPayMentPending() {
        return isPayMentPending;
    }

    public void setPayMentPending(boolean isPayMentPending) {
        this.isPayMentPending = isPayMentPending;
    }

    public Long getUserAccessId() {
        return userAccessId;
    }

    public void setUserAccessId(Long userAccessId) {
        this.userAccessId = userAccessId;
    }

    public Long getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(Long userAccountId) {
        this.userAccountId = userAccountId;
    }

    public Long getAccessId() {
        return accessId;
    }

    public void setAccessId(Long accessId) {
        this.accessId = accessId;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public boolean getIsMarkedForCancellation() {
        return isMarkedForCancellation;
    }

    public void setIsMarkedForCancellation(boolean isMarkedForDeletion) {
        this.isMarkedForCancellation = isMarkedForDeletion;
    }

    public Long getTerm() {
        return term;
    }

    public void setTerm(Long term) {
        this.term = term;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Long> getNewAccessIds() {
        return newAccessIds;
    }

    public void setNewAccessIds(List<Long> newAccessIds) {
        this.newAccessIds = newAccessIds;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public boolean isUserAccountActive() {
        return isUserAccountActive;
    }

    public void setUserAccountActive(boolean isUserAccountActive) {
        this.isUserAccountActive = isUserAccountActive;
    }


    public boolean isFirmAccessAdmin() {
		return isFirmAccessAdmin;
	}

	public void setFirmAccessAdmin(boolean isFirmAccessAdmin) {
		this.isFirmAccessAdmin = isFirmAccessAdmin;
	}

	public boolean isFirmLevelAccess() {
		return isFirmLevelAccess;
	}

	public void setFirmLevelAccess(boolean isFirmLevelAccess) {
		this.isFirmLevelAccess = isFirmLevelAccess;
	}

	public int getMaxUsersAllowed() {
		return maxUsersAllowed;
	}

	public void setMaxUsersAllowed(int maxUsersAllowed) {
		this.maxUsersAllowed = maxUsersAllowed;
	}



	public Long getFirmAdminUserAccessId() {
		return firmAdminUserAccessId;
	}

	public void setFirmAdminUserAccessId(Long firmAdminUserAccessId) {
		this.firmAdminUserAccessId = firmAdminUserAccessId;
	}

	public boolean isFirmLevelUserSubscription(){
		if(!this.isFirmLevelAccess()){
			return false;
		} else if (!this.isFirmAccessAdmin() && this.getFirmAdminUserAccessId() != null){
			return true;
		} else {
			// It's an admin user subscription
			return false;
		}
	}


	public boolean isGovernmentAccess() {
		return isGovernmentAccess;
	}

	public void setGovernmentAccess(boolean isGovernmentAccess) {
		this.isGovernmentAccess = isGovernmentAccess;
	}

	public int getMaxDocumentsAllowed() {
		return maxDocumentsAllowed;
	}

	public void setMaxDocumentsAllowed(int maxDocumentsAllowed) {
		this.maxDocumentsAllowed = maxDocumentsAllowed;
	}

	public String getSubscriptionType(){
		if (this.category != null && !this.category.equals("")  && this.category.equals(AccessType.CERTIFIED_NON_RECURRING_SUBSCRIPTION.toString())){
			return CERTIFIED_ACCESS;
		} else if(this.isGovernmentAccess()){
			return GOVERNMENT_ACCESS;
		} else if (this.isFirmLevelAccess()){
			if(this.maxDocumentsAllowed > 0){
				return FIRM_USER_RECURRING_DOC_BASED;
			} else if(this.maxUsersAllowed > 1){
				return FIRM_USER_RECURRING_USER_BASED;
			}
		} else if (this.category != null && !this.category.equals("") ){
			if(this.category.equals(AccessType.RECURRING_SUBSCRIPTION.toString())){
				return RECURRING;
			} else if(this.category.equals(AccessType.FREE_SUBSCRIPTION.toString())){
				return FREE;
			} else {
				return PAY_PER_VIEW;
			}
		}
		return "";
	}

	@Override
    public String toString() {
        return "SubscriptionDTO [siteId=" + siteId + ", siteName=" + siteName
                + ", subscription=" + subscription + ", subscriptionFee="
                + subscriptionFee + ", lastBillingDate=" + lastBillingDate
                + ", nextBillingDate=" + nextBillingDate
                + ", isPayMentPending=" + isPayMentPending + ", userAccessId="
                + userAccessId + ", userAccountId=" + userAccountId
                + ", accessId=" + accessId + ", period=" + period
                + ", isMarkedForCancellation=" + isMarkedForCancellation
                + ", term=" + term + ", category=" + category
                + ", comments=" + comments + ", user=" + user
                + ", newAccessIds=" + newAccessIds + ", nodeName=" + nodeName
                + ", accessOverridden=" + accessOverridden
                + ", isAuthorizationRequired=" + isAuthorizationRequired
                + ", isAuthorized=" + isAuthorized + ", accountModifiedDate="
                + accountModifiedDate + ", isVisible=" + isVisible
                + ", isFirmAccessAdmin=" + isFirmAccessAdmin
                + ", isFirmLevelAccess=" + isFirmLevelAccess
                + ", isGovernmentAccess=" + isGovernmentAccess
                + ", maxUsersAllowed=" + maxUsersAllowed
                + ", maxDocumentsAllowed=" + maxDocumentsAllowed
                + ", firmAdminUserAccessId=" + firmAdminUserAccessId
                + "]";
    }


}