package com.fdt.elasticsearch.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.search.result.filter.FilterResult;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdt.elasticsearch.util.StopwordLoader;
import com.google.common.base.Joiner;

public class ESQueryTranslator {

    private static final Logger logger = LoggerFactory.getLogger(ESQueryTranslator.class);

    private List<Column> columnList;
    private Set<String> dynamicSearchableColumns;

    public ESQueryTranslator(List<Column> columnList, String columnsString) {
        this.columnList = columnList;
        if(!U.isEmpty(columnsString)) {
            dynamicSearchableColumns = new HashSet<String>();
            String[] list = columnsString.toLowerCase().split(",");
            for(String c : list) {
                dynamicSearchableColumns.add(c);
            }
        }
    }

    private boolean isSearchable(Column column) {
        return dynamicSearchableColumns == null && column.getIsSearchable() || dynamicSearchableColumns != null
                && dynamicSearchableColumns.contains(column.getColumnName().toLowerCase());
    }

    public AbstractQuery translate(String queryStr, FilterResult filterResult, DatasetConfiguration dc) {
        if (columnList != null) {
            return processClauses(queryStr, filterResult);
        } else {
            return new BoolQuery.Builder().build();
        }
    }

    private AbstractQuery processClauses(String queryStr, FilterResult filterResult) {

        BoolQuery.Builder result = new BoolQuery.Builder();

        List<Column> searchCols = columnList.stream()
            .filter(c -> isSearchable(c))
            .filter(c -> !c.getColumnType().equalsIgnoreCase("java.sql.Timestamp"))
            .collect(Collectors.toList());

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

        String phraseQueryStr = buildPhraseQueryString(queryStr);

        if (!queryStr.contains("\"")) {
            queryStr = buildQueryString(queryStr);
        }

        QueryStringQuery individual = new QueryStringQuery.Builder(queryStr)
                .addField(individualFields)
                .withUseDisMax(true)
                .build();

        QueryStringQuery phrase = new QueryStringQuery.Builder(phraseQueryStr)
                .addField(phraseFields)
                .withPhraseSlop(2)
                .withUseDisMax(true)
                .build();

        return result.addShouldClause(individual).addShouldClause(phrase).build();

    }

    private static String buildQueryString(String queryStr) {

        Set<String> stopwords = StopwordLoader.getStopwordSet();
        String[] split = queryStr.split("\\s+");

        List<String> parts = new ArrayList<>();
        for (String part : split) {
            if (stopwords.contains(part)) {
                parts.add(part);
            } else {
                parts.add("+" + part);
            }
        }

        return Joiner.on(" ").join(parts);
    }

    private static String buildPhraseQueryString(String queryStr) {
        String phraseQueryStr = queryStr;
        if (!phraseQueryStr.startsWith("\"")) {
            phraseQueryStr = "\"" + phraseQueryStr;
        }
        if (!phraseQueryStr.endsWith("\"")) {
            phraseQueryStr = phraseQueryStr + "\"";
        }
        return phraseQueryStr;
    }
}
