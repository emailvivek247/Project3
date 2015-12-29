package com.fdt.elasticsearch.query;

import javax.servlet.http.HttpServletRequest;

import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.search.HTMLEntities;
import net.javacoding.xsearch.search.SearchQueryParser;
import net.javacoding.xsearch.search.result.SearchResult;
import net.javacoding.xsearch.search.result.filter.FilterResult;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ESQueryHelper {

    private static final Logger logger = LoggerFactory.getLogger(ESQueryHelper.class);

    public static BoolQuery.Builder getSearchQuery(SearchResult sr, String q, String lq, FilterResult filterResult,
            HttpServletRequest request, DatasetConfiguration dc, String dynamicSearchable, boolean debug) {

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
                if (!U.isEmpty(q)) {
                    if (sr != null) {
                        sr.setUserInput(q);
                    }
                    ESQueryTranslator translator = new ESQueryTranslator(dc.getColumns(), dynamicSearchable);
                    builder.addMustClause(translator.translate(q, filterResult, dc));
                }
                if (!U.isEmpty(lq)) {
                    String queryStr = SearchQueryParser.elasticsearchParse(lq);
                    builder.addMustClause(new QueryStringQuery.Builder(queryStr).build());
                    if (debug) {
                        logger.info("added Lucene query: " + HTMLEntities.encode(queryStr));
                    }
                }
            }
        } catch (Exception ex) {
            if (debug)
                logger.info("Exception Occurred", ex);
            if (debug)
                logger.error("Cannot parse query: " + q);
        }

        return builder;
    }
}
