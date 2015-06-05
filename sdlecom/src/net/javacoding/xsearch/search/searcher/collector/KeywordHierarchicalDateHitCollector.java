package net.javacoding.xsearch.search.searcher.collector;

import java.io.IOException;

import net.javacoding.xsearch.search.result.filter.FilterColumn;
import net.javacoding.xsearch.search.searcher.IndexReaderSearcher;

import org.apache.lucene.search.FieldCache;

public class KeywordHierarchicalDateHitCollector extends AbstractSumHitCollector {

	private String[] docToColumn = null;
	private String[][] docToColumns = null;

	public KeywordHierarchicalDateHitCollector(IndexReaderSearcher irs, FilterColumn filterColumn, String fieldPrefix) {
        super(irs,filterColumn);
		try {
			if(irs.subReaders!=null){
				docToColumns = new String[irs.subReaders.length][];
				for(int i=0;i<docToColumns.length;i++){
					docToColumns[i] = FieldCache.DEFAULT.getStrings(irs.subReaders[i], fieldPrefix+filterColumn.column.getColumnName());
				}
			}else{
				docToColumn = FieldCache.DEFAULT.getStrings(irs.getIndexReader(), fieldPrefix+filterColumn.column.getColumnName());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    int whichReader = 0;
	public void collect(int doc, float score) {
		String[] d2c = null;
		if(docToColumns!=null){
			whichReader = irs.getReaderIndex(doc);
			d2c = docToColumns[whichReader];
			doc -= irs.readerStarts[whichReader];
		}else{
			d2c = docToColumn;
		}
		if(d2c==null)return;
        int[] sum = super.sum(doc,whichReader);
		String c = d2c[doc];
		if (c == null)
			return;
		filterColumn.increaseCountByDate(c,sum);
	}
}
