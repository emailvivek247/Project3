package com.fdt.webtx.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fdt.webtx.dto.PaymentInfoDTO;
import com.fdt.webtx.entity.WebCaptureTx;
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

	public void archiveWebTransactions(String archivedBy, String archiveComments);
	
	public void saveWebCaptureTx(WebCaptureTx webCaptureTx);

	public Map<Long, PaymentInfoDTO> getPaymentInfoMap(List<String> paymentTokens);

	public PaymentInfoDTO getPaymentInfoByID(Long paymentInfoID);

}