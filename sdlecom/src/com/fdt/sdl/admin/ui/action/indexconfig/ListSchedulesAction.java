package com.fdt.sdl.admin.ui.action.indexconfig;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.Schedule;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.utility.ActionTools;
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
public class ListSchedulesAction extends Action {

    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.config.action.ListSchedulesAction");

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

        String indexName = request.getParameter("indexName");
        request.setAttribute("indexName", indexName);
        String operation = request.getParameter("operation");

        try {
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
            if(dc.getSchedules().size()<=0) {
                return (mapping.findForward("noSchedule"));
            }else {
                if("delete".equals(operation)) {
                    Schedule s = dc.findScheduleById(U.getInt(request.getParameter("scheduleId"),-1));
                    if(s!=null) {
                        s.setIsEnabled(false);
                        SchedulerTool.scheduleIndexingJob(dc, s);
                        dc.getSchedules().remove(s);
                        dc.save();
                    }
                    return ActionTools.goForward(mapping, "listSchedules", new String[] {"indexName="+indexName});
                }
            }
            request.setAttribute("dc", dc);
        } catch (Exception e) {
            errors.add("error", new ActionMessage("configuration.changeschedule.error", e));
        } finally {
            saveErrors(request, errors);
            saveMessages(request, messages);
        }

        // Forward to the velocity page
        return (mapping.findForward("continue"));
    }
}
