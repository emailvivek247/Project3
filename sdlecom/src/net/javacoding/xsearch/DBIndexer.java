package net.javacoding.xsearch;

import io.searchbox.client.JestClient;
import io.searchbox.indices.CreateIndex;

import java.io.File;
import java.io.IOException;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.DeletionDataquery;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.core.AffectedDirectoryGroup;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.task.DeletionSQLTaskFactory;
import net.javacoding.xsearch.core.task.Task;
import net.javacoding.xsearch.core.task.WorkerTask;
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

import com.fdt.elasticsearch.config.SpringContextUtil;
import com.fdt.elasticsearch.util.JestExecute;
import com.fdt.sdl.admin.ui.action.constants.IndexType;

/**
 * <code>DBIndexer</code> class based on configuration retrieve the documents
 * from data sources and save the content to ContentWriter
 */
public class DBIndexer {

    private static final Logger logger = LoggerFactory.getLogger(DBIndexer.class);

    private IndexerContext ic;
    private DatasetConfiguration dc;

    private boolean needDeletion = false;
    private boolean isThoroughDelete = false;

    private AffectedDirectoryGroup affectedDirectoryGroup;
    private JestClient jestClient;

    private String targetIndexName;

    public DBIndexer(IndexerContext ic) throws Exception {
        logger.info("building index for " + ic);
        this.ic = ic;
        this.dc = ic.getDatasetConfiguration();
        this.affectedDirectoryGroup = new AffectedDirectoryGroup();
        this.jestClient = SpringContextUtil.getBean(JestClient.class);
    }

    public void setFullIndexing(boolean fullIndexing) {
        ic.isFullIndexing = fullIndexing;
    }

    public void setNeedDeletion(boolean needDeletion) {
        this.needDeletion = needDeletion;
    }

    public void setIsThoroughDelete(boolean isThoroughDelete) {
        this.isThoroughDelete = isThoroughDelete;
    }

    public void setIsRecreate(boolean b) {
        ic.setIsRecreate(b);
        if (b) {
            logger.info("re-creating index ...");
        } else {
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

            if (dc.getIndexType() == IndexType.ELASTICSEARCH) {
                prepareESIndex();
            }

            SerialWorkerTask swt = null;

            if (dc.getDataSourceType() == DatasetConfiguration.DATASOURCE_TYPE_FETCHER) {
                ic.initAll(affectedDirectoryGroup, targetIndexName);
                swt = new SerialWorkerTask(ic.getScheduler());
                swt.addWorkTask(new FetcherWorkerTask(ic));
            } else {

                ic.initConnections();

                WorkerTask initTask = null;
                if (dc.getDeletionQuery() != null && needDeletion) {
                    ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
                    if (sc.getAllowedLicenseLevel() <= 0) {
                        logger.warn("Warning!!! Skipping deletion query because license level is 0!");
                    } else {
                        initTask = DeletionSQLTaskFactory.createTask(ic, isThoroughDelete);
                    }
                }
                if (initTask != null) {
                    initTask.prepare();
                    initTask.execute();
                    initTask.stop();
                }

                ic.initAll(affectedDirectoryGroup, targetIndexName);

                swt = new SerialWorkerTask(ic.getScheduler());
                String driver = dc.getDataSource(0).getJdbcdriver().toLowerCase();

                if (!ic.isFullIndexing && !ic.getIsRecreate() && dc.getIncrementalDataquery() != null) {
                    // only process it in incremental mode
                    swt.addWorkTask(new FetchDocumentListBySQLTask(ic));
                } else if (driver.indexOf("mysql") >= 0 || driver.indexOf("postgresql") >= 0) {
                    // mysql only handle normal incremental indexing, not the alternative incremental indexing
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
        }
    }

    private void prepareDirectories() {

        File dir = null;

        if (ic.getIsRecreate()) {
            dir = IndexStatus.findNonActiveMainDirectoryFile(dc);
            affectedDirectoryGroup.addOldDirectory(IndexStatus.findActiveMainDirectoryFile(dc));
        } else {
            // now it's incremental indexing, still, we can go directly to
            // main if no main index exists
            dir = IndexStatus.findActiveMainDirectoryFile(dc);
            if (dir == null) {
                dir = dc.getMainIndexDirectoryFile();
            }
            if ((dc.getIndexType() == null || dc.getIndexType() == IndexType.LUCENE) && IndexStatus.countIndexSize(dir) <= 0) {
                // so dir is the main directory but empty, just use the main directory to prevent duplication checking
                ic.setIsRecreate(true);
            } else {
                File oldDir = IndexStatus.findActiveTempDirectoryFile(dc);
                dir = IndexStatus.findNonActiveTempDirectoryFile(dc);
                if (IndexStatus.countIndexSize(oldDir) > 0) {
                    try {
                        FileUtil.deleteAllFiles(dir);
                        FileUtil.copyAll(oldDir, dir);
                        // except the "ready" file
                        IndexStatus.setIndexNotReady(dir);
                    } catch (IOException e) {
                        logger.warn("Error when preparing directory:" + dir, e);
                    }
                }
                affectedDirectoryGroup.addOldDirectory(oldDir);
            }
        }
        if (ic.getIsRecreate()) {
            affectedDirectoryGroup.addOldDirectory(dc.getAltTempIndexDirectoryFile());
            affectedDirectoryGroup.addOldDirectory(dc.getTempIndexDirectoryFile());
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        logger.info("Working in directory:" + dir);
        affectedDirectoryGroup.setNewDirectory(dir);
    }

    private void prepareESIndex() {
        if (ic.getIsRecreate()) {
            logger.info("Preparing elasticsearch index for recreate, data set name = {}", dc.getName());
            targetIndexName = IndexStatus.findNewIndexName(jestClient, dc.getName());
            logger.info("New index name = {}", targetIndexName);
            Object indexSettings = SpringContextUtil.getBean("indexSettings");
            CreateIndex createIndex = new CreateIndex.Builder(targetIndexName).settings(indexSettings).build();
            JestExecute.execute(jestClient, createIndex);
        } else {
            logger.info("Initializing elasticsearch index for updates, data set name = {}", dc.getName());
            targetIndexName = IndexStatus.findCurrentIndexName(jestClient, dc.getName());
            logger.info("Target index name = {}", targetIndexName);
        }
    }

    private void cleanDirectories() {
        if (ic.isDataComplete) {
            affectedDirectoryGroup.setFinalReadyStatus();
        }
    }
}
