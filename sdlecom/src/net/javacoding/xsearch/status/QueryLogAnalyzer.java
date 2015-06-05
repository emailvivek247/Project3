/*
 * Created on Jan 25, 2005
 */
package net.javacoding.xsearch.status;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.util.PriorityQueue;

//import org.apache.commons.collections.bidimap.TreeBidiMap;
//import org.apache.commons.collections.*;
/**
 *
 */
public class QueryLogAnalyzer {
    private static Logger logger = LoggerFactory.getLogger(QueryLogAnalyzer.class.getName());

    public static int TOP_NUMBER = 100;
    private static long lastLoadTime;
    private static ArrayList<QueryRecord> records;
    private static HashSet<String> dataSets; // TreeBidiMap keyMap;

    public static void refresh() {
        lastLoadTime = 0;
    }

    public static Date getDataDate() {
        readAllLog();
        Date d = new Date(lastLoadTime);
        return d;
    }

    private static final synchronized void readAllLog() {
        long currentTime = System.currentTimeMillis();
        //if there is no records loaded or time different 1 mins
        if (currentTime - lastLoadTime >  3600 * 1000) {

            dataSets = new HashSet<String>();
            records = new ArrayList<QueryRecord>();
            lastLoadTime = currentTime;

            File[] files = QueryLogger.getFiles();
            if (files != null) {
	            readLogFromFile(files[0]);
            }
        }
    }

