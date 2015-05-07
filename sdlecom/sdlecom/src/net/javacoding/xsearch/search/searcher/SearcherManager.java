package net.javacoding.xsearch.search.searcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.IndexFieldType;
import net.javacoding.xsearch.search.KeywordsFieldCache;
import net.javacoding.xsearch.search.SearchQueryParser;
import net.javacoding.xsearch.search.impl.DateWeightedSortComparator;
import net.javacoding.xsearch.status.IndexStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

/**
 * Static variables stored in memory when server is running
 * 
 * 
 */

public class SearcherManager {
    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.search.searcher.SearcherManager");

    private static Hashtable<String, SearcherProvider> searcherPools = new Hashtable<String, SearcherProvider>();

    /**
     * called in ApplicaitonInitServlet.destroy
     */
    public static void destroy() {
        for (Enumeration<String> e = searcherPools.keys() ; e.hasMoreElements() ;) {
            destroy(e.nextElement());
        }
    }

    public static void init(DatasetConfiguration dc) {
        try {
            setSearcherProvider(dc.getName(), createSearcherProviderByDataset(dc));
        }catch(Throwable t) {
            logger.info("Failed to add searcher pool "+dc.getName()+": " + t.toString());
            t.printStackTrace(System.out);
        }
    }

    public static SearcherProvider createSearcherProviderByDataset(DatasetConfiguration dc) {
        SearcherProvider sp = null;
        boolean ret = false;
        try {
            sp = new DefaultSearcherProvider();
            ret = sp.configure(IndexStatus.findActiveMainDirectoryFile(dc), 
                    IndexStatus.findActiveTempDirectoryFile(dc), 
                    dc,
                    dc.getIsInMemorySearch()
                    );
            if(ret){
                // warm up
                logger.info("warm up "+dc.getSearcherMaxidle()+" searcher(s) for " + dc.getName()+" ...");
                warmUpSearch(sp, dc);
                logger.info("Searcher ready for " + dc.getName());
            }
        } catch (Throwable t) {
            logger.info("Failed to add searcher pool for " + dc.getName() + ":" + t);
            t.printStackTrace();
        } finally {
            if (!ret) {
                sp.shutdown();
                sp = null;
            }
        }
        return sp;
    }
    
    public static void setSearcherProvider(String name, SearcherProvider sp) {
        if (name == null) return;
        if (sp == null) return;
        name = name.trim().toLowerCase().intern();
        synchronized(searcherPools){
            searcherPools.put(name, sp);
        }
    }

    public static SearcherProvider getSearcherProvider(String name) {
        if (name == null) return null;
        name = name.trim().toLowerCase().intern();
        synchronized(searcherPools){
            return (SearcherProvider) searcherPools.get(name);
        }
    }

    public static void destroy(String name) {
    	switchSearchProvider(name,null);
    }
    /**
     * Atomic operation to switch in new searcher pool and switch out old searcher pool.
     * Close the old searcher pool.
     * @param name
     * @param the new initialized warmed-up searcher pool
     */
    public static void switchSearchProvider(String name, SearcherProvider newSp) {
        name = name.trim().toLowerCase().intern();
        SearcherProvider oldSp = (SearcherProvider) searcherPools.get(name);
        synchronized(searcherPools){
            logger.info("removing old searchers...");
            searcherPools.remove(name);
            if(newSp!=null){
                logger.info("adding new searchers...");
                searcherPools.put(name, newSp);
            }
        }
        if(oldSp!=null){
            logger.info("shut down old searchers...");
            oldSp.shutdown();
        }
    }

    /**
     * Must be used togethher with closeSearcher()
     * 
     * @param name index name
     * @return a searcher borrowed from searcher pool of the index
     */
    public static IndexReaderSearcher getIndexReaderSearcher(DatasetConfiguration dc) {
        IndexReaderSearcher irs = null;
        try {
            irs = getIndexReaderSearcher(dc.getName());
        } catch (Throwable t) {
            logger.info("Failed to get either Searcher: " + t);
            t.printStackTrace();
        }
        return irs;
    }

