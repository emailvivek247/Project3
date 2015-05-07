package net.javacoding.xsearch.search.searcher.collector;

import java.io.IOException;

import net.javacoding.xsearch.lib.FlexibleIntArray;
import net.javacoding.xsearch.lib.LayeredIntArrayList;
import net.javacoding.xsearch.search.KeywordsFieldCache;
import net.javacoding.xsearch.search.result.filter.FilterColumn;
import net.javacoding.xsearch.search.searcher.IndexReaderSearcher;

public class KeywordsIntHitCollector extends AbstractSumHitCollector {

	private LayeredIntArrayList docToColumn = null;
	private LayeredIntArrayList[] docToColumns = null;
	public KeywordsIntHitCollector(IndexReaderSearcher irs, FilterColumn filterColumn) {
        super(irs,filterColumn);
		try {
			if(irs.subReaders!=null){
				docToColumns = new LayeredIntArrayList[irs.subReaders.length];
				for(int i=0;i<docToColumns.length;i++){
					docToColumns[i] = KeywordsFieldCache.DEFAULT.getInts(irs.subReaders[i], filterColumn.column.getColumnName());
				}
			}else{
				docToColumn = KeywordsFieldCache.DEFAULT.getInts(irs.getIndexReader(), filterColumn.column.getColumnName());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    int whichReader = 0;
	public void collect(int doc, float score) {
		LayeredIntArrayList d2c = null;
		if(docToColumns!=null){
			whichReader = irs.getReaderIndex(doc);
			d2c = docToColumns[whichReader];
			doc -= irs.readerStarts[whichReader];
		}else{
			d2c = docToColumn;
		}
        if(d2c==null)return;
        int[] sum = super.sum(doc,whichReader);
		for (int x = 0;; x++) {
			int c = d2c.get(doc, x);
	        if (c == FlexibleIntArray.EMPTY_INT)
	            return;
			filterColumn.increaseCountByInt(c, sum);
		}
	}
}
