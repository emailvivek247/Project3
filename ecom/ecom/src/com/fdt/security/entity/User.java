package com.fdt.security.entity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fdt.common.entity.AbstractBaseEntity;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.Term;
import com.fdt.recurtx.entity.UserAccount;

@Entity
@Table(name = "AUTH_USERS")
public class User extends AbstractBaseEntity implements UserDetails {

    private static final long serialVersionUID = 851699782534338603L;

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

    @Column(name = "IS_EMAIL_NOTIFICATION_SENT", nullable = false)
    @Type(type="yes_no")
    private boolean isEmailNotificationSent = false;

    @Column(name = "ACCOUNT_NONLOCKED", nullable = false)
    @Type(type="yes_no")
    boolean accountNonLocked = true;

    @Column(name = "CREDENIALS_NONEXPIRED", nullable = false)
    @Type(type="yes_no")
    boolean credentialsNonExpired = true;

    @Column(name = "LAST_LOGIN_TIME")
    private Date lastLoginTime = new Date();

    @Column(name = "CURRENT_LOGIN_TIME", nullable = false)
    private Date currentLoginTime = new Date();

	@Column(name = "FIRM_NAME")
    private String firmName;

    @Column(name = "FIRM_NUMBER")
    private String firmNumber;

    @Column(name = "BAR_NUMBER")
    private String barNumber;

    @Column(name = "REGISTERED_NODE", nullable = false)
    private String registeredNode = null;

