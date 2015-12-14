package com.fdt.elasticsearch.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AnalyzerHelper {

    public static ObjectNode getAnalysisNode(DatasetConfiguration datasetConfiguration) {

        ObjectMapper mapper = new ObjectMapper();

        List<AbstractAnalyzer> analyzers = new ArrayList<>();
        List<AbstractTokenizer> tokenizers = new ArrayList<>();
        List<AbstractTokenFilter> tokenFilters = new ArrayList<>();
        List<AbstractCharFilter> charFilters = new ArrayList<>();

        String analyzerClassName = datasetConfiguration.getAnalyzerName();
        Optional<AbstractAnalyzer> defaultAnalyzer = null;
        if (analyzerClassName != null && !analyzerClassName.isEmpty()) {
            defaultAnalyzer = AbstractAnalyzer.fromAnalyzerClassName(analyzerClassName);
            if (defaultAnalyzer.isPresent()) {
                analyzers.add(defaultAnalyzer.get());
                tokenizers.addAll(defaultAnalyzer.get().getTokenizers());
                tokenFilters.addAll(defaultAnalyzer.get().getTokenFilters());
                charFilters.addAll(defaultAnalyzer.get().getCharFilters());
            }
        }

        for (Column column : datasetConfiguration.getColumns()) {
            Optional<AbstractAnalyzer> analyzer = AbstractAnalyzer.fromColumn(column);
            if (analyzer.isPresent()) {
                analyzers.add(analyzer.get());
                tokenizers.addAll(analyzer.get().getTokenizers());
                tokenFilters.addAll(analyzer.get().getTokenFilters());
                charFilters.addAll(analyzer.get().getCharFilters());
            }
        }

        ObjectNode analyzerNode = mapper.createObjectNode();
        if (defaultAnalyzer.isPresent()) {
            analyzerNode.set("default", defaultAnalyzer.get().getAsJsonObject());
        }
        analyzers.forEach(a -> analyzerNode.set(a.getName(), a.getAsJsonObject()));

        ObjectNode tokenizerNode = mapper.createObjectNode();
        tokenizers.forEach(t -> tokenizerNode.set(t.getName(), t.getAsJsonObject()));

        ObjectNode tokenFilterNode = mapper.createObjectNode();
        tokenFilters.forEach(t -> tokenFilterNode.set(t.getName(), t.getAsJsonObject()));

        ObjectNode charFilterNode = mapper.createObjectNode();
        charFilters.forEach(c -> charFilterNode.set(c.getName(), c.getAsJsonObject()));

        ObjectNode analysisObjectNode = mapper.createObjectNode();
        analysisObjectNode.set("analyzer", analyzerNode);
        analysisObjectNode.set("tokenizer", tokenizerNode);
        analysisObjectNode.set("filter", tokenFilterNode);
        analysisObjectNode.set("char_filter", charFilterNode);

        return analysisObjectNode;
    }
}
