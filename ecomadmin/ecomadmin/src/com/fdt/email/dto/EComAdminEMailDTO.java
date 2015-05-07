package com.fdt.email.dto;

import java.io.Serializable;
import java.util.Map;

public class EComAdminEMailDTO implements Serializable {

    private static final long serialVersionUID = 1558157581299593076L;

    private String fromEmailId = null;

    private String toEmailId = null;

    private String subject = null;

    private String emailTemplateName = null;

    private String text = null;

    private Map<String, Object> mapData = null;

    public String getFromEmailId() {
        return fromEmailId;
    }

    public void setFromEmailId(String fromEmailId) {
        this.fromEmailId = fromEmailId;
    }

    public String getToEmailId() {
        return toEmailId;
    }

    public void setToEmailId(String toEmailId) {
        this.toEmailId = toEmailId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getEmailTemplateName() {
        return emailTemplateName;
    }

    public void setEmailTemplateName(String emailTemplateName) {
        this.emailTemplateName = emailTemplateName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, Object> getMapData() {
        return mapData;
    }

    public void setMapData(Map<String, Object> mapData) {
        this.mapData = mapData;
    }

    @Override
    public String toString() {
        return "EComAdminEMailDTO [fromEmailId=" + fromEmailId + ", toEmailId="
                + toEmailId + ", subject=" + subject + ", emailTemplateName="
                + emailTemplateName + ", text=" + text + ", mapData=" + mapData
                + "]";
    }
}
