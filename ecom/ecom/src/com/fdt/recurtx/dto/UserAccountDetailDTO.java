package com.fdt.recurtx.dto;

import com.fdt.common.dto.AbstractBaseDTO;
import com.fdt.ecom.entity.Site;
import com.fdt.recurtx.entity.UserAccount;
import com.fdt.subscriptions.entity.SubscriptionFee;

public class UserAccountDetailDTO extends AbstractBaseDTO {

    private static final long serialVersionUID = -4132319979195568576L;

    private Long userId = null;

    private Site site = null;

    private UserAccount userAccount = null;

    private SubscriptionFee subFee = null;

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public SubscriptionFee getSubFee() {
        return subFee;
    }

    public void setSubFee(SubscriptionFee subFee) {
        this.subFee = subFee;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "UserAccountDetailDTO [userId=" + userId + ", site=" + site
                + ", userAccount=" + userAccount + ", subFee=" + subFee
                + ", createdDate=" + createdDate + ", modifiedDate="
                + modifiedDate + ", modifiedBy=" + modifiedBy + ", active="
                + active + ", createdBy=" + createdBy + "]";
    }
}