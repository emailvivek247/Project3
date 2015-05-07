package com.fdt.security.ui.controller;

import static com.fdt.security.ui.SecurityViewConstants.SECURITY_ACCESS_DENIED;
import static com.fdt.security.ui.SecurityViewConstants.SECURITY_LOGIN;
import static com.fdt.security.ui.SecurityViewConstants.MORE_THAN_ONE_SESSION_FOR_THE_SAME_USER;
import static com.fdt.security.ui.SecurityViewConstants.SESSION_EXPIRED;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.common.util.SystemUtil;

@Controller
public class LoginController extends AbstractBaseController {

    @RequestMapping(value="/publicLogin.admin")
    public ModelAndView viewLogin(HttpServletRequest request) {
        String userName = null;
        ModelAndView modelAndView = this.getModelAndView(request, SECURITY_LOGIN);
        DefaultSavedRequest defaultSavedRequest = (DefaultSavedRequest) request.getSession()
            .getAttribute("SPRING_SECURITY_SAVED_REQUEST");
        if(defaultSavedRequest != null) {
            String[] userNames = defaultSavedRequest.getParameterValues("token1");
            if(userNames!=null && userNames.length > 0) {
            	modelAndView.addObject("userName", SystemUtil.decrypt(userNames[0]));
            }
        } else {
            userName = request.getParameter("userName");
            modelAndView.addObject("userName", userName);
        }
        return modelAndView;
    }

    @RequestMapping(value="/accessDenied.admin", method=RequestMethod.GET)
    public ModelAndView viewAccessDenied(HttpServletRequest request) {
        return this.getModelAndView(request, SECURITY_ACCESS_DENIED);
    }

    @RequestMapping(value="/publicMoreThanOneSessionForTheSameUser.admin")
    public ModelAndView moreThanOneSessionForTheSameUser(HttpServletRequest request) {
        ModelAndView modelAndView = this.getModelAndView(request, MORE_THAN_ONE_SESSION_FOR_THE_SAME_USER);
        return modelAndView;
    }

    @RequestMapping(value="/sessionExpired.admin")
    public ModelAndView sessionExpired(HttpServletRequest request) {
        ModelAndView modelAndView = this.getModelAndView(request, SESSION_EXPIRED);
        return modelAndView;
    }

    @RequestMapping(value="/secure/KeepAlive.admin", produces="application/json")
    @ResponseBody
    public String updateCreditCardInfo() {
        return "OK";
    }
}
