package com.fdt.ecom.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.fdt.common.entity.AbstractBaseEntity;

@Entity
@Table(name = "AUTH_TERMS")
public class Term extends AbstractBaseEntity {

    private static final long serialVersionUID = -5118447735128504454L;

    @Column(name="DEFAULT_FLG", nullable = false)
    @Type(type="yes_no")
    private boolean isDefault = false;

    @Transient
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="SITE_ID", nullable=false, unique=true)
    private Site site = null;

    @Column(name="TERM_DESC")
    private String description = null;

    @Transient
    TermType termType = new TermType();

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public TermType getTermType() {
        return termType;
    }

    public void setTermType(TermType termType) {
        this.termType = termType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Term [isDefault=" + isDefault + ", site=" + site
                + ", description=" + description + ", termType=" + termType
                + ", id=" + id + ", createdDate=" + createdDate
                + ", modifiedDate=" + modifiedDate + ", modifiedBy="
                + modifiedBy + ", createdBy=" + createdBy + ", active="
                + active + "]";
    }
}
