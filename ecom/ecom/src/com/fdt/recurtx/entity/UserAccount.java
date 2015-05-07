package com.fdt.recurtx.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.fdt.common.entity.AbstractBaseEntity;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.security.entity.UserAccess;


@Entity
@Table(name="ECOMM_USERS_ACCOUNT")
public class UserAccount extends AbstractBaseEntity {

    private static final long serialVersionUID = 6697338267742941590L;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_BILLING_DATE")
    private Date lastBillingDate = new DateTime().toDateMidnight().toDateTime().toDate();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "NEXT_BILLING_DATE")
    private Date nextBillingDate = new DateTime().toDateMidnight().toDateTime().toDate();

    @Column(name = "MARK_FOR_CANCELLATION")
    @Type(type="yes_no")
    private boolean markForCancellation = false;

    @OneToOne
    @JoinColumn(name = "CREDIT_CARD_ID", nullable = false)
    private CreditCard creditCard;

    @OneToOne
    @JoinColumn(name = "USER_ACCESS_ID", nullable = false)
    private UserAccess userAccess;

    @Transient
    private String lastTxRefNum = null;


    public String getLastTxRefNum() {
        return lastTxRefNum;
    }

    public void setLastTxRefNum(String lastTxRefNum) {
        this.lastTxRefNum = lastTxRefNum;
    }

    @Column(name = "IS_VERIFIED", nullable = false)
    @Type(type="yes_no")
    protected boolean isVerified = true;

    public Date getLastBillingDate() {
        return lastBillingDate;
    }

    public void setLastBillingDate(Date lastBillingDate) {
        this.lastBillingDate = lastBillingDate;
    }

    public Date getNextBillingDate() {
        return nextBillingDate;
    }

    public void setNextBillingDate(Date nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    public boolean isMarkForCancellation() {
        return markForCancellation;
    }

    public void setMarkForCancellation(boolean markForCancellation) {
        this.markForCancellation = markForCancellation;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    public UserAccess getUserAccess() {
        return userAccess;
    }

    public void setUserAccess(UserAccess userAccess) {
        this.userAccess = userAccess;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

	@Override
	public String toString() {
		return "UserAccount [lastBillingDate=" + lastBillingDate
				+ ", nextBillingDate=" + nextBillingDate
				+ ", markForCancellation=" + markForCancellation
				+ ", creditCard=" + creditCard + ", userAccess=" + userAccess
				+ ", lastTxRefNum=" + lastTxRefNum + ", isVerified="
				+ isVerified + "]";
	}

}
