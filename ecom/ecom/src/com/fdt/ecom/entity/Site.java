package com.fdt.ecom.entity;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;


import com.fdt.common.entity.AbstractBaseEntity;
import com.fdt.otctx.entity.MagensaInfo;
import com.fdt.security.entity.Access;


@Entity
@Table(name="ECOMM_SITE")
public class Site extends AbstractBaseEntity {

    private static final long serialVersionUID = -4632454677095318492L;

    @Column(name="NAME", nullable = false)
    private String name = null;

    @Column(name="DESCRIPTION")
    private String description = null;

    @Column(name="COUNTY", nullable = false)
    private String county = null;

    @Column(name="STATE")
    private String state = null;

    @Column(name="AUTOACTIVATE", nullable = false)
    private boolean autoActivate = false;

    @Column(name="TIMEZONE")
    private String timeZone = null;

    @Column(name="SUBSCRIPTION_VALIDATION_TEXT", nullable = false)
    private String subscriptionValidationText = null;


    @Column(name="ENABLE_MICRO_TX_OTC", nullable = false)
    private boolean isEnableMicroTxOTC = false;

    @Column(name="ENABLE_MICRO_TX_WEB", nullable = false)
    private boolean isEnableMicroTxWeb = false;

    @Column(name="NAME_ON_CHECK", nullable = false)
    private String nameOnCheck = null;

    @Column(name="SEARCH_DAY_THRESHOLD")
    private Long searchDayThreshold = null;

    @Column(name="IS_FIRM_NUMBER_REQUIRED", nullable = false)
    @Type(type="yes_no")
    private boolean firmNumberRequired = false;

    @Column(name="IS_BAR_NUMBER_REQUIRED", nullable = false)
    @Type(type="yes_no")
    private boolean barNumberRequired = false;

    @Column(name="IS_FREE_SITE", nullable = false)
    @Type(type="yes_no")
    private boolean freeSite = false;

    @Column(name="IS_SUM_TXAMOUNT_PLUS_SERVICEFEE", nullable = false)
    @Type(type="yes_no")
    private boolean sumTxamountPlusServiceFee = false;

    @Column(name="IS_LOCATION_ENABLED", nullable = false)
    @Type(type="yes_no")
    private boolean isLocationEnabled = false;

