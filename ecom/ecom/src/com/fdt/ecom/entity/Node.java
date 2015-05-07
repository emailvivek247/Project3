package com.fdt.ecom.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fdt.common.entity.AbstractBaseEntity;

@Entity
@Table(name = "ECOMM_NODE")
public class Node extends AbstractBaseEntity {

    private static final long serialVersionUID = -5687594454856379712L;

    @Column(name = "NAME", unique = true, nullable = false)
    private String name = null;

    @Column(name="DESCRIPTION")
    private String description = null;

    @Transient
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "node")
    private List<Site> sites = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Site> getSites() {
        return sites;
    }

    public void setSites(List<Site> sites) {
        this.sites = sites;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Node [name=" + name + ", description=" + description
                + ", sites=" + sites + ", id=" + id + ", createdDate="
                + createdDate + ", modifiedDate=" + modifiedDate
                + ", modifiedBy=" + modifiedBy + ", createdBy=" + createdBy
                + ", active=" + active + "]";
    }
}