package com.fdt.alerts.dto;

import java.util.LinkedList;
import java.util.List;

public class UserAlertDTO {

    String userName = null;

    List<Long> userAlertIdList = new LinkedList<Long>();

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<Long> getUserAlertIdList() {
        return userAlertIdList;
    }

    public void setUserAlertIdList(List<Long> userAlertIdList) {
        this.userAlertIdList = userAlertIdList;
    }

}
