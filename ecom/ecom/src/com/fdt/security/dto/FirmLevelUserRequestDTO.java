package com.fdt.security.dto;

import com.fdt.common.dto.AbstractBaseDTO;
import com.fdt.security.entity.User;

/**
 * This class is an inbound request to Firm Level User Add/Update services.
 * 
 * @author APatel
 *
 */
public class FirmLevelUserRequestDTO extends AbstractBaseDTO {


	private static final long serialVersionUID = 7654976779922799948L;

	private User user;
    
    private Long accessId;
    
    private String nodeName;
    
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}


	public Long getAccessId() {
		return accessId;
	}

	public void setAccessId(Long accessId) {
		this.accessId = accessId;
	}
	
	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	@Override
	public String toString() {
		return "FirmLevelUserRequestDTO ["
				+ "user=" + user  
				+ "accessId=" + accessId  
				+ "modifiedDate=" + modifiedDate  
				+ "active=" + active  
				+ "createdBy=" + createdBy  
				+ "machineName=" + machineName
				+ "nodeName=" + nodeName  
				+ "]";
	}
	
    

}