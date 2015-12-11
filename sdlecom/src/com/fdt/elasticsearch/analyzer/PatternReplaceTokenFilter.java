package com.fdt.elasticsearch.analyzer;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class PatternReplaceTokenFilter extends AbstractTokenFilter {

    private final String type = "pattern_replace";

    private final String pattern;
    private final String replacement;

    protected PatternReplaceTokenFilter(Builder builder) {
        super(builder.name);
        this.pattern = builder.pattern;
        this.replacement = builder.replacement;
    }

    public ObjectNode getAsJsonObject() {

        ObjectNode innerNode = mapper.createObjectNode();
        innerNode.put("type", type);
        innerNode.put("pattern", pattern);
        innerNode.put("replacement", replacement);

        return innerNode;
    }

    public static class Builder {

        private String name;
        private String pattern;
        private String replacement;

        public Builder(String name) {
            this.name = name;
        }

        public Builder withPattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder withReplacement(String replacement) {
            this.replacement = replacement;
            return this;
        }

        public PatternReplaceTokenFilter build() {
            return new PatternReplaceTokenFilter(this);
        }
    }
}
