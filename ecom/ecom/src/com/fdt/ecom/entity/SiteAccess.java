package com.fdt.ecom.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fdt.common.entity.AbstractBaseEntity;
import com.fdt.security.entity.Access;

@Entity
@Table(name = "ECOMM_SITE_ACCESS")
public class SiteAccess extends AbstractBaseEntity {

    private static final long serialVersionUID = 980572793205772388L;

    public SiteAccess() {
    }

    public SiteAccess(Site site, Access access) {
        this.site = site;
        this.access = access;
        this.siteAccessCompositePrimaryKey= new SiteAccessCompositePrimaryKey(site.getId(),access.getId());
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SITE_ID", insertable = false, updatable = false, nullable = false)
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCESS_ID", insertable = false, updatable = false, nullable = false)
    private Access access;

    private SiteAccessCompositePrimaryKey siteAccessCompositePrimaryKey;

    @Embeddable
    public static class SiteAccessCompositePrimaryKey implements Serializable {

        private static final long serialVersionUID = 1L;

        @Column(name="SITE_ID", nullable = false)
        private Long siteId;

        @Column(name="ACCESS_ID", nullable = false)
        private Long accessId;

        public SiteAccessCompositePrimaryKey(){
        }

        public SiteAccessCompositePrimaryKey(Long siteId, Long accessId){
            this.siteId=siteId;
            this.accessId=accessId;
        }

        public Long getSiteId() {
            return this.siteId;
        }

        public void setSiteId(Long siteId) {
            this.siteId = siteId;
        }

        public Long getAccessId() {
            return accessId;
        }

        public void setAccessId(Long accessId) {
            this.accessId = accessId;
        }
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public SiteAccessCompositePrimaryKey getSiteAccessCompositePrimaryKey() {
        return siteAccessCompositePrimaryKey;
    }

    public void setSiteAccessCompositePrimaryKey(
            SiteAccessCompositePrimaryKey siteAccessCompositePrimaryKey) {
        this.siteAccessCompositePrimaryKey = siteAccessCompositePrimaryKey;
    }

}
