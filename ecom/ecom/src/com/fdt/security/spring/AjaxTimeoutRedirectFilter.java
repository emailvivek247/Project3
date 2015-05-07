package com.fdt.security.spring;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.filter.GenericFilterBean;

public class AjaxTimeoutRedirectFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(AjaxTimeoutRedirectFilter.class);

    /** Custom Error Code For Ajax Request **/
    private int customSessionExpiredErrorCode = 901;

    /** Session Expired Page **/
    private String sessionTimeOutPage = "/sessionExpired.admin";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(servletRequest, servletResponse);
            logger.debug("Chain processed normally");
        } catch (IOException ex) {
            throw ex;
        } catch (AuthenticationCredentialsNotFoundException ex) {
            logger.info("User session expired or not logged in yet");
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            String ajaxHeader = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(ajaxHeader)) {
                logger.info("Ajax call detected, send {} error code", this.customSessionExpiredErrorCode);
                HttpServletResponse resp = (HttpServletResponse) servletResponse;
                resp.sendError(this.customSessionExpiredErrorCode);
            } else if (isSessionInvalid((HttpServletRequest) servletRequest)) {
                HttpServletResponse resp = (HttpServletResponse) servletResponse;
                resp.sendRedirect(request.getContextPath() + sessionTimeOutPage);
            } else {
                throw ex;
            }
        }
    }

    public void setCustomSessionExpiredErrorCode(int customSessionExpiredErrorCode) {
        this.customSessionExpiredErrorCode = customSessionExpiredErrorCode;
    }

    private boolean isSessionInvalid(HttpServletRequest httpServletRequest) {
        return (httpServletRequest.getRequestedSessionId() != null) && !httpServletRequest.isRequestedSessionIdValid();
    }

    public void setSessionTimeOutPage(String sessionTimeOutPage) {
        this.sessionTimeOutPage = sessionTimeOutPage;
    }
}
