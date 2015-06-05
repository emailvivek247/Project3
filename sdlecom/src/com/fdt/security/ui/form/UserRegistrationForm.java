package com.fdt.security.ui.form;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.ScriptAssert;

@ScriptAssert.List({
	@ScriptAssert(lang="javascript",script="_this.username.equals(_this.confirmUsername)",message="security.notmatch.username"),
    @ScriptAssert(lang="javascript",script="_this.password.equals(_this.confirmPassword)",message="security.notmatch.password")
})

public class UserRegistrationForm extends UserForm {

	@NotNull(message = "{security.notnull.username}")
	@Size(min = 7, max = 50, message = "{security.size.username}")
	@Email(message="{security.invalidEmail.username}")
	private String confirmUsername = null;

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

	@NotNull
	@Size(min = 1, max = 3, message="{security.notnull.access}")
	private String accessId;

	@NotNull(message="{security.invalid.phoneNumber}")
	@Size(min = 13, max = 13, message="{security.invalid.phoneNumber}")
	@Pattern(regexp="([\\(]{1}[1-9]{1}[0-9]{2}[\\)]{1}[0-9]{3}[\\-]{1}[0-9]{4})", message="{security.invalid.phoneNumber}")	
	private String phoneNumber;


	private String recaptcha_challenge_field;

	@NotNull
	@Size(min = 1, max = 50, message="{security.size.recaptchaResponse}")
	private String recaptcha_response_field;
	
	
	@Size(max = 100, message = "{security.size.firmName}")
	private String firmName = null;

	@Size(max = 20, message = "{security.size.firmNumber}")
	private String firmNumber = null;

	@Size(max = 20, message = "{security.size.barNumber}")
	private String barNumber = null;

	public String getConfirmUsername() {
		return confirmUsername;
	}

	public void setConfirmUsername(String confirmUsername) {
		this.confirmUsername = confirmUsername;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
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

	public String getAccessId() {
		return accessId;
	}

	public void setAccessId(String accessId) {
		this.accessId = accessId;
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
	
    public String getFirmName() {
		return StringUtils.isBlank(firmName) ? null : firmName.trim() ;
	}

	public void setFirmName(String firmName) {
		this.firmName  = StringUtils.isBlank(firmName) ? null : firmName.trim() ;
	}

	public String getFirmNumber() {
		return StringUtils.isBlank(firmNumber) ? null : firmNumber.trim() ;
	}

	public void setFirmNumber(String firmNumber) {
		this.firmNumber  = StringUtils.isBlank(firmNumber) ? null : firmNumber.trim() ;
	}

	public String getBarNumber() {
		return StringUtils.isBlank(barNumber) ? null : barNumber.trim() ;
	}

	public void setBarNumber(String barNumber) {
		this.barNumber  = StringUtils.isBlank(barNumber) ? null : barNumber.trim() ;
	}


}
