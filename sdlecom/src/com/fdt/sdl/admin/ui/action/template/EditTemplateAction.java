package com.fdt.sdl.admin.ui.action.template;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.utility.ActionTools;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;

import com.fdt.sdl.styledesigner.Template;
import com.fdt.sdl.styledesigner.Template.Directory;
import com.fdt.sdl.styledesigner.util.TemplateUtil;
import com.fdt.sdl.util.SecurityUtil;

/**
 * An action that handles the edit of a search template.
 *
 * <p>The action support an <i>action</i> URL parameter. This URL parameter
 * controls what this action class does. The following values are supported:</p>
 * <ul>
 *   <li>list - list, this is the default if no action parameter is specified
 *   <li>save - save the template
 *   <li>deleteFile - delete a template file
 *   <li>addFile - add a template file
 *   <li>uploadFile - upload a file
 *   <li>preview - preview the template
 * </ul>
 *
 */
public class EditTemplateAction extends DispatchAction {

    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.config.action.EditTemplateAction");

    public ActionForward save(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request))return (mapping.findForward("welcome"));

        HttpSession session = request.getSession();

        String indexName = request.getParameter("indexName");
        String templateName = U.getText(request.getParameter("templateName"), "default");
        Template t = TemplateUtil.getTemplate(indexName, templateName);
        t.longname = request.getParameter("longname");
        t.description = request.getParameter("description");
        t.defaultLength = U.getInteger(request.getParameter("defaultLength"));
        TemplateUtil.saveTemplate(indexName, t);

        ActionMessages messages = new ActionMessages();
        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("configuration.save.success"));
        saveMessages(session, messages);

        return ActionTools.goForward(mapping, "continue", new String[] {"indexName="+indexName,"templateName="+templateName});
    }

    public ActionForward deleteFile(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        HttpSession session = request.getSession();

        String indexName = request.getParameter("indexName");
        String templateName = U.getText(request.getParameter("templateName"), "default");
        
        Template t = TemplateUtil.getTemplate(indexName, templateName);
        Directory dir = t.findCurrentDirectory(request.getParameter("directoryName"));

        String fileName = request.getParameter("df");
        File f = TemplateUtil.getTemplateFile(indexName, templateName, dir.toString(), fileName);

        ActionMessages messages = new ActionMessages();
        ActionMessages errors = new ActionMessages();

        if (f.delete()) {
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("action.editTemplate.deleteFile.success", fileName));
            saveMessages(session, messages);
        } else {
            errors.add("error", new ActionMessage("action.editTemplate.deleteFile.error", fileName));
            ActionTools.saveErrors(session, errors);
        }

        return ActionTools.goForward(mapping, "continue", new String[] {"indexName="+indexName,"templateName="+templateName});
    }

    public ActionForward addFile(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        HttpSession session = request.getSession();

        String indexName = request.getParameter("indexName");
        String templateName = U.getText(request.getParameter("templateName"), "default");
        Template t = TemplateUtil.getTemplate(indexName, templateName);
        Directory dir = t.findCurrentDirectory(request.getParameter("directoryName"));

        String fileName = request.getParameter("af");
        File f = TemplateUtil.getTemplateFile(indexName, templateName, dir.toString(), fileName);

        ActionMessages messages = new ActionMessages();
        ActionMessages errors = new ActionMessages();

        try {
            if (f.createNewFile()) {
                messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("action.editTemplate.addFile.success", fileName));
            } else {
                errors.add("fileName", new ActionMessage("errors.templatefile.duplicate", fileName));
            } 
        } catch (IOException e) {
            errors.add("fileName", new ActionMessage("errors.templatefile.invalid", fileName));
        }

        saveMessages(session, messages);
        ActionTools.saveErrors(session, errors);

        return ActionTools.goForward(mapping, "continue", new String[] {"indexName="+indexName,"templateName="+templateName});
    }

    public ActionForward uploadFile(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        HttpSession session = request.getSession();

        String indexName = request.getParameter("indexName");
        String templateName = request.getParameter("templateName");
        Template t = TemplateUtil.getTemplate(indexName, templateName);
        String directoryName = request.getParameter("directoryName");
        Directory dir = t.findCurrentDirectory(request.getParameter("directoryName"));

        String uploadDir = null;
        if (indexName != null) {
            uploadDir = TemplateUtil.getTemplateDirectory(indexName, templateName).getPath();
        }
        if (directoryName != null) {
        	uploadDir = uploadDir + "\\" + directoryName;
        }
        
        // required parameters
        session.setAttribute("uploadDir", uploadDir);
        session.setAttribute("forwardName", request.getParameter("forwardName"));

        // parameters for getting back to the calling page
        session.setAttribute("indexName", indexName);
        session.setAttribute("urlParams", request.getParameter("urlParams"));

        // Set navigation variables
        if (!U.isEmpty(request.getParameter("selectedToptab"))) {
            session.setAttribute("_selectedToptab", request.getParameter("selectedToptab"));
        }
        if (!U.isEmpty(request.getParameter("selectedSubtab"))) {
            session.setAttribute("_selectedSubtab", request.getParameter("selectedSubtab"));   
        }
        if (!U.isEmpty(request.getParameter("jumperAction"))) {
            session.setAttribute("_jumperAction", request.getParameter("jumperAction"));   
        }

        return mapping.findForward("uploadFile");
    }

    public ActionForward preview(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        return ActionTools.goForward(mapping, "preview", new String[] {
                "indexName="+request.getParameter("indexName"),
                "templateName="+U.getText(request.getParameter("templateName"), "default"),
                "fileName="+U.getText(request.getParameter("fileName"), "main.stl"),
                "q="+request.getParameter("q")});
    }

    public ActionForward list(ActionMapping mapping,
                              ActionForm actionForm,
                              HttpServletRequest request,
                              HttpServletResponse response)
                              throws IOException, ServletException {

        HttpSession session = request.getSession();

        String indexName = request.getParameter("indexName");
        session.setAttribute("indexName", indexName);

        String templateName = U.getText(request.getParameter("templateName"), "default");
        Template t = TemplateUtil.getTemplate(indexName, templateName);
        request.setAttribute("template", t);
        Directory dir = t.findCurrentDirectory(request.getParameter("directoryName"));
        request.setAttribute("directoryName", dir==null ? "" : dir.toString());
        request.setAttribute("files", TemplateUtil.getTemplateFiles(indexName, templateName, request.getParameter("directoryName")));

        saveMessages(request, ActionTools.getMessages(session));
        saveErrors(request, ActionTools.getErrors(session));

        return mapping.findForward("velocity");
    }

    protected String getMethodName(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response, String parameter) throws Exception {
        String action = request.getParameter("operation");
        if ("save".equals(action)
                || "deleteFile".equals(action)
                || "addFile".equals(action)
                || "uploadFile".equals(action)
                || "preview".equals(action)) {
            return action;
        } else {
            return "list";
        }
    }

}
