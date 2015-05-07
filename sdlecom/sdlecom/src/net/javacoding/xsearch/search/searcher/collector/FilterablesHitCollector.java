package net.javacoding.xsearch.search.searcher.collector;

import java.util.ArrayList;
import java.util.List;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.IndexFieldType;
import net.javacoding.xsearch.search.analysis.AdvancedQueryAnalysis;
import net.javacoding.xsearch.search.result.filter.FilterResult;
import net.javacoding.xsearch.search.result.filter.FilterValue;
import net.javacoding.xsearch.search.result.filter.FilteredColumn;
import net.javacoding.xsearch.search.result.filter.StringFilterValue;
import net.javacoding.xsearch.search.result.filter.StringRangeFilterValue;
import net.javacoding.xsearch.search.searcher.IndexReaderSearcher;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.search.HitCollector;

public class FilterablesHitCollector extends HitCollector {

    private Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.search.searcher.FilterablesHitCollector");

    private HitCollector[] hcs = null;
    public FilterablesHitCollector(IndexReaderSearcher irs, FilterResult filterResult) {
        if(filterResult.filterColumns == null)return;
        List<HitCollector> hcList = new ArrayList<HitCollector>(filterResult.filterColumns.size());
        for(int i=0; i<filterResult.filterColumns.size(); i++) {
            Column col = filterResult.filterColumns.get(i).column;
            if(col.getFilterParentColumnName()==null || filterResult.hasFilteredColumn(col.getFilterParentColumnName())) {
                if(col.getHasMultipleKeywords()) {
                    try {
                        if("Number" == col.getColumnTypeShortName()){
                            hcList.add(new KeywordsIntHitCollector(irs,filterResult.filterColumns.get(i)));
                            //hcs[i] = new KeywordsStringHitCollector(ir,col,columnToCounts[i]);
                        }else{
                            hcList.add(new KeywordsStringHitCollector(irs,filterResult.filterColumns.get(i)));
                            //use this only when Solid State Disk is popular
                            //hcs[i] = new CachedDocToValuesHitCollector(ir,col,columnToCounts[i]);
                        }
                    } catch (RuntimeException re) {
                        logger.debug("load Column:"+col.getColumnName(), re);
                    }//to prevent RuntimeException ("no terms in field " + field) by FieldCache.DEFAULT.getStrings}
                } else if(col.getIndexFieldType()==IndexFieldType.KEYWORD_DATE_HIERARCHICAL) {
                    //even filtered, still need to filter because it's date hierarchical
                    FilteredColumn fc = filterResult.getFilteredColumn(col.getColumnName());
                    if(fc==null){
                        hcList.add(new KeywordHierarchicalDateHitCollector(irs,filterResult.filterColumns.get(i), "y"));
                    }else {
                        FilterValue v = fc.getValue();
                        String text = "";
                        if(v instanceof StringFilterValue) {
                            text = ((StringFilterValue)v).toString();
                        }else if(v instanceof StringRangeFilterValue) {
                            text = U.nvl(((StringRangeFilterValue)v).getBegin(),((StringRangeFilterValue)v).getEnd());
                        }
                        if(AdvancedQueryAnalysis.yearPattern.matcher(text).matches()) {
                            hcList.add(new KeywordHierarchicalDateHitCollector(irs,filterResult.filterColumns.get(i), "ym"));
                        }
                    }
                }else if(!filterResult.hasFilteredColumn(col.getColumnName())){
                    try {
                        if("Number" == col.getColumnTypeShortName()){
                            hcList.add(new KeywordIntHitCollector(irs,filterResult.filterColumns.get(i)));
                            //hcs[i] = new KeywordStringHitCollector(ir,col,columnToCounts[i]);
                        }else{
                            hcList.add(new KeywordStringHitCollector(irs,filterResult.filterColumns.get(i)));
                        }
                    } catch (RuntimeException re) {
                        logger.debug("load Column:"+col.getColumnName(), re);
                    }//to prevent RuntimeException ("no terms in field " + field) by FieldCache.DEFAULT.getStrings}
                }
            }
        }
        hcs = hcList.toArray(new HitCollector[hcList.size()]);
    }

    public void collect(int doc, float score) {
        for(int i=0; i< hcs.length; i++) {
            if(score>0) {
                hcs[i].collect(doc, score);
            }
        }
    }
}
