package com.fdt.elasticsearch.config;

import net.javacoding.xsearch.config.DatasetConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fdt.elasticsearch.analyzer.AnalyzerHelper;

public class IndexSettings {

    private static final Logger logger = LoggerFactory.getLogger(IndexSettings.class);

    private final DatasetConfiguration datasetConfiguration;
    private final ObjectNode analysisNode;

    public IndexSettings(DatasetConfiguration datasetConfiguration) {
        this.datasetConfiguration = datasetConfiguration;
        this.analysisNode = AnalyzerHelper.getAnalysisNode(datasetConfiguration);
    }

    public String getIndexSettingsStr() {

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.set("analysis", analysisNode);
        rootNode.put("max_result_window", SpringContextUtil.getMaxResultWindow());
        rootNode.put("number_of_shards", datasetConfiguration.getNumberShards());
        rootNode.put("number_of_replicas", datasetConfiguration.getNumberReplicas());

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
