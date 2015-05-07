package net.javacoding.xsearch.search.result.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.facet.FacetRange;
import net.javacoding.xsearch.config.facet.FacetType;
import net.javacoding.xsearch.config.facet.NumberFacet;
import net.javacoding.xsearch.foundation.WebserverStatic;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * This class hold a facet that counts on all possible values
 */
public class FilterColumn{
	public Column column;
	Map<Object, Count> counts;
	transient private boolean rendered;
	
	private int maxInt = Integer.MIN_VALUE;
	private int minInt = Integer.MAX_VALUE;
	
	FacetType facetChoice;
	public FilterColumn(Column c){
		this.column = c;
		this.counts = new HashMap<Object, Count>();
		this.facetChoice = WebserverStatic.getFacetTypes().getFacetType(c.getFilterFacetTypeName());
	}
	public FilterColumn setRendered() {
	    this.rendered = true;
	    return this;
	}
	public boolean getRendered() {
	    return this.rendered;
	}
	public int getMaxIntegerValue() {
	    return maxInt;
	}
	public int getMinIntegerValue() {
	    return minInt;
	}
	
    /**
     * Used during search, not for rendering
     */
	public Count increaseCountByDate(String v, int[] sum) {
        Count count = (Count) counts.get(v);
        if (count == null) {
            count = new Count(this.column,(String)v);
            count.increaseAndSum(sum);
            counts.put(v, count);
        }else {
            count.increaseAndSum(sum);
        }
        return count;
	}
	
    /**
     * Used during search, not for rendering
     */
    public void increaseCountByInt(int v, int[] sum){
        if(v < minInt) {
            minInt = v;
        }
        if(v > maxInt) {
            maxInt = v;
        }
        if(this.facetChoice!=null) {
            if(this.facetChoice instanceof NumberFacet) {
                FacetRange r = ((NumberFacet)this.facetChoice).findFacetRange(v);
                if(r==null) return;
                Count count = (Count) counts.get(r);
                if (count == null) {
                    count = new Count(this.column,(FacetRange)r);
                    counts.put(r, count);
                }
                count.increaseAndSum(sum);
            }
        } else {
            Count count = (Count) counts.get(v);
            if (count == null) {
                count = new Count(this.column,v);
                counts.put(v, count);
            }
            count.increaseAndSum(sum);
        }
    }

    /**
	 * Used during search, not for rendering
	 */
	public Count increaseCountByString(String v, int[] sum){
		Count count = (Count) counts.get(v);
		if (count == null) {
			count = new Count(this.column,v);
            count.increaseAndSum(sum);
            counts.put(v, count);
		}else {
		    count.increaseAndSum(sum);
		}
		return count;
	}
	/**
	 * @return a list of counts ordered by column values
	 */
	public ArrayList<Count> getTopCounts(int x){
        ArrayList<Count> cvc = new ArrayList<Count>();
        Count[] columnValueCounts = (Count[]) counts.values().toArray(new Count[counts.size()]);
        Arrays.sort(columnValueCounts, new Comparator<Count>(){public int compare(Count o1, Count o2) {return o2.getValue()-o1.getValue();} });
        int length = x < columnValueCounts.length ? x : columnValueCounts.length;
        Count[] topColumnValueCounts = new Count[length];
        for(int i=0;i<length;i++) {
            topColumnValueCounts[i] = columnValueCounts[i];
        }
        if(column.getIsNumber()){
            Arrays.sort(topColumnValueCounts, Count.getColumnNumberAscendingComparator());
        }else{
            Arrays.sort(topColumnValueCounts, Count.getColumnStringAscendingComparator());
        }
        for (int j = 0; j < length; j++) {
            cvc.add(topColumnValueCounts[j]);                  // re-build list, now sorted
        }
        return cvc;
	}
	public ArrayList<Count> getCounts(){
        if(column.getSortFilterCountsBy()==Column.SortFilterCountsByValueDescending){
        	if(column.getIsNumber()){
                return sortCountsBy(Count.getColumnNumberDescendingComparator());
        	}else{
                return sortCountsBy(Count.getColumnStringDescendingComparator());
        	}
        }else if(column.getSortFilterCountsBy()==Column.SortFilterCountsByValueAscending){
        	if(column.getIsNumber()){
                return sortCountsBy(Count.getColumnNumberAscendingComparator());
        	}else{
                return sortCountsBy(Count.getColumnStringAscendingComparator());
        	}
        }else if(column.getSortFilterCountsBy()==Column.SortFilterCountsBySumDescending){
            return sortCountsBy(Count.getColumnSumDescendingComparator());
        }else if(column.getSortFilterCountsBy()==Column.SortFilterCountsBySumAscending){
            return sortCountsBy(Count.getColumnSumAscendingComparator());
        }else if(column.getSortFilterCountsBy()==Column.SortFilterCountsByAvgDescending){
            return sortCountsBy(Count.getColumnAvgDescendingComparator());
        }else if(column.getSortFilterCountsBy()==Column.SortFilterCountsByAvgAscending){
            return sortCountsBy(Count.getColumnAvgAscendingComparator());
        }else if(column.getSortFilterCountsBy()==Column.SortFilterNoSorting){
            //No Sorting, just skipping
        }else if(column.getSortFilterCountsBy()==Column.SortFilterCountsByCount){
            return sortCountsBy(Count.getCountDescendingComparator());
        }
        return sortCountsBy(null);
	}
    public ArrayList<Count> getCountsOrderByDescendingSum(String columnName){
        return sortCountsBy(Count.getColumnSumDescendingComparator(columnName));
    }
    public ArrayList<Count> getCountsOrderByAscendingSum(String columnName){
        return sortCountsBy(Count.getColumnSumAscendingComparator(columnName));
    }
    public ArrayList<Count> getCountsOrderByDescendingAverage(String columnName){
        return sortCountsBy(Count.getColumnAvgDescendingComparator(columnName));
    }
    public ArrayList<Count> getCountsOrderByAscendingAverage(String columnName){
        return sortCountsBy(Count.getColumnAvgAscendingComparator(columnName));
    }

    private ArrayList<Count> sortCountsBy(Comparator<Count> comparator) {
        Count[] columnValueCounts = (Count[]) counts.values().toArray(new Count[counts.size()]);
        if(comparator!=null) {
            Arrays.sort(columnValueCounts, comparator);
        }
        ArrayList<Count> cvc = new ArrayList<Count>();
        for (int j = 0; j < columnValueCounts.length; j++) {
            cvc.add(columnValueCounts[j]);                  // re-build list, now sorted
        }
        return cvc;
    }
    public Column getColumn() {
        return column;
    }
    public JSONObject toJSONObject() {
        JSONObject self = new JSONObject();
        try {
            self.put("column", column.getDisplayName());
            JSONArray jcounts = new JSONArray();
            for(Map.Entry<Object, Count> e : counts.entrySet()) {
                jcounts.put(e.getValue().toJSONObject());
            }
            self.put("counts", jcounts);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return self;
    }
    
}