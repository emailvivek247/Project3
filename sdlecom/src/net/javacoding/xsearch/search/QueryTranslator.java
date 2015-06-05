package net.javacoding.xsearch.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.config.IndexFieldType;
import net.javacoding.xsearch.indexer.ProxyAnalyzer;
import net.javacoding.xsearch.search.analysis.AdvancedQueryAnalysis;
import net.javacoding.xsearch.search.query.DbsClause;
import net.javacoding.xsearch.search.query.DbsPhrase;
import net.javacoding.xsearch.search.query.DbsTerm;
import net.javacoding.xsearch.search.result.filter.FilterResult;
import net.javacoding.xsearch.search.result.filter.FilteredColumn;
import net.javacoding.xsearch.utility.U;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanClause.Occur;

/** Translation from Nutch queries to Lucene queries. */
public class QueryTranslator {
    private static Logger logger = LoggerFactory.getLogger(QueryTranslator.class.getName());

    public static int AND = 1;
    public static int OR = 2;

    public QueryTranslator(ArrayList<Column> columnList) {
        this.columnList = columnList;
    }
    public QueryTranslator(ArrayList<Column> columnList, String columnsString) {
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

    ArrayList<Column> columnList;
    Set<String> dynamicSearchableColumns;
    public int slop =  Integer.MAX_VALUE;
    private int booleanOperator = 0;
    private boolean isAllNegative = true;

    public QueryTranslator setSlop(int slop) {
        this.slop = slop;
        return this;
    }

    public void setBooleanOperator(int booleanOperator){
        this.booleanOperator = booleanOperator;
    }
    
    public boolean getIsAllNegative() {
        return isAllNegative;
    }
    
    
    /**
     *  Translate a query into a Lucene query 
     * @param analyzer user provided analyzer
     * @param local Query object
     * @param filteredColumns for returning back the explict fields in the query
     * @param dc dataset configuration
     * @return lucene Query object and the field set
     */
    public org.apache.lucene.search.Query translate(Analyzer analyzer, net.javacoding.xsearch.search.query.DbsQuery input, FilterResult filterResult,DatasetConfiguration dc) {
        if (columnList != null) {
            return processClauses(analyzer, input, columnList, slop,filterResult,dc, this.booleanOperator);        
        } else {
            return new BooleanQuery();
        }
    }

    public  org.apache.lucene.search.Query translate(Analyzer analyzer, net.javacoding.xsearch.search.query.DbsQuery input){
        return translate( analyzer, input,null,null);
    }
    /**
     * Add all terms from a Nutch query to a Lucene query, searching the named
     * field as a sloppy phrase and as individual terms..
     */
    private org.apache.lucene.search.Query processClauses(Analyzer globalAnalyzer, net.javacoding.xsearch.search.query.DbsQuery input, ArrayList<Column> columnList, int slop, FilterResult filterResult,DatasetConfiguration dc, int booleanOperator) {
        Analyzer analyzer = globalAnalyzer;
        if ( analyzer==null && dc != null) try {
            analyzer = dc.getAnalyzer();
        }catch (Exception e){
            logger.error("QueryAnalysis error"+e.getMessage());            
        }
        BooleanQuery enteredQuery = new BooleanQuery();  //user input query
        BooleanQuery filterQuery = new BooleanQuery();   //any query that has a field
        DbsClause[] clauses = input.getClauses();

        for (DbsClause inputClause : clauses) {
            if(isAllNegative) {isAllNegative = inputClause.isProhibited();}
            String clauseField = inputClause.getField();
            int clauseSlop = inputClause.getSlop();            

            BooleanQuery requirements = new BooleanQuery();
            
            boolean hasField = !DbsClause.DEFAULT_FIELD.equalsIgnoreCase(clauseField);

            // remember which field is filtered already, used in narrowBy.vm
            if (hasField && filterResult!=null && !inputClause.isProhibited()) {
                filterResult.addFilteredColumn(new FilteredColumn(dc, clauseField, inputClause.valueToString()));
            }
            
            for(Column column : columnList) {
                
                if (!column.getColumnName().equalsIgnoreCase(clauseField) && ( hasField || !isSearchable(column))) {
                    // We all create query clause for this field only if
                    //1. field match
                    //2. Default field clause on searchable column, and current column is not fieldColumn
                    continue;
                }

                //re-parse the query using analyzer
                String[] opt;
                if (inputClause.isPhrase()) {  // optimize phrase clauses
                    opt = optimizePhrase(analyzer, inputClause.getPhrase(),column.getColumnName(), column);
                } else {
                    String t = inputClause.getTerm().toString();
                    if(dc!=null && (dc.getIsWildcardAllowed()&&(t.indexOf("*")>=dc.getMinWildcardPrefixLength()||t.indexOf("?")>=dc.getMinWildcardPrefixLength()))) {
                        opt = new String[] {t};
                    } else {
                        opt = optimizeTerm(analyzer, inputClause.getTerm(),column.getColumnName(), column);
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

                if ( !clause.isProhibited()) {
                    if (!clause.isPhrase()) {
                        if(hasField && clause.getTerm().toString().indexOf(",")>0) {
                            requirements.add(multiSelectionQuery(column, clause.getTerm().toString(),1.0f), BooleanClause.Occur.SHOULD);
                        }else {
                            requirements.add(termQuery(column, clause.getTerm(), column.getSearchWeight()), BooleanClause.Occur.SHOULD);
                        }
                    } else {
                        requirements.add(exactPhrase(clause.getPhrase(), column.getColumnName(), tmpSlop,column.getSearchWeight()), BooleanClause.Occur.SHOULD);
                    }
                } else {
                    if(hasField){
                        if (!clause.isPhrase()) {
                            if(clause.getTerm().toString().indexOf(",")>0) {
                                filterQuery.add(multiSelectionQuery(column, clause.getTerm().toString(),1.0f), BooleanClause.Occur.MUST_NOT);
                            }else {
                                filterQuery.add(termQuery(column, clause.getTerm(),1.0f), BooleanClause.Occur.MUST_NOT);
                            }
                        } else {
                            filterQuery.add(exactPhrase(clause.getPhrase(), column.getColumnName(), 0,column.getSearchWeight()), BooleanClause.Occur.MUST_NOT);
                        }
                    }else{
                        if (!clause.isPhrase()) {
                            enteredQuery.add(termQuery(column, clause.getTerm(),1.0f), BooleanClause.Occur.MUST_NOT);
                        } else {
                            enteredQuery.add(exactPhrase(clause.getPhrase(), column.getColumnName(), 0,column.getSearchWeight()), BooleanClause.Occur.MUST_NOT);
                        }
                    }
                }
            } //columns
            if (requirements.getClauses() != null && requirements.getClauses().length > 0) {
                //based on dc configuration, use OR/AND to connect each clause
                if ( hasField ) {
                    filterQuery.add(requirements,  BooleanClause.Occur.MUST );
                }else if (inputClause.isRequired()) {
                    enteredQuery.add(requirements,  BooleanClause.Occur.MUST );
                }else if (dc ==null || booleanOperator== AND || (booleanOperator!= OR && dc.getIsQueryDefaultAnd()) ) {
                    String t = inputClause.toString();
                    if(ProxyAnalyzer.getStopwords().contains(t)) {
                        enteredQuery.add(requirements,  BooleanClause.Occur.SHOULD );
                    }else {
                        enteredQuery.add(requirements,  BooleanClause.Occur.MUST );
                    }
                } else {
                    enteredQuery.add(requirements,  BooleanClause.Occur.SHOULD );
                }
            }
        } //clause

        Query ret = AdvancedQueryAnalysis.appendQuery(enteredQuery, filterQuery);

        //add searching the whole string if the query is "department of minister"(without the quotes)
        if(clauses.length>1 && dynamicSearchableColumns == null){
            boolean hasPhraseSearch = false;
            boolean hasProhibited = false;
            boolean hasField = false;
            for(int x=0;x<clauses.length&&!hasPhraseSearch&&!hasProhibited&&!hasField;x++){
                if(clauses[x].isPhrase()){
                    hasPhraseSearch = true;
                }else if(clauses[x].isProhibited()){
                    hasProhibited = true;
                }else if(!DbsClause.DEFAULT_FIELD.equalsIgnoreCase(clauses[x].getField())){
                    hasField = true;
                }
            }
            if(!hasPhraseSearch&&!hasProhibited&&!hasField){
                BooleanQuery fullMatch = new BooleanQuery();
                for (Column c : columnList) {
                    if(isSearchable(c)){
                        if(IndexFieldType.isKeyword(c.getIndexFieldType())) {
                            fullMatch.add(termQuery(c.getColumnName(), input.getTerms(), c.getSearchWeight()), BooleanClause.Occur.SHOULD);
                        }else {
                            String[] analyzedStrings = optimizePhrase(analyzer, new net.javacoding.xsearch.search.query.DbsPhrase(input.getTerms()),c.getColumnName(), c);
                            fullMatch.add(exactPhrase(new DbsPhrase(analyzedStrings), c.getColumnName(), 0, c.getSearchWeight()*10), BooleanClause.Occur.SHOULD);
                        }
                    }
                }
                BooleanQuery retQuery = new BooleanQuery();
                retQuery.add(ret, BooleanClause.Occur.SHOULD);
                retQuery.add(fullMatch, BooleanClause.Occur.SHOULD);
                ret = retQuery;
            }
        }
        
        return ret;
    }

    /*
     * The text should be comma separated values
     */
    private static org.apache.lucene.search.Query multiSelectionQuery(Column c, String texts , float boost) {
        org.apache.lucene.search.BooleanQuery result = new BooleanQuery();
        String[] terms = texts.split(",");
        for(int i=0;i<terms.length;i++) {
            String term = terms[i];
            if(U.isEmpty(term))continue;
            //escaping comma with \,
            while(term.endsWith("\\")) {
                if(i>=(terms.length-1)) {
                    term=term.substring(0, term.length()-1)+",";
                }else {
                    term=term.substring(0, term.length()-1)+","+terms[++i];
                }
            }
            if(c.getIndexFieldType()==IndexFieldType.KEYWORD_DATE_HIERARCHICAL){
                if(AdvancedQueryAnalysis.yearPattern.matcher(term).matches()) {
                    result.add(new TermQuery(luceneTerm("y"+c.getName(), term)), Occur.SHOULD);
                }else if(AdvancedQueryAnalysis.yearAndMonthPattern.matcher(term).matches()) {
                    result.add(new TermQuery(luceneTerm("ym"+c.getName(), term)), Occur.SHOULD);
                }
            }else {
                result.add(new TermQuery(luceneTerm(c.getName(), term)), Occur.SHOULD);
            }
        }
        //handling filed:"Last Name, First Name" cases, where the whole string is a Keyword with comma inside
        result.add(new TermQuery(luceneTerm(c.getName(), texts)), Occur.SHOULD);

        result.setBoost(boost);
        
        return result;        
    }
    private static org.apache.lucene.search.Query termQuery(Column c, DbsTerm term, float boost) {
        String text = term.toString();
        org.apache.lucene.search.Query result = null;
        if (text!=null && (text.indexOf("*")>=0 || text.indexOf("?")>=0)){
            result = new WildcardQuery(luceneTerm(c.getName(), term));            
        }else if(c.getIndexFieldType()==IndexFieldType.KEYWORD_DATE_HIERARCHICAL){
            if(text!=null) {
                if(AdvancedQueryAnalysis.yearPattern.matcher(text).matches()) {
                    result = new TermQuery(luceneTerm("y"+c.getName(), term));
                }else if(AdvancedQueryAnalysis.yearAndMonthPattern.matcher(text).matches()) {
                    result = new TermQuery(luceneTerm("ym"+c.getName(), term));
                }
            }
        }
        if(result==null){
           result = new TermQuery(luceneTerm(c.getName(), term));
        }
        result.setBoost(boost);
        return result;        
    }
    private static org.apache.lucene.search.Query termQuery(String field, String[] terms, float boost) {
        org.apache.lucene.search.Query result;
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<terms.length;i++) {
            if(i>0) {
                sb.append(" ");
            }
            sb.append(terms[i]);
        }
        result = new TermQuery(new org.apache.lucene.index.Term(field,sb.toString()));
        result.setBoost(boost);
        return result;        
    }

    /** Utility to construct a Lucene exact phrase query for a Nutch phrase. */
    private static PhraseQuery exactPhrase(DbsPhrase nutchPhrase, String field, int slop, float boost) {
        DbsTerm[] terms = nutchPhrase.getTerms();
        PhraseQuery exactPhrase = new PhraseQuery();
        if (slop != 0)
            exactPhrase.setSlop(slop);
        for (int i = 0; i < terms.length; i++) {
            exactPhrase.add(luceneTerm(field, terms[i]));
        }
        exactPhrase.setBoost(boost);
        return exactPhrase;
    }

    /** Utility to construct a Lucene Term given a Nutch query term and field. */
    private static org.apache.lucene.index.Term luceneTerm(String field, DbsTerm term) {
        return new org.apache.lucene.index.Term(field, term.toString());
    }
    private static org.apache.lucene.index.Term luceneTerm(String field, String text) {
        return new org.apache.lucene.index.Term(field, text);
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
    
    /** Optimizes phrase queries to use n-grams when possible.  and re-analyze*/
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
