package com.fdt.recurtx.dto;

import java.util.Date;

public class ExpiredOverriddenSubscriptionDTO {

	private Long  userAccessId;
	
	private Long  accessId;
	
	private Long  userId;
	
	private Date  overriddenUntillDate;
	
	private String emailId;
	
	private String firstName;
	
	private String lastName;
	
	private String accessDescription;
	
	private String expiredOverriddenSubscriptionNotificationSubject;
	
	private String expiredOverriddenSubscriptionNotificationTemplate;
	
	private String fromEmailAddress;
	
	private String emailTemplateFolder;
	
	public String getFromEmailAddress() {
		return fromEmailAddress;
	}

	public void setFromEmailAddress(String fromEmailAddress) {
		this.fromEmailAddress = fromEmailAddress;
	}

	public String getEmailTemplateFolder() {
		return emailTemplateFolder;
	}

	public void setEmailTemplateFolder(String emailTemplateFolder) {
		this.emailTemplateFolder = emailTemplateFolder;
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

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Date getOverriddenUntillDate() {
		return overriddenUntillDate;
	}

	public void setOverriddenUntillDate(Date overriddenUntillDate) {
		this.overriddenUntillDate = overriddenUntillDate;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
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

	public String getAccessDescription() {
		return accessDescription;
	}

	public void setAccessDescription(String accessDescription) {
		this.accessDescription = accessDescription;
	}

	public String getExpiredOverriddenSubscriptionNotificationSubject() {
		return expiredOverriddenSubscriptionNotificationSubject;
	}

	public void setExpiredOverriddenSubscriptionNotificationSubject(
			String expiredOverriddenSubscriptionNotificationSubject) {
		this.expiredOverriddenSubscriptionNotificationSubject = expiredOverriddenSubscriptionNotificationSubject;
	}

	public String getExpiredOverriddenSubscriptionNotificationTemplate() {
		return expiredOverriddenSubscriptionNotificationTemplate;
	}

	public void setExpiredOverriddenSubscriptionNotificationTemplate(
			String expiredOverriddenSubscriptionNotificationTemplate) {
		this.expiredOverriddenSubscriptionNotificationTemplate = expiredOverriddenSubscriptionNotificationTemplate;
	}
	
	
}
