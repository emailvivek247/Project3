package com.fdt.sdl.admin.ui.action.desktop;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.search.searcher.SearcherManager;
import net.javacoding.xsearch.utility.FileUtil;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.sdl.admin.ui.action.indexconfig.CreateIndexAction;
import com.fdt.sdl.core.ui.action.indexing.status.QueryReportAction;
import com.fdt.sdl.styledesigner.util.TemplateUtil;
import com.fdt.sdl.util.SecurityUtil;

public class GoDashboardAction extends Action {

    private static Logger logger = LoggerFactory.getLogger(GoDashboardAction.class);

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                                 throws IOException, ServletException {
       if (!SecurityUtil.isAdminUser(request))return (mapping.findForward("unauthenticated"));
       
        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
        
        ActionMessages actionMessages = (ActionMessages)request.getAttribute("org.apache.struts.action.ACTION_MESSAGE");
        if (actionMessages != null) {
        	messages.add(actionMessages);
        }
        
        HttpSession session = request.getSession();
        QueryReportAction.setProduct(request);
        String action = request.getParameter("act");

        WebserverStatic.setURIFile(request);
        try{
            ServerConfiguration sc = ServerConfiguration.getServerConfiguration();

            if ("delete".equals(action)) {
                String[] names = request.getParameterValues("indexSelect");
                StringBuffer sb = new StringBuffer();
                if (names != null) {  // there're something to delete
                    for (int i = 0; i < names.length; i++) {
                    	sc.deleteSchedulesAssociatedWithIndex(names[i]);
                    	sc.updateInstanceTimerJobs(names[i]);
                    	SearcherManager.destroy(names[i]);
                        sc.deleteDataset(names[i]);
                        
                        // Construct the name list for displaying
                        if (i == 0) {
                            sb.append(names[i]);
                        } else {
                            sb.append(", ").append(names[i]);
                        }
                                                
                    }
                    sc.syncDatasets();
                    messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("action.goDashboard.delete.success", sb.toString()));
                }
            }else if("copy".equals(action)){
                String[] names = request.getParameterValues("indexSelect");
                String fromName = names[0];
                String toName = request.getParameter("indexNameToCopy");
                //Convert to lowercase
                toName = toName.toLowerCase();
                if(CreateIndexAction.validateIndexName(toName, sc, false, errors)) {
                    // Create dataset configuration file
                    DatasetConfiguration dc = sc.getDatasetConfiguration(fromName);
                    dc.setName(toName);
                    //dc.setIndexdir("indexes"+File.separator+toName);
                    dc.setConfigFile(sc.getDatasetConfigurationFile(toName));
                    dc.save();
                    sc.syncDatasets(true);
                    
                    FileUtil.copyAll(TemplateUtil.getTemplateDirectory(fromName), TemplateUtil.getTemplateDirectory(toName));
                }
            }

            // This should come after the delete action
            session.setAttribute("dcs", ServerConfiguration.getDatasetConfigurations());
        } catch (Exception e) {
            errors.add("error", new ActionMessage("action.goDashboard.error", e));
        }

        saveErrors(request, errors);
        saveMessages(request, messages);

        // Forward to the velocity page
        return (mapping.findForward("continue"));
    }

}
