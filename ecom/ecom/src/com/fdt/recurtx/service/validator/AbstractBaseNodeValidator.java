package com.fdt.recurtx.service.validator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.ecom.dao.EComDAO;
import com.fdt.recurtx.dao.RecurTxDAO;
import com.fdt.subscriptions.dao.SubDAO;

public abstract class AbstractBaseNodeValidator {

    @Autowired
    protected EComDAO eComDAO = null;

    @Autowired
    protected SubDAO subDAO = null;

    @Autowired
    protected RecurTxDAO recurTransactionDAO = null;

    public abstract void checkForValidSubscription(String userName, List<Long> newAccessIdList, String nodeName)
            throws SDLBusinessException;

}