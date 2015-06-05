package com.fdt.sdl.admin.ui.action.registration;

import static com.fdt.sdl.admin.ui.action.constants.ActionErrorKeys.REGISTRATION_LOGIN_PASSWORD_OLD_INCORRECT;
import static com.fdt.sdl.admin.ui.action.constants.ActionErrorKeys.REGISTRATION_LOGIN_USER_NAME_OLD_INCORRECT;
import static com.fdt.sdl.admin.ui.util.RegistrationUtil.changeAdminUserPassword;
import static com.fdt.sdl.admin.ui.util.RegistrationUtil.validatePassword;
import static com.fdt.sdl.admin.ui.util.RegistrationUtil.validateUser;
import static com.fdt.sdl.admin.ui.util.RegistrationUtil.validateUserNamePassword;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.foundation.WebserverStatic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

/**
 * 
 */
public class ChangeUserNamePasswordAction extends Action {
	
    private static Logger logger = LoggerFactory.getLogger(ChangeUserNamePasswordAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        WebserverStatic.setURIFile(request);

        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
        String FORWARD_NAME = "continue";
        boolean validExistingUserNamePassword = true;
        
    	String oldAdminUserName = request.getParameter("oldAdminUserName");
    	String newAdminUserName = request.getParameter("newAdminUserName");
        String oldpassword = request.getParameter("oldpassword");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String action = request.getParameter("operation");
        
        if (action != null) {
	        if(!validatePassword(request, oldpassword)) {
	        	validExistingUserNamePassword = false;
	            logger.debug("Incorrect Old Password, please try agian");
	            errors.add("error", new ActionMessage(REGISTRATION_LOGIN_PASSWORD_OLD_INCORRECT));
	    	} 
	        if(!validateUser(request, oldAdminUserName)) {
	        	validExistingUserNamePassword = false;
	            logger.debug("Incorrect old Admin User Name, please try agian");
	            errors.add("error", new ActionMessage(REGISTRATION_LOGIN_USER_NAME_OLD_INCORRECT));
	    	} 
	        if(validExistingUserNamePassword && validateUserNamePassword(newAdminUserName, password, confirmPassword, errors)) {
	    		if (validateUserNamePassword(newAdminUserName, password, confirmPassword, errors)) {
	    			String errorMessageKey = changeAdminUserPassword(request, newAdminUserName, password);
	    			logger.debug("Change Admin User Name and Password" + errorMessageKey);
	    			if (errorMessageKey == null) {
	    				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("configuration.save.success"));
	    				FORWARD_NAME = "success";
	    			}
	    		}
	    	}
        }
    	saveErrors(request, errors);
    	saveMessages(request, messages);
    	return mapping.findForward(FORWARD_NAME);
    }
}
