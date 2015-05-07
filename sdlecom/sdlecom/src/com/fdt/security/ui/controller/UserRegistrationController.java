package com.fdt.security.ui.controller;

import static com.fdt.common.ui.BaseViewConstants.COMMON_FORWARD_SUCCESS;
import static com.fdt.common.ui.BaseViewConstants.COMMON_SUCCESS;
import static com.fdt.security.ui.SecurityViewConstants.SECURITY_SIGNUP;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.apache.commons.lang.StringUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.ecom.entity.Site;
import com.fdt.security.entity.Access;
import com.fdt.security.entity.User;
import com.fdt.security.exception.UserNameAlreadyExistsException;
import com.fdt.security.ui.form.UserForm;
import com.fdt.security.ui.form.UserRegistrationForm;
import com.fdt.subscriptions.dto.AccessDetailDTO;

@Controller
public class UserRegistrationController extends AbstractBaseController {

	@Autowired
	private ReCaptcha reCaptcha;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.setAllowedFields(new String[] {
				"username",
				"confirmUsername",
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
				"subscription",
				"recaptcha_challenge_field",
				"recaptcha_response_field",
				"phoneNumber",
				"firmName",
				"firmNumber",
				"barNumber"
		});
	}

	@RequestMapping(value = "/publicRegisterUser.admin", method = RequestMethod.POST)
	public ModelAndView registerUser(@ModelAttribute("RegistrationForm") @Valid UserRegistrationForm userRegistrationForm,
				BindingResult bindingResult, HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, COMMON_FORWARD_SUCCESS);
		try {
			userRegistrationForm.setPhoneNumber(userRegistrationForm.getPhoneNumber().replace("-", "").replace("(","").replace(")",""));
			verifyBinding(bindingResult);
			String remoteAddr = request.getRemoteAddr();
			String challenge = request.getParameter("recaptcha_challenge_field");
			String uresponse = request.getParameter("recaptcha_response_field");
			ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, uresponse);
			if (!reCaptchaResponse.isValid()) {
				bindingResult.rejectValue("recaptcha_response_field", "security.incorrect.captcha");
			}
			modelAndView.addObject("accessId" , request.getParameter("accessId"));
			if (userRegistrationForm.getAccessId() == null || userRegistrationForm.getAccessId() == "") {
				bindingResult.addError(new ObjectError("accessId", "security.noaccess.access"));
			}
			convertPasswordError(bindingResult);
			//Temporary fix to remove the trailing . at the end of email address
			if (userRegistrationForm.getUsername().endsWith(".")) {
				bindingResult.rejectValue("username", "security.invalidEmail.username");
			}
			
			if (bindingResult.hasErrors()) {
				setModelAndViewForError(modelAndView, request);
				return modelAndView;
			}
			this.registerUser(userRegistrationForm, request);
			List<AccessDetailDTO> accessDetailsList = this.getService().getAccessDetails(new Long(userRegistrationForm.getAccessId()));
			if (accessDetailsList != null && accessDetailsList.size() > 0 && accessDetailsList.get(0).isAuthorizationRequired()) {
				modelAndView.addObject("accessRequiresAuthorization" , accessDetailsList.get(0));
			}
			String successMsg = this.getMessage("security.authentication.success");
			modelAndView.addObject(SUCCESS_MSG , successMsg);
			modelAndView.addObject("selectedTab" , "Registration");
		} catch (UserNameAlreadyExistsException userNameAlreadyExistsException) {
			bindingResult.rejectValue("username", "security.notUnique.username", userNameAlreadyExistsException.getDescription());
			setModelAndViewForError(modelAndView, request);
		}
		return modelAndView;
	}
	
	@RequestMapping(value="/publicGetSite.admin", method=RequestMethod.GET,  produces="application/json")
	@ResponseBody
	private Site getSite(@RequestParam String siteId){
		if(!StringUtils.isBlank(siteId)){
			List<Site> siteList = this.getService().getSitesForNode(this.nodeName);
			for(Site site : siteList){
				if(site.getId().toString().equals(siteId)){
					return site;
				}
			}
		}
		return null;
	}

	/*@RequestMapping(value = "/publicCaptcha.admin", method = RequestMethod.GET)
	public void getCapctcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
		byte[] captchaChallengeAsJpeg = null;
		ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
		String captchaId = request.getSession().getId();
		BufferedImage challenge = this.reCaptcha.getImageChallengeForID(captchaId, request.getLocale());
		ImageIO.write(challenge, "jpeg", jpegOutputStream);
		captchaChallengeAsJpeg = jpegOutputStream.toByteArray();
		*//** Setting the Response Headers for JCaptcha **//*
		response.setHeader("Cache-Control",	"no-store, no-cache, must-revalidate");
		response.addHeader("Cache-Control", "post-check=0, pre-check=0");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/jpeg");
		ServletOutputStream responseOutputStream = response.getOutputStream();
		responseOutputStream.write(captchaChallengeAsJpeg);
		responseOutputStream.flush();
		responseOutputStream.close();
	}*/

	@RequestMapping(value="/publicSignUp.admin", method=RequestMethod.GET)
	public ModelAndView viewSignup(HttpServletRequest request, Model model){
		ModelAndView modelAndView = this.getModelAndView(request, SECURITY_SIGNUP);
		UserForm userForm = new UserRegistrationForm();
		List<Site> siteList = this.getService().getSitesForNode(this.nodeName);
		String recaptchaHtml = reCaptcha.createRecaptchaHtml(null, "white", null);
		modelAndView.addObject("reCaptcha", recaptchaHtml);
		modelAndView.addObject("sites", siteList);
		modelAndView.addObject("RegistrationForm", userForm);
		modelAndView.addObject("selectedTab" , "Registration");
		return modelAndView;
	}

	@RequestMapping(value="/publicGetSubscriptions.admin", method=RequestMethod.GET,  produces="application/json")
	@ResponseBody
	public List<Access> getAccesForSite(@RequestParam String siteId) {
		List<Access> accessList = this.getService().getAccessesForSite(siteId);
		List<Access> accessListFilter = new LinkedList<Access>();
		for (Access access : accessList) {
			if (access.isVisible()) {
				accessListFilter.add(access);
			}
		}
		return accessListFilter;
	}

	@RequestMapping(value="/publicSuccess.admin")
	public String userCreated() {
		return COMMON_SUCCESS;
	}

	private static void convertPasswordError(BindingResult bindingResult) {
		for (ObjectError error : bindingResult.getGlobalErrors()) {
			String msg = error.getDefaultMessage();
			if ("security.notmatch.password".equals(msg)) {
				if (!bindingResult.hasFieldErrors("password")) {
					bindingResult.rejectValue("password", "security.notmatch.password");
				}
			} else if ("security.notmatch.username".equals(msg)) {
				if (!bindingResult.hasFieldErrors("username")) {
					bindingResult.rejectValue("username", "security.notmatch.username");
				}
			}  else if ("security.noaccess.access".equals(msg)) {
				if (!bindingResult.hasFieldErrors("accessId")) {
					bindingResult.rejectValue("accessId", "security.noaccess.access");
				}
			}
		}
	}

	private void registerUser(UserRegistrationForm userRegistrationForm, HttpServletRequest request)
			throws UserNameAlreadyExistsException {
		User user = new User();
		user.setUsername(userRegistrationForm.getUsername());
		user.setPassword(userRegistrationForm.getPassword());
		user.setFirstName(userRegistrationForm.getFirstName());
		user.setLastName(userRegistrationForm.getLastName());
		user.setModifiedBy(userRegistrationForm.getUsername());
		user.setCreatedBy(userRegistrationForm.getUsername());
		user.setCreatedIp(request.getRemoteHost());
		user.setPhone(userRegistrationForm.getPhoneNumber());
		user.setFirmName(userRegistrationForm.getFirmName());
		user.setFirmNumber(userRegistrationForm.getFirmNumber());
		user.setBarNumber(userRegistrationForm.getBarNumber());
		String userId = userRegistrationForm.getSiteId();
		String accessId = userRegistrationForm.getAccessId() ;
		this.getService().registerUser(user, new Long(userId), new Long(accessId), this.nodeName, this.ecomClientURL);
	}

	private void setModelAndViewForError(ModelAndView  modelAndView, HttpServletRequest request) {
		String recaptchaHtml = reCaptcha.createRecaptchaHtml("InCorrect Captcha", "white", null);
		modelAndView.addObject("reCaptcha", recaptchaHtml);
		String[] siteIds = request.getParameterValues("siteId");
		modelAndView.addObject("sites", this.getService().getSitesForNode(this.nodeName));
		modelAndView.addObject("selectedSites", siteIds);
		modelAndView.setViewName(SECURITY_SIGNUP);
	}
}