package com.fdt.elasticsearch.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class BoolQuery extends AbstractQuery {

    private final List<AbstractQuery> mustClauses;
    private final List<AbstractQuery> mustNotClauses;
    private final List<AbstractQuery> filterClauses;
    private final List<AbstractQuery> shouldClauses;

    private final Optional<Float> boost;

    public BoolQuery(Builder builder) {
        this.mustClauses = builder.mustClauses;
        this.mustNotClauses = builder.mustNotClauses;
        this.filterClauses = builder.filterClauses;
        this.shouldClauses = builder.shouldClauses;
        this.boost = builder.boost;
    }

    @Override
    public ObjectNode getAsJsonObject() {

        ObjectNode boolNode = mapper.createObjectNode();

        addClauses("must", mustClauses, boolNode);
        addClauses("filter", filterClauses, boolNode);
        addClauses("must_not", mustNotClauses, boolNode);
        addClauses("should", shouldClauses, boolNode);

        if (boost.isPresent()) {
            boolNode.put("boost", boost.get());
        }

        ObjectNode queryNode = mapper.createObjectNode();
        queryNode.set("bool", boolNode);

        return queryNode;
    }

    private static void addClauses(String field, List<AbstractQuery> clauses, ObjectNode boolNode) {
        if (clauses.size() == 1) {
            boolNode.set(field, clauses.get(0).getAsJsonObject());
        } else if (clauses.size() > 1) {
            ArrayNode mustArrayNode = boolNode.putArray(field);
            clauses.stream().forEach(c -> mustArrayNode.add(c.getAsJsonObject()));
        }
    }

    public static class Builder {

        private List<AbstractQuery> mustClauses;
        private List<AbstractQuery> mustNotClauses;
        private List<AbstractQuery> filterClauses;
        private List<AbstractQuery> shouldClauses;

        private Optional<Float> boost;

        public Builder() {
            boost = Optional.empty();
            mustClauses = new ArrayList<>();
            mustNotClauses = new ArrayList<>();
            filterClauses = new ArrayList<>();
            shouldClauses = new ArrayList<>();
        }

        public Builder addMustClause(AbstractQuery query) {
            this.mustClauses.add(query);
            return this;
        }

        public Builder addMustClause(Collection<AbstractQuery> queries) {
            this.mustClauses.addAll(queries);
            return this;
        }

        public Builder addMustNotClause(AbstractQuery query) {
            this.mustNotClauses.add(query);
            return this;
        }

        public Builder addMustNotClause(Collection<AbstractQuery> queries) {
            this.mustNotClauses.addAll(queries);
            return this;
        }

        public Builder addFilterClause(AbstractQuery query) {
            this.filterClauses.add(query);
            return this;
        }

        public Builder addFilterClause(Collection<AbstractQuery> queries) {
            this.filterClauses.addAll(queries);
            return this;
        }

        public Builder addShouldClause(AbstractQuery query) {
            this.shouldClauses.add(query);
            return this;
        }

        public Builder addShouldClause(Collection<AbstractQuery> queries) {
            this.shouldClauses.addAll(queries);
            return this;
        }

        public Builder withBoost(Float boost) {
            this.boost = Optional.of(boost);
            return this;
        }

        public BoolQuery build() {
            return new BoolQuery(this);
        }
    }

    public static void main(String[] args) {

        MatchQuery query1 = new MatchQuery.Builder()
                .withField("testField1")
                .withQuery("test query 1")
                .build();

        MatchQuery query2 = new MatchQuery.Builder()
                .withField("testField2")
                .withQuery("test query 2")
                .withBoost(3F)
                .withOperator("and")
                .withType("phrase")
                .build();

        MatchQuery query3 = new MatchQuery.Builder()
                .withField("testField3")
                .withQuery("test query 3")
                .build();

        MatchQuery query4 = new MatchQuery.Builder()
                .withField("testField4")
                .withQuery("test query 4")
                .build();

        BoolQuery boolQuery = new BoolQuery.Builder()
                .addMustClause(query1)
                .addMustClause(query2)
                .addFilterClause(query3)
                .addShouldClause(query4)
                .build();

        System.out.println(boolQuery.getAsStringPrettyPrint());

    }
}
