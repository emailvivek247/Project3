package com.fdt.sdl.admin.ui.action.indexconfig;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.DeletionDataquery;
import net.javacoding.xsearch.config.IncrementalDataquery;
import net.javacoding.xsearch.config.ServerConfiguration;
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
 * An action that handles the advanced settings of sql queries.
 */
public class AdvancedQueriesAction extends Action {

    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.config.action.AdvancedQueriesAction");

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                                 throws IOException, ServletException {
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
    
            if ("save".equals(action)) {
                ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
                if(sc.getAllowedLicenseLevel()<=0) {
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("limit.license", "Standard"));
                } else {
                    String primaryKeyListSql = U.getText(request.getParameter("primaryKeyListSql"),null);
                    if(primaryKeyListSql!=null) {
                        DeletionDataquery dSql = new DeletionDataquery();
                        dSql.setSql(primaryKeyListSql);
                        dSql.setIsDeleteOnly(U.getBoolean(request.getParameter("isSoftDeletion"), "on", false));
                        dc.addDataquery(dSql);
                    } else {
                        dc.setDeletionQuery(null);
                    }
                    
                    String incrementalSql = U.getText(request.getParameter("incrementalSql"),null);
                    if(incrementalSql!=null) {
                        IncrementalDataquery dSql = new IncrementalDataquery();
                        dSql.setSql(incrementalSql);
                        dc.addDataquery(dSql);
                    }else {
                        dc.setIncrementalDataquery(null);
                    }
                    dc.save();
                    messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("configuration.save.success"));
                }
            }
        } catch (NullPointerException e) {
            errors.add("error", new ActionMessage("configuration.load.error", e));
        } catch (IOException e) {
            errors.add("error", new ActionMessage("configuration.changes.error", e));
        } finally {
            saveErrors(request, errors);
            saveMessages(request, messages);
        }
        return (mapping.findForward("continue"));
    }
}
