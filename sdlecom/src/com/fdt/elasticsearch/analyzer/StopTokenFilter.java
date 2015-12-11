package com.fdt.elasticsearch.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class StopTokenFilter extends AbstractTokenFilter {

    private final String type = "stop";

    private Optional<String> stopwordLanguageSet;
    private List<String> stopwordList;

    protected StopTokenFilter(Builder builder) {
        super(builder.name);
        this.stopwordLanguageSet = builder.stopwordLanguageSet;
        this.stopwordList = builder.stopwordList;
    }

    public ObjectNode getAsJsonObject() {

        ObjectNode innerNode = mapper.createObjectNode();
        innerNode.put("type", type);
        if (stopwordLanguageSet.isPresent()) {
            innerNode.put("stopwords", stopwordLanguageSet.get());
        } else if (!stopwordList.isEmpty()) {
            ArrayNode stopwordsArrayNode = innerNode.putArray("stopwords");
            stopwordList.forEach(s -> stopwordsArrayNode.add(s));
        }

        return innerNode;
    }

    public static class Builder {

        private String name;
        private Optional<String> stopwordLanguageSet;
        private List<String> stopwordList;

        public Builder(String name) {
            this.name = name;
            this.stopwordLanguageSet = Optional.empty();
            this.stopwordList = new ArrayList<>();
        }

        public Builder withStopwordLanguageSet(String stopwordLanguageSet) {
            this.stopwordLanguageSet = Optional.of(stopwordLanguageSet);
            return this;
        }

        public Builder addStopword(String stopword) {
            this.stopwordList.add(stopword);
            return this;
        }

        public Builder addStopword(List<String> stopwords) {
            this.stopwordList.addAll(stopwords);
            return this;
        }

        public StopTokenFilter build() {
            return new StopTokenFilter(this);
        }
    }
}
