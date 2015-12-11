package com.fdt.elasticsearch.analyzer;

import java.util.Optional;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class KeywordTokenizer extends AbstractTokenizer {

    private final String type = "keyword";

    private final Optional<Integer> bufferSize;

    protected KeywordTokenizer(Builder builder) {
        super(builder.name);
        this.bufferSize = builder.bufferSize;
    }

    public ObjectNode getAsJsonObject() {

        ObjectNode innerNode = mapper.createObjectNode();
        innerNode.put("type", type);
        if (bufferSize.isPresent()) {
            innerNode.put("buffer_size", bufferSize.get());
        }

        return innerNode;
    }

    public static class Builder {

        private String name;
        private Optional<Integer> bufferSize;

        public Builder(String name) {
            this.name = name;
        }

        public Builder withBufferSize(Integer bufferSize) {
            this.bufferSize = Optional.of(bufferSize);
            return this;
        }

        public KeywordTokenizer build() {
            return new KeywordTokenizer(this);
        }
    }
}
