package net.javacoding.xsearch.core.component;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Properties;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.RecordManagerOptions;
import jdbm.btree.BTree;
import jdbm.helper.StringComparator;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.utility.FileUtil;

public class IndexingCache {
    File dbFile;
    RecordManager mydb;
    BTree indexByName;
    int commitInteval = 10000;
    int counter;
    public IndexingCache(DatasetConfiguration dc, String name) {
        init(dc,name);
    }
    private void init(DatasetConfiguration dc, String name) {
        try {
            dbFile = FileUtil.resolveFile(dc.getWorkDirectoryFile(), name);
            dbFile.getParentFile().mkdirs();
            clearFile(dbFile);
            Properties p = new Properties();
            p.setProperty(RecordManagerOptions.DISABLE_TRANSACTIONS, "true");
            p.setProperty(RecordManagerOptions.CACHE_SIZE, "100000");
            this.mydb = RecordManagerFactory.createRecordManager(dbFile.getAbsolutePath(), p);
            this.indexByName = loadOrCreateBTree(mydb, "default", new StringComparator());
            this.counter = commitInteval;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void clearFile(File file) {
        if(!file.getParentFile().exists()) {
            return;
        }
        File[] files = file.getParentFile().listFiles();
        for(File f : files) {
            if(f.getName().startsWith(file.getName())) {
                f.delete();
            }
        }
    }
    public void close() {
        try {
            this.mydb.commit();
            this.mydb.close();
            clearFile(dbFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean contains(Object pkValue) {
        return lookup(pkValue)!=null;
    }
    public Object lookup(Object pkValue) {
        try {
            return indexByName.find(pkValue);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public Object store(Object pkValue, Object obj) {
        try {
            Object ret = this.indexByName.insert(pkValue, obj, true);
            if(this.counter--<0) {
                this.mydb.commit();
                this.counter = commitInteval;
            }
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Obtains a BTree used to index objects, or creates it if it does not
     * exist.
     * 
     * @param aRecordManager
     *            the database.
     * @param aName
     *            the name of the BTree.
     * @param aComparator
     *            the Comparator object used to sort the elements of the BTree.
     * @return the BTree with that name.
     * @throws IOException
     *             if an I/O error happens.
     */
    public static BTree loadOrCreateBTree(RecordManager aRecordManager, String aName, Comparator aComparator) throws IOException {
        // So you can't remember the recordID of the B-Tree? Well, let's
        // try to remember it from its name...
        long recordID = aRecordManager.getNamedObject(aName);
        BTree tree = null;

        if (recordID == 0) {
            // Well, the B-Tree has not been previously stored,
            // so let's create one
            tree = BTree.createInstance(aRecordManager, aComparator);
            // store it with the given name
            aRecordManager.setNamedObject(aName, tree.getRecid());
            // And commit changes
            aRecordManager.commit();
        } else {
            // Yes, we already created this B-Tree in a previous run,
            // so let's retrieve it from the record manager
            tree = BTree.load(aRecordManager, recordID);
        }
        return tree;
    }
}
