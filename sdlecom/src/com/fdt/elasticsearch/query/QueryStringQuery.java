package com.fdt.elasticsearch.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import net.javacoding.xsearch.search.result.SearchSort;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class QueryStringQuery extends AbstractQuery {

    private final List<String> fields;
    private final String query;

    private final Optional<String> defaultOperator;
    private final Optional<String> defaultField;
    private final Optional<String> analyzer;
    private final Optional<Integer> phraseSlop;
    private final Optional<Boolean> useDisMax;

    protected QueryStringQuery(Builder builder) {
        super(builder);
        this.fields = builder.fields;
        this.query = builder.query;
        this.defaultOperator = builder.defaultOperator;
        this.defaultField = builder.defaultField;
        this.analyzer = builder.analyzer;
        this.phraseSlop = builder.phraseSlop;
        this.useDisMax = builder.useDisMax;
    }

    @Override
    public ObjectNode getQueryObjectNode() {

        ObjectNode queryStringNode = mapper.createObjectNode();

        if (!fields.isEmpty()) {
            ArrayNode fieldsArrayNode = queryStringNode.putArray("fields");
            fields.stream().forEach(f -> fieldsArrayNode.add(f));
        }

        queryStringNode.put("query", query);

        if (boost.isPresent()) {
            queryStringNode.put("boost", boost.get());
        }
        if (defaultOperator.isPresent()) {
            queryStringNode.put("default_operator", defaultOperator.get());
        }
        if (defaultField.isPresent()) {
            queryStringNode.put("default_field", defaultField.get());
        }
        if (analyzer.isPresent()) {
            queryStringNode.put("analyzer", analyzer.get());
        }
        if (phraseSlop.isPresent()) {
            queryStringNode.put("phrase_slop", phraseSlop.get());
        }
        if (useDisMax.isPresent()) {
            queryStringNode.put("use_dis_max", useDisMax.get());
        }

        ObjectNode queryNode = mapper.createObjectNode();
        queryNode.set("query_string", queryStringNode);

        return queryNode;
    }

    public static class Builder extends AbstractQueryBuilder<QueryStringQuery, Builder> {

        private List<String> fields;
        private String query;

        private Optional<String> defaultOperator;
        private Optional<String> defaultField;
        private Optional<String> analyzer;
        private Optional<Integer> phraseSlop;
        private Optional<Boolean> useDisMax;

        public Builder(String query) {
            this.query = query;
            this.fields = new ArrayList<>();
            this.defaultOperator = Optional.empty();
            this.defaultField = Optional.empty();
            this.analyzer = Optional.empty();
            this.phraseSlop = Optional.empty();
            this.useDisMax = Optional.empty();
        }

        public Builder addField(String field) {
            this.fields.add(field);
            return this;
        }

        public Builder addField(Collection<String> fields) {
            this.fields.addAll(fields);
            return this;
        }

        public Builder withDefaultOperator(String defaultOperator) {
            this.defaultOperator = Optional.of(defaultOperator);
            return this;
        }

        public Builder withDefaultField(String defaultField) {
            this.defaultField = Optional.of(defaultField);
            return this;
        }

        public Builder withAnalyzer(String analyzer) {
            this.analyzer = Optional.of(analyzer);
            return this;
        }

        public Builder withPhraseSlop(int phraseSlop) {
            this.phraseSlop = Optional.of(phraseSlop);
            return this;
        }

        public Builder withUseDisMax(boolean useDisMax) {
            this.useDisMax = Optional.of(useDisMax);
            return this;
        }

        public QueryStringQuery build() {
            return new QueryStringQuery(this);
        }
    }

    public static void main(String[] args) {

        QueryStringQuery query = new QueryStringQuery.Builder("test query")
                .addField("testField")
                .addField("testField2")
                .build();
        System.out.println(query.getAsStringPrettyPrint());

        query = new QueryStringQuery.Builder("testField:test*query*").build();
        System.out.println(query.getAsStringPrettyPrint());

        query = new QueryStringQuery.Builder("testField:test*query*")
                .addSort(new SearchSort("testField", true))
                .addSort(new SearchSort("testField2", false))
                .build();
        System.out.println(query.getAsStringPrettyPrint());

    }
}
