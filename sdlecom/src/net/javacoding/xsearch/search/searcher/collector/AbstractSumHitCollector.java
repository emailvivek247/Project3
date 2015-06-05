package net.javacoding.xsearch.search.searcher.collector;

import net.javacoding.xsearch.lib.FlexibleIntArray;
import net.javacoding.xsearch.search.KeywordsFieldCache;
import net.javacoding.xsearch.search.result.filter.FilterColumn;
import net.javacoding.xsearch.search.searcher.IndexReaderSearcher;

import org.apache.lucene.search.HitCollector;

public abstract class AbstractSumHitCollector extends HitCollector{

    IndexReaderSearcher irs = null;

    FilterColumn filterColumn = null;

    private int[][] docToSum = null;
    private int[][][] docToSums = null;
    boolean hasSum = false;

    public AbstractSumHitCollector(IndexReaderSearcher irs, FilterColumn filterColumn) {
        this.irs = irs;
        this.filterColumn = filterColumn;
        if(filterColumn.column.getSumColumnNames()!=null) {
            if(irs.subReaders!=null){
                docToSums = new int[irs.subReaders.length][filterColumn.column.getSumColumnNames().length][];
                for(int i=0;i<docToSums.length;i++){
                    for(int j=0;j<filterColumn.column.getSumColumnNames().length;j++) {
                        try {
                            docToSums[i][j] = KeywordsFieldCache.DEFAULT.getIntArray(irs.subReaders[i], filterColumn.column.getSumColumnNames()[j]);
                        }catch(Exception e) {
                            docToSums[i][j] = null;
                        }
                    }
                }
            }else{
                docToSum = new int[filterColumn.column.getSumColumnNames().length][];
                for(int j=0;j<docToSum.length;j++) {
                    try {
                        docToSum[j] = KeywordsFieldCache.DEFAULT.getIntArray(irs.getIndexReader(), filterColumn.column.getSumColumnNames()[j]);
                    }catch(Exception e) {
                        docToSum[j] = null;
                    }
                }
            }
            hasSum = true;
            sumReturn = new int[filterColumn.column.getSumColumnNames().length];
        }
    }

    private int[][] d2s;
    private int[] sumReturn;
    //doc is already adjusted within caller collect()!
    public int[] sum(int doc, int whichReader) {
        if(hasSum) {
            if(docToSums!=null) {
                d2s = docToSums[whichReader];
            }else {
                d2s = docToSum;
            }
            if(d2s==null) return null;
            for(int i=0;i<sumReturn.length;i++) {
                int ret = d2s[i][doc];
                sumReturn[i] = ret == FlexibleIntArray.EMPTY_INT ? 0 : ret;
            }
            return sumReturn;
        }
        return null;
    }

}
