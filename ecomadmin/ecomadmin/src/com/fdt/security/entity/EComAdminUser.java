package com.fdt.security.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fdt.common.entity.EcomAdminAbstractBaseEntity;

@Entity
@Table(name = "AUTH_ADMIN_USERS")
public class EComAdminUser extends EcomAdminAbstractBaseEntity implements UserDetails {
	
	private static final long serialVersionUID = -7803334038517903012L;

	@Column(name = "EMAIL_ID", unique = true, nullable = false)
	private String username;

	@Transient
	private String existingPassword;

	@Column(name = "PASSWORD", nullable = false)
	private String password;

	@Column(name = "FIRST_NAME")
	private String firstName;

	@Column(name = "LAST_NAME")
	private String lastName;
	
	@Column(name = "ADDRESS_1")
	private String addressLine1;

	@Column(name = "ADDRESS_2")
	private String addressLine2;

	@Column(name = "CITY")
	private String city;

	@Column(name = "STATE")
	private String state;

	@Column(name = "ZIP")
	private String zip;

	@Column(name = "PHONE")
	private String phone;
	
	@Column(name = "CREATED_IP")
	private String createdIp;

	@Column(name = "ACCOUNT_NONEXPIRED", nullable = false)
	@Type(type="yes_no")
	boolean accountNonExpired = true;

	@Column(name = "ACCOUNT_NONLOCKED", nullable = false)
	@Type(type="yes_no")
	boolean accountNonLocked = true;
	
	@Column(name = "CREDENIALS_NONEXPIRED", nullable = false)
	@Type(type="yes_no")
	boolean credentialsNonExpired = true;

	@Column(name = "LAST_LOGIN_TIME")
	private Date lastLoginTime = new Date();

	@Transient
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(name = "AUTH_ADMIN_USERS_ACCESS", joinColumns = {@JoinColumn(name="USER_ID")},	inverseJoinColumns={@JoinColumn(name="ACCESS_ID")})
	private List<EComAdminAccess> access = new LinkedList<EComAdminAccess>();

	@Transient
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(name = "AUTH_ADMIN_USERS_SITES", joinColumns = {@JoinColumn(name="USER_ID")},	inverseJoinColumns={@JoinColumn(name="SITE_ID")})
	private List<EComAdminSite> sites = new LinkedList<EComAdminSite>();

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = (firstName == null ? null : firstName.toUpperCase());
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = (lastName == null ? null : lastName.toUpperCase());
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = (addressLine1 == null ? null : addressLine1.toUpperCase());
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = (addressLine2 == null ? null : addressLine2.toUpperCase());
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = (city == null ? null : city.toUpperCase());
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = (state == null ? null : state.toUpperCase());
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCreatedIp() {
		return createdIp;
	}

	public void setCreatedIp(String createdIp) {
		this.createdIp = createdIp;
	}

	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	public List<EComAdminAccess> getAccess() {
		return access;
	}

	public void setAccess(EComAdminAccess anAccess) {
		if (this.access == null) {
			this.access =  new LinkedList<EComAdminAccess>();
		}
		this.access.add(anAccess);
	}
	
	public void setAccess(List<EComAdminAccess> access) {
		this.access = access;
	}

	public String getExistingPassword() {
		return existingPassword;
	}

	public void setExistingPassword(String existingPassword) {
		this.existingPassword = existingPassword;
	}

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	@Override
	public boolean isEnabled() {
		return isActive();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		for(EComAdminAccess access : this.getAccess()) {
			GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(access.getCode());
			authorities.add(grantedAuthority);
		}
		return authorities;
	}
	
	public List<EComAdminSite> getSites() {
		return sites;
	}

	public void setSites(List<EComAdminSite> sites) {
		this.sites = sites;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EComAdminUser other = (EComAdminUser) obj;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	public String toString() {
		return "EComAdminUser [username=" + username + ", existingPassword="
				+ existingPassword + ", password=" + password + ", firstName="
				+ firstName + ", lastName=" + lastName + ", addressLine1="
				+ addressLine1 + ", addressLine2=" + addressLine2 + ", city="
				+ city + ", state=" + state + ", zip=" + zip + ", phone="
				+ phone + ", createdIp=" + createdIp + ", accountNonExpired="
				+ accountNonExpired + ", accountNonLocked=" + accountNonLocked
				+ ", credentialsNonExpired=" + credentialsNonExpired
				+ ", lastLoginTime=" + lastLoginTime + ", access=" + access
				+ ", sites=" + sites + ", id=" + id + ", createdDate="
				+ createdDate + ", modifiedDate=" + modifiedDate
				+ ", modifiedBy=" + modifiedBy + ", createdBy=" + createdBy
				+ ", isActive=" + active + "]";
	}
	
}