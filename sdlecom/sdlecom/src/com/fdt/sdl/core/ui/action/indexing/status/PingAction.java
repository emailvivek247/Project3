package com.fdt.sdl.core.ui.action.indexing.status;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class PingAction extends Action {
	public static String key = "empty";
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    	if("check".equals(request.getParameter("key"))){
    		request.setAttribute("key", PingAction.key);
            return mapping.findForward("key");
    	}
        return mapping.findForward("continue");
    }
}
