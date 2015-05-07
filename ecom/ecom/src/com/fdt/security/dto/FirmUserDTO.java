package com.fdt.security.dto;

import java.io.Serializable;

public class FirmUserDTO implements Serializable{

	private static final long serialVersionUID = 4142982784178499941L;

	private Long userId;
	
	private Long Id;
	
	private String firstName;
	
	private String lastName;
	
	private String username;
	
	private String phone;
	
	private String firmName;
	
	private String firmNumber;
	
	private String barNumber;
	
	private boolean isFirmAccessAdmin;
	
	private Long accessId;
	
	private String accessName;
	
	private boolean isFirmLevelAccess = false;
	
	// This flag indicates if user has purchased any documents for this access
    private boolean paidTransactions;
    
    // User Access List
    private Long userAccessId;
    
    private boolean userAccessActive;
    
    private boolean userLocked;
    
    private String nodeName;

    public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		Id = id;
	}

	public boolean isPaidTransactions() {
		return paidTransactions;
	}

	public void setPaidTransactions(boolean paidTransactions) {
		this.paidTransactions = paidTransactions;
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}


	public Long getUserAccessId() {
		return userAccessId;
	}

	public void setUserAccessId(Long userAccessId) {
		this.userAccessId = userAccessId;
	}

	public boolean isUserAccessActive() {
		return userAccessActive;
	}

	public void setUserAccessActive(boolean userAccessActive) {
		this.userAccessActive = userAccessActive;
	}
	
	

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
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

	public boolean getIsFirmAccessAdmin() {
		return isFirmAccessAdmin;
	}

	public void setIsFirmAccessAdmin(boolean isFirmAccessAdmin) {
		this.isFirmAccessAdmin = isFirmAccessAdmin;
	}

	public Long getAccessId() {
		return accessId;
	}

	public void setAccessId(Long accessId) {
		this.accessId = accessId;
	}

	public String getAccessName() {
		return accessName;
	}

	public void setAccessName(String accessName) {
		this.accessName = accessName;
	}

	public boolean isUserLocked() {
		return userLocked;
	}

	public void setUserLocked(boolean userLocked) {
		this.userLocked = userLocked;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public boolean isFirmLevelAccess() {
		return isFirmLevelAccess;
	}

	public void setFirmLevelAccess(boolean isFirmLevelAccess) {
		this.isFirmLevelAccess = isFirmLevelAccess;
	}

	@Override
    public String toString() {
        return "UserAccessDetailDTO ["
        		+ "userId=" + userId
        		+ "firstName=" + firstName
        		+ ", lastName=" + lastName
                + ", lastName=" + lastName
                + ", phone=" + phone
                + ", userAccessId=" + userAccessId
                + ", userAccessActive=" + userAccessActive
                + ", accessId=" + accessId
                + ", accessName=" + accessName
                + ", userLocked=" + userLocked
                + ", nodeName=" + nodeName
                + ", isFirmLevelAccess=" + isFirmLevelAccess
                + ", paidTransactions=" + paidTransactions
                + "]";
	}
}