package com.fdt.elasticsearch.mapping;

import java.util.Optional;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class StringFieldMapping extends AbstractMapping {

    private final String type = "string";

    private final Optional<Float> boost;
    private final Optional<String> index;
    private final Optional<Boolean> store;

    protected StringFieldMapping(Builder builder) {
        this.field = builder.field;
        this.boost = builder.boost;
        this.index = builder.index;
        this.store = builder.store;
    }

    @Override
    public ObjectNode getAsJsonObject() {

        ObjectNode innerNode = mapper.createObjectNode();
        innerNode.put("type", type);
        if (boost.isPresent()) {
            innerNode.put("boost", boost.get());
        }
        if (index.isPresent()) {
            innerNode.put("index", index.get());
        }
        if (store.isPresent()) {
            innerNode.put("store", store.get());
        }

        return innerNode;
    }

    public static class Builder {

        private String field;

        private Optional<Float> boost;
        private Optional<String> index;
        private Optional<Boolean> store;

        public Builder(String field) {
            this.field = field;
            this.boost = Optional.empty();
            this.index = Optional.empty();
            this.store = Optional.empty();
        }

        public Builder withBoost(Float boost) {
            this.boost = Optional.of(boost);
            return this;
        }

        public Builder withIndex(String index) {
            this.index = Optional.of(index);
            return this;
        }

        public Builder withStore(Boolean store) {
            this.store = Optional.of(store);
            return this;
        }

        public StringFieldMapping build() {
            return new StringFieldMapping(this);
        }
    }
}
