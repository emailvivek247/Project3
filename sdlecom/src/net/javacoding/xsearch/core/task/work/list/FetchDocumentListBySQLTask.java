package net.javacoding.xsearch.core.task.work.list;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.WeakHashMap;

import net.javacoding.queue.DiskBackedQueue;
import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.IncrementalDataquery;
import net.javacoding.xsearch.config.WorkingQueueDataquery;
import net.javacoding.xsearch.core.DirectorySizeChecker;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.PeriodEntry;
import net.javacoding.xsearch.core.PeriodTable;
import net.javacoding.xsearch.core.component.TextDocument;
import net.javacoding.xsearch.core.task.WorkerTask;
import net.javacoding.xsearch.core.task.work.BaseWorkerTaskImpl;
import net.javacoding.xsearch.core.task.work.util.DocumentSQLTaskHelper;
import net.javacoding.xsearch.core.task.work.util.TaskUtil;
import net.javacoding.xsearch.status.IndexStatus;
import net.javacoding.xsearch.utility.DBTool;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 
 */
public class FetchDocumentListBySQLTask extends BaseWorkerTaskImpl {
    protected Logger                logger               = LoggerFactory.getLogger(this.getClass().getName());

    protected IndexerContext        ic;
    protected WorkingQueueDataquery docListQuery;
    private   Column                primaryKeyColumn     = null;
    private   Column                modifiedDateColumn   = null;

    /*
     * remember indexed document period for this run
     */
    protected PeriodEntry           pe;                                                                
    protected boolean               hasPreviouslyIndexed = false;

    protected boolean               hasExistingIndex     = false;

    protected long                  last_report_time     = 0;
    protected DirectorySizeChecker  directorySizeChecker = null;
    protected int                   count                = 0;

    // For testing whether more rows to be fetched for indexing
    // currently only used by MySqlFetchDocumentListBySQLTask
    protected int                   processedRowCount    = 0;

    protected long                  license_end_time     = Long.MAX_VALUE;

    public FetchDocumentListBySQLTask(IndexerContext ic) {
        super(WorkerTask.WORKERTASK_RETRIEVERTASK, ic.getScheduler());
        this.ic = ic;
        this.docListQuery = ic.getDatasetConfiguration().getWorkingQueueDataquery();
        this.primaryKeyColumn = docListQuery.getPrimaryKeyColumn();
        this.modifiedDateColumn = docListQuery.getModifiedDateColumn();
        this.pe = new PeriodEntry();
        this.directorySizeChecker = new DirectorySizeChecker(ic.getDatasetConfiguration());
        this.count = 0;
    }

    public void prepare() {
        if(!ic.getIsRecreate()){
            hasExistingIndex = IndexStatus.countIndexSize(IndexStatus.findActiveMainDirectoryFile(ic.getDatasetConfiguration()))>0;
        }
    }
    
