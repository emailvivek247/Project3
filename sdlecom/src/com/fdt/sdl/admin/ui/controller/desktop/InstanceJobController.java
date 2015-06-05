package com.fdt.sdl.admin.ui.controller.desktop;

import static com.fdt.security.ui.SecurityViewConstants.LIST_SCHEDULES;
import static com.fdt.security.ui.SecurityViewConstants.SAVE_INSTANCE_JOB;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.InstanceJobSchedule;
import net.javacoding.xsearch.config.Schedule;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.core.exception.ConfigurationException;
import net.javacoding.xsearch.foundation.NativeForkJob;
import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.utility.U;

import org.apache.commons.lang.StringUtils;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.matchers.KeyMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.sdl.admin.ui.controller.AbstractBaseSDLController;
import com.fdt.sdl.core.ui.action.indexing.status.SDLJobListener;
import com.fdt.sdl.styledesigner.util.PageStyleUtil;

import freemarker.template.SimpleHash;

/** This Class contains methods related to Instance Job Schedule.  */

@Controller
public class InstanceJobController extends AbstractBaseSDLController {
	
	static Logger logger = LoggerFactory.getLogger(InstanceJobController.class);

	@RequestMapping(value = "/admin/saveJobLink.admin")
	public ModelAndView saveJobLink(HttpServletRequest request, @RequestParam(required = false) String operation,
			@RequestParam(required = false) String scheduleType, @RequestParam(required = false) String interval, 
			@RequestParam(required = false) String scheduleName, @RequestParam(required = false) String isEnabled) 
			throws ConfigurationException, SchedulerException {
		
		/*** Initializing the variables. */
		ModelAndView modelAndView = new ModelAndView();
		Map<Integer, String> jobMap = new TreeMap<Integer, String>();
		Map<String, String> modelMap = new LinkedHashMap<String, String>();
		Scheduler sched = WebserverStatic.getScheduler();
		String message = "", cronSettings = null, cronTrigger = null, groupName = "Instance Job";
		sched.deleteJob(new JobKey(scheduleName, groupName));
		Schedule schedule = new Schedule();
		ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
		List<String> stringarray = new LinkedList<String>();
		stringarray.add("-1");
		Trigger trigger = null;
		List<Integer> jobList = new LinkedList<Integer>();
		InstanceJobSchedule instanceJobSchedule = new InstanceJobSchedule();
		List<DatasetConfiguration> indexConfigList = ServerConfiguration.getDatasetConfigurations();
		modelAndView.setViewName(SAVE_INSTANCE_JOB);
		modelAndView.addObject("request", request);
		modelAndView.addObject("stringarray", stringarray);
		
		/** Constructing Job Map with key as order and value as action. */
		for(DatasetConfiguration indexConfig: indexConfigList) {
			String indexName = indexConfig.getName();
			String actionParameter = "action_".concat(indexName);
			String orderParameter = "actionorder_".concat(indexName);
			String action = request.getParameter(actionParameter);
			String order = request.getParameter(orderParameter);
			Integer key = new Integer(order);
			if(StringUtils.isBlank(action)) {
				continue;
			} 
			modelMap.put(actionParameter, action);
			modelMap.put(orderParameter, order);
			jobList.add(key);
			jobMap.put(key, indexName.concat(",").concat(action));			
		}
		SimpleHash modelMapSimpleHash = new SimpleHash();
		modelMapSimpleHash.putAll(modelMap);
		modelAndView.addObject("modelMap", modelMapSimpleHash);
		
		/** Throwing Exception When Multiple Indexes Have The Same Order. */
		if((jobList.size() != jobMap.size()) || indexConfigList.size() < 1) {
			message = "Action Order Needs To Be Unique Among Selected Indexes!";
			modelAndView.addObject("Exception", message);
			modelAndView.addObject("schedule", null);
			return modelAndView;
		}
		if(StringUtils.isBlank(scheduleName)) {
			message = "Schedule Name Needs To Be Specified!";
			modelAndView.addObject("Exception", message);
			modelAndView.addObject("schedule", null);
			return modelAndView;
		} else {
			scheduleName = scheduleName.toUpperCase();
			if(isScheduleNameAlreadyExists(scheduleName) && operation.equals("save") ) {
				message = "Schedule Name Should To Be Unique!";
				modelAndView.addObject("Exception", message);
				modelAndView.addObject("schedule", null);
				return modelAndView;
			}
		}
		
		/** Throwing Exception When Multiple Timer Details are Improperly Specified. */
		boolean flag1 = StringUtils.isBlank(interval); 
		boolean flag2 = request.getParameter("day_sel").equals("1") && request.getParameter("day") == null;
		boolean flag3 = request.getParameter("day_sel").equals("2") && request.getParameter("day2") == null;
		boolean flag4 = request.getParameter("month_sel").equals("1") && request.getParameter("month") == null;
		boolean flag5 = request.getParameter("hour_sel").equals("1") && request.getParameter("hour") == null;
		boolean flag6 =request.getParameter("minute_sel").equals("1") && request.getParameter("minute")  == null;
		boolean flag7 = flag2||flag3||flag4||flag5||flag6;
		if (operation.equals("save") && ((scheduleType.equals("0") && flag1) || (scheduleType.equals("1") && flag7)))  {
			message = "Timer Configuration Needs To Be Specified!";
			modelAndView.addObject("Exception", message);
			modelAndView.addObject("schedule", null);
			return modelAndView;
		}
		
		/** Constructing Job Data String. */
		String jobDataString = "";
		for (Map.Entry<Integer, String> entry : jobMap.entrySet()) {
			jobDataString = jobDataString.concat(entry.getValue()).concat("|");
		}
		
		/** Constructing Timer Configuration Based On Timer Type, Cron or Interval. */
		if(scheduleType != null && scheduleType.equals("1") && request.getParameter("day_sel") != null) {
			   schedule.setIsInterval(false);
			   if ("1".equals(request.getParameter("day_sel"))) {
		            cronSettings = getCronValue(request, "minute_sel", "minute")+" "+
	    		    getCronValue(request, "hour_sel"  , "hour")+" "+
	    		    "?" + " " + getCronValue(request, "month_sel" , "month")+" "+
	    		    getCronValue(request, "day_sel"       , "day");
	           } else {
	                cronSettings = getCronValue(request, "minute_sel", "minute")+ " " +
	                getCronValue(request, "hour_sel"  , "hour") + " " +
	                getCronValue(request, "day_sel"       , "day2") + " " +
	                getCronValue(request, "month_sel" , "month") + " " + "?";
	           }
	           cronTrigger = "0 "+ cronSettings;
	           schedule.setCronSetting(cronSettings);	          
	           modelAndView.addObject("cronTrigger", cronTrigger);
	           trigger =  newTrigger()
	        		     .withIdentity(scheduleName, groupName)
						 .withSchedule(cronSchedule(cronTrigger).withMisfireHandlingInstructionFireAndProceed())						
						 .build();			   
		} else if(scheduleType != null && scheduleType.equals("0") && interval != null) {
			   schedule.setIsInterval(true);
			   schedule.setInterval(Long.parseLong(interval));
			   modelAndView.addObject("interval", interval);
			   modelAndView.addObject("isInterval", true);
        	   trigger = (SimpleTrigger) newTrigger()
					     .withIdentity(scheduleName, groupName)
						 .withSchedule(simpleSchedule()
						 .withMisfireHandlingInstructionNowWithRemainingCount()
						 .repeatForever()
						 .withIntervalInMilliseconds(Long.parseLong(interval) * 1000 * 60))
						 .startAt(new java.util.Date(System.currentTimeMillis() + 60 * 1000))
						 .build();			  
		}
		
		/** Creating Quartz Job Schedule. */
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
			sched.getListenerManager().addJobListener(sdlJobListener, KeyMatcher.keyEquals(new JobKey(scheduleName, groupName)));
			if(isEnabled != null && isEnabled.equalsIgnoreCase("1")) {			
				sched.scheduleJob(jobDetail, trigger);
				schedule.setIsEnabled(true);
			} else {
				schedule.setIsEnabled(false);
			}
		} catch (SchedulerException e) {
			message = "Scheduler Failed To Schedule A Linking Job!";
			logger.error(message);
		}
		instanceJobSchedule.setJobDataString(jobDataString);
		modelMap = PageStyleUtil.getModelMap(jobDataString);
		instanceJobSchedule.setModelMap(modelMap);
		instanceJobSchedule.setSchedule(schedule);
		instanceJobSchedule.setScheduleName(scheduleName);
		ServerConfiguration.setInstanceJobSchedule(instanceJobSchedule);
		Map<String, InstanceJobSchedule> instanceJobSchedules = ServerConfiguration.getInstanceJobSchedules();
		SimpleHash instanceJobSchedulesSimpleHash = new SimpleHash();
		instanceJobSchedulesSimpleHash.putAll(instanceJobSchedules);
		try {
			sc.save();
			modelAndView.addObject("instanceJobSchedules", instanceJobSchedulesSimpleHash);
			modelAndView.setViewName(LIST_SCHEDULES);
			return modelAndView;
		} catch (IOException e) {
			message = "Failed To Save Configuration!";
			logger.error(message);
		}
		modelAndView.addObject("Exception", message);
		modelAndView.addObject("schedule", schedule);
		return modelAndView;		
	}
	
	
	private boolean isScheduleNameAlreadyExists(String scheduleName) {
		Map<String, InstanceJobSchedule> instanceJobSchedules = ServerConfiguration.getInstanceJobSchedules();
		for(Map.Entry<String, InstanceJobSchedule>  entry : instanceJobSchedules.entrySet()) {
			if(entry.getValue().getScheduleName().equalsIgnoreCase(scheduleName)) {
				return true;
			}
		}
		return false;
	}


	@RequestMapping(value = "/admin/deleteInstanceJob.admin")
	public ModelAndView deleteInstanceJob(HttpServletRequest request, @RequestParam(required = false) String operation) 
			throws ConfigurationException {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("request", request);
		modelAndView.setViewName(LIST_SCHEDULES);
		
		ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
		Scheduler scheduler = WebserverStatic.getScheduler();
		Map<String, InstanceJobSchedule> instanceJobSchedules = ServerConfiguration.getInstanceJobSchedules();
		String message = "", groupName = "Instance Job";
		SimpleHash instanceJobSchedulesSimpleHash = new SimpleHash();
		instanceJobSchedulesSimpleHash.putAll(instanceJobSchedules);
		modelAndView.addObject("instanceJobSchedules", instanceJobSchedulesSimpleHash);
		if(!StringUtils.isBlank(operation)) {
			String scheduleName = operation.toUpperCase();
			for(Map.Entry<String, InstanceJobSchedule>  entry : instanceJobSchedules.entrySet()) {
				if(entry.getValue().getScheduleName().equalsIgnoreCase(scheduleName)) {
					ServerConfiguration.removeInstanceJobSchedule(entry.getValue());
					break;
				}
			}
			try {
				sc.save();
				scheduler.deleteJob(new JobKey(scheduleName, groupName));
			} catch (IOException e) {
				message = "Failed To Delete Configuration!";
				logger.error(message);
			} catch (SchedulerException e) {
				message = "Failed To Delete Schedule!";
				logger.error(message);
			}
			return modelAndView;
		}  		
		return modelAndView;
	}
	
	@RequestMapping(value = "/admin/disableJob.admin")
	public ModelAndView disableJob(HttpServletRequest request, @RequestParam(required = false) String scheduleName) 
			throws ConfigurationException, SchedulerException, IOException {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("request", request);
		modelAndView.setViewName(LIST_SCHEDULES);
		ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
		Scheduler scheduler = WebserverStatic.getScheduler();
		Map<String, InstanceJobSchedule> instanceJobSchedules = ServerConfiguration.getInstanceJobSchedules();
		InstanceJobSchedule instanceJobSchedule = null;
		Trigger trigger = null;
		Schedule schedule = new Schedule();
		String groupName = "Instance Job";
		SimpleHash instanceJobSchedulesSimpleHash = new SimpleHash();
		instanceJobSchedulesSimpleHash.putAll(instanceJobSchedules);
		modelAndView.addObject("instanceJobSchedules", instanceJobSchedulesSimpleHash);
		if(!StringUtils.isBlank(scheduleName)) {
			scheduleName = scheduleName.toUpperCase();
			for(Map.Entry<String, InstanceJobSchedule>  entry : instanceJobSchedules.entrySet()) {
				instanceJobSchedule = entry.getValue();
				schedule = instanceJobSchedule.getSchedule();
				if(instanceJobSchedule.getScheduleName().equalsIgnoreCase(scheduleName)) {
					if(schedule.getIsEnabled()) {
						schedule.setIsEnabled(false);
						scheduler.deleteJob(new JobKey(scheduleName, groupName));						
					}
					instanceJobSchedule.setSchedule(schedule);
					instanceJobSchedule.setScheduleName(scheduleName);
					ServerConfiguration.setInstanceJobSchedule(instanceJobSchedule);
					sc.save();	
					break;
				}
			}					
			return modelAndView;			
		}		
		return modelAndView;
	}
	
	@RequestMapping(value = "/admin/enableJob.admin")
	public ModelAndView enableJob(HttpServletRequest request, @RequestParam(required = false) String scheduleName) 
			throws ConfigurationException, SchedulerException, IOException {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("request", request);
		modelAndView.setViewName(LIST_SCHEDULES);
		ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
		Scheduler scheduler = WebserverStatic.getScheduler();
		Map<String, InstanceJobSchedule> instanceJobSchedules = ServerConfiguration.getInstanceJobSchedules();
		InstanceJobSchedule instanceJobSchedule = null;
		Trigger trigger = null;
		Schedule schedule = new Schedule();
		String groupName = "Instance Job";
		SimpleHash instanceJobSchedulesSimpleHash = new SimpleHash();
		instanceJobSchedulesSimpleHash.putAll(instanceJobSchedules);
		modelAndView.addObject("instanceJobSchedules", instanceJobSchedulesSimpleHash);
		if(!StringUtils.isBlank(scheduleName)) {
			scheduleName = scheduleName.toUpperCase();
			for(Map.Entry<String, InstanceJobSchedule>  entry : instanceJobSchedules.entrySet()) {
				instanceJobSchedule = entry.getValue();
				schedule = instanceJobSchedule.getSchedule();
				if(instanceJobSchedule.getScheduleName().equalsIgnoreCase(scheduleName)) {
					if(!schedule.getIsEnabled()) {
						schedule.setIsEnabled(true);
						boolean isInterval = schedule.getIsInterval();
						if(!isInterval) {
							String cronSettings = schedule.getCronSetting();
							String cronTrigger = "0 "+ cronSettings;
					       	trigger =  newTrigger()
				        		     .withIdentity(scheduleName, groupName)
									 .withSchedule(cronSchedule(cronTrigger).withMisfireHandlingInstructionFireAndProceed())						
									 .build();							
						} else {
							 long  interval = schedule.getInterval();
							 trigger = (SimpleTrigger) newTrigger()
								     .withIdentity(scheduleName, groupName)
									 .withSchedule(simpleSchedule()
									 .withMisfireHandlingInstructionNowWithRemainingCount()
									 .repeatForever()
									 .withIntervalInMilliseconds(interval * 1000 * 60))
									 .startAt(new java.util.Date(System.currentTimeMillis() + 60 * 1000))
									 .build();
						}
						long time = 0;
						String status = "";
						Class nativeForkJob = NativeForkJob.class;
						JobDetail jobDetail = newJob(nativeForkJob).withIdentity(scheduleName, groupName).build();
						jobDetail.getJobDataMap().put(NativeForkJob.PROPERTY_JAVA_COMMAND, 
								instanceJobSchedule.getJobDataString());
						jobDetail.getJobDataMap().put(NativeForkJob.PROPERTY_CLASSPATH, WebserverStatic.getClassPath());
						jobDetail.getJobDataMap().put(NativeForkJob.PROPERTY_DIRECTORY, 
								WebserverStatic.getJobWorkingDirectory());
						jobDetail.getJobDataMap().put(NativeForkJob.PROPERTY_JOB_TYPE, "linkingJob");
						SDLJobListener sdlJobListener = new SDLJobListener(groupName+scheduleName, status, time);
						scheduler.getListenerManager().addJobListener(sdlJobListener, 
								KeyMatcher.keyEquals(new JobKey(scheduleName, groupName)));
						scheduler.scheduleJob(jobDetail, trigger);
						instanceJobSchedule.setSchedule(schedule);
						instanceJobSchedule.setScheduleName(scheduleName);
						ServerConfiguration.setInstanceJobSchedule(instanceJobSchedule);
						sc.save();
					}
					break;
				}
			}						
			return modelAndView;			
		}		
		return modelAndView;
	}
	
	@RequestMapping(value = "/admin/viewInstanceJob.admin")
	public ModelAndView viewInstanceJob(HttpServletRequest request, @RequestParam(required = false) String operation) 
			throws ConfigurationException {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("request", request);
		modelAndView.setViewName(SAVE_INSTANCE_JOB);
		List<String> stringarray = new LinkedList<String>();
		stringarray.add("-1");
		modelAndView.addObject("stringarray", stringarray);		
		String scheduleName = operation;
		Schedule schedule = null;
		Map<String, String> modelMap = new LinkedHashMap<String, String>();
		InstanceJobSchedule instanceJobSchedule = ServerConfiguration.getInstanceJobSchedules().get(scheduleName);
		if(!StringUtils.isBlank(scheduleName) && instanceJobSchedule != null) {
			String jobDataString = instanceJobSchedule.getJobDataString();
			schedule = instanceJobSchedule.getSchedule();
			if(schedule != null && jobDataString != null && scheduleName != null) {		
				modelMap = PageStyleUtil.getModelMap(jobDataString);
			}
		} else {
			scheduleName = null;
		}
		SimpleHash modelMapSimpleHash = new SimpleHash();
		modelMapSimpleHash.putAll(modelMap);
		modelAndView.addObject("modelMap", modelMapSimpleHash);
		modelAndView.addObject("schedule", schedule);
		modelAndView.addObject("scheduleName", scheduleName);
		return modelAndView;
	}
	
	/**  
	 *  This Method returns the constituent selected cron value..
	 */
	private String getCronValue(HttpServletRequest request, String selectAllId,
			String id) {
		String typeValue = U.getText(request.getParameter(selectAllId), "*");
		if (typeValue.equals("*"))
			return typeValue;
		String[] values = request.getParameterValues(id);
		if (values == null)
			return "*";
		if (values.length == 1)
			return values[0];
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < values.length; i++) {
			if (i != 0)
				sb.append(",");
			sb.append(values[i]);
		}
		return sb.toString();
	}
	
}
