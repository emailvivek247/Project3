package com.fdt.ecomadmin.ui.controller.fundtransfer;

import static com.fdt.ecomadmin.ui.controller.MenuConstants.SUB_FUNDS_TRANSFER_ACH;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.TOP_FUNDS_TRANSFER;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.ACH_PAYMENT;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.ACH_PAYMENT_CONFIRMATION;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.REDIRECT_ACCESS_DENIED;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.achtx.dto.ACHTxDTO;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.exception.SDLException;
import com.fdt.common.ui.breadcrumbs.Link;
import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.enums.PaymentType;

@Controller
public class AchPaymentController extends AbstractBaseController {

	@Link(label="ACH", family="ACCEPTADMIN", parent = "Home" )
	@RequestMapping(value="/achPayment.admin")
	public ModelAndView achtransfer(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(required = false) Long siteId,
			@RequestParam(required = false) String txMode) {
		ModelAndView modelAndView = this.getModelAndView(request, ACH_PAYMENT, TOP_FUNDS_TRANSFER, SUB_FUNDS_TRANSFER_ACH);
		if (!this.isFeatureEnabledForUser(request, "ACHPayment")) {
			modelAndView.setViewName(REDIRECT_ACCESS_DENIED);
			return modelAndView;
		}
		List<Site> sites = this.getAssignedSites(request);
		List<PaymentType> paymentTypes = new LinkedList<PaymentType>();
		for (PaymentType transactionMode : PaymentType.values()) {
			paymentTypes.add(transactionMode);
		}
		if (!this.isInternalUser(request)) {
			for (Site site : sites) {
				siteId = site.getId();
			}
		}
		modelAndView.addObject("sites", sites);
		modelAndView.addObject("paymentTypes", paymentTypes);
		return modelAndView;
	}
	@Link(label="ACH Balance", family="ACCEPTADMIN", parent = "Home" )
	@RequestMapping(value="/calculateBalance.admin")
	public ModelAndView calculateBalance(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(required = false) Long siteId,
			@RequestParam(required = false) String txMode) {
		ModelAndView modelAndView = this.getModelAndView(request, ACH_PAYMENT, TOP_FUNDS_TRANSFER, SUB_FUNDS_TRANSFER_ACH);
		if (!this.isFeatureEnabledForUser(request, "ACHPayment")) {
			modelAndView.setViewName(REDIRECT_ACCESS_DENIED);
			return modelAndView;
		}
		List<Site> sites = this.getAssignedSites(request);
		List<PaymentType> paymentTypes = new LinkedList<PaymentType>();
		for (PaymentType transactionMode : PaymentType.values()) {
			paymentTypes.add(transactionMode);
		}
		if (!this.isInternalUser(request)) {
			for (Site site : sites) {
				siteId = site.getId();
			}
		}
		PaymentType selectedPaymentType = null;
		if (txMode.equalsIgnoreCase(PaymentType.WEB.toString())) {
			selectedPaymentType = PaymentType.WEB;
		} else if (txMode.equalsIgnoreCase(PaymentType.RECURRING.toString())) {
			selectedPaymentType = PaymentType.RECURRING;
		}  else if (txMode.equalsIgnoreCase(PaymentType.OTC.toString())) {
			selectedPaymentType = PaymentType.OTC;
		} else if (txMode.equalsIgnoreCase(PaymentType.PAYASUGO.toString())) {
			selectedPaymentType = PaymentType.PAYASUGO;
		}
		ACHTxDTO achResponse = null;
    	Date fromDate = null;
    	Date toDate = null;
    	try {
    		if (siteId != null && txMode != null && siteId > 0 && txMode != "")  {
        		achResponse = this.getServiceStub().getACHDetailsForTransfer(siteId, selectedPaymentType,
        				request.getRemoteHost(), request.getRemoteUser());
        	}
		} catch (SDLBusinessException sdlBusinessException) {
			modelAndView.addObject("ERROR", sdlBusinessException.getDescription());
		} catch (SDLException sdlException) {
			modelAndView.addObject("ERROR", sdlException.getDescription());
		}

		modelAndView.addObject("sites", sites);
		modelAndView.addObject("paymentTypes", paymentTypes);
		modelAndView.addObject("selectedSiteId", siteId);
		modelAndView.addObject("selectedTxMode", txMode);
		modelAndView.addObject("achResponse", achResponse);
		modelAndView.addObject("fromDate", fromDate);
		modelAndView.addObject("toDate", toDate);
		return modelAndView;
	}

	@Link(label="ACH Payment", family="ACCEPTADMIN", parent = "Home" )
	@RequestMapping(value="/processAchPayment.admin")
	public ModelAndView processAchPayment(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(required = false) Long siteId,
			@RequestParam(required = false) String txMode) {
		ModelAndView modelAndView = this.getModelAndView(request, ACH_PAYMENT_CONFIRMATION, TOP_FUNDS_TRANSFER, SUB_FUNDS_TRANSFER_ACH);
		if (!this.isFeatureEnabledForUser(request, "ACHPayment")) {
			modelAndView.setViewName(REDIRECT_ACCESS_DENIED);
			return modelAndView;
		}
		List<Site> sites = this.getAssignedSites(request);
		if (!this.isInternalUser(request)) {
			for (Site site : sites) {
				siteId = site.getId();
			}
		}

		PaymentType selectedPaymentType = null;
		if (txMode.equalsIgnoreCase(PaymentType.WEB.toString())) {
			selectedPaymentType = PaymentType.WEB;
		} else if (txMode.equalsIgnoreCase(PaymentType.RECURRING.toString())) {
			selectedPaymentType = PaymentType.RECURRING;
		}  else if (txMode.equalsIgnoreCase(PaymentType.OTC.toString())) {
			selectedPaymentType = PaymentType.OTC;
		} else if (txMode.equalsIgnoreCase(PaymentType.PAYASUGO.toString())) {
			selectedPaymentType = PaymentType.PAYASUGO;
		}

		ACHTxDTO achResponse = null;
    	if (siteId != null && txMode != null && siteId > 0 && txMode != "" && selectedPaymentType != null)  {
    		try {
				achResponse = this.getServiceStub().doACHTransfer(selectedPaymentType,
					siteId, request.getRemoteHost(), request.getRemoteUser());
			} catch (SDLBusinessException sdlBusinessException) {
				modelAndView.addObject("ERROR", sdlBusinessException.getDescription());
			} catch (SDLException sdlException) {
    			modelAndView.addObject("ERROR", sdlException.getDescription());
			}
    	}
		modelAndView.addObject("achResponse", achResponse);
		return modelAndView;
	}
}