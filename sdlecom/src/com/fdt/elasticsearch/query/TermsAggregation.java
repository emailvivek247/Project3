package com.fdt.elasticsearch.query;

import java.util.Optional;

import net.javacoding.xsearch.config.Column;

public class TermsAggregation {

    public final String fieldName;
    public final Boolean isDate;
    public final Optional<Integer> size;

    public TermsAggregation(String fieldName, Boolean isDate, Integer size) {
        this.fieldName = fieldName;
        this.isDate = isDate;
        this.size = Optional.of(size);
    }

    public TermsAggregation(Column column, Integer size) {
        this.fieldName = column.getColumnName();
        this.isDate = column.getIsDate();
        this.size = Optional.of(size);
    }

    public TermsAggregation(Column column) {
        this.fieldName = column.getColumnName();
        this.isDate = column.getIsDate();
        this.size = Optional.empty();
    }
}
