package net.javacoding.xsearch.core.task.work;

import java.io.IOException;
import java.util.ArrayList;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.IndexFieldType;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.component.TextDocument;
import net.javacoding.xsearch.core.task.Scheduler;
import net.javacoding.xsearch.core.task.WorkerTask;
import net.javacoding.xsearch.core.task.work.util.DocumentSQLTaskHelper;
import net.javacoding.xsearch.status.IndexStatus;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;

public class WriteDocumentToIndexTask extends BaseWorkerTaskImpl {
    protected static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.core.task.work.WriteDocumentToIndexTask");

    protected transient IndexerContext ic = null;
    protected TextDocument document = null;
    protected static ArrayList<Column> columns = null;
    protected static ArrayList<Column> boostColumns = null;
    private IndexWriter _writer = null;
    private String pkColumnName = null;

    public WriteDocumentToIndexTask(Scheduler sched, TextDocument  doc, int contextId) {
        super(WorkerTask.WORKERTASK_WRITERTASK, sched);
        this.document = doc;
        this.contextId = contextId;
    }

    public void prepare() {
    	super.prepare();
        this.ic 	 = scheduler.getIndexerContext();
        if(columns==null){
            columns= this.ic.getDatasetConfiguration().getColumns();
        }
        if(boostColumns==null){
            for (int i = 0; i < columns.size(); i++) {
                Column col = (Column)columns.get(i);
                if(col.getIndexFieldType()==IndexFieldType.BOOST||col.getIndexFieldType()==IndexFieldType.KEYWORD_BOOST) {
                    if(boostColumns==null) {
                        boostColumns = new ArrayList<Column>();
                    }
                    boostColumns.add(col);
                }
            }
        }
        pkColumnName = ic.getDatasetConfiguration().getPrimaryKeyColumn()==null?null:ic.getDatasetConfiguration().getPrimaryKeyColumn().getColumnName();
        try {
            _writer = ic.getIndexWriterProvider().getIndexWriter();
        } catch (Exception e) {
            logger.error("Error during creating index writer", e);
        }
    }

    public void execute() {
        try {
            if(!ic.isStopping()){
                save(document);
            }
            /*
            if(ic.getScheduler().getWriterToDoTasksCount()<50){
                //logger.debug("Writer notifying...");
                synchronized(ic.docFetcherFlag) {
                    ic.docFetcherFlag.notify();
                }
            }
            */
            ic.p.w(ic, document);
        } catch (OutOfMemoryError oom) {
            logger.error("Out of memory, stopping current indexing", oom);
            logger.error(NOTIFY_ADMIN, "Out of memory, stopping current indexing", oom);
            IndexStatus.setError(ic.getDatasetConfiguration().getName(),"Indexing is out of memory!");
            ic.setStopping();
            
        } catch (Throwable t) {
            logger.error("Error during save", t);
            ic.setStopping();
        } finally {
        }
    }
    public void save(TextDocument doc) throws Throwable {
        boolean written = false;
        int triedTimes = 0;
        while(!ic.isStopping()&&!written&&triedTimes<15){
            try{
                if(_writer==null) continue;
                Document d = new Document();
                //set the boost first
                if(boostColumns!=null) {
                    for (int i = 0; i < boostColumns.size(); i++) {
                        Column col = (Column)boostColumns.get(i);
                        String[] values = doc.getValues(col.getColumnName());
                        if(values!=null) {
                            for(int k = 0; k< values.length; k++) {
                                float b = U.getFloat(values[k],1.0f);
                                if(b<=0) {
                                    b = 1.0f;
                                }
                                d.setBoost(b*d.getBoost());
                            }
                        }
                    }
                }
                //then really add the content
                for (int i = 0; i < columns.size(); i++) {
                    Column col = (Column)columns.get(i);
                    String[] values = doc.getValues(col.getColumnName());
                    if(values!=null) {
                        for(int k = 0; k< values.length; k++) {
                            DocumentSQLTaskHelper.addToDocument(d, values[k],col);
                            ic.hasAddition = true;
                        }
                    }
                }
                if(pkColumnName!=null){
                    _writer.updateDocument(new Term(pkColumnName, doc.get(pkColumnName)),d);
                }else{
                    _writer.addDocument(d);
                }
                written = true;
            } catch(java.util.NoSuchElementException nsee){
                logger.info("Didn't get IndexWriter from the pool. Trying again.");
                triedTimes++;
                try {
                	Thread.sleep(239);
                } catch(Exception e) {
                		
                }
            } catch(IOException ioe){
                triedTimes++;
                try {
                	Thread.sleep(239);
                } catch(Exception e) {
                	
                }
            }
        }
    }
}
