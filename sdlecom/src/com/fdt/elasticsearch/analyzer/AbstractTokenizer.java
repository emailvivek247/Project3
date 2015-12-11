package com.fdt.elasticsearch.analyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class AbstractTokenizer {

    protected final ObjectMapper mapper = new ObjectMapper();

    protected final String name;

    public AbstractTokenizer(String name) {
        this.name = name;
    }

    public abstract ObjectNode getAsJsonObject();

    public String getName() {
        return name;
    }
}
