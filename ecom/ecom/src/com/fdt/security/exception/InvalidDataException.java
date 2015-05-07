package com.fdt.security.exception;

import com.fdt.common.exception.SDLException;

public class InvalidDataException extends SDLException {

    private static final long serialVersionUID = -610787882358730874L;

    public InvalidDataException(String message){
        super(message);
    }
}
