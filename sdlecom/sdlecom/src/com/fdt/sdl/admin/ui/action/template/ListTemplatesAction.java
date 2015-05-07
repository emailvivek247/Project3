package com.fdt.sdl.admin.ui.action.template;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.utility.ActionTools;
import net.javacoding.xsearch.utility.FileUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.fdt.sdl.styledesigner.Template;
import com.fdt.sdl.styledesigner.util.TemplateUtil;
import com.fdt.sdl.util.SecurityUtil;

public class ListTemplatesAction extends Action {

    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.config.action.ListTemplatesAction");

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                                 throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request))return (mapping.findForward("welcome"));
    
        HttpSession session = request.getSession();
        ActionMessages errors = new ActionMessages();
        ActionMessages messages = ActionTools.getMessages(session);
    
        String indexName = request.getParameter("indexName");
        session.setAttribute("indexName", indexName);
    
        String operation = request.getParameter("operation");
    
        DatasetConfiguration dc = null;
        try {
            dc = ServerConfiguration.getDatasetConfiguration(indexName);
            session.setAttribute("dc", dc);
    
            if ("save".equals(operation)) {
                String name = request.getParameter("pc");
                String tabletTemplateName = request.getParameter("tablet");
                String mobileTemplateName = request.getParameter("mobile");
                dc.setDefaultTemplateName(name);
                dc.setTabletTemplateName(tabletTemplateName);
                dc.setMobileTemplateName(mobileTemplateName);
                dc.save();
                messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("configuration.save.success"));
            } else if ("preview".equals(operation)) {
                return ActionTools.goForward(mapping, "preview", new String[] {
                        "indexName="+indexName,"templateName="+request.getParameter("templateName"),"q="+request.getParameter("q")});
            } else if ("copy".equals(operation)) {
                String toName = request.getParameter("tn");
                if (ActionTools.validateTemplateName(toName, errors)) {
                    File toDir = TemplateUtil.getTemplateDirectory(indexName, toName);
                    if (!toDir.exists()) {
                        String fromName = request.getParameter("templateName");
                        File fromDir = TemplateUtil.getTemplateDirectory(indexName, fromName);
                        try {
                            FileUtil.copyAll(fromDir, toDir);
                            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("action.listTemplates.copy.success", toName, fromName));
                        } catch (IOException e) {
                            errors.add("error", new ActionMessage("action.createTemplate.create.error", toName+": "+e));
                        }
                    } else {
                        errors.add("error", new ActionMessage("errors.templatename.duplicate", toName));
                    }
                }
            } else if ("delete".equals(operation)) {
                String[] names = request.getParameterValues("templateSelect");
                StringBuffer sb = new StringBuffer();
                if (names != null) {
                    for (int i = 0; i < names.length; i++) {
                        File dir = TemplateUtil.getTemplateDirectory(indexName, names[i]);
                        FileUtil.deleteAll(dir);
                        // Construct the name list for displaying
                        if (i == 0) {
                            sb.append(names[i]);
                        } else {
                            sb.append(", ").append(names[i]);
                        }
                    }
                    messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("action.listTemplates.delete.success", sb.toString()));
                }
            }
        } catch (NullPointerException e) {
            errors.add("error", new ActionMessage("configuration.load.error", e));
        } catch (IOException e) {
            errors.add("error", new ActionMessage("configuration.changes.error", e));
        }
    
        Template[] templates = TemplateUtil.getTemplates(indexName);
        request.setAttribute("templates", templates);
        // Set default template if there's only one template
        if (templates != null && templates.length == 1) {
            dc.setDefaultTemplateName(templates[0].getName());
            dc.setMobileTemplateName(templates[0].getName());
            dc.setTabletTemplateName(templates[0].getName());
              dc.save();
        }
        // Remove default template if there exists no template
        if (templates == null || templates.length == 0) {
            dc.setDefaultTemplateName(null);
            dc.save();
        }
    
        saveErrors(request, errors);
        saveMessages(request, messages);
    
        // Forward to the velocity page
        return (mapping.findForward("continue"));
    }

}
