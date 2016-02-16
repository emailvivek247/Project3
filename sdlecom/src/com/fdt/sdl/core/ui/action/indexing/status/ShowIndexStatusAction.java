package com.fdt.sdl.core.ui.action.indexing.status;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.params.Parameters;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.status.IndexStatus;

import org.apache.lucene.index.IndexReader;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.elasticsearch.config.SpringContextUtil;
import com.fdt.elasticsearch.query.MatchAllQuery;
import com.fdt.elasticsearch.type.result.CustomSearchResult;
import com.fdt.elasticsearch.util.JestExecute;
import com.fdt.sdl.admin.ui.action.constants.IndexType;
import com.fdt.sdl.util.SecurityUtil;

public final class ShowIndexStatusAction extends Action {

    private static Logger logger = LoggerFactory.getLogger(ShowIndexStatusAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request)) {
            return (mapping.findForward("welcome"));
        }
        ActionMessages errors = new ActionMessages();
        String indexName = request.getParameter("indexName");
        String docNum = request.getParameter("docNum");
        Integer intDocNumber = null;
        IndexReader ir = null;
        try {
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
            request.setAttribute("dc", dc);
            Column pkColumn = null;
            if (dc.getWorkingQueueDataquery() != null) {
                pkColumn = dc.getWorkingQueueDataquery().getPrimaryKeyColumn();
                request.setAttribute("pkColumn", pkColumn);
            }
            if (docNum != null && docNum.trim().length() > 0) {
                intDocNumber = new Integer(docNum);
            } else {
                intDocNumber = new Integer(0);
            }
            request.setAttribute("periodTable", IndexStatus.getPeriodTable(dc));
            request.setAttribute("docNum", intDocNumber);
            request.setAttribute("indexName", indexName);
            if (dc.getIndexType() == null || dc.getIndexType() == IndexType.LUCENE) {
                ir = IndexStatus.openIndexReader(dc);
                if (ir != null) {
                    request.setAttribute("totalCount", new Integer(ir.maxDoc()));
                    request.setAttribute("doc", ir.document(intDocNumber.intValue()));
                } else {
                    request.setAttribute("totalCount", new Integer(0));
                }
            } else if (dc.getIndexType() == IndexType.ELASTICSEARCH) {
                JestClient client = SpringContextUtil.getBean(JestClient.class);
                MatchAllQuery query = new MatchAllQuery.Builder().addSort("_doc").build();
                Search search = new Search.Builder(query.getAsString())
                        .addIndex(IndexStatus.getAliasName(dc))
                        .setParameter(Parameters.SIZE, 1)
                        .setParameter("from", intDocNumber)
                        .build();
                SearchResult searchResult = JestExecute.execute(client, search);
                CustomSearchResult result = new CustomSearchResult(searchResult);
                request.setAttribute("totalCount", new Integer(result.getTotal()));
                request.setAttribute("result", result.getResultAsMap(0));
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
        } finally {
            if (ir != null) {
                ir.close();
            }
            saveErrors(request, errors);
        }

        // Forward control to the display velocity page
        return (mapping.findForward("continue"));

    }
}
