package com.fdt.achtx.dao;

import com.fdt.achtx.dto.ACHTxDTO;
import com.fdt.ecom.entity.enums.PaymentType;

public interface ACHTxDAO {

    public ACHTxDTO doACHTransfer(PaymentType paymentType, Long siteId, String machineIp, String createdBy);

    public ACHTxDTO getACHDetailsForTransfer(Long siteId, PaymentType paymentType, String createdBy, String machineName);

    public void updateCheckHistory(String checkNumber, String achTxRefNumber);

}
