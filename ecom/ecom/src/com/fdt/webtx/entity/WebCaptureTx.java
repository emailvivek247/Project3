package com.fdt.webtx.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fdt.common.entity.AbstractBaseEntity;

@Entity
@Table(name = "ECOMM_WEBPAY_CAPTURE_TX")
public class WebCaptureTx extends AbstractBaseEntity {

	private static final long serialVersionUID = 3173158436458276331L;

	@Column(name = "CAPTURE_TX_REFERENCE_NUM", nullable = false)
	private String captureTxReferenceNumber = null;
	
	@Column(name="CAPTURE_TX_AMOUNT", nullable = false)
    private Double captureTxAmount;
	
	@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CAPTURE_TX_DATE", nullable = false)
    private Date captureTxDate = null;
	
	@Column(name = "COMMENTS")
    private String comments = null;
	
	@Column(name = "WEBTX_ID", nullable = false)
    private Long webTxId = null;

	public String getCaptureTxReferenceNumber() {
		return captureTxReferenceNumber;
	}

	public void setCaptureTxReferenceNumber(String captureTxReferenceNumber) {
		this.captureTxReferenceNumber = captureTxReferenceNumber;
	}

	public Double getCaptureTxAmount() {
		return captureTxAmount;
	}

	public void setCaptureTxAmount(Double captureTxAmount) {
		this.captureTxAmount = captureTxAmount;
	}

	public Date getCaptureTxDate() {
		return captureTxDate;
	}

	public void setCaptureTxDate(Date captureTxDate) {
		this.captureTxDate = captureTxDate;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Long getWebTxId() {
		return webTxId;
	}

	public void setWebTxId(Long webTxId) {
		this.webTxId = webTxId;
	}
	
	
	
	

}