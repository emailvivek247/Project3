package com.fdt.sdl.util.displaytag.exportview;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.displaytag.Messages;
import org.displaytag.exception.BaseNestableJspTagException;
import org.displaytag.exception.SeverityEnum;
import org.displaytag.export.BinaryExportView;
import org.displaytag.model.Column;
import org.displaytag.model.ColumnIterator;
import org.displaytag.model.HeaderCell;
import org.displaytag.model.Row;
import org.displaytag.model.RowIterator;
import org.displaytag.model.TableModel;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.lowagie.text.BadElementException;


/**
 * PDF exporter using IText. This class is provided more as an example than as a "production ready" class: users
 * probably will need to write a custom export class with a specific layout.
 * @author Ivan Markov
 * @author Fabrizio Giustina
 * @version $Revision: 1160 $ ($Author: fgiust $)
 */
public class PdfView implements BinaryExportView
{

    /** TableModel to render. */
    private TableModel model;

    /** export full list? */
    private boolean exportFull;

    /** include header in export? */
    private boolean isIncludHeader;
    
    /** decorate export? */
    private boolean decorated;

    /** This is the table, added as an Element to the PDF document. It contains all the data, needed to represent the
     * visible table into the PDF */
    private PdfPTable tablePDF;

    /** The default font used in the document. */
    private Font smallFont;

    /** The default font used in the document. */
    private Font boldFont;
    
    /**
     * @see org.displaytag.export.ExportView#setParameters(TableModel, boolean, boolean, boolean)
     */
    public void setParameters(TableModel tableModel, boolean exportFullList, boolean includeHeader,
        boolean decorateValues)
    {
        this.model = tableModel;
        this.exportFull = exportFullList;
        this.isIncludHeader = includeHeader;
        this.decorated = decorateValues;
    }

    /**
     * Initialize the main info holder table.
     * @throws BadElementException for errors during table initialization
     */
    protected void initTable() throws BadElementException
    {
        tablePDF = new PdfPTable(this.model.getNumberOfColumns());
        smallFont = new Font(FontFamily.HELVETICA, 7, Font.NORMAL, new BaseColor(0, 0, 0));
        boldFont  = new Font(FontFamily.HELVETICA, 9, Font.BOLD,   new BaseColor(0, 0, 0));
    }

    /**
     * @see org.displaytag.export.BaseExportView#getMimeType()
     * @return "application/pdf"
     */
    public String getMimeType()
    {
        return "application/pdf"; //$NON-NLS-1$
    }

    /**
     * The overall PDF table generator.
     * @throws JspException for errors during value retrieving from the table model
     * @throws BadElementException IText exception
     */
    protected void generatePDFTable() throws JspException, BadElementException
    {
        if (this.isIncludHeader)
        {
            generateHeaders();
        }
    	if (this.model.getSubTotalLabel() != null) {
    		this.generateTotalRows();
    	} else {
    		this.generateTotalRows();
    	}
    }

    /**
     * @see org.displaytag.export.BinaryExportView#doExport(OutputStream)
     */
    public void doExport(OutputStream out) throws JspException
    {
        try
        {
            // Initialize the table with the appropriate number of columns
            initTable();

            // Initialize the Document and register it with PdfWriter listener and the OutputStream
            Document document = new Document(PageSize.A4.rotate());
            document.setMargins(20, 20, 40, 40);
            document.setMarginMirroring(true);
            document.addCreationDate();

            PdfWriter.getInstance(document, out);

            // Fill the virtual PDF table with the necessary data
            generatePDFTable();
            document.open();
            document.add(this.tablePDF);
            document.close();

        }
        catch (Exception e)
        {
            throw new PdfGenerationException(e);
        }
    }
    

