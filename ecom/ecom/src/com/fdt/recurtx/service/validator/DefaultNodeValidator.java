package com.fdt.recurtx.service.validator;

import java.util.LinkedList;
import java.util.List;

import com.fdt.common.exception.SDLBusinessException;
import com.fdt.ecom.entity.Site;
import com.fdt.security.entity.Access;
import com.fdt.security.entity.User;
import com.fdt.security.entity.enums.AccessType;
import com.fdt.subscriptions.dto.AccessDetailDTO;

public class DefaultNodeValidator extends AbstractBaseNodeValidator {

	/**
	 * Check for two validations
	 *  1. User can not add the same subscription again.
	 *  2. User can not have more than one subscription in the same subscription category. i.e web recurring
	 */
    @Override
    public void checkForValidSubscription(String userName,
            List<Long> newAccessIdList, String nodeName)
            throws SDLBusinessException {
        boolean flagForOnlyOneSubscriptionTypeForUserException = false;
        boolean flagForDuplicateSubscriptionForUserException = false;
        int RecurringSubscriptions = 0;
        int NonRecurringSubscriptions = 0;
        int FreeSubscriptions = 0;
        int certNonRecurringSubscriptoins = 0;
        User user = this.subDAO.getCurrentSubscriptions(userName);
        if (user != null) {
            List<Access> currentAccessList = user.getAccess();
            List<Access>  newAccessList = new LinkedList<Access>();
            List<Access>  mergedAccessList = new LinkedList<Access>();

            List<AccessDetailDTO> newAccessDTOList = this.subDAO.getSubDetailsByAccesIds(newAccessIdList);
            for(AccessDetailDTO accessDetailDTO : newAccessDTOList){
                newAccessList.add(accessDetailDTO.getSite().getAccess().get(0));
            }
            // Check for Duplication of Subscriptions
            for(Access access : currentAccessList) {
                if (newAccessList.contains(access)) {
                    flagForDuplicateSubscriptionForUserException = true;
                    break;
                }
            }
            if (flagForDuplicateSubscriptionForUserException) {
                throw new SDLBusinessException("Users cannot have duplicate subscriptions");
            }
            mergedAccessList.addAll(currentAccessList);
            mergedAccessList.addAll(newAccessList);

            List<Site> siteList = this.eComDAO.getSitesForNode(nodeName);
            for(Site site : siteList) {
                List<Access> siteSpecificAccessList = site.getAccess();
                    FreeSubscriptions =0;
                    RecurringSubscriptions = 0;
                    NonRecurringSubscriptions = 0;
                    certNonRecurringSubscriptoins = 0;
                  for(Access access : siteSpecificAccessList) {
                      if (mergedAccessList.contains(access)) {
                          if (access.getAccessType() == AccessType.FREE_SUBSCRIPTION) {
                              FreeSubscriptions = FreeSubscriptions + 1;
                          } else if (access.getAccessType() == AccessType.RECURRING_SUBSCRIPTION) {
                              RecurringSubscriptions = RecurringSubscriptions + 1;
                          }  else if (access.getAccessType() == AccessType.NON_RECURRING_SUBSCRIPTION) {
                              NonRecurringSubscriptions = NonRecurringSubscriptions + 1;
                          } else if (access.getAccessType() == AccessType.CERTIFIED_NON_RECURRING_SUBSCRIPTION) {
                        	  certNonRecurringSubscriptoins = certNonRecurringSubscriptoins + 1;
                          }
                      }
                  }
                  if (FreeSubscriptions > 1 || RecurringSubscriptions > 1 || NonRecurringSubscriptions > 1 || certNonRecurringSubscriptoins > 1) {
                      flagForOnlyOneSubscriptionTypeForUserException = true;
                      break;
                  }
            }
            if(flagForOnlyOneSubscriptionTypeForUserException == true) {
                throw new SDLBusinessException("You can only have one subscription of one particular " +
                		"type(Recurring, Pay-As-You-Go) within a particular site. " +
                		"If you are trying to add a recurring subscription you have the option to Upgrade or Downgrade your current subscription. " +
                		"To upgrade or downgrade your subscription go to the Account Information page, click on the Manage link corresponding to the subscription you want to modify, " +
                		"then select the subscription to which you want to change and click on change.");
            }
        }
    }
}