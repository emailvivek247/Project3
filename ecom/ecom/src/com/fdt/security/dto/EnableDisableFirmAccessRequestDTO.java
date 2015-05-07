package com.fdt.security.dto;

import com.fdt.common.dto.AbstractBaseDTO;


/**
 * This class is an inbound request to Firm Level User Lock/Unlock service.
 * 
 * @author APatel
 *
 */
public class EnableDisableFirmAccessRequestDTO extends AbstractBaseDTO {


	private static final long serialVersionUID = -4533407381723874424L;

	private String firmUserName;

	private boolean isEnable;
    
	private String comments;
    
    private Long userAccessId = null;
    

	public String getFirmUserName() {
		return firmUserName;
	}

	public void setFirmUserName(String firmUserName) {
		this.firmUserName = firmUserName;
	}


	public boolean isEnable() {
		return isEnable;
	}

	public void setEnable(boolean isEnable) {
		this.isEnable = isEnable;
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

	public Long getUserAccessId() {
		return userAccessId;
	}

	public void setUserAccessId(Long userAccessId) {
		this.userAccessId = userAccessId;
	}
	

	@Override
	public String toString() {
		return "LockUnlockFirmUserRequestDTO ["
				+ "firmUserName=" + firmUserName  
				+ "isLock=" + isEnable  
				+ "comments=" + comments  
				+ "userAccessId=" + userAccessId  
				+ "modifiedDate=" + modifiedDate  
				+ "active=" + active  
				+ "createdBy=" + createdBy  
				+ "machineName=" + machineName  
				+ "]";
	}
	
    

}