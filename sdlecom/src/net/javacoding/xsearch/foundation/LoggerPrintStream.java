package net.javacoding.xsearch.foundation;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.slf4j.Logger;

/**
 *  Wrap a Logger as a PrintStream. Useful mainly for porting code that previously logged to System.out or a proxy.
 */
public class LoggerPrintStream extends PrintStream {

    private Logger                logger = null;
    private ByteArrayOutputStream _baos   = null;

    /**
     * Wrap a Logger as a PrintStream .
     * 
     * @param logger
     *            Everything written to this PrintStream will be passed to the appropriate method of the Logger
     * @param level
     *            This indicates which method of the Logger should be called.
     */
    public LoggerPrintStream(Logger logger) {
        super(new ByteArrayOutputStream(), true);
        _baos = (ByteArrayOutputStream) (this.out);
        this.logger = logger;
    }

    // from PrintStream
    public synchronized void flush() {
        super.flush();
        if (_baos.size() == 0) return;
        String out = _baos.toString();

        logit(out);

        _baos.reset();
    }

    // from PrintStream
    public synchronized void println() {
        flush();
    }

    // from PrintStream
    public synchronized void println(Object x) {
        super.print(x); // flush already adds a newline
        flush();
    }

    // from PrintStream
    public synchronized void println(String x) {
        super.print(x); // flush already adds a newline
        flush();
    }

    // from PrintStream
    public synchronized void close() {
        flush();
        super.close();
    }

    // from PrintStream
    public synchronized boolean checkError() {
        flush();
        return super.checkError();
    }

    private synchronized void logit(String s) {
    	if (this.logger.isTraceEnabled()) {
    		this.logger.trace(s);
    	} else if (logger.isDebugEnabled()) {
            this.logger.debug(s);
        } else if (logger.isInfoEnabled()) {
            this.logger.info(s);
        } else if (logger.isWarnEnabled()) {
            this.logger.warn(s);
        } else if (logger.isErrorEnabled()) {
            this.logger.error(s);
        } else {
           return;
        }
    }

}
