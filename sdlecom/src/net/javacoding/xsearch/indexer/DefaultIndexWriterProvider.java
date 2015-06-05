package net.javacoding.xsearch.indexer;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;

import java.io.File;
import java.io.IOException;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.exception.DataSourceException;
import net.javacoding.xsearch.foundation.LoggerPrintStream;
import net.javacoding.xsearch.status.IndexStatus;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An index writer provider that uses an Apache commons pool for parallel
 * indexing
 * 
 * @see IndexWriterProvider
 * @author various people
 */
public class DefaultIndexWriterProvider implements IndexWriterProvider {

    private Logger      logger = LoggerFactory.getLogger(DefaultIndexWriterProvider.class);

    private IndexWriter iw;
    private File indexWriterWorkDirectoryFile;
    private Directory directory;

    private IndexerContext ic = null;
    private DatasetConfiguration dc = null;
    private Analyzer analyzer;

    public IndexWriter getIndexWriter() throws Exception {
        return iw;
    }

    public void configure(IndexerContext ic) throws DataSourceException {
        this.ic = ic;
        this.dc = ic.getDatasetConfiguration();
        logger.info("Creating index writer under directory: " + dc.getIndexDirectoryFile());

        logger.info("Loading Analyzer:" + dc.getAnalyzerName());
        try {
            analyzer = ic.getDatasetConfiguration().getIndexingAnalyzer();
        } catch (Exception e) {
            logger.warn("Error Loading Analyzer", e);
        }

        //now creating the index writer
        indexWriterWorkDirectoryFile = ic.getAffectedDirectoryGroup().getNewDirectory();

        try {
            directory = FSDirectory.getDirectory(indexWriterWorkDirectoryFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //iw = new IndexWriter(FSDirectory.getDirectory(dir),false, analyzer, true); //false);
            iw = new IndexWriter(indexWriterWorkDirectoryFile, analyzer, MaxFieldLength.UNLIMITED); //false);
            iw.setInfoStream(new LoggerPrintStream(logger));
            //iw.setMaxBufferedDocs(50);
            iw.setMaxMergeDocs(dc.getMaxMergeDocs());
            //iw.setRAMBufferSizeMB(dc.getDocumentBufferSizeMB());
            iw.setMaxFieldLength(dc.getMaxFieldLength());
            iw.setMergeFactor(dc.getMergeFactor());
            iw.setUseCompoundFile(true);
            iw.setSimilarity(dc.getSimilarity());
            logger.debug("<<writer created");
        } catch (java.io.IOException ioe) {
            logger.warn("!!writer creation has error", ioe);
        }
    }

    /**
     * @see net.javacoding.xsearch.IndexWriter.IndexWriterProvider#close()
     */
    public void close() throws DataSourceException {
        try {
            if (iw != null) {
                iw.close();
            }
            if(ic.hasAddition||ic.hasDeletion){
                IndexStatus.setIndexReady(indexWriterWorkDirectoryFile);
            }else{
                ic.setAffectedDirectoryGroup(null);
            }
        } catch (Exception e) {
            logger.error(NOTIFY_ADMIN, "could not close index pool", e);
            throw new DataSourceException();
        } finally {
            try {
                if (IndexWriter.isLocked(directory)) {
                    IndexWriter.unlock(directory);
                }
            } catch (IOException e) {
                logger.error("IOException", e);
            }
        }
    }

}
