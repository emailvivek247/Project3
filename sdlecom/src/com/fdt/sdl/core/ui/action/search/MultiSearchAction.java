package com.fdt.sdl.core.ui.action.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.search.HitDocument;
import net.javacoding.xsearch.search.analysis.QueryHelper;
import net.javacoding.xsearch.search.result.MultiSearchResult;
import net.javacoding.xsearch.search.result.SearchResult;
import net.javacoding.xsearch.search.result.filter.FilterResult;
import net.javacoding.xsearch.search.searcher.IndexReaderSearcher;
import net.javacoding.xsearch.search.searcher.SearcherManager;
import net.javacoding.xsearch.utility.HttpUtil;
import net.javacoding.xsearch.utility.U;
import net.javacoding.xsearch.utility.VMTool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

/**
 * Implementation of <strong>Action </strong> that performs search on multiple indexes
 * This is not heavily used, and the code are copied SearchAction.java
 * 
 * 
 */

public class MultiSearchAction extends Action {
    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.search.action.MultiSearchAction");

    /**
     * Process the search request.
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ActionMessages errors = new ActionMessages();
        HttpSession session = request.getSession();
        String[] indexNames = request.getParameterValues("indexName");
        if(indexNames!=null&&indexNames.length==1) {
            indexNames = indexNames[0].split(",");
        }
        IndexReaderSearcher irs = null;
        ActionForward af = mapping.findForward("continue");
        try {
            long _start = System.currentTimeMillis();
            String q = U.getText(request.getParameter("q"), "");
            String lq = U.getText(request.getParameter("lq"), "");

            int offset = U.getInt(request.getParameter("start"), 0);
            if (offset < 0) offset = 0;

            int rowsToReturn = U.getInt(request.getParameter("length"), 5);
            if (rowsToReturn <= 0) rowsToReturn = 5;

            MultiSearchResult msr = new MultiSearchResult();
            MultiSearchContext msc = findSearchContext(request);
            Query query = null;

            for (int i = 0; i < msc.indexNames.length; i++) {
                try {
                    irs = msc.irss.get(i);
                    if (irs == null) {
                        continue;
                    }
                    if(msc.debug) logger.info("Got searcher: " + (System.currentTimeMillis() - _start));

                    try {
                        if(msc.debug) logger.info("Loaded Analyzer: " + (System.currentTimeMillis() - _start));
                        if (!U.isEmpty(q)||!U.isEmpty(lq)) {
                            query = QueryHelper.getSearchQuery(q, lq, null, request, msc.dcs.get(i), irs, SearchAction.getBooleanOperator(request), request.getParameter("searchable"), U.getInt(request.getParameter("randomQuerySeed"),0), false);
                        }else {
                            query = new MatchAllDocsQuery();
                        }
                    } catch (Exception ex) {
                        if(msc.debug) logger.info("Exception Occurred", ex);
                        logger.error("Cannot parse query: " + q);
                    }
                    if(msc.debug) logger.info("Parsed Query: " + (System.currentTimeMillis() - _start));
    
                    List<HitDocument> docs = null;
                    int[] total = new int[1];
                    if(msc.debug) logger.info("Start Searching: " + (System.currentTimeMillis() - _start));
                    if (query != null) {
                        Hits hits = null;
                        try {
                            hits = SearchAction.directSearch(query, irs, msc.dcs.get(i), hits, errors, request);
                            docs = SearchAction.collectHits(msc.dcs.get(i), hits, rowsToReturn, offset, total);
                        } catch (RuntimeException re) {
                            errors.add("error", new ActionMessage("action.search.runtime.error", re.getMessage()));
                        }
                    }
                    long searchTime = (System.currentTimeMillis() - _start);
                        
                    if(msc.debug) logger.info("Got docs from disk: " + searchTime);
    
                    SearchResult sr = new SearchResult(msc, i, q, lq, query, docs, searchTime, total[0], offset, rowsToReturn, null, new FilterResult(), request, response);
                    
                    msr.add(sr);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }finally {
                    if(irs!=null) {
                        SearcherManager.close(msc.dcs.get(i).getName(), irs);
                    }
                }
            }
            
            msr.setSearchTimeString(VMTool.timeFormat.get().format((System.currentTimeMillis() - _start) * .001));
            msr.setURLEncodedQuery(java.net.URLEncoder.encode(q, "utf-8"));
            msr.setQueryString(request.getQueryString());
            msr.setNoStartQueryString(HttpUtil.addOrSetQuery(request.getQueryString(), "start", ""));
            msr.setEncoding(WebserverStatic.getEncoding());

            request.setAttribute("dcs", msc.dcs);
            request.setAttribute("multiSearchResult", msr);
            try {
                request.setAttribute("summarizer", new net.javacoding.xsearch.search.Highlighter(msc.dcs.get(0).getAnalyzer(), query, q));
            } catch (Exception e) {}

        } catch (NullPointerException se) {
            errors.add("error", new ActionMessage("action.showIndexStatus.index.error", indexNames));
            se.printStackTrace();
            return (mapping.findForward("error"));
        } finally {
            request.setAttribute("layout", "Empty.vm");
            saveErrors(request, errors);
        }
        return af;

    }
    public static class MultiSearchContext{
        public boolean debug;

        public String[] indexNames;
        public List<DatasetConfiguration> dcs;
        public List<IndexReaderSearcher> irss;

        public MultiSearchContext() {
            dcs = new ArrayList<DatasetConfiguration>();
            irss = new ArrayList<IndexReaderSearcher>();
        }
    }
    public static MultiSearchContext findSearchContext(HttpServletRequest request) {
        MultiSearchContext sc = new MultiSearchContext();
        sc.debug = U.getBoolean(request.getParameter("debug"),"y",false);

        if(request.getParameter("indexName")!=null){
            sc.indexNames = request.getParameter("indexName").split(",");
        }else {
            sc.indexNames = new String[0];
        }
        for(String indexName : sc.indexNames) {
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
            sc.dcs.add(dc);
            sc.irss.add(dc==null? null : SearcherManager.getIndexReaderSearcher(dc));
        }
        return sc;
    }
}
