package com.fdt.common.export;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;


/**
 * Styles of the Header Cell and Normal Cell are defined in this class which is fix for the excel document.
 * It can be changed as needed in future.
 *
 * This class supports limited functionality of creating a PDF document with a data table inside
 * More features can be added as needed.
 *
 * The steps to create document are as below.
 *
		super.init(COLUMN_WIDTHS.length, COLUMN_WIDTHS);

		// Add the headers first
		super.createHeaderRow(this.getHeaders());

		// Iterate through and add the rows
		for(Data data : dataList){
			// Add the row together using     protected void createRow(List<String> cells);
			 OR
			 // Add cell one by one using addNormalCell();
		}
 *
 *
 * @author apatel
 *
 */
public class PDFExport  {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Font smallFont;

    private Font boldFont;

    private PdfPTable table;

    private Document document;

    private int numberOfColumns;

    private int[] columnWidths;

	private PDFExport(){
	}

	public PDFExport(int numberOfColumns, int[] columnWidths){
		this.numberOfColumns = numberOfColumns;
		this.columnWidths = columnWidths;
	}
	// This method must be called before adding rows/columns to the document.
	protected void init() throws Exception {
	    this.smallFont = new Font(FontFamily.HELVETICA, 7, Font.NORMAL, new BaseColor(0, 0, 0));
	    this.boldFont  = new Font(FontFamily.HELVETICA, 7, Font.BOLD,   new BaseColor(0, 0, 0));
	    this.createTable(numberOfColumns, columnWidths);
		this.document = new Document(PageSize.LETTER_LANDSCAPE.rotate());
	}

	public  byte[] exportToPDF(List<String> headers, List<List<PDFCell>> rows) throws Exception {

		this.init();
		this.setPageSize(PageSize.LEGAL_LANDSCAPE.rotate());
		// Add the headers first
		this.createHeaderRow(headers);

		// Iterate through and add the rows
		for(List<PDFCell> eachRow : rows){
			for(PDFCell pdfCell : eachRow){
				this.addNormalCell(pdfCell.getValue(), pdfCell.isWrap());
			}
			this.endTableRow();
		}
		return this.getDocumentBytes();
	}


	/**
	 * Set Page Size , By default it is Letter in Landscape Mode
	 *
	 * @param pageSize
	 */
	protected void setPageSize(Rectangle pageSize){
		this.document.setPageSize(pageSize);
	}

	protected void createTable(int numberOfColumns, int[] columnWidths) throws Exception {
		this.table = new PdfPTable(numberOfColumns);
		this.table.setWidthPercentage(100);
		try{
			this.table.setWidths(columnWidths);
		}catch(DocumentException e){
			logger.error("Error while creating PDF table", e);
			throw new Exception("Error While creating PDF Table");
		}
	}


    /**
     * Create Header row by iterating through header cells
     * @param headerCells
     */
    protected void createHeaderRow(List<String> headerCells){
    	for(String cellValue : headerCells){
    		this.addHeaderCell(cellValue);
    	}
    	this.table.completeRow();
    }

    /**
     * Create a new row without column values wrapping
     *
     * @param cells
     */
    protected void createRow(List<String> cells){
    	for(String cellValue : cells){
    		this.addNormalCell(cellValue, false);
    	}
    	this.table.completeRow();
    }

    // End Table Row after done adding cells
    protected void endTableRow(){
    	this.table.completeRow();
    }

	private void addHeaderCell(String headerName){
    	PdfPCell cell = new PdfPCell(new Phrase(new Chunk(StringUtils.trimToEmpty(headerName), boldFont)));
		cell.setNoWrap(false);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
		this.table.addCell(cell);
    }


	/**
     * Add a Normal Cell
     *
     * @param headerName
     */
	protected void addNormalCell(String cellValue, boolean wrap){
    	PdfPCell cell = new PdfPCell(new Phrase(new Chunk(StringUtils.trimToEmpty(cellValue), smallFont)));
		cell.setNoWrap(!wrap);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
		this.table.addCell(cell);
    }


    /**
     * Finally create the document and return bytes.
     *
     * @return
     * @throws IOException
     */
    protected byte[] getDocumentBytes() throws Exception {
    	try{
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
			PdfWriter.getInstance(document, bos);
			this.document.open();

			// Add Table to document and return bytes
			this.document.add(this.table);

			document.close();
			return bos.toByteArray();
    	}catch(DocumentException e){
    		logger.error("Error while creating PDF document", e);
    		throw new Exception("Error While creating PDF document");
    	}
    }

    /**
     * Create PDF file
     *
     * @param fileName
     * @throws IOException
     */
    protected void createDocument(String fileName) throws Exception {
    	try{
    		FileOutputStream fos = new FileOutputStream(new File(fileName));
    		PdfWriter.getInstance(document, fos);
			this.document.open();

			// Add Table to document and return bytes
			this.document.add(this.table);

			document.close();
			fos.close();
    	}catch(DocumentException e){
    		logger.error("Error while creating PDF document", e);
    		throw new Exception("Error While creating PDF document");
    	}
    }



}