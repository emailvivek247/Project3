package com.fdt.security.spring;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

public class SDLSimpleUrlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MessageSource messageSource = null;

    private static String AUTHENTICATION_ERROR_MESSAGE = "AUTHENTICATION_ERROR_MESSAGE";

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        String errorMessage = null;
        if (logger.isDebugEnabled()) {
            logger.debug("Exception Occured while Authenticating", exception);
        }
        if (exception instanceof UsernameNotFoundException) {
            errorMessage = messageSource.getMessage("security.authentication.usernamenotfound", null, Locale.US);
        } else if (exception instanceof BadCredentialsException) {
            errorMessage = messageSource.getMessage("security.authentication.badcrentials", null, Locale.US);
        } else if (exception instanceof AccountExpiredException) {
            errorMessage = messageSource.getMessage("security.authentication.accountexpired", null, Locale.US);
        } else if (exception instanceof DisabledException) {
            errorMessage = messageSource.getMessage("security.authentication.accountdisabled", null, Locale.US);
        } else if (exception instanceof LockedException) {        	
            errorMessage = messageSource.getMessage("security.authentication.accountlocked", null, Locale.US);
        } else if (exception instanceof CredentialsExpiredException) {
            errorMessage = messageSource.getMessage("security.authentication.accountlocked", null, Locale.US);
        }  else {
            logger.error("Exception Occured while Authenticating", exception);
            errorMessage = messageSource.getMessage("security.authentication.generalerror", null, Locale.US);
        }
        request.setAttribute(AUTHENTICATION_ERROR_MESSAGE, errorMessage);
        super.onAuthenticationFailure(request, response, exception);
    }
}
