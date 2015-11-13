package com.fdt.elasticsearch.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class QueryStringQuery extends AbstractQuery {

    private final List<String> fields;
    private final String query;

    private final Optional<Float> boost;
    private final Optional<String> defaultOperator;
    private final Optional<String> defaultField;

    protected QueryStringQuery(Builder builder) {
        this.fields = builder.fields;
        this.query = builder.query;
        this.boost = builder.boost;
        this.defaultOperator = builder.defaultOperator;
        this.defaultField = builder.defaultField;
    }

    @Override
    public ObjectNode getAsJsonObject() {

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

        ObjectNode queryNode = mapper.createObjectNode();
        queryNode.set("query_string", queryStringNode);

        return queryNode;
    }

    public static class Builder {

        private List<String> fields;
        private String query;

        private Optional<Float> boost;
        private Optional<String> defaultOperator;
        private Optional<String> defaultField;

        public Builder() {
            fields = new ArrayList<>();
            boost = Optional.empty();
            defaultOperator = Optional.empty();
            defaultField = Optional.empty();
        }

        public Builder addField(String field) {
            this.fields.add(field);
            return this;
        }

        public Builder addField(Collection<String> fields) {
            this.fields.addAll(fields);
            return this;
        }

        public Builder withQuery(String query) {
            this.query = query;
            return this;
        }

        public Builder withBoost(Float boost) {
            this.boost = Optional.of(boost);
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

        public QueryStringQuery build() {
            return new QueryStringQuery(this);
        }
    }

    public static void main(String[] args) {

        QueryStringQuery query = new QueryStringQuery.Builder()
                .addField("testField")
                .addField("testField2")
                .withQuery("test query")
                .build();
        System.out.println(query.getAsStringPrettyPrint());

        query = new QueryStringQuery.Builder()
                .addField("testField")
                .addField("testField2")
                .withQuery("test*query*")
                .build();
        System.out.println(query.getAsStringPrettyPrint());

    }
}
