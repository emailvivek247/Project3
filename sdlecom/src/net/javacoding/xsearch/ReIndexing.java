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
public class ReIndexing extends AbstractReIndexing {

    private static final Logger logger = LoggerFactory.getLogger(ReIndexing.class);

    private IndexReader indexReader;
    private IndexWriter indexWriter;

    public ReIndexing(DatasetConfiguration dc, AffectedDirectoryGroup adg) {
        super(dc, adg);
        try {
            this.indexReader = IndexStatus.openIndexReader(dc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reIndexing() {
    
        try {
    
            int intTotal = indexReader.numDocs();
            createIndexWriterUnder(branchCount <= 1 ? null : "0");
    
            Thread shutdownHookThread = addShutdownHook();
            DecimalFormat rateFormatter = new DecimalFormat("#,###,###.0");
            DecimalFormat percentFormatter = new DecimalFormat("#,##0.00%");
    
            long start_doc_count = 0;
            long start_size_count = 0;
            long current_size_count = 0;
            long last_report_time = System.currentTimeMillis();
            long branchingCount = intTotal / getBranchCount();
            int branchingStep = 0;
    
            for (int i = 0; i < intTotal && running; i++, branchingCount--) {

                if (indexReader.isDeleted(i)) {
                    continue;
                }

                Document doc = new Document();

                @SuppressWarnings("unchecked")
                List<Field> fields = indexReader.document(i).getFields();

                for (Field f : fields) {
                    // loop inside getColumn
                    Column col = dc.findColumn(f.name());
                    String s = f.stringValue();
                    current_size_count += s.length();
                    Field[] existingFields = doc.getFields(f.name());
                    if (col != null && (existingFields == null || existingFields.length <= 0)) {
                        DocumentSQLTaskHelper.addToDocument(doc, s, col);
                    }
                }
    
                if (branchingCount <= 0) {
                    indexWriter.close();
                    branchingStep++;
                    createIndexWriterUnder(Integer.toString(branchingStep));
                    branchingCount = intTotal / getBranchCount();
                }
    
                indexWriter.addDocument(doc);
    
                long now = System.currentTimeMillis();
    
                if ((now - last_report_time) > 1900) {
    
                    double size_rate = (current_size_count - start_size_count) * 1.0 / (now - last_report_time);
                    start_size_count = current_size_count;
    
                    double doc_rate = (i - start_doc_count) * 1.0 / (now - last_report_time) * 1000;
                    double percent = (i + 1) * 1.0 / intTotal;
                    start_doc_count = i;
                    logger.info("{}, {} doc/sec, {} KB/sec", percentFormatter.format(percent),
                            rateFormatter.format(doc_rate), rateFormatter.format(size_rate));
                    last_report_time = now;
                }
            }
            if (running) {
                logger.info("Optimizing...");
                indexWriter.setInfoStream(new LoggerPrintStream(logger));
                indexWriter.optimize();
            }
            if (running) {
                logger.info("Closing...");
                try {
                    if (indexWriter.getInfoStream() != null) {
                        indexWriter.getInfoStream().close();
                    }
                    indexWriter.close();
                    removeShutdownHook(shutdownHookThread);
                } catch (Exception e) {
                    logger.info("reIndexing", e);
                    logger.error(U.getStatckTrace(e));
                }
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            if (indexReader != null)
                indexReader.close();
            if (running) {
                IndexStatus.setIndexReady(workingDir);
            }
        } catch (IOException ioe) {
            logger.warn("Failed to close index reader " + dc.getMainIndexDirectoryFile() + ",\n" + ioe);
        }
    }

    private void createIndexWriterUnder(String dir) throws IOException, ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        indexWriter = new IndexWriter(FSDirectory.getDirectory(dir == null ? workingDir : new File(workingDir, dir)),
                dc.getIndexingAnalyzer(), true, MaxFieldLength.UNLIMITED);
        indexWriter.setSimilarity(dc.getSimilarity());
        indexWriter.setUseCompoundFile(true);
        indexWriter.setMergeFactor(dc.getMergeFactor());
        logger.info("Allocating " + dc.getDocumentBufferSizeMB() + "MB memory for indexing buffer...");
        indexWriter.setRAMBufferSizeMB(dc.getDocumentBufferSizeMB());
        indexWriter.setMaxFieldLength(dc.getMaxFieldLength());
        indexWriter.setMaxMergeDocs(dc.getMaxMergeDocs());
    }

    public Thread addShutdownHook() {
        Thread hook = new Thread() {
            public void run() {
                try {
                    logger.info("Urgent Shutdown...");
                    running = false;
                    indexReader.close();
                    indexWriter.close();
                    logger.info("Program Terminated");
                } catch (IOException ioe) {
                    logger.warn("Failed to close index reader " + dc.getMainIndexDirectoryFile() + ",\n" + ioe);
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(hook);
        return hook;
    }

}
