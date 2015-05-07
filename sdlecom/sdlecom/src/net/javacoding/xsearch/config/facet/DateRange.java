package net.javacoding.xsearch.config.facet;

import java.util.Date;

import net.javacoding.xsearch.utility.U;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("date-range")
public class DateRange implements FacetRange{
    
	@XStreamAlias("begin")
    @XStreamAsAttribute
    public Date begin;
    
	@XStreamAlias("end")
    @XStreamAsAttribute
    public Date end;
	
	public transient long long_begin = -1;
    
	public transient long long_end = -1;
    
	public DateRange() {
		super();
	}
	
	public long getBeginLong() {
        if(begin==null) return Long.MIN_VALUE;
        if(long_begin==-1) {
            long_begin = begin.getTime();
        }
        return long_begin;
    }
    public long getEndLong() {
        if(end==null) return Long.MAX_VALUE;
        if(long_end==-1) {
            long_end = end.getTime();
        }
        return long_end;
    }

    @Override
    public int hashCode() {
        return (begin==null? 0 : begin.hashCode())*37 + (end==null ? 0 : end.hashCode());
    }
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof DateRange)) return false;
        DateRange other = (DateRange)obj;
        return U.equals(this.begin, other.begin)&&U.equals(this.end, other.end);
    }

    @Override
    public String toString() {
        return "["+(begin==null?"":begin)+","+(end==null?"":end)+")";
    }

    public String toBeginValue() {
        return null;
    }

    public String toEndValue() {
        return null;
    }
}
