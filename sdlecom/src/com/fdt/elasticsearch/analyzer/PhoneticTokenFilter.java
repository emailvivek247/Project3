package com.fdt.elasticsearch.analyzer;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class PhoneticTokenFilter extends AbstractTokenFilter {

    private final String type = "phonetic";

    private final String encoder;

    protected PhoneticTokenFilter(Builder builder) {
        super(builder.name);
        this.encoder = builder.encoder;
    }

    public ObjectNode getAsJsonObject() {

        ObjectNode innerNode = mapper.createObjectNode();
        innerNode.put("type", type);
        innerNode.put("encoder", encoder);

        return innerNode;
    }

    public static class Builder {

        private String name;
        private String encoder;

        public Builder(String name) {
            this.name = name;
        }

        public Builder withEncoder(String encoder) {
            this.encoder = encoder;
            return this;
        }

        public PhoneticTokenFilter build() {
            return new PhoneticTokenFilter(this);
        }
    }
}
