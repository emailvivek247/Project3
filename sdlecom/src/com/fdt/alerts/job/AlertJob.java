package com.fdt.alerts.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class AlertJob implements Job {

	public static String PROPERTY_JAVA_COMMAND = "javaCommand";
	
	public static String PROPERTY_PARAMETERS = "parameters";
	
	public static String PROPERTY_DIRECTORY = "dir";
	
	public static String PROPERTY_CLASSPATH = "cp";

	private static Logger logger = LoggerFactory.getLogger(AlertJob.class);

	public void execute(JobExecutionContext context) throws JobExecutionException {

		JobDataMap data = context.getJobDetail().getJobDataMap();
		String command = data.getString(PROPERTY_JAVA_COMMAND);
		if (command == null || command.trim().length() <= 0)
			return;
		String[] cmd = command.split("\\s");
		String[] env = new String[1];
		String dir = data.getString(PROPERTY_DIRECTORY);
		String cp = data.getString(PROPERTY_CLASSPATH);
		env[0] = "CLASSPATH=" + cp;
		ForkAlertThread ft = new ForkAlertThread(cmd, cp, new java.io.File(dir));
		ft.run();
	}

}
