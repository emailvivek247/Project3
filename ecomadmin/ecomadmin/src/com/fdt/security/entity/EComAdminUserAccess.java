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
@Table(name="AUTH_ADMIN_USERS_ACCESS")
public class EComAdminUserAccess extends EcomAdminAbstractBaseEntity  {

    private static final long serialVersionUID = -7575158415019523726L;

    public EComAdminUserAccess() {
    }

    public EComAdminUserAccess(EComAdminUser user, EComAdminAccess access) {
        this.user = user;
        this.access = access;
        this.compositePrimaryKey= new CompositePrimaryKey(user.getId(),access.getId());
    }

    @Embeddable
    public static class CompositePrimaryKey implements Serializable{

        private static final long serialVersionUID = 1L;

        @Column(name="USER_ID", nullable = false)
        private Long userId;

        @Column(name="ACCESS_ID", nullable = false)
        private Long accessId;

        public CompositePrimaryKey(){
        }

        public CompositePrimaryKey(Long userId, Long accessId){
            this.userId=userId;
            this.accessId=accessId;
        }
    }


    private CompositePrimaryKey compositePrimaryKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", insertable = false, updatable = false, nullable = false)
    private EComAdminUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCESS_ID", insertable = false, updatable = false, nullable = false)
    private EComAdminAccess access;

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

    public EComAdminAccess getAccess() {
        return access;
    }

    public void setAccess(EComAdminAccess access) {
        this.access = access;
    }
}
