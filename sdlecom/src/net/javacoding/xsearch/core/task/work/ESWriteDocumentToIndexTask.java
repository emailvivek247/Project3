package net.javacoding.xsearch.core.task.work;

import io.searchbox.core.Index;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.IndexFieldType;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.component.TextDocument;
import net.javacoding.xsearch.core.task.Scheduler;
import net.javacoding.xsearch.core.task.WorkerTask;
import net.javacoding.xsearch.utility.VMTool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.elasticsearch.util.DocumentBuilder;

public class ESWriteDocumentToIndexTask extends BaseWorkerTaskImpl {

    protected static final Logger logger = LoggerFactory.getLogger(ESWriteDocumentToIndexTask.class);

    private TextDocument document;

    private IndexerContext ic;
    private DatasetConfiguration dc;

    private Optional<String> primaryKeyColumnName;
    private List<Column> columns;

    private BlockingQueue<Index> queue;

    public ESWriteDocumentToIndexTask(Scheduler sched, TextDocument doc, int contextId) {
        super(WorkerTask.WORKERTASK_WRITERTASK, sched);
        this.document = doc;
        this.contextId = contextId;
    }

    @Override
    public void prepare() {
        super.prepare();
        ic = scheduler.getIndexerContext();
        dc = ic.getDatasetConfiguration();
        queue = ic.getQueue();
        if (columns == null) {
            columns = dc.getColumns();
        }
        if (dc.getPrimaryKeyColumn() != null) {
            primaryKeyColumnName = Optional.of(dc.getPrimaryKeyColumn().getColumnName());
        } else {
            primaryKeyColumnName = Optional.empty();
        }
    }

    @Override
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

    public void save(TextDocument doc) {

        Optional<String> primaryKeyValue = primaryKeyColumnName.map(column -> doc.get(column));

        DocumentBuilder docBuilder = new DocumentBuilder();
        for (Column column : columns) {
            String[] values = doc.getValues(column.getColumnName());
            if (values != null) {
                for (String value : values) {
                    if (column.getIndexFieldType() == IndexFieldType.KEYWORD_DATE_HIERARCHICAL) {
                        String dateValue = String.valueOf(VMTool.storedStringToLongValue(value));
                        docBuilder.add(column.getColumnName(), dateValue);
                    } else {
                        docBuilder.add(column.getColumnName(), value);
                    }
                }
            }
        }

        Index.Builder builder = new Index.Builder(docBuilder.buildDocument());
        if (primaryKeyValue.isPresent()) {
            builder = builder.id(primaryKeyValue.get());
        }

        Index index = builder.build();

        boolean written = false;
        while (!ic.isStopping() && !written) {
            try {
                written = queue.offer(index, 1, TimeUnit.MINUTES);
                if (!written) {
                    logger.info("Index queue full. Retrying in 1 minute.");
                }
            } catch (InterruptedException e) {
                // Just ignore and continue to try and add
            }
        }
    }
}
