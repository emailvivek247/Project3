package com.fdt.sdl.admin.ui.action.template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.search.searcher.IndexReaderSearcher;
import net.javacoding.xsearch.search.searcher.SearcherManager;
import net.javacoding.xsearch.search.searcher.SearcherProvider;
import net.javacoding.xsearch.utility.U;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fdt.sdl.util.SecurityUtil;

public class IndexFieldValuesAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		if (!SecurityUtil.isAdminUser(request)){
			return (mapping.findForward("welcome"));
		}
		
        String operation = U.getText(request.getParameter("operation"),"list");
        String indexName = request.getParameter("indexName");
        request.setAttribute("indexName", indexName);
        DatasetConfiguration dc = ServerConfiguration.getDatasetConfiguration(indexName);
        request.setAttribute("dc", dc);
        String columnName = request.getParameter("columnName");
        Column column = dc.findColumn(columnName);
        request.setAttribute("column", column);
        
        if("list".equals(operation)) {
            List<String> ret = new ArrayList<String>();
            int[] count = new int[1];
            listIndexValues(dc, column, ret, 10, count);
            request.setAttribute("value_count", count[0]);
            request.setAttribute("values", ret);
            return (mapping.findForward("list"));
        }
        return (mapping.findForward("continue"));
	}
	
	public void listIndexValues(DatasetConfiguration dc, Column column, List<String> ret, int limit, int[] count){
        SearcherProvider sp = SearcherManager.getSearcherProvider(dc.getName());
        if (sp != null) {
            IndexReaderSearcher irs = null;
            try {
                irs = sp.getIndexReaderSearcher();
                TermEnum termEnum = irs.getIndexReader().terms(new Term(column.getColumnName(),""));
                int counter = 0;
                try {
                    if (termEnum.term() == null) return;
                    do {
                        Term term = termEnum.term();
                        if (term.field() != column.getName()) break;
                        count[0]++;
                        if(ret.size()<limit) {
                            ret.add(term.text().intern());
                        }
                    } while (termEnum.next());
                } finally {
                    termEnum.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    irs.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
	}

}
