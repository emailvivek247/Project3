package com.fdt.security.ui.controller;

import static com.fdt.ecomadmin.ui.controller.ViewConstants.FORWARD_SUCCESS;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.SIGNUP;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.SUCCESS;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.ecom.entity.Site;
import com.fdt.security.entity.EComAdminSite;
import com.fdt.security.entity.EComAdminUser;
import com.fdt.security.exception.UserNameAlreadyExistsException;
import com.fdt.security.ui.form.UserForm;
import com.fdt.security.ui.form.UserRegistrationForm;

@Controller
public class UserRegistrationController extends AbstractBaseController {

	@Autowired
	private ReCaptcha reCaptcha;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.setAllowedFields(new String[] {
				"username",
				"password",
				"confirmPassword",
				"firstName",
				"lastName",
				"addressLine1",
				"captcha",
				"termsAccept",
				"signup",
				"siteId",
				"accessId",
				"recaptcha_challenge_field",
				"recaptcha_response_field",
				"subscription"
		});
	}

	@RequestMapping(value = "/publicregisteruser.admin", method = RequestMethod.POST)
	public ModelAndView registerUser(HttpServletRequest request,
									 @ModelAttribute("RegistrationForm") @Valid UserRegistrationForm userRegistrationForm,
									 BindingResult bindingResult) {
		ModelAndView modelAndView = this.getModelAndView(request, FORWARD_SUCCESS);
		try {
			verifyBinding(bindingResult);
			String remoteAddr = request.getRemoteAddr();
			String challenge = request.getParameter("recaptcha_challenge_field");
			String uresponse = request.getParameter("recaptcha_response_field");
			ReCaptchaResponse reCaptchaResponse = this.reCaptcha.checkAnswer(remoteAddr, challenge, uresponse);
			if (!reCaptchaResponse.isValid()) {
				bindingResult.rejectValue("recaptcha_response_field", "security.incorrect.captcha");
			}
			
			Pattern regex = Pattern.compile("[$&+,:;=?#|]");
			Matcher matcher = regex.matcher(userRegistrationForm.getUsername());
			if(matcher.find()){
				bindingResult.rejectValue("username", "security.invalidEmail.username");
			}
			
			convertPasswordError(bindingResult);
			if (bindingResult.hasErrors()) {
				setModelAndViewForError(modelAndView, request);
				return modelAndView;
			}
			//Temporary fix to remove the trailing . at the end of email address
			if (userRegistrationForm.getUsername().endsWith(".")) {
				userRegistrationForm.setUsername(userRegistrationForm.getUsername().substring(0,
					userRegistrationForm.getUsername().length() - 1));
			}
			this.registerUser(userRegistrationForm, request);
			String successMsg = this.getMessage("security.authentication.success");
			modelAndView.addObject(SUCCESS_MSG , successMsg);
		} catch (UserNameAlreadyExistsException e) {
			bindingResult.rejectValue("username", "security.notUnique.username");
			setModelAndViewForError(modelAndView, request);
		}
		return modelAndView;
	}

	@RequestMapping(value="/publicsignUp.admin", method=RequestMethod.GET)
	public String viewSignup(Model model){
		UserForm userForm = new UserRegistrationForm();
		List<Site> siteList = this.getServiceStub().getSites();
		String recaptchaHtml = this.reCaptcha.createRecaptchaHtml(null, "white", null);
		model.addAttribute("reCaptcha", recaptchaHtml);
		model.addAttribute("sites", siteList);
		model.addAttribute("RegistrationForm", userForm);
		return SIGNUP;
	}

	@RequestMapping(value="/publicsuccess.admin")
	public String userCreated() {
		return SUCCESS;
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

	private void registerUser(UserRegistrationForm userRegistrationForm, HttpServletRequest request)
			throws UserNameAlreadyExistsException {
		EComAdminUser user = new EComAdminUser();
		user.setUsername(userRegistrationForm.getUsername());
		user.setPassword(userRegistrationForm.getPassword());
		user.setFirstName(userRegistrationForm.getFirstName());
		user.setLastName(userRegistrationForm.getLastName());
		List<EComAdminSite> siteList = new LinkedList<EComAdminSite>();
		EComAdminSite site = new EComAdminSite();
		site.setId(Long.getLong(userRegistrationForm.getSiteId()));
		siteList.add(site);
		user.setSites(siteList);
		user.setModifiedBy(userRegistrationForm.getUsername());
		user.setCreatedBy(userRegistrationForm.getUsername());
		user.setCreatedIp(request.getRemoteHost());
		String userId = userRegistrationForm.getSiteId();
		this.eComAdminUserService.registerUser(user, new Long(userId));
	}

	private void setModelAndViewForError(ModelAndView  modelAndView,
										HttpServletRequest request) {
		String recaptchaHtml = this.reCaptcha.createRecaptchaHtml("InCorrect Captcha", "white", null);
		modelAndView.addObject("reCaptcha", recaptchaHtml);
		String[] siteIds = request.getParameterValues("sites");
		modelAndView.addObject("sites", this.getServiceStub().getSites());
		modelAndView.addObject("selectedSites", siteIds);
		modelAndView.setViewName(SIGNUP);
	}
}