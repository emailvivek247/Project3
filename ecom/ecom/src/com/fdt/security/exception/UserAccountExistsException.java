package com.fdt.security.exception;

import com.fdt.common.exception.SDLException;

public class UserAccountExistsException extends SDLException {

	private static final long serialVersionUID = -6737986939717885720L;

	public UserAccountExistsException(String message) {
		super(message);
	}

}
