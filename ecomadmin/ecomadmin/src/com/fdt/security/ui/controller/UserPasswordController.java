package com.fdt.security.ui.controller;

import static com.fdt.ecomadmin.ui.controller.ViewConstants.FORWARD_SUCCESS;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.FORWARD_SUCCESSFUL_UPDATION;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.RESET_PASSWORD;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_CHANGE_PASSWORD;
import static com.fdt.ecomadmin.ui.controller.ViewConstants.VIEW_RESET_PASSWORD_REQUEST;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.mail.MailException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.ui.controller.AbstractBaseController;
import com.fdt.common.util.SystemUtil;
import com.fdt.security.entity.EComAdminUser;
import com.fdt.security.exception.BadPasswordException;
import com.fdt.security.exception.InvalidDataException;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.security.exception.UserNotActiveException;
import com.fdt.security.ui.form.ChangePasswordForm;
import com.fdt.security.ui.form.ResetPasswordForm;
import com.fdt.security.ui.form.UserForm;

@Controller
public class UserPasswordController extends AbstractBaseController {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAllowedFields(new String[] {
                "username",
                "token",
                "password",
                "existingPassword",
                "confirmPassword",
                "resetPasswordRequest",
                "resetPassword",
                "changepassword"
        });
    }

    @RequestMapping(value="/publicviewresetpasswordrequest.admin", method=RequestMethod.GET)
    public String viewResetPasswordRequest(Model model){
        model.addAttribute("ResetPasswordRequestForm", new UserForm());
        return VIEW_RESET_PASSWORD_REQUEST;
    }

    @RequestMapping(value="/publicresetpasswordrequest.admin", method=RequestMethod.POST)
    public ModelAndView resetPasswordRequest(HttpServletRequest request,
                                            @ModelAttribute("ResetPasswordRequestForm") @Valid UserForm userForm,
                                            BindingResult bindingResult) {
        ModelAndView modelAndView = this.getModelAndView(request, FORWARD_SUCCESS);
        verifyBinding(bindingResult);
        if (bindingResult.hasErrors()) {
            String view = VIEW_RESET_PASSWORD_REQUEST;
            this.setModelAndViewForError(modelAndView, view);
            return modelAndView;
        }
        String successMsg = this.getMessage("security.authentication.resetPasswordRequestSuccess");
        try {
            this.eComAdminUserService.resetPasswordRequest(userForm.getUsername());
        } catch (UserNameNotFoundException | UserNotActiveException userNameNotFoundException) {
            successMsg = userNameNotFoundException.getDescription();
            bindingResult.rejectValue("username", successMsg, successMsg);
        } catch (MailException mailException) {
            logger.error("Exception Occured while callin recover Password", mailException);
        }
        if (bindingResult.hasErrors()) {
            String view = VIEW_RESET_PASSWORD_REQUEST;
            this.setModelAndViewForError(modelAndView, view);
            return modelAndView;
        }
        modelAndView.addObject(SUCCESS_MSG, successMsg);
        return modelAndView;
    }

    @RequestMapping(value="/publicresetpassword.admin", method=RequestMethod.GET)
    public ModelAndView viewResetPassword(HttpServletRequest request,
                                         @RequestParam("token") String token,
                                         @RequestParam("userName") String username) {
        ModelAndView modelAndView = this.getModelAndView(request, RESET_PASSWORD);
        try {
        	username = SystemUtil.decrypt(username);
        	token = SystemUtil.decrypt(token);
            this.eComAdminUserService.checkValidResetPasswordRequest(username, token);
        } catch (UserNameNotFoundException userNameNotFoundException) {
            modelAndView.addObject(FAILURE_MSG, userNameNotFoundException.getDescription());
            return modelAndView;
        } catch (InvalidDataException invalidDataException) {
            modelAndView.addObject(FAILURE_MSG, invalidDataException.getDescription());
            return modelAndView;
        }
        ResetPasswordForm resetPasswordForm = new ResetPasswordForm();
        resetPasswordForm.setToken(token);
        resetPasswordForm.setUsername(username);
        modelAndView.addObject("ResetPasswordForm", resetPasswordForm);
        return modelAndView;
    }

    @RequestMapping(value="/publicsubmitresetpassword.admin", method=RequestMethod.POST)
    public ModelAndView resetPassword(@ModelAttribute("ResetPasswordForm") @Valid ResetPasswordForm resetPasswordForm,
            BindingResult bindingResult, HttpServletRequest request) {
        ModelAndView modelAndView = this.getModelAndView(request, FORWARD_SUCCESS);
        String successMsg = this.getMessage("security.authentication.resetPasswordSuccess");
        try {
            EComAdminUser user = null;
            verifyBinding(bindingResult);
            convertPasswordError(bindingResult);
            if (bindingResult.hasErrors()) {
                String view = RESET_PASSWORD;
                this.setModelAndViewForError(modelAndView, view);
                return modelAndView;
            }
            user = new EComAdminUser();
            user.setUsername(resetPasswordForm.getUsername());
            user.setPassword(resetPasswordForm.getPassword());
            this.eComAdminUserService.resetPassword(user, resetPasswordForm.getToken());
            modelAndView.addObject(SUCCESS_MSG, successMsg);
        } catch (UserNameNotFoundException userNameNotFoundException) {
            successMsg = userNameNotFoundException.getDescription();
        }
        modelAndView.addObject(SUCCESS_MSG, successMsg);
        return modelAndView;
    }

    @RequestMapping(value="/viewchangepassword.admin", method=RequestMethod.GET)
    public ModelAndView viewChangePassword(ModelAndView modelAndView, HttpServletRequest request) {
        modelAndView = new ModelAndView(VIEW_CHANGE_PASSWORD);
        ChangePasswordForm changePasswordForm = new ChangePasswordForm();
        UsernamePasswordAuthenticationToken userPasswordAuthToken
                    = (UsernamePasswordAuthenticationToken) request.getUserPrincipal();
        EComAdminUser user = (EComAdminUser)userPasswordAuthToken.getPrincipal();
        modelAndView.addObject("user", user);
        modelAndView.addObject("ChangePasswordForm", changePasswordForm);
        return modelAndView;
    }

    @RequestMapping(value="/changepasswordsubmit.admin", method=RequestMethod.POST)
    public ModelAndView changePassword(HttpServletRequest request,
                                       @ModelAttribute("ChangePasswordForm") @Valid ChangePasswordForm changePasswordForm,
                                       BindingResult bindingResult) {
        EComAdminUser user = null;
        ModelAndView modelAndView = this.getModelAndView(request, FORWARD_SUCCESSFUL_UPDATION);
        try {
            verifyBinding(bindingResult);
            convertPasswordError(bindingResult);
            if (bindingResult.hasErrors()) {
                String view = VIEW_CHANGE_PASSWORD;
                UsernamePasswordAuthenticationToken userPasswordAuthToken
                            = (UsernamePasswordAuthenticationToken) request.getUserPrincipal();
                user = (EComAdminUser)userPasswordAuthToken.getPrincipal();
                modelAndView.addObject("user", user);
                this.setModelAndViewForError(modelAndView, view);
                return modelAndView;
            }
            user = new EComAdminUser();
            user.setUsername(request.getRemoteUser());
            user.setPassword(changePasswordForm.getPassword());
            user.setExistingPassword(changePasswordForm.getExistingPassword());
            this.eComAdminUserService.changePassword(user);
        } catch (UserNameNotFoundException userNameNotFoundException) {
            bindingResult.rejectValue("username", "security.authentication.usernotfound");
        } catch (BadPasswordException badpasswordException) {
            bindingResult.rejectValue("existingPassword", "security.authentication.badcrentials");
        }
        UsernamePasswordAuthenticationToken userPasswordAuthToken = (UsernamePasswordAuthenticationToken)request
                .getUserPrincipal();
        user = (EComAdminUser)userPasswordAuthToken.getPrincipal();
        modelAndView.addObject("user", user);
        if (bindingResult.hasErrors()) {
            String view = VIEW_CHANGE_PASSWORD;
            this.setModelAndViewForError(modelAndView, view);
            return modelAndView;
        }
        String successMsg = this.getMessage("security.authentication.passwordChangeSuccess");
        modelAndView.addObject(SUCCESS_MSG, successMsg);
        return modelAndView;
    }

    private static void convertPasswordError(BindingResult bindingResult) {
        for (ObjectError error : bindingResult.getGlobalErrors()) {
            String msg = error.getDefaultMessage();
            if ("security.notmatch.password".equals(msg)) {
                if (!bindingResult.hasFieldErrors("password")) {
                    bindingResult.rejectValue("password", "security.notmatch.password");
                }
            }
        }
    }

    private void setModelAndViewForError(ModelAndView modelAndView, String view) {
        modelAndView.setViewName(view);
    }
}
