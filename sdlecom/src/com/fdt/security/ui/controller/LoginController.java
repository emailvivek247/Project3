package com.fdt.security.ui.controller;

import static com.fdt.security.ui.SecurityViewConstants.SECURITY_ACCESS_DENIED;
import static com.fdt.security.ui.SecurityViewConstants.SECURITY_LOGIN;
import static com.fdt.security.ui.SecurityViewConstants.SECURITY_SESSION_EXPIRED;
import static com.fdt.security.ui.SecurityViewConstants.SECURITY_VIEW_USER_TERMS;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.ecom.entity.Term;

@Controller
public class LoginController extends AbstractBaseController {
	
	@RequestMapping(value="/publicLogin.admin")
	public ModelAndView viewLogin(HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, SECURITY_LOGIN);
		modelAndView.addObject("selectedTab" , "Login");
		return modelAndView;
	}
	
	@RequestMapping(value="/accessDenied.admin", method=RequestMethod.GET)
	public ModelAndView viewAccessDenied(HttpServletRequest request) {
		return this.getModelAndView(request, SECURITY_ACCESS_DENIED);
	}

	@RequestMapping(value="/publicMoreThanOneSessionForTheSameUser.admin")
	public ModelAndView moreThanOneSessionForTheSameUser(HttpServletRequest request) {
		return this.getModelAndView(request, SECURITY_SESSION_EXPIRED);
	}

	@RequestMapping(value="/publicTerms.admin", method=RequestMethod.GET)
	public ModelAndView viewTerms(HttpServletRequest request) {
		Term siteTerm = this.getService().getTerm(this.clientName);
		ModelAndView modelAndView = this.getModelAndView(request, SECURITY_VIEW_USER_TERMS);
		modelAndView.addObject("terms" , siteTerm.getDescription());
		return modelAndView;
	}
}