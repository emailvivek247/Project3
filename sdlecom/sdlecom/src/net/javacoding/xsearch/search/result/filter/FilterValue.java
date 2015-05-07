package net.javacoding.xsearch.search.result.filter;

import java.util.regex.Pattern;



/**
 * Storing possible values for a facet
 * Typically it's just string, but it could also be a range of values with begin and end
 */
public abstract class FilterValue {
    public abstract boolean equals(String... strings);
    public abstract boolean matches(Pattern p);
}
