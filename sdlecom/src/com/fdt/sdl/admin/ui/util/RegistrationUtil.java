package com.fdt.sdl.admin.ui.util;

import static com.fdt.sdl.admin.ui.action.constants.ActionErrorKeys.REGISTRATION_LOGIN_PASSWORD_CONFIRM_EMPTY;
import static com.fdt.sdl.admin.ui.action.constants.ActionErrorKeys.REGISTRATION_LOGIN_PASSWORD_EMPTY;
import static com.fdt.sdl.admin.ui.action.constants.ActionErrorKeys.REGISTRATION_LOGIN_PASSWORD_MISMATCH;
import static com.fdt.sdl.admin.ui.action.constants.ActionErrorKeys.REGISTRATION_LOGIN_PASSWORD_TOO_SHORT;
import static com.fdt.sdl.admin.ui.action.constants.ActionErrorKeys.REGISTRATION_LOGIN_USER_NAME_EMPTY;
import static com.fdt.sdl.admin.ui.action.constants.ActionErrorKeys.REGISTRATION_LOGIN_USER_NAME_TOO_SHORT;
import static com.fdt.sdl.admin.ui.action.constants.ActionErrorKeys.REGISTRATION_LOGIN_USER_NAME_PASSWORD_NOT_EXIST;
import static com.fdt.sdl.admin.ui.action.constants.ActionErrorKeys.REGISTRATION_LOGIN_USER_NAME_PASSWORD_CANNOT_WRITE;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.utility.HttpUtil;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.fdt.sdl.styledesigner.util.PageStyleUtil;
import com.fdt.sdl.util.SecurityUtil;

public final class RegistrationUtil {
	
    private static Logger logger = LoggerFactory.getLogger(RegistrationUtil.class);

