package com.fdt.sdl.core.ui.action.indexing.status;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.params.Parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.status.IndexStatus;
import net.javacoding.xsearch.status.TermFrequency;
import net.javacoding.xsearch.utility.U;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;
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
import com.fdt.elasticsearch.query.TermsAggregation;
import com.fdt.elasticsearch.type.result.CustomSearchResult;
import com.fdt.elasticsearch.util.JestExecute;
import com.fdt.sdl.admin.ui.action.constants.IndexType;
import com.fdt.sdl.util.SecurityUtil;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Implementation of <strong>Action</strong> that list most frequent terms in the index.
 * 
 * 
 * @version $Revision: 3971 $ $Date: 2007-03-30 23:57:32 -0700 (Fri, 30 Mar 2007) $
 */
public final class ShowFrequentTermsAction extends Action {
    private static Logger logger = LoggerFactory.getLogger(ShowFrequentTermsAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {

        if (!SecurityUtil.isAdminUser(request)) {
            return (mapping.findForward("welcome"));
        }

        ActionMessages errors = new ActionMessages();
        String indexName = request.getParameter("indexName");
        String fieldName = request.getParameter("fieldName");
        int length = U.getInt(request.getParameter("length"), 100);

        IndexReader ir = null;

        try {

            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
            request.setAttribute("dc", dc);

            List<Column> columns = dc.getColumns(true);
            List<String> fieldNames = new ArrayList<>();
            List<String> columnNames = new ArrayList<>();
            if (!U.isEmpty(fieldName)) {
                fieldNames.add(fieldName);
            }
            for (Column c : columns) {
                if (c.getIndexFieldType().startsWith("Text") || "Keywords" == c.getIndexFieldType()) {
                    columnNames.add(c.getColumnName());
                    if (U.isEmpty(fieldName)) {
                        fieldNames.add(c.getColumnName());
                    }
                }
            }
            request.setAttribute("indexName", indexName);
            request.setAttribute("columnNames", columnNames);

            Comparator<TermFrequency> comparator = Comparator.comparing(TermFrequency::getFrequency).reversed();
            MinMaxPriorityQueue<TermFrequency> queue = MinMaxPriorityQueue
                    .orderedBy(comparator)
                    .maximumSize(length)
                    .create();

            if (dc.getIndexType() == null || dc.getIndexType() == IndexType.LUCENE) {
                ir = IndexStatus.openIndexReader(dc);
                TermEnum terms = ir.terms();
                while (terms.next()) {
                    if (inList(terms.term().field(), fieldNames)) {
                        queue.add(new TermFrequency(terms.term().field(), terms.term().text(), terms.docFreq()));
                    }
                }
                request.setAttribute("totalCount", new Integer(ir.maxDoc()));
            } else if (dc.getIndexType() == IndexType.ELASTICSEARCH) {
                JestClient client = SpringContextUtil.getBean(JestClient.class);
                List<TermsAggregation> aggs = fieldNames.stream().map(
                        f -> new TermsAggregation(f, false, length)
                ).collect(Collectors.toList());
                MatchAllQuery query = new MatchAllQuery.Builder().addTermsAggregation(aggs).build();
                String queryStr = query.getAsString();
                logger.info("Elasticsearch query: {}", queryStr);
                Search search = new Search.Builder(queryStr)
                        .addIndex(indexName)
                        .setParameter(Parameters.SIZE, 0)
                        .setParameter("from", 0)
                        .build();
                SearchResult searchResult = JestExecute.execute(client, search);
                CustomSearchResult result = new CustomSearchResult(searchResult);
                JsonObject aggregations = result.getAggregations();
                for (Map.Entry<String, JsonElement> aggregation : aggregations.entrySet()) {
                    String field = aggregation.getKey();
                    JsonObject aggValueObject = aggregation.getValue().getAsJsonObject();
                    JsonArray bucketsArray = aggValueObject.getAsJsonArray("buckets");
                    for (JsonElement bucketElement : bucketsArray) {
                        JsonObject bucket = bucketElement.getAsJsonObject();
                        int docCount = bucket.get("doc_count").getAsInt();
                        String value = bucket.get("key").getAsString();
                        queue.add(new TermFrequency(field, value, docCount));
                    }
                }
                request.setAttribute("totalCount", new Integer(result.getTotal()));
            }

            List<TermFrequency> termFrequencies = new ArrayList<>();
            while (queue.size() != 0) {
                termFrequencies.add(queue.poll());
            }
            request.setAttribute("termFrequencies", termFrequencies);



        } catch (IOException ex) {
            errors.add("error", new ActionMessage("action.showIndexStatus.index.error", indexName));
            logger.debug("Exception Occurred", ex);
            return (mapping.findForward("continue"));
        } catch (NullPointerException se) {
            errors.add("error", new ActionMessage("action.showIndexStatus.index.error", indexName + " is not found"));
            return (mapping.findForward("continue"));
        } finally {
            lastMatch = null;
            if (ir != null) {
                ir.close();
            }
            saveErrors(request, errors);
        }

        // Forward control to the display velocity page
        return (mapping.findForward("continue"));

    }

    // a last match for caching purpose
    private String lastMatch = null;

    private boolean inList(String v, List<String> a) {
        v = v.intern();
        if (v == lastMatch)
            return true;
        for (int i = 0; i < a.size(); i++) {
            String newValue = a.get(i).intern();
            if (v == newValue) {
                lastMatch = v;
                return true;
            }
        }
        return false;
    }
}
