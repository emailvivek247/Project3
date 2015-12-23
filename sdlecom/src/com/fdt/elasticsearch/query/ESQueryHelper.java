package com.fdt.elasticsearch.query;

import javax.servlet.http.HttpServletRequest;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.search.HTMLEntities;
import net.javacoding.xsearch.search.SearchQueryParser;
import net.javacoding.xsearch.search.analysis.QueryAnalysis;
import net.javacoding.xsearch.search.query.DbsQuery;
import net.javacoding.xsearch.search.result.SearchResult;
import net.javacoding.xsearch.search.result.filter.FilterResult;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ESQueryHelper {

    private static final Logger logger = LoggerFactory.getLogger(ESQueryHelper.class);

    public static BoolQuery.Builder getSearchQuery(SearchResult sr, String q, String lq, FilterResult filterResult,
            HttpServletRequest request, DatasetConfiguration dc, int booleanOperator, String dynamicSearchable,
            int randomQuerySeed, boolean debug) {

        BoolQuery.Builder builder = new BoolQuery.Builder(); 

        if (filterResult == null) {
            filterResult = new FilterResult();
        }

        try {
            if ("Y".equalsIgnoreCase(request.getParameter("lucene"))) {
                // option to switch to Lucene's RAW query parser
                String queryStr = SearchQueryParser.elasticsearchParse(q);
                builder.addMustClause(new QueryStringQuery.Builder(queryStr).build());
            } else {
                if (U.isEmpty(q) && dc.getIsEmptyQueryMatchAll() && U.isEmpty(lq)) {
                    builder.addMustClause(new MatchAllQuery.Builder().build());
                }
                /*
                 * TODO: Translate this part
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
                    builder.addMustClause(<something>);
                }
                */
                if (!U.isEmpty(q)) {
                    
                    if (debug) {
                        logger.info("Start parse query: " + HTMLEntities.encode(q));
                    }
                    DbsQuery myQuery = QueryAnalysis.parseQuery(q, dc);
                    request.setAttribute("parsedQuery", myQuery);
                    if (sr != null) {
                        sr.setUserInput(myQuery.getUserInput());
                    }
                    if (debug) {
                        logger.info("parsed query: " + HTMLEntities.encode(myQuery.toString()));
                    }
                    
                    ESQueryTranslator2 translator = new ESQueryTranslator2(dc.getColumns(), dynamicSearchable);
                    translator.setSlop(5);
                    translator.setBooleanOperator(booleanOperator);
                    builder.addMustClause(translator.translate(q, filterResult, dc));
                    if (translator.getIsAllNegative()) {
                        builder.addMustClause(new MatchAllQuery.Builder().build());
                    }
                }
                if (!U.isEmpty(lq)) {
                    String queryStr = SearchQueryParser.elasticsearchParse(lq);
                    builder.addMustClause(new QueryStringQuery.Builder(queryStr).build());
                    if (debug) {
                        logger.info("added Lucene query: " + HTMLEntities.encode(queryStr));
                    }
                }
            }
            /*
             * TODO: Translate these
             *
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
            */
        } catch (Exception ex) {
            if (debug)
                logger.info("Exception Occurred", ex);
            if (debug)
                logger.error("Cannot parse query: " + q);
        }

        return builder;
    }
}
