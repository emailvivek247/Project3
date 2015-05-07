package com.fdt.ecom.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fdt.common.entity.AbstractBaseEntity;

@Entity
@Table(name = "CODELOOKUP")
public class Code extends AbstractBaseEntity {

    private static final long serialVersionUID = 4667611951715376116L;

    @Column(name="CODE", nullable = false)
    private String code = null;

    @Column(name="DESCRIPTION", nullable = false)
    private String description = null;

    @Column(name="CATEGORY")
    private String category = null;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Code [code=" + code + ", description=" + description
                + ", category=" + category + ", id=" + id + ", createdDate="
                + createdDate + ", modifiedDate=" + modifiedDate
                + ", modifiedBy=" + modifiedBy + ", createdBy=" + createdBy
                + ", active=" + active + "]";
    }
}
