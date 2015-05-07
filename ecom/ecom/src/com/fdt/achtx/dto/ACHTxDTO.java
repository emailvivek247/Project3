package com.fdt.achtx.dto;

import java.util.Date;

import com.fdt.common.dto.AbstractBaseDTO;
import com.fdt.ecom.entity.enums.PaymentType;

public class ACHTxDTO extends AbstractBaseDTO {

    private static final long serialVersionUID = 5158423900142174558L;

    private String achLoginKey = null;

    private String pin = null;

    private String acctNumber = null;

    private String acctName = null;

    private String acctRoutingNo = null;

    private String clientIp = null;

    private double txAmount = 0d;

    private String description = null;

    private PaymentType paymentType = null;

    private String siteName = null;

    private Integer totalTransactions = 0;

    /** This is an output Variable **/
    private String txRefNumber = null;

    /** This is an output Variable **/
    private String checkNumber = null;

    /** This is an output Variable **/
    private int noOfTransactions = 0;

    /** This is an output Variable **/
    private String resultMsg = null;

    /** This is the transaction cut off time**/
    private Date txCutOffTime = null;

    /** This is the first transaction timestamp that is included in the check**/
    private Date startTxDate = null;

    public String getAchLoginKey() {
        return achLoginKey;
    }

    public void setAchLoginKey(String achLoginKey) {
        this.achLoginKey = achLoginKey;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getAcctNumber() {
        return acctNumber;
    }

    public void setAcctNumber(String acctNumber) {
        this.acctNumber = acctNumber;
    }

    public String getAcctName() {
        return acctName;
    }

    public void setAcctName(String acctName) {
        this.acctName = acctName;
    }

    public String getAcctRoutingNo() {
        return acctRoutingNo;
    }

    public void setAcctRoutingNo(String acctRoutingNo) {
        this.acctRoutingNo = acctRoutingNo;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public double getTxAmount() {
        return txAmount;
    }

    public void setTxAmount(double txAmount) {
        this.txAmount = txAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public String getTxRefNumber() {
        return txRefNumber;
    }

    public void setTxRefNumber(String txRefNumber) {
        this.txRefNumber = txRefNumber;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNum(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public int getNoOfTransactions() {
        return noOfTransactions;
    }

    public void setNoOfTransactions(int noOfTransactions) {
        this.noOfTransactions = noOfTransactions;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public Date getTxCutOffTime() {
        return txCutOffTime;
    }

    public void setTxCutOffTime(Date txCutOffTime) {
        this.txCutOffTime = txCutOffTime;
    }

    public Date getStartTxDate() {
		return startTxDate;
	}

	public void setStartTxDate(Date startTxDate) {
		this.startTxDate = startTxDate;
	}

	public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public Integer getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(Integer totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    @Override
    public String toString() {
        return "ACHDTO [achLoginKey=" + achLoginKey + ", pin=" + pin
                + ", acctNumber=" + acctNumber + ", acctName=" + acctName
                + ", acctRoutingNo=" + acctRoutingNo + ", clientIp=" + clientIp
                + ", txAmount=" + txAmount + ", description=" + description
                + ", paymentType=" + paymentType + ", siteName=" + siteName
                + ", totalTransactions=" + totalTransactions + ", txRefNumber="
                + txRefNumber + ", checkNumber=" + checkNumber
                + ", noOfTransactions=" + noOfTransactions + ", resultMsg="
                + resultMsg + ", txCutOffTime=" + txCutOffTime
                + ", createdDate=" + createdDate + ", modifiedDate="
                + modifiedDate + ", modifiedBy=" + modifiedBy + ", active="
                + active + ", createdBy=" + createdBy + "]";
    }
}