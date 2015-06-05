package net.javacoding.xsearch;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.core.AffectedDirectoryGroup;
import net.javacoding.xsearch.core.task.work.util.DocumentSQLTaskHelper;
import net.javacoding.xsearch.foundation.LoggerPrintStream;
import net.javacoding.xsearch.status.IndexStatus;
import net.javacoding.xsearch.utility.U;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * reIndexing with the new analyzer or filed type
 */
public class ReIndexing {
	
	private static Logger logger = LoggerFactory.getLogger(ReIndexing.class);	

    protected DatasetConfiguration dc;
    protected IndexReader indexReader     = null;
    protected IndexWriter iw = null;
    protected File workingDir = null;
    protected boolean running = true;
    
    private int branchCount = 1;
    public void setBranchCount(int branchCount) {
        this.branchCount = branchCount;
    }
    public int getBranchCount() {
        return this.branchCount>0? this.branchCount : 1;
    }

    public ReIndexing(DatasetConfiguration dataconf, AffectedDirectoryGroup adg) {
        this.dc           = dataconf;
        try{
            this.indexReader     = IndexStatus.openIndexReader(dc);
            adg.setNewDirectory(IndexStatus.findNonActiveMainDirectoryFile(dc));
            adg.addOldDirectory(dc.getTempIndexDirectoryFile());
            adg.addOldDirectory(dc.getAltTempIndexDirectoryFile());
            adg.addOldDirectory(IndexStatus.findActiveMainDirectoryFile(dc));
            logger.info("Opened index "+dc.getName());
            workingDir = adg.getNewDirectory();
            logger.info("Working in "+workingDir);
        }catch(IOException ioe){
            logger.warn("When getting index reader for index "+dc.getName()+",\n"+ioe);
        }
    }
    public static AffectedDirectoryGroup start(DatasetConfiguration dc) throws Throwable {
        AffectedDirectoryGroup adg = new AffectedDirectoryGroup();
        ReIndexing dd = new ReIndexing(dc, adg);
        dd.reIndexing();
        dd.close();
        return adg;
    }
    private void createIndexWriterUnder(String dir) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        iw = new IndexWriter(FSDirectory.getDirectory(dir==null? workingDir : new File(workingDir, dir)),dc.getIndexingAnalyzer(),true, MaxFieldLength.UNLIMITED);
        iw.setSimilarity(dc.getSimilarity());
        iw.setUseCompoundFile(true);
        iw.setMergeFactor( dc.getMergeFactor() );
        //iw.setMaxBufferedDocs(50);
        logger.info("Allocating "+dc.getDocumentBufferSizeMB()+"MB memory for indexing buffer..." );
        iw.setRAMBufferSizeMB(dc.getDocumentBufferSizeMB());
        iw.setMaxFieldLength(dc.getMaxFieldLength());
        iw.setMaxMergeDocs(dc.getMaxMergeDocs());
        //iw.minMergeDocs = 1000;
//iw.infoStream = System.out;
    }
    public void close ( ) {
        try{
            if(indexReader!=null) indexReader.close();
            if(running){
                //FileUtil.deleteAllFiles(dc.getIndexDirectory(), "periodTbl");
                //FileUtil.moveAllFiles(workingDir, dc.getIndexDirectoryFile());
                //FileUtil.deleteAll(workingDir);
                //moved to IndexManager, updateIndex
                //IndexStatus.moveIndex(dc.getName(),workingDir, dc.getIndexDirectoryFile());
                IndexStatus.setIndexReady(workingDir);
            }
        }catch(IOException ioe){
            logger.warn("Failed to close index reader "+dc.getMainIndexDirectoryFile()+",\n"+ioe);
        }
    }
    public void reIndexing() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        int intTotal = indexReader.numDocs();
        createIndexWriterUnder(branchCount<=1? null : "0");
        Thread shutdownHookThread = addShutdownHook();
        DecimalFormat rateFormatter = new DecimalFormat("#,###,###.0");
        DecimalFormat percentFormatter = new DecimalFormat("#,##0.00%");
        long start_doc_count = 0;
        long start_size_count = 0;
        long current_size_count = 0;
        long last_report_time = System.currentTimeMillis();
        long branchingCount = intTotal/getBranchCount();
        int branchingStep = 0;

        for(int i=0,intReportCounter=1;i<intTotal&&running;i++,intReportCounter++,branchingCount--){
            if(indexReader.isDeleted(i)) continue;
            Document doc = new Document();
            List<Field> fields = indexReader.document(i).getFields();
            for(Field f : fields){
                //loop inside getColumn
                Column col = dc.findColumn(f.name());
                String s   = f.stringValue();
                current_size_count += s.length();
                Field[] existingFields = doc.getFields(f.name());
                
                if(col!=null&&(existingFields==null||existingFields.length<=0)){
                    DocumentSQLTaskHelper.addToDocument(doc,s,col);
                }
            }
            if(branchingCount<=0) {
                iw.close();
                branchingStep++;
                createIndexWriterUnder(Integer.toString(branchingStep));
                branchingCount = intTotal/getBranchCount();
            }
            iw.addDocument(doc);
            long now = System.currentTimeMillis();
            if ((now - last_report_time) > 1900) {
                double size_rate = (current_size_count - start_size_count) * 1.0 / (now - last_report_time);
                start_size_count = current_size_count;

                double doc_rate = (i-start_doc_count)*1.0/(now - last_report_time)*1000;
                double percent = (i+1)*1.0/intTotal;
                start_doc_count = i;
                logger.info(percentFormatter.format(percent)+", "+rateFormatter.format(doc_rate)+" doc/sec, "+rateFormatter.format(size_rate)+" KB/sec" );
                last_report_time = now;
            }
        }
        if(running){
            logger.info("Optimizing...");
            iw.setInfoStream(new LoggerPrintStream(logger));
            iw.optimize();
        }
        if(running){
            logger.info("Closing...");
            try {
            	if(iw.getInfoStream()!=null){
                    iw.getInfoStream().close();
            	}
                iw.close();
                removeShutdownHook(shutdownHookThread);
            }catch(Exception e) {
                logger.info("reIndexing", e);
                logger.error(U.getStatckTrace(e));
            }
        }
    }
    public Thread addShutdownHook() throws java.io.IOException {
        Thread hook = new Thread() {
             public void run() {
                try{
                    logger.info("Urgent Shutdown...");
                    running = false;
                    indexReader.close();
                    iw.close();
                    logger.info("Program Terminated");
                }catch(IOException ioe){
                    logger.warn("Failed to close index reader "+dc.getMainIndexDirectoryFile()+",\n"+ioe);
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(hook);
        return hook;
    }
    public void removeShutdownHook(Thread hook) throws java.io.IOException {
        Runtime.getRuntime().removeShutdownHook(hook);
    }
}
