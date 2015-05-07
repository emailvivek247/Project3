package net.javacoding.xsearch.status;

import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryReport {
    private static Logger logger = LoggerFactory.getLogger(QueryReport.class.getName());
    String reportName;

    float avgSearchingTime;

    KeyWord[] topKeyWords;
    KeyWord[] topIps;
    KeyWord[] topNoResultSearches;
    HourHit[]  hourHits;
    float topHits;
    int recordCount;
    private long startTime;
    private int days;

    ArrayList queryList;
    
    public void setReportName(String rName) {
        reportName = rName;
    }

    public String getReportName() {
        return reportName;
    }

    public void setRecordCount(int count) {
        this.recordCount = count;
    }

    public long getRecordCount() {
        return recordCount;
    }

    public void setAvgSearchingTime(float time) {
        avgSearchingTime = time;
    }

    public float getAvgSearchingTime() {
        return avgSearchingTime;
    }

    public void setTopKeyWords(KeyWord[] words) {
        topKeyWords = words;
    }

    public KeyWord[] getTopKeyWords() {
        return topKeyWords;
    }
  
    public void setQueryList(ArrayList list){
       queryList = list;
    }
    
    public ArrayList getQueryList(){
        return queryList;
    }
    
    public void save(HttpServletRequest request){
        logger.info("Save into session: "+this.toString());
        request.getSession().setAttribute("queryReport",this);        
    }
    
    
    public String toString(){
        StringBuffer sb= new StringBuffer();
        sb.append("reportName="+this.getReportName()+"\n");
        sb.append("count="+this.getRecordCount()+"\n");
        sb.append("avg. searching time="+this.getAvgSearchingTime()+"\n");        
        if (topKeyWords!=null)
        {
            sb.append("Top keywords("+topKeyWords.length+"):\n");
          for (int i=0;i<topKeyWords.length&&topKeyWords[i]!=null;i++)
            sb.append("top"+i+"="+topKeyWords[i].getKey()+"("+topKeyWords[i].getHitNumber()+")\n");
        }
        return sb.toString();
    }

    /**
     * @param topIps The topIps to set.
     */
    public void setTopIps(KeyWord[] topIps) {
        this.topIps = topIps;
    }

    /**
     * @return Returns the topIps.
     */
    public KeyWord[] getTopIps() {
        return topIps;
    }

    /**
     * @return Returns the hourHits.
     */
    public HourHit[] getHourHits() {
        return hourHits;
    }
    /**
     * @param hourHits The hourHits to set.
     */
    public void setHourHits(HourHit[] hourHits) {
        
        float max = 0;
        for (int i=0;i<hourHits.length;i++)
        if (hourHits[i].getAvgHits()>max) max=hourHits[i].getAvgHits();
        this.hourHits = hourHits;
        this.topHits = max;
       // logger.info("topHits="+max);
    }
    /**
     * @return Returns the topHits.
     */
    public float getTopHits() {
        return topHits;
    }

    /**
     * @param startTime The startTime to set.
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * @return Returns the startTime.
     */
    public Date getStartDate() {
        return new Date(startTime);
    }

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

    public KeyWord[] getTopNoResultSearches() {
        return topNoResultSearches;
    }

    public void setTopNoResultSearches(KeyWord[] topNoResultSearches) {
        this.topNoResultSearches = topNoResultSearches;
    }

}
