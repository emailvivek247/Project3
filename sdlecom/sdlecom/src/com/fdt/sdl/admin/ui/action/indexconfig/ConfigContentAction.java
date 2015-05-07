package com.fdt.sdl.admin.ui.action.indexconfig;

import javax.servlet.http.HttpServletRequest;

import net.javacoding.xsearch.config.ContentDataquery;
import net.javacoding.xsearch.config.Dataquery;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.foundation.UserPreference;

public class ConfigContentAction extends ConfigDataqueryAction {

    protected Dataquery getDataquery(DatasetConfiguration dc, int id, HttpServletRequest request) {
        ContentDataquery[] queries = dc.getContentDataqueryArray();
        ContentDataquery query;
        if (queries == null || queries.length < 1 || id < 1 || id > queries.length) {
            // Create a new content query if there's no query, or id is out of range
            query = new ContentDataquery();
            dc.addDataquery(query);
            // Reset id to current max id + 1
            request.setAttribute("id", queries != null ? String.valueOf(queries.length+1) : "1");
            request.setAttribute("pageMode", "create");
        } else {
            query = queries[id-1];
            request.setAttribute("pageMode", "edit");
        }
        return query;
    }

    protected void saveProgress(String name, boolean val) {
        UserPreference.setBoolean("configContent."+name, val);
    }
}
