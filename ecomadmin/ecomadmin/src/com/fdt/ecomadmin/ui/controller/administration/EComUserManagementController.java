package com.fdt.ecomadmin.ui.controller.administration;

import static com.fdt.common.util.JsonResponse.ERROR;
import static com.fdt.common.util.JsonResponse.SUCCESS;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.TOP_USER_ADMIN;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.EXCEL;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.FIND_USERS;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.KENDOUI_SORT_FIELD_PARAM;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.KENDOUI_SORT_TYPE_PARAM;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.LOGINSUCCESS;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.PDF;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_CREDIT_CARD_DETAILS;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_USER_DETAILS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.metadata.ConstraintDescriptor;

import org.apache.commons.lang.StringUtils;
import org.jmesa.model.TableModel;
import org.jmesa.view.component.Row;
import org.jmesa.view.component.Table;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.editor.DateCellEditor;
import org.jmesa.view.html.component.HtmlColumn;
import org.jmesa.view.html.component.HtmlRow;
import org.jmesa.view.html.component.HtmlTable;
import org.jmesa.view.html.editor.HtmlCellEditor;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fdt.common.dto.ErrorCodeDTO;
import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.export.ExcelExport;
import com.fdt.common.export.PDFCell;
import com.fdt.common.export.PDFExport;
import com.fdt.common.ui.breadcrumbs.Link;
import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.common.util.JsonResponse;
import com.fdt.common.util.SystemUtil;
import com.fdt.common.util.spring.PayAsUGoTxSerializer;
import com.fdt.common.util.spring.RecurTxSerializer;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.Site;
import com.fdt.ecomadmin.ui.controller.util.ExportConstants;
import com.fdt.ecomadmin.ui.form.CreditCardForm;
import com.fdt.ecomadmin.ui.form.CreditCardForm.DefaultGroup;
import com.fdt.ecomadmin.ui.form.UserDetails;
import com.fdt.ecomadmin.ui.validator.CreditCardFormValidator;
import com.fdt.payasugotx.entity.PayAsUGoTx;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.recurtx.entity.RecurTx;
import com.fdt.security.dto.FirmUserDTO;
import com.fdt.security.dto.SearchCriteriaDTO;
import com.fdt.security.entity.Access;
import com.fdt.security.entity.User;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.subscriptions.dto.SubscriptionDTO;

@Controller
public class EComUserManagementController extends AbstractBaseController {

	public static String WAITING_FOR_AUTHORIZATION = "waitingForAuthorization";
	public static String USERACCESS_ACTIVE = "paymentDue";
	public static String HAS_OVERRIDEN_ACCESS = "accessOverridden";
	public static String CURRENT_LOGIN_TIME = "lastLoginTime";
	public static String DATE_TIME_CREATED = "createdDate";
	public static String PAYED_USER = "payedUser";
	public static String USER_ACTIVE = "active";
	public static String FIRM_NAME = "firmName";
	public static String FIRM_NUMBER = "firmNumber";
	public static String ACCOUNT_NON_LOCKED = "accountNonLocked";
	public static String FIRM_ADMIN = "firmAdmin";
	
	
	private static int[] PDF_USER_COLUMN_WIDTHS = new int[] {21,8,9,9,9,10,9,8,9,6,8,20,20,14,12,10};


    @Autowired(required = true)
    private Validator validator;

    @Autowired
    @Qualifier(value="creditCardFormValidator")
    private CreditCardFormValidator creditCardValidator;

    /** Used to Store the User Not Found Message **/
    protected final static String USER_NOT_FOUND_MSG = "USER_NOT_FOUND_MSG";

    @RequestMapping(value="/enabledisableaccess.admin", produces="application/json")
    @ResponseBody
    public List<ErrorCodeDTO> enableDisableAccess(HttpServletRequest request, @RequestParam(required = false) String username,
    	@RequestParam(required = false) String nodename, @RequestParam(required = false) Long useraccessid,
    		@RequestParam(required = false) Boolean enableaccess , @RequestParam(required = false) String comments, @RequestParam(required = false) String endDate,
    		RedirectAttributes redirectAttributes) {
    	List<ErrorCodeDTO> errors = new LinkedList<ErrorCodeDTO>();
        boolean isValidSiteAdmin = false;
        Site authorizingSite = this.getServiceStub().getUserAccessDetails(useraccessid).getSite();
        if (!this.isInternalUser(request)) {
            List<Site> sites = this.getAssignedSites(request);
            for (Site site : sites) {
                if (authorizingSite.getId() == site.getId()) {
                    isValidSiteAdmin = true;
                }
            }
        } else if(this.isInternalUser(request)) {
            isValidSiteAdmin = true;
        }
        if (isValidSiteAdmin) {
            try {
            	this.getServiceStub().enableDisableUserAccess(useraccessid, enableaccess.booleanValue(),
            		request.getRemoteUser(), comments, enableaccess.booleanValue(), endDate);
            	ErrorCodeDTO error = new ErrorCodeDTO();
        		error.setCode("SUCCESS");
        		if (enableaccess.booleanValue() == true) {
        			error.setDescription("Subscription enabled successfully");
        		} else {
        			error.setDescription("Subscription disabled successfully");
        		}
                errors.add(error);
            } catch (Exception e) {
            	ErrorCodeDTO error = new ErrorCodeDTO();
        		error.setCode("ERROR");
                error.setDescription(e.getMessage());
                errors.add(error);
            }

        } else {
        	ErrorCodeDTO error = new ErrorCodeDTO();
    		error.setCode("ERROR");
            error.setDescription("Not authorized to perform this operation");
            errors.add(error);
        }
        return errors;
    }

    @RequestMapping(value="/removeaccess.admin", produces="application/json")
    @ResponseBody
    public List<ErrorCodeDTO> removeAccess(HttpServletRequest request, @RequestParam(required = false) String username, @RequestParam(required = false) Long useraccessid,
    		@RequestParam(required = false) String comments, RedirectAttributes redirectAttributes) {
        boolean isValidSiteAdmin = false;
        List<ErrorCodeDTO> errors = new LinkedList<ErrorCodeDTO>();
        Site authorizingSite = this.getServiceStub().getUserAccessDetails(useraccessid).getSite();
        if (!this.isInternalUser(request)) {
            List<Site> sites = this.getAssignedSites(request);
            for (Site site : sites) {
                if (authorizingSite.getId() == site.getId()) {
                    isValidSiteAdmin = true;
                }
            }
        } else if(this.isInternalUser(request)) {
            isValidSiteAdmin = true;
        }
        if (isValidSiteAdmin) {
            try {
                PayPalDTO paypalDto = this.getServiceStub().removeSubscription(username, useraccessid,
                	request.getRemoteUser(), comments, true);
                ErrorCodeDTO error = new ErrorCodeDTO();
        		error.setCode("SUCCESS");
                error.setDescription("Subscription removed successfully");
                errors.add(error);
            } catch (PaymentGatewaySystemException payPalSystemException) {
                redirectAttributes.addFlashAttribute("ERROR_MODIFY_SUBSCRIPTION", "paypal.errorcode."
                	+ payPalSystemException.getErrorCode());
                ErrorCodeDTO error = new ErrorCodeDTO();
        		error.setCode("ERROR");
                error.setDescription("paypal.errorcode." + payPalSystemException.getErrorCode());
                errors.add(error);
            }  catch (SDLBusinessException sdlBusinessException) {
            	ErrorCodeDTO error = new ErrorCodeDTO();
        		error.setCode("ERROR");
                error.setDescription(sdlBusinessException.getDescription());
                errors.add(error);
            }

        } else {
    		ErrorCodeDTO error = new ErrorCodeDTO();
    		error.setCode("ERROR");
            error.setDescription("Access is denied");
            errors.add(error);
        }
        return errors;
    }

