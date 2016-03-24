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
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.gson.Gson;

public class ESIndexConsumer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ESWriteDocumentToIndexTask.class);

    private BlockingQueue<Index> queue;
    private String indexName;
    private String typeName;

    private boolean running;
    private JestClient jestClient;
    private int batchSize;

    public ESIndexConsumer(BlockingQueue<Index> queue, String indexName, String typeName) {
        this.queue = queue;
        this.indexName = indexName;
        this.typeName = typeName;
        this.running = true;
        this.jestClient = SpringContextUtil.getBean(JestClient.class);
        this.batchSize = SpringContextUtil.getBatchSize();
    }

    public void run() {
        try {
            logger.info("Thread ID = {}. Starting ESIndexConsumer run method", Thread.currentThread().getId());
            List<Index> actions = new ArrayList<>();
            while (running || !queue.isEmpty()) {
                Queues.drainUninterruptibly(queue, actions, batchSize, 5, TimeUnit.SECONDS);
                if (!actions.isEmpty()) {
                    submitList(actions);
                    actions.clear();
                }
            }
        } catch (Throwable t) {
            logger.error("Uncaught exception in ESIndexConsumer", t);
            throw t;
        }
    }

    public void finish() {
        running = false;
    }

    private void submitList(List<Index> actions) {
        if (!upload(actions)) {
            Lists.partition(actions, actions.size() / 10).forEach(l -> resubmitList(l));
        }
    }

    private void resubmitList(List<Index> actions) {
        boolean success = false;
        int numTries = 0;
        while (!success && numTries++ < 10) {
            success = upload(actions);
        }
        actions.forEach(a -> submitSingleItem(a));
    }

    private void submitSingleItem(Index action) {
        boolean success = false;
        int numTries = 0;
        while (!success && numTries++ < 10) {
            success = uploadSingleItem(action);
        }
    }

    private boolean upload(List<Index> actions) {
        boolean success = false;
        long threadId = Thread.currentThread().getId();
        try {
            logger.info("Thread ID = {}. Submitting a batch in ESIndexConsumer: " +
                    "size = {}, indexName = {}, typeName = {}", threadId, actions.size(), indexName, typeName);
            long start = System.currentTimeMillis();
            Bulk bulk = new Bulk.Builder().defaultIndex(indexName).defaultType(typeName).addAction(actions).build();
            JestExecute.execute(jestClient, bulk);
            long duration = System.currentTimeMillis() - start;
            logger.info("Thread ID = {}. Done submitting a batch in ESIndexConsumer: " +
                    "duration = {}ms", threadId, duration);
            success = true;
        } catch (Throwable t) {
            logger.warn("Thread ID = " + threadId + ". Failed to submit a batch.");
        }
        return success;
    }

    private boolean uploadSingleItem(Index action) {
        boolean success = false;
        long threadId = Thread.currentThread().getId();
        try {
            logger.info("Thread ID = {}. Submitting a single item in ESIndexConsumer: " +
                    "size = {}, indexName = {}, typeName = {}", threadId, indexName, typeName);
            long start = System.currentTimeMillis();
            Index index = new Index.Builder(action.getData(new Gson()))
                    .id(action.getId())
                    .index(indexName)
                    .type(typeName)
                    .build();
            JestExecute.execute(jestClient, index);
            long duration = System.currentTimeMillis() - start;
            logger.info("Thread ID = {}. Done submitting a single item in ESIndexConsumer: " +
                    "duration = {}ms", threadId, duration);
            success = true;
        } catch (Throwable t) {
            logger.error("Thread ID = " + threadId + ". Failed to submit a single item.", t);
        }
        return success;
    }
}