    public PreparedStatement prepareStatement(Connection conn) throws SQLException, IOException {
        PreparedStatement ps = null;
        if(!ic.isFullIndexing&&!ic.getIsRecreate()) {
            //this is only incremental indexing
            IncrementalDataquery incrementalSql = ic.getDatasetConfiguration().getIncrementalDataquery();
            if(incrementalSql!=null) {
                if(ic.getPeriodTable()==null||modifiedDateColumn==null) {
                    logger.info("This is incremental indexing, but no latest modified time is found. So skipping incremental sql");
                } else {
                    PeriodTable pt = IndexStatus.createPeriodTableIfNeeded(ic.getDatasetConfiguration());
                    ps = conn.prepareStatement(incrementalSql.getSql(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    Timestamp ts = new Timestamp(pt.getLatest());
                    incrementalSql.setTimestamp(ps, ts);
                    logger.info(incrementalSql.getSql());
                    logger.info("parameter: "+ts);
                    ic.setIsIncrementalSql(true);
                }
            }
        }
        if(ps==null) {
            ps = conn.prepareStatement(docListQuery.getSql(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            logger.info(docListQuery.getSql());
        }
        return ps;
    }

    /**
     * 1. Check directory size every 1 minute 
     * 2. check if modified doc is already within indexed docs, 
     * by checking period tables 
     * The docs doesn't need to be ordered 
     * The task will wait for document fetcher tasks if they are over loaded 
     * The task permanently save indexed period for this round of indexing in the end.
     *
     * 
     */
    public void execute() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if (ic.isStopping()) return;
            logger.info("Getting the connection...");
            conn = ic.getConnectionProvider().getConnection();
            logger.info("Connected.");
            logger.info("Preparing the statement...");
            ps = prepareStatement(conn);
            try{
                if(ic.getDatasetConfiguration().getListFetchSize()>0) {
                    ps.setFetchSize(ic.getDatasetConfiguration().getListFetchSize());
                    logger.info("Set Fetch Size to "+ic.getDatasetConfiguration().getListFetchSize());
                }
                logger.info("Started executing the Query");
                long startTime = System.currentTimeMillis();
                rs = ps.executeQuery();
                long endTime = System.currentTimeMillis();
                logger.info("Finished executing the Query");
                logger.info("Time taken to execute the query is=" + (endTime - startTime) + " : ms");
            }catch(OutOfMemoryError e){
                try{
                    ps.setFetchSize(0);
                    logger.warn("Too many rows causing OutOfMemory Error. Adjust Fetch Size to "+0);
                    rs = ps.executeQuery();
                }catch(OutOfMemoryError oome){
                    ps.setFetchSize(Integer.MIN_VALUE);
                    logger.warn("Adjust Fetch Size to "+Integer.MIN_VALUE);
                    rs = ps.executeQuery();
                }
            }catch(SQLException e){
            	logger.error(NOTIFY_ADMIN, "SQL Exception:" + e.getMessage());
            }
            if(rs==null) {
                logger.warn("Empty document List!");
                return;
            }
            processResults(rs);
            logger.info(count + " documents to index");
        } catch (OutOfMemoryError oome) {
            logger.error("Out Of Memory Error During Main Query:" + oome);
            if(ic.getIsRecreate()){
                ic.isDataComplete = false;
            }
            ic.setStopping();
        } catch (Throwable t) {
        	logger.error(NOTIFY_ADMIN, "Error During Executing Main Query:" + t + ":" + t.getMessage(), t);
            if(ic.getIsRecreate()){
                ic.isDataComplete = false;
            }
            ic.setStopping();
        } finally {
            TaskUtil.close(rs);
            TaskUtil.close(ps);
            close(conn);
            logger.info("Scheduled Period to index:" + pe);
            // add indexed period to the total indexed period table
            ic.getPeriodTable().add(this.pe);
            logger.info("Full Index Period:" + ic.getPeriodTable());
        }
    }

    static Object flag = new Object();
    private boolean isDocumentIndexingNeeded(ResultSet rs) throws java.sql.SQLException {
        //logger.debug("##getIsRecreate is "+ ic.getIsRecreate());
        //do this before checking existing saved periodTable
        if(ic.getIsRecreate()) return true;

        //logger.debug("##in isDocumentIndexingNeeded");
        if(primaryKeyColumn==null) return true;
        String pkValue = DBTool.getString(rs, primaryKeyColumn);
        if(ic.getIsIncrementalSql()) {
            addPossibleDuplicatePrimaryKey(pkValue);
        }else {
            //logger.debug("##pkValue is "+ pkValue);
            if(pkLookup(pkValue)!=null){
                return false;
            }
            //logger.debug("##mainIndexSize is "+ mainIndexSize);
            if(!hasExistingIndex) {
                pkStore(pkValue, flag);
                return true;
            }

            long currentArticleDate = (modifiedDateColumn==null?0:DBTool.getLong(rs, modifiedDateColumn.getColumnIndex(), modifiedDateColumn.getColumnType()));
            //logger.debug("##currentArticleDate is "+ currentArticleDate);
            //logger.debug("##getPeriodTable is "+ ic.getPeriodTable().getEarliest() +"~"+ic.getPeriodTable().getLatest());
            // if it's already indexed
            if(modifiedDateColumn!=null){
                if (ic.getPeriodTable().contains(currentArticleDate)) {
                    hasPreviouslyIndexed = true;
                    return false;
                }
            }

            addPossibleDuplicatePrimaryKey(pkValue);
            pkStore(pkValue, flag);
        }
        return true;
    }
    
    /** The internal cache. **/
    final WeakHashMap pkCache = new WeakHashMap();
    /** See if an object is in the cache. */
    private Object pkLookup (Object name) {
      synchronized (this) {
        return pkCache.get (name);
      }
    }
    /** Put an object into the cache. */
    private Object pkStore (Object name, Object value) {
      synchronized (this) {
          return pkCache.put (name, value);
      }
    }
    protected void checkListSize(){
        long now = System.currentTimeMillis();
        if ((now - last_report_time) > 10000) {
            // check if ( current size/written doc # * list doc#) >> size limit
            long write_now_count = ic.getScheduler().getWriterJobsDone();
            if (write_now_count > 100) {
                long current_size = directorySizeChecker.getIndexDirectorySize();
                long temp_size = directorySizeChecker.getTemporaryIndexDirectorySize();
                if (!ic.getIsRecreate() && temp_size * (count-write_now_count) > write_now_count * 6 * (ic.getDatasetConfiguration().getIndexMaxSize() * 1024 * 1024 - current_size)) {
                    logger.debug("Max Index Size = "+ic.getDatasetConfiguration().getIndexMaxSize() * 1024 * 1024+" Bytes");
                    logger.debug("Existing Records Size = "+current_size);
                    logger.debug("     New Records Size = "+temp_size);
                    logger.debug("   New Records Number = "+write_now_count);
                    logger.debug("Estimated Records List to Retrieve = "+(ic.getDatasetConfiguration().getIndexMaxSize() * 1024 * 1024 - current_size) * write_now_count / (temp_size>0?temp_size:1));
                    logger.debug("Scheduled Records List to Retrieve = "+count);
                    logger.debug("Current list is supposed to be large enough.");
                    ic.setStopping();
                }else if (ic.getIsRecreate() && temp_size * count > write_now_count * 6 * ic.getDatasetConfiguration().getIndexMaxSize() * 1024 * 1024 ) {
                    logger.debug("Recreating Indexing mode");
                    logger.debug("Max Index Size = "+ic.getDatasetConfiguration().getIndexMaxSize() * 1024 * 1024+" Bytes");
                    logger.debug("Existing Records Size = "+current_size);
                    logger.debug("     New Records Size = "+temp_size);
                    logger.debug("   New Records Number = "+write_now_count);
                    logger.debug("Estimated Records List to Retrieve = "+ ic.getDatasetConfiguration().getIndexMaxSize() * 1024 * 1024 * write_now_count / (temp_size>0?temp_size:1));
                    logger.debug("Scheduled Records List to Retrieve = "+count);
                    logger.debug("Current list is supposed to be large enough.");
                }
            }
            last_report_time = now;
        }
    }
    protected void close(Connection conn){
        if (conn != null) {
            try {
                ic.getConnectionProvider().closeConnection(conn);
                conn = null;
            } catch (Throwable e) {}
        }
    }
    protected boolean processResults(ResultSet rs) throws SQLException{
        while (!ic.isStopping() && rs!=null && (!hasPreviouslyIndexed || ic.isFullIndexing)) {
            if(rs.next()) {
            	/** commented as a part of license check **/
                //checkListSize();
                if (ic.isStopping()) return false;
                if (!hasExistingIndex || isDocumentIndexingNeeded(rs)) {
                	/** commented as a part of license check **/
                    /*if(modifiedDateColumn!=null){
                        long currentArticleDate = DBTool.getLong(rs, modifiedDateColumn.getColumnIndex(), modifiedDateColumn.getColumnType());
                        if (currentArticleDate > license_end_time) {
                            logger.warn("Your License Expired! Skipping this record.");
                            continue;
                        }
                    }*/
                    TextDocument doc = new TextDocument();
                    try {
                        DocumentSQLTaskHelper.saveToTextDocument(doc, rs, docListQuery);
                        ic.getScheduler().schedule(this.contextId+1, doc);
                        count++;
                    }catch(Throwable t) {
                        logger.warn("Error in processResults", t);
                    }
                }
                // record indexed period, or add it if already documented but not recorded
                if(modifiedDateColumn!=null){
                    pe.add(DBTool.getLong(rs, modifiedDateColumn.getColumnIndex(), modifiedDateColumn.getColumnType()));
                }
                processedRowCount++;
            }else {
                break;
            }
        }

        return true;
    }

    @Override
    public void stop() {
        if(hasExistingIndex){
            deleteDuplicates();
        }
    }
    private DiskBackedQueue possibleDuplicatePrimaryKeys;
    private void addPossibleDuplicatePrimaryKey(String key){
        //hasExistingIndex==true
        if(possibleDuplicatePrimaryKeys==null){
            try {
                possibleDuplicatePrimaryKeys = new DiskBackedQueue(ic.getDatasetConfiguration().getWorkDirectoryFile(),"duplist",false,10000);
            } catch (IOException e) {
                logger.warn("Failed to create updated document list in directory " + ic.getDatasetConfiguration().getWorkDirectoryFile() + ",\n" + e);
            }
        }
        possibleDuplicatePrimaryKeys.enqueue(key);
    }
    private void deleteDuplicates(){
        if(possibleDuplicatePrimaryKeys==null) return;

        logger.debug("Start Deleting possibly updated "+possibleDuplicatePrimaryKeys.length()+" documents ...");

        IndexReader indexReader = null;
        Searcher searcher = null;
        try {
            //delete from main directory, and current working temp directory
            indexReader = IndexStatus.openIndexReader(IndexStatus.findActiveMainDirectoryFile(ic.getDatasetConfiguration()));
            searcher = new IndexSearcher(indexReader);
            while(possibleDuplicatePrimaryKeys.length()>0){
                String pkValue = (String) possibleDuplicatePrimaryKeys.dequeue();
                Term pkTerm = new Term(primaryKeyColumn.getColumnName(), pkValue);
                Query pkQuery = new TermQuery(pkTerm);
                Hits hits = searcher.search(pkQuery);
                if (hits.length() > 0) {
                    for (int i = 0; i < hits.length(); i++) {
                        //logger.debug("Deleting duplicate document " + hits.id(i) + " for " + pkQuery);
                        indexReader.deleteDocument(hits.id(i));
                    }
                }
            }
            indexReader.flush();
        } catch (IOException e) {
            logger.warn("When deleting updated document in index " + ic.getDatasetConfiguration().getIndexDirectory() + ",\n" + e);
        }finally{
            possibleDuplicatePrimaryKeys.disconnect();
            TaskUtil.close(searcher);
            TaskUtil.close(indexReader);
        }
    }


}
