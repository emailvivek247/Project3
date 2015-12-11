package com.fdt.elasticsearch.analyzer;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class WhitespaceAnalyzer extends AbstractAnalyzer {

    private final String type = "whitespace";

    protected WhitespaceAnalyzer(Builder builder) {
        super(builder.name);
    }

    @Override
    public ObjectNode getAsJsonObject() {
        ObjectNode innerNode = mapper.createObjectNode();
        innerNode.put("type", type);
        return innerNode;
    }

    public static class Builder {

        private String name;

        public Builder(String name) {
            this.name = name;
        }

        public WhitespaceAnalyzer build() {
            return new WhitespaceAnalyzer(this);
        }
    }
}
