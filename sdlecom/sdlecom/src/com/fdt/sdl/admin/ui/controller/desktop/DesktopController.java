package com.fdt.sdl.admin.ui.controller.desktop;

import static com.fdt.sdl.admin.ui.SDLViewConstants.SDL_DASHBOARD_REPORT;
import static com.fdt.sdl.admin.ui.SDLViewConstants.SDL_SCHEDULE_TASK;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.InstanceJobSchedule;
import net.javacoding.xsearch.config.Schedule;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.core.exception.ConfigurationException;
import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.utility.SchedulerTool;

import org.apache.commons.lang.StringUtils;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.entity.ErrorCode;
import com.fdt.sdl.admin.ui.controller.AbstractBaseSDLController;

@Controller
public class DesktopController extends AbstractBaseSDLController {

	@RequestMapping("/admin/scheduledTasks.admin")
	public ModelAndView getScheduledTasks(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView modelAndView = this.getModelAndView(request, SDL_SCHEDULE_TASK);
		return modelAndView;
	}
	
	@RequestMapping("/admin/searchReport.admin")
	public ModelAndView getDashboardReport(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView modelAndView = this.getModelAndView(request, SDL_DASHBOARD_REPORT);
		return modelAndView;
	}
	
	@RequestMapping(value = "/admin/enableTimers.admin", produces="application/json")
	@ResponseBody
	public List<ErrorCode> enableTimers(HttpServletRequest request) {
		List<ErrorCode> errors = new LinkedList<ErrorCode>();
		ErrorCode error = new ErrorCode();
		errors.add(error);
		try {
			String indexList = this.enableDisableTimers(request, true);
			if (indexList == "") {
				error.setCode("SUCCESS");
			} else {
				error.setCode("ERROR");	
				indexList = StringUtils.removeEnd(indexList, ",");
				error.setDescription(indexList);
			}
		} catch (IOException e) {
			error.setCode("ERROR");
			error.setDescription("IO Exception");	
		} catch (ConfigurationException e) {
			error.setCode("ERROR");
			error.setDescription("Configuration Exception");	
		} catch (SchedulerException e) {
			error.setCode("ERROR");
			error.setDescription("Scheduler Exception");	
		}			
		return errors;
	}
	
	@RequestMapping(value = "/admin/disableTimers.admin", produces="application/json")
	@ResponseBody
	public List<ErrorCode> disableTimers(HttpServletRequest request) {
		List<ErrorCode> errors = new LinkedList<ErrorCode>();
		ErrorCode error = new ErrorCode();
		errors.add(error);
		boolean enableDisableTimerFlag = true;
		request.setAttribute("enableDisableTimerFlag", enableDisableTimerFlag);	
		try {
			String indexList = this.enableDisableTimers(request, false);
			indexList = StringUtils.removeEnd(indexList, ",");
			error.setCode("SUCCESS");
			error.setDescription(indexList);
		} catch (IOException e) {
			error.setCode("ERROR");
			error.setDescription("IO Exception");	
		} catch (ConfigurationException e) {
			error.setCode("ERROR");
			error.setDescription("Configuration Exception");	
		} catch (SchedulerException e) {
			error.setCode("ERROR");
			error.setDescription("Scheduler Exception");	
		}
		
		return errors;
	}
	
	public String enableDisableTimers(HttpServletRequest request, boolean enableDisableTimerFlag) throws IOException,
	ConfigurationException, SchedulerException {
		request.setAttribute("enableDisableTimerFlag", !enableDisableTimerFlag);	
		List<DatasetConfiguration> datasetConfigurations = ServerConfiguration.getDatasetConfigurations();
		List<Schedule> newScheduleList = null;
		String indexListString = "";
		boolean isSave = true;
		for(DatasetConfiguration datasetConfiguration: datasetConfigurations) {
			List<Schedule> scheduleList = datasetConfiguration.getSchedules();
			newScheduleList = new ArrayList<Schedule>();
			for(Schedule schedule : scheduleList) {
				if(enableDisableTimerFlag) {					
					if (!schedule.getIsEnabled()) {
						if(checkForInstanceTimer(datasetConfiguration)) {
							indexListString = indexListString + datasetConfiguration.getName().concat(",");
							isSave = false;
							break;
						}
						schedule.setIsEnabled(true);
						SchedulerTool.scheduleIndexingJob(datasetConfiguration, schedule);					}					
				} else if (schedule.getIsEnabled()){
					schedule.setIsEnabled(false);
					Scheduler scheduler = WebserverStatic.getScheduler();
		            String jobName = schedule.getIndexingMode();
		            String groupName = datasetConfiguration.getName();
		            scheduler.deleteJob(new JobKey(jobName, groupName));					
				}				
				newScheduleList.add(schedule);
			 }
			if(isSave) {
				datasetConfiguration.setSchedules(null);
				datasetConfiguration.setSchedules(newScheduleList);
				datasetConfiguration.save();
			}
		}
		return indexListString;
	}

	private boolean checkForInstanceTimer(DatasetConfiguration datasetConfiguration) {
		InstanceJobSchedule instanceJobSchedule = null;
		String jobDataString = null;
		Map<String, InstanceJobSchedule> instanceJobSchedules = ServerConfiguration.getInstanceJobSchedules();
		for(Map.Entry<String, InstanceJobSchedule>  entry : instanceJobSchedules.entrySet()) {
			instanceJobSchedule = entry.getValue();	
			if(instanceJobSchedule.getSchedule().getIsEnabled()) {
				jobDataString = instanceJobSchedule.getJobDataString().toUpperCase();
				int index = jobDataString.indexOf(datasetConfiguration.getName().toUpperCase());
				if(index > -1) {
					return true;
				}
			}
		}
		return false;
	}
	
}
