package com.fdt.ecom.ui.controller;

import static com.fdt.alerts.ui.AlertsViewConstants.ALERTS_REDIRECT_GET_USER_ALERTS;
import static com.fdt.common.ui.BaseViewConstants.COMMON_GENERAL_ERROR;
import static com.fdt.common.ui.BaseViewConstants.COMMON_HOME_FORWARD;
import static com.fdt.common.ui.BaseViewConstants.LOGOUT;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_CANCEL_SUBSCRIPTION;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_CHANGE_SUBSCRIPTION;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_CONFIRM_ADD_SUBSCRIPTION;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_GET_SUBSCRIPTION_DETAILS;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_PAYASUGO_PAYMENT_DETAILS;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_PAYMENT_AUTHORIZATION_PENDING;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_PAYMENT_HISTORY;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_REACTIVATE_SUBSCRIPTION;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_RECURRING_PAYMENT_DETAILS;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_REDIRECT_CANCEL_SUBSCRIPTION;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_REDIRECT_CHANGE_SUBSCRIPTION;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_REDIRECT_CONFIRM_ADD_SUBSCRIPTION;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_REDIRECT_VIEW_AVAILABLE_SUBSCRIPTIONS;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_REDIRECT_VIEW_CHANGE_SUBSCRIPTION_INFO;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_VIEW_AVAILABLE_SUBSCRIPTIONS;
import static com.fdt.ecom.ui.EcomViewConstants.ECOM_VIEW_CHANGE_SUBSCRIPTION_INFO;
import static com.fdt.security.ui.SecurityViewConstants.ACCEPT_TERMS_CONDITIONS;
import static com.fdt.security.ui.SecurityViewConstants.EXCEL;
import static com.fdt.security.ui.SecurityViewConstants.PDF;
import static com.fdt.security.ui.SecurityViewConstants.REDIRECT_ACCEPT_TERMS_CONDITIONS;
import static com.fdt.security.ui.SecurityViewConstants.REDIRECT_CHECK_SUBSCRIPTION;
import static com.fdt.security.ui.SecurityViewConstants.SECURITY_ACCOUNT_INFORMATION;
import static com.fdt.security.ui.SecurityViewConstants.SECURITY_FORWARD_ACCOUNT_INFORMATION;
import static com.fdt.security.ui.SecurityViewConstants.SECURITY_REDIRECT_REACTIVATE_SUBSCRIPTION;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.jmesa.view.component.Column;
import org.jmesa.view.component.Row;
import org.jmesa.view.component.Table;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.editor.DateCellEditor;
import org.jmesa.view.editor.NumberCellEditor;
import org.jmesa.view.html.component.HtmlColumn;
import org.jmesa.view.html.component.HtmlRow;
import org.jmesa.view.html.component.HtmlTable;
import org.jmesa.view.html.editor.HtmlCellEditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fdt.alerts.dto.UserAlertDTO;
import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.entity.ErrorCode;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.ShoppingCartItem;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.Term;
import com.fdt.ecom.ui.util.ExcelExport;
import com.fdt.ecom.ui.util.ExportConstants;
import com.fdt.ecom.ui.util.PDFCell;
import com.fdt.ecom.ui.util.PDFExport;
import com.fdt.payasugotx.entity.PayAsUGoTx;
import com.fdt.payasugotx.entity.PayAsUGoTxView;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.recurtx.dto.UpgradeDowngradeDTO;
import com.fdt.recurtx.entity.RecurTx;
import com.fdt.sdl.styledesigner.util.PageStyleUtil;
import com.fdt.security.entity.Access;
import com.fdt.security.entity.User;
import com.fdt.security.entity.enums.AccessType;
import com.fdt.security.exception.MaxUsersExceededException;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.security.spring.SDLSavedRequestAwareAuthenticationSuccessHandler;
import com.fdt.subscriptions.dto.AccessDetailDTO;
import com.fdt.subscriptions.dto.SubscriptionDTO;

@Controller
public class AccountSettingsController extends AbstractBaseController {

	public static String SHOPPING_CART = "SHOPPING_CART";

	public static int PAGE_SIZE = 5;

	private static int[] PAYASUGO_PDF_COLUMN_WIDTHS = new int[] {30, 18, 14, 10, 10, 30, 8, 16, 10};
	private static int[] RECUR_PDF_COLUMN_WIDTHS = new int[] {18, 14, 10, 30, 8, 16, 10, 10};


