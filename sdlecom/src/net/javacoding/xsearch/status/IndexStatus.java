package net.javacoding.xsearch.status;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.indices.aliases.GetAliases;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.core.IndexerContext;
import net.javacoding.xsearch.core.PeriodEntry;
import net.javacoding.xsearch.core.PeriodTable;
import net.javacoding.xsearch.search.searcher.SearcherManager;
import net.javacoding.xsearch.search.searcher.SearcherProvider;
import net.javacoding.xsearch.utility.FileUtil;
import net.javacoding.xsearch.utility.SchedulerTool;
import net.javacoding.xsearch.utility.VMTool;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.elasticsearch.type.result.GetAliasesResult;
import com.fdt.elasticsearch.util.JestExecute;
import com.fdt.sdl.admin.ui.action.constants.IndexType;

/**
 * status for an index
 */
public final class IndexStatus {

    private static final Logger logger = LoggerFactory.getLogger(IndexStatus.class);

    public static final String indexingTemp = "_temp";

    /**
     * unlocks _temp directories
     */
    public static void unlockStoppedIndex(DatasetConfiguration dc) {
        logger.info("unlock temp files if exists");
        if (dc == null) return;
        try {
            unlockDirectory(dc.getTempIndexDirectoryFile());
            unlockDirectory(dc.getAltTempIndexDirectoryFile());
            unlockDirectory(dc.getMainIndexDirectoryFile());
            unlockDirectory(dc.getAltMainIndexDirectoryFile());
            unlockDirectory(dc.getDictionaryIndexDirectoryFile());
            unlockDirectory(dc.getPhraseIndexDirectoryFile());
            unlockDirectory(dc.getFullListIndexDirectoryFile());
            // unlockDirectory(dc.getWorkDirectoryFile());
        } catch (Exception e) {
            logger.error("Failed to unlock jobs: " + e.toString());
            e.printStackTrace();
        }
    }

    public static void unlockDirectory(Directory dir) {
        try {
            if(IndexReader.isLocked(dir)) {
                IndexReader.unlock(dir);
        }
        } catch (IOException e) {
        }
    }

    public static void unlockDirectory(File theDir) {
        if (theDir.exists()) {
            try {
                FSDirectory dir = FSDirectory.getDirectory(theDir);
                unlockDirectory(dir);
                dir.close();
            } catch (IOException ioe) {
                logger.warn("IOException", ioe);
            }
        }
    }

    public static void unlockDirectory(String dir) {
        unlockDirectory(new File(dir));
    }

    public static long getTimestamp(DatasetConfiguration dc) {
        File segments = new File(dc.getMainIndexDirectoryFile(),"segments.gen");
        return segments.exists()? segments.lastModified() : 0L;
    }

    private static PeriodTable getPeriodTable(File storedFile, IndexType indexType) {
        if (storedFile == null) return null;
        PeriodTable periodTable = null;
        try {
            if (storedFile.exists()) {
                if (indexType == null || indexType == IndexType.LUCENE) {
                    File segments = new File(storedFile.getParentFile(), "segments.gen");
                    if (segments.exists() && segments.lastModified() < storedFile.lastModified()) {
                        periodTable = PeriodTable.load(storedFile);
                    }
                } else if (indexType == IndexType.ELASTICSEARCH) {
                    periodTable = PeriodTable.load(storedFile);
                }
            }
        } catch (Exception ss) {
            logger.info("Reading Period Table file error:" + storedFile);
            periodTable = null;
        }
        return periodTable;
    }

    public static File getPeriodTableStoreFile(File dir){
        return new File(dir, IndexerContext.PERIOD_TABLE_FILE);
    }
    public static PeriodTable getPeriodTable(DatasetConfiguration dc) throws IOException{
        PeriodTable periodTable = getExistingPeriodTable(dc);
        if (periodTable != null) {
            return periodTable;
        }
        return doCreatePeriodTable(dc);
    }

    public static PeriodTable getExistingPeriodTable(DatasetConfiguration dc) throws IOException {
        if (dc == null) {
            return null;
        }
        PeriodTable ret = getExistingPeriodTable(IndexStatus.findActiveMainDirectoryFile(dc), dc.getIndexType());
        PeriodTable tmp = getExistingPeriodTable(IndexStatus.findActiveTempDirectoryFile(dc), dc.getIndexType());
        if (ret != null && tmp != null) {
            ret.merge(tmp);
        }
        if (ret == null && tmp != null) {
            ret = tmp;
        }
        return ret;
    }

