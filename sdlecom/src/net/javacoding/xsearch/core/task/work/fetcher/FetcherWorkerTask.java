package net.javacoding.xsearch.core.task.work.fetcher;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.task.WorkerTask;
import net.javacoding.xsearch.core.task.work.BaseWorkerTaskImpl;
import net.javacoding.xsearch.fetch.AbstractFetcher;
import net.javacoding.xsearch.fetch.FetcherManager;

public class FetcherWorkerTask extends BaseWorkerTaskImpl {

    protected IndexerContext ic;
    public FetcherWorkerTask(IndexerContext ic) {
        super(WorkerTask.WORKERTASK_RETRIEVERTASK, ic.getScheduler());
        this.ic = ic;
    }

    public void execute() {
        DatasetConfiguration dc = ic.getDatasetConfiguration();
        AbstractFetcher fetcher = FetcherManager.load(dc.getFetcherConfiguration().getDir());
        try {
            fetcher.setIndexerContext(ic);
            if(ic.getIsRecreate()) {
                fetcher.execute(dc.getFetcherConfiguration().getProperties(), 0);
            }else {
                fetcher.execute(dc.getFetcherConfiguration().getProperties(), ic.getPeriodTable().getLatest());
            }
        }finally {
            fetcher.shutdown();
        }
    }

}
