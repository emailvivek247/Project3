package com.fdt.ecomadmin.ui.validator;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.fdt.ecomadmin.ui.form.CreditCardForm;

@Component("creditCardFormValidator")
public class CreditCardFormValidator implements Validator {

    @Override
    public boolean supports(Class aClass) {
        return CreditCardForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CreditCardForm form = (CreditCardForm) target;

        if(form.getNumber() != null) {
            if(!form.getNumber().trim().isEmpty()){
                Matcher checkForNumber = Pattern.compile("^[4563][0-9]{12,15}$").matcher(String.valueOf(form.getNumber().trim()));
                if (!checkForNumber.matches()) {
                    errors.rejectValue("number", "security.invalid.cardNumberFormat");
                    return;
                }
            } else {
                errors.rejectValue("number", "security.notnull.number");
                return;
            }
        } else {
            errors.rejectValue("number", "security.notnull.number");
                return;
        }

        if(form.getExpMonth() != null && form.getExpMonth().intValue() == -1) {
            errors.rejectValue("expMonthS", "security.invalid.expMonth");
            return;
        }

        if(form.getExpMonthS()== null || form.getExpMonthS() == ""){
            errors.rejectValue("expMonthS", "security.invalid.expMonthS");
            return;
        }

        int expiryMonth = form.getExpMonth();
        int expiryYear = form.getExpYear();

        int currentMonth =  Calendar.getInstance().get(Calendar.MONTH) + 1;
        int currentYear =  Calendar.getInstance().get(Calendar.YEAR);




        if(form.getExpMonthS()== null || form.getExpMonthS() == ""){
            errors.rejectValue("expMonthS", "security.invalid.expMonthS");
        }

        if (expiryYear > currentYear + 10) {
            errors.rejectValue("expYear", "security.invalid.expYear");
        }
        if (expiryYear < currentYear) {
            errors.rejectValue("expYear", "security.invalid.expYear");
        } else if (expiryYear == currentYear) {
            if (expiryMonth < currentMonth) {
                errors.rejectValue("expMonthS", "security.invalid.expMonthS");
                return;
            }
        }
        Matcher amexMatcher = Pattern.compile("^3[47][0-9]{13}$").matcher(String.valueOf(form.getNumber()));
        if (amexMatcher.matches()) {
            if (String.valueOf(form.getCvv()).length() != 4) {
                errors.rejectValue("cvv", "security.invalid.cvv");
            }
        } else {
            if (String.valueOf(form.getCvv()).length() != 3) {
                errors.rejectValue("cvv", "security.invalid.cvv");
            }
        }
    }
}
