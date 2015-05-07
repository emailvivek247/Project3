package net.javacoding.xsearch.core;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

import net.javacoding.xsearch.config.DataSource;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.connection.ConnectionProvider;
import net.javacoding.xsearch.core.exception.DataSourceException;
import net.javacoding.xsearch.core.task.Scheduler;
import net.javacoding.xsearch.core.task.dispatch.FetcherPoolDispatchTask;
import net.javacoding.xsearch.core.task.dispatch.WriterPoolDispatchTask;
import net.javacoding.xsearch.core.threading.WorkerThreadPool;
import net.javacoding.xsearch.indexer.IndexWriterProvider;
import net.javacoding.xsearch.status.IndexStatus;
import net.javacoding.xsearch.status.P;
import net.javacoding.xsearch.utility.FileUtil;

public abstract class IndexerContext {

	abstract public ConnectionProvider getConnectionProvider();

	abstract public IndexWriterProvider getIndexWriterProvider();

	abstract public Scheduler getScheduler();

	abstract public WorkerThreadPool getFetcherPool();

	abstract public WorkerThreadPool getWriterPool();

	abstract public FetcherPoolDispatchTask getFetcherPoolDispatchTask();

	abstract public WriterPoolDispatchTask getWriterPoolDispatchTask();

    abstract public void initConnections() throws IOException, DataSourceException;
    abstract public void initAll(AffectedDirectoryGroup affectedDirectoryGroup) throws java.io.IOException, DataSourceException;
	abstract public void stopAll();

	abstract public PeriodTable getPeriodTable();

	abstract public boolean isStopping();

	abstract public void setStopping();

	abstract public void setIsRecreate(boolean b);

	abstract public boolean getIsRecreate();

    abstract public void setIsIncrementalSql(boolean b);

    abstract public boolean getIsIncrementalSql();

    public boolean hasDeletion = false;
	public void setHasDeletion(boolean hasDeletion) {
		this.hasDeletion = hasDeletion;
	}
	public boolean hasDeletion() {
		return hasDeletion;
	}

	public boolean hasAddition = false;
	public void setHasAddition(boolean hasAddition) {
		this.hasAddition = hasAddition;
	}
	public boolean hasAddition() {
		return hasAddition;
	}

	public boolean isDataComplete = true;

	public Object listFetcherFlag = new Object();
	public Object docFetcherFlag = new Object();
	public static final String PERIOD_TABLE_FILE = "peridTbl";
	public boolean isFullIndexing = false;

	public P p = new P();

	/** The internal cache. * */
	final Map cache = new WeakHashMap();
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

    protected DatasetConfiguration dc;
    public DatasetConfiguration getDatasetConfiguration() {
        return this.dc;
    }

	public IndexerContext(DatasetConfiguration dc) {
		this.dc = dc;
	}
	
    protected AffectedDirectoryGroup affectedDirectoryGroup;
    
    public AffectedDirectoryGroup getAffectedDirectoryGroup(){
        return this.affectedDirectoryGroup;
    }
    public void setAffectedDirectoryGroup(AffectedDirectoryGroup adg){
        this.affectedDirectoryGroup = adg;
    }

    public File getIndexWriterWorkingDirectory(){
        return affectedDirectoryGroup.getNewDirectory();
    }


}
