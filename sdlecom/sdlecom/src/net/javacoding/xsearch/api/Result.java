package net.javacoding.xsearch.api;

import java.util.ArrayList;
import java.util.List;

import net.javacoding.xsearch.api.protocol.SearchProtocol;

/**
 * This class wraps SearchProtocol.SearchResponse.
 * The purpose is to decorate the objects from SearchProtocol with more useful functions.
 */
public class Result {
    SearchProtocol.SearchResponse response;
    List<Document> docs;
    List<FacetChoice> facetChoices;

    public Result(SearchProtocol.SearchResponse response) {
        this.response = response;
    }
    public long getSearchTime() {return this.response.getSearchTime();}
    public int getTotal() { return this.response.getTotal();}
    /**
     * @return a list of document. This is the main part of search result.
     */
    public List<Document> getDocList() {
        if(docs==null) {
            docs = new ArrayList<Document>();
            for(SearchProtocol.Document d : this.response.getDocList()) {
                docs.add(new Document(d));
            }
        }
        return docs;
    }
    
    /**
     * @return a list of facet search choices, each choice have several value~count pair.
     */
    public List<FacetChoice> getFacetChoiceList(){
        if(facetChoices==null) {
            facetChoices = new ArrayList<FacetChoice>();
            for(SearchProtocol.FacetChoice fc: this.response.getFacetChoiceList()) {
                facetChoices.add(new FacetChoice(fc));
            }
        }
        return facetChoices;
    }
}
