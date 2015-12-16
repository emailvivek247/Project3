package com.fdt.elasticsearch.query;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.IndexFieldType;
import net.javacoding.xsearch.indexer.ProxyAnalyzer;
import net.javacoding.xsearch.search.query.DbsClause;
import net.javacoding.xsearch.search.query.DbsPhrase;
import net.javacoding.xsearch.search.query.DbsQuery;
import net.javacoding.xsearch.search.query.DbsTerm;
import net.javacoding.xsearch.search.result.filter.FilterResult;
import net.javacoding.xsearch.search.result.filter.FilteredColumn;
import net.javacoding.xsearch.utility.U;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class ESQueryTranslator {

    private static final Logger logger = LoggerFactory.getLogger(ESQueryTranslator.class);

    private List<Column> columnList;
    private Set<String> dynamicSearchableColumns;
    public int slop = Integer.MAX_VALUE;
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
        return dynamicSearchableColumns==null&&column.getIsSearchable() || dynamicSearchableColumns!=null && dynamicSearchableColumns.contains(column.getColumnName().toLowerCase());
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

    public AbstractQuery translate(Analyzer analyzer, DbsQuery input, FilterResult filterResult, DatasetConfiguration dc) {
        if (columnList != null) {
            return processClauses(analyzer, input, columnList, slop, filterResult, dc, this.booleanOperator);
        } else {
            return new BoolQuery.Builder().build();
        }
    }

    public AbstractQuery translate(Analyzer analyzer, net.javacoding.xsearch.search.query.DbsQuery input) {
        return translate(analyzer, input, null, null);
    }

    private AbstractQuery processClauses(Analyzer globalAnalyzer, DbsQuery input, List<Column> columnList,
            int slop, FilterResult filterResult, DatasetConfiguration dc, int booleanOperator) {

        Analyzer analyzer = globalAnalyzer;
        if (analyzer == null && dc != null) {
            try {
                analyzer = dc.getAnalyzer();
            } catch (Exception e) {
                logger.error("QueryAnalysis error" + e.getMessage());
            }
        }

        BoolQuery.Builder enteredQuery = new BoolQuery.Builder();  //user input query
        BoolQuery.Builder filterQuery = new BoolQuery.Builder();   //any query that has a field
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
                String[] opt = null;
                if (inputClause.isPhrase()) {
                    // optimize phrase clauses
                    opt = optimizePhrase(analyzer, inputClause.getPhrase(), column.getColumnName(), column);
                } else {
                    String t = inputClause.getTerm().toString();
                    int wcPreLen = dc.getMinWildcardPrefixLength();
                    if (dc.getIsWildcardAllowed() && (t.indexOf("*") >= wcPreLen || t.indexOf("?") >= wcPreLen)) {
                        opt = new String[] { t };
                    } else {
                        opt = optimizeTerm(analyzer, inputClause.getTerm(), column.getColumnName(), column);
                    }
                }

                int tmpSlop = 0;
                DbsClause clause;
                if (opt.length == 0) {
                    continue; // this is when all words are in stop words for that analyzer
                } else if (opt.length == 1) {
                    clause = new DbsClause(new DbsTerm(opt[0]), inputClause.isRequired(), inputClause.isProhibited());
                } else {
                    clause = new DbsClause(new DbsPhrase(opt), inputClause.isRequired(), inputClause.isProhibited());
                    tmpSlop = clauseSlop + input.getTerms().length - opt.length; // count in skipped words
                    tmpSlop = (tmpSlop>0 ? tmpSlop : 0);
                }

                if (!clause.isProhibited()) {
                    if (!clause.isPhrase()) {
                        if (hasField && clause.getTerm().toString().indexOf(",") > 0) {
                            requirements.addShouldClause(multiSelectionQuery(column, clause.getTerm().toString(),
                                    1.0f));
                        } else {
                            requirements.addShouldClause(termQuery(column, clause.getTerm(),
                                    column.getSearchWeight()));
                        }
                    } else {
                        requirements.addShouldClause(exactPhrase(clause.getPhrase(), column.getColumnName(),
                                tmpSlop, column.getSearchWeight()));
                    }
                } else {
                    if(hasField){
                        if (!clause.isPhrase()) {
                            if(clause.getTerm().toString().indexOf(",")>0) {
                                filterQuery.addMustNotClause(multiSelectionQuery(column, clause.getTerm().toString(),1.0f));
                            }else {
                                filterQuery.addMustNotClause(termQuery(column, clause.getTerm(),1.0f));
                            }
                        } else {
                            filterQuery.addMustNotClause(exactPhrase(clause.getPhrase(), column.getColumnName(), 0,column.getSearchWeight()));
                        }
                    }else{
                        if (!clause.isPhrase()) {
                            enteredQuery.addMustNotClause(termQuery(column, clause.getTerm(),1.0f));
                        } else {
                            enteredQuery.addMustNotClause(exactPhrase(clause.getPhrase(), column.getColumnName(), 0,column.getSearchWeight()));
                        }
                    }
                }
            } 

            if (hasField) {
                filterQuery.addMustClause(requirements.build());
            } else if (inputClause.isRequired()) {
                enteredQuery.addMustClause(requirements.build());
            } else if (dc == null || booleanOperator == AND || (booleanOperator != OR && dc.getIsQueryDefaultAnd())) {
                String t = inputClause.toString();
                if (ProxyAnalyzer.getStopwords().contains(t)) {
                    enteredQuery.addShouldClause(requirements.build());
                } else {
                    enteredQuery.addMustClause(requirements.build());
                }
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
                BoolQuery.Builder fullMatch = new BoolQuery.Builder();
                for (Column c : columnList) {
                    if (isSearchable(c)) {
                        if (IndexFieldType.isKeyword(c.getIndexFieldType())) {
                            fullMatch.addShouldClause(termQuery(c.getColumnName(), input.getTerms(),
                                    c.getSearchWeight()));
                        } else {
                            String[] analyzedStrings = optimizePhrase(analyzer, new DbsPhrase(input.getTerms()),
                                    c.getColumnName(), c);
                            fullMatch.addShouldClause(exactPhrase(new DbsPhrase(analyzedStrings), c.getColumnName(), 0,
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
                builder.addShouldClause(new TermQuery.Builder(c.getName(), term).build());
            }
        }

        builder.addShouldClause(new TermQuery.Builder(c.getName(), texts).build());

        builder.withBoost(boost);

        return builder.build();
    }

    private static AbstractQuery termQuery(Column c, DbsTerm term, float boost) {
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
            result = new TermQuery.Builder(c.getName(), text).withBoost(boost).build();
        }

        return result;
    }

    private static TermQuery termQuery(String field, String[] terms, float boost) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < terms.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(terms[i]);
        }
        return new TermQuery.Builder(field, sb.toString()).withBoost(boost).build();
    }

    private static MatchQuery exactPhrase(DbsPhrase nutchPhrase, String field, int slop, float boost) {
        DbsTerm[] terms = nutchPhrase.getTerms();
        String phrase = Joiner.on(" ").join(Arrays.stream(terms).map(t -> t.toString()).iterator());
        MatchQuery.Builder builder = new MatchQuery.Builder(field, phrase);
        if (slop != 0) {
            builder.withSlop(slop);
        }
        builder.withBoost(boost);
        return builder.build();
    }

    /** Optimizes terms and re-analyze */
    private static String[] optimizeTerm(Analyzer a, DbsTerm term, String fieldName, Column column) {
    	String t = term.toString();
        if (column.getIsKeyword()){
            if(column.getIsSimpleKeyword()){
                return new String[]{t};
            }else{
                return new String[]{t.toLowerCase()};
            }
        }
        ArrayList<String> result = new ArrayList<String>();
        TokenStream ts = a.tokenStream(fieldName, new java.io.StringReader(t));
        Token token = null;
        try {
            while ((token = ts.next()) != null) {
                result.add(token.termText());                
            }
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    /** Optimizes phrase queries to use n-grams when possible. and re-analyze */
    private static String[] optimizePhrase(Analyzer a, DbsPhrase phrase, String fieldName, Column column) {
    	 ArrayList<String> result = new ArrayList<String>();
         //TODO:cylu set ts = analyzer.tokenStream("dummy", new StringReader())
         /*
          * TokenStream ts = new ArrayTokens(phrase); ts = new
          * StandardFilter(ts); ts = new LowerCaseFilter(ts); ts = new
          * StopFilter(ts,QueryAnalysis.STOP_SET);
          */
         StringBuffer buffer = new StringBuffer();
         for (int i = 0; i < phrase.getTerms().length; i++) {
             buffer.append(phrase.getTerms()[i].toString());
             if (i != phrase.getTerms().length - 1) buffer.append(" ");
         }
         String s =buffer.toString();
         if (column.getIsKeyword()){
             if(column.getIsSimpleKeyword()){
                 return new String[]{s};
             }else{
                 return new String[]{s.toLowerCase()};
             }
         }

         TokenStream ts = a.tokenStream(fieldName, new java.io.StringReader(s));

         Token token= null;
         try {
             while ((token = ts.next()) != null) {
                 result.add(token.termText());                
             }
         } catch (IOException e) {
             throw new RuntimeException(e.toString());
         }
        // if (prev != null) result.add(prev.termText());
         return (String[]) result.toArray(new String[result.size()]);

    }

}
