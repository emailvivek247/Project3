package net.javacoding.xsearch.core.task.work.list;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;
import java.io.File;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.Dataquery;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.task.WorkerTask;
import net.javacoding.xsearch.core.task.work.BaseWorkerTaskImpl;
import net.javacoding.xsearch.core.task.work.util.TaskUtil;
import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.status.IndexStatus;
import net.javacoding.xsearch.utility.DBTool;
import net.javacoding.xsearch.utility.FileUtil;
import oracle.jdbc.OracleTypes;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;

import com.fdt.sdl.styledesigner.util.PageStyleUtil;

public abstract class BaseFetchPrimaryKeysBySingleQueryTask extends BaseWorkerTaskImpl {
    protected Logger             logger             = LoggerFactory.getLogger(this.getClass().getName());

    protected IndexerContext     ic;
    protected Dataquery          simpleDataquery;
    protected Column             primaryKeyColumn   = null;

    protected IndexReader        indexReader        = null;
    protected Searcher           searcher           = null;

    protected int                processedRowCount  = 0;
    protected int                count              = 0;
    protected long               lastReportTime     = 0;

    protected boolean            needServerDeletion = true;
    protected boolean            isMySql            = false;

    protected ArrayList<Integer> toBeDeleted        = null;

    protected boolean            isListComplete     = false;
    
    

    public BaseFetchPrimaryKeysBySingleQueryTask(IndexerContext ic) {
        super(WorkerTask.WORKERTASK_RETRIEVERTASK, ic.getScheduler());
        this.ic = ic;
        this.simpleDataquery = ic.getDatasetConfiguration().getDeletionQuery();
        if (simpleDataquery == null)
            return;
        this.primaryKeyColumn = ic.getDatasetConfiguration().getWorkingQueueDataquery().getPrimaryKeyColumn();
        this.count = 0;
        this.needServerDeletion = true;
        this.isMySql = ic.getDatasetConfiguration().getDataSource(0).getJdbcdriver().toLowerCase().indexOf("mysql") >= 0;
    }

    public void prepare() {
        super.prepare();
        if (simpleDataquery == null)
            return;
        if (!ic.getIsRecreate())
            try {
                indexReader = IndexStatus.openIndexReaderWithNewTemp(ic.getDatasetConfiguration());
                if (indexReader != null) {
                    searcher = new IndexSearcher(indexReader);
                }
            } catch (IOException ioe) {
                logger.warn("When getting index reader for index " + ic.getDatasetConfiguration().getIndexDirectory() + ",\n" + ioe);
            }
        this.isListComplete = false;
    }

    public void execute() {
        if (simpleDataquery == null)
            return;
        if (isMySql) {
            executePaginated();
        } else {
            executeNormal();
        }
    }

    /**
     * Select existing doc's primary key, and delete the doc that's not in the
     * list from the index
     * 
     * 
     */
    public void executeNormal() {
        if (primaryKeyColumn == null)
            return;
        if (indexReader == null)
            return;
        if (ic.getIsRecreate())
            return;
        if (simpleDataquery == null)
            return;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int fetchSize = 0;
        File propertiesFile = null;
		String filePath = null;
		String fileName = null;
		// if ic is null ?? 
		String indexName = ic.toString();
		fileName = indexName.concat("-deleteQuery.properties");
		try {
			propertiesFile = FileUtil.resolveFile(WebserverStatic.getRootDirectoryFile(), "data", "properties", fileName);
			filePath = propertiesFile.getAbsolutePath();
		} catch (Exception e) {
			  logger.debug(fileName + " is not present at specified path. ");
			  filePath = WebserverStatic.getRootDirectoryFile().toString().concat("\\data\\").concat("\\properties\\").concat(fileName); 
			 
		}
		try {
            if (ic.isStopping())
                return;
            logger.info("Getting the connection...");
            conn = ic.getConnectionProvider().getConnection();
            logger.info("Connected.");
            logger.info("Preparing the Prepared Statement or Callable Statement...");
            try {
            	if (ic.getDatasetConfiguration().getListFetchSize() > 0) {
            		fetchSize = ic.getDatasetConfiguration().getListFetchSize();
            	}
                if (simpleDataquery.getSql().toLowerCase().contains("call")) {
                	if (conn.getMetaData().getDriverName().contains("Oracle")) {
                		rs = executeOracleSP(conn, simpleDataquery.getSql(), fetchSize, filePath);
                	} else {
                		rs = executeNormalSP(conn, simpleDataquery.getSql(), fetchSize, filePath); 
                	}
                } else {
                	ps = this.getPreparedStatement(simpleDataquery.getSql(), conn, fetchSize, filePath );
                   	logger.info("executing the statement...");
                	rs = ps.executeQuery();
                }
            } catch (OutOfMemoryError e) {
                try {
                    ps.setFetchSize(1);
                    logger.warn("Too many rows causing OutOfMemory Error. Adjust Fetch Size to " + 1);
                    rs = ps.executeQuery();
                } catch (OutOfMemoryError oome) {
                    ps.setFetchSize(Integer.MIN_VALUE);
                    logger.warn("Adjust Fetch Size to " + Integer.MIN_VALUE);
                    rs = ps.executeQuery();
                }
            } catch (SQLException e) {
                logger.error(NOTIFY_ADMIN, "SQL Exception:" + e.getMessage());
            }
            logger.info("Get the document list by sql:" + simpleDataquery.getSql());
            if (rs == null) {
                logger.warn("Empty document List!");
                return;
            }
            processResults(rs);
            this.writeLastTimeStamp(filePath);
            logger.info(count + " documents to be processed.");
            this.isListComplete = true;
        } catch (OutOfMemoryError oome) {
            logger.error(NOTIFY_ADMIN, "Out Of Memory Error During Deleted Documents Query:" + oome);
            emergencyStop();
        } catch (Throwable t) {
            logger.error(NOTIFY_ADMIN, "Error During Deletion Documents Query:" + t + ":" + t.getMessage(), t);
            emergencyStop();
        } finally {
            TaskUtil.close(rs);
            TaskUtil.close(ps);
            close(conn);
        }
    }
    
