package net.javacoding.xsearch.core;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.indices.IndicesExists;
import io.searchbox.indices.Stats;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.status.IndexStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.elasticsearch.config.SpringContextUtil;
import com.fdt.elasticsearch.type.result.StatsResult;
import com.fdt.elasticsearch.util.JestExecute;
import com.fdt.sdl.admin.ui.action.constants.IndexType;

public class DirectorySizeChecker {

    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(DirectorySizeChecker.class);

    protected static final long MILLIS_PER_SECOND = 1000;
    public static final long BYTES_PER_MEGA = 1024 * 1024;

    protected long _directoryCheckInterval = 17;
    protected long _directoryNextCheck = 0;

    // It is also possible to filter the list of returned files.
    // This example does not return any files that start with `.'.
    private static final FilenameFilter nameFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return !name.startsWith(".");
        }
    };

    // This filter only returns directories
    // According to javadoc,
    // Pathnames denoting the directory itself
    // and the directory's parent directory are not included in the result.
    private static final FileFilter dirFilter = new FileFilter() {
        public boolean accept(File f) {
            return f.isDirectory();
        }
    };

    private IndexType indexType;

    private File _mainIndexDir = null;
    private File _tempIndexDir = null;
    private double _indexMaxSize = 0;
    private boolean _isIndexing = false;
    private boolean _isRecreating = false;

    private JestClient jestClient;
    private String dataSetName;

    public DirectorySizeChecker() {

    }

    // used only during java indexing process
    public DirectorySizeChecker(DatasetConfiguration dc) {
        // the ordering matters!
        _isIndexing = true;
        init(dc);
    }

    // used in Velocity and FreeMarker
    public DirectorySizeChecker init(DatasetConfiguration dc) {
        indexType = dc.getIndexType();
        if (indexType == null || indexType == IndexType.LUCENE) {
            _mainIndexDir = dc.getMainIndexDirectoryFile();
            _tempIndexDir = dc.getTempIndexDirectoryFile();
            _indexMaxSize = dc.getIndexMaxSize();
            if (_isIndexing && _isRecreating) {
                _mainIndexDir = IndexStatus.findNonActiveMainDirectoryFile(dc);
                _tempIndexDir = null;
            } else if (_isIndexing && !_isRecreating) {
                _mainIndexDir = IndexStatus.findActiveMainDirectoryFile(dc);
                _tempIndexDir = IndexStatus.findNonActiveTempDirectoryFile(dc);
            } else {
                _mainIndexDir = IndexStatus.findActiveMainDirectoryFile(dc);
                _tempIndexDir = IndexStatus.findActiveTempDirectoryFile(dc);
            }
        } else {
            jestClient = SpringContextUtil.getBean(JestClient.class);
            dataSetName = dc.getName();
        }
        return this;
    }

    public void setIsIndexing(boolean indexing) {
        _isIndexing = indexing;
    }

    public boolean isDirectorySizeOverLimit() {
        return isOverLimit(_indexMaxSize);
    }

    public boolean isOverLimit(double maxSize) {
        return false;
    }

    public void setCheckInterval(long checkInterval) {
        _directoryCheckInterval = checkInterval;
    }

    /*
     * checkInterval, in seconds default to 60 seconds
     */
    public long getCheckInterval() {
        return _directoryCheckInterval;
    }

    public void setNextCheck(long nextCheck) {
        _directoryNextCheck = nextCheck;
    }

    public long getNextCheck() {
        return _directoryNextCheck;
    }

    public long getIndexDirectorySize() {
        long dirSize = 0;
        if (indexType == null || indexType == IndexType.LUCENE) {
            dirSize = getDirectorySize(_mainIndexDir);
            dirSize += getDirectorySize(_tempIndexDir);
        } else if (indexType == IndexType.ELASTICSEARCH) {
            IndicesExists indicesExists = new IndicesExists.Builder(dataSetName).build();
            JestResult jestResult = JestExecute.executeNoCheck(jestClient, indicesExists);
            if (jestResult.isSucceeded()) {
                // This means the index exists
                Stats request = new Stats.Builder().addIndex(dataSetName).build();
                jestResult = JestExecute.execute(jestClient, request);
                StatsResult result = new StatsResult(jestResult);
                dirSize = result.getSizeForFirstIndex();
            }
        }
        return dirSize;
    }

    public long getTemporaryIndexDirectorySize() {
        long dirSize = getDirectorySize(_tempIndexDir);
        return dirSize;
    }

    public double getIndexDirectorySizeInMB() {
        try {
            return getIndexDirectorySize() * 1.0 / 1024 / 1024;
        } catch (NullPointerException npe) {
            return 0;
        }
    }

    /**
     * @param b
     */
    public void setIsRecreate(boolean b) {
        _isRecreating = b;
    }

    public File getMainIndexDirectory() {
        return _mainIndexDir;
    }

    public File getTempIndexDirectory() {
        return _tempIndexDir;
    }

    public static long countDirectorySize(File dir) {
        long totalSize = 0;
        if (dir == null || !dir.exists()) {
            return 0;
        }

        File[] files = dir.listFiles(nameFilter);
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                totalSize += files[i].length();
            }
        }

        File[] subDirs = dir.listFiles(dirFilter);
        if (subDirs != null) {
            for (int i = 0; i < subDirs.length; i++) {
                totalSize += countDirectorySize(subDirs[i]);
            }
        }
        return totalSize;
    }

    /**
     * physically count the directory size
     */
    public static long getDirectorySize(File dir) {
        try {
            return countDirectorySize(dir);
        } catch (NullPointerException npe) {
            return 0;
        }
    }
}
