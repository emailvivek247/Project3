package com.fdt.ecom.ui.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import com.fdt.common.export.AbstractPDFExport;
import com.fdt.payasugotx.entity.PayAsUGoTxView;

public class ExportPayAsUGoTxPDF extends AbstractPDFExport {

	private static int[] COLUMN_WIDTHS = new int[] {30, 18, 14, 10, 10, 30, 8, 16, 10};	

	public ExportPayAsUGoTxPDF(){
	}
	
	public byte[] exportToPDF(List<PayAsUGoTxView> payAsUGoTxList) throws Exception {
		super.init(COLUMN_WIDTHS.length, COLUMN_WIDTHS);

		// Add the headers first
		super.createHeaderRow(ExportConstants.getPayAsUGoHeaders());
		
		// Iterate through and add the rows
		for(PayAsUGoTxView payAsUGoTx : payAsUGoTxList){
			this.addPayAsUGoTxTxRow(payAsUGoTx);
		}
		return super.getDocumentBytes();
	}
	
	private void addPayAsUGoTxTxRow(PayAsUGoTxView payAsUGoTx ){
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa z");  
		String paymentDate = payAsUGoTx.getTransactionDate() != null ? df.format(payAsUGoTx.getTransactionDate()) : "";
		super.addNormalCell(payAsUGoTx.getUserName(), false);
		super.addNormalCell(paymentDate, false);
		super.addNormalCell(payAsUGoTx.getTxRefNum(), false);
		super.addNormalCell(payAsUGoTx.getTransactionType().toString(), false);
		super.addNormalCell(payAsUGoTx.getSiteDescription(), false);
		super.addNormalCell(payAsUGoTx.getSubscription(), true);
		super.addNormalCell(payAsUGoTx.getCardNumber(), false);
		super.addNormalCell(payAsUGoTx.getAccountName(), true);
		super.addNormalCell(payAsUGoTx.getTotalTxAmount().toString(), false);
		super.endTableRow();
	}
	
	
}