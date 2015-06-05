/*
 * Created on 2006-1-22
 */
package net.javacoding.xsearch.storage;

import net.javacoding.xsearch.config.StorageConfiguration;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

public class LocalStorage implements Storage {

    StorageConfiguration stc = null;
    public LocalStorage(StorageConfiguration stc) {
        this.stc = stc;
    }
    public IndexReader getIndexReader() {
        // TODO Auto-generated method stub
        return null;
    }

    public IndexWriter getIndexWriter() {
        // TODO Auto-generated method stub
        return null;
    }

    public Storage getInstance() {
        // TODO Auto-generated method stub
        return null;
    }

}
