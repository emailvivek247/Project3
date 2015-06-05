package com.fdt.sdl.admin.ui.action.registration;

import static com.fdt.sdl.admin.ui.action.constants.ActionErrorKeys.REGISTRATION_LOGIN_PASSWORD_INVALID;
import static com.fdt.sdl.admin.ui.action.constants.ActionErrorKeys.REGISTRATION_LOGIN_USER_NAME_INVALID;
import static com.fdt.sdl.admin.ui.util.RegistrationUtil.isExistingPasswordEmpty;
import static com.fdt.sdl.admin.ui.util.RegistrationUtil.setCookie;
import static com.fdt.sdl.admin.ui.util.RegistrationUtil.validCookie;
import static com.fdt.sdl.admin.ui.util.RegistrationUtil.validatePassword;
import static com.fdt.sdl.admin.ui.util.RegistrationUtil.validateUser;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.fdt.sdl.util.SecurityUtil;

/**
 *  Login action
 */
public class LoginAction extends Action {
	
    private static Logger logger = LoggerFactory.getLogger(LoginAction.class);

    /**
     * Process the search request.
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();

        HttpSession session = request.getSession();
        String operation = request.getParameter("operation");

        try {
            if (isExistingPasswordEmpty()) return (mapping.findForward("emptyPass"));

            // if user click login
            if (operation != null && "login".equals(operation)) {
                String user = request.getParameter("username");
                String password = request.getParameter("password");
                String remeberme = request.getParameter("remeberme");
                if (validateUser(request, user)) {
                    if (validatePassword(request, password)) {
                        if ("on".equals(remeberme)) {
                        	setCookie(request, response, password);
                        }
                        SecurityUtil.setAdminUser(request);
                        return mapping.findForward("success");
                    } else //invalid password
                    {
                        errors.add("error", new ActionMessage(REGISTRATION_LOGIN_PASSWORD_INVALID));
                        logger.debug("invalid password");
                    }
                } else //invalid User
                {
                    errors.add("error", new ActionMessage(REGISTRATION_LOGIN_USER_NAME_INVALID));
                    logger.debug("invalid User");
                }
            } else { //if already logged in
                Boolean admin = (Boolean) session.getAttribute("adminUser");
                if (admin == null && validCookie(request) || admin != null && admin.booleanValue()) {
                    session.setAttribute("adminUser", Boolean.TRUE);
                    return mapping.findForward("success");
                }
            }
        } catch (Exception e) {
            logger.error("Login exception", e);
            errors.add("error", new ActionMessage(e.getMessage()));
        }

        saveErrors(request, errors);
        saveMessages(request, messages);
        return (mapping.findForward("continue"));
    }

}
