package com.fdt.sdl.core.ui.action.search;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.search.memory.BufferIndex;
import net.javacoding.xsearch.search.memory.BufferIndexManager;
import com.fdt.sdl.core.ui.action.indexing.memory.SubmitAction;
import com.fdt.sdl.util.SecurityUtil;

import net.javacoding.xsearch.search.searcher.IndexReaderSearcher;
import net.javacoding.xsearch.search.searcher.SearcherManager;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.search.Query;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

/**
 * Process deletion by query
 * Similar to SQL
 *   delete * from some_index where q = ...
 */

public class DeleteAction extends Action {
    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.search.action.DeleteAction");

    /**
     * Process the search request.
     * If no "commit" parameter, then do the deletion, and do the commit
     * if "commit=y", do the commit, and return, no deletion is done
     * If "commit=n", do the deletion, but not the commit
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ActionMessages errors = new ActionMessages();
        String indexName = request.getParameter("indexName");
        IndexReaderSearcher irs = null;
        DatasetConfiguration dc = null;
        try {
            // get searcher
            if (indexName != null) {
                // logger.debug("search index: " + indexName);
            }
            dc = ServerConfiguration.getDatasetConfiguration(indexName);
            ActionForward af = mapping.findForward("continue");
            if (dc == null) return af;
            if (!SecurityUtil.isAllowed(request, dc)) {
                errors.add("error", new ActionMessage("action.operation.security.error", "delete"));
                return af;
            }
            // logger.debug("Got config: " + (System.currentTimeMillis() - _start));
            // Searcher searcher = getSearcher(dc,sc);
            irs = SearcherManager.getIndexReaderSearcher(dc);
            if (irs == null) {
                logger.warn("Can not get searcher ");
                errors.add("error", new ActionMessage("action.search.index.error", indexName));
                return (af);
            }
            
            if ("y".equals(request.getParameter("commit"))){
                irs.getIndexReader().commit();
                logger.debug("Deletion committed.");
                return (af);
            }

            // parse the query
            String q = request.getParameter("q");
            if (q==null) {
                q = "";
            }
            String lq = request.getParameter("lq");
            Query query = SearchAction.getSearchQuery(q, lq, null, request, dc, irs, SearchAction.getBooleanOperator(request), request.getParameter("searchable"), U.getInt(request.getParameter("randomQuerySeed"),0), false);

            // logger.debug("Start Searching: " + (System.currentTimeMillis() - _start));
            if (query != null) {
                SubmitAction.deleteExisting(irs.getIndexReader(), query, !"n".equals(request.getParameter("commit")));
                BufferIndex bi = BufferIndexManager.getIndex(dc.getName(),false);
                if(bi!=null) {
                    SubmitAction.deleteExisting(bi.getReader(), query, true);
                }
                request.setAttribute("ret", new Integer(1));
            }

            return af;
        } catch (NullPointerException se) {
            errors.add("error", new ActionMessage("action.showIndexStatus.index.error", indexName));
            return (mapping.findForward("error"));
        } finally {
            request.setAttribute("layout", "Empty.vm");
            if (dc != null) {
                SearcherManager.close(dc.getName(), irs);
            }
            saveErrors(request, errors);
        }

    }

}