	@Autowired
	@Qualifier("sDLSavedRequestAwareAuthenticationSuccessHandler")
	private SDLSavedRequestAwareAuthenticationSuccessHandler sDLSavedRequestAwareAuthenticationSuccessHandler = null;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.setAllowedFields(new String[] {
				"models",
				"id",
				"username",
				"firstName",
				"lastName",
				"phone",
		});
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	    binder.registerCustomEditor(Date.class, new CustomDateEditor(
	            dateFormat, false));
	}

	@RequestMapping(value="/checkSubscription.admin")
	public ModelAndView checkSubscription(HttpServletRequest request, HttpServletResponse response,
											  @RequestParam(defaultValue="false") boolean isReAu){
		boolean isAuthorizationPending = false;
		 HttpSession session = request.getSession(false);
		 if (isReAu) {
			 this.reAuthenticate(request);
		 }
		 List<ShoppingCartItem> itemList = new LinkedList<ShoppingCartItem>();
		 itemList = this.getService().getShoppingBasketItems(request.getRemoteUser(), this.nodeName);
		 Map<String, ShoppingCartItem> shoppingCart = new LinkedHashMap<String, ShoppingCartItem>();
		 if (itemList != null) {
			 for (ShoppingCartItem item : itemList) {
				 shoppingCart.put(item.getProductId() + item.getUniqueIdentifier(), item);
			 }
			 request.getSession().setAttribute(SHOPPING_CART + request.getRemoteUser(), shoppingCart);
		 }
		 ModelAndView modelAndView = this.getModelAndView(request, COMMON_HOME_FORWARD);

		 String url = null;
		 String viewName = null;
		 if (session != null) {
        	 SavedRequest savedRequest = sDLSavedRequestAwareAuthenticationSuccessHandler.getRequestCache()
        			.getRequest(request, response);
        	 if(savedRequest != null) {
	        	 url = savedRequest.getRedirectUrl();
	        	 viewName = "redirect:" + url;
        		 modelAndView.setViewName(viewName);
        		 /** Removed the Saved Request from the Request Cach e. So that the Request is not saved in the
        		  * Subsequent Request**/
        		 sDLSavedRequestAwareAuthenticationSuccessHandler.getRequestCache().removeRequest(request, response);
        	 }
        }
		User loggedInUser = this.getUser(request);
		modelAndView.addObject("user", loggedInUser);
		for (Access access : loggedInUser.getAccess()) {
				logger.debug("THE LOGGED IN USER ACCES DETAILS {} ", access.getCode());
		}
		isAuthorizationPending = loggedInUser.isAuthorizationPending();
		modelAndView.addObject("isAuthorizationPending", isAuthorizationPending);
		if (!loggedInUser.isAcceptedTerms()) {
			return this.getModelAndView(request, REDIRECT_ACCEPT_TERMS_CONDITIONS);
		} else if (loggedInUser.isPayedUser() && loggedInUser.isPaymentDue() || isAuthorizationPending) {
			User paidSubUnpaidUser = this.getService().getPaidSubUnpaidByUser(request.getRemoteUser(), this.nodeName);
			if (paidSubUnpaidUser == null) {
				return this.getModelAndView(request, LOGOUT);
			}
			List <Site> siteList = paidSubUnpaidUser.getSites();
			modelAndView.addObject("sites", siteList);
			boolean hasFreeAccess = false;
			boolean hasActivePaidAcccess = false;
			List <Access> accessList = loggedInUser.getAccess();
			Iterator<Access>  accessListIterator = accessList.iterator();
			while(accessListIterator.hasNext()){
				Access access = (Access)accessListIterator.next();
				if(AccessType.FREE_SUBSCRIPTION.equals(access.getAccessType()) && access.getSite().getName().equalsIgnoreCase(this.clientName)) {
					hasFreeAccess = true;
					break;
				} else if ((AccessType.NON_RECURRING_SUBSCRIPTION.equals(access.getAccessType()) ||
						   AccessType.RECURRING_SUBSCRIPTION.equals(access.getAccessType())) && access.getSite().getName().equalsIgnoreCase(this.clientName)) {
					if (access.isActive()) {
						hasActivePaidAcccess = true;
					}
				}
			}
			if (hasFreeAccess) {
				modelAndView.addObject("hasFreeAccess", hasFreeAccess);
			} else if (hasActivePaidAcccess) {
				modelAndView.addObject("hasActivePaidAcccess", hasActivePaidAcccess);
			} else {
				modelAndView.setViewName(ECOM_PAYMENT_AUTHORIZATION_PENDING);
			}
			modelAndView.addObject("serverUrl",this.ecomServerURL);
			logger.debug("REDIRECT TO PAYMENT PENDING PAGE =======>");
		} else if (loggedInUser.getAccess().size() == 1 && (loggedInUser.getAccess().get(0)).isGuestFlg()) {
			logger.debug("REDIRECT TO ACCOUNT SETTINGS PAGE =======>");
			modelAndView.setViewName(SECURITY_FORWARD_ACCOUNT_INFORMATION);
		}
		logger.debug("REDIRECT TO SEARCH PAGE =======>");
		return modelAndView;
	}

    @RequestMapping(value = "/accountInformation.admin", method = RequestMethod.GET)
    public ModelAndView accountInformation(HttpServletRequest request,
            @RequestParam(defaultValue = "false") boolean isReAu) {

        ModelAndView modelAndView = this.getModelAndView(request, SECURITY_ACCOUNT_INFORMATION);
        if (isReAu) {
            this.reAuthenticate(request);
        }

        List<SubscriptionDTO> subscriptionDTOs = this.getService().getUserSubscriptions(request.getRemoteUser(),
                this.nodeName, null, false, false);
        User user = this.getUser(request);
        int shoppingCartSize = 0;

        List<CreditCard> cardList = getService().getCreditCardDetailsList(user.getId());
        List<CreditCard> sortedCardList = cardList.stream().sorted((one, two) -> {
            return Boolean.compare(two.getDefaultCC(),  one.getDefaultCC());
        }).collect(Collectors.toList());

        List<ShoppingCartItem> itemList = new LinkedList<ShoppingCartItem>();
        itemList = this.getService().getShoppingBasketItems(request.getRemoteUser(), this.nodeName);
        Map<String, ShoppingCartItem> shoppingCart = new LinkedHashMap<String, ShoppingCartItem>();
        if (itemList != null) {
            for (ShoppingCartItem item : itemList) {
                shoppingCart.put(item.getProductId() + item.getUniqueIdentifier(), item);
            }
            request.getSession().setAttribute(SHOPPING_CART + request.getRemoteUser(), shoppingCart);
        } else {
            request.getSession().removeAttribute(SHOPPING_CART + request.getRemoteUser());
        }

        if (shoppingCart != null) {
            shoppingCartSize = shoppingCart.size();
        }

        modelAndView.addObject("shoppingCartSize", shoppingCartSize);
        modelAndView.addObject("user", user);
        modelAndView.addObject("creditCardList", sortedCardList);
        modelAndView.addObject("isFirmLevelAdministrator", this.isFirmLevelAdministrator(subscriptionDTOs));
        modelAndView.addObject("serverUrl", this.ecomServerURL);
        modelAndView.addObject("subscriptions", subscriptionDTOs);
        return modelAndView;
    }

	@RequestMapping(value="/getSubscriptionDetails.admin", method=RequestMethod.GET)
	public ModelAndView getSubscriptionDetails(@RequestParam("uaId") String accessId, HttpServletRequest request,
			@RequestParam(defaultValue="false") boolean isReAu) {
		ModelAndView modelAndView = this.getModelAndView(request, ECOM_GET_SUBSCRIPTION_DETAILS);
		if (isReAu) {
			this.reAuthenticate(request);
		}
		User user = this.getUser(request);
		SubscriptionDTO subscriptionDTO = this.getService().getSubscriptionDetails(request.getRemoteUser(),
				new Long(accessId));
		List<Access> accessList = this.getService().getAccessesForSite(subscriptionDTO.getSiteId().toString());
		List<Access> accessListFiltered = new LinkedList<Access>();
		if (subscriptionDTO.isVisible()) {
			 for (Access access : accessList) {
				 if (access.isVisible()) {
					 accessListFiltered.add(access);
				 }
			 }
		} else {
			accessListFiltered = accessList;
		}
		modelAndView.addObject("user", user);
		modelAndView.addObject("subscription", subscriptionDTO);
		modelAndView.addObject("serverUrl",this.ecomServerURL);
		modelAndView.addObject("clientUrl",this.ecomClientURL);
		modelAndView.addObject("siteAccessList", accessListFiltered);
		return modelAndView;
	}

	@RequestMapping(value="/cancelSubscription.admin", method=RequestMethod.POST)
	public ModelAndView cancelSubscription(HttpServletRequest request) {
		PayPalDTO payPalDTO = null;
		ModelAndView modelAndView = this.getModelAndView(request, ECOM_REDIRECT_CANCEL_SUBSCRIPTION);
		try {
			payPalDTO = this.getService().cancelSubscription(request.getRemoteUser(),
					Long.parseLong(request.getParameter("userAccessID")));
			request.getSession().removeAttribute("payPalDTO");
			request.getSession().removeAttribute("paypalDTOReActivate");
			request.getSession().setAttribute("payPalDTO", payPalDTO);
			if (payPalDTO != null && payPalDTO.getTxRefNum() != null) {
				request.getSession().setAttribute(SUCCESS_MSG,
					this.getMessage("paypal.cancelsubscription.successTransactionID"));
			} else {
				request.getSession().setAttribute(SUCCESS_MSG,
					this.getMessage("paypal.cancelsubscription.successNoTransactionID"));
			}
		} catch (PaymentGatewaySystemException payPalSystemException) {
			request.getSession().setAttribute(FAILURE_MSG, this.getMessage("paypal.errorcode.generalsystemerror"));
		} catch (SDLBusinessException sDLBusinessException) {
			request.getSession().setAttribute(BUSSINESS_EXCP,
					this.getMessage("security.ecommerce.alreadyCanceledSubscription"));
		}
		return modelAndView;
	}

	/** Redirect to ECommerce **/
	@RequestMapping(value="/viewAccountInformation.admin")
	public ModelAndView viewAccountInformation(HttpServletRequest request, HttpServletResponse response,
	        @RequestParam(defaultValue = "false") boolean addNewCard,
	        @RequestParam(defaultValue = "-1") Long creditCardId) {
		String return_url = request.getParameter("return_url");
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String serverUrl = this.ecomServerURL;
		String viewName = "redirect:" + serverUrl + "secure/viewAccountInformation.admin?"
				+ "token1="	  + PageStyleUtil.encrypt(((User)authentication.getPrincipal()).getUsername())
				+ "&token2="  + PageStyleUtil.encrypt(authentication.getCredentials().toString())
				+ "&token3="  + return_url
				+ "&token4="  + addNewCard
				+ "&token5="  + creditCardId;
		ModelAndView modelAndView = new ModelAndView(viewName);
		return modelAndView;
	}


	@RequestMapping(value="/cancelSubscriptionConfirmation.admin", method=RequestMethod.GET)
	public ModelAndView cancelSubscriptionConfirmation(HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, ECOM_CANCEL_SUBSCRIPTION);
		User user = this.getUser(request);
		modelAndView.addObject("user", user);
		this.populateErrorSuccessMsg(modelAndView, request);
		modelAndView.addObject("payPalDTO", request.getSession().getAttribute("payPalDTO"));
		modelAndView.addObject("isCancelRequest", "TRUE");
		modelAndView.addObject("isReactivateRequest", "FALSE");
		this.reAuthenticate(request);
		return modelAndView;
	}

	@RequestMapping(value="/reactivateSubscription.admin", method=RequestMethod.POST)
	public String reactivateSubscription(HttpServletRequest request) {
		PayPalDTO paypalDTO = null;
		try {
			paypalDTO = this.getService().reactivateCancelledSubscription(request.getRemoteUser(),
					Long.parseLong(request.getParameter("userAccessID")));
			request.getSession().removeAttribute("payPalDTO");
			request.getSession().removeAttribute("paypalDTOReActivate");
			request.getSession().setAttribute("paypalDTOReActivate", paypalDTO);
			if (paypalDTO != null) {
				request.getSession().setAttribute("SUCCESS_MSG",
						this.getMessage("paypal.reactivesubscription.success"));
			}
		}catch (PaymentGatewayUserException payPalUserException) {
			request.getSession().setAttribute(FAILURE_MSG, this.getMessage("paypal.errorcode."
				+ payPalUserException.getErrorCode()));
		} catch (PaymentGatewaySystemException payPalSystemException) {
			request.getSession().setAttribute(FAILURE_MSG, this.getMessage("paypal.errorcode.generalsystemerror"));
		}
		return SECURITY_REDIRECT_REACTIVATE_SUBSCRIPTION;
	}

	@RequestMapping(value="/reactivateSubscriptionConfirmation.admin", method=RequestMethod.GET)
	public ModelAndView reactivateSubscriptionConfirmation(HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, ECOM_REACTIVATE_SUBSCRIPTION);
		User user = this.getUser(request);
		modelAndView.addObject("user", user);
		this.populateErrorSuccessMsg(modelAndView, request);
		modelAndView.addObject("paypalDTOReActivate", request.getSession().getAttribute("paypalDTOReActivate"));
		modelAndView.addObject("isCancelRequest", "FALSE");
		modelAndView.addObject("isReactivateRequest", "TRUE");
		return modelAndView;
	}

	@RequestMapping(value="/viewAvailableSubscriptions.admin", method=RequestMethod.GET)
	public ModelAndView viewAvailableSubscriptions(ModelAndView modelAndView, HttpServletRequest request) {
		modelAndView = this.getModelAndView(request, ECOM_VIEW_AVAILABLE_SUBSCRIPTIONS);
		String siteName = null;
		User user = this.getUser(request);
		List<Site> siteList = this.getService().getSitesForNode(this.nodeName);
		List<SubscriptionDTO> subscriptionDTOs = this.getService().getUserSubscriptions(request.getRemoteUser(), this.nodeName, siteName, false, false);
		modelAndView.addObject("user", user);
		modelAndView.addObject("sites", siteList);
		modelAndView.addObject("subscriptions", subscriptionDTOs);
		if (request.getSession().getAttribute(BUSSINESS_EXCP) != null) {
			modelAndView.addObject(BUSSINESS_EXCP, request.getSession().getAttribute(BUSSINESS_EXCP));
		}
		if (request.getSession().getAttribute(FAILURE_MSG) != null) {
			modelAndView.addObject(FAILURE_MSG, request.getSession().getAttribute(FAILURE_MSG));
		}
		request.getSession().setAttribute("BUSSINESS_EXCP", null);
		request.getSession().setAttribute("FAILURE_MSG", null);
		return modelAndView;
	}

	@RequestMapping(value="/addSubscription.admin")
	public ModelAndView addSubscription(ModelAndView modelAndView, HttpServletRequest request) {
		modelAndView = this.getModelAndView(request, ECOM_REDIRECT_CONFIRM_ADD_SUBSCRIPTION);
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String nodeName = this.nodeName;
		String newSubscriptions[] = (String[])request.getParameterValues("newSubscription");
		List<Long> newAccessIdList = new LinkedList<Long>();
		List<AccessDetailDTO> accessDetailDTOList = new LinkedList<AccessDetailDTO>();
		if (newSubscriptions == null || newSubscriptions.length == 0) {
			request.getSession().setAttribute(FAILURE_MSG, this.getMessage("security.ecommerce.error.SelectaSubscription"));
			modelAndView.setViewName(ECOM_REDIRECT_VIEW_AVAILABLE_SUBSCRIPTIONS);
			return modelAndView;
		}
		for (String newAccessId : newSubscriptions) {
			newAccessIdList.add(new Long(newAccessId));
		}
		try {
			SubscriptionDTO subscriptionDTO =  new SubscriptionDTO();
			subscriptionDTO.setUser(user);
			subscriptionDTO.setNewAccessIds(newAccessIdList);
			subscriptionDTO.setNodeName(nodeName);
			accessDetailDTOList = this.getService().addSubscription(subscriptionDTO);
			this.reAuthenticate(request);
		} catch (SDLBusinessException sDLBusinessException) {
			request.getSession().setAttribute("BUSSINESS_EXCP", sDLBusinessException.getDescription());
			modelAndView.setViewName(ECOM_REDIRECT_VIEW_AVAILABLE_SUBSCRIPTIONS);
		}
		request.getSession().setAttribute("sesAccessDetailsList", accessDetailDTOList);
		return modelAndView;
	}

	@RequestMapping(value="/confirmAddSubscription.admin", method=RequestMethod.GET)
	public ModelAndView confirmAddSubscription(HttpServletRequest request) {
        Authentication newAuthentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) newAuthentication.getPrincipal();
        ModelAndView modelAndView = this.getModelAndView(request, ECOM_CONFIRM_ADD_SUBSCRIPTION);
		modelAndView.addObject("user" , user);
		modelAndView.addObject("serverUrl", this.ecomServerURL);
		modelAndView.addObject("clientURL", this.ecomClientURL);
		User loggedInUser = this.getUser(request);
		if (loggedInUser.isAcceptedTerms()) {
			modelAndView.addObject("acceptedTermsAndConditions", true);
		} else {
			modelAndView.addObject("acceptedTermsAndConditions", false);
		}
		List<AccessDetailDTO> accessDetailDTOList = (List<AccessDetailDTO>)request.getSession()
				.getAttribute("sesAccessDetailsList");
		modelAndView.addObject("accessDetailsList" , accessDetailDTOList);
		modelAndView.addObject(SUCCESS_MSG, this.getMessage("security.ecommerce.confirmAddSubscription"));
		return modelAndView;
	}

	@RequestMapping(value="/viewChangeSubscriptionInfoRequest.admin", method=RequestMethod.POST)
	public String viewChangeSubscriptionInfoRequest(HttpServletRequest request, @RequestParam("accessId") String accessId,
			@RequestParam(defaultValue="false") boolean isReAu) {
		if (isReAu) {
			this.reAuthenticate(request);
		}
		User user = this.getUser(request);
		request.getSession().removeAttribute(FAILURE_MSG);
		try {

			if (accessId != null && !accessId.isEmpty())  {
				UpgradeDowngradeDTO changeDTO = this.getService().getChangeSubscriptionInfo
						(Long.parseLong(request.getParameter("userAccessID")),
								Long.parseLong(accessId), user.getUsername());
				request.getSession().setAttribute("changeSubscriptionInfo", changeDTO);
			} else {
				request.getSession().setAttribute(FAILURE_MSG,
					this.getMessage("security.ecommerce.label.errorProcessingRequest"));
			}
		} catch (SDLBusinessException sDLBusinessException) {
			request.getSession().setAttribute(FAILURE_MSG, sDLBusinessException.getBusinessMessage());
		} catch (MaxUsersExceededException maxUserExceededException) {
			request.getSession().setAttribute(FAILURE_MSG, maxUserExceededException.getDescription());
		}
		return ECOM_REDIRECT_VIEW_CHANGE_SUBSCRIPTION_INFO;
	}

	@RequestMapping(value="/viewChangeSubscriptionInfo.admin", method=RequestMethod.GET)
	public ModelAndView viewChangeSubscriptionInfo(HttpServletRequest request,
			@RequestParam(defaultValue="false") boolean isReAu) {
		if (isReAu) {
			this.reAuthenticate(request);
		}
		ModelAndView modelAndView = this.getModelAndView(request, ECOM_VIEW_CHANGE_SUBSCRIPTION_INFO);
		User user = this.getUser(request);
		modelAndView.addObject("user", user);
		modelAndView.addObject("changeSubscriptionInfo", request.getSession().getAttribute("changeSubscriptionInfo"));
		if (request.getSession().getAttribute(FAILURE_MSG) != null) {
			modelAndView.addObject(FAILURE_MSG, request.getSession().getAttribute(FAILURE_MSG));
		}
		return modelAndView;
	}

	@RequestMapping(value="/changeSubscription.admin", method=RequestMethod.POST)
	public String changeSubscription(HttpServletRequest request) {
		String successMsg = null;
		String failureMsg = null;
		UpgradeDowngradeDTO changeDTO = null;
		try {
			changeDTO = this.getService().changeFromRecurringToRecurringSubscription(Long.parseLong(request.getParameter("currentUserAccessId")),
					Long.parseLong(request.getParameter("newAccessId")), request.getRemoteUser(), request.getRemoteAddr());
			successMsg = this.getMessage("security.ecommerce.changeSubscriptionStatus");
			request.getSession().setAttribute(SUCCESS_MSG, successMsg);
			this.reAuthenticate(request);
		} catch (SDLBusinessException sDLBusinessException) {
			request.getSession().setAttribute(BUSSINESS_EXCP, sDLBusinessException.getBusinessMessage());
		} catch (PaymentGatewayUserException payPalUserException) {
			failureMsg = this.getMessage("paypal.errorcode." + payPalUserException.getErrorCode());
			request.getSession().setAttribute(FAILURE_MSG, failureMsg);
		} catch (PaymentGatewaySystemException payPalSystemException) {
			failureMsg = this.getMessage("paypal.errorcode.generalsystemerror");
			request.getSession().setAttribute(FAILURE_MSG, failureMsg);
		} catch (MaxUsersExceededException maxUserExceededException) {
			request.getSession().setAttribute(BUSSINESS_EXCP, maxUserExceededException.getDescription());
		}
		request.getSession().setAttribute("changeDTO", changeDTO);
		return ECOM_REDIRECT_CHANGE_SUBSCRIPTION;
	}

	@RequestMapping(value="/changeSubscriptionConfirmation.admin", method=RequestMethod.GET)
	public ModelAndView changeSubscriptionConfirmation(HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, ECOM_CHANGE_SUBSCRIPTION);
		User user = this.getUser(request);
		modelAndView.addObject("user", user);
		this.populateErrorSuccessMsg(modelAndView, request);
		modelAndView.addObject("changeDTO", request.getSession().getAttribute("changeDTO"));
		return modelAndView;
	}

	@RequestMapping(value="/paymenthistory.admin")
	public ModelAndView getPaymentHistory(HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, ECOM_PAYMENT_HISTORY);
		User user = this.getUser(request);
		modelAndView.addObject("payAsUGoTxHistory", true);
		modelAndView.addObject("recurTxHistory", true);
		modelAndView.addObject("user", user);
		return modelAndView;
	}

	@RequestMapping(value="/recurringPaymentHistory.admin", method=RequestMethod.GET,  produces="application/json")
	@ResponseBody
	public List<RecurTx> getRecurringPaymentHistory(HttpServletRequest request, HttpServletResponse response) {
			User user = this.getUser(request);
			List<RecurTx> recurTxHistory = new LinkedList<RecurTx>();
			recurTxHistory = this.getService().getRecurTransactionsByNode(request.getRemoteUser(), this.nodeName);
		return recurTxHistory;
	}

	@RequestMapping(value="/payAsUGoPaymentHistory.admin", method=RequestMethod.GET,  produces="application/json")
	@ResponseBody
	public PageRecordsDTO getPayAsUGoPaymentHistory(HttpServletRequest request, @RequestParam(required=false) Date fromDate,
			@RequestParam(required=false) Date toDate, @RequestParam(required=false) String comments,
			@RequestParam(required=false) String transactionType, Integer skip, Integer take) {
		User user = this.getUser(request);
		return this.getService().getPayAsUGoTransactionsByNodePerPage(user.getUsername(), this.nodeName,
				comments, fromDate, toDate, transactionType, skip, take);
	}

	@RequestMapping(value="/recurringPaymentHistoryExport.admin")
    public void recurringPaymentHistoryExport(HttpServletRequest request, HttpServletResponse response, String exportType) {

		List<RecurTx> recurTxList = this.getService().getRecurTransactionsByNode(request.getRemoteUser(), this.nodeName);
        byte[] outputBytes = new byte[1];
        String fileExtension = "";
        try{
	        if(exportType.equals(EXCEL)){
	        	// Create Excel File contents
	        	List<List<String>> transactions = new ArrayList<List<String>>();
	        	for(RecurTx tx : recurTxList){
	        		transactions.add(this.getRecurTxRowForExcel(tx));
	        	}
	        	ExcelExport export = new ExcelExport();
	        	outputBytes = export.exportToExcel(ExportConstants.getRecurHeaders(), transactions);
	        	fileExtension = "xls";

	        } else if (exportType.equals(PDF)){
	        	// Create PDF file contents
	        	List<List<PDFCell>> transactions = new ArrayList<List<PDFCell>>();
	        	for(RecurTx tx : recurTxList){
	        		transactions.add(this.getRecurTxRowForPDF(tx));
	        	}
	        	PDFExport export = new PDFExport(RECUR_PDF_COLUMN_WIDTHS.length, RECUR_PDF_COLUMN_WIDTHS);
	        	outputBytes = export.exportToPDF(ExportConstants.getRecurHeaders(), transactions);
	        	fileExtension = "pdf";
	        }
	        response.reset();
			response.resetBuffer();
			response.setContentType("application/x-download");
			response.setHeader("Content-Disposition",  "attachment; filename=RecurringPaymentHistory." + fileExtension);
			response.setHeader("Expires", " 0");
			response.setHeader("Cache-Control", " must-revalidate, post-check=0, pre-check=0" );
			response.setHeader("Pragma" , "public");
			ByteArrayOutputStream outputStream =  new ByteArrayOutputStream();
			outputStream.write(outputBytes);
			outputStream.writeTo(response.getOutputStream());
			response.getOutputStream().flush();
        } catch(Exception e){
        	logger.error("Error while writing exporting Recurring Transactions " , e);
        }

	}

	@RequestMapping(value="/payAsUGoPaymentHistoryExport.admin")
    public void payAsUGoPaymentHistoryExport(HttpServletRequest request, HttpServletResponse response, String exportType,
    		@RequestParam(required=false) String comments,
    		@RequestParam(required=false) Date fromDate,
			@RequestParam(required=false)  Date toDate) {
		List<PayAsUGoTxView> payAsUGoTxList = this.getService().getPayAsUGoTransactionsByNode(request.getRemoteUser(),
				this.nodeName, comments, fromDate, toDate);
        byte[] outputBytes = new byte[1];
        String fileExtension = "";
        try{
	        if(exportType.equals(EXCEL)){
	        	// Create Excel File contents
	        	List<List<String>> transactions = new ArrayList<List<String>>();
	        	for(PayAsUGoTxView tx : payAsUGoTxList){
	        		transactions.add(getPayAsUGoTxRowForExcel(tx));
	        	}
	        	ExcelExport export = new ExcelExport();
	        	outputBytes = export.exportToExcel(ExportConstants.getPayAsUGoHeaders(), transactions);
	        	fileExtension = "xls";
	        } else if (exportType.equals(PDF)){
	        	// Create PDF file contents
	        	List<List<PDFCell>> transactions = new ArrayList<List<PDFCell>>();
	        	for(PayAsUGoTxView tx : payAsUGoTxList){
	        		transactions.add(getPayAsUGoTxRowForPDF(tx));
	        	}
	        	PDFExport export = new PDFExport(PAYASUGO_PDF_COLUMN_WIDTHS.length, PAYASUGO_PDF_COLUMN_WIDTHS);
	        	outputBytes = export.exportToPDF(ExportConstants.getPayAsUGoHeaders(), transactions);
	        	fileExtension = "pdf";
	        }
	        response.reset();
			response.resetBuffer();
			response.setContentType("application/x-download");
			response.setHeader("Content-Disposition",  "attachment; filename=PayAsUGoHistory." + fileExtension);
			response.setHeader("Expires", " 0");
			response.setHeader("Cache-Control", " must-revalidate, post-check=0, pre-check=0" );
			response.setHeader("Pragma" , "public");
			ByteArrayOutputStream outputStream =  new ByteArrayOutputStream();
			outputStream.write(outputBytes);
			outputStream.writeTo(response.getOutputStream());
			response.getOutputStream().flush();
        } catch(Exception e){
        	logger.error("Error while writing exporting Pay As You Go Transactions " , e);
        }

    }

	@RequestMapping(value="/deleteUserAlert.admin", method=RequestMethod.GET)
	public ModelAndView deleteUserAlert(HttpServletRequest request, @RequestParam Long id) {
		ModelAndView modelAndView = this.getModelAndView(request, ALERTS_REDIRECT_GET_USER_ALERTS);
		List<Long> userAlertIdList = new LinkedList<Long>();
		userAlertIdList.add(id);
		UserAlertDTO userAlertDTO = new UserAlertDTO();
		userAlertDTO.setUserAlertIdList(userAlertIdList);
		userAlertDTO.setUserName(request.getRemoteUser());
		this.getService().deleteUserAlerts(userAlertDTO);
		return modelAndView;
	}

	 private Table getHtmlExportTable(String reportId, String reportName) {
		Table table = new HtmlTable().caption(reportName);
		Row row = new HtmlRow();
		table.setRow(row);
		if (reportId == "recurTxHistory"){
			Column transactionDate = new HtmlColumn("transactionDate").title("Payment Date");
			transactionDate.setCellEditor(new DateCellEditor("MM-dd-yyyy HH:mm:ss z"));
			row.addColumn(transactionDate);
			Column transReferenceNum = new HtmlColumn("txRefNum").title("Transaction Reference Number");
			row.addColumn(transReferenceNum);
			Column siteName = new HtmlColumn("site.description").title("Site Name");
			row.addColumn(siteName);
			Column accessDescription = new HtmlColumn("access.description").title("Subscription");
			row.addColumn(accessDescription);
			Column cardNumber = new HtmlColumn("cardNumber").title("Card Charged");
			row.addColumn(cardNumber);
			Column accountHolderName = new HtmlColumn("accountName").title("Name on Card");
			row.addColumn(accountHolderName);
			Column transactionType = new HtmlColumn("transactionType").title("Transaction Type");
			transactionType.setCellEditor(new CellEditor() {
	            public Object getValue(Object item, String property, int rowcount) {
	            	Double value = new Double(new HtmlCellEditor().getValue(item, "totalTxAmount", rowcount).toString());
	            	if (value < 0) {
	            		return "REFUND";
	            	} else {
	            		return "CHARGE";
	            	}
	            }
	        });
			row.addColumn(transactionType);
			Column totalAmount = new HtmlColumn("totalTxAmount").title("Amount");
			totalAmount.setCellEditor(new NumberCellEditor("$###,##0.00"));
			row.addColumn(totalAmount);
		} else if (reportId == "webTxHistory") {
			Column transactionDate = new HtmlColumn("transactionDate").title("Payment Date");
			transactionDate.setCellEditor(new DateCellEditor("MM-dd-yyyy HH:mm:ss z"));
			row.addColumn(transactionDate);
			Column transReferenceNum = new HtmlColumn("txRefNum").title("Transaction Reference Number");
			row.addColumn(transReferenceNum);
			Column txType = new HtmlColumn("transactionType").title("Transaction Type");
			row.addColumn(txType);
			Column siteName = new HtmlColumn("site.description").title("Site Name");
			row.addColumn(siteName);
			Column cardNumber = new HtmlColumn("cardNumber").title("Card Charged");
			row.addColumn(cardNumber);
			Column accountName = new HtmlColumn("accountName").title("Name on Card");
			row.addColumn(accountName);
			Column txAmount = new HtmlColumn("totalTxAmount").title("Amount Charged");
			txAmount.setCellEditor(new NumberCellEditor("$###,##0.00"));
			row.addColumn(txAmount);
		}
		return table;
	}

    private Table getExportTable(String reportId, String reportName) {
    	Table table = new Table().caption(reportName);
		Row row = new Row();
		table.setRow(row);
		if (reportId == "recurTxHistory"){
			Column transactionDate = new Column("transactionDate").title("Payment Date");
			transactionDate.setCellEditor(new DateCellEditor("MM-dd-yyyy HH:mm:ss z"));
			row.addColumn(transactionDate);
			Column transReferenceNum = new Column("txRefNum").title("Transaction Reference Number");
			row.addColumn(transReferenceNum);
			Column siteName = new Column("site.description").title("Site Name");
			row.addColumn(siteName);
			Column accessDescription = new Column("access.description").title("Subscription");
			row.addColumn(accessDescription);
			Column cardNumber = new Column("cardNumber").title("Card Charged");
			row.addColumn(cardNumber);
			Column accountHolderName = new Column("accountName").title("Name on Card");
			row.addColumn(accountHolderName);
			Column transactionType = new Column("transactionType").title("Transaction Type");
			transactionType.setCellEditor(new CellEditor() {
	            public Object getValue(Object item, String property, int rowcount) {
	            	Double value = new Double(new HtmlCellEditor().getValue(item, "totalTxAmount", rowcount).toString());
	            	if (value < 0) {
	            		return "REFUND";
	            	} else {
	            		return "CHARGE";
	            	}
	            }
	        });
			row.addColumn(transactionType);
			Column totalAmount = new Column("totalTxAmount").title("Amount");
			totalAmount.setCellEditor(new NumberCellEditor("$###,##0.00"));
			row.addColumn(totalAmount);
		} else if (reportId == "webTxHistory") {
			Column transactionDate = new Column("transactionDate").title("Payment Date");
			transactionDate.setCellEditor(new DateCellEditor("MM-dd-yyyy HH:mm:ss z"));
			row.addColumn(transactionDate);
			Column transReferenceNum = new Column("txRefNum").title("Transaction Reference Number");
			row.addColumn(transReferenceNum);
			Column txType = new Column("transactionType").title("Transaction Type");
			row.addColumn(txType);
			Column siteName = new Column("site.description").title("Site Name");
			row.addColumn(siteName);
			Column cardNumber = new Column("cardNumber").title("Card Charged");
			row.addColumn(cardNumber);
			Column accountName = new Column("accountName").title("Name on Card");
			row.addColumn(accountName);
			Column txAmount = new Column("totalTxAmount").title("Amount Charged");
			txAmount.setCellEditor(new NumberCellEditor("$###,##0.00"));
			row.addColumn(txAmount);
		}
		return table;
    }

    @RequestMapping(value="/viewRecurringPaymentDetails.admin", method=RequestMethod.GET)
	public ModelAndView getRecurringPaymentDetails(HttpServletRequest request, @RequestParam String recurTxRefNum) {
		ModelAndView modelAndView = this.getModelAndView(request, ECOM_RECURRING_PAYMENT_DETAILS);
		User user = this.getUser(request);
		List<RecurTx> recurTxHistInfoList = this.getService().getRecurTxDetail(recurTxRefNum, request.getRemoteUser());
		modelAndView.addObject("user", user);
		modelAndView.addObject("recurTxHistInfoList", recurTxHistInfoList);
		return modelAndView;
	}

	@RequestMapping(value="/viewPayAsUGoPaymentDetails.admin", method=RequestMethod.GET)
	public ModelAndView getPayAsUGoPaymentDetails(HttpServletRequest request, @RequestParam Long webTxId,
			@RequestParam (required=false) String userName,  @RequestParam(defaultValue="N") String isRefund) {
		ModelAndView modelAndView = this.getModelAndView(request, ECOM_PAYASUGO_PAYMENT_DETAILS);
		User user = this.getUser(request);
		if(StringUtils.isBlank(userName)) {
			userName = user.getUsername();
		}
		PayAsUGoTx payAsUGoTransaction = this.getService().getPayAsUGoTransactionDetail(webTxId, userName, isRefund);
		modelAndView.addObject("user", user);
		modelAndView.addObject("payAsUGoTransaction", payAsUGoTransaction);
		return modelAndView;
	}

	@RequestMapping(value="/viewNewTerms.admin", method=RequestMethod.GET)
	public ModelAndView getNewTermsAndConditionsforUser(HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, ACCEPT_TERMS_CONDITIONS);
		User user = this.getUser(request);
		List<Term> newTermsList = new LinkedList<Term>();
		newTermsList = this.getService().getNewTermsAndConditionsforUser(request.getRemoteUser(), this.nodeName);
		modelAndView.addObject("user", user);
		modelAndView.addObject("newTermsList", newTermsList);
		return modelAndView;
	}

	@RequestMapping(value="/acceptNewTerms.admin", method=RequestMethod.POST)
	public String acceptNewTerms(HttpServletRequest request, RedirectAttributes redirectAttributes) {
		String viewName = REDIRECT_CHECK_SUBSCRIPTION;
		String acceptedTermsList[] = (String[])request.getParameterValues("newTerms");
		User user = this.getUser(request);
		List<Term> newTermsList = new LinkedList<Term>();

		if (acceptedTermsList == null || acceptedTermsList.length == 0) {
			redirectAttributes.addFlashAttribute(FAILURE_MSG, this.getMessage("security.ecommerce.error.acceptTerms"));
			viewName = REDIRECT_ACCEPT_TERMS_CONDITIONS;
			return viewName;
		}

		for (String newTermId : acceptedTermsList) {
			Term term = new Term();
			term.setId(new Long(newTermId));
			newTermsList.add(term);
		}
		user.setTerms(newTermsList);
		try {
			this.getService().updateUserTerms(user);
		} catch (UserNameNotFoundException userNameNotFoundException) {
			viewName = COMMON_GENERAL_ERROR;
			logger.error("There is an Error at the UI Layer for acceptNewTerms() ", userNameNotFoundException.getDescription());
		}
		return viewName;
	}
	
	@RequestMapping(value="/removeCard.admin", produces="application/json")
    @ResponseBody
    public List<ErrorCode> removeCard(HttpServletRequest request, @RequestParam(required = false) String username, @RequestParam(required = false) String creditCardId,
    		@RequestParam(required = false) String comments, RedirectAttributes redirectAttributes) {
        List<ErrorCode> errors = new LinkedList<ErrorCode>();
        try {
                boolean isCardRemoved = this.getService().removeCard(username, creditCardId);
                if (isCardRemoved) {
	                ErrorCode error = new ErrorCode();
	        		error.setCode("SUCCESS");
	                error.setDescription("Subscription removed successfully");
	                errors.add(error);
                } else {
                	ErrorCode error = new ErrorCode();
            		error.setCode("ERROR");
                    error.setDescription("Something went Wrong.");
                    errors.add(error);
                }
        } catch (Exception sdlBusinessException) {
            	ErrorCode error = new ErrorCode();
        		error.setCode("ERROR");
                error.setDescription(sdlBusinessException.getMessage());
                errors.add(error);
            }
       return errors;
    }

	private boolean isFirmLevelAdministrator(List<SubscriptionDTO> subscriptions){
		boolean firmLevelAdministrator = false;
		for(SubscriptionDTO subscriptionDTO : subscriptions){
			if(subscriptionDTO.isFirmAccessAdmin() && subscriptionDTO.isActive()){
				firmLevelAdministrator = true;
				break;
			}
		}
		return firmLevelAdministrator;
	}

	private List<String> getPayAsUGoTxRowForExcel(PayAsUGoTxView payAsUGoTx){
		List<String> row = new ArrayList<String>();
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa z");

		String paymentDate = payAsUGoTx.getTransactionDate() != null ? df.format(payAsUGoTx.getTransactionDate()) : "";
		row.add(payAsUGoTx.getUserName());
		row.add(paymentDate);
		row.add(payAsUGoTx.getTxRefNum());
		row.add(payAsUGoTx.getTransactionType().toString());
		row.add(payAsUGoTx.getSiteDescription());
		row.add(payAsUGoTx.getSubscription());
		row.add(payAsUGoTx.getCardNumber());
		row.add(payAsUGoTx.getAccountName());
		row.add(String.format( "%.2f", payAsUGoTx.getTotalTxAmount()));
		return row;
	}

	private List<PDFCell> getPayAsUGoTxRowForPDF(PayAsUGoTxView payAsUGoTx ){
		List<PDFCell> cells = new ArrayList<PDFCell>();
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa z");
		String paymentDate = payAsUGoTx.getTransactionDate() != null ? df.format(payAsUGoTx.getTransactionDate()) : "";
		cells.add(new PDFCell(payAsUGoTx.getUserName(), false));
		cells.add(new PDFCell(paymentDate, false));
		cells.add(new PDFCell(payAsUGoTx.getTxRefNum(), false));
		cells.add(new PDFCell(payAsUGoTx.getTransactionType().toString(), false));
		cells.add(new PDFCell(payAsUGoTx.getSiteDescription(), false));
		cells.add(new PDFCell(payAsUGoTx.getSubscription(), true));
		cells.add(new PDFCell(payAsUGoTx.getCardNumber(), false));
		cells.add(new PDFCell(payAsUGoTx.getAccountName(), true));
		cells.add(new PDFCell("$" + String.format( "%.2f", payAsUGoTx.getTotalTxAmount()), false));
		return cells;
	}

	private List<String> getRecurTxRowForExcel(RecurTx recurTx){
		List<String> row = new ArrayList<String>();
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa z");

		String paymentDate = recurTx.getTransactionDate() != null ? df.format(recurTx.getTransactionDate()) : "";
		row.add(paymentDate);
		row.add(recurTx.getTxRefNum());
		row.add(recurTx.getSite().getDescription());
		row.add(recurTx.getAccess().getDescription());
		row.add(recurTx.getCardNumber());
		row.add(recurTx.getAccountName());
		row.add(recurTx.getTransactionType().toString());
		row.add(String.format( "%.2f", recurTx.getTotalTxAmount()));
		return row;
	}

	private List<PDFCell> getRecurTxRowForPDF(RecurTx recurTx){
		List<PDFCell> cells = new ArrayList<PDFCell>();

		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa z");
		String paymentDate = recurTx.getTransactionDate() != null ? df.format(recurTx.getTransactionDate()) : "";
		cells.add(new PDFCell(paymentDate, false));
		cells.add(new PDFCell(recurTx.getTxRefNum(), false));
		cells.add(new PDFCell(recurTx.getSite().getDescription(), false));
		cells.add(new PDFCell(recurTx.getAccess().getDescription(), true));
		cells.add(new PDFCell(recurTx.getCardNumber(), false));
		cells.add(new PDFCell(recurTx.getAccountName(), true));
		cells.add(new PDFCell(recurTx.getTransactionType().toString(), false));
		cells.add(new PDFCell("$" + String.format( "%.2f", recurTx.getTotalTxAmount()), false));
		return cells;
	}

}
