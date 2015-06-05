package net.javacoding.xsearch.foundation;

import net.javacoding.xsearch.IndexManager;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.core.IndexerContextFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class InMemoryJob implements Job {
	
    protected Logger logger   = LoggerFactory.getLogger(InMemoryJob.class);

    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        try {
            String indexName = data.getString(NativeForkJob.PROPERTY_INDEX_NAME);
            String command = data.getString(NativeForkJob.PROPERTY_PARAMETERS);
            if(command==null||command.trim().length()<=0) return;
            String[]cmd = command.split("\\s");

            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
            Thread.currentThread().setPriority((Thread.MIN_PRIORITY));// +2));//min
            IndexManager im = new IndexManager();
            im.setDatasetConfiguration(dc);
            im.setCommands(cmd);
            im.setIndexerContext(IndexerContextFactory.createContext(dc));
            im.start();
        } catch (Throwable e) {
            logger.error("Error Occured", e);
        }
    }

}
