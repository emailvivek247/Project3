package com.fdt.ecom.ui.controller;

import static com.fdt.ecom.ui.EcomViewConstants.ECOM_AZ_EXTERNAL_PAYMENT_PROCESSING;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_AZ_PAYMENT_GATEWAY_CONFIRMATION;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_EXTERNAL_PAYMENT_PROCESSING;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_PAYASUGO_PAYMENT_CONFIRMATION;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_PAYASUGO_PAYMENT_INFO;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_PAYMENT_GATEWAY_CONFIRMATION;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_REDIRECT_PAYASUGO_PAYMENT_CONFIRMATION;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_REDIRECT_PAYMENT_GATEWAY;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_REVIEW_SHOPPING_SERVER_CART;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_SHOPPING_CART_PAYMENT_CONFIRMATION;

import java.security.Principal;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.metadata.ConstraintDescriptor;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.savedrequest.SavedRequest;
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
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.common.util.SystemUtil;
import com.fdt.ecom.entity.BankAccount;
import com.fdt.ecom.entity.Code;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.ShoppingCartItem;
import com.fdt.ecom.ui.form.CreditCardForm;
import com.fdt.ecom.ui.form.CreditCardForm.BankAccountGroup;
import com.fdt.ecom.ui.form.CreditCardForm.CreditCardGroup;
import com.fdt.ecom.ui.validator.CreditCardFormValidator;
import com.fdt.payasugotx.dto.PayAsUSubDTO;
import com.fdt.payasugotx.entity.PayAsUGoTx;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.security.entity.User;
import com.fdt.security.spring.SDLSavedRequestAwareAuthenticationSuccessHandler;
import com.fdt.webtx.dto.PaymentInfoDTO;
import com.fdt.webtx.dto.WebTransactionDTO;
import com.fdt.webtx.entity.WebTx;
import com.fdt.webtx.entity.WebTxItem;

@Controller
public class PaymentGatewayController extends AbstractBaseController {

