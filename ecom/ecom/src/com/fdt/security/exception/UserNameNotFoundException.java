package com.fdt.security.exception;

import com.fdt.common.exception.SDLException;

public class UserNameNotFoundException extends SDLException {

    private static final long serialVersionUID = 7013624614498472840L;

    public UserNameNotFoundException(String message){
        super(message);
    }
}
