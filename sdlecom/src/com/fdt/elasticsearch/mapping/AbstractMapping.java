package com.fdt.elasticsearch.mapping;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.IndexFieldType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fdt.elasticsearch.analyzer.AbstractAnalyzer;

public abstract class AbstractMapping {

    protected final ObjectMapper mapper = new ObjectMapper();

    protected String field;

    public abstract ObjectNode getAsJsonObject();

    public String getField() {
        return field;
    }

    public String getAsString() {
        return writeToString(getAsJsonObject());
    }

    public String getAsStringPrettyPrint() {
        return writeToString(getAsJsonObject(), true);
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

    public static AbstractMapping fromColumn(DatasetConfiguration datasetConfiguration, Column column) {

        
        AbstractMapping result = null;
        String defaultAnalyzerName = datasetConfiguration.getAnalyzerName();

        switch (column.getIndexFieldType()) {
        case IndexFieldType.TEXT:
        case IndexFieldType.TEXT_COMPRESSED:
        case IndexFieldType.KEYWORD_CASE_INSENSITIVE:
        case IndexFieldType.UN_STORED:
            result = new StringFieldMapping
                    .Builder(column.getColumnName())
                    .withStore(false)
                    .withIndex("analyzed")
                    .withAnalyzer(AbstractAnalyzer.fromColumn(defaultAnalyzerName, column))
                    .build();
            break;
        case IndexFieldType.UN_INDEXED:
        case IndexFieldType.UN_INDEXED_COMPRESSED:
            result = new StringFieldMapping
                    .Builder(column.getColumnName())
                    .withStore(false)
                    .withIndex("no")
                    .build();
            break;
        case IndexFieldType.KEYWORD:
        case IndexFieldType.KEYWORDS:
        case IndexFieldType.KEYWORD_BOOST:
        case IndexFieldType.BOOST:
            if (column.getIsNumber()) {
                String type = null;
                if (column.getColumnScale() == 0) {
                    type= NumericFieldMapping.Type.LONG.toString();
                } else {
                    type= NumericFieldMapping.Type.DOUBLE.toString();
                }
                result = new NumericFieldMapping
                        .Builder(type, column.getColumnName())
                        .withStore(false)
                        .withIndex("not_analyzed")
                        .build();
            } else {
                result = new StringFieldMapping
                        .Builder(column.getColumnName())
                        .withStore(false)
                        .withIndex("not_analyzed")
                        .build();
            }
            break;
        case IndexFieldType.KEYWORD_DATE_HIERARCHICAL:
            result = new DateFieldMapping
                    .Builder(column.getColumnName())
                    .withStore(false)
                    .withIndex("not_analyzed")
                    .build();
            break;
        }
        return result;
    }
}
