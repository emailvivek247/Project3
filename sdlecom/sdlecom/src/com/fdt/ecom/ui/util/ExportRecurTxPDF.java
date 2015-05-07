package com.fdt.ecom.ui.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import com.fdt.common.export.AbstractPDFExport;
import com.fdt.recurtx.entity.RecurTx;

public class ExportRecurTxPDF extends AbstractPDFExport {
	
	private static int[] COLUMN_WIDTHS = new int[] {18, 14, 10, 30, 8, 16, 10, 10};	

	public ExportRecurTxPDF(){
	}
	
	public byte[] exportToPDF(List<RecurTx> recurTxList) throws Exception {
		super.init(COLUMN_WIDTHS.length, COLUMN_WIDTHS);

		// Add the headers first
		super.createHeaderRow(ExportConstants.getRecurHeaders());
		
		// Iterate through and add the rows
		for(RecurTx recurTx : recurTxList){
			this.addRecurTxRow(recurTx);
		}
		return super.getDocumentBytes();
	}
	
	private void addRecurTxRow(RecurTx recurTx){
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa z");  
		String paymentDate = recurTx.getTransactionDate() != null ? df.format(recurTx.getTransactionDate()) : "";
		super.addNormalCell(paymentDate, false);
		super.addNormalCell(recurTx.getTxRefNum(), false);
		super.addNormalCell(recurTx.getSite().getDescription(), false);
		super.addNormalCell(recurTx.getAccess().getDescription(), true);
		super.addNormalCell(recurTx.getCardNumber(), false);
		super.addNormalCell(recurTx.getAccountName(), true);
		super.addNormalCell(recurTx.getTransactionType().toString(), false);
		super.addNormalCell(recurTx.getTotalTxAmount().toString(), false);
		super.endTableRow();
	}
	
	
}