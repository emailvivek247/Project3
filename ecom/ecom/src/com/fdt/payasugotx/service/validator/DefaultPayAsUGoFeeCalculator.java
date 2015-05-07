package com.fdt.payasugotx.service.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.ecom.entity.NonRecurringFee;
import com.fdt.ecom.entity.ShoppingCartItem;
import com.fdt.security.dto.FirmUserDTO;
import com.fdt.security.entity.Access;

public class DefaultPayAsUGoFeeCalculator extends AbstractPayAsUGoFeeCalculator {

    public static String PER_PAGE_FIXED = "PP";

    public static String PER_DOCUMENT_FIXED = "PD";

    public static String PER_PAGE_VARIABLE = "PPV";

    public static String FLAT_RATE_FLAT = "FR";
    public List<ShoppingCartItem> calculateFeeForShoppingcart(List<ShoppingCartItem> shoppingCartItems)
            throws SDLBusinessException {
        Assert.notEmpty(shoppingCartItems, "Shopping Cart Cannot Be Empty!");
        Set<String>  uniqueAccessCodes = new LinkedHashSet<String>();
        for(ShoppingCartItem shoppingCartItem : shoppingCartItems) {
            uniqueAccessCodes.add(shoppingCartItem.getAccessName());
        }
        /** Contains a, that Contains the Key as ACCESS_NAME and valiue as Access Object **/
        Map<String, Access> accessNameNonRecurringFeeMap =  this.eComDAO.getAccessListWithNonRecurringFee(
                new LinkedList<String>(uniqueAccessCodes));

        /** Set the Access Based on the Access Name **/
        for(ShoppingCartItem shoppingCartItem : shoppingCartItems) {
            Assert.notNull(shoppingCartItem.getAccessName(), "ACESS Name Cannot Be Null");
            Access access = accessNameNonRecurringFeeMap.get(shoppingCartItem.getAccessName());
            shoppingCartItem.setAccess(access);
            shoppingCartItem.setSumTxamountPlusServiceFee(access.getNonReccurringFeeList().get(0).isSumTxamountPlusServiceFee());
        }
        return this.getShoppingCartValue(shoppingCartItems);
    }

