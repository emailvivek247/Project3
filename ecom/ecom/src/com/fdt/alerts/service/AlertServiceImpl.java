package com.fdt.alerts.service;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fdt.alerts.dao.AlertDAO;
import com.fdt.alerts.dto.AlertNameHyperLinkKeyMap;
import com.fdt.alerts.entity.UserAlert;
import com.fdt.ecom.dao.EComDAO;
import com.fdt.ecom.entity.NodeConfiguration;
import com.fdt.email.EmailProducer;
import com.fdt.email.dto.EMailDTO;
import com.fdt.security.exception.DuplicateAlertException;
import com.fdt.security.exception.MaximumNumberOfAlertsReachedException;

@Service("alertService")
public class AlertServiceImpl implements AlertService {

    @Autowired
    private AlertDAO alertDAO;

    @Autowired
    private EComDAO eComDAO;

    @Autowired
    private EmailProducer emailProducer;

    public static int MAXIMUM_ALERTS = 50;

    @Transactional(readOnly = true)
    public List<UserAlert> getUserAlerts(String nodeName, int firstResult) {
        Assert.hasLength(nodeName, "nodeName Cannot Be Null/Empty");
        Assert.notNull(firstResult, "firstResult Cannot Be Null");
        return this.alertDAO.getUserAlerts(nodeName, firstResult);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void sendEmail(EMailDTO emailDTO, List<AlertNameHyperLinkKeyMap> alertNameHyperLinkKeyMap) {
        this.emailProducer.sendEmail(emailDTO, alertNameHyperLinkKeyMap);
    }

    @Transactional(readOnly = true)
    public NodeConfiguration getNodeConfiguration(String nodeName) {
        return this.eComDAO.getNodeConfiguration(nodeName);
    }

    @Transactional(readOnly = true)
    public List<UserAlert> getUserAlertsByUserName(String userName, String nodeName) {
        Assert.hasLength(userName, "User Name Cannot Be Null/Empty");
        Assert.hasLength(nodeName, "Node Name Cannot Be Null/Empty");
        String alertQuery = null;
        List<UserAlert> userAlertList = new LinkedList<UserAlert>();
        userAlertList = this.alertDAO.getUserAlertsByUserName(userName, nodeName);
        if(userAlertList == null || userAlertList.size() == 0) {
            return new LinkedList<UserAlert>();
        }
        if(!userAlertList.isEmpty()) {
            for(UserAlert userAlert : userAlertList) {
                alertQuery = userAlert.getAlertQuery();
                if(alertQuery.startsWith("lq=")) {
                    alertQuery = userAlert.getAlertQuery().replace("lq=", "");
                    alertQuery = alertQuery.replace("#", "");
                    alertQuery = alertQuery.replace("~5", "");
                    alertQuery = alertQuery.replace(":", "=");
                    alertQuery = alertQuery.replace("\"", "");
                } else {
                    alertQuery = userAlert.getAlertQuery().replace("q=", "");
                }
                userAlert.setAlertQuery(alertQuery);
            }
        }
        return userAlertList;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class)
    public void saveUserAlert(UserAlert userAlert) throws UsernameNotFoundException, DuplicateAlertException,
            MaximumNumberOfAlertsReachedException {
        Assert.notNull(userAlert.getAlertQuery(), "Alert Query Cannot Be Null");
        Assert.notNull(userAlert.getUsername(),   "User Name Cannot Be Null");
        Assert.notNull(userAlert.getAlertName(),  "Alert Name Cannot Be Null");
        boolean isAlertExist = false;
        List<UserAlert> userAlertList = this.getUserAlertsByUserName(userAlert.getUsername(), userAlert.getNodeName());
        int numberOfAlerts = 0;
        if(userAlertList != null) {
            numberOfAlerts = userAlertList.size();
        }
        if(numberOfAlerts > MAXIMUM_ALERTS) {
            throw new MaximumNumberOfAlertsReachedException("User Can Have Only Fifty Alerts.");
        }
        isAlertExist = this.alertDAO.isAlertExist(userAlert.getUsername(), userAlert.getAlertName());
        if (isAlertExist) {
            throw new DuplicateAlertException("An Alert With The Name Already Exists. Please Create The Alert " +
                    "With A New Name. ");
        }
        int count = this.alertDAO.saveUserAlert(userAlert.getUsername(), userAlert);
        if (count == 0) {
            throw new UsernameNotFoundException("User Not Found");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor= Throwable.class, readOnly = false)
    public void deleteUserAlerts(String userName, List<Long> userAlertIdList) {
        Assert.notNull(userAlertIdList, "userAlertIdList Cannot Be Null");
        this.alertDAO.deleteUserAlerts(userName, userAlertIdList);
    }

}
