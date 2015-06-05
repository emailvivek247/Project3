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
import net.javacoding.xsearch.config.TimeWeight;
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

public class ConfigDateWeightColumnAction extends Action {
    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.config.action.ConfigDateWeightColumnAction");

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

            //get all date columns
            ArrayList<Column> columns = dc.getColumns(true);
            ArrayList<Column> al = new ArrayList<Column>();
            for (int i = 0; i < columns.size(); i++) {
                Column c = (Column)columns.get(i);
                if(c!=null && c.canBeDateWeight()){
                    al.add(c);
                }
            }

            request.setAttribute("columns", al);
            TimeWeight[] timeWeights = dc.getTimeWeights();
            request.setAttribute("timeWeights", timeWeights);
        	StringBuffer sb = new StringBuffer();
        	sb.append("[");
            for(int i=0; i<timeWeights.length;i++){
            	if(i!=0){
                	sb.append(",");
            	}
            	sb.append(timeWeights[i].toJson());
            }
        	sb.append("]");
            request.setAttribute("jsonTimeWeights", sb.toString());
            
            if ("save".equals(action)) {
                dc.setDateWeightColumnName(U.getText(request.getParameter("dateColumn"), null));
                dc.save();//needed when user only set description
                messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("configuration.save.success.need.start"));
            }
    
        } catch (NullPointerException e) {
            errors.add("error", new ActionMessage("configuration.load.error", e));
            e.printStackTrace();
        } finally {
            saveErrors(request, errors);
            saveMessages(request, messages);
        }

        // Forward to the velocity page
        return (mapping.findForward("continue"));
    }
}
