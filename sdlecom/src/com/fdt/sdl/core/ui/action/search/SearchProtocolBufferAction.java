package com.fdt.sdl.core.ui.action.search;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.params.Parameters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.javacoding.xsearch.api.Document;
import net.javacoding.xsearch.api.SDLIndexDocument;
import net.javacoding.xsearch.api.protocol.SearchProtocol;
import net.javacoding.xsearch.api.protocol.SearchProtocol.SearchRequest;
import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.ServerConfiguration;
import net.javacoding.xsearch.search.HTMLEntities;
import net.javacoding.xsearch.search.Highlighter;
import net.javacoding.xsearch.search.HitDocument;
import net.javacoding.xsearch.search.analysis.QueryHelper;
import net.javacoding.xsearch.search.result.SearchResult;
import net.javacoding.xsearch.search.result.SearchSort;
import net.javacoding.xsearch.search.result.filter.Count;
import net.javacoding.xsearch.search.result.filter.FilterColumn;
import net.javacoding.xsearch.search.result.filter.FilterResult;
import net.javacoding.xsearch.status.IndexStatus;
import net.javacoding.xsearch.status.QueryLogger;
import net.javacoding.xsearch.utility.EscapeChars;
import net.javacoding.xsearch.utility.U;

import org.apache.lucene.document.Field;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.elasticsearch.config.SpringContextUtil;
import com.fdt.elasticsearch.parsing.ESColumnHelper;
import com.fdt.elasticsearch.parsing.ESQueryHelper;
import com.fdt.elasticsearch.query.AbstractQuery;
import com.fdt.elasticsearch.type.result.CustomSearchResult;
import com.fdt.elasticsearch.util.ESSearchUtils;
import com.fdt.sdl.admin.ui.action.constants.IndexType;
import com.fdt.sdl.core.ui.action.search.SearchAction.SearchContext;

/**
 * This search receives normal url input, but return binary stream of results
 * via Google's Protocol Buffer
 */
public class SearchProtocolBufferAction extends Action {

