package com.fdt.sdl.admin.ui.action.license;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.utility.FileUtil;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

public class ImportLicenseAction extends Action {

	public ActionForward execute(ActionMapping actionMapping,
			ActionForm paramActionForm, HttpServletRequest request,
			HttpServletResponse response) {
		
		ActionMessages actionMessages = new ActionMessages();
		try {
			String operation = request.getParameter("operation");
			if (operation == null) {
				return actionMapping.findForward("view");
			}
			File licenseFile = FileUtil.resolveFile(WebserverStatic.getRootDirectoryFile(), new String[] { "WEB-INF", "data", "license.xml" });
			String licenseFileContents = request.getParameter("license");
			this.writeFile(licenseFile, licenseFileContents);
			return actionMapping.findForward("sucess");
		} catch (IOException ioException) {
			actionMessages.add("error", new ActionMessage("configuration.changes.error", ioException));
			this.saveErrors(request, actionMessages);
		}
		return actionMapping.findForward("continue");
	}

	public void writeFile(File paramFile, String fileContents)
			throws IOException {
		BufferedWriter localBufferedWriter = null;
		try {
			localBufferedWriter = new BufferedWriter(new FileWriter(paramFile));
			localBufferedWriter.write(fileContents);
			localBufferedWriter.flush();
		} finally {
			if (localBufferedWriter != null)
				localBufferedWriter.close();
		}
	}

}
