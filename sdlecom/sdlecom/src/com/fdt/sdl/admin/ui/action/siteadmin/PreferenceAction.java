package com.fdt.sdl.admin.ui.action.siteadmin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.LoggingConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.foundation.WebserverStatic;
import net.javacoding.xsearch.utility.U;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.sdl.util.SecurityUtil;

public class PreferenceAction extends Action {
	
    private static Logger logger = LoggerFactory.getLogger(PreferenceAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request)) return (mapping.findForward("welcome"));

        WebserverStatic.setURIFile(request);
        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
        String operation = request.getParameter("operation");
        if("resetURL".equals(operation)) {
            WebserverStatic.resetURIFile(request);
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("configuration.save.success"));
        }else if("save".equals(operation)) {
            ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
            sc.setIsMergingOldDatasetValues(U.getBoolean(request.getParameter("isMergingOldDatasetValues"), "on", false));
            sc.setSearchLogSizeInMB(U.getInt(request.getParameter("searchLogSizeInMB"),1));
            sc.setIsShortIndexingLogEnabled(U.getBoolean(request.getParameter("isShortIndexingLogEnabled"), "on", false));
            sc.setIndexingLogSizeInMB(U.getInt(request.getParameter("indexingLogSizeInMB"),3));
           	sc.setIndexRootDirectory(StringUtils.isEmpty(request.getParameter("indexRootDirectory")) ? null 
           		: request.getParameter("indexRootDirectory"));
            LoggingConfiguration l = new LoggingConfiguration();
            l.setIsEnabled(U.getBoolean(request.getParameter("emailLogging_isEnabled"), "on", false));
            l.setFromAddress(request.getParameter("emailLogging_fromAddress"));
            l.setToAddress(request.getParameter("emailLogging_toAddress"));
            l.setSmtpHost(U.getText(request.getParameter("emailLogging_smtpHost"),"localhost"));
            l.setThreshold(U.getText(request.getParameter("emailLogging_threshold"), "OFF"));
            sc.setLoggingConfiguration(l);
            sc.save();
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("configuration.save.success"));
        }
        saveErrors(request, errors);
        saveMessages(request, messages);

        return (mapping.findForward("continue"));
    }
}
