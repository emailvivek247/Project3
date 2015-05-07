package com.fdt.security.ui.form;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.ScriptAssert;


@ScriptAssert(
        lang = "javascript",
        script = "_this.password.equals(_this.confirmPassword)",
        message = "security.notmatch.password"
        )
public class UserRegistrationForm extends UserForm {

    @NotNull
    @Size(min = 8, max = 20, message = "{security.size.password}")
    private String password = null;

    @NotNull
    @Size(min = 8, max = 20, message = "{security.size.password}")
    private String confirmPassword = null;

    @NotNull
    @Size(min = 1, max = 50, message="{security.size.firstName}")
    private String firstName;

    @NotNull
    @Size(min = 1, max = 50, message="{security.size.lastName}")
    private String lastName;

    private String captcha;

    @AssertTrue(message = "{security.assertTrue.termsAccept}")
    private boolean termsAccept = false;

    @NotNull
    private String siteId;

    private String recaptcha_challenge_field;

	@NotNull
	@Size(min = 1, max = 50, message="{security.size.recaptchaResponse}")
	private String recaptcha_response_field;

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

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public boolean isTermsAccept() {
        return termsAccept;
    }

    public void setTermsAccept(boolean termsAccept) {
        this.termsAccept = termsAccept;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

	public String getRecaptcha_challenge_field() {
		return recaptcha_challenge_field;
	}

	public void setRecaptcha_challenge_field(String recaptcha_challenge_field) {
		this.recaptcha_challenge_field = recaptcha_challenge_field;
	}

	public String getRecaptcha_response_field() {
		return recaptcha_response_field;
	}

	public void setRecaptcha_response_field(String recaptcha_response_field) {
		this.recaptcha_response_field = recaptcha_response_field;
	}

}
