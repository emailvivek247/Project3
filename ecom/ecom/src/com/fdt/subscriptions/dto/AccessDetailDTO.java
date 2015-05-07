package com.fdt.subscriptions.dto;

import com.fdt.common.dto.AbstractBaseDTO;
import com.fdt.ecom.entity.Site;
import com.fdt.subscriptions.entity.SubscriptionFee;

public class AccessDetailDTO extends AbstractBaseDTO {

    private static final long serialVersionUID = -6067484076079493391L;

    private SubscriptionFee subFee = null;

    private Site site = null;

    private boolean isAuthorizationRequired = false;

    public SubscriptionFee getSubFee() {
        return subFee;
    }

    public void setSubFee(SubscriptionFee subFee) {
        this.subFee = subFee;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public boolean isAuthorizationRequired() {
        return isAuthorizationRequired;
    }

    public void setAuthorizationRequired(boolean isAuthorizationRequired) {
        this.isAuthorizationRequired = isAuthorizationRequired;
    }

    @Override
    public String toString() {
        return "AccessDetailDTO [subFee=" + subFee + ", site=" + site
                + ", isAuthorizationRequired=" + isAuthorizationRequired
                + ", createdDate=" + createdDate + ", modifiedDate="
                + modifiedDate + ", modifiedBy=" + modifiedBy + ", active="
                + active + ", createdBy=" + createdBy + "]";
    }
}