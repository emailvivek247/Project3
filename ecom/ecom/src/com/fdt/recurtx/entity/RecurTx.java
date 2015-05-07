package com.fdt.recurtx.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fdt.common.entity.Tx;
import com.fdt.security.entity.Access;

@Entity
@Table(name = "ECOMM_RECUR_TX_HIST_INFO")
@JsonIgnoreProperties(ignoreUnknown=true)
public class RecurTx extends Tx {

	private static final long serialVersionUID = -2599691092889511496L;

	@Column(name = "USER_ID", nullable = false)
    private Long userId = null;

    @Column(name = "ACCESS_ID", nullable = false)
    private Long accessId = null;

    @Column(name = "IS_PREVIOUS_ACCESS")
    @Type(type="yes_no")
    private boolean isPreviousAccess = false;

    @Transient
	private Access access = null;

    @Transient
    private boolean isCardExpired = false;

    @Column(name="CLIENT_SHARE")
    private Double clientShare;

    @Transient
    private Date nextBillingDate = null;

    @Transient
    private Date lastBillingDate = null;

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

	public boolean isPreviousAccess() {
		return isPreviousAccess;
	}

	public void setPreviousAccess(boolean isPreviousAccess) {
		this.isPreviousAccess = isPreviousAccess;
	}

	public Access getAccess() {
		return access;
	}

	public void setAccess(Access access) {
		this.access = access;
	}

	public boolean isCardExpired() {
		return isCardExpired;
	}

	public void setCardExpired(boolean isCardExpired) {
		this.isCardExpired = isCardExpired;
	}

	public Double getClientShare() {
		return clientShare;
	}

	public void setClientShare(Double clientShare) {
		this.clientShare = clientShare;
	}

	public Date getNextBillingDate() {
		return nextBillingDate;
	}

	public void setNextBillingDate(Date nextBillingDate) {
		this.nextBillingDate = nextBillingDate;
	}

	public Date getLastBillingDate() {
		return lastBillingDate;
	}

	public void setLastBillingDate(Date lastBillingDate) {
		this.lastBillingDate = lastBillingDate;
	}
	
	
	@JsonProperty("Subscription")
	public String getSubscription(){
		if(this.getAccess() != null){
			return this.getAccess().getDescription();
		}
		return "";
	}

	@JsonProperty("SiteName")
	public String getSiteName(){
		if(super.getSite() != null){
			return super.getSite().getDescription();
		}
		return "";
	}

	@Override
	public String toString() {
		return "RecurTx [userId=" + userId + ", accessId=" + accessId
				+ ", isPreviousAccess=" + isPreviousAccess + ", access="
				+ access + ", isCardExpired=" + isCardExpired
				+ ", clientShare=" + clientShare + ", nextBillingDate="
				+ nextBillingDate + ", lastBillingDate=" + lastBillingDate
				+ ", id=" + id + ", createdDate=" + createdDate
				+ ", modifiedDate=" + modifiedDate + ", modifiedBy="
				+ modifiedBy + ", createdBy=" + createdBy + ", active="
				+ active + "]";
	}

}
