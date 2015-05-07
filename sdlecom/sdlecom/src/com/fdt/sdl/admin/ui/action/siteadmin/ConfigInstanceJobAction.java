package com.fdt.sdl.admin.ui.action.siteadmin;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.InstanceJobSchedule;
import net.javacoding.xsearch.config.Schedule;
import net.javacoding.xsearch.config.ServerConfiguration;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.sdl.styledesigner.util.PageStyleUtil;
import com.fdt.sdl.util.SecurityUtil;

import freemarker.template.SimpleHash;

public class ConfigInstanceJobAction extends Action  {
	 
	public static Logger logger = LoggerFactory.getLogger(ConfigInstanceJobAction.class);
	
	 public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, 
			 HttpServletResponse response) throws IOException, ServletException {
		if (!SecurityUtil.isAdminUser(request)) return (mapping.findForward("welcome"));
		Map<String, InstanceJobSchedule> schedules = ServerConfiguration.getInstanceJobSchedules();
		Map<String, String> modelMap = new LinkedHashMap<String, String>();
		SimpleHash instanceJobSchedules = new SimpleHash();
		InstanceJobSchedule instanceJobSchedule = null;
		String scheduleName = null;
		String jobDataString = null;
		Schedule schedule = null;
		List<String> stringarray = new LinkedList<String>();
		stringarray.add("-1");
		request.setAttribute("stringarray", stringarray);
		for(Map.Entry<String, InstanceJobSchedule>  entry : schedules.entrySet()) {
			instanceJobSchedule = entry.getValue();
			scheduleName = instanceJobSchedule.getScheduleName();
			jobDataString = instanceJobSchedule.getJobDataString();
			schedule = instanceJobSchedule.getSchedule();
			if(schedule != null && jobDataString != null && scheduleName != null) {		
				modelMap = PageStyleUtil.getModelMap(jobDataString);
			}
			instanceJobSchedule.setModelMap(modelMap);
			instanceJobSchedules.put(scheduleName, instanceJobSchedule);
		}
		if(instanceJobSchedules.size() > 0) {
			request.setAttribute("instanceJobSchedules", instanceJobSchedules);
			return (mapping.findForward("listInstanceSchedules"));			 
		} 
		return (mapping.findForward("continue"));			
	 }	
}

