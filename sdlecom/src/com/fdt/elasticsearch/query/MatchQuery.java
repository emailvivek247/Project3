package com.fdt.elasticsearch.query;

import java.util.Optional;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class MatchQuery extends AbstractQuery {

    private final String field;
    private final String query;

    private final Optional<String> operator;
    private final Optional<String> type;
    private final Optional<Integer> slop;

    protected MatchQuery(Builder builder) {
        super(builder);
        this.field = builder.field;
        this.query = builder.query;
        this.operator = builder.operator;
        this.type = builder.type;
        this.slop = builder.slop;
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
        if (slop.isPresent()) {
            innerNode.put("slop", slop.get());
        }

        ObjectNode matchNode = mapper.createObjectNode();
        matchNode.set(field, innerNode);

        ObjectNode queryNode = mapper.createObjectNode();
        queryNode.set("match", matchNode);

        return queryNode;
    }

    public static class Builder extends AbstractQuery.Builder<MatchQuery, Builder>  {

        private String field;
        private String query;

        private Optional<String> operator;
        private Optional<String> type;
        private Optional<Integer> slop;

        public Builder(String field, String query) {
            this.field = field;
            this.query = query;
            this.operator = Optional.empty();
            this.type = Optional.empty();
            this.slop = Optional.empty();
        }

        public Builder withType(String type) {
            this.type = Optional.of(type);
            return this;
        }

        public Builder withOperator(String operator) {
            this.operator = Optional.of(operator);
            return this;
        }

        public Builder withSlop(Integer slop) {
            this.slop = Optional.of(slop);
            return this;
        }

        public MatchQuery build() {
            return new MatchQuery(this);
        }
    }

    public static void main(String[] args) {

        MatchQuery query = new MatchQuery.Builder("testField", "test query").build();
        System.out.println(query.getAsStringPrettyPrint());

        query = new MatchQuery.Builder("testField", "test query")
                .withBoost(3F)
                .withOperator("and")
                .withType("phrase")
                .withSlop(3)
                .build();
        System.out.println(query.getAsStringPrettyPrint());
    }
}
