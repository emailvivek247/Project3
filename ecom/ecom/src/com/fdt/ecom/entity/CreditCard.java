package com.fdt.ecom.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.jasypt.hibernate4.type.EncryptedStringType;

import com.fdt.common.entity.AbstractBaseEntity;
import com.fdt.ecom.entity.enums.CardType;
import com.fdt.recurtx.entity.UserAccount;

@TypeDef (name="encryptedString", typeClass= EncryptedStringType.class,
        parameters= {
            @Parameter(name="encryptorRegisteredName",  value="hibernateStringEncryptor")
        }
    )
@Entity
@Table(name = "ECOMM_CCINFO")
public class CreditCard extends AbstractBaseEntity {

    private static final long serialVersionUID = -6827624352031126235L;

    @Column(name = "ACCOUNTNAME", nullable = false)
    private String name = null;

    @Type(type="encryptedString")
    @Column(name = "ACCOUNTNUMBER", nullable = false)
    private String number;

    @Column(name = "EXPMON", nullable = false)
    private Integer expiryMonth;

    @Column(name = "EXPYEAR", nullable = false)
    private Integer expiryYear;

    @Column(name = "ADDRESS_1", nullable = false)
    private String addressLine1 = null;

    @Column(name = "ADDRESS_2")
    private String addressLine2 = null;

    @Column(name = "CITY", nullable = false)
    private String city = null;

    @Column(name = "STATE", nullable = false)
    private String state = null;

    @Column(name = "ZIP", nullable = false)
    private String zip= null;

    @Column(name = "PHONE")
    private Long phone;

    @Column(name = "USER_ID", nullable = false)
    private Long userId = null;

    @Transient
    @OneToOne(fetch=FetchType.LAZY, mappedBy="creditCard")
    private UserAccount userAccount;

    @Transient
    private String securityCode = null;

    @Transient
    private CardType cardType = null;

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = (name == null ? null : name.toUpperCase());
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
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

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = (addressLine1 == null ? null : addressLine1.toUpperCase());
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = (addressLine2 == null ? null : addressLine2.toUpperCase());
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = (city == null ? null : city.toUpperCase());
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = (state == null ? null : state.toUpperCase());
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

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

	@Override
	public String toString() {
		return "CreditCard [name=" + name + ", userId=" + userId
				+ ", userAccount=" + userAccount + "]";
	}


}
