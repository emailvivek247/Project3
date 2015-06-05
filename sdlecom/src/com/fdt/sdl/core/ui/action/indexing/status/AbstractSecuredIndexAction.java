package com.fdt.sdl.core.ui.action.indexing.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fdt.sdl.util.SecurityUtil;

/**
 * Do security checking based on 1. ip addresses that can operate on this
 * instance 2. already logged in users 3. requests from localhost
 * 
 * Some duplication with AbstractIndexAction
 */

public abstract class AbstractSecuredIndexAction extends Action {

    abstract void doExecute(ActionMapping mapping, HttpServletRequest request, DatasetConfiguration dc, List<String> errors);

    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.status.action.AbstractSecuredIndexAction");

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ArrayList<String> errors = new ArrayList<String>();
        String indexName = request.getParameter("indexName");
        try {
            logger.info("Security request from " + request.getRemoteAddr());
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);

            if (dc==null) {
                errors.add("No dataset is found for:"+indexName);
                return (mapping.findForward("continue"));
            }

            request.setAttribute("dc", dc);

            if (!SecurityUtil.isAllowed(request, dc)) {
                errors.add("access is not allowed!");
                return (mapping.findForward("continue"));
            }
            
            doExecute(mapping, request, dc, errors);

        } catch (Exception ex) {
            errors.add("Exception:" + ex);
            return (mapping.findForward("continue"));
        } finally {
            request.setAttribute("errs", errors);
            logger.info(request.getRemoteAddr() + " " + (errors.isEmpty() ? "OK" : errors.get(0)) + " " + request.getQueryString());
        }

        // Forward control to the display velocity page
        return (mapping.findForward("continue"));

    }
}
