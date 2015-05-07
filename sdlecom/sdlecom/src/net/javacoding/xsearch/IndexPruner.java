package net.javacoding.xsearch;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.utility.FileUtil;

/**
 * Prune old documents in a set of Lucene indexes.
 */
public class IndexPruner {
    public static void emptyIndexData(DatasetConfiguration dc) throws Exception {
        FileUtil.deleteAll(dc.getMainIndexDirectoryFile());
        FileUtil.deleteAll(dc.getAltMainIndexDirectoryFile());
        FileUtil.deleteAll(dc.getTempIndexDirectoryFile());
        FileUtil.deleteAll(dc.getAltTempIndexDirectoryFile());
        FileUtil.deleteAll(dc.getWorkDirectoryFile());
    }
}
