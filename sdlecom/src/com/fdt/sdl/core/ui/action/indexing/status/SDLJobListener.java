package com.fdt.sdl.core.ui.action.indexing.status;

import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

public class SDLJobListener implements JobListener {

	private static final String INDEXING = "Indexing";
    private static final String COMPLETED = "Completed";
    private static final String VETOED = "Vetoed";
    
    private String name;
    
    private String status;
    
    private long time;
    
    private Date endTime;
    
    public SDLJobListener(String name, String status, long time) {
		super();
		this.name = name;
		this.status = status;
		this.time = time;
	}

	public String getStatus() {
			return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@Override
	public String getName() {
        return name;
    }

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		status = VETOED;
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		status = INDEXING;
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException arg1) {
		status = COMPLETED;
		endTime = new Date();
		long timeInMilliseconds = (long) context.getJobRunTime();
		time = timeInMilliseconds;
		//time = new BigDecimal(timeInMilliseconds).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
	}
}