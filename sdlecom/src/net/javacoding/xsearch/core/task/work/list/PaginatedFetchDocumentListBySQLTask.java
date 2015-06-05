package net.javacoding.xsearch.core.task.work.list;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.task.work.util.TaskUtil;

public class PaginatedFetchDocumentListBySQLTask extends FetchDocumentListBySQLTask {
	
	private boolean isSupportingLimit = true;

	public PaginatedFetchDocumentListBySQLTask(IndexerContext ic) {
		super(ic);
	}

	public void execute() {
		int fetchSize = ic.getDatasetConfiguration().getListFetchSize();
		//no operation if already has limit, or fetchsize is 0
		if(docListQuery.hasLimitClause()||fetchSize<=0){
			super.execute();
			return;
		}
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String limitSql = docListQuery.getSql().concat("\n LIMIT ? OFFSET ? ");
        try {
            if (ic.isStopping()) return;
            logger.info("Paginated: Getting the connection...");
            conn = ic.getConnectionProvider().getConnection();
            logger.info("Connected.");
            try{
                logger.info("Getting the document list by sql:" + limitSql);
            	ps = conn.prepareStatement(limitSql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            }catch(SQLException e){
            	logger.error("SQLException",e);
                logger.warn("!!!!! Error to append clause \"Limit ? OFFSET ?\"");
                logger.info("!!!!! Limit grammar not supported. Getting the document list by sql:" + docListQuery.getSql());
            	isSupportingLimit = false;
            	ps = conn.prepareStatement(docListQuery.getSql(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            }
            int old_count = -1;
            while(old_count<processedRowCount){
                old_count = processedRowCount;
                int i = 0;
                if(isSupportingLimit){
	                ps.setInt(i+1, fetchSize);
	                ps.setInt(i+2, processedRowCount);
	                logger.info("paginating the statement...");
                }else{
                    logger.info("selecting all results from the statement...");
                }
                rs = ps.executeQuery();
                if(rs==null) {
                    logger.warn("Empty document List!");
                    return;
                }
                logger.info("processing the result set...");
                processResults(rs);
                ic.p.p(ic, "m");
                if(!isSupportingLimit){
                	//no more looping if not supporting limit
                	break;
                }
            }
            logger.info(count + " documents to index");
        } catch (OutOfMemoryError oome) {
            logger.error("Out Of Memory Error During Main Query:" + oome);
            ic.setStopping();
        } catch (Throwable t) {
            logger.error("Error During Main Query:" + t + ":" + t.getMessage());
            t.printStackTrace();
            ic.setStopping();
        } finally {
            logger.info("closing result set ...");
        	TaskUtil.close(rs);
            logger.debug("closing index reader...");
        	TaskUtil.close(ps);
            logger.info("closing jdbc connection ...");
        	close(conn);
            logger.info("Scheduled Period to index:" + pe);
            // add indexed period to the total indexed period table
            if(ic.getPeriodTable()!=null){
                ic.getPeriodTable().add(this.pe);
                logger.info("Index Period:" + ic.getPeriodTable());
            }
        }
	}

}
