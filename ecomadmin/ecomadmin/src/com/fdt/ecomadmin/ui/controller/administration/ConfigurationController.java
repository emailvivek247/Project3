package com.fdt.ecomadmin.ui.controller.administration;

import static com.fdt.common.util.JsonResponse.ERROR;
import static com.fdt.common.util.JsonResponse.SUCCESS;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.SUB_ADMIN_CONFIG;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.TOP_ADMINISTRATION;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.REDIRECT_UPDATE_NODE_CONFIG;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.REDIRECT_UPDATE_SITE_CONFIG;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.UPDATE_NODE_CONFIG;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.UPDATE_SITE_CONFIG;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_AVAILABLE_NODES_SITES;
import static org.jmesa.worksheet.WorksheetValidation.TRUE;
import static org.jmesa.worksheet.WorksheetValidationType.REQUIRED;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.beanutils.PropertyUtils;
import org.jmesa.model.AllItems;
import org.jmesa.model.TableModel;
import org.jmesa.model.WorksheetSaver;
import org.jmesa.view.component.Table;
import org.jmesa.view.html.component.HtmlColumn;
import org.jmesa.view.html.component.HtmlRow;
import org.jmesa.view.html.component.HtmlTable;
import org.jmesa.worksheet.Worksheet;
import org.jmesa.worksheet.WorksheetCallbackHandler;
import org.jmesa.worksheet.WorksheetColumn;
import org.jmesa.worksheet.WorksheetRow;
import org.jmesa.worksheet.WorksheetRowStatus;
import org.jmesa.worksheet.WorksheetValidation;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.ui.breadcrumbs.Link;
import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.common.util.JsonResponse;
import com.fdt.ecom.entity.Location;
import com.fdt.ecom.entity.Node;
import com.fdt.ecom.entity.NodeConfiguration;
import com.fdt.ecom.entity.ReceiptConfiguration;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.SiteConfiguration;
import com.fdt.ecomadmin.jmesa.CustomToolbar;
import com.fdt.ecomadmin.ui.form.administration.NodeConfigurationForm;
import com.fdt.ecomadmin.ui.form.administration.SiteConfigurationForm;

@Controller
public class ConfigurationController extends AbstractBaseController {


