package com.fdt.ecom.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fdt.common.entity.AbstractBaseEntity;

@Entity
@Table(name = "SITE_CONFIGURATION")
public class SiteConfiguration extends AbstractBaseEntity {

    private static final long serialVersionUID = -3810507506871620564L;

    @Column(name = "SITE_ID", nullable = false)
    private Long siteId = null;

    @Column(name = "EMAIL_TEMPLATE_FOLDER", nullable = false)
    private String emailTemplateFolder = null;

    @Column(name = "FROM_EMAIL_ADDRESS", nullable = false)
    private String fromEmailAddress = null;

    @Column(name = "PAYMENT_CONF_SUB", nullable = false)
    private String paymentConfirmationSubject = null;

    @Column(name = "CHANGE_SUBSCRIPTION_SUB", nullable = false)
    private String changeSubscriptionSubject = null;

    @Column(name = "CANCEL_SUBSCRIPTION_SUB", nullable = false)
    private String cancelSubscriptionSubject = null;

    @Column(name = "REACTIVATE_SUBSCRIPTION_SUB", nullable = false)
    private String reactivateSubscriptionSubject = null;

    @Column(name = "PAYMENT_CONF_TEMPLATE", nullable = false)
    private String paymentConfirmationTemplate = null;

    @Column(name = "CHANGE_SUBSCRIPTION_TEMPLATE", nullable = false)
    private String changeSubscriptionTemplate = null;

    @Column(name = "CANCEL_SUBSCRIPTION_TEMPLATE", nullable = false)
    private String cancelSubscriptionTemplate = null;

    @Column(name = "REACTIVATE_CANCELLLED_SUBSCRIPTION_TEMPLATE", nullable = false)
    private String reactivateCancelledSubscriptionTemplate = null;

    @Column(name = "RECURRING_PAYMENT_SUCCESS_TEMPLATE", nullable = false)
    private String recurringPaymentSuccessTemplate = null;

    @Column(name = "RECURRING_PAYMENT_UNSUCCESSFUL_TEMPLATE", nullable = false)
    private String recurringPaymentUnsuccessfulTemplate = null;

    @Column(name = "RECURRING_PAYMENT_SUCCESS_SUB", nullable = false)
    private String recurringPaymentSuccessSubject = null;

    @Column(name = "RECURRING_PAYMENT_UNSUCCESSFUL_SUB", nullable = false)
    private String recurringPaymentUnsuccessfulSubject = null;

    @Column(name = "WEB_PAYMENT_CONFIRMATION_SUB")
    private String webPaymentConfSubject = null;

    @Column(name = "WEB_PAYMENT_CONFIRMATION_TEMPLATE")
    private String webPaymentConfTemplate = null;

    @Column(name = "PAYASUGO_PAYMENT_CONFIRMATION_SUB")
    private String payAsUGoPaymentConfSubject = null;

    @Column(name = "PAYASUGO_PAYMENT_CONFIRMATION_TEMPLATE")
    private String payAsUGoPaymentConfTemplate = null;

    @Column(name = "REMOVE_SUBSCRIPTION_SUB")
    private String removeSubscriptionSubject = null;

    @Column(name = "REMOVE_SUBSCRIPTION_TEMPLATE")
    private String removeSubscriptionTemplate = null;

    @Column(name = "ACCESS_AUTHORIZATION_SUB")
    private String accessAuthorizationSubject = null;

    @Column(name = "ACCESS_AUTHORIZATION_TEMPLATE")
    private String accessAuthorizationTemplate = null;

    @Column(name = "ADD_SUBSCRIPTION_SUB", nullable = false)
    private String addSubscriptionSub = null;
    
    @Column(name = "EXPIRED_OVERRIDDEN_SUBSCRIPTION_NOTIFICATION_SUB")
    private String expiredOverriddenSubscriptionNotificationSubject = null;

    @Column(name = "EXPIRED_OVERRIDDEN_SUBSCRIPTION_NOTIFICATION_TEMPLATE", nullable = false)
    private String expiredOverriddenSubscriptionNotificationTemplate = null;
    
    public String getExpiredOverriddenSubscriptionNotificationSubject() {
		return expiredOverriddenSubscriptionNotificationSubject;
	}

	public void setExpiredOverriddenSubscriptionNotificationSubject(
			String expiredOverriddenSubscriptionNotificationSubject) {
		this.expiredOverriddenSubscriptionNotificationSubject = expiredOverriddenSubscriptionNotificationSubject;
	}

