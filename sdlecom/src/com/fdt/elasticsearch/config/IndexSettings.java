package com.fdt.elasticsearch.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class IndexSettings {
    
    private static final Logger logger = LoggerFactory.getLogger(IndexSettings.class);

    private final Map<String, String> indexSettingsMap;
    private ObjectNode analysisNode;

    public IndexSettings(Map<String, String> indexSettingsMap) {
        this.indexSettingsMap = indexSettingsMap;
    }

    public void setAnalysisNode(ObjectNode analysisNode) {
        this.analysisNode = analysisNode;
    }

    public String getIndexSettingsStr() {

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.set("analysis", analysisNode);
        indexSettingsMap.forEach((k, v) -> rootNode.put(k, v));

        String indexSettingsStr = null;
        try {
            if (logger.isDebugEnabled()) {
                String indexSettingsStrPP = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
                logger.debug("Generated index settings:\n{}", indexSettingsStrPP);
            }
            indexSettingsStr = mapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return indexSettingsStr;
    }
}
