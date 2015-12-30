package com.fdt.elasticsearch.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.javacoding.xsearch.search.result.SearchSort;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class AbstractQuery {

    protected final ObjectMapper mapper = new ObjectMapper();

    protected final List<SearchSort> sorts;
    protected final List<String> highlightFields;
    protected final Optional<Float> boost;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public AbstractQuery(AbstractQueryBuilder builder) {
        boost = builder.boost;
        sorts = builder.sorts;
        highlightFields = builder.highlightFields;
    }

    public abstract ObjectNode getQueryObjectNode();

    public ObjectNode getHighlightObjectNode() {

        ObjectNode fieldsNode = mapper.createObjectNode();
        for (String highlightField : highlightFields) {
            ObjectNode innerNode = mapper.createObjectNode();
            fieldsNode.set(highlightField, innerNode);
        }

        ObjectNode highlightNode = mapper.createObjectNode();
        highlightNode.set("fields", fieldsNode);
        highlightNode.put("encoder", "html");
        highlightNode.put("number_of_fragments", 0);

        ArrayNode preTagsArrayNode = mapper.createArrayNode();
        preTagsArrayNode.add("<hl-tag-replace>");
        ArrayNode postTagsArrayNode = mapper.createArrayNode();
        postTagsArrayNode.add("</hl-tag-replace>");

        highlightNode.set("pre_tags", preTagsArrayNode);
        highlightNode.set("post_tags", postTagsArrayNode);

        return highlightNode;
    }

    public ArrayNode getSortArrayNode() {

        List<ObjectNode> outerNodes = sorts.stream().map((sort) -> {
            String sortField = sort.field;
            if (sort.getColumn().getIsNumber()) {
                sortField += ".number";
            }
            ObjectNode innerNode = mapper.createObjectNode();
            innerNode.put("order", sort.descending ? "desc" : "asc");
            ObjectNode outerNode = mapper.createObjectNode();
            outerNode.set(sortField, innerNode);
            return outerNode;
        }).collect(Collectors.toList());

        ArrayNode sortNode = mapper.createArrayNode();
        sortNode.addAll(outerNodes);
        sortNode.add("_score");

        return sortNode;
    }

    public ObjectNode getAsWrappedJsonObject() {
        ObjectNode outerNode = mapper.createObjectNode();
        outerNode.set("query", getQueryObjectNode());
        outerNode.set("sort", getSortArrayNode());
        outerNode.set("highlight", getHighlightObjectNode());
        return outerNode;
    }

    public String getAsString() {
        return writeToString(getAsWrappedJsonObject());
    }

    public String getAsStringPrettyPrint() {
        return writeToString(getAsWrappedJsonObject(), true);
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

    @SuppressWarnings("unchecked")
    public abstract static class AbstractQueryBuilder<T extends AbstractQuery, K> {

        protected List<SearchSort> sorts;
        protected List<String> highlightFields;
        protected Optional<Float> boost;

        public AbstractQueryBuilder() {
            sorts = new ArrayList<>();
            highlightFields = new ArrayList<>();
            boost = Optional.empty();
        }

        public K addSort(SearchSort sort) {
            if (sort != null) {
                this.sorts.add(sort);
            }
            return (K) this;
        }

        public K addSort(Collection<SearchSort> sort) {
            if (sort != null) {
                this.sorts.addAll(sort);
            }
            return (K) this;
        }

        public K addHighlightField(String highlightField) {
            if (highlightField != null) {
                this.highlightFields.add(highlightField);
            }
            return (K) this;
        }

        public K addHighlightField(Collection<String> highlightField) {
            if (highlightField != null) {
                this.highlightFields.addAll(highlightField);
            }
            return (K) this;
        }

        public K withBoost(Float boost) {
            this.boost = Optional.of(boost);
            return (K) this;
        }

        public abstract T build();
    }
}
