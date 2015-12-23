package com.fdt.elasticsearch.query;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.search.result.filter.FilterResult;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ESQueryTranslator2 {

    private static final Logger logger = LoggerFactory.getLogger(ESQueryTranslator2.class);

    private List<Column> columnList;
    private Set<String> dynamicSearchableColumns;
    private int slop = Integer.MAX_VALUE;
    private int booleanOperator = 0;
    private boolean isAllNegative = true;

    private static int AND = 1;
    private static int OR = 2;

    public ESQueryTranslator2(List<Column> columnList, String columnsString) {
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

    public void setSlop(int slop) {
        this.slop = slop;
    }

    public void setBooleanOperator(int booleanOperator) {
        this.booleanOperator = booleanOperator;
    }

    public boolean getIsAllNegative() {
        return isAllNegative;
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

        for (Column column : columnList) {

            if (!isSearchable(column) || column.getColumnType().equalsIgnoreCase("java.sql.Timestamp")) {
                continue;
            }

            AbstractQuery query = queryStringQuery(column, queryStr);
            result.addShouldClause(query);

        }

        return result.build();
    }


    private AbstractQuery queryStringQuery(Column c, String queryStr) {
        return new QueryStringQuery.Builder(queryStr)
                .withDefaultField(c.getColumnName())
                .withPhraseSlop(slop)
                .withBoost(c.getSearchWeight())
                .build();
    }

}
