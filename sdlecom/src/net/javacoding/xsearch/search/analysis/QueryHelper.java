package net.javacoding.xsearch.search.analysis;

import javax.servlet.http.HttpServletRequest;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.search.HTMLEntities;
import net.javacoding.xsearch.search.QueryTranslator;
import net.javacoding.xsearch.search.SearchQueryParser;
import net.javacoding.xsearch.search.function.RandomQuery;
import net.javacoding.xsearch.search.function.TimeWeightQuery;
import net.javacoding.xsearch.search.query.DbsQuery;
import net.javacoding.xsearch.search.result.SearchResult;
import net.javacoding.xsearch.search.result.filter.FilterResult;
import net.javacoding.xsearch.search.searcher.IndexReaderSearcher;
import net.javacoding.xsearch.utility.U;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryHelper {

    private static final Logger logger = LoggerFactory.getLogger(QueryHelper.class);

    public static Query getSearchQuery(String q, String lq, FilterResult filterResult, HttpServletRequest request,
            DatasetConfiguration dc, IndexReaderSearcher irs, int booleanOperator, String dynamicSearchable,
            int randomQuerySeed, boolean debug) {
        // this is used for cases you don't have a SearchResult available
        return getSearchQuery(null, q, lq, filterResult, request, dc, irs, booleanOperator, dynamicSearchable,
                randomQuerySeed, debug);
    }

    public static Query getSearchQuery(SearchResult sr, String q, String lq, FilterResult filterResult,
            HttpServletRequest request, DatasetConfiguration dc, IndexReaderSearcher irs, int booleanOperator,
            String dynamicSearchable, int randomQuerySeed, boolean debug) {

        Query advancedQuery = null;
        Query query = null;
        if (filterResult == null) {
            filterResult = new FilterResult();
        }

        try {
            if ("Y".equalsIgnoreCase(request.getParameter("lucene"))) {
                // option to switch to Lucene's RAW query parser
                query = SearchQueryParser.luceneParse(dc.getAnalyzer(), q);
            } else {
                if (U.isEmpty(q) && dc.getIsEmptyQueryMatchAll() && U.isEmpty(lq)) {
                    advancedQuery = new MatchAllDocsQuery();
                }
                if (!U.isEmpty(q)) {
                    AdvancedQueryAnalysis aqa = new AdvancedQueryAnalysis(dc, q, filterResult);
                    q = aqa.getRemainingQueryString();
                    advancedQuery = aqa.getAdvancedQuery();
                    if (debug)
                        logger.info("Advanced query: " + advancedQuery);
                    if (aqa.getIsAllNegative() && advancedQuery != null) {
                        advancedQuery = AdvancedQueryAnalysis.appendQuery(advancedQuery, new MatchAllDocsQuery(),
                                Occur.MUST);
                    }
                }
                if (!U.isEmpty(q)) {
                    QueryTranslator translator = new QueryTranslator(dc.getColumns(), dynamicSearchable);
                    if (debug)
                        logger.info("Start parse query: " + HTMLEntities.encode(q));
                    DbsQuery myQuery = QueryAnalysis.parseQuery(q, dc);
                    request.setAttribute("parsedQuery", myQuery);
                    if (sr != null) {
                        sr.setUserInput(myQuery.getUserInput());
                    }
                    if (debug)
                        logger.info("parsed query: " + HTMLEntities.encode(myQuery.toString()));
                    translator.setSlop(5);
                    translator.setBooleanOperator(booleanOperator);
                    query = translator.translate(dc.getAnalyzer(), myQuery, filterResult, dc);
                    if (debug)
                        logger.info("translated query: " + HTMLEntities.encode(query.toString()));
                    if (translator.getIsAllNegative()) {
                        query = AdvancedQueryAnalysis.appendQuery(query, new MatchAllDocsQuery(), Occur.MUST);
                    }
                }
                query = AdvancedQueryAnalysis.andQuery(query, advancedQuery);
                if (!U.isEmpty(lq)) {
                    Query luceneQuery = SearchQueryParser.luceneParse(dc.getAnalyzer(), lq);
                    query = AdvancedQueryAnalysis.appendQuery(query, luceneQuery);
                    if (debug)
                        logger.info("added Lucene query: " + HTMLEntities.encode(query.toString()));
                }
            }
            if (dc.getDateWeightColumnName() != null) {
                query = new TimeWeightQuery(query, dc, irs.getIndexReader());
                if (debug)
                    logger.info("added time-weighted query: " + HTMLEntities.encode(query.toString()));
            }
            if (randomQuerySeed != 0) {
                query = new RandomQuery(query, randomQuerySeed);
                if (debug)
                    logger.info("added random query: " + HTMLEntities.encode(query.toString()));
            }
        } catch (Exception ex) {
            if (debug)
                logger.info("Exception Occurred", ex);
            if (debug)
                logger.error("Cannot parse query: " + q);
        }
        return query;
    }
}
