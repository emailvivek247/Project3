package net.javacoding.xsearch.foundation;

import java.io.File;
import java.util.Date;
import java.util.StringTokenizer;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.utility.SchedulerTool;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NativeForkJob implements Job {
    public static String PROPERTY_JAVA_COMMAND = "javaCommand";
    public static String PROPERTY_PARAMETERS = "parameters";
    public static String PROPERTY_DIRECTORY = "dir";
    public static String PROPERTY_CLASSPATH = "cp";
    public static String PROPERTY_INDEX_NAME = "inName";
    public static String PROPERTY_JOB_TYPE = "normal";

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        String command = null;
        String entireJobChain = null;
        String[] cmd = null;
        String job = null;
        String indexingAction = null;
        int heapSize = 256;
        String jobType = data.getString(PROPERTY_JOB_TYPE);

        /** Checks For Jobtype and if jobtype is a linking job then it Tokenizes the jobs and schedules each indivdually
         * one after the another based on the order specified. */
        if(jobType != null && jobType.equalsIgnoreCase("linkingJob")) {
        	entireJobChain = data.getString(PROPERTY_JAVA_COMMAND);
        	if(entireJobChain==null||entireJobChain.trim().length()<=0) return;
        	StringTokenizer entireJobChainTokenizer = new StringTokenizer(entireJobChain, "|");
        	String jobWithIndexingAction = null;
        	while (entireJobChainTokenizer.hasMoreTokens()) {
        		jobWithIndexingAction = entireJobChainTokenizer.nextToken();
        		StringTokenizer jobWithIndexingActionTokenizer = new StringTokenizer(jobWithIndexingAction, ",");
        		while (jobWithIndexingActionTokenizer.hasMoreTokens()) {
      			   job = jobWithIndexingActionTokenizer.nextToken();
      			   indexingAction = jobWithIndexingActionTokenizer.nextToken();
      			   indexingAction = WebserverStatic.getIndexingActionMap().get(indexingAction.toUpperCase());
      		    }
        		DatasetConfiguration indexConfiguration = ServerConfiguration.getDatasetConfiguration(job);
        		if(indexConfiguration != null) {
        			heapSize = indexConfiguration.getJvmMaxHeapSize();
        		}
        		command = WebserverStatic.getJavaCommand(job, heapSize)+ " " + indexingAction;
                if(command==null||command.trim().length()<=0) return;
                cmd = command.split("\\s");
    	        String dir = data.getString(PROPERTY_DIRECTORY);
    	        String indexName = job;
    	        String cp = data.getString(PROPERTY_CLASSPATH);
    	        if (isRunning(indexName)) {
    	        	return;
    	        }
    	        logger.info("starting : " + command);
    	        logger.info("classpath: " + cp);
    	        logger.info("directory: " + dir);
    	        ForkThread forkthread = new ForkThread(cmd, cp, new java.io.File(dir));
    	        logger.info("Starting Timer On Index " + indexName);
    	        forkthread.run();
    	        logger.info("Ending Timer On Index : " + indexName);
    	        try {
    	        	logger.info("Pausing the Execution For 5 Minutes");
					Thread.sleep(60000);
					logger.info("Resuming the Execution After 5 Minutes");
				} catch (InterruptedException e) {
					logger.info("InterruptedException" + e);
				}
        	}
        } else {
        	/** Checks For Jobtype and if jobtype is other than a linking job then it Simply Performs scheduled Indexing Action.  */
        	command = data.getString(PROPERTY_JAVA_COMMAND) + data.getString(PROPERTY_PARAMETERS);
            if(command==null||command.trim().length()<=0) return;
            cmd = command.split("\\s");
	        String dir = data.getString(PROPERTY_DIRECTORY);
	        String indexName = data.getString(PROPERTY_INDEX_NAME);
	        String cp = data.getString(PROPERTY_CLASSPATH);
	        if (isRunning(indexName)) {
	        	return;
	        }
	        logger.info("starting : " + command);
	        logger.info("classpath: " + cp);
	        logger.info("directory: " + dir);
	        ForkThread forkthread = new ForkThread(cmd, cp, new java.io.File(dir));
	        logger.info("Starting Timer On Index " + indexName);
	        forkthread.run(); //If use start, The Process Will Return Right Away and Considered Finished In Quartz.
	        logger.info("Ending Timer On Index : " + indexName);
        }
    }

    public boolean isRunning(String indexName) {
    	DatasetConfiguration indexConfiguration = ServerConfiguration.getDatasetConfiguration(indexName);
    	File file = SchedulerTool.getRunningfile(indexConfiguration);
    	int numberOfHoursBeforeDeletion = indexConfiguration.getNumberOfHoursBeforeDeletion();
    	if (SchedulerTool.isRunning(indexConfiguration)) {
			logger.error("Skipping Indexing of " + indexName + " at " + DateTimeFormat.forPattern("MM/DD/yyyy HH:mm:ss:SSS")
				.print(new DateTime()) + " as an action is already Running on this Index");
			DateTime lastModifieddate = new DateTime(new Date (file.lastModified()));
			DateTime currentDate = new DateTime();
			Period period = new Period(lastModifieddate, currentDate, PeriodType.time());
			int gapInHours = period.getHours();
			logger.debug("Time Gap In Hours : " + gapInHours);
			logger.debug("Number Of Hours To Wait Before Deletion: " + numberOfHoursBeforeDeletion);
			if(gapInHours >= numberOfHoursBeforeDeletion) {
				if (file.delete()) {
					logger.debug(file.getAbsolutePath() + " is deleted");
				} else {
					logger.debug(file.getAbsolutePath() + " can not be deleted");
				}
			}
			return true;
      	}
    	return false;
     }
}
