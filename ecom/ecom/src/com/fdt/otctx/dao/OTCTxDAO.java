package com.fdt.otctx.dao;

import com.fdt.otctx.entity.OTCTx;

public interface OTCTxDAO {

    public void saveOTCTx(OTCTx oTCTransaction);

    public OTCTx getOTCTxByTxRefNum(String txRefNumber, String siteName);

    public OTCTx getReferencedOTCTx(String txRefNumber, String siteName);

	public OTCTx getOTCTransactionByInvoiceNumber(String invoiceNumber, String siteName);

	public void archiveOTCTransactions(String archivedBy, String archiveComments);
}