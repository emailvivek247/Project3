package com.fdt.paymentgateway.exception;

import com.fdt.common.exception.SDLException;

public class PaymentGatewaySystemException extends SDLException {

    private static final long serialVersionUID = -3401698500685036661L;

    public PaymentGatewaySystemException() {
        super();
    }

    public PaymentGatewaySystemException(String message) {
        super(message);
    }
}