package com.fdt.elasticsearch.analyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CustomAnalyzer extends AbstractAnalyzer {

    private final String type = "custom";

    private final String tokenizerName;
    private final List<String> tokenFilterNames;
    private final List<String> charFilterNames;

    protected CustomAnalyzer(Builder builder) {
        super(builder.name);
        this.tokenizerName = builder.tokenizerName;
        this.tokenFilterNames = builder.tokenFilterNames;
        this.charFilterNames = builder.charFilterNames;
    }

    @Override
    public ObjectNode getAsJsonObject() {

        ObjectNode innerNode = mapper.createObjectNode();
        innerNode.put("type", type);
        innerNode.put("tokenizer", tokenizerName);

        ArrayNode filterArrayNode = innerNode.putArray("filter");
        tokenFilterNames.forEach(f -> filterArrayNode.add(f));

        ArrayNode charFilterArrayNode = innerNode.putArray("char_filter");
        charFilterNames.forEach(f -> charFilterArrayNode.add(f));

        return innerNode;
    }

    public static class Builder {

        private String name;
        private String tokenizerName;
        private List<String> tokenFilterNames;
        private List<String> charFilterNames;

        public Builder(String name) {
            this.name = name;
            this.tokenFilterNames = new ArrayList<>();
            this.charFilterNames = new ArrayList<>();
        }

        public Builder withTokenizerName(String tokenizerName) {
            this.tokenizerName = tokenizerName;
            return this;
        }

        public Builder addTokenFilterName(String tokenFilterName) {
            this.tokenFilterNames.add(tokenFilterName);
            return this;
        }

        public Builder addTokenFilterName(Collection<String> tokenFilterNames) {
            this.tokenFilterNames.addAll(tokenFilterNames);
            return this;
        }

        public Builder addCharFilterName(String charFilterName) {
            this.charFilterNames.add(charFilterName);
            return this;
        }

        public Builder addCharFilterName(Collection<String> charFilterNames) {
            this.charFilterNames.addAll(charFilterNames);
            return this;
        }

        public CustomAnalyzer build() {
            return new CustomAnalyzer(this);
        }
    }
}
