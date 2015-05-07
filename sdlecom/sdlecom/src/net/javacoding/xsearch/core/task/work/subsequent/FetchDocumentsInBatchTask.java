package net.javacoding.xsearch.core.task.work.subsequent;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.javacoding.xsearch.config.ContentDataquery;
import net.javacoding.xsearch.config.Dataquery;
import net.javacoding.xsearch.config.Parameter;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.component.TextDocument;
import net.javacoding.xsearch.core.task.Scheduler;
import net.javacoding.xsearch.core.task.WorkerTask;
import net.javacoding.xsearch.core.task.work.BaseWorkerTaskImpl;
import net.javacoding.xsearch.core.task.work.util.DocumentSQLTaskHelper;
import net.javacoding.xsearch.core.task.work.util.TaskUtil;
import net.javacoding.xsearch.status.IndexStatus;
import net.javacoding.xsearch.utility.DBTool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetch documents in batched SQL
 * 
 */
public class FetchDocumentsInBatchTask extends BaseWorkerTaskImpl {
    protected static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.core.task.work.FetchDocumentBySQLTask");

    protected transient IndexerContext ic;
    protected TextDocument[] documents;

    public FetchDocumentsInBatchTask(Scheduler sched, int contextId, TextDocument[] docs) {
        super(WorkerTask.WORKERTASK_RETRIEVERTASK, sched);
        this.contextId = contextId;
        this.documents = docs;
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
                int lengthParameters = query.getParameters().size();
                if(lengthParameters!=1){
                	logger.error("Fix This! In batch mode, this query should only have one parameter:\n"+query.toString());
                	return;
                }
                Parameter param = (Parameter)query.getParameters().get(0);

                //get parameters from text documents as objects
                List<Object> paramList = new ArrayList<Object>();
                for (int j = 0; j < documents.length; j++) {
                    String[] vStrings = documents[j].getValues(param.getName());
                    if(vStrings!=null) {
                        for(String s : vStrings) {
                            paramList.add(DBTool.createObject(param.getType(), s));
                        }
                    }else if(!query.getIsSkippingNullParameters()){
                        if(ic.getDatasetConfiguration().getPrimaryKeyColumn()!=null) {
                            String pkName = ic.getDatasetConfiguration().getPrimaryKeyColumn().getColumnName();
                            logger.info("Record with Primary Key "+ pkName
                                    +"="+documents[j].get(pkName)+" has null "+param.getName()+" for batching subsequent query "+contextId);
                        }else {
                            logger.info("Parameter "+param.getName()+" is null! Skipping...");
                        }
                    }
                }
                if(paramList.size()==0)return;
                Object[] parameterValues = paramList.toArray(new Object[paramList.size()]);

                ps = prepareTheStatement(conn,query,parameterValues);
            	//logger.info("Batch "+documents.length+" subsequent query "+this.subsequentQueryIndex+"...");
                rs = ps.executeQuery();

            	//logger.info("processing Batch "+documents.length+" subsequent query "+this.subsequentQueryIndex+" results...");
                DocumentSQLTaskHelper.saveToTextDocuments(documents, rs, query);

            }catch(SQLException sqle){
                logger.warn("SQLException is:"+sqle);
            }finally{
            	TaskUtil.close(rs,ps, conn);
            }
            if(!ic.isStopping()){
                //ic.getScheduler().schedule(new WriteDocumentToIndexTask(scheduler,document));
            	for(int i=0;i<documents.length;i++){
                    ic.getScheduler().schedule(this.contextId+1,documents[i]);
            	}
            }
            ic.p.r(ic, documents);
        } catch (OutOfMemoryError oom) {
        	logger.error(NOTIFY_ADMIN, "Out Of Memory Error", oom);
            IndexStatus.setError(ic.getDatasetConfiguration().getName(),"Indexing is out of memory!");
        }catch(Throwable t){
        	logger.error(NOTIFY_ADMIN, "Error In Execute Method", t);
        	t.printStackTrace();
        }
    }
    public static PreparedStatement prepareTheStatement(Connection conn, Dataquery dq, Object[] values) throws SQLException {
        Parameter param = (Parameter)dq.getParameters().get(0);
        String sql = createBatchSql(dq.getSql(), param, values);
        PreparedStatement ps = conn.prepareStatement(sql);
        if(param.getIsVariableBinding()) {
            for(int p = 0; p< values.length ; p++){
                DBTool.setParameter(ps, p+1, values[p]);
            }
        }
        return ps;
    }
    /*
     * Input a string, replace "in (?)" with "in (?, ?, ...)" or "in ('values','abc')"
     */
    public static String createBatchSql(String sql, Parameter param, Object[] values) {
        StringBuffer sb = new StringBuffer(" in (");
        for(int i=0;i<values.length;i++){
        	if(i!=0){
        		sb.append(",");
        	}
            if(!param.getIsVariableBinding()) {
            	sb.append(DBTool.toValueInSqlString(param.getType(), values[i]));
            }else {
                sb.append("?");
            }
        }
        sb.append(")");
        return inListPattern.matcher(sql).replaceAll(sb.toString());
    }
    public static final String IN_LIST_REGULAR_EXPRESSION = "\\s*in\\s*\\(\\s*\\?\\s*\\)";
    private static int patternMode = Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.CANON_EQ;
    public static Pattern inListPattern = Pattern.compile(IN_LIST_REGULAR_EXPRESSION, patternMode);

	public int getTotalDocumentsDone() {
		return documents.length;
	}

}
