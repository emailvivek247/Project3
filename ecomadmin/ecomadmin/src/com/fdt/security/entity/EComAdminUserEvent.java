package com.fdt.security.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fdt.common.entity.EcomAdminAbstractBaseEntity;

@Entity
@Table(name="AUTH_ADMIN_USERS_EVENTS")
public class EComAdminUserEvent extends EcomAdminAbstractBaseEntity {

    private static final long serialVersionUID = -6554340545783679787L;

    public EComAdminUserEvent() {
    }

    public EComAdminUserEvent(Long id, String token, String controllerURL, String clientName,
            Long userId, String firstName, String lastName, String userName) {
        super.setId(id);
        this.token = token;
        this.controllerURL = controllerURL;
        this.user = new EComAdminUser();
        this.user.setId(userId);
        this.user.setUsername(userName);
        this.user.setFirstName(firstName);
        this.user.setLastName(lastName);
    }

    @Column(name = "REQUEST_TOKEN", nullable = false)
    private String token = UUID.randomUUID().toString();

    @OneToOne
    private EComAdminUser user = new EComAdminUser();

    @Transient
    private String controllerURL = null;

    @Transient
    private String fromEMailAddress = null;

    @Transient
    private String emailTemplateFile = null;

    @Transient
    private String subject = null;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getControllerURL() {
        return controllerURL;
    }

    public void setControllerURL(String controllerURL) {
        this.controllerURL = controllerURL;
    }

    public EComAdminUser getUser() {
        return user;
    }

    public void setUser(EComAdminUser user) {
        this.user = user;
    }

    public String getEmailTemplateFile() {
        return emailTemplateFile;
    }

    public void setEmailTemplateFile(String emailTemplateFile) {
        this.emailTemplateFile = emailTemplateFile;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFromEMailAddress() {
        return fromEMailAddress;
    }

    public void setFromEMailAddress(String fromEMailAddress) {
        this.fromEMailAddress = fromEMailAddress;
    }
}
