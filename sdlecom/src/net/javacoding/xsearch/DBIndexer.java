package net.javacoding.xsearch;

import java.io.File;
import java.io.IOException;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.DeletionDataquery;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.core.AffectedDirectoryGroup;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.task.Task;
import net.javacoding.xsearch.core.task.WorkerTask;
import net.javacoding.xsearch.core.task.work.BaseWorkerTaskImpl;
import net.javacoding.xsearch.core.task.work.SerialWorkerTask;
import net.javacoding.xsearch.core.task.work.fetcher.FetcherWorkerTask;
import net.javacoding.xsearch.core.task.work.list.FastFetchFullDocumentListBySQLTask;
import net.javacoding.xsearch.core.task.work.list.FetchDeletedDocumentListBySQLTask;
import net.javacoding.xsearch.core.task.work.list.FetchDocumentListBySQLTask;
import net.javacoding.xsearch.core.task.work.list.FetchFullDocumentListBySQLTask;
import net.javacoding.xsearch.core.task.work.list.PaginatedFetchDocumentListBySQLTask;
import net.javacoding.xsearch.status.IndexStatus;
import net.javacoding.xsearch.utility.FileUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>DBIndexer</code> class based on configuration retrieve the documents
 * from data sources and save the content to ContentWriter
 */
public class DBIndexer {
    protected Logger         logger = LoggerFactory.getLogger(this.getClass().getName());
    public IndexerContext ic;
    public long              start  = 0;
    private boolean needDeletion = false;
    private boolean isThoroughDelete = false;
    private AffectedDirectoryGroup affectedDirectoryGroup;

    public DBIndexer(IndexerContext ic) throws Exception {
        logger.info("buiding index for " + ic );
        start = System.currentTimeMillis();
        this.ic = ic;
        this.affectedDirectoryGroup = new AffectedDirectoryGroup();
    }

    public void setFullIndexing(boolean fullIndexing){
        ic.isFullIndexing = fullIndexing;
    }
    
    public void setNeedDeletion(boolean needDeletion) {
        this.needDeletion = needDeletion;
    }

    public void setIsThoroughDelete(boolean isThoroughDelete) {
        this.isThoroughDelete = isThoroughDelete;
    }

    public void setIsRecreate(boolean b){
        ic.setIsRecreate(b);
        if(b){
            logger.info("re-creating index ...");
        }else{
            logger.info("incremental indexing ...");
        }
    }

    /**
     * Save a list of documents and save the changes 1. pop from work queue 2.
     * call retrieveOne to retrieve the item, and save the changes, update
     * last_run_date
     */

    public void start() {
        try {
            prepareDirectories();
            
            SerialWorkerTask swt = null;
            if(ic.getDatasetConfiguration().getDataSourceType()==DatasetConfiguration.DATASOURCE_TYPE_FETCHER) {
                ic.initAll(affectedDirectoryGroup);
                swt = new SerialWorkerTask(ic.getScheduler());
                swt.addWorkTask(new FetcherWorkerTask(ic));
            }else {
                WorkerTask initTask = null;
                DeletionDataquery dq = ic.getDatasetConfiguration().getDeletionQuery();
                if(dq!=null && needDeletion){
                    ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
                    if(sc.getAllowedLicenseLevel()<=0) {
                        logger.warn("Warning!!! Skipping deletion query because license level is 0!");
                    }else {
                        if(dq.getIsDeleteOnly()) {
                            initTask = new FetchDeletedDocumentListBySQLTask(ic);
                        }else {
                            if(isThoroughDelete) {
                                initTask = new FetchFullDocumentListBySQLTask(ic); //this is slower
                            }else {
                                initTask = new FastFetchFullDocumentListBySQLTask(ic);
                            }
                        }
                    }
                }
                ic.initConnections();
                if(initTask!=null){
                    initTask.prepare();
                    initTask.execute();
                    initTask.stop();
                }
                ic.initAll(affectedDirectoryGroup);
                swt = new SerialWorkerTask(ic.getScheduler());
                if(!ic.isFullIndexing&&!ic.getIsRecreate()&&ic.getDatasetConfiguration().getIncrementalDataquery()!=null) {
                    //only process it in incremental mode
                    swt.addWorkTask(new FetchDocumentListBySQLTask(ic));
                }else if(ic.getDatasetConfiguration().getDataSource(0).getJdbcdriver().toLowerCase().indexOf("mysql")>=0
                        || ic.getDatasetConfiguration().getDataSource(0).getJdbcdriver().toLowerCase().indexOf("postgresql")>=0) {
                    //mysql only handle normal incremental indexing, not the alternative incremental indexing
                    swt.addWorkTask(new PaginatedFetchDocumentListBySQLTask(ic));
                } else {
                    swt.addWorkTask(new FetchDocumentListBySQLTask(ic));
                }
            }
            ic.getScheduler().schedule(0, swt);

            Task dispatcherTask = ic.getFetcherPoolDispatchTask();

            synchronized (dispatcherTask) {

                ic.getFetcherPool().assignGroupTask(ic.getFetcherPoolDispatchTask());
                ic.getWriterPool().assignGroupTask(ic.getWriterPoolDispatchTask());

                try {
                    // now wait for the spidering to be ended.
                    dispatcherTask.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            logger.info("Stopping all threads...");
            ic.stopAll();
            
            cleanDirectories();

        } catch (Throwable e) {
            logger.error("Error in Start Method", e);
        } finally {}
    }

    private void prepareDirectories() {
        java.io.File dir = null;
        if(ic.getIsRecreate()){
            dir = IndexStatus.findNonActiveMainDirectoryFile(ic.getDatasetConfiguration());
            affectedDirectoryGroup.addOldDirectory(IndexStatus.findActiveMainDirectoryFile(ic.getDatasetConfiguration()));
        }else{
            //now it's incremental indexing, still, we can go directly to main if no main index exists
            dir = IndexStatus.findActiveMainDirectoryFile(ic.getDatasetConfiguration());
            if(dir==null){
                dir = ic.getDatasetConfiguration().getMainIndexDirectoryFile();
            }
            if(IndexStatus.countIndexSize(dir)<=0){
                //so dir is the main directory but empty, just use the main directory
                ic.setIsRecreate(true); // to prevent duplication checking
            }else{
                File oldDir = IndexStatus.findActiveTempDirectoryFile(ic.getDatasetConfiguration());
                dir = IndexStatus.findNonActiveTempDirectoryFile(ic.getDatasetConfiguration());
                if(IndexStatus.countIndexSize(oldDir)>0){
                    try {
                        FileUtil.deleteAllFiles(dir);
                        FileUtil.copyAll(oldDir, dir);
                        //except the "ready" file
                        IndexStatus.setIndexNotReady(dir);
                    } catch (IOException e) {
                        logger.warn("Error when preparing directory:"+dir,e);
                    }
                }
                affectedDirectoryGroup.addOldDirectory(oldDir);
            }
        }
        if(ic.getIsRecreate()){
            affectedDirectoryGroup.addOldDirectory(ic.getDatasetConfiguration().getAltTempIndexDirectoryFile());
            affectedDirectoryGroup.addOldDirectory(ic.getDatasetConfiguration().getTempIndexDirectoryFile());
        }
        if (!dir.exists()) dir.mkdirs();
        logger.info("Working in directory:"+dir);
        affectedDirectoryGroup.setNewDirectory(dir);
    }
    private void cleanDirectories() {
        if(ic.isDataComplete){
            affectedDirectoryGroup.setFinalReadyStatus();
        }
    }
}
