package com.fdt.ecom.ui.controller;

import static com.fdt.ecom.ui.EcomViewConstants.ECOM_PAYMENT_CONFIRMATION;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_PAY_NOW;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_REDIRECT_PAYMENT_CONFIRMATION;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.exception.AccessUnAuthorizedException;
import com.fdt.ecom.ui.form.CreditCardSelectionForm;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.security.entity.User;
import com.fdt.subscriptions.dto.SubscriptionDTO;

@Controller
public class CreditCardController extends AbstractBaseController {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAllowedFields(new String[] {
            "selectedCardId"
        });
    }

    @RequestMapping(value = "/paymentInfo.admin")
    public ModelAndView getPaymentDetails(ModelAndView modelAndView, HttpServletRequest request) {
        modelAndView = this.getModelAndView(request, ECOM_PAY_NOW);
        // Call the Service to get the list of Subscriptions.
        List<Site> paidSubUnpaidList = getService().getPaidSubUnpaidByUser(request.getRemoteUser(), nodeName).getSites();
        User user = this.getUser(request);
        /** This Attribute is Removed in payForPendingPaidSubscriptions **/
        request.getSession().setAttribute("sites", paidSubUnpaidList);
        modelAndView.addObject("sites", paidSubUnpaidList);
        modelAndView.addObject("user", user);
        List<CreditCard> cardList = getService().getCreditCardDetailsList(user.getId());
        modelAndView.addObject("serverUrl", ecomServerURL);
        modelAndView.addObject("creditCardSelectionForm", new CreditCardSelectionForm(cardList));
        return modelAndView;
    }

    @RequestMapping(value = "/payNow.admin", method = RequestMethod.POST)
    public ModelAndView payForPendingPaidSubscriptions(
            @ModelAttribute("creditCardSelectionForm") CreditCardSelectionForm creditCardSelectionForm,
            BindingResult bindingResult, HttpServletRequest request, ModelAndView modelAndView,
            @RequestParam(defaultValue = "false") boolean isReAu) {

        modelAndView = this.getModelAndView(request, ECOM_REDIRECT_PAYMENT_CONFIRMATION);
        if (isReAu) {
            this.reAuthenticate(request);
        }
        User user = this.getUser(request);
        this.verifyBinding(bindingResult);

        Long selectedCardId = creditCardSelectionForm.getSelectedCardId();
        CreditCard creditCard = getService().getCreditCardDetailsWithId(user.getId(), selectedCardId);

        List<PayPalDTO> payPalDtoList = null;
        String failureMsg = null;
        try {
            SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
            subscriptionDTO.setCreditCard(creditCard);
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

        for (PayPalDTO payPalDTO : payPalDtoList) {
            if (!payPalDTO.isSucessful()) {
                if (payPalDTO.isSystemException()) {
                    payPalDTO.setErrorDesc(this.getMessage("paypal.errorcode.generalsystemerror"));
                } else {
                    payPalDTO.setErrorDesc(this.getMessage("paypal.errorcode." + payPalDTO.getErrorCode()));
                }
            }
        }

        this.reAuthenticate(request);

        // This Attribute Was put in the Session in the Method getPaymentDetails
        request.getSession().removeAttribute("sites");
        request.getSession().setAttribute("payments", payPalDtoList);
        return modelAndView;
    }

    @RequestMapping(value = "/paymentConfirmation.admin")
    public ModelAndView viewPaymentConfirmation(ModelAndView modelAndView, HttpServletRequest request) {

        modelAndView = this.getModelAndView(request, ECOM_PAYMENT_CONFIRMATION);
        Authentication newAuthentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) newAuthentication.getPrincipal();

        @SuppressWarnings("unchecked")
        List<PayPalDTO> payments = (List<PayPalDTO>) request.getSession().getAttribute("payments");

        modelAndView.addObject("user", user);
        modelAndView.addObject("payments", payments);
        modelAndView.addObject("PAYPALERROR", request.getSession().getAttribute("PAYPALERROR"));

        return modelAndView;
    }

}
