package com.fdt.common.export;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;


/**
 * This class supports just a one sheet for the excel.
 * It can modified to support multiple sheets.
 * 
 * Styles of the Header Cell and Normal Cell are defined in this class which is fix for the excel document.
 * It can be changed as needed in future.
 * 
 * @author apatel
 *
 */
public  class ExcelExport  {
    // work book
	private HSSFWorkbook wb ;
    
	// sheet
	private HSSFSheet sheet ;
	
	// Heading style;
	private HSSFCellStyle headingStyle;

	// Standard Cell Style
	private HSSFCellStyle standardStyle;
	
	//current row starts at zero
	private int rowNumber = 0;
	
	// This method must be called before adding rows/columns to the document.
	protected void init(){
        this.wb = new HSSFWorkbook();
        this.sheet = this.wb.createSheet();
        this.standardStyle = getStandardCellStyle(); 
        this.headingStyle = getHeaderCellStyle();
	}
	
	
	public  byte[] exportToExcel(List<String> headers, List<List<String>> rows ) throws IOException {
    	this.init();
		// Add the headers first
		this.createHeaderRow(headers);
		// Iterate through and add the rows
		for(List<String> eachRow : rows){
	    	HSSFRow row = this.sheet.createRow(this.rowNumber++);
	    	int columnNumber = 0;
			for(String cellValue : eachRow){
		    	this.addCell(row, this.standardStyle, HSSFCell.CELL_TYPE_STRING, cellValue, columnNumber++);
			}	
		}
		return this.getDocumentBytes();
    }
	
	
	
	/**
	 * Header Cell Style
	 * 
	 * @return
	 */
    private HSSFCellStyle getHeaderCellStyle(){
        HSSFFont headingFont = this.wb.createFont();
        headingFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        HSSFCellStyle headingStyle = this.wb.createCellStyle();
        headingStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        headingStyle.setFont(headingFont);
    	return headingStyle;
    }

	/**
	 * Standard Cell Style
	 * 
	 * @return
	 */
    private  HSSFCellStyle getStandardCellStyle(){
        HSSFFont font = this.wb.createFont();
        HSSFCellStyle standardStyle = this.wb.createCellStyle();
        standardStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        standardStyle.setFont(font);
    	return standardStyle;
    }
    
    /**
     * Create Header row by iterating through header cells
     * @param headerCells
     */
    protected void createHeaderRow(List<String> headerCells){
    	HSSFRow row = this.sheet.createRow(this.rowNumber++);
    	int columnNumber = 0;
    	for(String cellValue : headerCells){
    		this.addCell(row, this.headingStyle, HSSFCell.CELL_TYPE_STRING, cellValue, columnNumber++);
    	}
    }

    /**
     * add normal cell

     * @param headerCells
     */
    protected void addNormalCell(String cellValue, int cellType){
    	int columnNumber = 0;
    	HSSFRow headingRow  = this.sheet.createRow(0);
   		addCell(headingRow, headingStyle, cellType, cellValue, columnNumber++);
    }

    private void addCell(HSSFRow row, HSSFCellStyle style, int cellType, String value, int columnNumber){
    	HSSFCell cell = row.createCell(columnNumber);
    	cell.setCellType(cellType);
        cell.setCellStyle(style);
        cell.setCellValue(value);
    }
    
    /**
     * Create a new row 
     * @param cells
     */
    protected void createRow(List<String> cells ){
    	// Everytime we add a row we increament the row number
    	HSSFRow row = this.sheet.createRow(this.rowNumber++);
    	int columnNumber = 0;
    	for(String cellValue : cells){
    		this.addCell(row, this.standardStyle, HSSFCell.CELL_TYPE_STRING, cellValue, columnNumber++);
    	}
    }
    
    /**
     * Finally create the document and return bytes.
     * 
     * @return
     * @throws IOException
     */
    protected byte[] getDocumentBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
   		wb.write(bos);
        return bos.toByteArray();
    }
    
    /**
     * Create an excel file
     * 
     * @param fileName
     * @throws IOException
     */
    protected void createDocument(String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(fileName));
   		wb.write(fos);
    }
    
    

}