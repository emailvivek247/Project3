/*
 * Copyright 2004.
 */

package net.javacoding.xsearch.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import net.javacoding.xsearch.core.task.work.subsequent.FetchDocumentBySQLTask;
import net.javacoding.xsearch.foundation.QuerySampleValues;
import net.javacoding.xsearch.utility.DBTool;
import net.javacoding.xsearch.utility.U;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Class mapped to the <code>&lt;dataquery&gt;</code> element of a dataset
 * configuration file.
 */
@XStreamAlias("dataquery")
public class Dataquery extends ConfigurationComponent implements ConfigConstants {

    // ----------------------------------------------------- Instance Variables

    /** Parameter values for the SQL query. */
    @XStreamImplicit
    protected ArrayList<Parameter> parameters = null;

    /** Columns in the SELECT statement of the SQL query. */
    @XStreamImplicit
    protected ArrayList<Column> columns = null;

    // ----------------------------------------------------------- Constructors

    public Dataquery() {
        parameters = new ArrayList<Parameter>();
        columns = new ArrayList<Column>();
    }

   // ------------------------------------------------------------- Properties

    /** The name of the dataquery. */
    @XStreamAsAttribute
    protected String name = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setDirty();
    }
    
    /** The name of the data source. */
    @XStreamAlias("data-source-name")
    protected String dataSourceName = null;

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
        setDirty();
    }

    /** The SQL query. */
    protected String sql = null;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
        setDirty();
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Returns the list of SQL query parameters.
     */
    public ArrayList<Parameter> getParameters() {
        return parameters;
    }

    /**
     * Add a parameter.
     *
     * @param parameter the parameter to be added
     */
    public void addParameter(Parameter parameter) {
        parameters.add(parameter);
        parameter.setConfigObject(configObject);
        setDirty();
    }

    /**
     * Removes all the parameters.
     */
    public void clearParameters() {
        parameters.clear();
        setDirty();
    }

    /**
     * Returns the list of SQL query columns.
     */
    public ArrayList<Column> getColumns() {
        return columns;
    }

    /**
     * Loop through all columns to return the SQL query column by name.
     */
    public Column getColumn(String columnName) {
        if (columnName == null) return null;
        for (Column c : columns ) {
            if (c != null && columnName.equalsIgnoreCase(c.getColumnName())) {
                return c;
            }
        }
        return null;
    }

    /**
     * Adds a column.
     *
     * @param column the column to be added
     */
    public void addColumn(Column column) {
        column.setDataquery(this);
        columns.add(column);
        column.setConfigObject(configObject);
        setDirty();
    }
    
    public Column findColumn(String columnName){
    	for(Column c : columns){
    		if(columnName==c.getColumnName()){
    			return c;
    		}
    	}
    	return null;
    }

    /**
     * Removes all the columns.
     */
    public void clearColumns() {
        columns.clear();
        setDirty();
    }

    @XStreamAsAttribute
    @XStreamAlias("is-cache-needed")
    private boolean isCacheNeeded = false;
    public boolean getIsCacheNeeded() {
        return isCacheNeeded;
    }
    public void setIsCacheNeeded(boolean isCacheNeeded) {
        this.isCacheNeeded = isCacheNeeded;
        setDirty();
    }

    @XStreamAsAttribute
    @XStreamAlias("is-batch-needed")
    private boolean isBatchNeeded = false;
    public boolean getIsBatchNeeded() {
        return isBatchNeeded;
    }
    public void setIsBatchNeeded(boolean isBatchNeeded) {
        this.isBatchNeeded = isBatchNeeded;
        setDirty();
    }
    
    @XStreamAsAttribute
    @XStreamAlias("batch-size")
    private int batchSize = 1000;
    public int getBatchSize() {
        return batchSize;
    }
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        setDirty();
    }

    @XStreamAsAttribute
    @XStreamAlias("is-skipping-null-parameters")
    private boolean isSkippingNullParameters = true;
    public boolean getIsSkippingNullParameters() {
        return isSkippingNullParameters;
    }
    public void setIsSkippingNullParameters(boolean isSkippingNullParameters) {
        this.isSkippingNullParameters = isSkippingNullParameters;
        setDirty();
    }

    /**
     * for deletion dataquery only, this is not a good design, isn't it?
     */
    @XStreamAsAttribute
    @XStreamAlias("is-delete-only")
    private boolean isDeleteOnly = false;
    public boolean getIsDeleteOnly() {
        return isDeleteOnly;
    }
    public void setIsDeleteOnly(boolean isDeleteOnly) {
        this.isDeleteOnly = isDeleteOnly;
        setDirty();
    }
    /**
     * Returns an XML representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("  <dataquery");
        if (name != null) {
            sb.append(" name=\"").append(name).append("\"");
        }
        if (dataSourceName != null) {
            sb.append(" data-source-name=\"").append(dataSourceName).append("\"");
        }
        if (isCacheNeeded) {
            sb.append(" is-cache-needed=\"").append(isCacheNeeded).append("\"");
        }
        if (isBatchNeeded){
        	sb.append(" is-batch-needed=\"").append(isBatchNeeded).append("\"");
        }
        if (isDeleteOnly) {
            sb.append(" is-delete-only=\"").append(isDeleteOnly).append("\"");
        }
        if (batchSize != 1000) {
            sb.append(" batch-size=\"").append(batchSize).append("\"");
        }
        if (isSkippingNullParameters == false) {
            sb.append(" is-skipping-null-parameters=\"").append(isSkippingNullParameters).append("\"");
        }
        sb.append(">\n");

        if (sql != null) {
            sb.append("    <sql><![CDATA[\n").append(sql).append("\n    ]]></sql>\n");
        }

        if (parameters != null) {
            for (int i = 0; i < parameters.size(); i++) {
                Parameter p = (Parameter)parameters.get(i);
                if (p != null) {
                    sb.append(p);
                }
            }
        }

        if (columns != null) {
            for (int i = 0; i < columns.size(); i++) {
                Column c = (Column)columns.get(i);
                if (c != null) {
                    sb.append(c);
                }
            }
        }

        sb.append("  </dataquery>\n");

        return sb.toString();
    }

    // ----------------------------------------------------- Validation Methods

    /**
     * Automatically populates the column list from the sql query
     *
     * @param conn the database connection
     */
    public void populateColumns(Connection conn, DatasetConfiguration dc) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Object[] values = new Object[parameters.size()];
            for(int i=0; i< values.length;i++) {
                values[i] = ((Parameter)parameters.get(i)).getValue();
            }

            stmt = FetchDocumentBySQLTask.prepareTheStatement(conn,this,values);
            try {
                stmt.setMaxRows(5);
            }catch(Exception e) {}
            rs = stmt.executeQuery();
            populateColumnsFromMetaData(rs.getMetaData(), dc);
            rememberColumnExampleValue(rs);
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    throw e;
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    throw e;
                }
            }
        }
    }

    /**
     * @return Returns the tempResults.
     */
    public ArrayList<ArrayList<String>> getTempResults() {
        return tempResults;
    }
    private ArrayList<ArrayList<String>> tempResults = null;
    /**
     * Save values to dataquery.tempResults
     * @param rs
     * @throws SQLException
     */
    private void rememberColumnExampleValue(ResultSet rs) throws SQLException {
        int rowCount = 0;
        tempResults = new ArrayList<ArrayList<String>>();
        QuerySampleValues sampleValues = QuerySampleValues.load();
        while(rowCount<5 && rs.next()){
            ArrayList<String> aRow = new ArrayList<String>();
            for(Column c : columns){
                String ret =DBTool.getDisplayableValue(rs, c.getColumnIndex(), c.getColumnType()); 
                aRow.add(ret);
            	sampleValues.add(c.getColumnName(), ret);
            }
            tempResults.add(aRow);
            rowCount++;
        }
        sampleValues.save();
    }

    /**
     * Populates the column list from the Metadata
     *
     * @param metaData the resultset (column) metadata
     */
    protected void populateColumnsFromMetaData(ResultSetMetaData metaData, DatasetConfiguration dc) throws SQLException {
        if (metaData == null) {
            return;  // keep the existing columns
        }
        ConfigurationHistory ch = ConfigurationHistory.load(dc);
        ArrayList<Column> newColumns = new ArrayList<Column>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String name = U.getText(metaData.getColumnLabel(i), metaData.getColumnName(i));
            //retain other old values not set in current page
            Column c = getColumn(name);
            if(c==null) {
                c = new Column();
                c.setColumnName(name);
            }
            ch.init(c);
            c.setColumnIndex(i);
            String type = DBTool.getType(metaData,i);
            if(type!=null&&!type.equals(c.getColumnType())){
	            c.setColumnType(type);
	            if (c.getIsText()) {
	                c.setIndexFieldType(IndexFieldType.TEXT);
	            }else if(c.getIsDate()){
	                c.setIndexFieldType(IndexFieldType.KEYWORD_DATE_HIERARCHICAL);
                    c.setIsFilterable(true);
                    c.setSortFilterCountsBy(Column.SortFilterCountsByValueDescending);
	            }else {
	                c.setIndexFieldType(IndexFieldType.KEYWORD);
	            }
            }
            if(c.getIsNumber()) {
                c.setColumnPrecision(metaData.getPrecision(i));
                c.setColumnScale(metaData.getScale(i));
            }
            newColumns.add(c);
            c.setConfigObject(configObject);
        }
        columns.clear();  // Remove the old columns
        columns.addAll(newColumns);
        if (configObject != null) configObject.setDirty(true);
    }

    public boolean test(Connection conn, StringBuffer msg) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            Object[] values = new Object[parameters.size()];
            for(int i=0; i< values.length;i++) {
                values[i] = ((Parameter)parameters.get(i)).getValue();
            }

            stmt = FetchDocumentBySQLTask.prepareTheStatement(conn,this,values);
            try {
                stmt.setMaxRows(5);
            }catch(Exception e) {}
            rs = stmt.executeQuery();
            result = testColumns(rs.getMetaData(), msg);
        } catch (SQLException e) {
            msg.append(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    msg.append(e);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    msg.append(e);
                }
            }
        }
        return result;
    }

    protected boolean testColumns(ResultSetMetaData metaData, StringBuffer msg) {
        try {
            if (metaData == null) {
                msg.append("Failed to the retrieve the metadata.");
                return false;
            }
            if (columns.size() != metaData.getColumnCount()) {
                msg.append("The number of columns doesn't match the query.");
                return false;
            }
            for (int i = 0; i < columns.size(); i++) {
                Column c = (Column)columns.get(i);
                String type = DBTool.getType(metaData,(i+1));
                /* not needed: values from result set will always be get by columIndex
                 * if (!name.equalsIgnoreCase(c.getColumnName())) {
                    msg.append("Column ").append(i+1).append(" has an invalid name. Expected name: ").append(name);
                    return false;
                }*/
                if (!type.equalsIgnoreCase(c.getColumnType())) {
                    msg.append("Column ").append(i+1).append(" has an invalid type. Expected type: ").append(type);
                    return false;
                }
                if (c.getIsPrimaryKey() && !(c.getIndexFieldType()==IndexFieldType.KEYWORD)
                        // quick solution to avoid validation for ContentDataquery
                        // TODO: use virtual functions
                        && "WorkingQueue".equals(this.name)) {
                    msg.append("The index field type of the primary key column ").append(i+1).append(" can only be Keyword.");
                    return false;
                }
                if (c.getIsModifiedDate() && !(c.getIndexFieldType()==IndexFieldType.KEYWORD||c.getIndexFieldType()==IndexFieldType.KEYWORD_DATE_HIERARCHICAL)
                        // quick solution to avoid validation for ContentDataquery
                        // TODO: use virtual functions
                        && "WorkingQueue".equals(this.name)) {
                    msg.append("The index field type of the modified date column ").append(i+1).append(" can only be Keyword or Hierarchical Date.");
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            msg.append(e);
            return false;
        }
    }

    public Column getBlobColumn() {
        if (columns == null) {
            return null;
        }
        for (int i = 0; i < columns.size(); i++){
            Column c = (Column)columns.get(i);
            if(c != null && "java.sql.Blob" == c.getColumnType()) {
                return c;
            }
        }
        return null;
    }

    public static Pattern patternHasLimit = Pattern.compile(".+\\slimit\\s+\\d+.*",Pattern.CASE_INSENSITIVE|Pattern.MULTILINE|Pattern.DOTALL);
    /**
     * [LIMIT {[offset,] row_count | row_count OFFSET offset}]
     * For compatibility with PostgreSQL, MySQL also supports the 
     *   LIMIT row_count OFFSET offset
     * syntax.
     * @param sql
     * @return
     */
    public boolean hasLimitClause(){
        if(sql==null)return false;
        return patternHasLimit.matcher(sql.toLowerCase()).find();
    }

}
