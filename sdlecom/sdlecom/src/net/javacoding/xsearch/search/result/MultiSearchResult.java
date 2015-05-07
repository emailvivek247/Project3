package net.javacoding.xsearch.search.result;

import java.util.ArrayList;
import java.util.List;

/**
 * Not ready yet. To be finished.
 */
public class MultiSearchResult {
    List<SearchResult> searchResults;
    String searchTimeString;
    String URLEncodedQuery;
    String queryString;
    String encoding;
    String noStartQueryString;
    
    public MultiSearchResult() {
        searchResults = new ArrayList<SearchResult>();
    }
    public void add(SearchResult sr) {
        searchResults.add(sr);
    }

    public List<SearchResult> getSearchResults() {
        return searchResults;
    }

    public int getTotal() {
        int total = 0;
        for(SearchResult sr : searchResults) {
            total += sr.getTotal();
        }
        return total;
    }
    public String getSearchTimeString() {
        return searchTimeString;
    }
    public void setSearchTimeString(String searchTimeString) {
        this.searchTimeString = searchTimeString;
    }
    public String getURLEncodedQuery() {
        return URLEncodedQuery;
    }
    public void setURLEncodedQuery(String encodedQuery) {
        URLEncodedQuery = encodedQuery;
    }
    public String getQueryString() {
        return queryString;
    }
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }
    public String getEncoding() {
        return encoding;
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public String getNoStartQueryString() {
        return noStartQueryString;
    }
    public void setNoStartQueryString(String noStartQueryString) {
        this.noStartQueryString = noStartQueryString;
    }
    public String getURLEncodedClearQuery() { return noStartQueryString;}

}
