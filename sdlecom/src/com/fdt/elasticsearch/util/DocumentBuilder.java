package com.fdt.elasticsearch.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DocumentBuilder {

    protected final ObjectMapper mapper = new ObjectMapper();

    private Map<String, List<String>> map;

    public DocumentBuilder() {
        map = new HashMap<>();
    }

    public void add(String field, String value) {
        if (map.containsKey(field)) {
            map.get(field).add(value);
        } else {
            map.put(field, new ArrayList<>(Arrays.asList(value)));
        }
    }

    public String buildDocument() {
        ObjectNode rootNode = mapper.createObjectNode();
        for (Entry<String, List<String>> entry : map.entrySet()) {
            if (entry.getValue().size() == 1) {
                rootNode.put(entry.getKey(), entry.getValue().get(0));
            } else if (entry.getValue().size() > 1) {
                ArrayNode arrayNode = rootNode.putArray(entry.getKey());
                entry.getValue().stream().forEach(e -> arrayNode.add(e));
            }
        }
        try {
            return mapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
