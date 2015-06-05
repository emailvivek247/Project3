package net.javacoding.xsearch.utility;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.Schedule;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.exception.ConfigurationException;
import net.javacoding.xsearch.foundation.InMemoryJob;
import net.javacoding.xsearch.foundation.NativeForkJob;
import net.javacoding.xsearch.foundation.WebserverStatic;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.sdl.core.ui.action.indexing.status.SDLJobListener;

/**
 * created for scheduling jobs, now is using quartz
 */

public final class SchedulerTool {
    private static Logger  logger                = LoggerFactory.getLogger("net.javacoding.xsearch.utility.SchedulerTool");

    private static boolean performQuartzShutdown = true;

    public static File  logDirectory             = null;
    static {
        try {
            // has to be absolute, it's called by the webserver and by the ant
            // job
            // they have different meaning for current directory "."
            logDirectory = new File(ServerConfiguration.getServerConfiguration().getBaseDirectory().getCanonicalPath(),"log");
        } catch (NullPointerException ce) {
            logger.error("NullPointerException", ce);
        } catch (IOException ce) {
            logger.error("IOException", ce);
        }
    }

    private static boolean canNotStart = false;

    public static boolean start(DatasetConfiguration dc) {
        File f = getRunningfile(dc);
        try {
            if (!f.exists()) {
                canNotStart = !f.createNewFile();
                logger.debug("Created running file " + f);
            } else {
                logger.error("Another thread that created " + f + " is running");
                canNotStart = true;
            }
        } catch (IOException ioe) {
            logger.error(NOTIFY_ADMIN, "Can not create running file " + f + " for " + dc.getName());
            canNotStart = true;
        }
        return !canNotStart;
    }

    public static boolean setStatus(IndexerContext ic, String status) {
        File f = getRunningfile(ic.getDatasetConfiguration());
        try {
            if (f != null && f.exists()) {
                Writer w = new FileWriter(f);
                w.write(status);
                w.close();
                return true;
            } else {
                logger.error("Process file does not exist!:" + f);
            }
        } catch (IOException ioe) {
            logger.error(NOTIFY_ADMIN, "Can not create running file " + f + " for " + ic.getDatasetConfiguration().getName());
        }
        return false;
    }

    /**
     * called from the running indexing process
     */
    public static boolean isRunning(DatasetConfiguration dc) {
        if (canNotStart) return false;
        return getRunningfile(dc).exists();
    }

    private static int runningCheckInterval = 1000;
    private static int runnintCheckCounter = 1000;
    private static boolean runnintCheckResult = true;
    public static boolean isRunning(IndexerContext ic) {
        if (canNotStart) return false;
        if(runnintCheckCounter++>=runningCheckInterval&&!runnintCheckResult){
            runnintCheckResult = getRunningfile(ic.getDatasetConfiguration()).exists();
            runnintCheckCounter = 0;
        }
        return runnintCheckResult;
    }
    
    public static void stop(DatasetConfiguration dc) {
        logger.info("stopping indexing for " + dc.getName());
        getRunningfile(dc).delete();
    }
    public static void stop(IndexerContext ic) {
        getRunningfile(ic.getDatasetConfiguration()).delete();
    }

    protected static HashMap<String, File> indexRunningFlagFiles = new HashMap<String, File>();

    public static File getRunningfile(DatasetConfiguration dc) {
    	String stopFileName = dc.getName();
        File runningFile = indexRunningFlagFiles.get(stopFileName);
        if (runningFile == null) {
            synchronized (indexRunningFlagFiles) {
                // check it again if some other thread has done the change while
                // this thread is waiting
                runningFile = indexRunningFlagFiles.get(stopFileName);
                if (runningFile == null) {
                    runningFile = new File(logDirectory, stopFileName + ".pid");
                    indexRunningFlagFiles.put(stopFileName, runningFile);
                    runningFile.deleteOnExit();
                }
            }
        }
        return runningFile;
    }

    public static void stopAllIndexes() {
        ArrayList<DatasetConfiguration> al = null;
        try {
            al = ServerConfiguration.getDatasetConfigurations();
        } catch (ConfigurationException ce) {
            logger.error(NOTIFY_ADMIN, "Can not read dataset configurations!");
        }
        for (int i = 0; al != null && i < al.size(); i++) {
            stop(al.get(i));
        }
    }

