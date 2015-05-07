package com.fdt.ecomadmin.ui.controller.administration;

import static com.fdt.ecomadmin.ui.controller.MenuConstants.SUB_ADMIN_REFRESH_CACHE;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.TOP_ADMINISTRATION;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.REFRESH_CACHE;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.dto.ErrorCodeDTO;
import com.fdt.common.ui.breadcrumbs.Link;
import com.fdt.common.ui.controller.AbstractBaseController;

@Controller
public class EhCacheController extends AbstractBaseController {

	@Link(label="Refresh Cache Data", family="ACCEPTADMIN", parent = "Home" )
	@RequestMapping(value="/viewCaches.admin")
	public ModelAndView viewCacheList(HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, REFRESH_CACHE, TOP_ADMINISTRATION, SUB_ADMIN_REFRESH_CACHE);
		List<String> cacheList = new LinkedList<String>();
		cacheList = this.getServiceStub().getCacheNames();
		modelAndView.addObject("cacheList",cacheList);
		return modelAndView;
	}

	@RequestMapping(value="/refreshCache.admin", produces="application/json")
	@ResponseBody
	public List<ErrorCodeDTO> refreshCache(HttpServletRequest request, @RequestParam(required = false) String cacheName) {
		List<ErrorCodeDTO> errors = new LinkedList<ErrorCodeDTO>();
		if (cacheName != null  && cacheName != "") {
			try {
				this.getServiceStub().refreshCacheByName(cacheName);
				ErrorCodeDTO error = new ErrorCodeDTO();
				error.setCode("SUCCESS");
				error.setDescription("Cached Refreshed Successfully");
				errors.add(error);
			} catch (Exception e) {
				ErrorCodeDTO error = new ErrorCodeDTO();
				error.setCode("ERROR");
				error.setDescription(this.getMessage("system.invalid.data"));
				errors.add(error);
				return errors;
			}
		}
		return errors;
	}


	@RequestMapping(value="/refreshAllCaches.admin", produces="application/json")
	@ResponseBody
	public List<ErrorCodeDTO> refreshAllCaches(HttpServletRequest request) {
		List<ErrorCodeDTO> errors = new LinkedList<ErrorCodeDTO>();
		try {
			this.getServiceStub().refreshCache();
			ErrorCodeDTO error = new ErrorCodeDTO();
			error.setCode("SUCCESS");
			error.setDescription("Cached Refreshed Successfully");
			errors.add(error);
		} catch (Exception e) {
			ErrorCodeDTO error = new ErrorCodeDTO();
			error.setCode("ERROR");
			error.setDescription(this.getMessage("system.invalid.data"));
			errors.add(error);
			return errors;
		}

		return errors;
	}

}