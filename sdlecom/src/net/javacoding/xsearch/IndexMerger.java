package net.javacoding.xsearch;

import java.io.File;
import java.io.IOException;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.core.AffectedDirectoryGroup;
import net.javacoding.xsearch.core.DirectorySizeChecker;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.PeriodTable;
import net.javacoding.xsearch.foundation.LoggerPrintStream;
import net.javacoding.xsearch.status.IndexStatus;
import net.javacoding.xsearch.utility.FileUtil;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.sdl.admin.ui.action.constants.IndexType;

/** Creates an index for the output corresponding to a single fetcher run. */
public class IndexMerger {

    private static final Logger logger = LoggerFactory.getLogger(IndexMerger.class);

    private IndexWriter indexWriter;
    private DatasetConfiguration dc;

    public IndexMerger(IndexerContext ic) {
        this.dc = ic.getDatasetConfiguration();
    }

    /**
     * merge indexes in the sub directories to the main index. Delete the indexes after they are merged
     */
    public boolean needToMergeSubDirectories() throws IOException {
        boolean needToMerge = false;
        if (dc.getIndexType() == null || dc.getIndexType() == IndexType.LUCENE) {
            logger.info("detected lucene-style index, checking for merge");
            DirectorySizeChecker dsc = new DirectorySizeChecker().init(dc);
            long total = dsc.getIndexDirectorySize();
            if (total <= 0) return false;
            long tempTotal = dsc.getTemporaryIndexDirectorySize() * 10000;
            double ratio = (double)tempTotal / (double)total;
            double percentage = ratio / 100.0;
            logger.info("temp indexes size is " + tempTotal);
            logger.info("main indexes size is " + total);
            // logger.debug("ratio is "+ratio);
            logger.info("temp indexes size is " + percentage + "% of the whole directory");
            logger.info("thresh hold to merge temp indexes size is " + dc.getMergePercentage() + "%");
            needToMerge = percentage >= dc.getMergePercentage();
        } else if (dc.getIndexType() == IndexType.ELASTICSEARCH) {
            logger.info("detected elasticsearch-style index, so always merge");
            needToMerge = true;
        }
        return needToMerge;
    }

    /*
     * merge temp to main
     */
    public AffectedDirectoryGroup mergeIndexes() throws IOException {
        //these are active main and temp indexes
        DirectorySizeChecker dsc = new DirectorySizeChecker().init(dc);
        if (dsc.getTempIndexDirectory() == null || !dsc.getTempIndexDirectory().exists()) {
            return null;
        }
        if (dsc.getMainIndexDirectory() == null || !dsc.getMainIndexDirectory().exists()) {
            return null;
        }
        Directory mainDirectory = FSDirectory.getDirectory(dsc.getMainIndexDirectory());
        Directory tempDirectory = FSDirectory.getDirectory(dsc.getTempIndexDirectory());
        Directory[] indexDirs = { mainDirectory, tempDirectory };
        AffectedDirectoryGroup adg = new AffectedDirectoryGroup();
        if (dc.getIndexType() == null || dc.getIndexType() == IndexType.LUCENE) {
            if (IndexStatus.countIndexSize(tempDirectory) <= 0) {
                return null;
            }
            if (IndexStatus.countIndexSize(mainDirectory) <= 0) {
                adg.setNewDirectory(dsc.getMainIndexDirectory());
                adg.addOldDirectory(dsc.getTempIndexDirectory());
                FileUtil.copyAllFiles(dsc.getTempIndexDirectory(), dsc.getMainIndexDirectory());
            } else {
                adg.setNewDirectory(IndexStatus.findNonActiveMainDirectoryFile(dc));
                adg.addOldDirectory(dsc.getMainIndexDirectory());
                mergeDirectoriesTo(indexDirs, adg.getNewDirectory());
                mergePeriodTables(dsc, adg.getNewDirectory());
            }
            adg.addOldDirectory(dc.getTempIndexDirectoryFile());
            adg.addOldDirectory(dc.getAltTempIndexDirectoryFile());
        } else if (dc.getIndexType() == IndexType.ELASTICSEARCH) {
            adg.setNewDirectory(IndexStatus.findNonActiveMainDirectoryFile(dc));
            adg.addOldDirectory(dsc.getMainIndexDirectory());
            mergePeriodTables(dsc, adg.getNewDirectory());
            adg.addOldDirectory(dc.getTempIndexDirectoryFile());
            adg.addOldDirectory(dc.getAltTempIndexDirectoryFile());
        }
        return adg;
    }