	public String getExpiredOverriddenSubscriptionNotificationTemplate() {
		return expiredOverriddenSubscriptionNotificationTemplate;
	}

	public void setExpiredOverriddenSubscriptionNotificationTemplate(
			String expiredOverriddenSubscriptionNotificationTemplate) {
		this.expiredOverriddenSubscriptionNotificationTemplate = expiredOverriddenSubscriptionNotificationTemplate;
	}

	@Transient
    private String siteName = null;

    public String getEmailTemplateFolder() {
        return emailTemplateFolder;
    }

    public void setEmailTemplateFolder(String emailTemplateFolder) {
        this.emailTemplateFolder= emailTemplateFolder;
    }

    public String getFromEmailAddress() {
        return fromEmailAddress;
    }

    public void setFromEmailAddress(String fromEmailAddress) {
        this.fromEmailAddress = fromEmailAddress;
    }

    public String getPaymentConfirmationSubject() {
        return paymentConfirmationSubject;
    }

    public void setPaymentConfirmationSubject(String paymentConfirmationSubject) {
        this.paymentConfirmationSubject = paymentConfirmationSubject;
    }

    public String getChangeSubscriptionSubject() {
        return changeSubscriptionSubject;
    }

    public void setChangeSubscriptionSubject(String changeSubscriptionSubject) {
        this.changeSubscriptionSubject = changeSubscriptionSubject;
    }

    public String getCancelSubscriptionSubject() {
        return cancelSubscriptionSubject;
    }

    public void setCancelSubscriptionSubject(String cancelSubscriptionSubject) {
        this.cancelSubscriptionSubject = cancelSubscriptionSubject;
    }

    public String getReactivateSubscriptionSubject() {
        return reactivateSubscriptionSubject;
    }

    public void setReactivateSubscriptionSubject(String reactivateSubscriptionSubject) {
        this.reactivateSubscriptionSubject = reactivateSubscriptionSubject;
    }

    public String getPaymentConfirmationTemplate() {
        return paymentConfirmationTemplate;
    }

    public void setPaymentConfirmationTemplate(String paymentConfirmationTemplate) {
        this.paymentConfirmationTemplate = paymentConfirmationTemplate;
    }

    public String getChangeSubscriptionTemplate() {
        return changeSubscriptionTemplate;
    }

    public void setChangeSubscriptionTemplate(String changeSubscriptionTemplate) {
        this.changeSubscriptionTemplate = changeSubscriptionTemplate;
    }

    public String getCancelSubscriptionTemplate() {
        return cancelSubscriptionTemplate;
    }

    public void setCancelSubscriptionTemplate(String cancelSubscriptionTemplate) {
        this.cancelSubscriptionTemplate = cancelSubscriptionTemplate;
    }

    public String getReactivateCancelledSubscriptionTemplate() {
        return reactivateCancelledSubscriptionTemplate;
    }

