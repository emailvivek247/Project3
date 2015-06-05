package com.fdt.sdl.admin.ui.action.indexconfig;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.ConfigurationHistory;
import net.javacoding.xsearch.config.DataSource;
import net.javacoding.xsearch.config.Dataquery;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.IndexFieldType;
import net.javacoding.xsearch.config.Parameter;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.connection.JdbcToolkit;
import net.javacoding.xsearch.core.task.work.subsequent.FetchDocumentsInBatchTask;
import net.javacoding.xsearch.utility.ActionTools;
import net.javacoding.xsearch.utility.DBTool;
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
 * An action that handles the configuration of dataqueries.
 *
 * <p>The action support an <i>action</i> URL parameter. This URL parameter
 * controls what this action class does. The following values are supported:</p>
 * <ul>
 *   <li>list - list, this is the default if no action parameter is specified
 *   <li>populateParameters - populate the Parameter table
 *       (currently not supported due to database driver version issue)
 *   <li>populateColumns - populate the Column table
 *   <li>test - test
 *   <li>save - save
 * </ul>
 */
public abstract class ConfigDataqueryAction extends Action {

    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.config.action.ConfigDataqueryAction");

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                                 throws Exception {
        if (!SecurityUtil.isAdminUser(request))return (mapping.findForward("welcome"));
    
        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
        HttpSession session = request.getSession();
    
        String indexName = request.getParameter("indexName");
        session.setAttribute("indexName", indexName);
    
        String operation = request.getParameter("operation");
    
        try {
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
            request.setAttribute("dc", dc);
            request.setAttribute("id", U.getInteger(request.getParameter("id")));
            Dataquery query = getDataquery(dc, U.getInt(request.getParameter("id"), 0), request);
            if("create".equals(request.getAttribute("pageMode"))){
                return ActionTools.goForward(mapping, "configContent", new String[] {"indexName="+indexName, "id="+request.getAttribute("id")});
            }
            request.setAttribute("query", query);
    
            if ("populateColumns".equals(operation) || "test".equals(operation) || "save".equals(operation)) {
                query.setSql(request.getParameter("sql"));
                query.setIsCacheNeeded(U.getBoolean(request.getParameter("chooseCache"), "1", false));
                query.setIsBatchNeeded(U.getBoolean(request.getParameter("chooseBatch"), "1", false));
                query.setBatchSize(U.getInt(request.getParameter("batchSize"), 128));
                query.setIsSkippingNullParameters(U.getBoolean(request.getParameter("isSkippingNullParameters"), "1", false));

                // Save parameters
                query.clearParameters();
                String[] paramNames = request.getParameterValues("paramName");
                String[] paramValues = request.getParameterValues("paramValue");
                if (paramNames != null && paramValues != null) {
                    for (int i = 0; i < paramNames.length; i++) {
                        Parameter p = new Parameter();
                        p.setIndex(i+1);
                        p.setName(paramNames[i]);
                        String paramType = dc.findColumn(paramNames[i]).getColumnType();
                        try {
                            p.setValue(paramType, paramValues[i]);
                            p.setIsVariableBinding(U.getBoolean(request.getParameter("paramIsVariableBinding"+(i+1)), "1", false));
                        } catch (Exception e) {
                            String defaultValue = DBTool.getDefaultParamValue(paramType);
                            p.setValue(paramType, defaultValue);
                            errors.add("error", new ActionMessage("action.configDataquery.param.error",
                                    String.valueOf(i+1), paramType, defaultValue));
                        }
                        query.addParameter(p);
                    }
                }
    
                // Save columns
                readColumns(dc,query,request);
    
                if ("populateColumns".equals(operation)) {
                    populateColumns(dc, query, errors);
                    dc.save();
                } else if ("test".equals(operation)) {
                    if (test(dc, query, errors)) {
                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("action.configDataquery.test.success"));
                    }
                } else {
                    if ("1".equals(request.getParameter("notest")) || test(dc, query, errors)) {
                        dc.save();
                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("configuration.save.success"));
                        saveProgress(indexName,true);
                        request.setAttribute("pageMode", "edit");
                    }
                }
            } else if ("delete".equals(operation)) {
                dc.deleteContentDataquery(U.getInt(request.getParameter("id"), 0));
                dc.save();
                return ActionTools.goForward(mapping, "listContents", new String[] {"indexName="+indexName});
            }
        } catch (NullPointerException e) {
            errors.add("error", new ActionMessage("configuration.load.error", e));
            saveProgress(indexName,false);
        } catch (IOException e) {
            errors.add("error", new ActionMessage("configuration.changes.error", e));
            saveProgress(indexName,false);
        } finally {
            saveErrors(request, errors);
            saveMessages(request, messages);
        }
    
        // Forward to the velocity page
        return (mapping.findForward("continue"));
    }
    
    public static void readColumns(DatasetConfiguration dc, Dataquery query, HttpServletRequest request) {
        // Save columns
        query.clearColumns();
        ConfigurationHistory ch = ConfigurationHistory.load(dc);
        String[] columnNames = request.getParameterValues("columnName");
        String[] columnTypes = request.getParameterValues("columnType");
        String[] columnPrecisions = request.getParameterValues("columnPrecision");
        String[] columnScales = request.getParameterValues("columnScale");
        String[] indexFieldTypes = request.getParameterValues("indexFieldType");
        if (columnNames != null && columnTypes != null) {
            for (int i = 0; i < columnNames.length; i++) {
                //retain other old values not set in current page
                Column c = query.findColumn(columnNames[i]);
                if(c==null) {
                    c = new Column();
                    c.setColumnName(columnNames[i]);
                }
                ch.init(c);
                c.setColumnIndex(i+1);
                c.setColumnType(columnTypes[i]);
                c.setColumnPrecision(U.getInt(columnPrecisions[i], 0));
                c.setColumnScale(U.getInt(columnScales[i], 0));
                c.setIsPrimaryKey("1".equals(request.getParameter("primaryKey"+(i+1))));
                c.setIsModifiedDate("1".equals(request.getParameter("modifiedDate"+(i+1))));
                c.setIsAggregate("1".equals(request.getParameter("aggregate"+(i+1))));
                if (indexFieldTypes != null ) {  // for the Content page
                    c.setIndexFieldType(indexFieldTypes[i]);
                }
                if(IndexFieldType.KEYWORDS == c.getIndexFieldType()) {
                    c.setAnalyzerName("org.apache.lucene.analysis.WhitespaceAnalyzer");
                }else if(IndexFieldType.KEYWORD_CASE_INSENSITIVE == c.getIndexFieldType()) {
                    c.setAnalyzerName("com.fdt.sdl.core.analyzer.KeywordLowerCaseAnalyzer");
                }

                query.addColumn(c);
            }
        }
    }

    // ------------------------------------------------------ Protected Methods

    protected void populateColumns(DatasetConfiguration dc, Dataquery query, ActionMessages errors) {
        Connection conn = null;
        try {
            DataSource ds = dc.getDataSource(0);
            JdbcToolkit.registerDriver(ds.getJdbcdriver());
            conn = DriverManager.getConnection(ds.getDbUrl(), ds.getDbUsername(), ds.getDbPassword());
            query.populateColumns(conn,dc);
        } catch (SQLException e) {
            errors.add("error", new ActionMessage("action.configDataquery.test.error", e));
            saveProgress(dc.getName(),false);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    errors.add("error", new ActionMessage("action.configDataquery.test.error", e));
                }
            }
        }
    }

    protected boolean test(DatasetConfiguration dc, Dataquery query, ActionMessages errors) {
        Connection conn = null;
        boolean result = false;
        try {
            if(query.getIsBatchNeeded()) {
                if(!FetchDocumentsInBatchTask.inListPattern.matcher(query.getSql()).find()) {
                    errors.add("error", new ActionMessage("action.configDataquery.test.error", "SQL must be have <b>in ( ? )</b> for batching parameter list to expand to <b>in (?,?,?,?,?,...?)</b>"));
                    saveProgress(dc.getName(),false);
                }else if(query.getParameters().size()!=1) {
                    errors.add("error", new ActionMessage("action.configDataquery.test.error", "Must have one input parameter when Batching Query"));
                    saveProgress(dc.getName(),false);
                }else{
                    String parameterName = query.getParameters().get(0).getName();
                    boolean found = false;
                    for(Column c: query.getColumns()) {
                        if(c.getName().equals(parameterName)) {
                            found = true;
                            break;
                        }
                    }
                    if(!found) {
                        errors.add("error", new ActionMessage("action.configDataquery.test.error", "In Batch mode, the parameter <b>"+parameterName+"</b> must be also selected."));
                        saveProgress(dc.getName(),false);
                        return false;
                    }
                }
            }
            DataSource ds = dc.getDataSource(0);
            if(ds.getJdbcdriver().toLowerCase().indexOf("mysql")>=0) {
                if(query.getSql().trim().endsWith(";")) {
                    errors.add("error", new ActionMessage("action.configDataquery.test.error", "This SQL will be appended with LIMIT ? OFFSET ? and should not end with a semicolon."));
                    saveProgress(dc.getName(),false);
                    return false;
                }
            }
            JdbcToolkit.registerDriver(ds.getJdbcdriver());
            conn = DriverManager.getConnection(ds.getDbUrl(), ds.getDbUsername(), ds.getDbPassword());
            StringBuffer sb = new StringBuffer();
            result = query.test(conn, sb);
            if (!result) {
                errors.add("error", new ActionMessage("action.configDataquery.test.error", sb));
                saveProgress(dc.getName(),false);
            }
        } catch (SQLException e) {
            errors.add("error", new ActionMessage("action.configDataquery.test.error", e));
            saveProgress(dc.getName(),false);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    errors.add("error", new ActionMessage("action.configDataquery.test.error", e));
                }
            }
        }
        return result;
    }

    protected abstract Dataquery getDataquery(DatasetConfiguration dc, int id, HttpServletRequest request);
    protected abstract void saveProgress(String name, boolean val);
}
