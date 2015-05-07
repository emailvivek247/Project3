/**
 * 
 */
package net.javacoding.xsearch.config;

import org.joda.time.LocalDateTime;

public class NonPeakHours{
    boolean enabled;
    int begin = 1;
    int end = 3;
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public void setBegin(int begin) {
        this.begin = begin;
    }
    public void setEnd(int end) {
        this.end = end;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public int getBegin() {
        return begin;
    }
    public int getEnd() {
        return end;
    }
    public boolean isOK() {
        if(!enabled) return true;
        LocalDateTime dt = new LocalDateTime();
        int hours = dt.getHourOfDay();
        if(begin>end) {
            if(0<=hours && hours < end || begin<=hours && hours < 24) {
                return false;
            }
        }else {
            if(hours >= begin && hours < end) {
                return true;
            }
        }
        return false;
    }
}