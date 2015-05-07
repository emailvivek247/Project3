package com.fdt.alerts.job;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.javacoding.xsearch.api.Document;
import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.utility.FileUtil;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.fdt.alerts.service.AlertFacadeService;
import com.fdt.alerts.service.AlertNameHyperLinkKeyMap;
import com.fdt.alerts.service.EMailDTO;
import com.fdt.alerts.service.EMailDTO.MapData;
import com.fdt.alerts.service.EMailDTO.MapData.Entry;
import com.fdt.alerts.service.NodeConfiguration;
import com.fdt.alerts.service.UserAlert;
import com.fdt.alerts.util.AlertUtil;
import com.fdt.common.util.cxf.ClientPasswordCallback;

import com.fdt.sdl.styledesigner.util.PageStyleUtil;


public class AlertManager {
	
	static Logger logger = LoggerFactory.getLogger(AlertManager.class);
	
	public static void main(String args[]) throws MalformedURLException {

		/* Initializing the variables. */
		int firstResult = 0;
		String lastCheckDate = null, lastCheckTime = null, lastRunTime = null, alertQuery = null, emailTemplateFile = null;
		String baseURL = null, indexName = null, userName = null, nodeName = null, alertServiceWsdl = null;
		List<UserAlert> userAlerts = null;
		Map<String, List<UserAlert> > userAlertsHashMap = null;
		String firstName = null, lastName = null, advancedQuery = null, basicQuery = null, alertQueryToBeDisplayed = null;
		NodeConfiguration nodeConfig = null;
						
		/* Determining the last run time. */
		logger.info("************** TIMESTAMPS *******************");
		File alertPropertiesFile = FileUtil.resolveFile(WebserverStatic.getRootDirectoryFile(), "data", "AlertSystem.properties");
		String fileName = alertPropertiesFile.getAbsolutePath();
		try {
			lastRunTime = AlertUtil.readProperty("lastRunTime", fileName);
			nodeName = AlertUtil.readProperty("nodeName", fileName);
			alertServiceWsdl = AlertUtil.readProperty("alert.serviceWsdl", fileName);
		} catch (IOException e) {
			logger.debug("Configuration Properties Missing");
			lastCheckDate = null;
		}
		String currentDate = AlertUtil.getCurrentDateYYMMDD();
		String currentTime = AlertUtil.getCurrentHourHH().concat(AlertUtil.getMinuteMinusOne());
		logger.debug("currentDate: " + currentDate);
		logger.debug("currentTime: " + currentTime);
		lastCheckDate = AlertUtil.getLastCheckDate(lastRunTime);
		lastCheckTime = AlertUtil.getLastCheckTime(lastRunTime);
		logger.info("lastCheckDate: " + lastCheckDate);
		logger.info("lastCheckTime: " + lastCheckTime);
		logger.info("nodeName: " + nodeName);
		
		/* Creating the userAlertHash Map where key is userName(Email Id) & value is List<UserAlert> that particular user
		 *  registered to. */
		logger.info("************Number of users**************");
		
		AlertFacadeService alertFacadeService = AlertUtil.getAlertFacadeService(alertServiceWsdl);
		nodeConfig = alertFacadeService.getNodeConfiguration(nodeName);
		while(true) {
			userAlerts = alertFacadeService.getUserAlerts(nodeName, firstResult);
			if(userAlerts == null || userAlerts.size() == 0) {
				break;
			} else {
				userAlertsHashMap = AlertUtil.getUserAlertsHashMap(userAlerts);				
				firstResult = firstResult + 3;
		    }
			logger.info("userAlertsHashMap size: " + (userAlertsHashMap != null ? userAlertsHashMap.size(): "0"));
			/* With the given Criteria, searching an index... If record exists, 
			 * that criteria is added to a list and that list of alerts is mailed to corresponding user. */			
			emailTemplateFile = nodeConfig.getEmailTemplateFolder() + nodeConfig.getAlertTemplate();			
			if(userAlertsHashMap != null && userAlertsHashMap.size() > 0 ) {
				for (Map.Entry<String, List<UserAlert>> entry : userAlertsHashMap.entrySet()) {
				    userName = entry.getKey();
				    List<UserAlert> userAlertsForUser = entry.getValue();
				    List<UserAlert> toBeMailedUserAlertList = new LinkedList<UserAlert>();
				    int numberOfAlerts = 0;
				    for(UserAlert userAlertForUser: userAlertsForUser) {
				    	List<Document> resultList = null;
				    	firstName = userAlertForUser.getFirstName();
						lastName = userAlertForUser.getLastName();
				    	String actualCriteria = null;	
						baseURL = userAlertForUser.getBaseURL();
						indexName = userAlertForUser.getIndexName();
						actualCriteria = userAlertForUser.getAlertQuery();
					    alertQuery = "#DATE_MOD:[" + lastCheckDate + " TO " + currentDate + "]";
						alertQuery = alertQuery + " AND #HOUR_MINUTE:[" + lastCheckTime + " TO " + currentTime + "]";
						advancedQuery = null;
						basicQuery = null;
						if (actualCriteria.contains("lq=")) {
							actualCriteria = actualCriteria.replaceFirst("lq=", "");
							alertQuery = alertQuery + " AND " + actualCriteria;
							alertQuery = PageStyleUtil.encodeURL(alertQuery);
							advancedQuery = alertQuery;
							alertQueryToBeDisplayed = userAlertForUser.getAlertQuery();
							alertQueryToBeDisplayed = alertQueryToBeDisplayed.replace("lq=", "");
							alertQueryToBeDisplayed = alertQueryToBeDisplayed.replace("#", "");
							alertQueryToBeDisplayed = alertQueryToBeDisplayed.replace("~5", "");
							alertQueryToBeDisplayed = alertQueryToBeDisplayed.replace(":", "=");
							alertQueryToBeDisplayed = alertQueryToBeDisplayed.replace("\"", "");
						} else if (actualCriteria.contains("q=")) {
							actualCriteria = actualCriteria.replaceFirst("q=", "");
							basicQuery = actualCriteria;
							advancedQuery = PageStyleUtil.encodeURL(alertQuery);
							alertQueryToBeDisplayed = userAlertForUser.getAlertQuery();
							alertQueryToBeDisplayed = alertQueryToBeDisplayed.replace("q=", "");
						} 
						resultList = checkForAlerts(baseURL, indexName, basicQuery, advancedQuery );
						if (resultList != null && resultList.size() >= 1) {
							userAlertForUser.setDisplayable(true);
							numberOfAlerts = numberOfAlerts + 1;						
						} else {
							userAlertForUser.setDisplayable(false);
							logger.debug("No Record Has been Updated." + AlertUtil.getMinuteMinusOne());
						}
						userAlertForUser.setBasicQuery(basicQuery);
						userAlertForUser.setAdvancedQuery(advancedQuery);
						userAlertForUser.setAlertQueryToBeDisplayed(alertQueryToBeDisplayed);
	     				toBeMailedUserAlertList.add(userAlertForUser);
				    }
				    if( numberOfAlerts > 0 ) {
				    	logger.info("Number of Alerts For " + userName +" : " + numberOfAlerts);
				    	logger.info("-----------------------BEFORE SENDING EMAIL------------------------------------- ");
				    	for(UserAlert userAlert: toBeMailedUserAlertList) {
				    		logger.info("userName: " + userName);
				    		logger.info("firstName: " + firstName);
				    		logger.info("lastName: " + lastName);
				    		logger.info("alertName: " + userAlert.getAlertName());
							logger.info("BasicQuery: " + userAlert.getBasicQuery());
							logger.info("AdvancedQuery: " + userAlert.getAdvancedQuery());
							logger.info("isDisplyable: " + userAlert.isDisplayable());
				    		
				    	}
				    	logger.info("-----------------------BEFORE SENDING EMAIL------------------------------------- ");
				    	try {
							sendEmail(userName, firstName, lastName, numberOfAlerts, toBeMailedUserAlertList, fileName, 
									emailTemplateFile, alertFacadeService);	
							logger.info("emailTemplateFile: " + emailTemplateFile);
						} catch (Exception e) {
							e.printStackTrace();
						}
				    }
				}
			}
		}
		try {
			 AlertUtil.writeProperty("lastRunTime", new DateTime().toString(), fileName);
		} catch (Exception e) {
			logger.debug("Writing lastRunTime Operation Failed...");
		}		
	}
	
