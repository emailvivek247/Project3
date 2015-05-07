package com.fdt.sdl.admin.servlet;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import net.javacoding.xsearch.SmartDataLayer;
import net.javacoding.xsearch.config.InstanceJobSchedule;
import net.javacoding.xsearch.config.Schedule;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.config.WebConfiguration;
import net.javacoding.xsearch.foundation.NativeForkJob;
import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.utility.FileUtil;
import net.javacoding.xsearch.utility.SchedulerTool;
import net.javacoding.xsearch.utility.U;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ListenerManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.alerts.job.AlertJob;
import com.fdt.alerts.job.AlertManager;
import com.fdt.alerts.util.AlertUtil;
import com.fdt.sdl.core.ui.action.indexing.status.SDLJobListener;

/**
 * Initialisation Servlet which is called automaticaly at
 * the start of the servlet container.
 * Don't forget the  <load-on-startup>  in web.xml
 * @version 1.0
 */
public class SDLStartupServlet extends HttpServlet {

    private static Logger logger   = LoggerFactory.getLogger(SDLStartupServlet.class.getName());


    /** Initializes the servlet.
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try{
            WebserverStatic.setRootDirectory(config.getServletContext().getRealPath(""));
            WebserverStatic.setIsServer(true);

            // Set the server configuration file
            File configFile = FileUtil.resolveFile(WebserverStatic.getRootDirectoryFile(), "WEB-INF","data","xsearch-config.xml");
            File starterTemplate = FileUtil.resolveFile(WebserverStatic.getRootDirectoryFile(), "WEB-INF","data","xsearch-config.xml.template");
            if(!configFile.exists()&&starterTemplate.exists()) {
                FileUtil.copyFile(starterTemplate, configFile);
            }
            ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
            sc.setConfigFile(configFile);
            File webConfigFile = FileUtil.resolveFile(WebserverStatic.getRootDirectoryFile(), "WEB-INF","conf","analyzer-config.xml");
            WebserverStatic.setWebConfiguration(new WebConfiguration(webConfigFile));
            String encoding = config.getInitParameter("encoding");
            if(!U.isEmpty(encoding)) {
                WebserverStatic.setEncoding(encoding);
            }

            /** Reversed the Order So that the Scheduler Will fire after the Index is Loaded **/
            SmartDataLayer.init(new File(config.getServletContext().getRealPath("")));
            if (Boolean.parseBoolean(config.getServletContext().getInitParameter("isAlertsEnabled"))) {
            	this.initAlerts();
            }
            SchedulerTool.init(config.getServletContext().getRealPath(config.getInitParameter("scheduler-config-file")));
            for(Entry<String, InstanceJobSchedule> entry : sc.getInstanceJobSchedules().entrySet()) {
            	Schedule schedule = entry.getValue().getSchedule();
        		String jobDataString = entry.getValue().getJobDataString();
        		String scheduleName = entry.getKey();
        		if(schedule != null && jobDataString != null) {
            		String cronSetting = schedule.getCronSetting();
            		String cronTrigger = "0 "+ cronSetting;
            		long interval = schedule.getInterval();
        			boolean isInterval = schedule.getIsInterval();
        			boolean isEnabled = schedule.getIsEnabled();
        			if(isEnabled) {
        				configureSchedule(isInterval, cronTrigger, interval, jobDataString, scheduleName);
        			}
            	}  
            }
            logger.info("Smart Data Layer is up and running");
        } catch(Exception exception){
            exception.printStackTrace();
        }
    }

    /** Destroys the servlet.
     */
    @Override
    public void destroy() {
    	logger.info("Smart Data Layer is Shutting Down!");
        SmartDataLayer.destroy();
    }
    
    private void initAlerts() {
	    Scheduler scheduler = null;
		Trigger trigger = null;
		JobDetail jobDetail = null;
		try {
			int heapSize = 0;
			File propertiesFile = FileUtil.resolveFile(WebserverStatic.getRootDirectoryFile(), "WEB-INF", "data", 
					"AlertSystem.properties");
			String fileName = propertiesFile.getAbsolutePath();
			String cronTrigger = AlertUtil.readProperty("cronTrigger", fileName);
			String interval = AlertUtil.readProperty("interval", fileName);
			String heapSizeString  = AlertUtil.readProperty("heapSize", fileName);
		    logger.debug("Cron Trigger: " + cronTrigger);
		    logger.debug("interval: " + interval);
		    if( (cronTrigger == null && interval == null) || (cronTrigger != null && interval != null)) {
		    	throw new IOException("Specify Either cronTrigger or interval in AlertSystem.properties ");
		    }
		    if(heapSizeString != null) {
		    	heapSize = Integer.parseInt(heapSizeString);
		    }
		    String cmd = AlertUtil.getJavaCommand(AlertManager.class.getName(), heapSize);
			SchedulerFactory schedulerFactory = new StdSchedulerFactory();
			scheduler = schedulerFactory.getScheduler();
			scheduler.start();
			jobDetail = newJob(AlertJob.class).withIdentity("myJob", "group1").build();
			jobDetail.getJobDataMap().put(AlertJob.PROPERTY_JAVA_COMMAND, cmd);
	        jobDetail.getJobDataMap().put(AlertJob.PROPERTY_CLASSPATH, WebserverStatic.getClassPath());
	        jobDetail.getJobDataMap().put(AlertJob.PROPERTY_DIRECTORY, WebserverStatic.getJobWorkingDirectory());
			try {
				if(cronTrigger != null) {
					trigger = newTrigger()
							.withIdentity("trigger1", "group1")
							.withSchedule(cronSchedule(cronTrigger).withMisfireHandlingInstructionFireAndProceed())
							.forJob("myJob", "group1")
							.build();					
				} else if (interval != null) {
					trigger = (SimpleTrigger) newTrigger()
							.withIdentity("trigger1", "group1")
							.withSchedule(simpleSchedule()
											.withMisfireHandlingInstructionNowWithRemainingCount()
											.repeatForever()
											.withIntervalInMilliseconds(Long.parseLong(interval) * 1000 * 60))
							.startAt(new java.util.Date(System.currentTimeMillis() + 60 * 1000))
							.build();					
				}
				scheduler.scheduleJob(jobDetail, trigger);				
			} catch (Exception ex) {
				logger.debug("Check Cron trigger format!!!");
			}
		} catch (SchedulerException ex) {
			logger.debug("Scheduler Exception !!");
		} catch (IOException e) {
			logger.debug("No Trigger Information is specified !!");
		}
    }	

    
    private void configureSchedule(boolean isInterval, String cronTrigger, long interval, String jobDataString, 
    		String scheduleName) {
		Scheduler scheduler = WebserverStatic.getScheduler();
		String groupName = "Instance Job";
		Trigger trigger = null;
		if(!isInterval) {
			 trigger =  newTrigger()
			.withIdentity(scheduleName, groupName)
			.withSchedule(cronSchedule(cronTrigger).withMisfireHandlingInstructionFireAndProceed())						
			.build();			   
		} else {
			 trigger = (SimpleTrigger) newTrigger()
			.withIdentity(scheduleName, groupName)
			.withSchedule(simpleSchedule()
			.withMisfireHandlingInstructionNowWithRemainingCount()
			.repeatForever()
			.withIntervalInMilliseconds(interval * 1000 * 60))
			.startAt(new java.util.Date(System.currentTimeMillis() + 60 * 1000))
			.build();			  
		}
		try {
			long time = 0;
			String status = "";
			Class nativeForkJob = NativeForkJob.class;
			JobDetail jobDetail = newJob(nativeForkJob).withIdentity(scheduleName, groupName).build();
			jobDetail.getJobDataMap().put(NativeForkJob.PROPERTY_JAVA_COMMAND, jobDataString);
			jobDetail.getJobDataMap().put(NativeForkJob.PROPERTY_CLASSPATH, WebserverStatic.getClassPath());
			jobDetail.getJobDataMap().put(NativeForkJob.PROPERTY_DIRECTORY, WebserverStatic.getJobWorkingDirectory());
			jobDetail.getJobDataMap().put(NativeForkJob.PROPERTY_JOB_TYPE, "linkingJob");
			SDLJobListener sdlJobListener = new SDLJobListener(groupName+scheduleName, status, time);
			ListenerManager listenerManager = scheduler.getListenerManager();
			listenerManager.addJobListener(sdlJobListener, KeyMatcher.keyEquals(new JobKey(scheduleName, groupName)));
			scheduler.scheduleJob(jobDetail, trigger);
		} catch (SchedulerException e) {
			logger.error("Scheduler Failed To Schedule A Linking Job !!");
		}		
	}
}
