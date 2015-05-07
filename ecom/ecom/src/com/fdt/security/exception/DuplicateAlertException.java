package com.fdt.security.exception;

import com.fdt.common.exception.SDLException;

public class DuplicateAlertException extends SDLException {

    private static final long serialVersionUID = 2576754347201755494L;

    public DuplicateAlertException(String message){
        super(message);
    }
}