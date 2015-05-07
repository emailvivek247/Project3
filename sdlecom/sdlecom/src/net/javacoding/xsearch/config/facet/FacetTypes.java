package net.javacoding.xsearch.config.facet;

import java.util.ArrayList;
import java.util.List;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.XMLSerializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("facet-types")
public class FacetTypes extends XMLSerializable{
    
	@XStreamAlias("facet-type-list")
    public List<FacetType> facetTypes;
    
	public FacetTypes() {
		super();
	}

	public FacetType getFacetType(String name) {
        if(facetTypes==null) return null;
        for(FacetType f : facetTypes) {
            if(f.name!=null && f.name.equals(name)) {
                return f;
            }
        }
        return null;
    }
    
	public List<FacetType> getFacetTypes(){
        return facetTypes;
    }
    
	public List<FacetType> getFacetTypes(Column c){
        if(c==null) return this.facetTypes;
        List<FacetType> ret = new ArrayList<FacetType>();
        if(c.getIsNumber()) {
            for(FacetType ft : this.facetTypes) {
                if(ft instanceof NumberFacet) {
                    ret.add(ft);
                }
            }
        }else if(c.getIsDate()) {
            for(FacetType ft : this.facetTypes) {
                if(ft instanceof DateFacet) {
                    ret.add(ft);
                }
            }
        }
        return ret;
    }
    
    public static void main(String[]args) {
        FacetTypes fts = new FacetTypes();
        fts.facetTypes = new ArrayList<FacetType>();
        NumberFacet ft = new NumberFacet();
        ft.name = "price list";
        ft.applicableDataType = "number";
        ft.numberRanges = new NumberRange[1];
        ft.numberRanges[0] = new NumberRange();
        ft.numberRanges[0].begin = 23;
        ft.numberRanges[0].end = 48;
        fts.facetTypes.add(ft);
        System.out.println(fts.toXML());
    }
}
