package net.javacoding.xsearch;

import io.searchbox.client.JestClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchScroll;
import io.searchbox.params.Parameters;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.core.AffectedDirectoryGroup;
import net.javacoding.xsearch.status.IndexStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.elasticsearch.config.SpringContextUtil;
import com.fdt.elasticsearch.util.JestExecute;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ESReIndexing extends AbstractReIndexing {

    private static final Logger logger = LoggerFactory.getLogger(ESReIndexing.class);

    private final JestClient jestClient;

    public ESReIndexing(DatasetConfiguration dc, AffectedDirectoryGroup adg) {
        super(dc, adg);
        jestClient = SpringContextUtil.getBean(JestClient.class);
    }

    public void reIndexing() {

        Thread shutdownHookThread = addShutdownHook();

        logger.info("Beginning reindexing on Elasticsearch index for data set {}", dc.getName());

        String newIndexName = IndexStatus.findNewIndexName(jestClient, dc.getName());

        logger.info("New index name will be {}", newIndexName);

        IndexStatus.createIndex(jestClient, newIndexName);

        String query = "{\"query\" : { \"match_all\" : { } } }";

        Search search = new Search.Builder(query)
                .addIndex(dc.getName())
                .addType(dc.getName())
                .setParameter(Parameters.SIZE, 10000)
                .setParameter(Parameters.SCROLL, "5m")
                .setParameter(Parameters.SEARCH_TYPE, "scan")
                .build();

        SearchResult jestResult = JestExecute.execute(jestClient, search);
        String scrollId = jestResult.getJsonObject().get("_scroll_id").getAsString();
        int totalCount = jestResult.getJsonObject().getAsJsonObject("hits").get("total").getAsInt();

        logger.info("Total count from scroll search is {}", totalCount);

        JsonObject result = getNextHits(scrollId);

        JsonArray hits = result.getAsJsonObject("hits").getAsJsonArray("hits");
        scrollId = result.get("_scroll_id").getAsString();

        while (hits.size() > 0 && running) {

            logger.info("Processing a batch of hits. Batch size = {}", hits.size());

            List<Index> indexList = StreamSupport.stream(hits.spliterator(), true).map((hit) -> {
                JsonObject source = hit.getAsJsonObject().getAsJsonObject("_source");
                String id = hit.getAsJsonObject().get("_id").getAsString();
                return new Index.Builder(source).id(id).build();
            }).collect(Collectors.toList());

            Bulk bulk = new Bulk.Builder()
                    .defaultIndex(newIndexName)
                    .defaultType(dc.getName())
                    .addAction(indexList)
                    .build();
            JestExecute.execute(jestClient, bulk);
    
            result = getNextHits(scrollId);
            hits = result.getAsJsonObject("hits").getAsJsonArray("hits");
            scrollId = result.getAsJsonPrimitive("_scroll_id").getAsString();
        }

        logger.info("Done with batch processing loop");

        if (running) {
            logger.info("Removing shutdown hook");
            removeShutdownHook(shutdownHookThread);
        }
    }

    @Override
    public void close() {
        if (running) {
            try {
                IndexStatus.setIndexReady(workingDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Thread addShutdownHook() {
        Thread hook = new Thread() {
            public void run() {
                logger.info("Urgent Shutdown...");
                running = false;
                logger.info("Program Terminated");
            }
        };
        Runtime.getRuntime().addShutdownHook(hook);
        return hook;
    }

    private JsonObject getNextHits(String scrollId) {
        SearchScroll searchScroll = new SearchScroll.Builder(scrollId, "5m").build();
        return JestExecute.execute(jestClient, searchScroll).getJsonObject();
    }

}
