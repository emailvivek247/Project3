package net.javacoding.xsearch.core.task.work.util;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.javacoding.xsearch.config.Column;
import net.javacoding.xsearch.config.ContentDataquery;
import net.javacoding.xsearch.config.Dataquery;
import net.javacoding.xsearch.config.IndexFieldType;
import net.javacoding.xsearch.config.Parameter;
import net.javacoding.xsearch.config.WorkingQueueDataquery;
import net.javacoding.xsearch.core.component.TextDocument;
import net.javacoding.xsearch.indexer.textfilter.ZipToGeoConvertor;
import net.javacoding.xsearch.indexer.textfilter.ZipToGeoConvertor.GeoPosition;
import net.javacoding.xsearch.utility.DBTool;
import net.javacoding.xsearch.utility.NumberUtils;
import net.javacoding.xsearch.utility.U;
import net.javacoding.xsearch.utility.VMTool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fdt.sdl.core.parser.FileParser;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

public class DocumentSQLTaskHelper {
    static    long last_report_time = 0;//this line is to be deleted
    static    long last_total = 0;//this line is to be deleted
    static long total = 0;
    static Logger logger = LoggerFactory.getLogger("net.javacoding.xsearch.core.task.work.DocumentSQLTaskHelper");
    public static DateTimeFormatter yearDateFormat = DateTimeFormat.forPattern("yyyy");
    public static DateTimeFormatter yearAndMonthDateFormat = DateTimeFormat.forPattern("yyyy/MM");

