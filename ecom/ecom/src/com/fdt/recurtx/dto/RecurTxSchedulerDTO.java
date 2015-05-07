package com.fdt.recurtx.dto;

import java.util.Date;

import com.fdt.common.dto.AbstractBaseDTO;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.Site;

public class RecurTxSchedulerDTO extends AbstractBaseDTO {

    private static final long serialVersionUID = 8338579895290753896L;

    /** This is to disable the Access**/
    private Long userAccessId = null;

    /** This is to used to store Access Id**/
    private Long accessId = null;

    /** This is to used to store Access Id**/
    private String userFirstName = null;

    /** This is to used to store Access Id**/
    private String userLastName = null;

    /** This is to used to store Access Description**/
    private String accessDescription = null;

    /** This is to get the user Id**/
    private Long userId = null;

    private String userName = null;

    /** This is to get the User Account Id**/
    private Long userAccountId = null;

    /** This is used to Store the Site Information **/
    private Site site =  null;

    private Double clientShare;

    private Date lastBillingDate = new Date();

    private Date nextBillingDate = new Date();

    private String accountNumber = null;

    private CreditCard creditCard = null;

    private Double amtToCarge = null;

    private String paymentPeriod = null;
    
    // Flag for Firm Level Admin User
    protected boolean isFirmAccessAdmin = false;

    // Flag for Subscription indicating if it's a Firm Level subscription
    private boolean isFirmLevelAccess = false;
    
    // admin user access id if this is a firm level access and user is not administrator
    private Long firmAdminUserAccessId;


    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Double getClientShare() {
        return clientShare;
    }

    public void setClientShare(Double clientShare) {
        this.clientShare = clientShare;
    }

    public Long getUserAccessId() {
        return userAccessId;
    }

    public void setUserAccessId(Long userAccessId) {
        this.userAccessId = userAccessId;
    }

    public Long getAccessId() {
        return accessId;
    }

    public void setAccessId(Long accessId) {
        this.accessId = accessId;
    }

    public String getAccessDescription() {
        return accessDescription;
    }

    public void setAccessDescription(String accessDescription) {
        this.accessDescription = accessDescription;
    }

    public Long getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(Long userAccountId) {
        this.userAccountId = userAccountId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
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

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }


    public Double getAmtToCarge() {
        return amtToCarge;
    }

    public void setAmtToCarge(Double amtToCarge) {
        this.amtToCarge = amtToCarge;
    }

    public String getPaymentPeriod() {
        return paymentPeriod;
    }

    public void setPaymentPeriod(String paymentPeriod) {
        this.paymentPeriod = paymentPeriod;
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

	public Long getFirmAdminUserAccessId() {
		return firmAdminUserAccessId;
	}

	public void setFirmAdminUserAccessId(Long firmAdminUserAccessId) {
		this.firmAdminUserAccessId = firmAdminUserAccessId;
	}

	@Override
    public String toString() {
        return "RecurTxSchedulerDTO [userAccessId=" + userAccessId
                + ", accessId=" + accessId + ", userFirstName=" + userFirstName
                + ", userLastName=" + userLastName + ", accessDescription="
                + accessDescription + ", userId=" + userId + ", userName="
                + userName + ", userAccountId=" + userAccountId + ", site="
                + site + ", clientShare=" + clientShare + ", lastBillingDate="
                + lastBillingDate + ", nextBillingDate=" + nextBillingDate
                + ", accountNumber=" + accountNumber + ", creditCard="
                + creditCard + ", amtToCarge=" + amtToCarge
                + ", paymentPeriod=" + paymentPeriod + ", createdDate="
                + createdDate + ", modifiedDate=" + modifiedDate
                + ", modifiedBy=" + modifiedBy + ", active=" + active
                + ", isFirmAccessAdmin=" + isFirmAccessAdmin
                + ", isFirmLevelAccess=" + isFirmLevelAccess
                + ", firmAdminUserAccessId=" + firmAdminUserAccessId
                + ", createdBy=" + createdBy + "]";
    }
}