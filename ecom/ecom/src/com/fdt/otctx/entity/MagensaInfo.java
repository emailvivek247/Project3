package com.fdt.otctx.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fdt.common.entity.AbstractBaseEntity;
import com.fdt.ecom.entity.Site;

@Entity
@Table(name = "ECOMM_MAGENSAINFO")
public class MagensaInfo extends AbstractBaseEntity {

    private static final long serialVersionUID = -2248558667915906072L;

    @Transient
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="SITE_ID", nullable=false, unique=true)
    private Site site = null;

    @Column(name="HOST_ID", nullable = false)
    private String hostId = null;

    @Column(name="HOST_PASSWORD", nullable = false)
    private String hostPassword = null;

    @Column(name="REGISTEREDBY", nullable = false)
    private String registeredBy = null;

    @Column(name="ENCRYPTION_BLOCK_TYPE", nullable = false)
    private String encryptionBlockType = null;

    @Column(name="CARD_TYPE", nullable = false)
    private String cardType   = null;

    @Column(name="OUTPUT_FORMAT_CODE", nullable = false)
    private String outputFormatCode  = null;

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getHostPassword() {
        return hostPassword;
    }

    public void setHostPassword(String hostPassword) {
        this.hostPassword = hostPassword;
    }

    public String getRegisteredBy() {
        return registeredBy;
    }

    public void setRegisteredBy(String registeredBy) {
        this.registeredBy = registeredBy;
    }

    public String getEncryptionBlockType() {
        return encryptionBlockType;
    }

    public void setEncryptionBlockType(String encryptionBlockType) {
        this.encryptionBlockType = encryptionBlockType;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getOutputFormatCode() {
        return outputFormatCode;
    }

    public void setOutputFormatCode(String outputFormatCode) {
        this.outputFormatCode = outputFormatCode;
    }

    @Override
    public String toString() {
        return "MagensaInfo [site=" + site + ", hostId=" + hostId
                + ", hostPassword=" + hostPassword + ", registeredBy="
                + registeredBy + ", encryptionBlockType=" + encryptionBlockType
                + ", cardType=" + cardType + ", outputFormatCode="
                + outputFormatCode + ", id=" + id + ", createdDate="
                + createdDate + ", modifiedDate=" + modifiedDate
                + ", modifiedBy=" + modifiedBy + ", createdBy=" + createdBy
                + ", active=" + active + "]";
    }
}
