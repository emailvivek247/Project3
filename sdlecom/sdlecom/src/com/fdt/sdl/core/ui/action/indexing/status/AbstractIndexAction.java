package com.fdt.sdl.core.ui.action.indexing.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public abstract class AbstractIndexAction extends Action {

    abstract void doExecute(ActionMapping mapping, HttpServletRequest request, DatasetConfiguration dc, List<String> errors);

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ArrayList<String> errors = new ArrayList<String>();
        String indexName = request.getParameter("indexName");
        try {
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);

            if (dc==null) {
                errors.add("No dataset is found for:"+indexName);
                return (mapping.findForward("continue"));
            }

            request.setAttribute("dc", dc);

            doExecute(mapping, request, dc, errors);

        } catch (Exception ex) {
            errors.add("Exception:" + ex);
            return (mapping.findForward("continue"));
        } finally {
            request.setAttribute("errs", errors);
        }

        // Forward control to the display velocity page
        return (mapping.findForward("continue"));

    }
}
