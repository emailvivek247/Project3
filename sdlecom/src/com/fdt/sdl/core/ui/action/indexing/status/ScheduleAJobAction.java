package com.fdt.sdl.core.ui.action.indexing.status;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
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
 * Schedule any jobs related to the index in the quartz scheduler
 */

public final class ScheduleAJobAction extends Action {
    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.status.action.ScheduleAJobAction");

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ActionMessages errors = new ActionMessages();
        HttpSession session = request.getSession();
        String indexName = request.getParameter("indexName");
        String theCommand = request.getParameter("cmd");
        String theText = U.getText(request.getParameter("text"),"");
        try {
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
            session.setAttribute("dc", dc);

            if (!SecurityUtil.isAllowed(request, dc)) {
                errors.add("error", new ActionMessage("action.operation.security.error", "scheduling"));
                return (mapping.findForward("continue"));
            }

            SchedulerTool.scheduleAJob(dc, theText, "User Initiated:"+ dc.getName(), theCommand);

        } catch (Exception ex) {
            errors.add("error", new ActionMessage("action.reIndexing.error", indexName));
            return (mapping.findForward("continue"));
        } finally {
            saveErrors(request, errors);
        }

        // Forward control to the display velocity page
        return (mapping.findForward("continue"));

    }

}
