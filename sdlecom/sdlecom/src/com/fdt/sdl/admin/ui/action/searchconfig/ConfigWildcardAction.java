package com.fdt.sdl.admin.ui.action.searchconfig;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
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
 * An action that configure wildcard search or not, wildcard prefix length
 *
 * 
 */
public class ConfigWildcardAction extends Action {
    
    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.config.action.ConfigWildcardAction");

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
    
            if ("save".equals(action)) {
                dc.setIsWildcardAllowed(U.getBoolean(request.getParameter("isWildcardAllowed"), "Y", false));
                dc.setMinWildcardPrefixLength(U.getInt(request.getParameter("minWildcardPrefixLength"), 5));
                if(U.isEmpty(request.getParameter("isWildcardLowercaseNeeded"))) {
                    dc.setIsWildcardLowercaseNeeded(false);
                }else {
                    dc.setIsWildcardLowercaseNeeded(true);
                }
                dc.save();
                messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("configuration.save.success"));
            }
    
        } catch (NullPointerException e) {
            errors.add("error", new ActionMessage("configuration.load.error", e));
        } finally {
            saveErrors(request, errors);
            saveMessages(request, messages);
        }
    
        // Forward to the velocity page
        return (mapping.findForward("continue"));
    }
}
