package com.fdt.security.ui.controller;

import static com.fdt.security.ui.FirmLevelUserConstants.ACCESS_ID;
import static com.fdt.security.ui.FirmLevelUserConstants.ACTIVE;
import static com.fdt.security.ui.FirmLevelUserConstants.BAR_NUMBER;
import static com.fdt.security.ui.FirmLevelUserConstants.DISABLE_ACCESS_ACTION;
import static com.fdt.security.ui.FirmLevelUserConstants.ENABLE_ACCESS_ACTION;
import static com.fdt.security.ui.FirmLevelUserConstants.ERROR;
import static com.fdt.security.ui.FirmLevelUserConstants.FIRM_NAME;
import static com.fdt.security.ui.FirmLevelUserConstants.FIRM_NUMBER;
import static com.fdt.security.ui.FirmLevelUserConstants.FIRST_NAME;
import static com.fdt.security.ui.FirmLevelUserConstants.ID;
import static com.fdt.security.ui.FirmLevelUserConstants.INACTIVE;
import static com.fdt.security.ui.FirmLevelUserConstants.LAST_NAME;
import static com.fdt.security.ui.FirmLevelUserConstants.NODE_NAME;
import static com.fdt.security.ui.FirmLevelUserConstants.PHONE_NUMBER;
import static com.fdt.security.ui.FirmLevelUserConstants.SITE_ID;
import static com.fdt.security.ui.FirmLevelUserConstants.SUCCESS;
import static com.fdt.security.ui.FirmLevelUserConstants.USER_ACCESS_ID;
import static com.fdt.security.ui.FirmLevelUserConstants.USER_FORM;
import static com.fdt.security.ui.FirmLevelUserConstants.USER_NAME;
import static com.fdt.security.ui.SecurityViewConstants.MANAGE_FIRM_LEVEL_USERS;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.dto.ServiceResponseDTO;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.common.util.JsonResponse;
import com.fdt.security.dto.FirmUserDTO;
import com.fdt.security.entity.User;
import com.fdt.security.exception.MaxUsersExceededException;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.security.spring.SDLSavedRequestAwareAuthenticationSuccessHandler;
import com.fdt.security.ui.form.FirmUser;
import com.fdt.subscriptions.dto.SubscriptionDTO;

/**
 * This class is a controller for Managing Firm Level Users.
 * it supports Add / Update / Delete User operations.
 * 
 * @author APatel
 *
 */
@Controller
public class FirmLevelUserController extends AbstractBaseController {
	


