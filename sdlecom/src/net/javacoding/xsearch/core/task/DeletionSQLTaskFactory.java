package net.javacoding.xsearch.core.task;

import com.fdt.sdl.admin.ui.action.constants.IndexType;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.DeletionDataquery;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.task.work.list.ESFetchDeletedDocumentListBySQLTask;
import net.javacoding.xsearch.core.task.work.list.FastFetchFullDocumentListBySQLTask;
import net.javacoding.xsearch.core.task.work.list.FetchDeletedDocumentListBySQLTask;
import net.javacoding.xsearch.core.task.work.list.FetchFullDocumentListBySQLTask;

public class DeletionSQLTaskFactory {

    public static WorkerTask createTask(IndexerContext ic, boolean isThoroughDelete) {
        WorkerTask task = null;
        DatasetConfiguration dc = ic.getDatasetConfiguration();
        DeletionDataquery dq = dc.getDeletionQuery();
        if (dq.getIsDeleteOnly()) {
            if (dc.getIndexType() == null || dc.getIndexType() == IndexType.LUCENE) {
                task = new FetchDeletedDocumentListBySQLTask(ic);
            } else if (dc.getIndexType() == IndexType.ELASTICSEARCH) {
                task = new ESFetchDeletedDocumentListBySQLTask(ic);
            }
        } else {
            if (isThoroughDelete) {
                // This is slower
                task = new FetchFullDocumentListBySQLTask(ic);
            } else {
                task = new FastFetchFullDocumentListBySQLTask(ic);
            }
        }
        return task;
    }

}
