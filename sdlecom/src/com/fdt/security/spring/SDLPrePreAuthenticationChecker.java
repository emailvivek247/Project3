package com.fdt.security.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

public class SDLPrePreAuthenticationChecker implements UserDetailsChecker {

	private static final Logger logger = LoggerFactory.getLogger(SDLPrePreAuthenticationChecker.class);

	public void check(UserDetails user) {

		if (!user.isEnabled()) {
			logger.debug("User account is disabled");
			throw new DisabledException("User is disabled");
		}

		if (!user.isAccountNonLocked()) {
			logger.debug("User account is locked");
			throw new LockedException("User account is Locked");
		}


		if (!user.isAccountNonExpired()) {
			logger.debug("User account is expired");
			throw new AccountExpiredException("User account has expired");
		}
	}
}