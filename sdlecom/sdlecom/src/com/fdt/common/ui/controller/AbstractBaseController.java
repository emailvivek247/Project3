package com.fdt.common.ui.controller;

import static com.fdt.common.ui.BaseViewConstants.COMMON_GENERAL_ERROR;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import net.tanesha.recaptcha.ReCaptcha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.util.client.ServiceStub;
import com.fdt.security.entity.User;

public abstract class AbstractBaseController {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/** Used to Store the Success Message **/
	protected final static String SUCCESS_MSG = "SUCCESS_MSG";

	/** Used to Store the Failure Message **/
	protected static String FAILURE_MSG = "FAILURE_MSG";

	/** Used to Store the Business Exceptions **/
	protected static String BUSSINESS_EXCP = "BUSSINESS_EXCP";
	
	@Autowired
	protected ReCaptcha reCaptcha;

	@Value("${nodeName}")
	protected String nodeName = null;

	@Value("${isFirmNumberRequired}")
	protected boolean isFirmNumberRequired = false;

	@Value("${isBarNumberRequired}")
	protected boolean isBarNumberRequired = false;

	
	@Value("${ecommerce.clientName}")
	protected String clientName = null;

	@Value("${ecommerce.serverurl}")
	protected String ecomServerURL = null;

	@Value("${ecommerce.clienturl}")
	protected String ecomClientURL = null;


	/** Certification Properties Start **/
	
	@Value("${cert.watermarkText}")
	protected String watermarkText = null;

	@Value("${cert.inputFormat}")
	protected String inputFormat = null;

	@Value("${cert.footerDateFormat}")
	protected String footerDateFormat = null;

	@Value("${cert.attestedDateFormat}")
	protected String attestedDateFormat = null;

	@Value("${cert.urlVerification}")
	protected String urlVerification = null;

	/** Certification Properties End **/
	 
	
	@Autowired
	@Qualifier("serviceStubRS")
	private ServiceStub service = null;

	@Autowired
	private MessageSource messages = null;

	@Autowired
	protected AuthenticationManager authenticationManager;

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

	public ServiceStub getService() {
		return service;
	}

	public void setService(ServiceStub service) {
		this.service = service;
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

	protected User getUser(HttpServletRequest request) {
		UsernamePasswordAuthenticationToken userPasswordAuthToken
			= (UsernamePasswordAuthenticationToken) request.getUserPrincipal();
		User user = (User)userPasswordAuthToken.getPrincipal();
		return user;
	}

	protected void reAuthenticate(HttpServletRequest request) {
		/**	This code should not be Removed as it will be used for Reference **/
		/* Enabling access for the user who has paid.--While he is in session.
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		List<GrantedAuthority> authorities = new LinkedList<GrantedAuthority>(authentication.getAuthorities());
		for(PaypalDTO paypalDTO: paypalDtoList){
			authorities.add(new SimpleGrantedAuthority(paypalDTO.getAccessCode()));
		}
		Authentication newAuthentication = new UsernamePasswordAuthenticationToken(authentication.getPrincipal(),
			authentication.getCredentials(),authorities);
		SecurityContextHolder.getContext().setAuthentication(newAuthentication);*/
		/**Refresh the User with the new Access **/
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
	}

	protected ModelAndView getModelAndView(HttpServletRequest request, String viewName) {
		ModelAndView modelAndView = new ModelAndView(viewName);
		modelAndView.addObject("request", request);
		modelAndView.addObject("nodeName", this.nodeName);
		modelAndView.addObject("clientName", this.clientName);
		modelAndView.addObject("ecomServerURL", this.ecomServerURL);
		modelAndView.addObject("ecomClientURL", this.ecomClientURL);
		modelAndView.addObject("defaultSiteName", this.clientName);
		modelAndView.addObject("isFirmNumberRequired", this.isFirmNumberRequired);
		modelAndView.addObject("isBarNumberRequired", this.isBarNumberRequired);
		return modelAndView;
	}
}
