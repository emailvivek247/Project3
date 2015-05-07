package com.fdt.sdl.admin.ui.action.indexconfig;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.foundation.UserPreference;
import net.javacoding.xsearch.search.searcher.SearcherManager;
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

import com.fdt.sdl.util.SecurityUtil;

/**
 * An action that handles the configuration of.
 * <p>
 * The action support an <i>action </i> URL parameter. This URL parameter controls what this action class does. The following values are supported:
 * </p>
 * <ul>
 * <li>list - list, this is the default if no action parameter is specified
 * <li>save
 * </ul>
 * 
 */
public class ConfigBasicAction extends Action {

    private static Logger logger = LoggerFactory.getLogger(ConfigBasicAction.class);

    // --------------------------------------------------------- Public Methods

    /**
     * Handle server requests.
     * 
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
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

            /**
             * always automatically create new directory if not exists if directory is changed, checkbox option: move old files to new directory
             */

            if ("save".equals(action)) {
                dc.setDisplayName(request.getParameter("displayName"));
                dc.setDisplayOrder(U.getInt(request.getParameter("displayOrder"),0));
                dc.setDescription(request.getParameter("desc"));
               	dc.setPrefixIndexRootDirectory(request.getParameter("prefixIndexRootDirectory") != null ? true :false);
                if (!U.isEmpty(request.getParameter("dir"))) {
                    if (!request.getParameter("dir").equals(dc.getIndexdir())) {
                        String oldValue = dc.getIndexdir();
                        File oldDir = dc.getIndexDirectoryFile();
                        dc.setIndexdir(request.getParameter("dir"));
                        File newDir = dc.getIndexDirectoryFile();
                        logger.debug("old directory:" + oldDir);
                        logger.debug("new directory:" + newDir);
                        if (!newDir.exists()) {
                            newDir.mkdirs();
                        }
                        boolean ret = true;
                        if (oldDir!=null&&oldDir.exists() && newDir!=null&&newDir.exists() && "1".equals(request.getParameter("moveDir"))) {
                            ret = FileUtil.deleteAllFiles(newDir);
                            if (ret) ret = FileUtil.moveAllFiles(oldDir, newDir);
                            if (ret) ret = FileUtil.deleteAllFiles(oldDir);
                        }
                        if (oldDir!=null&&oldDir.exists() && newDir!=null&&newDir.exists() && "1".equals(request.getParameter("deleteDir"))) {
                            if (ret) ret = oldDir.delete();
                        }
                        if (ret) {
                            dc.save();//needed because SearcherManager.init read from disk again
                            SearcherManager.destroy(dc.getName());
                            SearcherManager.init(dc);
                        } else {
                            dc.setIndexdir(oldValue);
                            errors.add("error", new ActionMessage("configuration.save.error", "Failed to move " + oldDir + " to " + newDir));
                            // Forward to the velocity page
                            return (mapping.findForward("continue"));
                        }
                    }
                }
                dc.save();//needed when user only set description
                UserPreference.setBoolean("configBasic."+dc.getName(),true);
                UserPreference.save();
                messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("configuration.save.success"));
            }
            // Always validate the index directory
            if(!checkDirectory(dc.getIndexDirectory(), errors)){
                UserPreference.setBoolean("configBasic."+dc.getName(),false);
                UserPreference.save();
            }
        } catch (NullPointerException e) {
            errors.add("error", new ActionMessage("configuration.load.error", e));
            logger.error(U.getStatckTrace(e));
        } finally {
            saveErrors(request, errors);
            saveMessages(request, messages);
        }

        // Forward to the velocity page
        return (mapping.findForward("continue"));
    }

    private boolean checkDirectory(String theDir, ActionMessages errors) {
        if(theDir==null) {
            errors.add("dir", new ActionMessage("errors.indexdir.noexist"));
            return false;
        }
        File dir = new File(theDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!dir.exists()) {
            errors.add("dir", new ActionMessage("errors.indexdir.noexist"));
            return false;
        }
        return true;
    }
}
