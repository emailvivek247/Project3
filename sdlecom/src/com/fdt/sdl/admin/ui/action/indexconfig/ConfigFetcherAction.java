package com.fdt.sdl.admin.ui.action.indexconfig;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.fetch.FetcherManager;
import net.javacoding.xsearch.foundation.UserPreference;
import net.javacoding.xsearch.utility.U;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.fdt.sdl.util.SecurityUtil;

/**
 * An action that handles the configuration of database connection.
 *
 * <p>The action support an <i>action</i> URL parameter. This URL parameter
 * controls what this action class does. The following values are supported:</p>
 * <ul>
 *   <li>list - list, this is the default if no action parameter is specified
 *   <li>test - test
 *   <li>save - save
 * </ul>
 */
public class ConfigFetcherAction extends Action {

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
            request.setAttribute("dc", dc);
            request.setAttribute("fetchers", FetcherManager.list());
            if("save".equals(operation)) {
                dc.getFetcherConfiguration().setDir(request.getParameter("fetcher_dir"));
                Properties p = dc.getFetcherConfiguration().getProperties();
                for(int i = p.size()+4-1 ; i>=0 ; i--) {
                    dc.getFetcherConfiguration().addPair(U.getText(request.getParameter("property_name_"+i), null), U.getText(request.getParameter("property_value_"+i), null));
                }
                UserPreference.setBoolean("configDataSource."+dc.getName(),true);
                dc.save();
            }
            
        } catch (NullPointerException e) {
            errors.add("error", new ActionMessage("configuration.load.error", e));
        } finally{
            saveErrors(request, errors);
            saveMessages(request, messages);
            UserPreference.save();
        }
        // Forward to the velocity page
        return (mapping.findForward("continue"));
    }

}