    public static IndexReaderSearcher getIndexReaderSearcher(String name) {
        SearcherProvider sp = getSearcherProvider(name);
        try {
            if (sp != null) return sp.getIndexReaderSearcher();
        } catch (Exception e) {
            logger.info("Failed to get Searcher: " + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Must be used after using getSearcher(name)
     * 
     * @param name index name
     * @param s the searcher to return to the searcher pool
     */
    public static void close(String name, IndexReaderSearcher s) {
        if (name == null) {
            if (s != null) s.closeSearcher();
            return;
        }
        SearcherProvider sp = getSearcherProvider(name);
        try {
            if (sp != null) {
                if(s!=null) s.release();
            } else {
                //logger.debug("close one searcher");
                if (s != null) s.closeSearcher();
            }
        } catch (Throwable t) {
            logger.info("Failed to get Searcher: " + t);
            t.printStackTrace();
        }
    }

    /**
     * multiple keywords
     *   number
     *     KeywordsIntHitCollector
     *       KeywordsFieldCache.DEFAULT.getInts
     *   other
     *     KeywordsStringHitCollector
     *       KeywordsFieldCache.DEFAULT.getStrings
     * single value keywords
     *   number
     *     KeywordIntHitCollector
     *       KeywordsFieldCache.DEFAULT.getIntArray, getColumnName
     *   other
     *     KeywordStringHitCollector
     *       FieldCache.DEFAULT.getStrings
     * hieracrchical date
     *   KeywordHierarchicalDateHitCollector
     *     FieldCache.DEFAULT.getStrings, prefix "y", "ym"
     * all inherit from
     *   AbstractSumHitCollector
     *     KeywordsFieldCache.DEFAULT.getIntArray, getSumColumnName  
     * @param sp
     * @param dc
     */
    private static void warmUpSearch(SearcherProvider sp, DatasetConfiguration dc) {
        IndexReaderSearcher irs = null;
        try {
            irs = sp.getIndexReaderSearcher();
            
            if(irs.getIndexReader().maxDoc()<=0){
                //logger.debug("empty index:"+irs.getIndexReader().maxDoc());
                return;
            }

            // get top query
            int hitCount = 0;
            Query query = null;
            try {
                query = SearchQueryParser.parse(dc, "anything");
                Hits hits = irs.getSearcher().search(query);
                hitCount = hits.length();
                //logger.debug("hits length:" + hitCount);
            } catch (Throwable ex) {
                logger.error("Error In warmUpSearch", ex);
            }
            
            // warm up time-based tiered weight
            // for debugging: dc.setDateWeightColumnName(dc.getModifiedDateColumn().getColumnName());
            if (dc.getDateWeightColumnName()!=null) {
                long start_time = System.currentTimeMillis();
                irs.getSearcher().search(SearchQueryParser.parse(dc, "anything"), new Sort(new SortField(dc.getDateWeightColumnName(), new DateWeightedSortComparator(dc), true)));
                //logger.debug("time-tiered weight search time:"+(System.currentTimeMillis()-start_time));
            }

            // search each sort by
            ArrayList al = dc.getSortableColumns();
            for (int i = 0; i < al.size(); i++) {
                Column c = (Column) al.get(i);
                Sort s;
                if(!c.getHasMultipleKeywords()){
                    if(c.getIsNumber()) {
                        logger.info("warming up sorting number column:" + c.getColumnName());
                        s = new Sort(new SortField[] { new SortField("s"+c.getColumnName(), SortField.AUTO, true), SortField.FIELD_SCORE });
                    }else {
                        logger.info("warming up sorting string column:" + c.getColumnName());
                        s = new Sort(new SortField[] { new SortField(c.getColumnName(), SortField.AUTO, true), SortField.FIELD_SCORE });
                    }
                    try{
                    	irs.getSearcher().search(query, s);
                    }catch(RuntimeException e){
                        logger.error("Cannot warmup index for column:" + c.getColumnName(), e);
                    }
                }
            }

            List<Column> columns = dc.getFilterableColumns();
            for(Column col : columns) {
                if(col.getHasMultipleKeywords()) {
                	if(col.getIsNumber()){
                        try{
                            logger.info("warming up multi-valued int column:" + col.getColumnName());
                            KeywordsFieldCache.DEFAULT.getInts(irs.getIndexReader(), col.getColumnName());
                            //KeywordsFieldCache.DEFAULT.getStrings(irs.getIndexReader(), col.getColumnName());
                        }catch(RuntimeException e){
                            logger.error("Cannot warmup index for multi valued int column:" + col.getColumnName(), e);
                        }
                	}else{
                        try{
                            logger.info("warming up multi-valued column:" + col.getColumnName());
                            KeywordsFieldCache.DEFAULT.getStrings(irs.getIndexReader(), col.getColumnName());
                        }catch(RuntimeException e){
                            logger.error("Cannot warmup index for multi valued string column:" + col.getColumnName(), e);
                        }
                	}
                }else{
                    if(col.getIsNumber()){
                        try{
                            logger.info("warming up int column:" + col.getColumnName());
                            KeywordsFieldCache.DEFAULT.getIntArray(irs.getIndexReader(), col.getColumnName());
                            //KeywordsFieldCache.DEFAULT.getStrings(irs.getIndexReader(), col.getColumnName());
                        }catch(RuntimeException e){
                            logger.error("Cannot warmup index for int column:" + col.getColumnName(), e);
                        }
                    }else if(col.getIndexFieldType()==IndexFieldType.KEYWORD_DATE_HIERARCHICAL){
                        FieldCache.DEFAULT.getStrings(irs.getIndexReader(), "y"+col.getColumnName());
                        FieldCache.DEFAULT.getStrings(irs.getIndexReader(), "ym"+col.getColumnName());
                    }else{
                        try{
                            logger.info("warming up string column:" + col.getColumnName());
                            FieldCache.DEFAULT.getStrings(irs.getIndexReader(), col.getColumnName());
                            //KeywordsFieldCache.DEFAULT.getStrings(irs.getIndexReader(), col.getColumnName());
                        }catch(RuntimeException e){
                            logger.error("Cannot warmup index for string column:" + col.getColumnName(), e);
                        }
                    }
                }
            }
            for(Column col : columns) {
                if(col.getSumColumnNames()!=null) {
                    for(String sumColumnName : col.getSumColumnNames()) {
                        Column sumColumn = dc.getColumn(sumColumnName);
                        //no need to check whether this sum column is already loaded or not
                        //since it's just a cache
                        if(sumColumn==null) {
                            logger.warn("Column "+col.getColumnName()+" sums on column "+sumColumnName+" that does not exist any more!");
                            continue;
                        }
                        if(sumColumn.getIsNumber()) {
                            try{
                                logger.info("warming up sum column for:" + col.getColumnName());
                                KeywordsFieldCache.DEFAULT.getIntArray(irs.getIndexReader(), sumColumnName);
                            }catch(RuntimeException e){
                                logger.error("Cannot warmup index for sum column for:" + col.getColumnName(), e);
                            }
                        }
                    }
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                irs.release();
            } catch (Exception e1) {}
        }
    }
}
