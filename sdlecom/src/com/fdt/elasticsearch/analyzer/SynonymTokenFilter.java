package com.fdt.elasticsearch.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SynonymTokenFilter extends AbstractTokenFilter {

    private final String type = "synonym";

    private final Optional<String> synonymsPath;
    private final List<String> synonyms;

    protected SynonymTokenFilter(Builder builder) {
        super(builder.name);
        this.synonymsPath = builder.synonymsPath;
        this.synonyms = builder.synonyms;
    }

    public ObjectNode getAsJsonObject() {

        ObjectNode innerNode = mapper.createObjectNode();
        innerNode.put("type", type);
        if (synonymsPath.isPresent()) {
            innerNode.put("synonyms_path", synonymsPath.get());
        } else if (!synonyms.isEmpty()) {
            ArrayNode synonymsArrayNode = innerNode.putArray("synonyms");
            synonyms.forEach(s -> synonymsArrayNode.add(s));
        }

        return innerNode;
    }

    public static class Builder {

        private String name;
        private Optional<String> synonymsPath;
        private List<String> synonyms;

        public Builder(String name) {
            this.name = name;
            this.synonymsPath = Optional.empty();
            this.synonyms = new ArrayList<>();
        }

        public Builder withSynonymsPath(String synonymsPath) {
            this.synonymsPath = Optional.of(synonymsPath);
            return this;
        }

        public Builder addSynonym(String synonym) {
            this.synonyms.add(synonym);
            return this;
        }

        public Builder addSynonym(List<String> synonyms) {
            this.synonyms.addAll(synonyms);
            return this;
        }

        public SynonymTokenFilter build() {
            return new SynonymTokenFilter(this);
        }
    }
}
