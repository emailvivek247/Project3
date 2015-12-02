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
    protected final Optional<Float> boost;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public AbstractQuery(AbstractQueryBuilder builder) {
        boost = builder.boost;
        sorts = builder.sorts;
    }

    public abstract ObjectNode getQueryObjectNode();

    public ArrayNode getSortArrayNode() {

        List<ObjectNode> outerNodes = sorts.stream().map((sort) -> {
            ObjectNode innerNode = mapper.createObjectNode();
            innerNode.put("order", sort.descending ? "desc" : "asc");
            ObjectNode outerNode = mapper.createObjectNode();
            outerNode.set(sort.field, innerNode);
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
        protected Optional<Float> boost;

        public AbstractQueryBuilder() {
            sorts = new ArrayList<>();
            boost = Optional.empty();
        }

        public K addSort(SearchSort sort) {
            this.sorts.add(sort);
            return (K) this;
        }

        public K addSort(Collection<SearchSort> sort) {
            this.sorts.addAll(sort);
            return (K) this;
        }

        public K withBoost(Float boost) {
            this.boost = Optional.of(boost);
            return (K) this;
        }

        public abstract T build();
    }
}
