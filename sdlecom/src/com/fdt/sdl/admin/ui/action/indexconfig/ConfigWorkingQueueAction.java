package com.fdt.sdl.admin.ui.action.indexconfig;

import javax.servlet.http.HttpServletRequest;

import net.javacoding.xsearch.config.Dataquery;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.WorkingQueueDataquery;
import net.javacoding.xsearch.foundation.UserPreference;

public class ConfigWorkingQueueAction extends ConfigDataqueryAction {

    protected Dataquery getDataquery(DatasetConfiguration dc, int id, HttpServletRequest request) {
        WorkingQueueDataquery query = dc.getWorkingQueueDataquery();
        if (query == null) {
            query = new WorkingQueueDataquery();
            dc.addDataquery(query);
        }
        return query;
    }

    protected void saveProgress(String name, boolean val) {
        UserPreference.setBoolean("configWorkingQueue."+name, val);
    }
}