    @RequestMapping(value="/authorizeaccess.admin", produces="application/json")
    @ResponseBody
    public List<ErrorCodeDTO> authorizeAccess(HttpServletRequest request, @RequestParam(required = false) String username, @RequestParam(required = false) String nodename,
        @RequestParam(required = false) Long useraccessid, RedirectAttributes redirectAttributes) {
    	List<ErrorCodeDTO> errors = new LinkedList<ErrorCodeDTO>();
        boolean isValidSiteAdmin = false;
        Site authorizingSite = this.getServiceStub().getUserAccessDetails(useraccessid).getSite();
        if (!this.isInternalUser(request)) {
            List<Site> sites = this.getAssignedSites(request);
            for (Site site : sites) {
                if (authorizingSite.getId() == site.getId()) {
                    isValidSiteAdmin = true;
                }
            }
        } else if(this.isInternalUser(request)) {
            isValidSiteAdmin = true;
        }
        if (isValidSiteAdmin) {
            try {
                this.getServiceStub().authorize(useraccessid, true, request.getRemoteUser());
                ErrorCodeDTO error = new ErrorCodeDTO();
        		error.setCode("SUCCESS");
                error.setDescription("Subscription has been authorized successfully");
                errors.add(error);
            } catch (Exception e) {
            	ErrorCodeDTO error = new ErrorCodeDTO();
        		error.setCode("ERROR");
                error.setDescription(e.getMessage());
                errors.add(error);
            }
        } else {
        	ErrorCodeDTO error = new ErrorCodeDTO();
    		error.setCode("ERROR");
            error.setDescription("Not authorized to perform this operation");
            errors.add(error);
        }
        return errors;
    }

    @RequestMapping(value="/cancelaccess.admin", produces="application/json")
    @ResponseBody
    public List<ErrorCodeDTO> cancelAccess(HttpServletRequest request, @RequestParam(required = false) String username, @RequestParam(required = false) String nodename,
        @RequestParam(required = false) Long useraccessid, @RequestParam(required = false) String comments, RedirectAttributes redirectAttributes) {
    	List<ErrorCodeDTO> errors = new LinkedList<ErrorCodeDTO>();
        boolean isValidSiteAdmin = false;
        Site authorizingSite = this.getServiceStub().getUserAccessDetails(useraccessid).getSite();
        if (!this.isInternalUser(request)) {
            List<Site> sites = this.getAssignedSites(request);
            for (Site site : sites) {
                if (authorizingSite.getId() == site.getId()) {
                    isValidSiteAdmin = true;
                }
            }
        } else if(this.isInternalUser(request)) {
            isValidSiteAdmin = true;
        }
        if (isValidSiteAdmin) {
            try {
                PayPalDTO paypalDto = this.getServiceStub().cancelSubscription(username, useraccessid);
                if (paypalDto != null) {
                	ErrorCodeDTO error = new ErrorCodeDTO();
            		error.setCode("SUCCESS");
                    error.setDescription("Subscription has been cancelled successfully");
                    errors.add(error);
                }
            } catch (PaymentGatewaySystemException payPalSystemException) {
            	ErrorCodeDTO error = new ErrorCodeDTO();
        		error.setCode("ERROR");
                error.setDescription("paypal.errorcode." + payPalSystemException.getErrorCode());
                errors.add(error);
            }  catch (SDLBusinessException sdlBusinessException) {
            	ErrorCodeDTO error = new ErrorCodeDTO();
        		error.setCode("ERROR");
                error.setDescription("Not authorized to perform this operation");
                errors.add(error);
            }

        } else {
        	ErrorCodeDTO error = new ErrorCodeDTO();
    		error.setCode("ERROR");
            error.setDescription("Not authorized to perform this operation");
            errors.add(error);
        }
        return errors;
    }



    @RequestMapping(value="/changeFirmAdministrator.admin", method=RequestMethod.GET,  produces="application/json")
    @ResponseBody
    public JsonResponse ChangeFirmSubscriptionAdministrator(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(required = true) String newAdminUserName,
    		@RequestParam(required = true) Long accessId,
    		@RequestParam(required = true) String comments
    		) throws Exception {
    	JsonResponse resp = new JsonResponse();
    	try{
    		this.serviceStub.ChangeFirmSubscriptionAdministrator(newAdminUserName, accessId, comments, request.getRemoteUser());
    		resp.setErrorCode(SUCCESS);
    	}catch(UserNameNotFoundException e){
			resp.setErrorCode(ERROR);
			resp.addError(e.getDescription());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    	} catch (SDLBusinessException e){
			resp.setErrorCode(ERROR);
			resp.addError(e.getDescription());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    	}
    	return resp;
    }

    @RequestMapping(value="/getecomuserdetails.admin", method=RequestMethod.GET,  produces="application/json")
    @ResponseBody
    public UserDetails getUserDetails(HttpServletRequest request, @RequestParam(required = false) String username,
    		@RequestParam(required = false) String nodename) {
        UserDetails userDetails = new UserDetails();

        String userLoggedInSite = null;
        boolean isInternalUser = false;
        List<Site> sites = this.getAssignedSites(request);
        if (!this.isInternalUser(request)) {
            for (Site site : sites) {
                userLoggedInSite = site.getName();
            }
        } else {
            isInternalUser = true;
        }
        List<SubscriptionDTO> subscriptionDTOs = null;
        if (!StringUtils.isBlank(username)) {
        	subscriptionDTOs = this.serviceStub.getUserInfoForAdmin(username, userLoggedInSite);
            if (subscriptionDTOs != null && !subscriptionDTOs.isEmpty()) {
                User user = subscriptionDTOs.get(0).getUser();
                CreditCardForm creditCardForm = new CreditCardForm();
                CreditCard creditCard = user.getCreditCard();
                if (creditCard != null) {
                    creditCardForm = this.buildCreditCardForm(creditCard);
                }
                userDetails.setUser(user);
                userDetails.setSubscriptions(subscriptionDTOs);
                userDetails.setCreditCardForm(creditCardForm);
                if (user.getRegisteredNode() == null || user.getRegisteredNode() == "") {
                    if (subscriptionDTOs != null && subscriptionDTOs.size() > 0) {
                        userDetails.setNodeName(nodename);
                    }
                }
                userDetails.setSites(sites);
                userDetails.setInternalUser(isInternalUser);
            }
        } else {
            //modelAndView.setViewName(LOGINSUCCESS);
        }
        return userDetails;
    }


