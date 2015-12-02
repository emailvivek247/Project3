package com.fdt.elasticsearch.query;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class WildcardQuery extends AbstractQuery {

    private final String field;
    private final String value;

    protected WildcardQuery(Builder builder) {
        super(builder);
        this.field = builder.field;
        this.value = builder.value;
    }

    @Override
    public ObjectNode getQueryObjectNode() {

        ObjectNode innerNode = mapper.createObjectNode();
        innerNode.put("value", value);
        if (boost.isPresent()) {
            innerNode.put("boost", boost.get());
        }

        ObjectNode wildcardNode = mapper.createObjectNode();
        wildcardNode.set(field, innerNode);

        ObjectNode queryNode = mapper.createObjectNode();
        queryNode.set("wildcard", wildcardNode);

        return queryNode;
    }

    public static class Builder extends AbstractQueryBuilder<WildcardQuery, Builder>  {

        private String field;
        private String value;

        public Builder withField(String field) {
            this.field = field;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public WildcardQuery build() {
            return new WildcardQuery(this);
        }
    }

    public static void main(String[] args) {

        WildcardQuery query = new WildcardQuery.Builder()
                .withField("testField")
                .withValue("test*value**")
                .build();
        System.out.println(query.getAsStringPrettyPrint());

        query = new WildcardQuery.Builder()
                .withField("testField")
                .withValue("test?value")
                .withBoost(3F)
                .build();
        System.out.println(query.getAsStringPrettyPrint());
    }
}
