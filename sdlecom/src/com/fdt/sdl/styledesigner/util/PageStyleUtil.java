package com.fdt.sdl.styledesigner.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.utility.FileUtil;
import net.javacoding.xsearch.utility.U;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.fdt.common.util.SystemUtil;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.ShoppingCartItem;
import com.fdt.sdl.admin.ui.controller.printq.PrintQController;
import com.fdt.security.entity.User;

import freemarker.template.SimpleHash;

/**
 *  Created to send/receive http request/response in java
 */
public class PageStyleUtil {

    private static Logger logger = LoggerFactory.getLogger(PageStyleUtil.class.getName());

    /**
     * Convenience method to set a cookie
     *
     * @param response
     * @param name
     * @param value
     * @param path
     * @return HttpServletResponse
     */
    public static void setCookie(HttpServletResponse response, String name, String value, String path) {
    	logger.debug("Setting cookie '" + name + "' on path '" + path + "'");

    	Cookie cookie = new Cookie(name, value);
        cookie.setSecure(false);
        cookie.setPath(path);
        cookie.setMaxAge(3600 * 24 * 30); // 30 days
        response.addCookie(cookie);
    }

    /**
     * Convenience method to get a cookie by name
     *
     * @param request
     * @param name
     *
     * @return
     */
    public static Cookie getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        Cookie returnCookie = null;

        if (cookies == null) { return returnCookie; }
        for (int i = 0; i < cookies.length; i++) {
            Cookie thisCookie = cookies[i];

            if (thisCookie.getName().equals(name)) {
                // cookies with no value do me no good!
                if (!thisCookie.getValue().equals("")) {
                    returnCookie = thisCookie;

                    break;
                }
            }
        }

