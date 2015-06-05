package com.fdt.sdl.util;

import static com.fdt.sdl.admin.ui.util.RegistrationUtil.validCookie;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.utility.Encrypter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Utility class for user authentication.
 */
public class SecurityUtil {
	
    private static Logger logger = LoggerFactory.getLogger(SecurityUtil.class.getName());

    private static String DEFAULT_DEGEST_METHOD = "MD5";

    /**
     * encrypt service
     * 
     * @param source
     * @return encrypted string using defalut encrypter
     */
    public static String encrypt(String source) {
        return Encrypter.getDefaultEncrypter().encrypt(source);
    }

    /**
     * encrypt service
     * 
     * @param source
     * @return encrypted string using defalut encrypter
     */
    public static String encrypt(String key, String source) {
        return Encrypter.getEncrypter(key).encrypt(source);
    }

    /**
     * encrypt service
     * 
     * @param source
     * @return encrypted string using defalut encrypter
     */
    public static String decrypt(String source) {
        return Encrypter.getDefaultEncrypter().decrypt(source);

    }

    /**
     * encrypt service
     * 
     * @param source
     * @return encrypted string using defalut encrypter
     */
    public static String decrypt(String key, String source) {
        return Encrypter.getEncrypter(key).decrypt(source);

    }

    /**
     * check is user is authorized to edit
     * 
     * @param request http request
     * @return true if user is authorized
     */
    public static boolean isAdminUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Boolean admin = (Boolean) session.getAttribute("adminUser");
        if (admin != null && admin.booleanValue()) return true;
        // check cookie
        if (validCookie(request)) {
            setAdminUser(request);
            return true;
        }
        return false;
    }
    

    public static void setAdminUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("adminUser", Boolean.TRUE);
    }

    public static void logoutUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("adminUser", Boolean.FALSE);
    }

    /**
     * One way digest
     * 
     * @param source
     * @return digest value
     */
    public static String messageDigest(String source) {
        try {
            MessageDigest md = MessageDigest.getInstance(DEFAULT_DEGEST_METHOD);
            byte[] bytes = md.digest(source.getBytes());
            return bytesToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String bytesToString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            sb.append((int) (0x00FF & b));
            if (i + 1 < bytes.length) {
                sb.append("-");
            }
        }
        return sb.toString();
    }

    private static byte[] StringToBytes(String str) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StringTokenizer st = new StringTokenizer(str, "-", false);
        while (st.hasMoreTokens()) {
            int i = Integer.parseInt(st.nextToken());
            bos.write((byte) i);
        }
        return bos.toByteArray();
    }

    private static int licenseLevel = -1;
    public static boolean isAllowed(HttpServletRequest request, DatasetConfiguration dc) {
    	if(isAdminUser(request)) return true;
    	if(licenseLevel<=0){
            ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
            licenseLevel = sc.getAllowedLicenseLevel();
    	}
        if("127.0.0.1".equals(request.getRemoteAddr())) {
            if(licenseLevel<=0) {
                logger.error("License Level is " + licenseLevel);
                if(licenseLevel<0) {
                    logger.error("Invalid License");
                }
                return false;
            }
            return true;
        }
    	if(licenseLevel<=0) {
            logger.error("License Level is "+licenseLevel);
            if(licenseLevel<0) {
                logger.error("Invalid License");
            }
            return false;
        }
    	if(dc==null) return false;
    	if(dc.getAllowedIpList()==null) {
            logger.warn("remote operation attempt from "+request.getRemoteAddr());
    	    return false;
    	}
    	if(Pattern.matches(wildcardToRegex(dc.getAllowedIpList()), request.getRemoteAddr())) return true;
    	if(Pattern.matches(wildcardToRegex(dc.getAllowedIpList()), request.getRemoteHost())) return true;
        logger.warn("remote operation attempt failed from "+request.getRemoteAddr());
    	return false;
    }

    public static String wildcardToRegex(String wildcard){
        StringBuffer s = new StringBuffer(wildcard.length());
        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            char c = wildcard.charAt(i);
            switch(c) {
                case '*':
                    s.append(".*");
                    break;
                case '?':
                    s.append(".");
                    break;
                    // escape special regexp-characters
                case '(': case ')': case '[': case ']': case '$':
                case '^': case '.': case '{': case '}': case '|':
                case '\\':
                    s.append("\\");
                    s.append(c);
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        s.append('$');
        return(s.toString());
    }
}
