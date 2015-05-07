package com.fdt.sdl.core.ui.action.search;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.ServerConfiguration;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

/**
 * If request.getAttribute("site") is not null, 
 */
public class CompositeTemplateAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
        String ACTION_FORWARD = "continue";
        
        try{
            ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
            // This should come after the delete action
            request.setAttribute("dcs", ServerConfiguration.getDatasetConfigurations(false));
            if (ServerConfiguration.getDatasetConfigurations(false).isEmpty()) {
            	ACTION_FORWARD = "emptyIndex";
            }
            String indexName = request.getParameter("indexName");
            request.setAttribute("indexName", indexName);
        } catch (Exception e) {
            errors.add("error", new ActionMessage("action.goDashboard.error", e));
        }

        saveErrors(request, errors);
        saveMessages(request, messages);

        return mapping.findForward(ACTION_FORWARD);

    }

}
