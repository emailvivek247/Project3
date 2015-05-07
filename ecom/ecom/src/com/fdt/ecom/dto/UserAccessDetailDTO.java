package com.fdt.ecom.dto;

import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.SiteConfiguration;
import com.fdt.security.entity.Access;
import com.fdt.security.entity.User;

public class UserAccessDetailDTO {

    private Site site = null;

    private User user = null;
    
    private Access access = null;

    private SiteConfiguration siteConfiguration = null;
    
    private boolean isUserAccessActive;

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public SiteConfiguration getSiteConfiguration() {
        return siteConfiguration;
    }

    public void setSiteConfiguration(SiteConfiguration siteConfiguration) {
        this.siteConfiguration = siteConfiguration;
    }
    
    

    public boolean isUserAccessActive() {
		return isUserAccessActive;
	}

	public void setUserAccessActive(boolean isUserAccessActive) {
		this.isUserAccessActive = isUserAccessActive;
	}

	@Override
    public String toString() {
        return "UserAccessDetailDTO ["
        		+ "site=" + site 
        		+ ", user=" + user
                + ", access=" + access 
                + ", siteConfiguration=" + siteConfiguration 
                + ", isUserAccessActive=" + isUserAccessActive 
                + "]";
    }
}