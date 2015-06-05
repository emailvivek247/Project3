package net.javacoding.xsearch.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("time-weight")
public class TimeWeight {

	@XStreamAsAttribute
    private long time;
	
	@XStreamAsAttribute
    private float weight;

	public TimeWeight() {
		super();
	}
	
    public TimeWeight(long time, float weight) {
        this.time = time;
        this.weight = weight;
    }
    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }
    public float getWeight() {
        return weight;
    }
    public void setWeight(float weight) {
        this.weight = weight;
    }
    
    public StringBuffer toXML(){
    	StringBuffer sb = new StringBuffer();
    	sb.append("<time-weight time=\"");
    	sb.append(time);
    	sb.append("\" weight=\"");
    	sb.append(weight);
    	sb.append("\"/>");
    	return sb;
    }
    public StringBuffer toJson(){
    	StringBuffer sb = new StringBuffer();
    	sb.append("[");
    	sb.append(time);
    	sb.append(",");
    	sb.append(weight);
    	sb.append("]");
    	return sb;
    }
}
