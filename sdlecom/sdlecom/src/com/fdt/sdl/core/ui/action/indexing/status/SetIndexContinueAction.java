package com.fdt.sdl.core.ui.action.indexing.status;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fdt.sdl.util.SecurityUtil;

public final class SetIndexContinueAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, 
			HttpServletResponse response)
			throws IOException, ServletException {

		if (!SecurityUtil.isAdminUser(request))
			return (mapping.findForward("welcome"));

		String indexName = request.getParameter("indexName");
		HttpSession session = request.getSession();
		DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
		session.setAttribute("dc", dc);
		session.setAttribute("indexName", indexName);
		session.setAttribute("response", response);
		return mapping.findForward("continue");
	}
}
