package com.fdt.elasticsearch.mapping;

import java.util.Optional;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class NumericFieldMapping extends AbstractMapping {

    private final String type;

    private final Optional<Float> boost;
    private final Optional<String> index;
    private final Optional<Boolean> store;

    protected NumericFieldMapping(Builder builder) {
        this.field = builder.field;
        this.type = builder.type;
        this.boost = builder.boost;
        this.index = builder.index;
        this.store = builder.store;
    }

    @Override
    public ObjectNode getAsJsonObject() {

        ObjectNode innerNode = mapper.createObjectNode();
        innerNode.put("type", "string");
        if (boost.isPresent()) {
            innerNode.put("boost", boost.get());
        }
        if (index.isPresent()) {
            innerNode.put("index", index.get());
        }
        if (store.isPresent()) {
            innerNode.put("store", store.get());
        }

        ObjectNode numberNode = mapper.createObjectNode();
        numberNode.put("type", type);
        if (boost.isPresent()) {
            numberNode.put("boost", boost.get());
        }
        if (index.isPresent()) {
            numberNode.put("index", index.get());
        }
        if (store.isPresent()) {
            numberNode.put("store", store.get());
        }

        ObjectNode fieldsNode = mapper.createObjectNode();
        fieldsNode.set("number", numberNode);

        innerNode.set("fields", fieldsNode);

        return innerNode;
    }

    public static class Builder {

        private String type;
        private String field;

        private Optional<Float> boost;
        private Optional<String> index;
        private Optional<Boolean> store;

        public Builder(String type, String field) {
            this.type = type;
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

        public NumericFieldMapping build() {
            return new NumericFieldMapping(this);
        }

    }

    public static enum Type {
        LONG, INT, SHORT, BYTE, DOUBLE, FLOAT;
        public String toString() {
            return super.toString().toLowerCase();
        };
    }

}
