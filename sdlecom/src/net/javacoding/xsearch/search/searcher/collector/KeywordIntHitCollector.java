package net.javacoding.xsearch.search.searcher.collector;

import java.io.IOException;

import net.javacoding.xsearch.lib.FlexibleIntArray;
import net.javacoding.xsearch.search.KeywordsFieldCache;
import net.javacoding.xsearch.search.result.filter.FilterColumn;
import net.javacoding.xsearch.search.searcher.IndexReaderSearcher;

public class KeywordIntHitCollector extends AbstractSumHitCollector {

	private int[] docToColumn = null;
	private int[][] docToColumns = null;

	public KeywordIntHitCollector(IndexReaderSearcher irs, FilterColumn filterColumn) {
	    super(irs,filterColumn);
		try {
			if(irs.subReaders!=null){
				docToColumns = new int[irs.subReaders.length][];
				for(int i=0;i<docToColumns.length;i++){
				    try {
	                    docToColumns[i] = KeywordsFieldCache.DEFAULT.getIntArray(irs.subReaders[i], filterColumn.column.getColumnName());
				    }catch(Exception e) {
	                    docToColumns[i] = null;
				    }
				}
			}else{
				docToColumn = KeywordsFieldCache.DEFAULT.getIntArray(irs.getIndexReader(), filterColumn.column.getColumnName());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    int whichReader = 0;
	public void collect(int doc, float score) {
		int[] d2c = null;
		if(docToColumns!=null){
			whichReader = irs.getReaderIndex(doc);
			d2c = docToColumns[whichReader];
			doc -= irs.readerStarts[whichReader];
		}else{
			d2c = docToColumn;
		}
		if(d2c==null)return;
        int[] sum = super.sum(doc,whichReader);
		int c = d2c[doc];
        if (c == FlexibleIntArray.EMPTY_INT)
            return;
		filterColumn.increaseCountByInt(c, sum);
	}
}
