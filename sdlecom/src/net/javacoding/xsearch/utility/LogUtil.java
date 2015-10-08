package net.javacoding.xsearch.utility;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.LoggingConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.core.exception.ConfigurationException;
import net.javacoding.xsearch.search.HTMLEntities;
import net.javacoding.xsearch.status.QueryLogger;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.net.SMTPAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.spi.CyclicBufferTracker;

public final class LogUtil {
	
	private static final Logger logger = (Logger)LoggerFactory.getLogger(QueryLogger.class);
	
	public static void setLog(DatasetConfiguration indexConfiguration) {
		ServerConfiguration serverConfiguration = ServerConfiguration.getServerConfiguration();
		if(serverConfiguration.getLoggingConfiguration() != null &&	
				serverConfiguration.getIsShortIndexingLogEnabled()) { 
			setIndexShortLog(indexConfiguration, serverConfiguration);
		}
		setIndexLongLog(indexConfiguration, serverConfiguration);
		if(serverConfiguration.getLoggingConfiguration() != null &&	
				serverConfiguration.getLoggingConfiguration().getIsEnabled() && 
					!"OFF".equals(serverConfiguration.getLoggingConfiguration().getThreshold())) {
			logger.debug("Setting The SMTP Appender");
			setIndexSMPTPLog(indexConfiguration, serverConfiguration);
		}
	}
	
    public static void unsetLog(DatasetConfiguration indexConfiguration) {
		Logger logbackLogger = (Logger)LoggerFactory.getLogger(ROOT_LOGGER_NAME);
    	Appender<ILoggingEvent> shortFileAppender = logbackLogger.getAppender(indexConfiguration.getName() + "_short.log");
    	Appender<ILoggingEvent> longFileAppender = logbackLogger.getAppender(indexConfiguration.getName() + "_long.log");
    	Appender<ILoggingEvent> smtpAppender = logbackLogger.getAppender(indexConfiguration.getName() + "SMTP");
    	if (shortFileAppender != null) {
    		shortFileAppender.stop();
    		logbackLogger.detachAppender(shortFileAppender);
    	}
    	if (longFileAppender != null) {
    		longFileAppender.stop();
    		logbackLogger.detachAppender(longFileAppender);
    	}
    	if (smtpAppender != null) {
    		smtpAppender.stop();
        	logbackLogger.detachAppender(smtpAppender);
    	}
    }
    
    public static String getShortLogName(String indexName) throws ConfigurationException{
        ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
        return sc.getBaseDirectory() + "/log/" + indexName + "_short.log";
    }
    
    public static String getLongLogName(String indexName) throws ConfigurationException{
        ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
        return sc.getBaseDirectory() + "/log/" + indexName + "_long.log";
    }
    
    public static String getShortLogContent(String indexName, String lineSep) throws ConfigurationException, IOException {
        StringBuffer sb = new StringBuffer();
        String logName = getShortLogName(indexName);
        File log_1 = new File(logName+".1");
        File log = new File(logName);
        BufferedReader br = null;
        if(log_1.exists()){
            try{
                br = new BufferedReader(new FileReader(log_1));
                String nextLine = "";
                while ((nextLine = br.readLine()) != null) {
                    sb.append(HTMLEntities.encode(nextLine));
                    // note:BufferedReader strips the EOL character.
                    sb.append(lineSep);
                }
            }catch(java.io.FileNotFoundException fnfe){
            }finally{
                br.close();
            }
        }
        if(log.exists()){
            try{
                br = new BufferedReader(new FileReader(log));
                String nextLine = "";
                while ((nextLine = br.readLine()) != null) {
                    sb.append(HTMLEntities.encode(nextLine));
                    // note:BufferedReader strips the EOL character.
                    sb.append(lineSep);
                }
            }catch(java.io.FileNotFoundException fnfe){
            }finally{
                br.close();
            }
        }
        return sb.toString();
    }
    
    public static boolean existsLongLog(String indexName) throws ConfigurationException{
        File f = new File(getLongLogName(indexName));
        return f.exists();
    }
    
    public static String getLongLogContent(String indexName) throws ConfigurationException, IOException {
        StringBuffer sb = new StringBuffer();
        String logName = getLongLogName(indexName);
        File log = new File(logName);
        String lineSep = System.getProperty("line.separator");;
        if(log.exists()){
            BufferedReader br = new BufferedReader(new FileReader(log));
            try {
                String nextLine = "";
                while ((nextLine = br.readLine()) != null) {
                    sb.append(nextLine);
                    // note:BufferedReader strips the EOL character.
                    sb.append(lineSep);
                }
            }finally {
                br.close();
            }
        }
        return sb.toString();
    }
    
