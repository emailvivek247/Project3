package com.fdt.security.spring;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.util.Assert;

import com.fdt.common.util.SystemUtil;

public class SDLPreRequestHeaderAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {
    private String principalRequestHeader = "SM_USER";
    private String credentialsRequestHeader;
    private boolean exceptionIfHeaderMissing = true;

    /**
     * Read and returns the header named by {@code principalRequestHeader} from the request.
     *
     * @throws PreAuthenticatedCredentialsNotFoundException if the header is missing and {@code exceptionIfHeaderMissing}
     *          is set to {@code true}.
     */
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String principal = request.getParameter(principalRequestHeader);
        principal =  SystemUtil.decrypt(principal);
        if (principal == null && exceptionIfHeaderMissing) {
            throw new PreAuthenticatedCredentialsNotFoundException(principalRequestHeader
                    + " header not found in request.");
        }

        return principal;
    }

    /**
     * Credentials aren't usually applicable, but if a {@code credentialsRequestHeader} is set, this
     * will be read and used as the credentials value. Otherwise a dummy value will be used.
     */
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        if (credentialsRequestHeader != null) {
        	return SystemUtil.decrypt(request.getParameter(credentialsRequestHeader));
        }

        return "N/A";
    }

    public void setPrincipalRequestHeader(String principalRequestHeader) {
        Assert.hasText(principalRequestHeader, "principalRequestHeader must not be empty or null");
        this.principalRequestHeader = principalRequestHeader;
    }

    public void setCredentialsRequestHeader(String credentialsRequestHeader) {
        Assert.hasText(credentialsRequestHeader, "credentialsRequestHeader must not be empty or null");
        this.credentialsRequestHeader = credentialsRequestHeader;
    }

    /**
     * Defines whether an exception should be raised if the principal header is missing. Defaults to {@code true}.
     *
     * @param exceptionIfHeaderMissing set to {@code false} to override the default behaviour and allow
     *          the request to proceed if no header is found.
     */
    public void setExceptionIfHeaderMissing(boolean exceptionIfHeaderMissing) {
        this.exceptionIfHeaderMissing = exceptionIfHeaderMissing;
    }
}
