package net.javacoding.xsearch.core.task;

import net.javacoding.xsearch.core.IndexerContext;


/**
 * Provider interface that must be implemented upon each Provider class
 * that can generate task schedulers.
 *
 */
public interface SchedulerProvider {

    /**
     * Creates an instance of a JFetcher Task Scheduler.
     * @return Scheduler instance
     */
    public Scheduler createScheduler(IndexerContext ic);

}

