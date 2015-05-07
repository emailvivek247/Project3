/*
 * Copyright 2004.
 */

package net.javacoding.xsearch.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Class mapped to the <code>&lt;schedule&gt;</code> element of a dataset
 * configuration file.
 *
 */
public class Schedule implements ConfigConstants {

    // ------------------------------------------------------------- Properties

    /** <code>true</code> if the scheduled action is enabled. */
    @XStreamAsAttribute
    private int id = 0;
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    /** <code>true</code> if the scheduled action is enabled. */
    @XStreamAlias("is-enabled")
    private boolean isEnabled = false;
    public boolean getIsEnabled() { return isEnabled; }
    public void setIsEnabled(boolean isEnabled) { this.isEnabled = isEnabled; }

    /** <code>true</code> if the scheduling is based on interval. */
    @XStreamAlias("is-interval")
    private boolean isInterval = true;
    public boolean getIsInterval() { return isInterval; }
    public void setIsInterval(boolean isInterval) { this.isInterval = isInterval; }

    /** The interval measured in minute. */
    @XStreamAlias("interval")
    private long interval = 5L;
    public long getInterval() { return interval; }
    public void setInterval(long interval) { this.interval = interval; }

    /** A cron job setting in the format of &quot;minute hour day month weekday&quot;. */
    @XStreamAlias("cron-setting")
    private String cronSetting = "30 1 * * ?";
    public String getCronSetting() { return cronSetting; }
    public void setCronSetting(String cronSetting) {
        this.cronSetting = cronSetting;
    }

    /**
     * @return Returns the interned indexingMode.
     */
    @XStreamAlias("indexing-mode")
    private String indexingMode = "Incremental Indexing";
    public String getIndexingMode() {
        if(indexingMode==null) {
            return "Incremental Indexing";
        }
        return indexingMode;
    }
    public void setIndexingMode(String indexingMode) { this.indexingMode = (indexingMode==null?null:indexingMode.intern()); }

    // --------------------------------------------------------- Public Methods

    /**
     * Returns an XML representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("   <schedule id=\"").append(id).append("\">\n");
        sb.append("    <is-enabled>").append(isEnabled).append("</is-enabled>\n");
        sb.append("    <is-interval>").append(isInterval).append("</is-interval>\n");
        sb.append("    <interval>").append(interval).append("</interval>\n");
        if (cronSetting != null) {
            sb.append("    <cron-setting>").append(cronSetting).append("</cron-setting>\n");
        }
        if (indexingMode != null) {
            sb.append("    <indexing-mode>").append(indexingMode).append("</indexing-mode>\n");
        }
        sb.append("   </schedule>\n");

        return sb.toString();
    }

    public String[] getMinutes()    {return cronSetting.split("\\s")[0].split(",") ;}
    public String[] getHours()      {return cronSetting.split("\\s")[1].split(",") ;}
    public String[] getDayOfMonths(){return cronSetting.split("\\s")[2].split(",") ;}
    public String[] getMonths()     {return cronSetting.split("\\s")[3].split(",") ;}
    public String[] getDayOfWeeks() {return cronSetting.split("\\s")[4].split(",") ;}

    public boolean isByDayOfWeek(){
        String[] dayOfMonth = getDayOfMonths();
        return (dayOfMonth!=null&&dayOfMonth.length==1&&"?".equals(dayOfMonth[0]));
    }
}
