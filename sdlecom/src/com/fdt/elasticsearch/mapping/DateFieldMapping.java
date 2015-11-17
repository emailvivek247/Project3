package com.fdt.elasticsearch.mapping;

import java.util.Optional;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class DateFieldMapping extends AbstractMapping {

    private final String type = "date";

    private final Optional<Float> boost;
    private final Optional<String> index;
    private final Optional<Boolean> store;
    private final Optional<String> format;

    protected DateFieldMapping(Builder builder) {
        this.field = builder.field;
        this.boost = builder.boost;
        this.index = builder.index;
        this.store = builder.store;
        this.format = builder.format;
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
        if (format.isPresent()) {
            innerNode.put("format", format.get());
        }

        return innerNode;
    }
    
    public static class Builder {

        private String field;

        private Optional<Float> boost;
        private Optional<String> index;
        private Optional<Boolean> store;
        private Optional<String> format;

        public Builder(String field) {
            this.field = field;
            this.boost = Optional.empty();
            this.index = Optional.empty();
            this.store = Optional.empty();
            this.format = Optional.empty();
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

        public Builder withFormat(String format) {
            this.format = Optional.of(format);
            return this;
        }

        public DateFieldMapping build() {
            return new DateFieldMapping(this);
        }
    }
}
