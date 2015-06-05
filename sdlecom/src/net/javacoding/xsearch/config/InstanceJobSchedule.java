package net.javacoding.xsearch.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class InstanceJobSchedule {
	
	private String scheduleName = null;
	 
	private String jobDataString = null;
	
	private int id = 0;
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
	
	Schedule schedule = null;
	
	Map<String, String> modelMap = new LinkedHashMap<String, String>();

	public String getScheduleName() {
		return scheduleName;
	}

	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}

	public String getJobDataString() {
		return jobDataString;
	}

	public void setJobDataString(String jobDataString) {
		this.jobDataString = jobDataString;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}
	
	public Map<String, String> getModelMap() {
		return modelMap;
	}

	public void setModelMap(Map<String, String> modelMap) {
		this.modelMap = modelMap;
	}

	public String toString() {
		
		StringBuffer stringBuffer = new StringBuffer();
					
		stringBuffer.append("   <instance-job-schedule id=\"").append(id).append("\">\n");
		
		if(scheduleName != null && scheduleName.length() > 0) {
		    stringBuffer.append("  <schedule-name><![CDATA[").append(this.scheduleName.trim()).append("]]></schedule-name>\n");
		}
		if(jobDataString != null && jobDataString.length() > 0) {
		    stringBuffer.append("  <job-data-string><![CDATA[").append(this.jobDataString.trim()).append("]]></job-data-string>\n");
		}
		if(schedule != null) {
		    stringBuffer.append(schedule.toString());
		}
		
		stringBuffer.append("    </instance-job-schedule>\n");
		
		return stringBuffer.toString();
	}	
	
	
}
