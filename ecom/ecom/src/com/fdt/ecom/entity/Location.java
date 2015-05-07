package com.fdt.ecom.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fdt.common.entity.AbstractBaseEntity;

@Entity
@Table(name="ECOMM_LOCATION")
public class Location extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name="DESCRIPTION", nullable = false)
    private String description = null;

	@Column(name="LOCATION_CODE", nullable = false)
    private String locationCode = null;

	@Column(name="STATE_DESCRIPTION")
    private String stateDescription = null;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SITE_ID", insertable = false, updatable = false, nullable = false)
    private Site site;
	
	@Column(name="SEAL_OF_AUTHENTICITY", columnDefinition="varbinary(max)")
    private byte[] sealOfAuthenticity = null;
	
	@Column(name="SIGNATURE", columnDefinition="varbinary(max)")
    private byte[] signature = null;

	@Column(name="CLERK_NAME")
    private String clerkName = null;

	@Column(name="DESIGNATION")
    private String designation = null;

	
	@Column(name="NOTE_OF_AUTHENTICITY")
    private String noteOfAuthenticity = null;

	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStateDescription() {
		return stateDescription;
	}

	public void setStateDescription(String stateDescription) {
		this.stateDescription = stateDescription;
	}

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public byte[] getSealOfAuthenticity() {
		return sealOfAuthenticity;
	}

	public void setSealOfAuthenticity(byte[] sealOfAuthenticity) {
		this.sealOfAuthenticity = sealOfAuthenticity;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	public String getClerkName() {
		return clerkName;
	}

	public void setClerkName(String clerkName) {
		this.clerkName = clerkName;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getNoteOfAuthenticity() {
		return noteOfAuthenticity;
	}

	public void setNoteOfAuthenticity(String noteOfAuthenticity) {
		this.noteOfAuthenticity = noteOfAuthenticity;
	}

	public String getLocationCode() {
		return locationCode;
	}

	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}

	@Override
	public String toString() {
		return "Site ["
				+ "locationName=" + description
				+ " stateDescription=" + stateDescription
				+ ", site=" + site
				+ ", clerkName=" + clerkName
				+ ", designation=" + designation
				+ ", noteOfAuthenticity=" + noteOfAuthenticity
				+ ", id=" + id
				+ ", locationCode=" + locationCode
				+ ", createdDate=" + createdDate
                + ", modifiedDate=" + modifiedDate
                + ", modifiedBy=" + modifiedBy
                + ", createdBy=" + createdBy
                + ", active=" + active
                + "]";
	}
}