    @Transient
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="NODE_ID", unique=true, nullable=false)
    private Node node = null;

    @Transient
    @OneToMany(fetch=FetchType.LAZY, mappedBy="site")
    private List<Merchant> merchantList = null;

    @Transient
    @OneToOne(fetch=FetchType.LAZY, mappedBy="site")
    private CreditUsageFee cardUsageFee = null;

    @Transient
    @OneToOne(fetch=FetchType.LAZY, mappedBy="site")
    private WebPaymentFee webPaymentFee = null;

    @Transient
    @OneToOne(fetch=FetchType.LAZY, mappedBy="site")
    private Term term = null;

    @Transient
    @OneToOne(fetch=FetchType.LAZY, mappedBy="site")
    private MagensaInfo magensaInfo = null;

    @Transient
    private BankDetails bankDetails = null;

    @Transient
    private  BankDetails customerBankDetails = null;

    @OneToMany
    private List<Access> access = null;

    @OneToMany
    @Transient
    private List<SiteAccess> siteAccess = null;

    @Column(name="CHECK_HOLD_PERIOD", nullable = false)
    private Long checkHoldingPeriod = null;

    @Column(name="ACH_HOLD_PERIOD", nullable = false)
    private Long achHoldingPeriod = null;

    @Transient
    private boolean isAchEnabled = false;


    public List<SiteAccess> getSiteAccess() {
        return siteAccess;
    }

    public void setSiteAccess(List<SiteAccess> siteAccess) {
        this.siteAccess = siteAccess;
    }

    public Long getAchHoldingPeriod() {
        return achHoldingPeriod;
    }

    public void setAchHoldingPeriod(Long achHoldingPeriod) {
        this.achHoldingPeriod = achHoldingPeriod;
    }

    public boolean isAchEnabled() {
        return isAchEnabled;
    }

    public void setAchEnabled(boolean isAchEnabled) {
        this.isAchEnabled = isAchEnabled;
    }

    public BankDetails getCustomerBankDetails() {
        return customerBankDetails;
    }

    public void setCustomerBankDetails(BankDetails customerBankDetails) {
        this.customerBankDetails = customerBankDetails;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isAutoActivate() {
        return autoActivate;
    }

    public void setAutoActivate(boolean autoActivate) {
        this.autoActivate = autoActivate;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getSubscriptionValidationText() {
        return subscriptionValidationText;
    }

    public void setSubscriptionValidationText(String subscriptionValidationText) {
        this.subscriptionValidationText = subscriptionValidationText;
    }


	public Merchant getMerchant() {
        if (this.merchantList != null) {
            for (Merchant merchant : this.merchantList) {
                if (!merchant.isMicroPaymentAccount()) {
                    return merchant;
                }
            }
        }
        return null;
    }

    public Merchant getMicroMerchant() {
        if (this.merchantList != null) {
            for (Merchant merchant : this.merchantList) {
                if (merchant.isMicroPaymentAccount()) {
                    return merchant;
                }
            }
        }
        return null;
    }

    public void addMerchant(Merchant merchant) {
        if (this.merchantList == null) {
            this.merchantList = new LinkedList<Merchant>();
        }
        this.merchantList.add(merchant);
    }

    public List<Merchant> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchant> merchantList) {
		this.merchantList = merchantList;
	}

	public List<Access> getAccess() {
        return access;
    }

    public void setAccess(List<Access> access) {
        this.access = access;
    }

    public CreditUsageFee getCardUsageFee() {
        return cardUsageFee;
    }

    public void setCardUsageFee(CreditUsageFee cardUsageFee) {
        this.cardUsageFee = cardUsageFee;
    }

    public WebPaymentFee getWebPaymentFee() {
        return webPaymentFee;
    }

    public void setWebPaymentFee(WebPaymentFee webPaymentFee) {
        this.webPaymentFee = webPaymentFee;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public MagensaInfo getMagensaInfo() {
        return magensaInfo;
    }

    public void setMagensaInfo(MagensaInfo magensaInfo) {
        this.magensaInfo = magensaInfo;
    }

	public boolean isFirmNumberRequired() {
		return firmNumberRequired;
	}

	public void setFirmNumberRequired(boolean firmNumberRequired) {
		this.firmNumberRequired = firmNumberRequired;
	}

	public boolean isBarNumberRequired() {
		return barNumberRequired;
	}

	public void setBarNumberRequired(boolean barNumberRequired) {
		this.barNumberRequired = barNumberRequired;
	}


    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }


    public boolean isEnableMicroTxOTC() {
        return isEnableMicroTxOTC;
    }

    public void setEnableMicroTxOTC(boolean isEnableMicroTxOTC) {
        this.isEnableMicroTxOTC = isEnableMicroTxOTC;
    }

    public boolean isEnableMicroTxWeb() {
        return isEnableMicroTxWeb;
    }

    public void setEnableMicroTxWeb(boolean isEnableMicroTxWeb) {
        this.isEnableMicroTxWeb = isEnableMicroTxWeb;
    }

    public BankDetails getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(BankDetails bankDetails) {
        this.bankDetails = bankDetails;
    }

    public String getNameOnCheck() {
        return nameOnCheck;
    }

    public void setNameOnCheck(String nameOnCheck) {
        this.nameOnCheck = nameOnCheck;
    }

    public Long getCheckHoldingPeriod() {
        return checkHoldingPeriod;
    }

    public void setCheckHoldingPeriod(Long checkHoldingPeriod) {
        this.checkHoldingPeriod = checkHoldingPeriod;
    }

    public Long getSearchDayThreshold() {
        return searchDayThreshold;
    }

    public void setSearchDayThreshold(Long searchDayThreshold) {
        this.searchDayThreshold = searchDayThreshold;
    }
    
	public boolean isFreeSite() {
		return freeSite;
	}

	public void setFreeSite(boolean freeSite) {
		this.freeSite = freeSite;
	}

	public boolean isSumTxamountPlusServiceFee() {
		return sumTxamountPlusServiceFee;
	}

	public void setSumTxamountPlusServiceFee(boolean sumTxamountPlusServiceFee) {
		this.sumTxamountPlusServiceFee = sumTxamountPlusServiceFee;
	}
	

	public boolean isLocationEnabled() {
		return isLocationEnabled;
	}

	public void setLocationEnabled(boolean isLocationEnabled) {
		this.isLocationEnabled = isLocationEnabled;
	}

	@Override
	public String toString() {
		return "Site [name=" + name + ", description=" + description
				+ ", county=" + county + ", state=" + state + ", autoActivate="
				+ autoActivate + ", timeZone=" + timeZone
				+ ", subscriptionValidationText=" + subscriptionValidationText
				+ ", isEnableMicroTxOTC=" + isEnableMicroTxOTC
				+ ", isEnableMicroTxWeb=" + isEnableMicroTxWeb
				+ ", nameOnCheck=" + nameOnCheck + ", searchDayThreshold="
				+ searchDayThreshold + ", firmNumberRequired="
				+ firmNumberRequired + ", barNumberRequired="
				+ barNumberRequired + ", freeSite=" + freeSite
				+ ", sumTxamountPlusServiceFee=" + sumTxamountPlusServiceFee
				+ ", isLocationEnabled=" + isLocationEnabled + ", node=" + node
				+ ", merchant=" + merchantList + ", cardUsageFee=" + cardUsageFee
				+ ", webPaymentFee=" + webPaymentFee + ", term=" + term
				+ ", magensaInfo=" + magensaInfo + ", bankDetails="
				+ bankDetails + ", customerBankDetails=" + customerBankDetails
				+ ", access=" + access + ", siteAccess=" + siteAccess
				+ ", checkHoldingPeriod=" + checkHoldingPeriod
				+ ", achHoldingPeriod=" + achHoldingPeriod + ", isAchEnabled="
				+ isAchEnabled + ", id=" + id + ", createdDate=" + createdDate
				+ ", modifiedDate=" + modifiedDate + ", modifiedBy="
				+ modifiedBy + ", createdBy=" + createdBy + ", active="
				+ active + "]";
	}


}
