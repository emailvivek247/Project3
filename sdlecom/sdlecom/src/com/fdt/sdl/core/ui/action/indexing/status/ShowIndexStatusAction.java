package com.fdt.sdl.core.ui.action.indexing.status;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.status.IndexStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.fdt.sdl.util.SecurityUtil;

/**
 * Implementation of <strong>Action</strong> that performs search.
 *
 * 
 */

public final class ShowIndexStatusAction extends Action
{
  	private static Logger logger = LoggerFactory.getLogger(ShowIndexStatusAction.class);

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                                 throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request))return (mapping.findForward("welcome"));
        ActionMessages errors = new ActionMessages();
        String indexName = request.getParameter("indexName");
        String docNum = request.getParameter("docNum");
        Integer intDocNumber = null;
        IndexReader ir = null;
        try {
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
            request.setAttribute("dc", dc);
            Column pkColumn = null;
            if(dc.getWorkingQueueDataquery()!=null){
                pkColumn = dc.getWorkingQueueDataquery().getPrimaryKeyColumn();
                request.setAttribute("pkColumn", pkColumn);
            }

            if(docNum!=null&&docNum.trim().length()>0)
              intDocNumber = new Integer(docNum);
            else
              intDocNumber = new Integer(0);
            request.setAttribute("periodTable", IndexStatus.getPeriodTable(dc));
            request.setAttribute("docNum", intDocNumber);
            request.setAttribute("indexName", indexName);
            ir = IndexStatus.openIndexReader(dc);
            if(ir!=null){
                request.setAttribute("totalCount", new Integer(ir.maxDoc()));
                request.setAttribute("doc", ir.document(intDocNumber.intValue()));
            }else{
                request.setAttribute("totalCount", new Integer(0));
            }
        } catch (IOException ex) {
            errors.add("error", new ActionMessage("action.showIndexStatus.index.error",indexName));
            logger.debug("Exception Occurred", ex);
            return (mapping.findForward("continue"));
        } catch (NumberFormatException nfe) {
            errors.add("error", new ActionMessage("action.showIndexStatus.docNumber.error",docNum+" is not valid format"));
            return (mapping.findForward("continue"));
        } catch (IllegalArgumentException iae) {
            errors.add("error", new ActionMessage("action.showIndexStatus.docucment.deleted.error",intDocNumber));
            return (mapping.findForward("continue"));
        } catch (ArrayIndexOutOfBoundsException aobe) {
            errors.add("error", new ActionMessage("action.showIndexStatus.docNumber.error",intDocNumber+" is out of range"));
            return (mapping.findForward("continue"));
        } catch (NullPointerException se) {
            errors.add("error", new ActionMessage("action.showIndexStatus.index.error",indexName+" is not found"));
            return (mapping.findForward("continue"));
        }finally{
            if(ir!=null){ir.close();}
            saveErrors(request,errors);
        }

        // Forward control to the display velocity page
        return (mapping.findForward("continue"));

    }
}
