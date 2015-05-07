package net.javacoding.xsearch.config.facet;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("number-facet")
public class NumberFacet extends FacetType{
    
	@XStreamAlias("number-ranges")
    public NumberRange[] numberRanges;
	
	public NumberFacet() {
		super();
	}

	public FacetRange findFacetRange(int v) {
        for(int i=0; i<numberRanges.length;i++) {
            NumberRange nr = numberRanges[i];
            if(nr.begin==null) {
                if(v < nr.end) {
                    return nr;
                }
            }else if(nr.end == null) {
                if(v >= nr.begin) {
                    return nr;
                }
            }else if(nr.begin <= v && v < nr.end) {
                return nr;
            }
        }
        return null;
    }
}