    @RequestMapping(value="/getCreditCardDetails.admin")
    public ModelAndView getCreditCardDetails(HttpServletRequest request, @RequestParam(required = false) String userName) {
        ModelAndView modelAndView = this.getModelAndView(request, VIEW_CREDIT_CARD_DETAILS);
        if (!StringUtils.isBlank(userName)) {
        	CreditCard cardInfo = this.serviceStub.getCreditCardDetailsByUserName(userName);
            if (cardInfo != null) {
            	CreditCardForm creditCardForm = this.buildCreditCardForm(cardInfo);
            	modelAndView.addObject("userName", userName);
            	modelAndView.addObject("user", request.getRemoteUser());
                modelAndView.addObject("creditCardForm", creditCardForm);
            }
        } else {
            modelAndView.setViewName(LOGINSUCCESS);
        }
        return modelAndView;
    }



    @RequestMapping(value="/viewecomuserdetails.admin")
    public ModelAndView viewecomuserdetails(HttpServletRequest request, @RequestParam(required = false) String username) {
        ModelAndView modelAndView = this.getModelAndView(request, VIEW_USER_DETAILS);
        String userLoggedInSite = null;
        boolean isInternalUser = false;
        List<Site> sites = this.getAssignedSites(request);
        if (!this.isInternalUser(request)) {
            for (Site site : sites) {
                userLoggedInSite = site.getName();
            }
        } else {
            isInternalUser = true;
        }
        List<SubscriptionDTO> subscriptionDTOs = null;
        if (!StringUtils.isBlank(username)) {
        	subscriptionDTOs = this.serviceStub.getUserInfoForAdmin(username, userLoggedInSite);
            if (subscriptionDTOs != null && !subscriptionDTOs.isEmpty()) {
                User user = subscriptionDTOs.get(0).getUser();
                CreditCardForm creditCardForm = new CreditCardForm();
                CreditCard creditCard = user.getCreditCard();
                if (creditCard != null) {
                    creditCardForm = this.buildCreditCardForm(creditCard);
                }

                modelAndView.addObject("user", user);
                modelAndView.addObject("subscriptions", subscriptionDTOs);
                modelAndView.addObject("creditCardForm", creditCardForm);
                modelAndView.addObject("sites", sites);
                modelAndView.addObject("isInternalUser", isInternalUser);
            }
        } else {
            modelAndView.setViewName(LOGINSUCCESS);
        }
        return modelAndView;
    }

    @RequestMapping(value="/getFirmUsersBySubscription.admin", method=RequestMethod.GET,  produces="application/json")
    @ResponseBody
    public List<FirmUserDTO> getFirmUsersBySubscription(HttpServletRequest request, @RequestParam String userName,
    		@RequestParam long accessId) {
    	//ModelAndView modelAndView = this.getModelAndView(request, VIEW_USER_DETAILS);
    	List<FirmUserDTO> firmUserList =  new LinkedList<FirmUserDTO>();
    	firmUserList = this.getServiceStub().getFirmUsersbySubscriptionAndUserName(userName, accessId);
        return firmUserList;
    }


    @RequestMapping(value="/getRecurTransactionsForUser.admin", method=RequestMethod.GET,  produces="application/json")
    public void getRecurTransactionsForUser(HttpServletRequest request, @RequestParam(required = false) String username,
    		HttpServletResponse response) {
    	//ModelAndView modelAndView = this.getModelAndView(request, VIEW_USER_DETAILS);
    	try {
    		List<RecurTx> recurTransactions =  new LinkedList<RecurTx>();

            Long siteId =  null;
            List<Site> sites = this.getAssignedSites(request);
            if (!this.isInternalUser(request)) {
                for (Site site : sites) {
                    siteId = site.getId();
                }
            }
            if (!StringUtils.isBlank(username)) {
    	        recurTransactions = this.getServiceStub().getRecurTransactions(username, siteId);
            }

            StringHttpMessageConverter messageConverter = new StringHttpMessageConverter();
            MediaType jsonMimeType = MediaType.TEXT_PLAIN;

        	ObjectMapper objectMapper = new ObjectMapper();
        	SimpleModule simpleModule = new SimpleModule("SimpleModule");
        	simpleModule.addSerializer(new RecurTxSerializer());
        	objectMapper.registerModule(simpleModule);
        	JavaType stringType = objectMapper.getTypeFactory().constructCollectionType(List.class, RecurTx.class);
    		String stringifiedOutput = objectMapper.writerWithType(stringType).writeValueAsString(recurTransactions);
            if (messageConverter.canWrite(stringifiedOutput.getClass(), jsonMimeType)) {
    				messageConverter.write(stringifiedOutput, jsonMimeType, new ServletServerHttpResponse(response));
            }
    	} catch (HttpMessageNotWritableException httpMessageNotWritableException) {
			this.logger.error("Error in getRecurTransactionsForUser", httpMessageNotWritableException);
		} catch (IOException iOException) {
			this.logger.error("Error in getRecurTransactionsForUser", iOException);
		}
    }

    @RequestMapping(value="/getPayAsUGoTransactionsForUser.admin", method=RequestMethod.GET, produces="application/json")
    public void getPayAsUGoTransactionsForUser(HttpServletRequest request, @RequestParam(required = false) String username,
    			HttpServletResponse response) {
    	try {
	        Long siteId =  null;
	        List<PayAsUGoTx> payAsUGoTransactions = null;
	        List<Site> sites = this.getAssignedSites(request);
	        if (!this.isInternalUser(request)) {
	            for (Site site : sites) {
	                siteId = site.getId();
	            }
	        }
	        if (!StringUtils.isBlank(username)) {
		        payAsUGoTransactions = this.getServiceStub().getPayAsUGoTransactions(username, siteId);
	        }
	        StringHttpMessageConverter messageConverter = new StringHttpMessageConverter();
	        MediaType jsonMimeType = MediaType.TEXT_PLAIN;

	    	ObjectMapper objectMapper = new ObjectMapper();
	    	SimpleModule simpleModule = new SimpleModule("SimpleModule");
	    	simpleModule.addSerializer(new PayAsUGoTxSerializer());
	    	objectMapper.registerModule(simpleModule);
	    	JavaType stringType = objectMapper.getTypeFactory().constructCollectionType(List.class, PayAsUGoTx.class);
			String stringifiedOutput = objectMapper.writerWithType(stringType).writeValueAsString(payAsUGoTransactions);
	        if (messageConverter.canWrite(stringifiedOutput.getClass(), jsonMimeType)) {
					messageConverter.write(stringifiedOutput, jsonMimeType, new ServletServerHttpResponse(response));
	        }
		} catch (HttpMessageNotWritableException httpMessageNotWritableException) {
			this.logger.error("Error in getPayAsUGoTransactionsForUser", httpMessageNotWritableException);
		} catch (IOException iOException) {
			this.logger.error("Error in getPayAsUGoTransactionsForUser", iOException);
		}
    }