    /**
     * called in ApplicationInitServlet.destroy()
     */
    public static void destroy() {
        if (!performQuartzShutdown) return;
        try {
            stopAllIndexes();
            Scheduler sched = WebserverStatic.getScheduler();
            if (sched != null) sched.shutdown();
        } catch (Exception e) {
            logger.error("Scheduler failed to shutdown cleanly: " + e.toString());
            e.printStackTrace();
        }
        logger.info("Scheduler is successfully shutdown.");
    }

    public static void autoFixingScheduleIds() throws IOException {
        try {
            for(DatasetConfiguration dc : ServerConfiguration.getDatasetConfigurations()){
                if(dc.getSchedules()!=null&&dc.getSchedules().size()>1) {
                    for(int i=0;i<dc.getSchedules().size();i++) {
                        dc.getSchedules().get(i).setId(i);
                    }
                    dc.save();
                }
            }
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }
    public static void init(String configFile) throws IOException {
        //logger.info("Initializing Scheduler...");
        autoFixingScheduleIds();
        Scheduler sched = initQuartz(configFile);
        if (sched == null) {
            logger.error(NOTIFY_ADMIN, "Failed to initialize Scheduler.");
            return;
        }
        WebserverStatic.setScheduler(sched);
        if(ServerConfiguration.getServerConfiguration().getAllowedLicenseLevel()>0) {
            logger.info("Initializing scheduled jobs...");
            initScheduledJobs();
        }
    }

    private static Scheduler initQuartz(String configFile) {
        StdSchedulerFactory factory;
        try {
            String shutdownPref = null;// cfg.getInitParameter("scheduler-shutdown-on-unload");

            if (shutdownPref != null) performQuartzShutdown = Boolean.getBoolean(shutdownPref);

            // get Properties
            if (configFile != null) {
                factory = new StdSchedulerFactory(configFile);
            } else {
                factory = new StdSchedulerFactory();
            }

            Scheduler scheduler = factory.getScheduler();
            scheduler.start();

            return scheduler;

        } catch (Exception e) {
            logger.error("Scheduler failed to initialize: " + e.toString());
            return null;
        }
    }

    private static void initScheduledJobs() {
        try {
            for (DatasetConfiguration dc : ServerConfiguration.getDatasetConfigurations()) {
                scheduleIndexingJob(dc);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Scheduler failed to add scheduled jobs: " + e.toString());
        }
    }

    public static void scheduleIndexingJob(DatasetConfiguration dc) {
        for(Schedule s: dc.getSchedules()) {
            scheduleIndexingJob(dc,s);
        }
    }
    /** referenced in ChangeScheduleAction and SchedulerTool.initScheduledJobs */
    public static void scheduleIndexingJob(DatasetConfiguration indexConfig, Schedule schedule) {
        if (indexConfig == null) return;
        if(schedule==null) return;

        String jobName = schedule.getIndexingMode();
        String groupName = indexConfig.getName();
        try {
            Trigger trigger = null;
            Scheduler scheduler = WebserverStatic.getScheduler();
            if(scheduler == null) return;

            if (!schedule.getIsEnabled()) {
                // remove existing jobs
                scheduler.deleteJob(new JobKey(jobName, groupName));
            } else {
                if (schedule.getIsInterval()) {
                	/* (a) withMisfireHandlingInstructionNowWithRemainingCount():
	                	    (i) This setting will skip the misfired jobs. 
	                	    (ii) It does not wait for the next schedule time after current job is finished executing.
                	   (b) withMisfireHandlingInstructionNextWithRemainingCount():
	                	    (i) This setting will skip the misfired jobs. 
	                	    (ii) It does wait for the next schedule time after current job is finished executing.
                	   (c) withMisfireHandlingInstructionFireNow(): This setting will not wait for the next scheduled time 
                	        if there is a misfired job. It will execute immediately after the current job is 
                	        finished executing. 
                	 */
                    //trigger = new SimpleTriggerImpl(triggerName, groupName, new java.util.Date(System.currentTimeMillis() + 60 * 1000), null, SimpleTrigger.REPEAT_INDEFINITELY, schedule.getInterval() * 1000 * 60);
                    trigger = (SimpleTrigger) newTrigger() 
                		.withIdentity(jobName, groupName)
                		.withSchedule(simpleSchedule()
                						.repeatForever()
                						.withIntervalInMilliseconds(schedule.getInterval() * 1000 * 60))
                		.startAt(new java.util.Date(System.currentTimeMillis() + 60 * 1000))
                		.build();
                } else {
                	/** withMisfireHandlingInstructionDoNothing() -- This setting will skip the misfired jobs. 
                	 *  It will wait for the next schedule time after current job is finished executing.
                	 *  withMisfireHandlingInstructionFireAndProceed() -- This setting will not wait for the next scheduled 
                	 *  time if there is a misfired job. It will execute immediately after the current
                	 *  job is finished executing.
                	 * */
                    trigger = newTrigger()
                    	.withIdentity(jobName, groupName)
                    	.withSchedule(cronSchedule("0 " + schedule.getCronSetting()))
                    	.build();
                    //trigger = new CronTriggerImpl(triggerName, groupName, jobName, groupName, "0 " + schedule.getCronSetting());
                }
                scheduleAJob(indexConfig,jobName,groupName,WebserverStatic.getWebConfiguration().getCommand(schedule.getIndexingMode()),trigger);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Scheduler failed to schedule Indexing Job for " + jobName + "(" + schedule.getCronSetting() + "): " + e.toString());
        }
    }

    /**
     * Used in dashboard -> ScheduleAJobAction
     */
    public static void scheduleAJob(DatasetConfiguration dc, String jobName, String groupName, String theCommand) {
        if (dc == null) return;
        SimpleTrigger trigger = (SimpleTrigger) newTrigger() 
        	.withIdentity(jobName, groupName)
        	.startAt(new java.util.Date(System.currentTimeMillis()+5*1000))
        	.build();
        scheduleAJob(dc, jobName, groupName, theCommand, trigger);
    }

    private static void scheduleAJob(DatasetConfiguration dc, String jobName, String groupName, String theCommand, Trigger trigger) {
        if (dc == null) return;
        try {
            Scheduler sched = WebserverStatic.getScheduler();
        	long time = 0;
    		String status = "";
    		
            // remove existing jobs
            sched.deleteJob(new JobKey(jobName, groupName));

            Class c = NativeForkJob.class;
            if(theCommand != null && theCommand.startsWith("inMemory") ) {
                c = InMemoryJob.class;
            }
           
            JobDetail jobDetail = newJob(c).withIdentity(jobName, groupName).build();
            jobDetail.getJobDataMap().put(NativeForkJob.PROPERTY_JAVA_COMMAND, WebserverStatic.getJavaCommand(dc.getName(), 
            		dc.getJvmMaxHeapSize()));
            jobDetail.getJobDataMap().put(NativeForkJob.PROPERTY_PARAMETERS, theCommand);
            jobDetail.getJobDataMap().put(NativeForkJob.PROPERTY_CLASSPATH, WebserverStatic.getClassPath());
            jobDetail.getJobDataMap().put(NativeForkJob.PROPERTY_DIRECTORY, WebserverStatic.getJobWorkingDirectory());
            jobDetail.getJobDataMap().put(NativeForkJob.PROPERTY_INDEX_NAME, dc.getName());
          
            /* Registering a Job Listener to get job execution status & time. 
             * groupName is Index Name, and jobName is Indexing action. */
            SDLJobListener sdlJobListener = new SDLJobListener(groupName+jobName, status, time);
			sched.getListenerManager().addJobListener(sdlJobListener, KeyMatcher.keyEquals(new JobKey(jobName, groupName)));
            sched.scheduleJob(jobDetail, trigger);
            logger.info("Scheduled " + theCommand + " " + dc.getName()+" at "+ trigger.getNextFireTime());
        } catch (SchedulerException e) {
            e.printStackTrace();
            logger.error("Scheduler failed to schedule A Job " + theCommand + " for " + dc.getName() + ": " + e.toString());
        }
    }

    /** referenced in StopIndexingAction */
    public static void stopIndexingJob(DatasetConfiguration dc) {
        if (dc == null) return;
        try {
            // stop current
            stop(dc);
        } catch (Exception e) {
            logger.error("Scheduler failed to stop scheduled jobs: " + e.toString());
            e.printStackTrace();
        }
    }
}
