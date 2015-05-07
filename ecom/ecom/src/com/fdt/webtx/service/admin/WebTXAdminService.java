package com.fdt.webtx.service.admin;

import java.util.List;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.paymentgateway.dto.PayPalDTO;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.webtx.entity.WebTx;

public interface WebTXAdminService {

    public PayPalDTO doPartialReferenceCreditWeb(Long webTxItemId, String siteName, String comments,
            String modUserId, String machineName) throws PaymentGatewayUserException, PaymentGatewaySystemException,
            SDLBusinessException;

    public WebTx getWebTransactionItemByItemId(Long itemId, String siteName);

    public WebTx getReferencedWebTransactionItemByItemId(Long itemId, String siteName);

    public List<WebTx> getReferencedWebTransaction(String txRefNumber, String siteName);


}
