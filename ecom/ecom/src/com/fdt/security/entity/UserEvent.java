package com.fdt.security.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fdt.common.entity.AbstractBaseEntity;

@Entity
@Table(name="AUTH_USERS_EVENTS")
public class UserEvent extends AbstractBaseEntity {

    private static final long serialVersionUID = 2803849016712737363L;

    public UserEvent() {
    }

    public UserEvent(Long id, String token,    Long userId, String firstName, String lastName, String userName) {
        super.setId(id);
        this.token = token;
        this.user = new User();
        this.user.setId(userId);
        this.user.setUsername(userName);
        this.user.setFirstName(firstName);
        this.user.setLastName(lastName);
    }

    @Column(name = "REQUEST_TOKEN", nullable = false)
    private String token = UUID.randomUUID().toString();

    @OneToOne
    private User user = new User();

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFromEMailAddress() {
        return fromEMailAddress;
    }

    public void setFromEMailAddress(String fromEMailAddress) {
        this.fromEMailAddress = fromEMailAddress;
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

    @Override
    public String toString() {
        return "UserEvent [token=" + token + ", user=" + user
                + ", controllerURL=" + controllerURL + ", fromEMailAddress="
                + fromEMailAddress + ", emailTemplateFile=" + emailTemplateFile
                + ", subject=" + subject + ", id=" + id + ", createdDate="
                + createdDate + ", modifiedDate=" + modifiedDate
                + ", modifiedBy=" + modifiedBy + ", createdBy=" + createdBy
                + ", active=" + active + "]";
    }

}
