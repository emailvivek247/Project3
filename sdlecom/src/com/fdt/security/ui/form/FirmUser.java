package com.fdt.security.ui.form;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.Email;

public class FirmUser  {

	//@NotNull(message="{security.notnull.firstname}")
	@Size(min = 1, max = 50, message="{security.size.firstName}")
	private String firstName;

	//@NotNull(message="{security.notnull.lastname}")
	@Size(min = 1, max = 50, message="{security.size.lastName}")
	private String lastName;


	//@NotNull(message="{security.notnull.phoneNumber}")
	@Pattern(regexp="^\\(\\d{3}\\) ?\\d{3}( |-)?\\d{4}|^\\d{3}( |-)?\\d{3}( |-)?\\d{4}", message="{security.invalid.phoneNumberFormat}")
	private String phone;
	
	@NotNull(message = "{security.notnull.username}")
	@Size(min = 7, max = 50, message = "{security.size.username}")
	@Email(message="{security.invalidEmail.username}")
	private String username;
	
	private Long userAccessId;
	
	private Long accessId;
	
	// This flag indicates if user has purchased any documents for this access
    private boolean purchasedDocuments;
    
    // Subscription Status Locked or Unlocked
    private String subscriptionStatus;
    
    //Subscription action , it's either Enable or Diable 
    private String subscriptionAction;
    
    // The flog to indicate if user access is removable
    private boolean isRemovable = false;
    
    
    private String firmName;
    
    private String firmNumber;
    
    private String barNumber;
    
    private boolean userLocked;
    
    private String nodeName;
    
	
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}


	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = !StringUtils.isBlank(firstName) ? firstName.toUpperCase() : null;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = !StringUtils.isBlank(lastName) ? lastName.toUpperCase() : null;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	
	public boolean isPurchasedDocuments() {
		return purchasedDocuments;
	}

	public void setPurchasedDocuments(boolean purchasedDocuments) {
		this.purchasedDocuments = purchasedDocuments;
	}

	public String getSubscriptionStatus() {
		return subscriptionStatus;
	}

	public void setSubscriptionStatus(String subscriptionStatus) {
		this.subscriptionStatus = subscriptionStatus;
	}


	public Long getUserAccessId() {
		return userAccessId;
	}

	public void setUserAccessId(Long accessId) {
		this.userAccessId = accessId;
	}

	public String getSubscriptionAction() {
		return subscriptionAction;
	}

	public void setSubscriptionAction(String subscriptionAction) {
		this.subscriptionAction = subscriptionAction;
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


	public boolean isUserLocked() {
		return userLocked;
	}

	public void setUserLocked(boolean userLocked) {
		this.userLocked = userLocked;
	}

	public Long getAccessId() {
		return accessId;
	}

	public void setAccessId(Long accessId) {
		this.accessId = accessId;
	}

	public boolean isRemovable() {
		return isRemovable;
	}

	public void setRemovable(boolean isRemovable) {
		this.isRemovable = isRemovable;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	
}
