package net.javacoding.xsearch.search.result;

import java.util.List;

import net.javacoding.xsearch.config.Column;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class SearchSort {

    public String field = null;
    public boolean descending = true;
    public Column column = null;

    public SearchSort(Column c) {
        if (c == null) {
            return;
        }
        this.field = c.getColumnName();
        this.descending = c.getIsDescending();
        this.column = c;
    }

    public SearchSort(String field, boolean descending) {
        this.field = field;
        this.descending = descending;
    }

    /**
     * Used during searching. create Sort by sortBy, and desc=Y?, default to true
     * @param sorts
     * @param isDescending sort order for the first column
     * @return
     */
    public static Sort getLuceneSort(List<SearchSort>sorts) {
        if(sorts==null) return null;
        SortField[] sfs = new SortField[sorts.size()+1];
        int i = 0;
        for(SearchSort s : sorts){
            if(s.field==null||!s.column.getIsSortable()){
                sfs[i] = SortField.FIELD_SCORE;
            }else if("Number".equals(s.column.getColumnTypeShortName())) {
                sfs[i] = new SortField("s"+s.column.getColumnName(), SortField.AUTO, s.descending);
            }else {
                sfs[i] = new SortField(s.column.getColumnName(), SortField.AUTO, s.descending);
            }
            i++;
        }
        sfs[i] = SortField.FIELD_SCORE;

        return new Sort(sfs);
    }
    public String getField() {
        return field;
    }
    public boolean getIsDescending() {
        return descending;
    }
    public Column getColumn() {
        return column;
    }
    public static JSONArray toJSONArray(List<SearchSort> l) {
        JSONArray ret = new JSONArray();
        if(l!=null) {
            for(SearchSort s : l) {
                JSONObject h = new JSONObject(s,new String[]{"field","descending"});
                try {
                    h.put("column", s.column.getDisplayName());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ret.put(h);
            }
        }
        return ret; 
    }
}
