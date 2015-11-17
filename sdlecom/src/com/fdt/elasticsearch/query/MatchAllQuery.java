package com.fdt.elasticsearch.query;

import java.util.Optional;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class MatchAllQuery extends AbstractQuery {

    private final Optional<Float> boost;

    protected MatchAllQuery(Builder builder) {
        this.boost = builder.boost;
    }

    @Override
    public ObjectNode getAsJsonObject() {

        ObjectNode matchAllNode = mapper.createObjectNode();
        if (boost.isPresent()) {
            matchAllNode.put("boost", boost.get());
        }

        ObjectNode queryNode = mapper.createObjectNode();
        queryNode.set("match_all", matchAllNode);

        return queryNode;
    }

    public static class Builder {

        private Optional<Float> boost;

        public Builder() {
            boost = Optional.empty();
        }

        public Builder withBoost(Float boost) {
            this.boost = Optional.of(boost);
            return this;
        }

        public MatchAllQuery build() {
            return new MatchAllQuery(this);
        }
    }

    public static void main(String[] args) {

        MatchAllQuery query = new MatchAllQuery.Builder().build();
        System.out.println(query.getAsStringPrettyPrint());

        query = new MatchAllQuery.Builder().withBoost(3F).build();
        System.out.println(query.getAsStringPrettyPrint());
    }

}