    private void mergePeriodTables(DirectorySizeChecker dsc, File newDirectoryFile) throws IOException {
        newDirectoryFile.mkdirs();
        PeriodTable pMain = IndexStatus.getExistingPeriodTable(dsc.getMainIndexDirectory(), dc.getIndexType());
        PeriodTable pTemp = IndexStatus.getExistingPeriodTable(dsc.getTempIndexDirectory(), dc.getIndexType());
        if (pMain != null && pTemp != null) {
            pMain.merge(pTemp);
        } else if (pMain == null) {
            pMain = pTemp;
        }
        if (pMain != null) {
            pMain.save(IndexStatus.getPeriodTableStoreFile(newDirectoryFile));
        }
    }

    private void mergeDirectoriesTo(Directory[] indexDirs, File destDir) throws IOException {

        if (indexDirs == null) {
            return;
        }
        for (int i = 0; i < indexDirs.length; i++) {
            if (!IndexReader.indexExists(indexDirs[i])) {
                return;
            }
        }

        logger.info("Working in " + destDir);
        try {
            FileUtil.deleteAllFiles(destDir);
            this.indexWriter = new IndexWriter(destDir, dc.getAnalyzer(), true, MaxFieldLength.UNLIMITED);
            this.indexWriter.setMergeFactor( dc.getMergeFactor() );
            this.indexWriter.setRAMBufferSizeMB(dc.getDocumentBufferSizeMB()*dc.getWriterThreadsCount());
            this.indexWriter.setMaxFieldLength(dc.getMaxFieldLength());
            this.indexWriter.setUseCompoundFile(true);
            this.indexWriter.setMaxMergeDocs(dc.getMaxMergeDocs());
            this.indexWriter.setInfoStream(new LoggerPrintStream(logger));
            logger.debug("IndexWriter created on " + destDir);
        } catch (IOException ioe) {
            logger.error("IOEXception", ioe);
        } catch (ClassNotFoundException cnfe) {
            logger.error("ClassNotFoundException", cnfe);
        } catch (InstantiationException ie) {
            logger.error("InstantiationException", ie);
        } catch (IllegalAccessException iae) {
            logger.error("IllegalAccessException", iae);
        }

        Thread shutdownHookThread = addShutdownHook();
        //always use faster adding indexes
        logger.info("fast adding indexes, with maxMergeDocs "+indexWriter.getMaxMergeDocs());
        indexWriter.addIndexesNoOptimize(indexDirs);
        if(dc.getIsOptimizeNeeded()) {
            logger.info("Optimizing...");
            indexWriter.optimize();
        }

        removeShutdownHook(shutdownHookThread);

        indexWriter.getInfoStream().close();
        indexWriter.close();
        logger.info("Indexes merged to " + destDir);
    }

    public Thread addShutdownHook() throws IOException {
        Thread hook = new Thread() {
            public void run() {
                try {
                    indexWriter.getInfoStream().close();
                    indexWriter.close();
                } catch (IOException ioe) {
                    logger.warn("Failed to close index writer:\n" + ioe);
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(hook);
        return hook;
    }

    public void removeShutdownHook(Thread hook) throws IOException {
        Runtime.getRuntime().removeShutdownHook(hook);
        return;
    }
}
