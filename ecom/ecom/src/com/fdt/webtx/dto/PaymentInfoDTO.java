package com.fdt.webtx.dto;

import com.fdt.ecom.entity.enums.BankAccountType;

public class PaymentInfoDTO {
	
	private Long id = null;
	
	private String accountName = null;

	private String creditCardNumber = null;

	private String bankRoutingNumber = null;

	private String bankAccountNumber = null;

	private BankAccountType bankAccountType = null;

	private Integer expiryMonth;

	private Integer expiryYear;

	private String addressLine1 = null;

	private String addressLine2 = null;

	private String city = null;

	private String state = null;

	private String zip = null;

	private Long phone;
	
	private String cardType = null;
	
	public String getOptionDisplay() {
		String optionDisplay = null;
		String maskedCreditCardNumber = creditCardNumber.substring(0, 2).concat("******").concat(creditCardNumber.substring(creditCardNumber.length()-4));
		optionDisplay = cardType.concat(" Card ").concat(maskedCreditCardNumber).concat(" Exp ").concat(String.valueOf(expiryMonth)).concat("/").concat(String.valueOf(expiryYear));
		return optionDisplay;
	}
	
		
	public String getCardType() {
		return cardType;
	}


	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getCreditCardNumber() {
		return creditCardNumber;
	}

	public void setCreditCardNumber(String creditCardNumber) {
		this.creditCardNumber = creditCardNumber;
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

	public void setBankAccountType(BankAccountType bankAccountType) {
		this.bankAccountType = bankAccountType;
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
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PaymentInfoDTO other = (PaymentInfoDTO) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "PaymentInfoDTO [accountName=" + accountName
				+ ", creditCardNumber=" + creditCardNumber
				+ ", bankRoutingNumber=" + bankRoutingNumber
				+ ", bankAccountNumber=" + bankAccountNumber
				+ ", bankAccountType=" + bankAccountType + ", expiryMonth="
				+ expiryMonth + ", expiryYear=" + expiryYear
				+ ", addressLine1=" + addressLine1 + ", addressLine2="
				+ addressLine2 + ", city=" + city + ", state=" + state
				+ ", zip=" + zip + ", phone=" + phone + "]";
	}
	
	
	
}
