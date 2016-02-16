package net.javacoding.xsearch.core.task.work.list;

import io.searchbox.client.JestClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.BulkResult;
import io.searchbox.core.Delete;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Get;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.status.IndexStatus;
import net.javacoding.xsearch.utility.DBTool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.elasticsearch.config.SpringContextUtil;
import com.fdt.elasticsearch.type.result.GetResult;
import com.fdt.elasticsearch.util.JestExecute;

public class ESFetchDeletedDocumentListBySQLTask extends BaseFetchPrimaryKeysBySingleQueryTask {

    private static final Logger logger = LoggerFactory.getLogger(ESFetchDeletedDocumentListBySQLTask.class);

    private final DatasetConfiguration dc;
    private final JestClient jestClient;
    private final List<String> idsToDelete;

    public ESFetchDeletedDocumentListBySQLTask(IndexerContext ic) {
        super(ic);
        logger.info("Constructing ESFetchDeletedDocumentListBySQLTask instance");
        dc = ic.getDatasetConfiguration();
        jestClient = SpringContextUtil.getBean(JestClient.class);
        idsToDelete = new ArrayList<>();
    }

    @Override
    public void prepare() {
        super.prepare();
    }

    @Override
    public void execute() {
        logger.info("Execute ESFetchDeletedDocumentListBySQLTask");
        super.execute();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    protected int processOneResult(ResultSet rs) throws SQLException {

        String pkValue = DBTool.getString(rs, 1, primaryKeyColumn.getColumnType());
        logger.info("The Primary Key =====>" + pkValue);

        int toDeleteCount = 0;

        Get getRequest = new Get.Builder(dc.getName(), pkValue).type(dc.getName()).build();
        DocumentResult jestResult = JestExecute.executeNoCheck(jestClient, getRequest);
        GetResult result = new GetResult(jestResult);

        if (result.exists()) {
            idsToDelete.add(pkValue);
            toDeleteCount++;
        }

        return toDeleteCount;
    }

    @Override
    protected int deleteFromIndex() {

        logger.info("deleting " + idsToDelete.size() + " documents");

        int deleted = 0;

        if (idsToDelete.size() > 0) {

            List<Delete> deleteActions = idsToDelete.stream().map(
                    id -> new Delete.Builder(id).build()
            ).collect(Collectors.toList());
    
            Bulk bulkRequest = new Bulk.Builder()
                    .defaultIndex(IndexStatus.getAliasName(dc))
                    .defaultType(dc.getName())
                    .addAction(deleteActions)
                    .build();
    
            BulkResult bulkResult = JestExecute.execute(jestClient, bulkRequest);
            deleted = bulkResult.getItems().size();
    
            if (deleted > 0) {
                try {
                    IndexStatus.setIndexReady(IndexStatus.findActiveMainDirectoryFile(dc));
                    IndexStatus.setIndexReady(IndexStatus.findNonActiveTempDirectoryFile(dc));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                ic.setHasDeletion(true);
            }
        }

        return deleted;
    }
}
