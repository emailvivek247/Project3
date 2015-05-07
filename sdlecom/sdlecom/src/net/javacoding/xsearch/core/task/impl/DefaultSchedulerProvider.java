package net.javacoding.xsearch.core.task.impl;

import net.javacoding.xsearch.core.task.Scheduler;
import net.javacoding.xsearch.core.task.SchedulerProvider;
import net.javacoding.xsearch.core.IndexerContext;


public class DefaultSchedulerProvider implements SchedulerProvider {

    /**
     * Creates a new default scheduler implementation.
     * @return Scheduler instance
     */
    public Scheduler createScheduler(IndexerContext ic) {
        return new SchedulerImpl(ic);
    }

}
