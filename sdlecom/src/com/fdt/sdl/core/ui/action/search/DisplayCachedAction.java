package com.fdt.sdl.core.ui.action.search;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.search.SearchQueryParser;
import net.javacoding.xsearch.search.searcher.IndexReaderSearcher;
import net.javacoding.xsearch.search.searcher.SearcherManager;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.fdt.sdl.core.ui.action.search.SearchAction.SearchContext;
import com.fdt.sdl.styledesigner.util.TemplateUtil;


/**
 * 
 *
 * @
 */
public class DisplayCachedAction extends Action {
	
    private static Logger logger = LoggerFactory.getLogger(DisplayCachedAction.class);

    /**
     * Process the search request.
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ActionMessages errors = new ActionMessages();
        HttpSession session = request.getSession();
        String indexName = request.getParameter("indexName");
        IndexReaderSearcher irs = null;
        DatasetConfiguration dc = null;
        SearchContext sc = null; 
       try {
            long _start = System.currentTimeMillis();
            if(indexName!=null){
                logger.debug("search index: " + indexName);
            }
            dc = ServerConfiguration.getDatasetConfiguration(indexName);
            sc = SearchAction.findSearchContext(mapping, request);
            setTemplate(request, sc);
            if(dc==null) return sc.af;
            logger.debug("Got config: " + (System.currentTimeMillis() - _start));
            irs = SearcherManager.getIndexReaderSearcher(dc);
            if (irs == null) {
                logger.warn("Can not get searcher ");
                errors.add("error", new ActionMessage("action.search.index.error", indexName));
                return (sc.af);
            }
            logger.debug("Got searcher: " + (System.currentTimeMillis() - _start));
    
            // parse the query
            String q = request.getParameter("q");
            if (U.isEmpty(q)) {
                q = "";
            }
            Query query = null;
            try {
                query = SearchQueryParser.parse(dc, q);
                request.setAttribute("summarizer", new net.javacoding.xsearch.search.Highlighter(dc.getAnalyzer(), query, q)); // show
            } catch (Exception ex) {
                logger.debug("Exception Occurred", ex);
                logger.error("Cannot parse query: " + q);
            }
    
            if (query != null) {
                Hits hits = null;
                try{
                    hits = irs.getSearcher().search(query);
                }catch(RuntimeException re){
                    errors.add("error", new ActionMessage("action.search.runtime.error", re.getMessage()));
                }
                if (hits != null && hits.length()>0) {
                    Document doc = hits.doc(0);
                    request.setAttribute("doc", doc);
                }
            }
    
            request.setAttribute("dc", dc);
            request.setAttribute("indexName", indexName);
            request.setAttribute("q", q);
            request.setAttribute("request", request);
            request.setAttribute("response", response);
            request.setAttribute("encoding", WebserverStatic.getEncoding());
    
            return sc.af;
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
    protected void setTemplate(HttpServletRequest request, SearchContext sc) {
        String m_templateFile = null;
        String fileName = TemplateUtil.CACHE_TEMPLATE_FILE;

        if (!U.isEmpty(sc.actualIndexName)) {
            m_templateFile = TemplateUtil.getTemplateFilePath(sc.actualIndexName, sc.actualTemplateName, fileName);
        }
        logger.info("templateName:" + sc.templateName + " templateFile:" + m_templateFile);
        sc.af = new ActionForward(m_templateFile);
    }

}
