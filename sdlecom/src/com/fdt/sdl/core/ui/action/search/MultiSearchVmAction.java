package com.fdt.sdl.core.ui.action.search;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.core.exception.ConfigurationException;
import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.search.HTMLEntities;
import net.javacoding.xsearch.search.HitDocument;
import net.javacoding.xsearch.search.analysis.QueryHelper;
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

import com.fdt.sdl.styledesigner.util.DeviceDetectorUtil;

/**
 * Implementation of <strong>Action </strong> that performs search on multiple indexes
 * This is not heavily used, and the code are copied SearchAction.java
 * 
 */

public class MultiSearchVmAction extends Action {
	
    private static Logger logger = LoggerFactory.getLogger(MultiSearchVmAction.class);

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
        DatasetConfiguration[] dcs = null;
        ActionForward af = mapping.findForward("continue");
        try {
            long _start = System.currentTimeMillis();
            // get searcher
            if (indexNames != null) {
                // logger.debug("search index: " + indexName);
            }
            dcs = ServerConfiguration.getDatasetConfigurations(indexNames);
            if (dcs == null) return af;
            // logger.debug("Got config: " + (System.currentTimeMillis() - _start));
            String q = U.getText(request.getParameter("q"), "");

            request.setAttribute("isMultiSearch", Boolean.valueOf(true));
            request.setAttribute("numIndex", new Integer(dcs.length));
            request.setAttribute("q", q);

            int offset = U.getInt(request.getParameter("start"), 0);
            if (offset < 0) offset = 0;

            int rowsToReturn = U.getInt(request.getParameter("length"), 5);
            if (rowsToReturn <= 0) rowsToReturn = 5;

            Query query = null;
            for (int i = 0; i < dcs.length; i++) {
                try {
                    irs = SearcherManager.getIndexReaderSearcher(dcs[i]);
                    if (irs == null) {
                        continue;
                    }
                    logger.debug("Got searcher: " + (System.currentTimeMillis() - _start));

                    try {
                        logger.debug("Loaded Analyzer: " + (System.currentTimeMillis() - _start));
                        if (!U.isEmpty(q)) {
                            query = QueryHelper.getSearchQuery(q, request.getParameter("lq"), null, request, dcs[i], irs, SearchAction.getBooleanOperator(request), request.getParameter("searchable"), U.getInt(request.getParameter("randomQuerySeed"),0), false);
                        }else {
                            query = new MatchAllDocsQuery();
                        }
                        //not applicable, request.setAttribute("filteredColumns", filteredColumns);
                    } catch (Exception ex) {
                        logger.debug("Exception Occurred", ex);
                        logger.error("Cannot parse query: " + q);
                    }
                    logger.debug("Parsed Query: " + (System.currentTimeMillis() - _start));
    
                    List<HitDocument> retValue = null;
                    int[] total = new int[1];
                    logger.debug("Start Searching: " + (System.currentTimeMillis() - _start));
                    if (query != null) {
                        Hits hits = null;
                        try {
                            hits = SearchAction.directSearch(query, irs, dcs[i], hits, errors, request);
                            retValue = SearchAction.collectHits(dcs[i], hits, rowsToReturn, offset, total);
                        } catch (RuntimeException re) {
                            errors.add("error", new ActionMessage("action.search.runtime.error", re.getMessage()));
                        }
                    }
                    logger.debug("Got docs from disk: " + (System.currentTimeMillis() - _start));
    
                    setNAttribute(request, i, "indexDisplayName", dcs[i].getDisplayName());
                    setNAttribute(request, i, "indexName", dcs[i].getName());
                    setNAttribute(request, i, "templateName", DeviceDetectorUtil.identifyDevice(dcs[i], request));
                    setNAttribute(request, i, "docs", retValue);
                    setNAttribute(request, i, "total", new Integer(total[0]));
                    // request.setAttribute("summarizer", new net.javacoding.xsearch.search.Summarizer()); //show summary
                    setNAttribute(request, i, "summarizer", new net.javacoding.xsearch.search.Highlighter(dcs[i].getAnalyzer(), query, q)); // show
                    setNAttribute(request, i, "dc", dcs[i]);
                    // summary
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }finally {
                    if(irs!=null) {
                        SearcherManager.close(dcs[i].getName(), irs);
                    }
                }
            }
            request.setAttribute("searchTime", VMTool.timeFormat.get().format((System.currentTimeMillis() - _start) * .001));
            request.setAttribute("URLEncodedQuery", java.net.URLEncoder.encode(q, "utf-8")); // encode query in
            request.setAttribute("HTMLEncodedQuery", HTMLEntities.encode(q)); // encode query in html format
            request.setAttribute("request", request);
            request.setAttribute("response", response);
            request.setAttribute("queryString", request.getQueryString());
            request.setAttribute("noStartQueryString", HttpUtil.addOrSetQuery(request.getQueryString(), "start", ""));
            request.setAttribute("encoding", WebserverStatic.getEncoding());
            request.setAttribute("dcs", dcs);

            logger.debug("Set velocity variables: " + (System.currentTimeMillis() - _start));

            // logger.info("Found " + total[0] + " MATCHING with \"" + q + "\" in " + duration + " milliseconds");

            // searcher.close();

        } catch (NullPointerException se) {
            errors.add("error", new ActionMessage("action.showIndexStatus.index.error", indexNames));
            se.printStackTrace();
            return (mapping.findForward("error"));
        } catch (ConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            request.setAttribute("layout", "Empty.vm");
            saveErrors(request, errors);
        }
        return af;

    }

    protected void setNAttribute(HttpServletRequest request, int i, String name, Object value) {
        if(name!=null&&value!=null) {
            request.setAttribute((i+1)+name, value);
        }
    }
    protected void removeNAttribute(HttpServletRequest request, int i, String name) {
        if(name!=null) {
            request.removeAttribute((i+1)+name);
        }
    }
    protected void removeNAttribute(HttpSession session, int i, String name) {
        if(name!=null) {
            session.removeAttribute((i+1)+name);
        }
    }
}