    /**
     * Generates the header cells, which persist on every page of the PDF document.
     * @throws BadElementException IText exception
     */
    protected void generateHeaders() throws BadElementException {
        Iterator<HeaderCell> iterator = this.model.getHeaderCellList().iterator();

        /** Add a Title **/
        if (!StringUtils.isBlank(this.model.getProperties().getExportTitle())) {
	        int numberOfColumns = this.model.getHeaderCellList().size();
	        PdfPCell cell = new PdfPCell(new Phrase(this.model.getProperties().getExportTitle())); 
	        cell.setBackgroundColor(BaseColor.LIGHT_GRAY); 
	        cell.setHorizontalAlignment(Element.ALIGN_CENTER); 
	        cell.setColspan(numberOfColumns); 
	        tablePDF.addCell(cell);
        }
        
        while (iterator.hasNext()) {
            HeaderCell headerCell = iterator.next();
            String columnHeader = headerCell.getTitle();
            if (columnHeader == null) {
                columnHeader = StringUtils.capitalize(headerCell.getBeanPropertyName());
            }
            PdfPCell hdrCell = getCell(columnHeader);
            hdrCell.setGrayFill(0.9f);
            tablePDF.addCell(hdrCell);
        }
    }

    /**
     * Generates all the row cells.
     * @throws JspException for errors during value retrieving from the table model
     * @throws BadElementException errors while generating content
     */
    protected void generateNormalRows() throws JspException, BadElementException
    {
        // get the correct iterator (full or partial list according to the exportFull field)
        RowIterator rowIterator = this.model.getRowIterator(this.exportFull);
        // iterator on rows
        while (rowIterator.hasNext())
        {
            Row row = rowIterator.next();

            // iterator on columns
            ColumnIterator columnIterator = row.getColumnIterator(this.model.getHeaderCellList());

            while (columnIterator.hasNext())
            {
                Column column = columnIterator.nextColumn();

                // Get the value to be displayed for the column
                Object value = column.getValue(this.decorated);
                
                String strValue = ObjectUtils.toString(value);
                PdfPCell cell = getCell(strValue);                
                tablePDF.addCell(cell);
            }
        }
    }
    
