package com.fdt.security.exception;

import com.fdt.common.exception.SDLException;

public class UserNotActiveException extends SDLException {

    private static final long serialVersionUID = 1402770527162209101L;

    public UserNotActiveException(String message){
        super(message);
    }
}
