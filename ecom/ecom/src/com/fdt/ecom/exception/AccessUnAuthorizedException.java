package com.fdt.ecom.exception;

import com.fdt.common.exception.SDLException;

public class AccessUnAuthorizedException extends SDLException{

    private static final long serialVersionUID = 1268688818994878211L;

    public AccessUnAuthorizedException() {
        super();
    }

    public AccessUnAuthorizedException(String message) {
        super(message);
    }

}
