package com.fdt.security.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fdt.common.entity.EcomAdminAbstractBaseEntity;

@Entity
@Table(name="AUTH_ADMIN_USERS_SITES")
public class EComAdminUserSite extends EcomAdminAbstractBaseEntity  {

    private static final long serialVersionUID = 3238731464744491622L;

    public EComAdminUserSite() {
    }

    public EComAdminUserSite(EComAdminUser user, EComAdminSite site) {
        this.user = user;
        this.site = site;
        this.compositePrimaryKey= new CompositePrimaryKey(user.getId(), site.getId());
    }

    @Embeddable
    public static class CompositePrimaryKey implements Serializable{

        private static final long serialVersionUID = -1756289899060810206L;

        @Column(name="USER_ID", nullable = false)
        private Long userId;

        @Column(name="SITE_ID", nullable = false)
        private Long siteId;

        public CompositePrimaryKey(){
        }

        public CompositePrimaryKey(Long userId, Long siteId){
            this.userId=userId;
            this.siteId=siteId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getSiteId() {
            return siteId;
        }

        public void setSiteId(Long siteId) {
            this.siteId = siteId;
        }
    }

    private CompositePrimaryKey compositePrimaryKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", insertable = false, updatable = false, nullable = false)
    private EComAdminUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SITE_ID", insertable = false, updatable = false, nullable = false)
    private EComAdminSite site;

    public CompositePrimaryKey getCompositePrimaryKey() {
        return compositePrimaryKey;
    }

    public void setCompositePrimaryKey(CompositePrimaryKey compositePrimaryKey) {
        this.compositePrimaryKey = compositePrimaryKey;
    }

    public EComAdminUser getUser() {
        return user;
    }

    public void setUser(EComAdminUser user) {
        this.user = user;
    }

    public EComAdminSite getSite() {
        return site;
    }

    public void setSite(EComAdminSite site) {
        this.site = site;
    }
}
