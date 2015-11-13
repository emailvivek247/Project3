package com.fdt.elasticsearch.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class AbstractQuery {

    protected final ObjectMapper mapper = new ObjectMapper();

    public abstract ObjectNode getAsJsonObject();

    public ObjectNode getAsWrappedJsonObject() {
        return wrapQueryNode(getAsJsonObject());
    }

    public String getAsString() {
        return writeToString(getAsWrappedJsonObject());
    }

    public String getAsStringPrettyPrint() {
        return writeToString(getAsWrappedJsonObject(), true);
    }

    private ObjectNode wrapQueryNode(ObjectNode objectNode) {
        ObjectNode queryNode = mapper.createObjectNode();
        return (ObjectNode) queryNode.set("query", objectNode);
    }

    private String writeToString(ObjectNode objectNode) {
        try {
            return mapper.writeValueAsString(objectNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String writeToString(ObjectNode objectNode, boolean pretty) {
        if (pretty) {
            try {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            return writeToString(objectNode);
        }
    }
}
