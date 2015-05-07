/*
 * Created on Jan 25, 2005
 *
 */
package net.javacoding.xsearch.status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.javacoding.xsearch.utility.U;

/**
 *  Query record
 */
class QueryRecord {
    private static Logger logger = LoggerFactory.getLogger(QueryRecord.class.getName());
    String ip;

    String keyWord;

    String dataSet;

    String template;

    long visitTime;

    long searchingTime;

    long renderTime;

    int returnedDoc;

    static int size=8;
    /**
     * 
     * @param ip
     * @param keyWord
     * @param dataSet
     * @param template
     * @param visitTime
     * @param searchingTime
     * @param renderTime
     * @param returnedDoc
     */
    public QueryRecord(String ip, String keyWord, String dataSet,
            String template, long visitTime, long searchingTime, long renderTime,
            int returnedDoc) {
        this.ip = ip;
        this.keyWord = keyWord;
        this.dataSet = dataSet;
        this.template = template;
        this.visitTime = visitTime;
        this.searchingTime = searchingTime;
        this.renderTime = renderTime;
        this.returnedDoc = returnedDoc;
    }

    /**
     * constractor for log reader
     * 
     * @param input
     */
    public QueryRecord(String[] input) {
        this.ip = input[0];
        this.keyWord = input[1];
        this.dataSet = input[2];
        this.template = input[3];
        this.visitTime = U.getLong(input[4], -1);
        this.searchingTime = U.getLong(input[5], -1);
        this.renderTime = U.getLong(input[6], -1);
        this.returnedDoc = U.getInt(input[7], -1);
        if (dataSet==null)
            logger.info("not normal record:"+ip+" "+keyWord);
    }

    public String[] toArray() {
        String[] output = new String[] { ip, keyWord, dataSet, template,
                Long.toString(visitTime), Long.toString(searchingTime), Long.toString(renderTime),
                Integer.toString(returnedDoc) };
        return output;
    }

    public void log() {
        QueryLogger.log(this);
    }
}
