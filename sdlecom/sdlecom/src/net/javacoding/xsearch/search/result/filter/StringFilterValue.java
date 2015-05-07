package net.javacoding.xsearch.search.result.filter;

import java.util.regex.Pattern;


public class StringFilterValue extends FilterValue{
    String data;
    public StringFilterValue(String v) {
        this.data = v;
    }
    public String toString() {
        return this.data;
    }

    @Override
    public boolean equals(String... strings) {
        if(strings==null) return false;
        if(strings.length<=0) return false;
        return this.data!=null && this.data.equals(strings[0]);
    }
    @Override
    public boolean matches(Pattern p) {
        if(data==null) return false;
        return p.matcher(data).matches();
    }
}
