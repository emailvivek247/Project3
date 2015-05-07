package net.javacoding.xsearch.config.facet;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("date-facet")
public class DateFacet extends FacetType{
   
	@XStreamAlias("date-ranges")
    public DateRange[] dateRanges;
	
	public DateFacet() {
		super();
	}

	public FacetRange findFacetRange(long v) {
        for(DateRange dr : dateRanges) {
            if(dr.getBeginLong() <= v && v < dr.getEndLong()) {
                return dr;
            }
        }
        return null;
    }
}
