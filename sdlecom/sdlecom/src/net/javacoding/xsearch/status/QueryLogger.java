package net.javacoding.xsearch.status;


import java.io.File;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;

/**
 *  log each query activity into a private log, query analyzer can
 *         create report based on the log
 */
public class QueryLogger {

    private static String LOGGER_NAME = "SEARCH_STATS_LOGGER";

    private static String APPENDER_NAME = "SEARCH_STATS_APPENDER";

    private static final Logger queryLogger = (Logger)LoggerFactory.getLogger(LOGGER_NAME);

    private static final Logger logger = (Logger)LoggerFactory.getLogger(QueryLogger.class);

    /**
     * @param userName
     * @param ip
     * @param keyWord
     * @param indexName
     * @param templateName
     * @param visitTime
     * @param searchingTime
     * @param renderTime
     * @param returnedDoc
     */
    public static void log(String userName, String ip, String keyWord, String indexName, String templateName,
    		long visitTime, long searchingTime, long renderTime, int returnedDoc) {
        String[] output = new String[] { ip, keyWord, indexName, templateName, Long.toString(visitTime),
        	Long.toString(searchingTime), Long.toString(renderTime), Integer.toString(returnedDoc),
        	(userName == null? "EMPTY_USER" : userName)};
        StringBuilder logMessage = new StringBuilder();
        for(String eachOutput : output) {
        	logMessage.append(eachOutput);
        	logMessage.append("|");
        }
        queryLogger.info(logMessage.toString().substring(0, logMessage.length()-1));
    }

    /**
     * return log files used by queryLogger
     *
     * @return file array, the first file is the main log, the second one is the
     *         backup log
     */
    public static File[] getFiles() {
        if (queryLogger == null) return new File[] { null, null };
        RollingFileAppender<ILoggingEvent> appender = (RollingFileAppender<ILoggingEvent>)queryLogger.getAppender(APPENDER_NAME);
        if (appender == null || !(appender instanceof RollingFileAppender)) {
            logger.error("Query appender is null");
            return null;
        }
        String fileName = appender.getFile();
        if (fileName == null) {
            logger.error("Query fileName is null");
            return null;
        }
        File file1 = new File(fileName);
        return new File[] { file1};
    }

    public static void log(QueryRecord record) {
        if (queryLogger != null) {
        	queryLogger.info("{}", new Object[] { record.toArray()});
        }
    }
}