    public static void removeFromDocument(Document doc, Column col) throws IOException {
        doc.removeField(col.getColumnName());
        if(IndexFieldType.KEYWORD==col.getIndexFieldType()) {
            if(col.getIsNumber()) {
                doc.removeField("s"+col.getColumnName());
            }
        }else if(IndexFieldType.KEYWORDS==col.getIndexFieldType()) {
            if(col.getIsNumber()) {
                doc.removeField("s"+col.getColumnName());
            }
        }else if(IndexFieldType.KEYWORD_DATE_HIERARCHICAL==col.getIndexFieldType()){
            doc.removeField("y"+col.getColumnName());
            doc.removeField("ym"+col.getColumnName());
        }else if(IndexFieldType.KEYWORD_BOOST==col.getIndexFieldType()) {
            if(col.getIsNumber()) {
                doc.removeField("s"+col.getColumnName());
            }
        }else if(IndexFieldType.ZIP_CODE==col.getIndexFieldType()) {
            doc.removeField("_long");
            doc.removeField("_lat");
        }
    }
    public static void addToDocument(Document doc, String s, Column col) throws IOException {
        if(s==null||s.length()==0) return;
        if(IndexFieldType.TEXT==col.getIndexFieldType()){
            doc.add(new Field(col.getColumnName(), s, Field.Store.YES, Field.Index.ANALYZED));
        }else if(IndexFieldType.TEXT_HTML==col.getIndexFieldType()){
            doc.add(new Field(col.getColumnName(), new FileParser().parse(s), Field.Store.COMPRESS, Field.Index.ANALYZED));
        }else if(IndexFieldType.TEXT_COMPRESSED==col.getIndexFieldType()){
            doc.add(new Field(col.getColumnName(), s, Field.Store.COMPRESS, Field.Index.ANALYZED));
        }else if(IndexFieldType.UN_INDEXED==col.getIndexFieldType()){
            doc.add(new Field(col.getColumnName(), s, Field.Store.YES, Field.Index.NO));
        }else if(IndexFieldType.UN_INDEXED_COMPRESSED==col.getIndexFieldType()){
            doc.add(new Field(col.getColumnName(), s, Field.Store.COMPRESS, Field.Index.NO));
        }else if(IndexFieldType.KEYWORD==col.getIndexFieldType()){
            if(col.getIsNumber()) {
                doc.add(new Field(col.getColumnName(), s,Field.Store.YES, Field.Index.NOT_ANALYZED));
                doc.add(new Field("s"+col.getColumnName(), NumberUtils.double2sortableStr(s),Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
            }else {
                doc.add(new Field(col.getColumnName(), s,Field.Store.YES, Field.Index.NOT_ANALYZED));
            }
        }else if(IndexFieldType.KEYWORDS==col.getIndexFieldType()){
            if(col.getIsNumber()) {
                doc.add(new Field(col.getColumnName(), s,Field.Store.YES, Field.Index.NOT_ANALYZED));
                doc.add(new Field("s"+col.getColumnName(), NumberUtils.double2sortableStr(s),Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
            }else{
                doc.add(new Field(col.getColumnName(), s,Field.Store.YES, Field.Index.ANALYZED));
            }
        }else if(IndexFieldType.KEYWORD_CASE_INSENSITIVE==col.getIndexFieldType()){
            doc.add(new Field(col.getColumnName(), s,Field.Store.YES, Field.Index.ANALYZED));
        }else if(IndexFieldType.KEYWORD_DATE_HIERARCHICAL==col.getIndexFieldType()){
            doc.add(new Field(col.getColumnName(), s,Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
            long date = VMTool.storedStringToLongValue(s);
            doc.add(new Field("y"+col.getColumnName(), yearDateFormat.print(date),Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
            doc.add(new Field("ym"+col.getColumnName(), yearAndMonthDateFormat.print(date),Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
        }else if(IndexFieldType.UN_STORED==col.getIndexFieldType()){
            doc.add(new Field(col.getColumnName(), s, Field.Store.NO, Field.Index.ANALYZED));
        }else if(IndexFieldType.KEYWORD_BOOST==col.getIndexFieldType()){
            doc.add(new Field(col.getColumnName(), s,Field.Store.YES, Field.Index.NOT_ANALYZED));
            if(col.getIsNumber()) {
                doc.add(new Field("s"+col.getColumnName(), NumberUtils.double2sortableStr(s),Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
            }
            //doc.setBoost(U.getFloat(s,1.0f)*doc.getBoost());
        }else if(IndexFieldType.BOOST==col.getIndexFieldType()){
            //doc.setBoost(U.getFloat(s,1.0f)*doc.getBoost());
        }else if(IndexFieldType.ZIP_CODE==col.getIndexFieldType()){
            GeoPosition geo = ZipToGeoConvertor.lookup(s);
            doc.add(new Field(col.getColumnName(), s,Field.Store.YES, Field.Index.NO));
            doc.add(new Field("_long", NumberUtils.float2sortableStr(geo.getLongitude()),Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
            doc.add(new Field("_lat", NumberUtils.float2sortableStr(geo.getLatitude()),Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
        }else{
            //this is for "Only as Parameter", "Blob file name"
        }
    }
    
    public static void saveToTextDocument(TextDocument doc, ResultSet rs, Dataquery dataQuery) throws Throwable {
        ArrayList<Column> al  = dataQuery.getColumns();
        StringBuffer[] sbs = null;
        int countAggregation = 0;
        Column[] aggregateColumns = null;
        for(Column col : al){
            if(col.getIsAggregate()){
                countAggregation++;
            }
        }
        if(countAggregation>0) {
            aggregateColumns = new Column[countAggregation];
            sbs = new StringBuffer[countAggregation];
        }
        for(int i=0,k=0; i< al.size(); i++){
            Column col = (Column)al.get(i);
            if(col.getIsAggregate()){
                aggregateColumns[k] = col;
                sbs[k] = new StringBuffer();
                k++;
            }
        }
        for(int i=0,k=0; i< al.size(); i++){
            Column col = (Column)al.get(i);
            //logger.debug(col.getColumnName());
            if(col.getIsAggregate()){
              sbs[k++].append(rs.getString(col.getColumnIndex()));
            }else{
                addToTextDocument(doc,rs,col);
                //logger.debug(col.getColumnName()+":"+DBTool.getString(rs, col ));
            }
        }
        if(countAggregation>0){
            while(rs.next()){
                for(int i=0; i<countAggregation;i++) {
                    sbs[i].append(aggregateColumns[i].getAggregateSeparator()).append(rs.getString(aggregateColumns[i].getColumnIndex()));
                }
            }
            for(int i=0; i<countAggregation;i++) {
                addToTextDocument(doc,sbs[i].toString(),aggregateColumns[i]);
            }
        }else if(! (dataQuery instanceof WorkingQueueDataquery)){
            while (rs.next()) {
                for(Column col : al){
                    addToTextDocument(doc, rs, col);
                }
            }
        }
    }
    /**
     * Used during batch processing.
     */
    public static void saveToTextDocuments(TextDocument[] docs, ResultSet rs, ContentDataquery dataQuery) throws Throwable {
    	ArrayList<Parameter> parameters = dataQuery.getParameters();
    	if(parameters==null || parameters.size()>1){
    		logger.warn("Batched Subsequent Query should have one parameter: "+dataQuery.getSql());
    		return;
    	}
    	Column p = null;
    	for(Column c : dataQuery.getColumns()){
    		if(c.getColumnName()==parameters.get(0).getName()){
    			p = c;
    			break;
    		}
    	}
    	if(p==null){
    		logger.warn("Batched Subsequent Query should have selected the parameter column: "+parameters.get(0).getName());
    		return;
    	}
    	
    	//if(true) return;//avoiding this piece just for testing

		//logger.debug("getting rows from resultset...");
    	//1. get rows from resultset
    	int rowWidth = dataQuery.getColumns().size();
    	ArrayList<List<String>> rows = new ArrayList<List<String>>();
    	if(rs.next()) {
            saveToListOfArrayList(dataQuery, rs, rows);
    	}
    	
    	//logger.debug("put values into hash map...");
    	//2. put into hash map
        final int comparedColumnIndex = p.getColumnIndex()-1;
        Multimap<String,List<String>> rowMap = ArrayListMultimap.create();
    	for(List<String> r : rows) {
    	    rowMap.put(r.get(comparedColumnIndex),r);
    	}

    	//logger.debug("merging results with documents ...");
        //3. merge rows to docs
        for(TextDocument d : docs) {
            String[] fieldValues = d.getValues(p.getColumnName());
            if(fieldValues!=null) {
                for(String k : fieldValues) {
                    if(rowMap.containsKey(k)) {
                        for(List<String> row : rowMap.get(k)) {
                            saveToTextDocument(dataQuery, row, d);
                        }
                    }
                }
            }
        }

		//logger.debug("completed this batch ...");
    }

    private static void addToTextDocument(TextDocument doc, ResultSet rs, Column col) throws Throwable {
    	if(U.isEmpty(col.getIndexFieldType())) return;
        String value = DBTool.getString(rs, col );
        if(value == null) return;
        total += value.length();
        doc.add(col.getColumnName(), value);
    }
    private static void addToTextDocument(TextDocument doc, String s, Column col) throws Exception {
    	if(U.isEmpty(col.getIndexFieldType())) return;
        if(s==null) return;
        total += s.length();
        doc.add(col.getColumnName(), s);
    }

    /**
     * This is for caching, save LOV values to results
     * @param dataquery
     * @param rs
     * @param results
     * @throws SQLException
     */
    public static void saveToArrayList(ContentDataquery dataQuery, ResultSet rs, List<String> results) throws SQLException {
        for(Column col : dataQuery.getColumns()){
            results.add(DBTool.getString(rs, col)) ;
        }
    }

    public static void saveToListOfArrayList(ContentDataquery dataQuery, ResultSet rs, List<List<String>> results) throws SQLException {
        do{
            ArrayList<String> row = new ArrayList<String>();
            saveToArrayList(dataQuery, rs, row);
            results.add(row);
        }while(rs.next());
    }

    /**
     * save cached results into TextDocument
     * @param dataquery
     * @param results can be List<String> or List<List<String>>
     * @param document
     */
    public static void saveToTextDocument(ContentDataquery dataQuery, List results, TextDocument document) {
        if(results==null||results.size()<=0) return;
        if(results.get(0) instanceof String) {
            for(Column col : dataQuery.getColumns()){
                if(U.isEmpty(col.getIndexFieldType())) continue;
                document.add(col.getColumnName(), (String)results.get(col.getColumnIndex()-1));
            }
        }else if (results.get(0) instanceof List){
            for(List row : (List<List<String>>)results ) {
                for(Column col : dataQuery.getColumns()){
                    if(U.isEmpty(col.getIndexFieldType())) continue;
                    document.add(col.getColumnName(), (String)row.get(col.getColumnIndex()-1));
                }
            }
        }
    }

}
