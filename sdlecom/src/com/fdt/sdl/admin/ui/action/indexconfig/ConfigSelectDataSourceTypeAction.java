package com.fdt.sdl.admin.ui.action.indexconfig;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.utility.ActionTools;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.fdt.sdl.util.SecurityUtil;

public class ConfigSelectDataSourceTypeAction extends Action {

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                                 throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request))return (mapping.findForward("welcome"));
    
        ActionMessages errors = new ActionMessages();
    
        String indexName = request.getParameter("indexName");
        try {
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
            if(dc.getDataSourceType()==DatasetConfiguration.DATASOURCE_TYPE_FETCHER) {
                return ActionTools.goForward(mapping, "fetcher", new String[] {"indexName="+indexName});
            }else {
                return ActionTools.goForward(mapping, "database", new String[] {"indexName="+indexName});
            }
        } catch (NullPointerException e) {
            errors.add("error", new ActionMessage("configuration.load.error", e));
        }
        return ActionTools.goForward(mapping, "database", new String[] {"indexName="+indexName});
    }

}