    @Transient
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name = "AUTH_USERS_ACCESS", joinColumns = {@JoinColumn(name="USER_ID")},    inverseJoinColumns={@JoinColumn(name="ACCESS_ID")})
    private List<Access> access = null;

    @Transient
    private List<Site> sites =  null;

    @Transient
    private CreditCard creditCard = null;

    @Transient
    private List<UserAccess> userAccessList = null;

    @Transient
    private boolean creditCardActive = false;

    @Transient
    private boolean payedUser = false;

    @Transient
    private boolean paymentDue = false;

    @Transient
    private boolean cardAvailable = false;

    @Transient
    private List<UserAccount> userAccount =  null;

    @Transient
    private boolean waitingForAuthorization = false;

    @Transient
    private boolean accessOverridden = false;

    @Transient
    private boolean acceptedTerms = false;

    @Transient
    private List<Term> terms = null;

    @Transient
    private Date accountDeletionDate = null;

    @Transient
    private boolean purchasedDocuments;

    @Transient
    private boolean isAuthorizationPending = false;


    public List<Term> getTerms() {
		return terms;
	}

	public void setTerms(List<Term> terms) {
		this.terms = terms;
	}

	public boolean isAcceptedTerms() {
		return acceptedTerms;
	}

	public void setAcceptedTerms(boolean acceptedTerms) {
		this.acceptedTerms = acceptedTerms;
	}

	public boolean isAccessOverridden() {
        return accessOverridden;
    }

    public void setAccessOverridden(boolean accessOverridden) {
        this.accessOverridden = accessOverridden;
    }

    public boolean isWaitingForAuthorization() {
        return waitingForAuthorization;
    }

    public void setWaitingForAuthorization(boolean waitingForAuthorization) {
        this.waitingForAuthorization = waitingForAuthorization;
    }

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



    public String getFirmName() {
		return firmName;
	}

	public void setFirmName(String firmName) {
		this.firmName = firmName;
	}

	public String getFirmNumber() {
		return firmNumber;
	}

	public void setFirmNumber(String firmNumber) {
		this.firmNumber = firmNumber;
	}

	public String getBarNumber() {
		return barNumber;
	}

	public void setBarNumber(String barNumber) {
		this.barNumber = barNumber;
	}

    public List<Access> getAccess() {
        return access;
    }

    public void setAccess(List<Access> access) {
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

    public Date getCurrentLoginTime() {
        return currentLoginTime;
    }

    public void setCurrentLoginTime(Date currentLoginTime) {
        this.currentLoginTime = currentLoginTime;
    }

    public List<Site> getSites() {
        return sites;
    }

    public void setSites(List<Site> sites) {
        this.sites = sites;
    }

    public void setSite(Site site) {
        if (this.sites != null) {
            this.sites.add(site);
        } else {
            this.sites =  new LinkedList<Site>();
            this.sites.add(site);
        }
    }

    public boolean isCreditCardActive() {
        return creditCardActive;
    }

    public void setCreditCardActive(boolean creditCardActive) {
        this.creditCardActive = creditCardActive;
    }

    public boolean isPayedUser() {
        return payedUser;
    }

    public void setPayedUser(boolean payedUser) {
        this.payedUser = payedUser;
    }

    public boolean isPaymentDue() {
        return paymentDue;
    }

    public void setPaymentDue(boolean paymentDue) {
        this.paymentDue = paymentDue;
    }

    public boolean isCardAvailable() {
        return cardAvailable;
    }

    public void setCardAvailable(boolean cardAvailable) {
        this.cardAvailable = cardAvailable;
    }


    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    public List<UserAccess> getUserAccessList() {
        return userAccessList;
    }

    public void setUserAccessList(List<UserAccess> userAccessList) {
        this.userAccessList = userAccessList;
    }

    public List<UserAccount> getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(List<UserAccount> userAccount) {
        this.userAccount = userAccount;
    }

    @Override
    @JsonGetter("active")
    public boolean isEnabled() {
        return active;
    }

    public String getRegisteredNode() {
        return registeredNode;
    }

    public void setRegisteredNode(String registeredNode) {
        this.registeredNode = registeredNode;
    }

    /* This is required for JSON Mapper */
    public void setAuthorities() {
        if (this.getAccess() != null && !this.getAccess().isEmpty()) {
            List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
            for(Access access : this.getAccess()) {
                GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(access.getCode());
                authorities.add(grantedAuthority);
            }
        }
    }

    /* This is needed for Spring Security */
    @Override
    public Collection getAuthorities() {
        if (this.getAccess() != null && !this.getAccess().isEmpty()) {
            List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
            for(Access access : this.getAccess()) {
                GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(access.getCode());
                authorities.add(grantedAuthority);
            }
            return authorities;
        }
        return null;
    }

    public Date getAccountDeletionDate() {
		return accountDeletionDate;
	}

	public void setAccountDeletionDate(Date accountDeletionDate) {
		this.accountDeletionDate = accountDeletionDate;
	}

	public boolean isPurchasedDocuments() {
		return purchasedDocuments;
	}

	public void setPurchasedDocuments(boolean purchasedDocuments) {
		this.purchasedDocuments = purchasedDocuments;
	}


	public boolean isEmailNotificationSent() {
		return isEmailNotificationSent;
	}

	public void setEmailNotificationSent(boolean isEmailNotificationSent) {
		this.isEmailNotificationSent = isEmailNotificationSent;
	}

	public boolean isAuthorizationPending() {
		return isAuthorizationPending;
	}

	public void setAuthorizationPending(boolean isAuthorizationPending) {
		this.isAuthorizationPending = isAuthorizationPending;
	}

	public String getLastLoginTimeString(){
		if(this.getLastLoginTime() != null){
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa z");
			return df.format(this.getLastLoginTime());
		}
		return "";
	}

	public String getCurrentLoginTimeString(){
		if(this.getCurrentLoginTime() != null){
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa z");
			return df.format(this.getCurrentLoginTime());
		}
		return "";
	}


	public String getDateTimeCreatedString(){
		if(this.getCreatedDate() != null){
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm aaa z");
			return df.format(this.getCreatedDate());
		}
		return "";
	}


	@Override
	public String toString() {
		return "User [username=" + username + ", existingPassword="
				+ existingPassword + ", password=" + password + ", firstName="
				+ firstName + ", lastName=" + lastName + ", addressLine1="
				+ addressLine1 + ", addressLine2=" + addressLine2 + ", city="
				+ city + ", state=" + state + ", zip=" + zip + ", phone="
				+ phone + ", createdIp=" + createdIp + ", accountNonExpired="
				+ accountNonExpired + ", accountNonLocked=" + accountNonLocked
				+ ", credentialsNonExpired=" + credentialsNonExpired
				+ ", lastLoginTime=" + lastLoginTime
				+ ", currentLoginTime="+ currentLoginTime
				+ ", firmName=" + firmName
				+ ", firmNumber=" + firmNumber
				+ ", barNumber=" + barNumber
				+ ", access=" + access + ", sites=" + sites
				+ ", creditCard=" + creditCard + ", userAccessList="
				+ userAccessList + ", creditCardActive=" + creditCardActive
				+ ", payedUser=" + payedUser + ", paymentDue=" + paymentDue
				+ ", cardAvailable=" + cardAvailable + ", userAccount="
				+ userAccount + ", registeredNode=" + registeredNode
				+ ", waitingForAuthorization=" + waitingForAuthorization
				+ ", purchasedDocuments=" + purchasedDocuments
				+ ", accessOverridden=" + accessOverridden + ", acceptedTerms="
				+ acceptedTerms + ", terms=" + terms + ", accountDeletionDate="
				+ accountDeletionDate + ", id=" + id + ", createdDate="
				+ createdDate + ", modifiedDate=" + modifiedDate
				+ ", modifiedBy=" + modifiedBy + ", createdBy=" + createdBy
				+ ", active=" + active + "]";
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }

}