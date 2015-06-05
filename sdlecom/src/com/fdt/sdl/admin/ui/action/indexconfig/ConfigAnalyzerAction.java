package com.fdt.sdl.admin.ui.action.indexconfig;

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
import net.javacoding.xsearch.foundation.UserPreference;

import com.fdt.sdl.util.SecurityUtil;

import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

/**
 * An action that handles the advanced settings of a dataset.
 *
 * <p>The action support an <i>action</i> URL parameter. This URL parameter
 * controls what this action class does. The following values are supported:</p>
 * <ul>
 *   <li>save - save
 * </ul>
 *
 * 
 */
public class ConfigAnalyzerAction extends Action {

    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.config.action.ConfigAnalyzerAction");

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
            
            //columns
            ArrayList<Column> columns = dc.getColumns(true);
            ArrayList<Column> al = new ArrayList<Column>();
            for (int i = 0; i < columns.size(); i++) {
                Column c = (Column)columns.get(i);
                if(c!=null && (IndexFieldType.belongsTo(c.getIndexFieldType(), IndexFieldType.TEXT) || c.getIndexFieldType() == IndexFieldType.UN_STORED ||"Keywords"==c.getIndexFieldType())) {
                    //logger.debug("column type="+c.getColumnType());
                    al.add(c);
                }
                
            }    
            session.setAttribute("columns", al);
            
            if ("save".equals(action)) {
                dc.setLanguage(U.getText(request.getParameter("language_global"), dc.getLanguage()));
                dc.setAnalyzerName(U.getText(request.getParameter("analyzerClassName_global"), dc.getAnalyzerName()));                
                for(int i=0; i< al.size(); i++){
                    Column c = (Column)al.get(i);
                    String columnAnalyzerName =request.getParameter("analyzerClassName_"+c.getColumnName());                    
                    if ("default".equals(columnAnalyzerName)) {
                        c.setAnalyzerName(null);
                    }else {
                        c.setAnalyzerName(columnAnalyzerName);
                        if(columnAnalyzerName!=null && columnAnalyzerName.startsWith("net.javacoding.xsearch.indexer.analyzer.CommaSemicolon")) {
                            c.setIndexFieldType("Keywords");
                        }
                    }
                    c.setNeedSynonymsAndStopwords("1".equals(request.getParameter("synonym_stopword_"+c.getColumnName())));
                }
                dc.save();
                UserPreference.setBoolean("configAnalyzer."+indexName, true);
                messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("configuration.save.success"));
            }
            if ("test".equals(action)) {
                if(!U.isEmpty(request.getParameter("testString"))){
                    request.setAttribute("testString", request.getParameter("testString"));
                }
            }
        } catch (NullPointerException e) {
            errors.add("error", new ActionMessage("configuration.load.error", e));
        } catch (IOException e) {
            errors.add("error", new ActionMessage("configuration.changes.error", e));
        } finally {
            UserPreference.save();
            saveErrors(request, errors);
            saveMessages(request, messages);
        }
        // Forward to the velocity page
        return (mapping.findForward("continue"));
    }
}
