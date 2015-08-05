package net.javacoding.xsearch.core;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.status.IndexStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectorySizeChecker {
    private static Logger logger          = LoggerFactory.getLogger("net.javacoding.xsearch.core.DirectorySizeChecker");

    private File          _mainIndexDir   = null;
    private File          _tempIndexDir   = null;
    private double        _indexMaxSize   = 0;
    private boolean       _isIndexing     = false;
    private boolean       _isRecreating   = false;
    
    public DirectorySizeChecker() {}

    //used in Velocity and FreeMarker
    public DirectorySizeChecker init(DatasetConfiguration dc) {
        _mainIndexDir = dc.getMainIndexDirectoryFile();
        _tempIndexDir = dc.getTempIndexDirectoryFile();
        _indexMaxSize = dc.getIndexMaxSize();
        if(_isIndexing && _isRecreating) {
            _mainIndexDir = IndexStatus.findNonActiveMainDirectoryFile(dc);
            _tempIndexDir = null;
        }else if(_isIndexing && !_isRecreating) {
            _mainIndexDir = IndexStatus.findActiveMainDirectoryFile(dc);
            _tempIndexDir = IndexStatus.findNonActiveTempDirectoryFile(dc);
        }else {// _isIndexing == false, used during mergeIndexesIfNeeded
            _mainIndexDir = IndexStatus.findActiveMainDirectoryFile(dc);
            _tempIndexDir = IndexStatus.findActiveTempDirectoryFile(dc);
        }
        return this;
    }

    public void setIsIndexing(boolean indexing) {
        _isIndexing = indexing;
    }

    //used only during java indexing process
    public DirectorySizeChecker(DatasetConfiguration dc) {
        //the ordering matters!
        _isIndexing = true;
        init(dc);
    }

    public boolean isDirectorySizeOverLimit() {
        return isOverLimit(_indexMaxSize);
    }

    public boolean isOverLimit(double maxSize) {
        return false;
    }

    protected static final long   MILLIS_PER_SECOND = 1000;
    public static final long      BYTES_PER_MEGA    = 1024 * 1024;
    // It is also possible to filter the list of returned files.
    // This example does not return any files that start with `.'.
    private static FilenameFilter nameFilter        = new FilenameFilter() {
                                                        public boolean accept(File dir, String name) {
                                                            return !name.startsWith(".");
                                                        }
                                                    };
    // This filter only returns directories
    // According to javadoc,
    // Pathnames denoting the directory itself
    // and the directory's parent directory are not included in the result.
    private static FileFilter     dirFilter         = new FileFilter() {
                                                        public boolean accept(File f) {
                                                            return f.isDirectory();
                                                        }
                                                    };

    protected long _directoryCheckInterval = 17;

    public void setCheckInterval(long checkInterval) {
        _directoryCheckInterval = checkInterval;
    }

    /*
     * checkInterval, in seconds default to 60 seconds
     */
    public long getCheckInterval() {
        return _directoryCheckInterval;
    }

    protected long _directoryNextCheck = 0;

    public void setNextCheck(long nextCheck) {
        _directoryNextCheck = nextCheck;
    }

    public long getNextCheck() {
        return _directoryNextCheck;
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

    public long getIndexDirectorySize() {
        long dirSize = 0;
        dirSize = getDirectorySize(_mainIndexDir);
        dirSize += getDirectorySize(_tempIndexDir);
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

    public static long countDirectorySize(File dir) {
        long totalSize = 0;
        if (dir == null || !dir.exists()) return 0;

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
}
