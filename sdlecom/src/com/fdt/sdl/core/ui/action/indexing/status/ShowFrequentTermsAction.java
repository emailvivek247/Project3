package com.fdt.sdl.core.ui.action.indexing.status;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.status.IndexStatus;
import net.javacoding.xsearch.status.TermFrequency;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.util.PriorityQueue;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.fdt.sdl.util.SecurityUtil;

/**
 * Implementation of <strong>Action</strong> that list most frequent terms in the index.
 * 
 * 
 * @version $Revision: 3971 $ $Date: 2007-03-30 23:57:32 -0700 (Fri, 30 Mar 2007) $
 */

public final class ShowFrequentTermsAction extends Action {
    private static Logger logger = LoggerFactory.getLogger(ShowFrequentTermsAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!SecurityUtil.isAdminUser(request)) return (mapping.findForward("welcome"));
        ActionMessages errors = new ActionMessages();
        String indexName = request.getParameter("indexName");
        String fieldName = request.getParameter("fieldName");
        int length = U.getInt(request.getParameter("length"),100);
        IndexReader ir = null;
        try {
            DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
            request.setAttribute("dc", dc);

            //columns
            ArrayList<Column> columns = dc.getColumns(true);
            ArrayList<String> fieldNames = new ArrayList<String>();
            ArrayList<String> columnNames = new ArrayList<String>();
            if (!U.isEmpty(fieldName)) {
                fieldNames.add(fieldName);
            }
            for (int i = 0; i < columns.size(); i++) {
                Column c = (Column)columns.get(i);
                if(c!=null && (c.getIndexFieldType().startsWith("Text")||"Keywords"==c.getIndexFieldType())) {
                    columnNames.add(c.getColumnName());
                    if (U.isEmpty(fieldName)) {
                        fieldNames.add(c.getColumnName());
                    }
                }
            }
            request.setAttribute("indexName", indexName);
            request.setAttribute("columnNames", columnNames);

            ir = IndexStatus.openIndexReader(dc);

            TermFrequencyQueue tiq = new TermFrequencyQueue(length);
            TermEnum terms = ir.terms();

            while (terms.next()) {
                if (inList(terms.term().field(), fieldNames)) {
                    tiq.insert(new TermFrequency(terms.term(), terms.docFreq()));
                }
            }

            ArrayList<TermFrequency> revertedTermFrequencies = new ArrayList<TermFrequency>();
            while (tiq.size() != 0) {
                TermFrequency termFrequency = (TermFrequency) tiq.pop();
                revertedTermFrequencies.add(termFrequency);
                //System.out.println(termFrequency.getTerm() + " " + termFrequency.getFrequency());
            }
            
            ArrayList<TermFrequency> termFrequencies = new ArrayList<TermFrequency>(revertedTermFrequencies.size());
            for(int i=revertedTermFrequencies.size()-1;i>=0;i--) {
                termFrequencies.add(revertedTermFrequencies.get(i));
            }

            request.setAttribute("termFrequencies", termFrequencies);
            request.setAttribute("totalCount", new Integer(ir.maxDoc()));

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
    private boolean inList(String v, ArrayList<String> a) {
        v = v.intern();
        if(v == lastMatch) return true;
        for(int i=0;i<a.size();i++) {
            String newValue = a.get(i).intern();
            if(v == newValue) {
                lastMatch = v;
                return true;
            }
        }
        return false;
    }
}

final class TermFrequencyQueue extends PriorityQueue {
    TermFrequencyQueue(int size) {
        initialize(size);
    }

    protected final boolean lessThan(Object a, Object b) {
        TermFrequency termInfoA = (TermFrequency) a;
        TermFrequency termInfoB = (TermFrequency) b;
        return termInfoA.getFrequency() < termInfoB.getFrequency();
    }
}
