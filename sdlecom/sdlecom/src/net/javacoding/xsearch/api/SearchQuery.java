package net.javacoding.xsearch.api;

import java.util.ArrayList;
import java.util.List;

public class SearchQuery{
    String q ="";
    String lq ="";
    int start;
    int length = 30;
    List<Sort> sortBys;
    String beginHighlightTag = "<b>";
    String endHighlightTag = "</b>";
    List<StringColumnFormat> stringColumnFormats;
    boolean debug;
    int facetCountLimit = 17;
    String sourceLocation = "";
    String searchableColumns = "";
    boolean enableFacetSearch = true;
    
    public static final int DEFAULT = 0;
    public static final int AND = 1;
    public static final int OR = 2;
    int booleanOperator;

    public SearchQuery() {
        this.sortBys = new ArrayList<Sort>();
        this.stringColumnFormats = new ArrayList<StringColumnFormat>();
    }
    
    public SearchQuery(String query) {
        this.q = query;
        this.sortBys = new ArrayList<Sort>();
        this.stringColumnFormats = new ArrayList<StringColumnFormat>();
    }
    public SearchQuery setLuceneQuery(String luceneQuery) {
        this.lq = luceneQuery;
        return this;
    }
    
    public SearchQuery setAdvancedQuery(String advancedQuery) {
        this.lq = advancedQuery;
        return this;
    }    
    
    public SearchQuery addSort(String columnName, boolean desc) {
        this.sortBys.add(new Sort(columnName,desc));
        return this;
    }
    public SearchQuery setHighlightTag(String beginTag, String endTag) {
        this.beginHighlightTag = beginTag;
        this.endHighlightTag = endTag;
        return this;
    }
    public SearchQuery highlight(String columnName) {
        this.stringColumnFormats.add(new StringColumnFormat(columnName,StringColumnFormat.HIGHLIGHTED_HTML));
        return this;
    }
    public SearchQuery summarize(String columnName) {
        this.stringColumnFormats.add(new StringColumnFormat(columnName,StringColumnFormat.SUMMARIZED_HTML));
        return this;
    }
    public SearchQuery directHighlight(String columnName) {
        this.stringColumnFormats.add(new StringColumnFormat(columnName,StringColumnFormat.HIGHLIGHTED));
        return this;
    }
    public SearchQuery directSummarize(String columnName) {
        this.stringColumnFormats.add(new StringColumnFormat(columnName,StringColumnFormat.SUMMARIZED));
        return this;
    }
    public int getStart() {
        return start;
    }
    public SearchQuery setStart(int start) {
        this.start = start;
        return this;
    }
    public int getLength() {
        return length;
    }
    public SearchQuery setLength(int length) {
        this.length = length;
        return this;
    }
    public boolean isDebug() {
        return debug;
    }
    /**
     * If set to true, SDL will print out debug information, like the query details, explain the results, etc.
     * @param debug
     * @return
     */
    public SearchQuery setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }
    public SearchQuery setDefaultBooleanOperator() {
        this.booleanOperator = DEFAULT;
        return this;
    }
    public SearchQuery setAndAsBooleanOperator() {
        this.booleanOperator = AND;
        return this;
    }
    public SearchQuery setOrAsBooleanOperator() {
        this.booleanOperator = OR;
        return this;
    }
    public SearchQuery setFacetCountLimit(int facetCountLimit) {
        this.facetCountLimit = facetCountLimit;
        return this;
    }
    public String getSourceLocation() {
        return sourceLocation;
    }
    /**
     * It usually is the search query's IP address, or hostname, or any string. 
     * This is used during logging. And later we can run a little statistics on the log.
     * @param sourceLocation
     */
    public SearchQuery setSourceLocation(String sourceLocation) {
        this.sourceLocation = sourceLocation;
        return this;
    }
    /**
     * Comma separated columns names, to dyanmically set the searchable columns, overriding default searchable settings.
     * @param searchableColumns
     */
    public SearchQuery setSearchableColumns(String searchableColumns) {
        this.searchableColumns = searchableColumns;
        return this;
    }
    public SearchQuery disableFacetSearch() {
        this.enableFacetSearch = false;
        return this;
    }
}