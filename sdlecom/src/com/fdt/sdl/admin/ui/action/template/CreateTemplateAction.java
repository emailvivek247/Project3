package com.fdt.sdl.admin.ui.action.template;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.foundation.UserPreference;
import net.javacoding.xsearch.utility.ActionTools;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.fdt.sdl.styledesigner.Scaffold;
import com.fdt.sdl.styledesigner.ScaffoldManager;
import com.fdt.sdl.styledesigner.Template;
import com.fdt.sdl.styledesigner.util.TemplateUtil;
import com.fdt.sdl.util.SecurityUtil;

/**
 * An action that handles the creation of a search template.
 *
 * <p>The action support an <i>action</i> URL parameter. This URL parameter
 * controls what this action class does. The following values are supported:</p>
 * <ul>
 *   <li>create - create a new template from the example template
 * </ul>
 *
 */
public class CreateTemplateAction extends Action {

    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.config.action.CreateTemplateAction");

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                                 throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request))return (mapping.findForward("welcome"));

        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
        ActionForward forward = mapping.findForward("continue");

        String indexName = request.getParameter("indexName");
        String scaffoldName = U.getText(request.getParameter("scaffoldName"), (String) request.getSession().getAttribute("scaffoldName"));
        request.getSession().setAttribute("scaffoldName", scaffoldName);
        request.getSession().setAttribute("indexName",indexName);

        DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
        Scaffold s = ScaffoldManager.loadScaffold(scaffoldName, dc);
        request.setAttribute("scaffold", s);

        Template[] templates = TemplateUtil.getTemplates(indexName);
        request.setAttribute("templates", s.filterAcceptableTemplates(templates));

        String operation = request.getParameter("operation");
        if ("create".equals(operation)) {
            String templateName = request.getParameter("templateName");
            ScaffoldManager.saveScaffoldValues(s, request);
            if (ActionTools.validateTemplateName(templateName, errors)) {
                File templateDirectory = TemplateUtil.getTemplateDirectory(indexName, templateName);
                if (!templateDirectory.exists()|| U.getBoolean(request.getParameter("overwrite"), "Y", false)||s.isPartial || s.isChart || s.isMap) {
                    try {

                        ScaffoldManager.process(s, templateDirectory, dc, templateName);

                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("action.createTemplate.create.success", templateName));
                        if(!s.isPartial && !s.isChart && !s.isMap) {
                            UserPreference.setBoolean("listScaffolds."+indexName,true);
                        }
                        // Save messages into session to pass to the redirected action
                        saveMessages(request, messages);
                        forward = ActionTools.goForward(mapping, "success", new String[] {"indexName="+indexName});
                    } catch (IOException e) {
                        errors.add("error", new ActionMessage("action.createTemplate.create.error", templateName+": "+e));
                    }
                } else {
                    errors.add("error", new ActionMessage("errors.templatename.duplicate", templateName));
                }
            }
        }

        try {
        	request.setAttribute("dc", ServerConfiguration.getDatasetConfiguration(indexName));
        } catch (Exception e) {
            errors.add("error", new ActionMessage("configuration.load.error", e));
            //throw new ServletException(e);
        }

        saveErrors(request, errors);
        saveMessages(request, messages);

        return forward;
    }

}