    private static final void readLogFromFile(File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            //logger.info("log file doesn't exist:"+file.getAbsolutePath());
            return;
        }
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(file), '|');
            String[] line = null;
            while ((line = reader.getLine()) != null) {
                if (line.length >= QueryRecord.size) {
                    QueryRecord r = new QueryRecord(line);
                    records.add(r);
                    dataSets.add(r.dataSet);
                } else if(line.length == 0){
                } else {
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < line.length; i++) {
                        sb.append(line[i]);
                    }
                    logger.error("wrong format log:" + sb);
                    //continue;
                }
            }
        } catch (Exception e) {
            logger.error("Error happens when load log from file:" + e);
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException e1) {
                logger.error("Error happens when try to close reader:" + e1);
            }
        }
    }

    public String[] getAllDataSets() {
        readAllLog();
        if (dataSets == null) return null;
        int dataSetsNumber = dataSets.size();
        // logger.info("dataSet size="+dataSetsNumber);
        String[] allDataSets = new String[dataSetsNumber];
        int i = 0;
        Iterator<String> it = dataSets.iterator();
        while (it.hasNext()) {
            allDataSets[i] = it.next();
            i++;
        }
        return allDataSets;
    }

    /**
     * return the query report based on query log. create a filter obj if need
     *
     * @return
     */
    public static QueryReport getReport(String dataSetName, long time) {
        return createReport(dataSetName, time);
    }

    public static QueryReport getReport(String dataSetName, String time) {
        long period = 0;
        if (time != null) period = U.getLong(time,0) *60* 1000;
        return createReport(dataSetName, period);
    }

    //filter by dataSetName
    private final static QueryReport createReport(String dataSetName, long time) {
        readAllLog();

        int lineCounter = 0;
        long totalTime = 0;
        //        HashMap keyMap=new HashMap(); //alternation: TreeBidiMap keyMap;
        //        HashMap ipMap=new HashMap();
        KeyWordCounter keyCounter = new KeyWordCounter();
        KeyWordCounter ipCounter = new KeyWordCounter();
        KeyWordCounter noResultSearchCounter = new KeyWordCounter();
        HourCounter hourCounter = new HourCounter();
        boolean searchAll = (dataSetName == null || dataSetName.equals("") || dataSetName.equals("ALL"));
        long currentTime = System.currentTimeMillis();
        long startTime = 0;
        if (records != null)
            for (int i = 0; i < records.size(); i++) {
                QueryRecord r = records.get(i);
                //use filter to match
                if (!searchAll && !dataSetName.equals(r.dataSet)) continue;
                if (time != 0 && currentTime - r.visitTime > time) continue;
                //if matchs
                lineCounter++;
                keyCounter.addKey(r.keyWord);
                ipCounter.addKey(r.ip);
                if(r.returnedDoc<=0) {
                    noResultSearchCounter.addKey(r.keyWord);
                }
                hourCounter.addHit(r.visitTime);
                long searchingTime = r.searchingTime;
                totalTime += searchingTime;
                if (startTime ==0) startTime = r.visitTime;
            }


        //create report
        QueryReport report = new QueryReport();
        if (dataSetName == null)
            report.setReportName("All Indexes");
        else report.setReportName(dataSetName);

        report.setRecordCount(lineCounter);
        if (startTime ==0 )
            startTime = currentTime-time;
        report.setStartTime(startTime);

        if (lineCounter == 0) return report;
        if (lineCounter > 0) report.setAvgSearchingTime(totalTime *1.0f/ lineCounter);

        report.setQueryList(records);
        report.setTopKeyWords(keyCounter.getTopList());
        report.setTopIps(ipCounter.getTopList());
        report.setTopNoResultSearches(noResultSearchCounter.getTopList());
        report.setHourHits(hourCounter.getHourHits(currentTime));

        return report;

    }

    static class KeyWordQueue extends PriorityQueue {
        public KeyWordQueue(int size) {
            initialize(size);
        }

        protected final boolean lessThan(Object a, Object b) {
            KeyWord key1 = (KeyWord) a;
            KeyWord key2 = (KeyWord) b;
            return key1.getHitNumber() < key2.getHitNumber();
        }
    }

    static class KeyWordCounter {
        HashMap<String, KeyWord> keyMap;

        public KeyWordCounter() {
            init();
        }

        public void init() {
            keyMap = new HashMap<String, KeyWord>();
        }

        public void addKey(String key) {
            KeyWord k = keyMap.get(key);
            if (k == null) {//first time
                keyMap.put(key, new KeyWord(key));
            } else {
                k.addHitNumber();
            }
        }

        public KeyWord[] getTopList() {
            int keySize = keyMap.size();
            int listSize = (keySize > TOP_NUMBER) ? TOP_NUMBER : keySize;

            KeyWord[] topKeyWords = new KeyWord[listSize];
            KeyWordQueue keyQueue = new KeyWordQueue(listSize);
            Iterator<KeyWord> it = keyMap.values().iterator();
            while (it.hasNext()) {
                KeyWord key = it.next();
                //logger.info("insert:" + key.getKey() + "(" +
                // key.getHitNumber() + ")");
                keyQueue.insert(key);
            }
            int outputNumber = 0;
            int size = keyQueue.size();
            for (int i = size; i > 0; i--) {
                // while (outputNumber < TOP_NUMBER&&keyQueue.size()>0) {
                KeyWord key = (KeyWord) keyQueue.pop();
                // logger.info("pop:" + key.getKey());
                topKeyWords[i - 1] = key;
            }
            return topKeyWords;
        }
    }

    private static GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();

    /**
     * Util method to calculate hour from time
     *
     * @param time
     * @return
     */
    private static final int getHourFromTime(long time) {
        calendar.setTimeInMillis(time);
        int hour = calendar.get(GregorianCalendar.HOUR_OF_DAY);
        return hour;
    }

    static class HourCounter {
        HourBucket[] hours;

        private void init(long time) {
            //cover to the start of some hour
            long startTime = ((long) (time / 1000 / 3600)) * 1000 * 3600;
            //logger.info("startTime = "+startTime+",time = "+time);
            hours = new HourBucket[24];
            int hour = getHourFromTime(startTime);
            for (int i = 0; i < 24; i++) {
                if (i >= hour)
                    hours[i] = new HourBucket(startTime + 1000 * 3600 * (i - hour));
                else hours[i] = new HourBucket(startTime + 1000 * 3600 * (hour - i + 24));
            }
        }

        public void addHit(long timeStamp) {
            if (hours == null) init(timeStamp);
            int hour = getHourFromTime(timeStamp);
            hours[hour].addHit();
        }

        public HourHit[] getHourHits(long endTime) {
            HourHit[] s = new HourHit[24];
            for (int i = 0; i < 24; i++) {
                s[i] = hours[i].getHourHit(endTime);
            }
            return s;
        }
    }

    static class HourBucket {
        int totalHits = 0;
        long startTime;

        public HourBucket(long time) {
            this.startTime = time;
        }
        public void addHit() {
            totalHits++;
        }
        public HourHit getHourHit(long endTime){
            HourHit hit = new HourHit();
            hit.setDays(getDays(endTime));
            hit.setTotalHits(totalHits);
            return hit;
        }
        int getDays(long endTime){
            if (endTime < startTime) return 0; //this bucket has not been used.
            if (totalHits == 0) return 0;
            double d = (endTime - startTime) *1.0/ 1000 / 60 / 60 / 24;
            int days = (int) (d) + 1;
            return days;
        }
        public float getAvgHits(long endTime) {
            int days = getDays(endTime);
            //logger.info("diff=" + (endTime - startTime) + ",d=" + d + ", days=" + days + ",total=" + totalHits);
            return (float) totalHits / days;
        }
    }

}
