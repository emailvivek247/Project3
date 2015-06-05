package com.fdt.sdl.core.ui.action.indexing.memory;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.search.memory.BufferIndexManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class ClearBufferAction extends Action {
    private static Logger logger = LoggerFactory.getLogger(ClearBufferAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ArrayList<String> errors = new ArrayList<String>();
        String indexName = request.getParameter("indexName");
        try {
            ActionForward af = mapping.findForward("continue");

            BufferIndexManager.clearIndex(indexName);

            return af;
        } catch (NullPointerException se) {
            errors.add("Error when trying to access index "+indexName);
            return (mapping.findForward("continue"));
        } finally {
            request.setAttribute("errs", errors);
        }

    }

}
