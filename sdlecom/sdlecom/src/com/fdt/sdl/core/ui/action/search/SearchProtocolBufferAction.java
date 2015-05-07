package com.fdt.sdl.core.ui.action.search;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.api.protocol.SearchProtocol;
import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.search.HTMLEntities;
import net.javacoding.xsearch.search.Highlighter;
import net.javacoding.xsearch.search.HitDocument;
import net.javacoding.xsearch.search.result.SearchResult;
import net.javacoding.xsearch.search.result.SearchSort;
import net.javacoding.xsearch.search.result.filter.Count;
import net.javacoding.xsearch.search.result.filter.FilterColumn;
import net.javacoding.xsearch.search.result.filter.FilterResult;
import net.javacoding.xsearch.status.QueryLogger;
import net.javacoding.xsearch.utility.EscapeChars;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import com.fdt.sdl.core.ui.action.search.SearchAction.SearchContext;


/**
 * This search receives normal url input, but return binary stream of results via Google's Protocol Buffer
 */
public class SearchProtocolBufferAction extends Action {
    private static Logger logger = LoggerFactory.getLogger(SearchProtocolBufferAction.class);
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        long _start = System.currentTimeMillis();
        ActionMessages errors = new ActionMessages();
        SearchProtocol.SearchRequest r = SearchProtocol.SearchRequest.parseFrom(request.getInputStream());
        OutputStream outStream = response.getOutputStream();
        String[] indexNames = null;
        SearchContext sc = new SearchContext();
        try {
            SearchResult sr = new SearchResult();
            sc.debug = r.getDebug();
            indexNames = r.getIndexList().toArray(new String[r.getIndexCount()]);
            sc.irs = SearchAction.getIndexReaderSearcher(indexNames);
            if(r.getIndexCount()>0) {
                sc.dc = ServerConfiguration.getDatasetConfiguration(r.getIndex(0));
            }
            if(sc.debug) logger.info("Got searcher: " + (System.currentTimeMillis() - _start));

            FilterResult filterResult = new FilterResult();

            String q = r.getQuery();

            Query query = SearchAction.getSearchQuery(sr, q, r.getLuceneQuery(), filterResult, request, sc.dc, sc.irs, r.getBooleanOperator(), r.getSearchable(), r.getRandomQuerySeed(), sc.debug);

            List<HitDocument> docs = null;
            int[] total = new int[1];
            if(sc.debug) logger.info("Start Searching: " + (System.currentTimeMillis() - _start));
            long start = System.currentTimeMillis();
            long searchTime = 0;

            List<SearchSort> sortBys = new ArrayList<SearchSort>();
            for(SearchProtocol.Sort s : r.getSortList()) {
                SearchSort ss = new SearchSort(sc.dc.findColumn(s.getColumn()));
                ss.descending = s.getDescending();
                sortBys.add(ss);
            }

            if (query != null) {
                Hits hits = null;
                if(r.getEnalbeFacetSearch()) {
                    SearchAction.narrowBySearch(query, sc.irs, sc.dc, filterResult, errors, request);
                }
                if(sortBys.size()<=0) {
                    hits = SearchAction.directSearch(query, sc.irs, sc.dc, hits, errors, request);
                    searchTime = System.currentTimeMillis() - start;
                    docs = SearchAction.collectHits(sc.dc, hits, r.getResultPerPage(), r.getStart(), total);
                }else {
                    //to switch to searchSortedDocuments, comment out hits section
                    hits = SearchAction.sortBySearch(query, sc.irs, sc.dc, sortBys, request, hits, errors);
                    searchTime = System.currentTimeMillis() - start;
                    docs = SearchAction.collectHits(sc.dc, hits, r.getResultPerPage(), r.getStart(), total);
                }
                if(sc.debug){
                    logger.info("Top 3 results Explained +++++++++++++++++++++++++++++++++++ ");
                    for(int i=0;i<hits.length()&&i<3;i++){
                        logger.info("Result "+(i+1)+":"+hits.score(i));
                        logger.info(sc.irs.getSearcher().explain(query, hits.id(i)).toString());
                    }
                    logger.info("Top 3 results Explained ----------------------------------- ");
                }
            }
            if(sc.debug) logger.info("Got docs from disk: " + (System.currentTimeMillis() - _start));

            sr.init(sc, q, r.getLuceneQuery(), query, docs, null, searchTime, total[0], r.getStart(), r.getResultPerPage(), sortBys, filterResult, request, response);

            Highlighter summarizer = new Highlighter(sc.dc.getAnalyzer(), query.rewrite(sc.irs.getIndexReader()), q);
            
            summarizer.setHighlightPrefix(r.getBeginHighlightTag());
            summarizer.setHighlightSuffix(r.getEndHighlightTag());
            
            QueryLogger.log(request != null ? request.getRemoteUser() : null, r.getSourceLocation().length()==0? "API" : r.getSourceLocation(), 
                    HTMLEntities.encode(r.getQuery().length()==0 && r.getLuceneQuery().length()>=0 ? r.getLuceneQuery() : r.getQuery()),
                    U.join(r.getIndexList(), ","), "API", 
                    System.currentTimeMillis(), searchTime, System.currentTimeMillis() - _start, total[0]);
            
            SearchProtocol.SearchResponse.Builder rb = SearchProtocol.SearchResponse.newBuilder();
            rb.setSearchTime(sr.getSearchTime());
            rb.setTotal(sr.getTotal());
            for(HitDocument d : sr.getDocs()) {
                SearchProtocol.Document.Builder db = SearchProtocol.Document.newBuilder();
                db.setScore(d.getScore()).setBoost(d.getBoost());
                for(Field f : d.fields()) {
                    SearchProtocol.Field.Builder fb = SearchProtocol.Field.newBuilder();
                    fb.setName(f.name());
                    Column c = sc.dc.findColumn(f.name());
                    if(c==null)continue;
                    if(c.getIsDate()) {
                        fb.setType(SearchProtocol.Field.Type.DATETIME);
                        fb.setValue(f.stringValue());
                    }else if(c.getIsNumber()) {
                        fb.setType(SearchProtocol.Field.Type.NUMBER);
                        fb.setValue(f.stringValue());
                    }else {
                        fb.setType(SearchProtocol.Field.Type.STRING);
                        fb.setValue(f.stringValue());
                        for(SearchProtocol.StringColumnFormat scf : r.getStringColumnFormatList()) {
                            if(f.name().equals(scf.getColumn())) {
                                if (scf.getStringFormat() == SearchProtocol.StringColumnFormat.StringFormat.DIRECT) {
                                }else if(scf.getStringFormat() == SearchProtocol.StringColumnFormat.StringFormat.HTML) {
                                    fb.setValue(EscapeChars.forHTMLTag(f.stringValue()));
                                }else if(scf.getStringFormat() == SearchProtocol.StringColumnFormat.StringFormat.HIGHLIGHTED) {
                                    fb.setValue(summarizer.getHighlighted(f.stringValue(),f.name()));
                                }else if(scf.getStringFormat() == SearchProtocol.StringColumnFormat.StringFormat.HIGHLIGHTED_HTML) {
                                    fb.setValue(summarizer.getHighlighted(EscapeChars.forHTMLTag(f.stringValue()),f.name()));
                                }else if(scf.getStringFormat() == SearchProtocol.StringColumnFormat.StringFormat.SUMMARIZED) {
                                    fb.setValue(summarizer.getSummary(f.stringValue(),f.name()));
                                }else if(scf.getStringFormat() == SearchProtocol.StringColumnFormat.StringFormat.SUMMARIZED_HTML) {
                                    fb.setValue(summarizer.getSummary(EscapeChars.forHTMLTag(f.stringValue()),f.name()));
                                }
                                break;
                            }
                        }
                    }
                    db.addField(fb);
                }
                rb.addDoc(db);
            }
            for(FilterColumn fc : sr.getFilterResult().filterColumns) {
                SearchProtocol.FacetChoice.Builder fb = SearchProtocol.FacetChoice.newBuilder();
                fb.setColumn(fc.column.getColumnName());
                for(Count c : fc.getCounts()) {
                    SearchProtocol.FacetCount.Builder cb = SearchProtocol.FacetCount.newBuilder();
                    cb.setValue(c.columnValue==null? "" : c.columnValue);
                    cb.setEndValue(c.columnEndValue==null? "" : c.columnEndValue);
                    cb.setCount(c.value);
                    fb.addFacetCount(cb);
                    if(fb.getFacetCountCount()>=r.getFacetCountLimit()) {
                        break;
                    }
                }
                fb.setFacetCountTotal(fc.getCounts().size());
                fb.setMaxIntegerValue(fc.getMaxIntegerValue());
                fb.setMinIntegerValue(fc.getMinIntegerValue());
                rb.addFacetChoice(fb);
            }
            
            //write it to stream
            rb.build().writeTo(outStream);

            outStream.close();
            outStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return (mapping.findForward("error"));
        } finally {
            SearchAction.closeIndexReaderSearcher(sc.irs);
        }
        return null;
    }
}