    /** This method writes the current time to the specified file. */
    private void writeLastTimeStamp(String filePath){
    	if (filePath != null) {
        	try {
        		String date = PageStyleUtil.format(new java.util.Date().toString(), "EEE MMM dd HH:mm:ss z yyyy", "MM-dd-yyyy HH:mm:ss");
        		FileUtil.writeProperty("lastRunTime", date, filePath);
        	} catch (Exception f) {
        		 logger.debug(filePath + " is not present. ");
        		 File newFile  =  new File(filePath);
   			  try {
   				FileUtil.createNewFile(newFile);
   				FileUtil.writeProperty("lastRunTime", new java.util.Date().toString(), filePath);
   			} catch (IOException e1) {
   				e1.printStackTrace();
   			}
        	} 
        }
    }
    
    /** This method get called when Simple SQL Query is used in Delete SQL dialog & incremental indexing action is
	 * specified. If ? is specified in SQL Query as an argument, then lastTimestamp is read from 
	 * deleteQuery.properties file and passed as the parameter, otherwise the execution is normal.
	 */
	private PreparedStatement getPreparedStatement(String sql, Connection conn, int fetchSize, String filePath) throws SQLException {
		PreparedStatement ps = null;
		Timestamp lastCheckedTimestamp = null;
    	String lastRunTime = null;
        Calendar calendar = Calendar.getInstance(); 
    	int index = sql.indexOf("?");
    	int count = 0;
    	if(index != -1) {
    		count = StringUtils.countMatches(sql, "?");
    		try {
    			if(filePath != null) {
	    			lastRunTime = FileUtil.readProperty("lastRunTime", filePath);
	    			SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	    			try {
	    				calendar.setTime(format.parse(lastRunTime));
	    				lastCheckedTimestamp = new Timestamp(calendar.getTime().getTime());
	    			} catch (ParseException e) {
	    				logger.debug("Wrong Format of date!!"); 
	    			}
    			}
    		} catch (Exception e) {
    			logger.debug("Configuration Properties Missing");                			
    		}
    		if(lastRunTime == null) {
    			lastCheckedTimestamp = new Timestamp(calendar.getTime().getTime());
    		}
       	}
    	ps = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    	if(index != -1){
    		for(int i = 1; i <=count; i++ ) {
    			ps.setTimestamp(i, lastCheckedTimestamp);
    		}
    		logger.info("lastCheckedTimestamp: " + lastCheckedTimestamp);
    	}
    	ps.setFetchSize(fetchSize);
   		logger.info("Set Fetch Size to " + fetchSize);
   		return ps;
	}

