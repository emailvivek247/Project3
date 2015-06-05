/*
 * Created on Jan 2, 2005
 */
package net.javacoding.xsearch.utility;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLDecoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.foundation.WebserverStatic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *  Created to send/receive http request/response in java
 */
public class HttpUtil {

    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.utility.HttpUtil");

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

    public static boolean send(final String url) {
    	return send(url,false);
    }
    public static boolean send(final String url, boolean quietMode) {
        HttpPost post = new HttpPost(url);
        String ret = null;
        post.add("hello", "it's me");
        if(!quietMode) {
            logger.info("Connecting to " + url);
        }
        ret = post.send();
        if (ret != null && ret.trim().startsWith("OK")) {
            if (!quietMode) {
                logger.info("Server processing successful!");
            }
            return true;
        } else {
            if (!quietMode) {
                logger.info("Server process error:" + ret);
            }
            return false;
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
    
    public static String encode(String value) {
    	String encodedValue = null;
        try {
            encodedValue= URLEncoder.encode(value, "utf-8");
        } catch (Exception e){
            logger.error("URLEncoder error"+e);
        }
        return encodedValue;
    }
    public static String decode(String value) {
    	String encodedValue = null;
        try {
            encodedValue= URLDecoder.decode(value, "utf-8");
        } catch (Exception e){
            logger.error("URLEncoder error"+e);
        }
        return encodedValue;
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
    
    public static String open(String url) throws IOException{
        URL listUrl = new URL(url);
    	return open(listUrl);
    }
    public static String open(URL listUrl) throws IOException{
        InputStream listIn = listUrl.openStream();
        BufferedInputStream bListIn = new BufferedInputStream(listIn);
        StringWriter w = new StringWriter();
        int x;
        try {
            while ((x = bListIn.read()) != -1)
                w.write(x);
        }finally {
            if(listIn!=null)try {listIn.close();}catch(Exception e) {}
            if(bListIn!=null)try {bListIn.close();}catch(Exception e) {}
        }
        return w.toString();
    }
}

