package com.fdt.sdl.core.ui.action.indexing.status;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.search.searcher.SearcherManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionMapping;

/**
 * Close the reader, searcher.
 */

public final class ShutdownIndexAction extends AbstractSecuredIndexAction {
    
    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.status.action.ShutdownIndexAction");

    @Override
    void doExecute(ActionMapping mapping, HttpServletRequest request, DatasetConfiguration dc, List<String> errors) {
        SearcherManager.destroy(dc.getName());
        /* Commented By Vivek.
         *  System.gc();
         */;
        logger.info("Searchers cleared for "+dc.getName());
    }

}
