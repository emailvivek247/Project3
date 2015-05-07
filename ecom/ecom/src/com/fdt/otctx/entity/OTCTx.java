package com.fdt.otctx.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fdt.common.entity.Tx;

@Entity
@Table(name="ECOMM_OTC_TX")
public class OTCTx extends Tx {

    private static final long serialVersionUID = 2540079898258401639L;

    @Column(name = "ITEM_NAME")
    private String itemName = null;

    @Column(name = "PRODUCT_TYPE")
    private String productType = null;

    @Column(name="SITE_ID", nullable = false)
    private Long siteId = null;

    @Column(name="OFFICE_LOC_NAME")
	private String officeLoc = null;

	@Column(name = "OFFICE_LOC_ADDRLINE1")
	private String officeLocAddressLine1;

	@Column(name = "OFFICE_LOC_ADDRLINE2")
	private String officeLocAddressLine2;

	@Column(name = "OFFICE_LOC_CITY")
	private String officeLocCity;

	@Column(name = "OFFICE_LOC_STATE")
	private String officeLocState;

	@Column(name = "OFFICE_LOC_ZIP")
	private String officeLocZip;

	@Column(name = "OFFICE_LOC_PHONE")
	private String officeLocPhone;

	@Column(name = "OFFICE_LOC_COMMENTS1")
	private String officeLocComments1;

	@Column(name = "OFFICE_LOC_COMMENTS2")
	private String officeLocComments2;

	@Column(name = "INVOICE_NUMBER")
	private String invoiceNumber = null;

	@Column(name="SIGNATURE", columnDefinition="varbinary(max)")
    private byte[] signature = null;

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public Long getSiteId() {
		return siteId;
	}

	public void setSiteId(Long siteId) {
		this.siteId = siteId;
	}

	public String getOfficeLoc() {
		return officeLoc;
	}

	public void setOfficeLoc(String officeLoc) {
		this.officeLoc = officeLoc;
	}

	public String getOfficeLocAddressLine1() {
		return officeLocAddressLine1;
	}

	public void setOfficeLocAddressLine1(String officeLocAddressLine1) {
		this.officeLocAddressLine1 = officeLocAddressLine1;
	}

	public String getOfficeLocAddressLine2() {
		return officeLocAddressLine2;
	}

	public void setOfficeLocAddressLine2(String officeLocAddressLine2) {
		this.officeLocAddressLine2 = officeLocAddressLine2;
	}

	public String getOfficeLocCity() {
		return officeLocCity;
	}

	public void setOfficeLocCity(String officeLocCity) {
		this.officeLocCity = officeLocCity;
	}

	public String getOfficeLocState() {
		return officeLocState;
	}

	public void setOfficeLocState(String officeLocState) {
		this.officeLocState = officeLocState;
	}

	public String getOfficeLocZip() {
		return officeLocZip;
	}

	public void setOfficeLocZip(String officeLocZip) {
		this.officeLocZip = officeLocZip;
	}

	public String getOfficeLocPhone() {
		return officeLocPhone;
	}

	public void setOfficeLocPhone(String officeLocPhone) {
		this.officeLocPhone = officeLocPhone;
	}

	public String getOfficeLocComments1() {
		return officeLocComments1;
	}

	public void setOfficeLocComments1(String officeLocComments1) {
		this.officeLocComments1 = officeLocComments1;
	}

	public String getOfficeLocComments2() {
		return officeLocComments2;
	}

	public void setOfficeLocComments2(String officeLocComments2) {
		this.officeLocComments2 = officeLocComments2;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

}
