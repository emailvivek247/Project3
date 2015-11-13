package com.fdt.elasticsearch.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MultiMatchQuery extends AbstractQuery {

    private final List<String> fields;
    private final String query;

    private final Optional<Float> boost;
    private final Optional<String> operator;
    private final Optional<String> type;

    protected MultiMatchQuery(Builder builder) {
        this.fields = builder.fields;
        this.query = builder.query;
        this.boost = builder.boost;
        this.operator = builder.operator;
        this.type = builder.type;
    }

    @Override
    public ObjectNode getAsJsonObject() {

        ObjectNode multiMatchNode = mapper.createObjectNode();

        ArrayNode fieldsArrayNode = multiMatchNode.putArray("fields");
        fields.stream().forEach(f -> fieldsArrayNode.add(f));

        multiMatchNode.put("query", query);

        if (boost.isPresent()) {
            multiMatchNode.put("boost", boost.get());
        }
        if (operator.isPresent()) {
            multiMatchNode.put("operator", operator.get());
        }
        if (type.isPresent()) {
            multiMatchNode.put("type", type.get());
        }

        ObjectNode queryNode = mapper.createObjectNode();
        queryNode.set("multi_match", multiMatchNode);

        return queryNode;
    }

    public static class Builder {

        private List<String> fields;
        private String query;

        private Optional<Float> boost;
        private Optional<String> operator;
        private Optional<String> type;

        public Builder() {
            fields = new ArrayList<>();
            boost = Optional.empty();
            operator = Optional.empty();
            type = Optional.empty();
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

        public Builder withType(String type) {
            this.type = Optional.of(type);
            return this;
        }

        public Builder withOperator(String operator) {
            this.operator = Optional.of(operator);
            return this;
        }

        public MultiMatchQuery build() {
            return new MultiMatchQuery(this);
        }
    }

    public static void main(String[] args) {

        MultiMatchQuery query = new MultiMatchQuery.Builder()
                .addField("testField")
                .addField("testField2")
                .withQuery("test query")
                .build();
        System.out.println(query.getAsStringPrettyPrint());

        query = new MultiMatchQuery.Builder()
                .addField("testField")
                .addField("testField2")
                .withQuery("test query")
                .withBoost(3F)
                .withOperator("and")
                .withType("phrase")
                .build();
        System.out.println(query.getAsStringPrettyPrint());

        query = new MultiMatchQuery.Builder()
                .addField("*")
                .withQuery("test query")
                .build();
        System.out.println(query.getAsStringPrettyPrint());
    }
}
