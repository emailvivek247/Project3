package net.javacoding.xsearch.utility;

import java.io.File;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.WebConfiguration;
import net.javacoding.xsearch.core.exception.ConfigurationException;
import net.javacoding.xsearch.foundation.WebserverStatic;
import com.fdt.sdl.core.parser.FileParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
  * created to support java.sql.* object types
  */

public final class DBTool{
    private static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.utility.DBTool");

    /** Add a Object to a PreparedStatement
    *  Clob is added only for getString, because lobs are not used in parameters
    */
    public static void setParameter(PreparedStatement ps, int i, Object param) throws SQLException{
        if (param instanceof java.sql.Date){
            ps.setDate(i, (java.sql.Date) param);
        }else if (param instanceof java.math.BigDecimal){
            ps.setBigDecimal(i, (java.math.BigDecimal) param);
        }else if (param instanceof java.sql.Timestamp){
            ps.setTimestamp(i, (java.sql.Timestamp) param);
        }else if (param instanceof java.sql.Time){
            ps.setTime(i, (java.sql.Time) param);
        }else if (param instanceof java.lang.Float){
            ps.setFloat(i, ((java.lang.Float) param).floatValue());
        }else if (param instanceof java.lang.Double){
            ps.setDouble(i, ((java.lang.Double) param).doubleValue());
        }else{
            ps.setString(i, param.toString());
        }
    }

    /**
     * Most code is copied from below, it only can not support blob
     * I agree this is not good code reuse, but it's Java, no dynamic typing...
     */
    public static String getString(ResultSet rs, int i, String columnType) throws SQLException{
        String ret = null;
        if ("java.sql.Date"==columnType){
            java.sql.Date d = rs.getDate(i);
            if(d!=null) {
                ret = VMTool.timeToString(d.getTime());
            }
        }else if ("java.math.BigDecimal"==columnType){
            ret = rs.getString(i);
        }else if ("java.sql.Timestamp"==columnType){
            java.sql.Timestamp d = rs.getTimestamp(i);
            if(d!=null) {
                ret = VMTool.timeToString(d.getTime());
            }
        }else if ("java.sql.Time"==columnType){
            java.sql.Time d = rs.getTime(i);
            if(d!=null) {
                ret = VMTool.timeToString(d.getTime());
            }
        }else if ("java.lang.Float"==columnType){
            ret = Float.toString(rs.getFloat(i));
        }else if ("java.lang.Double"==columnType){
            ret = Double.toString(rs.getDouble(i));
        }else{
            ret = rs.getString(i);
        }
        return ret;
    }
    /** create a String for lucene term field content from resultset according to column index and column type
     *  Clob is added only for getString, because lobs are not used in parameters
     */
    public static String getString(ResultSet rs, Column c) throws SQLException{
        if(c==null) return null;
        int i = c.getColumnIndex();
        String columnType = c.getColumnType();
        String ret = null;
        if ("java.sql.Date"==columnType){
            java.sql.Date d = rs.getDate(i);
            if(d!=null) {
                ret = VMTool.timeToStoredString(d.getTime());
            }
        }else if ("java.math.BigDecimal"==columnType){
            ret = rs.getString(i);
        }else if ("java.sql.Timestamp"==columnType){
            java.sql.Timestamp d = rs.getTimestamp(i);
            if(d!=null) {
                ret = VMTool.timeToStoredString(d.getTime());
            }
        }else if ("java.sql.Time"==columnType){
            java.sql.Time d = rs.getTime(i);
            if(d!=null) {
                ret = VMTool.timeToStoredString(d.getTime());
            }
        }else if ("java.lang.Float"==columnType){
            ret = Float.toString(rs.getFloat(i));
        }else if ("java.lang.Double"==columnType){
            ret = Double.toString(rs.getDouble(i));
        }else if ("java.sql.Clob"==columnType){
            Clob clob = rs.getClob(i);
            ret = (clob != null ? clob.getSubString(1, (int) clob.length()) : null);
        }else if ("java.sql.Blob"==columnType){
            FileParser f = new FileParser();
            Blob blob = rs.getBlob(i);
            ret = (blob != null ? f.parse(blob.getBinaryStream()) : null);
        }else{
            ret = rs.getString(i);
        }
        return ret;
    }

    /**
     * Used when processing submitted values, to convert into strings to save into Document object
     */
    public static String getStringInDocument(String t, Column c) {
        if(c==null) return null;
        String columnType = c.getColumnType();
        String ret;
        if (c.getIsDate()){
            Long v = VMTool.stringToLongValue(t);
            ret = v==null? "" : VMTool.timeToStoredString(v);
        }else{
            ret = t;
        }
        return ret;
    }

    public static String getDisplayableValue(ResultSet rs, int i, String columnType) throws SQLException {
        columnType = (columnType==null?null:columnType.intern());
        String ret = null;
        if ("java.sql.Date"==columnType){
            ret = rs.getString(i);
        }else if ("java.math.BigDecimal"==columnType){
            ret = rs.getString(i);
        }else if ("java.sql.Timestamp"==columnType){
            ret = rs.getString(i);
        }else if ("java.sql.Time"==columnType){
            ret = rs.getString(i);
        }else if ("java.lang.Float"==columnType){
            ret = Float.toString(rs.getFloat(i));
        }else if ("java.lang.Double"==columnType){
            ret = Double.toString(rs.getDouble(i));
        }else if ("java.sql.Clob"==columnType){
            Clob clob = rs.getClob(i);
            int len = (clob!=null?(int) clob.length():0);
            ret = (clob != null ? clob.getSubString(1, (len>10?10:len)) : null);
            if(len>10) ret+="...";
        }else if ("java.sql.Blob"==columnType){
            ret ="(blob)";
        }else{
            ret = rs.getString(i);
        }
        return ret;
    }