    /**
     * Generates all the row cells.
     * @throws JspException for errors during value retrieving from the table model
     * @throws BadElementException errors while generating content
     */
	protected void generateTotalRows() throws JspException, BadElementException {
		RowIterator rowIterator = this.model.getRowIterator(this.exportFull);
		String previousItem = null;
		String currentItem = null;
		boolean breakFlag = false;
		Map<String, String> subTotals = new LinkedHashMap<String, String>();
		Map<String, String> grandTotals = new LinkedHashMap<String, String>();
		Row firstRow = null;

		/** Iterate the First Row **/
		if (this.model.getRowIterator(this.exportFull).hasNext()) {
			firstRow = this.model.getRowIterator(this.exportFull).next();
			ColumnIterator columnIterator = firstRow.getColumnIterator(this.model.getHeaderCellList());
			while (columnIterator.hasNext()) {
				Column column = columnIterator.nextColumn();
				if (column.getHeaderCell().getGroup() == 1) {
					subTotals.put(column.getHeaderCell().getIndexColumnName(), model.getSubTotalLabel());
					grandTotals.put(column.getHeaderCell().getIndexColumnName(), model.getTotalLabel());
				} else {
					subTotals.put(column.getHeaderCell().getIndexColumnName(), "");
					grandTotals.put(column.getHeaderCell().getIndexColumnName(), "");
				}  
			}
		}
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			ColumnIterator columnIterator = row.getColumnIterator(this.model.getHeaderCellList());
			while (columnIterator.hasNext()) {
				Column column = columnIterator.nextColumn();
				Object value = column.getValue(this.decorated);
				String strValue = ObjectUtils.toString(value).trim();
				if (column.getHeaderCell().getGroup() == 1)	 {
					currentItem = strValue;
					if (previousItem == null) {
						previousItem = strValue;
					} else if (previousItem.equalsIgnoreCase(currentItem)) {
						strValue = "";
					} else if (!previousItem.equalsIgnoreCase(currentItem)) {
						breakFlag = true;
						previousItem = currentItem;
					}
					if (breakFlag) {
						breakFlag = false;
						Iterator<String> it = subTotals.keySet().iterator();
						while (it.hasNext()) {
							PdfPCell cell = getBoldCell(subTotals.get(it.next()));
							tablePDF.addCell(cell);
						}
						subTotals.clear();
						subTotals.put(column.getHeaderCell().getIndexColumnName(), model.getSubTotalLabel());
					}
				} else if (column.getHeaderCell().isTotaled())	 {
				   	String totalPropertyName = column.getHeaderCell().getIndexColumnName();
				   	Integer columnValue = new Integer(strValue);
	                
				   	int previousGrandTotals = 0;
	                int previousSubTotal = 0;

	                if (subTotals.get(totalPropertyName) != null && !StringUtils.isEmpty(subTotals.get(totalPropertyName))) {
	                	previousSubTotal = new Integer(subTotals.get(totalPropertyName));
	                }
	                int subGrandTotal = previousSubTotal + columnValue.intValue();

	                if (grandTotals.get(totalPropertyName) != null && !StringUtils.isEmpty(grandTotals.get(totalPropertyName))) {
	                	previousGrandTotals = new Integer(grandTotals.get(totalPropertyName));
	                }
	                int grandTotal = previousGrandTotals + columnValue.intValue();

	                subTotals.put(totalPropertyName, String.valueOf(subGrandTotal));
	                grandTotals.put(totalPropertyName, String.valueOf(grandTotal));
				} else {
					String totalPropertyName = column.getHeaderCell().getIndexColumnName();
					subTotals.put(totalPropertyName, "");
	                grandTotals.put(totalPropertyName, "");					
				}
				PdfPCell cell = getCell(strValue);
				tablePDF.addCell(cell);
			}
		}
		Iterator<String> it = subTotals.keySet().iterator();
		while (it.hasNext()) {
			String subTotal = subTotals.get(it.next());
			if (!StringUtils.isBlank(subTotal)) {
				PdfPCell cell = getBoldCell(subTotal);
				tablePDF.addCell(cell);
			}
		}
		it = grandTotals.keySet().iterator();
		while (it.hasNext()) {
			String grandTotal = grandTotals.get(it.next());
			if (!StringUtils.isBlank(grandTotal)) {
				PdfPCell cell = getBoldCell(grandTotal);
				tablePDF.addCell(cell);
			}
		}
	}
    
    /**
     * Returns a formatted cell for the given value.
     * @param value cell value
     * @return Cell
     * @throws BadElementException errors while generating content
     */
    private PdfPCell getCell(String value) throws BadElementException
    {
    	PdfPCell cell = new PdfPCell(new Phrase(new Chunk(StringUtils.trimToEmpty(value), smallFont)));
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        return cell;
    }

    /**
     * Returns a formatted cell for the given value.
     * @param value cell value
     * @return Cell
     * @throws BadElementException errors while generating content
     */
    private PdfPCell getBoldCell(String value) throws BadElementException
    {
    	PdfPCell cell = new PdfPCell(new Phrase(new Chunk(StringUtils.trimToEmpty(value), boldFont)));
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        return cell;
    }

    /**
     * Wraps IText-generated exceptions.
     * @author Fabrizio Giustina
     * @version $Revision: 1160 $ ($Author: fgiust $)
     */
    static class PdfGenerationException extends BaseNestableJspTagException
    {

        /**
         * D1597A17A6.
         */
        private static final long serialVersionUID = 899149338534L;

        /**
         * Instantiate a new PdfGenerationException with a fixed message and the given cause.
         * @param cause Previous exception
         */
        public PdfGenerationException(Throwable cause)
        {
            super(PdfView.class, Messages.getString("PdfView.errorexporting"), cause); //$NON-NLS-1$
        }

        /**
         * @see org.displaytag.exception.BaseNestableJspTagException#getSeverity()
         */
        public SeverityEnum getSeverity()
        {
            return SeverityEnum.ERROR;
        }
    }
}