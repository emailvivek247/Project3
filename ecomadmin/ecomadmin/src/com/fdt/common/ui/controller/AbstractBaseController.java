package com.fdt.common.ui.controller;

import static com.fdt.ecomadmin.ui.controller.ViewConstants.GENERAL_ERROR;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.util.SystemUtil;
import com.fdt.common.util.rest.ServiceStubRS;
import com.fdt.ecom.entity.Site;
import com.fdt.security.entity.EComAdminSite;
import com.fdt.security.entity.EComAdminUser;
import com.fdt.security.service.EComAdminUserService;

/**@SessionAttributes({"sites"}) **/
public abstract class AbstractBaseController implements ServletContextAware {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/** Used to Store the Success Message **/
	protected final static String SUCCESS_MSG = "SUCCESS_MSG";

	/** Used to Store the Failure Message **/
	protected static String FAILURE_MSG = "FAILURE_MSG";

	@Value("${role.psotxadmin}")
	private String PSOTxAdmin = null;

	@Value("${role.psosuperadmin}")
	private String PSOSuperAdmin = null;

	@Value("${role.psouseradmin}")
	private String PSOUserAdmin = null;

	@Autowired(required = true)
	protected EComAdminUserService eComAdminUserService = null;

	@Autowired(required = true)
	protected ServiceStubRS serviceStub = null;

	@Autowired
	@Qualifier("messageSource")
	private MessageSource messages = null;

	private ServletContext servletContext = null;