    public static PeriodTable getExistingPeriodTable(File dir, IndexType indexType) throws IOException {
        if (dir == null || !dir.exists()) {
            return null;
        }
        File storedFile = new File(dir, IndexerContext.PERIOD_TABLE_FILE);
        return getPeriodTable(storedFile, indexType);
    }

    public static PeriodTable createPeriodTableIfNeeded(DatasetConfiguration dc) throws IOException {
        PeriodTable result = null;
        if (dc.getIndexType() == null || dc.getIndexType() == IndexType.LUCENE) {
            PeriodTable periodTable = getExistingPeriodTable(dc);
            if (periodTable != null) {
                result = periodTable;
            } else {
                result = doCreatePeriodTable(dc);
            }
        } else if (dc.getIndexType() == IndexType.ELASTICSEARCH) {
            PeriodTable periodTable = getExistingPeriodTable(dc);
            if (periodTable != null) {
                result = periodTable;
            } else {
                result = doCreatePeriodTable(dc);
            }
        }
        return result;
    }

    private static PeriodTable doCreatePeriodTable(DatasetConfiguration dc) throws IOException{
        if (dc == null) return null;
        if (dc.getWorkingQueueDataquery()== null || dc.getWorkingQueueDataquery().getModifiedDateColumn()== null) {
            //logger.debug("No modified time information.");
            return null;
        }

        PeriodEntry pe = null;
        IndexReader indexReader = null;
        try{
            Column mc = dc.getWorkingQueueDataquery().getModifiedDateColumn();
            if(mc!=null) {
                indexReader = IndexStatus.openIndexReader(dc);
                pe = getPeriodEntry(indexReader, mc.getColumnName());
                if(pe!=null) {
                    logger.debug("Re-created Indexed Period:" + pe);
                }
            }
        }catch(Throwable ioe){
            logger.debug("When getting period table:", ioe);
        }finally{
            try{
                if(indexReader!=null) indexReader.close();
            }catch(IOException e){}
        }

        File storedFile = new File(IndexStatus.findActiveMainDirectoryFile(dc), IndexerContext.PERIOD_TABLE_FILE);
        PeriodTable periodTable = null;
        if(pe!=null&&(!storedFile.exists()||storedFile.canWrite())) {
            periodTable = new PeriodTable();
            periodTable.add(pe);
            periodTable.save(storedFile);
            logger.info("Saved Indexed Period.");
        }
        return periodTable;
    }

    public static PeriodEntry getPeriodEntry(IndexReader indexReader, String datekeyName) {
        if(indexReader==null) return null;
        PeriodEntry pe = null;
        datekeyName = (datekeyName==null?null:datekeyName.intern());
        try {
            pe = new PeriodEntry();
            TermDocs termDocs = indexReader.termDocs();
            TermEnum termEnum = indexReader.terms(new Term(datekeyName, ""));
            try {
                if (termEnum.term() == null) { return null; }
                do {
                    Term term = termEnum.term();
                    if (term.field()!=datekeyName) break;
                    String termval = term.text();
                    termDocs.seek(termEnum);
                    while (termDocs.next()) {
                        pe.add(VMTool.storedStringToLong(termval));
                    }
                } while (termEnum.next());
            } finally {
                if(termDocs!=null)termDocs.close();
                if(termEnum!=null)termEnum.close();
            }
        } catch (Exception ioe) {
            logger.warn("When getting index period for " + indexReader + ",\n" + ioe);
            // } catch (Exception e) {
            // logger.error(e);
            // e.printStackTrace();
        }
        return pe;
    }
    
    public static void switchSearchersTo(DatasetConfiguration dc) {
        logger.info("creating searcher provider for index " + dc.getName() + " ...");
        SearcherProvider sp = SearcherManager.createSearcherProviderByDataset(dc);
        logger.info("created searcher provider");
        SearcherManager.switchSearchProvider(dc.getName(),sp);
        logger.info("searcher pool for index:"+dc.getName()+" is ready.");
    }
    public static void copyIndexToMain(DatasetConfiguration dc, File dir) throws IOException {
        File mainIndexDir = dc.getMainIndexDirectoryFile();
        boolean ret = FileUtil.deleteAll(dc.getTempIndexDirectoryFile());
        ret &= FileUtil.deleteAllFiles(mainIndexDir, "periodTbl");
        if(ret)logger.info("index files deleted in " + dc.getName());
        FileUtil.copyAll(dir, mainIndexDir, 128);
        logger.info("new index files copied to " + mainIndexDir);
    }
    public static void copyIndex(File fromDir, File toDir) throws IOException {
        FileUtil.deleteAllFiles(toDir, "periodTbl");
        FileUtil.copyAll(fromDir, toDir, 128);
    }

