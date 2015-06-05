package net.javacoding.xsearch;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import net.javacoding.xsearch.config.DataSource;
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

import com.fdt.common.util.TIFFToPDFConverter;

/** Creates an index for the output corresponding to a single fetcher run. */
public class IndexMerger {
	
	private static Logger logger = LoggerFactory.getLogger(IndexMerger.class);	

    private IndexWriter indexWriter = null;

    protected DatasetConfiguration dc = null;

    public IndexMerger(IndexerContext ic) {
        this.dc = ic.getDatasetConfiguration();
    }

    /**
     * merge indexes in the sub directories to the main index. Delete the indexes after they are merged
     */
    public boolean needToMergeSubDirectories() throws IOException {
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
        return percentage >= dc.getMergePercentage();
    }

    /*
     * merge temp to main
     */
    public AffectedDirectoryGroup mergeIndexes() throws IOException {
        //these are active main and temp indexes
        DirectorySizeChecker dsc = new DirectorySizeChecker().init(dc);
        if(dsc.getTempIndexDirectory()==null || !dsc.getTempIndexDirectory().exists()) return null;
        if(dsc.getMainIndexDirectory()==null || !dsc.getMainIndexDirectory().exists()) return null;
        Directory[] indexDirs = { FSDirectory.getDirectory(dsc.getMainIndexDirectory()), FSDirectory.getDirectory(dsc.getTempIndexDirectory()) };
        if(IndexStatus.countIndexSize(indexDirs[1])<=0) {
            return null;
        }
        AffectedDirectoryGroup adg = new AffectedDirectoryGroup();
        if(IndexStatus.countIndexSize(indexDirs[0])<=0) {
            adg.setNewDirectory(dsc.getMainIndexDirectory());
            adg.addOldDirectory(dsc.getTempIndexDirectory());
            FileUtil.copyAllFiles(dsc.getTempIndexDirectory(), dsc.getMainIndexDirectory());
        }else{
            adg.setNewDirectory(IndexStatus.findNonActiveMainDirectoryFile(dc));
            adg.addOldDirectory(dsc.getMainIndexDirectory());
            mergeDirectoriesTo(indexDirs, adg.getNewDirectory());
            PeriodTable pMain = IndexStatus.getExistingPeriodTable(dsc.getMainIndexDirectory());
            PeriodTable pTemp = IndexStatus.getExistingPeriodTable(dsc.getTempIndexDirectory());
            if(pMain!=null&&pTemp!=null){
                pMain.merge(pTemp);
            }else if(pMain==null){
                pMain = pTemp;
            }
            if(pMain!=null){
                pMain.save(IndexStatus.getPeriodTableStoreFile(adg.getNewDirectory()));
            }
        }
        adg.addOldDirectory(dc.getTempIndexDirectoryFile());
        adg.addOldDirectory(dc.getAltTempIndexDirectoryFile());
        return adg;
    }

    private void mergeDirectoriesTo(Directory[] indexDirs, File destDir) throws IOException {
        if (indexDirs == null) return;
        for (int i = 0; i < indexDirs.length; i++) {
            if (!IndexReader.indexExists(indexDirs[i])) { return; }
        }

        logger.info("Working in " + destDir);
        try {
            FileUtil.deleteAllFiles(destDir);
            //this.indexWriter = new IndexWriter(FSDirectory.getDirectory(destDir), false, dc.getAnalyzer(), true);
            this.indexWriter = new IndexWriter(destDir, dc.getAnalyzer(), true, MaxFieldLength.UNLIMITED); //false);
            this.indexWriter.setMergeFactor( dc.getMergeFactor() );
            // this.indexWriter.minMergeDocs = 1000;
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

    public static Directory[] getSubDirectoriesWithIndex(File theDir) {
        try {
            File[] subDirs = getSubDirectoryFilesWithIndex(theDir);
            if (subDirs != null) {
                Directory[] subIndexDirs = new Directory[subDirs.length];
                for (int i = 0; i < subIndexDirs.length; i++) {
                    subIndexDirs[i] = FSDirectory.getDirectory(subDirs[i]);
                    logger.info("adding " + subDirs[i]);
                }
                return subIndexDirs;
            }
        } catch (IOException ioe) {
            logger.info("IOException", ioe);
        }
        return null;
    }

    private static FileFilter dirFilter = new FileFilter() {
        public boolean accept(File f) {
            if (f.isDirectory()) { return IndexReader.indexExists(f); }
            return false;
        }
    };

    public static File[] getSubDirectoryFilesWithIndex(File theDir) {
        if (theDir == null) return null;
        File[] subDirs = theDir.listFiles(dirFilter);
        return subDirs;
    }

    public Thread addShutdownHook() throws java.io.IOException {
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

    public void removeShutdownHook(Thread hook) throws java.io.IOException {
        Runtime.getRuntime().removeShutdownHook(hook);
        return;
    }
}
