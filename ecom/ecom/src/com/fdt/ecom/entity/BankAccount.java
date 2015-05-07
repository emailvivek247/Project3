package com.fdt.ecom.entity;

import com.fdt.ecom.entity.enums.BankAccountType;

public class BankAccount {
	
	private String bankAccountName = null;
	
	private String bankRoutingNumber = null;
	
	private String bankAccountNumber = null;
	
	private BankAccountType bankAccountType;
	
	private String addressLine1 = null;
	
	private String addressLine2 = null;
	
	private String city = null;
	
	private String state = null;
	
	private String zip= null;
	
	private Long phone;
	

	public String getBankAccountName() {
		return bankAccountName;
	}

	public void setBankAccountName(String bankAccountName) {
		this.bankAccountName = bankAccountName;
	}

	public String getBankRoutingNumber() {
		return bankRoutingNumber;
	}

	public void setBankRoutingNumber(String bankRoutingNumber) {
		this.bankRoutingNumber = bankRoutingNumber;
	}

	public String getBankAccountNumber() {
		return bankAccountNumber;
	}

	public void setBankAccountNumber(String bankAccountNumber) {
		this.bankAccountNumber = bankAccountNumber;
	}

	public BankAccountType getBankAccountType() {
		return bankAccountType;
	}

	public void setBankAccountType(String bankAccountType) {
		if(bankAccountType != null && bankAccountType.equalsIgnoreCase("S")) {
			this.bankAccountType = BankAccountType.SAVING;
		} else {
			this.bankAccountType = BankAccountType.CHECKING;
		}
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
