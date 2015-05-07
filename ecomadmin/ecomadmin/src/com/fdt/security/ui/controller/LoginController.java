package com.fdt.security.ui.controller;

import static com.fdt.ecomadmin.ui.controller.ViewConstants.ACCESS_DENIED;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.LOGIN;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.SESSION_EXPIRED;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.SIGNUP;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_USER_TERMS;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.ui.controller.AbstractBaseController;

@Controller
public class LoginController extends AbstractBaseController {

    @RequestMapping(value="/accessdenied.admin", method=RequestMethod.GET)
    public ModelAndView viewAccessDenied(HttpServletRequest request) {
        ModelAndView modelAndView = this.getModelAndView(request, ACCESS_DENIED);
        return modelAndView;
    }

    @RequestMapping(value="/")
    public ModelAndView welcome(HttpServletRequest request) {
        ModelAndView modelAndView = this.getModelAndView(request, LOGIN);
        return modelAndView;
    }

    @RequestMapping(value="/publiclogin.admin")
    public ModelAndView viewLogin(HttpServletRequest request) {
        ModelAndView modelAndView = this.getModelAndView(request, LOGIN);
        return modelAndView;
    }

    @RequestMapping(value="/publicsignUp.admin")
    public ModelAndView viewSignup(HttpServletRequest request) {
        ModelAndView modelAndView = this.getModelAndView(request, SIGNUP);
        return modelAndView;
    }

    @RequestMapping(value="/publicTerms.admin")
    public ModelAndView viewTerms(HttpServletRequest request) {
        ModelAndView modelAndView = this.getModelAndView(request, VIEW_USER_TERMS);
        return modelAndView;
    }

    @RequestMapping(value="/publicMoreThanOneSessionForTheSameUser.admin", method=RequestMethod.GET)
    public ModelAndView moreThanOneSessionForTheSameUser(HttpServletRequest request){
        ModelAndView modelAndView = this.getModelAndView(request, SESSION_EXPIRED);
        return modelAndView;
    }
}
