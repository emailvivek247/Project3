/*
 * Created on Mar 1, 2005
 *
 */
package net.javacoding.xsearch.status;

/**
 * 
 */
public class HourHit {
    private int days=0;
    private int totalHits=0;    
    /**
     * @param days The days to set.
     */
    public void setDays(int days) {
        this.days = days;
    }
    /**
     * @return Returns the days.
     */
    public int getDays() {
        return days;
    }
    /**
     * @param totalHits The totalHits to set.
     */
    public void setTotalHits(int totalHits) {
        this.totalHits = totalHits;
    }
    /**
     * @return Returns the totalHits.
     */
    public int getTotalHits() {
        return totalHits;
    }
    
    /**
     * @return Returns the avgHits.
     */
    public float getAvgHits() {
        return (float)totalHits/(float)(days==0?1:days);
    }
}