    @Autowired
    @Qualifier("sDLSavedRequestAwareAuthenticationSuccessHandler")
    private SDLSavedRequestAwareAuthenticationSuccessHandler sDLSavedRequestAwareAuthenticationSuccessHandler = null;

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
            "bankAccountAddressLine1",
            "bankAccountAddressLine2",
            "bankAccountCity",
            "bankAccountState",
            "bankAccountZip",
            "bankAccountPhoneNumber",
            "saveCreditCard",
            "login",
            "paynow",
            "reset",
            "ERROR",
            "SUCCESS",
            "btnPayNow",
            "emailId",
            "returnUrl",
            "node_Name",
            "accessname",
            "numberofpages",
            "prdtype",
            "prdkey",
            "uniqueidentifier",            
            "payByMethod",
            "paymentMethodOne",
            "paymentMethodTwo",
            "paymentMethodThree",
            "bankRoutingNumber",
            "bankAccountNumber",
            "bankAccountName",
            "bankAccountType",
            "paymentInfoDTOId",
            "paymentMethodThreeNumber",
            "paymentMethodThreeExpMonthS",
            "paymentMethodThreeExpYear",
            "paymentMethodThreeCvv",
            "paymentMethodThreePhoneNumber",
            "paymentMethodThreeZip",
            "paymentMethodThreeAddressLine2",
            "paymentMethodThreeState",
            "paymentMethodThreeCity",
            "paymentMethodThreeAccountName",
            "paymentMethodThreeAddressLine1",
            "paymentMethodThreeIdentifier"
        });
    }

    private static String NEW_CREDIT_CARD = "N";

    private static String UPDATE_CREDIT_CARD = "U";

    public static String SHOPPING_CART = "SHOPPING_CART";

    private static String FIRM_CREDIT_CARD = "F";
    
    
    @RequestMapping(value="/publicGetPaymentInfoDTO.admin", method=RequestMethod.GET,  produces="application/json")
	@ResponseBody
	private PaymentInfoDTO publicGetPaymentInfoDTO(@RequestParam String paymentInfoDTOId){
		if(!StringUtils.isBlank(paymentInfoDTOId)){
			PaymentInfoDTO paymentInfoDTO = this.webTransactionService.getPaymentInfoByID(Long.valueOf(paymentInfoDTOId));
			String creditCardNumber = paymentInfoDTO.getCreditCardNumber();
			String maskedCreditCardNumber = creditCardNumber.substring(0, 2).concat("******").concat(creditCardNumber.substring(creditCardNumber.length()-4));
			paymentInfoDTO.setCreditCardNumber(maskedCreditCardNumber);
			return paymentInfoDTO;
		}
		return null;
	}

    /**
     * This method does the following
     * Interate through each new form element and identifies if a new item has to be created or the existing item has to be
     * updated.
     */
    @RequestMapping(value="/paymentgateway.admin")
    public ModelAndView viewWebPostItems(HttpServletRequest request) {
        ModelAndView modelAndView = getModelAndView(request, ECOM_EXTERNAL_PAYMENT_PROCESSING);
        ErrorCode error = new ErrorCode();
        HttpSession httpSession = request.getSession();
        String sessionId = httpSession.getId();
        String applicationName = request.getParameter("application_name");
        String siteName = request.getParameter("site_name");
        String imageUrl = request.getParameter("image_url");
        String footerUrl = request.getParameter("footer_url");
        String returnUrl = request.getParameter("return_url");
        String returnText = request.getParameter("return_text");
        String sendConfirmationEmail = request.getParameter("send_confirmation_email");
        String cancelUrl = request.getParameter("cancel_url");
        String cancelText = request.getParameter("cancel_text");
        String locName = request.getParameter("loc_name");
        String locAddrLine1 = request.getParameter("loc_addr_l1");
        String locAddrLine2 = request.getParameter("loc_addr_l2");
        String locCity = request.getParameter("loc_city");
        String locState = request.getParameter("loc_state");
        String locZip = request.getParameter("loc_zip");
        String locPhone = request.getParameter("loc_phone");
        String locComments1 = request.getParameter("loc_comments1");
        String locComments2 = request.getParameter("loc_comments2");
        String invoiceId = request.getParameter("invoiceId");
        String isAuthorizeTransaction = request.getParameter("isAuthorizeTransaction");

        if(StringUtils.isBlank(returnUrl)) {
        	returnUrl = httpSession.getAttribute("return_url_" + sessionId).toString();
        	modelAndView.addObject("return_url", returnUrl);

        }

        if(StringUtils.isBlank(returnText)) {
        	returnUrl = httpSession.getAttribute("return_text_" + sessionId).toString();
        	modelAndView.addObject("return_text", returnText);
        }

        if (!StringUtils.isBlank(applicationName) && !StringUtils.isBlank(siteName)
        		&& !StringUtils.isBlank(imageUrl)  && !StringUtils.isBlank(returnUrl)  && !StringUtils.isBlank(returnText)
        		&& !StringUtils.isBlank(sendConfirmationEmail) && !StringUtils.isBlank(cancelUrl)
        		&& !StringUtils.isBlank(cancelText) && !StringUtils.isBlank(locName)
        		&& !StringUtils.isBlank(locAddrLine1) && !StringUtils.isBlank(locCity)
        		&& !StringUtils.isBlank(locState) && !StringUtils.isBlank(locZip)
        		&& !StringUtils.isBlank(locPhone)) {
            Enumeration<String> paramNames = request.getParameterNames();
            Map<String, WebTxItem> items = new HashMap<String,WebTxItem>();
            List<Code> codes = this.eComService.getCodes("REGISTERED_APPLICATION");
            boolean isValidApplication = false;
            for (Code code : codes) {
                if (code.getCode().equals(applicationName)) {
                    isValidApplication = true;
                }
            }
            if (isValidApplication) {
                if (applicationName != null && applicationName.trim() !=""
                		&& siteName!= null && siteName.trim() !="") {
                    while(paramNames.hasMoreElements())
                    {
                          String paramName = (String)paramNames.nextElement();
                          if (paramName.contains("item_name_")) {
                              if (items.containsKey(paramName.replace("item_name_", ""))) {
                                items.get(paramName.replace("item_name_", ""))
                                .setItemName(request.getParameter(paramName));
                              } else {
                                  WebTxItem newItem = new WebTxItem();
                                  newItem.setItemName(request.getParameter(paramName));
                                  items.put(paramName.replace("item_name_", ""), newItem);
                              }
                          } else if (paramName.contains("item_type_")) {
                              if (items.containsKey(paramName.replace("item_type_", ""))) {
                                items.get(paramName.replace("item_type_", ""))
                                .setProductType(request.getParameter(paramName));
                              } else {
                                  WebTxItem newItem = new WebTxItem();
                                  newItem.setProductType(request.getParameter(paramName));
                                  items.put(paramName.replace("item_type_", ""), newItem);
                              }
                          } else if (paramName.contains("item_id_")) {
                              if (items.containsKey(paramName.replace("item_id_", ""))) {
                                items.get(paramName.replace("item_id_", "")).setProductId(request.getParameter(paramName));
                              } else {
                                  WebTxItem newItem = new WebTxItem();
                                  newItem.setProductId(request.getParameter(paramName));
                                  items.put(paramName.replace("item_id_", ""), newItem);
                              }
                          } else if (paramName.contains("item_quantity_")) {
                              if (items.containsKey(paramName.replace("item_quantity_", ""))) {
                                items.get(paramName.replace("item_quantity_", ""))
                                .setItemQuantity(Long.parseLong(request.getParameter(paramName)));
                              } else {
                                  WebTxItem newItem = new WebTxItem();
                                  newItem.setItemQuantity(Long.parseLong(request.getParameter(paramName)));
                                  items.put(paramName.replace("item_quantity_", ""), newItem);
                              }
                          } else if (paramName.contains("item_case_number_")) {
                              if (items.containsKey(paramName.replace("item_case_number_", ""))) {
                                items.get(paramName.replace("item_case_number_", ""))
                                .setCaseNumber(request.getParameter(paramName));
                              } else {
                                  WebTxItem newItem = new WebTxItem();
                                  newItem.setCaseNumber(request.getParameter(paramName));
                                  items.put(paramName.replace("item_case_number_", ""), newItem);
                              }
                          } else if (paramName.contains("item_party_role_")) {
                              if (items.containsKey(paramName.replace("item_party_role_", ""))) {
                                items.get(paramName.replace("item_party_role_", ""))
                                .setPartyRole(request.getParameter(paramName));
                              } else {
                                  WebTxItem newItem = new WebTxItem();
                                  newItem.setPartyRole(request.getParameter(paramName));
                                  items.put(paramName.replace("item_party_role_", ""), newItem);
                              }
                          } else if (paramName.contains("item_party_seq_")) {
                              if (items.containsKey(paramName.replace("item_party_seq_", ""))) {
                                items.get(paramName.replace("item_party_seq_", ""))
                                .setPartySeq(request.getParameter(paramName));
                              } else {
                                  WebTxItem newItem = new WebTxItem();
                                  newItem.setPartySeq(request.getParameter(paramName));
                                  items.put(paramName.replace("item_party_seq_", ""), newItem);
                              }
                          } else if (paramName.contains("item_amount_")) {
                              if (items.containsKey(paramName.replace("item_amount_", ""))) {
                                items.get(paramName.replace("item_amount_", ""))
                                .setBaseAmount(Double.parseDouble(request.getParameter(paramName).replace("$", "")));
                              } else {
                                  WebTxItem newItem = new WebTxItem();
                                  newItem.setBaseAmount(Double.parseDouble(request.getParameter(paramName).replace("$", "")));
                                  items.put(paramName.replace("item_amount_", ""), newItem);
                              }
                          } else if (paramName.contains("item_tax_")) {
                              if (items.containsKey(paramName.replace("item_tax_", ""))) {
                                items.get(paramName.replace("item_tax_", ""))
                                .setTax(Double.parseDouble(request.getParameter(paramName).replace("$", "")));
                              } else {
                                  WebTxItem newItem = new WebTxItem();
                                  newItem.setTax(Double.parseDouble(request.getParameter(paramName).replace("$", "")));
                                  items.put(paramName.replace("item_tax_", ""), newItem);
                              }
                          }

                    }
                    CreditCardForm creditCardForm =  new CreditCardForm();
                    modelAndView.addObject("creditCardForm", creditCardForm);
                    List<WebTxItem> itemList = new LinkedList<WebTxItem>(items.values());
                    Double amount = 0.0d;
                    Iterator<WebTxItem>  itemListIterator = itemList.iterator();
                    while(itemListIterator.hasNext()){
                        WebTxItem webTransactionItem = (WebTxItem)itemListIterator.next();
                        amount = amount + webTransactionItem.getBaseAmount();
                    }
                    try {
                        itemList = this.webTransactionService.doSaleGetInfoWEB(siteName, itemList);
                        modelAndView.addObject("ITEMS", itemList);
                    } catch (SDLBusinessException sDLBusinessException) {
                        modelAndView.addObject("ITEMS", null);
                    }
                    httpSession.setAttribute("image_url_" + sessionId, imageUrl);
                    httpSession.setAttribute("footer_url_" + sessionId, footerUrl);
                    httpSession.setAttribute("siteName_" + sessionId, siteName);
                    httpSession.setAttribute("invoiceId_" + sessionId, invoiceId);
                    httpSession.setAttribute("gatewayItems_" + sessionId, itemList);
                    httpSession.setAttribute("return_url_" + sessionId, returnUrl);
                    httpSession.setAttribute("return_text_" + sessionId, returnText);
                    httpSession.setAttribute("send_confirmation_email_" + sessionId,
                    		sendConfirmationEmail);
                    httpSession.setAttribute("application_name_" + sessionId,
                    		applicationName);
                    httpSession.setAttribute("cancel_url_" + sessionId, cancelUrl);
                    httpSession.setAttribute("cancel_text_" + sessionId, cancelText);
                    httpSession.setAttribute("loc_name" + sessionId, locName);
                    httpSession.setAttribute("loc_addr_l1" + sessionId, locAddrLine1);
                    httpSession.setAttribute("loc_addr_l2" + sessionId, locAddrLine2);
                    httpSession.setAttribute("loc_city" + sessionId, locCity);
                    httpSession.setAttribute("loc_state" + sessionId, locState);
                    httpSession.setAttribute("loc_zip" + sessionId, locZip);
                    httpSession.setAttribute("loc_phone" + sessionId, locPhone);
                    httpSession.setAttribute("loc_comments1" + sessionId, locComments1);
                    httpSession.setAttribute("loc_comments2" + sessionId, locComments2);
                    httpSession.setAttribute("isAuthorizeTransaction" + sessionId, isAuthorizeTransaction);
                    modelAndView.addObject("isAuthorizeTransaction", isAuthorizeTransaction);
                    modelAndView.addObject("image_url", httpSession.getAttribute("image_url_" + sessionId));
                    modelAndView.addObject("footer_url", httpSession.getAttribute("footer_url_" + sessionId));
                    modelAndView.addObject("cancel_url", cancelUrl);
                    modelAndView.addObject("cancel_text", cancelText);
                    modelAndView.addObject("send_confirmation_email", sendConfirmationEmail);
                    modelAndView.addObject("loc_name", locName);
                    modelAndView.addObject("loc_addr_l1", locAddrLine1);
                    modelAndView.addObject("loc_addr_l2", locAddrLine2);
                    modelAndView.addObject("loc_city", locCity);
                    modelAndView.addObject("loc_state", locState);
                    modelAndView.addObject("loc_zip", locZip);
                    modelAndView.addObject("loc_phone", locPhone);
                    modelAndView.addObject("loc_comments1", locComments1);
                    modelAndView.addObject("loc_comments2", locComments2);
                    modelAndView.addObject("invoiceId", invoiceId);
                }
            } else {
                error.setCode("ERROR");
                error.setDescription("Invalid Application Name or Application Is Not Registered With Gateway.");
                modelAndView.addObject("ERROR", error);
            }

        } else {
            error.setCode("ERROR");
            error.setDescription("Cannot Process Payment As Required Information Is Invalid. " +
            		"Please Contact The Administrator.");
            modelAndView.addObject("ERROR", error);
        }
        return modelAndView;

    }

    @RequestMapping(value="/paymentgatewayAZ.admin")
    public ModelAndView paymentgatewayAZ(HttpServletRequest request) {
        ModelAndView modelAndView = getModelAndView(request, ECOM_AZ_EXTERNAL_PAYMENT_PROCESSING);
        ErrorCode error = new ErrorCode();
        HttpSession httpSession = request.getSession();
        String sessionId = httpSession.getId();
        String applicationName = request.getParameter("application_name");
        String siteName = request.getParameter("site_name");
        String imageUrl = request.getParameter("image_url");
        String footerUrl = request.getParameter("footer_url");
        String returnUrl = request.getParameter("return_url");
        String returnText = request.getParameter("return_text");
        String sendConfirmationEmail = request.getParameter("send_confirmation_email");
        String cancelUrl = request.getParameter("cancel_url");
        String cancelText = request.getParameter("cancel_text");
        String locName = request.getParameter("loc_name");
        String locAddrLine1 = request.getParameter("loc_addr_l1");
        String locAddrLine2 = request.getParameter("loc_addr_l2");
        String locCity = request.getParameter("loc_city");
        String locState = request.getParameter("loc_state");
        String locZip = request.getParameter("loc_zip");
        String locPhone = request.getParameter("loc_phone");
        String locComments1 = request.getParameter("loc_comments1");
        String locComments2 = request.getParameter("loc_comments2");
        String invoiceId = request.getParameter("invoiceId");
        String paymentTokens = request.getParameter("paymentTokens");
        
        String isAuthorizeTransaction = request.getParameter("isAuthorizeTransaction");
        if(StringUtils.isBlank(returnUrl)) {
        	returnUrl = httpSession.getAttribute("return_url_" + sessionId).toString();
        	modelAndView.addObject("return_url", returnUrl);

        }
        if(StringUtils.isBlank(returnText)) {
        	returnUrl = httpSession.getAttribute("return_text_" + sessionId).toString();
        	modelAndView.addObject("return_text", returnText);
        }

        if (!StringUtils.isBlank(applicationName) && !StringUtils.isBlank(siteName)
        		&& !StringUtils.isBlank(imageUrl)  && !StringUtils.isBlank(returnUrl)  && !StringUtils.isBlank(returnText)
        		&& !StringUtils.isBlank(sendConfirmationEmail) && !StringUtils.isBlank(cancelUrl)
        		&& !StringUtils.isBlank(cancelText) && !StringUtils.isBlank(locName)
        		&& !StringUtils.isBlank(locAddrLine1) && !StringUtils.isBlank(locCity)
        		&& !StringUtils.isBlank(locState) && !StringUtils.isBlank(locZip)
        		&& !StringUtils.isBlank(locPhone)) {
            Enumeration<String> paramNames = request.getParameterNames();
            Map<String, WebTxItem> items = new HashMap<String,WebTxItem>();
            List<Code> codes = this.eComService.getCodes("REGISTERED_APPLICATION");
            boolean isValidApplication = false;
            for (Code code : codes) {
                if (code.getCode().equals(applicationName)) {
                    isValidApplication = true;
                }
            }
            if (isValidApplication) {
                if (applicationName != null && applicationName.trim() !=""
                		&& siteName!= null && siteName.trim() !="") {
                    while(paramNames.hasMoreElements())
                    {
                          String paramName = (String)paramNames.nextElement();
                          if (paramName.contains("item_name_")) {
                              if (items.containsKey(paramName.replace("item_name_", ""))) {
                                items.get(paramName.replace("item_name_", ""))
                                .setItemName(request.getParameter(paramName));
                              } else {
                                  WebTxItem newItem = new WebTxItem();
                                  newItem.setItemName(request.getParameter(paramName));
                                  items.put(paramName.replace("item_name_", ""), newItem);
                              }
                          } else if (paramName.contains("item_type_")) {
                              if (items.containsKey(paramName.replace("item_type_", ""))) {
                                items.get(paramName.replace("item_type_", ""))
                                .setProductType(request.getParameter(paramName));
                              } else {
                                  WebTxItem newItem = new WebTxItem();
                                  newItem.setProductType(request.getParameter(paramName));
                                  items.put(paramName.replace("item_type_", ""), newItem);
                              }
                          } else if (paramName.contains("item_id_")) {
                              if (items.containsKey(paramName.replace("item_id_", ""))) {
                                items.get(paramName.replace("item_id_", "")).setProductId(request.getParameter(paramName));
                              } else {
                                  WebTxItem newItem = new WebTxItem();
                                  newItem.setProductId(request.getParameter(paramName));
                                  items.put(paramName.replace("item_id_", ""), newItem);
                              }
                          } else if (paramName.contains("item_quantity_")) {
                              if (items.containsKey(paramName.replace("item_quantity_", ""))) {
                                items.get(paramName.replace("item_quantity_", ""))
                                .setItemQuantity(Long.parseLong(request.getParameter(paramName)));
                              } else {
                                  WebTxItem newItem = new WebTxItem();
                                  newItem.setItemQuantity(Long.parseLong(request.getParameter(paramName)));
                                  items.put(paramName.replace("item_quantity_", ""), newItem);
                              }
                          } else if (paramName.contains("item_case_number_")) {
                              if (items.containsKey(paramName.replace("item_case_number_", ""))) {
                                items.get(paramName.replace("item_case_number_", ""))
                                .setCaseNumber(request.getParameter(paramName));
                              } else {
                                  WebTxItem newItem = new WebTxItem();
                                  newItem.setCaseNumber(request.getParameter(paramName));
                                  items.put(paramName.replace("item_case_number_", ""), newItem);
                              }
                          } else if (paramName.contains("item_party_role_")) {
                              if (items.containsKey(paramName.replace("item_party_role_", ""))) {
                                items.get(paramName.replace("item_party_role_", ""))
                                .setPartyRole(request.getParameter(paramName));
                              } else {
                                  WebTxItem newItem = new WebTxItem();
                                  newItem.setPartyRole(request.getParameter(paramName));
                                  items.put(paramName.replace("item_party_role_", ""), newItem);
                              }
                          } else if (paramName.contains("item_party_seq_")) {
                              if (items.containsKey(paramName.replace("item_party_seq_", ""))) {
                                items.get(paramName.replace("item_party_seq_", ""))
                                .setPartySeq(request.getParameter(paramName));
                              } else {
                                  WebTxItem newItem = new WebTxItem();
                                  newItem.setPartySeq(request.getParameter(paramName));
                                  items.put(paramName.replace("item_party_seq_", ""), newItem);
                              }
                          } else if (paramName.contains("item_amount_")) {
                              if (items.containsKey(paramName.replace("item_amount_", ""))) {
                                items.get(paramName.replace("item_amount_", ""))
                                .setBaseAmount(Double.parseDouble(request.getParameter(paramName).replace("$", "")));
                              } else {
                                  WebTxItem newItem = new WebTxItem();
                                  newItem.setBaseAmount(Double.parseDouble(request.getParameter(paramName).replace("$", "")));
                                  items.put(paramName.replace("item_amount_", ""), newItem);
                              }
                          } else if (paramName.contains("item_tax_")) {
                              if (items.containsKey(paramName.replace("item_tax_", ""))) {
                                items.get(paramName.replace("item_tax_", ""))
                                .setTax(Double.parseDouble(request.getParameter(paramName).replace("$", "")));
                              } else {
                                  WebTxItem newItem = new WebTxItem();
                                  newItem.setTax(Double.parseDouble(request.getParameter(paramName).replace("$", "")));
                                  items.put(paramName.replace("item_tax_", ""), newItem);
                              }
                          }

                    }
                    CreditCardForm creditCardForm =  new CreditCardForm();
                    modelAndView.addObject("creditCardForm", creditCardForm);
                    List<WebTxItem> itemList = new LinkedList<WebTxItem>(items.values());
                    Double amount = 0.0d;
                    Iterator<WebTxItem>  itemListIterator = itemList.iterator();
                    while(itemListIterator.hasNext()){
                        WebTxItem webTransactionItem = (WebTxItem)itemListIterator.next();
                        amount = amount + webTransactionItem.getBaseAmount();
                    }
                    try {
                        itemList = this.webTransactionService.doSaleGetInfoWEB(siteName, itemList);
                        modelAndView.addObject("ITEMS", itemList);
                        List<String> paymentTokenList = null;
                        if(!StringUtils.isBlank(paymentTokens)) {
                        	paymentTokenList = new LinkedList<String>(SystemUtil.getPaymentTokens(paymentTokens));
                        	Map<Long, PaymentInfoDTO> paymentInfoDTOMap = this.webTransactionService.getPaymentInfoMap(paymentTokenList);
                            modelAndView.addObject("paymentInfoDTOList", paymentInfoDTOMap.values());
                            modelAndView.addObject("paymentInfoDTOMap", paymentInfoDTOMap);
                        } else {
                        	modelAndView.addObject("paymentInfoDTOList", null);
                            modelAndView.addObject("paymentInfoDTOMap", null);                        	
                        }              
                       
                    } catch (SDLBusinessException sDLBusinessException) {
                        modelAndView.addObject("ITEMS", null);
                        modelAndView.addObject("paymentInfoDTOList", null);
                    }
                    httpSession.setAttribute("image_url_" + sessionId, imageUrl);
                    httpSession.setAttribute("footer_url_" + sessionId, footerUrl);
                    httpSession.setAttribute("siteName_" + sessionId, siteName);
                    httpSession.setAttribute("invoiceId_" + sessionId, invoiceId);
                    httpSession.setAttribute("gatewayItems_" + sessionId, itemList);
                    httpSession.setAttribute("return_url_" + sessionId, returnUrl);
                    httpSession.setAttribute("return_text_" + sessionId, returnText);
                    httpSession.setAttribute("send_confirmation_email_" + sessionId,
                    		sendConfirmationEmail);
                    httpSession.setAttribute("application_name_" + sessionId,
                    		applicationName);
                    httpSession.setAttribute("cancel_url_" + sessionId, cancelUrl);
                    httpSession.setAttribute("cancel_text_" + sessionId, cancelText);
                    httpSession.setAttribute("loc_name" + sessionId, locName);
                    httpSession.setAttribute("loc_addr_l1" + sessionId, locAddrLine1);
                    httpSession.setAttribute("loc_addr_l2" + sessionId, locAddrLine2);
                    httpSession.setAttribute("loc_city" + sessionId, locCity);
                    httpSession.setAttribute("loc_state" + sessionId, locState);
                    httpSession.setAttribute("loc_zip" + sessionId, locZip);
                    httpSession.setAttribute("loc_phone" + sessionId, locPhone);
                    httpSession.setAttribute("loc_comments1" + sessionId, locComments1);
                    httpSession.setAttribute("loc_comments2" + sessionId, locComments2);
                    httpSession.setAttribute("isAuthorizeTransaction" + sessionId, isAuthorizeTransaction);
                   
                    
                    
                    modelAndView.addObject("image_url", httpSession.getAttribute("image_url_" + sessionId));
                    modelAndView.addObject("footer_url", httpSession.getAttribute("footer_url_" + sessionId));
                    modelAndView.addObject("cancel_url", cancelUrl);
                    modelAndView.addObject("cancel_text", cancelText);
                    modelAndView.addObject("send_confirmation_email", sendConfirmationEmail);
                    modelAndView.addObject("loc_name", locName);
                    modelAndView.addObject("loc_addr_l1", locAddrLine1);
                    modelAndView.addObject("loc_addr_l2", locAddrLine2);
                    modelAndView.addObject("loc_city", locCity);
                    modelAndView.addObject("loc_state", locState);
                    modelAndView.addObject("loc_zip", locZip);
                    modelAndView.addObject("loc_phone", locPhone);
                    modelAndView.addObject("loc_comments1", locComments1);
                    modelAndView.addObject("loc_comments2", locComments2);
                    modelAndView.addObject("isAuthorizeTransaction", isAuthorizeTransaction);
                    modelAndView.addObject("invoiceId", invoiceId);
                }
            } else {
                error.setCode("ERROR");
                error.setDescription("Invalid Application Name or Application Is Not Registered With Gateway.");
                modelAndView.addObject("ERROR", error);
            }

        } else {
            error.setCode("ERROR");
            error.setDescription("Cannot Process Payment As Required Information Is Invalid. " +
            		"Please Contact The Administrator.");
            modelAndView.addObject("ERROR", error);
        }
        return modelAndView;

    }
    
    @RequestMapping(value = "/paymentgatewaypaynowAZ.admin", method = RequestMethod.POST)
    public ModelAndView paymentgatewaypaynowAZ(@ModelAttribute("creditCardForm") CreditCardForm creditCardForm,
                                            BindingResult bindingResult,
                                            HttpServletRequest request,
                                            Principal principal) {
        ModelAndView modelAndView = getModelAndView(request, ECOM_AZ_PAYMENT_GATEWAY_CONFIRMATION);
        try {
            CreditCard creditCard = null;
            BankAccount bankAccount = null;
            this.verifyBinding(bindingResult);
            WebTransactionDTO webTransactionDTO = new WebTransactionDTO();
            WebTx webTransaction =  null;
            HttpSession httpSession = request.getSession();
            String sessionId = httpSession.getId();
            
            String invoiceId = httpSession.getAttribute("invoiceId_" + sessionId).toString();
            String imageUrl = httpSession.getAttribute("image_url_" + sessionId).toString();
            String footerUrl = httpSession.getAttribute("footer_url_" + sessionId).toString();
            String returnUrl = httpSession.getAttribute("return_url_" + sessionId).toString();
            String returnText = httpSession.getAttribute("return_text_" + sessionId).toString();
            String sendConfirmationEmail = httpSession.getAttribute("send_confirmation_email_" + sessionId).toString();
            String application =  httpSession.getAttribute("application_name_" + sessionId).toString();
            String locName =  httpSession.getAttribute("loc_name" + sessionId).toString();
            String locAddr1 =  httpSession.getAttribute("loc_addr_l1" + sessionId).toString();
            String locAddr2 =  httpSession.getAttribute("loc_addr_l2" + sessionId).toString();
            String locCity =  httpSession.getAttribute("loc_city" + sessionId).toString();
            String locState =  httpSession.getAttribute("loc_state" + sessionId).toString();
            String locZip =  httpSession.getAttribute("loc_zip" + sessionId).toString();
            String locPhone =  httpSession.getAttribute("loc_phone" + sessionId).toString();
            String locComments1 =  httpSession.getAttribute("loc_comments1" + sessionId).toString();
            String locComments2 =  httpSession.getAttribute("loc_comments2" + sessionId).toString();
            String isAuthorizeTransaction =  httpSession.getAttribute("isAuthorizeTransaction" + sessionId).toString();
            String payByMethod =  request.getParameter("payByMethod").toString();
            
            if(payByMethod.equalsIgnoreCase("One")) {
            	validate(creditCardForm, bindingResult, CreditCardGroup.class); // Performing validations specified by annotations.
                if (bindingResult.hasErrors()) {
                    modelAndView = setModelAndViewForError(modelAndView, request, principal);
                    return modelAndView;
                }
                creditCardValidator.validate(creditCardForm, bindingResult); // Performing custom validations.
                if (bindingResult.hasErrors()) {
                    modelAndView = setModelAndViewForError(modelAndView, request, principal);
                    return modelAndView;
                }
                creditCard = this.buildCreditCard(creditCardForm,request); 
            } else if(payByMethod.equalsIgnoreCase("Two"))  {            	
            	validate(creditCardForm, bindingResult, BankAccountGroup.class); // Performing validations specified by annotations.
                if (bindingResult.hasErrors()) {
                    modelAndView = setModelAndViewForError(modelAndView, request, principal);
                    return modelAndView;
                }
            	bankAccount = this.buildBankAccount(creditCardForm,request);           	
            } else if(payByMethod.equalsIgnoreCase("Three"))  {
            	String paymentMethodThreeIdentifier = request.getParameter("paymentMethodThreeIdentifier");
            	String cvv = request.getParameter("paymentMethodThreeCvv");
            	if(!StringUtils.isBlank(paymentMethodThreeIdentifier) && !StringUtils.isBlank(cvv)){
        			PaymentInfoDTO paymentInfoDTO = this.webTransactionService.getPaymentInfoByID(Long.valueOf(paymentMethodThreeIdentifier));
        			creditCard = this.buildCreditCardFromPaymentInfoDTO(paymentInfoDTO, request);
            	} else {
            		modelAndView = setModelAndViewForError(modelAndView, request, principal);
                    return modelAndView;
            	}
            } else {
            	 modelAndView = setModelAndViewForError(modelAndView, request, principal);
                 return modelAndView;
            }
            webTransactionDTO.setWebTransactionItemList((List<WebTxItem>) httpSession.getAttribute("gatewayItems_" + sessionId));
            webTransactionDTO.setCreditCard(creditCard);
            webTransactionDTO.setBankAccount(bankAccount);
            webTransactionDTO.setTransactionLocation(request.getRemoteAddr());
            webTransactionDTO.setOfficeLoc(locName);
            webTransactionDTO.setOfficeLocAddressLine1(locAddr1);
            webTransactionDTO.setOfficeLocAddressLine2(locAddr2);
            webTransactionDTO.setOfficeLocCity(locCity);
            webTransactionDTO.setOfficeLocState(locState);
            webTransactionDTO.setOfficeLocZip(locZip);
            webTransactionDTO.setOfficeLocPhone(locPhone);
            webTransactionDTO.setOfficeLocComments1(locComments1);
            webTransactionDTO.setOfficeLocComments2(locComments2);
            webTransactionDTO.setInvoiceId(invoiceId);
            webTransactionDTO.setPayByMethod(payByMethod);
            webTransactionDTO.setAuthorizeTransaction(Boolean.valueOf(isAuthorizeTransaction));
            modelAndView.addObject("image_url", imageUrl);
            modelAndView.addObject("footer_url", footerUrl);
            modelAndView.addObject("return_url", returnUrl);
            modelAndView.addObject("return_text", returnText);
            modelAndView.addObject("loc_name", locName);
            modelAndView.addObject("loc_addr_l1", locAddr1);
            modelAndView.addObject("loc_addr_l2", locAddr2);
            modelAndView.addObject("loc_city", locCity);
            modelAndView.addObject("loc_state", locState);
            modelAndView.addObject("loc_zip", locZip);
            modelAndView.addObject("loc_phone", locPhone);
            modelAndView.addObject("loc_comments1", locComments1);
            modelAndView.addObject("loc_comments2", locComments2);
            modelAndView.addObject("invoiceId", invoiceId);
            modelAndView.addObject("send_confirmation_email", sendConfirmationEmail);
            modelAndView.addObject("isAuthorizeTransaction", isAuthorizeTransaction);
            modelAndView.addObject("payByMethod", payByMethod);

            webTransaction = this.webTransactionService.doSaleWebPosts(httpSession.getAttribute("siteName_" +
                    sessionId).toString(), webTransactionDTO, creditCardForm.getEmailId(), application);

            modelAndView.addObject("webTransaction", webTransaction);          
            
            httpSession.removeAttribute("invoiceId_" + sessionId);
            httpSession.removeAttribute("siteName_" + sessionId);
            httpSession.removeAttribute("gatewayItems_" + sessionId);
            httpSession.removeAttribute("image_url_" + sessionId);
            httpSession.removeAttribute("footer_url_" + sessionId);
            httpSession.removeAttribute("return_url_" + sessionId);
            httpSession.removeAttribute("return_text_" + sessionId);
            httpSession.removeAttribute("send_confirmation_email_" + sessionId);
            httpSession.removeAttribute("application_name_" + sessionId);
            httpSession.removeAttribute("cancel_url_" + sessionId);
            httpSession.removeAttribute("cancel_text_" + sessionId);
            httpSession.removeAttribute("loc_name" + sessionId);
            httpSession.removeAttribute("loc_addr_l1" + sessionId);
            httpSession.removeAttribute("loc_addr_l2" + sessionId);
            httpSession.removeAttribute("loc_city" + sessionId);
            httpSession.removeAttribute("loc_state" + sessionId);
            httpSession.removeAttribute("loc_zip" + sessionId);
            httpSession.removeAttribute("loc_phone" + sessionId);
            httpSession.removeAttribute("loc_comments1" + sessionId);
            httpSession.removeAttribute("loc_comments2" + sessionId);
            httpSession.removeAttribute("isAuthorizeTransaction" + sessionId);
            httpSession.removeAttribute("payByMethod" + sessionId);

        } catch (SDLBusinessException sDLBusinessException) {
            modelAndView.addObject("ERROR", sDLBusinessException.getBusinessMessage());
        } catch (PaymentGatewaySystemException paypalSystemException) {
            modelAndView.addObject("ERROR", this.getMessage("paypal.errorcode.generalsystemerror"));
        } catch (PaymentGatewayUserException paypalUserException) {
            modelAndView.addObject("ERROR", this.getMessage("paypal.errorcode." + paypalUserException.getErrorCode()));
        }
        return modelAndView;
    }
    
    private BankAccount buildBankAccount(CreditCardForm creditCardForm,
			HttpServletRequest request) {
    	BankAccount bankAccount = new BankAccount();
    	bankAccount.setBankAccountName(creditCardForm.getBankAccountName());
    	bankAccount.setBankAccountNumber(creditCardForm.getBankAccountNumber());
    	bankAccount.setBankRoutingNumber(creditCardForm.getBankRoutingNumber());
    	bankAccount.setBankAccountType(creditCardForm.getBankAccountType());
    	bankAccount.setAddressLine1(creditCardForm.getBankAccountAddressLine1());
    	bankAccount.setAddressLine2(creditCardForm.getBankAccountAddressLine2());
    	bankAccount.setCity(creditCardForm.getBankAccountCity());
    	bankAccount.setState(creditCardForm.getBankAccountState());
    	bankAccount.setZip(creditCardForm.getBankAccountZip());
    	bankAccount.setPhone(creditCardForm.getBankAccountPhoneNumber());        
        return bankAccount;
	}

	@RequestMapping(value = "/paymentgatewaypaynow.admin", method = RequestMethod.POST)
    public ModelAndView paymentGatewayPayNow(@ModelAttribute("creditCardForm") CreditCardForm creditCardForm,
                                            BindingResult bindingResult,
                                            HttpServletRequest request,
                                            Principal principal) {
        ModelAndView modelAndView = getModelAndView(request, ECOM_PAYMENT_GATEWAY_CONFIRMATION);
        try {
            CreditCard creditCard = null;
            this.verifyBinding(bindingResult);
            WebTransactionDTO webTransactionDTO = new WebTransactionDTO();
            WebTx webTransaction =  null;
            HttpSession httpSession = request.getSession();
            String sessionId = httpSession.getId();
            validate(creditCardForm, bindingResult, CreditCardGroup.class); // Performing validations specified by annotations.
            String invoiceId = httpSession.getAttribute("invoiceId_" + sessionId).toString();
            String imageUrl = httpSession.getAttribute("image_url_" + sessionId).toString();
            String footerUrl = httpSession.getAttribute("footer_url_" + sessionId).toString();
            String returnUrl = httpSession.getAttribute("return_url_" + sessionId).toString();
            String returnText = httpSession.getAttribute("return_text_" + sessionId).toString();
            String sendConfirmationEmail = httpSession.getAttribute("send_confirmation_email_" + sessionId).toString();
            String application =  httpSession.getAttribute("application_name_" + sessionId).toString();
            String locName =  httpSession.getAttribute("loc_name" + sessionId).toString();
            String locAddr1 =  httpSession.getAttribute("loc_addr_l1" + sessionId).toString();
            String locAddr2 =  httpSession.getAttribute("loc_addr_l2" + sessionId).toString();
            String locCity =  httpSession.getAttribute("loc_city" + sessionId).toString();
            String locState =  httpSession.getAttribute("loc_state" + sessionId).toString();
            String locZip =  httpSession.getAttribute("loc_zip" + sessionId).toString();
            String locPhone =  httpSession.getAttribute("loc_phone" + sessionId).toString();
            String locComments1 =  httpSession.getAttribute("loc_comments1" + sessionId).toString();
            String locComments2 =  httpSession.getAttribute("loc_comments2" + sessionId).toString();
            String isAuthorizeTransaction =  httpSession.getAttribute("isAuthorizeTransaction" + sessionId).toString();
            String payByMethod =  request.getParameter("payByMethod").toString();
            if (bindingResult.hasErrors()) {
                modelAndView = setModelAndViewForError(modelAndView, request, principal);
                return modelAndView;
            }
            creditCardValidator.validate(creditCardForm, bindingResult); // Performing custom validations.
            if (bindingResult.hasErrors()) {
                modelAndView = setModelAndViewForError(modelAndView, request, principal);
                return modelAndView;
            }
            creditCard = this.buildCreditCard(creditCardForm,request);
            webTransactionDTO.setWebTransactionItemList((List<WebTxItem>) httpSession.getAttribute("gatewayItems_" + sessionId));
            webTransactionDTO.setCreditCard(creditCard);
            webTransactionDTO.setTransactionLocation(request.getRemoteAddr());
            webTransactionDTO.setOfficeLoc(locName);
            webTransactionDTO.setOfficeLocAddressLine1(locAddr1);
            webTransactionDTO.setOfficeLocAddressLine2(locAddr2);
            webTransactionDTO.setOfficeLocCity(locCity);
            webTransactionDTO.setOfficeLocState(locState);
            webTransactionDTO.setOfficeLocZip(locZip);
            webTransactionDTO.setOfficeLocPhone(locPhone);
            webTransactionDTO.setOfficeLocComments1(locComments1);
            webTransactionDTO.setOfficeLocComments2(locComments2);
            webTransactionDTO.setInvoiceId(invoiceId);
            webTransactionDTO.setPayByMethod(payByMethod);
            webTransactionDTO.setAuthorizeTransaction(Boolean.valueOf(isAuthorizeTransaction));

            webTransaction = this.webTransactionService.doSaleWebPosts(httpSession.getAttribute("siteName_" +
                    sessionId).toString(), webTransactionDTO, creditCardForm.getEmailId(), application);

            modelAndView.addObject("webTransaction", webTransaction);
            modelAndView.addObject("image_url", imageUrl);
            modelAndView.addObject("footer_url", footerUrl);
            modelAndView.addObject("return_url", returnUrl);
            modelAndView.addObject("return_text", returnText);
            modelAndView.addObject("loc_name", locName);
            modelAndView.addObject("loc_addr_l1", locAddr1);
            modelAndView.addObject("loc_addr_l2", locAddr2);
            modelAndView.addObject("loc_city", locCity);
            modelAndView.addObject("loc_state", locState);
            modelAndView.addObject("loc_zip", locZip);
            modelAndView.addObject("loc_phone", locPhone);
            modelAndView.addObject("loc_comments1", locComments1);
            modelAndView.addObject("loc_comments2", locComments2);
            modelAndView.addObject("invoiceId", invoiceId);
            modelAndView.addObject("send_confirmation_email", sendConfirmationEmail);

            httpSession.removeAttribute("invoiceId_" + sessionId);
            httpSession.removeAttribute("siteName_" + sessionId);
            httpSession.removeAttribute("gatewayItems_" + sessionId);
            httpSession.removeAttribute("image_url_" + sessionId);
            httpSession.removeAttribute("footer_url_" + sessionId);
            httpSession.removeAttribute("return_url_" + sessionId);
            httpSession.removeAttribute("return_text_" + sessionId);
            httpSession.removeAttribute("send_confirmation_email_" + sessionId);
            httpSession.removeAttribute("application_name_" + sessionId);
            httpSession.removeAttribute("cancel_url_" + sessionId);
            httpSession.removeAttribute("cancel_text_" + sessionId);
            httpSession.removeAttribute("loc_name" + sessionId);
            httpSession.removeAttribute("loc_addr_l1" + sessionId);
            httpSession.removeAttribute("loc_addr_l2" + sessionId);
            httpSession.removeAttribute("loc_city" + sessionId);
            httpSession.removeAttribute("loc_state" + sessionId);
            httpSession.removeAttribute("loc_zip" + sessionId);
            httpSession.removeAttribute("loc_phone" + sessionId);
            httpSession.removeAttribute("loc_comments1" + sessionId);
            httpSession.removeAttribute("loc_comments2" + sessionId);

        } catch (SDLBusinessException sDLBusinessException) {
            modelAndView.addObject("ERROR", sDLBusinessException.getBusinessMessage());
        } catch (PaymentGatewaySystemException paypalSystemException) {
            modelAndView.addObject("ERROR", this.getMessage("paypal.errorcode.generalsystemerror"));
        } catch (PaymentGatewayUserException paypalUserException) {
            modelAndView.addObject("ERROR", this.getMessage("paypal.errorcode." + paypalUserException.getErrorCode()));
        }
        return modelAndView;
    }

    @RequestMapping(value="/secure/viewpayasugopaymentinfo.admin", method=RequestMethod.GET)
    public ModelAndView viewPayAsUGoPaymentinfo(HttpServletRequest request,
                                           HttpServletResponse response,
                                           @RequestParam(value = "token1") String userNameInParameter,
                                           @RequestParam(value = "token3") String returnURL,
                                           @RequestParam(value = "nodeName") String nodeName) {
        Authentication authenticatedUser = this.reauthenticate(request);
        ModelAndView modelAndView = getModelAndView(request, ECOM_PAYASUGO_PAYMENT_INFO);
        HttpSession httpSession = request.getSession();
        httpSession.setMaxInactiveInterval(Integer.parseInt(sessionTimeout));
        User user = (User) authenticatedUser.getPrincipal();
        String userName = this.verifyLoggedInUser(request, response);
        if(userName == null) {
            this.logoutUser(request, response);
            modelAndView.setViewName("redirect:/secure/viewpayasugopaymentinfo.admin");
            modelAndView.addObject("token1", userNameInParameter);
            modelAndView.addObject("token3", returnURL);
            modelAndView.addObject("nodeName", nodeName);
            return modelAndView;
        }
        CreditCardForm creditCardForm =  new CreditCardForm();
        if (user.isCardAvailable()) {
            CreditCard creditCard = this.userService.getCreditCardDetails(request.getRemoteUser());
            creditCardForm = this.buildCreditCardForm(creditCard);
        }

        List<ShoppingCartItem> shoppingCart = getShoppingCart(request.getRemoteUser(), nodeName, request);

        modelAndView.addObject("user", user);
        modelAndView.addObject("creditCardForm", creditCardForm);
        modelAndView.addObject("returnUrl", returnURL);
        modelAndView.addObject("nodeName", nodeName);
        modelAndView.addObject(SHOPPING_CART, shoppingCart);

        httpSession.setAttribute("creditCardForm" + request.getRemoteUser(), creditCardForm);
        httpSession.setAttribute("token1" + request.getRemoteUser(), userNameInParameter);
        httpSession.setAttribute("returnUrl" + request.getRemoteUser(), returnURL);
        httpSession.setAttribute("nodeName" + request.getRemoteUser(), nodeName);

        return modelAndView;
    }


    private List<ShoppingCartItem> getShoppingCart(String userName, String nodeName, HttpServletRequest request) {
        List<ShoppingCartItem> shoppingCart = this.payAsUGoSubService.getShoppingBasketItems(userName, nodeName);
        HttpSession httpSession = request.getSession();
        try {

            shoppingCart = this.payAsUGoSubService.doSalePayAsUGoInfo(request.getRemoteUser(), shoppingCart);
        } catch (SDLBusinessException sDLBusinessException) {
            httpSession.setAttribute(BUSSINESS_EXCP, sDLBusinessException.getBusinessMessage());
        }
        return shoppingCart;
    }

    private ModelAndView setModelAndViewForError(ModelAndView modelAndView, HttpServletRequest request, Principal principal) {
    	HttpSession httpSession = request.getSession();
    	String sessionId = httpSession.getId();
        modelAndView.addObject("image_url", httpSession.getAttribute("image_url_" + sessionId));
        modelAndView.addObject("footer_url", httpSession.getAttribute("footer_url_" + sessionId));
        modelAndView.addObject("ITEMS", httpSession.getAttribute("gatewayItems_" + sessionId));
        modelAndView.addObject("cancel_url", httpSession.getAttribute("cancel_url_" + sessionId));
        modelAndView.addObject("cancel_text", httpSession.getAttribute("cancel_text_" + sessionId));
        modelAndView.addObject("user_name", httpSession.getAttribute("user_name_" + sessionId));
        modelAndView.addObject("send_confirmation_email", httpSession.getAttribute("send_confirmation_email_" + sessionId));
        modelAndView.setViewName(ECOM_REDIRECT_PAYMENT_GATEWAY);
        return modelAndView;
    }


    @RequestMapping(value = "/secure/payasugopaynow.admin", method = RequestMethod.POST)
    public ModelAndView payPayAsUGoTransaction(@ModelAttribute("creditCardForm") CreditCardForm creditCardForm,
                                            BindingResult bindingResult, HttpServletRequest request, Principal principal) {
        ModelAndView modelAndView = getModelAndView(request, ECOM_REDIRECT_PAYASUGO_PAYMENT_CONFIRMATION);
        try {
            CreditCard creditCard = null;
            this.verifyBinding(bindingResult);
            PayAsUSubDTO payAsUGoTransactionDTO = new PayAsUSubDTO();
            User user = this.getUser(request);
            modelAndView.addObject("user", user);
            List<PayAsUGoTx> payAsUGoTransactionList =  null;
            if(NEW_CREDIT_CARD.equalsIgnoreCase(creditCardForm.getUseExistingAccount())
                    || UPDATE_CREDIT_CARD.equalsIgnoreCase(creditCardForm.getUseExistingAccount())) {
                //Use New card.
                validate(creditCardForm, bindingResult, CreditCardGroup.class); // Performing validations specified by annotations.
                if (bindingResult.hasErrors()) {
                    //modelAndView.setViewName(ECOM_REDIRECT_WEB_PAYMENT_INFO + "?token1=" + request.getSession().getAttribute("token1" + request.getRemoteUser())
                    //                         + "&token3=" + request.getSession().getAttribute("returnUrl" + request.getRemoteUser())
                    //                         + "&nodeName=" + request.getSession().getAttribute("nodeName" + request.getRemoteUser()));
                    modelAndView.setViewName(ECOM_PAYASUGO_PAYMENT_INFO);
                    List<ShoppingCartItem> shoppingCart = getShoppingCart(request.getRemoteUser(),
                                request.getSession().getAttribute("nodeName" + request.getRemoteUser()).toString(), request);
                    modelAndView.addObject("returnUrl", request.getSession().getAttribute("returnUrl" + request.getRemoteUser()));
                    modelAndView.addObject("nodeName", request.getSession().getAttribute("nodeName" + request.getRemoteUser()));
                    modelAndView.addObject(SHOPPING_CART, shoppingCart);
                    return modelAndView;
                }
                creditCardValidator.validate(creditCardForm, bindingResult); // Performing custom validations.
                if (bindingResult.hasErrors()) {
                    //modelAndView.setViewName(ECOM_REDIRECT_WEB_PAYMENT_INFO + "?token1=" + request.getSession().getAttribute("token1" + request.getRemoteUser())
                    //         + "&token3=" + request.getSession().getAttribute("returnUrl" + request.getRemoteUser())
                    //         + "&nodeName=" + request.getSession().getAttribute("nodeName" + request.getRemoteUser()));
                    modelAndView.setViewName(ECOM_PAYASUGO_PAYMENT_INFO);
                    modelAndView.addObject("returnUrl", request.getSession().getAttribute("returnUrl" + request.getRemoteUser()));
                    modelAndView.addObject("nodeName", request.getSession().getAttribute("nodeName" + request.getRemoteUser()));
                    List<ShoppingCartItem> shoppingCart = getShoppingCart(request.getRemoteUser(),
                                request.getSession().getAttribute("nodeName" + request.getRemoteUser()).toString(), request);
                    modelAndView.addObject(SHOPPING_CART, shoppingCart);
                    return modelAndView;
                }
                creditCard = this.buildCreditCard(creditCardForm,request);
            } else if (FIRM_CREDIT_CARD.equalsIgnoreCase(creditCardForm.getUseExistingAccount())){
            	payAsUGoTransactionDTO.setUseFirmsCreditCard(true);
            }
            List<ShoppingCartItem> itemList = new LinkedList<ShoppingCartItem>();
            itemList = this.payAsUGoSubService.getShoppingBasketItems(request.getRemoteUser(), request.getParameter("node_Name"));
            Map<String, ShoppingCartItem> shoppingCartMap = new LinkedHashMap<String, ShoppingCartItem>();
            if (itemList != null) {
                for (ShoppingCartItem item : itemList) {
                    shoppingCartMap.put(item.getProductId() + item.getUniqueIdentifier(), item);
                }
            }
            if(!shoppingCartMap.isEmpty()) {
                List<ShoppingCartItem> shoppingCartItemList = payAsUGoTransactionDTO.getShoppingCartItemList();
                if(shoppingCartItemList == null) {
                    shoppingCartItemList = new LinkedList<ShoppingCartItem>(shoppingCartMap.values());
                } else {
                    List<ShoppingCartItem> tempShoppingCartItemList = new LinkedList<ShoppingCartItem>(shoppingCartMap.values());
                    shoppingCartItemList.addAll(tempShoppingCartItemList);
                }
                payAsUGoTransactionDTO.setShoppingCartItemList(shoppingCartItemList);
                payAsUGoTransactionDTO.setSaveCreditCard(creditCardForm.isSaveCreditCard());
                payAsUGoTransactionDTO.setCreditCard(creditCard);
                payAsUGoTransactionDTO.setTransactionLocation(request.getRemoteAddr());
                payAsUGoTransactionList = this.payAsUGoSubService.doSalePayAsUGo(request.getRemoteUser(), payAsUGoTransactionDTO);
                request.getSession().setAttribute("payAsUGoTransactionList", payAsUGoTransactionList);
                request.getSession().removeAttribute(SHOPPING_CART + request.getRemoteUser());
            }   else {
            	request.getSession().setAttribute("ERROR", "Shopping Cart Cannot be Empty");
            }
        } catch (SDLBusinessException sDLBusinessException) {
            request.getSession().setAttribute("ERROR", sDLBusinessException.getBusinessMessage());
        } catch (PaymentGatewaySystemException paypalSystemException) {
            request.getSession().setAttribute("ERROR", this.getMessage("paypal.errorcode.generalsystemerror"));
        } catch (PaymentGatewayUserException paypalUserException) {
            request.getSession().setAttribute("ERROR", this.getMessage("paypal.errorcode." + paypalUserException.getErrorCode()));
        }
        return modelAndView;
    }

    @RequestMapping(value="/secure/payAsUGoPaymentConfirmation.admin", method=RequestMethod.GET)
    public ModelAndView viewWebPaymentConfirmation(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = getModelAndView(request, ECOM_PAYASUGO_PAYMENT_CONFIRMATION);
        UsernamePasswordAuthenticationToken userPasswordAuthToken
                = (UsernamePasswordAuthenticationToken) request.getUserPrincipal();
        HttpSession httpSession = request.getSession();
        User user = (User)userPasswordAuthToken.getPrincipal();
        String returnUrl = httpSession.getAttribute("returnUrl" + request.getRemoteUser()).toString();
        String nodeName = httpSession.getAttribute("nodeName" + request.getRemoteUser()).toString();
        modelAndView.addObject("returnUrl", returnUrl);
        modelAndView.addObject("nodeName", nodeName);
        modelAndView.addObject("user", user);
        modelAndView.addObject("payAsUGoTransactionList", httpSession.getAttribute("payAsUGoTransactionList"));
        modelAndView.addObject("currentDate", new Date());
        modelAndView.addObject("ERROR", httpSession.getAttribute("ERROR"));
        httpSession.removeAttribute("returnUrl" + request.getRemoteUser());
        httpSession.removeAttribute("nodeName" + request.getRemoteUser());
        this.logoutUser(request, response);
        return modelAndView;
    }

    @RequestMapping(value="/secure/purchaseItems.admin")
    public ModelAndView purchaseItems(HttpServletRequest request, HttpServletResponse response, Principal principal) {
       HttpSession session = request.getSession(false);
       String sessionId = session.getId();
       ErrorCode error = new ErrorCode();
       Set<String> paramNames = null;
       SavedRequest savedRequest = null;
       ModelAndView modelAndView = getModelAndView(request, ECOM_REVIEW_SHOPPING_SERVER_CART);
       if (session != null) {
             savedRequest = sDLSavedRequestAwareAuthenticationSuccessHandler.getRequestCache()
                   .getRequest(request, response);
            if(savedRequest != null) {
               paramNames = savedRequest.getParameterMap().keySet();
            //sDLSavedRequestAwareAuthenticationSuccessHandler.getRequestCache().removeRequest(request, response);
            } else {
               error.setCode("ERROR");
            error.setDescription("General System Exception.");
            modelAndView.addObject("ERROR", error);
            return modelAndView;
            }
        }
        if (!request.getParameter("userName").equalsIgnoreCase(savedRequest.getParameterValues("userName")[0])) {
            error.setCode("ERROR");
            error.setDescription("Illegal Operation.");
            modelAndView.addObject("ERROR", error);
            return modelAndView;
        }
        Map<String, ShoppingCartItem> items = new HashMap<String,ShoppingCartItem>();
        List<Code> codes = this.eComService.getCodes("REGISTERED_APPLICATION");
        boolean isValidApplication = false;
        for (Code code : codes) {
            if (code.getCode().equals(savedRequest.getParameterValues("application_name")[0])) {
                isValidApplication = true;
            }
        }
        if (isValidApplication) {
            if (savedRequest.getParameterValues("application_name")[0] != null &&
            		savedRequest.getParameterValues("application_name")[0].trim() !="") {
                for (String paramName:paramNames) {
                      if (paramName.contains("item_type_")) {
                          if (items.containsKey(paramName.replace("item_type_", ""))) {
                            items.get(paramName.replace("item_type_", ""))
                            .setProductType(savedRequest.getParameterValues(paramName)[0]);
                          } else {
                              ShoppingCartItem newItem = new ShoppingCartItem();
                              newItem.setProductType(savedRequest.getParameterValues(paramName)[0]);
                              items.put(paramName.replace("item_type_", ""), newItem);
                          }
                      } else if (paramName.contains("item_id_")) {
                          if (items.containsKey(paramName.replace("item_id_", ""))) {
                            items.get(paramName.replace("item_id_", ""))
                            .setProductId(savedRequest.getParameterValues(paramName)[0]);
                          } else {
                              ShoppingCartItem newItem = new ShoppingCartItem();
                              newItem.setProductId(savedRequest.getParameterValues(paramName)[0]);
                              items.put(paramName.replace("item_id_", ""), newItem);
                          }
                      } else if (paramName.contains("item_pagecount_")) {
                          if (items.containsKey(paramName.replace("item_pagecount_", ""))) {
                            items.get(paramName.replace("item_pagecount_", ""))
                            .setPageCount(Integer.parseInt(savedRequest.getParameterValues(paramName)[0]));
                          } else {
                              ShoppingCartItem newItem = new ShoppingCartItem();
                              newItem.setPageCount(Integer.parseInt(savedRequest.getParameterValues(paramName)[0]));
                              items.put(paramName.replace("item_pagecount_", ""), newItem);
                          }
                      } else if (paramName.contains("item_access_name_")) {
                          if (items.containsKey(paramName.replace("item_access_name_", ""))) {
                            items.get(paramName.replace("item_access_name_", ""))
                            .setAccessName(savedRequest.getParameterValues(paramName)[0]);
                          } else {
                              ShoppingCartItem newItem = new ShoppingCartItem();
                              newItem.setAccessName(savedRequest.getParameterValues(paramName)[0]);
                              items.put(paramName.replace("item_access_name_", ""), newItem);
                          }
                      } else if (paramName.contains("item_unique_identifier_")) {
                          if (items.containsKey(paramName.replace("item_unique_identifier_", ""))) {
                            items.get(paramName.replace("item_unique_identifier_", ""))
                            .setUniqueIdentifier(savedRequest.getParameterValues(paramName)[0]);
                          } else {
                              ShoppingCartItem newItem = new ShoppingCartItem();
                              newItem.setUniqueIdentifier(savedRequest.getParameterValues(paramName)[0]);
                              items.put(paramName.replace("item_unique_identifier_", ""), newItem);
                          }
                      } else if (paramName.contains("item_download_url_")) {
                          if (items.containsKey(paramName.replace("item_download_url_", ""))) {
                            items.get(paramName.replace("item_download_url_", ""))
                            .setDownloadURL(savedRequest.getParameterValues(paramName)[0]);
                          } else {
                              ShoppingCartItem newItem = new ShoppingCartItem();
                              newItem.setDownloadURL(savedRequest.getParameterValues(paramName)[0]);
                              items.put(paramName.replace("item_download_url_", ""), newItem);
                          }
                      } else if (paramName.contains("item_application_")) {
                          if (items.containsKey(paramName.replace("item_application_", ""))) {
                            items.get(paramName.replace("item_application_", ""))
                            .setApplication(savedRequest.getParameterValues(paramName)[0]);
                          } else {
                              ShoppingCartItem newItem = new ShoppingCartItem();
                              newItem.setApplication(savedRequest.getParameterValues(paramName)[0]);
                              items.put(paramName.replace("item_application_", ""), newItem);
                          }
                      }
                }
                CreditCardForm creditCardForm = new CreditCardForm();
                CreditCard creditCard = this.userService.getCreditCardDetails(request.getRemoteUser());
                if (creditCard != null) {
                    creditCardForm = this.buildCreditCardForm(creditCard);
                }
                UsernamePasswordAuthenticationToken userPasswordAuthToken = (UsernamePasswordAuthenticationToken)principal;
                User user = (User)userPasswordAuthToken.getPrincipal();
                List<ShoppingCartItem> itemList = null;
                if (items != null && items.size() > 0) {
                    itemList = new LinkedList<ShoppingCartItem>(items.values());
                    try {
                        itemList = this.payAsUGoSubService.doSalePayAsUGoInfo(request.getParameter("userName"), itemList);
                    } catch (SDLBusinessException sDLBusinessException) {
                        session.setAttribute(BUSSINESS_EXCP, sDLBusinessException.getBusinessMessage());
                    }
                }
                session.setAttribute("SHOPPING_CART_" + sessionId, items);
                session.setAttribute("siteName_" + sessionId,savedRequest.getParameterValues("site_name")[0]);
                session.setAttribute("image_url_" + sessionId, savedRequest.getParameterValues("image_url")[0]);
                session.setAttribute("footer_url_" + sessionId, savedRequest.getParameterValues("footer_url")[0]);
                session.setAttribute("return_url_" + sessionId,savedRequest.getParameterValues("return_url")[0]);
                session.setAttribute("return_text_" + sessionId,savedRequest.getParameterValues("return_text")[0]);
                session.setAttribute("application_name_" + sessionId,savedRequest.getParameterValues("application_name")[0]);
                session.setAttribute("user_name_" + sessionId,request.getParameter("userName"));
                session.setAttribute("cancel_url_" + sessionId,savedRequest.getParameterValues("cancel_url")[0]);
                session.setAttribute("cancel_text_" + sessionId,savedRequest.getParameterValues("cancel_text")[0]);
                modelAndView.addObject("creditCardForm", creditCardForm);
                modelAndView.addObject("image_url", session.getAttribute("image_url_" + sessionId));
                modelAndView.addObject("footer_url", session.getAttribute("footer_url_" + sessionId));
                modelAndView.addObject("ITEMS", itemList);
                modelAndView.addObject("cancel_url", session.getAttribute("cancel_url_" + sessionId));
                modelAndView.addObject("cancel_text", session.getAttribute("cancel_text_" + sessionId));
                modelAndView.addObject("user_name", request.getParameter("userName"));
                modelAndView.addObject("user", user);
            }
        } else {
            error.setCode("ERROR");
            error.setDescription("Invalid Application Name or Application is not registered with gateway.");
            modelAndView.addObject("ERROR", error);
        }
        return modelAndView;
    }


    @RequestMapping(value = "/secure/purchaseItemsPayment.admin", method = RequestMethod.POST)
    public ModelAndView purchaseItemsPayment(@ModelAttribute("creditCardForm") CreditCardForm creditCardForm,
                                            BindingResult bindingResult,
                                            HttpServletRequest request,
                                            Principal principal) {
        ModelAndView modelAndView = getModelAndView(request, ECOM_SHOPPING_CART_PAYMENT_CONFIRMATION);
        HttpSession httpSession = request.getSession();
        try {
            CreditCard creditCard = null;
            this.verifyBinding(bindingResult);
            PayAsUSubDTO payAsUGoTransactionDTO = new PayAsUSubDTO();

            String sessionId = httpSession.getId();
            List<PayAsUGoTx> payAsUGoTransactionList =  null;
            if(NEW_CREDIT_CARD.equalsIgnoreCase(creditCardForm.getUseExistingAccount())
                    || UPDATE_CREDIT_CARD.equalsIgnoreCase(creditCardForm.getUseExistingAccount())) {
                //Use New card.
                validate(creditCardForm, bindingResult, CreditCardGroup.class); // Performing validations specified by annotations.
                if (bindingResult.hasErrors()) {
                    modelAndView = setModelAndViewForErrorForShoppingCart(modelAndView, request, principal);
                    return modelAndView;
                }
                creditCardValidator.validate(creditCardForm, bindingResult); // Performing custom validations.
                if (bindingResult.hasErrors()) {
                    modelAndView = setModelAndViewForErrorForShoppingCart(modelAndView, request, principal);
                    return modelAndView;
                }
                creditCard = this.buildCreditCard(creditCardForm,request);

            }
            String imageUrl = httpSession.getAttribute("image_url_" + sessionId).toString();
            String footerUrl = httpSession.getAttribute("footer_url_" + sessionId).toString();
            String returnUrl = httpSession.getAttribute("return_url_" + sessionId).toString();
            String returnText = httpSession.getAttribute("return_text_" + sessionId).toString();
            String userName =  httpSession.getAttribute("user_name_" + sessionId).toString();
            HashMap<String, ShoppingCartItem> shoppingCartMap = (HashMap<String, ShoppingCartItem>) httpSession.
                    getAttribute("SHOPPING_CART_" + sessionId);
            if(shoppingCartMap != null) {
                List<ShoppingCartItem> shoppingCartItemList = payAsUGoTransactionDTO.getShoppingCartItemList();
                if(shoppingCartItemList == null) {
                    shoppingCartItemList = new LinkedList<ShoppingCartItem>(shoppingCartMap.values());
                } else {
                    List<ShoppingCartItem> tempShoppingCartItemList = new LinkedList<ShoppingCartItem>(shoppingCartMap.values());
                    shoppingCartItemList.addAll(tempShoppingCartItemList);
                }
                payAsUGoTransactionDTO.setShoppingCartItemList(shoppingCartItemList);
            }
            payAsUGoTransactionDTO.setSaveCreditCard(creditCardForm.isSaveCreditCard());
            payAsUGoTransactionDTO.setCreditCard(creditCard);
            payAsUGoTransactionDTO.setTransactionLocation(request.getRemoteAddr());
            payAsUGoTransactionList = this.payAsUGoSubService.doSalePayAsUGo(userName, payAsUGoTransactionDTO);

            UsernamePasswordAuthenticationToken userPasswordAuthToken = (UsernamePasswordAuthenticationToken)principal;
            User user = (User)userPasswordAuthToken.getPrincipal();

            modelAndView.addObject("user", user);
            modelAndView.addObject("payAsUGoTransactionList", payAsUGoTransactionList);
            modelAndView.addObject("image_url", imageUrl);
            modelAndView.addObject("footer_url", footerUrl);
            modelAndView.addObject("return_url", returnUrl);
            modelAndView.addObject("return_text", returnText);
            httpSession.removeAttribute("siteName_" + sessionId);
            httpSession.removeAttribute("image_url_" + sessionId);
            httpSession.removeAttribute("footer_url_" + sessionId);
            httpSession.removeAttribute("return_url_" + sessionId);
            httpSession.removeAttribute("return_text_" + sessionId);
            httpSession.removeAttribute("application_name_" + sessionId);
            httpSession.removeAttribute("cancel_url_" + sessionId);
            httpSession.removeAttribute("cancel_text_" + sessionId);
            httpSession.removeAttribute("SHOPPING_CART_" + sessionId);
            httpSession.removeAttribute("user_name_" + sessionId);

        } catch (SDLBusinessException sDLBusinessException) {
            modelAndView.addObject("ERROR", sDLBusinessException.getBusinessMessage());
        } catch (PaymentGatewaySystemException paypalSystemException) {
            modelAndView.addObject("ERROR", this.getMessage("paypal.errorcode.generalsystemerror"));
        } catch (PaymentGatewayUserException paypalUserException) {
            modelAndView.addObject("ERROR", this.getMessage("paypal.errorcode." + paypalUserException.getErrorCode()));
        }
        return modelAndView;
    }


    private ModelAndView setModelAndViewForErrorForShoppingCart(ModelAndView modelAndView,
                                                                HttpServletRequest request,
                                                                Principal principal) {
    	HttpSession httpSession = request.getSession();
    	String sessionId = httpSession.getId();
        Map<String, ShoppingCartItem> items = (HashMap<String,ShoppingCartItem>) httpSession
            .getAttribute("SHOPPING_CART_" + sessionId);
        List<ShoppingCartItem> itemList = null;
        if (items != null && items.size() > 0) {
            itemList = new LinkedList<ShoppingCartItem>(items.values());
            try {
                itemList = this.payAsUGoSubService.doSalePayAsUGoInfo(httpSession.getAttribute("user_name_"
                        + sessionId).toString(), itemList);
            } catch (SDLBusinessException sDLBusinessException) {
            	httpSession.setAttribute(BUSSINESS_EXCP, sDLBusinessException.getBusinessMessage());
            }
        }
        UsernamePasswordAuthenticationToken userPasswordAuthToken = (UsernamePasswordAuthenticationToken)principal;
        User user = (User)userPasswordAuthToken.getPrincipal();

        modelAndView.addObject("user", user);
        modelAndView.addObject("image_url", httpSession.getAttribute("image_url_" + sessionId));
        modelAndView.addObject("footer_url", httpSession.getAttribute("footer_url_" + sessionId));
        modelAndView.addObject("ITEMS", itemList);
        modelAndView.addObject("cancel_url", httpSession.getAttribute("cancel_url" + sessionId));
        modelAndView.addObject("cancel_text", httpSession.getAttribute("cancel_text" + sessionId));
        modelAndView.addObject("user_name", httpSession.getAttribute("user_name_" + sessionId));
        modelAndView.setViewName(ECOM_REVIEW_SHOPPING_SERVER_CART);
        return modelAndView;
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
        if(creditCard.getExpiryMonth() < 10){
            creditCardForm.setExpMonthS("0" + (creditCard.getExpiryMonth().toString()));
        }else {
            creditCardForm.setExpMonthS(creditCard.getExpiryMonth().toString());
        }
        creditCardForm.setExpYear(creditCard.getExpiryYear());
        creditCardForm.setPhoneNumber(creditCard.getPhone());
        return creditCardForm;
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
            return creditCard;
        }
        
        private CreditCard buildCreditCardFromPaymentInfoDTO(PaymentInfoDTO paymentInfoDTO, HttpServletRequest request) {
            CreditCard creditCard = new CreditCard();
            creditCard.setName(paymentInfoDTO.getAccountName());
            creditCard.setNumber(paymentInfoDTO.getCreditCardNumber());
            creditCard.setExpiryMonth(paymentInfoDTO.getExpiryMonth());
            creditCard.setExpiryYear(paymentInfoDTO.getExpiryYear());
            creditCard.setSecurityCode(request.getParameter("paymentMethodThreeCvv"));
            creditCard.setAddressLine1(paymentInfoDTO.getAddressLine1());
            creditCard.setAddressLine2(paymentInfoDTO.getAddressLine2());
            creditCard.setCity(paymentInfoDTO.getCity());
            creditCard.setState(paymentInfoDTO.getState());
            creditCard.setZip(paymentInfoDTO.getZip());
            creditCard.setPhone(paymentInfoDTO.getPhone());
            creditCard.setModifiedBy(request.getRemoteUser());
            return creditCard;
        }

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
}
