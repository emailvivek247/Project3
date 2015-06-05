package net.javacoding.xsearch.core.task.dispatch;

import net.javacoding.xsearch.core.exception.FetcheringDoneException;
import net.javacoding.xsearch.core.exception.TaskAssignmentException;
import net.javacoding.xsearch.core.task.Scheduler;
import net.javacoding.xsearch.core.task.WorkerTask;
import net.javacoding.xsearch.core.threading.WorkerThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetcherPoolDispatchTask extends BaseDispatchTaskImpl {

    protected WorkerThreadPool fetcherPool;
    protected Scheduler        scheduler;

    public FetcherPoolDispatchTask(WorkerThreadPool fetcherPool, Scheduler scheduler) {
        super();
        this.fetcherPool = fetcherPool;
        this.scheduler = scheduler;
    }

    public void execute() {
        Logger logger = LoggerFactory.getLogger(this.getClass().getName());
        logger.debug("Fetcher task dispatcher running ...");
        // excluding writer taskQueue
        Thread[] threads = new Thread[scheduler.getTaskContexts().length - 1];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new FetchTaskQueueThread(i);
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.debug("Fetcher task dispatcher dying ...");

    }

    final class FetchTaskQueueThread extends Thread {
        int taskQueueId;

        public FetchTaskQueueThread(int taskQueueId) {
            super("Fetch Task Queue "+taskQueueId);
            this.taskQueueId = taskQueueId;
        }

        public void run() {
            try {
                while (running && !scheduler.getIndexerContext().isStopping()) {
                    try {
                        WorkerTask wt = scheduler.getFetcherTask(taskQueueId);
                        if (wt != null)
                            fetcherPool.assign(wt);
                    } catch (FetcheringDoneException e) {
                        running = false;
                    } catch (TaskAssignmentException e) {
                        // We dealt with all subclasses, so this shouldn't
                        // happen !!!
                    }
                }
            } catch (Throwable t) {}
        }
    }
}
