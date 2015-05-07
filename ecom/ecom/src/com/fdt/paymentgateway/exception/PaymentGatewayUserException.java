package com.fdt.paymentgateway.exception;

import com.fdt.common.exception.SDLException;

public class PaymentGatewayUserException extends SDLException {

    private static final long serialVersionUID = -2489902473516777568L;

    public PaymentGatewayUserException() {
        super();
    }

    public PaymentGatewayUserException(String message) {
        super(message);
    }
}