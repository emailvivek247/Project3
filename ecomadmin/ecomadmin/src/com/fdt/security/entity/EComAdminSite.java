package com.fdt.security.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;

import com.fdt.common.entity.EcomAdminAbstractBaseEntity;

@Entity
@Table(name = "ECOMM_SITE")
public class EComAdminSite extends EcomAdminAbstractBaseEntity {

    private static final long serialVersionUID = -5908392870767731531L;

    @Column(name="NAME", nullable = false)
    private String name = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        EComAdminSite other = (EComAdminSite) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (id.longValue() != other.id.longValue())
            return false;
        return true;
    }
}
