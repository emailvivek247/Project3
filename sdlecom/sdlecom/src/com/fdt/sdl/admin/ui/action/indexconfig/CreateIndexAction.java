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

import com.fdt.sdl.util.SecurityUtil;

/**
 * An action that handles the creation of an index.
 *
 * <p>The action support an <i>action</i> URL parameter. This URL parameter
 * controls what this action class does. The following values are supported:</p>
 * <ul>
 *   <li>list - list, this is the default if no action parameter is specified
 *   <li>create - create an index
 * </ul>
 *
 */
public class CreateIndexAction extends Action {

    private static Logger logger = LoggerFactory.getLogger(CreateIndexAction.class);

     

    /**
     * Handle server requests.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                                 throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request))return (mapping.findForward("welcome"));

        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
        HttpSession session = request.getSession();

        String action = request.getParameter("operation");
        String name = request.getParameter("name");
        String displayName = request.getParameter("displayName");
        String desc = request.getParameter("desc");

        // Cache the changes made by user
        request.setAttribute("name", name);
        request.setAttribute("displayName", displayName);
        request.setAttribute("desc", desc);

        try {
            ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
            session.setAttribute("sc", sc);

            if ("create".equals(action)) {
                // Won't create the directory if it does not exist
                if (validateIndexName(name, sc, false, errors)) {

                    // Create dataset configuration file
                    DatasetConfiguration dc = new DatasetConfiguration(name,
                            sc.getDatasetConfigurationFile(name), sc.getBaseDirectory());

                    dc.setName(name);
                    dc.setDisplayName(displayName);
                    dc.setDescription(desc);
                    dc.setPrefixIndexRootDirectory(true);
                    dc.setIsEmptyQueryMatchAll(false);
                    //dc.setIndexdir("indexes"+File.separator+name);
                    dc.setDataSourceType(U.getInt(request.getParameter("data_source_type"), 0));
                    dc.save();
                    File dir = dc.getIndexDirectoryFile();
                    if(!dir.exists()){dir.mkdirs();}

                    // Save server configuration file if dataset and search
                    // configuration files were created without exceptions
                    sc.syncDatasets();

                    UserPreference.setBoolean("configBasic."+dc.getName(),true);
                    UserPreference.save();

                    // Forward to the Basic page
                    session.setAttribute("dc", dc);
                    session.setAttribute("indexName", name);
                    messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("action.createIndex.create.success", name));
                    
                    return (mapping.findForward("success"));
                }
            }
        } catch (IOException e) {
            errors.add("error", new ActionMessage("configuration.changes.error", e));
        } finally{
            saveErrors(request, errors);
            saveMessages(request, messages);
        }

        // Forward to the velocity page
        return (mapping.findForward("continue"));

    }

    public static boolean validateIndexName(String name, ServerConfiguration sc, boolean createDir, ActionMessages errors) {
        int errId = sc.validateIndexName(name);
        if (errId == 0) {
            return true;
        } else {
            errors.add("name", ActionTools.getMessage(errId, "Name", name));
            return false;
        }
    }

}
