package net.javacoding.xsearch.config.facet;

import net.javacoding.xsearch.utility.U;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("number-range")
public class NumberRange implements FacetRange {
    
	@XStreamAlias("begin")
    @XStreamAsAttribute
    public Integer begin;
    
	@XStreamAlias("end")
    @XStreamAsAttribute
    public Integer end;
    
    public NumberRange() {
		super();
	}
	
    @Override
    public int hashCode() {
        return (begin==null? 0 : begin.hashCode())*37 + (end==null ? 0 : end.hashCode());
    }
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof NumberRange)) return false;
        NumberRange other = (NumberRange)obj;
        return U.equals(this.begin, other.begin)&&U.equals(this.end, other.end);
    }

    public String toString() {
        return "["+(begin==null?"":begin)+","+(end==null?"":end)+")";
    }

    public String toBeginValue() {
        return this.begin==null? "".intern() : Integer.toString(this.begin).intern();
    }

    public String toEndValue() {
        return this.end==null? "".intern() : Integer.toString(this.end).intern();
    }
}
