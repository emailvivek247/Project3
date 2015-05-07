package com.fdt.ecom.ui.controller;

import static com.fdt.ecom.ui.EcomViewConstants.ECOM_CC_INFO;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_PAYMENT_CONFIRMATION;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_PAY_NOW;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_REDIRECT_PAYMENT_CONFIRMATION;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.metadata.ConstraintDescriptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.entity.ErrorCode;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.exception.AccessUnAuthorizedException;
import com.fdt.ecom.ui.form.CreditCardForm;
import com.fdt.ecom.ui.form.CreditCardForm.DefaultGroup;
import com.fdt.ecom.ui.validator.CreditCardFormValidator;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.security.entity.User;
import com.fdt.subscriptions.dto.SubscriptionDTO;

@Controller
public class CreditCardController extends AbstractBaseController {

	@Autowired(required = true)
	private Validator validator;

	@Autowired
	@Qualifier(value="creditCardFormValidator")
	private CreditCardFormValidator creditCardValidator;

	private static String NEW_CREDIT_CARD = "N";

	private static String UPDATE_CREDIT_CARD = "U";

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.setAllowedFields(new String[] {
			"useExistingAccount",
			"accountName",
			"number",
			"expMonthS",
			"expYear",
			"cvv",
			"addressLine1",
			"addressLine2",
			"city",
			"state",
			"zip",
			"phoneNumber",
			"login",
			"paynow",
			"reset",
			"ERROR",
			"SUCCESS",
			"btnPayNow",
			"bankAccountNumber",
			"routingNumber"
		});
	}

	@RequestMapping(value="/paymentInfo.admin")
	public ModelAndView getPaymentDetails(ModelAndView modelAndView, HttpServletRequest request) {
		modelAndView = this.getModelAndView(request, ECOM_PAY_NOW);
		//Call the Service to get the list of Subscriptions.
		List <Site> paidSubUnpaidList = this.getService().getPaidSubUnpaidByUser(request.getRemoteUser(), this.nodeName).getSites();
		User user = this.getUser(request);
		CreditCardForm creditCardForm =  new CreditCardForm();
		/** This Attribute is Removed in payForPendingPaidSubscriptions**/
		request.getSession().setAttribute("sites", paidSubUnpaidList);
		modelAndView.addObject("sites", paidSubUnpaidList);
		modelAndView.addObject("user", user);
		if (user.isCardAvailable()) {
			CreditCard creditCard = this.getService().getCreditCardDetails(user.getId());
			creditCardForm = this.buildCreditCardForm(creditCard);
		}
		modelAndView.addObject("creditCardForm", creditCardForm);
		modelAndView.addObject("serverUrl", this.ecomServerURL);
		return modelAndView;
	}

	@RequestMapping(value = "/payNow.admin", method = RequestMethod.POST)
	public ModelAndView payForPendingPaidSubscriptions(@ModelAttribute("creditCardForm") CreditCardForm creditCardForm,
				BindingResult bindingResult, HttpServletRequest request, ModelAndView modelAndView,
				@RequestParam(defaultValue="false") boolean isReAu) {
		CreditCard creditCard = null;
		modelAndView = this.getModelAndView(request, ECOM_REDIRECT_PAYMENT_CONFIRMATION);
		if (isReAu) {
			this.reAuthenticate(request);
		}
		this.verifyBinding(bindingResult);
		if(NEW_CREDIT_CARD.equalsIgnoreCase(creditCardForm.getUseExistingAccount())
				|| UPDATE_CREDIT_CARD.equalsIgnoreCase(creditCardForm.getUseExistingAccount())) {
			//Use New card.
			validate(creditCardForm, bindingResult, DefaultGroup.class); // Performing validations specified by annotations.
			if (bindingResult.hasErrors()) {
				modelAndView = setModelAndViewForError(modelAndView, request);
				return modelAndView;
			}
			creditCardValidator.validate(creditCardForm, bindingResult); // Performing custom validations.
			if (bindingResult.hasErrors()) {
				modelAndView = setModelAndViewForError(modelAndView, request);
				return modelAndView;
			}
			creditCard = this.buildCreditCard(creditCardForm,request);
		}
		List<PayPalDTO> payPalDtoList = null;
		String failureMsg = null;
		try {
			SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
			subscriptionDTO.setCreditCard(creditCard);
			User user = new User();
			user.setUsername(request.getRemoteUser());
			subscriptionDTO.setUser(user);
			subscriptionDTO.setNodeName(this.nodeName);
			subscriptionDTO.setMachineName(request.getRemoteAddr());
			payPalDtoList = this.getService().paySubscriptions(subscriptionDTO);
		} catch (AccessUnAuthorizedException accessUnAuthorizedException) {
			failureMsg = this.getMessage("security.ecommerce.accessUnAuthorized");
			request.getSession().setAttribute(FAILURE_MSG, failureMsg);
		} catch (SDLBusinessException sDLBusinessException) {
			request.getSession().setAttribute(FAILURE_MSG, sDLBusinessException.getBusinessMessage());
		}

		for(PayPalDTO payPalDTO : payPalDtoList) {
			if (!payPalDTO.isSucessful()) {
				if (payPalDTO.isSystemException()) {
					payPalDTO.setErrorDesc(this.getMessage("paypal.errorcode.generalsystemerror"));
				} else {
					payPalDTO.setErrorDesc(this.getMessage("paypal.errorcode." + payPalDTO.getErrorCode()));
				}
			}
		}
		this.reAuthenticate(request);
		/** This Attribute Was put in the Session in the Method getPaymentDetails**/
		request.getSession().removeAttribute("sites");
 		request.getSession().setAttribute("payments", payPalDtoList);
		return modelAndView;

	}

	@RequestMapping(value="/paymentConfirmation.admin")
	public ModelAndView viewPaymentConfirmation(ModelAndView modelAndView, HttpServletRequest request) {
		modelAndView = this.getModelAndView(request, ECOM_PAYMENT_CONFIRMATION);
        Authentication newAuthentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) newAuthentication.getPrincipal();
		List<PayPalDTO> payments = (List<PayPalDTO>)request.getSession().getAttribute("payments");
		modelAndView.addObject("user" , user);
		modelAndView.addObject("payments" , payments);
		modelAndView.addObject("PAYPALERROR", request.getSession().getAttribute("PAYPALERROR"));
		return modelAndView;
	}

	@RequestMapping(value="/ccinfo.admin")
	public String viewCreditCardInformation(Model model, HttpServletRequest request){
		request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
				SecurityContextHolder.getContext());
		CreditCardForm creditCardForm = new CreditCardForm();
		UsernamePasswordAuthenticationToken userPasswordAuthToken
			= (UsernamePasswordAuthenticationToken) request.getUserPrincipal();
		User user = (User)userPasswordAuthToken.getPrincipal();
		CreditCard creditCard = this.getService().getCreditCardDetails(user.getId());
		if (creditCard != null) {
			creditCardForm = this.buildCreditCardForm(creditCard);
		}

		model.addAttribute("user", user);
		model.addAttribute("creditCardForm", creditCardForm);
		return ECOM_CC_INFO;
	}

	@RequestMapping(value="/updateCreditCard.admin", produces="application/json")
	@ResponseBody
	public Set<ErrorCode> updateCreditCardInfo(@ModelAttribute("creditCardForm")
				CreditCardForm creditCardForm, BindingResult bindingResult, HttpServletRequest request) {
		Set<ErrorCode> errors = new HashSet<ErrorCode>();

		String verificationResult = verifyBindingInJSON(bindingResult);
		if (verificationResult != null) {
			ErrorCode error = new ErrorCode();
			error.setCode("ERROR_DATA");
			error.setDescription(this.getMessage("system.invalid.data"));
			errors.add(error);
			return errors;
		}

		validate(creditCardForm, bindingResult, DefaultGroup.class);
		if (bindingResult.hasErrors()) {
			errors = this.populateErrorCodes(bindingResult.getFieldErrors());
		}

		creditCardValidator.validate(creditCardForm, bindingResult);
		if (bindingResult.hasErrors()) {
			errors.addAll(this.populateErrorCodes(bindingResult.getFieldErrors()));
		}

		if (errors != null && errors.size() > 0) {
			return errors;
		}

		String username= request.getRemoteUser();
		CreditCard creditCardInfo = buildCreditCard(creditCardForm, request);
		try {
			this.getService().updateExistingCreditCardInformation(username, username, creditCardInfo);
		} catch (PaymentGatewayUserException payPalUserException) {
			bindingResult.rejectValue("ERROR", "paypal.errorcode." + payPalUserException.getErrorCode());
		} catch (PaymentGatewaySystemException payPalSystemException) {
			bindingResult.rejectValue("ERROR", "paypal.errorcode.generalsystemerror");
		}
		if (bindingResult.hasErrors()) {
			return this.populateErrorCodes(bindingResult.getFieldErrors());
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = (User) auth.getPrincipal();
		user.setCardAvailable(true);
		user.setCreditCardActive(true);
		user.setCreditCard(creditCardInfo);
		String cardNumber  = user.getCreditCard().getNumber();
		user.getCreditCard().setNumber(cardNumber.substring(cardNumber.length() - 4, cardNumber.length()));

		Authentication newAuthentication = new UsernamePasswordAuthenticationToken(user,
				auth.getCredentials(),auth.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(newAuthentication);
		ErrorCode errorCode = new ErrorCode();
		errorCode.setCode("SUCCESS");
		errorCode.setDescription(this.getMessage("ecom.creditcard.updatesuccess"));
		errors.add(errorCode);
		return errors;
	}

	private Set<ErrorCode> populateErrorCodes(List<FieldError> fieldErrors) {
		Set<ErrorCode> errors = new HashSet<ErrorCode>();
		for (FieldError fieldError : fieldErrors) {
			if (fieldError != null) {
				ErrorCode errorCode = new ErrorCode();
				errorCode.setCode(fieldError.getField());
				String description;
				if (fieldError.getDefaultMessage() == null) {
					description = this.getMessage(fieldError.getCode());
				} else if(fieldError.getCode() != null){
					try {
						description = this.getMessage(fieldError.getCode());
					}
					catch (Exception e) {
						description = fieldError.getDefaultMessage();
					}
				} else {
						description = fieldError.getDefaultMessage();
				}
				errorCode.setDescription(description);
				errors.add(errorCode);
			}
		}
		return errors;
	}

	private ModelAndView setModelAndViewForError(ModelAndView modelAndView, HttpServletRequest request) {
		List<Site> siteList = null;
		UsernamePasswordAuthenticationToken userPasswordAuthToken
			= (UsernamePasswordAuthenticationToken)request.getUserPrincipal();
		User user = (User)userPasswordAuthToken.getPrincipal();
		if (request.getSession().getAttribute("sites") != null) {
			siteList =  (List<Site>)request.getSession().getAttribute("sites");
		} else {
			siteList = this.getService().getPaidSubUnpaidByUser(request.getRemoteUser(), this.nodeName).getSites();
		}
		modelAndView.addObject("sites", siteList);
		modelAndView.addObject("user", user);
		modelAndView.setViewName(ECOM_PAY_NOW);
		return modelAndView;
	}

	//For doing validations specified by annotations -- Used instead of @Valid.
	private void validate(Object target, Errors errors, Class<?>... groups) {
		Set<ConstraintViolation<Object>> result = validator.validate(target, groups);
		for (ConstraintViolation<Object> violation : result) {
			String field = violation.getPropertyPath().toString();
			FieldError fieldError = errors.getFieldError(field);
			if (fieldError == null || !fieldError.isBindingFailure()) {
				ConstraintDescriptor<?> constraintDescriptor = violation.getConstraintDescriptor();
				errors.rejectValue(field, violation.getMessageTemplate(),
						getArgumentsForConstraint(errors.getObjectName(),
								field, constraintDescriptor), this.getMessage(violation.getMessage()));
			}
		}
	}

	// Supporting function of validate.
	private Object[] getArgumentsForConstraint(String objectName, String field, ConstraintDescriptor<?> descriptor) {
		List<Object> arguments = new LinkedList<Object>();
		String[] codes = new String[] { objectName + Errors.NESTED_PATH_SEPARATOR + field, field };
		arguments.add(new DefaultMessageSourceResolvable(codes, field));
		Map<String, Object> attributesToExpose = new TreeMap<String, Object>();
		for (Map.Entry<String, Object> entry : descriptor.getAttributes().entrySet()) {
			String attributeName = entry.getKey();
			Object attributeValue = entry.getValue();
			if ("message".equals(attributeName) || "groups".equals(attributeName) || "payload".equals(attributeName)) {
				attributesToExpose.put(attributeName, attributeValue);
			}
		}
		arguments.addAll(attributesToExpose.values());
		return arguments.toArray(new Object[arguments.size()]);
	}

	private CreditCard buildCreditCard(CreditCardForm creditCardForm, HttpServletRequest request) {
		CreditCard creditCard = new CreditCard();
		creditCard.setName(creditCardForm.getAccountName());
		creditCard.setNumber(creditCardForm.getNumber());
		creditCard.setExpiryMonth(creditCardForm.getExpMonth());
		creditCard.setExpiryYear(creditCardForm.getExpYear());
		creditCard.setSecurityCode(creditCardForm.getCvv());
		creditCard.setAddressLine1(creditCardForm.getAddressLine1());
		creditCard.setAddressLine2(creditCardForm.getAddressLine2());
		creditCard.setCity(creditCardForm.getCity());
		creditCard.setState(creditCardForm.getState());
		creditCard.setZip(creditCardForm.getZip());
		creditCard.setPhone(creditCardForm.getPhoneNumber());
		creditCard.setModifiedBy(request.getRemoteUser());
		creditCard.setActive(true);
		return creditCard;
	}

	private CreditCardForm buildCreditCardForm(CreditCard creditCard) {
		CreditCardForm creditCardForm = new CreditCardForm();
		creditCardForm.setAccountName(creditCard.getName());
		creditCardForm.setNumber(creditCard.getNumber());
		creditCardForm.setAddressLine1(creditCard.getAddressLine1());
		creditCardForm.setAddressLine2(creditCard.getAddressLine2());
		creditCardForm.setCity(creditCard.getCity());
		creditCardForm.setState(creditCard.getState());
		creditCardForm.setZip(creditCard.getZip());
		if(creditCard.getExpiryMonth() < 10) {
			creditCardForm.setExpMonthS("0" + (creditCard.getExpiryMonth().toString()));
		}else {
			creditCardForm.setExpMonthS(creditCard.getExpiryMonth().toString());
		}
		creditCardForm.setExpYear(creditCard.getExpiryYear());
		creditCardForm.setPhoneNumber(creditCard.getPhone());
		return creditCardForm;
	}
}
