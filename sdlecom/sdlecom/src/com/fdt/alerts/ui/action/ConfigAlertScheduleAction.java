package com.fdt.alerts.ui.action;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.foundation.NativeForkJob;
import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.utility.FileUtil;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

import com.fdt.alerts.job.AlertJob;
import com.fdt.alerts.util.AlertUtil;
import com.fdt.alerts.job.AlertManager;
import com.fdt.sdl.util.SecurityUtil;

public class ConfigAlertScheduleAction extends Action  {
	 
	static Logger logger = LoggerFactory.getLogger(ConfigAlertScheduleAction.class);
	
	 public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, 
			 HttpServletResponse response) throws IOException, ServletException {
		/*return mapping.findForward("continue");*/
		 if (!SecurityUtil.isAdminUser(request)) return (mapping.findForward("welcome"));
		    JobDetail jobDetail = null;
	        ActionMessages errors = new ActionMessages();
	        ActionMessages messages = new ActionMessages();
	        String operation = request.getParameter("operation");
	        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
	        Scheduler  scheduler = null;
	       try {
	    	   File resourceFile = FileUtil.resolveFile(WebserverStatic.getRootDirectoryFile(), "WEB-INF", "data", 
	    			   "AlertSystem.properties");
	    	   String fileName = resourceFile.getAbsolutePath();
	    	   scheduler = schedulerFactory.getScheduler();
	    	   // When clicked on delete from alertConfiguration.stl
	    	   if(operation != null && operation.equalsIgnoreCase("delete")) {
	    		   AlertUtil.removeProperty("cronTrigger", fileName);
	    		   AlertUtil.removeProperty("interval", fileName);
	    		   TriggerKey triggerKey = new TriggerKey("trigger1", "group1");
	    		   scheduler.unscheduleJob(triggerKey);
	    		   return (mapping.findForward("continue"));
	    	   }
	    	   // When cronTrigger Format is present in properties file.
	    	   String cronTrigger = AlertUtil.readProperty("cronTrigger", fileName);
	    	   if(cronTrigger != null) {
    			   logger.debug("Cron Trigger: " + cronTrigger);
    			   request.setAttribute("cronTrigger", cronTrigger);
    			   request.setAttribute("isInterval", false);
    			   String isEnabled = AlertUtil.readProperty("isEnabled", fileName);
    			   if(isEnabled != null && isEnabled.equalsIgnoreCase("true")){
    				   request.setAttribute("isEnabled", true);
    			   } else {
    				   request.setAttribute("isEnabled", false);
    			   }
    			   return (mapping.findForward("alertConfiguration"));
    		    } 
    		 
    		   // When interval is specified in properties file.
    		   String interval = AlertUtil.readProperty("interval", fileName);
    		  
    		   if(interval != null) {
    			   logger.debug("interval: " + interval);
    			   request.setAttribute("interval", interval);
    			   request.setAttribute("isInterval", true);
    			   String isEnabled = AlertUtil.readProperty("isEnabled", fileName);
    			   if(isEnabled != null && isEnabled.equalsIgnoreCase("true")){
    				   request.setAttribute("isEnabled", true);
    			   } else {
    				   request.setAttribute("isEnabled", false);
    			   }
    			   return (mapping.findForward("alertConfiguration"));
    		    } 
    		   
    		   // When cronTrigger Format/interval is not present in properties file. 
    		    String cronSettings = null;
    		    String scheduleType = request.getParameter("scheduleType");
    		    interval = request.getParameter("interval");
    		    if((interval !=null && interval.trim() == "" && scheduleType.equals("0") )|| (interval !=null && interval.trim() != "" && scheduleType.equals("1"))) {
    		    	errors.add("error", new ActionMessage("configuration.alertwrongsettingsschedule.error", "Wrong Settings !!"));
    		    	return (mapping.findForward("continue"));
    		    }
    		    
    		    int heapSize = 0;
    		    File propertiesFile = FileUtil.resolveFile(WebserverStatic.getRootDirectoryFile(), "WEB-INF", "data", "AlertSystem.properties");
    			String alertFileName = propertiesFile.getAbsolutePath();
    		    String heapSizeString  = AlertUtil.readProperty("heapSize", alertFileName);
    		    if(heapSizeString != null) {
    		    	heapSize = Integer.parseInt(heapSizeString);
    		    }
    		    String cmd = AlertUtil.getJavaCommand(AlertManager.class.getName(), heapSize);
    		    scheduler.start();
    		    jobDetail = newJob(AlertJob.class).withIdentity("myJob", "group1").build();
    		    jobDetail.getJobDataMap().put(NativeForkJob.PROPERTY_JAVA_COMMAND, cmd );
    		    jobDetail.getJobDataMap().put(NativeForkJob.PROPERTY_CLASSPATH, WebserverStatic.getClassPath() );
    		    jobDetail.getJobDataMap().put(NativeForkJob.PROPERTY_DIRECTORY, WebserverStatic.getJobWorkingDirectory());
    		    Trigger trigger = null;
    		    if(scheduleType != null && scheduleType.equals("1") && request.getParameter("day_sel") != null) {
		           if ("1".equals(request.getParameter("day_sel"))) {
			           cronSettings = getCronValue(request, "minute_sel", "minute")+" "+
		    		   getCronValue(request, "hour_sel"  , "hour")+" "+
		    		   "?" + " " + getCronValue(request, "month_sel" , "month")+" "+
		    		   getCronValue(request, "day_sel"       , "day");
		           } else {//"day_sel" is 2 or *, Run at times selected by days of the month
		               cronSettings = getCronValue(request, "minute_sel", "minute")+ " " +
	                   getCronValue(request, "hour_sel"  , "hour") + " " +
	                   getCronValue(request, "day_sel"       , "day2") + " " +
	                   getCronValue(request, "month_sel" , "month") + " " + "?";
		           }
		           cronTrigger = "0 "+ cronSettings;
		           AlertUtil.writeProperty("cronTrigger", cronTrigger, fileName);
		           request.setAttribute("cronTrigger", cronTrigger);
		           trigger = newTrigger()
							.withIdentity("trigger1", "group1")
							.withSchedule(cronSchedule(cronTrigger).withMisfireHandlingInstructionFireAndProceed())
							.forJob("myJob", "group1")
							.build();
    			   String isEnabled = AlertUtil.readProperty("isEnabled", fileName);
    			   if(isEnabled != null && isEnabled.equalsIgnoreCase("true")){
    				   request.setAttribute("isEnabled", true);
    			   } else {
    				   request.setAttribute("isEnabled", false);
    			   }
    			   scheduler.scheduleJob(jobDetail, trigger);
		           return (mapping.findForward("alertConfiguration"));
           	   } else if(scheduleType != null && scheduleType.equals("0") && interval != null) {
	           	   request.setAttribute("interval", interval);
	 			   request.setAttribute("isInterval", true);
	 			   AlertUtil.writeProperty("interval", interval, fileName);
	 			   trigger = (SimpleTrigger) newTrigger()
							 .withIdentity("trigger1", "group1")
							 .withSchedule(simpleSchedule()
											.withMisfireHandlingInstructionNowWithRemainingCount()
											.repeatForever()
											.withIntervalInMilliseconds(Long.parseLong(interval) * 1000 * 60))
							.startAt(new java.util.Date(System.currentTimeMillis() + 60 * 1000))
							.build();
	 			   String isEnabled = AlertUtil.readProperty("isEnabled", fileName);
	 			   if(isEnabled != null && isEnabled.equalsIgnoreCase("true")){
	 				   request.setAttribute("isEnabled", true);
	 			   } else {
	 				   request.setAttribute("isEnabled", false);
	 			   }
	 			  scheduler.scheduleJob(jobDetail, trigger);
	 			   return (mapping.findForward("alertConfiguration"));
           	  } else {
           		     return (mapping.findForward("continue"));
           	   }
	       } catch (Exception e) {
	            errors.add("error", new ActionMessage("configuration.alertexceptionschedule.error", e.getMessage()));
	        } finally {
	            saveErrors(request, errors);
	            saveMessages(request, messages);
	        }
	        return (mapping.findForward("continue"));
 		
 }
	 private String getCronValue(HttpServletRequest request, String selectAllId, String id){
	        String typeValue = U.getText(request.getParameter(selectAllId),"*");        
	        if(typeValue.equals("*")) return typeValue;
	        String[] values = request.getParameterValues(id);        
	        if(values==null) return "*";
	        if(values.length==1) return values[0];
	        StringBuffer sb = new StringBuffer();
	        for(int i=0;i<values.length;i++){
	            if(i!=0) sb.append(",");
	            sb.append(values[i]);
	        }
	        return sb.toString();
	    }
}
