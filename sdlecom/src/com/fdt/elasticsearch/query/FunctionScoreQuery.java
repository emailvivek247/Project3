package com.fdt.elasticsearch.query;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class FunctionScoreQuery extends AbstractQuery {

    private final AbstractQuery query;
    private final List<String> boostFields;

    public FunctionScoreQuery(Builder builder) {
        super(builder);
        this.query = builder.query;
        this.boostFields = builder.boostFields;
    }

    @Override
    public ObjectNode getQueryObjectNode() {

        ObjectNode functionScoreNode = mapper.createObjectNode();
        functionScoreNode.set("query", query.getQueryObjectNode());

        ArrayNode functionsNode = functionScoreNode.putArray("functions");
        for (String boostField : boostFields) {
            ObjectNode fieldValueFactorNode = mapper.createObjectNode();
            fieldValueFactorNode.put("field", boostField + ".number");
            ObjectNode outerNode = mapper.createObjectNode();
            outerNode.set("field_value_factor", fieldValueFactorNode);
            functionsNode.add(outerNode);
        }

        functionScoreNode.set("functions", functionsNode);

        ObjectNode queryNode = mapper.createObjectNode();
        queryNode.set("function_score", functionScoreNode);

        return queryNode;
    }

    public static class Builder extends AbstractQuery.Builder<FunctionScoreQuery, Builder> {

        private AbstractQuery query;
        private List<String> boostFields;

        public Builder(AbstractQuery query) {
            this.query = query;
            this.boostFields = new ArrayList<>();
        }

        public Builder addBoostField(String boostField) {
            boostFields.add(boostField);
            return this;
        }

        public Builder addBoostField(List<String> boostField) {
            boostFields.addAll(boostField);
            return this;
        }

        public FunctionScoreQuery build() {
            return new FunctionScoreQuery(this);
        }
    }

}
