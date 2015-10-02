package net.javacoding.xsearch.core.task.work;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.component.TextDocument;
import net.javacoding.xsearch.core.task.Scheduler;
import net.javacoding.xsearch.core.task.WorkerTask;
import net.javacoding.xsearch.core.task.work.util.DocumentSQLTaskHelper;
import net.javacoding.xsearch.status.IndexStatus;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

public class ESWriteDocumentToIndexTask extends BaseWorkerTaskImpl {

    @Autowired
    private RestTemplate restTemplate = null;
    
    protected static final Logger logger = LoggerFactory.getLogger(ESWriteDocumentToIndexTask.class);

    protected transient IndexerContext ic;
    protected TextDocument document;
    protected static List<Column> columns;
    private IndexWriter _writer;
    private String pkColumnName;

    public ESWriteDocumentToIndexTask(Scheduler sched, TextDocument doc, int contextId) {
        super(WorkerTask.WORKERTASK_WRITERTASK, sched);
        this.document = doc;
        this.contextId = contextId;
    }

    public void prepare() {
        super.prepare();
        this.ic = scheduler.getIndexerContext();
        if (columns == null) {
            columns = this.ic.getDatasetConfiguration().getColumns();
        }
        Column primaryKeyColumn = ic.getDatasetConfiguration().getPrimaryKeyColumn();
        pkColumnName = primaryKeyColumn == null ? null : primaryKeyColumn.getColumnName();
        try {
            _writer = ic.getIndexWriterProvider().getIndexWriter();
        } catch (Exception e) {
            logger.error("Error during creating index writer", e);
        }
    }

    public void execute() {
        try {
            if (!ic.isStopping()) {
                save(document);
            }
            
        } catch (OutOfMemoryError oom) {
            logger.error("Out of memory, stopping current indexing", oom);
            logger.error(NOTIFY_ADMIN, "Out of memory, stopping current indexing", oom);
            IndexStatus.setError(ic.getDatasetConfiguration().getName(), "Indexing is out of memory!");
            ic.setStopping();
        } catch (Throwable t) {
            logger.error("Error during save", t);
            ic.setStopping();
        }
    }

    public void save(TextDocument doc) throws Throwable {
        boolean written = false;
        int triedTimes = 0;
        while (!ic.isStopping() && !written && triedTimes < 15) {
            try {

                JestClientFactory factory = new JestClientFactory();
                factory.setHttpClientConfig(new HttpClientConfig
                                       .Builder("http://localhost:9200")
                                       .multiThreaded(true)
                                       .build());
                JestClient client = factory.getObject();
                
                Map<String, String> source = new LinkedHashMap<String,String>();
               for(Column column : columns) {
                   String[] values = doc.getValues(column.getColumnName());
                   if (values != null) {
                       for (String value : values) {
                          source.put(column.getColumnName(), value);
                       }
                   }
               }

                Index index = new Index.Builder(source).index("test").type("test").build();
                client.execute(index);

                written = true;
            } catch (java.util.NoSuchElementException nsee) {
                logger.info("Didn't get IndexWriter from the pool. Trying again.");
                triedTimes++;
                try {
                    Thread.sleep(239);
                } catch (Exception e) {

                }
            } catch (IOException ioe) {
                triedTimes++;
                try {
                    Thread.sleep(239);
                } catch (Exception e) {

                }
            }
        }
    }
}
