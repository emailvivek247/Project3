package com.fdt.common.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.SiteAccess;
import com.fdt.security.entity.UserAccess;


public class EComUtil {

    /**
     * Find out an SiteAccess from an existing list
     * 
     * @param existingSiteAccessList
     * @param accessId
     * @return
     */
    public static Site findSiteByAccessIdInList(List<Site> siteList, Long accessId){
		for(Site site : siteList){
			SiteAccess siteAccess = site.getSiteAccess().get(0);
			if(siteAccess.getAccess().getId().equals(accessId)){
				return site;
			}
		}
		return null;
    }
    
    /**
     * Iterate through Sites and see if at least one site has Auto Activate to be true.
     * If So then return true;
     * 
     * @param existingSiteAccessList
     * @return
     */
    public static boolean isAccountNonLockedForSitesTrueInList(List<Site> siteList){
    	boolean accountNonLocked = false;
		for(Site site : siteList){
			if(site.isAutoActivate()){
				accountNonLocked = true;
				break;
			}
		}
		return accountNonLocked;
    }
    
    /**
     *  Get User Access Ids as a list
     *  
     * @param userAccessList
     * @return
     */
    public static List<Long> getUserAccessIdsAsList(List<UserAccess> userAccessList){
    	List<Long> ids = new ArrayList<Long>();
    	if(!CollectionUtils.isEmpty(userAccessList)){
	    	for(UserAccess userAccess : userAccessList){
	    		ids.add(userAccess.getId());
	    	}
    	}
    	return ids;
    }
    
    /**
     * Get Access Ids as a list
     * 
     * @param userAccessList
     * @return
     */
    public static List<Long> getAccessIdsAsList(List<UserAccess> userAccessList){
    	List<Long> ids = new ArrayList<Long>();
    	if(!CollectionUtils.isEmpty(userAccessList)){
	    	for(UserAccess userAccess : userAccessList){
	    		ids.add(userAccess.getAccess().getId());
	    	}
    	}
    	return ids;
    }

    /**
     * Get Emails as  a list
     * 
     * @param userAccessList
     * @return
     */
    public static List<String> getEmailsAsList(List<UserAccess> userAccessList){
    	List<String> emails = new ArrayList<String>();
    	if(!CollectionUtils.isEmpty(userAccessList)){
	    	for(UserAccess userAccess : userAccessList){
	    		if(userAccess.getUser() != null && !StringUtils.isBlank(userAccess.getUser().getUsername())){
	    			emails.add(userAccess.getUser().getUsername());
	    		}
	    	}
    	}
    	return emails;
    }
    
}
