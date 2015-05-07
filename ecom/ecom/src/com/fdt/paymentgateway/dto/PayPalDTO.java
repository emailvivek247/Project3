package com.fdt.paymentgateway.dto;

import com.fdt.common.dto.AbstractBaseDTO;
import com.fdt.subscriptions.entity.SubscriptionFee;

public class PayPalDTO extends AbstractBaseDTO {

    private static final long serialVersionUID = 3465874827818866616L;

    private Long userId;

    private Long accessId;

    private String txRefNum = null;

    private String referencedtxRefNum = null;

    private String authCode = null;

    /**This is to Used for Enable the User Access from the UI **/
    private String accessCode;

    /**This is to Used to displaying to the User **/
    private String accessDescription;

    /**This is to display to the Transaction Id to the User **/


    /** This is to store whether the transaction is Successful or Not **/
    private boolean isSucessful = true;

    /** This is to store whether the Error is System Exception **/
    private boolean isSystemException = false;

    /**This is used to store the error code if the transaction Failed **/
    private String errorCode = null;

    /**This is used to store the error description if the transaction Failed **/
    private String errorDesc = null;

    /**This is used to Store Subscription Fee **/
    private SubscriptionFee subFee = null;

    /** This is used to store merchantId **/
    private Long merchantId = null;

    /** This is used to store amount **/
    private Double txAmount;

    public Double getTxAmount() {
		return txAmount;
	}

	public void setTxAmount(Double txAmount) {
		this.txAmount = txAmount;
	}

	public PayPalDTO() {
        super();
    }

    public PayPalDTO(String accessCode, String accessDescription, String txRefNum,
            SubscriptionFee subFee, boolean isSucessful) {
        super();
        this.accessCode = accessCode;
        this.accessDescription = accessDescription;
        this.txRefNum = txRefNum;
        this.subFee = subFee;
        this.isSucessful = isSucessful;
    }

    public Long getAccessId() {
        return accessId;
    }

    public void setAccessId(Long accessId) {
        this.accessId = accessId;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public String getAccessDescription() {
        return accessDescription;
    }

    public void setAccessDescription(String accessDescription) {
        this.accessDescription = accessDescription;
    }

    public boolean isSucessful() {
        return isSucessful;
    }

    public void setSucessful(boolean isSucessful) {
        this.isSucessful = isSucessful;
    }

    public SubscriptionFee getSubFee() {
        return subFee;
    }

    public void setSubFee(SubscriptionFee subFee) {
        this.subFee = subFee;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isSystemException() {
        return isSystemException;
    }

    public void setSystemException(boolean isSystemException) {
        this.isSystemException = isSystemException;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    public void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getTxRefNum() {
		return txRefNum;
	}

	public void setTxRefNum(String txRefNum) {
		this.txRefNum = txRefNum;
	}

	public String getReferencedtxRefNum() {
		return referencedtxRefNum;
	}

	public void setReferencedtxRefNum(String referencedtxRefNum) {
		this.referencedtxRefNum = referencedtxRefNum;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	@Override
	public String toString() {
		return "PayPalDTO [userId=" + userId + ", accessId=" + accessId
				+ ", txRefNum=" + txRefNum + ", referencedtxRefNum="
				+ referencedtxRefNum + ", authCode=" + authCode
				+ ", accessCode=" + accessCode + ", accessDescription="
				+ accessDescription + ", isSucessful=" + isSucessful
				+ ", isSystemException=" + isSystemException + ", errorCode="
				+ errorCode + ", errorDesc=" + errorDesc + ", subFee=" + subFee
				+ ", merchantId=" + merchantId + ", txAmount=" + txAmount
				+ ", createdDate=" + createdDate + ", modifiedDate="
				+ modifiedDate + ", modifiedBy=" + modifiedBy + ", active="
				+ active + ", createdBy=" + createdBy + "]";
	}
}