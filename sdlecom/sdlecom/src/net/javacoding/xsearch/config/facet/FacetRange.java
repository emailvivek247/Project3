package net.javacoding.xsearch.config.facet;

public interface FacetRange {
    public String toBeginValue();
    public String toEndValue();
    public int hashCode();
    public boolean equals(Object obj);
}