package net.javacoding.xsearch.core;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.text.DecimalFormat;

import net.javacoding.xsearch.config.DataSource;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.core.impl.IndexerContextImpl;
import net.javacoding.xsearch.status.IndexStatus;
import net.javacoding.xsearch.utility.FileUtil;

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
        ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
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

    private static boolean      isChecking    = false;
    public static DecimalFormat sizeFormatter = new DecimalFormat("#,###");

    public boolean isOverLimit(double maxSize) {
        //avoid setting it to 0 and got free unlimited index size
        /*if (requiresChecking()) {
            synchronized (logger) {// just use one usable object to lock
                if (isChecking) return false; // let the checking thread check, and set stop flags
                isChecking = true;
            }
            // only one thread is executing the rest
            long dirSize = getIndexDirectorySize();
            logger.info("Dir size:" + sizeFormatter.format(dirSize) + " Bytes");
            if (dirSize >= (maxSize * BYTES_PER_MEGA)) {
                logger.warn("Index directory " + _mainIndexDir + " is over size limit " + maxSize + " MB");
                isChecking = false;
                return true;
            }
            touch();
            isChecking = false;
        }*/
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

    /**
     * Is it time to check to see if the resource source has been updated?
     */
    private boolean requiresChecking() {
        /*
         * short circuit this if modificationCheckInterval == 0 as this means "don't check"
         */
        if (getCheckInterval() <= 0) { return false; }

        /*
         * see if we need to check now
         */
        return (System.currentTimeMillis() >= getNextCheck());
    }

    /**
     * 'Touch' this directory and thereby resetting the nextCheck field.
     */
    private void touch() {
        setNextCheck(System.currentTimeMillis() + (MILLIS_PER_SECOND * getCheckInterval()));
    }

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
