package com.fdt.sdl.core.ui.action.search;

import javax.servlet.http.HttpServletRequest;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForward;

import com.fdt.sdl.styledesigner.util.DeviceDetectorUtil;
import com.fdt.sdl.styledesigner.util.TemplateUtil;

/**
 * This is to set templateName, templateFile in the request context.
 * These two variables are used in /templates/composite/composite.vm
 */
public class CompositeSearchAction extends SearchAction {
    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.search.action.CompositeSearchAction");

    protected ActionForward getTemplate(DatasetConfiguration dc, HttpServletRequest request) {
        if(dc==null) return null;
        String m_templateFile = null;
        // Forward control to the display velocity page
        String templateName = U.getText(request.getParameter("templateName"), DeviceDetectorUtil.identifyDevice(dc, request)) ;
        String fileName = "documents.vm";

        if (!U.isEmpty(templateName)) {
            m_templateFile = TemplateUtil.getTemplateFilePath(dc.getName(), templateName, fileName);
        }
        request.setAttribute("templateName", templateName);
        request.setAttribute("templateFile", m_templateFile);
        logger.info("templateName:" + templateName + " templateFile:" + m_templateFile);
        return null;
    }
}
