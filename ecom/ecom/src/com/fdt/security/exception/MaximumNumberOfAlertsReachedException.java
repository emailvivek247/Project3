package com.fdt.security.exception;

import com.fdt.common.exception.SDLException;

public class MaximumNumberOfAlertsReachedException extends SDLException {

    private static final long serialVersionUID = 8728352066327969889L;

    public MaximumNumberOfAlertsReachedException(String message){
        super(message);
    }
}