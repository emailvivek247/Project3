package com.fdt.sdl.core.ui.action.indexing.status;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;

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
 * Implementation of <strong>Action</strong> that refresh index readers.
 *
 * 
 */

public final class ShowIndexUsageAction extends Action
{
    private static Logger logger = LoggerFactory.getLogger(ShowIndexUsageAction.class);

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                                 throws IOException, ServletException {
        ActionMessages errors = new ActionMessages();
        HttpSession session = request.getSession();
        String indexName = request.getParameter("indexName");
        try {
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
            if (!SecurityUtil.isAllowed(request, dc)) {
                errors.add("error", new ActionMessage("action.operation.security.error", "status"));
                return (mapping.findForward("continue"));
            }else if (!SecurityUtil.isAdminUser(request)){
            	return (mapping.findForward("welcome"));
            }

            session.setAttribute("dc", dc);
            session.setAttribute("indexName", indexName);

            request.setAttribute("request", request);
        } catch (Exception ex) {
            errors.add("error", new ActionMessage("action.showIndexStatus.index.error",indexName+" is not found"));
            return (mapping.findForward("continue"));
        }finally{
            saveErrors(request,errors);
        }

        // Forward control to the display velocity page
        return (mapping.findForward("continue"));

    }

}
