package com.fdt.common.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;

@MappedSuperclass
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractBaseEntity implements Serializable {

    private static final long serialVersionUID = -7973227158909184128L;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="ID", nullable = false)
    protected Long id = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_TIME_CREATED", nullable = false)
    protected Date createdDate = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_TIME_MOD", nullable = false)
    protected Date modifiedDate = null;

    @Column(name = "MOD_USER_ID", nullable = false)
    protected String modifiedBy = null;

    @Column(name = "CREATED_BY", nullable = false)
    protected String createdBy = null;

    @Column(name = "ACTIVE", nullable = false)
    @Type(type="yes_no")
    protected boolean active = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @JsonSetter("active")
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean isActive) {
        this.active = isActive;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractBaseEntity other = (AbstractBaseEntity) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!(id.longValue() == other.id.longValue()))
            return false;
        return true;
    }
}