	@Autowired
	@Qualifier("sDLSavedRequestAwareAuthenticationSuccessHandler")
	private SDLSavedRequestAwareAuthenticationSuccessHandler sDLSavedRequestAwareAuthenticationSuccessHandler = null;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.setAllowedFields(new String[] {
				ID, FIRST_NAME, LAST_NAME, PHONE_NUMBER, USER_NAME, USER_FORM, ACCESS_ID, USER_ACCESS_ID, SITE_ID, FIRM_NAME, FIRM_NUMBER, BAR_NUMBER, NODE_NAME
		});
	}
	
	
	/**
	 * Display the GRID and display the subscriptions
	 * 
	 * @param request
	 * @param adminUserName
	 * @param isReAu
	 * @return
	 */
	@RequestMapping(value="/manageFirmLevelUsers.admin", method=RequestMethod.GET)
	public ModelAndView manageFirmLevelUsers(HttpServletRequest request, @RequestParam(required = true) String adminUserName, @RequestParam(defaultValue="false") boolean isReAu) {
		ModelAndView modelAndView = this.getModelAndView(request, MANAGE_FIRM_LEVEL_USERS);
		modelAndView.addObject("adminUserName" , adminUserName);
		List<SubscriptionDTO> subscriptionDTOs = this.getService().getUserSubscriptions(request.getRemoteUser(), this.nodeName, null, true, true);
		List<SubscriptionDTO>  paidSubscriptions = filterPaidSubscriptions(subscriptionDTOs);
		modelAndView.addObject("subscriptions" , paidSubscriptions);
		modelAndView.addObject("user" , this.getUser(request));
		return modelAndView;
	}
	
	
	/**
	 * Check if user exists.
	 * 
	 * @param request
	 * @param response
	 * @param userName
	 * @return
	 */
	@RequestMapping(value="/checkUser.admin", method=RequestMethod.GET,  produces="application/json")
	@ResponseBody
	public JsonResponse checkUser(HttpServletRequest request, HttpServletResponse response,  
			@RequestParam(required = true) String username){
		
		JsonResponse resp = new JsonResponse();
		try{
			// Call the service , if successful then we are fine.
			this.getService().findUser(username);
			resp.setErrorCode(SUCCESS);
			resp.addError("User Found");
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
	
	
	/**
	 * Get The Grid Users.
	 * This is json call from the javascript to get all the firm users.
	 * 
	 * It returns FirmLevelUserResponse which has list of users, errorCode & errorDescriptin (in case of failure)
	 * 
	 * @param request
	 * @param response
	 * @param adminUserName
	 * @param accessId
	 * @return
	 */
	@RequestMapping(value="/getFirmUsers.admin", method=RequestMethod.GET,  produces="application/json")
	@ResponseBody
	public JsonResponse getFirmUsers(HttpServletRequest request, HttpServletResponse response,  
			@RequestParam(required = true) String adminUserName, @RequestParam(required = false) Long accessId){
		
		JsonResponse resp = new JsonResponse();
		try{
				List<FirmUserDTO> users = this.getService().getFirmUsers(adminUserName, accessId > 0 ? accessId : null );
				List<FirmUser> gridUsers = this.getFirmUser(users, accessId);
				resp.setModels(gridUsers);
				resp.setErrorCode(SUCCESS);
		}catch(Exception e){
			resp.setErrorCode(ERROR);
			resp.addError("Server Error, Please contact Administrator");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return resp;
	}
	
	
	/**
	 * Add an User to the firm.
	 * 
	 * It returns FirmLevelUserResponse which has a User, errorCode & errorDescriptin (in case of failure)
	 * 
	 * @param request
	 * @param response
	 * @param user
	 * @param adminUserName
	 * @param accessId
	 * @param siteId
	 * @return
	 */
	@RequestMapping(value="/addFirmUser.admin", method=RequestMethod.POST,  produces="application/json")
	@ResponseBody
	public JsonResponse addFirmUser(HttpServletRequest request, HttpServletResponse response, String adminUserName, 
			@Valid FirmUser userForm, BindingResult bindingResult){
		JsonResponse resp = new JsonResponse();
		try {
			if(userForm.getAccessId() == null || userForm.getAccessId().equals(0)){
				bindingResult.addError(new ObjectError("accessId", super.getMessage("security.noaccess.access")));
			}

			if(this.validateFormForErrors(bindingResult, resp)) {
				User user = new User();
				user.setModifiedBy(adminUserName);
				user.setCreatedBy(adminUserName);
				user.setCreatedIp(request.getRemoteAddr());
				user.setFirstName(userForm.getFirstName());
				user.setLastName(userForm.getLastName());
				String phoneNbr = userForm.getPhone().replace("-", "").replace("(","").replace(")","");
				user.setPhone(phoneNbr);
				user.setUsername(userForm.getUsername());
				user.setFirmName(StringUtils.isBlank(userForm.getFirmName()) ? null : userForm.getFirmName());
				user.setFirmNumber(StringUtils.isBlank(userForm.getFirmNumber()) ? null : userForm.getFirmNumber());
				user.setBarNumber(StringUtils.isBlank(userForm.getBarNumber()) ? null : userForm.getBarNumber());
				
				ServiceResponseDTO serviceResponse = this.getService().addFirmLevelUser(user,  adminUserName, userForm.getAccessId(), this.nodeName, this.ecomClientURL);
				resp.addToModel(userForm);
				resp.setErrorCode(serviceResponse.getStatus());
				resp.addError(serviceResponse.getMessage());
			} else {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} catch (SDLBusinessException e) {
			resp.setErrorCode(ERROR);
			resp.addError(e.getDescription());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (MaxUsersExceededException e) {
			resp.setErrorCode(ERROR);
			resp.addError(e.getDescription());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (UserNameNotFoundException e) {
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

	
	/**
	 * Remove a firm level access
	 * 
	 * It returns FirmLevelUserResponse which has a User, errorCode & errorDescriptin (in case of failure)
	 * 
	 * 
	 * @param request
	 * @param response
	 * @param user
	 * @return
	 */
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
			this.getService().removeFirmLevelAccess(firmUserName, userAccessId, comments, request.getRemoteUser());
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
				String adminUserName, String firmUserName, Long userAccessId, boolean isEnable, String comments) {
		JsonResponse resp = new JsonResponse();
		try {
			this.getService().enableDisableFirmUserAccess(adminUserName, firmUserName, userAccessId, isEnable,	comments);
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
			String adminUserName, String firmUserName, Long userAccessId, boolean isLock, String comments, String nodeName) {
		
		JsonResponse resp = new JsonResponse();
		try {
			this.getService().lockUnLockUser(firmUserName, isLock, adminUserName, true, nodeName, comments);
			resp.setErrorCode(SUCCESS);
		} catch(Exception e){
			resp.setErrorCode(ERROR);
			resp.addError("Server Error, Please contact Administrator");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} 
		return resp;
	}
	
	@RequestMapping(value="/addFirmUserAccess.admin", method=RequestMethod.POST)
	@ResponseBody
	public JsonResponse addFirmUserAccess(HttpServletRequest request, HttpServletResponse response, 
			@Valid FirmUser userForm, String adminUserName) {
		JsonResponse resp = new JsonResponse();
		if(userForm.getAccessId() == null){
			resp.setErrorCode(ERROR);
			resp.addError(this.getMessage("security.noaccess.access"));
			return resp;
		}
		try {
			this.getService().addFirmUserAccess(adminUserName, userForm.getUsername(), userForm.getAccessId(), this.nodeName);
			resp.setErrorCode(SUCCESS);
		} catch (SDLBusinessException e) {
			resp.setErrorCode(ERROR);
			resp.addError(e.getDescription());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch(UserNameNotFoundException e){
			resp.setErrorCode(ERROR);
			resp.addError(e.getDescription());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch(MaxUsersExceededException e){
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

	private List<FirmUser> getFirmUser(List<FirmUserDTO> firmUsers, Long accessId){
		List<FirmUser> gridUsers = new ArrayList<FirmUser>();
		for(FirmUserDTO firmUser : firmUsers){
			FirmUser gridUser = new FirmUser();
			gridUser.setFirstName(firmUser.getFirstName());
			gridUser.setLastName(firmUser.getLastName());
			gridUser.setPhone(firmUser.getPhone());
			gridUser.setUsername(firmUser.getUsername());
			gridUser.setPurchasedDocuments(firmUser.isPaidTransactions());
			gridUser.setUserAccessId(firmUser.getUserAccessId());
			gridUser.setUserLocked(firmUser.isUserLocked());
			gridUser.setNodeName(firmUser.getNodeName());

			/**
			 * If User has purchased documents then he can not delete the user and subscription status has to be unlocked
			 * If user has purchased the documents then user can lock or unlock the user based on userAccess.isActive value.
			 * Similarly the delete button action will be lock or unlock based on userAccess.isActive value. 
			 * 
			 */
			if(accessId == null || accessId ==  0){
				gridUser.setSubscriptionAction("");
				gridUser.setSubscriptionStatus("");
			} 
			else {
				// Access can't be removed as he has purchased the documents.
				// It's access can be Enabled or Disabled based on subscripton active/inactive
				if(firmUser.isUserAccessActive()){
					gridUser.setSubscriptionAction(DISABLE_ACCESS_ACTION);
					gridUser.setSubscriptionStatus(ACTIVE);
				} else {
					gridUser.setSubscriptionAction(ENABLE_ACCESS_ACTION);
					gridUser.setSubscriptionStatus(INACTIVE);
				}
			}

			// Set if user access is removable
			//if(!firmUser.isPaidTransactions()){
				gridUser.setRemovable(true);
			//}
			
			gridUsers.add(gridUser);
		}
		return gridUsers;
	}
	
	/**
	 * Validate the input fields
	 * 
	 * @param bindingResult
	 * @param userForm
	 * @param resp
	 * @return
	 */
	private boolean validateFormForErrors(BindingResult bindingResult, JsonResponse resp){
		boolean valid = true;
		if (bindingResult.hasErrors()) {
			resp.setErrorCode(ERROR);
			List<ObjectError> errors = bindingResult.getAllErrors();
			for(ObjectError error: errors){
				resp.addError(error.getDefaultMessage());
			}
			valid = false;
		}
		return valid;
	}
	
	/**
	 *  Filter out the subscriptions that are paid only.
	 *  
	 *
	 *  THis is based on the logic below we had in accountInformation.stl
		<#if subscription.subscriptionFee = 0 >
			<@spring.message code="security.ecommerce.label.free" />
		<#elseif subscription.subscriptionFee &gt; 0 >
			<#if subscription.isPayMentPending()>	
				<#if subscription.isAuthorized()>
					<#if subscription.isAccessOverridden()>
						<@spring.message code="security.ecommerce.label.na" />
					<#else>
						<@spring.message code="security.ecommerce.label.paymentDue" />
					</#if>																																				
				<#elseif !subscription.isAuthorized()>
					<@spring.message code="security.ecommerce.label.authorizationRequiredtoPay" />
				</#if>
															
			<#else>
				<#if subscription.category = 'NON_RECURRING_SUBSCRIPTION'>
					<@spring.message code="security.ecommerce.label.payasyougo" />	
				<#else>
					<@spring.message code="security.ecommerce.label.paid" />
				</#if>												
			</#if>
		</#if>		
	 * 
	 * 
	 * @param subscriptions
	 * @return
	 */
	private List<SubscriptionDTO> filterPaidSubscriptions(List<SubscriptionDTO> subscriptions){
		List<SubscriptionDTO> paidSubscriptions = new ArrayList<SubscriptionDTO>();
		for(SubscriptionDTO subscription : subscriptions){
			
			if(subscription.getSubscriptionFee() == 0.0){
				// Subscription is free add it.
				paidSubscriptions.add(subscription);
			} else if (subscription.isAuthorized() && subscription.isPayMentPending()){
				// Payment is pending and authorized , check if an access is overridden
			} else {
				// Payment is NOT pending.
				paidSubscriptions.add(subscription);
			}
		}
		return paidSubscriptions;
	}

	
}