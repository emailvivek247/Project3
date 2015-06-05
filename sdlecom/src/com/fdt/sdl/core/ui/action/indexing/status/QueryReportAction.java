package com.fdt.sdl.core.ui.action.indexing.status;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.IndexManager;
import net.javacoding.xsearch.status.QueryLogAnalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fdt.sdl.util.SecurityUtil;

/**
 *  Login action
 */
public class QueryReportAction extends Action {
    private static Logger logger = LoggerFactory.getLogger(QueryReportAction.class.getName());

    /**
     * Process the search request.
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request))return (mapping.findForward("welcome"));

        //get server info:
        HashMap<String, Comparable> systemInfo = (HashMap<String, Comparable>)request.getSession().getAttribute("systemInfo");
        if (systemInfo == null) {
            systemInfo = new HashMap<String, Comparable>();
            systemInfo.put("numberOfProcessor", new Integer(Runtime.getRuntime().availableProcessors()));
            systemInfo.put("OSName", System.getProperty("os.name"));
            systemInfo.put("OSVersion", System.getProperty("os.version"));
            systemInfo.put("OSArchitecture", System.getProperty("os.arch"));
            systemInfo.put("JVMVersion", System.getProperty("java.runtime.version"));
            systemInfo.put("JVMVender", System.getProperty("java.vm.vendor"));
            systemInfo.put("freeMemory",formatSize (new Long(Runtime.getRuntime().freeMemory()), true));
            systemInfo.put("totalMemory",formatSize(new Long(Runtime.getRuntime().totalMemory()), true));
            systemInfo.put("maxMemory",formatSize(new Long(Runtime.getRuntime().maxMemory()), true));
            
            request.getSession().setAttribute("systemInfo", systemInfo);
        }
        
        setProduct(request);
        
        //QueryLogger.logTest();
        //save report into session "queryReport" attribute
        //String dataSet = request.getParameter("indexName");
        //QueryLogAnalyzer.getReport(dataSet,0).save(request);

        String action = request.getParameter("operation");
        if ("refresh".equals(action)){
            QueryLogAnalyzer.refresh();
        }
        return (mapping.findForward("continue"));

    }
    public static void setProduct(HttpServletRequest request) {
        request.setAttribute("product", IndexManager.loadProductProperties());
    }
    public static String formatSize(Object obj, boolean mb) {

        long bytes = -1L;

        if (obj instanceof Long) {
            bytes = ((Long) obj).longValue();
        } else if (obj instanceof Integer) {
            bytes = ((Integer) obj).intValue();
        }

        if (mb) {
            long mbytes = bytes / (1024 * 1024);
            long rest = 
                ((bytes - (mbytes * (1024 * 1024))) * 100) / (1024 * 1024);
            return (mbytes + "." + ((rest < 10) ? "0" : "") + rest + " MB");
        } else {
            return ((bytes / 1024) + " KB");
        }
    }
    
}
