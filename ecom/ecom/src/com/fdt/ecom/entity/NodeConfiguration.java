package com.fdt.ecom.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fdt.common.entity.AbstractBaseEntity;

@Entity
@Table(name = "NODE_CONFIGURATION")
public class NodeConfiguration extends AbstractBaseEntity {

    private static final long serialVersionUID = -3348627430433740823L;

    @Column(name = "NODE_ID", nullable = false)
    private Long nodeId = null;

    @Transient
    private String nodeName = null;

    @Transient
    private String nodeDescription = null;

    @Column(name = "FROM_EMAIL_ADDRESS", nullable = false)
    private String fromEmailAddress = null;

    @Column(name = "RESET_PASSWORD_SUB", nullable = false)
    private String resetPasswordSubject = null;

    @Column(name = "USER_ACTIVATION_SUB", nullable = false)
    private String userActivationSubject = null;

    @Column(name = "UNLOCK_USER_SUB", nullable = false)
    private String unlockUserSub = null;

    @Column(name = "LOCK_USER_SUB", nullable = false)
    private String lockUserSub = null;

    @Column(name = "EMAIL_TEMPLATE_FOLDER", nullable = false)
    private String emailTemplateFolder = null;

    @Column(name = "RESET_PASSWORD_TEMPLATE", nullable = false)
    private String resetPasswordEmailTemplate = null;

    @Column(name = "USER_ACTIVATION_TEMPLATE", nullable = false)
    private String userActivationEmailTemplate = null;

    @Column(name = "LOCK_USER_TEMPLATE", nullable = false)
    private String lockUserEmailTemplate = null;

    @Column(name = "UNLOCK_USER_TEMPLATE", nullable = false)
    private String unlockUserEmailTemplate = null;

    @Column(name = "ALERT_SUBJECT")
    private String alertSubject = null;

    @Column(name = "ALERT_TEMPLATE")
    private String alertTemplate = null;

    @Column(name = "INACTIVE_USER_NOTIF_SUB")
    private String inActiveUserNotifSubject = null;

    @Column(name = "INACTIVE_USER_NOTIF_TEMPLATE")
    private String inActiveUserNotifTemplate = null;
    
    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public String getFromEmailAddress() {
        return fromEmailAddress;
    }

    public void setFromEmailAddress(String fromEmailAddress) {
        this.fromEmailAddress = fromEmailAddress;
    }

    public String getResetPasswordSubject() {
        return resetPasswordSubject;
    }

    public void setResetPasswordSubject(String resetPasswordSubject) {
        this.resetPasswordSubject = resetPasswordSubject;
    }

    public String getUserActivationSubject() {
        return userActivationSubject;
    }

    public void setUserActivationSubject(String userActivationSubject) {
        this.userActivationSubject = userActivationSubject;
    }

    public String getUnlockUserSub() {
        return unlockUserSub;
    }

    public void setUnlockUserSub(String unlockUserSub) {
        this.unlockUserSub = unlockUserSub;
    }

    public String getLockUserSub() {
        return lockUserSub;
    }

    public void setLockUserSub(String lockUserSub) {
        this.lockUserSub = lockUserSub;
    }

    public String getEmailTemplateFolder() {
        return emailTemplateFolder;
    }

    public void setEmailTemplateFolder(String emailTemplateFolder) {
        this.emailTemplateFolder = emailTemplateFolder;
    }

    public String getResetPasswordEmailTemplate() {
        return resetPasswordEmailTemplate;
    }

    public void setResetPasswordEmailTemplate(String resetPasswordEmailTemplate) {
        this.resetPasswordEmailTemplate = resetPasswordEmailTemplate;
    }

    public String getUserActivationEmailTemplate() {
        return userActivationEmailTemplate;
    }

    public void setUserActivationEmailTemplate(String userActivationEmailTemplate) {
        this.userActivationEmailTemplate = userActivationEmailTemplate;
    }

    public String getLockUserEmailTemplate() {
        return lockUserEmailTemplate;
    }

    public void setLockUserEmailTemplate(String lockUserEmailTemplate) {
        this.lockUserEmailTemplate = lockUserEmailTemplate;
    }

    public String getUnlockUserEmailTemplate() {
        return unlockUserEmailTemplate;
    }

    public void setUnlockUserEmailTemplate(String unlockUserEmailTemplate) {
        this.unlockUserEmailTemplate = unlockUserEmailTemplate;
    }

    public String getAlertSubject() {
        return alertSubject;
    }

    public void setAlertSubject(String alertSubject) {
        this.alertSubject = alertSubject;
    }

    public String getAlertTemplate() {
        return alertTemplate;
    }

    public void setAlertTemplate(String alertTemplate) {
        this.alertTemplate = alertTemplate;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeDescription() {
        return nodeDescription;
    }

    public void setNodeDescription(String nodeDescription) {
        this.nodeDescription = nodeDescription;
    }
    
    public String getInActiveUserNotifSubject() {
		return inActiveUserNotifSubject;
	}

	public void setInActiveUserNotifSubject(String inActiveUserNotifSub) {
		this.inActiveUserNotifSubject = inActiveUserNotifSub;
	}

	public String getInActiveUserNotifTemplate() {
		return inActiveUserNotifTemplate;
	}

	public void setInActiveUserNotifTemplate(String inActiveUserNotifTemplate) {
		this.inActiveUserNotifTemplate = inActiveUserNotifTemplate;
	}

	@Override
	public String toString() {
		return "NodeConfiguration [nodeId=" + nodeId + ", nodeName=" + nodeName
				+ ", nodeDescription=" + nodeDescription
				+ ", fromEmailAddress=" + fromEmailAddress
				+ ", resetPasswordSubject=" + resetPasswordSubject
				+ ", userActivationSubject=" + userActivationSubject
				+ ", unlockUserSub=" + unlockUserSub + ", lockUserSub="
				+ lockUserSub + ", emailTemplateFolder=" + emailTemplateFolder
				+ ", resetPasswordEmailTemplate=" + resetPasswordEmailTemplate
				+ ", userActivationEmailTemplate="
				+ userActivationEmailTemplate + ", lockUserEmailTemplate="
				+ lockUserEmailTemplate + ", unlockUserEmailTemplate="
				+ unlockUserEmailTemplate + ", alertSubject=" + alertSubject
				+ ", alertTemplate=" + alertTemplate
				+ ", inActiveUserNotifSubject=" + inActiveUserNotifSubject
				+ ", inActiveUserNotifTemplate=" + inActiveUserNotifTemplate
				+ ", id=" + id + ", createdDate=" + createdDate
				+ ", modifiedDate=" + modifiedDate + ", modifiedBy="
				+ modifiedBy + ", createdBy=" + createdBy + ", active="
				+ active + "]";
	}
}
