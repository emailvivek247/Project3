package com.fdt.security.exception;

import com.fdt.common.exception.SDLException;

public class BadPasswordException extends SDLException {

    private static final long serialVersionUID = 9187506811577939929L;
	
    public BadPasswordException(String message) {
        super(message);
    }
}