        return returnCookie;
    }

    /**
     * Convenience method for deleting a cookie by name
     *
     * @param response
     *            the current web response
     * @param cookie
     *            the cookie to delete
     *
     * @return the modified response
     */
    public static void deleteCookie(HttpServletResponse response, Cookie cookie, String path) {
    	logger.debug("deleteCookie'" + cookie + "' on path '" + path + "'");

        if (cookie != null) {
            // Delete the cookie by setting its maximum age to zero
            cookie.setMaxAge(0);
            cookie.setPath(path);
            response.addCookie(cookie);
        }
    }

   /**
    * modify a query, append new value to the existing value
    * @param query original query
    * @param name parameter name
    * @param value new value that need to be appended
    * @return modified query.
    * for example appendQuery("a=123&b=345","a","789") should return "a=123+789&b=345".
    * if the name is not in the original query, the new name-value pairt will be append to the query
    */
    public static String appendQuery(String query, String name, String value){
        return modifyQuery( query,  name,  value, true, WebserverStatic.getEncoding());
    }
    public static String appendQuery(String query, String name, String value, String encoding){
        return modifyQuery( query,  name,  value, true, encoding);
    }
    
    public static String getBasketNameFromSession(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String basketName = (String) session.getAttribute("PRINTQ_BASKET_NAME");
		return basketName;
	}

    /**
     * modify a query, replace existing value to new value
     * @param query original query
     * @param name parameter name
     * @param value new vlaue
     * @return modified query
     * for example addOrSetQuery("a=123&b=345","a","789") should return "a=789&b=345"
     * if the name is not in the original query, the new name-value pairt will be append to the query
     * if the new value is null, the current name-value pair will be removed
     */
    public static String addOrSetQuery(String query, String name, Object value){
        return modifyQuery( query,  name,  (value==null?null:value.toString()), false, WebserverStatic.getEncoding());
    }
    /**
     * Same as addOrSetQuery function
    */
    public static String setQuery(String query, String name, Object value){
        return modifyQuery( query,  name,  (value==null?null:value.toString()), false, WebserverStatic.getEncoding());
    }
    public static String setQuery(String query, String name, Object value, String encoding){
        return modifyQuery( query,  name,  (value==null?null:value.toString()), false, encoding);
    }
    public static String addOrSetQuery(String query, String name, Object value, String encoding){
        return modifyQuery( query,  name,  (value==null?null:value.toString()), false, encoding);
    }

    public static String encodeURL(String aURLFragment){
        if(aURLFragment==null) return "";
        String result = null;
        try {
          result = URLEncoder.encode(aURLFragment, "UTF-8");
        }
        catch (UnsupportedEncodingException ex){
          throw new RuntimeException("UTF-8 not supported", ex);
        }
        return result;
      }

      /**
       * Synonym for <tt>URLDecoder.decode(String, "UTF-8")</tt>.
       */
      public static String decodeURL(String s) {
          if(s==null) return "";
          String result = null;
          try {
              result = URLDecoder.decode(s, "UTF-8");
          } catch (UnsupportedEncodingException ex) {
              throw new RuntimeException("UTF-8 not supported", ex);
          }
          return result;
      }

    private static String modifyQuery(String query, String name, String value, boolean append, String encoding){

        String origQuery = query;
        if (origQuery == null ) origQuery = "";

        origQuery = updateQueryString(origQuery);
        String encodedValue = null;
        if (value != null)
            try {
                encodedValue= java.net.URLEncoder.encode(value, U.getText(encoding, "utf-8"));
            } catch (Exception e){
                logger.error("URLEncoder error"+e);
            }

        boolean found = false;
        boolean isFirst = true;
        StringBuffer sb = new StringBuffer();
        String [] parameters=origQuery.split("&");
        for (int i=0;i<parameters.length;i++){
           if (parameters[i].startsWith(name+"=")){
               found = true;
               if (append){
	               String oldValue =parameters[i].substring(name.length()+1);
	               if (!oldValue.trim().equals("")) oldValue += "+";
	               if (!U.isEmpty(value))
	               	parameters[i]=name+"="+oldValue+encodedValue;
               }else {
                   if (!U.isEmpty(value))
                       parameters[i]=name+"="+encodedValue;
                   else
                       parameters[i]=null;
               }
           }
           if (parameters[i] != null && !parameters[i].equals("")){
               if (!isFirst) sb.append('&');
               sb.append(parameters[i]);
               isFirst = false;
           }
        }
        if (!found && !U.isEmpty(value))
        {
           if (!isFirst) sb.append('&');
           sb.append(name+"="+encodedValue);
        }
        return sb.toString();
    }

    public static List<String> getYears(int frmYear) {
    	Calendar rightNow = Calendar.getInstance();
    	int currentYear = rightNow.get(Calendar.YEAR);
    	List<String> years = new ArrayList<String>();
    	for (int start = currentYear ; start >= frmYear; start--) {
    		years.add(String.valueOf(start));
    	}
    	return years;
    }

    private static String updateQueryString(String query) {
        String origQuery = query;
        if (origQuery == null ) origQuery = "";
        StringBuffer sb = new StringBuffer();
        String [] parameters = origQuery.split("&");
        for (int i = 0; i < parameters.length; i++){
           if (parameters[i].trim().startsWith("q=") ||
        	   parameters[i].trim().startsWith("lq=") ||
        	   parameters[i].trim().startsWith("searchQuery=") ||
        	   parameters[i].trim().startsWith("searchType=") ||
        	   parameters[i].trim().startsWith("start=") ||
        	   parameters[i].trim().startsWith("searchable=") ||
        	   parameters[i].trim().startsWith("desc=") ||
        	   parameters[i].trim().startsWith("length=") ||
        	   parameters[i].trim().startsWith("sortBy=") ||
        	   parameters[i].trim().startsWith("templateName=") ||
        	   parameters[i].trim().startsWith("indexName=")){
	    		   sb.append('&');
	    		   sb.append(parameters[i].trim());
           }
        }
        return sb.toString();
    }

    public static String format(String inputDateStr, String inputFormat, String outputFormat) {
		String outputDateStr = null;
		if (inputDateStr == null || inputDateStr.isEmpty()) {
			return inputDateStr;
		}
		try {
			DateFormat inputDateFormat = new SimpleDateFormat(inputFormat);
			Date inputDate = inputDateFormat.parse(inputDateStr);
			DateFormat outputDateFormat = new SimpleDateFormat(outputFormat);
			outputDateStr =  outputDateFormat.format(inputDate);
		} catch (ParseException parseException) {
				parseException.printStackTrace();
		}
		return outputDateStr;
	}

    public static String encodeChartText(String aTagFragment){
        if(aTagFragment==null) return "";
        final StringBuffer result = new StringBuffer();

        final StringCharacterIterator iterator = new StringCharacterIterator(aTagFragment);
        char character =  iterator.current();
        while (character != StringCharacterIterator.DONE ){
          if (character == '<') {
            result.append("&lt;");
          }
          else if (character == '>') {
            result.append("&gt;");
          }
          else if (character == '\"') {
            result.append("&quot;");
          }
          else if (character == '\'') {
            result.append("&#039;");
          }
          else if (character == '\\') {
             result.append("&#092;");
          }
          else if (character == '&') {
             result.append("&amp;");
          }
          else {
            //the char is not a special one
            //add it to the result as is
            result.append(character);
          }
          character = iterator.next();
        }
        return result.toString();
      }

    public static User getUser(HttpServletRequest request) {
    	UsernamePasswordAuthenticationToken userPasswordAuthToken
		= (UsernamePasswordAuthenticationToken) request.getUserPrincipal();
    	User user = (User)userPasswordAuthToken.getPrincipal();
    	return user;
    }

    public static String encrypt(String plainText) {
    	return SystemUtil.encrypt(plainText);
    }

    public static String decrypt(String encryptedText) {
    	return SystemUtil.decrypt(encryptedText);
    }

    public static String decrypt(String encryptedText, String algorithm, String passwordKey) {
    	return SystemUtil.decrypt(encryptedText, algorithm, passwordKey);
    }
    
    public static String decrypt(String encryptedText, String algorithm) {
    	return SystemUtil.decrypt(encryptedText, algorithm);
    }

    public static String encrypt(String plainText, String algorithm, String passwordKey) {
    	return SystemUtil.encrypt(plainText, algorithm, passwordKey);
    }

    public static String encrypt(String plainText, String algorithm) {
    	return SystemUtil.encrypt(plainText, algorithm);
    }
    
    public static String getSystemValue(String key) {
		return getSystemValue(null, key);
	}

    public static String getSystemValue(String bundle, String key) {
		String returnedValue = null;
		ResourceBundle resourceBundle = null;
		try {
			if (bundle == null) {
				resourceBundle = ResourceBundle.getBundle("system");
			} else {
				resourceBundle = ResourceBundle.getBundle(bundle);
			}
			returnedValue = (String)resourceBundle.getString(key);
		} catch (Exception exp) {
			logger.error("The Resource bundle/Key is not Found " + key);
		}
		return returnedValue;
	}

	public static String getBuildVersion() {
		String majorNumber = getSystemValue(null, "build.major.number");
		String minorNumber = getSystemValue(null, "build.minor.number");
		String revisionNumber = getSystemValue(null, "build.revision.number");
		return majorNumber + "." + minorNumber +  "." + revisionNumber;
	}

	public static String getBuildDate() {
		String buildDate = getSystemValue(null, "build.date");
		return buildDate;
	}

	public static String[] getSystemValues(String bundle, String key) {
		String[] returnedValues = null;
		ResourceBundle resourceBundle = null;
		try {
			if (bundle == null) {
				resourceBundle = ResourceBundle.getBundle(bundle);
			} else {
				resourceBundle = ResourceBundle.getBundle("system");
			}
			resourceBundle = ResourceBundle.getBundle("system");
			returnedValues = resourceBundle.getString(key).split(",");
		} catch (Exception exp) {}
		return returnedValues;
	}

	public int getshoppingCartSize(HttpServletRequest request) {
		int shoppingCartSize = 0;
		Map<String, ShoppingCartItem> shoppingBasket
			= (HashMap<String, ShoppingCartItem>)request.getSession().getAttribute("SHOPPING_CART" + request.getRemoteUser());
		if (shoppingBasket != null) {
			shoppingCartSize = shoppingBasket.size();
		}
		return shoppingCartSize;
	}

    public static Boolean isCardExpired(CreditCard creditCard) {
        Integer expiryYear = creditCard.getExpiryYear();
        Integer expiryMonth = creditCard.getExpiryMonth();
        LocalDate expiryDate = LocalDate.of(expiryYear, expiryMonth, 1).with(TemporalAdjusters.lastDayOfMonth());
        return expiryDate.compareTo(LocalDate.now()) < 0;
    }

    public static Boolean areAllCardsExpired(List<CreditCard> creditCardList) {
        return creditCardList.stream().allMatch(c -> PageStyleUtil.isCardExpired(c));
    }

    public static String getExpirationDate(CreditCard creditCard) {
        Integer expiryYear = creditCard.getExpiryYear();
        Integer expiryMonth = creditCard.getExpiryMonth();
        LocalDate expiryDate = LocalDate.of(expiryYear, expiryMonth, 1);
        java.time.format.DateTimeFormatter formatter = 
                java.time.format.DateTimeFormatter.ofPattern("MM/yyyy");
        return expiryDate.format(formatter);
    }

	public int getPrinQSize(HttpServletRequest request) {
		return PrintQController.getPrinQSize(request);
	}

	 public static void main(String[] args) {
			
			/*private static String PASSWORD_KEY = "Nk0BusFu9aLE4qb";		
			private static String PASSWORD_KEY = "dookudu";*/
	    	String text1 = "Gr@nicus723$";
	    	String text2 = "A893A81F03B327BF21EC41D4EADB8509E715F72B445B152A329676B13071250A";
			String algorithm = "PBEWITHSHA256AND256BITAES-CBC-BC";
			String devPasswordKey = "dookudu";
			String prodPasswordKey1 = "Nk0BusFu9aLE4qb";
			String prodPasswordKey2 = "zEFyZpXc0NYAgYU";
			
			//System.out.println(PageStyleUtil.encrypt(text1,algorithm,prodPasswordKey2));
			System.out.println(PageStyleUtil.decrypt(text2,algorithm,prodPasswordKey2));
	}

    /*public static void main(String[] args) {
		String enc = encrypt("roam");
		System.out.println("Encrypted String " + enc);
		System.out.println("Decrypted Text   " + decrypt(enc));
    } */

	public static Object load(String clazz) {
		try {
			return Class.forName(clazz).newInstance();
		} catch (Exception e) {
			return null;
		}
	}


	public static String convertMilliSecondsIntoHHmmSSsss(long timeInMilliseconds) {
		String executionTime = null;
		int hours = 0, minutes = 0, seconds = 0, milliseconds = 0;
		hours = (int) (timeInMilliseconds / (1000*60*60));
		minutes = (int) ((timeInMilliseconds % (1000*60*60)) / (1000*60));
		seconds = (int) (((timeInMilliseconds % (1000*60*60)) % (1000*60)) / 1000);
		milliseconds = (int) (((timeInMilliseconds % (1000*60*60)) % (1000*60)) % 1000);
		executionTime = StringUtils.leftPad(String.valueOf(hours), 2, "0").concat(":") +
				StringUtils.leftPad(String.valueOf(minutes), 2, "0").concat(":") +
				StringUtils.leftPad(String.valueOf(seconds), 2, "0").concat(":") +
				StringUtils.leftPad(String.valueOf(milliseconds), 3, "0");
		return executionTime;

	}
	
	public static String getLastUpdateTimeMessages() throws IOException {
		File msgCenterPropertiesFile = FileUtil.resolveFile(WebserverStatic.getRootDirectoryFile(), "WEB-INF", "data", "properties", "MessageCenter.properties");
		String fileName = msgCenterPropertiesFile.getAbsolutePath();		
		String lastUpdatedDateTime = null;
		lastUpdatedDateTime = PageStyleUtil.readProperty("lastUpdatedDateTime", fileName);
		return lastUpdatedDateTime;
	}

    public static boolean isValidRequest(HttpServletRequest request, String[] lisOfValidIPs) {
    	boolean isMatched = false;
    	try {
	    	String inputIp = request.getRemoteAddr();
	        if (request.getHeader("HTTP_X_FORWARDED_FOR") != null) {
	        	inputIp = request.getHeader("HTTP_X_FORWARDED_FOR");
	        }else if (request.getHeader("X-FORWARDED-FOR") != null) {
	        	inputIp = request.getHeader("X-FORWARDED-FOR");
	        }
	       	logger.debug("The Input IPAddress is==> {}", inputIp);
		    if (!isValidIPAddress(inputIp)) {
	        	logger.debug("The Input IPAddress Not Matched!");
			    return false;
			}
		    if (lisOfValidIPs == null) {
		    	return false;
		    }
			for (String ip : lisOfValidIPs) {
				if (isMatch(inputIp, ip)) {
					logger.debug("The Input IPAddress Matched {}", inputIp);
					isMatched = true;
					break;
				}
	  	    }
	       	logger.debug("The Input IPAddress Matched!");
    	} catch (Exception exception) {
    		logger.error("There is an Error in isValidRequest", exception);
    	}
		return isMatched;
	}

    public static boolean isValidRequest(HttpServletRequest request, String resouceBundleName, String key) {
    	boolean isMatched = false;
    	try {
	    	String inputIp = request.getRemoteAddr();
	        if (request.getHeader("HTTP_X_FORWARDED_FOR") != null) {
	        	inputIp = request.getHeader("HTTP_X_FORWARDED_FOR");
	        }else if (request.getHeader("X-FORWARDED-FOR") != null) {
	        	inputIp = request.getHeader("X-FORWARDED-FOR");
	        }
	        if (logger.isDebugEnabled()) {
	        	logger.debug("The Input IPAddress is==>{}", inputIp);
	        }
		    if (!isValidIPAddress(inputIp)) {
		        if (logger.isDebugEnabled()) {
		        	logger.debug("The Input IPAddress Not Matched!");
		        }
			    return false;
			}
		    String[] lisOfValidIPs = getSystemValues(resouceBundleName, key);
		    if (lisOfValidIPs == null) {
	        	logger.warn("Valid Ip's Are not Specified Pls Specify it!");
		    	return false;
		    }
			for (String ip : lisOfValidIPs) {
				if (isMatch(inputIp, ip)) {
					isMatched = true;
					logger.debug("The Input IPAddress Matched!");
					break;
				}
	  	    }
			logger.debug("The Input IPAddress Did Not Match!");
    	} catch (Exception exception) {
    		logger.error("There is an Error in isValidRequest", exception);
    	}
		return isMatched;
	}

	public static boolean isValidIPAddress(String ip) {
		StringTokenizer stringTokenizer = new StringTokenizer(ip, ".");
		String[] parts = { new String(), new String(), new String(), new String()};
		int i = 0;
		while (stringTokenizer.hasMoreTokens()) {
			if (i >= 4) {
				return false;
			}
			parts[i] = (String) stringTokenizer.nextElement();
			if ((Integer.parseInt(parts[i]) < 0) || (Integer.parseInt(parts[i]) > 255)) {
				return false;
			}
			i = i + 1;
		}
		return true;
	}

	public static boolean isMatch(String string, String regexp) {
		Pattern pattern = Pattern.compile(regexp);
		Matcher matcher = pattern.matcher(string);
		return matcher.matches();
	}

	public static Map<String, String> getModelMap(String jobDataString) {
		Map<String, String> modelMap = new LinkedHashMap<String, String>();
		String actionParameter = null, orderParameter = null, line = null;
		int count = 0;
		StringTokenizer st1 = new StringTokenizer(jobDataString, "|");
		while (st1.hasMoreTokens()) {
		   count = count + 1;
		   line = st1.nextToken();
		   StringTokenizer st2 = new StringTokenizer(line, ",");
		   while (st2.hasMoreTokens()) {
			   String indexName = st2.nextToken();
			   actionParameter = "action_".concat(indexName);
			   orderParameter = "actionorder_".concat(indexName);
			   modelMap.put(actionParameter, st2.nextToken());
			   modelMap.put(orderParameter, String.valueOf(count));
		   }
		}
		return modelMap;
	}

	public static String formatCurrency(String value) {
		try {
			if (value == null) {
	            return null;
	        }
	        Locale locale = new Locale("en", "US");
	        Double doubleValue = Double.valueOf(value);
	        NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
	        value = nf.format(doubleValue);
	        return value;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 *  This Method does two things (i) Checks whether role passed is in SECURITY_ROLES request Parameter.
	 *  (ii) If SECURITY_ROLES request Parameter is null/empty then it simply checks whether Logged In User has role.
	 */
	public static boolean isUserInRole(String roleName, HttpServletRequest request) {
		String securityRoles = (String) request.getParameter("SECURITY_ROLES");
		if (StringUtils.isBlank(securityRoles)) {
			return request.isUserInRole(roleName);
		}
        String[] rolesArray = tokenizeToStringArray(securityRoles, ",", true, true);
        for(String role: rolesArray) {
        	if(role.equalsIgnoreCase(roleName)) {
        		return true;
        	}
        }
        return false;
	}

	/**
	 *  This Method does two things (i) Checks whether any role associated with roleGroup passed is in SECURITY_ROLES
	 *  request Parameter.
	 *  (ii) If SECURITY_ROLES request Parameter is null/empty then it simply checks whether Logged In User has any
	 *   of the role specified in roleGroup.
	 */
	public static boolean isUserInRoleGroup(String roleGroup, HttpServletRequest request) {
		logger.debug("roleGroup: " + roleGroup);
		String roleGroupValue = getSystemValue(roleGroup);
		logger.debug("roleGroupValue: " + roleGroupValue);
		if (StringUtils.isBlank(roleGroupValue)) {
			return false;
		}
		String[] roleNames = tokenizeToStringArray(roleGroupValue, ",", true, true);
		String securityRoles = (String) request.getParameter("SECURITY_ROLES");
		if (StringUtils.isBlank(securityRoles)) {
			for (String roleName : roleNames) {
				if(request.isUserInRole(roleName)) {
					logger.debug("ROLE MATCHED " + roleName + " request.isUserInRole-->" + request.isUserInRole(roleName));
					return true;
				}
			}
		} else {
	        String[] rolesArray = tokenizeToStringArray(securityRoles, ",", true, true);
	        for(String role: rolesArray) {
				for (String roleName : roleNames) {
		        	if(role.equalsIgnoreCase(roleName)) {
		        		return true;
		        	}
				}

	        }
		}
		logger.debug("ROLE NOT MATCHED");
        return false;
	}
	
	/* This Method is used to write a property in Properties File. */
		public static void writeProperty(String key, String value, String fileName) throws IOException {
			Properties properties = new Properties();
			OutputStream out = null;
			InputStream in = null;
			in = new FileInputStream(fileName);
			properties.load(in);
			properties.setProperty(key, value);
			out = new FileOutputStream(fileName);
		    properties.setProperty(key, value);
		    properties.store(out, null);
			out.close();
		}
		
		/* This Method is used to read a property from the Properties File. */
		public static String readProperty(String key, String fileName) throws IOException {
			String value = null;
			Properties properties = new Properties();
			InputStream in = null;
			in = new FileInputStream(fileName);
			properties.load(in);
			value = (String) properties.get(key);
			in.close();
			return value;
		}
		
		/* This Method is Used To remove a property from the Properties File. */
		public static void removeProperty(String key, String fileName) throws IOException {
			Properties properties = new Properties();
			OutputStream out = null;
			InputStream in = null;
			in = new FileInputStream(fileName);
			properties.load(in);
			in.close();
			properties.remove(key);
			out = new FileOutputStream(fileName);
		    properties.store(out, null);
			out.close();
		}


	public static String[] tokenizeToStringArray(String str, String delimiters,	boolean trimTokens, boolean ignoreEmptyTokens) {
		if (str == null) {
			return null;
		}
		StringTokenizer st = new StringTokenizer(str, delimiters);
		List<String> tokens = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (trimTokens) {
				token = token.trim();
			}
			if (!ignoreEmptyTokens || token.length() > 0) {
				tokens.add(token);
			}
		}
		return tokens.toArray(new String[tokens.size()]);
	}

	public static String convertCamelCase(String s) {
    	return WordUtils.capitalizeFully(s);
    }

    public static String getDateInTimezone(Date date, String timeZone) {
    	DateTime dateTime = new DateTime(date);
    	DateTime dateTimeInTimezone = dateTime.withZoneRetainFields(DateTimeZone.forID(timeZone));
    	DateTimeFormatter format = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm a zzz");
    	String dateAsString = format.print(dateTimeInTimezone);
    	return dateAsString;
    }

    public static ByteArrayOutputStream  getByteArrayOutputStream(String pdfFile) throws IOException {

		 FileInputStream input = new FileInputStream (pdfFile);
		 ByteArrayOutputStream output = new ByteArrayOutputStream ();
		 byte [] buffer = new byte [65536];
		 int l;
		 while ((l = input.read (buffer)) > 0)
		     output.write (buffer, 0, l);
		 input.close ();
		 return output;
	 }
    
    public static ByteArrayOutputStream  getByteArrayOutputStreamFromByteArray(byte [] byteArray) throws IOException { 
		 ByteArrayOutputStream output = new ByteArrayOutputStream (byteArray.length);
		 output.write(byteArray, 0, byteArray.length);
		 return output;
	 }

    
    public static SimpleHash getHierarchialTreeForFolder(String filePath) {
    	logger.debug("filePath: " + filePath);
    	SimpleHash modelMapSimpleHash = new SimpleHash();
    	File folder = new File(filePath);
        Map<String, List<String>>  tree = new HashMap<String, List<String>>();
        String fileName = null;

        int i = -1;
        String key = null;
        if(folder!=null && folder.listFiles().length > 0) {
	        for (File fileEntry : folder.listFiles()) {
	          fileName = fileEntry.getName();
	          i = StringUtils.indexOf(fileName, "_");
	          if (i != -1) {
	            key = StringUtils.substring(fileName, 0, i);
	            tree = put(tree, key, fileEntry.getAbsolutePath());
	          }
	        }
	        logger.debug("tree: " + tree);
	        modelMapSimpleHash.putAll(tree);
        } else {
        	 logger.debug("Image Location Is empty: " + tree);
        }
        return modelMapSimpleHash;
      }

    private static Map<String, List<String>> put(Map<String, List<String>> tree, String key, String fileName) {
        if (StringUtils.isBlank(key)) {
          return tree;
        }
        List<String> fileList = (List<String>)tree.get(key);
        if (fileList != null) {
          fileList.add(fileName);
        } else {
          fileList = new LinkedList<String>();
          fileList.add(fileName);
        }
        tree.put(key, fileList);
        return tree;
      }
}