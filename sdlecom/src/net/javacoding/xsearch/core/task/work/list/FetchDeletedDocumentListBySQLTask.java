package net.javacoding.xsearch.core.task.work.list;

import net.javacoding.xsearch.core.IndexerContext;

public class FetchDeletedDocumentListBySQLTask extends BaseFetchPrimaryKeysBySingleQueryTask {

    public FetchDeletedDocumentListBySQLTask(IndexerContext ic) {
        super(ic);
    }
}