    /** Method For Calculating Fee Information For Each Access.   **/
    private List<ShoppingCartItem> getShoppingCartValue(List<ShoppingCartItem> shoppingCartItems) 
    		throws SDLBusinessException {
    	
    	//Define a Map that holds the accessId and documents purchased in database
    	// Once we store the count in this map , it stays there and doesn't change
    	Map<Long, Integer> purchasedDocsCountMap = new HashMap<Long, Integer>();
    	// Define a map that holds accessId and total documents purchased including the documents in shopping cart
    	// This count changes as we iterate through shopping cart items/documents 
        Map<Long, Integer> shoppingCartDocsCountMap = new HashMap<Long, Integer>();
        // Defind Firm Users Map which holds list of firm users for a given access id
        Map<Long, List<FirmUserDTO>> firmUsersMap = new HashMap<Long, List<FirmUserDTO>>();
        for (ShoppingCartItem shoppingCartItem : shoppingCartItems)  {
        	
            Double fee = 0.0d;
            Double baseAmount = 0.0d;
            Double serviceFee = 0.0d;
            Double totalTransactionAmount = 0.0d;
            boolean calculateFee = true;
            
            // Lets check if this is a firm level access, if so then check if there is a NoOfDocuments limit
            Access access = shoppingCartItem.getAccess();
            if(access.isGovernmentAccess()){
            	calculateFee = false;
            } else if(access.isFirmLevelAccess() && access.getMaxDocumentsAllowed() > 0){
               
           		// See if we already have retrieved firm users from database
	           	List<FirmUserDTO> firmUsers = firmUsersMap.get(access.getId());
	           	if(firmUsers == null){
	           		// Find all firm users except the current one.
	           		firmUsers = this.userDAO.getFirmUsersbySubscriptionAndUserName(
	           				shoppingCartItem.getUser().getUsername(), access.getId());
	           		firmUsersMap.put(access.getId(),  firmUsers);
	           	}

	           	if(!StringUtils.isBlank(shoppingCartItem.getBarNumber())){
    	           	// See if we already retrieved the firm users from database for the access id
    	           	if(this.barNumberExistsInFirm(firmUsers, shoppingCartItem.getBarNumber())){
    	           		// One of the user in the firm has this bar number, so it should be free item for this user
    	           		calculateFee = false;
    	           	}
               	}

               	// Find out if we already retrieved from database and found the count for a subscription
        		Integer docsCount = shoppingCartDocsCountMap.get(access.getId());
        		if(docsCount == null){
        			// Find docs purchased from database
            		int documentsPurchased = 
            			this.payAsUGoTxDAO.getDocsPurchasedForCurrentSubCycle(shoppingCartItem.getUserId(),
            					access.getId(), this.getBarNumbers(firmUsers, shoppingCartItem.getUser().getBarNumber()));
            		// Store it in maps
            		shoppingCartDocsCountMap.put(access.getId(), new Integer(documentsPurchased));
            		purchasedDocsCountMap.put(access.getId(), new Integer(documentsPurchased));
        		} else {
        			// increase the document count by one as user has more items in the shopping cart
        			// This increament is on the shopping cart document count map
        			if(calculateFee){
        				shoppingCartDocsCountMap.put(access.getId(), docsCount + 1);
        			}
        		}
        		
        		// Set the documents purchased in the shopping cart (UI layer needs this)
    			shoppingCartItem.setDocumentsPurchased(purchasedDocsCountMap.get(access.getId()));
        		 
    			// Find count from shopping cart doc count map and see if fees need to be calculated
        		docsCount = shoppingCartDocsCountMap.get(access.getId());
           		if(docsCount < access.getMaxDocumentsAllowed()){
           			calculateFee = false;
           		}
            }
           	
            if(calculateFee){
	            List<NonRecurringFee> nonRecurringFeeListByAccess = shoppingCartItem.getAccess().getNonReccurringFeeList();
	            for(NonRecurringFee nonRecurringFee: nonRecurringFeeListByAccess) {
	                String subscriptionType = nonRecurringFee.getCode().getCode();
	                if(PER_PAGE_FIXED.equals(subscriptionType)){
	                    fee = this.getFeeForPerPageFixed(shoppingCartItem.getPageCount(), nonRecurringFee);
	                } else if(PER_DOCUMENT_FIXED.equals(subscriptionType)){
	                    fee = this.getFeeForPerDocumentFixed(nonRecurringFee);
	                } else if(PER_PAGE_VARIABLE.equals(subscriptionType)){
	                    fee = this.getFeeForPerPageVariable(shoppingCartItem.getPageCount(), nonRecurringFee);
	                } else if(FLAT_RATE_FLAT.equals(subscriptionType)){
	                    fee = this.getFeeForFlateRate(shoppingCartItem.getPageCount(), nonRecurringFee);
	                }
	                if (nonRecurringFee.isServiceFee()) {
	                    serviceFee = serviceFee + fee;
	                } else {
	                    baseAmount = baseAmount + fee;
	                }
	            }
	            totalTransactionAmount = baseAmount + serviceFee;
            }
            shoppingCartItem.setBaseAmount(baseAmount);
            shoppingCartItem.setServiceFee(serviceFee);
            shoppingCartItem.setTotalTxAmount(totalTransactionAmount);
        }
        return shoppingCartItems;
    }
    
    private List<String> getBarNumbers(List<FirmUserDTO> firmUsers, String barNumber){
    	List<String> barNumbers = new ArrayList<String>();
		for(FirmUserDTO user : firmUsers){
            if(	!StringUtils.isBlank(user.getBarNumber())){
            	barNumbers.add(user.getBarNumber());
            }
		}
		if(!StringUtils.isBlank(barNumber)){
			barNumbers.add(barNumber);
		}
		return barNumbers;
    }
    
