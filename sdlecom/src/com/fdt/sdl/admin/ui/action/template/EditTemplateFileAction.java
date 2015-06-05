package com.fdt.sdl.admin.ui.action.template;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.utility.FileUtil;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.fdt.sdl.styledesigner.util.TemplateUtil;
import com.fdt.sdl.util.SecurityUtil;

/**
 * An action that handles the edit of a search template file.
 *
 * <p>The action support an <i>action</i> URL parameter. This URL parameter
 * controls what this action class does. The following values are supported:</p>
 * <ul>
 *   <li>list - list, this is the default if no action parameter is specified
 *   <li>save
 * </ul>
 *
 */
public class EditTemplateFileAction extends Action {

    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.config.action.EditTemplateFileAction");

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                                 throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request))return (mapping.findForward("welcome"));

        ActionForward forward = mapping.findForward("continue");
        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
        HttpSession session = request.getSession();

        String indexName = request.getParameter("indexName");
        session.setAttribute("indexName", indexName);

        String templateName = U.getText(request.getParameter("templateName"), "default");
        request.setAttribute("template", TemplateUtil.getTemplate(indexName, templateName));

        String directoryName = U.getText(request.getParameter("directoryName"), null);
        request.setAttribute("directoryName", directoryName);

        String fileName = U.getText(request.getParameter("fileName"), "main.vm");
        request.setAttribute("fileName", fileName);

        File f = TemplateUtil.getTemplateFile(indexName, templateName, directoryName, fileName);
        request.setAttribute("templateFile", f);

        try {
            session.setAttribute("dc", ServerConfiguration.getDatasetConfiguration(indexName));
        } catch (Exception e) {
            errors.add("error", new ActionMessage("configuration.load.error", e));
            //throw new ServletException(e);
        }

        String action = request.getParameter("operation");
        if ("save".equals(action)) {
            request.setAttribute("templateText", request.getParameter("templateText"));
            if (f.canWrite()) {
                try {
                    FileUtil.writeFile(f, request.getParameter("templateText"), "UTF-8");
                    messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("configuration.save.success"));
                } catch (IOException e) {
                    errors.add("error", new ActionMessage("action.editTemplateFile.error", fileName, e));
                }
            } else {
                errors.add("error", new ActionMessage("action.editTemplateFile.readonly", fileName));
            }
        } else {
            request.setAttribute("templateText", FileUtil.readFile(f, "UTF-8"));
        }

        saveErrors(request, errors);
        saveMessages(request, messages);

        return forward;
    }

}
