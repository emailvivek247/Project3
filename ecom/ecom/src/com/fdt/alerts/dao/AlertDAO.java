package com.fdt.alerts.dao;

import java.util.List;

import com.fdt.alerts.dto.AlertNameHyperLinkKeyMap;
import com.fdt.alerts.entity.UserAlert;
import com.fdt.ecom.entity.NodeConfiguration;
import com.fdt.email.dto.EMailDTO;
import com.fdt.security.exception.DuplicateAlertException;
import com.fdt.security.exception.UserNameNotFoundException;

public interface AlertDAO {

    public List<UserAlert> getUserAlerts(String nodeName, int firstResult);

    public void sendEmail(EMailDTO emailDTO, List<AlertNameHyperLinkKeyMap> alertNameHyperLinkKeyMap);

    public NodeConfiguration getNodeConfiguration(String nodeName);

    public List<UserAlert> getUserAlertsByUserName(String userName, String nodeName);

    public void saveUserAlert(UserAlert userAlert) throws UserNameNotFoundException, DuplicateAlertException;

    public void deleteUserAlerts(String userName, List<Long> userAlertIdList);

    public boolean isAlertExist(String userName, String alertName);

    public int saveUserAlert(String userName, UserAlert userAlert);

}
