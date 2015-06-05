package net.javacoding.xsearch.search.result.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.IndexFieldType;
import net.javacoding.xsearch.search.analysis.AdvancedQueryAnalysis;

import org.codehaus.jettison.json.JSONArray;

public class FilterResult{
	public ArrayList<FilterColumn> filterColumns;
	public ArrayList<FilteredColumn> filteredColumns;
	public FilterResult() {
        filterColumns = new ArrayList<FilterColumn>();
        filteredColumns = new ArrayList<FilteredColumn>();
	}
	public void setFilterColumns(ArrayList<Column> columns){
		for(Column c: columns){
			filterColumns.add(new FilterColumn(c));
		}
	}

	public List<FilterColumn> getFilterColumns(){
	    List<FilterColumn> ret = new ArrayList<FilterColumn>(filterColumns.size());
	    for(FilterColumn fc : filterColumns) {
	        if(!fc.getRendered()) {
	            ret.add(fc);
	        }
	    }
		return ret;
	}
    public FilterColumn getFilterColumn(String columnName) {
        for(FilterColumn fc : filterColumns) {
            if(fc.column.getName().equalsIgnoreCase(columnName)) {
                return fc;
            }
        }
        return null;
    }
    public List<FilteredColumn> getFilteredColumns(){
        return filteredColumns;
    }
    public FilteredColumn getFilteredColumn(String columnName) {
        for(FilteredColumn c : filteredColumns) {
            if(c.column.getName().equalsIgnoreCase(columnName)) {
                return c;
            }
        }
        return null;
    }
    public void addFilteredColumn(FilteredColumn c) {
        for(FilteredColumn fc : filteredColumns) {
            if(fc.column.getFilterParentColumnName()==c.getColumn().getName()) {
                c.child = fc;
            }else if(c.column.getFilterParentColumnName()==fc.column.getName()) {
                fc.child = c;
            }
        }
        filteredColumns.add(c);
    }
	public boolean hasFilteredColumn(String columnName) {
	    for(FilteredColumn c : filteredColumns) {
	        if(c.column.getName().equalsIgnoreCase(columnName)) {
	            return true;
	        }
	    }
	    return false;
	}
	/**
	 * Used during searching. Not for rendering.
	 */
	public void finish() {
	    //mark Count filtered
        for(FilteredColumn filtered : filteredColumns) {
            for(FilterColumn filter : filterColumns) {
                if(filtered.column.getName().equals(filter.column.getName())) {
                    for(Count c : filter.getCounts()) {
                        if(filtered.value.equals(c.columnValue,c.columnEndValue)) {
                            c.isFiltered = true;
                        }
                    }
                }
            }
        }
        //for duplicated filtered columns, if it's hierarchical date, set the parent-child relationship
        int length = filteredColumns.size();
        for(int i=0;i<length;i++) {
            if(filteredColumns.get(i).column.getIndexFieldType()==IndexFieldType.KEYWORD_DATE_HIERARCHICAL) {
                for(int j=i+1;j<length;j++) {
                    if(filteredColumns.get(j).column == filteredColumns.get(i).column) {
                        markParentFilteredColumn(filteredColumns.get(i),filteredColumns.get(j),AdvancedQueryAnalysis.yearPattern, AdvancedQueryAnalysis.yearAndMonthPattern);
                    }
                }
            }
        }
	}
	private void markParentFilteredColumn(FilteredColumn a, FilteredColumn b, Pattern parentPattern, Pattern childPattern) {
        if(a.value.matches(parentPattern)&&b.value.matches(childPattern)) {
            a.child = b;
        }else if(a.value.matches(childPattern)&&b.value.matches(parentPattern)) {
            b.child = a;
        }
	}
	/**
	 * Provided just for backward compatibility. Use getFilterColumns() to loop through the filtered results
	 * @deprecated
	 */
    public ArrayList<ArrayList<Count>> getColumnCounts() {
        ArrayList<ArrayList<Count>> columnCounts = new ArrayList<ArrayList<Count>>();
        for(FilterColumn fc : filterColumns) {
            columnCounts.add(fc.getCounts());
        }
        return columnCounts;
    }
    /**
     * Provided just for backward compatibility.
     * @deprecated
     */
    public List<String> getFilteredColumnNames() {
        ArrayList<String> columnNames = new ArrayList<String>();
        for(FilteredColumn fc : filteredColumns) {
            columnNames.add(fc.column.getColumnName().toLowerCase());
        }
        return columnNames;
    }
    public JSONArray toJSONArray() {
        return toJSONArray(filterColumns);
    }
    public static JSONArray toJSONArray(List<FilterColumn> l) {
        JSONArray ret = new JSONArray();
        if(l!=null) {
            for(FilterColumn c : l) {
                ret.put(c.toJSONObject());
            }
        }
        return ret; 
    }
}