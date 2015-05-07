package com.fdt.security.dto;

import com.fdt.common.dto.AbstractBaseDTO;


/**
 * This class is an inbound request to Delete Firm Level User  service.
 *
 * @author APatel
 *
 */
public class RemoveFirmLevelAccessRequestDTO extends AbstractBaseDTO {

	private static final long serialVersionUID = -620555462471244L;

	private String firmUserName;

	private boolean isSendNotification;

	private long userAccessId;

	private String modifiedBy;

	private String comments;


	public String getFirmUserName() {
		return firmUserName;
	}

	public void setFirmUserName(String firmUserName) {
		this.firmUserName = firmUserName;
	}

	public long getUserAccessId() {
		return userAccessId;
	}

	public void setUserAccessId(long userAccessId) {
		this.userAccessId = userAccessId;
	}

	public boolean isSendNotification() {
		return isSendNotification;
	}

	public void setSendNotification(boolean isSendNotification) {
		this.isSendNotification = isSendNotification;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	@Override
	public String toString() {
		return "DeleteFirmUserRequestDTO ["
				+ "firmUserName=" + firmUserName
				+ "userAccessId=" + userAccessId
				+ "modifiedDate=" + modifiedDate
				+ "active=" + active
				+ "createdBy=" + createdBy
				+ "modifiedBy=" + modifiedBy
				+ "comments=" + comments
				+ "machineName=" + machineName
				+ "]";
	}


}