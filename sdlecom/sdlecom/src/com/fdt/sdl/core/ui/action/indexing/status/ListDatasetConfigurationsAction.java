package com.fdt.sdl.core.ui.action.indexing.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.core.exception.ConfigurationException;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fdt.sdl.util.SecurityUtil;

/**
 * Put a flag file to tell the indexing process to stop.
 *
 */

public final class ListDatasetConfigurationsAction extends Action {
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        List<DatasetConfiguration> dcs = new ArrayList<DatasetConfiguration>();
        List<String> errors = new ArrayList<String>();
        try {
            for(DatasetConfiguration dc : ServerConfiguration.getDatasetConfigurations()) {
                if (!SecurityUtil.isAllowed(request, dc)) {
                    errors.add("access is not allowed for "+dc.getName());
                }else {
                    dcs.add(dc);
                }
            }
            request.setAttribute("dcs", dcs);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

        return (mapping.findForward("continue"));
    }

}
