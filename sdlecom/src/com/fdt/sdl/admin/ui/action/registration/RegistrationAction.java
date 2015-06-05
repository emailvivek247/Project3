package com.fdt.sdl.admin.ui.action.registration;

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

import static com.fdt.sdl.admin.ui.action.constants.ActionMessageKeys.REGISTRATION_SUCESS;
import static com.fdt.sdl.admin.ui.util.RegistrationUtil.changeAdminUserPassword;
import static com.fdt.sdl.admin.ui.util.RegistrationUtil.isExistingAdminUserEmpty;
import static com.fdt.sdl.admin.ui.util.RegistrationUtil.isExistingPasswordEmpty;
import static com.fdt.sdl.admin.ui.util.RegistrationUtil.validateUserNamePassword;

public class RegistrationAction extends Action {

	private static Logger logger = LoggerFactory.getLogger(RegistrationAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		String FORWARD_NAME = "success";
		boolean existingPassword = isExistingPasswordEmpty();
		boolean existingAdminUser = isExistingAdminUserEmpty();
		if (existingPassword && existingAdminUser) {
			WebserverStatic.setURIFile(request);
			ActionMessages errors = new ActionMessages();
			ActionMessages messages = new ActionMessages();
			String adminUserName = request.getParameter("adminusername");
			String password = request.getParameter("password");
			String confirmPassword = request.getParameter("confirmpassword");
			
			if (validateUserNamePassword(adminUserName, password, confirmPassword, errors)) {
				String errorMessageKey = changeAdminUserPassword(request, adminUserName, password);
				logger.debug("Change Admin User Name and Password" + errorMessageKey);
				if (errorMessageKey == null) {
					messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(REGISTRATION_SUCESS));
				}
			} else {
				FORWARD_NAME = "continue";
			}
			saveErrors(request, errors);
			saveMessages(request, messages);
		}
		return mapping.findForward(FORWARD_NAME);
	}
}
