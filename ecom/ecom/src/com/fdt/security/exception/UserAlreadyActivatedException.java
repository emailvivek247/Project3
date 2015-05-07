package com.fdt.security.exception;

import com.fdt.common.exception.SDLException;

public class UserAlreadyActivatedException extends SDLException {

    private static final long serialVersionUID = -5142243526547170976L;

    public UserAlreadyActivatedException(String message){
        super(message);
    }
}