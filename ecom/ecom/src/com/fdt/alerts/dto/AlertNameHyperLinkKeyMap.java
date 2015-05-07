package com.fdt.alerts.dto;

import com.fdt.alerts.entity.UserAlert;

public class AlertNameHyperLinkKeyMap {

    private String key = null;

    private UserAlert value = null;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public UserAlert getValue() {
        return value;
    }

    public void setValue(UserAlert value) {
        this.value = value;
    }

}
