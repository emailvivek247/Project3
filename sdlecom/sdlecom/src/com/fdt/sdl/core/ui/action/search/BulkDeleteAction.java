package com.fdt.sdl.core.ui.action.search;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.search.searcher.IndexReaderSearcher;
import net.javacoding.xsearch.search.searcher.SearcherManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.Term;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.fdt.sdl.util.SecurityUtil;

/**
 * Delete where _hid = ...
 * 
 * This is created for a customer, but not really useful in the end.
 */

public class BulkDeleteAction extends Action {
    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.search.action.BulkDeleteAction");

    /**
     * Process the search request.
     * do the deletion, and do the commit
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

            // process the primary key column
            Column primaryKeyColumn = dc.getPrimaryKeyColumn();
            String[] toBeDeleted = null;
            if(primaryKeyColumn!=null) {
                toBeDeleted = request.getParameterValues(primaryKeyColumn.getColumnName());
            }
            if (toBeDeleted!=null) {
                try {
                    for(int i=0;i<toBeDeleted.length;i++) {
                        Term pkTerm = new Term(primaryKeyColumn.getColumnName(), toBeDeleted[i]);
                        irs.getIndexReader().deleteDocuments(pkTerm);
                    }
                } catch (IOException ioe) {
                    logger.warn("When bulk deleting: " + ioe);
                    ioe.printStackTrace();
                } finally {}
                return af;
            }
            
            // process "_hid"
            toBeDeleted = request.getParameterValues("_hid");
            if (toBeDeleted!=null) {
                try {
                    for(int i=0;i<toBeDeleted.length;i++) {
                        irs.getIndexReader().deleteDocument(Integer.parseInt(toBeDeleted[i]));
                    }
                    irs.getIndexReader().flush();
                } catch (IOException ioe) {
                    logger.warn("When bulk deleting: " + ioe);
                    ioe.printStackTrace();
                } finally {}
                return (af);
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
