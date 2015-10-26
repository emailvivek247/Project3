package net.javacoding.xsearch.core.task.work;

import io.searchbox.client.JestClient;
import io.searchbox.core.Index;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.component.TextDocument;
import net.javacoding.xsearch.core.task.Scheduler;
import net.javacoding.xsearch.core.task.WorkerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.elasticsearch.config.SpringContextUtil;

public class ESWriteDocumentToIndexTask extends BaseWorkerTaskImpl {

    protected static final Logger logger = LoggerFactory.getLogger(ESWriteDocumentToIndexTask.class);

    private TextDocument document;
    
    private IndexerContext ic;
    private DatasetConfiguration dc;
    private List<Column> columns;

    private JestClient jestClient;

    public ESWriteDocumentToIndexTask(Scheduler sched, TextDocument doc, int contextId) {
        super(WorkerTask.WORKERTASK_WRITERTASK, sched);
        this.document = doc;
        this.contextId = contextId;
    }

    public void prepare() {
        super.prepare();
        ic = scheduler.getIndexerContext();
        dc = ic.getDatasetConfiguration();
        if (columns == null) {
            columns = dc.getColumns();
        }
        jestClient = SpringContextUtil.getBean(JestClient.class);
    }

    public void execute() {
        try {
            if (!ic.isStopping()) {
                save(document);
            }
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

                String indexName = dc.getName();

                Map<String, String> source = new LinkedHashMap<String, String>();
                for (Column column : columns) {
                    String[] values = doc.getValues(column.getColumnName());
                    if (values != null) {
                        for (String value : values) {
                            source.put(column.getColumnName(), value);
                        }
                    }
                }

                Index index = new Index.Builder(source).index(indexName).type(indexName).build();
                jestClient.execute(index);

                written = true;

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
