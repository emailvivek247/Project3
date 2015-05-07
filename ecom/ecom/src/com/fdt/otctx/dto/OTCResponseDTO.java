package com.fdt.otctx.dto;

import com.fdt.common.dto.AbstractBaseDTO;

public class OTCResponseDTO extends AbstractBaseDTO {

    private static final long serialVersionUID = 7450404114776152622L;

    private String payPalTxRefNum;

    private String authCode;

    private String errorCode;

    private String errorDesc;

    private double baseAmount;

    private double serviceFee;

    private double totalTxAmount;

    public String getPayPalTxRefNum() {
        return payPalTxRefNum;
    }

    public void setPayPalTxRefNum(String payPalTxRefNum) {
        this.payPalTxRefNum = payPalTxRefNum;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    public void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
    }

    public double getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(double serviceFee) {
        this.serviceFee = serviceFee;
    }

    public double getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(double baseAmount) {
        this.baseAmount = baseAmount;
    }

    public double getTotalTxAmount() {
        return totalTxAmount;
    }

    public void setTotalTxAmount(double totalTxAmount) {
        this.totalTxAmount = totalTxAmount;
    }

    @Override
    public String toString() {
        return "OTCResponseDTO [payPalTxRefNum=" + payPalTxRefNum
                + ", authCode=" + authCode + ", errorCode=" + errorCode
                + ", errorDesc=" + errorDesc + ", baseAmount=" + baseAmount
                + ", serviceFee=" + serviceFee + ", totalTxAmount="
                + totalTxAmount + ", createdDate=" + createdDate
                + ", modifiedDate=" + modifiedDate + ", modifiedBy="
                + modifiedBy + ", active=" + active + ", createdBy="
                + createdBy + "]";
    }
}