package com.fdt.achtx.service;

import com.fdt.achtx.dto.ACHTxDTO;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.exception.SDLException;
import com.fdt.ecom.entity.enums.PaymentType;

public interface ACHTxService {

    /**
     * @param siteId
     * @param paymentType
     * @param createdBy
     * @param machineName
     * @return
     * @throws SDLException
     */
    public ACHTxDTO getACHDetailsForTransfer(Long siteId, PaymentType paymentType,
        String createdBy, String machineName)  throws SDLException;

    /**
     * @param paymentType
     * @param siteId
     * @param machineIp
     * @param createdBy
     * @return
     * @throws SDLException
     * @throws SDLBusinessException
     */
    public ACHTxDTO doACHTransfer(PaymentType paymentType, Long siteId, String machineIp,
        String createdBy) throws SDLException, SDLBusinessException;
}