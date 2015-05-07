/*
 * Created on 2006-1-22
 */
package net.javacoding.xsearch.storage;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

public interface Storage {
    IndexReader getIndexReader();
    IndexWriter getIndexWriter();
}
