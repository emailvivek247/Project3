package com.fdt.otctx.service.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fdt.ecom.service.EComService;
import com.fdt.otctx.dao.OTCTxDAO;
import com.fdt.otctx.entity.OTCTx;
import com.fdt.otctx.service.OTCTXService;
import com.fdt.paymentgateway.service.PaymentGatewayService;

@Service("OTCTXAdminService")
public class OTCTXAdminServiceImpl implements OTCTXAdminService {

    private final static Logger logger = LoggerFactory.getLogger(OTCTXAdminServiceImpl.class);

    @Autowired
    private EComService eComService;

    @Autowired
    private OTCTXService oTCTXService;

    @Autowired
    @Qualifier("paymentGateway")
    private PaymentGatewayService paymentGateway;

    @Autowired
    private OTCTxDAO oTCDAO;

    @Transactional(readOnly = true)
    public OTCTx getReferencedOTCTx(String txRefNumber, String siteName) {
        Assert.hasLength(txRefNumber, "Tx Reference Number Cannot be Null/Empty");
        return this.oTCDAO.getReferencedOTCTx(txRefNumber, siteName);
    }
}
