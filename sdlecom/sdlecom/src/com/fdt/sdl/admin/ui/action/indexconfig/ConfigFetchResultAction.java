package com.fdt.sdl.admin.ui.action.indexconfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.ConfigurationHistory;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.IndexFieldType;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.config.WorkingQueueDataquery;
import net.javacoding.xsearch.fetch.AbstractFetcher;
import net.javacoding.xsearch.fetch.FetcherManager;
import net.javacoding.xsearch.fetch.FieldType;
import net.javacoding.xsearch.fetch.NumberFieldType;
import net.javacoding.xsearch.fetch.StringFieldType;
import net.javacoding.xsearch.fetch.TimestampFieldType;
import net.javacoding.xsearch.foundation.UserPreference;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.fdt.sdl.util.SecurityUtil;

/**
 * An action that handles the configuration of database connection.
 *
 * <p>The action support an <i>action</i> URL parameter. This URL parameter
 * controls what this action class does. The following values are supported:</p>
 * <ul>
 *   <li>list - list, this is the default if no action parameter is specified
 *   <li>test - test
 *   <li>save - save
 * </ul>
 */
public class ConfigFetchResultAction extends Action {

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                                 throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request))return (mapping.findForward("welcome"));
    
        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
    
        String indexName = request.getParameter("indexName");
        request.setAttribute("indexName", indexName);
    
        String operation = request.getParameter("operation");
    
        try {
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
            request.setAttribute("dc", dc);
            AbstractFetcher fetcher = FetcherManager.load(dc.getFetcherConfiguration().getDir());
            List<FieldType> fieldTypes = fetcher.getFieldTypes(dc.getFetcherConfiguration().getProperties());
            request.setAttribute("fieldTypes", fieldTypes);
            request.setAttribute("fetcher", fetcher);
            WorkingQueueDataquery query = dc.getWorkingQueueDataquery();
            if("save".equals(operation)) {
                if (query == null) {
                    query = new WorkingQueueDataquery();
                    dc.addDataquery(query);
                }
                query.clearColumns();
                ConfigurationHistory ch = ConfigurationHistory.load(dc);
                String[] columnNames = request.getParameterValues("columnName");
                String[] indexFieldTypes = request.getParameterValues("indexFieldType");
                if (columnNames != null) {
                    for (int i = 0; i < columnNames.length; i++) {
                        //retain other old values not set in current page
                        Column c = query.findColumn(columnNames[i]);
                        if(c==null) {
                            c = new Column();
                            c.setColumnName(columnNames[i]);
                        }
                        ch.init(c);
                        c.setColumnIndex(i+1);
                        setColumnTypeByFieldType(c,fieldTypes.get(i));
                        c.setIsPrimaryKey(fieldTypes.get(i).isPrimaryKey());
                        c.setIsModifiedDate(fieldTypes.get(i).isModifiedTime());
                        c.setIndexFieldType(indexFieldTypes[i]);
                        if(IndexFieldType.KEYWORDS == c.getIndexFieldType()) {
                            c.setAnalyzerName("org.apache.lucene.analysis.WhitespaceAnalyzer");
                        }else if(IndexFieldType.KEYWORD_CASE_INSENSITIVE == c.getIndexFieldType()) {
                            c.setAnalyzerName("com.fdt.sdl.core.analyzer.KeywordLowerCaseAnalyzer");
                        }
                        query.addColumn(c);
                    }
                }

                UserPreference.setBoolean("configDataSelect."+dc.getName(),true);
                dc.save();
            }else {
                if(query==null) {
                    //initialize it for the first time
                    query = new WorkingQueueDataquery();
                    dc.addDataquery(query);
                }
                
                ConfigurationHistory ch = ConfigurationHistory.load(dc);
                int i = 1;
                
                List<Column> columns = new ArrayList<Column>();
                for(FieldType ft : fieldTypes) {
                    Column oldColumn = query.findColumn(ft.getName());
                    Column c = oldColumn==null ? new Column() : oldColumn;
                    c.setColumnName(ft.getName());
                    ch.init(c);
                    c.setColumnIndex(i++);
                    setColumnTypeByFieldType(c, ft);
                    c.setIsPrimaryKey(ft.isPrimaryKey());
                    c.setIsModifiedDate(ft.isModifiedTime());
                    if(c.getIndexFieldType()==null) {
                        if(ft.isPrimaryKey()) {
                            c.setIndexFieldType(IndexFieldType.KEYWORD);
                        }else if(ft.isModifiedTime()) {
                            c.setIndexFieldType(IndexFieldType.KEYWORD_DATE_HIERARCHICAL);
                        }else if(ft instanceof NumberFieldType) {
                            c.setIndexFieldType(IndexFieldType.KEYWORD);
                        }else if(ft instanceof TimestampFieldType) {
                            c.setIndexFieldType(IndexFieldType.KEYWORD_DATE_HIERARCHICAL);
                        }else if(ft instanceof StringFieldType) {
                            c.setIndexFieldType(IndexFieldType.TEXT);
                        }else {
                            c.setIndexFieldType(IndexFieldType.TEXT);
                        }
                    }
                    columns.add(c);
                }
                query.clearColumns();
                for(Column c: columns) {
                    query.addColumn(c);
                }
                dc.save();
            }
        } catch (NoClassDefFoundError e) {
            errors.add("error", new ActionMessage("configuration.load.error", e));
        } catch (NullPointerException e) {
            errors.add("error", new ActionMessage("configuration.load.error", e));
        } finally{
            saveErrors(request, errors);
            saveMessages(request, messages);
            UserPreference.save();
        }
        // Forward to the velocity page
        return (mapping.findForward("continue"));
    }

    private void setColumnTypeByFieldType(Column c, FieldType fieldType) {
        if(fieldType instanceof NumberFieldType) {
            c.setColumnType("java.math.BigDecimal");
        }else if(fieldType instanceof TimestampFieldType) {
            c.setColumnType("java.sql.Date");
        }else {
            c.setColumnType("java.lang.String");
        }
    }

}
