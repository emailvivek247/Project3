package net.javacoding.xsearch.core.task.work;

import io.searchbox.client.JestClient;
import io.searchbox.core.Index;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.component.TextDocument;
import net.javacoding.xsearch.core.task.Scheduler;
import net.javacoding.xsearch.core.task.WorkerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.elasticsearch.config.SpringContextUtil;
import com.fdt.elasticsearch.util.JestExecute;

public class ESWriteDocumentToIndexTask extends BaseWorkerTaskImpl {

    protected static final Logger logger = LoggerFactory.getLogger(ESWriteDocumentToIndexTask.class);

    private TextDocument document;

    private IndexerContext ic;
    private DatasetConfiguration dc;

    private Optional<String> primaryKeyColumnName;
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
        if (dc.getPrimaryKeyColumn() != null) {
            primaryKeyColumnName = Optional.of(dc.getPrimaryKeyColumn().getColumnName());
        } else {
            primaryKeyColumnName = Optional.empty();
        }
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

                Optional<String> primaryKeyValue = primaryKeyColumnName.map(column -> doc.get(column));

                Map<String, String> source = new LinkedHashMap<String, String>();
                for (Column column : columns) {
                    String[] values = doc.getValues(column.getColumnName());
                    if (values != null) {
                        for (String value : values) {
                            source.put(column.getColumnName(), value);
                        }
                    }
                }

                String aliasName = dc.getName();
                String indexName = ic.getTargetIndexName();

                Index.Builder builder = new Index.Builder(source).index(indexName).type(aliasName);
                if (primaryKeyValue.isPresent()) {
                    builder = builder.id(primaryKeyValue.get());
                }
                Index request = builder.build();

                JestExecute.execute(jestClient, request);

                written = true;

            } catch (Throwable t) {
                triedTimes++;
                try {
                    Thread.sleep(239);
                } catch (InterruptedException e) {
                    // Ignore InterruptedException
                }
            }
        }
    }
}
