package com.fdt.sdl.admin.ui.action.searchconfig;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.IndexFieldType;
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
 * @
 */
public class ConfigSortableColumnsAction extends Action {

    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.config.action.ConfigSortableColumnsAction");

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
    
            ArrayList<Column> al = dc.findColumnsByFieldType(IndexFieldType.KEYWORD);
            session.setAttribute("columns", al);
            if ("save".equals(action)) {
                for(int i=0; i< al.size(); i++){
                    Column c = (Column)al.get(i);
                    c.setDisplayName(U.getText(request.getParameter("name_"+c.getColumnName()), null));
                    c.setIsSortable(U.getBoolean(request.getParameter("enable_"+c.getColumnName()), "Y", false));
                    c.setSortDisplayOrder(U.getInt(request.getParameter("sortDisplayOrder_"+c.getColumnName()), 0));
                    c.setIsDescending(U.getBoolean(request.getParameter("sort_"+c.getColumnName()), "desc", true));
                    c.setTag(U.getText(request.getParameter("tag_"+c.getColumnName()), null));
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