    @RequestMapping(value="/viewecomuserwebhistory.admin")
    public String getUsersWebTransactionHistory(@RequestParam(required = false) String username, HttpServletRequest request,
    	HttpServletResponse response) throws IOException {
        String webTransactions =  null;
        Long siteId =  null;
        List<Site> sites = this.getAssignedSites(request);
        if (!this.isInternalUser(request)) {
            for (Site site : sites) {
                siteId = site.getId();
            }
        }
        webTransactions = getUserWebTransactions(username, request, siteId);
        byte[] contents = webTransactions.getBytes();
        response.getOutputStream().write(contents);
        return null;
    }

    @RequestMapping(value="/viewecomuserrecurringhistory.admin")
    public String getUsersRecurringTransactionHistory(@RequestParam(required = false) String username,
    		HttpServletRequest request, HttpServletResponse response) throws IOException {
        String recurringTransactions =  null;
        Long siteId =  null;
        List<Site> sites = this.getAssignedSites(request);
        if (!this.isInternalUser(request)) {
            for (Site site : sites) {
                siteId = site.getId();
            }
        }
        recurringTransactions = this.getUserRecurringTransactions(username, request, siteId);
        byte[] contents = recurringTransactions.getBytes();
        response.getOutputStream().write(contents);
        return null;
    }

    @RequestMapping(value="/viewecomuserrecurringhistoryexport.admin")
    public String getUsersRecurringHistoryExport(HttpServletRequest request, HttpServletResponse response) {
        TableModel tableModel = new TableModel("recurringTransactions", request, response);
        tableModel.setStateAttr("restore");
        Table table = new Table();
        Row row = new Row();
        table.setRow(row);
        tableModel.setTable(getUsersRecurringHistoryHtmlExportTable(request));
        String html = tableModel.render();
        request.setAttribute("webTransactions", html);
        return html;
    }

    @RequestMapping(value="/lockecomuser.admin", produces="application/json")
    @ResponseBody
    public List<ErrorCodeDTO> lockUser(HttpServletRequest request, @RequestParam(required = false) String username,
    	@RequestParam(defaultValue="false", required=false) Boolean sendnotification,
    		@RequestParam(required = false) String nodename, @RequestParam(required = false) String comments,
    			@RequestParam(required = false) String path) {
    	List<ErrorCodeDTO> errors = new LinkedList<ErrorCodeDTO>();
    	ErrorCodeDTO error = new ErrorCodeDTO();
        this.getServiceStub().lockUnLockUser(username, true, request.getRemoteUser(), sendnotification.booleanValue(),
            nodename, comments);
        error.setCode("SUCCESS");
        error.setDescription("User account has been locked Successfully");
        errors.add(error);
        return errors;
    }

    @RequestMapping(value="/unlockecomuser.admin", produces="application/json")
    @ResponseBody
    public List<ErrorCodeDTO> unlockUser(HttpServletRequest request, @RequestParam(required = false) String username,
    	@RequestParam(defaultValue="false", required=false) Boolean sendnotification,
    		@RequestParam(required = false) String nodename, @RequestParam(required = false) String comments,
    			@RequestParam(required = false) String path) {
    	List<ErrorCodeDTO> errors = new LinkedList<ErrorCodeDTO>();
    	ErrorCodeDTO error = new ErrorCodeDTO();

        this.getServiceStub().lockUnLockUser(username, false, request.getRemoteUser(), sendnotification.booleanValue(),
            nodename, comments);
        error.setCode("SUCCESS");
        error.setDescription("User account has been unlocked Successfully");
        errors.add(error);
        return errors;
    }

    @RequestMapping(value="/archiveUser.admin", produces="application/json")
    @ResponseBody
    public List<ErrorCodeDTO> archiveUser(HttpServletRequest request, @RequestParam(required = true) String username,
    		@RequestParam(required = true) String nodename, @RequestParam(required = true) String archievecomments,
    			@RequestParam(required = false) String path, RedirectAttributes redirectAttributes) {
    	List<ErrorCodeDTO> errors = new LinkedList<ErrorCodeDTO>();
    	ErrorCodeDTO error = new ErrorCodeDTO();

        try {
        if (archievecomments.length() > 250) {
        	 error.setCode("ERROR");
             error.setDescription("Comments cannot exceed 250 characters");
             errors.add(error);
             return errors;
        }
        this.getServiceStub().archiveUser(username, archievecomments, request.getRemoteUser(), request.getRemoteHost());

        error.setCode("SUCCESS");
        error.setDescription("User Deleted Successfully");
        errors.add(error);

        } catch (SDLBusinessException sdlBusinessException) {
            error.setCode("ERROR");
            error.setDescription(sdlBusinessException.getDescription());
            errors.add(error);
            return errors;
        }
        return errors;
    }

    @RequestMapping(value="/viewecomuserexport.admin", method=RequestMethod.GET,  produces="application/json")
    @ResponseBody
    public void getUsersExport(SearchCriteriaDTO searchCriteria,
            BindingResult bindingResult,    HttpServletRequest request, HttpServletResponse response,
            	@RequestParam(required = true) String exportType
    			) {

        if (!this.isInternalUser(request)) {
            List<Site> sites = this.getAssignedSites(request);
            for (Site site : sites) {
            	long siteId = site.getId();
                searchCriteria.setSiteId(siteId);
            }
        }

        //List<User> users = this.serviceStub.exportUsers(searchCriteria);
        searchCriteria.setStartingFrom(0);
        searchCriteria.setNumberOfRecords(Integer.MAX_VALUE);
        PageRecordsDTO pageRecords  = this.serviceStub.findUsers(searchCriteria);
    	Collection<User> users = (Collection<User>)pageRecords.getRecords();

        byte[] outputBytes = new byte[1];
        String fileExtension = "";
        try{
	        if(exportType.equals(EXCEL)){
	        	List<List<String>> usersList = new ArrayList<List<String>>();
	        	for(User user : users){
	    			usersList.add(this.getUserRowForExcel(user));
	    		}
	        	ExcelExport export = new ExcelExport();
	        	outputBytes = export.exportToExcel(ExportConstants.getUsersHeaders(), usersList);
	        	fileExtension = "xls";
	        } else if (exportType.equals(PDF)){
	        	// Create PDF file contents
	        	List<List<PDFCell>> usersList = new ArrayList<List<PDFCell>>();
	        	for(User user : users){
	    			usersList.add(this.getUserRowForPDF(user));
	    		}
	        	PDFExport export = new PDFExport(PDF_USER_COLUMN_WIDTHS.length, PDF_USER_COLUMN_WIDTHS);
	        	outputBytes = export.exportToPDF(ExportConstants.getUsersHeaders(), usersList);
	        	fileExtension = "pdf";
	        }
	        response.reset();
			response.resetBuffer();
			response.setContentType("application/x-download");
			response.setHeader("Content-Disposition",  "attachment; filename=UserList." + fileExtension);
			response.setHeader("Expires", " 0");
			response.setHeader("Cache-Control", " must-revalidate, post-check=0, pre-check=0" );
			response.setHeader("Pragma" , "public");
			ByteArrayOutputStream outputStream =  new ByteArrayOutputStream();
			outputStream.write(outputBytes);
			outputStream.writeTo(response.getOutputStream());
			response.getOutputStream().flush();
        } catch(Exception e){
        	logger.error("Error while writing exporting find user " , e);
        }
    }

