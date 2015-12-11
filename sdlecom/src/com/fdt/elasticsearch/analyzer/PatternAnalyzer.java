package com.fdt.elasticsearch.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class PatternAnalyzer extends AbstractAnalyzer {

    private final String type = "pattern";

    private final Optional<String> pattern;
    private final Optional<Boolean> lowercase;
    private final Optional<String> stopwordLanguageSet;
    private final List<String> stopwordList;

    protected PatternAnalyzer(Builder builder) {
        super(builder.name);
        this.pattern = builder.pattern;
        this.lowercase = builder.lowercase;
        this.stopwordLanguageSet = builder.stopwordLanguageSet;
        this.stopwordList = builder.stopwordList;
    }

    @Override
    public ObjectNode getAsJsonObject() {

        ObjectNode innerNode = mapper.createObjectNode();

        innerNode.put("type", type);

        if (pattern.isPresent()) {
            innerNode.put("pattern", pattern.get());
        }
        if (lowercase.isPresent()) {
            innerNode.put("lowercase", lowercase.get());
        }

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
        private Optional<String> pattern;
        private Optional<Boolean> lowercase;
        private Optional<String> stopwordLanguageSet;
        private List<String> stopwordList;

        public Builder(String name) {
            this.name = name;
            this.pattern = Optional.empty();
            this.lowercase = Optional.empty();
            this.stopwordLanguageSet = Optional.empty();
            this.stopwordList = new ArrayList<>();
        }

        public Builder withPattern(String pattern) {
            this.pattern = Optional.of(pattern);
            return this;
        }

        public Builder withLowercase(Boolean lowercase) {
            this.lowercase = Optional.of(lowercase);
            return this;
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

        public PatternAnalyzer build() {
            return new PatternAnalyzer(this);
        }
    }
}
