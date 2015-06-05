package com.fdt.sdl.admin.ui.action.messagecenter;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.utility.FileUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fdt.sdl.styledesigner.util.PageStyleUtil;

public class AdminMessage extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		try {
			File msgCenterPropertiesFile = FileUtil.resolveFile(WebserverStatic.getRootDirectoryFile(), "WEB-INF", 
					"data", "properties", "MessageCenter.properties");
			String fileName = msgCenterPropertiesFile.getAbsolutePath();
			String lastUpdatedDateTime = null, htmlMessageContent = null;
			MessageForm aForm = (MessageForm) form;
			lastUpdatedDateTime = aForm.getLastUpdatedDateTime();
			htmlMessageContent = aForm.getHtmlMessageContent();
			PageStyleUtil.writeProperty("messageContent", htmlMessageContent,fileName);
			//if(StringUtils.isBlank(lastUpdatedDateTime)) {
				Date date = new Date();
				lastUpdatedDateTime = PageStyleUtil.format(date.toString(),
										"EEE MMM dd HH:mm:ss zzz yyyy",
										"MM/dd/yyyy");
			//}
			PageStyleUtil.writeProperty("lastUpdatedDateTime", lastUpdatedDateTime, fileName);	
			request.setAttribute("successMessage", "Published Message Successfully");
			return (mapping.findForward("continue"));
		} catch (Exception exception) {
			request.setAttribute("errorMessage", exception.getMessage());
			return (mapping.findForward("continueError"));
		}		
		
	}

}