    @Link(label="User Administration", family="ACCEPTADMIN", parent = "Home" )
    @RequestMapping(value="/findecomusers.admin")
    public ModelAndView viewFindUsers(HttpServletRequest request) {
        ModelAndView modelAndView = this.getModelAndView(request, FIND_USERS, TOP_USER_ADMIN, "NA");
        boolean isInternalUser = false;
        SearchCriteriaDTO searchCriteria = new SearchCriteriaDTO();
        searchCriteria.setFirstName("");
        searchCriteria.setLastName("");
        if (request.getParameterMap().containsKey("username")) {
        	searchCriteria.setUserName(request.getParameter("username"));
        } else {
        	searchCriteria.setUserName("");
        }
        searchCriteria.setFirmName("");
        searchCriteria.setFirmNumber("");
        searchCriteria.setBarNumber("");
        if (this.isInternalUser(request)) {
            isInternalUser = true;
        }
        modelAndView.addObject("isInternalUser", isInternalUser);
        modelAndView.addObject("searchCriteria" , searchCriteria);
        List<User> users = new LinkedList<User>();
        List<Site> sites = this.getAssignedSites(request);
        modelAndView.addObject("sites", sites);
        modelAndView.addObject("users", users);
        return modelAndView;
    }

    @RequestMapping(value="/searchUsers.admin", method=RequestMethod.GET,  produces="application/json")
	@ResponseBody
    public PageRecordsDTO searchUsers(SearchCriteriaDTO searchCriteria, Integer skip, Integer take,
                BindingResult bindingResult,
                HttpServletRequest request) {
        List<Site> sites = this.getAssignedSites(request);

        if (!this.isInternalUser(request)) {
            for (Site site : sites) {
            	long siteId = site.getId();
                searchCriteria.setSiteId(siteId);
            }
        }

        // Get Sort Field & type Ascending, descending and
        String sortType = request.getParameter(KENDOUI_SORT_TYPE_PARAM);
        searchCriteria.setSortType(sortType);

        String gridSortByField = request.getParameter(KENDOUI_SORT_FIELD_PARAM);
        String sortByField = this.getSortByField(gridSortByField, searchCriteria);
        searchCriteria.setSortField(sortByField);

        // Set pageing variables.
        searchCriteria.setStartingFrom(skip);
        searchCriteria.setNumberOfRecords(take);


        PageRecordsDTO pageRecords  = this.serviceStub.findUsers(searchCriteria);
        return pageRecords;
    }

	private String getSortByField(String gridSortByField, SearchCriteriaDTO searchCriteria){
		if(StringUtils.isBlank(gridSortByField)){
			return null;
		}
		String sortByField = null;
		if(gridSortByField.equals(WAITING_FOR_AUTHORIZATION)){
			sortByField = SearchCriteriaDTO.WAITING_FOR_AUTHORIZATION;
		} else if(gridSortByField.equals(USERACCESS_ACTIVE)){
			sortByField = SearchCriteriaDTO.USERACCESS_ACTIVE;
		} else if(gridSortByField.equals(HAS_OVERRIDEN_ACCESS)){
			sortByField = SearchCriteriaDTO.HAS_OVERRIDEN_ACCESS;
		} else if(gridSortByField.equals(CURRENT_LOGIN_TIME)){
			sortByField = SearchCriteriaDTO.CURRENT_LOGIN_TIME;
		} else if(gridSortByField.equals(PAYED_USER)){
			sortByField = SearchCriteriaDTO.PAYED_USER;
		} else if(gridSortByField.equals(USER_ACTIVE)){
			sortByField = SearchCriteriaDTO.USER_ACTIVE;
		} else if(gridSortByField.equals(FIRM_NAME)){
			sortByField = SearchCriteriaDTO.FIRM_NAME;
		} else if(gridSortByField.equals(FIRM_NUMBER)){
			sortByField = SearchCriteriaDTO.FIRM_NUMBER;
		} else if(gridSortByField.equals(ACCOUNT_NON_LOCKED)){
			sortByField = SearchCriteriaDTO.ACCOUNT_NON_LOCKED;
		} else if(gridSortByField.equals(FIRM_ADMIN)){
			sortByField = SearchCriteriaDTO.FIRM_ADMIN;
		}		
		
		return sortByField;
	}

	@RequestMapping(value="/updatecreditcard.admin", produces="application/json")
    @ResponseBody
    public List<ErrorCodeDTO> updateCreditCardInfo(HttpServletRequest request, @ModelAttribute("creditCardForm") @Valid CreditCardForm creditCardForm,
    					@RequestParam(required = false) String userName, BindingResult bindingResult) {
        List<ErrorCodeDTO> errors = new LinkedList<ErrorCodeDTO>();
        String verificationResult = verifyBindingInJSON(bindingResult);
        if (verificationResult != null) {
            ErrorCodeDTO error = new ErrorCodeDTO();
            error.setCode("ERROR");
            error.setDescription(this.getMessage("system.invalid.data"));
            errors.add(error);
            return errors;
        }
        validate(creditCardForm, bindingResult, DefaultGroup.class);
//        if (bindingResult.hasErrors()) {
//            errors = this.populateErrorCodes(bindingResult.getFieldErrors());
//        }
        creditCardValidator.validate(creditCardForm, bindingResult);
        if (bindingResult.hasErrors()) {
            errors.addAll(this.populateErrorCodes(bindingResult.getFieldErrors()));
        }
        if (errors != null && errors.size() > 0) {
            return errors;
        }
        CreditCard creditCardInfo = buildCreditCard(creditCardForm, request);
        try {
            this.getServiceStub().updateExistingCreditCardInformation(userName, request.getRemoteUser(), creditCardInfo);
        } catch (PaymentGatewayUserException payPalUserException) {
            bindingResult.rejectValue("ERROR", "paypal.errorcode." + payPalUserException.getErrorCode());
        } catch (PaymentGatewaySystemException payPalSystemException) {
            bindingResult.rejectValue("ERROR", "paypal.errorcode.generalsystemerror");
        }
        if (bindingResult.hasErrors()) {
            return this.populateErrorCodes(bindingResult.getFieldErrors());
        }
        ErrorCodeDTO errorCode = new ErrorCodeDTO();
        errorCode.setCode("SUCCESS");
        errorCode.setDescription(this.getMessage("ecom.creditcard.updatesuccess"));
        errors.add(errorCode);
        return errors;
    }