    public static boolean validatePassword(HttpServletRequest request, String password) {
        ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
        String ps = sc.getPassword();
        if (comparePassword(ps, password)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean validateUser(HttpServletRequest request, String user) {
        ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
        String currentUser = sc.getAdminUser();
        if (compareUser(currentUser, user)) {
            return true;
        } else {
            return false;
        }
    }
    
    private static boolean comparePassword(String currentPass, String inputPassword) {
        //if both null, return true
        if (currentPass == null || "".equals(currentPass.trim())) if (inputPassword == null || "".equals(inputPassword.trim()))
            return true;
        else return false;
        //if currentPasswd invalid, throw invalid password error
        if (inputPassword == null || "".equals(inputPassword.trim())) return false;
        return (currentPass.equals(inputPassword));
    }

    private static boolean compareUser(String currentUser, String inputUser) {
        //if both null, return true
        if (currentUser == null || "".equals(currentUser.trim())) if (inputUser == null || "".equals(inputUser.trim()))
            return true;
        else return false;
        //if currentPasswd invalid, throw invalid password error
        if (inputUser == null || "".equals(inputUser.trim())) return false;
        return (currentUser.equalsIgnoreCase(inputUser));
    }    
    
    public static boolean isExistingPasswordEmpty() {
        try {
            ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
            if(sc == null ) return false;
            String password = ServerConfiguration.getServerConfiguration().getPassword();
            return (password == null || "".equals(password));
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean isExistingAdminUserEmpty() {
        try {
            ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
            if(sc == null ) return false;
            String adminUser = ServerConfiguration.getServerConfiguration().getAdminUser();
            return (adminUser == null || "".equals(adminUser));
        } catch (Exception e) {
            return false;
        }
    }
    

    public static boolean validCookie(HttpServletRequest request) {
        Cookie cookie = HttpUtil.getCookie(request, REMEBER_LOGIN);
        return checkCookie(request,cookie);
    }
    
    public static boolean checkCookie(HttpServletRequest request, Cookie c) {
        if (c == null) return false;
        String value = c.getValue();        
        //logger.debug("getCooikie="+value);
        try {
			value = URLDecoder.decode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String t = SecurityUtil.decrypt(value);
        int index = t.indexOf('+');
        if (index < 0) return false; //format should be time+password
        String time = t.substring(0, index);
        String pass = t.substring(index + 1, t.length());
        // logger.info("after decryption t="+time+";pass="+pass);
        long cookieTime = U.getLong(time, 0);
        long sysTime = System.currentTimeMillis();
        // logger.info("cookieTime=" + cookieTime + ";sysTime=" +
        // sysTime+";diff="+(sysTime - cookieTime));
        if (sysTime < cookieTime || sysTime - cookieTime > 1209600000) { //1000*60*60*24*14
            logger.error("Cookie expired");
            return false; //wrong time, should be expired
        }
        return (validatePassword(request, pass)) ;
    }    
    
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String password) {
        //logger.info("setCooikie time=" + System.currentTimeMillis());
        String value = SecurityUtil.encrypt(Long.toString(System.currentTimeMillis()) + "+" + password);
        try {
			value = URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //logger.debug("setCooikie="+value);
        HttpUtil.setCookie(response, REMEBER_LOGIN, value, request.getContextPath());
    }
    
	public static boolean validateUserNamePassword(String userName, String password, String confirmPassword, ActionMessages errors) {
		boolean isValidUserNamePassword = true;
		if (U.isEmpty(userName)) {
			logger.debug("User Name is empty, Please try again");
			errors.add("error", new ActionMessage(REGISTRATION_LOGIN_USER_NAME_EMPTY));
			isValidUserNamePassword = false;
		} 
		if (U.isEmpty(password)) {
			logger.debug("Password is empty, Please try again");
			errors.add("error", new ActionMessage(REGISTRATION_LOGIN_PASSWORD_EMPTY));
			isValidUserNamePassword = false;
		} 
		if (U.isEmpty(confirmPassword)) {
			logger.debug("Confirm Password is empty, Please try again");
			errors.add("error", new ActionMessage(REGISTRATION_LOGIN_PASSWORD_CONFIRM_EMPTY));
			isValidUserNamePassword = false;
		} 
		if (!U.isEmpty(password) && !U.isEmpty(confirmPassword) && !password.equalsIgnoreCase(confirmPassword)) {
			logger.debug("The Passwords are different, please try again");
			errors.add("error", new ActionMessage(REGISTRATION_LOGIN_PASSWORD_MISMATCH));
			isValidUserNamePassword = false;
		} 
		if (!U.isEmpty(password) && password.length() < 6) {
			logger.debug("The passwords are too short");
			errors.add("error", new ActionMessage(REGISTRATION_LOGIN_PASSWORD_TOO_SHORT));
			isValidUserNamePassword = false;
		}
		if (!U.isEmpty(userName) && userName.length() < 6) {
			logger.debug("The UserName is too short");
			errors.add("error", new ActionMessage(REGISTRATION_LOGIN_USER_NAME_TOO_SHORT));
			isValidUserNamePassword = false;
		} 
		return isValidUserNamePassword;
	}
	
    public static String changeAdminUserPassword(HttpServletRequest request, String adminUserName, String password) {
        try {
            ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
            try {
                if (!sc.exists()) sc.syncDatasets();
            }catch(NullPointerException npe) {
                return "action.server.config.file.not.exist";
            }
            if (!sc.exists()) return REGISTRATION_LOGIN_USER_NAME_PASSWORD_NOT_EXIST;
            if (!sc.canWrite()) return REGISTRATION_LOGIN_USER_NAME_PASSWORD_CANNOT_WRITE;
            sc.setAdminUser(PageStyleUtil.encrypt(adminUserName));
            sc.setPassword(PageStyleUtil.encrypt(password));
            sc.save();
        } catch (IOException ie) {
            logger.error("IOException Excepiton", ie);
            return ("action.global.IOException");
        }
        HttpSession session = request.getSession();
        session.setAttribute("adminUser", Boolean.TRUE);
        return null;
    }    
    
    private static String REMEBER_LOGIN = "SDLCOOKIELOGIN";   
}
