package com.fdt.security.ui.form;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;

public class UserForm   {

    @NotNull(message = "{security.notnull.username}")
    @Size(min = 7, max = 50, message = "{security.size.username}")
    @Email(message="{security.invalidEmail.username}")
    private String username = null;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}