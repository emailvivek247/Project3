package com.fdt.ecomadmin.ui.form.administration;

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Email;

public class SiteConfigurationForm {

    @NotNull(message = "{administration.notnull.siteId}")
    private Long siteId = null;

    @NotNull(message = "{administration.notnull.fromemailaddress}")
    @Email(message="{administration.invalid.fromemailaddress}")
    private String fromEmailAddress = null;

    @NotNull(message = "{administration.notnull.paymentConfirmationSubject}")
    private String paymentConfirmationSubject = null;

    @NotNull(message = "{administration.notnull.changeSubscriptionSubject}")
    private String changeSubscriptionSubject = null;

    @NotNull(message = "{administration.notnull.cancelSubscriptionSubject}")
    private String cancelSubscriptionSubject = null;

    @NotNull(message = "{administration.notnull.reactivateSubscriptionSubject}")
    private String reactivateSubscriptionSubject = null;

    @NotNull(message = "{administration.notnull.recurringPaymentSuccessSubject}")
    private String recurringPaymentSuccessSubject = null;

    @NotNull(message = "{administration.notnull.recurringPaymentUnsuccessfulSubject}")
    private String recurringPaymentUnsuccessfulSubject = null;

    @NotNull(message = "{administration.notnull.webPaymentConfirmationSubject}")
    private String webPaymentConfirmationSubject = null;

    @NotNull(message = "{administration.notnull.payAsUGoPaymentConfirmationSubject}")
    private String payAsUGoPaymentConfirmationSubject = null;

    @NotNull(message = "{administration.notnull.removeSubscriptionSubject}")
    private String removeSubscriptionSubject = null;

    @NotNull(message = "{administration.notnull.accessAuthorizationSubject}")
    private String accessAuthorizationSubject = null;
    
    @NotNull(message = "{administration.notnull.expiredOverriddenSubscriptionNotificationSubject}")
    private String expiredOverriddenSubscriptionNotificationSubject = null;

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
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

    public void setReactivateSubscriptionSubject(
            String reactivateSubscriptionSubject) {
        this.reactivateSubscriptionSubject = reactivateSubscriptionSubject;
    }

    public String getRecurringPaymentSuccessSubject() {
        return recurringPaymentSuccessSubject;
    }

    public void setRecurringPaymentSuccessSubject(
            String recurringPaymentSuccessSubject) {
        this.recurringPaymentSuccessSubject = recurringPaymentSuccessSubject;
    }

    public String getRecurringPaymentUnsuccessfulSubject() {
        return recurringPaymentUnsuccessfulSubject;
    }

    public void setRecurringPaymentUnsuccessfulSubject(
            String recurringPaymentUnsuccessfulSubject) {
        this.recurringPaymentUnsuccessfulSubject = recurringPaymentUnsuccessfulSubject;
    }

    public String getWebPaymentConfirmationSubject() {
        return webPaymentConfirmationSubject;
    }

    public void setWebPaymentConfirmationSubject(
            String webPaymentConfirmationSubject) {
        this.webPaymentConfirmationSubject = webPaymentConfirmationSubject;
    }

    public String getRemoveSubscriptionSubject() {
        return removeSubscriptionSubject;
    }

    public void setRemoveSubscriptionSubject(String removeSubscriptionSubject) {
        this.removeSubscriptionSubject = removeSubscriptionSubject;
    }

    public String getAccessAuthorizationSubject() {
        return accessAuthorizationSubject;
    }

    public void setAccessAuthorizationSubject(String accessAuthorizationSubject) {
        this.accessAuthorizationSubject = accessAuthorizationSubject;
    }

	public String getPayAsUGoPaymentConfirmationSubject() {
		return payAsUGoPaymentConfirmationSubject;
	}

	public void setPayAsUGoPaymentConfirmationSubject(
			String payAsUGoPaymentConfirmationSubject) {
		this.payAsUGoPaymentConfirmationSubject = payAsUGoPaymentConfirmationSubject;
	}

	public String getExpiredOverriddenSubscriptionNotificationSubject() {
		return expiredOverriddenSubscriptionNotificationSubject;
	}

	public void setExpiredOverriddenSubscriptionNotificationSubject(
			String expiredOverriddenSubscriptionNotificationSubject) {
		this.expiredOverriddenSubscriptionNotificationSubject = expiredOverriddenSubscriptionNotificationSubject;
	}

	
}
