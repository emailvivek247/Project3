package com.fdt.webtx.entity;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fdt.common.entity.Tx;

@Entity
@Table(name = "ECOMM_WEBPAY_TX")
public class WebTx extends Tx {


	private static final long serialVersionUID = -2570977918063904127L;

	@Transient
	private List<WebTxItem> webTxItems = null;

	@Transient
	private Long itemsPurchased = 0L;

	@Transient
	private Long itemsRefunded = 0L;

	@Column(name = "APPLICATION")
	private String application = null;

	@Column(name = "SITE_ID")
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

	@Column(name = "EXPMON")
	private Integer expiryMonth;

	@Column(name = "EXPYEAR")
	private Integer expiryYear;

	@Column(name = "ADDRESS_1")
	private String addressLine1 = null;

	@Column(name = "ADDRESS_2")
	private String addressLine2 = null;

	@Column(name = "CITY")
	private String city = null;

	@Column(name = "STATE")
	private String state = null;

	@Column(name = "ZIP")
	private String zip= null;

	@Column(name = "PHONE")
	private Long phone;

	public List<WebTxItem> getWebTxItems() {
		return webTxItems;
	}

	public void setWebTxItems(List<WebTxItem> webTxItems) {
		this.webTxItems = webTxItems;
	}

	public void setWebTxItem(WebTxItem webTxItem) {
		if (this.webTxItems == null) {
			this.webTxItems =  new LinkedList<WebTxItem>();
		}
		this.webTxItems.add(webTxItem);
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public Long getItemsPurchased() {
		return itemsPurchased;
	}

	public void setItemsPurchased(Long itemsPurchased) {
		this.itemsPurchased = itemsPurchased;
	}

	public Long getItemsRefunded() {
		return itemsRefunded;
	}

	public void setItemsRefunded(Long itemsRefunded) {
		this.itemsRefunded = itemsRefunded;
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

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public Integer getExpiryMonth() {
		return expiryMonth;
	}

	public void setExpiryMonth(Integer expiryMonth) {
		this.expiryMonth = expiryMonth;
	}

	public Integer getExpiryYear() {
		return expiryYear;
	}

	public void setExpiryYear(Integer expiryYear) {
		this.expiryYear = expiryYear;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public Long getPhone() {
		return phone;
	}

	public void setPhone(Long phone) {
		this.phone = phone;
	}

}