    @RequestMapping(value="/getsubscriptionsbysite.admin", method=RequestMethod.GET,  produces="application/json")
    @ResponseBody
    public List<Access> getAccesForSite(@RequestParam(required = false) String siteId) {
    	if(!StringUtils.isBlank(siteId) && siteId.contains("|")) {
    		//siteId = siteId.split("\\|")[1];
    		siteId = siteId.substring(siteId.indexOf("|") + 1);
    	}
        List<Access> accessList = this.getServiceStub().getAccessesForSite(siteId);
        return accessList;
    }

    @RequestMapping(value="/addSubscription.admin", produces="application/json")
    @ResponseBody
    public List<ErrorCodeDTO> addSubscription(HttpServletRequest request, @RequestParam(required = false) String userName,
    		@RequestParam String siteId2, @RequestParam(required = false) Long accessIdAddSubscription) {

    	List<ErrorCodeDTO> errors = new LinkedList<ErrorCodeDTO>();
    	ErrorCodeDTO error = new ErrorCodeDTO();

    	if (userName == null || userName == ""  || siteId2 == null || siteId2 == "" || accessIdAddSubscription == null) {
    		error.setCode("ERROR");
            error.setDescription("Please select the site and subscription to add a new subscription");
            errors.add(error);
            return errors;
    	}

    	List<Long> accessIds = new LinkedList<Long>();
        //String nodeName = siteId2.split("\\|")[0];
    	String nodeName = siteId2.substring(0, siteId2.indexOf("|"));
        accessIds.add(accessIdAddSubscription);

        try {
        	User user = this.getServiceStub().getUserDetailsForAdmin(userName);
            SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
            subscriptionDTO.setNewAccessIds(accessIds);
            subscriptionDTO.setUser(user);
            subscriptionDTO.setNodeName(nodeName);
            this.getServiceStub().addSubscription(subscriptionDTO);
            error.setCode("SUCCESS");
            error.setDescription("Subscription Added Successfully");
            errors.add(error);
        } catch (SDLBusinessException sdlBusinessException) {
        	error.setCode("ERROR");
            error.setDescription(sdlBusinessException.getDescription());
            errors.add(error);
        } catch (UserNameNotFoundException userNameNotFoundException) {
        	error.setCode("ERROR");
            error.setDescription("User Name not found.");
            errors.add(error);
        } catch (Exception exception) {
        	error.setCode("ERROR");
            error.setDescription("System Exception. Please contact the administrator for more information!");
            errors.add(error);
        }
        return errors;
    }

