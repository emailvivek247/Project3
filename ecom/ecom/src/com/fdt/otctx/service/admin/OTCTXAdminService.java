package com.fdt.otctx.service.admin;

import com.fdt.otctx.entity.OTCTx;

public interface OTCTXAdminService {

    public OTCTx getReferencedOTCTx(String txRefNumber, String siteName);

}
