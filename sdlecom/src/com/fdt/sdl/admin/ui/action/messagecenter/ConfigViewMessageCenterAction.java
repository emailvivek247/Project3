package com.fdt.sdl.admin.ui.action.messagecenter;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.utility.FileUtil;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fdt.sdl.styledesigner.util.PageStyleUtil;

public class ConfigViewMessageCenterAction extends Action {
	
	 public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, 
			 HttpServletResponse response) throws IOException, ServletException {
		File msgCenterPropertiesFile = FileUtil.resolveFile(WebserverStatic.getRootDirectoryFile(), "WEB-INF", "data", "properties", "MessageCenter.properties");
		String fileName = msgCenterPropertiesFile.getAbsolutePath();		
		String lastUpdatedDateTime = null, htmlMessageContent = null;		
		htmlMessageContent = PageStyleUtil.readProperty("messageContent", fileName);
		lastUpdatedDateTime = PageStyleUtil.readProperty("lastUpdatedDateTime", fileName);
		request.setAttribute("messageContent", htmlMessageContent);
		request.setAttribute("lastUpdatedDateTime", lastUpdatedDateTime);
		return (mapping.findForward("continue"));			
	 }	
	 	
}
