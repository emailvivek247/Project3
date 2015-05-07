package net.javacoding.xsearch.search.result.filter;

import java.util.Comparator;
import java.util.List;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.facet.FacetRange;
import net.javacoding.xsearch.utility.U;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Count has two parts
 * <ol>
 *   <li> String columnValue : usually the displayed part for this category
 *   <lI> int value : the count for this category
 * </ol>
 * For example, if in a music album search, the "type" category has possible columnValue
 * like "classic", "rock", etc. So the possible Count object can be
 *   <ul>
 *     <li>Count(columnValue="classic", value=250)
 *     <li>Count(columnValue="rock",    value=150)
 *     <li>...
 *   </ul>
 * 
 */
public class Count {
    public String columnName;
	public String columnValue;
    public String columnEndValue;
	public int value;

	public boolean hasSum = false;
    public List<String> sumColumns;
    public int[] sums;

    public boolean isFiltered = false;
    
    public boolean isRangeSearch = false;

    public Count(Column column, Integer columnValue) {
        this.columnName = column.getColumnName();
        this.columnValue = Integer.toString(columnValue).intern();
        this.isRangeSearch = false;
        this.sumColumns = column.getSumColumnNameList();
    }
	public Count(Column column, String columnValue) {
        this.columnName = column.getColumnName();
		this.columnValue = columnValue==null? null : columnValue.intern();
        this.isRangeSearch = false;
        this.sumColumns = column.getSumColumnNameList();
	}

    public Count(Column column, FacetRange facetRange) {
        this.columnName = column.getColumnName();
        this.columnValue = facetRange.toBeginValue();
        this.columnEndValue = facetRange.toEndValue();
        this.isRangeSearch = true;
        this.sumColumns = column.getSumColumnNameList();
    }

    public void setColumnValue(String columnValue) {
        this.columnValue = columnValue==null? null : columnValue.intern();
	}

    /**
     * @return current columnName
     */
    public String getColumnName() {
        return columnName;
    }
    /**
     * @return current columnValue
     */
	public String getColumnValue() {
		return columnValue;
	}
    public String getColumnEndValue() {
        return columnEndValue;
    }
	
	/**
	 * @return a well formated query
	 */
	public String getQuery() {
	    if(columnEndValue!=null) {
	        return this.columnName+":["+this.columnValue+","+this.columnEndValue+")";
	    }else {
            return this.columnName+":\""+this.columnValue+"\"";
	    }
	}
    public String getColumnValueString() {
        if(columnEndValue!=null) {
            return "["+this.columnValue+","+this.columnEndValue+")";
        }else {
            return this.columnValue;
        }
    }

	/**
	 * @return the count value for current columnValue
	 */
	public int getValue() {
		return value;
	}

	public int increaseAndSum(int[] sum) {
		value++;
        if(sum!=null) {
            hasSum=true;
            if(this.sums==null) {
                this.sums = new int[this.sumColumns.size()];
            }
            for(int i=0;i<sums.length&&i<sum.length;i++) {
                this.sums[i] += sum[i];
            }
        }
		return value;
	}

	public boolean getHasSum() {
	    return hasSum;
	}
	
	public List<String> getSumColumnNames(){
	    return this.sumColumns;
	}
	
	/**
	 * Usage: cCount.sum("columnA")
	 * @param columnName the other column to sum on. Need to configure it on filterableColumns page first.
	 */
    public int sum(String columnName){
        if(sums==null||sums.length<1||columnName==null) return 0;
        columnName = columnName.intern();
        for(int i=0;i<sumColumns.size();i++) {
            if(columnName==sumColumns.get(i)) {
                return sums[i];
            }
        }
        return 0;
    }
    /**
     * Usage: cCount.average("columnA")
     * @param columnName the other column to average on. Need to configure it on filterableColumns page first.
     */
    public float average(String columnName){
        if(sums==null||sums.length<1||columnName==null) return 0;
        columnName = columnName.intern();
        for(int i=0;i<sumColumns.size();i++) {
            if(columnName==sumColumns.get(i)) {
                return sums[i]*1.0f/value;
            }
        }
        return 0;
    }
    /**
     * exactly the same as average(columnName)
     */
    public float avg(String columnName){
        return average(columnName);
    }

    /**
	 * @deprecated
	 */
	public int getSum(){
	    if(sums==null||sums.length<1) return 0;
		return sums[0];
	}
    /**
     * @deprecated
     */
    public int getAverage(){
        if(sums==null||sums.length<1) return 0;
        return sums[0]/value;
    }

