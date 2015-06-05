package net.javacoding.xsearch.config;

import java.util.ArrayList;
import java.util.List;

import net.javacoding.xsearch.utility.U;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Class mapped to the <code>&lt;column&gt;</code> element of a dataset
 * configuration file. It describes a database column in a SELECT statement.
 *
 */
@XStreamAlias("column")
public class Column extends ConfigurationComponent implements ConfigConstants {

	public Column() {
		super();
	}
	
	private transient Dataquery dataquery = null;
    public Dataquery getDataquery() {
        return dataquery;
    }
    protected void setDataquery(Dataquery dataquery) {
        this.dataquery = dataquery;
        if (configObject != null) configObject.setDirty(true);
    }
    // ------------------------------------------------------------- Properties

    /** The name of the column. */
    @XStreamAlias("column-name")
    private String columnName;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = (columnName==null?null:columnName.intern());
        if (configObject != null) configObject.setDirty(true);
    }
    /**
     * Same as getColumnName() for convenience
     */
    public String getName() {
        return columnName;
    }

    /** The display name of the column. */
    @XStreamAlias("display-name")
    private String displayName;

    public String getDisplayName() {
        if (U.isEmpty(displayName)) return columnName;
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = (displayName==null?null:displayName.intern());
        if (configObject != null) configObject.setDirty(true);
    }

    /** The index of the column in a SELECT statement starting from 1.*/
    @XStreamAlias("column-index")
    private int columnIndex;

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
        if (configObject != null) configObject.setDirty(true);
    }

    /** The database type of the column. */
    @XStreamAlias("column-type")
    private String columnType;

    /**
     * @return interned column type
     */
    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = (columnType==null?null:columnType.intern());
        if (configObject != null) configObject.setDirty(true);
    }

    /** The database type of the column. */
    @XStreamAlias("column-precision")
    private int columnPrecision;
    public int getColumnPrecision() { return columnPrecision; }
    public void setColumnPrecision(int columnPrecision) {
        this.columnPrecision = columnPrecision;
        if (configObject != null) configObject.setDirty(true);
    }
    /** The database type of the column. */
    @XStreamAlias("column-scale")
    private int columnScale;
    public int getColumnScale() { return columnScale; }
    public void setColumnScale(int columnScale) {
        this.columnScale = columnScale;
        if (configObject != null) configObject.setDirty(true);
    }

    /** <code>true</code> if the column is the primary key. */
    @XStreamAlias("primary-key")
    private boolean isPrimaryKey;

    public boolean getIsPrimaryKey() {
        return isPrimaryKey;
    }

    public void setIsPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
        if (configObject != null) configObject.setDirty(true);
    }

    /** <code>true</code> if the column is the modified date. */
    @XStreamAlias("modified-date")
    private boolean isModifiedDate;

    public boolean getIsModifiedDate() {
        return isModifiedDate;
    }

    public void setIsModifiedDate(boolean isModifiedDate) {
        this.isModifiedDate = isModifiedDate;
        if (configObject != null) configObject.setDirty(true);
    }

    /** <code>true</code> if the column is the unique identifier of a aggregate. */
    @XStreamAlias("aggregate-separator")
    private String aggregateSeparator;

    public String getAggregateSeparator() {
        return (aggregateSeparator==null?"\n":aggregateSeparator);
    }

    public void setAggregateSeparator(String aggregateSeparator) {
        if(U.isEmpty(aggregateSeparator)) {
            this.aggregateSeparator = null;
        }else {
            aggregateSeparator = aggregateSeparator.intern();
            if(aggregateSeparator=="null"||aggregateSeparator=="\\n") {
                this.aggregateSeparator = null;
            }else {
                this.aggregateSeparator = aggregateSeparator;
            }
        }
        if (configObject != null) configObject.setDirty(true);
    }

    /** <code>true</code> if the column needs to be combined. */
    @XStreamAlias("aggregate")
    private boolean isAggregate;

    public boolean getIsAggregate() {
        return isAggregate;
    }

    public void setIsAggregate(boolean isAggregate) {
        this.isAggregate = isAggregate;
        if (configObject != null) configObject.setDirty(true);
    }

    @XStreamAlias("index-field-type")
    private String indexFieldType;

    public String getIndexFieldType() {
        return indexFieldType;
    }

    public void setIndexFieldType(String indexFieldType) {
        this.indexFieldType = (indexFieldType==null?null:indexFieldType.intern());
        if (configObject != null) configObject.setDirty(true);
    }

    @XStreamAlias("is-sortable")
    private boolean isSortable = false;

    public boolean canBeSortable() {
        return IndexFieldType.isKeyword(indexFieldType);
    }
    public boolean getIsSortable() {
        if(!IndexFieldType.isKeyword(indexFieldType)){
            return false;
        }
        return isSortable;
    }

    public void setIsSortable(boolean isSortable) {
        this.isSortable = isSortable;
        if (configObject != null) configObject.setDirty(true);
    }
    
    @XStreamAlias("sort-display-order")
    private int sortDisplayOrder = 0;

    /**
     * @return Returns the sortDisplayOrder.
     */
    public int getSortDisplayOrder() {
        return sortDisplayOrder;
    }
    /**
     * @param sortDisplayOrder The sortDisplayOrder to set.
     */
    public void setSortDisplayOrder(int sortOrder) {
        this.sortDisplayOrder = sortOrder;
        if (configObject != null) configObject.setDirty(true);
    }
    
    @XStreamAlias("sort-descending")
    private boolean isDescending = true; 
    /**
     * @return Returns the isDescending.
     */
    public boolean getIsDescending() {
        return isDescending;
    }
    /**
     * @param isDescending The isDescending to set.
     */
    public void setIsDescending(boolean isDescending) {
        this.isDescending = isDescending;
        if (configObject != null) configObject.setDirty(true);
    }

    @XStreamAlias("is-searchable")
    private boolean isSearchable = true;
    public boolean canBeSearchable() {
        if(IndexFieldType.belongsTo(indexFieldType,IndexFieldType.TEXT) 
                || IndexFieldType.UN_STORED==indexFieldType 
                || IndexFieldType.isKeyword(indexFieldType)){
            return true;
        }
        return false;
    }
    public boolean getIsSearchable() {
        if(canBeSearchable()) {
            return isSearchable;
        }
        return false;
    }

    public void setIsSearchable(boolean isSearchable) {
        this.isSearchable = isSearchable;
        if (configObject != null) configObject.setDirty(true);
    }
    
    /** The weight of the field in search. 
     * Useful when isSearchable is true 
     */
    @XStreamAlias("search-weight")
    private float searchWeight = -1.0f;

    public float getSearchWeight() {
        if(searchWeight<=0) {
            if(isPrimaryKey) {
                return 1000.0f;
            }
            if(IndexFieldType.isKeyword(indexFieldType)) {
                return 3.0f;
            }
            return 1.0f;
        }
        return searchWeight;
    }

    public void setSearchWeight(float weight) {
        this.searchWeight = weight;
        if (configObject != null) configObject.setDirty(true);
    }

    /**
     * Determine if it is Narrow By field 
     */
    @XStreamAlias("is-filterable")
    private boolean isFilterable = false;

    public boolean getIsFilterable() {
        if(canBeFilterable()){
            return isFilterable;
        }
        return false;
    }
    public void setIsFilterable(boolean isFilterable) {
        this.isFilterable = isFilterable;
        if (configObject != null) configObject.setDirty(true);
    }
    
    public boolean getIsKeyword() {
        return IndexFieldType.isKeyword(indexFieldType);
    }
    public boolean getIsSimpleKeyword(){
    	if(indexFieldType==IndexFieldType.KEYWORD|| indexFieldType==IndexFieldType.KEYWORD_BOOST){
    		return true;
    	}
    	if(indexFieldType==IndexFieldType.KEYWORDS){
    		if(this.analyzerName == "com.fdt.sdl.core.analyzer.CommaSemicolonAnalyzer"){
    			return false;
    		}
    		return true;
    	}
    	return false;
    }
    public boolean canBeFilterable() {
        return getIsKeyword();
    }
    @XStreamAlias("filter-has-multiple-keywords")
    private boolean hasMultipleKeywords = false;
    public void setHasMultipleKeywords(boolean hasMultipleKeywords) {
        this.hasMultipleKeywords = hasMultipleKeywords;
        if (configObject != null) configObject.setDirty(true);
    }
    public boolean getHasMultipleKeywords() {
        return hasMultipleKeywords || "Keywords".equals(indexFieldType);
    }
    
    @XStreamAlias("filter-display-order")
    private int filterDisplayOrder = 0 ;

    /**
     * @return Returns the filterDisplayOrder.
     */
    public int getFilterDisplayOrder() {
        return filterDisplayOrder;
    }
    /**
     * @param filterDisplayOrder The filterDisplayOrder to set.
     */
    public void setFilterDisplayOrder(int filterOrder) {
        this.filterDisplayOrder = filterOrder;
        if (configObject != null) configObject.setDirty(true);
    }
    
    @XStreamAlias("filter-parent-column-name")
    private String filterParentColumnName = null;
    
    /**
     * @return Returns the filterParentColumnName.
     */
    public String getFilterParentColumnName() {
        return filterParentColumnName;
    }
    /**
     * @param filterParentColumnName The filterParentColumnName to set.
     */
    public void setFilterParentColumnName(String parentColumnName) {
        this.filterParentColumnName = (parentColumnName==null?null:parentColumnName.intern());
        if (configObject != null) configObject.setDirty(true);
    }
    
    @XStreamAlias("sum-column-names")
    private String[] sumColumnNames = null;

    public String[] getSumColumnNames() {
        return sumColumnNames;
    }
    public List<String> getSumColumnNameList() {
        if(sumColumnNames==null)return null;
        List<String> ret = new ArrayList<String>(sumColumnNames.length);
        for(int i=0;i<sumColumnNames.length;i++) {
            ret.add(sumColumnNames[i]);
        }
        return ret;
    }
    public void setCommaSeparatedSumColumnNames(String sumColumnNames) {
        if(!U.isEmpty(sumColumnNames)) {
            this.sumColumnNames = StringUtils.split(sumColumnNames,',');
        }else {
            this.sumColumnNames = null;
        }
        if(this.sumColumnNames!=null) {
            for(int i=0;i<this.sumColumnNames.length;i++) {
                this.sumColumnNames[i] = this.sumColumnNames[i].intern();
            }
        }
        if (configObject != null) configObject.setDirty(true);
    }
    public void setSumColumnNames(String[] sumColumnNames) {
        this.sumColumnNames = sumColumnNames;
        if(this.sumColumnNames!=null) {
            for(int i=0;i<this.sumColumnNames.length;i++) {
                this.sumColumnNames[i] = this.sumColumnNames[i].intern();
            }
        }
        if (configObject != null) configObject.setDirty(true);
    }
    //only for backward compatibility
    public void setSumColumnName(String sumColumnName) {
        this.sumColumnNames = new String[] {sumColumnName};
        if (configObject != null) configObject.setDirty(true);
    }

    @XStreamAlias("filter-facet-type-name")
    private String filterFacetTypeName = null;
    public String getFilterFacetTypeName() {
        return filterFacetTypeName;
    }
    public void setFilterFacetTypeName(String facetTypeName) {
        this.filterFacetTypeName = (facetTypeName==null?null:facetTypeName.intern());
        if (configObject != null) configObject.setDirty(true);
    }

    public static byte SortFilterCountsByCount           = 0;
    public static byte SortFilterCountsByValueDescending = 1;
    public static byte SortFilterCountsByValueAscending  = 2;
    public static byte SortFilterCountsBySumDescending   = 3;
    public static byte SortFilterCountsBySumAscending    = 4;
    public static byte SortFilterCountsByAvgDescending   = 5;
    public static byte SortFilterCountsByAvgAscending    = 6;
    public static byte SortFilterNoSorting               = 100;
    @XStreamAlias("sort-filter-counts-by")//use K for historical reason, because we shipped a version with K already
    private byte sortFilterCountsBy = SortFilterCountsByCount;
    public byte getSortFilterCountsBy() {
        return sortFilterCountsBy;
    }
    public void setSortFilterCountsBy(byte sortFilterCountsBy) {
    	this.sortFilterCountsBy = sortFilterCountsBy;
        if (configObject != null) configObject.setDirty(true);
    }

    public boolean canBeDisplayable() {
        return IndexFieldType.belongsTo(indexFieldType,IndexFieldType.TEXT) || IndexFieldType.belongsTo(indexFieldType,IndexFieldType.UN_INDEXED) || IndexFieldType.isKeyword(indexFieldType);
    }
    
    // --------------------------------------------------------- Public Methods

    @XStreamAlias("analyzer-name")
    protected String analyzerName = null;
    
    public String getAnalyzerName() {
        return analyzerName;
    }
    public void setAnalyzerName(String analyzer) {
        this.analyzerName = (analyzer==null? null : analyzer.intern());
        if (configObject != null) configObject.setDirty(true);
    }

    @XStreamAlias("need-synonyms-and-stopwords")
    protected boolean needSynonymsAndStopwords = false;
    
    public boolean getNeedSynonymsAndStopwords() {
        return needSynonymsAndStopwords;
    }
    public void setNeedSynonymsAndStopwords(boolean needSynonymsAndStopwords) {
        this.needSynonymsAndStopwords = needSynonymsAndStopwords;
        if (configObject != null) configObject.setDirty(true);
    }

    @XStreamAlias("is-secure")
    private boolean isSecure = false;
    public boolean getIsSecure() {
        return isSecure;
    }
    public void setIsSecure(boolean secure) {
        isSecure = secure;
        if (configObject != null) configObject.setDirty(true);
    }

    @XStreamAlias("is-spell-checking")
    private boolean isSpellChecking = false;
    public boolean getIsSpellChecking() {
        return isSpellChecking;
    }
    public void setIsSpellChecking(boolean isSpellChecking) {
        this.isSpellChecking = isSpellChecking;
        if (configObject != null) configObject.setDirty(true);
    }
    public boolean canBeSpellChecking() {
        if(IndexFieldType.belongsTo(indexFieldType, IndexFieldType.TEXT)
                ||IndexFieldType.isKeyword(indexFieldType)&&getColumnTypeShortName()=="String"){
            return true;
        }
        return false;
    }

    @XStreamAlias("is-date-weight")
    private boolean isDateWeight = false;
    public boolean canBeDateWeight() {
        return getIsDate();
    }
    public boolean getIsDateWeight() {
        return isDateWeight;
    }
    public void setIsDateWeight(boolean isDateWeight) {
    	if(canBeDateWeight()){
            this.isDateWeight = isDateWeight;
    	}
        if (configObject != null) configObject.setDirty(true);
    }

    @XStreamAlias("tag")
    protected String tag = null;
    
    public String getTag() {
        return tag==null? "" : tag;
    }
    public void setTag(String tag) {
        this.tag = (tag==null? null : tag.intern());
        if (configObject != null) configObject.setDirty(true);
    }
    
    /**
     * Fill the newly created Column with values from "old" Column
     * Used in Configuration history.
     */
    public void merge(Column old) {
        this.setDisplayName(old.getDisplayName());
        this.setColumnType(old.getColumnType());
        this.setColumnPrecision(old.getColumnPrecision());
        this.setColumnScale(old.getColumnScale());
        this.setIsPrimaryKey(old.getIsPrimaryKey());
        this.setIsModifiedDate(old.getIsModifiedDate());
        this.setAggregateSeparator(old.getAggregateSeparator());
        this.setIsAggregate(old.getIsAggregate());
        this.setIndexFieldType(old.getIndexFieldType());
        this.setIsSortable(old.getIsSortable());
        this.setSortDisplayOrder(old.getSortDisplayOrder());
        this.setIsDescending(old.getIsDescending());
        this.setIsSearchable(old.getIsSearchable());
        this.setSearchWeight(old.getSearchWeight());
        this.setIsFilterable(old.getIsFilterable());
        this.setHasMultipleKeywords(old.getHasMultipleKeywords());
        this.setFilterDisplayOrder(old.getFilterDisplayOrder());
        this.setFilterParentColumnName(old.getFilterParentColumnName());
        this.setSumColumnNames(old.getSumColumnNames());
        this.setFilterFacetTypeName(old.getFilterFacetTypeName());
        this.setSortFilterCountsBy(old.getSortFilterCountsBy());
        this.setAnalyzerName(old.getAnalyzerName());
        this.setNeedSynonymsAndStopwords(old.getNeedSynonymsAndStopwords());
        this.setIsSecure(old.getIsSecure());
        this.setIsSpellChecking(old.getIsSpellChecking());
        this.setIsDateWeight(old.getIsDateWeight());
        this.setTag(old.getTag());
    }
    
    /**
     * Returns an XML representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("    <column>\n");

        if (columnName != null) {
            sb.append("      <column-name>").append(columnName).append("</column-name>\n");
        }

        if (displayName != null) {
            sb.append("      <display-name><![CDATA[").append(displayName).append("]]></display-name>\n");
        }

        sb.append("      <column-index>").append(columnIndex).append("</column-index>\n");

        if (columnType != null) {
            if(getIsNumber()&&columnPrecision>0) {
                sb.append("      <column-type precision=\"").append(columnPrecision).append("\"");
                sb.append(" scale=\"").append(columnScale).append("\"");
                sb.append(">");
                sb.append(columnType).append("</column-type>\n");
            }else {
                sb.append("      <column-type>").append(columnType).append("</column-type>\n");
            }
        }

        if( isPrimaryKey==true ) {
            sb.append("      <primary-key>").append(isPrimaryKey).append("</primary-key>\n");
        }

        if( isModifiedDate == true ) {
            sb.append("      <modified-date>").append(isModifiedDate).append("</modified-date>\n");
        }

        if( isDateWeight==true ) {
            sb.append("      <is-date-weight>").append(isDateWeight).append("</is-date-weight>\n");
        }

        if( isAggregate == true ) {
            if(U.isEmpty(aggregateSeparator) || "null".intern()==aggregateSeparator) {
                sb.append("      <aggregate>").append(isAggregate).append("</aggregate>\n");
            }else {
                sb.append("      <aggregate separator=\"").append(aggregateSeparator).append("\">").append(isAggregate).append("</aggregate>\n");
            }
        }

        if (indexFieldType != null) {
        	if (indexFieldType.equalsIgnoreCase(IndexFieldType.KEYWORD)) {
        		this.analyzerName = null;
        	}
            sb.append("      <index-field-type>").append(indexFieldType).append("</index-field-type>\n");
        }

        if ( isSortable || sortDisplayOrder != 0 || isDescending!=true) {
            sb.append("      <is-sortable");
            if(sortDisplayOrder!=0) {
                sb.append(" sort-display-order=\"").append(sortDisplayOrder).append("\"");
            }
            if(isDescending!=true) {
                sb.append(" descending=\"").append(isDescending).append("\"");
            }
            sb.append(">").append(isSortable).append("</is-sortable>\n");
        }

        if ( !isSearchable || searchWeight!=1.0f&&searchWeight>0) {
            sb.append("      <is-searchable");
            if(searchWeight!=1.0f){
                sb.append(" search-weight=\"").append(searchWeight).append("\"");
            }
            sb.append(">").append(isSearchable).append("</is-searchable>\n");
        }

        if ( isFilterable || filterDisplayOrder != 0 || filterParentColumnName!=null) {
            sb.append("      <is-filterable");
            if(filterDisplayOrder!=0) {
                sb.append(" filter-display-order=\"").append(filterDisplayOrder).append("\"");
            }
            if(filterParentColumnName!=null) {
                sb.append(" filter-parent-column-name=\"").append(filterParentColumnName).append("\"");
            }
            if(sumColumnNames!=null) {
                sb.append(" sum-column-names=\"").append(StringUtils.join(sumColumnNames,',')).append("\"");
            }
            if(hasMultipleKeywords!=false) {
                sb.append(" has-multiple-keywords=\"").append(hasMultipleKeywords).append("\"");
            }
            if(sortFilterCountsBy!=SortFilterCountsByCount){
                sb.append(" sort-filter-counts-by=\"").append(sortFilterCountsBy).append("\"");
            }
            if(filterFacetTypeName!=null) {
                sb.append(" filter-facet-type-name=\"").append(filterFacetTypeName).append("\"");
            }

            sb.append(">").append(isFilterable).append("</is-filterable>\n");
        }
        
        if ( !U.isEmpty(analyzerName)) {
            sb.append("      <analyzer-name>").append(analyzerName).append("</analyzer-name>\n");
        }
        
        if (needSynonymsAndStopwords == true) {
            sb.append("      <need-synonyms-and-stopwords>").append(needSynonymsAndStopwords).append("</need-synonyms-and-stopwords>\n");
        }

        if (isSecure == true) {
            sb.append("      <is-secure>").append(isSecure).append("</is-secure>\n");
        }
        
        if (isSpellChecking == true) {
            sb.append("      <is-spell-checking>").append(isSpellChecking).append("</is-spell-checking>\n");
        }
        if (tag != null) {
            sb.append("      <tag><![CDATA[").append(tag).append("]]></tag>\n");
        }

        sb.append("    </column>\n");

        return sb.toString();
    }

    public String getColumnTypeShortName() {
        if(columnType==null) return "";
        if(columnType == "java.math.BigDecimal") return "Number";
        int idx = columnType.lastIndexOf('.')+1;
        return columnType.substring(idx).intern();
    }
    public boolean getIsText() {
        String t = getColumnTypeShortName();
        return t=="String" || t=="Clob" || t=="Blob";
    }
    public boolean getIsDate() {
        String t = getColumnTypeShortName();
        return t=="Date" || t=="Timestamp" || t=="Time";
    }
    public boolean getIsNumber() {
        String t = getColumnTypeShortName();
        return t=="Number"||t=="Double"||t=="Float";
    }
}
