package com.fdt.payasugotx.service.validator;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.ecom.entity.ShoppingCartItem;
import com.fdt.payasugotx.dao.PayAsUGoTxDAO;
import com.fdt.payasugotx.entity.PayAsUGoTxItem;
import com.fdt.security.entity.Access;

@Component
public class PayAsUGoTxValidator {


    @Autowired
    @Qualifier("serverMessageSource")
    private MessageSource messages = null;

    @Autowired
    private PayAsUGoTxDAO payAsUGoSubDAO = null;

    public void validateCreditCardWithAccess(List<ShoppingCartItem> shoppingCartItems)
    			throws SDLBusinessException{
    	//IF shopping cart has just one item, no validation is needed
    	if(shoppingCartItems.size() < 2){
    		return;
    	}
    	
    	// Check if shopping cart has firm level & non-firm level access items
    	// Throw an exception if it is the case
    	Access firmLevelAccess = null;
    	Access nonfirmLevelAccess = null;
    	for(ShoppingCartItem item : shoppingCartItems){
    		if(item.getAccess().isFirmLevelAccess()){
    			firmLevelAccess = item.getAccess();
    		} else {
    			nonfirmLevelAccess= item.getAccess();
    		}
    	}
    	
    	if(firmLevelAccess != null && nonfirmLevelAccess != null){
    		throw new SDLBusinessException(this.getMessage("payasugotx.shoppingcart.firmLevelNonFirmLevelAccessInvalid"));
    	}
    }
    
    public void validateFirmDocumentAlreadyPurchased(List<ShoppingCartItem> shoppingCartItems) 
			throws SDLBusinessException{
    	for (ShoppingCartItem shoppingCartItem : shoppingCartItems)  {
    		if(shoppingCartItem.getAccess().isFirmLevelAccess()){
    			PayAsUGoTxItem item = this.payAsUGoSubDAO.getPayAsUGoTxIdForPurchasedDoc(shoppingCartItem.getUser().getUsername(), 
	    				shoppingCartItem.getProductId(), shoppingCartItem.getUniqueIdentifier());
	    		if(item != null){
	    			throw new SDLBusinessException(this.getMessage("payasugotx.shoppingcart.itemAlreadyPurchasedByFirmUser",
	    					new String[]{shoppingCartItem.getProductId()}));
	    		}
    		}
    	}
    }

    
    /**
     * Validate for active subscriptions
     * This is needed in case user (specially firm user) had added an item to shopping cart earlier
     * and user's subscription was disabled / cancelled.
     * 
     *  accessList has only those accesses for which UserAccess is active for an user
     *  
     * @param shoppingCartItems
     * 
     * @throws SDLBusinessException
     */
    public void validateForActiveSubscription(List<ShoppingCartItem> shoppingCartItems, List<Access> accessList) 
			throws SDLBusinessException{
    	for (ShoppingCartItem shoppingCartItem : shoppingCartItems)  {
    		boolean foundActiveAccess = false;
            for(Access access : accessList){
            	if(access.getId().equals(shoppingCartItem.getAccess().getId())){
            		foundActiveAccess = true;
            		break;
            	}
            }
            // No Active Access found
            if(!foundActiveAccess){
    			throw new SDLBusinessException(this.getMessage("payasugotx.shoppingcart.subscriptionnotactive",
    					new String[]{shoppingCartItem.getProductId()}));
            }

    	}
    }
    

    private String getMessage(String messageKey, Object[] object) {
        return this.messages.getMessage(messageKey, object, new Locale("en"));
    }

    private String getMessage(String messageKey) {
        return this.messages.getMessage(messageKey, null, new Locale("en"));
    }


}
