package com.fdt.ecom.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fdt.common.entity.AbstractBaseEntity;


@Entity
@Table(name = "AUTH_TERM_TYP")
public class TermType extends AbstractBaseEntity {

    private static final long serialVersionUID = -7266968348273716809L;

    @Column(name="TERM_TYP_CD", nullable = false)
    private String termTypeCode = null;

    @Column(name="TERM_TYP_DESC", nullable = false)
    private String termTypeDesc = null;

    public String getTermTypeCode() {
        return termTypeCode;
    }

    public void setTermTypeCode(String termTypeCode) {
        this.termTypeCode = termTypeCode;
    }

    public String getTermTypeDesc() {
        return termTypeDesc;
    }

    public void setTermTypeDesc(String termTypeDesc) {
        this.termTypeDesc = termTypeDesc;
    }

    @Override
    public String toString() {
        return "TermType [termTypeCode=" + termTypeCode + ", termTypeDesc="
                + termTypeDesc + ", id=" + id + ", createdDate=" + createdDate
                + ", modifiedDate=" + modifiedDate + ", modifiedBy="
                + modifiedBy + ", createdBy=" + createdBy + ", active="
                + active + "]";
    }
}