    private static final Logger logger = LoggerFactory.getLogger(SearchProtocolBufferAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {

        long _start = System.currentTimeMillis();
        ActionMessages errors = new ActionMessages();
        SearchProtocol.SearchRequest r = SearchProtocol.SearchRequest.parseFrom(request.getInputStream());
        OutputStream outStream = response.getOutputStream();

        SearchContext sc = new SearchContext();
        try {
            SearchResult sr = new SearchResult();
            sc.debug = r.getDebug();
            String[] indexNames = r.getIndexList().toArray(new String[r.getIndexCount()]);

            if (r.getIndexCount() > 0) {
                sc.dc = ServerConfiguration.getDatasetConfiguration(r.getIndex(0));
            }
            if (sc.debug) {
                logger.info("Got searcher: " + (System.currentTimeMillis() - _start));
            }

            FilterResult filterResult = new FilterResult();
            String q = r.getQuery();
            String lq = r.getLuceneQuery();

            List<SearchSort> sortBys = new ArrayList<SearchSort>();
            for (SearchProtocol.Sort s : r.getSortList()) {
                SearchSort ss = new SearchSort(sc.dc.findColumn(s.getColumn()));
                ss.descending = s.getDescending();
                sortBys.add(ss);
            }

            long searchTime = 0;
            long start = System.currentTimeMillis();
            Highlighter summarizer = null;

            if (sc.dc.getIndexType() == null || sc.dc.getIndexType() == IndexType.LUCENE) {
                List<HitDocument> docs = null;
                int[] total = new int[1];
                if (sc.debug) {
                    logger.info("Start Searching: " + (System.currentTimeMillis() - _start));
                }
                sc.irs = SearchAction.getIndexReaderSearcher(indexNames);
                Query query = QueryHelper.getSearchQuery(sr, q, r.getLuceneQuery(), filterResult, request, sc.dc,
                        sc.irs, r.getBooleanOperator(), r.getSearchable(), r.getRandomQuerySeed(), sc.debug);
                if (query != null) {
                    Hits hits = null;
                    if (r.getEnalbeFacetSearch()) {
                        SearchAction.narrowBySearch(query, sc.irs, sc.dc, filterResult, errors, request);
                    }
                    if (sortBys.size() <= 0) {
                        hits = SearchAction.directSearch(query, sc.irs, sc.dc, hits, errors, request);
                        searchTime = System.currentTimeMillis() - start;
                        docs = SearchAction.collectHits(sc.dc, hits, r.getResultPerPage(), r.getStart(), total);
                    } else {
                        // to switch to searchSortedDocuments, comment out hits section
                        hits = SearchAction.sortBySearch(query, sc.irs, sc.dc, sortBys, request, hits, errors);
                        searchTime = System.currentTimeMillis() - start;
                        docs = SearchAction.collectHits(sc.dc, hits, r.getResultPerPage(), r.getStart(), total);
                    }
                    if (sc.debug) {
                        logger.info("Top 3 results Explained +++++++++++++++++++++++++++++++++++ ");
                        for (int i = 0; i < hits.length() && i < 3; i++) {
                            logger.info("Result " + (i + 1) + ":" + hits.score(i));
                            logger.info(sc.irs.getSearcher().explain(query, hits.id(i)).toString());
                        }
                        logger.info("Top 3 results Explained ----------------------------------- ");
                    }
                    if (sc.debug) {
                        logger.info("Got docs from disk: " + (System.currentTimeMillis() - _start));
                    }

                    sr.init(sc, q, r.getLuceneQuery(), query, docs, null, searchTime, total[0], r.getStart(),
                            r.getResultPerPage(), sortBys, filterResult, request, response);

                    summarizer = new Highlighter(sc.dc.getAnalyzer(), query.rewrite(sc.irs.getIndexReader()), q);
                    summarizer.setHighlightPrefix(r.getBeginHighlightTag());
                    summarizer.setHighlightSuffix(r.getEndHighlightTag());

                    QueryLogger.log(
                            request != null ? request.getRemoteUser() : null,
                            r.getSourceLocation().isEmpty() ? "API" : r.getSourceLocation(),
                            HTMLEntities.encode(q.isEmpty() && !lq.isEmpty() ? lq : q),
                            U.join(r.getIndexList(), ","),
                            "API", System.currentTimeMillis(), searchTime, System.currentTimeMillis() - _start,
                            total[0]);
                }
            } else if (sc.dc.getIndexType() == IndexType.ELASTICSEARCH) {

                List<Document> resultDocs = new ArrayList<>();
                int totalCount = 0;

                if (sc.dc.getIsEmptyQueryMatchAll() || !U.isEmpty(q) || !U.isEmpty(lq)) {
                    boolean forceLucene = "Y".equalsIgnoreCase(request.getParameter("lucene"));
                    String searchableColsStr = request.getParameter("searchable");
                    ESColumnHelper columnHelper = new ESColumnHelper(sc.dc.getColumns(), searchableColsStr);
                    ESQueryHelper queryHelper = new ESQueryHelper(sc.dc, q, lq, forceLucene, columnHelper);

                    AbstractQuery.Builder<?, ?> esQueryBuilder = queryHelper.getSearchQuery();
                    esQueryBuilder.addTermsAggregation(sc.dc.getFilterableColumns());
                    esQueryBuilder.addSort(sortBys);
                    esQueryBuilder.addHighlightField(columnHelper.getHighlightColsStr());

                    filterResult.addFilteredColumns(queryHelper.getFilteredColumns());
                    sr.setUserInput(queryHelper.getUserInput());

                    AbstractQuery abstractQuery = esQueryBuilder.build();

                    JestClient client = SpringContextUtil.getBean(JestClient.class);

                    String elasticSearchQuery = abstractQuery.getAsString();
                    logger.info("Elastic Search Query: " + elasticSearchQuery);

                    Search search = new Search.Builder(elasticSearchQuery)
                            .addIndex(IndexStatus.getAliasName(sc.dc))
                            .setParameter(Parameters.SIZE, r.getResultPerPage())
                            .setParameter("from", r.getStart())
                            .build();

                    searchTime = System.currentTimeMillis() - start;

                    CustomSearchResult result = new CustomSearchResult(client.execute(search));
                    resultDocs = ESSearchUtils.extractResultDocs(result);
                    totalCount = result.getTotal();
                    ESSearchUtils.populateFilterResult(filterResult, result, sc.dc);
                }

                sr.initFor3Tier(sc, q, lq, null, resultDocs, null, searchTime, totalCount, r.getStart(),
                        r.getResultPerPage(), sortBys, filterResult, request, response);

            }

            SearchProtocol.SearchResponse.Builder rb = SearchProtocol.SearchResponse.newBuilder();
            rb.setSearchTime(sr.getSearchTime());
            rb.setTotal(sr.getTotal());
            for (SDLIndexDocument d : sr.getDocs()) {
                if (sc.dc.getIndexType() == null || sc.dc.getIndexType() == IndexType.LUCENE) {
                    rb.addDoc(buildLuceneDoc(r, sc, summarizer, d));
                } else if (sc.dc.getIndexType() == IndexType.ELASTICSEARCH) {
                    rb.addDoc(buildEsDoc(r, sc, d));
                }
            }

            for (FilterColumn fc : sr.getFilterResult().filterColumns) {
                SearchProtocol.FacetChoice.Builder fb = SearchProtocol.FacetChoice.newBuilder();
                fb.setColumn(fc.column.getColumnName());
                for (Count c : fc.getCounts()) {
                    SearchProtocol.FacetCount.Builder cb = SearchProtocol.FacetCount.newBuilder();
                    cb.setValue(c.columnValue == null ? "" : c.columnValue);
                    cb.setEndValue(c.columnEndValue == null ? "" : c.columnEndValue);
                    cb.setCount(c.value);
                    fb.addFacetCount(cb);
                    if (fb.getFacetCountCount() >= r.getFacetCountLimit()) {
                        break;
                    }
                }
                fb.setFacetCountTotal(fc.getCounts().size());
                fb.setMaxIntegerValue(fc.getMaxIntegerValue());
                fb.setMinIntegerValue(fc.getMinIntegerValue());
                rb.addFacetChoice(fb);
            }

            rb.build().writeTo(outStream);
            outStream.close();
            outStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
            return (mapping.findForward("error"));
        } finally {
            if (sc.irs != null) {
                SearchAction.closeIndexReaderSearcher(sc.irs);
            }
        }
        return null;
    }

    private SearchProtocol.Document.Builder buildLuceneDoc(SearchRequest r, SearchContext sc, Highlighter summarizer,
            SDLIndexDocument d) {

        SearchProtocol.Document.Builder db = SearchProtocol.Document.newBuilder();
        db.setScore(d.getScore()).setBoost(d.getBoost());

        for (Field f : d.fields()) {
            SearchProtocol.Field.Builder fb = SearchProtocol.Field.newBuilder();
            fb.setName(f.name());
            Column c = sc.dc.findColumn(f.name());
            if (c == null) {
                continue;
            }
            if (c.getIsDate()) {
                fb.setType(SearchProtocol.Field.Type.DATETIME);
                fb.setValue(f.stringValue());
            } else if (c.getIsNumber()) {
                fb.setType(SearchProtocol.Field.Type.NUMBER);
                fb.setValue(f.stringValue());
            } else {
                fb.setType(SearchProtocol.Field.Type.STRING);
                fb.setValue(f.stringValue());
                for (SearchProtocol.StringColumnFormat scf : r.getStringColumnFormatList()) {
                    if (f.name().equals(scf.getColumn())) {
                        if (scf.getStringFormat() == SearchProtocol.StringColumnFormat.StringFormat.HTML) {
                            fb.setValue(EscapeChars.forHTMLTag(f.stringValue()));
                        } else if (scf.getStringFormat() == SearchProtocol.StringColumnFormat.StringFormat.HIGHLIGHTED) {
                            fb.setValue(summarizer.getHighlighted(f.stringValue(), f.name()));
                        } else if (scf.getStringFormat() == SearchProtocol.StringColumnFormat.StringFormat.HIGHLIGHTED_HTML) {
                            fb.setValue(summarizer.getHighlighted(EscapeChars.forHTMLTag(f.stringValue()), f.name()));
                        } else if (scf.getStringFormat() == SearchProtocol.StringColumnFormat.StringFormat.SUMMARIZED) {
                            fb.setValue(summarizer.getSummary(f.stringValue(), f.name()));
                        } else if (scf.getStringFormat() == SearchProtocol.StringColumnFormat.StringFormat.SUMMARIZED_HTML) {
                            fb.setValue(summarizer.getSummary(EscapeChars.forHTMLTag(f.stringValue()), f.name()));
                        }
                        break;
                    }
                }
            }
            db.addField(fb);
        }
        return db;
    }

    private SearchProtocol.Document.Builder buildEsDoc(SearchRequest r, SearchContext sc, SDLIndexDocument d) {

        SearchProtocol.Document.Builder db = SearchProtocol.Document.newBuilder();
        db.setScore(d.getScore()).setBoost(d.getBoost());

        for (String fieldName : d.fieldNames()) {
            SearchProtocol.Field.Builder fb = SearchProtocol.Field.newBuilder();
            fb.setName(fieldName);
            Column c = sc.dc.findColumn(fieldName);
            if (c == null) {
                continue;
            }
            if (c.getIsDate()) {
                fb.setType(SearchProtocol.Field.Type.DATETIME);
                fb.setValue(d.getString(fieldName));
            } else if (c.getIsNumber()) {
                fb.setType(SearchProtocol.Field.Type.NUMBER);
                fb.setValue(d.getString(fieldName));
            } else {
                fb.setType(SearchProtocol.Field.Type.STRING);
                fb.setValue(d.getString(fieldName));
                for (SearchProtocol.StringColumnFormat scf : r.getStringColumnFormatList()) {
                    if (fieldName.equals(scf.getColumn())) {
                        if (scf.getStringFormat() == SearchProtocol.StringColumnFormat.StringFormat.HTML) {
                            fb.setValue(EscapeChars.forHTMLTag(d.getString(fieldName)));
                        } else if (scf.getStringFormat() == SearchProtocol.StringColumnFormat.StringFormat.HIGHLIGHTED) {
                            fb.setValue(d.getString(fieldName));
                        } else if (scf.getStringFormat() == SearchProtocol.StringColumnFormat.StringFormat.HIGHLIGHTED_HTML) {
                            fb.setValue(EscapeChars.forHTMLTag(d.getString(fieldName)));
                        } else if (scf.getStringFormat() == SearchProtocol.StringColumnFormat.StringFormat.SUMMARIZED) {
                            fb.setValue(d.getString(fieldName));
                        } else if (scf.getStringFormat() == SearchProtocol.StringColumnFormat.StringFormat.SUMMARIZED_HTML) {
                            fb.setValue(EscapeChars.forHTMLTag(d.getString(fieldName)));
                        }
                        break;
                    }
                }
            }
            db.addField(fb);
        }

        return db;
    }
}
