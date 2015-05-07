package net.javacoding.xsearch.search.memory;

import java.io.File;
import java.io.IOException;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.core.exception.DataSourceException;
import net.javacoding.xsearch.search.searcher.IndexReaderSearcher;
import net.javacoding.xsearch.search.searcher.SearcherProvider;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;

public class BufferIndex {
    private RAMDirectory directory;
    private IndexReader reader;
    private BufferIndexSearcherProvider dummySearcherProvider;
    
    public BufferIndex() {
        try {
            directory = new RAMDirectory();
            IndexWriter writer = new IndexWriter(directory, new WhitespaceAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);
            writer.optimize();
            writer.close();
            writer = null;
            reader = IndexReader.open(directory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public synchronized void addDocuemnt(DatasetConfiguration dc, Document d) {
        try {
            IndexWriter writer = new IndexWriter(directory, dc.getIndexingAnalyzer(), new IndexWriter.MaxFieldLength(dc.getMaxFieldLength()));
            String pkName = dc.getPrimaryKeyColumn().getName();
            String pkValue = d.get(pkName);
            writer.updateDocument(new Term(pkName,pkValue), d);
            writer.optimize();
            writer.close();
        } catch (CorruptIndexException e) {
            e.printStackTrace();
        } catch (LockObtainFailedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    protected void finalize() throws Throwable {
        if(reader!=null) {
            reader.close();
            reader = null;
        }
        if(directory!=null) {
            directory.close();
            directory = null;
        }
    }
    public RAMDirectory getDirectory() {
        return directory;
    }
    public void setDirectory(RAMDirectory directory) {
        this.directory = directory;
    }
    public IndexReader getReader() {
        try {
            if(reader==null || !reader.isCurrent()) {
                closeReader();
                if(IndexReader.indexExists(directory)) {
                    reader = IndexReader.open(directory);
                }
            }
        } catch (CorruptIndexException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return reader;
    }
    public void setReader(IndexReader reader) {
        this.reader = reader;
    }
    public void closeReader() throws IOException {
        if(reader!=null) {
            reader.close();
            reader = null;
        }
    }
    
    public static class BufferIndexSearcherProvider implements SearcherProvider{
        public void close(IndexReaderSearcher s) throws Exception {
        }
        public boolean configure(File mainDir, File tempDir, DatasetConfiguration dc, boolean isInMemory) throws DataSourceException, IOException {
            return false;
        }
        public int counter() {
            return 0;
        }
        public IndexReaderSearcher getIndexReaderSearcher() throws Exception {
            return null;
        }
        public boolean isInMemorySearch() {
            return true;
        }
        public boolean isInterim() {
            return false;
        }
        public void setInterim(boolean interim) {
        }

        public void shutdown() {
        }
    }
    
    public SearcherProvider getSearcherProvider() {
        if(dummySearcherProvider==null) {
            dummySearcherProvider = new BufferIndexSearcherProvider();
        }
        return dummySearcherProvider;
    }
}
