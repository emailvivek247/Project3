package com.fdt.ecom.ui.util;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.fdt.common.export.AbstractExcelExport;
import com.fdt.recurtx.entity.RecurTx;

public class ExportRecurTxExcel extends AbstractExcelExport {
	

	public ExportRecurTxExcel(){
	}
	
	public byte[] exportToExcel(List<RecurTx> recurTxList) throws IOException {
		super.init();

		// Add the headers first
		super.createHeaderRow(ExportConstants.getRecurHeaders());
		
		// Iterate through and add the rows
		for(RecurTx recurTx : recurTxList){
			super.createRow(this.getRecurTxRow(recurTx));
		}
		return super.getDocumentBytes();
	}
	
	private List<String> getRecurTxRow(RecurTx recurTx){
		List<String> row = new ArrayList<String>();
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa z");  
		
		String paymentDate = recurTx.getTransactionDate() != null ? df.format(recurTx.getTransactionDate()) : "";
		row.add(paymentDate);
		row.add(recurTx.getTxRefNum());
		row.add(recurTx.getSite().getDescription());
		row.add(recurTx.getAccess().getDescription());
		row.add(recurTx.getCardNumber());
		row.add(recurTx.getAccountName());
		row.add(recurTx.getTransactionType().toString());
		row.add(recurTx.getTotalTxAmount().toString());
		return row;
	}
	
	
	
	
}