	public ServletContext getServletContext() {
		return servletContext;
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	protected String getMessage(String msgCode) {
		return this.messages.getMessage(msgCode, null,  new Locale("en"));
	}

	@ExceptionHandler(HttpClientErrorException.class)
	protected ModelAndView handleHttpClientErrorException(HttpServletRequest request, HttpClientErrorException exception) {
		ModelAndView modelAndView = this.getModelAndView(request, GENERAL_ERROR);
		logger.error("The WebServices May Be Down", exception);
		if (exception.getStatusCode() == HttpStatus.FORBIDDEN) {
			modelAndView.addObject("generalException", "UnAuthorized WebServices Access " + exception.getStatusText());
		} else {
			modelAndView.addObject("generalException", "Error Calling WebServices " + exception.getMessage());
		}
		return modelAndView;
	}

	@ExceptionHandler(HttpServerErrorException.class)
	protected ModelAndView handleHttpServerErrorException(HttpServletRequest request, HttpServerErrorException exception) {
		ModelAndView modelAndView = this.getModelAndView(request, GENERAL_ERROR);
		logger.error("There is an Error in the WebServices", exception);
		modelAndView.addObject("generalException", "Error Thrown in the WebService " + exception.getMessage());
		return modelAndView;
	}

	@ExceptionHandler(ResourceAccessException.class)
	protected ModelAndView handleResourceAccessException(HttpServletRequest request, ResourceAccessException exception) {
		ModelAndView modelAndView = this.getModelAndView(request, GENERAL_ERROR);
		logger.error("The WebServices is pointing to a Wrong URL ", exception);
		modelAndView.addObject("generalException", "The WebServices is pointing to a Wrong URL " + exception.getMessage());
		return modelAndView;
	}

	@ExceptionHandler(Exception.class)
	protected ModelAndView handleAdminExceptions(HttpServletRequest request, Exception exception) {
		ModelAndView modelAndView = this.getModelAndView(request, GENERAL_ERROR);
		String generalException = null;
		logger.error("There is an Error at the UI Layer", exception);
		generalException =  "Error : " + exception;
		if (exception instanceof BeanCreationException) {
			generalException =  "The Web Services is DOWN : " + exception;
			logger.error("The Web Services is DOWN", exception);
		}
		modelAndView.addObject("generalException", generalException);
		return modelAndView;
	}

	protected static void verifyBinding(BindingResult result) {
		String[] suppressedFields = result.getSuppressedFields();
		if (suppressedFields.length > 0) {
			throw new RuntimeException("Attempting to bind suppressed fields: "
										+ StringUtils.arrayToCommaDelimitedString(suppressedFields));
		}
	}

	protected static String verifyBindingInJSON(BindingResult result) {
		String[] suppressedFields = result.getSuppressedFields();
		if (suppressedFields.length > 0) {
			return StringUtils.arrayToCommaDelimitedString(suppressedFields);
		}
		return null;
	}

	protected ModelAndView getModelAndView(HttpServletRequest request, String viewName) {
		ModelAndView modelAndView = new ModelAndView(viewName);
		modelAndView.addObject("request", request);
		modelAndView.addObject("buildVersion", SystemUtil.getBuildVersion());
		modelAndView.addObject("buildDate", SystemUtil.getBuildDate());
		modelAndView.addObject("topMenu", "topMenu");
		modelAndView.addObject("subMenu", "subMenu");
		return modelAndView;
	}

	protected ModelAndView getModelAndView(HttpServletRequest request, String viewName, String topMenu, String subMenu) {
		ModelAndView modelAndView = new ModelAndView(viewName);
		modelAndView.addObject("request", request);
		modelAndView.addObject("buildVersion", SystemUtil.getBuildVersion());
		modelAndView.addObject("buildDate", SystemUtil.getBuildDate());
		modelAndView.addObject("topMenu", topMenu);
		modelAndView.addObject("subMenu", subMenu);
		return modelAndView;
	}

	protected List<Site> getAssignedSites(HttpServletRequest request) {
		List<Site> newSiteList = null;
		List<EComAdminSite> userAssignedSites = new LinkedList<EComAdminSite>();
		UsernamePasswordAuthenticationToken userPasswordAuthToken
				= (UsernamePasswordAuthenticationToken)request.getUserPrincipal();
		Object user = userPasswordAuthToken.getPrincipal();

		List<Site> sites = this.getServiceStub().getSites();
		if  (user instanceof EComAdminUser) {
			EComAdminUser eComAdminUser = (EComAdminUser)user;
			userAssignedSites = eComAdminUser.getSites();
			newSiteList = new LinkedList<Site>();
			for (Site site : sites) {
				for (EComAdminSite ecomAdminSite : userAssignedSites) {
					if (site.getId().longValue() == ecomAdminSite.getId().longValue()) {
						newSiteList.add(site);
					}
				}
			}
		} else if (user instanceof LdapUserDetailsImpl ){
			newSiteList = sites;
		}
		return newSiteList;
	}

	protected boolean isInternalUser(HttpServletRequest request) {
		boolean isInternaluser = false;
		UsernamePasswordAuthenticationToken userPasswordAuthToken
				= (UsernamePasswordAuthenticationToken)request.getUserPrincipal();
		Object user = userPasswordAuthToken.getPrincipal();
		if (user instanceof LdapUserDetailsImpl ){
			isInternaluser = true;
		}
		return isInternaluser;
	}

	protected boolean isFeatureEnabledForUser(HttpServletRequest request, String featureCode) {
		boolean isFeatureEnabledForUser = false;
		if ((request.isUserInRole(PSOSuperAdmin) || request.isUserInRole(PSOTxAdmin)
				|| request.isUserInRole("C_RECURRINGTX_ADMIN")) && featureCode == "RecurringTransactions") {
			isFeatureEnabledForUser = true;
		} else if ((request.isUserInRole(PSOSuperAdmin) || request.isUserInRole(PSOTxAdmin)
				|| request.isUserInRole("C_WEBTX_ADMIN")) && featureCode == "WebTransactions") {
			isFeatureEnabledForUser = true;
		} else if ((request.isUserInRole(PSOSuperAdmin) || request.isUserInRole(PSOTxAdmin)
				|| request.isUserInRole("C_PAYASUGOTX_ADMIN")) && featureCode == "PayAsUGoTransactions") {
			isFeatureEnabledForUser = true;
		} else if ((request.isUserInRole(PSOSuperAdmin) || request.isUserInRole(PSOTxAdmin)
				|| request.isUserInRole("C_OTCTX_ADMIN") || request.isUserInRole("C_WEBTX_ADMIN")
				|| request.isUserInRole("C_PAYASUGOTX_ADMIN") || request.isUserInRole("C_RECURRINGTX_ADMIN"))
				&& featureCode == "GTReport") {
			isFeatureEnabledForUser = true;
		} else if ((request.isUserInRole(PSOSuperAdmin) || request.isUserInRole(PSOTxAdmin)
				|| request.isUserInRole("C_OTCTX_ADMIN")) && featureCode == "OTCTransactions") {
			isFeatureEnabledForUser = true;
		} else if ((request.isUserInRole(PSOSuperAdmin) || request.isUserInRole(PSOTxAdmin)
				|| request.isUserInRole("C_REFUNDTX_ADMIN")) && featureCode == "RefundTransaction") {
			isFeatureEnabledForUser = true;
		} else if ((request.isUserInRole(PSOSuperAdmin) || request.isUserInRole("C_CHECKPRINT_ADMIN"))
				&& featureCode == "CheckPrinting") {
			isFeatureEnabledForUser = true;
		} else if ((request.isUserInRole(PSOSuperAdmin) || request.isUserInRole("C_ACHPAYMENT_ADMIN"))
					&& featureCode == "ACHPayment") {
				isFeatureEnabledForUser = true;
		} else if ((request.isUserInRole(PSOSuperAdmin) || request.isUserInRole("C_CHECKHISTORY_ADMIN"))
				&& featureCode == "CheckHistory") {
			isFeatureEnabledForUser = true;
		} else if ((request.isUserInRole(PSOSuperAdmin) ||  request.isUserInRole(PSOUserAdmin)
				|| request.isUserInRole("C_USER_ADMIN")) && featureCode == "UserOperations") {
			isFeatureEnabledForUser = true;
		} else if ((request.isUserInRole(PSOSuperAdmin) ||  request.isUserInRole(PSOUserAdmin)
				|| request.isUserInRole("C_RECURRINGTX_ADMIN") || request.isUserInRole("C_OTCTX_ADMIN")
				|| request.isUserInRole("C_WEBTX_ADMIN") || request.isUserInRole("C_PAYASUGOTX_ADMIN") ||
				request.isUserInRole("C_REFUNDTX_ADMIN"))
				&& featureCode == "TransactionLookup") {
			isFeatureEnabledForUser = true;
		}
		return isFeatureEnabledForUser;
	}

	protected boolean isPSOSuperAdmin(HttpServletRequest request) {
		return request.isUserInRole(PSOSuperAdmin);
	}

	protected boolean isPSOTxAdmin(HttpServletRequest request) {
		return request.isUserInRole(PSOTxAdmin);
	}

	protected boolean checkLoggedinUserSiteValidity(HttpServletRequest request, Long siteId) {
		boolean isValidSiteAdmin = false;

		if (!this.isInternalUser(request)) {
			List<Site> sites = this.getAssignedSites(request);
			for (Site site : sites) {
				if (siteId == site.getId()) {
					isValidSiteAdmin = true;
				}
			}
		} else if(this.isInternalUser(request)) {
			isValidSiteAdmin = true;
		}
		return isValidSiteAdmin;
	}

	public ServiceStubRS getServiceStub() {
		return serviceStub;
	}

	public void setServiceStub(ServiceStubRS serviceStub) {
		this.serviceStub = serviceStub;
	}

}