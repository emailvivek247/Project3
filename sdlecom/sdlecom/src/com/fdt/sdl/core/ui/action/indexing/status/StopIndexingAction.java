package com.fdt.sdl.core.ui.action.indexing.status;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.utility.SchedulerTool;

import org.apache.struts.action.ActionMapping;

/**
 * Put a flag file to tell the indexing process to stop.
 *
 */

public final class StopIndexingAction extends AbstractSecuredIndexAction {
    @Override
    void doExecute(ActionMapping mapping, HttpServletRequest request, DatasetConfiguration dc, List<String> errors) {
        SchedulerTool.stopIndexingJob(dc);
    }
}