    /**
     * Used to get long value of timestamp columns, in FetchDocumentListBySQLTask
     */
    public static long getLong(ResultSet rs, int i, String columnType) throws SQLException{
        columnType = (columnType==null?null:columnType.intern());
        long ret = -1;
        try{
            if ("java.sql.Date"==columnType){
                java.sql.Date d = rs.getDate(i);
                if(d!=null) {
                    ret = d.getTime();
                }
            }else if ("java.math.BigDecimal"==columnType){
                java.math.BigDecimal d = rs.getBigDecimal(i);
                if(d!=null) {
                    ret = d.longValue();
                }
            }else if ("java.sql.Timestamp"==columnType){
                java.sql.Timestamp d = rs.getTimestamp(i);
                if(d!=null) {
                    ret = d.getTime();
                }
            }else if ("java.sql.Time"==columnType){
                java.sql.Time d = rs.getTime(i);
                if(d!=null) {
                    ret = d.getTime();
                }
            }else{
                try{
                    ret = Long.parseLong(rs.getString(i));
                }catch(Exception e){}
            }
        }catch(Exception e){}
        return ret;
    }

    public static String getDefaultParamValue(String paramType) {
        String ret = null;
        if ("java.sql.Date".equals(paramType)) {
            ret = "2004-01-01";
        } else if ("java.math.BigDecimal".equals(paramType)) {
            ret = "1";
        } else if ("java.sql.Timestamp".equals(paramType)) {
            ret = "2004-01-01 00:00:00";
        } else if ("java.sql.Time".equals(paramType)) {
            ret = "00:00:00";
        } else if ("java.lang.Float".equals(paramType)) {
            ret = "3.14";
        } else if ("java.lang.Double".equals(paramType)) {
            ret = "3.14";
        } else {
            ret = "";
        }
        return ret;
    }

    /**
     * Used when passing parameters in subsequent sqls
     * And when restoring parameters to java objects from xml files
     * @param type
     * @param value
     * @return
     */
    public static Object createObject(String type, String value) {
        type = (type==null?null:type.intern());
        Object ret = null;
        try{
            if ("java.sql.Date"==type) {
                ret = java.sql.Date.valueOf(value);
            } else if ("java.math.BigDecimal"==type) {
                ret = new java.math.BigDecimal(value);
            } else if ("java.sql.Timestamp"==type) {
                ret = java.sql.Timestamp.valueOf(value);
            } else if ("java.sql.Time"==type) {
                ret = java.sql.Time.valueOf(value);
            } else if ("java.lang.Float"==type) {
                ret = new Float(value);
            } else if ("java.lang.Double"==type) {
                ret = new Double(value);
            } else if ("java.lang.Integer"==type) {
                ret = new Integer(value);
            } else {
                ret = value;
            }
        }catch(Exception e){
        }
        return ret;
    }
    public static String toValueInSqlString(String type, Object value) {
        type = (type==null?null:type.intern());
        if ("java.lang.String"==type) {
        	return "'"+value.toString().replaceAll("'", "''")+"'";
        }else{
        	return value.toString();
        }
    }

    /**
     * Mapping metadata from resultset to supported java object types
     * @param metaData
     * @param i
     * @return
     */
    public static String getType(ResultSetMetaData metaData, int i) {
        try {
            String className = metaData.getColumnClassName(i);
            if(className!=null) {
                className = className.intern();
            }
            if(className == "java.math.BigDecimal") {
                return className;
            }else if(className == "java.lang.String") {
                return className;
            }else if(className == "java.sql.Timestamp") {
                return className;
            }
            int type = metaData.getColumnType(i);
//            logger.debug("column "+i+":"+type+":"+metaData.getColumnClassName(i));
            switch(type) {
            case Types.DOUBLE         : return "java.math.BigDecimal";
            case Types.FLOAT          : return "java.math.BigDecimal";
            case Types.REAL           : return "java.math.BigDecimal";
            case Types.NUMERIC        : return "java.math.BigDecimal";
            case Types.DECIMAL        : return "java.math.BigDecimal";
            case Types.BIGINT         : return "java.math.BigDecimal";
            case Types.INTEGER        : return "java.math.BigDecimal";
            case Types.SMALLINT       : return "java.math.BigDecimal";
            case Types.TINYINT        : return "java.math.BigDecimal";
            case Types.CHAR           : return "java.lang.String";
            case Types.VARCHAR        : return "java.lang.String";
            case Types.DATE           : return "java.sql.Timestamp";
            case Types.TIME           : return "java.sql.Timestamp";
            case Types.TIMESTAMP      : return "java.sql.Timestamp";
            case Types.LONGVARCHAR    : return "java.lang.String";
            case Types.BLOB           : return "java.sql.Blob";
            case Types.CLOB           : return "java.sql.Clob";
            case -4                   : return "java.sql.Blob"; //just for MySql BLOB
            }
            return "java.lang.String";
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
