package com.fdt.security.ui.controller;

import static com.fdt.common.ui.BaseViewConstants.COMMON_FORWARD_SUCCESS;
import static com.fdt.security.ui.SecurityViewConstants.SECURITY_FORWARD_SUCCESSFUL_UPDATION;
import static com.fdt.security.ui.SecurityViewConstants.SECURITY_RESET_PASSWORD;
import static com.fdt.security.ui.SecurityViewConstants.SECURITY_VIEW_CHANGE_PASSWORD;
import static com.fdt.security.ui.SecurityViewConstants.SECURITY_VIEW_RESET_PASSWORD_REQUEST;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.entity.ErrorCode;
import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.sdl.styledesigner.util.PageStyleUtil;
import com.fdt.security.entity.User;
import com.fdt.security.exception.BadPasswordException;
import com.fdt.security.exception.InvalidDataException;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.security.exception.UserNotActiveException;
import com.fdt.security.ui.form.ChangePasswordForm;
import com.fdt.security.ui.form.ResetPasswordForm;
import com.fdt.security.ui.form.UserForm;

@Controller
public class UserPasswordController extends AbstractBaseController {

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.setAllowedFields(new String[] {
				"username",
				"token",
				"password",
				"existingPassword",
				"confirmPassword",
				"resetPasswordRequest",
				"resetPassword",
				"changepassword"
		});
	}

	@RequestMapping(value="/publicViewResetPasswordRequest.admin", method=RequestMethod.GET)
	public String viewResetPasswordRequest(Model model){
		model.addAttribute("ResetPasswordRequestForm", new UserForm());
		return SECURITY_VIEW_RESET_PASSWORD_REQUEST;
	}

	@RequestMapping(value="/publicResetPasswordRequest.admin", method=RequestMethod.POST)
	public ModelAndView resetPasswordRequest(@ModelAttribute("ResetPasswordRequestForm") @Valid UserForm userForm,
			BindingResult bindingResult) {
		ModelAndView modelAndView = new ModelAndView(COMMON_FORWARD_SUCCESS);
		verifyBinding(bindingResult);
		if (bindingResult.hasErrors()) {
			String view = SECURITY_VIEW_RESET_PASSWORD_REQUEST;
			this.setModelAndViewForError(modelAndView, view);
			return modelAndView;
		}
		String successMsg = this.getMessage("security.authentication.resetPasswordRequestSuccess");
		try {
			this.getService().resetPasswordRequest(userForm.getUsername(), nodeName, this.ecomClientURL);
		} catch (UserNameNotFoundException userNameNotFoundException) {
			successMsg = userNameNotFoundException.getDescription();
			bindingResult.rejectValue("username", "security.notFound.resetPasswordforUsername", successMsg);
		} catch (UserNotActiveException userNotActiveException) {
			successMsg = userNotActiveException.getDescription();
			bindingResult.rejectValue("username", "security.notFound.resetPasswordforUsername", successMsg);
		}
		if (bindingResult.hasErrors()) {
			String view = SECURITY_VIEW_RESET_PASSWORD_REQUEST;
			this.setModelAndViewForError(modelAndView, view);
			return modelAndView;
		}
		modelAndView.addObject(SUCCESS_MSG, successMsg);
		return modelAndView;
	}

	@RequestMapping(value="/publicResetPassword.admin", method=RequestMethod.GET)
	public ModelAndView viewResetPassword(@RequestParam("token") String token, @RequestParam("userName") String username) {
		ModelAndView modelAndView = new ModelAndView(SECURITY_RESET_PASSWORD);
		try {
			this.getService().checkValidResetPasswordRequest(PageStyleUtil.decrypt(username), PageStyleUtil.decrypt(token));
		} catch (InvalidDataException invalidDataException) {
			modelAndView.addObject(FAILURE_MSG, invalidDataException.getDescription());
			return modelAndView;
		}
		ResetPasswordForm resetPasswordForm = new ResetPasswordForm();
		resetPasswordForm.setToken(PageStyleUtil.decrypt(token));
		resetPasswordForm.setUsername(PageStyleUtil.decrypt(username));
		modelAndView.addObject("ResetPasswordForm", resetPasswordForm);
		return modelAndView;
	}

	@RequestMapping(value="/publicSubmitResetPassword.admin", method=RequestMethod.POST)
	public ModelAndView resetPassword(@ModelAttribute("ResetPasswordForm") @Valid ResetPasswordForm resetPasswordForm,
			BindingResult bindingResult, HttpServletRequest request) {
		String successMsg = null;
		ModelAndView modelAndView = null;
		try {
			User user = null;
			modelAndView = new ModelAndView(COMMON_FORWARD_SUCCESS);
			verifyBinding(bindingResult);
			convertPasswordError(bindingResult);
			if (bindingResult.hasErrors()) {
				String view = SECURITY_RESET_PASSWORD;
				this.setModelAndViewForError(modelAndView, view);
				return modelAndView;
			}
			user = new User();
			user.setUsername(resetPasswordForm.getUsername());
			user.setPassword(resetPasswordForm.getPassword());
			successMsg = this.getMessage("security.authentication.resetPasswordSuccess");
			this.getService().resetPassword(user, resetPasswordForm.getToken());
			modelAndView.addObject(SUCCESS_MSG, successMsg);
		} catch (UserNameNotFoundException userNameNotFoundException) {
			modelAndView.addObject(FAILURE_MSG, successMsg);
		} catch (InvalidDataException invalidDataException) {
			successMsg = invalidDataException.getDescription();
			modelAndView.addObject(FAILURE_MSG, successMsg);
		}
		return modelAndView;
	}

	@RequestMapping(value="/viewChangePassword.admin", method=RequestMethod.GET)
	public ModelAndView viewChangePassword(ModelAndView modelAndView, HttpServletRequest request) {
		modelAndView = new ModelAndView(SECURITY_VIEW_CHANGE_PASSWORD);
		ChangePasswordForm changePasswordForm = new ChangePasswordForm();
		User user = this.getUser(request);
		modelAndView.addObject("user", user);
		modelAndView.addObject("ChangePasswordForm", changePasswordForm);
		return modelAndView;
	}

	@RequestMapping(value="/updatePersonalInformation.admin", produces="application/json")
	@ResponseBody
	public  Set<ErrorCode> updatePersonalInformation( HttpServletRequest request,
				@RequestParam("username") String username,
				@RequestParam("firstName") String firstName,
				@RequestParam("lastName") String lastName,
				@RequestParam("phoneNumber") String phoneNumber,
				@RequestParam("firmName") String firmName,
				@RequestParam("firmNumber") String firmNumber,
				@RequestParam("barNumber") String barNumber) {
		Set<ErrorCode> errors = new HashSet<ErrorCode>();
		String successMsg = null;
		User user = this.getUser(request);
		if (firstName != null && firstName !="" && lastName != null && lastName !="" &&
				phoneNumber != null && phoneNumber !="" && checkValidPhoneNumber(phoneNumber) && username != null && username !="") {
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setPhone(phoneNumber);
			user.setFirmName(firmName);
			user.setFirmNumber(firmNumber);
			user.setBarNumber(barNumber);
			try	{
				this.getService().updateUser(user, user.getUsername());
				ErrorCode error = new ErrorCode();
				error.setCode("SUCCESS");
				error.setDescription(this.getMessage("system.msg.success"));
				errors.add(error);
			} catch (UserNameNotFoundException userNameNotFoundException) {
				ErrorCode error = new ErrorCode();
				error.setCode("ERROR");
				error.setDescription(successMsg);
				errors.add(error);

			} catch (InvalidDataException invalidDataException) {
				ErrorCode error = new ErrorCode();
				error.setCode("ERROR");
				error.setDescription(invalidDataException.getDescription());
				errors.add(error);
			}
		} else {
			if (firstName == null || firstName =="") {
				ErrorCode error = new ErrorCode();
				error.setCode("firstName");
				error.setDescription(this.getMessage("system.msg.error"));
				errors.add(error);
			}
			if (lastName == null || lastName == "") {
				ErrorCode error = new ErrorCode();
				error.setCode("lastName");
				error.setDescription(this.getMessage("system.msg.error"));
				errors.add(error);
			}
			if (phoneNumber == null || phoneNumber == "" || !checkValidPhoneNumber(phoneNumber)) {
				ErrorCode error = new ErrorCode();
				error.setCode("phoneNumber");
				error.setDescription(this.getMessage("system.msg.error"));
				errors.add(error);
			}
			if (username == null || username == "") {
				ErrorCode error = new ErrorCode();
				error.setCode("firstName");
				error.setDescription(this.getMessage("system.msg.error"));
				errors.add(error);
			}
		}
		return errors;
	}

	@RequestMapping(value="/changePasswordSubmit.admin", method=RequestMethod.POST)
	public ModelAndView changePassword(@ModelAttribute("ChangePasswordForm") @Valid ChangePasswordForm changePasswordForm,
			BindingResult bindingResult, HttpServletRequest request) {
		User user = null;
		ModelAndView modelAndView = new ModelAndView(SECURITY_FORWARD_SUCCESSFUL_UPDATION);
		try {
			verifyBinding(bindingResult);
			convertPasswordError(bindingResult);
			if (bindingResult.hasErrors()) {
				String view = SECURITY_VIEW_CHANGE_PASSWORD;
				user = this.getUser(request);
				modelAndView.addObject("user", user);
				this.setModelAndViewForError(modelAndView, view);
				return modelAndView;
			}
			user = new User();
			user.setUsername(request.getRemoteUser());
			user.setPassword(changePasswordForm.getPassword());
			user.setExistingPassword(changePasswordForm.getExistingPassword());
			this.getService().changePassword(user);
		} catch (UserNameNotFoundException userNameNotFoundException) {
			bindingResult.rejectValue("username", "security.authentication.usernotfound");
		} catch (BadPasswordException badpasswordException) {
			bindingResult.rejectValue("existingPassword", "security.authentication.badcredentials");
		}
		user = this.getUser(request);
		modelAndView.addObject("user", user);
		if (bindingResult.hasErrors()) {
			String view = SECURITY_VIEW_CHANGE_PASSWORD;
			this.setModelAndViewForError(modelAndView, view);
			return modelAndView;
		}
		String successMsg = this.getMessage("security.authentication.passwordChangeSuccess");
		modelAndView.addObject(SUCCESS_MSG, successMsg);
		return modelAndView;
	}

	private static void convertPasswordError(BindingResult bindingResult) {
		for (ObjectError error : bindingResult.getGlobalErrors()) {
			String msg = error.getDefaultMessage();
			if ("security.notmatch.password".equals(msg)) {
				if (!bindingResult.hasFieldErrors("password")) {
					bindingResult.rejectValue("password", "security.notmatch.password");
				}
			}
		}
	}

	private boolean checkValidPhoneNumber (String phoneNumber) {
			try{
			   new BigInteger(phoneNumber);
			   if (phoneNumber.length() == 10) {
				   return true;
			   } else {
				   return false;
			   }
			}catch(Exception ex) {
			}
			return false;
	}

	private void setModelAndViewForError(ModelAndView modelAndView, String view) {
		modelAndView.setViewName(view);
	}
}
