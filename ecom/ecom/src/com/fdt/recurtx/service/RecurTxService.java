package com.fdt.recurtx.service;

import java.util.List;

import com.fdt.recurtx.entity.RecurTx;

public interface RecurTxService {

	/** This Method Is Used To Get The List Of Recurring Transactions Of The Supplied userName For A nodeName.
    * @param userName EmailId Of The User Logged In.
    * @param nodeName Name Of the Node.
    * @return List Of Recurring Transactions.
    */
    public List<RecurTx> getRecurTxByNode(String userName, String nodeName);

    /** This Method Is Used To Get The List Of Recurring Transactions Associated With The recurTxRefNum Of a userName.
     * @param userName EmailId Of The User Logged In.
     * @param recurTxRefNum Recurring Transaction Reference Number.
     * @return List Of Recurring Transactions.
     */
    public List<RecurTx> getRecurTxDetail(String userName, String recurTxRefNum);

}
