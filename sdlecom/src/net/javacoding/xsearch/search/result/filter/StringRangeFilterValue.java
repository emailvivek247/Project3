package net.javacoding.xsearch.search.result.filter;

import java.util.regex.Pattern;

public class StringRangeFilterValue extends RangeFilterValue {
    String begin, end;
    public StringRangeFilterValue(String begin, boolean includeBegin, String end, boolean includeEnd) {
        this.begin = begin;
        this.end = end;
        this.includeBegin = includeBegin;
        this.includeEnd = includeEnd;
    }
    public String getBegin() {
        return begin;
    }
    public String getEnd() {
        return end;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(includeBegin? "[" : "(");
        sb.append(begin==null? "" : begin).append(",").append(end==null? "":end);
        sb.append(includeEnd? "]": ")");
        return sb.toString();
    }

    @Override
    public boolean equals(String... strings) {
        if(strings==null) return false;
        if(strings.length!=2) return false;
        return compare(begin,strings[0]) && compare(end,strings[1]);
    }
    private boolean compare(String a, String b) {
        if(a==null) {
            return b==null;
        }else {
            return a.equals(b);
        }
    }
    @Override
    public boolean matches(Pattern p) {
        String t = end == null ? begin : end;
        if(t==null) return false;
        return p.matcher(t).matches();
    }
}
