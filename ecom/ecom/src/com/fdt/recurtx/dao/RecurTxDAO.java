package com.fdt.recurtx.dao;

import java.util.List;

import com.fdt.recurtx.dto.ExpiredOverriddenSubscriptionDTO;
import com.fdt.recurtx.dto.RecurTxSchedulerDTO;
import com.fdt.recurtx.entity.RecurTx;
import com.fdt.security.entity.UserAccess;

public interface RecurTxDAO {

    public List<RecurTxSchedulerDTO> getRecurringProfilesForVerification();

    public List<RecurTx> getRecurTransactionsByNode(String userName, String nodeName);

    public List<RecurTx> getRecuringTxDetail(String userName, String recurTxRefNum);

    public void saveRecurTransaction(RecurTx recurTransaction);

    public List<RecurTx> getRecurringTransactionByTxRefNum(String txRefNumber, String siteName);

    public RecurTx getReferencedRecurringTransactionByTxRefNum(String originaltxRefNumber, String siteName);

    public List<RecurTx> getRecurTransactions(String userName);

    public List<RecurTx> getRecurTransactionsBySite(String userName, Long siteId);

    public List<RecurTx> getRecurTxBySite(String siteName);

    public List<RecurTx> getRecurTxByUser(String userName);

	public void archiveRecurTransactions(String archivedBy, String archiveComments);

	public List<ExpiredOverriddenSubscriptionDTO> getExpiredOverriddenSubscriptions();

	public void disableOverriddenSubscription(ExpiredOverriddenSubscriptionDTO expiredOverriddenSubscriptionDTO);

}