	private boolean barNumberExistsInFirm(List<FirmUserDTO> firmUsers, String barNumber){
		boolean barNumberExists = false;
		for(FirmUserDTO user : firmUsers){
            if(	!StringUtils.isBlank(user.getBarNumber()) && 
                	StringUtils.equalsIgnoreCase(user.getBarNumber(), barNumber)){
                barNumberExists = true;
                break;
            }
		}
		return barNumberExists;
	}
    

    /** Method For Calculating Fees In Case Of Per Page Fixed. 
     * 
     * ??? Verify with Vivek
     *  If pageCount > 0
     *  	if pageCount <= PageThreshold 
     *  		fee = FeeUnderPageThreshold
     *  	else 
     *  		fee = FeeUnderPageThreshold + FeeOverPageThreshold
     * 
     * **/
    private Double getFeeForPerPageFixed(int pageCount, NonRecurringFee nonRecurringFee){
        Double fee = 0.0d;
        Long pageThreshold = nonRecurringFee.getPageThreshold();
        Double feeUnderPageThreshold = nonRecurringFee.getFeeUnderPageThreshold();
        Double feeOverPageThreshold = nonRecurringFee.getFeeOverPageThreshold();

        if (pageCount > 0) {
            Long pagesUnderThreshold = 0L;
            if(pageCount > pageThreshold) {
                pagesUnderThreshold = pageThreshold;
                fee = fee + feeOverPageThreshold;
                if (pagesUnderThreshold > 0) {
                    fee = fee + feeUnderPageThreshold;
                }
            } else if(pageCount <= pageThreshold) {
                fee = fee + feeUnderPageThreshold;
            }
        }
        return fee;
    }

    /** Method For Calculating Fees In Case Of Per Page Variable.
     * 
     * ??? Verify with Vivek 
     *  If pageCount > 0
     *  	if pageCount <= PageThreshold 
     *  		fee = FeeUnderPageThreshold
     *  	else 
     *  		fee = FeeUnderPageThreshold + ( FeeOverPageThreshold * (pageCount - PageThreshold))
     *  
     *  
     *  **/
    private Double getFeeForPerPageVariable(int pageCount, NonRecurringFee nonRecurringFee){
        Double fee = 0.0d;
        Long pageThreshold = nonRecurringFee.getPageThreshold();
        Double feeUnderPageThreshold = nonRecurringFee.getFeeUnderPageThreshold();
        Double feeOverPageThreshold = nonRecurringFee.getFeeOverPageThreshold();
        if (pageCount > 0) {
            Long pagesOverThreshold = 0L;
            Long pagesUnderThreshold = 0L;

            if(pageCount > pageThreshold) {
                pagesOverThreshold = pageCount - pageThreshold;
                pagesUnderThreshold = pageThreshold;
                fee = fee + pagesOverThreshold * feeOverPageThreshold;
                if (pagesUnderThreshold > 0) {
                    fee = fee + pagesUnderThreshold * feeUnderPageThreshold;
                }
            } else if(pageCount <= pageThreshold) {
                fee = fee + pageCount * feeUnderPageThreshold;
            }
        }
        return fee;
    }

    /** Method For Calculating Fees In Case Of Per Document Fixed. **/
    private Double getFeeForPerDocumentFixed(NonRecurringFee nonRecurringFee){
        Double fee = 0.0d;
        fee = fee + nonRecurringFee.getFeeUnderPageThreshold();
        return fee;
    }

    /** Method For Calculating Fees In Case Of Flat Rate. **/
    private Double getFeeForFlateRate(int pageCount, NonRecurringFee nonRecurringFee){
        Double fee = 0.0d;
        if (pageCount > 0) {
            fee = fee + nonRecurringFee.getFeeUnderPageThreshold();
        }
        return fee;
    }
    
    private String getMessage(String messageKey, Object[] object) {
        return this.messages.getMessage(messageKey, object, new Locale("en"));
    }
   
}