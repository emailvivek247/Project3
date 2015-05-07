package com.fdt.security.ui.controller;

import static com.fdt.security.ui.SecurityViewConstants.ECertify_VIEW_RETRIEVE_CERTIFIED_DOCUMENT;
import static com.fdt.security.ui.SecurityViewConstants.ECertify_VIEW_RETRIEVE_CERTIFIED_DOCUMENT_RENDER;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.tanesha.recaptcha.ReCaptchaResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.common.util.adapter.DocumentManagementSystemAdapter;
import com.fdt.common.util.adapter.SDLDMSDocument;
import com.fdt.sdl.styledesigner.util.PageStyleUtil;

@Controller
public class ECertifyController extends AbstractBaseController {
	
	
	@RequestMapping(value = "/publicRetrieveDocumentSubmit.admin", method = RequestMethod.POST)
	public ModelAndView publicRetrieveDocumentSubmit(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView modelAndView = this.getModelAndView(request, ECertify_VIEW_RETRIEVE_CERTIFIED_DOCUMENT);
		String certifiedDocumentNumber = request.getParameter("certifiedDocumentNumber");
		String pattern = "\\w{3}-\\w{4}-\\w{4}-\\w{4}";
		String errorMessage = null;
		String remoteAddr = request.getRemoteAddr();
		String challenge = request.getParameter("recaptcha_challenge_field");
		String uresponse = request.getParameter("recaptcha_response_field");
		ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, uresponse);
		if (!reCaptchaResponse.isValid()) {
			String recaptchaHtml = reCaptcha.createRecaptchaHtml("InCorrect Captcha", "white", null);
			modelAndView.addObject("reCaptcha", recaptchaHtml);
			errorMessage = this.getMessage("security.incorrect.captcha");
			modelAndView.addObject("ECERTIFY_ERROR_MESSAGE", errorMessage);
			return modelAndView;
		}
		if(!StringUtils.isBlank(certifiedDocumentNumber) && PageStyleUtil.isMatch(certifiedDocumentNumber, pattern)) {
			String documentId = this.getService().getDocumentIdByCertifiedDocumentNumber(certifiedDocumentNumber, clientName);
			if(!StringUtils.isBlank(documentId) && !documentId.equals("-1")) {
				DocumentManagementSystemAdapter documentManagementSystemAdapter = new DocumentManagementSystemAdapter();
				SDLDMSDocument SDLDMSDocument = documentManagementSystemAdapter.getDocumentByDMSID(documentId);
				modelAndView = this.getModelAndView(request, ECertify_VIEW_RETRIEVE_CERTIFIED_DOCUMENT_RENDER);
				modelAndView.addObject("SDLDMSDocument", SDLDMSDocument);
				modelAndView.addObject("response", response);
			} else {
				String recaptchaHtml = reCaptcha.createRecaptchaHtml(null, "white", null);
				modelAndView.addObject("reCaptcha", recaptchaHtml);
				errorMessage = this.getMessage("portal.ecertify.expiredcertifieddocumentnumber");
				modelAndView.addObject("ECERTIFY_ERROR_MESSAGE", errorMessage);
			}
		} else {
			String recaptchaHtml = reCaptcha.createRecaptchaHtml(null, "white", null);
			modelAndView.addObject("reCaptcha", recaptchaHtml);
			errorMessage = this.getMessage("portal.ecertify.invalidcertifieddocumentnumber");
			modelAndView.addObject("ECERTIFY_ERROR_MESSAGE", errorMessage);
		}
		return modelAndView;
	}
	
	
	@RequestMapping(value="/publicRetrieveDocument.admin")
	public ModelAndView publicRetrieveDocument(HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, ECertify_VIEW_RETRIEVE_CERTIFIED_DOCUMENT);
		String recaptchaHtml = reCaptcha.createRecaptchaHtml(null, "white", null);
		modelAndView.addObject("reCaptcha", recaptchaHtml);
		return modelAndView;
	}
	
}