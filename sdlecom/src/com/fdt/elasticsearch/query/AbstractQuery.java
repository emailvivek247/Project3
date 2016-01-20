package com.fdt.elasticsearch.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.search.result.SearchSort;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class AbstractQuery {

    protected final ObjectMapper mapper = new ObjectMapper();

    protected final List<SearchSort> sorts;
    protected final List<String> highlightFields;
    protected final List<TermsAggregation> termsAggregations;
    protected final Optional<Float> boost;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public AbstractQuery(Builder builder) {
        boost = builder.boost;
        sorts = builder.sorts;
        highlightFields = builder.highlightFields;
        termsAggregations = builder.termsAggregations;
    }

    public abstract ObjectNode getQueryObjectNode();

    public ArrayNode getSortArrayNode() {

        List<ObjectNode> outerNodes = sorts.stream().map((sort) -> {
            String sortField = sort.field;
            if (sort.getColumn() != null && sort.getColumn().getIsNumber()) {
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
        highlightNode.put("require_field_match", true);

        ArrayNode preTagsArrayNode = mapper.createArrayNode();
        preTagsArrayNode.add("<hl-tag-replace>");
        ArrayNode postTagsArrayNode = mapper.createArrayNode();
        postTagsArrayNode.add("</hl-tag-replace>");

        highlightNode.set("pre_tags", preTagsArrayNode);
        highlightNode.set("post_tags", postTagsArrayNode);

        return highlightNode;
    }

    public ObjectNode getAggsObjectNode() {

        ObjectNode aggsNode = mapper.createObjectNode();
        for (TermsAggregation termsAggregation : termsAggregations) {
            if (termsAggregation.isDate) {
                ObjectNode yearDateHistogramNode = mapper.createObjectNode();
                yearDateHistogramNode.put("field", termsAggregation.fieldName);
                yearDateHistogramNode.put("interval", "year");
                yearDateHistogramNode.put("format", "yyyy");
                ObjectNode innerNode = mapper.createObjectNode();
                innerNode.set("date_histogram", yearDateHistogramNode);
                aggsNode.set(termsAggregation.fieldName + "-yyyy", innerNode);
                ObjectNode monthDateHistogramNode = mapper.createObjectNode();
                monthDateHistogramNode.put("field", termsAggregation.fieldName);
                monthDateHistogramNode.put("interval", "month");
                monthDateHistogramNode.put("format", "yyyy/MM");
                innerNode = mapper.createObjectNode();
                innerNode.set("date_histogram", monthDateHistogramNode);
                aggsNode.set(termsAggregation.fieldName + "-yyyy/MM", innerNode);
            } else {
                ObjectNode termsNode = mapper.createObjectNode();
                termsNode.put("field", termsAggregation.fieldName);
                if (termsAggregation.size.isPresent()) {
                    termsNode.put("size", termsAggregation.size.get());
                }
                ObjectNode innerNode = mapper.createObjectNode();
                innerNode.set("terms", termsNode);
                aggsNode.set(termsAggregation.fieldName, innerNode);
            }
        }

        return aggsNode;
    }

    public ObjectNode getAsWrappedJsonObject() {
        ObjectNode outerNode = mapper.createObjectNode();
        outerNode.set("query", getQueryObjectNode());
        outerNode.set("sort", getSortArrayNode());
        outerNode.set("highlight", getHighlightObjectNode());
        outerNode.set("aggs", getAggsObjectNode());
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
    public abstract static class Builder<T extends AbstractQuery, K> {

        protected List<SearchSort> sorts;
        protected List<String> highlightFields;
        protected List<TermsAggregation> termsAggregations;
        protected Optional<Float> boost;

        public Builder() {
            sorts = new ArrayList<>();
            highlightFields = new ArrayList<>();
            termsAggregations = new ArrayList<>();
            boost = Optional.empty();
        }

        public K addSort(String field) {
            return addSort(field, false);
        }

        public K addSort(String field, boolean descending) {
            this.sorts.add(new SearchSort(field, descending));
            return (K) this;
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

        public K addTermsAggregation(Collection<Column> termsAggregation) {
            if (termsAggregation != null) {
                termsAggregation.forEach(t -> this.termsAggregations.add(new TermsAggregation(t)));
            }
            return (K) this;
        }

        public K addTermsAggregation(Column termsAggregation) {
            if (termsAggregation != null) {
                this.termsAggregations.add(new TermsAggregation(termsAggregation));
            }
            return (K) this;
        }

        public K addTermsAggregation(List<TermsAggregation> termsAggregation) {
            if (termsAggregation != null) {
                this.termsAggregations.addAll(termsAggregation);
            }
            return (K) this;
        }

        public K addTermsAggregation(TermsAggregation termsAggregation) {
            if (termsAggregation != null) {
                this.termsAggregations.add(termsAggregation);
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
