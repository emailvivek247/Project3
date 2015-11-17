package com.fdt.elasticsearch.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class RootMapping extends AbstractMapping {

    private final List<AbstractMapping> mappings;

    public RootMapping(Builder builder) {
        this.mappings = builder.mappings;
    }

    @Override
    public ObjectNode getAsJsonObject() {

        ObjectNode propertiesNode = mapper.createObjectNode();

        mappings.stream().forEach(m -> propertiesNode.set(m.getField(), m.getAsJsonObject()));

        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.set("properties", propertiesNode);

        return rootNode;
    }

    public static class Builder {

        private List<AbstractMapping> mappings;

        public Builder() {
            mappings = new ArrayList<>();
        }

        public Builder addMapping(AbstractMapping mapping) {
            this.mappings.add(mapping);
            return this;
        }

        public Builder addMapping(Collection<AbstractMapping> mappings) {
            this.mappings.addAll(mappings);
            return this;
        }

        public RootMapping build() {
            return new RootMapping(this);
        }
    }

}
