package com.fdt.sdl.core.ui.action.indexing.status;

import io.searchbox.client.JestClient;
import io.searchbox.core.Count;
import io.searchbox.core.CountResult;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Get;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.params.Parameters;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.status.IndexStatus;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.fdt.elasticsearch.config.SpringContextUtil;
import com.fdt.elasticsearch.query.MatchAllQuery;
import com.fdt.elasticsearch.type.result.CustomSearchResult;
import com.fdt.elasticsearch.type.result.GetResult;
import com.fdt.elasticsearch.util.JestExecute;
import com.fdt.sdl.admin.ui.action.constants.IndexType;
import com.fdt.sdl.util.SecurityUtil;

public final class ShowIndexContentByPKAction extends Action {

    private static Logger logger = LoggerFactory.getLogger(ShowIndexContentByPKAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request)) {
            return (mapping.findForward("welcome"));
        }
        ActionMessages errors = new ActionMessages();
        HttpSession session = request.getSession();
        String indexName = request.getParameter("indexName");
        String pkValue = request.getParameter("pkValue");
        request.setAttribute("pkValue", pkValue);
        Integer intDocNumber = null;
        IndexReader ir = null;
        try {
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
            session.setAttribute("dc", dc);
            if (!U.isEmpty(pkValue)) {
                pkValue = pkValue.trim();
            }
            Column pkColumn = null;
            if (dc.getWorkingQueueDataquery() != null) {
                pkColumn = dc.getWorkingQueueDataquery().getPrimaryKeyColumn();
                request.setAttribute("pkColumn", pkColumn);
            }
            request.setAttribute("periodTable", IndexStatus.getPeriodTable(dc));
            request.setAttribute("docNum", intDocNumber);
            session.setAttribute("indexName", indexName);
            if (dc.getIndexType() == null || dc.getIndexType() == IndexType.LUCENE) {
                ir = IndexStatus.openIndexReader(dc);
                if (ir != null) {
                    request.setAttribute("totalCount", new Integer(ir.numDocs()));
                } else {
                    request.setAttribute("totalCount", new Integer(0));
                }
                if (!U.isEmpty(pkValue) && ir != null && pkColumn != null) {
                    Query pkQuery = null;
                    Term pkTerm = null;
                    pkTerm = new Term(pkColumn.getColumnName(), pkValue);
                    pkQuery = new TermQuery(pkTerm);
                    Hits hits = null;
                    Searcher s = new IndexSearcher(ir);
                    try {
                        hits = s.search(pkQuery);
                    } catch (RuntimeException re) {
                        errors.add("error", new ActionMessage("action.search.runtime.error", re.getMessage()));
                    }
                    if (hits != null) {
                        int totalFound = hits.length();
                        if (totalFound > 1) {
                            errors.add("error", new ActionMessage("action.showIndexStatus.duplicatePrimaryKey.error",
                                    pkColumn.getColumnName() + "=" + pkValue));
                            Document doc = hits.doc(0);
                            request.setAttribute("doc", doc);
                        } else if (totalFound <= 0) {
                            errors.add("error", new ActionMessage("action.showIndexStatus.notFoundPrimaryKey.error",
                                    pkColumn.getColumnName() + "=" + pkValue));
                        } else {
                            Document doc = hits.doc(0);
                            request.setAttribute("doc", doc);
                        }
                    }
                } else {
                    intDocNumber = new Integer(0);
                }
            } else if (dc.getIndexType() == IndexType.ELASTICSEARCH) {
                JestClient client = SpringContextUtil.getBean(JestClient.class);
                Get getRequest = new Get.Builder(IndexStatus.getAliasName(dc), pkValue).build();
                GetResult getResult = new GetResult(JestExecute.executeNoCheck(client, getRequest));
                if (!getResult.exists()) {
                    errors.add("error", new ActionMessage("action.showIndexStatus.notFoundPrimaryKey.error",
                            pkColumn.getColumnName() + "=" + pkValue));
                } else {
                    request.setAttribute("result", getResult.getAsMap());
                }
                Count countRequest = new Count.Builder().addIndex(IndexStatus.getAliasName(dc)).build();
                CountResult countResult = JestExecute.execute(client, countRequest);
                request.setAttribute("totalCount", countResult.getCount());
            }

        } catch (IOException ex) {
            errors.add("error", new ActionMessage("action.showIndexStatus.index.error",indexName));
            logger.debug("Exception Occurred", ex);
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
