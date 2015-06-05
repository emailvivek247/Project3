package com.fdt.sdl.core.ui.action.indexing.memory;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.core.component.TextDocument;
import net.javacoding.xsearch.search.memory.BufferIndexManager;
import net.javacoding.xsearch.search.searcher.IndexReaderSearcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocCollector;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fdt.sdl.util.SecurityUtil;

public class SubmitAction extends Action {
    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.memory.action.SubmitAction");

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ArrayList<String> errors = new ArrayList<String>();
        String indexName = request.getParameter("indexName");
        IndexReaderSearcher irs = null;
        DatasetConfiguration dc = null;
        try {
            ActionForward af = mapping.findForward("continue");
            // get searcher
            if (indexName == null) return af;

            dc = ServerConfiguration.getDatasetConfiguration(indexName);
            if (dc == null) return af;
            if (!SecurityUtil.isAllowed(request, dc)) {
                errors.add("Security Error during submit. Please check allowed IP address list includes "+ request.getRemoteAddr());
                return af;
            }
            Column pkColumn = dc.getPrimaryKeyColumn();
            if (pkColumn==null) {
                errors.add("Primary Key Column should exist when submitting content.");
                return af;
            }
            String pkValue = request.getParameter(pkColumn.getColumnName());
            if (pkValue==null) {
                errors.add("Primary Key Column "+pkColumn.getColumnName()+" should have a value.");
                return af;
            }
            //move data to text Document
            TextDocument td =  new TextDocument();
            for (Column col : dc.getColumns()) {
                String[] values = request.getParameterValues(col.getColumnName());
                if(values==null)continue;
                for(String value: values) {
                    td.add(col.getColumnName(), value);
                }
            }
            
            BufferIndexManager.submit(dc, td);

            return af;
        } catch (Exception se) {
            logger.debug("error when trying to submit to "+indexName, se);
            errors.add("Error when trying to access index "+indexName);
            return (mapping.findForward("continue"));
        } finally {
            request.setAttribute("errs", errors);
        }

    }
    public static synchronized Document deleteExisting(IndexReader reader, Query query, boolean commit) throws CorruptIndexException, IOException {
        Document d = null;
        TopDocCollector collector = new TopDocCollector(20);
        new IndexSearcher(reader).search(query, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        for (int i = 0; i < hits.length; i++) {
            if(d == null) {
                d = reader.document(hits[i].doc);
            }
            reader.deleteDocument(hits[i].doc);
        }
        if(commit) {
            reader.flush();
        }
        return d;
    }

}
