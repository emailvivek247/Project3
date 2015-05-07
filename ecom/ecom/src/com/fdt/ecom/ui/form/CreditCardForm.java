package com.fdt.ecom.ui.form;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.CreditCardNumber;
import org.hibernate.validator.constraints.Length;

public class CreditCardForm {

    public interface CreditCardGroup {
    }
    
    public interface BankAccountGroup {
    }

    private static String NEW_CREDIT_CARD = "N";

    private String useExistingAccount = NEW_CREDIT_CARD;

    @Pattern(regexp="[a-zA-Z][a-zA-Z ]+",message="security.invalid.accountName" , groups = { CreditCardGroup.class } )
    @Size(min = 1, max = 50, message="security.invalid.accountName" , groups = { CreditCardGroup.class })
    private String accountName;

    @Pattern(regexp="[a-zA-Z][a-zA-Z ]+",message="security.invalid.bankAccountName" , groups = { BankAccountGroup.class } )
    @Size(min = 1, max = 50, message="security.invalid.bankAccountName" , groups = { BankAccountGroup.class })
    private String bankAccountName;
    
    @CreditCardNumber(message="security.invalid.number" , groups = { CreditCardGroup.class })
    private String number;
    
    @NotNull(message="security.invalid.bankAccountNumber", groups = { BankAccountGroup.class })
    private String bankAccountNumber;
    
    @NotNull(message="security.invalid.bankRoutingNumber", groups = { BankAccountGroup.class })
    private String bankRoutingNumber;
    
    @NotNull(message="security.invalid.bankAccountType", groups = { BankAccountGroup.class })
    private String bankAccountType;
    
    @NotNull(message="security.invalid.payByACH", groups = { BankAccountGroup.class })
    private String payByMethod;

    private Integer expMonth;

    private String expMonthS;

    @NotNull(message="security.invalid.expYear", groups = { CreditCardGroup.class })
    @Min(value=2011, message="security.invalid.expYear", groups = { CreditCardGroup.class })
    private Integer expYear;

    @NotNull(message="security.invalid.cvv", groups = { CreditCardGroup.class })
    @Size(min = 3, max = 4, message="security.invalid.cvv" , groups = { CreditCardGroup.class })
    @Digits(integer=4, fraction=0, message="security.invalid.cvv" , groups = { CreditCardGroup.class })
    private String cvv;

    @Pattern(regexp="[a-zA-Z0-9]+[a-zA-Z0-9 #.,-]+",message="security.invalid.addressLine1" , groups = { CreditCardGroup.class } )
    @Size(min=1, max=250, message="security.invalid.addressLine1", groups = { CreditCardGroup.class })
    private String addressLine1;

    @Size(min=0, max=250, message="security.invalid.addressLine2" , groups = { CreditCardGroup.class })
    private String addressLine2;

    @Pattern(regexp="[a-zA-Z]+[a-zA-Z ]+",message="security.invalid.city" , groups = { CreditCardGroup.class } )
    @Size(min=1, max=50, message="security.invalid.city", groups = { CreditCardGroup.class })
    private String city;

    @Pattern(regexp="[a-zA-Z]{2}",message="security.invalid.state" , groups = { CreditCardGroup.class } )
    @Length(min=2, max=2, message="security.invalid.state", groups = { CreditCardGroup.class })
    private String state;

    @Pattern(regexp="[0-9]{5}",message="security.invalid.zip" , groups = { CreditCardGroup.class } )
    @Size(min=5, max=5, message="security.invalid.zip", groups = { CreditCardGroup.class })
    private String zip;


    @NotNull(message="security.invalid.phoneNumber", groups = { CreditCardGroup.class })
    @Digits(integer=10, fraction=0, message="security.invalid.phoneNumber", groups = { CreditCardGroup.class })
    private Long phoneNumber;
    
    @Pattern(regexp="[a-zA-Z0-9]+[a-zA-Z0-9 #.,-]+",message="security.invalid.addressLine1" , groups = { BankAccountGroup.class } )
    @Size(min=1, max=250, message="security.invalid.addressLine1", groups = { BankAccountGroup.class })
    private String bankAccountAddressLine1;

    @Size(min=0, max=250, message="security.invalid.addressLine2" , groups = { BankAccountGroup.class })
    private String bankAccountAddressLine2;

    @Pattern(regexp="[a-zA-Z]+[a-zA-Z ]+",message="security.invalid.city" , groups = { BankAccountGroup.class } )
    @Size(min=1, max=50, message="security.invalid.city", groups = { BankAccountGroup.class })
    private String bankAccountCity;

    @Pattern(regexp="[a-zA-Z]{2}",message="security.invalid.state" , groups = { BankAccountGroup.class } )
    @Length(min=2, max=2, message="security.invalid.state", groups = { BankAccountGroup.class })
    private String bankAccountState;

