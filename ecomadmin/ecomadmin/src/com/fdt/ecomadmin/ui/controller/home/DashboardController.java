package com.fdt.ecomadmin.ui.controller.home;

import static com.fdt.ecomadmin.ui.controller.MenuConstants.TOP_DASH;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.TOP_SITE_INFO;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.LOGINSUCCESS;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_SITE_DETAILS;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_SITE_INFO;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.ui.breadcrumbs.Link;
import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.ecom.dto.UserCountDTO;
import com.fdt.ecom.entity.Node;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.SiteConfiguration;
import com.fdt.ecomadmin.ui.form.administration.SiteConfigurationForm;

@Controller
public class DashboardController extends AbstractBaseController {


	@RequestMapping(value="/getUserCountsBySite.admin", produces="application/json")
	@ResponseBody
	public List<UserCountDTO> getUserCountsBySite(HttpServletRequest request, @RequestParam(required = false) Long siteId) {
		if (!this.isInternalUser(request)) {
			List<Site> sites = this.getAssignedSites(request);
			for (Site site : sites) {
				siteId = site.getId();
			}
    	}
		List<UserCountDTO> userCounts = new LinkedList<UserCountDTO>();

		if(siteId == null) {
			userCounts = this.serviceStub.getUserCountsForAllSite();
		} else {
			UserCountDTO userCount = this.serviceStub.getUserCountForSite(siteId);
			userCounts.add(userCount);
		}

		return userCounts;
	}

	@RequestMapping(value="/getUserCountsBySubscription.admin", produces="application/json")
	@ResponseBody
	public List<UserCountDTO> getUserCountsBySubscription(HttpServletRequest request, @RequestParam(required = false) Long siteId) {
		if (!this.isInternalUser(request)) {
			List<Site> sites = this.getAssignedSites(request);
			for (Site site : sites) {
				siteId = site.getId();
			}
    	}
		List<UserCountDTO> userCounts = this.serviceStub.getUserCountsBySubForASite(siteId);
		return userCounts;
	}

	@RequestMapping(value="/getUserDistributionBySubscription.admin", produces="application/json")
	@ResponseBody
	public List<UserCountDTO> getUserDistributionBySubscription(HttpServletRequest request, @RequestParam(required = false) Long siteId,
		@RequestParam(required = false) Long accessId) {
		if (!this.isInternalUser(request)) {
			List<Site> sites = this.getAssignedSites(request);
			for (Site site : sites) {
				siteId = site.getId();
			}
    	}
		List<UserCountDTO> userCounts = this.serviceStub.getUserDistributionBySubscription(siteId, accessId);
		return userCounts;
	}

	@Link(label="Home", family="ACCEPTADMIN", parent = "" )
	@RequestMapping(value="/dashboard.admin")
	public ModelAndView viewDashboard(HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, LOGINSUCCESS, TOP_DASH, "NA");
		modelAndView.addObject("request", request);
		List<Node> nodes = new LinkedList<Node>();
		boolean isInternalUser = false;
		Map<Long, Node> nodeMap = new HashMap<Long, Node>();
		List<Site> sites = this.getAssignedSites(request);
		for (Site site : sites) {
			nodeMap.put(site.getNode().getId(), site.getNode());
		}
		for (Map.Entry<Long,Node> node : nodeMap.entrySet()) {
			nodes.add(node.getValue());
		}
		modelAndView.addObject("sites", sites);
		modelAndView.addObject("nodes", nodes);
		if (this.isInternalUser(request)) {
			isInternalUser = true;
		}
		modelAndView.addObject("isInternalUser", isInternalUser);
		return modelAndView;
	}

	@Link(label="Site Information", family="ACCEPTADMIN", parent = "Home" )
	@RequestMapping(value="/viewSiteInfo.admin")
	public ModelAndView viewSiteInfo(HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, VIEW_SITE_INFO, TOP_SITE_INFO, "NA");
		modelAndView.addObject("request", request);
		List<Node> nodes = new LinkedList<Node>();
		boolean isInternalUser = false;
		Map<Long, Node> nodeMap = new HashMap<Long, Node>();
		List<Site> sites = this.getAssignedSites(request);
		for (Site site : sites) {
			nodeMap.put(site.getNode().getId(), site.getNode());
		}
		for (Map.Entry<Long,Node> node : nodeMap.entrySet()) {
			nodes.add(node.getValue());
		}
		modelAndView.addObject("sites", sites);
		modelAndView.addObject("nodes", nodes);
		if (this.isInternalUser(request)) {
			isInternalUser = true;
		}
		modelAndView.addObject("isInternalUser", isInternalUser);
		return modelAndView;
	}

	@RequestMapping(value="/getSiteList.admin", produces="application/json")
	@ResponseBody
	public List<Site> getSiteList(HttpServletRequest request) {
		List<Site> sites = this.getAssignedSites(request);
		return sites;
	}

	@RequestMapping(value="/viewSiteDetails.admin")
	public ModelAndView getSiteAdminDetails(HttpServletRequest request, @RequestParam(required = false) Long siteId) {
		ModelAndView modelAndView = this.getModelAndView(request, VIEW_SITE_DETAILS);
		if (!this.isInternalUser(request)) {
			List<Site> sites = this.getAssignedSites(request);
			for (Site site : sites) {
				siteId = site.getId();
			}
    	}
		Site site = this.getServiceStub().getSiteAdminDetails(siteId);
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
		modelAndView.addObject("site", site);
		return modelAndView;
	}


}