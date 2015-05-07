package com.fdt.ecom.service;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.fdt.recurtx.dto.RecurTxSchedulerDTO;
import com.fdt.recurtx.service.admin.RecurTxAdminService;
import com.fdt.security.entity.User;
import com.fdt.security.entity.UserAccess;
import com.fdt.security.service.admin.UserAdminService;

@Service
public class EComSchedulerService {

    private final static Logger logger = LoggerFactory.getLogger(EComSchedulerService.class);

    @Autowired
    private RecurTxAdminService recurTXAdminService = null;

    @Autowired
    private UserAdminService userAdminService = null;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Value("${scheduler.ecom.isenabled}")
    private boolean isSchedulerEnabled = false;

    @Scheduled(cron= "${scheduler.ecom.chargerecurringprofiles}")
    public void chargeRecurringProfiles() {
        if (!this.isSchedulerEnabled()) {
            logger.info("Skiping Scheduler Service Charge Recurring Profiles");
            return;
        }
        logger.info("Starting Scheduler Service To Charge Recurring Profiles");
        List <RecurTxSchedulerDTO> recurTxSchedulerDTOs = recurTXAdminService.getRecurProfilesForVerification();
        for (final RecurTxSchedulerDTO recurTxSchedulerDTO : recurTxSchedulerDTOs) {
            /** Start of Transaction **/
            transactionTemplate.execute(new TransactionCallback<Void>() {
                public Void doInTransaction(TransactionStatus txStatus) {
                    try {
                        recurTXAdminService.chargeRecurSub(recurTxSchedulerDTO);
                    } catch (Exception exp) {
                        logger.error(NOTIFY_ADMIN, "Exception in the EComScheduler for RecurTxSchedulerDTO {}",
                            recurTxSchedulerDTO, exp);
                        txStatus.setRollbackOnly();
                    }
                    return null;
                }
            });
            /** End of of Transaction **/
        }
        logger.info("Ending Scheduler Service Charge Recurring Profiles");
    }

    @Scheduled(cron= "${scheduler.ecom.cancelrecurringprofile}")
    public void cancelRecurringProfiles() {
        if (!this.isSchedulerEnabled()) {
            logger.info("Skiping Scheduler Service Cancel Recurring Profiles");
            return;
        }
        logger.info("Starting Scheduler Service Cancel Pay Pal Profiles");
        /** Start of Transaction **/
        transactionTemplate.execute(new TransactionCallback<Void>() {
            public Void doInTransaction(TransactionStatus txStatus) {
                try {
                    recurTXAdminService.archiveCancelledRecurSub();
                } catch (RuntimeException runTimeException) {
                    logger.error(NOTIFY_ADMIN, "Exception in the cancelRecurringProfiles", runTimeException);
                }
                return null;
            }
        });
        /** End of of Transaction **/
        logger.info("Ending Scheduler Service Cancel Recurring Profiles");
    }

    @Scheduled(cron= "${scheduler.ecom.notifyinactiveusers}")
    public void notifyInactiveUsers() {
        if (!this.isSchedulerEnabled()) {
            logger.info("Skiping Scheduler Service Notify Inactive Users");
            return;
        }
        logger.info("Starting Scheduler Service Notify Inactive Users");
        List<User> users = this.userAdminService.getInactiveUsers();
        for (User user : users) {
        	boolean isNotifyUser = true;
        	List<UserAccess> userAccessList = user.getUserAccessList();
        	if(userAccessList != null && userAccessList.size() > 0) {
        		for(UserAccess userAccess : userAccessList) {
            		if(userAccess.isAccessOverriden()) {
            			isNotifyUser = false;
            		}
            	}
            	if (isNotifyUser) {
            		this.userAdminService.notifyInactiveUsers(user);
            	}
        	}
        }
        logger.info("Ending Scheduler Service Notify Inactive Users");
    }

    private boolean isSchedulerEnabled() {
        return (new Boolean(this.isSchedulerEnabled)).booleanValue();
    }
}