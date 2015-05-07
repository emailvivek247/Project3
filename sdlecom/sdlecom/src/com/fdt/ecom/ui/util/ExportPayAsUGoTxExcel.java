package com.fdt.ecom.ui.util;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.fdt.common.export.AbstractExcelExport;
import com.fdt.payasugotx.entity.PayAsUGoTxView;

public class ExportPayAsUGoTxExcel extends AbstractExcelExport {
	
	private static int[] PAYASUGO_PDF_COLUMN_WIDTHS = new int[] {30, 18, 14, 10, 10, 30, 8, 16, 10};	

	public ExportPayAsUGoTxExcel(){
	}
	
	public byte[] exportToExcel(List<PayAsUGoTxView> payAsuGoTxList) throws IOException {
		super.init();

		// Add the headers first
		super.createHeaderRow(ExportConstants.getPayAsUGoHeaders());
		
		// Iterate through and add the rows
		for(PayAsUGoTxView payAsUGoTx : payAsuGoTxList){
			super.createRow(this.getPayAsUGoTxRow(payAsUGoTx));
		}
		return super.getDocumentBytes();
	}
	
	private List<String> getPayAsUGoTxRow(PayAsUGoTxView payAsUGoTx){
		List<String> row = new ArrayList<String>();
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa z");  
		
		String paymentDate = payAsUGoTx.getTransactionDate() != null ? df.format(payAsUGoTx.getTransactionDate()) : "";
		row.add(payAsUGoTx.getUserName());
		row.add(paymentDate);
		row.add(payAsUGoTx.getTxRefNum());
		row.add(payAsUGoTx.getTransactionType().toString());
		row.add(payAsUGoTx.getSiteDescription());
		row.add(payAsUGoTx.getSubscription());
		row.add(payAsUGoTx.getCardNumber());
		row.add(payAsUGoTx.getAccountName());
		row.add(payAsUGoTx.getTotalTxAmount().toString());
		return row;
	}
	
	
	
	
}