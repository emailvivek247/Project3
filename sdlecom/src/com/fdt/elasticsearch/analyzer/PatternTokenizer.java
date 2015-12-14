package com.fdt.elasticsearch.analyzer;

import java.util.Optional;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class PatternTokenizer extends AbstractTokenizer {

    private final String type = "pattern";

    private final Optional<String> pattern;

    protected PatternTokenizer(Builder builder) {
        super(builder.name);
        this.pattern = builder.pattern;
    }

    public ObjectNode getAsJsonObject() {

        ObjectNode innerNode = mapper.createObjectNode();
        innerNode.put("type", type);
        if (pattern.isPresent()) {
            innerNode.put("pattern", pattern.get());
        }

        return innerNode;
    }

    public static class Builder {

        private String name;
        private Optional<String> pattern;

        public Builder(String name) {
            this.name = name;
        }

        public Builder withPattern(String pattern) {
            this.pattern = Optional.of(pattern);
            return this;
        }

        public PatternTokenizer build() {
            return new PatternTokenizer(this);
        }
    }
}
