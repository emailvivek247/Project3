package com.fdt.ecomadmin.ui.form.administration;

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Email;

public class NodeConfigurationForm {

    @NotNull(message = "{administration.notnull.nodename}")
    private String node = null;

    @NotNull(message = "{administration.notnull.fromemailaddress}")
    @Email(message="{administration.invalid.fromemailaddress}")
    private String fromEmailAddress = null;

    @NotNull(message = "{administration.notnull.resetPasswordSubject}")
    private String resetPasswordSubject = null;

    @NotNull(message = "{administration.notnull.userActivationSubject}")
    private String userActivationSubject = null;

    @NotNull(message = "{administration.notnull.lockUserSubject}")
    private String lockUserSubject = null;

    @NotNull(message = "{administration.notnull.unlockUserSubject}")
    private String unlockUserSubject = null;

    @NotNull(message = "{administration.notnull.alertSubject}")
    private String alertSubject = null;
    
    @NotNull(message = "{administration.notnull.inActiveUserNotifSubject}")
    private String inActiveUserNotifSubject = null;

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

    public String getLockUserSubject() {
        return lockUserSubject;
    }

    public void setLockUserSubject(String lockUserSubject) {
        this.lockUserSubject = lockUserSubject;
    }

    public String getUnlockUserSubject() {
        return unlockUserSubject;
    }

    public void setUnlockUserSubject(String unlockUserSubject) {
        this.unlockUserSubject = unlockUserSubject;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getAlertSubject() {
        return alertSubject;
    }

    public void setAlertSubject(String alertSubject) {
        this.alertSubject = alertSubject;
    }

	public String getInActiveUserNotifSubject() {
		return inActiveUserNotifSubject;
	}

	public void setInActiveUserNotifSubject(String inActiveUserNotifSubject) {
		this.inActiveUserNotifSubject = inActiveUserNotifSubject;
	}
}
