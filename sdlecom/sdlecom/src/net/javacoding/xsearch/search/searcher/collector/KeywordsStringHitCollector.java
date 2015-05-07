package net.javacoding.xsearch.search.searcher.collector;

import java.io.IOException;

import net.javacoding.xsearch.lib.LayeredStringArrayList;
import net.javacoding.xsearch.search.KeywordsFieldCache;
import net.javacoding.xsearch.search.result.filter.FilterColumn;
import net.javacoding.xsearch.search.searcher.IndexReaderSearcher;

public class KeywordsStringHitCollector extends AbstractSumHitCollector {

	private LayeredStringArrayList docToColumn = null;
	private LayeredStringArrayList[] docToColumns = null;
	public KeywordsStringHitCollector(IndexReaderSearcher irs, FilterColumn filterColumn) {
        super(irs,filterColumn);
		try {
			if(irs.subReaders!=null){
				docToColumns = new LayeredStringArrayList[irs.subReaders.length];
				for(int i=0;i<docToColumns.length;i++){
					docToColumns[i] = KeywordsFieldCache.DEFAULT.getStrings(irs.subReaders[i], filterColumn.column.getColumnName());
				}
			}else{
				docToColumn = KeywordsFieldCache.DEFAULT.getStrings(irs.getIndexReader(), filterColumn.column.getColumnName());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    int whichReader = 0;
	public void collect(int doc, float score) {
		LayeredStringArrayList d2c = null;
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
			String c = d2c.get(doc, x);
			if (c == null)
				return;
			filterColumn.increaseCountByString(c,sum);
		}
	}
}
