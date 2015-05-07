package net.javacoding.xsearch.api;

import net.javacoding.xsearch.api.protocol.SearchProtocol;

public class FacetCount {
    SearchProtocol.FacetCount facetCount;

    public FacetCount(SearchProtocol.FacetCount facetCount) {
        this.facetCount = facetCount;
    }
    
    public String getValue() {return this.facetCount.getValue();}
    public String getEndValue() {return this.facetCount.getEndValue();}
    public int getCount() {return this.facetCount.getCount();}
    
    public String toString() {
        if(getEndValue().length()==0){
            return getValue()+"("+getCount()+")";
        }else {
            return getValue()+"~"+getEndValue()+"("+getCount()+")";
        }
    }
}