	public static List<Document> checkForAlerts(String URL, String indexName, String basicQuery, String advancedQuery) {
		List<Document> docList = null;
		try {
			logger.info("***********************CHECKING FOR ALERTS WITH THE CRITERIA***************** ");
			logger.info("URL: " + URL);
			logger.info("indexName: " + indexName);
			logger.info("basicQuery here: " + basicQuery);
			logger.info("advancedQuery here: " + advancedQuery);
			docList = AlertUtil.getDocument(URL, indexName, basicQuery, advancedQuery);			
		} catch (Exception e) {
			logger.debug("Something Wrong with URL, indexName or search criteria.");
		}
		return docList;
	}
	
	public static void sendEmail(String toEmailAddress, String firstName, String lastName, int numberOfAlerts,
			List<UserAlert> toBeMailedUserAlertList, String fileName, String emailTemplateFile,
			AlertFacadeService alertFacadeService) throws IOException {
		String link = null, message = null, advancedQuery = null, basicQuery = null, baseURL = null, indexName = null;
		MapData mapData = new MapData();
		String fromEmailAddress = AlertUtil.readProperty("alert.fromEmailAddress", fileName);
		String alertSubject = AlertUtil.readProperty("alert.emailSubject", fileName);
		List<AlertNameHyperLinkKeyMap> alertNameHyperLinkKeyLinkList = new LinkedList<AlertNameHyperLinkKeyMap>();
		logger.info("-----------------------EMAIL------------------------------------- ");
		for(UserAlert userAlert: toBeMailedUserAlertList) {
			indexName = userAlert.getIndexName();
			String alertName = userAlert.getAlertName();
			baseURL = userAlert.getBaseURL();
			String templateName = userAlert.getTemplateName();
			message = baseURL + "search.do?indexName=" + indexName;
			message = message + "&templateName=" + templateName;
			advancedQuery = userAlert.getAdvancedQuery();
			basicQuery = userAlert.getBasicQuery();
			if(advancedQuery != null) {
				advancedQuery = "&lq=" + advancedQuery;
				message = message + advancedQuery;
			}
			if(basicQuery != null) {
				basicQuery = "&q=" + basicQuery;
				message = message + basicQuery;
			}
			link = message;
			userAlert.setEmailLink(link);
			AlertNameHyperLinkKeyMap alertNameHyperLink = new AlertNameHyperLinkKeyMap();
			alertNameHyperLink.setKey(alertName);
			alertNameHyperLink.setValue(userAlert);
			logger.info("alertName: " + alertName);
			logger.info("link: " + link);
			logger.info("isDisplyable: " + userAlert.isDisplayable());
			logger.info("emailTemplateFile: " + emailTemplateFile);
			alertNameHyperLinkKeyLinkList.add(alertNameHyperLink);
		}
		EMailDTO emailDTO = new EMailDTO();
		emailDTO.setFromEmailId(fromEmailAddress);
		emailDTO.setToEmailId(toEmailAddress);
		emailDTO.setSubject(alertSubject);
		emailDTO.setEmailTemplateName(emailTemplateFile);
		Entry e1 = new Entry();
		Entry e2 = new Entry();
		Entry e3 = new Entry();
		Entry e4 = new Entry();
		Entry e5 = new Entry();
		Entry e6 = new Entry();
		List<Entry> entryList = new LinkedList<Entry>();
		e1.setKey("userFirstName");
		e1.setValue(firstName);
		entryList.add(e1);
		e2.setKey("userLastName");
		e2.setValue(lastName);
		entryList.add(e2);
		e3.setKey("baseURL");
		e3.setValue(baseURL);
		entryList.add(e3);
		e4.setKey("currentDate");
		e4.setValue(new Date());
		entryList.add(e4);
		e5.setKey("currentDate");
		e5.setValue(new Date());
		entryList.add(e5);
		e6.setKey("fromEmailAddress");
		e6.setValue(fromEmailAddress);
		entryList.add(e6);
		mapData.getEntry().addAll(entryList);
		emailDTO.setMapData(mapData);
		try {
			alertFacadeService.sendEmail(emailDTO, alertNameHyperLinkKeyLinkList);
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("Exception Occured in Sending Alert Email.." + e.getMessage());
		}
	}
}
