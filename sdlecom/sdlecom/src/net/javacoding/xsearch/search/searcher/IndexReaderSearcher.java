/*
 */
package net.javacoding.xsearch.search.searcher;

import java.io.IOException;
import java.util.ArrayList;

import net.javacoding.xsearch.search.memory.BufferIndex;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;

/**
 *  A simple wrapper to extent IndexSearcher so the IndexReader within it can be accessed
 */
public class IndexReaderSearcher {
    private SearcherProvider searcherProvider;
    private SearcherProvider[] subSearcherProviders;
    
    private IndexReader reader;
    private IndexSearcher searcher;

    public int[] readerStarts;
    public IndexReader[] subReaders;

    public IndexReaderSearcher(IndexReader reader) {
        this.reader = reader;
    }
    /*
     * initialize subReaders and readerStarts, which are only used with Multiple indexReaderSearchers
     * This is provided to access cached doc->value(s) mapping for each index reader, which are warmed up
     * during server starts up or when index just created. Accessing each index reader individually saving
     * from creating the doc->value(s) mapping for MultiReader.
     */
    public IndexReaderSearcher(ArrayList<IndexReaderSearcher> irss, BufferIndex bi) {
    	//initialize readerStarts
    	//based on MultiReader.initialize()
        int start = 0;
        int maxDoc = 0;
        if(bi!=null&&bi.getReader()!=null) {
            subReaders = new IndexReader[irss.size()+1];
            subSearcherProviders = new SearcherProvider[irss.size()+1];
            this.readerStarts = new int[irss.size()+1];
            subReaders[0] = bi.getReader();
            subSearcherProviders[0] = bi.getSearcherProvider();
            maxDoc = subReaders[0].maxDoc();
            start = 1;
        }else {
            if(irss.size()==1) {
                this.reader = irss.get(0).reader;
                this.searcherProvider = irss.get(0).searcherProvider;
                return;
            }else {
                subReaders = new IndexReader[irss.size()];
                this.readerStarts = new int[irss.size()];
                subSearcherProviders = new SearcherProvider[irss.size()];
            }
        }
        for(int i=start;i<subReaders.length;i++){
        	subReaders[i] = irss.get(i-start).reader;
        	this.readerStarts[i] = maxDoc;
            maxDoc += subReaders[i].maxDoc();      // compute maxDocs
            subSearcherProviders[i] = irss.get(i-start).searcherProvider;
        }
        UniqueMultiReader multiReader = new UniqueMultiReader(subReaders);

		this.reader = multiReader;
    }
    public static class UniqueMultiReader extends MultiReader{
        public UniqueMultiReader(IndexReader[] subReaders) {
            super(subReaders);
        }
        public boolean equals(Object obj) {
            if(obj instanceof UniqueMultiReader) {
                UniqueMultiReader other = (UniqueMultiReader)obj;
                if(other.subReaders.length==this.subReaders.length) {
                    for(int i=0;i<this.subReaders.length;i++) {
                        if(!this.subReaders[i].equals(other.subReaders[i])) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        public int hashCode() {
            int ret = 7;
            for(IndexReader r : this.subReaders) {
                ret += r.hashCode()*31;
            }
            return ret;
        }
        
    }
    public IndexReader getIndexReader() {
        return this.reader;
    }

    public Searcher getSearcher() {
        this.searcher = new IndexSearcher(reader);
        return this.searcher;
    }

    public void closeSearcher() {
        try {
            if(searcher!=null) {
                searcher.close();
                searcher = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public void closeReader() {
        try {
            if(reader!=null) {
                reader.close();
                reader = null;
                subReaders = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public static IndexReaderSearcher getIndexReaderSearcher(ArrayList<IndexReaderSearcher> irss, BufferIndex bufferIndex){
    	if(irss.size()<0){
    		return null;
    	}else if(irss.size()==1){
    		return new IndexReaderSearcher(irss,bufferIndex);
    	}else{
    		return new IndexReaderSearcher(irss,bufferIndex);
    	}
	}
	
	public int getReaderIndex(int n){
	    int i= this.readerStarts.length - 1;
	    while(n<this.readerStarts[i]) {
	        i--;
	    }
	    return i;
		//return readerIndex(n,this.readerStarts,this.readerStarts.length);
	}
	
    // find reader for doc n. Copied from MultiSegmentReader.readerIndex
	static int readerIndex(int n, int[] starts, int numSubReaders) {
		int lo = 0; // search starts array
		int hi = numSubReaders - 1; // for first element less

		while (hi >= lo) {
			int mid = (lo + hi) >> 1;
			int midValue = starts[mid];
			if (n < midValue)
				hi = mid - 1;
			else if (n > midValue)
				lo = mid + 1;
			else { // found a match
				while (mid + 1 < numSubReaders && starts[mid + 1] == midValue) {
					mid++; // scan to last match
				}
				return mid;
			}
		}
		return hi;
	}
    public SearcherProvider getSearcherProvider() {
        return searcherProvider;
    }
    public void setSearcherProvider(SearcherProvider searcherProvider) {
        this.searcherProvider = searcherProvider;
    }
    public void release() throws Exception {
        if(this.searcherProvider!=null) {
            this.searcherProvider.close(this);
        }
        if(this.subSearcherProviders!=null) {
            for(SearcherProvider sp : this.subSearcherProviders) {
                sp.close(null);
            }
        }
    }
}
