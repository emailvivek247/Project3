package com.fdt.sdl.admin.ui.action;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.utility.ActionTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
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
public class AjaxValidationAction extends Action {

    private static Logger logger = LoggerFactory.getLogger(AjaxValidationAction.class);

    // --------------------------------------------------------- Public Methods

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
        HttpSession session = request.getSession();

        String name = request.getParameter("name");
        request.setAttribute("name", name);

        ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
        session.setAttribute("sc", sc);
        if (!validateIndexName(name, sc, false, errors)) {
        	saveErrors(request, errors);
        }
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