    @RequestMapping(value="/removeFirmUserAccess.admin", method=RequestMethod.POST)
	@ResponseBody
	public JsonResponse removeFirmUserAccess(HttpServletRequest request, HttpServletResponse response,
			String firmUserName,	Long userAccessId, String comments) {
		JsonResponse resp = new JsonResponse();
		if(StringUtils.isBlank(comments)){
			resp.setErrorCode(ERROR);
			resp.addError(this.getMessage("user.alert.commentsrequired"));
			return resp;
		}
		try {
			this.getServiceStub().removeFirmLevelAccess(firmUserName, userAccessId, comments, request.getRemoteUser());
			resp.setErrorCode(SUCCESS);
		} catch (SDLBusinessException e) {
			resp.setErrorCode(ERROR);
			resp.addError(e.getDescription());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch(UserNameNotFoundException e){
			resp.setErrorCode(ERROR);
			resp.addError(e.getDescription());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch(Exception e){
			resp.setErrorCode(ERROR);
			resp.addError("Server Error, Please contact Administrator");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return resp;
	}

	@RequestMapping(value="/enableDisableFirmUserAccess.admin", method=RequestMethod.POST)
	@ResponseBody
	public JsonResponse enableDisableFirmUserAccess(HttpServletRequest request, HttpServletResponse response,
				String firmUserName, Long userAccessId, boolean isEnable, String comments) {
		JsonResponse resp = new JsonResponse();
		try {
			this.getServiceStub().enableDisableFirmUserAccess(firmUserName, userAccessId, isEnable,	comments, request.getRemoteUser());
			resp.setErrorCode(SUCCESS);
		} catch (SDLBusinessException e) {
			resp.setErrorCode(ERROR);
			resp.addError(e.getDescription());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch(UserNameNotFoundException e){
			resp.setErrorCode(ERROR);
			resp.addError(e.getDescription());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch(Exception e){
			resp.setErrorCode(ERROR);
			resp.addError("Server Error, Please contact Administrator");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return resp;
	}

	@RequestMapping(value="/lockUnLockFirmUser.admin", method=RequestMethod.POST)
	@ResponseBody
	public JsonResponse lockUnLockFirmUser(HttpServletRequest request, HttpServletResponse response,
			String firmUserName, boolean isLock, String comments, String nodeName) {

		JsonResponse resp = new JsonResponse();
		try {
			this.getServiceStub().lockUnLockUser(firmUserName, isLock, request.getRemoteUser(), true, nodeName, comments);
			resp.setErrorCode(SUCCESS);
		} catch(Exception e){
			resp.setErrorCode(ERROR);
			resp.addError("Server Error, Please contact Administrator");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return resp;
	}

    private String getUserRecurringTransactions(String userName, HttpServletRequest request, Long siteId) {
        List<RecurTx> recurTransactions =  null;
        if (!this.isInternalUser(request)) {
			List<Site> sites = this.getAssignedSites(request);
			for (Site site : sites) {
				siteId = site.getId();
			}
    	}
        recurTransactions = this.getServiceStub().getRecurTransactions(userName, siteId);
        TableModel tableModel = new TableModel("recurringTransactions", request);
        tableModel.setItems(recurTransactions);
        tableModel.setExportTypes();
        tableModel.setStateAttr("restore");
        Table table = new Table();
        Row row = new Row();
        table.setRow(row);
        tableModel.setTable(getUsersRecurringHistoryHtmlExportTable(request));
        String webTransactionHTML = tableModel.render();
        return webTransactionHTML;
    }

    private String getUserWebTransactions(String userName, HttpServletRequest request, Long siteId) {
        List<PayAsUGoTx> payAsUGoTransactions =  null;
        if (!this.isInternalUser(request)) {
			List<Site> sites = this.getAssignedSites(request);
			for (Site site : sites) {
				siteId = site.getId();
			}
    	}
        payAsUGoTransactions = this.getServiceStub().getPayAsUGoTransactions(userName, siteId);
        TableModel tableModel = new TableModel("webTransactions", request);
        tableModel.setItems(payAsUGoTransactions);
        tableModel.setExportTypes();
        tableModel.setStateAttr("restore");
        Table table = new Table();
        Row row = new Row();
        table.setRow(row);
        tableModel.setTable(getUsersWebHistoryHtmlExportTable());
        String webTransactionHTML = tableModel.render();
        return webTransactionHTML;
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

    private List<ErrorCodeDTO> populateErrorCodes(List<FieldError> fieldErrors) {
        List<ErrorCodeDTO> errors = new LinkedList<ErrorCodeDTO>();
        for (FieldError fieldError : fieldErrors) {
            if (fieldError != null) {
                ErrorCodeDTO errorCode = new ErrorCodeDTO();
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


    private Table getUsersRecurringHistoryHtmlExportTable(HttpServletRequest request) {
        Table table = new HtmlTable();
        Row row = new HtmlRow();
        //row.getRowRenderer().setStyleClass("jmesaRow");
        table.setRow(row);
        HtmlColumn transDate = new HtmlColumn("transactionDate").title("Transaction Date");
        transDate.setFilterable(false);
        transDate.setCellEditor(new CellEditor() {
            public Object getValue(Object item, String property, int rowcount) {
            	DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
                Object transDateVal = new HtmlCellEditor().getValue(item, "transactionDate", rowcount);
                Object timeZoneVal = new HtmlCellEditor().getValue(item, "site.timeZone", rowcount);
                String cValue = SystemUtil.getDateInTimezone(formatter.parseDateTime(transDateVal.toString()).toDate(), timeZoneVal.toString());
                return cValue;
            }
        });
        row.addColumn(transDate);
        HtmlColumn txRefNum = new HtmlColumn("txRefNum").title("Transaction Number");
        txRefNum.setFilterable(false);
        txRefNum.setCellEditor(new CellEditor() {
            public Object getValue(Object item, String property, int rowcount) {
                Object transactionRefNum = new HtmlCellEditor().getValue(item, "txRefNum", rowcount);
                String cValue = "<a style='color: #1F92FF;text-decoration: underline;' href='viewtransactiondetails.admin?transactionRefNum=" + transactionRefNum.toString() + "&paymentChannel=RECURRING'>" + transactionRefNum.toString() + "</a>";
                return cValue;
            }
        });
        row.addColumn(txRefNum);

        HtmlColumn siteName = new HtmlColumn("site.name").title("Site Name");
        siteName.setFilterable(false);
        row.addColumn(siteName);
        HtmlColumn accessDescription = new HtmlColumn("access.description").title("Subscription");
        accessDescription.setFilterable(false);
        accessDescription.setStyle("width:100px");
        row.addColumn(accessDescription);
        HtmlColumn cardNumber = new HtmlColumn("cardNumber").title("Card Number");
        cardNumber.setFilterable(false);
        row.addColumn(cardNumber);
        HtmlColumn accountName = new HtmlColumn("accountName").title("Name on Card");
        accountName.setFilterable(false);
        row.addColumn(accountName);

        HtmlColumn transactionType = new HtmlColumn("transactionType").title("Transaction Type");
        transactionType.setFilterable(false);
        transactionType.setCellEditor(new CellEditor() {
            public Object getValue(Object item, String property, int rowcount) {
                Double value = new Double(new HtmlCellEditor().getValue(item, "baseAmount", rowcount).toString());
                if (value < 0) {
                    return "REFUND";
                } else {
                    return "CHARGE";
                }
            }
        });
        row.addColumn(transactionType);

        HtmlColumn createdDate = new HtmlColumn("createdDate").title("Date Created");
        createdDate.setFilterable(false);
        createdDate.setCellEditor(new DateCellEditor("MM/dd/yyyy hh:mm a zzz"));
        row.addColumn(createdDate);
        HtmlColumn modifiedDate = new HtmlColumn("modifiedDate").title("Date Modified");
        modifiedDate.setFilterable(false);
        modifiedDate.setCellEditor(new DateCellEditor("MM/dd/yyyy hh:mm a zzz"));
        row.addColumn(modifiedDate);
        HtmlColumn modifiedBy = new HtmlColumn("modifiedBy").title("Modified By");
        modifiedBy.setFilterable(false);
        row.addColumn(modifiedBy);
        HtmlColumn createdBy = new HtmlColumn("createdBy").title("Created By");
        createdBy.setFilterable(false);
        row.addColumn(createdBy);
        HtmlColumn totalTxAmount = new HtmlColumn().title("Total Amount");
        totalTxAmount.setFilterable(false);
        totalTxAmount.setStyle("text-align:right");
        totalTxAmount.setCellEditor(new CellEditor() {
            public Object getValue(Object item, String property, int rowcount) {
                Double value = new Double(new HtmlCellEditor().getValue(item, "baseAmount", rowcount).toString());
                String cValue = "";
                if (value < 0) {
                    value = 0 - value;
                    cValue = NumberFormat.getCurrencyInstance().format(value);
                    cValue = "- " + cValue;
                } else {
                    cValue = NumberFormat.getCurrencyInstance().format(value);
                }
                return cValue;
            }
        });
        row.addColumn(totalTxAmount);
        return table;
    }


    private Table getUsersWebHistoryHtmlExportTable() {
        Table table = new HtmlTable();
        Row row = new HtmlRow();
        table.setRow(row);
        HtmlColumn transDate = new HtmlColumn("transactionDate").title("Transaction Date");
        transDate.setFilterable(false);
        transDate.setCellEditor(new CellEditor() {
            public Object getValue(Object item, String property, int rowcount) {
            	DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
                Object transDateVal = new HtmlCellEditor().getValue(item, "transactionDate", rowcount);
                Object timeZoneVal = new HtmlCellEditor().getValue(item, "site.timeZone", rowcount);
                String cValue = SystemUtil.getDateInTimezone(formatter.parseDateTime(transDateVal.toString()).toDate(), timeZoneVal.toString());
                return cValue;
            }
        });
        row.addColumn(transDate);
        HtmlColumn txRefNum = new HtmlColumn("txRefNum").title("Transaction Number");
        txRefNum.setFilterable(false);
        txRefNum.setCellEditor(new CellEditor() {
            public Object getValue(Object item, String property, int rowcount) {
                Object transactionRefNum = new HtmlCellEditor().getValue(item, "txRefNum", rowcount);
                String cValue = "<a style='color: #1F92FF;text-decoration: underline;' " +
                	"href='viewtransactiondetails.admin?transactionRefNum=" + transactionRefNum.toString()
                		+ "&paymentChannel=PayAsUGo'>" + transactionRefNum.toString() + "</a>";
                return cValue;
            }
        });
        row.addColumn(txRefNum);
        HtmlColumn itemsPurchased = new HtmlColumn("itemsPurchased").title("Items Purchased");
        itemsPurchased.setFilterable(false);
        row.addColumn(itemsPurchased);
        HtmlColumn siteName = new HtmlColumn("site.name").title("Site Name");
        siteName.setFilterable(false);
        row.addColumn(siteName);
        HtmlColumn cardNumber = new HtmlColumn("cardNumber").title("Card Number");
        cardNumber.setFilterable(false);
        row.addColumn(cardNumber);
        HtmlColumn accountName = new HtmlColumn("accountName").title("Name on Card");
        accountName.setFilterable(false);
        row.addColumn(accountName);
        HtmlColumn transactionType = new HtmlColumn("transactionType").title("Transaction Type");
        transactionType.setFilterable(false);
        row.addColumn(transactionType);
        HtmlColumn createdDate = new HtmlColumn("createdDate").title("Date Created");
        createdDate.setFilterable(false);
        createdDate.setCellEditor(new DateCellEditor("MM/dd/yyyy hh:mm a zzz"));
        row.addColumn(createdDate);
        HtmlColumn modifiedDate = new HtmlColumn("modifiedDate").title("Date Modified");
        modifiedDate.setFilterable(false);
        modifiedDate.setCellEditor(new DateCellEditor("MM/dd/yyyy hh:mm a zzz"));
        row.addColumn(modifiedDate);
        HtmlColumn modifiedBy = new HtmlColumn("modifiedBy").title("Modified By");
        modifiedBy.setFilterable(false);
        row.addColumn(modifiedBy);
        HtmlColumn createdBy = new HtmlColumn("createdBy").title("Created By");
        createdBy.setFilterable(false);
        row.addColumn(createdBy);
        HtmlColumn totalTxAmount = new HtmlColumn().title("Total Amount");
        totalTxAmount.setFilterable(false);
        totalTxAmount.setStyle("text-align:right");
        totalTxAmount.setCellEditor(new CellEditor() {
            public Object getValue(Object item, String property, int rowcount) {
                Object transactionType = new HtmlCellEditor().getValue(item, "transactionType", rowcount);
                Double value = new Double(new HtmlCellEditor().getValue(item, "totalTxAmount", rowcount).toString());
                String cValue = NumberFormat.getCurrencyInstance().format(value);
                if (transactionType.equals("REFUND")) {
                    cValue = "- " + cValue;
                }
                return cValue;
            }
        });
        row.addColumn(totalTxAmount);
        return table;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAllowedFields(new String[] {
            "userName",
            "username",
            "firstName",
            "lastName",
            "maxRows",
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
            "users_tr_",
            "users_mr_",
            "users_p_",
            "firmName",
            "firmNumber",
            "barNumber",
            "siteId",
            "accessId",
            "active",
            "paiduser",
            "userSubscriptionStatus",
            "take",  // Kendo UI Variable
            "skip"	// Kendo UI Variable
        });
    }

    private List<String> getUserRowForExcel(User user){
		List<String> columns = new ArrayList<String>();
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa z");
		String createdDateText = user.getCreatedDate() != null ? df.format(user.getCreatedDate()) : "";
		String lastLoginDateText = user.getLastLoginTime() != null ? df.format(user.getLastLoginTime()) : "";


		columns.add(user.getUsername() != null ? user.getUsername().toLowerCase() : "");
		columns.add(user.isFirmAdmin() ? "Yes" : "No");
		columns.add(user.getFirstName() != null ? StringUtils.capitalize(user.getFirstName().toLowerCase()) : "");
		columns.add(user.getLastName() != null ? StringUtils.capitalize(user.getLastName().toLowerCase()) : "");
		columns.add(user.getPhone() != null ? user.getPhone() : "");
		columns.add(user.isWaitingForAuthorization() ? "Yes" : "No");
		columns.add(user.isPayedUser() ? "Yes" : "No");
		columns.add(user.isPaymentDue() ? "Yes" : "No");
		columns.add(user.isAccessOverridden() ? "Yes" : "No");
		columns.add(user.isActive() ? "Yes" : "No");
		columns.add(user.isAccountNonLocked() ? "No" : "Yes");
		columns.add(lastLoginDateText);
		columns.add(createdDateText);
		columns.add(user.getFirmName() != null ? user.getFirmName() : "");
		columns.add(user.getFirmNumber() != null ? user.getFirmNumber() : "");
		columns.add(user.getBarNumber() != null ? user.getBarNumber() : "");
		return columns;
    }

	private  List<PDFCell> getUserRowForPDF(User user){
		List<PDFCell> cells = new ArrayList<PDFCell>();

		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa z");
		String createdDateText = user.getCreatedDate() != null ? df.format(user.getCreatedDate()) : "";
		String lastLoginDateText = user.getLastLoginTime() != null ? df.format(user.getLastLoginTime()) : "";
		boolean wrap = true;
		cells.add(new PDFCell(user.getUsername() != null ? user.getUsername().toLowerCase() : "", !wrap));
		cells.add(new PDFCell(user.isFirmAdmin() ? "Yes" : "No", !wrap));
		cells.add(new PDFCell(user.getFirstName() != null ? StringUtils.capitalize(user.getFirstName().toLowerCase()) : "", wrap));
		cells.add(new PDFCell(user.getLastName() != null ? StringUtils.capitalize(user.getLastName().toLowerCase()) : "", wrap));
		cells.add(new PDFCell(user.getPhone() != null ? user.getPhone() : "", !wrap));
		cells.add(new PDFCell(user.isWaitingForAuthorization() ? "Yes" : "No", !wrap));
		cells.add(new PDFCell(user.isPayedUser() ? "Yes" : "No", !wrap));
		cells.add(new PDFCell(user.isPaymentDue() ? "Yes" : "No", !wrap));
		cells.add(new PDFCell(user.isAccessOverridden() ? "Yes" : "No", !wrap));
		cells.add(new PDFCell(user.isActive() ? "Yes" : "No", !wrap));
		cells.add(new PDFCell(user.isAccountNonLocked() ? "No" : "Yes", !wrap));
		cells.add(new PDFCell(lastLoginDateText, !wrap));
		cells.add(new PDFCell(createdDateText, !wrap));
		cells.add(new PDFCell(user.getFirmName() != null ? user.getFirmName() : "", wrap));
		cells.add(new PDFCell(user.getFirmNumber() != null ? user.getFirmNumber() : "", wrap));
		cells.add(new PDFCell(user.getBarNumber() != null ? user.getBarNumber() : "", !wrap));

		return cells;
	}


}