	@Link(label="Node Configuration", family="ACCEPTADMIN", parent = "Home" )
	@RequestMapping(value="/viewnodesandsitesconfig.admin")
	public ModelAndView viewNodesAndSites(HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, VIEW_AVAILABLE_NODES_SITES, TOP_ADMINISTRATION, SUB_ADMIN_CONFIG);
		List<Node> nodes = new LinkedList<Node>();
		List<Site> sites = this.getServiceStub().getSites();
		Map<Long, Node> nodeMap = new HashMap<Long, Node>();
		for (Site site : sites) {
			nodeMap.put(site.getNode().getId(), site.getNode());
		}
		for (Map.Entry<Long,Node> node : nodeMap.entrySet()) {
			nodes.add(node.getValue());
		}
		modelAndView.addObject("nodes", nodes);
		modelAndView.addObject("sites", sites);
		return modelAndView;
	}

	@Link(label="Update Node Configuration", family="ACCEPTADMIN", parent = "Configuration" )
	@RequestMapping(value = "/viewnodeconfig.admin")
    public ModelAndView viewNodeConfig(HttpServletRequest request, @RequestParam(required = false) String nodeName) {
		ModelAndView modelAndView = this.getModelAndView(request, UPDATE_NODE_CONFIG, TOP_ADMINISTRATION, SUB_ADMIN_CONFIG);
        NodeConfigurationForm nodeConfigurationForm = null;
        NodeConfiguration nodeConfiguration = this.getServiceStub().getNodeConfiguration(nodeName);
        if (nodeConfiguration != null) {
        	nodeConfigurationForm = new NodeConfigurationForm();
        	nodeConfigurationForm.setFromEmailAddress(nodeConfiguration.getFromEmailAddress());
	        nodeConfigurationForm.setResetPasswordSubject(nodeConfiguration.getResetPasswordSubject());
	        nodeConfigurationForm.setUserActivationSubject(nodeConfiguration.getUserActivationSubject());
	        nodeConfigurationForm.setLockUserSubject(nodeConfiguration.getLockUserSub());
	        nodeConfigurationForm.setUnlockUserSubject(nodeConfiguration.getUnlockUserSub());
	        nodeConfigurationForm.setAlertSubject(nodeConfiguration.getAlertSubject());
	        nodeConfigurationForm.setInActiveUserNotifSubject(nodeConfiguration.getInActiveUserNotifSubject());
	        nodeConfigurationForm.setNode(nodeConfiguration.getNodeName());
	        modelAndView.addObject("modUserId" , nodeConfiguration.getModifiedBy());
        }
        modelAndView.addObject("nodeConfigForm" , nodeConfigurationForm);
        return modelAndView;
    }

	@RequestMapping(value = "/updatenodeconfig.admin")
    public ModelAndView updateNodeConfig(@ModelAttribute("nodeConfigForm")
    		@Valid NodeConfigurationForm nodeConfigurationForm,
    		BindingResult bindingResult, HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, UPDATE_NODE_CONFIG, TOP_ADMINISTRATION, SUB_ADMIN_CONFIG);
		verifyBinding(bindingResult);
		if (bindingResult.hasErrors()) {
			return modelAndView;
		}
        NodeConfiguration nodeConfiguration = new NodeConfiguration();
        nodeConfiguration.setFromEmailAddress(nodeConfigurationForm.getFromEmailAddress());
        nodeConfiguration.setResetPasswordSubject(nodeConfigurationForm.getResetPasswordSubject());
        nodeConfiguration.setUserActivationSubject(nodeConfigurationForm.getUserActivationSubject());
        nodeConfiguration.setLockUserSub(nodeConfigurationForm.getLockUserSubject());
        nodeConfiguration.setUnlockUserSub(nodeConfigurationForm.getUnlockUserSubject());
        nodeConfiguration.setNodeName(nodeConfigurationForm.getNode());
        nodeConfiguration.setAlertSubject(nodeConfigurationForm.getAlertSubject());
        nodeConfiguration.setInActiveUserNotifSubject(nodeConfigurationForm.getInActiveUserNotifSubject());
        nodeConfiguration.setModifiedBy(request.getRemoteUser());
        this.getServiceStub().updateNodeConfiguration(nodeConfiguration);
        modelAndView.setViewName(REDIRECT_UPDATE_NODE_CONFIG + "?nodeName=" + nodeConfigurationForm.getNode());
        return modelAndView;
    }

	@Link(label="Site Configuration", family="ACCEPTADMIN", parent = "Configuration" )
	@RequestMapping(value = "/viewsiteconfig.admin")
    public ModelAndView viewSiteConfiguration(HttpServletRequest request, @RequestParam(required = false) Long siteId) {
		ModelAndView modelAndView = this.getModelAndView(request, UPDATE_SITE_CONFIG, TOP_ADMINISTRATION, SUB_ADMIN_CONFIG);
        SiteConfigurationForm siteConfigurationForm = null;
        SiteConfiguration siteConfiguration = this.getServiceStub().getSiteConfiguration(siteId);
        if (siteConfiguration != null) {
        	siteConfigurationForm = new  SiteConfigurationForm();
        	siteConfigurationForm.setSiteId(siteConfiguration.getSiteId());
			siteConfigurationForm.setSiteId(siteConfiguration.getSiteId());
			siteConfigurationForm.setFromEmailAddress(siteConfiguration.getFromEmailAddress());
			siteConfigurationForm.setPaymentConfirmationSubject(siteConfiguration.getPaymentConfirmationSubject());
			siteConfigurationForm.setChangeSubscriptionSubject(siteConfiguration.getChangeSubscriptionSubject());
			siteConfigurationForm.setCancelSubscriptionSubject(siteConfiguration.getCancelSubscriptionSubject());
			siteConfigurationForm.setReactivateSubscriptionSubject(siteConfiguration.getReactivateSubscriptionSubject());
			siteConfigurationForm.setRecurringPaymentSuccessSubject(siteConfiguration.getRecurringPaymentSuccessSubject());
			siteConfigurationForm.setRecurringPaymentUnsuccessfulSubject(siteConfiguration.getRecurringPaymentUnsuccessfulSubject());
			siteConfigurationForm.setWebPaymentConfirmationSubject(siteConfiguration.getWebPaymentConfSubject());
			siteConfigurationForm.setPayAsUGoPaymentConfirmationSubject(siteConfiguration.getPayAsUGoPaymentConfSubject());
			siteConfigurationForm.setRemoveSubscriptionSubject(siteConfiguration.getRemoveSubscriptionSubject());
			siteConfigurationForm.setAccessAuthorizationSubject(siteConfiguration.getAccessAuthorizationSubject());
			siteConfigurationForm.setExpiredOverriddenSubscriptionNotificationSubject(siteConfiguration.getExpiredOverriddenSubscriptionNotificationSubject());
			modelAndView.addObject("modUserId" , siteConfiguration.getModifiedBy());
        }
        modelAndView.addObject("siteConfigForm" , siteConfigurationForm);
        modelAndView.addObject("siteId" , siteId);
        return modelAndView;
    }

    private String getReceiptConfigurationHTML(final Long siteId, HttpServletRequest request) {
    	TableModel tableModel = new TableModel("worksheet", request);
    	tableModel.setToolbar(new CustomToolbar());
    	tableModel.setEditable(true);
		tableModel.saveWorksheet(new WorksheetSaver() {
	            public void saveWorksheet(Worksheet worksheet) {
	                saveWorksheetChanges(worksheet, siteId);
	            }
	        });

		 tableModel.setItems(new AllItems() {
	            public Collection<?> getItems() {
	                return getServiceStub().getReceiptConfigurationsForSite(siteId);
	            }
	        });

		//tableModel.setItems(receiptConfigurations);

		tableModel.setTable(getUsersReceiptConfigurationHtmlTable());
		String webTransactionHTML = tableModel.render();
		return webTransactionHTML;
    }

    protected void saveWorksheetChanges(Worksheet worksheet, Long siteId) {
        final List<ReceiptConfiguration> receiptConfigurations = this.getServiceStub().getReceiptConfigurationsForSite(siteId);
        worksheet.processRows(new WorksheetCallbackHandler() {
            public void process(WorksheetRow worksheetRow) {
               if (worksheetRow.getRowStatus().equals(WorksheetRowStatus.MODIFY)) {
                    Collection<WorksheetColumn> columns = worksheetRow.getColumns();
                    for (ReceiptConfiguration receiptConfiguration : receiptConfigurations)
                    {
                    	if (receiptConfiguration.getId() == Long.parseLong(worksheetRow.getUniqueProperty().getValue())) {
                    		for (WorksheetColumn worksheetColumn : columns) {
                                String changedValue = worksheetColumn.getChangedValue();

                                validateColumn(worksheetColumn, changedValue);
                                if (worksheetColumn.hasError()) {
                                    continue;
                                }
                                String property = worksheetColumn.getProperty();
                                try {
                                	PropertyUtils.setProperty(receiptConfiguration, property, changedValue);
                                } catch (Exception ex) {
                                    String msg = "Not able to set the property [" + property + "] when saving worksheet.";
                                    throw new RuntimeException(msg);
                                }
                            }
                    		getServiceStub().saveReceiptConfiguration(receiptConfiguration);
                    	}
                    }
                }
            }
        });
    }

    private void validateColumn(WorksheetColumn worksheetColumn, String changedValue) {
        if (changedValue.equals("foo")) {
            worksheetColumn.setErrorKey("foo.error");
        } else {
            worksheetColumn.removeError();
        }
    }

	@RequestMapping(value = "/updatesiteconfig_temp.admin")
    public ModelAndView updateSiteConfig(@ModelAttribute("siteConfigForm") @Valid SiteConfigurationForm siteConfigurationForm,
                            BindingResult bindingResult, HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, UPDATE_SITE_CONFIG, TOP_ADMINISTRATION, SUB_ADMIN_CONFIG);
    	verifyBinding(bindingResult);
		if (bindingResult.hasErrors()) {
			return modelAndView;
		}
        SiteConfiguration siteConfiguration = new SiteConfiguration();
        siteConfiguration.setFromEmailAddress(siteConfigurationForm.getFromEmailAddress());
        siteConfiguration.setPaymentConfirmationSubject(siteConfigurationForm.getPaymentConfirmationSubject());
        siteConfiguration.setChangeSubscriptionSubject(siteConfigurationForm.getChangeSubscriptionSubject());
        siteConfiguration.setCancelSubscriptionSubject(siteConfigurationForm.getCancelSubscriptionSubject());
        siteConfiguration.setReactivateSubscriptionSubject(siteConfigurationForm.getReactivateSubscriptionSubject());
        siteConfiguration.setRecurringPaymentSuccessSubject(siteConfigurationForm.getRecurringPaymentSuccessSubject());
        siteConfiguration.setRecurringPaymentUnsuccessfulSubject(
        	siteConfigurationForm.getRecurringPaymentUnsuccessfulSubject());
        siteConfiguration.setWebPaymentConfSubject(
	        siteConfigurationForm.getWebPaymentConfirmationSubject());
        siteConfiguration.setPayAsUGoPaymentConfSubject(siteConfigurationForm.getPayAsUGoPaymentConfirmationSubject());
        siteConfiguration.setRemoveSubscriptionSubject(siteConfigurationForm.getRemoveSubscriptionSubject());
        siteConfiguration.setAccessAuthorizationSubject(siteConfigurationForm.getAccessAuthorizationSubject());
        siteConfiguration.setExpiredOverriddenSubscriptionNotificationSubject(siteConfigurationForm.getExpiredOverriddenSubscriptionNotificationSubject());
        siteConfiguration.setSiteId(siteConfigurationForm.getSiteId());
        siteConfiguration.setModifiedBy(request.getRemoteUser());
        this.getServiceStub().updateSiteConfiguration(siteConfiguration);
        modelAndView.setViewName(REDIRECT_UPDATE_SITE_CONFIG + "?siteId=" + siteConfigurationForm.getSiteId().toString());
        return modelAndView;
    }

	@RequestMapping(value="/updatesiteconfig.admin", method=RequestMethod.POST,  produces="application/json")
	@ResponseBody
	public JsonResponse updateSiteConfiguration(HttpServletRequest request, HttpServletResponse response, String adminUserName,
			@Valid SiteConfigurationForm siteConfigurationForm, BindingResult bindingResult){
		JsonResponse resp = new JsonResponse();

		if(this.validateFormForErrors(bindingResult, resp)) {
	        SiteConfiguration siteConfiguration = new SiteConfiguration();
	        siteConfiguration.setFromEmailAddress(siteConfigurationForm.getFromEmailAddress());
	        siteConfiguration.setPaymentConfirmationSubject(siteConfigurationForm.getPaymentConfirmationSubject());
	        siteConfiguration.setChangeSubscriptionSubject(siteConfigurationForm.getChangeSubscriptionSubject());
	        siteConfiguration.setCancelSubscriptionSubject(siteConfigurationForm.getCancelSubscriptionSubject());
	        siteConfiguration.setReactivateSubscriptionSubject(siteConfigurationForm.getReactivateSubscriptionSubject());
	        siteConfiguration.setRecurringPaymentSuccessSubject(siteConfigurationForm.getRecurringPaymentSuccessSubject());
	        siteConfiguration.setRecurringPaymentUnsuccessfulSubject(
	        	siteConfigurationForm.getRecurringPaymentUnsuccessfulSubject());
	        siteConfiguration.setWebPaymentConfSubject(
		        siteConfigurationForm.getWebPaymentConfirmationSubject());
	        siteConfiguration.setPayAsUGoPaymentConfSubject(siteConfigurationForm.getPayAsUGoPaymentConfirmationSubject());
	        siteConfiguration.setRemoveSubscriptionSubject(siteConfigurationForm.getRemoveSubscriptionSubject());
	        siteConfiguration.setAccessAuthorizationSubject(siteConfigurationForm.getAccessAuthorizationSubject());
	        siteConfiguration.setExpiredOverriddenSubscriptionNotificationSubject(siteConfigurationForm.getExpiredOverriddenSubscriptionNotificationSubject());
	        siteConfiguration.setSiteId(siteConfigurationForm.getSiteId());
	        siteConfiguration.setModifiedBy(request.getRemoteUser());
	        this.getServiceStub().updateSiteConfiguration(siteConfiguration);
	        resp.setErrorCode(SUCCESS);
		} else {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return resp;
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



	private Table getUsersReceiptConfigurationHtmlTable() {
	 	HtmlTable table = new HtmlTable().caption("Receipt Configuration");
	 	HtmlRow row = new HtmlRow().uniqueProperty("id");
		table.setRow(row);

		HtmlColumn businessName = new HtmlColumn("businessName").title("Business Name");
		businessName.addWorksheetValidation(new WorksheetValidation(REQUIRED, TRUE));
		row.addColumn(businessName);

		HtmlColumn addressLine1 = new HtmlColumn("addressLine1").title("Address Line 1");
		addressLine1.addWorksheetValidation(new WorksheetValidation(REQUIRED, TRUE));
		row.addColumn(addressLine1);

		HtmlColumn addressLine2 = new HtmlColumn("addressLine2").title("Address Line 2");
		addressLine2.addWorksheetValidation(new WorksheetValidation(REQUIRED, TRUE));
		row.addColumn(addressLine2);

		HtmlColumn city = new HtmlColumn("city").title("City");
		city.addWorksheetValidation(new WorksheetValidation(REQUIRED, TRUE));
		row.addColumn(city);

		HtmlColumn state = new HtmlColumn("state").title("State");
		state.addWorksheetValidation(new WorksheetValidation(REQUIRED, TRUE));
		row.addColumn(state);

		HtmlColumn zip = new HtmlColumn("zip").title("Zip");
		zip.addWorksheetValidation(new WorksheetValidation(REQUIRED, TRUE));
		row.addColumn(zip);

		HtmlColumn phone = new HtmlColumn("phone").title("Phone");
		phone.addWorksheetValidation(new WorksheetValidation(REQUIRED, TRUE));
		phone.setFilterable(false);
		row.addColumn(phone);

		HtmlColumn comments1 = new HtmlColumn("comments1").title("Comments 1");
		comments1.setFilterable(false);
		row.addColumn(comments1);

		HtmlColumn comments2 = new HtmlColumn("comments2").title("Comments 2");
		comments2.setFilterable(false);
		row.addColumn(comments2);

		HtmlColumn type = new HtmlColumn("type").title("TYPE").editable(false);
		type.setFilterable(false);
		row.addColumn(type);

		return table;
	}

	@RequestMapping(value="/getLocationsBySiteId.admin", method=RequestMethod.GET,  produces="application/json")
	@ResponseBody
    public List<Location> getLocationsBySiteId(HttpServletRequest request, @RequestParam(required = true) Long siteId) {
    	return this.getServiceStub().getLocationsBySiteId(siteId);
    }

    @RequestMapping(value="/getLocationSignature.admin")
    public void getLocationSignature(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = true) Long locationId) {
    	Location location = this.getServiceStub().getLocationsSignatureById(locationId);
    	if(location.getSignature() != null && location.getSignature().length > 0){
    		response.setContentLength(location.getSignature().length);
        	response.setContentType("image/jpeg");
        	try{
        		response.getOutputStream().write(location.getSignature());
        	} catch (IOException ie){
            	logger.error("Error while writing image file for Location Signature " , ie);
        	}
    	}
    }

    @RequestMapping(value="/getLocationSeal.admin")
    public void getLocationSeal(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = true) Long locationId) {
    	Location location = this.getServiceStub().getLocationsSealById(locationId);
    	if(location.getSealOfAuthenticity() != null && location.getSealOfAuthenticity().length > 0){
    		response.setContentLength(location.getSealOfAuthenticity().length);
        	response.setContentType("image/jpeg");
        	try{
        		response.getOutputStream().write(location.getSealOfAuthenticity());
        	} catch (IOException ie){
            	logger.error("Error while writing image file for Location Signature " , ie);
        	}
    	}
    }


    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAllowedFields(new String[] {
        	"siteId",
			"fromEmailAddress",
			"node",
            "resetPasswordSubject",
            "userActivationSubject",
            "paymentConfirmationSubject",
            "changeSubscriptionSubject",
            "cancelSubscriptionSubject",
            "reactivateSubscriptionSubject",
            "recurringPaymentSuccessSubject",
            "recurringPaymentUnsuccessfulSubject",
            "webPaymentConfirmationSubject",
            "payAsUGoPaymentConfirmationSubject",
            "submit",
            "username",
            "lockUserSubject",
            "unlockUserSubject",
            "alertSubject",
            "removeSubscriptionSubject",
            "accessAuthorizationSubject",
            "expiredOverriddenSubscriptionNotificationSubject",
            "inActiveUserNotifSubject",
        });
    }
}
