package net.javacoding.xsearch.core.task.work.subsequent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;

import net.javacoding.xsearch.config.ContentDataquery;
import net.javacoding.xsearch.config.Dataquery;
import net.javacoding.xsearch.config.Parameter;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.component.TextDocument;
import net.javacoding.xsearch.core.task.Scheduler;
import net.javacoding.xsearch.core.task.WorkerTask;
import net.javacoding.xsearch.core.task.work.BaseWorkerTaskImpl;
import net.javacoding.xsearch.core.task.work.util.DocumentSQLTaskHelper;
import net.javacoding.xsearch.status.IndexStatus;
import net.javacoding.xsearch.utility.DBTool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchDocumentBySQLTask extends BaseWorkerTaskImpl {
    protected static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.core.task.work.FetchDocumentBySQLTask");

    protected transient IndexerContext ic;
    protected TextDocument document;

    public FetchDocumentBySQLTask(Scheduler sched, int contextId, TextDocument  doc) {
        super(WorkerTask.WORKERTASK_RETRIEVERTASK, sched);
        this.contextId = contextId;
        this.document = doc;
    }

    public void prepare() {
    	super.prepare();
        this.ic 	 = scheduler.getIndexerContext();
        //context.throttle(site);
    }

    public void execute() {
        if(ic.isStopping()) return;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try{
            ContentDataquery query = ic.getDatasetConfiguration().getContentDataqueries().get(this.contextId-1);
            conn = ic.getConnectionProvider().getConnection(query.getDataSourceName());
            try{
                boolean hasEnoughParameters = true;
                int lengthParameters = query.getParameters().size();
                int lengthValues = 0;
                Object[][] values = new Object[lengthParameters][];
                for (int p = 0; p < lengthParameters && hasEnoughParameters; p++) {
                    Parameter param = (Parameter)query.getParameters().get(p);
                    String[] paramValues = document.getValues(param.getName());
                    if (paramValues == null) {
                        hasEnoughParameters = false;
                        if(!query.getIsSkippingNullParameters()) {
                            if(ic.getDatasetConfiguration().getPrimaryKeyColumn()!=null) {
                                String pkName = ic.getDatasetConfiguration().getPrimaryKeyColumn().getColumnName();
                                logger.info("Record with Primary Key "+ pkName +"="+document.get(pkName)+" has null "+param.getName()+" for subsequent query "+contextId);
                            }else {
                                logger.info("Parameter "+param.getName()+" is null! Skipping...");
                            }
                        }
                    } else {
                        List<Object> paramList = new ArrayList<Object>();
                        if (p == 0) {
                            lengthValues = paramValues.length;
                        }
                        for (int j = 0; j < lengthValues && hasEnoughParameters; j++) {
                            Object v = DBTool.createObject(param.getType(), paramValues[j]);
                            if (v == null) {
                                hasEnoughParameters = false;
                                if(!query.getIsSkippingNullParameters()) {
                                    logger.warn("Need Parameter :" + param.getName());
                                }
                            } else {
                                paramList.add(v);
                            }
                        }
                        values[p] = paramList.toArray();
                    }
                }
                if(!hasEnoughParameters){
                    return;
                }

                for (int j = 0; j < lengthValues; j++) {
                    String lookupKey = null;
                    List<List<String>> results = null;
                    if(query.getIsCacheNeeded()){
                        //create cache key and look up in cache
                        StringBuffer sb = new StringBuffer("q").append(this.contextId);
                        for(int p = 0; p< values.length; p++){
                              sb.append("|").append(values[p][j]);
                        }
                        lookupKey = sb.toString();
                        results = (List<List<String>>) ic.lookup(lookupKey);
                        if(results!=null){
                            //logger.debug("cache hit for "+lookupKey);
                            DocumentSQLTaskHelper.saveToTextDocument(query, results, document );
                            continue;
                        }
                    }
                    Object[] params = new Object[lengthParameters];
                    for (int p = 0; p < lengthParameters; p++) {
                        params[p] = values[p][j];
                    }
                    if (rs != null){try{rs.close();}catch (SQLException e){}}
                    if (ps != null){try{ps.close();}catch (SQLException e){}}
                    ps = prepareTheStatement(conn,query,params);
                    rs = ps.executeQuery();
                    // logger.debug(cdqs[i].getSql());
                    if (rs.next()) {
                        // logger.debug("has data");
                        // multi-value parameters are also supported for cached query
                        if (query.getIsCacheNeeded()) {
                            results = new ArrayList<List<String>>();
                            DocumentSQLTaskHelper.saveToListOfArrayList(query, rs, results);
                            DocumentSQLTaskHelper.saveToTextDocument(query, results, document);
                            ic.store(lookupKey, results);
                        } else {
                            // cached results won't work for concatenated columns
                            DocumentSQLTaskHelper.saveToTextDocument(document, rs, query);
                        }
                    }
                }
            }catch(SQLException sqle){
                logger.warn("SQLException is:"+sqle);
            }finally{
                if (rs != null){try{rs.close();}catch (SQLException e){}}
                if (ps != null){try{ps.close();}catch (SQLException e){}}
                if (conn != null){try{ic.getConnectionProvider().closeConnection(conn);}catch (Throwable e){}}
            }
            if(!ic.isStopping()){
                //ic.getScheduler().schedule(new WriteDocumentToIndexTask(scheduler,document));
                ic.getScheduler().schedule(this.contextId+1,document);
            }
            ic.p.r(ic, document);
        } catch (OutOfMemoryError oom) {
        	logger.error(NOTIFY_ADMIN, "Out Of Memory Error", oom);
            IndexStatus.setError(ic.getDatasetConfiguration().getName(), "Indexing is out of memory!");
        } catch(Throwable t) {
        	logger.error("Error In Execute Method", t);
        	t.printStackTrace();
        }
    }
    public static PreparedStatement prepareTheStatement(Connection conn, Dataquery dq, Object[] values) throws SQLException {
        ArrayList<Parameter> parameters = dq.getParameters();
        int lengthParameters = parameters.size();
        String sql = createSql(dq.getSql(), parameters, values);
        PreparedStatement ps = conn.prepareStatement(sql);
        int offset = 0; // number of parameters that's string concatenation before this parameter
        for(int p = 0; p< lengthParameters ; p++){
            Parameter param = (Parameter)parameters.get(p);
            if(param.getIsVariableBinding()) {
                DBTool.setParameter(ps, param.getIndex()-offset, values[p]);
            }else {
                offset++;
            }
        }
        return ps;
    }
    public static String createSql(String sql, ArrayList<Parameter> parameters, Object[] values) {
        boolean needStringReplacement = false;
        for(int p = 0; p< parameters.size() ; p++){
            Parameter param = (Parameter)parameters.get(p);
            if(!param.getIsVariableBinding()) {
                needStringReplacement = true;
                break;
            }
        }
        if(!needStringReplacement) return sql;
        String[] s = sql.split("\\?");
        StringBuffer sb = new StringBuffer(s[0]);
        for(int p = 0; p< parameters.size() ; p++){
            Parameter param = (Parameter)parameters.get(p);
            if(!param.getIsVariableBinding()) {
                sb.append(values[p]);
            }else {
                sb.append("?");
            }
            if(p+1<s.length) {
                sb.append(s[p+1]);
            }
        }
        return sb.toString();
    }

}