	/** This method get called when Oracle Stored Procedure is used in Delete SQL dialog & incremental indexing action is
	 * specified. If ? is specified in Stored Procedure as an argument, then lastTimestamp is read from 
	 * deleteQuery.properties file and passed as the parameter, otherwise the execution is normal.
	 */
	private ResultSet executeOracleSP(Connection conn, String sp, int fetchSize, String filePath) throws SQLException {
		logger.info("Started Executing SP based on Oracle DB");
		ResultSet rs = null;
		Timestamp lastCheckedTimestamp = null;
    	String lastRunTime = null;
        Calendar calendar = Calendar.getInstance(); 
    	int count = StringUtils.countMatches(sp, "?");
    	if(count > 1) {
    		try {
    			if(filePath != null) {
	    			lastRunTime = FileUtil.readProperty("lastRunTime", filePath );
	    			SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	    			try {
	    				calendar.setTime(format.parse(lastRunTime));
	    				lastCheckedTimestamp = new Timestamp(calendar.getTime().getTime());
	    			} catch (ParseException e) {
	    				logger.debug("Wrong Format of date!!"); 
	    			}
    			}
    		} catch (Exception e) {
    			logger.debug("Configuration Properties Missing");                			
    		}
    		if(lastRunTime == null) {
    			lastCheckedTimestamp = new Timestamp(calendar.getTime().getTime());
    		}     		
    	}
    	CallableStatement cstmt = conn.prepareCall(sp, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		if(count > 1) {
    		for(int i = 2; i <= count; i++ ) {
    			cstmt.setTimestamp(i, lastCheckedTimestamp);
    		}
    		logger.info("lastCheckedTimestamp: " + lastCheckedTimestamp);
    	}
		cstmt.registerOutParameter(1, OracleTypes.CURSOR);
		cstmt.setFetchSize(fetchSize);
		cstmt.execute();
		rs = (ResultSet) cstmt.getObject(1);
		logger.info("Finished Executing SP based on Oracle DB");
		return rs;
	}
	
	/** This method get called when SQL Server Stored Procedure is used in Delete SQL dialog & incremental indexing action is
	 * specified. If ? is specified in Stored Procedure as an argument, then lastTimestamp is read from 
	 * deleteQuery.properties file and passed as the parameter, otherwise the execution is normal.
	 */
	private ResultSet executeNormalSP(Connection conn, String sp, int fetchSize, String filePath) throws SQLException {
		logger.info("Started Executing Normal SP");
		ResultSet rs = null;
		Timestamp lastCheckedTimestamp = null;
    	String lastRunTime = null;
        Calendar calendar = Calendar.getInstance(); 
    	int index = sp.indexOf("?");
    	int count = 0;
    	if(index != -1) {
    		count = StringUtils.countMatches(sp, "?");
    		try {
    			if(filePath != null) {
	    			lastRunTime = FileUtil.readProperty("lastRunTime", filePath );
	    			SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	    			try {
	    				calendar.setTime(format.parse(lastRunTime));
	    				lastCheckedTimestamp = new Timestamp(calendar.getTime().getTime());
	    			} catch (ParseException e) {
	    				logger.debug("Wrong Format of date!!"); 
	    			}
    			}
    		} catch (Exception e) {
    			logger.debug("Configuration Properties Missing");                			
    		}
    		if(lastRunTime == null) {
    			lastCheckedTimestamp = new Timestamp(calendar.getTime().getTime());
    		}
       	}
		PreparedStatement ps = conn.prepareCall(sp, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		if(index != -1) {
    		for(int i = 1; i <= count; i++ ) {
    			ps.setTimestamp(i, lastCheckedTimestamp);
    		}
    		logger.info("lastCheckedTimestamp: " + lastCheckedTimestamp);
    	}
		ps.setFetchSize(fetchSize);
		rs = ps.executeQuery();
		logger.info("Finished Executing Normal SP");
		return rs;
	}    
    

    public void executePaginated() {
        if (primaryKeyColumn == null)
            return;
        if (indexReader == null)
            return;
        if (ic.getIsRecreate())
            return;
        int fetchSize = ic.getDatasetConfiguration().getListFetchSize();
        // no operation if already has limit, or fetchsize is 0
        if (simpleDataquery == null || simpleDataquery.hasLimitClause() || fetchSize <= 0) {
            executeNormal();
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String limitSql = simpleDataquery.getSql().concat(" LIMIT ? OFFSET ? ");
        boolean isSupportingLimit = true;

        try {
            if (ic.isStopping())
                return;
            logger.info("Getting the connection...");
            conn = ic.getConnectionProvider().getConnection();
            logger.info("Connected.");
            try {
                logger.info("Getting the paginated document list by sql:" + limitSql);
                ps = conn.prepareStatement(limitSql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            } catch (SQLException e) {
                logger.info("Limit grammar not supported. Getting the document list by sql:" + simpleDataquery.getSql());
                isSupportingLimit = false;
                ps = conn.prepareStatement(simpleDataquery.getSql(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            }
            int old_count = -1;
            while (old_count < processedRowCount) {
                old_count = processedRowCount;
                if (isSupportingLimit) {
                    ps.setInt(1, fetchSize);
                    ps.setInt(2, processedRowCount);
                }
                logger.info("paginating the statement...");
                rs = ps.executeQuery();
                if (rs == null) {
                    logger.warn("Empty document List!");
                    return;
                }
                logger.info("processing the result set...");
                processResults(rs);
                if (!isSupportingLimit) {
                    // no more looping if not supporting limit
                    break;
                }
            }
            logger.info(count + " documents to process");
            this.isListComplete = true;
        } catch (OutOfMemoryError oome) {
            logger.error("Out Of Memory Error During Deleted Documents Query:" + oome);
            emergencyStop();
        } catch (Throwable t) {
            logger.error("Error During Deleted Documents Query:" + t + ":" + t.getMessage());
            logger.error("Detailed Error", t);
            emergencyStop();
        } finally {
            TaskUtil.close(rs);
            TaskUtil.close(ps);
            close(conn);
        }
    }

    protected void close(Connection conn) {
        if (conn != null) {
            try {
                ic.getConnectionProvider().closeConnection(conn);
                conn = null;
            } catch (Throwable e) {}
        }
    }

    public void stop() {
        if (simpleDataquery != null) {
            if (indexReader != null) {
                deleteFromIndex();
            }
            TaskUtil.close(searcher);
            TaskUtil.close(indexReader);
        }
    }

    protected int processOneResult(ResultSet rs) throws java.sql.SQLException {
        String pkValue = DBTool.getString(rs, 1, primaryKeyColumn.getColumnType());
        logger.debug("The Primary Key =====>" + pkValue);
        if (toBeDeleted == null) {
            toBeDeleted = new ArrayList<Integer>(1000);
        }
        int toDeletCount = 0;
        Hits hits = null;
        try {
            Term pkTerm = new Term(primaryKeyColumn.getColumnName(), pkValue);
            Query pkQuery = new TermQuery(pkTerm);
            hits = searcher.search(pkQuery);
            for (int i = 0; i < hits.length(); i++, toDeletCount++) {
                toBeDeleted.add(new Integer(hits.id(i)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return toDeletCount;
    }

    /**
     * bulk Delete from the current searcher
     * 
     * @param pkValue
     * @return
     */
    protected int deleteFromIndex() {
        if (toBeDeleted == null || toBeDeleted.size() <= 0) {
            return 0;
        }
        logger.debug("deleting " + toBeDeleted.size() + " documents");
        int deleted = 0;
        try {
            for (int i = 0; i < toBeDeleted.size(); i++) {
                indexReader.deleteDocument(((Integer) toBeDeleted.get(i)).intValue());
                deleted++;
            }
            if (toBeDeleted.size() > 0) {
                indexReader.commit();
                IndexStatus.setIndexReady(IndexStatus.findActiveMainDirectoryFile(ic.getDatasetConfiguration()));
                IndexStatus.setIndexReady(IndexStatus.findNonActiveTempDirectoryFile(ic.getDatasetConfiguration()));
                // this deletion setting is for indexManager to know need to
                // send a refreshIndex.do to current server
                ic.setHasDeletion(true);
            }
        } catch (IOException ioe) {
            logger.warn("When deleting deleted-in-database document in index " + ic.getDatasetConfiguration().getIndexDirectory() + ",\n" + ioe);
        } finally {}
        return deleted;
    }

    protected boolean processResults(ResultSet rs) throws SQLException {
        while (!ic.isStopping() && rs != null) {
            if (rs.next()) {
                if (ic.isStopping())
                    return false;
                count += processOneResult(rs);
                processedRowCount++;
                if (lastReportTime + 3000 < System.currentTimeMillis()) {
                    logger.info(processedRowCount + " processed...");
                    lastReportTime = System.currentTimeMillis();
                }
            } else {
                break;
            }
        }

        return true;
    }

    protected void emergencyStop() {
        ic.setStopping();
        this.isListComplete = false;
    }

    public int getContextId() {
        return 0;
    }
}
