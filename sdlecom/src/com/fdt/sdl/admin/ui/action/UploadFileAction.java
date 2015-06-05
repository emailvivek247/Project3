package com.fdt.sdl.admin.ui.action;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.utility.ActionTools;
import net.javacoding.xsearch.utility.FileUtil;

import com.fdt.sdl.util.SecurityUtil;

import net.javacoding.xsearch.utility.U;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

/**
 * An action that handles file upload.
 *
 * <p>The file upload page (module) is designed to be reusable. To enable file
 * upload on your page (typicall through an Upload submit button), pass the
 * following session attibutes to the file upload page. See editTemplat.vm as
 * an example.</p>
 * <ul>
 *   <li>uploadDir - the complete path of the directory where files are
 *       uploaded (eg. "/tmp/upload")</li>
 *   <li>forwardName - the Struts forward mapping to the calling page. It can
 *       be defined under action "uploadFile.do", and its redirect attribute
 *       should be set to true. A restriction is that the forward name has to
 *       be same as the corresponding action name. For example, if the action
 *       path is "/editTemplate.do", then the forward name should be
 *       "editTemplate"</li>
 *   <li>indexName - name of the current index</li>
 *   <li>urlParams - url parameters needed for going back to the calling page (
 *       eg. "param1=value1&param2=value2")</li>
 *   <li>selectedToptab - for navigation
 *   <li>selectedSubtab - for navigation
 *   <li>jumperAction - for navigation
 * </ul>
 * <p>In order to display messages passed from the upload file page, in the
 * calling page's Action class, variables <code>errors</code> and <code>
 * message</code> should be created in the following way:<br />
 * <pre>
 *     ActionMessages messages = ActionTools.getMessages(session));
 *     ActionMessages errors = ActionTools.getErrors(session));
 * </pre>
 *
 */
public class UploadFileAction extends Action {

    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.foundation.action.UploadFilesAction");

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                                 throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request))return (mapping.findForward("welcome"));

        HttpSession session = request.getSession();
        // Go to Dashboard if the return page is not set
        if (U.isEmpty((String)session.getAttribute("forwardName"))
                || U.isEmpty((String)session.getAttribute("uploadDir"))) {
            return mapping.findForward("welcome");
        }

        ActionMessages messages = new ActionMessages();
        ActionMessages errors = new ActionMessages();

        if (FileUpload.isMultipartContent(request)) {
            try {
                DiskFileUpload upload = new DiskFileUpload();
                upload.setSizeMax(10*1024*1024);
                List items = upload.parseRequest(request);
                Iterator iter = items.iterator();
                File uploadedFile = null;
                File uploadDir = new File((String)session.getAttribute("uploadDir"));
                while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();
                    if (item.isFormField()) {
                        String fieldName = item.getFieldName();
                        String fileRename = item.getString();
                        if (fieldName.startsWith("rename") && !U.isEmpty(fileRename) && uploadedFile != null) {
                            uploadedFile.renameTo(FileUtil.resolveFile(uploadDir, fileRename));
                        }
                    } else {
                        String fileName = (new File(item.getName())).getName();
                        uploadedFile = FileUtil.resolveFile(uploadDir, fileName);
                        item.write(uploadedFile);
                    }
                }

                messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("action.uploadFiles.success"));
                saveMessages(session, messages);
    
                ActionForward forward = ActionTools.goForward(mapping,
                        (String)session.getAttribute("forwardName"), new String[] {
                        "indexName="+(String)session.getAttribute("indexName"),
                        (String)session.getAttribute("urlParams")});
    
                // Remove page-scope session attributes before returning to the calling page
                session.removeAttribute("uploadDir");
                session.removeAttribute("forwardName");
                // Keep indexName in the session
                session.removeAttribute("urlParams");
                session.removeAttribute("_selectedToptab");
                session.removeAttribute("_selectedSubtab");
                session.removeAttribute("_jumperAction");
    
                return forward;
            } catch (Exception e) {
                errors.add("error", new ActionMessage("action.uploadFiles.error"));
                saveErrors(request, errors);
                return mapping.findForward("continue");
            }
        } else {  // from other page
            //
        }

        return mapping.findForward("continue");
    }
}
