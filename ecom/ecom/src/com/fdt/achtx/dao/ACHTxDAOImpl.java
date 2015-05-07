package com.fdt.achtx.dao;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import com.fdt.achtx.dto.ACHTxDTO;
import com.fdt.achtx.entity.CheckHistory;
import com.fdt.common.dao.AbstractBaseDAOImpl;
import com.fdt.ecom.entity.enums.PaymentType;

@Repository
public class ACHTxDAOImpl extends AbstractBaseDAOImpl implements ACHTxDAO   {

    public ACHTxDTO doACHTransfer(PaymentType paymentType, Long siteId, String machineIp,
            String createdBy) {
        ACHTxDTO acHDTO = null;
        Session session = currentSession();
        Query sqlQuery = session.getNamedQuery("GET_ACH_DETAILS")
                             .setParameter("siteId", siteId)
                             .setParameter("paymentType", paymentType.toString())
                             .setParameter("modifiedBy", createdBy)
                             .setParameter("machineName", machineIp)
                             .setParameter("markAsSettled", "Y");
        List<Object> resultList = sqlQuery.list();
        if(!resultList.isEmpty()) {
            Object[] row = (Object[]) resultList.get(0);
            acHDTO = new ACHTxDTO();
            acHDTO.setSiteName(this.getString(row[0]));
            acHDTO.setCheckNum(this.getString(row[1]));
            acHDTO.setTotalTransactions(this.getInteger(row[2]));
            acHDTO.setTxAmount(this.getDoubleFromBigDecimal(row[3]));
            acHDTO.setResultMsg(this.getString(row[4]));
            acHDTO.setTxCutOffTime(this.getDate(row[5]));
            acHDTO.setAchLoginKey(row[6] == null ? null : this.getPbeStringEncryptor().decrypt(row[6].toString()));
            acHDTO.setPin(row[7] == null ? null : this.getPbeStringEncryptor().decrypt(row[7].toString()));
            acHDTO.setAcctName(this.getString(row[8]));
            acHDTO.setAcctNumber(this.getString(row[9]));
            acHDTO.setAcctRoutingNo(this.getString(row[10]));
            acHDTO.setStartTxDate(this.getDate(row[11]));
            acHDTO.setClientIp(machineIp);
            acHDTO.setPaymentType(paymentType);
        }
        return acHDTO;
    }

    public ACHTxDTO getACHDetailsForTransfer(Long siteId,
            PaymentType paymentType, String modifiedBy, String machineIp) {
    	ACHTxDTO acHDTO = null;
        Session session = currentSession();
        Query sqlQuery = session.getNamedQuery("GET_ACH_DETAILS")
                             .setParameter("siteId", siteId)
                             .setParameter("paymentType", paymentType.toString())
                             .setParameter("modifiedBy", modifiedBy)
                             .setParameter("machineName", machineIp)
                             .setParameter("markAsSettled", "N");
        List<Object> resultList = sqlQuery.list();
        if(!resultList.isEmpty()) {
            Object[] row = (Object[]) resultList.get(0);
            acHDTO = new ACHTxDTO();
            acHDTO.setSiteName(this.getString(row[0]));
            acHDTO.setCheckNum(this.getString(row[1]));
            acHDTO.setTotalTransactions(this.getInteger(row[2]));
            acHDTO.setTxAmount(this.getDoubleFromBigDecimal(row[3]));
            acHDTO.setResultMsg(this.getString(row[4]));
            acHDTO.setTxCutOffTime(this.getDate(row[5]));
            acHDTO.setAchLoginKey(row[6] == null ? null : this.getPbeStringEncryptor().decrypt(row[6].toString()));
            acHDTO.setPin(row[7] == null ? null : this.getPbeStringEncryptor().decrypt(row[7].toString()));
            acHDTO.setAcctName(this.getString(row[8]));
            acHDTO.setAcctNumber(this.getString(row[9]));
            acHDTO.setAcctRoutingNo(this.getString(row[10]));
            acHDTO.setStartTxDate(this.getDate(row[11]));
            acHDTO.setClientIp(machineIp);
            acHDTO.setPaymentType(paymentType);
        }
        return acHDTO;
    }

    public void updateCheckHistory(String checkNumber, String achTxRefNumber) {
        Session session = currentSession();
        Query checkHistoryQuery = session.createQuery("from CheckHistory where checkNumber = :checkNumber");
        checkHistoryQuery.setParameter("checkNumber", Long.parseLong(checkNumber));
        CheckHistory checkHistory = (CheckHistory)checkHistoryQuery.list().get(0);
        checkHistory.setAchTxRefNumber(achTxRefNumber);
        checkHistory.setModifiedDate(new Date());
        session.saveOrUpdate(checkHistory);
        session.flush();
    }
}