package com.fdt.elasticsearch.query;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class MatchAllQuery extends AbstractQuery {

    protected MatchAllQuery(Builder builder) {
        super(builder);
    }

    @Override
    public ObjectNode getQueryObjectNode() {

        ObjectNode matchAllNode = mapper.createObjectNode();
        if (boost.isPresent()) {
            matchAllNode.put("boost", boost.get());
        }

        ObjectNode queryNode = mapper.createObjectNode();
        queryNode.set("match_all", matchAllNode);

        return queryNode;
    }

    public static class Builder extends AbstractQuery.Builder<MatchAllQuery, Builder>  {
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
