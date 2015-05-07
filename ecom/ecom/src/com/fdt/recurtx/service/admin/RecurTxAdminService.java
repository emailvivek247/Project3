package com.fdt.recurtx.service.admin;

import java.util.List;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.recurtx.dto.RecurTxSchedulerDTO;
import com.fdt.recurtx.entity.RecurTx;

public interface RecurTxAdminService {

	/** This Method Is Used To do A Reference Credit For Recurring Transactions.
     * @param txRefNumber Original Transaction Reference Number.
     * @param comments Comments Supplied By The User Who Is Issuing A Refund.
     * @param modUserId User Who Is Issuing A Refund.
     * @param machineName Machine From Which User Is Issuing a Refund.
     * @param siteName Name Of The Site.
     * @return Returns DTO With TxNumber Set If Payment Goes Through. If not, errorMessage & errorDescription Is Set.
     * @throws PaymentGatewayUserException
     * @throws PaymentGatewaySystemException
     * @throws SDLBusinessException
     */
    public PayPalDTO doReferenceCreditRecurTx(String txRefNumber, String comments, String modUserId,
        String machineName, String siteName) throws PaymentGatewayUserException, PaymentGatewaySystemException,
            SDLBusinessException;

    /** This Method Is Used To Get The Refund Transaction Number For The Supplied Original Transaction Reference Number.
     * @param originaltxRefNumber Transaction Reference Number.
     * @param siteName Name Of the Site.
     * @return RecurTransaction Of Refund Transaction.
     */
    public RecurTx getReferencedRecurTxByTxRefNum(String originaltxRefNumber, String siteName);

    /** This Method Is Used To Get The Details Of Supplied Transaction Reference Number.
     * @param txRefNumber Transaction Reference Number.
     * @param siteName Name Of The Site.
     * @return List Of RecurTransactions
     */
    public List<RecurTx> getRecurTxByTxRefNum(String txRefNumber, String siteName);

    /** Called By Scheduler To Archive Cancelled Subscriptions. Cancelled Subscriptions Are Subscriptions
     *  Which Are Manually Cancelled By The User. So, User Can Access These Subscriptions Until The End Of
     *  Subscription Recurring Cycle Period. At The End Of Period, Scheduler Calls This Method To Delete The
     *  Subscriptions Of The User.
     */
    public void archiveCancelledRecurSub();

    /** This Method Is Called By Scheduler To Do Recurring Payments. This Method Returns A List Of DTOs Which Contain
     * All The Necessary Information To Charge For The Recurring Billing Cycles.
     * @return List Of RecurTxSchedulerDTOs.
     */
    public List<RecurTxSchedulerDTO> getRecurProfilesForVerification();

    /** Called From Scheduler For Each Profile. This Method Is Used To Charge Recurring Subscription Users Every Recurring
     * Billing Cycle. If Payment Goes Through, Then Recurring Transaction Is Saved, Billing Dates Are Updated And Payment
     * Successful Email Is Sent. If Payment Is Not Successful, Then UserAccount Is Disabled, UserAccess Is Disabled,
     * CreditCard Is Disabled, And Finally Payment Failure Mail Is Sent.
     * @param payPalSchDTO
     */
    public void chargeRecurSub(RecurTxSchedulerDTO payPalSchDTO);

    /** This Method Returns All The Recurring Transactions Associated To A User.
     * @param siteName Name Of A Site.
     * @return List Of Recurring Transactions.
     */
    public List<RecurTx> getRecurTxByUser(String userName);

    /** This Method Returns All The Recurring Transactions Made On A Particular Site.
     * @param userName EmailId of the user logged in.
     * @return List Of Recurring Transactions.
     */
    public List<RecurTx> getRecurTxBySite(String siteName);

    /** This Method Returns All The Recurring Transactions Made By A User On A Particular Site.
	 * @param userName EmailId Of The User Logged In.
	 * @param siteId siteId
	 * @return List Of Recurring Transactions.
	 */
	public List<RecurTx> getRecurTxByUserAndSite(String userName, Long siteId);
}
