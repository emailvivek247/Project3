package com.fdt.elasticsearch.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.search.SearchQueryParser;
import net.javacoding.xsearch.search.result.filter.FilteredColumn;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.elasticsearch.parsing.ESQuery.PartType;
import com.fdt.elasticsearch.parsing.ESQuery.QueryPart;
import com.fdt.elasticsearch.query.AbstractQuery;
import com.fdt.elasticsearch.query.BoolQuery;
import com.fdt.elasticsearch.query.MatchAllQuery;
import com.fdt.elasticsearch.query.QueryStringQuery;
import com.fdt.elasticsearch.util.StopwordLoader;
import com.google.common.base.Joiner;

public class ESQueryHelper {

    private static final Logger logger = LoggerFactory.getLogger(ESQueryHelper.class);

    private DatasetConfiguration dc;
    private String q;
    private String lq;
    private String searchableColsStr;
    private boolean forceLucene;

    private ESColumnHelper columnHelper;
    private ESParser parser;
    private ESQuery query;
    private BoolQuery.Builder builder;

    public ESQueryHelper(DatasetConfiguration dc, String q, String lq, String searchableColsStr,
            boolean forceLucene) {
        this.dc = dc;
        this.q = q;
        this.lq = lq;
        this.searchableColsStr = searchableColsStr;
        this.forceLucene = forceLucene;
        evaluate();
    }

    private BoolQuery.Builder evaluate() {

        columnHelper = new ESColumnHelper(dc.getColumns(), searchableColsStr);
        parser = new ESParser();
        builder = new BoolQuery.Builder();

        if (forceLucene) {
            logger.info("Adding Lucene query portion to final query");
            String queryStr = SearchQueryParser.elasticsearchParse(q);
            builder.addMustClause(new QueryStringQuery.Builder(queryStr).build());
        } else {
            if (U.isEmpty(q) && dc.getIsEmptyQueryMatchAll() && U.isEmpty(lq)) {
                logger.info("Adding a match all query portion to final query");
                builder.addMustClause(new MatchAllQuery.Builder().build());
            }
            if (!U.isEmpty(q)) {
                logger.info("Adding 'q' query portion to final query");
                query = parser.parse(q);
                builder.addMustClause(getFinalQueryStr());
            }
            if (!U.isEmpty(lq)) {
                logger.info("Adding Lucene query portion to final query");
                String queryStr = SearchQueryParser.elasticsearchParse(lq);
                builder.addMustClause(new QueryStringQuery.Builder(queryStr).build());
            }
        }

        return builder;
    }

    public BoolQuery.Builder getSearchQuery() {
        return builder;
    }

    public List<FilteredColumn> getFilteredColumns() {
        List<FilteredColumn> result = new ArrayList<>();
        if (query != null) {
            List<QueryPart> fieldParts = query.getFieldParts();
            for (QueryPart part : fieldParts) {
                String field = part.field;
                String value = part.part;
                Optional<Column> matchingColumn = dc.getColumns().stream().filter(
                        c -> c.getColumnName().equals(field)
                ).findFirst();
                if (matchingColumn.isPresent()) {
                    if (matchingColumn.get().getIsDate()) {
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        }
                    }
                }
                
                result.add(new FilteredColumn(dc, field, value));
            }
        }
        return result;
    }

    public String getUserInput() {
        if (query != null) {
            return query.getNonFieldPartsStr();
        } else {
            return "";
        }
    }

    private AbstractQuery getFinalQueryStr() {

        BoolQuery.Builder result = new BoolQuery.Builder();

        List<Column> searchCols = columnHelper.getSearchCols();

        List<String> individualFields = searchCols.stream().map((c) -> {
            String field = c.getColumnName();
            float boost = c.getSearchWeight();
            if (boost != 1.0F) {
                field += "^" + boost;
            }
            return field;
        }).collect(Collectors.toList());

        List<String> phraseFields = searchCols.stream().map((c) -> {
            String field = c.getColumnName();
            float boost = c.getIsKeyword() ? c.getSearchWeight() : c.getSearchWeight() * 10F;
            if (boost != 1.0F) {
                field += "^" + boost;
            }
            return field;
        }).collect(Collectors.toList());


        String normalQueryStr = buildQueryString(query.parts);
        String phraseQueryStr = buildPhraseQueryString(query.parts);

        QueryStringQuery individual = new QueryStringQuery.Builder(normalQueryStr)
                .addField(individualFields)
                .withUseDisMax(true)
                .build();

        QueryStringQuery phrase = null;
        if (phraseQueryStr != null) {
            phrase = new QueryStringQuery.Builder(phraseQueryStr)
                .addField(phraseFields)
                .withPhraseSlop(2)
                .withUseDisMax(true)
                .build();
        }

        return result.addShouldClause(individual).addShouldClause(phrase).build();

    }

    private String buildQueryString(List<QueryPart> queryParts) {

        Set<String> stopwords = StopwordLoader.getStopwordSet();

        List<String> parts = new ArrayList<>();
        for (QueryPart part : queryParts) {
            boolean isDate = false;
            if (part.field != null) {
                Optional<Column> matchingColumn = dc.getColumns().stream().filter(
                        c -> c.getColumnName().equals(part.field)
                ).findFirst();
                if (matchingColumn.isPresent() && matchingColumn.get().getIsDate()) {
                    isDate = true;
                }
            }
            if (part.partType == PartType.NORMAL && !stopwords.contains(part.part)) {
                if (isDate) {
                    parts.add("+" + part.getAsDateRangeQueryStr());
                } else {
                    parts.add("+" + part.getAsQueryStr());
                }
            } else {
                if (isDate) {
                    parts.add(part.getAsDateRangeQueryStr());
                } else {
                    parts.add(part.getAsQueryStr());
                }
            }
        }

        return Joiner.on(" ").join(parts);
    }

    private String buildPhraseQueryString(List<QueryPart> queryParts) {
        if (queryParts.stream().anyMatch(p -> p.isPhrase)) {
            return null;
        } else {
            String phrase = queryParts.stream()
                    .filter(p -> p.field == null)
                    .map(p -> p.part)
                    .collect(Collectors.joining(" "));
            return "\"" + phrase + "\"";
        }
    }
}
