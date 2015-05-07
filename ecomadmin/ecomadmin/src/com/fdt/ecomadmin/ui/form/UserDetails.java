package com.fdt.ecomadmin.ui.form;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;

import com.fdt.ecom.entity.Site;
import com.fdt.security.entity.User;
import com.fdt.subscriptions.dto.SubscriptionDTO;



public class UserDetails {

	private User user;
	
	private Collection<SubscriptionDTO> subscriptions;
	
	private CreditCardForm creditCardForm;
	
	private String nodeName;
	
	private Collection<Site> sites;
	
	private String path;
	
	private boolean isInternalUser = false;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Collection<SubscriptionDTO> getSubscriptions() {
		return CollectionUtils.isEmpty(this.subscriptions) ? new ArrayList<SubscriptionDTO>() : this.subscriptions;
	}

	public void setSubscriptions(Collection<SubscriptionDTO> subscriptions) {
		this.subscriptions = subscriptions;
	}

	public CreditCardForm getCreditCardForm() {
		return creditCardForm;
	}

	public void setCreditCardForm(CreditCardForm creditCardForm) {
		this.creditCardForm = creditCardForm;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public Collection<Site> getSites() {
		return CollectionUtils.isEmpty(this.sites) ? new ArrayList<Site>() : this.sites;
	}

	public void setSites(Collection<Site> sites) {
		this.sites = sites;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isInternalUser() {
		return isInternalUser;
	}

	public void setInternalUser(boolean isInternalUser) {
		this.isInternalUser = isInternalUser;
	}
	
	
	
    



}