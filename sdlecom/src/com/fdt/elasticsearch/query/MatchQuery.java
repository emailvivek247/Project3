package com.fdt.elasticsearch.query;

import java.util.Optional;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class MatchQuery extends AbstractQuery {

    private final String field;
    private final String query;

    private final Optional<String> operator;
    private final Optional<String> type;

    protected MatchQuery(Builder builder) {
        super(builder);
        this.field = builder.field;
        this.query = builder.query;
        this.operator = builder.operator;
        this.type = builder.type;
    }

    @Override
    public ObjectNode getQueryObjectNode() {

        ObjectNode innerNode = mapper.createObjectNode();
        innerNode.put("query", query);
        if (boost.isPresent()) {
            innerNode.put("boost", boost.get());
        }
        if (operator.isPresent()) {
            innerNode.put("operator", operator.get());
        }
        if (type.isPresent()) {
            innerNode.put("type", type.get());
        }

        ObjectNode matchNode = mapper.createObjectNode();
        matchNode.set(field, innerNode);

        ObjectNode queryNode = mapper.createObjectNode();
        queryNode.set("match", matchNode);

        return queryNode;
    }

    public static class Builder extends AbstractQueryBuilder<MatchQuery, Builder>  {

        private String field;
        private String query;

        private Optional<String> operator;
        private Optional<String> type;

        public Builder() {
            operator = Optional.empty();
            type = Optional.empty();
        }

        public Builder withField(String field) {
            this.field = field;
            return this;
        }

        public Builder withQuery(String query) {
            this.query = query;
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

        public MatchQuery build() {
            return new MatchQuery(this);
        }
    }

    public static void main(String[] args) {

        MatchQuery query = new MatchQuery.Builder()
                .withField("testField")
                .withQuery("test query")
                .build();
        System.out.println(query.getAsStringPrettyPrint());

        query = new MatchQuery.Builder()
                .withField("testField")
                .withQuery("test query")
                .withBoost(3F)
                .withOperator("and")
                .withType("phrase")
                .build();
        System.out.println(query.getAsStringPrettyPrint());
    }
}
