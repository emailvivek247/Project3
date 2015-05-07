package com.fdt.common.ui.controller;

import static com.fdt.common.ui.BaseViewConstants.COMMON_GENERAL_ERROR;

import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.util.SystemUtil;
import com.fdt.ecom.service.EComService;
import com.fdt.payasugotx.service.PayAsUGoTxService;
import com.fdt.recurtx.service.RecurTxService;
import com.fdt.security.entity.User;
import com.fdt.security.service.UserService;
import com.fdt.webtx.service.WebTxService;

public abstract class AbstractBaseController implements ServletContextAware {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** Used to Store the Success Message **/
    protected final static String SUCCESS_MSG = "SUCCESS_MSG";

    /** Used to Store the Failure Message **/
    protected static String FAILURE_MSG = "FAILURE_MSG";

    /** Used to Store the Business Exceptions **/
    protected static String BUSSINESS_EXCP = "BUSSINESS_EXCP";

    @Value("${session.timeout}")
    protected String sessionTimeout = null;

    @Autowired
    @Qualifier("messageSource")
    private MessageSource messages = null;

    @Autowired
    protected AuthenticationManager authenticationManager = null;

    @Autowired
    protected SecurityContextLogoutHandler securityContextLogoutHandler = null;

    @Autowired
    protected CookieClearingLogoutHandler cookieClearingLogoutHandler = null;

    @Autowired
    protected RecurTxService recurTransactionService = null;

    @Autowired
    protected EComService eComService = null;

    @Autowired
    protected UserService userService = null;

    @Autowired
    protected WebTxService webTransactionService = null;

    @Autowired
    protected PayAsUGoTxService payAsUGoSubService = null;

    private ServletContext servletContext = null;

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @ExceptionHandler(Exception.class)
    protected ModelAndView handleAdminExceptions(HttpServletRequest request, Exception exception) {
        ModelAndView modelAndView = this.getModelAndView(request, COMMON_GENERAL_ERROR);
        logger.error("There is an Error at the UI Layer", exception);
        return modelAndView;
    }

    protected void verifyBinding(BindingResult result) {
        String[] suppressedFields = result.getSuppressedFields();
        if (suppressedFields.length > 0) {
            throw new RuntimeException("Attempting to bind suppressed fields: "
                                        + StringUtils.arrayToCommaDelimitedString(suppressedFields));
        }
    }

    protected String verifyBindingInJSON(BindingResult result) {
        String[] suppressedFields = result.getSuppressedFields();
        if (suppressedFields.length > 0) {
            return StringUtils.arrayToCommaDelimitedString(suppressedFields);
        }
        return null;
    }

    protected String getMessage(String msgCode) {
        return this.messages.getMessage(msgCode, null,  new Locale("en"));
    }

    protected void populateErrorSuccessMsg(ModelAndView modelAndView, HttpServletRequest request) {
        if (request.getSession().getAttribute(SUCCESS_MSG) != null) {
            modelAndView.addObject(SUCCESS_MSG, request.getSession().getAttribute(SUCCESS_MSG));
        }
        if (request.getSession().getAttribute(FAILURE_MSG) != null) {
            modelAndView.addObject(FAILURE_MSG, request.getSession().getAttribute(FAILURE_MSG));
        }
        if (request.getSession().getAttribute(BUSSINESS_EXCP) != null) {
            modelAndView.addObject(BUSSINESS_EXCP, request.getSession().getAttribute(BUSSINESS_EXCP));
        }
    }

    protected ModelAndView getModelAndView(HttpServletRequest request, String viewName) {
        ModelAndView modelAndView = new ModelAndView(viewName);
        modelAndView.addObject("request", request);
        return modelAndView;
    }

    protected User getUser(HttpServletRequest request) {
        AbstractAuthenticationToken abstractAuthenticationToken
            = (AbstractAuthenticationToken) request.getUserPrincipal();
        User user = (User)abstractAuthenticationToken.getPrincipal();
        return user;
    }

    protected Authentication reauthenticate(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsernamePasswordAuthenticationToken userPwdAuthtoken = new UsernamePasswordAuthenticationToken(
                request.getRemoteUser(), authentication.getCredentials(), authentication.getAuthorities());
        request.getSession();
        userPwdAuthtoken.setDetails(new WebAuthenticationDetails(request));
        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());
        /**ReAuthenticating to get the new User Roles/User Details as the user has paid for his access **/
        Authentication authenticatedUser = this.authenticationManager.authenticate(userPwdAuthtoken);
        SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
        return authenticatedUser;
    }

    protected void logoutUser(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            cookieClearingLogoutHandler.logout(request, response, auth);
            securityContextLogoutHandler.logout(request, response, auth);
        }
    }

    protected String verifyLoggedInUser(HttpServletRequest request, HttpServletResponse response) {
        User user = this.getUser(request);
        String loggedInUserName = user.getUsername();
        String userNameInParameter = SystemUtil.decrypt(request.getParameter("token1"));
        if(loggedInUserName.equalsIgnoreCase(userNameInParameter)) {
            return user.getUsername();
        } else {
            return null;
        }
    }

}