    public static long getIndexTimestamp(File dir) {
        File readyFile = new File(dir,"ready");
        return readyFile.lastModified();
    }
    /*
     * Same as isNewIndexValid().
     * @deprecated
     */
    public static boolean isNewIndexReady(File fromDir) throws IOException {
        File readyFile = new File(fromDir,"ready");
        if (fromDir == null || !fromDir.exists()) { return false; }
        if (!readyFile.exists()) { return false; }
        if (!org.apache.lucene.index.IndexReader.indexExists(fromDir)) { return false; }
        if (fromDir.isDirectory()) {
            File[] files = fromDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if(files[i].getName().endsWith("cfs")&&files[i].lastModified()>readyFile.lastModified()) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isNewIndexValid(File fromDir) {
        return isNewIndexValid(fromDir, null);
    }

    public static boolean isNewIndexValid(File fromDir, IndexType indexType) {
        File readyFile = new File(fromDir, "ready");
        if (fromDir == null || !fromDir.exists()) {
            return false;
        }
        if (!readyFile.exists()) {
            return false;
        }
        if (indexType == null || indexType == IndexType.LUCENE) {
            if (!IndexReader.indexExists(fromDir)) {
                return false;
            }
            if (fromDir.isDirectory()) {
                File[] files = fromDir.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].getName().endsWith("cfs") && files[i].lastModified() > readyFile.lastModified()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean setIndexReady(File theDir) throws IOException {
        if (theDir == null || !theDir.exists()) { return false; }
        File readyFile = new File(theDir, "ready");
        if(readyFile.exists()||readyFile.canWrite()) {
            logger.info("Directory is marked ready as up-to-date: "+theDir);
            return readyFile.setLastModified(new java.util.Date().getTime());
        }else {
            logger.info("Directory is marked ready: "+theDir);
            return readyFile.createNewFile();
        }
    }
    public static boolean setIndexNotReady(File theDir) throws IOException {
        if (theDir == null || !theDir.exists()) { return true; }
        File readyFile = new File(theDir, "ready");
        logger.info("Directory is marked not ready: "+theDir);
        return readyFile.delete();
    }
    public static IndexReader openIndexReader(File dir) throws IOException {
        IndexReader indexReader = null;
        if (dir!=null && IndexReader.indexExists(dir)) {
            indexReader = IndexReader.open(dir);
        }
        return indexReader;
    }
    public static IndexReader openIndexReader(DatasetConfiguration dc) throws IOException {
        IndexReader indexReader = openIndexReader(IndexStatus.findActiveMainDirectoryFile(dc));
        IndexReader tempIndexReader = openIndexReader(IndexStatus.findActiveTempDirectoryFile(dc));
        //pick both, or pick temp, or simply return main index reader
        if (tempIndexReader!=null && indexReader != null) {
            indexReader = new MultiReader(new IndexReader[]{indexReader, tempIndexReader});
        }else if(tempIndexReader!=null){
            indexReader = tempIndexReader;
        }
        return indexReader;
    }
    public static IndexReader openIndexReaderWithNewTemp(DatasetConfiguration dc) throws IOException {
        IndexReader indexReader = openIndexReader(IndexStatus.findActiveMainDirectoryFile(dc));
        IndexReader tempIndexReader = openIndexReader(IndexStatus.findNonActiveTempDirectoryFile(dc));
        //pick both, or pick temp, or simply return main index reader
        if (tempIndexReader!=null && indexReader != null) {
            indexReader = new MultiReader(new IndexReader[]{indexReader, tempIndexReader});
        }else if(tempIndexReader!=null){
            indexReader = tempIndexReader;
        }
        return indexReader;
    }
    public static Directory[] getIndexDirectories(DatasetConfiguration dc) throws IOException {
        Directory[] allDirs = new Directory[2];
        File mainIndexDirFile = dc.getMainIndexDirectoryFile();
        File _tempIndexDirFile = dc.getTempIndexDirectoryFile();
        if (IndexReader.indexExists(mainIndexDirFile)) {
            allDirs[0] = FSDirectory.getDirectory(mainIndexDirFile);
        }
        if (IndexReader.indexExists(_tempIndexDirFile)) {
            allDirs[1] = FSDirectory.getDirectory(_tempIndexDirFile);
        }
        //pick both, or pick temp, or simply return main index reader
        if (allDirs[0]==null && allDirs[1] == null) {
            return null;
        }
        if(allDirs[0]!=null && allDirs[1] != null) {
            return allDirs;
        }
        Directory[] singleDirs = new Directory[1];
        if(allDirs[0]!=null) {
            singleDirs[0] = allDirs[0];
        }else{
            singleDirs[0] = allDirs[1];
        }
        return singleDirs;
    }

    public static void clearError(String indexName) {
        getErrorFile(indexName).delete();
    }
    public static void setError(String indexName, String err) {
        try {
            FileUtil.writeFile(getErrorFile(indexName), err);
        }catch(IOException ioe) {}
    }
    public static String getError(String indexName) {
        try {
            return FileUtil.readFile(getErrorFile(indexName));
        }catch(IOException ioe) {}
        return null;
    }
    private static File getErrorFile(String indexName) {
        return new File(SchedulerTool.logDirectory, indexName + ".err");
    }

    public static File findActiveMainDirectoryFile(DatasetConfiguration dc) {
        return findActiveDirectoryFile(dc.getMainIndexDirectoryFile(), dc.getAltMainIndexDirectoryFile(), dc.getIndexType());
    }

    public static File findActiveTempDirectoryFile(DatasetConfiguration dc) {
        return findActiveDirectoryFile(dc.getTempIndexDirectoryFile(), dc.getAltTempIndexDirectoryFile(), dc.getIndexType());
    }

    public static File findNonActiveMainDirectoryFile(DatasetConfiguration dc) {
        return findNonActiveDirectoryFile(dc.getMainIndexDirectoryFile(), dc.getAltMainIndexDirectoryFile(), dc.getIndexType());
    }

    public static File findNonActiveTempDirectoryFile(DatasetConfiguration dc) {
        return findNonActiveDirectoryFile(dc.getTempIndexDirectoryFile(), dc.getAltTempIndexDirectoryFile(), dc.getIndexType());
    }

    private static File findActiveDirectoryFile(File one, File two, IndexType indexType) {
        boolean oneReady = isNewIndexValid(one, indexType);
        boolean twoReady = isNewIndexValid(two, indexType);
        if (oneReady && twoReady) {
            return getIndexTimestamp(one) > getIndexTimestamp(two) ? one : two;
        }
        return oneReady ? one : twoReady ? two : null;
    }

    private static File findNonActiveDirectoryFile(File one, File two, IndexType indexType) {
        boolean oneReady = isNewIndexValid(one, indexType);
        boolean twoReady = isNewIndexValid(two, indexType);
        if (oneReady && twoReady) {
            return getIndexTimestamp(one) > getIndexTimestamp(two) ? two : one;
        }
        return oneReady ? two : twoReady ? one : one;
    }

    public static int countIndexSize(Directory dir) {
        int size = 0;
        try {
            IndexReader indexReader = IndexReader.open(dir);
            size = indexReader.numDocs();
            indexReader.close();
        } catch (IOException ioe) {}
        return size;
    }
    public static int countIndexSize(File dir) {
        int size = 0;
        try {
            if(dir==null||!dir.exists()) return 0;
            IndexReader indexReader = IndexReader.open(dir);
            size = indexReader.numDocs();
            indexReader.close();
        } catch (IOException ioe) {}
        return size;
    }

    public static String findNewIndexName(JestClient jestClient, String aliasName) {

        GetAliases getAliases = new GetAliases.Builder().build();
        JestResult jestResult = JestExecute.execute(jestClient, getAliases);
        GetAliasesResult result = new GetAliasesResult(jestResult);
        List<String> currentIndexes = result.getIndexNameList();

        String newIndexName = null;
        int counter = 1;
        do {
            newIndexName = aliasName + "_" + String.format("%05d", counter++);
        } while (currentIndexes.contains(newIndexName));

        return newIndexName;
    }

    public static String findCurrentIndexName(JestClient jestClient, String aliasName) {

        String currentIndexName = null;

        GetAliases getAliases = new GetAliases.Builder().build();
        JestResult jestResult = JestExecute.execute(jestClient, getAliases);
        GetAliasesResult result = new GetAliasesResult(jestResult);
        List<String> currentIndexes = result.getIndexNameList();

        for (String indexName : currentIndexes) {
            if (result.getAliases(indexName).contains(aliasName)) {
                currentIndexName = indexName;
            }
        }

        return currentIndexName;
    }
}
