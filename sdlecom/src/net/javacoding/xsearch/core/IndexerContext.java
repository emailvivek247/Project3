package net.javacoding.xsearch.core;

import io.searchbox.core.Index;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.connection.ConnectionProvider;
import net.javacoding.xsearch.core.exception.DataSourceException;
import net.javacoding.xsearch.core.task.Scheduler;
import net.javacoding.xsearch.core.task.dispatch.FetcherPoolDispatchTask;
import net.javacoding.xsearch.core.task.dispatch.WriterPoolDispatchTask;
import net.javacoding.xsearch.core.task.work.ESIndexConsumer;
import net.javacoding.xsearch.core.threading.WorkerThreadPool;
import net.javacoding.xsearch.indexer.IndexWriterProvider;
import net.javacoding.xsearch.status.P;

import com.fdt.sdl.admin.ui.action.constants.IndexType;

public abstract class IndexerContext {

    public static final String PERIOD_TABLE_FILE = "peridTbl";

    public boolean hasDeletion = false;
    public boolean hasAddition = false;

    public boolean isDataComplete = true;
    public boolean isFullIndexing = false;
    
    public Object listFetcherFlag = new Object();
    public Object docFetcherFlag = new Object();

    public P p = new P();

    protected DatasetConfiguration dc;
    protected AffectedDirectoryGroup affectedDirectoryGroup;

    protected IndexType indexType;
    protected String targetIndexName;

    protected List<ESIndexConsumer> consumerThreads;
    protected ExecutorService consumerService;
    protected BlockingQueue<Index> queue;

    final Map<Object, Object> cache = new WeakHashMap<Object, Object>();

    public IndexerContext(DatasetConfiguration dc) {
        this.dc = dc;
        this.indexType = dc.getIndexType();
    }

    abstract public ConnectionProvider getConnectionProvider();

    abstract public IndexWriterProvider getIndexWriterProvider();

    abstract public Scheduler getScheduler();

    abstract public WorkerThreadPool getFetcherPool();

    abstract public WorkerThreadPool getWriterPool();

    abstract public FetcherPoolDispatchTask getFetcherPoolDispatchTask();

    abstract public WriterPoolDispatchTask getWriterPoolDispatchTask();

    abstract public void initConnections() throws IOException, DataSourceException;

    abstract public void initAll(AffectedDirectoryGroup affectedDirectoryGroup, String targetIndexName) throws IOException, DataSourceException;

    abstract public void stopAll();

    abstract public PeriodTable getPeriodTable();

    abstract public boolean isStopping();

    abstract public void setStopping();

    abstract public void setIsRecreate(boolean b);

    abstract public boolean getIsRecreate();

    abstract public void setIsIncrementalSql(boolean b);

    abstract public boolean getIsIncrementalSql();

    public void setHasDeletion(boolean hasDeletion) {
        this.hasDeletion = hasDeletion;
    }

    public boolean hasDeletion() {
        return hasDeletion;
    }

    public void setHasAddition(boolean hasAddition) {
        this.hasAddition = hasAddition;
    }

    public boolean hasAddition() {
        return hasAddition;
    }

    /** See if an object is in the cache. */
    public Object lookup(Object name) {
        synchronized (this) {
            return cache.get(name);
        }
    }

    /** Put an object into the cache. */
    public Object store(Object name, Object value) {
        synchronized (this) {
            return cache.put(name, value);
        }
    }

    public DatasetConfiguration getDatasetConfiguration() {
        return dc;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public String getTargetIndexName() {
        return targetIndexName;
    }

    public AffectedDirectoryGroup getAffectedDirectoryGroup() {
        return affectedDirectoryGroup;
    }

    public void setAffectedDirectoryGroup(AffectedDirectoryGroup adg) {
        this.affectedDirectoryGroup = adg;
    }

    public File getIndexWriterWorkingDirectory() {
        return affectedDirectoryGroup.getNewDirectory();
    }

    public BlockingQueue<Index> getQueue() {
        return queue;
    }
}
