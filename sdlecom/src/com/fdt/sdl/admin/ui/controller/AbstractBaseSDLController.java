package com.fdt.sdl.admin.ui.controller;

import static com.fdt.common.ui.BaseViewConstants.COMMON_GENERAL_ERROR;
import static com.fdt.sdl.admin.ui.SDLViewConstants.SDL_FORWARD_WELCOME;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.sdl.util.SecurityUtil;
import com.fdt.security.entity.User;

public abstract class AbstractBaseSDLController {
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	/** Used to Store the Success Message **/
	protected final static String SUCCESS_MSG = "SUCCESS_MSG";
	
	/** Used to Store the Failure Message **/
	protected static String FAILURE_MSG = "FAILURE_MSG";
	
	@Autowired
	private MessageSource messages = null;
	
	@ExceptionHandler(Exception.class)
	protected ModelAndView handleException(HttpServletRequest request, Exception exception){
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
	
	protected User getUser(HttpServletRequest request) {
		UsernamePasswordAuthenticationToken userPasswordAuthToken 
			= (UsernamePasswordAuthenticationToken) request.getUserPrincipal(); 
		User user = (User)userPasswordAuthToken.getPrincipal();
		return user;
	}
	
	protected ModelAndView getModelAndView(HttpServletRequest request, String viewName) {
		ModelAndView modelAndView = new ModelAndView(viewName);
		if (!SecurityUtil.isAdminUser(request)) {
			modelAndView.setViewName(SDL_FORWARD_WELCOME);
		} else {
			modelAndView.addObject("request", request);
		}
		return modelAndView;
	}
	
	protected String getMessage(String msgCode) {
		return this.messages.getMessage(msgCode, null,  new Locale("en"));
	}
}
