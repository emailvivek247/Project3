package com.fdt.security.ui.form;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.ScriptAssert;

@ScriptAssert(
        lang = "javascript",
        script = "_this.password.equals(_this.confirmPassword)",
        message = "security.notmatch.password"
        )
public class ChangePasswordForm {

    @NotNull
    @Size(min = 8, max = 20, message = "{security.size.existingPassword}")
    private String existingPassword = null;

    @NotNull
    @Size(min = 8, max = 20, message = "{security.size.password}")
    private String password = null;


    @NotNull
    @Size(min = 8, max = 20, message = "{security.size.password}")
    private String confirmPassword = null;

    public String getExistingPassword() {
        return existingPassword;
    }

    public void setExistingPassword(String existingPassword) {
        this.existingPassword = existingPassword;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
