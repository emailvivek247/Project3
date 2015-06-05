package com.fdt.sdl.core.ui.action.indexing.status;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.search.memory.BufferIndexManager;
import net.javacoding.xsearch.search.searcher.SearcherManager;
import net.javacoding.xsearch.search.searcher.SearcherProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fdt.sdl.util.SecurityUtil;

/**
 * Implementation of <strong>Action</strong> that refresh index readers.
 *
 * Just re-read the actual index file again, open readers, warm up, etc
 * 
 * Note: use UpdateIndexAction to "update" the index with new content
 */

public final class RefreshIndexAction extends Action
{
  	private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.status.action.RefreshIndexAction");

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                                 throws IOException, ServletException
    {
        ArrayList<String> errors = new ArrayList<String>();
        //HttpSession session = request.getSession();
        String indexName = request.getParameter("indexName");
        try {
            logger.info("Searchers refresh request from "+request.getRemoteAddr());
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
            request.setAttribute("dc", dc);

            if(!("127.0.0.1".equals(request.getRemoteAddr())||SecurityUtil.isAllowed(request, dc))) {
                errors.add("access is not allowed!");
                return (mapping.findForward("continue"));
            }

            SearcherProvider sp = null;
            try{
                BufferIndexManager.pause(dc);
                BufferIndexManager.clearIndex(dc);
                sp = SearcherManager.createSearcherProviderByDataset(dc);
                SearcherManager.switchSearchProvider(dc.getName(), sp);
                if(sp==null) {
                    errors.add("Failed to create searcher pool.");
                }
                BufferIndexManager.unPause(dc);
            }catch(Exception e){
                logger.warn("Exception:",e);
                errors.add("Exception:"+e);
            }

        } catch (Exception ex) {
            logger.warn("Exception:",ex);
            errors.add("Exception:"+ex);
            return (mapping.findForward("continue"));
        }finally{
            request.setAttribute("errs", errors);
            logger.info(request.getRemoteAddr()+" "+(errors.isEmpty()?"OK":errors.get(0))+" "+request.getQueryString());
        }

        // Forward control to the display velocity page
        return (mapping.findForward("continue"));

    }

}
