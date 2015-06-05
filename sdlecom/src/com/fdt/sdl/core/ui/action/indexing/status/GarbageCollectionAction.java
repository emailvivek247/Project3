package com.fdt.sdl.core.ui.action.indexing.status;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.core.exception.ConfigurationException;

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
 */

public final class GarbageCollectionAction extends Action
{
    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.status.action.GarbageCollectionAction");

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                                 throws IOException, ServletException {
        ArrayList<String> errors = new ArrayList<String>();
        try {
            logger.info("Garbage Collection request from "+request.getRemoteAddr());

            if(!isAllowed(request)){
                errors.add("Not Allowed!");
                return (mapping.findForward("continue"));
            }
            
            /* Commented By Vivek.
             *  System.gc();
             */
            
        } catch (Exception ex) {
            errors.add("Exception:"+ex);
            return (mapping.findForward("continue"));
        }finally{
            request.setAttribute("errs", errors);
            logger.info(request.getRemoteAddr()+" "+(errors.isEmpty()?"OK":errors.get(0))+" "+request.getQueryString());
        }

        // Forward control to the display velocity page
        return (mapping.findForward("continue"));

    }
    
    private boolean isAllowed(HttpServletRequest request) throws ConfigurationException {
        ArrayList<DatasetConfiguration> dcs = ServerConfiguration.getDatasetConfigurations();
        for (int i = 0; i<dcs.size(); i++) {
            if(SecurityUtil.isAllowed(request, (DatasetConfiguration) dcs.get(i))){
                return true;
            }
        }
        return false;
    }

}