    public JSONObject toJSONObject() {
        JSONObject self = new JSONObject();
        try {
            self.put(this.getColumnValueString(), this.value);
            if(this.sums!=null) {
                JSONObject sumsObject = new JSONObject();
                for(int i=0;i<sums.length&&i<this.sumColumns.size();i++) {
                    sumsObject.put(this.sumColumns.get(i), this.sums[i]);
                }
                self.put("sums", sumsObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return self;
    }

    /**
     * This is useful for multi-valued columns, like tags, to highlight selected tags, or to avoid re-displaying it. 
     */
    public boolean getIsFiltered() {
        return isFiltered;
    }
    public boolean getIsRangeSearch() {
        return this.isRangeSearch;
    }
    /**
     * @return column value ascending comparator 
     */
    public static Comparator<Count> getColumnNumberAscendingComparator(){
        return new Comparator<Count>(){public int compare(Count o1, Count o2) {
            if(o1.columnValue=="") return -1;
            if(o1.columnEndValue=="") return 1;
            if(o2.columnValue=="") return 1;
            if(o2.columnEndValue=="") return -1;
            return (int)(U.getFloat(o1.columnValue, 0) - U.getFloat(o2.columnValue, 0));
        }};
    }
    public static Comparator<Count> getColumnSumAscendingComparator(){
        return new Comparator<Count>(){public int compare(Count o1, Count o2) {
            return o1.getSum() - o2.getSum();
        }};
    }
    public static Comparator<Count> getColumnAvgAscendingComparator(){
        return new Comparator<Count>(){public int compare(Count o1, Count o2) {
            return o1.getAverage() - o2.getAverage();
        }};
    }
    public static Comparator<Count> getColumnSumAscendingComparator(final String columnName){
        return new Comparator<Count>(){public int compare(Count o1, Count o2) {
            return o1.sum(columnName) - o2.sum(columnName);
        }};
    }
    public static Comparator<Count> getColumnAvgAscendingComparator(final String columnName){
        return new Comparator<Count>(){public int compare(Count o1, Count o2) {
            float delta = o1.average(columnName) - o2.average(columnName);
            return delta > 0 ? 1 : delta < 0 ? -1 : 0;
        }};
    }
    public static Comparator<Count> getColumnStringAscendingComparator(){
        return new Comparator<Count>(){public int compare(Count o1, Count o2) {
            if(o1.columnValue==null) return 1;
            if(o2.columnValue==null) return -1;
            return o1.columnValue.compareTo(o2.columnValue);
        }};
    }
    /**
     * @return column value ascending comparator 
     */
    public static Comparator<Count> getColumnNumberDescendingComparator(){
        return new Comparator<Count>(){public int compare(Count o1, Count o2) {
            if(o1.columnValue=="") return 1;
            if(o1.columnEndValue=="") return -1;
            if(o2.columnValue=="") return -1;
            if(o2.columnEndValue=="") return 1;
            return (int)(U.getFloat(o2.columnValue, 0) - U.getFloat(o1.columnValue, 0));
        }};
    }
    public static Comparator<Count> getColumnSumDescendingComparator(final String columnName){
        return new Comparator<Count>(){public int compare(Count o1, Count o2) {
            return o2.sum(columnName) - o1.sum(columnName);
        }};
    }
    public static Comparator<Count> getColumnAvgDescendingComparator(final String columnName){
        return new Comparator<Count>(){public int compare(Count o1, Count o2) {
            float delta = o2.average(columnName) - o1.average(columnName);
            return delta > 0 ? 1 : delta < 0 ? -1 : 0;
        }};
    }
    public static Comparator<Count> getColumnSumDescendingComparator(){
        return new Comparator<Count>(){public int compare(Count o1, Count o2) {
            return o2.getSum() - o1.getSum();
        }};
    }
    public static Comparator<Count> getColumnAvgDescendingComparator(){
        return new Comparator<Count>(){public int compare(Count o1, Count o2) {
            return o2.getAverage() - o1.getAverage();
        }};
    }
    public static Comparator<Count> getColumnStringDescendingComparator(){
        return new Comparator<Count>(){public int compare(Count o1, Count o2) {
            if(o1.columnValue==null) return -1;
            if(o2.columnValue==null) return 1;
            return o2.columnValue.compareTo(o1.columnValue);
        }};
    }
    /**
     * @return column value ascending comparator 
     */
    public static Comparator<Count> getCountDescendingComparator(){
        return new Comparator<Count>(){public int compare(Count o1, Count o2) {
            if(o1==null) return 1;
            if(o2==null) return -1;
            return o2.getValue()-o1.getValue();
        }};
    }
}
