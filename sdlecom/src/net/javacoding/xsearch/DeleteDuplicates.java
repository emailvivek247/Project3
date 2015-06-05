package net.javacoding.xsearch;

import java.io.IOException;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.WorkingQueueDataquery;
import net.javacoding.xsearch.core.AffectedDirectoryGroup;
import net.javacoding.xsearch.status.IndexStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;

/**
 * Deletes duplicate documents in a set of Lucene indexes. Duplicates have either the same contents (via MD5 hash) or the same URL.
 */
public class DeleteDuplicates {
    private static Logger           logger      = LoggerFactory.getLogger("net.javacoding.xsearch.DeleteDuplicates");

    protected DatasetConfiguration  dc;
    protected WorkingQueueDataquery docListQuery;
    protected IndexReader           indexReader = null;

    public DeleteDuplicates(DatasetConfiguration dc, AffectedDirectoryGroup adg) {
        this.dc = dc;
        this.docListQuery = dc.getWorkingQueueDataquery();
        try {
            this.indexReader = IndexStatus.openIndexReader(dc);
        } catch (IOException ioe) {
            logger.warn("When getting index reader for index " + dc.getIndexDirectory() + ",\n" + ioe);
        }
    }

    /** Delete duplicates in the indexes in the named directory. */
    public static AffectedDirectoryGroup start(DatasetConfiguration dc) throws Throwable {
        AffectedDirectoryGroup adg = new AffectedDirectoryGroup();
        DeleteDuplicates dd = new DeleteDuplicates(dc,adg);
        return dd.deleteDuplicates()>0?adg:null;
    }

    private int deleteDuplicates() throws IOException {
        if(indexReader == null) return 0;
        if(docListQuery.getPrimaryKeyColumn()==null) return 0;
        String pkName = docListQuery.getPrimaryKeyColumn().getColumnName();
        int deletedCount = 0;
        Searcher searcher = null;
        long lastReportTime = System.currentTimeMillis();
        try {
            searcher = new IndexSearcher(indexReader);
            Hits hits = null;
            int totalMaxDoc = indexReader.maxDoc();
            logger.debug("Start de-duplicating " + totalMaxDoc + " documents");
            for (int i = 0; i < totalMaxDoc; i++) {
                long now = System.currentTimeMillis();
                if (now - lastReportTime > 1700) {
                    lastReportTime = now;
                    logger.debug("deleted "+ deletedCount +" duplicates of processed " + i + " documents");
                }
                if (!indexReader.isDeleted(i)) {
                    Document d = indexReader.document(i);
                    Query keyQuery = null;
                    if(d.get(pkName) != null){
                        keyQuery = new TermQuery(new Term(pkName, d.get(pkName)));
                        hits = searcher.search(keyQuery);
                        for(int j= 0; j< hits.length() ; j++) {
                            int x = hits.id(j);
                            if(x==i) continue;
                            indexReader.deleteDocument(x);
                            deletedCount++;
                        }
                    }
                }
            }
            logger.debug("Deleted Duplicated " + deletedCount + " documents");
            if(deletedCount>0){
                indexReader.flush();
            }
            return deletedCount;
        } catch (IOException ioe) {
            logger.warn("When checking duplicated document in index " + dc.getMainIndexDirectoryFile() + ",\n" + ioe);
        } finally {
            try {
                if (searcher != null) searcher.close();
                indexReader.close();
            } catch (IOException ioe) {
                logger.warn("Failed to close index reader " + dc.getMainIndexDirectoryFile() + ",\n" + ioe);
            }
        }
        return deletedCount;
    }
}
