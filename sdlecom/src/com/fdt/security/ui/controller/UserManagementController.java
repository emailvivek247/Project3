package com.fdt.security.ui.controller;

import static com.fdt.common.ui.BaseViewConstants.COMMON_FORWARD_SUCCESS;
import static com.fdt.security.ui.SecurityViewConstants.SECURITY_VIEW_RESEND_USER_ACTIVATION_EMAIL;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.sdl.styledesigner.util.PageStyleUtil;
import com.fdt.security.exception.InvalidDataException;
import com.fdt.security.exception.UserAlreadyActivatedException;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.security.ui.form.UserForm;

@Controller
public class UserManagementController extends AbstractBaseController {

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.setAllowedFields(new String[] {
				"username",
				"resendUserActivation"
		});
	}

	@RequestMapping(value="/publicActivateUser.admin", method=RequestMethod.GET)
	public ModelAndView activateUser(@RequestParam("token") String requestToken, @RequestParam("userName") String userName){
		ModelAndView modelAndView = new ModelAndView(COMMON_FORWARD_SUCCESS);
		String message = this.getMessage("security.authentication.activationSuccess");
		try {
			String userNameDecrypt = PageStyleUtil.decrypt(userName);
			String requestTokenDecrypt = PageStyleUtil.decrypt(requestToken);
			if (userNameDecrypt != null && requestTokenDecrypt != null) {
				this.getService().activateUser(userNameDecrypt, requestTokenDecrypt);
			} else {
				modelAndView.addObject(SUCCESS_MSG, "Invalid Activation Link");
			}
		} catch (InvalidDataException invalidDataException) {
			message = invalidDataException.getDescription();
		} catch (UserAlreadyActivatedException userAlreadyActivatedException) {
			message = userAlreadyActivatedException.getDescription();
		} catch (Exception exception) {
			modelAndView.addObject(SUCCESS_MSG, "Invalid Activation Link");
		}
		modelAndView.addObject(SUCCESS_MSG, message);
		return modelAndView;
	}

	@RequestMapping(value="/publicViewResendActivation.admin", method=RequestMethod.GET)
	public String viewResendUserActivationEMail(Model model) {
		UserForm userForm =  new UserForm();
		model.addAttribute("UserForm", userForm);
		return SECURITY_VIEW_RESEND_USER_ACTIVATION_EMAIL;
	}

	@RequestMapping(value="/publicResendUserActivationEmail.admin", method=RequestMethod.POST)
	public ModelAndView resendUserActivationEMail(@ModelAttribute("UserForm") @Valid UserForm userForm,
			BindingResult bindingResult) {
		ModelAndView modelAndView = new ModelAndView(COMMON_FORWARD_SUCCESS);
		verifyBinding(bindingResult);
		if (bindingResult.hasErrors()) {
			String view = SECURITY_VIEW_RESEND_USER_ACTIVATION_EMAIL;
			this.setModelAndViewForError(modelAndView, view);
			return modelAndView;
		}
		String successMsg = this.getMessage("security.authentication.resendActivationSuccess");
		try {
			getService().resendUserActivationEmail(userForm.getUsername(), this.nodeName, this.ecomClientURL);
		} catch (UserAlreadyActivatedException userAlreadyActivated) {
			successMsg = userAlreadyActivated.getDescription();
		} catch (UserNameNotFoundException userNameNotFoundException) {
			successMsg = userNameNotFoundException.getDescription();
			bindingResult.rejectValue("username", "security.notFound.username", successMsg);
		}
		if (bindingResult.hasErrors()) {
			String view = SECURITY_VIEW_RESEND_USER_ACTIVATION_EMAIL;
			this.setModelAndViewForError(modelAndView, view);
			return modelAndView;
		}
		modelAndView.addObject(SUCCESS_MSG, successMsg);
		return modelAndView;
	}

	private void setModelAndViewForError(ModelAndView modelAndView, String view) {
		modelAndView.setViewName(view);
	}
}
