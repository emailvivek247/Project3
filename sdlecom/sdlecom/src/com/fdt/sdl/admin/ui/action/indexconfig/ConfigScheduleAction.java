package com.fdt.sdl.admin.ui.action.indexconfig;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.Schedule;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.foundation.UserPreference;
import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.utility.SchedulerTool;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.quartz.JobKey;
import org.quartz.Scheduler;

import com.fdt.sdl.util.SecurityUtil;

/**
 * An action that handles the advanced settings of a dataset.
 *
 * <p>The action support an <i>action</i> URL parameter. This URL parameter
 * controls what this action class does. The following values are supported:</p>
 * <ul>
 *   <li>DatasetConfiguration in session attribute as "dc"
 * </ul>
 *
 */
public class ConfigScheduleAction extends Action {

    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.config.action.ConfigScheduleAction");

    // --------------------------------------------------------- Public Methods

    /**
     * Handle server requests.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                                 throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request))return (mapping.findForward("welcome"));

        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
        HttpSession session = request.getSession();

        String indexName = request.getParameter("indexName");
        session.setAttribute("indexName", indexName);
        String action = request.getParameter("operation");
        try {
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
            session.setAttribute("dc", dc);
            int scheduleId = U.getInt(request.getParameter("scheduleId"), 0);
            request.setAttribute("scheduleId", scheduleId);
            if(dc != null) {
                request.setAttribute("schedule", dc.findScheduleById(scheduleId));
            }
            
         	List<DatasetConfiguration> indexConfigurations = ServerConfiguration.getDatasetConfigurations();
           	request.setAttribute("indexConfigs", indexConfigurations);	
            
            boolean schedulerType = "0".equals(request.getParameter("scheduleType"));
            boolean isSchedulerEnabled = "1".equals(request.getParameter("isEnabled"));
            String indexingMode = U.getText(request.getParameter("indexingMode"), "Incremental Indexing");
            String subsriptionUrl = U.getText(request.getParameter("subscriptionUrl"), null);
            if(subsriptionUrl!=null&&!subsriptionUrl.endsWith("/")) {
                subsriptionUrl += "/";
            }
            String cronSettings = null;
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
        	String saveMode = request.getParameter("saveMode");
            if ("save".equals(action) && dc!= null && "C".equals(saveMode)) {
	            Schedule schedule = dc.findScheduleById(scheduleId)==null ? new Schedule() : dc.findScheduleById(scheduleId);
	            if(dc.findScheduleByMode(indexingMode)!=null&&dc.findScheduleByMode(indexingMode).getId()!=schedule.getId()) {
	                errors.add("error", new ActionMessage("configuration.changeschedule.error", "Duplicate schedule for <b>"
	                		+indexingMode+"</b> found! Each Indexing mode can only have one schedule."));
	                return (mapping.findForward("continue"));
	            }
	            //remove previous job schedule, some times duplicated with SchedulerTool.scheduleIndexingJob(dc, s)
	            Scheduler scheduler = WebserverStatic.getScheduler();
	            String jobName = schedule.getIndexingMode();
	            String groupName = dc.getName();
	            scheduler.deleteJob(new JobKey(jobName, groupName));
	            
	            schedule.setIndexingMode(indexingMode);
	            schedule.setInterval(U.getLong(request.getParameter("interval"), schedule.getInterval()));
	            schedule.setIsEnabled(isSchedulerEnabled);
	            schedule.setIsInterval(schedulerType);
	            schedule.setCronSetting(cronSettings);
	            dc.setSubscriptionUrl(subsriptionUrl);
	            dc.addSchedule(schedule);
	            dc.save();
	
	            UserPreference.setBoolean("configSchedule." + indexName, true);
	
	            SchedulerTool.scheduleIndexingJob(dc, schedule);
	            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("configuration.changeschedule.success"));
	            return (mapping.findForward("configure_schedule_success"));
            } else if ("save".equals(action) && dc!= null && "A".equals(saveMode)) {
            	for(DatasetConfiguration indexConfig : indexConfigurations) {
    	            Schedule schedule = indexConfig.findScheduleByMode(indexingMode) == null ? new Schedule() 
    	            	: indexConfig.findScheduleByMode(indexingMode);
    	            schedule.setIndexingMode(indexingMode);
    	            schedule.setInterval(U.getLong(request.getParameter("interval"), schedule.getInterval()));
    	            schedule.setIsEnabled(isSchedulerEnabled);
    	            schedule.setIsInterval(schedulerType);
    	            schedule.setCronSetting(cronSettings);
    	            indexConfig.setSubscriptionUrl(subsriptionUrl);
    	            indexConfig.addSchedule(schedule);
    	            indexConfig.save();
    	            SchedulerTool.scheduleIndexingJob(indexConfig, schedule);
            	}
	            return (mapping.findForward("configure_schedule_success"));
	        } else if ("save".equals(action) && dc!= null && "S".equals(saveMode)) {
	        	String[] indexes = request.getParameterValues("indexes");
	        	for(String selectedIndexName : indexes) {
	        		DatasetConfiguration indexConfig = ServerConfiguration.getDatasetConfiguration(selectedIndexName);
		            Schedule schedule = indexConfig.findScheduleByMode(indexingMode) == null ? new Schedule() 
		            	: indexConfig.findScheduleByMode(indexingMode);
		            schedule.setIndexingMode(indexingMode);
		            schedule.setInterval(U.getLong(request.getParameter("interval"), schedule.getInterval()));
		            schedule.setIsEnabled(isSchedulerEnabled);
		            schedule.setIsInterval(schedulerType);
		            schedule.setCronSetting(cronSettings);
		            indexConfig.setSubscriptionUrl(subsriptionUrl);
		            indexConfig.addSchedule(schedule);
		            indexConfig.save();
		            SchedulerTool.scheduleIndexingJob(indexConfig, schedule);
	        	}
	            return (mapping.findForward("configure_schedule_success"));
	        }
        } catch (Exception e) {
            errors.add("error", new ActionMessage("configuration.changeschedule.error", e));
        } finally {
            UserPreference.save();
            saveErrors(request, errors);
            saveMessages(request, messages);
        }
        // Forward to the velocity page
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
