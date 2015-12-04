package net.javacoding.xsearch.core.task.work;

import io.searchbox.client.JestClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.elasticsearch.config.SpringContextUtil;
import com.fdt.elasticsearch.util.JestExecute;
import com.google.common.collect.Queues;

public class ESIndexConsumer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ESWriteDocumentToIndexTask.class);

    private static final int BATCH_SIZE = 2000;

    private BlockingQueue<Index> queue;
    private String indexName;
    private String typeName;

    private boolean running;
    private JestClient jestClient;

    public ESIndexConsumer(BlockingQueue<Index> queue, String indexName, String typeName) {
        this.queue = queue;
        this.indexName = indexName;
        this.typeName = typeName;
        this.running = true;
        this.jestClient = SpringContextUtil.getBean(JestClient.class);
    }

    public void run() {
        logger.info("Starting ESIndexConsumer run method");
        List<Index> actions = new ArrayList<>();
        while (running || !queue.isEmpty()) {
            Queues.drainUninterruptibly(queue, actions, BATCH_SIZE, 5, TimeUnit.SECONDS);
            submitList(actions);
            actions.clear();
        }
    }

    public void finish() {
        running = false;
    }

    private void submitList(List<Index> actions) {
        if (!actions.isEmpty()) {
            logger.info("Processing a batch in ESIndexConsumer: size = {}", actions.size());
            Bulk bulk = new Bulk.Builder()
                    .defaultIndex(indexName)
                    .defaultType(typeName)
                    .addAction(actions)
                    .build();
            JestExecute.execute(jestClient, bulk);
        }
    }

}
