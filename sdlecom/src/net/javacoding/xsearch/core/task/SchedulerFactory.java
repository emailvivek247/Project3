package net.javacoding.xsearch.core.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.javacoding.xsearch.core.task.impl.DefaultSchedulerProvider;
import net.javacoding.xsearch.core.IndexerContext;


public class SchedulerFactory {

    public static final int DEFAULT_MONITORING_INTERVAL = 1000;

    public Scheduler createScheduler(IndexerContext ic) {

        Class<DefaultSchedulerProvider> providerClass = DefaultSchedulerProvider.class;
        Logger logger = LoggerFactory.getLogger(this.getClass().getName());
        logger.info("TaskScheduler provider class is '" + providerClass + "'");

        try {
            SchedulerProvider provider = (SchedulerProvider) providerClass.newInstance();
            Scheduler scheduler = provider.createScheduler(ic);
            return scheduler;
        } catch (InstantiationException e) {
            logger.error("InstantiationException on Scheduler", e);
            return null;
        } catch (IllegalAccessException e) {
            logger.error("IllegalAccessException on Scheduler", e);
            return null;
        }
    }
}
