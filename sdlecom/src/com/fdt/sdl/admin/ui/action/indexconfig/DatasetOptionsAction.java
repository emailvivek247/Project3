package com.fdt.sdl.admin.ui.action.indexconfig;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexWriter;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.fdt.sdl.util.SecurityUtil;

/**
 * An action that handles the advanced settings of a dataset.
 *
 * <p>The action support an <i>action</i> URL parameter. This URL parameter
 * controls what this action class does. The following values are supported:</p>
 * <ul>
 *   <li>list - list, this is the default if no action parameter is specified
 *   <li>save - save
 * </ul>
 *
 */
public class DatasetOptionsAction extends Action {

    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.config.action.DatasetOptionsAction");

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request))return (mapping.findForward("welcome"));
    
        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
        HttpSession session = request.getSession();
    
        String indexName = request.getParameter("indexName");
        session.setAttribute("indexName", indexName);
    
        String action = request.getParameter("operation");
    
        try {
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
            session.setAttribute("dc", dc);
    
            if ("save".equals(action)) {
                // If a request parameter is invalid, default it to the current
                // value of dc's corresponding property
                double _indexMaxSize = U.getDouble(request.getParameter("indexMaxSize"), dc.getIndexMaxSize());
                ServerConfiguration sc = ServerConfiguration.getServerConfiguration();
                dc.setIndexMaxSize(_indexMaxSize);
                dc.setMaxMergeDocs(U.getInt(request.getParameter("maxMergeDocs"), dc.getMaxMergeDocs()));//removed from ftl, not in use
                dc.setWorkDirectory(U.getText(request.getParameter("workDirectory"), "_work"));
                dc.setMergePercentage(U.getFloat(request.getParameter("mergePercentage"), dc.getMergePercentage()));
                dc.setPrunePercentage(U.getInt(request.getParameter("prunePercentage"), dc.getPrunePercentage()));
                dc.setNumberReplicas(U.getInt(request.getParameter("numberReplicas"), dc.getNumberReplicas()));
                dc.setNumberShards(U.getInt(request.getParameter("numberShards"), dc.getNumberShards()));
                dc.setMergeHours(request.getParameter("mergeHoursEnabled"), request.getParameter("mergeHoursBegin"), request.getParameter("mergeHoursEnd"));
                dc.setNumberOfHoursBeforeDeletion(U.getInt(request.getParameter("numberOfHoursBeforeDeletion"), dc.getNumberOfHoursBeforeDeletion()));
    
                dc.setFetcherThreadsCount(U.getInt(request.getParameter("fetcherThreadsCount"), dc.getFetcherThreadsCount()));
                dc.setWriterThreadsCount(U.getInt(request.getParameter("writerThreadsCount"), dc.getWriterThreadsCount()));
                dc.setIsOptimizeNeeded(U.getBoolean(request.getParameter("isOptimizeNeeded"), "on", false));
                dc.setMaxFieldLength(U.getInt(request.getParameter("maxFieldLength"),IndexWriter.DEFAULT_MAX_FIELD_LENGTH));
                dc.setJvmMaxHeapSize(U.getInt(request.getParameter("jvmMaxHeapSize"), dc.getJvmMaxHeapSize()));
                dc.setDocumentBufferSizeMB(U.getInt(request.getParameter("documentBufferSizeMB"), dc.getDocumentBufferSizeMB()));
                dc.setMaxOpenFiles(U.getInt(request.getParameter("maxOpenFiles"), dc.getMaxOpenFiles()));
                dc.setListFetchSize(U.getInt(request.getParameter("listFetchSize"), dc.getListFetchSize()));
                
                dc.setSearcherMaxactive(U.getInt(request.getParameter("searcherMaxactive"), dc.getSearcherMaxactive()));
                dc.setSearcherMaxidle(U.getInt(request.getParameter("searcherMaxidle"), dc.getSearcherMaxidle()));
                dc.setSearcherMaxwait(U.getLong(request.getParameter("searcherMaxwait"), dc.getSearcherMaxwait()));
                //Deprecated
                //dc.setSearchWhenIndexing(U.getBoolean(request.getParameter("searchWhenIndexing"), "on", false));
                dc.setIsEmptyQueryMatchAll(U.getBoolean(request.getParameter("isEmptyQueryMatchAll"), "on", false));
                dc.setUrlToPing(U.getText(request.getParameter("urlToPing"), null));
                
                dc.setSimilarityName(U.getText(request.getParameter("similarityClassName"),DatasetConfiguration.DefaultSimilarity));

                if(U.getText(request.getParameter("allowedIpList"), null)!=null) {
                    if(sc.getAllowedLicenseLevel()>=1) {
                        dc.setAllowedIpList(U.getText(request.getParameter("allowedIpList"), null));
                    } else {
                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("limit.license", "setting allowed IP addresses"));
                    }
                }

                dc.save();
                messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("configuration.save.success"));
            }
        } catch (NullPointerException e) {
            errors.add("error", new ActionMessage("configuration.load.error", e));
        } catch (IOException e) {
            errors.add("error", new ActionMessage("configuration.changes.error", e));
        }
    
        saveErrors(request, errors);
        saveMessages(request, messages);
    
        // Forward to the velocity page
        return (mapping.findForward("continue"));
    }
}
