package com.fdt.webtx.dao;

import java.util.Date;
import java.util.List;

import com.fdt.webtx.entity.WebTx;
import com.fdt.webtx.entity.WebTxItem;

public interface WebTxDAO {

    public WebTx getWebTransactionByTxRefNum(String txRefNumber, String siteName);

    public List<WebTx> getWebTransactionsForExtApp(String siteName, Date fromDate, Date endDate, String txType);

    public WebTx getWebTransactionItemByItemId(Long itemId, String siteName);

    public WebTx getReferencedWebTransactionItemByItemId(Long itemId, String siteName);

    public List<WebTx> getReferencedWebTransaction(String txRefNumber, String siteName);

    public void saveWebTransaction(WebTx webTransaction);

    public void saveWebTransactionItem(List<WebTxItem> webTransactionItems);

    public int updateRefundTxForWebTxItem(Long webTxItemId, Long refundTxId, String modifiedBy);

    public int updateRefundTxForWebTxItems(Long originalTxId, Long refundTxId, String modifiedBy);

	public WebTx getWebTxByInvoiceNumber(String invoiceId, String siteName);

}