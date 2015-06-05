package com.fdt.sdl.admin.ui.action.indexconfig;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
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
 * 
 *
 * TODO To change the template for this generated type comment go to
 * @
 */
public class ConfigSpellCheckAction extends Action {
    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.config.action.ConfigSearchableColumnsAction");

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
            request.setAttribute("dc", dc);
    
            ArrayList<Column> columns = dc.getColumns(true);
            ArrayList<Column> al = new ArrayList<Column>();
            for (Column c : columns) {
                if(c!=null&&c.canBeSpellChecking()) {
                    al.add(c);
                }
            }
    
            request.setAttribute("columns", al);
            if ("save".equals(action)) {
                dc.setIsSpellChecking(U.getBoolean(request.getParameter("isSpellChecking"), "on", false));
                for(Column c : al){
                    c.setDisplayName(U.getText(request.getParameter("name_"+c.getColumnName()), null));
                    c.setIsSpellChecking(U.getBoolean(request.getParameter("spell_checking_"+c.getColumnName()), "Y", false));
                }
                dc.save();//needed when user only set description
                messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("configuration.save.success"));
            }
    
        } catch (NullPointerException e) {
            errors.add("error", new ActionMessage("configuration.load.error", e));
        } finally {
            saveErrors(request, errors);
            saveMessages(request, messages);
        }
    
        // Forward to the velocity page
        return (mapping.findForward("continue"));
    }
}
