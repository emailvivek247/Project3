package com.fdt.security.ui.form;

import java.util.Date;

import com.fdt.security.entity.User;

public class LoggedinUserForm {
	
	private User user = null;
	
	private String sessionId = null;
	
	private Date lastRequestTime = null;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Date getLastRequestTime() {
		return lastRequestTime;
	}

	public void setLastRequestTime(Date lastRequestTime) {
		this.lastRequestTime = lastRequestTime;
	}
}