package com.fdt.sdl.admin.ui.action.registration;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fdt.sdl.util.SecurityUtil;

import net.javacoding.xsearch.utility.HttpUtil;

/**
 * 
 *  
 */
public class LogoutAction extends Action {
	
    private static Logger logger  = LoggerFactory.getLogger(LogoutAction.class);

    private static String REMEBER_LOGIN = "SDLCOOKIELOGIN";

    /**
     * Process the search request.
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Cookie c = HttpUtil.getCookie(request, REMEBER_LOGIN);

        if (c != null) HttpUtil.deleteCookie(response, c, request.getContextPath());
        SecurityUtil.logoutUser(request);
        return (mapping.findForward("continue"));

    }
}
