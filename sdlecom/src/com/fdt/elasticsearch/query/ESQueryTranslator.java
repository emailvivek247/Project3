package com.fdt.elasticsearch.query;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.IndexFieldType;
import net.javacoding.xsearch.search.query.DbsClause;
import net.javacoding.xsearch.search.query.DbsPhrase;
import net.javacoding.xsearch.search.query.DbsQuery;
import net.javacoding.xsearch.search.query.DbsTerm;
import net.javacoding.xsearch.search.result.filter.FilterResult;
import net.javacoding.xsearch.search.result.filter.FilteredColumn;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class ESQueryTranslator {

    private static final Logger logger = LoggerFactory.getLogger(ESQueryTranslator.class);

    private List<Column> columnList;
    private Set<String> dynamicSearchableColumns;
    private int slop = Integer.MAX_VALUE;
    private int booleanOperator = 0;
    private boolean isAllNegative = true;

    private static int AND = 1;
    private static int OR = 2;

    public ESQueryTranslator(List<Column> columnList, String columnsString) {
        this.columnList = columnList;
        if(!U.isEmpty(columnsString)) {
            dynamicSearchableColumns = new HashSet<String>();
            String[] list = columnsString.toLowerCase().split(",");
            for(String c : list) {
                dynamicSearchableColumns.add(c);
            }
        }
    }

    private boolean isSearchable(Column column) {
        return dynamicSearchableColumns == null && column.getIsSearchable() || dynamicSearchableColumns != null
                && dynamicSearchableColumns.contains(column.getColumnName().toLowerCase());
    }

    public void setSlop(int slop) {
        this.slop = slop;
    }

    public void setBooleanOperator(int booleanOperator) {
        this.booleanOperator = booleanOperator;
    }

    public boolean getIsAllNegative() {
        return isAllNegative;
    }

    public AbstractQuery translate(DbsQuery input, FilterResult filterResult, DatasetConfiguration dc) {
        if (columnList != null) {
            return processClauses(input, columnList, slop, filterResult, dc, this.booleanOperator);
        } else {
            return new BoolQuery.Builder().build();
        }
    }

    public AbstractQuery translate(DbsQuery input) {
        return translate(input, null, null);
    }

    private AbstractQuery processClauses(DbsQuery input, List<Column> columnList, int slop,
            FilterResult filterResult, DatasetConfiguration dc, int booleanOperator) {

        BoolQuery.Builder enteredQuery = new BoolQuery.Builder();
        BoolQuery.Builder filterQuery = new BoolQuery.Builder();
        DbsClause[] clauses = input.getClauses();

        for (DbsClause inputClause : clauses) {

            if (isAllNegative) {
                isAllNegative = inputClause.isProhibited();
            }
            String clauseField = inputClause.getField();
            int clauseSlop = inputClause.getSlop();

            BoolQuery.Builder requirements = new BoolQuery.Builder();

            boolean hasField = !DbsClause.DEFAULT_FIELD.equalsIgnoreCase(clauseField);

            // remember which field is filtered already, used in narrowBy.vm
            if (hasField && filterResult != null && !inputClause.isProhibited()) {
                filterResult.addFilteredColumn(new FilteredColumn(dc, clauseField, inputClause.valueToString()));
            }

            for (Column column : columnList) {

                if (!column.getColumnName().equalsIgnoreCase(clauseField) && (hasField || !isSearchable(column))) {
                    // We all create query clause for this field only if
                    // 1. field match
                    // 2. Default field clause on searchable column, and current column is not fieldColumn
                    continue;
                }

                if (column.getColumnType().equalsIgnoreCase("java.sql.Timestamp")) {
                    continue;
                }

                // re-parse the query using analyzer
                String opt;
                clauseSlop = clauseSlop > 0 ? clauseSlop : 0;
                if (inputClause.isPhrase()) {
                    opt = optimizePhrase(inputClause.getPhrase(), column);
                } else {
                    String t = inputClause.getTerm().toString();
                    int wcPreLen = dc.getMinWildcardPrefixLength();
                    if (dc.getIsWildcardAllowed() && (t.indexOf("*") >= wcPreLen || t.indexOf("?") >= wcPreLen)) {
                        opt = t;
                    } else {
                        opt = optimizeTerm(inputClause.getTerm(), column);
                    }
                }

                DbsClause clause = new DbsClause(new DbsTerm(opt), inputClause.isRequired(), inputClause.isProhibited());

                float boost = clause.isProhibited() ? 1f : column.getSearchWeight();
                int theSlop = clause.isProhibited() ? 0 : clauseSlop;
                AbstractQuery query = null;
                if (hasField && clause.getTerm().toString().indexOf(",") > 0) {
                    query = multiSelectionQuery(column, clause.getTerm().toString(), boost);
                } else {
                    query = matchQuery(column, clause.getTerm(), theSlop, boost);
                }

                if (!clause.isProhibited()) {
                    requirements.addShouldClause(query);
                } else {
                    requirements.addMustNotClause(query);
                }
            }

            if (hasField) {
                filterQuery.addMustClause(requirements.build());
            } else if (inputClause.isRequired()) {
                enteredQuery.addMustClause(requirements.build());
            } else if (dc == null || booleanOperator == AND || (booleanOperator != OR && dc.getIsQueryDefaultAnd())) {
                enteredQuery.addMustClause(requirements.build());
            } else {
                enteredQuery.addShouldClause(requirements.build());
            }

        }

        AbstractQuery result = new BoolQuery.Builder()
                .addMustClause(enteredQuery.build())
                .addMustClause(filterQuery.build())
                .build();

        // add searching the whole string if the query is
        // "department of minister"(without the quotes)
        if (clauses.length > 1 && dynamicSearchableColumns == null) {
            boolean hasPhraseSearch = false;
            boolean hasProhibited = false;
            boolean hasField = false;
            for (int x = 0; x < clauses.length && !hasPhraseSearch && !hasProhibited && !hasField; x++) {
                if (clauses[x].isPhrase()) {
                    hasPhraseSearch = true;
                } else if (clauses[x].isProhibited()) {
                    hasProhibited = true;
                } else if (!DbsClause.DEFAULT_FIELD.equalsIgnoreCase(clauses[x].getField())) {
                    hasField = true;
                }
            }
            if (!hasPhraseSearch && !hasProhibited && !hasField) {
                logger.debug("Adding add'l fullMatch section to query");
                BoolQuery.Builder fullMatch = new BoolQuery.Builder();
                for (Column c : columnList) {
                    if (isSearchable(c)) {
                        if (IndexFieldType.isKeyword(c.getIndexFieldType())) {
                            fullMatch.addShouldClause(matchQuery(c.getColumnName(), input.getTerms(),
                                    c.getSearchWeight()));
                        } else {
                            fullMatch.addShouldClause(matchQuery(c.getColumnName(), input.getTerms(),
                                    c.getSearchWeight() * 10));
                        }
                    }
                }
                BoolQuery.Builder retQuery = new BoolQuery.Builder();
                retQuery.addShouldClause(result);
                retQuery.addShouldClause(fullMatch.build());
                result = retQuery.build();
            }
        }

        return result;
    }


    private static BoolQuery multiSelectionQuery(Column c, String texts, float boost) {
        BoolQuery.Builder builder = new BoolQuery.Builder();
        String[] terms = texts.split(",");
        for (int i = 0; i < terms.length; i++) {
            String term = terms[i];
            if (U.isEmpty(term)) {
                continue;
            }
            // escaping comma with \,
            while (term.endsWith("\\")) {
                if (i >= (terms.length - 1)) {
                    term = term.substring(0, term.length() - 1) + ",";
                } else {
                    term = term.substring(0, term.length() - 1) + "," + terms[++i];
                }
            }
            if (c.getIndexFieldType() == IndexFieldType.KEYWORD_DATE_HIERARCHICAL) {
                /*
                 * TODO: Translate this part
                if (AdvancedQueryAnalysis.yearPattern.matcher(term).matches()) {
                    result.add(new TermQuery(luceneTerm("y" + c.getName(), term)), Occur.SHOULD);
                } else if (AdvancedQueryAnalysis.yearAndMonthPattern.matcher(term).matches()) {
                    result.add(new TermQuery(luceneTerm("ym" + c.getName(), term)), Occur.SHOULD);
                }
                */
            } else {
                builder.addShouldClause(new MatchQuery.Builder(c.getName(), term).build());
            }
        }

        builder.addShouldClause(new MatchQuery.Builder(c.getName(), texts).build());

        builder.withBoost(boost);

        return builder.build();
    }

    private static AbstractQuery matchQuery(Column c, DbsTerm term, int slop, float boost) {
        String text = term.toString();
        AbstractQuery result = null;
        if (text != null && (text.indexOf("*") >= 0 || text.indexOf("?") >= 0)) {
            result = new WildcardQuery.Builder(c.getName(), text).withBoost(boost).build();
        } else if (c.getIndexFieldType() == IndexFieldType.KEYWORD_DATE_HIERARCHICAL) {
            /*
             * TODO: Translate this part
            if (text != null) {
                if (AdvancedQueryAnalysis.yearPattern.matcher(text).matches()) {
                    result = new TermQuery(luceneTerm("y" + c.getName(), term));
                } else if (AdvancedQueryAnalysis.yearAndMonthPattern.matcher(text).matches()) {
                    result = new TermQuery(luceneTerm("ym" + c.getName(), term));
                }
            }
            */
        }
        if (result == null) {
            MatchQuery.Builder builder = new MatchQuery.Builder(c.getName(), text).withBoost(boost);
            if (slop != 0) {
                builder.withSlop(slop);
            }
            result = builder.build();
        }

        return result;
    }

    private static MatchQuery matchQuery(String field, String[] terms, float boost) {
        String query = Joiner.on(" ").join(terms);
        return new MatchQuery.Builder(field, query).withBoost(boost).build();
    }

    private static String optimizeTerm(DbsTerm term, Column column) {
        String t = term.toString();
        if (column.getIsKeyword()) {
            if (column.getIsSimpleKeyword()) {
                return t;
            } else {
                return t.toLowerCase();
            }
        } else {
            return t;
        }
    }

    private static String optimizePhrase(DbsPhrase phrase, Column column) {

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < phrase.getTerms().length; i++) {
            buffer.append(phrase.getTerms()[i].toString());
            if (i != phrase.getTerms().length - 1) {
                buffer.append(" ");
            }
        }

        String s = buffer.toString();
        if (column.getIsKeyword()) {
            if (column.getIsSimpleKeyword()) {
                return s;
            } else {
                return s.toLowerCase();
            }
        } else {
            return s;
        }

    }

}
