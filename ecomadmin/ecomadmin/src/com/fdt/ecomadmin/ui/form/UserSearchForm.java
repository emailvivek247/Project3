package com.fdt.ecomadmin.ui.form;

import org.apache.commons.lang.StringUtils;

public class UserSearchForm {

    private String firstName = null;

    private String lastName = null;

    private String userName = null;
    
	private String firmName = null;
    
    private String firmNumber = null;
    
    private String barNumber = null;
    
    private Integer siteId = null;
    
    
    private String accessId = null;
    
    private String active = null;
    
    private String paiduser = null;
    
    private String userSubscriptionStatus = null;
    
    // Start the search from first row.
    private Integer skip = 0;
    
    // Default value is maximum in case use doesn't supply it.
    private Integer take = Integer.MAX_VALUE;


    
	public Integer getSiteId() {
		return this.siteId;
	}

	public void setSiteId(Integer siteId) {
		this.siteId = siteId;
	}

	public String getFirstName() {
		return StringUtils.isBlank(this.firstName) ? null : this.firstName.trim() ;
    }

    public void setFirstName(String firstName) {
		this.firstName  = firstName;
        
    }

    public String getLastName() {
		return StringUtils.isBlank(this.lastName) ? null : this.lastName.trim() ;
    }

    public void setLastName(String lastName) {
		this.lastName  = lastName;
    }

    public String getUserName() {
		return StringUtils.isBlank(this.userName) ? null : this.userName.trim() ;
    }

    public void setUserName(String userName) {
		this.userName  = userName;
    }
    
    public String getFirmName() {
		return StringUtils.isBlank(this.firmName) ? null : this.firmName.trim() ;
	}

	public void setFirmName(String firmName) {
		this.firmName  = firmName;
	}

	public String getFirmNumber() {
		return StringUtils.isBlank(this.firmNumber) ? null : this.firmNumber.trim() ;
	}

	public void setFirmNumber(String firmNumber) {
		this.firmNumber  = firmNumber;
	}

	public String getBarNumber() {
		return StringUtils.isBlank(this.barNumber) ? null : this.barNumber.trim() ;
	}

	public void setBarNumber(String barNumber) {
		this.barNumber  = barNumber;
	}

	public String getAccessId() {
		return StringUtils.isBlank(this.accessId) ? null : this.accessId.trim() ;
	}

	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	public String getActive() {
		return StringUtils.isBlank(this.active) ? null : this.active.trim() ;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public String getPaiduser() {
		return StringUtils.isBlank(this.paiduser) ? null : this.paiduser.trim() ;
	}

	public void setPaiduser(String paidUser) {
		this.paiduser = paidUser;
	}

	public String getUserSubscriptionStatus() {
		return StringUtils.isBlank(this.userSubscriptionStatus) ? null : this.userSubscriptionStatus.trim() ;
	}

	public void setUserSubscriptionStatus(String userSubscriptionStatus) {
		this.userSubscriptionStatus = userSubscriptionStatus;
	}

	public Integer getSkip() {
		return skip;
	}

	public void setSkip(Integer skip) {
		this.skip = skip;
	}

	public Integer getTake() {
		return take;
	}

	public void setTake(Integer take) {
		this.take = take;
	}


}
