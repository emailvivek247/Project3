package net.javacoding.xsearch.status;

import java.text.DecimalFormat;

import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.component.TextDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class P {

    protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private static final ThreadLocal<DecimalFormat> rateFormatter =
        new ThreadLocal<DecimalFormat>() {
            @Override
            protected DecimalFormat initialValue() {
               return new DecimalFormat("#,###,##0.0");
            }
        };

    private long last_report_time = 0;

    public P() {
        last_report_time = System.currentTimeMillis();
    }

    private double read_doc_rate;
    private double write_doc_rate;
    private double read_size_rate;
    private double write_size_rate;

    private long read_start_size_count = 0;
    private long write_start_size_count = 0;
    private long read_size_count = 0;
    private long write_size_count = 0;
    private long read_start_doc_count = 0;
    private long write_start_doc_count = 0;

    public void r(IndexerContext ic, TextDocument td) {
        p(ic, "r");
        read_size_count += td.getSize();
    }
    public void r(IndexerContext ic, TextDocument[] tds) {
        p(ic, "r");
        for(int i=0;i<tds.length;i++){
            read_size_count += tds[i].getSize();
        }
    }

    public void w(IndexerContext ic, TextDocument td) {
        p(ic, "w");
        write_size_count += td.getSize();
    }

    public void p(IndexerContext ic, String prefix) {
        long now = System.currentTimeMillis();
        if ((now - last_report_time) > 900) {
            long write_now_count = ic.getScheduler().getWriterJobsDone();
            write_doc_rate = (write_now_count - write_start_doc_count) * 1.0 / (now - last_report_time) * 1000;
            write_start_doc_count = write_now_count;
            write_size_rate = (write_size_count - write_start_size_count) * 1.0 / (now - last_report_time);
            write_start_size_count = write_size_count;

            long read_now_count = ic.getScheduler().getFetcherJobsDone();
            read_doc_rate = (read_now_count - read_start_doc_count) * 1.0 / (now - last_report_time) * 1000;
            read_start_doc_count = read_now_count;
            read_size_rate = (read_size_count - read_start_size_count) * 1.0 / (now - last_report_time);
            read_start_size_count = read_size_count;

            last_report_time = now;
            logger.info(
                    prefix
                    + ic.getScheduler().status()
                    + rateFormatter.get().format(read_size_rate)+"K("
                    + rateFormatter.get().format(read_doc_rate)+"):"
                    + rateFormatter.get().format(write_size_rate)+"K("
                    + rateFormatter.get().format(write_doc_rate)+")/s"
                    );
           
        }
    }

}