    @Pattern(regexp="[0-9]{5}",message="security.invalid.zip" , groups = { BankAccountGroup.class } )
    @Size(min=5, max=5, message="security.invalid.zip", groups = { BankAccountGroup.class })
    private String bankAccountZip;


    @NotNull(message="security.invalid.phoneNumber", groups = { BankAccountGroup.class })
    @Digits(integer=10, fraction=0, message="security.invalid.phoneNumber", groups = { BankAccountGroup.class })
    private Long bankAccountPhoneNumber;
    
    
    
    public String getBankAccountAddressLine1() {
		return bankAccountAddressLine1;
	}

	public void setBankAccountAddressLine1(String bankAccountAddressLine1) {
		this.bankAccountAddressLine1 = bankAccountAddressLine1;
	}

	public String getBankAccountAddressLine2() {
		return bankAccountAddressLine2;
	}

	public void setBankAccountAddressLine2(String bankAccountAddressLine2) {
		this.bankAccountAddressLine2 = bankAccountAddressLine2;
	}

	public String getBankAccountCity() {
		return bankAccountCity;
	}

	public void setBankAccountCity(String bankAccountCity) {
		this.bankAccountCity = bankAccountCity;
	}

	public String getBankAccountState() {
		return bankAccountState;
	}

	public void setBankAccountState(String bankAccountState) {
		this.bankAccountState = bankAccountState;
	}

	public String getBankAccountZip() {
		return bankAccountZip;
	}

	public void setBankAccountZip(String bankAccountZip) {
		this.bankAccountZip = bankAccountZip;
	}

	public Long getBankAccountPhoneNumber() {
		return bankAccountPhoneNumber;
	}

	public void setBankAccountPhoneNumber(Long bankAccountPhoneNumber) {
		this.bankAccountPhoneNumber = bankAccountPhoneNumber;
	}

	private boolean saveCreditCard = true;

    private String maskedCard = null;

    private String emailId = null;

    private String application = null;

    private String SUCCESS;

    private String ERROR;

    public String getSUCCESS() {
        return SUCCESS;
    }

    public void setSUCCESS(String sUCCESS) {
        SUCCESS = sUCCESS;
    }

    public String getERROR() {
        return ERROR;
    }

    public void setERROR(String eRROR) {
        ERROR = eRROR;
    }

    public String getUseExistingAccount() {
        return useExistingAccount;
    }

    public void setUseExistingAccount(String useExistingAccount) {
        this.useExistingAccount = useExistingAccount;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Integer getExpMonth() {
        return expMonth;
    }

    public void setExpMonth(Integer expMonth) {
        this.expMonth = expMonth;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
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

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getExpMonthS() {
        if (this.expMonthS == null ) {
            return this.expMonthS;
        }
        if (this.expMonth.intValue() < 10 ) {
            this.expMonthS = "0" + this.expMonth.toString();
        } else {
            this.expMonthS = this.expMonth.toString();
        }
        return this.expMonthS;
    }

    public void setExpMonthS(String expMonthS) {
        if(expMonthS == ""){
            this.expMonthS = null;
            this.expMonth = null;
        } else {
            this.expMonthS = expMonthS;
            try {
                this.expMonth = new Integer(expMonthS);
            } catch (Exception e) {
                this.expMonth = new Integer(-1);
            }
        }
    }

    public Integer getExpYear() {
        return expYear;
    }

    public void setExpYear(Integer expYear) {
        this.expYear = expYear;
    }

    public boolean isSaveCreditCard() {
        return saveCreditCard;
    }

    public void setSaveCreditCard(boolean saveCreditCard) {
        this.saveCreditCard = saveCreditCard;
    }

    public String getMaskedCard() {
        if (this.number != null) {
            return  this.number.substring(number.length()-4, number.length());
        }
        return maskedCard;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

	public String getBankAccountNumber() {
		return bankAccountNumber;
	}

	public void setBankAccountNumber(String bankAccountNumber) {
		this.bankAccountNumber = bankAccountNumber;
	}

	public String getBankRoutingNumber() {
		return bankRoutingNumber;
	}

	public void setBankRoutingNumber(String bankRoutingNumber) {
		this.bankRoutingNumber = bankRoutingNumber;
	}

	public String getBankAccountType() {
		return bankAccountType;
	}

	public void setBankAccountType(String bankAccountType) {
		this.bankAccountType = bankAccountType;
	}

	public String getPayByMethod() {
		return payByMethod;
	}

	public void setPayByMethod(String payByMethod) {
		this.payByMethod = payByMethod;
	}

	public String getBankAccountName() {
		return bankAccountName;
	}

	public void setBankAccountName(String bankAccountName) {
		this.bankAccountName = bankAccountName;
	}
    
		
}
