package com.fdt.security.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

import com.fdt.common.entity.AbstractBaseEntity;
import com.fdt.recurtx.entity.UserAccount;

@Entity
@Table(name="AUTH_USERS_ACCESS")
public class UserAccess extends AbstractBaseEntity  {

    private static final long serialVersionUID = 4097958292629700971L;

    public UserAccess() {
    }

    public UserAccess(User user, Access access) {
        this.user = user;
        this.access = access;
        this.userAccessCompositePrimaryKey= new UserAccessCompositePrimaryKey(user.getId(),access.getId());
    }

    @Embeddable
    public static class UserAccessCompositePrimaryKey implements Serializable {

        private static final long serialVersionUID = 1L;

        @Column(name="USER_ID", nullable = false)
        private Long userId;

        @Column(name="ACCESS_ID", nullable = false)
        private Long accessId;

        public UserAccessCompositePrimaryKey(){
        }

        public UserAccessCompositePrimaryKey(Long userId, Long accessId){
            this.userId=userId;
            this.accessId=accessId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getAccessId() {
            return accessId;
        }

        public void setAccessId(Long accessId) {
            this.accessId = accessId;
        }
    }

    private UserAccessCompositePrimaryKey userAccessCompositePrimaryKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", insertable = false, updatable = false, nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCESS_ID", insertable = false, updatable = false, nullable = false)
    private Access access;

    @OneToOne(fetch=FetchType.EAGER, mappedBy = "userAccess")
    private UserAccount userAccount;

    @Column(name = "COMMENTS")
    private String comments = null;

    @Column(name = "IS_ACCESS_OVERRIDDEN")
    @Type(type="yes_no")
    protected boolean accessOverriden = false;

    @Column(name = "IS_AUTHORIZED", nullable = false)
    @Type(type="yes_no")
    protected boolean isAuthorized = true;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "AUTHORIZED_DATETIME")
    private Date authorizationDate = null;

    @Column(name = "AUTHORIZED_BY")
    protected String authorizedBy = null;

    @Column(name = "IS_FIRM_ACCESS_ADMIN", nullable = false)
    @Type(type="yes_no")
    protected boolean isFirmAccessAdmin = false;

    @Column(name="FIRM_ADMIN_USER_ACCESS_ID")
    private Long firmAdminUserAccessId;

    @Column(name = "IS_DELETED", nullable = false)
    @Type(type="yes_no")
    protected boolean isDeleted = false;


    public Date getAutorizationDate() {
        return authorizationDate;
    }

    public void setAuthorizationDate(Date authorizationDate) {
        this.authorizationDate = authorizationDate;
    }

    public String getAuthorizedBy() {
        return authorizedBy;
    }

    public void setAuthorizedBy(String authorizedBy) {
        this.authorizedBy = authorizedBy;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
    }

    public UserAccessCompositePrimaryKey getUserAccessCompositePrimaryKey() {
        return userAccessCompositePrimaryKey;
    }

    public void setUserAccessCompositePrimaryKey(
            UserAccessCompositePrimaryKey userAccessCompositePrimaryKey) {
        this.userAccessCompositePrimaryKey = userAccessCompositePrimaryKey;
    }

	public boolean isFirmAccessAdmin() {
		return isFirmAccessAdmin;
	}

	public void setFirmAccessAdmin(boolean isFirmAccessAdmin) {
		this.isFirmAccessAdmin = isFirmAccessAdmin;
	}

	public Long getFirmAdminUserAccessId() {
		return firmAdminUserAccessId;
	}

	public void setFirmAdminUserAccessId(Long firmAdminUserAccessId) {
		this.firmAdminUserAccessId = firmAdminUserAccessId;
	}

	public boolean isAccessOverriden() {
		return accessOverriden;
	}

	public void setAccessOverriden(boolean accessOverriden) {
		this.accessOverriden = accessOverriden;
	}

	public Date getAuthorizationDate() {
		return authorizationDate;
	}
	
	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}	

	public boolean isFirmLevelUserSubscription(){
		if (!this.isFirmAccessAdmin() && this.getFirmAdminUserAccessId() != null){
			return true;
		} else {
			// It's an admin user subscription
			return false;
		}
	}
	@Override
    public String toString() {
        return "UserAccess ["  
        		+ "user=" + user
        		+ ", access=" + access
        		+ ", userAccount=" + userAccount
        		+ ", comments=" + comments
        		+ ", accessOverriden=" + accessOverriden
        		+ ", isAuthorized=" + isAuthorized
        		+ ", authorizationDate=" + authorizationDate
        		+ ", authorizedBy=" + authorizedBy
        		+ ", isFirmAccessAdmin=" + isFirmAccessAdmin
        		+ ", firmAdminUserAccessId=" + firmAdminUserAccessId
        		+ ", isDeleted=" + isDeleted
        		+ ", createdDate=" + createdDate
        		+ ", modifiedBy=" + modifiedBy
        		+ ", modifiedDate=" + modifiedDate
        		+ ", active=" + active
                + "]";
    }


}
