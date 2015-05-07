package com.fdt.alerts.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fdt.common.entity.AbstractBaseEntity;

@Entity
@Table(name = "AUTH_USERS_ALERTS")
public class UserAlert extends AbstractBaseEntity {

    private static final long serialVersionUID = -8016201414899925492L;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "NODE_NAME", nullable = false)
    private String nodeName;

    @Column(name = "ALERT_NAME", nullable = false)
    private String alertName;

    @Column(name = "BASE_URL", nullable = false)
    private String baseURL;

    @Column(name = "INDEX_NAME", nullable = false)
    private String indexName;

    @Column(name = "TEMPLATE_NAME")
    private String templateName;

    @Column(name = "ALERT_QUERY", nullable = false)
    private String alertQuery;

    @Column(name = "SITE_NAME")
    private String siteName;

    @Column(name = "COMMENTS")
    private String comments;

    public Long getUserId() {
        return userId;
    }

    @Transient
    private String username;

    @Transient
    private String firstName;

    @Transient
    private String lastName;

    @Transient
    private String advancedQuery = null;

    @Transient
    private String basicQuery = null;

    @Transient
    private boolean isDisplayable = false;

    @Transient
    private String alertQueryToBeDisplayed = null;

    @Transient
    private String emailLink = null;

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getAlertName() {
        return alertName;
    }

    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getAlertQuery() {
        return alertQuery;
    }

    public void setAlertQuery(String alertQuery) {
        this.alertQuery = alertQuery;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAdvancedQuery() {
        return advancedQuery;
    }

    public void setAdvancedQuery(String advancedQuery) {
        this.advancedQuery = advancedQuery;
    }

    public String getBasicQuery() {
        return basicQuery;
    }

    public void setBasicQuery(String basicQuery) {
        this.basicQuery = basicQuery;
    }

    public boolean isDisplayable() {
        return isDisplayable;
    }

    public void setDisplayable(boolean isDisplayable) {
        this.isDisplayable = isDisplayable;
    }

    public String getEmailLink() {
        return emailLink;
    }

    public void setEmailLink(String emailLink) {
        this.emailLink = emailLink;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getAlertQueryToBeDisplayed() {
        return alertQueryToBeDisplayed;
    }

    public void setAlertQueryToBeDisplayed(String alertQueryToBeDisplayed) {
        this.alertQueryToBeDisplayed = alertQueryToBeDisplayed;
    }

}
