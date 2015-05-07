package com.fdt.security.exception;

import com.fdt.common.exception.SDLException;

public class UserNameAlreadyExistsException extends SDLException {

    private static final long serialVersionUID = 7899929245278038722L;

    public UserNameAlreadyExistsException(String message){
        super(message);
    }
}
