package com.fdt.common.exception;

public class SDLBusinessException extends SDLException {

    private static final long serialVersionUID = -7822678975310758783L;

    private String businessMessage = null;

    public SDLBusinessException() {
        super();
    }

    public SDLBusinessException(String message) {
        super(message);
        this.businessMessage = message;
    }

    public String getBusinessMessage() {
        return businessMessage;
    }

    public void setBusinessMessage(String businessMessage) {
        this.businessMessage = businessMessage;
    }
}