package net.javacoding.xsearch.config;

import java.io.File;

public interface StorageConfiguration {

    /**
     * @return interned name
     */
    public abstract String getName();

    public abstract String getIndexdir();

    public abstract void setIndexdir(String indexdir);

    public abstract File getWorkDirectoryFile();

    public abstract String getWorkDirectory();

    public abstract void setWorkDirectory(String workDirectory);

    public abstract double getIndexMaxSize();

    public abstract void setIndexMaxSize(double indexMaxSize);

    public abstract float getMergePercentage();

    public abstract void setMergePercentage(float percent);

    public abstract int getPrunePercentage();

    public abstract void setPrunePercentage(int percent);

    /**
     * Sets the base directory.
     */
    public abstract void setBaseDirectory(File baseDirectory);

    public abstract File getBaseDirectoryFile();

    /**
     * @return the absolute pathname for the index directory.
     */
    public abstract String getIndexDirectory();

    /**
     * @return the File object for the index directory.
     */
    public abstract File getIndexDirectoryFile();

    /**
     * @return the File object for the main index directory
     */
    public abstract File getMainIndexDirectoryFile();

    /**
     * @return the File object for the temp index directory
     */
    public abstract File getTempIndexDirectoryFile();

}
