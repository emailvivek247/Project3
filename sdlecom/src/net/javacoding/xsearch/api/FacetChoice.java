package net.javacoding.xsearch.api;

import java.util.ArrayList;
import java.util.List;

import net.javacoding.xsearch.api.protocol.SearchProtocol;

public class FacetChoice {
    SearchProtocol.FacetChoice facetChoice;
    List<FacetCount> facetCounts;

    public FacetChoice(SearchProtocol.FacetChoice facetChoice) {
        this.facetChoice = facetChoice;
    }
    public String getColumn() {return this.facetChoice.getColumn();}
    /**
     * @return total facet count, because sometimes only partial list is included in this object(defined in search Query). 
     */
    public int getFacetCountTotal() {return this.facetChoice.getFacetCountTotal();}
    /**
     * @return a value~count pair
     */
    public List<FacetCount> getFacetCountList(){
        if(facetCounts==null) {
            facetCounts = new ArrayList<FacetCount>();
            for(SearchProtocol.FacetCount fc : this.facetChoice.getFacetCountList()) {
                facetCounts.add(new FacetCount(fc));
            }
        }
        return facetCounts;
    }
}
