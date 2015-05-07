package com.fdt.ecomadmin.ui.controller.administration;

import static com.fdt.ecomadmin.ui.controller.MenuConstants.SUB_ADMIN_USER_MGMT;
import static com.fdt.ecomadmin.ui.controller.MenuConstants.TOP_ADMINISTRATION;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.REDIRECT_VIEW_ADMIN_USERS;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.REDIRECT_VIEW_ADMIN_USER_DETAIL;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_ADMIN_USERS;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_ADMIN_USER_DETAIL;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fdt.common.dto.ErrorCodeDTO;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.ui.breadcrumbs.Link;
import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.security.entity.EComAdminAccess;
import com.fdt.security.entity.EComAdminUser;
import com.fdt.security.exception.UserNameNotFoundException;

@Controller
public class EComAdminUserManagementController extends AbstractBaseController {

    /** Used to Store the User Not Found Message **/
    protected final static String USER_NOT_FOUND_MSG = "USER_NOT_FOUND_MSG";

	@RequestMapping(value="/getEcomadminusers.admin", produces="application/json")
	@ResponseBody
	public List<EComAdminUser> getEcomadminusers(HttpServletRequest request, @RequestParam(required = false) Long siteId) {
        List<EComAdminUser> users = this.eComAdminUserService.getUsers();
		return users;
	}

	@Link(label="Accept User Management", family="ACCEPTADMIN", parent = "Home" )
	@RequestMapping(value="/viewecomadminusers.admin")
	public ModelAndView viewEcomAdminUsers(HttpServletRequest request) {
		ModelAndView modelAndView = this.getModelAndView(request, VIEW_ADMIN_USERS, TOP_ADMINISTRATION, SUB_ADMIN_USER_MGMT);
		return modelAndView;
	}

	@Link(label="Admin Details", family="AdminUserManagementController", parent = "Accept Administrators" )
    @RequestMapping(value="/viewecomadminuserdetails.admin")
    public ModelAndView getAdminUserDetails(HttpServletRequest request, @RequestParam(required = false) String username) {
        ModelAndView modelAndView = this.getModelAndView(request, VIEW_ADMIN_USER_DETAIL);
        EComAdminUser user = this.eComAdminUserService.loadUserByUsername(username);
        List<EComAdminAccess> accessList =  this.eComAdminUserService.getAccess();
        modelAndView.addObject("user", user);
        modelAndView.addObject("accessList", accessList);
        return modelAndView;
    }

    @RequestMapping(value="/lockecomadminuser.admin", produces="application/json")
    @ResponseBody
    public List<ErrorCodeDTO> lockAdminUser(HttpServletRequest request, @RequestParam(required = false) String username,
            @RequestParam(defaultValue="false", required=false) Boolean sendnotification,
            @RequestParam(required = false) String comments) {
    	List<ErrorCodeDTO> errors = new LinkedList<ErrorCodeDTO>();
    	ErrorCodeDTO error = new ErrorCodeDTO();
        this.eComAdminUserService.lockUnLockUser(username, true, request.getRemoteUser(), sendnotification.booleanValue(),
        	comments);
        error.setCode("SUCCESS");
        error.setDescription("User account has been Locked Successfully");
        errors.add(error);
        return errors;
    }

    @RequestMapping(value="/unlockecomadminuser.admin", produces="application/json")
    @ResponseBody
    public List<ErrorCodeDTO> unlockAdminUser(HttpServletRequest request, @RequestParam(required = false) String username,
            @RequestParam(defaultValue="false", required=false) Boolean sendnotification,
            @RequestParam(required = false) String comments) {
    	List<ErrorCodeDTO> errors = new LinkedList<ErrorCodeDTO>();
    	ErrorCodeDTO error = new ErrorCodeDTO();
        this.eComAdminUserService.lockUnLockUser(username, false, request.getRemoteUser(), sendnotification.booleanValue(),
        	comments);
        error.setCode("SUCCESS");
        error.setDescription("User account has been unlocked Successfully");
        errors.add(error);
        return errors;
    }

    @RequestMapping(value="/updateecomadminuseraccess.admin", produces="application/json")
    @ResponseBody
    public List<ErrorCodeDTO> updateAdminAccess(HttpServletRequest request, @RequestParam(required = false) String username)
            throws UserNameNotFoundException {
    	List<ErrorCodeDTO> errors = new LinkedList<ErrorCodeDTO>();
    	ErrorCodeDTO error = new ErrorCodeDTO();
        List<Long> accessIds = new LinkedList<Long>();
        String access[] = request.getParameterValues("access");
        if (access != null && access.length > 0) {
            for (int i = 0; i < access.length; i++) {
                accessIds.add(Long.parseLong(access[i]));
            }
        }
        EComAdminUser user = this.eComAdminUserService.loadUserByUsername(username);
        this.eComAdminUserService.assignAccess(username, accessIds, request.getRemoteUser());
        error.setCode("SUCCESS");
        error.setDescription("User Access has been updated successfully");
        errors.add(error);
        return errors;
    }

    @RequestMapping(value="/archiveAdminUser.admin")
    public String archiveAdminUser(HttpServletRequest request, @RequestParam(required = false) String adminusername,
    				@RequestParam(required = false) String comments, RedirectAttributes redirectAttributes) {
        String viewName = REDIRECT_VIEW_ADMIN_USERS;
        try
        {
            this.eComAdminUserService.archiveAdminUser(adminusername);
            redirectAttributes.addFlashAttribute("SUCCESS_ARCHIEVE", "User " + adminusername +  " Archived Successfully.");
        } catch (SDLBusinessException sdlBusinessException) {
            redirectAttributes.addFlashAttribute("ERROR_ARCHIVE_USER", sdlBusinessException.getDescription());
            viewName = REDIRECT_VIEW_ADMIN_USER_DETAIL + "?username=" + adminusername;
        }
        return viewName;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAllowedFields(new String[] {
            "userName",
            "firstName",
            "lastName",
            "maxRows"
        });
    }
}
