package net.javacoding.xsearch.fetch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.core.DirectorySizeChecker;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.PeriodEntry;
import net.javacoding.xsearch.core.TextDocumentField;
import net.javacoding.xsearch.core.component.IndexingCache;
import net.javacoding.xsearch.core.component.TextDocument;
import net.javacoding.xsearch.core.task.work.util.TaskUtil;
import net.javacoding.xsearch.status.IndexStatus;
import net.javacoding.xsearch.utility.U;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFetcher {
	
    protected static final Logger logger  = LoggerFactory.getLogger(AbstractFetcher.class);

    private IndexerContext ic;
    private boolean isDebug;
    
    /**
     * Simply list out all possible fields from this fetcher. Example:
     * <pre>
    public List<FieldType> getFieldTypes(Properties p) {
        List<FieldType> ret = new ArrayList<FieldType>();
        ret.add(new NumberFieldType("id").setPrimaryKey(true));
        ret.add(new StringFieldType("title"));
        ret.add(new TimestampFieldType("modified_time").setModifiedTime(true));
        return ret;
    }
     * </pre>
     *  @param p the properties defined using the web UI. Same as the one used in start(Properties p)
     */
    public abstract List<FieldType> getFieldTypes(Properties p);

    /**
     * Overwrite this function to process. If lastRunTime is 0, it will be re-creating the index from scratch.
       If lastRunTime is not 0, it will be a incremental indexing, retrieving documents later than this time.
       
     * Call scheduleDocument(document) to pass the document down the crawling pipeline. 
     * DBSight will handle the update automatically if the primary key is defined. 
     * Example:<pre>
     public void execute(Properties p, long lastRunTime) {
        if(lastRunTime>0) {
            // an incremental indexing
            for(int i=50;i<150;i++) {
                TextDocument td = new TextDocument();
                td.add("id", Integer.toString(i));
                td.add("title", "incrementally updated document title "+i);
                td.add("modified_time", new Date(System.currentTimeMillis()));
                scheduleDocument(td);
            }
        }else {
            // a re-create indexing
            for(int i=0;i<100;i++) {
                TextDocument td = new TextDocument();
                td.add("id", Integer.toString(i));
                td.add("title", "document title "+i);
                td.add("modified_time", new Date(System.currentTimeMillis()));
                scheduleDocument(td);
            }
        }
     }
        </pre>
     * @param p the properties defined using the web UI.
     * @param lastRunTime
     */
    public abstract void execute(Properties p, long lastRunTime);

    public boolean isDebug() {
        return isDebug;
    }

    /**
     * Used during testing, not really used during actual indexing
     * @param isDebug
     */
    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    /**
     * This should be called to pass the TextDocument to the pipeline, to be processed further.
     * With this, you don't need to keep all documents in memory, thus saving a lot of memory.
     * @param doc
     */
    final public void scheduleDocument(TextDocument doc) {
        if(this.allowedBytes>0) {
            this.allowedBytes-=doc.getSize();
        }else {
            println("Exceeding Index Size Limit!");
            return;
        }
        if(isDebug) {
            println("added document:");
            println("allowed bytes:"+this.allowedBytes);
            for(TextDocumentField f : doc.getFields()) {
                println("  "+f.getName() +": "+f.getValue());
            }
            return;
        }
        if(primaryKeyColumn==null) {
            println("Primary key column is needed!");
            return;
        }else if(U.isEmpty(doc.get(primaryKeyColumn.getName()))) {
            println("Value for Primary key column is missing!");
            return;
        }
        if(isDocumentIndexingNeeded(doc)) {
            if(ic.getScheduler()!=null) {
                ic.getScheduler().schedule(1, doc);
            }
        }else {
            if(isDebug) {
                if(primaryKeyColumn!=null) {
                    println("skipping document: "+doc.get(ic.getDatasetConfiguration().getPrimaryKeyColumn().getName()));
                }
            }
        }
    }

    private   Column                primaryKeyColumn     = null;
    private   Column                modifiedDateColumn   = null;
    private   boolean               hasExistingIndex     = false;
    private   PeriodEntry           pe;
    private   ArrayList<Integer>    toBeDeleted        = null;
    private   IndexReader        indexReader        = null;
    private   Searcher           searcher           = null;
    private   long           allowedBytes     = 0;
    private   IndexingCache pkCache;
    /**
     * Set by DBSight during actual running time.
     */
    final public void setIndexerContext(IndexerContext ic) {
        this.ic = ic;
        this.primaryKeyColumn = ic.getDatasetConfiguration().getPrimaryKeyColumn();
        this.modifiedDateColumn = ic.getDatasetConfiguration().getModifiedDateColumn();
        this.pe = new PeriodEntry();
        if(!ic.getIsRecreate()){
            hasExistingIndex = IndexReader.indexExists(ic.getDatasetConfiguration().getMainIndexDirectoryFile());
        }
        if(hasExistingIndex) {
            toBeDeleted = new ArrayList<Integer>();
            try {
                indexReader = IndexStatus.openIndexReader(ic.getDatasetConfiguration());
                if (indexReader != null) {
                    searcher = new IndexSearcher(indexReader);
                }
            } catch (IOException ioe) {
                logger.warn("When getting index reader for index " + ic.getDatasetConfiguration().getIndexDirectory() + ",\n" + ioe);
            }
        }

        //calculating allowed index size
        double sizeInMegaBytes = ic.getDatasetConfiguration().getIndexMaxSize();
        this.allowedBytes = (long) (sizeInMegaBytes * 1024 * 1024);
        //minors existing indexes
        if(!ic.getIsRecreate()) {
            this.allowedBytes -= DirectorySizeChecker.getDirectorySize(ic.getDatasetConfiguration().getMainIndexDirectoryFile());
            this.allowedBytes -= DirectorySizeChecker.getDirectorySize(ic.getDatasetConfiguration().getTempIndexDirectoryFile());
        }
        this.pkCache = new IndexingCache(ic.getDatasetConfiguration(), "pkCache");
    }
    /**
     * Called by DBSight to clean things up
     */
    final public int shutdown() {
        ic.getPeriodTable().add(this.pe);
        this.pkCache.close();

        if (toBeDeleted == null || toBeDeleted.size() <= 0) {
            return 0;
        }
        logger.info("deleting " + toBeDeleted.size() + " documents");
        int deleted = 0;
        try {
            for (int i = 0; i < toBeDeleted.size(); i++) {
                indexReader.deleteDocument(((Integer) toBeDeleted.get(i)).intValue());
                deleted++;
            }
            if (toBeDeleted.size() > 0) {
                indexReader.flush();
                // this index ready file is for other instances to pick up this
                // index update
                IndexStatus.setIndexReady(ic.getDatasetConfiguration().getWorkDirectoryFile());
                // this deletion setting is for indexManager to know need to
                // send a refreshIndex.do to current server
                ic.setHasDeletion(true);
            }
        } catch (IOException ioe) {
            logger.warn("When deleting duplicated document in index " + ic.getDatasetConfiguration().getIndexDirectory(),ioe);
        } finally {
            TaskUtil.close(searcher);
            TaskUtil.close(indexReader);
        }
        return deleted;
    }
    final private boolean isDocumentIndexingNeeded(TextDocument doc) {
        if(modifiedDateColumn!=null){
            long currentDocumentDate = U.getLong(doc.get(modifiedDateColumn.getName()),0);
            this.pe.add(currentDocumentDate);
        }
        if(ic.getIsRecreate())return true;
        if(primaryKeyColumn==null) return true;
        String pkValue = doc.get(primaryKeyColumn.getName());
        if(pkLookup(pkValue)!=null) return false;
        if(!hasExistingIndex) {
            pkStore(pkValue, flag);
            return true;
        }
        if(modifiedDateColumn!=null) {
            long currentDocumentDate = U.getLong(doc.get(modifiedDateColumn.getName()),0);
            if (ic.getPeriodTable().contains(currentDocumentDate)) {
                return false;
            }
        }
        collectDuplicatedDocumentIds(pkValue);
        pkStore(pkValue, flag);
        return true;
    }
    final protected int collectDuplicatedDocumentIds(String pkValue){
        int toDeletCount = 0;
        Hits hits = null;
        try {
            Term pkTerm = new Term(primaryKeyColumn.getColumnName(), pkValue);
            Query pkQuery = new TermQuery(pkTerm);
            hits = searcher.search(pkQuery);
            for (int i = 0; i < hits.length(); i++, toDeletCount++) {
                toBeDeleted.add(new Integer(hits.id(i)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return toDeletCount;
    }

    
    /**
     * Simply print out messages to system console
     * @param s
     */
    public void println(String s) {
        logger.info(s);
    }

    private   String flag = "";
    /** See if an object is in the cache. */
    private Object pkLookup (Object name) {
        return pkCache.lookup(name);
    }
    /** Put an object into the cache. */
    private Object pkStore (Object name, Object value) {
        return pkCache.store(name, value);
    }
}
