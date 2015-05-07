package net.javacoding.xsearch.search.result.filter;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.DatasetConfiguration;
import net.javacoding.xsearch.utility.EscapeChars;
import net.javacoding.xsearch.utility.U;

public class FilteredColumn {
    public Column column;
    public FilterValue value;
    public FilteredColumn child = null;
    public FilteredColumn(DatasetConfiguration dc, String columnName, String value) {
        this.value = new StringFilterValue(value);
        this.column = dc.getColumn(columnName);
    }
    public FilteredColumn(DatasetConfiguration dc, String columnName, RangeFilterValue rfv) {
        this.value = rfv;
        this.column = dc.getColumn(columnName);
    }
    public Column getColumn() {
        return column;
    }
    public FilterValue getValue() {
        return value;
    }
    private String recursiveRemoveSelfAndChild(String v) {
        if(v.indexOf(column.getName())>=0) {
            if(value instanceof RangeFilterValue) {
                v = v.replaceAll("\\s*[\\+\\-]?"+EscapeChars.forRegex(column.getName()+":"+value+""), "");
            }else {
                v = v.replaceAll("\\s*[\\+\\-]?"+EscapeChars.forRegex(column.getName()+":\""+value+"\""), "");
            }
        }
        if(child!=null) {
            v = child.recursiveRemoveSelfAndChild(v);
        }
        return v;
    }
    
    
    public String removeSelf(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        String queryString = request.getQueryString();
        queryString = updateQueryString(queryString);
        if(queryString !=null) {
            String[] name_values = queryString.split("&");
            for(int i=0;i<name_values.length;i++) {
                if(i>0) {
                    sb.append("&");
                }
                String[] name_value = name_values[i].split("=");
                if(name_value==null||name_value.length!=2) continue;
                String name = name_value[0];
                String v = name_value[1];
                if(v==null || v.equals("")) continue;
                if(name.equals("q")) {
                    v = recursiveRemoveSelfAndChild(java.net.URLDecoder.decode(v));
                }
                if(name.equals("lq")) {
                    v = java.net.URLDecoder.decode(v);
                }
                try {
                	if (!name.equals("searchQuery")){
                		sb.append(name).append("=").append(java.net.URLEncoder.encode(v,"utf8"));
                	} else {
                		sb.append(name).append("=").append(v);
                	}
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
    
    private static String updateQueryString(String query) {
        String origQuery = query;
        if (origQuery == null ) origQuery = "";
        StringBuffer sb = new StringBuffer();
        String [] parameters = origQuery.split("&");
        for (int i = 0; i < parameters.length; i++){
           if (parameters[i].trim().startsWith("q=") || 
        	   parameters[i].trim().startsWith("lq=") ||
        	   parameters[i].trim().startsWith("searchQuery=") || 
        	   parameters[i].trim().startsWith("searchType=") ||        	   
        	   parameters[i].trim().startsWith("templateName=") ||
        	   parameters[i].trim().startsWith("indexName=")) {
	    		   sb.append('&');
	    		   sb.append(parameters[i]);
           }
        }
        return sb.toString();
    }    
    
    public static String clearFields(String q, List<FilteredColumn> filteredColumns) {
        if(U.isEmpty(q)) return "";
        for(FilteredColumn fc : filteredColumns) {
            String fieldName = fc.column.getName();
            if(q.indexOf(fieldName)>=0) {
                if(fc.value instanceof RangeFilterValue) {
                    q = q.replaceAll("\\s*[\\+\\-]?"+EscapeChars.forRegex(fieldName+":"+fc.value+""), "");
                }else {
                    q = q.replaceAll("\\s*[\\+\\-]?"+EscapeChars.forRegex(fieldName+":\""+fc.value+"\""), "");
                }
            }
        }
        try {
            return java.net.URLEncoder.encode(q,"utf8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        return q;
    }
}
