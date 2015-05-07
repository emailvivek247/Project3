package com.fdt.webtx.dto;

import java.io.Serializable;
import java.util.List;

import com.fdt.common.dto.AbstractBaseDTO;
import com.fdt.ecom.entity.BankAccount;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.ShoppingCartItem;
import com.fdt.webtx.entity.WebTxItem;

public class WebTransactionDTO extends AbstractBaseDTO implements Serializable {

    private static final long serialVersionUID = -1764770670860168937L;

    private CreditCard creditCard = null;
    
    private BankAccount bankAccount = null;

    private List<ShoppingCartItem> shoppingCartItemList = null;

    private List<WebTxItem> webTransactionItemList = null;

    private boolean isSaveCreditCard = false;

    private String userName = null;

    private String siteName = null;

    private String transactionLocation = null;

    private String officeLoc = null;

    private String officeLocAddressLine1 = null;

    private String officeLocAddressLine2 = null;

    private String officeLocCity = null;

    private String officeLocState = null;

    private String officeLocZip = null;

    private String officeLocPhone = null;

    private String officeLocComments1 = null;

    private String officeLocComments2 = null;

    private String invoiceId = null;
    
    private boolean isAuthorizeTransaction = false;
    
    private String payByMethod = null;    

	public String getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}

	public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    public List<ShoppingCartItem> getShoppingCartItemList() {
        return shoppingCartItemList;
    }

    public void setShoppingCartItemList(List<ShoppingCartItem> shoppingCartItemList) {
        this.shoppingCartItemList = shoppingCartItemList;
    }

    public List<WebTxItem> getWebTransactionItemList() {
        return webTransactionItemList;
    }

    public void setWebTransactionItemList(
            List<WebTxItem> webTransactionItemList) {
        this.webTransactionItemList = webTransactionItemList;
    }

    public boolean isSaveCreditCard() {
        return isSaveCreditCard;
    }

    public void setSaveCreditCard(boolean isSaveCreditCard) {
        this.isSaveCreditCard = isSaveCreditCard;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getTransactionLocation() {
        return transactionLocation;
    }

    public void setTransactionLocation(String transactionLocation) {
        this.transactionLocation = transactionLocation;
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
    
    public boolean isAuthorizeTransaction() {
		return isAuthorizeTransaction;
	}

	public void setAuthorizeTransaction(boolean isAuthorizeTransaction) {
		this.isAuthorizeTransaction = isAuthorizeTransaction;
	}

	public BankAccount getBankAccount() {
		return bankAccount;
	}

	public void setBankAccount(BankAccount bankAccount) {
		this.bankAccount = bankAccount;
	}
	
	public String getPayByMethod() {
		return payByMethod;
	}

	public void setPayByMethod(String payByMethod) {
		this.payByMethod = payByMethod;
	}

	@Override
    public String toString() {
        return "WebTransactionDTO [creditCard=" + creditCard
                + ", shoppingCartItemList=" + shoppingCartItemList
                + ", webTransactionItemList=" + webTransactionItemList
                + ", isSaveCreditCard=" + isSaveCreditCard + ", userName="
                + userName + ", siteName=" + siteName
                + ", transactionLocation=" + transactionLocation
                + ", officeLoc=" + officeLoc + ", officeLocAddressLine1="
                + officeLocAddressLine1 + ", officeLocAddressLine2="
                + officeLocAddressLine2 + ", officeLocCity=" + officeLocCity
                + ", officeLocState=" + officeLocState + ", officeLocZip="
                + officeLocZip + ", officeLocPhone=" + officeLocPhone
                + ", officeLocComments1=" + officeLocComments1
                + ", officeLocComments2=" + officeLocComments2 + "]";
    }

}