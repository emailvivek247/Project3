package com.fdt.elasticsearch.query;

import java.util.Optional;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class TermQuery extends AbstractQuery {

    private final String field;
    private final String value;

    private final Optional<Float> boost;

    protected TermQuery(Builder builder) {
        this.field = builder.field;
        this.value = builder.value;
        this.boost = builder.boost;

    }

    @Override
    public ObjectNode getAsJsonObject() {

        ObjectNode innerNode = mapper.createObjectNode();
        innerNode.put("value", value);
        if (boost.isPresent()) {
            innerNode.put("boost", boost.get());
        }

        ObjectNode termNode = mapper.createObjectNode();
        termNode.set(field, innerNode);

        ObjectNode queryNode = mapper.createObjectNode();
        queryNode.set("term", termNode);

        return queryNode;
    }

    public static class Builder {

        private String field;
        private String value;

        private Optional<Float> boost;

        public Builder() {
            boost = Optional.empty();
        }

        public Builder withField(String field) {
            this.field = field;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withBoost(Float boost) {
            this.boost = Optional.of(boost);
            return this;
        }

        public TermQuery build() {
            return new TermQuery(this);
        }
    }

    public static void main(String[] args) {

        TermQuery query = new TermQuery.Builder()
                .withField("testField")
                .withValue("test value")
                .build();
        System.out.println(query.getAsStringPrettyPrint());

        query = new TermQuery.Builder()
                .withField("testField")
                .withValue("test value")
                .withBoost(3F)
                .build();
        System.out.println(query.getAsStringPrettyPrint());
    }
}
