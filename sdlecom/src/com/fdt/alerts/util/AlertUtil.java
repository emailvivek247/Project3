package com.fdt.alerts.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import net.javacoding.xsearch.api.Document;
import net.javacoding.xsearch.api.SearchConnection;
import net.javacoding.xsearch.api.SearchQuery;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.alerts.service.AlertFacadeService;
import com.fdt.alerts.service.UserAlert;
import com.fdt.common.util.cxf.ClientPasswordCallback;

public class AlertUtil {
	
	static Logger logger = LoggerFactory.getLogger(AlertUtil.class);
	
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
	
	/*  This Method is used to return a String which when executed on command line starts java process. First argument
	 *  takes the class name and this should have main method defined in it. */
	public static String getJavaCommand(String alertMainClassName, int heapSize) {
		String cmd = ""
	            +" -Xmx" + heapSize + "m"
	            +" " + alertMainClassName + " ";
		return cmd;
	}
	
	/* This Method is used to return Today's Date in YYYYMMDD format. */
	public static String getCurrentDateYYMMDD() {
		String currentDate = null;
		DateTimeFormatter ymdFormatter = DateTimeFormat.forPattern("yyyyMMdd");
		DateTime todaysDate = new DateTime();
		currentDate = ymdFormatter.print(todaysDate);
		return currentDate;
		
	}
	
	/* This Method is used to return current hour according to 24 hour format. */	
	public static String getCurrentHourHH() {
		String hour = null;
		DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("HH");
		DateTime todaysDate = new DateTime();
		hour = hourFormatter.print(todaysDate);	
		return hour;
			
	}
	/* This Method is used to return current hour according to 24 hour format. */	
	public static String getMinuteMinusOne() {
		String minute = null;
		DateTimeFormatter minuteFormatter = DateTimeFormat.forPattern("mm");
		DateTime todaysDate = new DateTime();
		todaysDate = todaysDate.minusMinutes(1);
		minute = minuteFormatter.print(todaysDate);
		return minute;
		
	}
	
	/* This Method is used to return a list documents satisfying a supplied criteria(query) for a given index. */
	public static List<Document> getDocument(String URL, String indexName, String basicQuery, String advancedQuery) throws IOException {
		SearchConnection  searchConnection = new SearchConnection();
		SearchQuery searchQuery = null;
		/*if (query.contains("lq=")) {
			query = query.replaceFirst("lq=", "");
			searchQuery = new SearchQuery();
			searchQuery.setAdvancedQuery(query);
		} else if (query.contains("q=")) {
			query = query.replaceFirst("q=", "");
			searchQuery = new SearchQuery(query);
		} */
		if(basicQuery != null) {
			searchQuery = new SearchQuery(basicQuery);
		} else {
			searchQuery = new SearchQuery();
		}
		searchQuery.setAdvancedQuery(advancedQuery);
		searchConnection.setIndex(indexName);
		searchConnection.setURL(URL);
		/*System.out.println("AlertUtil-URL:" + URL);
		System.out.println("AlertUtil-indexName:" + indexName);
		System.out.println("AlertUtil-query:" + query);*/
		net.javacoding.xsearch.api.Result result  = searchConnection.search(searchQuery);
		return result.getDocList();	
	}
	
	/* This Method is used to send an email. */	
	public static boolean sendMail(String body, String emailId, String fromAddress, String smtpServerName,
			String smtpServerPassword, int smtpServerPort, String sEmailSubject)
			throws Exception {
		boolean isSuccess = false;
		final Session session = Session.getInstance(System.getProperties(), null);
		final Transport transport = session.getTransport("smtps");
		try {
			final Message msg = new MimeMessage(session);
			logger.debug("msg: " + msg);
			logger.debug("body: " + body);
			logger.debug("emailId: " + emailId);
			logger.debug("fromAddress: " + fromAddress);
			logger.debug("smtpServerName: " + smtpServerName);
			logger.debug("smtpServerPassword: " + smtpServerPassword);
			logger.debug("smtpServerPort: " + smtpServerPort);
			logger.debug("sEmailSubject: " + sEmailSubject);
			msg.setFrom(new InternetAddress(fromAddress));
			InternetAddress[] recipientAddresses = new InternetAddress[1];
			recipientAddresses[0] = new InternetAddress(emailId);
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(emailId));
			msg.setSentDate(new Date());
			msg.setSubject(sEmailSubject);
			String link = "<a href=\"".concat(body).concat("\"").concat(">").concat("click on the link to view all the " +
					"records which are updated.").concat("</a>");
			msg.setContent(link, "text/html");
			transport.connect(smtpServerName, smtpServerPort, fromAddress, smtpServerPassword);
			transport.sendMessage(msg, recipientAddresses);
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			transport.close();
		}
		return isSuccess;
	}
	
	/* Calling the Web Service. Since Web Service deployed is backed with WS Security, Authentication Credentials Needs 
	 * To Be Sent From The Client Side. */	
	public static AlertFacadeService getAlertFacadeService(String wsdl) {
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.getInInterceptors().add(new LoggingInInterceptor());
		Map<String,Object> outProps = new HashMap<String,Object>();
		outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
		outProps.put(WSHandlerConstants.USER, "developer");
		outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
		outProps.put(WSHandlerConstants.PW_CALLBACK_CLASS, ClientPasswordCallback.class.getName());
		factory.getOutInterceptors().add(new WSS4JOutInterceptor(outProps));
		factory.setServiceClass(AlertFacadeService.class);
		factory.setAddress(wsdl);
		AlertFacadeService alertFacadeService = (AlertFacadeService) factory.create();
		return alertFacadeService;
	}

	public static Map<String, List<UserAlert>> getUserAlertsHashMap(List<UserAlert> userAlerts) {
		Map<String, List<UserAlert> > userAlertsHashMap = null;
		List<UserAlert> userAlertsByUserName = null;
		String userName = null;
		userAlertsHashMap = new HashMap<String, List<UserAlert>>();
		for (UserAlert userAlert : userAlerts) {
			userName = userAlert.getUsername();
			if(userAlertsHashMap.get(userName) == null) {
				userAlertsByUserName = new LinkedList<UserAlert>();
				userAlertsByUserName.add(userAlert);
				userAlertsHashMap.put(userName, userAlertsByUserName);
			} else {
				userAlertsByUserName = userAlertsHashMap.get(userName);
				userAlertsByUserName.add(userAlert);
				userAlertsHashMap.put(userName, userAlertsByUserName);
			}
		}
		return userAlertsHashMap;
	}

	public static String getLastCheckDate(String lastRunTime) {
		String lastCheckDate = null, year = null, month = null, day = null;
		if(lastRunTime == null) {
			lastCheckDate = getCurrentDateYYMMDD();			
		} else {
			year = lastRunTime.substring(0,4);
			month = lastRunTime.substring(5,7);
			day = lastRunTime.substring(8,10);			 
			lastCheckDate = year.concat(month).concat(day);			
		}
		return lastCheckDate;
	}

	public static String getLastCheckTime(String lastRunTime) {
		String lastCheckTime = null, hour = null, minute = null;
		if(lastRunTime == null) {			
			lastCheckTime = getCurrentHourHH().concat(AlertUtil.getMinuteMinusOne());
		} else {
			hour = lastRunTime.substring(11,13); 
			minute = lastRunTime.substring(14,16); 
			lastCheckTime = hour.concat(minute);
		}
		return lastCheckTime;
	}		
}
