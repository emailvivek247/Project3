package com.fdt.ecomadmin.ui.form;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.CreditCardNumber;
import org.hibernate.validator.constraints.Length;


public class CreditCardForm {

    public interface DefaultGroup {

    }

    private static String NEW_CREDIT_CARD = "N";

    private String useExistingAccount = NEW_CREDIT_CARD;

    @Pattern(regexp="[a-zA-Z][a-zA-Z ]+",message="security.invalid.accountName" , groups = { DefaultGroup.class } )
    @Size(min = 1, max = 50, message="security.invalid.accountName" , groups = { DefaultGroup.class })
    private String accountName;

    @CreditCardNumber(message="security.invalid.number" , groups = { DefaultGroup.class })
    private String number;

    private Integer expMonth;

    private String expMonthS;

    @NotNull(message="security.invalid.expYear", groups = { DefaultGroup.class })
    @Min(value=2011, message="security.invalid.expYear", groups = { DefaultGroup.class })
    private Integer expYear;

    @NotNull(message="security.invalid.cvv", groups = { DefaultGroup.class })
    @Size(min = 3, max = 4, message="security.invalid.cvv" , groups = { DefaultGroup.class })
    @Digits(integer=4, fraction=0, message="security.invalid.cvv" , groups = { DefaultGroup.class })
    private String cvv;

    @Pattern(regexp="[a-zA-Z0-9]+[a-zA-Z0-9 #.,-]+",message="security.invalid.addressLine1" , groups = { DefaultGroup.class } )
    @Size(min=1, max=250, message="security.invalid.addressLine1", groups = { DefaultGroup.class })
    private String addressLine1;

    @Size(min=0, max=250, message="security.invalid.addressLine2" , groups = { DefaultGroup.class })
    private String addressLine2;

    @Pattern(regexp="[a-zA-Z]+[a-zA-Z ]+",message="security.invalid.city" , groups = { DefaultGroup.class } )
    @Size(min=1, max=50, message="security.invalid.city", groups = { DefaultGroup.class })
    private String city;

    @Pattern(regexp="[a-zA-Z]{2}",message="security.invalid.state" , groups = { DefaultGroup.class } )
    @Length(min=2, max=2, message="security.invalid.state", groups = { DefaultGroup.class })
    private String state;

    @Pattern(regexp="[0-9]{5}",message="security.invalid.zip" , groups = { DefaultGroup.class } )
    @Size(min=5,max=10, message="security.invalid.zip", groups = { DefaultGroup.class })
    private String zip;

    @NotNull(message="security.invalid.phoneNumber", groups = { DefaultGroup.class })
    @Digits(integer=10, fraction=0, message="security.invalid.phoneNumber", groups = { DefaultGroup.class })
    private Long phoneNumber;

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

        if(this.expMonthS == null ){
            this.expMonthS = null;
            return this.expMonthS;

        }
        if (this.expMonth.intValue() < 10 ) {
            this.expMonthS = "0" + this.expMonth.toString();
        }
        else {
            this.expMonthS = this.expMonth.toString();
        }
        return this.expMonthS;
    }

    public void setExpMonthS(String expMonthS) {

        if(expMonthS == ""){
            this.expMonthS = null;
            this.expMonth = null;
        }
        else {
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


}