    private static void setIndexShortLog(DatasetConfiguration indexConfiguration, ServerConfiguration serverConfiguration) {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		
		/** Create a Rolling File Appender **/
		RollingFileAppender<ILoggingEvent>  rfAppender = new RollingFileAppender<ILoggingEvent>();
		rfAppender.setName(indexConfiguration.getName() + "_short.log");
		rfAppender.setContext(loggerContext);
		rfAppender.setFile(serverConfiguration.getBaseDirectory() + "/log/" + indexConfiguration.getName() + "_short.log");
		
		/**Create a Rolling Policy **/
		FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
		rollingPolicy.setContext(loggerContext);
		rollingPolicy.setParent(rfAppender);
		rollingPolicy.setFileNamePattern(serverConfiguration.getBaseDirectory() + "/log/" + indexConfiguration.getName() 
			+ "_short.log.%i");
		rollingPolicy.setMinIndex(1);
		rollingPolicy.setMaxIndex(1);
		rollingPolicy.start();
		
		SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
		triggeringPolicy.setMaxFileSize("1KB");
		triggeringPolicy.start();
		
		/** Create The Message Format **/
		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(loggerContext);
		encoder.setPattern("%-5level %d{MM-dd-yy HH:mm:ss.SSS} : %msg%n");
		encoder.start();
		
		rfAppender.setEncoder(encoder);
		rfAppender.setRollingPolicy(rollingPolicy);
		rfAppender.setTriggeringPolicy(triggeringPolicy);
		rfAppender.start();
		
		Logger logbackLogger = (Logger)LoggerFactory.getLogger(ROOT_LOGGER_NAME);
		/** Add The Appender to the Root Logger **/
		logbackLogger.addAppender(rfAppender);
	}
	
	private static void setIndexLongLog(DatasetConfiguration indexConfiguration, ServerConfiguration serverConfiguration) {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		
		/** Create a Rolling File Appender **/
		RollingFileAppender<ILoggingEvent>  rfAppender = new RollingFileAppender<ILoggingEvent>();
		rfAppender.setName(indexConfiguration.getName() + "_long.log");
		rfAppender.setContext(loggerContext);
		rfAppender.setFile(serverConfiguration.getBaseDirectory() + "/log/" + indexConfiguration.getName() + "_long.log");
		
		/**Create a Rolling Policy **/
		FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
		rollingPolicy.setContext(loggerContext);
		rollingPolicy.setParent(rfAppender);
		rollingPolicy.setFileNamePattern(serverConfiguration.getBaseDirectory() + "/log/" + indexConfiguration.getName() 
			+ "_long.log.%i");
		rollingPolicy.setMinIndex(1);
		rollingPolicy.setMaxIndex(1);
		rollingPolicy.start();
		
		SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
		triggeringPolicy.setMaxFileSize(serverConfiguration.getIndexingLogSizeInMB()+"MB");
		triggeringPolicy.start();
		
		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(loggerContext);
		encoder.setPattern("%-5level %d{MM-dd-yy HH:mm:ss.SSS} : %msg%n");
		encoder.start();
		
		rfAppender.setEncoder(encoder);
		rfAppender.setRollingPolicy(rollingPolicy);
		rfAppender.setTriggeringPolicy(triggeringPolicy);
		rfAppender.start();
		
		Logger logbackLogger = (Logger)LoggerFactory.getLogger(ROOT_LOGGER_NAME);
		/** Add The Appender to the Root Logger **/
		logbackLogger.addAppender(rfAppender);
		
	} 	    
    
	private static void setIndexSMPTPLog(DatasetConfiguration indexConfiguration, ServerConfiguration serverConfiguration) {

		LoggingConfiguration loggingConfiguration = serverConfiguration.getLoggingConfiguration();
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		
		/** Create a SMTP Appender **/
		SMTPAppender sMTPAppender = new SMTPAppender();
		sMTPAppender.setName(indexConfiguration.getName() + "SMTP");
		sMTPAppender.setContext(loggerContext);
		sMTPAppender.setSMTPHost(loggingConfiguration.getSmtpHost());
		sMTPAppender.setFrom(loggingConfiguration.getFromAddress());
		sMTPAppender.addTo(loggingConfiguration.getToAddress());
		sMTPAppender.setSubject("Indexing Log For " + indexConfiguration.getName());
		
		/** Create a Message Format **/
		PatternLayout layout = new PatternLayout();
		layout.setContext(loggerContext);
		layout.setPattern("%-5level %d{MM-dd-yy HH:mm:ss.SSS} : %msg%n");
		layout.start();
		
		CyclicBufferTracker<ILoggingEvent> cyclicBufferTracker = new CyclicBufferTracker<ILoggingEvent>();
		cyclicBufferTracker.setBufferSize(2048);
		
		sMTPAppender.setCyclicBufferTracker(cyclicBufferTracker);
		sMTPAppender.setLayout(layout);
		sMTPAppender.start();
		
		Logger logbackLogger = (Logger)LoggerFactory.getLogger(ROOT_LOGGER_NAME);
		
		/** Add The Appender to the Root Logger **/
		logbackLogger.addAppender(sMTPAppender);
	}        
}