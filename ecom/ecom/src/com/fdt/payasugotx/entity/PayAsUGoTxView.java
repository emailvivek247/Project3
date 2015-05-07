package com.fdt.payasugotx.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "V_GET_PAYASUGO_TX_INCLUDE_FIRM_USERS")
@JsonIgnoreProperties(ignoreUnknown=true)
public class PayAsUGoTxView implements Serializable {


	private static final long serialVersionUID = 6794977899904546628L;

    @Id
	@Column(name="ID")
    private Long payAsUGoTxId = null;

	@Column(name = "TX_REFERENCE_NUM")
	private String txRefNum = null;

	@Column(name = "TX_AMOUNT")
	private Double totalTxAmount = null;

	// With java Date hibernate will not return the time along with date. It solved the problem when we use Timestamp
	@Column(name = "TX_DATE")
	private Timestamp transactionDate = null;

	@Column(name = "CARD_NUMBER")
	private String cardNumber = null;

	@Column(name = "ACCOUNTNAME")
	private String accountName = null;

	@Column(name = "TX_TYPE")
	private String transactionType = null;

    @Column(name = "IS_CERTIFIED", nullable = false)
    @Type(type="yes_no")
    protected boolean isCertified = false;

    @Column(name = "DATE_TIME_CREATED")
	private Date createdDate = null;

	@Column(name = "SITE_NAME")
	private String siteName = null;

	@Column(name = "ITEMSPURCHASED")
	private Integer itemsPurchased = null;

	@Column(name = "CREATED_BY")
	private String createdBy = null;

	@Column(name = "SITE_DESCRIPTION")
	private String siteDescription = null;

	@Column(name = "SITE_TIMEZONE")
	private String timezone = null;

	@Column(name = "USERNAME")
	private String userName = null;

	@Column(name = "SUBSCRIPTION")
	private String subscription = null;

	@Column(name = "FIRM_USERNAME")
	private String firmUserName = null;

	@Column(name = "NODE_NAME")
	private String nodeName = null;
	

	public Long getPayAsUGoTxId() {
		return payAsUGoTxId;
	}

	public void setPayAsUGoTxId(Long payAsUGoTxId) {
		this.payAsUGoTxId = payAsUGoTxId;
	}


	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public Integer getItemsPurchased() {
		return itemsPurchased;
	}

	public void setItemsPurchased(Integer itemsPurchased) {
		this.itemsPurchased = itemsPurchased;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	@JsonProperty("Site")
	public String getSiteDescription() {
		return siteDescription;
	}

	public void setSiteDescription(String siteDescription) {
		this.siteDescription = siteDescription;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	@JsonProperty("User")
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@JsonProperty("Subscription")
	public String getSubscription() {
		return this.subscription;
	}

	public void setSubscription(String subscription) {
		this.subscription = subscription;
	}

	public String getFirmUserName() {
		return firmUserName;
	}

	public void setFirmUserName(String firmUserName) {
		this.firmUserName = firmUserName;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getTxRefNum() {
		return txRefNum;
	}

	public void setTxRefNum(String txRefNum) {
		this.txRefNum = txRefNum;
	}

	public Double getTotalTxAmount() {
		return totalTxAmount;
	}

	public void setTotalTxAmount(Double totalTxAmount) {
		this.totalTxAmount = totalTxAmount;
	}

	public Timestamp getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Timestamp transactionDate) {
		this.transactionDate = transactionDate;
	}

	public String getTransactionType() {
		return transactionType;
	}
	
	public boolean isCertified() {
		return isCertified;
	}

	public void setCertified(boolean isCertified) {
		this.isCertified = isCertified;
	}
	

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	@JsonProperty("transactionDateString")
	public String getTransactionDateString(){
		// UI Layer needs the date as a String with Timezone
		if(this.transactionDate != null){
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa");
			String transactionDateString = df.format(this.transactionDate);
			return transactionDateString + " " +
					TimeZone.getTimeZone(this.getTimezone()).getDisplayName(false, TimeZone.SHORT);
		} else {
			return "";
		}
		
	}
	
	@Override
	public String toString() {
		return "PayAsUGoTxView [payAsUGoTxId=" + payAsUGoTxId + ", txRefNum="
				+ txRefNum + ", totalTxAmount=" + totalTxAmount
				+ ", transactionDate=" + transactionDate + ", cardNumber="
				+ cardNumber + ", accountName=" + accountName
				+ ", transactionType=" + transactionType + ", isCertified="
				+ isCertified + ", createdDate=" + createdDate + ", siteName="
				+ siteName + ", itemsPurchased=" + itemsPurchased
				+ ", siteDescription=" + siteDescription
				+ ", createdBy=" + createdBy + ", timezone=" + timezone + ", userName="
				+ userName + ", subscription=" + subscription
				+ ", firmUserName=" + firmUserName + ", nodeName=" + nodeName
				+ "]";
	}

	


}