package com.fdt.payasugotx.service.admin;

import java.util.List;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.payasugotx.entity.PayAsUGoTx;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;

public interface PayAsUGoTxAdminService {

    public PayPalDTO doPartialReferenceCreditPayAsUGo(Long webTxItemId, String siteName, String comments,
            String modUserId, String machineName) throws PaymentGatewayUserException, PaymentGatewaySystemException,
            SDLBusinessException;

    public PayAsUGoTx getPayAsUGoTransactionItemByItemId(Long itemId, String siteName);

    public PayAsUGoTx getReferencedPayAsUGoTransactionItemByItemId(Long itemId, String siteName);

    public List<PayAsUGoTx> getReferencedPayAsUGoTransaction(String txRefNumber, String siteName);

	public List<PayAsUGoTx> getPayAsUGoTransactions(String userName, Long siteId);


}
