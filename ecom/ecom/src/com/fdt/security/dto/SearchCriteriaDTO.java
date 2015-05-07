package com.fdt.security.dto;

import java.io.Serializable;


public class SearchCriteriaDTO implements Serializable {
	
	private static final long serialVersionUID = 52407815833072420L;
	
	public static String WAITING_FOR_AUTHORIZATION = "WAITINGFORAUTHORIZATION";
	public static String USERACCESS_ACTIVE = "USERACCESS_ACTIVE";
	public static String HAS_OVERRIDEN_ACCESS = "HASOVERRIDENACCESS";
	public static String CURRENT_LOGIN_TIME = "CURRENT_LOGIN_TIME";
	public static String DATE_TIME_CREATED = "DATE_TIME_CREATED";
	public static String PAYED_USER = "ISPAIDUSER";
	public static String USER_ACTIVE = "ACTIVE";
	public static String FIRM_NAME = "FIRM_NAME";
	public static String FIRM_NUMBER = "FIRM_NUMBER";
	public static String ACCOUNT_NON_LOCKED = "ACCOUNT_NONLOCKED";
	public static String FIRM_ADMIN = "T_IS_FIRM_ACCESS_ADMIN";
	

	private String userName;
	
	private String firstName;
	
	private String lastName;
	
	private Long siteId;
	
	private Long accessId;
	
	private String active;
	
	private String paiduser;
	
	private String userSubscriptionStatus;
	
	private String firmName;
	
	private String firmNumber;
	
	private String barNumber;
	
	private String sortField;
	
	private String sortType;
	
	private String accountNonLocked;
	
	private String firmAdmin;
	
	
	public String getFirmAdmin() {
		return firmAdmin;
	}

	public void setFirmAdmin(String firmAdmin) {
		this.firmAdmin = firmAdmin;
	}

	/**
	 * Pagination Variables
	 * The next two variables are for pagination.
	 * startFrom indicates which row number to start from in the search process
	 * numberOfRecords indicates how many rows to be returned (starting from startFrom)
	 * 
	 * For example : If there are 50 records , user has page of 10 records each and user is on page 2.
	 * Now if user clicks next --> 
	 * 			startFrom will be 20 and numberOfRows returned will be 10 
	 */
    private Integer startingFrom = 0;

    // In case use doesn't supply this value , all the records will be returned.
    private Integer numberOfRecords = Integer.MAX_VALUE;

    public String getAccountNonLocked() {
		return accountNonLocked;
	}

	public void setAccountNonLocked(String accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Long getSiteId() {
		return siteId;
	}

	public void setSiteId(Long siteId) {
		this.siteId = siteId;
	}

	public Long getAccessId() {
		return accessId;
	}

	public void setAccessId(Long accessId) {
		this.accessId = accessId;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public String getPaiduser() {
		return paiduser;
	}

	public void setPaiduser(String paidUser) {
		this.paiduser = paidUser;
	}

	public String getUserSubscriptionStatus() {
		return userSubscriptionStatus;
	}

	public void setUserSubscriptionStatus(String userSubscriptionStatus) {
		this.userSubscriptionStatus = userSubscriptionStatus;
	}

	public String getFirmName() {
		return firmName;
	}

	public void setFirmName(String firmName) {
		this.firmName = firmName;
	}

	public String getFirmNumber() {
		return firmNumber;
	}

	public void setFirmNumber(String firmNumber) {
		this.firmNumber = firmNumber;
	}

	public String getBarNumber() {
		return barNumber;
	}

	public void setBarNumber(String barNumber) {
		this.barNumber = barNumber;
	}

	public String getSortField() {
		return sortField;
	}

	public void setSortField(String sortField) {
		this.sortField = sortField;
	}

	public String getSortType() {
		return sortType;
	}

	public void setSortType(String sortType) {
		this.sortType = sortType;
	}

	public Integer getStartingFrom() {
		return startingFrom;
	}

	public void setStartingFrom(Integer startingFrom) {
		this.startingFrom = startingFrom;
	}

	public Integer getNumberOfRecords() {
		return numberOfRecords;
	}

	public void setNumberOfRecords(Integer numberOfRecords) {
		this.numberOfRecords = numberOfRecords;
	}
	
	@Override
    public String toString() {
        return "SearchCriteriaDTO ["
        		+ "userName=" + userName 
        		+ ", firstName=" + firstName
        		+ ", lastName=" + lastName
        		+ ", siteId=" + siteId
        		+ ", accessId=" + accessId
        		+ ", active=" + active
        		+ ", paiduser=" + paiduser
        		+ ", userSubscriptionStatus=" + userSubscriptionStatus
        		+ ", firmName=" + firmName
        		+ ", firmNumber=" + firmNumber
        		+ ", barNumber=" + barNumber
        		+ ", sortField=" + sortField
        		+ ", sortType=" + sortType
                + "]";
    }

	
}