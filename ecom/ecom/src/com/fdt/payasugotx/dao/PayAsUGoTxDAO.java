package com.fdt.payasugotx.dao;

import java.util.Date;
import java.util.List;

import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.ecom.entity.Location;
import com.fdt.ecom.entity.ShoppingCartItem;
import com.fdt.ecom.entity.Site;
import com.fdt.payasugotx.entity.PayAsUGoTx;
import com.fdt.payasugotx.entity.PayAsUGoTxItem;
import com.fdt.payasugotx.entity.PayAsUGoTxView;

public interface PayAsUGoTxDAO {

    public List<PayAsUGoTxView> getPayAsUGoTransactionsByNode(String userName, String nodeName, String comments,
    		Date fromDate, Date toDate) ;

	public PageRecordsDTO getPayAsUGoTransactionsByNodePerPage(String firmUserName, String nodeName, String comments,
			 Date fromDate, Date toDate, int startingFrom, int numberOfRecords);



    public PayAsUGoTx getPayAsUGoTransactionDetail(String userName, Long payAsUGoTxId, String isRefund);

    public PayAsUGoTx getPayAsUGoTransactionByTxRefNum(String txRefNumber, String siteName);

    public PayAsUGoTx getPayAsUGoTransactionItemByItemId(Long itemId, String siteName);

    public PayAsUGoTx getReferencedPayAsUGoTransactionItemByItemId(Long itemId, String siteName);

    public List<PayAsUGoTx> getReferencedPayAsUGoTransaction(String txRefNumber, String siteName);

    public void savePayAsUGoTransaction(PayAsUGoTx payAsUGoTransaction);

    public PayAsUGoTxItem getPayAsUGoTxIdForPurchasedDoc(String userName, String productId, String uniqueIdentifier);


    public void savePayAsUGoTransactionItem(List<PayAsUGoTxItem> payAsUGoTransactionItems);

    public int updateRefundTxForPayAsUGoTxItem(Long payAsUGoTxItemId, Long refundTxId, String modifiedBy);

    public int updateRefundTxForPayAsUGoTxItems(Long originalTxId, Long refundTxId, String modifiedBy);

    public List<ShoppingCartItem> getShoppingCart(String userName);

    public List<ShoppingCartItem> getShoppingCart(String userName, String nodeName);

    public void saveShoppingCartItem(ShoppingCartItem shoppingCartItem);

    public void deleteShoppingCart(List<ShoppingCartItem> shoppingCartItems);

    public void deleteShoppingCartItem(ShoppingCartItem shoppingCartItem);

    public List<PayAsUGoTx> getPayAsUGoTransactions(String userName);

    public List<PayAsUGoTx> getPayAsUGoTransactionsBySite(String userName, Long siteId);

	public int getDocsPurchasedForCurrentSubCycle(Long userId, Long accessId, List<String> barNumbers);

	public void updateShoppingCartComments(Long shoppingCartId, String comments);

	public Location getLocationByNameAndAccessName(String locationName, String accessName);

	public List<Location> getLocationsBySiteId(Long siteId);

	public Location getLocationSignatureById(Long locationId);

	public Location getLocationById(Long locationId);

	public void archivePayAsUGoTransactions(String archivedBy, String archiveComments);

	public Double getGranicusRevenueFromPayAsUGoTx(Site site);

	public String getDocumentIdByCertifiedDocumentNumber(String certifiedDocumentNumber, String siteName);


}
