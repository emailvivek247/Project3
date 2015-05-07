package com.fdt.common.entity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.enums.CardType;
import com.fdt.ecom.entity.enums.SettlementStatusType;
import com.fdt.ecom.entity.enums.TransactionType;

@MappedSuperclass
public class Tx extends AbstractBaseEntity {

    private static final long serialVersionUID = 1447207089698745236L;

    @Column(name = "TX_REFERENCE_NUM", nullable = false)
    private String txRefNum = null;

    @Column(name="AMOUNT", nullable = false)
    private Double baseAmount;

    @Column(name="SERVICE_FEE", nullable = false)
    private Double serviceFee = 0.0d;

    @Column(name="TX_AMOUNT", nullable = false)
    private Double totalTxAmount;

    @Column(name="CARD_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private CardType cardType = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TX_DATE", nullable = false)
    private Date transactionDate = null;

    @Column(name="SETTLEMENT_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private SettlementStatusType settlementStatus = null;

    @Column(name="TX_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType = null;

    @Column(name = "CARD_NUMBER", nullable = false)
    private String cardNumber = null;

    @Column(name = "ACCOUNTNAME", nullable = false)
    private String accountName = null;

    @Column(name = "COMMENTS")
    private String comments = null;

    @Column(name="AUTH_CODE")
    private String authCode = null;

    @Column(name = "MACHINENAME")
    private String machineName = null;

    @Column(name="CHECKNUM")
    private String checkNum = null;

    @Column(name = "ORG_REF_NUM")
    private String origTxRefNum = null;

    @Column(name = "MERCHANT_ID" , nullable = false)
    private Long merchantId = null;

    @Column(name="TAX")
    private Double tax = 0.0d;

    @Column(name="TRAN_FEE_PERCENTAGE")
    private Double txFeePercent;

    @Column(name="TRAN_FEE_FLAT")
    private Double txFeeFlat;

    @Transient
    private Site site = null;

	public String getTxRefNum() {
		return txRefNum;
	}

	public void setTxRefNum(String txRefNum) {
		this.txRefNum = txRefNum;
	}

	public Double getBaseAmount() {
		return baseAmount;
	}

	public void setBaseAmount(Double baseAmount) {
		this.baseAmount = baseAmount;
	}

	public Double getServiceFee() {
		return serviceFee;
	}

	public void setServiceFee(Double serviceFee) {
		this.serviceFee = serviceFee;
	}

	public Double getTotalTxAmount() {
		return totalTxAmount;
	}

	public void setTotalTxAmount(Double totalTxAmount) {
		this.totalTxAmount = totalTxAmount;
	}

	public CardType getCardType() {
		return cardType;
	}

	public void setCardType(CardType cardType) {
		this.cardType = cardType;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	public SettlementStatusType getSettlementStatus() {
		return settlementStatus;
	}

	public void setSettlementStatus(SettlementStatusType settlementStatus) {
		this.settlementStatus = settlementStatus;
	}

	public TransactionType getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(TransactionType transactionType) {
		this.transactionType = transactionType;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		if(cardNumber != null && cardNumber.length() > 4) {
			cardNumber = cardNumber.substring(cardNumber.length() - 4);
		}
		this.cardNumber = cardNumber;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getCheckNum() {
		return this.checkNum;
	}

	public void setCheckNum(String checkNum) {
		this.checkNum = checkNum;
	}

	public String getOrigTxRefNum() {
		return origTxRefNum;
	}

	public void setOrigTxRefNum(String origTxRefNum) {
		this.origTxRefNum = origTxRefNum;
	}

	public Long getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Long merchantId) {
		this.merchantId = merchantId;
	}

	public Double getTax() {
		return tax;
	}

	public void setTax(Double tax) {
		this.tax = tax;
	}

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Double getTxFeePercent() {
		return txFeePercent;
	}

	public void setTxFeePercent(Double txFeePercent) {
		this.txFeePercent = txFeePercent;
	}

	public Double getTxFeeFlat() {
		return txFeeFlat;
	}

	public void setTxFeeFlat(Double txFeeFlat) {
		this.txFeeFlat = txFeeFlat;
	}

	public String getTransactionDateString(){
		if(this.transactionDate != null){
			if(site != null && site.getTimeZone() != null){
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa");
				String transactionDateString = df.format(this.transactionDate);
				return transactionDateString + " " +
						TimeZone.getTimeZone(this.site.getTimeZone()).getDisplayName(false, TimeZone.SHORT);
			}
			else {
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa z");
				return df.format(this.getTransactionDate());
			}
		}
		return "";
	}

	@Override
	public String toString() {
		return "Tx [txRefNum=" + txRefNum + ", baseAmount=" + baseAmount
				+ ", serviceFee=" + serviceFee + ", totalTxAmount="
				+ totalTxAmount + ", cardType=" + cardType
				+ ", transactionDate=" + transactionDate
				+ ", settlementStatus=" + settlementStatus
				+ ", transactionType=" + transactionType + ", cardNumber="
				+ cardNumber + ", accountName=" + accountName + ", comments="
				+ comments + ", authCode=" + authCode + ", machineName="
				+ machineName + ", checkNum=" + checkNum + ", origTxRefNum="
				+ origTxRefNum + ", merchantId=" + merchantId + ", tax=" + tax
				+ ", txFeePercent=" + txFeePercent + ", txFeeFlat=" + txFeeFlat
				+ ", site=" + site + "]";
	}


}
