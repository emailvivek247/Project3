package com.fdt.paymentgateway.exception;


public enum PaymentGatewayUserExceptionCodes {

    TRANSACTION_DECLINED("12"),
    ORIGINAL_TRANSACTIONID_NOTFOUND("19"),
    INVALID_ACCOUNT_NUMBER("23"),
    INVALID_EXPIRATION_DATE("24"),
    DUPLICATE_TRANSACTION("30"),
    ERROR_ADDING_RECUR_PROFILE("31"),
    ERROR_MODIFYING_RECUR_PROFILE("32"),
    ERROR_CANCEL_RECUR_PROFILE("33"),
    ERROR_REACTIVATE_RECUR_PROFILE("35"),
    INVALID_RECURRING_PROFILE_ID("37"),    
    INSUFFICIENT_FUNDS("50"),
    GENERAL_ERROR("99"),
    CREDIT_AMOUNT_CANNOT_EXCEED_ACTUAL_SALEAMOUNT("105"),
    CARD_SECURITY_CODE_MISMATCH("114"),
    TRANSACTION_EXCEEDED("117");
    

    private String errorCode;

    PaymentGatewayUserExceptionCodes(String errorCode) {
        this.errorCode = errorCode;
    }

    public static boolean isEqual(String errorCode) {
        for (PaymentGatewayUserExceptionCodes code: PaymentGatewayUserExceptionCodes.values()){
            if(code.getErrorCode().equals(errorCode)) {
                return true;
            }
        }
        return false;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
