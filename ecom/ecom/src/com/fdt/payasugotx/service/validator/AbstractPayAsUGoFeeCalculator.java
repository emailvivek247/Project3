package com.fdt.payasugotx.service.validator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.ecom.dao.EComDAO;
import com.fdt.ecom.entity.ShoppingCartItem;
import com.fdt.payasugotx.dao.PayAsUGoTxDAO;
import com.fdt.security.dao.UserDAO;

public abstract class AbstractPayAsUGoFeeCalculator {

    @Autowired
    protected EComDAO eComDAO = null;

    @Autowired
    protected UserDAO userDAO = null;

    @Autowired
    protected PayAsUGoTxDAO payAsUGoTxDAO = null;
    
    @Autowired
    @Qualifier("serverMessageSource")
    protected MessageSource messages = null;
    

    public abstract List<ShoppingCartItem> 
    		calculateFeeForShoppingcart(List<ShoppingCartItem> inputShoppingCart)
        throws SDLBusinessException;
}