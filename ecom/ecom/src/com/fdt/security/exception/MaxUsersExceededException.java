package com.fdt.security.exception;

import com.fdt.common.exception.SDLException;

public class MaxUsersExceededException extends SDLException {

    private static final long serialVersionUID = 7899929245278038722L;

    public MaxUsersExceededException(String message){
        super(message);
    }
}