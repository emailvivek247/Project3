package com.fdt.sdl.admin.ui.action.license;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

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

import com.fdt.sdl.license.EncrypterDecrypter;
import com.fdt.sdl.license.FDTLicense;

public class ViewLicenseAction extends Action {

	public ActionForward execute(ActionMapping actionMapping,
			ActionForm paramActionForm, HttpServletRequest request,
			HttpServletResponse response) {
		FDTLicense fdtLicense = new FDTLicense();
		ActionMessages actionMessages = new ActionMessages();
		try {
			File licenseFile = FileUtil.resolveFile(WebserverStatic
					.getRootDirectoryFile(), new String[] { "WEB-INF", "data",
					"license.xml" });
			String fileContents = this.getContents(licenseFile);
			if (fileContents != null && !fileContents.isEmpty()) {
				fdtLicense = FDTLicense.fromXML(EncrypterDecrypter
						.decryptXML(fileContents));
			}
		} catch (Exception exception) {
			actionMessages.add("error", new ActionMessage("license.error",
					exception));
			this.saveMessages(request, actionMessages);
		}
		request.setAttribute("fdtlicense", fdtLicense);
		return actionMapping.findForward("sucess");
	}

	private String getContents(File aFile) throws Exception {
		StringBuilder contents = new StringBuilder();
		BufferedReader input = new BufferedReader(new FileReader(aFile));
		try {
			String line = null;
			while ((line = input.readLine()) != null) {
				contents.append(line);
				contents.append(System.getProperty("line.separator"));
			}
		} finally {
			input.close();
		}
		return contents.toString();
	}

}
