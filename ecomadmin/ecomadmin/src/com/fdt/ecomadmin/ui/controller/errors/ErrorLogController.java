package com.fdt.ecomadmin.ui.controller.errors;

import static com.fdt.ecomadmin.ui.controller.MenuConstants.SUB_ADMIN_ERROR;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.TOP_ADMINISTRATION;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_ERROR_LOG;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.ui.breadcrumbs.Link;
import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.common.util.JsonResponse;

@Controller
public class ErrorLogController extends AbstractBaseController {

	@Link(label="Error Log", family="ACCEPTADMIN", parent = "Home" )
	@RequestMapping(value="/viewErrorLog.admin")
	public ModelAndView getErrorLog(HttpServletRequest request,
			@RequestParam(required = false) String fromDate,
			@RequestParam(required = false) String toDate,
			@RequestParam(required = false) String userName) {
		ModelAndView modelAndView = this.getModelAndView(request, VIEW_ERROR_LOG, TOP_ADMINISTRATION, SUB_ADMIN_ERROR);
		return modelAndView;
	}

	@RequestMapping(value="/searchErrorLog.admin", produces="application/json")
	@ResponseBody
	public PageRecordsDTO searchErrorLog(HttpServletRequest request,
			@RequestParam(required = false) String fromDate,
			@RequestParam(required = false) String toDate,
			@RequestParam(required = false) String userName,
			@RequestParam(required = true) Integer skip,
			@RequestParam(required = true) Integer take) {
			return this.getServiceStub().getErrorLog(fromDate, toDate, userName, skip, take);
	}

    @RequestMapping(value="/deleteErrorLog.admin", method=RequestMethod.GET,  produces="application/json")
	@ResponseBody
    public JsonResponse deleteErrorLog(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(required = true) Long errorLogId) {
    	JsonResponse resp = new JsonResponse();
    	try{
    		this.getServiceStub().deleteErrorLogContents(errorLogId);
    		resp.setErrorCode("SUCCESS");
    	}catch(Exception e){
			resp.setErrorCode("ERROR");
			resp.addError("Server Error, Please contact Administrator");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    	}
    	return resp;
    }

}