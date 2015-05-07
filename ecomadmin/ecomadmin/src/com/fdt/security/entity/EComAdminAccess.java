package com.fdt.security.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fdt.common.entity.EcomAdminAbstractBaseEntity;


@Entity
@Table(name="AUTH_ADMIN_ACCESS")
public class EComAdminAccess extends EcomAdminAbstractBaseEntity {

    private static final long serialVersionUID = 5609922344369664470L;

    @Column(name="ACCESS_CD", nullable = false)
    private String code;

    @Column(name="ACCESS_DESCR", nullable = false)
    private String description;

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
}