    public void setReactivateCancelledSubscriptionTemplate(String reactivateCancelledSubscriptionTemplate) {
        this.reactivateCancelledSubscriptionTemplate = reactivateCancelledSubscriptionTemplate;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public String getRecurringPaymentSuccessTemplate() {
        return recurringPaymentSuccessTemplate;
    }

    public void setRecurringPaymentSuccessTemplate(String recurringPaymentSuccessTemplate) {
        this.recurringPaymentSuccessTemplate = recurringPaymentSuccessTemplate;
    }

    public String getRecurringPaymentUnsuccessfulTemplate() {
        return recurringPaymentUnsuccessfulTemplate;
    }

    public void setRecurringPaymentUnsuccessfulTemplate(String recurringPaymentUnsuccessfulTemplate) {
        this.recurringPaymentUnsuccessfulTemplate = recurringPaymentUnsuccessfulTemplate;
    }

    public String getRecurringPaymentSuccessSubject() {
        return recurringPaymentSuccessSubject;
    }

    public void setRecurringPaymentSuccessSubject(String recurringPaymentSuccessSubject) {
        this.recurringPaymentSuccessSubject = recurringPaymentSuccessSubject;
    }

    public String getRecurringPaymentUnsuccessfulSubject() {
        return recurringPaymentUnsuccessfulSubject;
    }

    public void setRecurringPaymentUnsuccessfulSubject(String recurringPaymentUnsuccessfulSubject) {
        this.recurringPaymentUnsuccessfulSubject = recurringPaymentUnsuccessfulSubject;
    }

    public String getWebPaymentConfSubject() {
        return webPaymentConfSubject;
    }

    public void setWebPaymentConfSubject(String webPaymentConfSubject) {
        this.webPaymentConfSubject = webPaymentConfSubject;
    }

    public String getWebPaymentConfTemplate() {
        return webPaymentConfTemplate;
    }

    public void setWebPaymentConfTemplate(String webPaymentConfTemplate) {
        this.webPaymentConfTemplate = webPaymentConfTemplate;
    }

    public String getRemoveSubscriptionSubject() {
        return removeSubscriptionSubject;
    }

    public void setRemoveSubscriptionSubject(String removeSubscriptionSubject) {
        this.removeSubscriptionSubject = removeSubscriptionSubject;
    }

    public String getRemoveSubscriptionTemplate() {
        return removeSubscriptionTemplate;
    }

    public void setRemoveSubscriptionTemplate(String removeSubscriptionTemplate) {
        this.removeSubscriptionTemplate = removeSubscriptionTemplate;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getAccessAuthorizationSubject() {
        return accessAuthorizationSubject;
    }

    public void setAccessAuthorizationSubject(String accessAuthorizationSubject) {
        this.accessAuthorizationSubject = accessAuthorizationSubject;
    }

    public String getAccessAuthorizationTemplate() {
        return accessAuthorizationTemplate;
    }

    public void setAccessAuthorizationTemplate(String accessAuthorizationTemplate) {
        this.accessAuthorizationTemplate = accessAuthorizationTemplate;
    }

	public String getPayAsUGoPaymentConfSubject() {
		return payAsUGoPaymentConfSubject;
	}

	public void setPayAsUGoPaymentConfSubject(String payAsUGoPaymentConfSubject) {
		this.payAsUGoPaymentConfSubject = payAsUGoPaymentConfSubject;
	}

	public String getPayAsUGoPaymentConfTemplate() {
		return payAsUGoPaymentConfTemplate;
	}

	public void setPayAsUGoPaymentConfTemplate(String payAsUGoPaymentConfTemplate) {
		this.payAsUGoPaymentConfTemplate = payAsUGoPaymentConfTemplate;
	}
	
	public String getAddSubscriptionSub() {
		return addSubscriptionSub;
	}

	public void setAddSubscriptionSub(String addSubscriptionSub) {
		this.addSubscriptionSub = addSubscriptionSub;
	}

	@Override
	public String toString() {
		return "SiteConfiguration [siteId=" + siteId + ", emailTemplateFolder="
				+ emailTemplateFolder + ", fromEmailAddress="
				+ fromEmailAddress + ", paymentConfirmationSubject="
				+ paymentConfirmationSubject + ", changeSubscriptionSubject="
				+ changeSubscriptionSubject + ", cancelSubscriptionSubject="
				+ cancelSubscriptionSubject
				+ ", reactivateSubscriptionSubject="
				+ reactivateSubscriptionSubject
				+ ", paymentConfirmationTemplate="
				+ paymentConfirmationTemplate + ", changeSubscriptionTemplate="
				+ changeSubscriptionTemplate + ", cancelSubscriptionTemplate="
				+ cancelSubscriptionTemplate
				+ ", reactivateCancelledSubscriptionTemplate="
				+ reactivateCancelledSubscriptionTemplate
				+ ", recurringPaymentSuccessTemplate="
				+ recurringPaymentSuccessTemplate
				+ ", recurringPaymentUnsuccessfulTemplate="
				+ recurringPaymentUnsuccessfulTemplate
				+ ", recurringPaymentSuccessSubject="
				+ recurringPaymentSuccessSubject
				+ ", recurringPaymentUnsuccessfulSubject="
				+ recurringPaymentUnsuccessfulSubject
				+ ", webPaymentConfSubject=" + webPaymentConfSubject
				+ ", webPaymentConfTemplate=" + webPaymentConfTemplate
				+ ", payAsUGoPaymentConfSubject=" + payAsUGoPaymentConfSubject
				+ ", payAsUGoPaymentConfTemplate="
				+ payAsUGoPaymentConfTemplate + ", removeSubscriptionSubject="
				+ removeSubscriptionSubject + ", removeSubscriptionTemplate="
				+ removeSubscriptionTemplate + ", accessAuthorizationSubject="
				+ accessAuthorizationSubject + ", accessAuthorizationTemplate="
				+ accessAuthorizationTemplate + ", siteName=" + siteName 
				+ ",addSubscriptionSub=" + addSubscriptionSub 
				+ "]";
	}

}
