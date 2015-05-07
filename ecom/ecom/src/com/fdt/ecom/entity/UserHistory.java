package com.fdt.ecom.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fdt.common.entity.AbstractBaseEntity;

@Entity
@Table(name = "ECOMM_USERS_HISTORY")
public class UserHistory extends AbstractBaseEntity {

    private static final long serialVersionUID = 7748006624563424502L;

    @Column(name="EMAIL_ID")
    private String userName = null;

    @Column(name="ACCESS_ID", nullable = false)
    private Long accesId = null;

    @Column(name="COMMENTS")
    private String comments = null;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getAccesId() {
        return accesId;
    }

    public void setAccesId(Long accesId) {
        this.accesId = accesId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "UserHistory [userName=" + userName + ", accesId=" + accesId
                + ", comments=" + comments + ", id=" + id + ", createdDate=" + createdDate
                + ", modifiedDate=" + modifiedDate + ", modifiedBy="
                + modifiedBy + ", createdBy=" + createdBy + ", active="
                + active + "]";
    }
}
