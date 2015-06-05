package com.fdt.sdl.admin.ui.action.searchconfig;

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
 * @
 */
public class ConfigFilterableColumnsAction extends Action {
    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.config.action.ConfigFilterableColumnsAction");

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request))return (mapping.findForward("welcome"));
    
        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
        HttpSession session = request.getSession();
    
        String indexName = U.getText(request.getParameter("indexName"), (String)session.getAttribute("indexName"));
        session.setAttribute("indexName", indexName);
    
        String action = request.getParameter("operation");
    
        try {
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
            session.setAttribute("dc", dc);
    
            ArrayList<Column> al = dc.getColumns(true);
            ArrayList<Column> filterColumnList = new ArrayList<Column>();
            ArrayList<Column> sumColumnList = new ArrayList<Column>();
            for(int i=0; i< al.size(); i++){
                Column c = (Column)al.get(i);
                if(c!=null&&c.canBeFilterable()){
                    filterColumnList.add(c);
                    if(c.getIsNumber()){
                        sumColumnList.add(c);
                    }
                }
            }
            request.setAttribute("columns", filterColumnList);
            request.setAttribute("sumColumns", sumColumnList);
            if ("save".equals(action)) {
                for(int i=0; i< filterColumnList.size(); i++){
                    Column c = (Column)filterColumnList.get(i);
                    c.setDisplayName(U.getText(request.getParameter("name_"+c.getColumnName()), null));
                    c.setIsFilterable(U.getBoolean(request.getParameter("enable_"+c.getColumnName()), "Y", false));
                    c.setFilterDisplayOrder(U.getInt(request.getParameter("filterOrder_"+c.getColumnName()), 0));
                    c.setFilterParentColumnName(U.getText(request.getParameter("parentColumnName_"+c.getColumnName()), null));
                   	c.setSumColumnNames(U.getText(request.getParameter("sumColumnNames_" + c.getColumnName()), "").split(","));
                    c.setSortFilterCountsBy(U.getByte(request.getParameter("sortFilterCountsBy_"+c.getColumnName()), Column.SortFilterCountsByCount));
                    c.setHasMultipleKeywords(U.getBoolean(request.getParameter("hasMultipleKeywords_"+c.getColumnName()), "Y", false));
                    c.setFilterFacetTypeName(U.getText(request.getParameter("facetType_"+c.getColumnName()), null));
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
