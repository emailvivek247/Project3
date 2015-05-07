package com.fdt.ecom.ui.controller;

import static com.fdt.ecom.ui.EcomViewConstants.ECOM_CC_INFO;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_VIEW_SECURITY_CODE_HELP;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.metadata.ConstraintDescriptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
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
import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.ui.form.CreditCardForm;
import com.fdt.ecom.ui.form.CreditCardForm.CreditCardGroup;
import com.fdt.ecom.ui.validator.CreditCardFormValidator;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;

@Controller
public class CreditCardController extends AbstractBaseController {

    @Autowired(required = true)
    private Validator validator;

    @Autowired
    @Qualifier(value="creditCardFormValidator")
    private CreditCardFormValidator creditCardValidator;

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
            "routingNumber",
            "emailId"
        });
    }

    @RequestMapping(value="/secure/viewAccountInformation.admin")
    public ModelAndView viewCreditCardInformation(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  @RequestParam(value = "token1") String userNameInParameter,
                                                  @RequestParam(value = "token3") String returnURL) {
        CreditCardForm creditCardForm = new CreditCardForm();
        request.getSession().setMaxInactiveInterval(Integer.parseInt(sessionTimeout));
        ModelAndView modelAndView = this.getModelAndView(request, ECOM_CC_INFO);
        String userName = this.verifyLoggedInUser(request, response);
        if(userName == null) {
            this.logoutUser(request, response);
            modelAndView.setViewName("redirect:/secure/viewAccountInformation.admin");
            modelAndView.addObject("token1", userNameInParameter);
            modelAndView.addObject("token3", returnURL);
            return modelAndView;
        }
        CreditCard creditCard = this.userService.getCreditCardDetails(userName);
        if (creditCard != null) {
            creditCardForm = this.buildCreditCardForm(creditCard);
        }
        creditCardForm.setEmailId(userName);
        modelAndView.addObject("creditCardForm", creditCardForm);
        modelAndView.addObject("returnUrl", returnURL);
        return modelAndView;
    }

    @RequestMapping(value="/secure/updateAccountInformation.admin", produces="application/json")
    @ResponseBody
    public Set<ErrorCode> updateCreditCardInfo(@ModelAttribute("creditCardForm")
                CreditCardForm creditCardForm, BindingResult bindingResult, HttpServletRequest request, HttpServletResponse response) {
        Set<ErrorCode> errors = new HashSet<ErrorCode>();

        String verificationResult = verifyBindingInJSON(bindingResult);
        if (verificationResult != null) {
            ErrorCode error = new ErrorCode();
            error.setCode("ERROR_DATA");
            error.setDescription(this.getMessage("system.invalid.data"));
            errors.add(error);
        }
        if (creditCardForm.getEmailId() == null || creditCardForm.getEmailId() == "") {
            ErrorCode error = new ErrorCode();
            error.setCode("ERROR_EMAIL");
            error.setDescription(this.getMessage("security.ecommerce.error.illegalOperation"));
            errors.add(error);
        }

        if (errors != null && errors.size() > 0) {
            return errors;
        }

        validate(creditCardForm, bindingResult, CreditCardGroup.class);
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

        CreditCard creditCardInfo = buildCreditCard(creditCardForm, request);
        try {
            this.userService.updateExistingCreditCardInformation(creditCardForm.getEmailId(), creditCardForm.getEmailId(),
            		creditCardInfo);
        } catch (PaymentGatewayUserException payPalUserException) {
            bindingResult.rejectValue("ERROR", "paypal.errorcode." + payPalUserException.getErrorCode());
        } catch (PaymentGatewaySystemException payPalSystemException) {
            bindingResult.rejectValue("ERROR", "paypal.errorcode.generalsystemerror");
        }
        if (bindingResult.hasErrors()) {
            return this.populateErrorCodes(bindingResult.getFieldErrors());
        }
        this.logoutUser(request, response);
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
                    } catch (Exception e) {
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

    @RequestMapping(value="/secure/publicsecuritycodehelp.admin", method=RequestMethod.GET)
    public ModelAndView viewSecurityCodeHelp(HttpServletRequest request) {
        ModelAndView modelAndView = this.getModelAndView(request, ECOM_VIEW_SECURITY_CODE_HELP);
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
